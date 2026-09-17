package com.autorizamed.api.relatorio.dto;

import java.util.UUID;

public record AuditorProdutividadeDTO(
        UUID idAuditor,
        String nome,
        String registro,
        long guiasAnalisadas,
        String tempoMedioResposta,
        double taxaAprovacao
) {}
