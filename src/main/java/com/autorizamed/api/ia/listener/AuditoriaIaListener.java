package com.autorizamed.api.ia.listener;

import com.autorizamed.api.autorizacao.event.GuiaEmAnaliseEvent;
import com.autorizamed.api.ia.service.AuditoriaIaService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AuditoriaIaListener {

    private final AuditoriaIaService auditoriaIaService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void processarAvaliacaoIa(GuiaEmAnaliseEvent event) {
        auditoriaIaService.avaliarGuiaResiliente(event.guiaId());
    }
}
