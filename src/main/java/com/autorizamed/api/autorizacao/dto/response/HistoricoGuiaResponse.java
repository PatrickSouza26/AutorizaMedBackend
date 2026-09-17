package com.autorizamed.api.autorizacao.dto.response;

import com.autorizamed.api.autorizacao.enums.StatusGuia;
import java.time.LocalDateTime;
import java.util.UUID;

public record HistoricoGuiaResponse(
        UUID idHistorico,
        String nomeUsuario,
        String perfilUsuario,
        StatusGuia statusAnterior,
        StatusGuia statusNovo,
        LocalDateTime dataMovimentacao,
        String parecer,
        String motivoNegativa
) {}