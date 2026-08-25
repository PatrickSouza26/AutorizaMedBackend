package com.autorizamed.api.elegibilidade.dto.request;

import com.autorizamed.api.elegibilidade.enums.TipoPlano;

public record AtualizarBeneficiarioRequest(
        String nome,
        String endereco,
        TipoPlano tipoPlano
) {}
