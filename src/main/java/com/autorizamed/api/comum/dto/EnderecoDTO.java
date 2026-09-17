package com.autorizamed.api.comum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnderecoDTO(
        @NotBlank(message = "O CEP é obrigatório")
        String cep,

        @NotBlank(message = "O logradouro é obrigatório")
        String logradouro,

        String numero,

        String complemento,

        @NotBlank(message = "O bairro é obrigatório")
        String bairro,

        @NotBlank(message = "A cidade é obrigatória")
        String cidade,

        @NotBlank(message = "A UF é obrigatória")
        @Size(min = 2, max = 2)
        String uf
) {}