package com.autorizamed.api.autorizacao.dto.response;

import com.autorizamed.api.auditoria.model.Auditavel;
import com.autorizamed.api.autorizacao.enums.CaraterSolicitacao;
import com.autorizamed.api.autorizacao.enums.StatusGuia;
import com.autorizamed.api.autorizacao.enums.TipoSolicitacao;

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
        String nomeFuncionarioOperador,
        String nomeProfissionalSolicitante,
        String conselhoProfissionalSolicitante,
        String nomeProfissionalExecutante,
        String conselhoProfissionalExecutante,
        List<String> examesSolicitados,
        StatusGuia statusAtual,
        String motivoNegativa,
        String indicacaoClinica,
        LocalDateTime dataSolicitacao,
        CaraterSolicitacao caraterSolicitacao,
        LocalDateTime dataLimiteAprovacao,
        String nomeAuditorResponsavel,
        TipoSolicitacao tipoSolicitacao,
        List<HistoricoGuiaResponse> historico,
        List<AnexoResponse> anexos
) implements Auditavel {

    @Override
    public UUID getId() {
        return idGuia();
    }

    @Override
    public String getNomeEntidade() {
        return "Guia nº " + numeroGuia();
    }
}