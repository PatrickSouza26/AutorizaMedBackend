package com.autorizamed.api.autorizacao.mapper;

import com.autorizamed.api.autorizacao.dto.response.ProfissionalSaudeResponse;
import com.autorizamed.api.autorizacao.entity.ProfissionalSaude;

public class ProfissionalSolicitanteMapper {

    public static ProfissionalSaudeResponse converteEntidade(ProfissionalSaude profissional) {
        if (profissional == null) {
            return null;
        }

        return new ProfissionalSaudeResponse(
                profissional.getId(),
                profissional.getNome(),
                profissional.getSiglaConselho(),
                profissional.getNumeroConselho(),
                profissional.getUfConselho(),
                profissional.getCbo() != null ? profissional.getCbo().getId() : null,
                profissional.getCbo() != null ? profissional.getCbo().getCodigo() : null,
                profissional.getCbo() != null ? profissional.getCbo().getTitulo() : null
        );
    }
}