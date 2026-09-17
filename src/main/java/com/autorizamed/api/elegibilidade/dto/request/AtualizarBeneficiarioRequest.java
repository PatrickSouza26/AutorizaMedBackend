package com.autorizamed.api.elegibilidade.dto.request;

import com.autorizamed.api.comum.dto.EnderecoDTO;
import com.autorizamed.api.comum.model.Endereco;
import com.autorizamed.api.elegibilidade.enums.TipoPlano;

public record AtualizarBeneficiarioRequest(
        String nome,
        EnderecoDTO endereco,
        TipoPlano tipoPlano
) {}
