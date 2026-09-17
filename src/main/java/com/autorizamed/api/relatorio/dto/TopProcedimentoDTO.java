package com.autorizamed.api.relatorio.dto;

public record TopProcedimentoDTO(
        String codigoTuss,
        String descricao,
        Long totalSolicitado
) {}