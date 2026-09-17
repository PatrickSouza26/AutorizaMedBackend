package com.autorizamed.api.relatorio.dto;

import java.util.UUID;

public record TopPrestadorDTO(
        UUID idPrestador,
        String nome,
        String cnpj,
        long guiasMes,
        double crescimentoPercentual,
        String tendencia
) {}
