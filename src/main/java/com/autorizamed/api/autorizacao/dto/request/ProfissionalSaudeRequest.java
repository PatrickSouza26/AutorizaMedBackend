package com.autorizamed.api.autorizacao.dto.request;

import com.autorizamed.api.autorizacao.entity.Cbo;
import com.autorizamed.api.autorizacao.enums.SiglaConselho;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ProfissionalSaudeRequest(
        @NotBlank(message = "O nome do profissional é obrigatório.")
        @Size(max = 150, message = "O nome não pode ultrapassar 150 caracteres.")
        String nome,

        @NotNull(message = "A sigla do conselho é obrigatória.")
        SiglaConselho siglaConselho,

        @NotBlank(message = "O número do conselho é obrigatório.")
        @Size(max = 20, message = "O número do conselho não pode ultrapassar 20 caracteres.")
        String numeroConselho,

        @NotBlank(message = "A UF do conselho é obrigatória.")
        @Size(min = 2, max = 2, message = "A UF deve conter exatamente 2 caracteres.")
        String ufConselho,

        @NotNull(message = "O ID do CBO é obrigatório.")
        CboRequest cbo
) {}