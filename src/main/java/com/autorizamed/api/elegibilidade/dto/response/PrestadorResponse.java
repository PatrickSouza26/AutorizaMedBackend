package com.autorizamed.api.elegibilidade.dto.response;

import java.util.UUID;

public record PrestadorResponse(
        UUID id,
        String nome,
        String documento,
        String endereco,
        String telefone,
        boolean ativo
      ) {}
