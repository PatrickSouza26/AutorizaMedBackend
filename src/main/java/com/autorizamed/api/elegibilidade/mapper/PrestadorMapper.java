package com.autorizamed.api.elegibilidade.mapper;

import com.autorizamed.api.comum.mapper.EnderecoMapper;
import com.autorizamed.api.elegibilidade.dto.response.PrestadorResponse;
import com.autorizamed.api.elegibilidade.entity.Prestador;

public class PrestadorMapper {

    public static PrestadorResponse converteEntidade(Prestador entidade) {
        if (entidade == null) {
            return null;
        }

        return new PrestadorResponse(
                entidade.getId(),
                entidade.getNome(),
                entidade.getEmail(),
                entidade.getTelefone(),
                entidade.getDocumento(),
                entidade.getNumeroPrestador(),
                entidade.isAtivo(),
                EnderecoMapper.converteEntidade(entidade.getEndereco())
        );
    }
}