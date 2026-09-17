package com.autorizamed.api.autorizacao.dto.request;

import com.autorizamed.api.autorizacao.enums.CaraterSolicitacao;
import com.autorizamed.api.autorizacao.enums.TipoSolicitacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public record SolicitarGuiaRequest(
        @NotBlank(message = "A carteirinha é obrigatória.")
        String carteirinhaBeneficiario,

        @NotNull(message = "O prestador é obrigatório.")
        UUID prestadorId,

        UUID funcionarioId,

        @NotNull(message = "O profissional solicitante é obrigatório.")
        ProfissionalSaudeRequest profissionalSolicitante,

        ProfissionalSaudeRequest profissionalExecutante,

        @NotEmpty(message = "Pelo menos um procedimento deve ser solicitado.")
        @Valid
        List<ItemProcedimentoRequest> procedimentos,

        @NotNull(message = "O caráter da solicitação é obrigatório.")
        CaraterSolicitacao caraterSolicitacao,

        @NotNull(message = "Informe se é atendimento a recém-nascido.")
        Boolean atendimentoRn,

        @NotNull(message = "Informe se há declaração de acidente.")
        Boolean declaracaoAcidente,

        @NotBlank(message = "A indicação clínica é obrigatória para a análise.")
        String indicacaoClinica,

        @NotNull(message = "O tipo de solicitação é obrigatório (ex: CONSULTA, EXAME).")
        TipoSolicitacao tipoSolicitacao
) {
        public record ItemProcedimentoRequest(
                @NotBlank(message = "O código TUSS é obrigatório.")
                String codigoTuss,

                @NotNull(message = "A quantidade é obrigatória.")
                @Min(value = 1, message = "A quantidade mínima é 1.")
                Integer quantidade
        ) {}
}