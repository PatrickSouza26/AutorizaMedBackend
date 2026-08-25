package com.autorizamed.api.elegibilidade.service;

import com.autorizamed.api.elegibilidade.dto.request.AtualizarProcedimentoRequest;
import com.autorizamed.api.elegibilidade.dto.request.ProcedimentoRequest;
import com.autorizamed.api.elegibilidade.dto.response.ProcedimentoResponse;
import com.autorizamed.api.elegibilidade.entity.Procedimento;
import com.autorizamed.api.elegibilidade.repository.ProcedimentoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcedimentoService {

    private final ProcedimentoRepository repository;

    @Transactional
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
        return converterParaResponse(procedimento);
    }

    @Transactional(readOnly = true)
    public ProcedimentoResponse buscarPorCodigo(String codigoTuss) {
        Procedimento procedimento = repository.findByCodigoTuss(codigoTuss)
                .orElseThrow(() -> new EntityNotFoundException("Procedimento não encontrado pelo código TUSS informado."));
        return converterParaResponse(procedimento);
    }

    @Transactional
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

        return converterParaResponse(procedimento);
    }

    @Transactional
    public void inativar(UUID id) {
        Procedimento procedimento = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Procedimento não encontrado pelo ID informado."));

        if (!procedimento.isAtivo()) {
            throw new IllegalArgumentException("Este procedimento já está inativo no sistema.");
        }

        procedimento.setAtivo(false);
        repository.save(procedimento);
    }

    private ProcedimentoResponse converterParaResponse(Procedimento entidade) {
        return new ProcedimentoResponse(
                entidade.getId(),
                entidade.getCodigoTuss(),
                entidade.getDescricao(),
                entidade.getCategoria(),
                entidade.isRequerAutorizacao(),
                entidade.isAtivo()
        );
    }
}
