package com.autorizamed.api.elegibilidade.dto.request;

import com.autorizamed.api.elegibilidade.enums.TipoPlano;

import java.util.List;

public record AtualizarRedeCredenciadaRequest(
        List<TipoPlano> planosAceitos
) {}
