package com.autorizamed.api.relatorio.dto;

import com.autorizamed.api.autorizacao.enums.StatusGuia;

import java.time.LocalDateTime;

public record AvaliacaoRecenteDTO(
        String numeroGuia,
        LocalDateTime dataAuditoria,
        String nomePaciente,
        String procedimentoFormatado,
        String tempoAnalise,
        StatusGuia veredito
) {}
