package com.autorizamed.api.relatorio.dto.response;

import com.autorizamed.api.relatorio.dto.EvolucaoDiariaDTO;
import com.autorizamed.api.relatorio.dto.StatusCountDTO;
import com.autorizamed.api.relatorio.dto.TopProcedimentoDTO;
import java.util.List;

public record DashboardAdminResponse(
        long totalBeneficiarios,
        long totalCredenciados,
        long totalAuditores,
        long solicitacoesPeriodo,
        double taxaAprovacao,
        List<EvolucaoDiariaDTO> graficoEvolucaoAbertas,
        List<EvolucaoDiariaDTO> graficoEvolucaoResolvidas,
        List<StatusCountDTO> graficoStatus,
        List<TopProcedimentoDTO> topProcedimentos
) {}