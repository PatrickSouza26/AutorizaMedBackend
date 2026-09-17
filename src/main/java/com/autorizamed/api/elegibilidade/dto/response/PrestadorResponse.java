package com.autorizamed.api.elegibilidade.dto.response;

import com.autorizamed.api.auditoria.model.Auditavel;
import com.autorizamed.api.comum.dto.EnderecoDTO;

import java.util.UUID;

public record PrestadorResponse(
        UUID id,
        String nome,
        String email,
        String telefone,
        String documento,
        String numeroPrestador,
        boolean ativo,
        EnderecoDTO endereco
) implements Auditavel {

    @Override
    public UUID getId() {
        return id();
    }

    @Override
    public String getNomeEntidade() {
        return nome();
    }
}