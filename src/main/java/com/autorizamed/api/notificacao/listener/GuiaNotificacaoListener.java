package com.autorizamed.api.notificacao.listener;

import com.autorizamed.api.autorizacao.event.GuiaStatusAlteradaEvent;
import com.autorizamed.api.autorizacao.event.PendenciaRespondidaEvent;
import com.autorizamed.api.notificacao.enums.TipoDestinatario;
import com.autorizamed.api.notificacao.enums.TipoAviso;
import com.autorizamed.api.notificacao.service.AvisoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class GuiaNotificacaoListener {

    private final AvisoService avisoService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGuiaStatusAlterada(GuiaStatusAlteradaEvent event) {

        String titulo = switch (event.novoStatus()) {
            case AUTORIZADA -> "Guia Autorizada";
            case NEGADA -> "Guia Negada";
            case PENDENCIA -> "Pendência na Guia";
            default -> "Atualização na Guia";
        };

        String mensagem = String.format("A guia %s teve seu status alterado para %s.%s",
                event.numeroGuia(),
                event.novoStatus(),
                event.observacao() != null && !event.observacao().isBlank() ? " Motivo: " + event.observacao() : ""
        );

        TipoAviso tipoAviso = switch (event.novoStatus()) {
            case AUTORIZADA -> TipoAviso.SUCESSO;
            case NEGADA -> TipoAviso.ERRO;
            case PENDENCIA -> TipoAviso.ALERTA;
            default -> TipoAviso.INFO;
        };

        avisoService.criarAviso(
                event.prestadorId(),
                TipoDestinatario.PRESTADOR,
                titulo,
                mensagem,
                event.guiaId(),
                tipoAviso
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePendenciaRespondida(PendenciaRespondidaEvent event) {
        avisoService.criarAviso(
                event.auditorId(),
                TipoDestinatario.AUDITOR,
                "Pendência Respondida",
                "O prestador enviou os dados complementares para a Guia " + event.numeroGuia() + ".",
                event.guiaId(),
                TipoAviso.INFO
        );
    }
}