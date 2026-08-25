package com.autorizamed.api.elegibilidade.dto.request;

public record AtualizarPrestadorRequest(
        String nome,
        String endereco,
        String telefone
) {}
