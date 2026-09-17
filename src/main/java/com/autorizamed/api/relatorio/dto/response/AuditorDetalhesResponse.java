package com.autorizamed.api.relatorio.dto.response;

import com.autorizamed.api.relatorio.dto.AvaliacaoRecenteDTO;
import com.autorizamed.api.relatorio.dto.DecisaoAuditorDTO;
import java.util.List;

public record AuditorDetalhesResponse(
        java.util.UUID auditorId,
        String nome,
        String registro,
        String especialidade,
        List<DecisaoAuditorDTO> graficoDecisoes,
        List<AvaliacaoRecenteDTO> ultimasAvaliacoes
) {}