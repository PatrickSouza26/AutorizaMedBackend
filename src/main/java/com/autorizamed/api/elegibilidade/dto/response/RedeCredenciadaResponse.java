package com.autorizamed.api.elegibilidade.dto.response;

import com.autorizamed.api.auditoria.model.Auditavel;
import com.autorizamed.api.elegibilidade.enums.TipoPlano;

import java.util.List;
import java.util.UUID;

public record RedeCredenciadaResponse(
        UUID id,
        String nomePrestador,
        String documentoPrestador,
        String codigoTuss,
        String descricaoProcedimento,
        List<TipoPlano> planosAceitos,
        boolean ativo
) implements Auditavel {

    @Override
    public UUID getId() {
        return id();
    }

    @Override
    public String getNomeEntidade() {
        return "Vínculo: " + nomePrestador() + " -> " + codigoTuss();
    }
}