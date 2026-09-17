package com.autorizamed.api.relatorio.dto;

import java.util.UUID;

public record TopAuditorDTO(
        UUID id,
        String nome,
        String registro,
        Long totalAnalisado
) {}
