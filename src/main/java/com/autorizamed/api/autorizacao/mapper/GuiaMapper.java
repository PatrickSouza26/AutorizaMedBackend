package com.autorizamed.api.autorizacao.model;

import com.autorizamed.api.autorizacao.dto.response.GuiaResponse;
import com.autorizamed.api.autorizacao.entity.GuiaAutorizacao;
import com.autorizamed.api.elegibilidade.entity.Procedimento;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GuiaMapper {
    public GuiaResponse converterParaResponse(GuiaAutorizacao guia) {
        List<String> nomesExames = guia.getProcedimentos().stream()
                .map(Procedimento::getDescricao)
                .collect(Collectors.toList());

        return new GuiaResponse(
                guia.getId(),
                guia.getNumeroGuia(),
                guia.getBeneficiario().getCarteirinha(),
                guia.getBeneficiario().getNome(),
                guia.getPrestador().getDocumento(),
                guia.getPrestador().getNome(),
                nomesExames,
                guia.getStatus(),
                guia.getMotivoNegativa(),
                guia.getIndicacaoClinica(),
                guia.getDataSolicitacao(),
                guia.getCaraterSolicitacao(),
                guia.getDataLimiteAprovacao()
        );
    }
}
