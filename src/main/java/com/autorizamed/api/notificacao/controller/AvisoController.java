package com.autorizamed.api.notificacao.controller;

import com.autorizamed.api.notificacao.dto.AvisoResponse;
import com.autorizamed.api.notificacao.enums.TipoDestinatario;
import com.autorizamed.api.notificacao.service.AvisoService;
import com.autorizamed.api.seguranca.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/avisos")
@RequiredArgsConstructor
public class AvisoController {

    private final AvisoService avisoService;

    @GetMapping("/{tipo}/{destinatarioId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AvisoResponse>> listarAvisos(
            @PathVariable TipoDestinatario tipo,
            @PathVariable UUID destinatarioId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanho) {

        PageRequest pageRequest = PageRequest.of(pagina, tamanho);
        Page<AvisoResponse> response = avisoService.buscarAvisosPorDestinatario(destinatarioId, tipo, pageRequest);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{tipo}/{destinatarioId}/nao-lidos/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> contarNaoLidos(
            @PathVariable TipoDestinatario tipo,
            @PathVariable UUID destinatarioId) {

        long quantidade = avisoService.contarAvisosNaoLidos(destinatarioId, tipo);
        return ResponseEntity.ok(quantidade);
    }

    @PatchMapping("/{avisoId}/lido")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> marcarComoLido(
            @PathVariable UUID avisoId,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        avisoService.marcarComoLido(avisoId, usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{tipo}/{destinatarioId}/ler-todos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> marcarTodosComoLidos(
            @PathVariable TipoDestinatario tipo,
            @PathVariable UUID destinatarioId,
            @AuthenticationPrincipal Usuario usuarioLogado) {
        avisoService.marcarTodosComoLidos(destinatarioId, tipo, usuarioLogado);
        return ResponseEntity.noContent().build();
    }
}