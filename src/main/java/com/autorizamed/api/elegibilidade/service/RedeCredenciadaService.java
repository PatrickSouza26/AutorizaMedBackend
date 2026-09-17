package com.autorizamed.api.elegibilidade.service;

import com.autorizamed.api.auditoria.annotation.AuditCreate;
import com.autorizamed.api.auditoria.annotation.AuditUpdate;
import com.autorizamed.api.auditoria.enums.AcaoAuditoria;
import com.autorizamed.api.elegibilidade.dto.request.AtualizarRedeCredenciadaRequest;
import com.autorizamed.api.elegibilidade.dto.request.RedeCredenciadaRequest;
import com.autorizamed.api.elegibilidade.dto.response.RedeCredenciadaResponse;
import com.autorizamed.api.elegibilidade.entity.Prestador;
import com.autorizamed.api.elegibilidade.entity.Procedimento;
import com.autorizamed.api.elegibilidade.entity.RedeCredenciada;
import com.autorizamed.api.elegibilidade.mapper.RedeCredenciadaMapper;
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
    @AuditCreate(entidadeTipo = RedeCredenciada.class)
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
                .ativo(true)
                .build();

        rede = redeRepository.save(rede);
        return RedeCredenciadaMapper.converteEntidade(rede);
    }

    @Transactional(readOnly = true)
    public List<RedeCredenciadaResponse> listarPorPrestador(UUID prestadorId) {
        if (!prestadorRepository.existsById(prestadorId)) {
            throw new EntityNotFoundException("Prestador não encontrado.");
        }

        List<RedeCredenciada> lista = redeRepository.findByPrestadorId(prestadorId);

        return lista.stream()
                .map(RedeCredenciadaMapper::converteEntidade) // Method reference limpo
                .collect(Collectors.toList());
    }

    @Transactional
    @AuditUpdate(entidadeTipo = RedeCredenciada.class)
    public RedeCredenciadaResponse atualizarPlanos(UUID id, AtualizarRedeCredenciadaRequest request) {
        RedeCredenciada rede = redeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vínculo de Rede Credenciada não encontrado."));

        if (request.planosAceitos() != null && !request.planosAceitos().isEmpty()) {
            rede.setPlanosAceitos(request.planosAceitos());
            rede = redeRepository.save(rede);
        }

        return RedeCredenciadaMapper.converteEntidade(rede);
    }

    @Transactional
    @AuditUpdate(entidadeTipo = RedeCredenciada.class, acao = AcaoAuditoria.INATIVACAO)
    public RedeCredenciadaResponse inativar(UUID id) {
        RedeCredenciada rede = redeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vínculo de Rede Credenciada não encontrado."));

        if (!rede.isAtivo()) {
            throw new IllegalArgumentException("Este vínculo já está inativo.");
        }

        rede.setAtivo(false);
        RedeCredenciada redeSalva = redeRepository.save(rede);
        return RedeCredenciadaMapper.converteEntidade(redeSalva);
    }

    @Transactional
    @AuditUpdate(entidadeTipo = RedeCredenciada.class, acao = AcaoAuditoria.REATIVACAO)
    public RedeCredenciadaResponse reativar(UUID id) {
        RedeCredenciada rede = redeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vínculo de Rede Credenciada não encontrado."));

        if (rede.isAtivo()) {
            throw new IllegalArgumentException("Este vínculo já está ativo.");
        }

        rede.setAtivo(true);
        RedeCredenciada redeSalva = redeRepository.save(rede);
        return RedeCredenciadaMapper.converteEntidade(redeSalva);
    }
}