package com.autorizamed.api.relatorio.dto.response;

import com.autorizamed.api.relatorio.dto.AuditorProdutividadeDTO;
import com.autorizamed.api.relatorio.dto.EvolucaoDiariaDTO;
import com.autorizamed.api.relatorio.dto.StatusCountDTO;
import java.util.List;


public record RelatorioOperacionalResponse(
        long totalGuiasPeriodo,
        double taxaPendencias,
        double taxaNegativas,
        String tempoMedioGeral,
        List<EvolucaoDiariaDTO> graficoEvolucaoAbertas,
        List<EvolucaoDiariaDTO> graficoEvolucaoResolvidas,
        List<StatusCountDTO> distribuicaoStatus,
        List<AuditorProdutividadeDTO> produtividadeAuditores
) {}