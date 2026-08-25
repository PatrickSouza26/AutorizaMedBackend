package com.autorizamed.api.elegibilidade.service;

import com.autorizamed.api.elegibilidade.dto.request.AtualizarPrestadorRequest;
import com.autorizamed.api.elegibilidade.dto.request.PrestadorRequest;
import com.autorizamed.api.elegibilidade.dto.response.PrestadorResponse;
import com.autorizamed.api.elegibilidade.entity.Prestador;
import com.autorizamed.api.elegibilidade.repository.PrestadorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrestadorService {

    private final PrestadorRepository repository;

    @Transactional
    public PrestadorResponse cadastrar(PrestadorRequest request) {
        if (repository.findByDocumento(request.documento()).isPresent()) {
            throw new IllegalArgumentException("Já existe um prestador cadastrado com este documento (CRM/CNPJ).");
        }

        Prestador prestador = Prestador.builder()
                .nome(request.nome())
                .documento(request.documento())
                .endereco(request.endereco())
                .telefone(request.telefone())
                .ativo(true) // TODO PRESTADOR "NASCE" ATIVO NO SISTEMA
                .build();

        prestador = repository.save(prestador);
        return converterParaResponse(prestador);
    }

    @Transactional(readOnly = true)
    public PrestadorResponse buscarPorDocumento(String documento) {
        Prestador prestador = repository.findByDocumento(documento)
                .orElseThrow(() -> new EntityNotFoundException("Prestador não encontrado pelo documento informado."));
        return converterParaResponse(prestador);
    }

    @Transactional
    public PrestadorResponse atualizarParcial(UUID id, AtualizarPrestadorRequest request) {
        Prestador prestador = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prestador não encontrado pelo ID informado."));

        boolean houveAlteracao = false;

        if (request.nome() != null && !request.nome().isBlank() && !request.nome().equals(prestador.getNome())) {
            prestador.setNome(request.nome());
            houveAlteracao = true;
        }

        if (request.endereco() != null && !request.endereco().isBlank() && !request.endereco().equals(prestador.getEndereco())) {
            prestador.setEndereco(request.endereco());
            houveAlteracao = true;
        }

        if (request.telefone() != null && !request.telefone().isBlank() && !request.telefone().equals(prestador.getTelefone())) {
            prestador.setTelefone(request.telefone());
            houveAlteracao = true;
        }

        if (houveAlteracao) {
            prestador = repository.save(prestador);
        }

        return converterParaResponse(prestador);
    }

    @Transactional
    public void inativar(UUID id) {
        Prestador prestador = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prestador não encontrado pelo ID informado."));

        if (!prestador.isAtivo()) {
            throw new IllegalArgumentException("Este prestador já está inativo no sistema.");
        }

        prestador.setAtivo(false);
        repository.save(prestador);
    }

    private PrestadorResponse converterParaResponse(Prestador entidade) {
        return new PrestadorResponse(
                entidade.getId(),
                entidade.getNome(),
                entidade.getDocumento(),
                entidade.getEndereco(),
                entidade.getTelefone(),
                entidade.isAtivo()
        );
    }
}
