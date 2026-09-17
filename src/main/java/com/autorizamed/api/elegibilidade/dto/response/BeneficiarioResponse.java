package com.autorizamed.api.elegibilidade.dto.response;

import com.autorizamed.api.auditoria.model.Auditavel;
import com.autorizamed.api.comum.dto.EnderecoDTO;
import com.autorizamed.api.elegibilidade.enums.TipoPlano;

import java.time.LocalDate;
import java.util.UUID;

public record BeneficiarioResponse(
        UUID id,
        String nome,
        String email,
        String telefone,
        String cpf,
        LocalDate dataNascimento,
        String carteirinha,
        boolean planoAtivo,
        TipoPlano tipoPlano,
        EnderecoDTO endereco,
        LocalDate dataAdesao
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