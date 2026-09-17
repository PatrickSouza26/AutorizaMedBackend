package com.autorizamed.api.elegibilidade.dto.response;

import com.autorizamed.api.auditoria.model.Auditavel;
import com.autorizamed.api.comum.dto.EnderecoDTO;

import java.util.UUID;

public record FuncionarioResponse(
        UUID id,
        String nome,
        String email,
        String telefone,
        String login,
        String cpf,
        UUID prestadorId,
        String nomePrestador,
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