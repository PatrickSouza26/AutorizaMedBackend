package com.autorizamed.api.elegibilidade.mapper;

import com.autorizamed.api.comum.mapper.EnderecoMapper;
import com.autorizamed.api.elegibilidade.dto.response.FuncionarioResponse;
import com.autorizamed.api.elegibilidade.entity.Funcionario;

public class FuncionarioMapper {

    public static FuncionarioResponse converteEntidade(Funcionario funcionario) {
        if (funcionario == null) {
            return null;
        }

        return new FuncionarioResponse(
                funcionario.getId(),
                funcionario.getNome(),
                funcionario.getEmail(),
                funcionario.getTelefone(),
                funcionario.getLogin(),
                funcionario.getCpf(),
                funcionario.getPrestador() != null ? funcionario.getPrestador().getId() : null,
                funcionario.getPrestador() != null ? funcionario.getPrestador().getNome() : null,
                funcionario.isAtivo(),
                funcionario.getEndereco() != null ? EnderecoMapper.converteEntidade(funcionario.getEndereco()) : null
        );
    }
}