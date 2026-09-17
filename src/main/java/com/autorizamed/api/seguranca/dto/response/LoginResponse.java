package com.autorizamed.api.seguranca.dto.response;

import java.util.UUID;

public record LoginResponse(
        String token,
        UUID id,
        String nome,
        String login,
        String role,
        UUID prestadorId
) {}
