package com.autorizamed.api.relatorio.dto;

import java.util.UUID;

public record TopBeneficiarioDTO(
        UUID idBeneficiario,
        String nome,
        String carteirinha,
        long quantidadeGuias,
        String principalProcedimento,
        String tendencia
) {}
