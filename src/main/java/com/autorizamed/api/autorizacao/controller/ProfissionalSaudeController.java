package com.autorizamed.api.autorizacao.controller;

import com.autorizamed.api.autorizacao.dto.request.ProfissionalSaudeRequest;
import com.autorizamed.api.autorizacao.dto.response.ProfissionalSaudeResponse;
import com.autorizamed.api.autorizacao.service.ProfissionalSaudeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profissionais-solicitantes")
@RequiredArgsConstructor
public class ProfissionalSaudeController {

    private final ProfissionalSaudeService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESTADOR', 'FUNCIONARIO_PRESTADOR')")
    public ResponseEntity<ProfissionalSaudeResponse> cadastrar(@RequestBody @Valid ProfissionalSaudeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESTADOR', 'FUNCIONARIO_PRESTADOR')")
    public ResponseEntity<ProfissionalSaudeResponse> atualizar(@PathVariable UUID id, @RequestBody @Valid ProfissionalSaudeRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProfissionalSaudeResponse>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfissionalSaudeResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/conselho/{numeroConselho}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfissionalSaudeResponse> buscarPorConselho(@PathVariable String numeroConselho) {
        return ResponseEntity.ok(service.buscarPorConselho(numeroConselho));
    }
}