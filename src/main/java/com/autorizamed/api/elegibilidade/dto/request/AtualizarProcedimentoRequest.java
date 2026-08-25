package com.autorizamed.api.elegibilidade.dto.request;

import com.autorizamed.api.elegibilidade.enums.CategoriaProcedimento;

public record AtualizarProcedimentoRequest(
        String descricao,
        CategoriaProcedimento categoria,
        Boolean requerAutorizacao
) {}
