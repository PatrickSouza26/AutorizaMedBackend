package com.autorizamed.api.autorizacao.event;

import java.util.UUID;

public record PendenciaRespondidaEvent(
        UUID guiaId,
        String numeroGuia,
        UUID auditorId
) {}