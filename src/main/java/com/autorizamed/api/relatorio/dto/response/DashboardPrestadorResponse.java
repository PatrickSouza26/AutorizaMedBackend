package com.autorizamed.api.relatorio.dto.response;

import com.autorizamed.api.autorizacao.dto.response.GuiaResponse;
import com.autorizamed.api.notificacao.dto.AvisoResponse;
import org.springframework.data.domain.Page;

public record DashboardPrestadorResponse(
        long totalEmAndamento,
        long totalPendencias,
        long totalAutorizadas,
        long totalNegadas,
        Page<GuiaResponse> ultimasSolicitacoes,
        Page<AvisoResponse> avisosRecentes
) {}