package com.autorizamed.api.autorizacao.dto.response;

import com.autorizamed.api.auditoria.model.Auditavel;
import com.autorizamed.api.autorizacao.enums.SiglaConselho;
import com.autorizamed.api.comum.dto.EnderecoDTO;

import java.util.UUID;

public record AuditorResponse(
        UUID id,
        String nome,
        String email,
        String telefone,
        String cpf,
        String numeroConselho,
        SiglaConselho tipoConselho,
        String ufConselho,
        String especialidade,
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