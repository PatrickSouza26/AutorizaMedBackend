package com.autorizamed.api.autorizacao.service;

import com.autorizamed.api.autorizacao.dto.request.SolicitarGuiaRequest;
import com.autorizamed.api.autorizacao.dto.response.GuiaResponse;
import com.autorizamed.api.autorizacao.entity.AnexoGuia;
import com.autorizamed.api.autorizacao.entity.GuiaAutorizacao;
import com.autorizamed.api.autorizacao.enums.CaraterSolicitacao;
import com.autorizamed.api.autorizacao.enums.StatusGuia;
import com.autorizamed.api.autorizacao.model.GuiaMapper;
import com.autorizamed.api.autorizacao.repository.AnexoGuiaRepository;
import com.autorizamed.api.autorizacao.repository.GuiaAutorizacaoRepository;
import com.autorizamed.api.elegibilidade.entity.Beneficiario;
import com.autorizamed.api.elegibilidade.entity.Prestador;
import com.autorizamed.api.elegibilidade.entity.Procedimento;
import com.autorizamed.api.elegibilidade.entity.RedeCredenciada;
import com.autorizamed.api.elegibilidade.repository.BeneficiarioRepository;
import com.autorizamed.api.elegibilidade.repository.PrestadorRepository;
import com.autorizamed.api.elegibilidade.repository.ProcedimentoRepository;
import com.autorizamed.api.elegibilidade.repository.RedeCredenciadaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuiaAutorizacaoService {

    private final GuiaAutorizacaoRepository guiaRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    private final PrestadorRepository prestadorRepository;
    private final ProcedimentoRepository procedimentoRepository;
    private final RedeCredenciadaRepository redeCredenciadaRepository;
    private final AnexoGuiaRepository anexoRepository;
    private final GuiaMapper mapper;

    @Transactional
    public GuiaResponse solicitar(SolicitarGuiaRequest request) {

        Beneficiario paciente = beneficiarioRepository.findByCarteirinha(request.carteirinhaBeneficiario())
                .orElseThrow(() -> new EntityNotFoundException("Erro crítico: Paciente não existe na base de dados."));

        Prestador prestador = prestadorRepository.findByDocumento(request.documentoPrestador())
                .orElseThrow(() -> new EntityNotFoundException("Erro crítico: Prestador não existe na base de dados."));

        List<Procedimento> procedimentosSolicitados = procedimentoRepository.findAllByCodigoTussIn(request.codigosTuss());

        ResultadoValidacao validacao = validarRegrasDeNegocio(paciente, prestador, procedimentosSolicitados, request.codigosTuss().size(), request.caraterSolicitacao());

        Long proximoNumero = guiaRepository.getProximoNumeroGuia();
        String numeroGerado = String.format("%d%06d", Year.now().getValue(), proximoNumero);

        LocalDateTime dataLimite = request.caraterSolicitacao() == CaraterSolicitacao.URGENCIA
                ? LocalDateTime.now().plusHours(3)
                : LocalDateTime.now().plusDays(5);

        GuiaAutorizacao guia = GuiaAutorizacao.builder()
                .numeroGuia(numeroGerado)
                .beneficiario(paciente)
                .prestador(prestador)
                .procedimentos(procedimentosSolicitados)
                .status(validacao.statusFinal)
                .motivoNegativa(validacao.motivoNegativa)
                .alertaSistema(validacao.alertaSistema)
                .caraterSolicitacao(request.caraterSolicitacao())
                .dataLimiteAprovacao(dataLimite)
                .dataSolicitacao(LocalDateTime.now())
                .indicacaoClinica(request.indicacaoClinica())
                .build();

        return mapper.converterParaResponse(guiaRepository.save(guia));
    }

    private record ResultadoValidacao(StatusGuia statusFinal, String motivoNegativa, String alertaSistema) {}

    private ResultadoValidacao validarRegrasDeNegocio(Beneficiario paciente, Prestador prestador, List<Procedimento> procedimentos, int totalCodigosEnviados, CaraterSolicitacao caraterSolicitacao) {

        if (!paciente.isPlanoAtivo()) return new ResultadoValidacao(StatusGuia.NEGADA, "O plano do paciente está inativo.", null);
        if (!prestador.isAtivo()) return new ResultadoValidacao(StatusGuia.NEGADA, "O prestador está inativo no sistema.", null);
        if (procedimentos.isEmpty() || procedimentos.size() != totalCodigosEnviados) return new ResultadoValidacao(StatusGuia.NEGADA, "Um ou mais códigos TUSS informados são inválidos.", null);

        boolean precisaDeAuditoria = false;
        String alerta = null;
        LocalDateTime dataCorteFrequencia = LocalDateTime.now().minusDays(30);

        for (Procedimento proc : procedimentos) {
            if (!proc.isAtivo()) {
                return new ResultadoValidacao(StatusGuia.NEGADA, "O procedimento " + proc.getCodigoTuss() + " está inativo.", null);
            }

            Optional<RedeCredenciada> contratoOpt = redeCredenciadaRepository.findByPrestadorIdAndProcedimentoId(prestador.getId(), proc.getId());
            if (contratoOpt.isEmpty()) {
                return new ResultadoValidacao(StatusGuia.NEGADA, "O hospital não é credenciado para o procedimento " + proc.getCodigoTuss() + ".", null);
            }

            RedeCredenciada contrato = contratoOpt.get();
            if (!contrato.isAtivo()) {
                return new ResultadoValidacao(StatusGuia.NEGADA, "O contrato para o procedimento " + proc.getCodigoTuss() + " está suspenso.", null);
            }

            if (!contrato.getPlanosAceitos().contains(paciente.getTipoPlano())) {
                return new ResultadoValidacao(StatusGuia.NEGADA, "O prestador não atende a categoria de plano " + paciente.getTipoPlano() + " para o TUSS " + proc.getCodigoTuss() + ".", null);
            }

            if (proc.isRequerAutorizacao()) {
                precisaDeAuditoria = true;
            }

            if (caraterSolicitacao == CaraterSolicitacao.ELETIVA) {
                long repeticoes = guiaRepository.contarProcedimentoRecente(paciente.getId(), proc.getId(), dataCorteFrequencia);
                if (repeticoes > 0) {
                    precisaDeAuditoria = true;
                    alerta = "Paciente já realizou o exame " + proc.getCodigoTuss() + " nos últimos 30 dias.";
                }
            }
        }

        StatusGuia status = precisaDeAuditoria ? StatusGuia.EM_ANALISE : StatusGuia.AUTORIZADA;
        return new ResultadoValidacao(status, null, alerta);
    }

    @Transactional
    public void anexarArquivo(UUID guiaId, MultipartFile arquivo) {
        GuiaAutorizacao guia = guiaRepository.findById(guiaId)
                .orElseThrow(() -> new EntityNotFoundException("Guia não encontrada."));

        try {
            AnexoGuia anexo = AnexoGuia.builder()
                    .nomeArquivo(arquivo.getOriginalFilename())
                    .tipoArquivo(arquivo.getContentType())
                    .dados(arquivo.getBytes())
                    .guia(guia)
                    .build();

            anexoRepository.save(anexo);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar o arquivo anexado.", e);
        }
    }

    @Transactional
    public GuiaResponse responderPendencia(UUID idGuia) {

        GuiaAutorizacao guia = guiaRepository.findById(idGuia)
                .orElseThrow(() -> new EntityNotFoundException("Guia não encontrada."));

        if (guia.getStatus() != StatusGuia.PENDENCIA) {
            throw new IllegalArgumentException("Apenas guias com status PENDENCIA podem ser respondidas.");
        }
        
        guia.setStatus(StatusGuia.PENDENCIA_RESPONDIDA);

        // Aqui você poderia, opcionalmente, limpar o motivoNegativa,
        // ou criar um log dizendo "A clínica enviou a resposta".

        return mapper.converterParaResponse(guiaRepository.save(guia));
    }

    @Transactional(readOnly = true)
    public List<GuiaResponse> listarPorCarteirinha(String carteirinha) {
        List<GuiaAutorizacao> guias = guiaRepository.findByBeneficiarioCarteirinha(carteirinha);
        return guias.stream().map(mapper::converterParaResponse).collect(Collectors.toList());
    }


}
