package com.autorizamed.api.elegibilidade.mapper;

import com.autorizamed.api.elegibilidade.dto.response.RedeCredenciadaResponse;
import com.autorizamed.api.elegibilidade.entity.RedeCredenciada;

public class RedeCredenciadaMapper {

    public static RedeCredenciadaResponse converteEntidade(RedeCredenciada entidade) {
        if (entidade == null) {
            return null;
        }

        return new RedeCredenciadaResponse(
                entidade.getId(),
                entidade.getPrestador().getNome(),
                entidade.getPrestador().getDocumento(),
                entidade.getProcedimento().getCodigoTuss(),
                entidade.getProcedimento().getDescricao(),
                entidade.getPlanosAceitos(),
                entidade.isAtivo()
        );
    }
}