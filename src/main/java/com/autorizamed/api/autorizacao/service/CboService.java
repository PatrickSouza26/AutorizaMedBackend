package com.autorizamed.api.autorizacao.service;

import com.autorizamed.api.auditoria.annotation.AuditCreate;
import com.autorizamed.api.auditoria.annotation.AuditUpdate;
import com.autorizamed.api.auditoria.enums.AcaoAuditoria;
import com.autorizamed.api.autorizacao.dto.request.CboRequest;
import com.autorizamed.api.autorizacao.dto.response.CboResponse;
import com.autorizamed.api.autorizacao.entity.Cbo;
import com.autorizamed.api.autorizacao.mapper.CboMapper;
import com.autorizamed.api.autorizacao.repository.CboRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CboService {

    private final CboRepository repository;

    @Transactional
    @AuditCreate(entidadeTipo = Cbo.class)
    public CboResponse cadastrar(CboRequest request) {
        if (repository.findByCodigo(request.codigo()).isPresent()) {
            throw new IllegalArgumentException("Já existe um CBO cadastrado com este código.");
        }

        Cbo cbo = Cbo.builder()
                .codigo(request.codigo())
                .titulo(request.titulo())
                .ativo(true)
                .build();

        return CboMapper.converteEntidade(repository.save(cbo));
    }

    @Transactional
    public List<CboResponse> cadastrarLote(List<CboRequest> requests) {
        List<CboResponse> responses = new ArrayList<>();
        for (CboRequest request : requests) {
            if (repository.findByCodigo(request.codigo()).isEmpty()) {
                responses.add(cadastrar(request));
            }
        }
        return responses;
    }

    @Transactional
    @AuditUpdate(entidadeTipo = Cbo.class)
    public CboResponse atualizar(UUID id, CboRequest request) {
        Cbo cbo = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CBO não encontrado."));

        if (!cbo.getCodigo().equals(request.codigo()) && repository.findByCodigo(request.codigo()).isPresent()) {
            throw new IllegalArgumentException("Já existe outro CBO cadastrado com este código.");
        }

        cbo.setCodigo(request.codigo());
        cbo.setTitulo(request.titulo());

        return CboMapper.converteEntidade(repository.save(cbo));
    }

    @Transactional
    @AuditUpdate(entidadeTipo = Cbo.class, acao = AcaoAuditoria.INATIVACAO)
    public void inativar(UUID id) {
        Cbo cbo = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CBO não encontrado."));
        cbo.setAtivo(false);
        repository.save(cbo);
    }

    @Transactional
    @AuditUpdate(entidadeTipo = Cbo.class, acao = AcaoAuditoria.REATIVACAO)
    public void reativar(UUID id) {
        Cbo cbo = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("CBO não encontrado."));
        cbo.setAtivo(true);
        repository.save(cbo);
    }

    public org.springframework.data.domain.Page<CboResponse> listarTodos(org.springframework.data.domain.Pageable pageable) {
        return repository.findAll(pageable).map(CboMapper::converteEntidade);
    }

    public CboResponse buscarPorId(UUID id) {
        return repository.findById(id).map(CboMapper::converteEntidade)
                .orElseThrow(() -> new EntityNotFoundException("CBO não encontrado."));
    }

    public CboResponse buscarPorCodigo(String codigo) {
        return repository.findByCodigoContainingIgnoreCase(codigo).map(CboMapper::converteEntidade)
                .orElseThrow(() -> new EntityNotFoundException("CBO não encontrado."));
    }
}
