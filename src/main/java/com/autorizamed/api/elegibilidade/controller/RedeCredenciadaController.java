package com.autorizamed.api.elegibilidade.controller;

import com.autorizamed.api.elegibilidade.dto.request.AtualizarRedeCredenciadaRequest;
import com.autorizamed.api.elegibilidade.dto.request.RedeCredenciadaRequest;
import com.autorizamed.api.elegibilidade.dto.response.RedeCredenciadaResponse;
import com.autorizamed.api.elegibilidade.service.RedeCredenciadaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rede-credenciada")
@RequiredArgsConstructor
public class RedeCredenciadaController {

    private final RedeCredenciadaService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RedeCredenciadaResponse> vincularContrato(@RequestBody @Valid RedeCredenciadaRequest request) {
        RedeCredenciadaResponse response = service.vincular(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/prestador/{prestadorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RedeCredenciadaResponse>> listarPorPrestador(@PathVariable UUID prestadorId) {
        List<RedeCredenciadaResponse> response = service.listarPorPrestador(prestadorId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RedeCredenciadaResponse> atualizarPlanos(
            @PathVariable("id") UUID id,
            @RequestBody AtualizarRedeCredenciadaRequest request
    ) {
        RedeCredenciadaResponse response = service.atualizarPlanos(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/inativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> inativar(@PathVariable("id") UUID id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reativar(@PathVariable UUID id) {
        service.reativar(id);
        return ResponseEntity.noContent().build();
    }
}