package com.autorizamed.api.relatorio.dto.response;

import com.autorizamed.api.relatorio.dto.HistoricoMensalDTO;
import com.autorizamed.api.relatorio.dto.TopProcedimentoDTO;
import java.util.List;

public record PrestadorDetalhesResponse(
        List<HistoricoMensalDTO> graficoVolume6Meses,
        List<TopProcedimentoDTO> procedimentosFavoritos
) {}