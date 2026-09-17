package com.autorizamed.api.relatorio.dto.response;

import com.autorizamed.api.relatorio.dto.TopBeneficiarioDTO;
import com.autorizamed.api.relatorio.dto.TopPrestadorDTO;
import com.autorizamed.api.relatorio.dto.TopProcedimentoDTO;
import java.util.List;

public record FrequenciaUtilizacaoResponse(
        List<TopProcedimentoDTO> topProcedimentos,
        List<TopPrestadorDTO> topPrestadores,
        List<TopBeneficiarioDTO> topBeneficiarios
) {}