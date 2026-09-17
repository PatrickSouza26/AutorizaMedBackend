package com.autorizamed.api.elegibilidade.service;

import com.autorizamed.api.auditoria.annotation.AuditCreate;
import com.autorizamed.api.auditoria.annotation.AuditUpdate;
import com.autorizamed.api.auditoria.enums.AcaoAuditoria;
import com.autorizamed.api.elegibilidade.dto.request.AtualizarProcedimentoRequest;
import com.autorizamed.api.elegibilidade.dto.request.ProcedimentoRequest;
import com.autorizamed.api.elegibilidade.dto.response.ProcedimentoResponse;
import com.autorizamed.api.elegibilidade.entity.Procedimento;
import com.autorizamed.api.elegibilidade.mapper.ProcedimentoMapper;
import com.autorizamed.api.elegibilidade.repository.ProcedimentoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcedimentoService {

    private final ProcedimentoRepository repository;

    @Transactional
    @AuditCreate(entidadeTipo = Procedimento.class)
    public ProcedimentoResponse cadastrar(ProcedimentoRequest request) {
        if (repository.findByCodigoTuss(request.codigoTuss()).isPresent()) {
            throw new IllegalArgumentException("Já existe um procedimento cadastrado com este Código TUSS.");
        }

        Procedimento procedimento = Procedimento.builder()
                .codigoTuss(request.codigoTuss())
                .descricao(request.descricao())
                .categoria(request.categoria())
                .requerAutorizacao(request.requerAutorizacao())
                .ativo(true) //TODO PROCEDIMENTO "NASCE" ATIVO
                .build();

        procedimento = repository.save(procedimento);
        return ProcedimentoMapper.converteEntidade(procedimento);
    }

    @Transactional
    public List<ProcedimentoResponse> cadastrarLote(List<ProcedimentoRequest> requests) {
        List<ProcedimentoResponse> responses = new ArrayList<>();
        for (ProcedimentoRequest request : requests) {
            if (repository.findByCodigoTuss(request.codigoTuss()).isEmpty()) {
                responses.add(cadastrar(request));
            }
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ProcedimentoResponse> listarTodos(org.springframework.data.domain.Pageable pageable) {
        return repository.findAll(pageable).map(ProcedimentoMapper::converteEntidade);
    }

    public List<ProcedimentoResponse> buscar(String termoBusca) {
        List<Procedimento> procedimentos = repository.buscarPorCodigoOuDescricao(termoBusca);
        return procedimentos.stream().map(ProcedimentoMapper::converteEntidade).toList();
    }

    @Transactional
    @AuditUpdate(entidadeTipo = Procedimento.class)
    public ProcedimentoResponse atualizarParcial(UUID id, AtualizarProcedimentoRequest request) {
        Procedimento procedimento = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Procedimento não encontrado pelo ID informado."));

        boolean houveAlteracao = false;

        if (request.descricao() != null && !request.descricao().isBlank() && !request.descricao().equals(procedimento.getDescricao())) {
            procedimento.setDescricao(request.descricao());
            houveAlteracao = true;
        }

        if (request.categoria() != null && request.categoria() != procedimento.getCategoria()) {
            procedimento.setCategoria(request.categoria());
            houveAlteracao = true;
        }

        if (request.requerAutorizacao() != null && !request.requerAutorizacao().equals(procedimento.isRequerAutorizacao())) {
            procedimento.setRequerAutorizacao(request.requerAutorizacao());
            houveAlteracao = true;
        }

        if (houveAlteracao) {
            procedimento = repository.save(procedimento);
        }

        return ProcedimentoMapper.converteEntidade(procedimento);
    }

    @Transactional
    @AuditUpdate(entidadeTipo = Procedimento.class, acao = AcaoAuditoria.INATIVACAO)
    public ProcedimentoResponse inativar(UUID id) {
        Procedimento procedimento = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Procedimento não encontrado pelo ID informado."));

        if (!procedimento.isAtivo()) {
            throw new IllegalArgumentException("Este procedimento já está inativo no sistema.");
        }

        procedimento.setAtivo(false);
        Procedimento procedimentoSalvo = repository.save(procedimento);
        return ProcedimentoMapper.converteEntidade(procedimentoSalvo);
    }

    @Transactional
    @AuditUpdate(entidadeTipo = Procedimento.class, acao = AcaoAuditoria.REATIVACAO)
    public ProcedimentoResponse reativar(UUID id) {
        Procedimento procedimento = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Procedimento não encontrado pelo ID informado."));

        if (procedimento.isAtivo()) {
            throw new IllegalArgumentException("Este procedimento já está ativo no sistema.");
        }

        procedimento.setAtivo(true);
        Procedimento procedimentoSalvo = repository.save(procedimento);
        return ProcedimentoMapper.converteEntidade(procedimentoSalvo);
    }
}