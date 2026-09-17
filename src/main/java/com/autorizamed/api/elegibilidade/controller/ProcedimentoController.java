package com.autorizamed.api.elegibilidade.controller;

import com.autorizamed.api.elegibilidade.dto.request.AtualizarProcedimentoRequest;
import com.autorizamed.api.elegibilidade.dto.request.ProcedimentoRequest;
import com.autorizamed.api.elegibilidade.dto.response.ProcedimentoResponse;
import com.autorizamed.api.elegibilidade.service.ProcedimentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/procedimentos")
@RequiredArgsConstructor
public class ProcedimentoController {

    private final ProcedimentoService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProcedimentoResponse> cadastrar(@RequestBody @Valid ProcedimentoRequest request) {
        ProcedimentoResponse response = service.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/lote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProcedimentoResponse>> cadastrarLote(@RequestBody List<ProcedimentoRequest> requests) {
        List<ProcedimentoResponse> response = service.cadastrarLote(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<org.springframework.data.domain.Page<ProcedimentoResponse>> listarTodos(
            @org.springframework.data.web.PageableDefault(size = 50) org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(service.listarTodos(pageable));
    }

    @GetMapping("/{termoBusca}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProcedimentoResponse>> buscar(
            @PathVariable("termoBusca") String termoBusca
    ) {
        List<ProcedimentoResponse> response = service.buscar(termoBusca);
        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProcedimentoResponse> atualizarParcial(
            @PathVariable("id") UUID id,
            @RequestBody AtualizarProcedimentoRequest request
    ) {
        ProcedimentoResponse response = service.atualizarParcial(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/inativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> inativar(@PathVariable("id") UUID id) {
        service.inativar(id);
        return ResponseEntity.noContent().build(); // 204 - NO CONTENT
    }

    @PatchMapping("/{id}/reativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reativar(@PathVariable UUID id) {
        service.reativar(id);
        return ResponseEntity.noContent().build();
    }
}