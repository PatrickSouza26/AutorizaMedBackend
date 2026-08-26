package com.autorizamed.api.autorizacao.service;

import com.autorizamed.api.autorizacao.dto.request.AvaliacaoAuditorRequest;
import com.autorizamed.api.autorizacao.dto.response.GuiaResponse;
import com.autorizamed.api.autorizacao.entity.GuiaAutorizacao;
import com.autorizamed.api.autorizacao.enums.CaraterSolicitacao;
import com.autorizamed.api.autorizacao.enums.StatusGuia;
import com.autorizamed.api.autorizacao.model.GuiaMapper;
import com.autorizamed.api.autorizacao.repository.GuiaAutorizacaoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditorService {

    private final GuiaAutorizacaoRepository guiaRepository;
    private final GuiaMapper mapper;

    @Transactional(readOnly = true)
    public Page<GuiaResponse> listarFilaDeTrabalho(CaraterSolicitacao filtro, Pageable pageable) {

        Page<GuiaAutorizacao> guiasPaginadas;

        if (filtro == CaraterSolicitacao.URGENCIA) {
            guiasPaginadas = guiaRepository.buscarFilaAuditoriaSomenteUrgencia(pageable);
        } else if (filtro == CaraterSolicitacao.ELETIVA) {
            guiasPaginadas = guiaRepository.buscarFilaAuditoriaSomenteEletiva(pageable);
        } else {
            guiasPaginadas = guiaRepository.buscarFilaAuditoria(pageable);
        }

        return guiasPaginadas.map(mapper::converterParaResponse);
    }

    @Transactional
    public GuiaResponse iniciarAuditoria(UUID idGuia) {
        GuiaAutorizacao guia = buscarGuia(idGuia);

        if (guia.getStatus() != StatusGuia.EM_ANALISE && guia.getStatus() != StatusGuia.PENDENCIA_RESPONDIDA) {
            throw new IllegalArgumentException("Esta guia não está disponível. Ela já pode estar sendo auditada por outro profissional.");
        }

        guia.setStatus(StatusGuia.EM_AUDITORIA);

        return mapper.converterParaResponse(guiaRepository.save(guia));
    }

    @Transactional
    // @AuditarAcao(acao = "NEGATIVA_MANUAL")
    public GuiaResponse negarGuia(UUID idGuia, AvaliacaoAuditorRequest request) {
        GuiaAutorizacao guia = buscarGuia(idGuia);
        validarStatus(guia);

        guia.setStatus(StatusGuia.NEGADA);
        guia.setMotivoNegativa(request.motivo());

        guia = guiaRepository.save(guia);
        return mapper.converterParaResponse(guia);
    }

    @Transactional
    public GuiaResponse aprovarGuia(UUID idGuia) {
        GuiaAutorizacao guia = buscarGuia(idGuia);
        validarStatus(guia);

        guia.setStatus(StatusGuia.AUTORIZADA);
        guia.setMotivoNegativa(null);

        return mapper.converterParaResponse(guiaRepository.save(guia));
    }

    @Transactional
    public GuiaResponse solicitarPendencia(UUID idGuia, AvaliacaoAuditorRequest request) {
        GuiaAutorizacao guia = buscarGuia(idGuia);
        validarStatus(guia);

        guia.setStatus(StatusGuia.PENDENCIA);
        guia.setMotivoNegativa(request.motivo());

        return mapper.converterParaResponse(guiaRepository.save(guia));
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
}