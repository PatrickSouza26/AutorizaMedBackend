package com.autorizamed.api.autorizacao.dto.response;

import com.autorizamed.api.autorizacao.enums.CaraterSolicitacao;
import com.autorizamed.api.autorizacao.enums.StatusGuia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GuiaResponse(
        UUID idGuia,
        String numeroGuia,
        String carteirinhaPaciente,
        String nomePaciente,
        String docPrestador,
        String nomePrestador,
        List<String> examesSolicitados, //nomes/códigos dos exames
        StatusGuia statusAtual,
        String motivoNegativa,
        String indicacaoClinica,
        LocalDateTime dataSolicitacao,
        CaraterSolicitacao caraterSolicitacao,
        LocalDateTime dataLimiteAprovacao
) {}
