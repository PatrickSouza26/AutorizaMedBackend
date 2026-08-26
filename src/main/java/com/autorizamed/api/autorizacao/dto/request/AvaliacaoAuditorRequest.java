package com.autorizamed.api.autorizacao.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AvaliacaoAuditorRequest(
        @NotBlank(message = "O motivo da negativa ou pendência é obrigatório.")
        String motivo
) {}
