package com.autorizamed.api.elegibilidade.dto.response;

import com.autorizamed.api.elegibilidade.enums.CategoriaProcedimento;

import java.util.UUID;

public record ProcedimentoResponse(
        UUID id,
        String codigoTuss,
        String descricao,
        CategoriaProcedimento categoria,
        boolean requerAutorizacao
) {}
