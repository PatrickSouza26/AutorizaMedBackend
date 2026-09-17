package com.autorizamed.api.autorizacao.service;

import com.autorizamed.api.auditoria.annotation.AuditCreate;
import com.autorizamed.api.auditoria.annotation.AuditUpdate;
import com.autorizamed.api.auditoria.enums.AcaoAuditoria;
import com.autorizamed.api.autorizacao.dto.request.AuditorRequest;
import com.autorizamed.api.autorizacao.dto.request.AvaliacaoAuditorRequest;
import com.autorizamed.api.autorizacao.dto.response.AuditorResponse;
import com.autorizamed.api.autorizacao.dto.response.FilaKpiResponse;
import com.autorizamed.api.autorizacao.dto.response.GuiaResponse;
import com.autorizamed.api.autorizacao.entity.Auditor;
import com.autorizamed.api.autorizacao.entity.GuiaAutorizacao;
import com.autorizamed.api.autorizacao.enums.CaraterSolicitacao;
import com.autorizamed.api.autorizacao.enums.StatusGuia;
import com.autorizamed.api.autorizacao.event.GuiaStatusAlteradaEvent;
import com.autorizamed.api.autorizacao.mapper.AuditorMapper;
import com.autorizamed.api.autorizacao.mapper.GuiaMapper;
import com.autorizamed.api.autorizacao.repository.AuditorRepository;
import com.autorizamed.api.autorizacao.repository.GuiaAutorizacaoRepository;
import com.autorizamed.api.comum.mapper.EnderecoMapper;
import com.autorizamed.api.comum.model.Endereco;
import com.autorizamed.api.seguranca.enums.RoleUsuario;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditorService {

    private final GuiaAutorizacaoRepository guiaRepository;
    private final AuditorRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @AuditCreate(entidadeTipo = Auditor.class)
    public AuditorResponse cadastrarAuditor(AuditorRequest request) {
        String senhaTemporaria = request.cpf().replaceAll("[^0-9]", "");
        String senhaCriptografada = passwordEncoder.encode(senhaTemporaria);

        Endereco endereco = EnderecoMapper.converteDto(request.endereco());

        Auditor auditor = Auditor.builder()
                .nome(request.nome())
                .email(request.email())
                .telefone(request.telefone())
                .login(request.email())
                .senha(senhaCriptografada)
                .role(RoleUsuario.AUDITOR)
                .endereco(endereco)
                .cpf(request.cpf())
                .numeroConselho(request.numeroConselho())
                .tipoConselho(request.tipoConselho())
                .ufConselho(request.ufConselho())
                .especialidade(request.especialidade())
                .ativo(true)
                .build();

        Auditor auditorSalvo = repository.save(auditor);
        return AuditorMapper.converteEntidade(auditorSalvo);
    }

    @Transactional(readOnly = true)
    public Page<AuditorResponse> listarAuditores(String termoBusca, Boolean ativo, Pageable pageable) {
        return repository.buscarPorTermoEStatus(termoBusca, ativo, pageable)
                .map(AuditorMapper::converteEntidade);
    }

    @Transactional
    @AuditUpdate(entidadeTipo = Auditor.class)
    public AuditorResponse atualizar(UUID id, AuditorRequest request) {
        Auditor auditor = buscarAuditorSemVerificarStatus(id);
        
        auditor.setNome(request.nome());
        auditor.setEmail(request.email());
        auditor.setTelefone(request.telefone());
        auditor.setCpf(request.cpf());
        auditor.setNumeroConselho(request.numeroConselho());
        auditor.setTipoConselho(request.tipoConselho());
        auditor.setUfConselho(request.ufConselho());
        auditor.setEspecialidade(request.especialidade());
        
        if (request.endereco() != null) {
            auditor.getEndereco().setCep(request.endereco().cep());
            auditor.getEndereco().setLogradouro(request.endereco().logradouro());
            auditor.getEndereco().setNumero(request.endereco().numero());
            auditor.getEndereco().setComplemento(request.endereco().complemento());
            auditor.getEndereco().setBairro(request.endereco().bairro());
            auditor.getEndereco().setCidade(request.endereco().cidade());
            auditor.getEndereco().setUf(request.endereco().uf());
        }

        Auditor auditorAtualizado = repository.save(auditor);
        return AuditorMapper.converteEntidade(auditorAtualizado);
    }

    @Transactional
    @AuditUpdate(entidadeTipo = Auditor.class, acao = AcaoAuditoria.INATIVACAO)
    public void inativar(UUID id) {
        Auditor auditor = buscarAuditorSemVerificarStatus(id);
        auditor.setAtivo(false);
        repository.save(auditor);
    }

    @Transactional
    @AuditUpdate(entidadeTipo = Auditor.class, acao = AcaoAuditoria.REATIVACAO)
    public void reativar(UUID id) {
        Auditor auditor = buscarAuditorSemVerificarStatus(id);
        auditor.setAtivo(true);
        repository.save(auditor);
    }

    private Auditor buscarAuditorSemVerificarStatus(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Auditor não encontrado no sistema."));
    }

    @Transactional(readOnly = true)
    public Page<GuiaResponse> listarFilaDeTrabalho(CaraterSolicitacao filtro, String busca, Pageable pageable) {

        Page<GuiaAutorizacao> guiasPaginadas;

        boolean isBuscaAtiva = busca != null && !busca.trim().isEmpty();

        if (filtro == CaraterSolicitacao.URGENCIA) {
            if (isBuscaAtiva) {
                guiasPaginadas = guiaRepository.buscarFilaAuditoriaSomenteUrgenciaComBusca(busca, pageable);
            } else {
                guiasPaginadas = guiaRepository.buscarFilaAuditoriaSomenteUrgencia(pageable);
            }
        } else if (filtro == CaraterSolicitacao.ELETIVA) {
            if (isBuscaAtiva) {
                guiasPaginadas = guiaRepository.buscarFilaAuditoriaSomenteEletivaComBusca(busca, pageable);
            } else {
                guiasPaginadas = guiaRepository.buscarFilaAuditoriaSomenteEletiva(pageable);
            }
        } else {
            if (isBuscaAtiva) {
                guiasPaginadas = guiaRepository.buscarFilaAuditoriaComBusca(busca, pageable);
            } else {
                guiasPaginadas = guiaRepository.buscarFilaAuditoria(pageable);
            }
        }

        return guiasPaginadas.map(GuiaMapper::converteEntidade);
    }

    @Transactional(readOnly = true)
    public FilaKpiResponse buscarKpisFila() {
        LocalDateTime inicioDia = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime fimDia = LocalDateTime.now().toLocalDate().atTime(23, 59, 59, 999999999);
        
        long total = guiaRepository.contarFilaAuditoria();
        long urgentes = guiaRepository.contarFilaAuditoriaUrgencias();
        long hoje = guiaRepository.contarFilaAuditoriaHoje(inicioDia, fimDia);
        
        return new FilaKpiResponse(total, urgentes, hoje);
    }

    @Transactional
    @AuditUpdate(entidadeTipo = GuiaAutorizacao.class, acao = AcaoAuditoria.GUIA_EM_ANALISE)
    public GuiaResponse iniciarAuditoria(UUID idGuia, UUID idAuditor) {
        GuiaAutorizacao guia = buscarGuia(idGuia);
        Auditor auditor = buscarAuditor(idAuditor);

        if (guia.getStatus() != StatusGuia.EM_ANALISE && guia.getStatus() != StatusGuia.PENDENCIA_RESPONDIDA) {
            throw new IllegalArgumentException("Esta guia não está disponível. Ela já pode estar sendo auditada por outro profissional.");
        }

        guia.setAuditor(auditor);
        guia.registrarHistorico(auditor, StatusGuia.EM_AUDITORIA, "Auditoria médica iniciada.", null);

        return GuiaMapper.converteEntidade(guiaRepository.save(guia));
    }

    @Transactional
    @AuditUpdate(entidadeTipo = GuiaAutorizacao.class, acao = AcaoAuditoria.GUIA_NEGADA)
    public GuiaResponse negarGuia(UUID idGuia, UUID idAuditor, AvaliacaoAuditorRequest request) {
        GuiaAutorizacao guia = buscarGuia(idGuia);
        validarStatus(guia);
        Auditor auditor = buscarAuditor(idAuditor);

        guia.registrarHistorico(auditor, StatusGuia.NEGADA, null, request.motivo());
        GuiaAutorizacao guiaSalva = guiaRepository.save(guia);

        eventPublisher.publishEvent(new GuiaStatusAlteradaEvent(
                guiaSalva.getId(),
                guiaSalva.getNumeroGuia(),
                guiaSalva.getPrestador().getId(),
                guiaSalva.getStatus(),
                request.motivo()
        ));

        return GuiaMapper.converteEntidade(guiaSalva);
    }

    @Transactional
    @AuditUpdate(entidadeTipo = GuiaAutorizacao.class, acao = AcaoAuditoria.GUIA_AUTORIZADA)
    public GuiaResponse aprovarGuia(UUID idGuia, UUID idAuditor, AvaliacaoAuditorRequest request) {
        GuiaAutorizacao guia = buscarGuia(idGuia);
        validarStatus(guia);
        Auditor auditor = buscarAuditor(idAuditor);

        guia.registrarHistorico(auditor, StatusGuia.AUTORIZADA, null, null);

        GuiaAutorizacao guiaSalva = guiaRepository.save(guia);

        eventPublisher.publishEvent(new GuiaStatusAlteradaEvent(
                guiaSalva.getId(),
                guiaSalva.getNumeroGuia(),
                guiaSalva.getPrestador().getId(),
                guiaSalva.getStatus(),
                request.motivo()
        ));

        return GuiaMapper.converteEntidade(guiaSalva);
    }

    @Transactional
    @AuditUpdate(entidadeTipo = GuiaAutorizacao.class, acao = AcaoAuditoria.GUIA_PENDENCIA)
    public GuiaResponse solicitarPendencia(UUID idGuia, UUID idAuditor, AvaliacaoAuditorRequest request) {
        GuiaAutorizacao guia = buscarGuia(idGuia);
        validarStatus(guia);
        Auditor auditor = buscarAuditor(idAuditor);

        guia.registrarHistorico(auditor, StatusGuia.PENDENCIA, null, request.motivo());

        GuiaAutorizacao guiaSalva = guiaRepository.save(guia);

        eventPublisher.publishEvent(new GuiaStatusAlteradaEvent(
                guiaSalva.getId(),
                guiaSalva.getNumeroGuia(),
                guiaSalva.getPrestador().getId(),
                guiaSalva.getStatus(),
                request.motivo()
        ));

        return GuiaMapper.converteEntidade(guiaSalva);
    }

    private void validarStatus(GuiaAutorizacao guia) {
        if (guia.getStatus() != StatusGuia.EM_AUDITORIA) {
            throw new IllegalArgumentException("Você precisa iniciar a análise desta guia antes de processar esta ação.");
        }
    }

    private GuiaAutorizacao buscarGuia(UUID id) {
        return guiaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Guia não encontrada."));
    }

    private Auditor buscarAuditor(UUID id) {
        Auditor auditor = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Auditor não encontrado no sistema."));

        if (!auditor.isAtivo()) {
            throw new IllegalStateException("Este auditor está inativo no sistema.");
        }

        return auditor;
    }
}