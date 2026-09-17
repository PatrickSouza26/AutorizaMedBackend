package com.autorizamed.api.elegibilidade.mapper;

import com.autorizamed.api.elegibilidade.dto.response.ProcedimentoResponse;
import com.autorizamed.api.elegibilidade.entity.Procedimento;

public class ProcedimentoMapper {

    public static ProcedimentoResponse converteEntidade(Procedimento entidade) {
        if (entidade == null) {
            return null;
        }

        return new ProcedimentoResponse(
                entidade.getId(),
                entidade.getCodigoTuss(),
                entidade.getDescricao(),
                entidade.getCategoria(),
                entidade.isRequerAutorizacao(),
                entidade.isAtivo()
        );
    }
}