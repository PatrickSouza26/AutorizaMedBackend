package com.autorizamed.api.elegibilidade.service;

import com.autorizamed.api.elegibilidade.dto.request.AtualizarBeneficiarioRequest;
import com.autorizamed.api.elegibilidade.dto.request.BeneficiarioRequest;
import com.autorizamed.api.elegibilidade.dto.response.BeneficiarioResponse;
import com.autorizamed.api.elegibilidade.entity.Beneficiario;
import com.autorizamed.api.elegibilidade.repository.BeneficiarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeneficiarioService {

    private final BeneficiarioRepository repository;

    @Transactional
    public BeneficiarioResponse cadastrar(BeneficiarioRequest request) {
        if (repository.findByCpf(request.cpf()).isPresent()) {
            throw new IllegalArgumentException("Já existe um beneficiário cadastrado com este CPF.");
        }

        Long proximoNumero = repository.getProximoValorCarteirinha();
        String carteirinhaGerada = String.format("1000%05d", proximoNumero);

        Beneficiario beneficiario = Beneficiario.builder()
                .nome(request.nome())
                .cpf(request.cpf())
                .dataNascimento(request.dataNascimento())
                .endereco(request.endereco())
                .carteirinha(carteirinhaGerada)
                .tipoPlano(request.tipoPlano())
                .planoAtivo(true) // TODO PACIENTE " NASCE" COM PLANO ATIVO
                .build();

        beneficiario = repository.save(beneficiario);
        return converterParaResponse(beneficiario);
    }

    @Transactional(readOnly = true)
    public BeneficiarioResponse buscarPorCpfOuCarteirinha(String termoBusca) {
        Beneficiario beneficiario;

        String termoLimpo = termoBusca.replaceAll("[^0-9]", "");

        if (termoLimpo.startsWith("1000") && termoLimpo.length() >= 9) {
            beneficiario = repository.findByCarteirinha(termoBusca)
                    .orElseThrow(() -> new EntityNotFoundException("Beneficiário não encontrado pela carteirinha informada."));
        } else {
            beneficiario = repository.findByCpf(termoBusca)
                    .orElseThrow(() -> new EntityNotFoundException("Beneficiário não encontrado pelo CPF informado."));
        }

        return converterParaResponse(beneficiario);
    }

    @Transactional
    public BeneficiarioResponse atualizarParcial(UUID id, AtualizarBeneficiarioRequest request) {
        Beneficiario beneficiario = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Beneficiário não encontrado."));

        boolean houveAlteracao = false;

        if (request.nome() != null && !request.nome().isBlank() && !request.nome().equals(beneficiario.getNome())) {
            beneficiario.setNome(request.nome());
            houveAlteracao = true;
        }

        if (request.endereco() != null && !request.endereco().isBlank() && !request.endereco().equals(beneficiario.getEndereco())) {
            beneficiario.setEndereco(request.endereco());
            houveAlteracao = true;
        }

        if (request.tipoPlano() != null && request.tipoPlano() != beneficiario.getTipoPlano()) {
            beneficiario.setTipoPlano(request.tipoPlano());
            houveAlteracao = true;
        }

        if (houveAlteracao) {
            beneficiario = repository.save(beneficiario);
        }

        return converterParaResponse(beneficiario);
    }

    @Transactional
    public void inativar(UUID id) {
        Beneficiario beneficiario = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Beneficiário não encontrado pelo ID informado."));

        if (!beneficiario.isPlanoAtivo()) {
            throw new IllegalArgumentException("O plano deste beneficiário já está inativo.");
        }

        beneficiario.setPlanoAtivo(false);
        repository.save(beneficiario);
    }

    private BeneficiarioResponse converterParaResponse(Beneficiario entidade) {
        return new BeneficiarioResponse(
                entidade.getId(),
                entidade.getNome(),
                entidade.getCpf(),
                entidade.getDataNascimento(),
                entidade.getEndereco(),
                entidade.getCarteirinha(),
                entidade.isPlanoAtivo(),
                entidade.getTipoPlano()
        );
    }
}
