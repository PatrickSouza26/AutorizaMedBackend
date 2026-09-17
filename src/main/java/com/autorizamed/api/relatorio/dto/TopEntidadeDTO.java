package com.autorizamed.api.relatorio.dto;

import java.util.UUID;

//TOP 5 CREDENCIADOS OU TOP 5 BENEFICIÁRIOS
public record TopEntidadeDTO(
        UUID id,
        String nome,
        String documentoOuCarteirinha,
        Long totalGuias
) {}
