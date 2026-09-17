package com.autorizamed.api.notificacao.dto;

import com.autorizamed.api.notificacao.entity.Aviso;

import com.autorizamed.api.notificacao.enums.TipoAviso;
import java.time.LocalDateTime;
import java.util.UUID;

public record AvisoResponse(
        UUID id,
        String titulo,
        String mensagem,
        LocalDateTime dataCriacao,
        boolean lido,
        UUID guiaId,
        TipoAviso tipoAviso
) {
    public static AvisoResponse converteEntidade(Aviso aviso) {
        return new AvisoResponse(
                aviso.getId(),
                aviso.getTitulo(),
                aviso.getMensagem(),
                aviso.getDataCriacao(),
                aviso.isLido(),
                aviso.getGuiaId(),
                aviso.getTipoAviso()
        );
    }
}