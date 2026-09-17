package com.autorizamed.api.elegibilidade.controller;

import com.autorizamed.api.elegibilidade.dto.request.AtualizarFuncionarioRequest;
import com.autorizamed.api.elegibilidade.dto.request.FuncionarioRequest;
import com.autorizamed.api.elegibilidade.dto.response.FuncionarioResponse;
import com.autorizamed.api.elegibilidade.service.FuncionarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/funcionarios")
@RequiredArgsConstructor
public class FuncionarioController {

    private final FuncionarioService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESTADOR')")
    public ResponseEntity<FuncionarioResponse> cadastrar(@RequestBody @Valid FuncionarioRequest request) {
        FuncionarioResponse response = service.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/prestador/{prestadorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESTADOR')")
    public ResponseEntity<List<FuncionarioResponse>> listarPorPrestador(@PathVariable UUID prestadorId) {
        List<FuncionarioResponse> response = service.listarPorPrestador(prestadorId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESTADOR')")
    public ResponseEntity<FuncionarioResponse> atualizarParcial(
            @PathVariable("id") UUID id,
            @RequestBody AtualizarFuncionarioRequest request
    ) {
        FuncionarioResponse response = service.atualizarParcial(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/inativar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESTADOR')")
    public ResponseEntity<Void> inativar(@PathVariable("id") UUID id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reativar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESTADOR')")
    public ResponseEntity<Void> reativar(@PathVariable UUID id) {
        service.reativar(id);
        return ResponseEntity.noContent().build();
    }
}