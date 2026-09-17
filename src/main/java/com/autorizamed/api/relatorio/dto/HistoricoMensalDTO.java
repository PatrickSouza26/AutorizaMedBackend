package com.autorizamed.api.relatorio.dto;

import com.autorizamed.api.autorizacao.enums.StatusGuia;

//GRÁFICO 6 MESES PARA CREDENCIADO E BENEFICIÁRIO
public record HistoricoMensalDTO(
        Integer ano,
        Integer mes,
        StatusGuia status,
        Long total
) {}
