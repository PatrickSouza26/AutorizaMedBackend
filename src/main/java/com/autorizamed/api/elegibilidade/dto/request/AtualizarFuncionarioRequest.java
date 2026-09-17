package com.autorizamed.api.elegibilidade.dto.request;

import com.autorizamed.api.comum.dto.EnderecoDTO;

public record AtualizarFuncionarioRequest(
        String nome,
        String email,
        String telefone,
        EnderecoDTO endereco
) {}