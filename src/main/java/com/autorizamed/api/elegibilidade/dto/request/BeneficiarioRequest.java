package com.autorizamed.api.elegibilidade.dto.request;

import com.autorizamed.api.comum.dto.EnderecoDTO;
import com.autorizamed.api.elegibilidade.enums.TipoPlano;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record BeneficiarioRequest(
        @NotBlank(message = "O nome não pode estar em branco.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "E-mail inválido.")
        String email,

        String telefone,

        @NotBlank(message = "O CPF é obrigatório.")
        String cpf,

        @NotNull(message = "A data de nascimento é obrigatória.")
        @Past(message = "A data de nascimento deve estar no passado.")
        LocalDate dataNascimento,

        @Valid
        EnderecoDTO endereco,

        @NotNull(message = "O tipo do plano é obrigatório.")
        TipoPlano tipoPlano,

        @NotNull(message = "A data de adesão é obrigatória.")
        LocalDate dataAdesao
) {}
