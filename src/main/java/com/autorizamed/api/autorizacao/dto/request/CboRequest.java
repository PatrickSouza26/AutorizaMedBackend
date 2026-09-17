package com.autorizamed.api.autorizacao.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CboRequest(
        @NotBlank(message = "O código do CBO é obrigatório.")
        @Size(max = 20, message = "O código não pode ultrapassar 20 caracteres.")
        String codigo,

        @NotBlank(message = "O título do CBO é obrigatório.")
        @Size(max = 150, message = "O título não pode ultrapassar 150 caracteres.")
        String titulo
) {}