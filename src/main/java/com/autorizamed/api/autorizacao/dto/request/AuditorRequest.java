package com.autorizamed.api.autorizacao.dto.request;

import com.autorizamed.api.autorizacao.enums.SiglaConselho;
import com.autorizamed.api.comum.dto.EnderecoDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record AuditorRequest(
        @NotBlank(message = "O nome do auditor é obrigatório.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        String email,

        String telefone,

        @NotBlank(message = "O CPF é obrigatório.")
        @CPF(message = "CPF inválido.")
        String cpf,

        @NotBlank(message = "O número do conselho é obrigatório.")
        String numeroConselho,

        @NotNull(message = "O tipo do conselho é obrigatório.")
        SiglaConselho tipoConselho,

        @NotBlank(message = "A UF do conselho é obrigatória.")
        @Size(min = 2, max = 2, message = "A UF deve conter exatamente 2 caracteres.")
        String ufConselho,

        String especialidade,

        @Valid
        EnderecoDTO endereco
) {}