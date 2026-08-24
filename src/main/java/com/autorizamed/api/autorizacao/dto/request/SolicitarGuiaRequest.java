package com.autorizamed.api.autorizacao.dto.request;

import com.autorizamed.api.autorizacao.enums.CaraterSolicitacao;
import com.autorizamed.api.autorizacao.enums.SiglaConselho;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SolicitarGuiaRequest(
        @NotBlank(message = "A carteirinha é obrigatória.")
        String carteirinhaBeneficiario,

        @NotBlank(message = "O documento do prestador é obrigatório.")
        String documentoPrestador,

        @NotEmpty(message = "Pelo menos um procedimento deve ser solicitado.")
        List<String> codigosTuss,

        @NotNull(message = "A sigla do conselho é obrigatória.")
        SiglaConselho siglaConselho,

        @NotBlank(message = "A UF do conselho é obrigatória.")
        @Size(min = 2, max = 2, message = "A UF deve conter exatamente 2 letras.")
        String ufConselho,

        @NotBlank(message = "O CBOS do profissional é obrigatório.")
        String cbosProfissional,

        @NotNull(message = "O caráter da solicitação é obrigatório.")
        CaraterSolicitacao caraterSolicitacao,

        @NotNull(message = "Informe se é atendimento a recém-nascido.")
        Boolean atendimentoRn,

        @NotNull(message = "Informe se há declaração de acidente.")
        Boolean declaracaoAcidente,

        @NotBlank(message = "A indicação clínica é obrigatória para a análise.")
        String indicacaoClinica
) {}
