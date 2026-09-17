package com.autorizamed.api.autorizacao.service;

import com.autorizamed.api.auditoria.annotation.AuditCreate;
import com.autorizamed.api.auditoria.annotation.AuditUpdate;
import com.autorizamed.api.autorizacao.dto.request.ProfissionalSaudeRequest;
import com.autorizamed.api.autorizacao.dto.response.ProfissionalSaudeResponse;
import com.autorizamed.api.autorizacao.entity.Cbo;
import com.autorizamed.api.autorizacao.entity.ProfissionalSaude;
import com.autorizamed.api.autorizacao.mapper.ProfissionalSolicitanteMapper;
import com.autorizamed.api.autorizacao.repository.CboRepository;
import com.autorizamed.api.autorizacao.repository.ProfissionalSaudeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfissionalSaudeService {

    private final ProfissionalSaudeRepository repository;
    private final CboRepository cboRepository;

    @Transactional
    @AuditCreate(entidadeTipo = ProfissionalSaude.class)
    public ProfissionalSaudeResponse cadastrar(ProfissionalSaudeRequest request) {
        if (repository.findByNumeroConselhoAndUfConselhoAndSiglaConselho(
                request.numeroConselho(), request.ufConselho(), request.siglaConselho()).isPresent()) {
            throw new IllegalArgumentException("Profissional já cadastrado.");
        }

        Cbo cbo = cboRepository.findByCodigo(request.cbo().codigo())
                .orElseThrow(() -> new EntityNotFoundException("CBO não encontrado."));

        ProfissionalSaude profissional = ProfissionalSaude.builder()
                .nome(request.nome())
                .siglaConselho(request.siglaConselho())
                .numeroConselho(request.numeroConselho())
                .ufConselho(request.ufConselho())
                .cbo(cbo)
                .ativo(true)
                .build();

        return ProfissionalSolicitanteMapper.converteEntidade(repository.save(profissional));
    }

    @Transactional
    @AuditUpdate(entidadeTipo = ProfissionalSaude.class)
    public ProfissionalSaudeResponse atualizar(UUID id, ProfissionalSaudeRequest request) {
        ProfissionalSaude profissional = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado."));

        Cbo cbo = cboRepository.findByCodigo(request.cbo().codigo())
                .orElseThrow(() -> new EntityNotFoundException("CBO não encontrado."));

        profissional.setNome(request.nome());
        profissional.setSiglaConselho(request.siglaConselho());
        profissional.setNumeroConselho(request.numeroConselho());
        profissional.setUfConselho(request.ufConselho());
        profissional.setCbo(cbo);

        return ProfissionalSolicitanteMapper.converteEntidade(repository.save(profissional));
    }

    @Transactional(readOnly = true)
    public List<ProfissionalSaudeResponse> listarTodos() {
        return repository.findAll().stream()
                .map(ProfissionalSolicitanteMapper::converteEntidade)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProfissionalSaudeResponse buscarPorId(UUID id) {
        return repository.findById(id)
                .map(ProfissionalSolicitanteMapper::converteEntidade)
                .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado."));
    }

    @Transactional(readOnly = true)
    public ProfissionalSaudeResponse buscarPorConselho(String numeroConselho) {
        return repository.findByNumeroConselhoContainingIgnoreCase(numeroConselho)
                .map(ProfissionalSolicitanteMapper::converteEntidade)
                .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado."));
    }
}