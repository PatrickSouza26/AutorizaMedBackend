package com.autorizamed.api.notificacao.service;

import com.autorizamed.api.elegibilidade.entity.Funcionario;
import com.autorizamed.api.elegibilidade.entity.Prestador;
import com.autorizamed.api.notificacao.dto.AvisoResponse;
import com.autorizamed.api.notificacao.entity.Aviso;
import com.autorizamed.api.notificacao.enums.TipoDestinatario;
import com.autorizamed.api.notificacao.repository.AvisoRepository;
import com.autorizamed.api.seguranca.entity.Usuario;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autorizamed.api.notificacao.enums.TipoAviso;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvisoService {

    private final AvisoRepository avisoRepository;


    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void criarAviso(UUID destinatarioId, TipoDestinatario tipo, String titulo, String mensagem, UUID guiaId, TipoAviso tipoAviso) {
        Aviso aviso = new Aviso();
        aviso.setDestinatarioId(destinatarioId);
        aviso.setTipoDestinatario(tipo);
        aviso.setTipoAviso(tipoAviso != null ? tipoAviso : TipoAviso.INFO);
        aviso.setTitulo(titulo);
        aviso.setMensagem(mensagem);
        aviso.setGuiaId(guiaId);

        avisoRepository.save(aviso);
    }


    @Transactional(readOnly = true)
    public Page<AvisoResponse> buscarAvisosPorDestinatario(UUID destinatarioId, TipoDestinatario tipo, Pageable pageable) {
        Page<Aviso> avisos = avisoRepository
                .findByDestinatarioIdAndTipoDestinatarioOrderByDataCriacaoDesc(destinatarioId, tipo, pageable);

        return avisos.map(AvisoResponse::converteEntidade);
    }

    @Transactional(readOnly = true)
    public long contarAvisosNaoLidos(UUID destinatarioId, TipoDestinatario tipo) {
        return avisoRepository.countByDestinatarioIdAndTipoDestinatarioAndLidoFalse(destinatarioId, tipo);
    }

    @Transactional
    public void marcarComoLido(UUID avisoId, Usuario usuarioLogado) {
        Aviso aviso = avisoRepository.findById(avisoId)
                .orElseThrow(() -> new EntityNotFoundException("Aviso não encontrado com ID: " + avisoId));

        UUID prestadorId = getPrestadorId(usuarioLogado);
        boolean podeAcessar = aviso.getDestinatarioId().equals(usuarioLogado.getId()) || 
                (prestadorId != null && aviso.getDestinatarioId().equals(prestadorId));

        if (!podeAcessar) {
            throw new AccessDeniedException("Você não tem permissão para alterar um aviso que pertence a outro destinatário.");
        }

        aviso.setLido(true);
        avisoRepository.save(aviso);
    }

    @Transactional
    public void marcarTodosComoLidos(UUID destinatarioId, TipoDestinatario tipo, Usuario usuarioLogado) {
        UUID prestadorId = getPrestadorId(usuarioLogado);
        boolean podeAcessar = destinatarioId.equals(usuarioLogado.getId()) || 
                (prestadorId != null && destinatarioId.equals(prestadorId));

        if (!podeAcessar) {
            throw new AccessDeniedException("Você não tem permissão para alterar os avisos deste destinatário.");
        }

        avisoRepository.marcarTodosComoLidos(destinatarioId, tipo);
    }

    private UUID getPrestadorId(Usuario usuario) {
        if (usuario instanceof Funcionario funcionario) {
            return funcionario.getPrestador() != null ? funcionario.getPrestador().getId() : null;
        } else if (usuario instanceof Prestador) {
            return usuario.getId();
        }
        return null;
    }
}