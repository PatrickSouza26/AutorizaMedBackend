package com.autorizamed.api.relatorio.dto;

import com.autorizamed.api.autorizacao.enums.StatusGuia;


public record DecisaoAuditorDTO(
        Integer ano,
        Integer mes,
        StatusGuia status,
        Long total
) {}