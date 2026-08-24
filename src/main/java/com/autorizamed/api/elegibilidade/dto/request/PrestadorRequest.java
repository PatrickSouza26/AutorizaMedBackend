package com.autorizamed.api.elegibilidade.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PrestadorRequest(
        @NotBlank(message = "O nome do prestador é obrigatório.")
        @Size(max = 150, message = "O nome não pode ultrapassar 150 caracteres.")
        String nome,

        @NotBlank(message = "O documento (CRM/CNPJ) é obrigatório.")
        @Size(max = 20, message = "O documento não pode ultrapassar 20 caracteres.")
        String documento,

        @NotBlank(message = "O endereço é obrigatório.")
        @Size(max = 200, message = "O endereço não pode ultrapassar 200 caracteres.")
        String endereco,

        @Size(max = 20, message = "O telefone não pode ultrapassar 20 caracteres.")
        String telefone
) {}
