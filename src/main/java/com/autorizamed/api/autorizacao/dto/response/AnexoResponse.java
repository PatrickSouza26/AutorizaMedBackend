package com.autorizamed.api.autorizacao.dto.response;

import java.util.UUID;

public record AnexoResponse(
        UUID idAnexo,
        String nomeArquivo,
        String tipoArquivo
) {}
