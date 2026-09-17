package com.autorizamed.api.autorizacao.dto.response;

public record FilaKpiResponse(
        long totalPendentes,
        long totalUrgentes,
        long totalHoje
) {}
