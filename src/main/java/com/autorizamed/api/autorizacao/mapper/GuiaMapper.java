package com.autorizamed.api.autorizacao.mapper;

import com.autorizamed.api.autorizacao.dto.response.AnexoResponse;
import com.autorizamed.api.autorizacao.dto.response.GuiaResponse;
import com.autorizamed.api.autorizacao.dto.response.HistoricoGuiaResponse;
import com.autorizamed.api.autorizacao.entity.GuiaAutorizacao;

import java.util.List;
import java.util.stream.Collectors;

public class GuiaMapper {

    public static GuiaResponse converteEntidade(GuiaAutorizacao entidade) {
        if (entidade == null) {
            return null;
        }

        List<String> procedimentos = entidade.getProcedimentos().stream()
                .map(item -> item.getQuantidade() + "x " + item.getProcedimento().getCodigoTuss() + " - " + item.getProcedimento().getDescricao())
                .collect(Collectors.toList());

        List<HistoricoGuiaResponse> linhaDoTempo = entidade.getHistoricos().stream()
                .map(HistoricoMapper::converteEntidade)
                .collect(Collectors.toList());

        List<AnexoResponse> anexos = entidade.getAnexos() != null ? entidade.getAnexos().stream()
                .map(a -> new AnexoResponse(a.getId(), a.getNomeArquivo(), a.getTipoArquivo()))
                .collect(Collectors.toList()) : List.of();

        String nomeFuncionario = entidade.getFuncionario() != null
                ? entidade.getFuncionario().getNome() : null;

        String nomeSolicitante = entidade.getProfissionalSolicitante() != null
                ? entidade.getProfissionalSolicitante().getNome() : null;

        String conselhoSolicitante = entidade.getProfissionalSolicitante() != null
                ? entidade.getProfissionalSolicitante().getSiglaConselho() + " "
                  + entidade.getProfissionalSolicitante().getNumeroConselho() + "/"
                  + entidade.getProfissionalSolicitante().getUfConselho() : null;

        String nomeExecutante = entidade.getProfissionalExecutante() != null
                ? entidade.getProfissionalExecutante().getNome() : null;

        String conselhoExecutante = entidade.getProfissionalExecutante() != null
                ? entidade.getProfissionalExecutante().getSiglaConselho() + " "
                  + entidade.getProfissionalExecutante().getNumeroConselho() + "/"
                  + entidade.getProfissionalExecutante().getUfConselho() : null;

        return new GuiaResponse(
                entidade.getId(),
                entidade.getNumeroGuia(),
                entidade.getBeneficiario().getCarteirinha(),
                entidade.getBeneficiario().getNome(),
                entidade.getPrestador().getDocumento(),
                entidade.getPrestador().getNome(),
                nomeFuncionario,
                nomeSolicitante,
                conselhoSolicitante,
                nomeExecutante,
                conselhoExecutante,
                procedimentos,
                entidade.getStatus(),
                entidade.getMotivoNegativa(),
                entidade.getIndicacaoClinica(),
                entidade.getDataSolicitacao(),
                entidade.getCaraterSolicitacao(),
                entidade.getDataLimiteAprovacao(),
                entidade.getAuditor() != null ? entidade.getAuditor().getNome() : null,
                entidade.getTipoSolicitacao(),
                linhaDoTempo,
                anexos
        );
    }
}