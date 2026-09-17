package com.autorizamed.api.autorizacao.event;

import com.autorizamed.api.autorizacao.enums.StatusGuia;
import java.util.UUID;

public record GuiaStatusAlteradaEvent(
        UUID guiaId,
        String numeroGuia,
        UUID prestadorId,
        StatusGuia novoStatus,
        String observacao
) {}