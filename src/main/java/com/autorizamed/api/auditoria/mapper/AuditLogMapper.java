package com.autorizamed.api.auditoria.mapper;

import com.autorizamed.api.auditoria.dto.AuditLogResponseDTO;
import com.autorizamed.api.auditoria.entity.AuditLog;

public class AuditLogMapper {

    public static AuditLogResponseDTO converteEntidade(AuditLog entidade) {
        if (entidade == null) {
            return null;
        }

        return new AuditLogResponseDTO(
                entidade.getId(),
                entidade.getAcao(),
                entidade.getData(),
                entidade.getUsuarioId(),
                entidade.getUsuarioNome(),
                entidade.getUsuarioIp(),
                entidade.getEntidadeId(),
                entidade.getEntidadeNome(),
                entidade.getEntidadeTipo(),
                entidade.getDescricao()
        );
    }
}