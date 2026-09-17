package com.autorizamed.api.relatorio.dto;

import com.autorizamed.api.autorizacao.enums.StatusGuia;

import java.time.LocalDateTime;

public record LinhaTempoGuiaDTO(
        String numeroGuia,
        LocalDateTime dataAtualizacao,
        StatusGuia status,
        String procedimentoResumido
) {}