package com.autorizamed.api.autorizacao.mapper;

import com.autorizamed.api.autorizacao.dto.request.CboRequest;
import com.autorizamed.api.autorizacao.dto.response.CboResponse;
import com.autorizamed.api.autorizacao.entity.Cbo;

public class CboMapper {

    public static CboResponse converteEntidade(Cbo cbo) {
        if (cbo == null) {
            return null;
        }

        return new CboResponse(
                cbo.getId(),
                cbo.getCodigo(),
                cbo.getTitulo(),
                cbo.isAtivo()
        );
    }

    public static Cbo converteDto(CboRequest cboDto) {
        if (cboDto == null) {
            return null;
        }

        return Cbo.builder()
                .codigo(cboDto.codigo())
                .titulo(cboDto.titulo())
                .ativo(true)
                .build();
    }
}