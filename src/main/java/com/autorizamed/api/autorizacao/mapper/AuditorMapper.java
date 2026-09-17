package com.autorizamed.api.autorizacao.mapper;

import com.autorizamed.api.autorizacao.dto.response.AuditorResponse;
import com.autorizamed.api.autorizacao.entity.Auditor;
import com.autorizamed.api.comum.mapper.EnderecoMapper;

public class AuditorMapper {

    public static AuditorResponse converteEntidade(Auditor entidade) {
        if (entidade == null) {
            return null;
        }

        return new AuditorResponse(
                entidade.getId(),
                entidade.getNome(),
                entidade.getEmail(),
                entidade.getTelefone(),
                entidade.getCpf(),
                entidade.getNumeroConselho(),
                entidade.getTipoConselho(),
                entidade.getUfConselho(),
                entidade.getEspecialidade(),
                entidade.isAtivo(),
                EnderecoMapper.converteEntidade(entidade.getEndereco())
        );
    }
}