package com.autorizamed.api.autorizacao.service;

import com.autorizamed.api.auditoria.annotation.AuditCreate;
import com.autorizamed.api.auditoria.annotation.AuditUpdate;
import com.autorizamed.api.auditoria.enums.AcaoAuditoria;
import com.autorizamed.api.autorizacao.dto.request.SolicitarGuiaRequest;
import com.autorizamed.api.autorizacao.dto.response.AnexoDownloadDTO;
import com.autorizamed.api.autorizacao.dto.response.GuiaResponse;
import com.autorizamed.api.autorizacao.entity.AnexoConteudo;
import com.autorizamed.api.autorizacao.entity.AnexoGuia;
import com.autorizamed.api.autorizacao.entity.GuiaAutorizacao;
import com.autorizamed.api.autorizacao.entity.ProcedimentoGuia;
import com.autorizamed.api.autorizacao.entity.ProfissionalSaude;
import com.autorizamed.api.autorizacao.enums.CaraterSolicitacao;
import com.autorizamed.api.autorizacao.enums.StatusGuia;
import com.autorizamed.api.autorizacao.event.PendenciaRespondidaEvent;
import com.autorizamed.api.autorizacao.mapper.CboMapper;
import com.autorizamed.api.autorizacao.mapper.GuiaMapper;
import com.autorizamed.api.autorizacao.repository.AnexoGuiaRepository;
import com.autorizamed.api.autorizacao.repository.GuiaAutorizacaoRepository;
import com.autorizamed.api.autorizacao.repository.ProfissionalSaudeRepository;
import com.autorizamed.api.elegibilidade.entity.Beneficiario;
import com.autorizamed.api.elegibilidade.entity.Funcionario;
import com.autorizamed.api.elegibilidade.entity.Prestador;
import com.autorizamed.api.elegibilidade.entity.Procedimento;
import com.autorizamed.api.elegibilidade.entity.RedeCredenciada;
import com.autorizamed.api.elegibilidade.repository.BeneficiarioRepository;
import com.autorizamed.api.elegibilidade.repository.FuncionarioRepository;
import com.autorizamed.api.elegibilidade.repository.PrestadorRepository;
import com.autorizamed.api.elegibilidade.repository.ProcedimentoRepository;
import com.autorizamed.api.elegibilidade.repository.RedeCredenciadaRepository;
import com.autorizamed.api.notificacao.enums.TipoDestinatario;
import com.autorizamed.api.notificacao.service.AvisoService;
import com.autorizamed.api.seguranca.entity.Usuario;
import com.autorizamed.api.seguranca.enums.RoleUsuario;
import com.autorizamed.api.seguranca.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
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
    private final FuncionarioRepository funcionarioRepository;
    private final ProcedimentoRepository procedimentoRepository;
    private final RedeCredenciadaRepository redeCredenciadaRepository;
    private final AnexoGuiaRepository anexoRepository;
    private final ProfissionalSaudeRepository profissionalSaudeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    @AuditCreate(entidadeTipo = GuiaAutorizacao.class, acao = AcaoAuditoria.GUIA_CRIADA)
    public GuiaResponse solicitar(SolicitarGuiaRequest request) {

        Beneficiario paciente = beneficiarioRepository.findByCarteirinha(request.carteirinhaBeneficiario())
                .orElseThrow(() -> new EntityNotFoundException("Erro crítico: Paciente não existe na base de dados."));

        Prestador prestador = prestadorRepository.findById(request.prestadorId())
                .orElseThrow(() -> new EntityNotFoundException("Erro crítico: Prestador não existe na base de dados."));

        Funcionario funcionario = null;
        if (request.funcionarioId() != null) {
            funcionario = funcionarioRepository.findById(request.funcionarioId())
                    .orElseThrow(() -> new EntityNotFoundException("Erro crítico: Funcionário não existe na base de dados."));
        }

        ProfissionalSaude solicitante = profissionalSaudeRepository.findByNumeroConselho(request.profissionalSolicitante().numeroConselho())
                .orElseGet(() -> {
                    ProfissionalSaude novoProfissional = ProfissionalSaude.builder()
                            .nome(request.profissionalSolicitante().nome())
                            .numeroConselho(request.profissionalSolicitante().numeroConselho())
                            .siglaConselho(request.profissionalSolicitante().siglaConselho())
                            .ufConselho(request.profissionalSolicitante().ufConselho())
                            .cbo(CboMapper.converteDto(request.profissionalSolicitante().cbo()))
                            .build();
                    return profissionalSaudeRepository.save(novoProfissional);
                });

        ProfissionalSaude executante = null;
        if (request.profissionalExecutante() != null) {
            executante = profissionalSaudeRepository.findByNumeroConselho(request.profissionalExecutante().numeroConselho())
                    .orElseGet(() -> {
                        ProfissionalSaude novoProfissional = ProfissionalSaude.builder()
                                .nome(request.profissionalExecutante().nome())
                                .numeroConselho(request.profissionalExecutante().numeroConselho())
                                .siglaConselho(request.profissionalExecutante().siglaConselho())
                                .ufConselho(request.profissionalExecutante().ufConselho())
                                .cbo(CboMapper.converteDto(request.profissionalExecutante().cbo()))
                                .build();
                        return profissionalSaudeRepository.save(novoProfissional);
                    });
        }

        List<String> codigosEnviados = request.procedimentos().stream()
                .map(SolicitarGuiaRequest.ItemProcedimentoRequest::codigoTuss)
                .collect(Collectors.toList());

        List<Procedimento> procedimentosEncontrados = procedimentoRepository.findAllByCodigoTussIn(codigosEnviados);

        if (procedimentosEncontrados.size() != codigosEnviados.size()) {
            throw new EntityNotFoundException("Um ou mais códigos TUSS informados não existem no sistema.");
        }

        ResultadoValidacao validacao = validarRegrasDeNegocio(paciente, prestador, procedimentosEncontrados, codigosEnviados.size(), request.caraterSolicitacao());

        Long proximoNumero = guiaRepository.getProximoNumeroGuia();
        String numeroGerado = String.format("%d%06d", Year.now().getValue(), proximoNumero);

        LocalDateTime dataLimite = request.caraterSolicitacao() == CaraterSolicitacao.URGENCIA
                ? LocalDateTime.now().plusHours(3)
                : LocalDateTime.now().plusDays(5);

        GuiaAutorizacao guia = GuiaAutorizacao.builder()
                .numeroGuia(numeroGerado)
                .beneficiario(paciente)
                .prestador(prestador)
                .funcionario(funcionario)
                .profissionalSolicitante(solicitante)
                .profissionalExecutante(executante)
                .status(validacao.statusFinal())
                .motivoNegativa(validacao.motivoNegativa())
                .alertaSistema(validacao.alertaSistema())
                .caraterSolicitacao(request.caraterSolicitacao())
                .atendimentoRn(request.atendimentoRn())
                .declaracaoAcidente(request.declaracaoAcidente())
                .dataLimiteAprovacao(dataLimite)
                .dataSolicitacao(LocalDateTime.now())
                .indicacaoClinica(request.indicacaoClinica())
                .tipoSolicitacao(request.tipoSolicitacao())
                .build();

        if (funcionario != null) {
            String parecerCriacao = validacao.statusFinal() == StatusGuia.AUTORIZADA
                    ? "Guia autorizada."
                    : "Guia gerada e enviada para análise da auditoria.";

            guia.registrarHistorico(funcionario, validacao.statusFinal(), parecerCriacao, validacao.motivoNegativa());
        }

        for (SolicitarGuiaRequest.ItemProcedimentoRequest itemReq : request.procedimentos()) {

            Procedimento procEncontrado = procedimentosEncontrados.stream()
                    .filter(p -> p.getCodigoTuss().equals(itemReq.codigoTuss()))
                    .findFirst()
                    .orElseThrow();

            ProcedimentoGuia itemGuia = ProcedimentoGuia.builder()
                    .procedimento(procEncontrado)
                    .quantidade(itemReq.quantidade())
                    .build();

            guia.adicionarProcedimento(itemGuia);
        }

        return GuiaMapper.converteEntidade(guiaRepository.save(guia));
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
    @AuditUpdate(entidadeTipo = GuiaAutorizacao.class, acao = AcaoAuditoria.GUIA_ANEXO_ADICIONADO)
    public GuiaResponse anexarArquivo(UUID guiaId, MultipartFile arquivo) {
        GuiaAutorizacao guia = guiaRepository.findById(guiaId)
                .orElseThrow(() -> new EntityNotFoundException("Guia não encontrada."));

        try {
            AnexoGuia anexo = AnexoGuia.builder()
                    .nomeArquivo(arquivo.getOriginalFilename())
                    .tipoArquivo(arquivo.getContentType())
                    .guia(guia)
                    .build();

            AnexoConteudo conteudo = AnexoConteudo.builder()
                    .dados(arquivo.getBytes())
                    .anexo(anexo)
                    .build();

            anexo.setConteudo(conteudo);
            anexoRepository.save(anexo);

            return GuiaMapper.converteEntidade(guia);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao processar o arquivo anexado.", e);
        }
    }

    @Transactional
    @AuditUpdate(entidadeTipo = GuiaAutorizacao.class, acao = AcaoAuditoria.GUIA_PENDENCIA_RESPONDIDA)
    public GuiaResponse responderPendencia(UUID idGuia, Usuario usuarioLogado) {

        GuiaAutorizacao guia = guiaRepository.findById(idGuia)
                .orElseThrow(() -> new EntityNotFoundException("Guia não encontrada."));

        if (guia.getStatus() != StatusGuia.PENDENCIA) {
            throw new IllegalArgumentException("Apenas guias com status PENDENCIA podem ser respondidas.");
        }

        guia.registrarHistorico(usuarioLogado, StatusGuia.PENDENCIA_RESPONDIDA, "Pendência respondida pelo prestador.", null);

        GuiaAutorizacao guiaSalva = guiaRepository.save(guia);

        if (guiaSalva.getAuditor() != null) {
            eventPublisher.publishEvent(new PendenciaRespondidaEvent(
                    guiaSalva.getId(),
                    guiaSalva.getNumeroGuia(),
                    guiaSalva.getAuditor().getId()
            ));
        }

        return GuiaMapper.converteEntidade(guiaSalva);
    }

    @Transactional(readOnly = true)
    public AnexoDownloadDTO baixarAnexo(UUID idAnexo) {
        AnexoGuia anexo = anexoRepository.findById(idAnexo)
                .orElseThrow(() -> new EntityNotFoundException("Anexo não encontrado: " + idAnexo));
        return new AnexoDownloadDTO(
                anexo.getNomeArquivo(),
                anexo.getTipoArquivo(),
                anexo.getConteudo().getDados()
        );
    }

    @Transactional(readOnly = true)
    public GuiaResponse buscarPorId(UUID id, Usuario usuarioLogado) {
        GuiaAutorizacao guia = guiaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Guia não encontrada: " + id));

        if (usuarioLogado.getRole() == RoleUsuario.ADMIN || usuarioLogado.getRole() == RoleUsuario.AUDITOR) {
            return GuiaMapper.converteEntidade(guia);
        }

        Prestador prestadorDoUsuario = obterPrestadorDoUsuario(usuarioLogado);

        if (prestadorDoUsuario == null || !prestadorDoUsuario.getId().equals(guia.getPrestador().getId())) {
            throw new AccessDeniedException("Usuário não tem permissão para acessar essa guia.");
        }

        return GuiaMapper.converteEntidade(guia);
    }

    private Prestador obterPrestadorDoUsuario(Usuario usuario) {
        if (usuario instanceof Prestador prestador) {
            return prestador;
        }
        if (usuario instanceof Funcionario funcionario) {
            return funcionario.getPrestador();
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<GuiaResponse> listarGuias(Usuario usuarioLogado) {
        List<GuiaAutorizacao> guias;
        if (usuarioLogado.getRole() == RoleUsuario.PRESTADOR) {
            guias = guiaRepository.findByPrestadorIdOrderByDataSolicitacaoDesc(usuarioLogado.getId(), Pageable.unpaged()).getContent();
        } else if (usuarioLogado.getRole() == RoleUsuario.FUNCIONARIO_PRESTADOR) {
            Funcionario f = (Funcionario) usuarioLogado;
            guias = guiaRepository.findByPrestadorIdOrderByDataSolicitacaoDesc(f.getPrestador().getId(), Pageable.unpaged()).getContent();
        } else {
            guias = guiaRepository.findAll(Sort.by(Sort.Direction.DESC, "dataSolicitacao"));
        }
        return guias.stream().map(GuiaMapper::converteEntidade).collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GuiaResponse> listarPorCarteirinha(String carteirinha) {
        List<GuiaAutorizacao> guias = guiaRepository.findByBeneficiarioCarteirinha(carteirinha);
        return guias.stream().map(GuiaMapper::converteEntidade).collect(Collectors.toList());
    }
}