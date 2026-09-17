package com.autorizamed.api.elegibilidade.dto.request;

import com.autorizamed.api.comum.dto.EnderecoDTO;

public record AtualizarPrestadorRequest(
        String nome,
        EnderecoDTO endereco,
        String telefone
) {}
