package com.autorizamed.api.comum.mapper;

import com.autorizamed.api.comum.dto.EnderecoDTO;
import com.autorizamed.api.comum.model.Endereco;

public class EnderecoMapper {

    public static Endereco converteDto(EnderecoDTO dto) {
        if (dto == null) {
            return null;
        }

        return Endereco.builder()
                .cep(dto.cep())
                .logradouro(dto.logradouro())
                .numero(dto.numero())
                .complemento(dto.complemento())
                .bairro(dto.bairro())
                .cidade(dto.cidade())
                .uf(dto.uf())
                .build();
    }

    public static EnderecoDTO converteEntidade(Endereco entidade) {
        if (entidade == null) {
            return null;
        }

        return new EnderecoDTO(
                entidade.getCep(),
                entidade.getLogradouro(),
                entidade.getNumero(),
                entidade.getComplemento(),
                entidade.getBairro(),
                entidade.getCidade(),
                entidade.getUf()
        );
    }
}