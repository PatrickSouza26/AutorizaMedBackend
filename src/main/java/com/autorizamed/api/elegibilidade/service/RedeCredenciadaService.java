package com.autorizamed.api.elegibilidade.service;

import com.autorizamed.api.elegibilidade.dto.request.AtualizarRedeCredenciadaRequest;
import com.autorizamed.api.elegibilidade.dto.request.RedeCredenciadaRequest;
import com.autorizamed.api.elegibilidade.dto.response.RedeCredenciadaResponse;
import com.autorizamed.api.elegibilidade.entity.Prestador;
import com.autorizamed.api.elegibilidade.entity.Procedimento;
import com.autorizamed.api.elegibilidade.entity.RedeCredenciada;
import com.autorizamed.api.elegibilidade.repository.PrestadorRepository;
import com.autorizamed.api.elegibilidade.repository.ProcedimentoRepository;
import com.autorizamed.api.elegibilidade.repository.RedeCredenciadaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedeCredenciadaService {

    private final RedeCredenciadaRepository redeRepository;
    private final PrestadorRepository prestadorRepository;
    private final ProcedimentoRepository procedimentoRepository;

    @Transactional
    public RedeCredenciadaResponse vincular(RedeCredenciadaRequest request) {
        Prestador prestador = prestadorRepository.findById(request.prestadorId())
                .orElseThrow(() -> new EntityNotFoundException("Prestador não encontrado."));

        Procedimento procedimento = procedimentoRepository.findById(request.procedimentoId())
                .orElseThrow(() -> new EntityNotFoundException("Procedimento não encontrado."));

        if (redeRepository.findByPrestadorIdAndProcedimentoId(prestador.getId(), procedimento.getId()).isPresent()) {
            throw new IllegalArgumentException("Este prestador já possui este procedimento vinculado.");
        }

        RedeCredenciada rede = RedeCredenciada.builder()
                .prestador(prestador)
                .procedimento(procedimento)
                .planosAceitos(request.planosAceitos())
                .ativo(true) // Regra: O contrato nasce ativo
                .build();

        rede = redeRepository.save(rede);
        return converterParaResponse(rede);
    }

    @Transactional(readOnly = true)
    public List<RedeCredenciadaResponse> listarPorPrestador(UUID prestadorId) {
        if (!prestadorRepository.existsById(prestadorId)) {
            throw new EntityNotFoundException("Prestador não encontrado.");
        }

        List<RedeCredenciada> lista = redeRepository.findByPrestadorId(prestadorId);

        return lista.stream()
                .map(this::converterParaResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RedeCredenciadaResponse atualizarPlanos(UUID id, AtualizarRedeCredenciadaRequest request) {
        RedeCredenciada rede = redeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vínculo de Rede Credenciada não encontrado."));

        if (request.planosAceitos() != null && !request.planosAceitos().isEmpty()) {
            rede.setPlanosAceitos(request.planosAceitos());
            rede = redeRepository.save(rede);
        }

        return converterParaResponse(rede);
    }

    @Transactional
    public void inativar(UUID id) {
        RedeCredenciada rede = redeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vínculo de Rede Credenciada não encontrado."));

        if (!rede.isAtivo()) {
            throw new IllegalArgumentException("Este vínculo já está inativo.");
        }

        rede.setAtivo(false);
        redeRepository.save(rede);
    }

    private RedeCredenciadaResponse converterParaResponse(RedeCredenciada entidade) {
        // Atualize seu RedeCredenciadaResponse para incluir o campo ativo, se desejar
        return new RedeCredenciadaResponse(
                entidade.getId(),
                entidade.getPrestador().getNome(),
                entidade.getPrestador().getDocumento(),
                entidade.getProcedimento().getCodigoTuss(),
                entidade.getProcedimento().getDescricao(),
                entidade.getPlanosAceitos()
        );
    }
}
