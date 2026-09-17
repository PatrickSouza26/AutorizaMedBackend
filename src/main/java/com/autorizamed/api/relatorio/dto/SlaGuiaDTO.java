package com.autorizamed.api.relatorio.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlaGuiaDTO(
        UUID idGuia,
        LocalDateTime dataInicioAnalise,
        LocalDateTime dataFimAnalise
) {}