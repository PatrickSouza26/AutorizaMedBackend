package com.autorizamed.api.relatorio.dto;

import com.autorizamed.api.autorizacao.enums.StatusGuia;

public record StatusCountDTO(
        StatusGuia status,
        Long total
) {}
