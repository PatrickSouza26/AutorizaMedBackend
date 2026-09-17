package com.autorizamed.api.elegibilidade.controller;

import com.autorizamed.api.elegibilidade.dto.request.AtualizarPrestadorRequest;
import com.autorizamed.api.elegibilidade.dto.request.PrestadorRequest;
import com.autorizamed.api.elegibilidade.dto.response.PrestadorResponse;
import com.autorizamed.api.elegibilidade.service.PrestadorService;
import com.autorizamed.api.seguranca.entity.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/prestadores")
@RequiredArgsConstructor
public class PrestadorController {

    private final PrestadorService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PrestadorResponse> cadastrar(@RequestBody @Valid PrestadorRequest request) {
        PrestadorResponse response = service.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<org.springframework.data.domain.Page<PrestadorResponse>> listar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Boolean ativo,
            org.springframework.data.domain.Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarPrestadores(busca, ativo, pageable));
    }

    @GetMapping("/{documento}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PrestadorResponse> buscarPorDocumento(@PathVariable String documento) {
        PrestadorResponse response = service.buscarPorDocumento(documento);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESTADOR')")
    public ResponseEntity<PrestadorResponse> atualizarParcial(
            @PathVariable("id") UUID id,
            @RequestBody AtualizarPrestadorRequest request,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        PrestadorResponse response = service.atualizarParcial(id, request, usuarioLogado);
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