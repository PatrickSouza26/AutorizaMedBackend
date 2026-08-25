package com.autorizamed.api.autorizacao.service;

import com.autorizamed.api.autorizacao.dto.request.SolicitarGuiaRequest;
import com.autorizamed.api.autorizacao.dto.response.GuiaResponse;
import com.autorizamed.api.autorizacao.entity.AnexoGuia;
import com.autorizamed.api.autorizacao.entity.GuiaAutorizacao;
import com.autorizamed.api.autorizacao.enums.StatusGuia;
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

    @Transactional
    public GuiaResponse solicitar(SolicitarGuiaRequest request) {

        // BUSCA BASE PRA RELACIONAMENTO
        Beneficiario paciente = beneficiarioRepository.findByCarteirinha(request.carteirinhaBeneficiario())
                .orElseThrow(() -> new EntityNotFoundException("Erro crítico: Paciente não existe na base de dados."));

        Prestador prestador = prestadorRepository.findByDocumento(request.documentoPrestador())
                .orElseThrow(() -> new EntityNotFoundException("Erro crítico: Prestador não existe na base de dados."));

        List<Procedimento> procedimentosSolicitados = procedimentoRepository.findAllByCodigoTussIn(request.codigosTuss());

        // VARIÁVEIS PARA CONTROLE DE ESTADO DA GUIA
        StatusGuia statusFinal = StatusGuia.AUTORIZADA;
        String motivoNegativa = null;
        boolean precisaDeAuditoria = false;

        // VALIDAÇÕES
        if (!paciente.isPlanoAtivo()) {
            statusFinal = StatusGuia.NEGADA;
            motivoNegativa = "O plano do paciente está inativo.";
        } else if (!prestador.isAtivo()) {
            statusFinal = StatusGuia.NEGADA;
            motivoNegativa = "O prestador está inativo no sistema.";
        } else if (procedimentosSolicitados.isEmpty() || procedimentosSolicitados.size() != request.codigosTuss().size()) {
            statusFinal = StatusGuia.NEGADA;
            motivoNegativa = "Um ou mais códigos TUSS informados são inválidos ou não existem.";
        } else {

            // LOOP DE ELEGIBILIDADE
            for (Procedimento proc : procedimentosSolicitados) {

                if (!proc.isAtivo()) {
                    statusFinal = StatusGuia.NEGADA;
                    motivoNegativa = "O procedimento " + proc.getCodigoTuss() + " está inativo.";
                    break;
                }

                // BUSCA O CONTRATO E FAZ VALIDAÇÕES
                Optional<RedeCredenciada> contratoOpt = redeCredenciadaRepository.findByPrestadorIdAndProcedimentoId(prestador.getId(), proc.getId());

                if (contratoOpt.isEmpty()) {
                    statusFinal = StatusGuia.NEGADA;
                    motivoNegativa = "O hospital " + prestador.getNome() + " não é credenciado para realizar o procedimento " + proc.getCodigoTuss() + ".";
                    break;
                }

                RedeCredenciada contrato = contratoOpt.get();

                if (!contrato.isAtivo()) {
                    statusFinal = StatusGuia.NEGADA;
                    motivoNegativa = "O contrato para o procedimento " + proc.getCodigoTuss() + " está suspenso.";
                    break;
                }

                if (!contrato.getPlanosAceitos().contains(paciente.getTipoPlano())) {
                    statusFinal = StatusGuia.NEGADA;
                    motivoNegativa = "O prestador realiza o procedimento, mas não atende a categoria de plano " + paciente.getTipoPlano() + " para o TUSS " + proc.getCodigoTuss() + ".";
                    break;
                }

                if (proc.isRequerAutorizacao()) {
                    precisaDeAuditoria = true;
                }
            }
        }

        if (statusFinal != StatusGuia.NEGADA && precisaDeAuditoria) {
            statusFinal = StatusGuia.EM_ANALISE;
        }

        // SALVA A GUIA (SALVA EM TODOS OS CASOS)
        GuiaAutorizacao guia = GuiaAutorizacao.builder()
                .beneficiario(paciente)
                .prestador(prestador)
                .procedimentos(procedimentosSolicitados)
                .status(statusFinal)
                .motivoNegativa(motivoNegativa)
                .dataSolicitacao(LocalDateTime.now())
                .indicacaoClinica(request.indicacaoClinica())
                .build();

        guia = guiaRepository.save(guia);
        return converterParaResponse(guia);
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

    @Transactional(readOnly = true)
    public List<GuiaResponse> listarPorCarteirinha(String carteirinha) {
        List<GuiaAutorizacao> guias = guiaRepository.findByBeneficiarioCarteirinha(carteirinha);
        return guias.stream().map(this::converterParaResponse).collect(Collectors.toList());
    }

    private GuiaResponse converterParaResponse(GuiaAutorizacao guia) {
        List<String> nomesExames = guia.getProcedimentos().stream()
                .map(Procedimento::getDescricao)
                .collect(Collectors.toList());

        return new GuiaResponse(
                guia.getId(),
                guia.getBeneficiario().getNome(),
                guia.getPrestador().getNome(),
                nomesExames,
                guia.getStatus(),
                guia.getMotivoNegativa(),
                guia.getDataSolicitacao()
        );
    }
}
