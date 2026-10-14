package com.autorizamed.api.ia.dto;

import dev.langchain4j.model.output.structured.Description;

public record ResultadoAuditoriaIA(
        @Description("Status final da avaliação: true se a guia deve ser APROVADA, false se deve ser NEGADA.")
        boolean aprovado,

        @Description("Justificativa clínica técnica e detalhada para o auditor ler. Máximo de 3 frases.")
        String justificativa
) {}