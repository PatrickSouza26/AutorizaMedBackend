package com.autorizamed.api.autorizacao.dto.response;

import com.autorizamed.api.auditoria.model.Auditavel;
import com.autorizamed.api.autorizacao.enums.SiglaConselho;

import java.util.UUID;

public record ProfissionalSaudeResponse(
        UUID id,
        String nome,
        SiglaConselho siglaConselho,
        String numeroConselho,
        String ufConselho,
        UUID cboId,
        String cboCodigo,
        String cboTitulo
) implements Auditavel {

    @Override
    public UUID getId() {
        return id();
    }

    @Override
    public String getNomeEntidade() {
        return nome() + " - " + numeroConselho();
    }
}