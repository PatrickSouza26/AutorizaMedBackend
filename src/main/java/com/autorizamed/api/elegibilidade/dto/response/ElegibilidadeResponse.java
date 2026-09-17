package com.autorizamed.api.elegibilidade.dto.response;

public record ElegibilidadeResponse(
        boolean elegivel,
        String motivo,
        BeneficiarioResponse beneficiario
) {}