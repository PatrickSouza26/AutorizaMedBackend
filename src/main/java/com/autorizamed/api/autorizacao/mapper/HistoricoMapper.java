package com.autorizamed.api.autorizacao.mapper;

import com.autorizamed.api.autorizacao.dto.response.HistoricoGuiaResponse;
import com.autorizamed.api.autorizacao.entity.HistoricoGuia;

public class HistoricoMapper {
    public static HistoricoGuiaResponse converteEntidade(HistoricoGuia entidade) {
        if (entidade == null) return null;

        return new HistoricoGuiaResponse(
                entidade.getId(),
                entidade.getUsuario().getNome(),
                entidade.getUsuario().getRole().name(),
                entidade.getStatusAnterior(),
                entidade.getStatusNovo(),
                entidade.getDataMovimentacao(),
                entidade.getParecer(),
                entidade.getMotivoNegativa()
        );
    }
}