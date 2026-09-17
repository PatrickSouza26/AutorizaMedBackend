package com.autorizamed.api.autorizacao.dto.response;

import com.autorizamed.api.auditoria.model.Auditavel;
import java.util.UUID;

public record CboResponse(
        UUID id,
        String codigo,
        String titulo,
        boolean ativo
) implements Auditavel {

    @Override
    public UUID getId() {
        return id();
    }

    @Override
    public String getNomeEntidade() {
        return codigo() + " - " + titulo();
    }
}