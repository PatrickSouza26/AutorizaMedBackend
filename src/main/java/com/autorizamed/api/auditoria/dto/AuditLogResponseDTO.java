package com.autorizamed.api.auditoria.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponseDTO(
        UUID id,
        String acao,
        LocalDateTime data,
        UUID usuarioId,
        String usuarioNome,
        String usuarioIp,
        UUID entidadeId,
        String entidadeNome,
        String entidadeTipo,
        String descricao
) {}