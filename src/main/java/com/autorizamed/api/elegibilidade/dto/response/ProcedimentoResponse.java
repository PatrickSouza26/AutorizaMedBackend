package com.autorizamed.api.elegibilidade.dto.response;

import com.autorizamed.api.auditoria.model.Auditavel;
import com.autorizamed.api.elegibilidade.enums.CategoriaProcedimento; // Ajuste para o seu enum exato

import java.util.UUID;

public record ProcedimentoResponse(
        UUID id,
        String codigoTuss,
        String descricao,
        CategoriaProcedimento categoria,
        boolean requerAutorizacao,
        boolean ativo
) implements Auditavel {

    @Override
    public UUID getId() {
        return id();
    }

    @Override
    public String getNomeEntidade() {
        return codigoTuss() + " - " + descricao();
    }
}