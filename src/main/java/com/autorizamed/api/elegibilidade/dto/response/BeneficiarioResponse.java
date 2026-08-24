package com.autorizamed.api.elegibilidade.dto.response;

import com.autorizamed.api.elegibilidade.enums.TipoPlano;

import java.time.LocalDate;
import java.util.UUID;

public record BeneficiarioResponse(
        UUID id,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        String endereco,
        String carteirinha,
        boolean planoAtivo,
        TipoPlano tipoPlano
) {}
