package com.autorizamed.api.elegibilidade.dto.request;

import com.autorizamed.api.elegibilidade.enums.CategoriaProcedimento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProcedimentoRequest(

        @NotBlank(message = "O código TUSS é obrigatório.")
        String codigoTuss,

        @NotBlank(message = "A descrição é obrigatória.")
        String descricao,

        @NotNull(message = "A categoria é obrigatória.")
        CategoriaProcedimento categoria,

        @NotNull(message = "Informe se requer autorização (true/false).")
        Boolean requerAutorizacao
) {}