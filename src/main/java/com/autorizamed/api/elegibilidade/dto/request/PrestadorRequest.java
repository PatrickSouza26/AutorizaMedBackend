package com.autorizamed.api.elegibilidade.dto.request;

import com.autorizamed.api.comum.dto.EnderecoDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PrestadorRequest(
        @NotBlank(message = "O nome do prestador é obrigatório.")
        @Size(max = 150, message = "O nome não pode ultrapassar 150 caracteres.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        String email,

        @Size(max = 20, message = "O telefone não pode ultrapassar 20 caracteres.")
        String telefone,

        @NotBlank(message = "O login é obrigatório.")
        String login,

        @NotBlank(message = "O documento (CNPJ) é obrigatório.")
        @Size(max = 20, message = "O documento não pode ultrapassar 20 caracteres.")
        String documento,

        @Valid
        EnderecoDTO endereco
) {}