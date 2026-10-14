package com.autorizamed.api.ia.service;

import com.autorizamed.api.autorizacao.entity.GuiaAutorizacao;
import com.autorizamed.api.autorizacao.enums.StatusGuia;
import com.autorizamed.api.autorizacao.event.GuiaStatusAlteradaEvent;
import com.autorizamed.api.autorizacao.repository.GuiaAutorizacaoRepository;
import com.autorizamed.api.ia.dto.ResultadoAuditoriaIA;
import com.autorizamed.api.seguranca.entity.Usuario;
import com.autorizamed.api.seguranca.repository.UsuarioRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditoriaIaService {

    private final GuiaAutorizacaoRepository guiaRepository;
    private final AssistenteAuditoria assistenteAuditoria;
    private final UsuarioRepository usuarioRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Retry(name = "ia-auditoria", fallbackMethod = "fallbackAuditoria")
    @CircuitBreaker(name = "ia-auditoria")
    public void avaliarGuiaResiliente(UUID guiaId) {
        log.info("Iniciando busca da guia ID {} para avaliação por IA...", guiaId);

        GuiaAutorizacao guia = guiaRepository.findById(guiaId)
                .orElseThrow(() -> new EntityNotFoundException("Guia não encontrada para o ID: " + guiaId));

        String paciente = guia.getBeneficiario().getNome();
        String justificativa = guia.getIndicacaoClinica();
        String procedimentos = guia.getProcedimentos().stream()
                .map(p -> p.getProcedimento().getCodigoTuss() + " - " + p.getProcedimento().getDescricao())
                .collect(Collectors.joining(", "));

        log.info("Chamando IA para avaliar guia do(a) paciente {}...", paciente);


        ResultadoAuditoriaIA resultadoIa = assistenteAuditoria.auditarGuia(paciente, procedimentos, justificativa);;

        log.info("Resposta da IA recebida. Decisão: {}", resultadoIa.justificativa());

        Usuario admin = (Usuario) usuarioRepository.findByLogin("admin");

        if (resultadoIa.aprovado()) {
            guia.setStatus(StatusGuia.AUTORIZADA);
            guia.registrarHistorico(admin, StatusGuia.AUTORIZADA, "Autorizado automaticamente por IA médica.", null);

            eventPublisher.publishEvent(new GuiaStatusAlteradaEvent(
                    guia.getId(),
                    guia.getNumeroGuia(),
                    guia.getPrestador().getId(),
                    guia.getStatus(),
                    "Aprovado automaticamente via inteligência artificial."
            ));

        } else {
            guia.setStatus(StatusGuia.EM_ANALISE);
            guia.setAlertaSistema("Sugestão da IA para negar: " + resultadoIa.justificativa());
            guia.registrarHistorico(admin, StatusGuia.EM_ANALISE, "IA identificou divergências. Encaminhado para auditoria humana.", resultadoIa.justificativa());
        }

        guiaRepository.save(guia);
    }

    public void fallbackAuditoria(UUID guiaId, Exception e) {
        log.error("Falha ao comunicar com a IA para a guia ID {}. Erro: {}. Encaminhando para auditoria humana.", guiaId, e.getMessage());

        GuiaAutorizacao guia = guiaRepository.findById(guiaId).orElse(null);

        if (guia != null) {
            guia.setAlertaSistema("Auditoria IA indisponível (Timeout/Erro). Guia aguarda análise manual do auditor.");
            guiaRepository.save(guia);
        }
    }
}