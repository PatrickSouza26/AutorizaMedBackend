package com.autorizamed.api.autorizacao.controller;

import com.autorizamed.api.autorizacao.dto.request.CboRequest;
import com.autorizamed.api.autorizacao.dto.response.CboResponse;
import com.autorizamed.api.autorizacao.service.CboService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cbos")
@RequiredArgsConstructor
public class CboController {

    private final CboService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CboResponse> cadastrar(@RequestBody @Valid CboRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(request));
    }

    @PostMapping("/lote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CboResponse>> cadastrarLote(@RequestBody List<CboRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrarLote(requests));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CboResponse> atualizar(@PathVariable UUID id, @RequestBody @Valid CboRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PatchMapping("/{id}/inativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> inativar(@PathVariable UUID id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reativar(@PathVariable UUID id) {
        service.reativar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<org.springframework.data.domain.Page<CboResponse>> listarTodos(@org.springframework.data.web.PageableDefault(size = 50) org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(service.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CboResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CboResponse> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(service.buscarPorCodigo(codigo));
    }
}
