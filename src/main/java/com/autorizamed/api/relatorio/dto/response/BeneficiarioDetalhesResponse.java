package com.autorizamed.api.relatorio.dto.response;

import com.autorizamed.api.relatorio.dto.HistoricoMensalDTO;
import com.autorizamed.api.relatorio.dto.LinhaTempoGuiaDTO;

import java.util.List;

public record BeneficiarioDetalhesResponse(
        List<HistoricoMensalDTO> graficoVolume6Meses,
        List<LinhaTempoGuiaDTO> linhaDoTempo
) {}