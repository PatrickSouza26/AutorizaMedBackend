package com.autorizamed.api.elegibilidade.dto.request;

import com.autorizamed.api.elegibilidade.enums.TipoPlano;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record RedeCredenciadaRequest(
        @NotNull(message = "O ID do prestador é obrigatório.")
        UUID prestadorId,

        @NotNull(message = "O ID do procedimento é obrigatório.")
        UUID procedimentoId,

        @NotEmpty(message = "É necessário informar pelo menos um plano aceito.")
        List<TipoPlano> planosAceitos
) {}
