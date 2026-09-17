package com.autorizamed.api.elegibilidade.mapper;

import com.autorizamed.api.comum.mapper.EnderecoMapper;
import com.autorizamed.api.elegibilidade.dto.response.BeneficiarioResponse;
import com.autorizamed.api.elegibilidade.entity.Beneficiario;

public class BeneficiarioMapper {

    public static BeneficiarioResponse converteEntidade(Beneficiario entidade) {
        if (entidade == null) {
            return null;
        }

        return new BeneficiarioResponse(
                entidade.getId(),
                entidade.getNome(),
                entidade.getEmail(),
                entidade.getTelefone(),
                entidade.getCpf(),
                entidade.getDataNascimento(),
                entidade.getCarteirinha(),
                entidade.isPlanoAtivo(),
                entidade.getTipoPlano(),
                EnderecoMapper.converteEntidade(entidade.getEndereco()),
                entidade.getDataAdesao()
        );
    }
}