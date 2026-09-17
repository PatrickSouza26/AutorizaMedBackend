package com.autorizamed.api.autorizacao.dto.response;

public record AnexoDownloadDTO(
    String nomeArquivo,
    String tipoArquivo,
    byte[] dados
) {}
