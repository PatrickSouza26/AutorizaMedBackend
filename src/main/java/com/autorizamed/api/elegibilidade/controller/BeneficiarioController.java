package com.autorizamed.api.elegibilidade.controller;

import com.autorizamed.api.elegibilidade.dto.request.AtualizarBeneficiarioRequest;
import com.autorizamed.api.elegibilidade.dto.request.BeneficiarioRequest;
import com.autorizamed.api.elegibilidade.dto.response.BeneficiarioResponse;
import com.autorizamed.api.elegibilidade.dto.response.ElegibilidadeResponse;
import com.autorizamed.api.elegibilidade.service.BeneficiarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/beneficiarios")
@RequiredArgsConstructor
public class BeneficiarioController {

    private final BeneficiarioService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BeneficiarioResponse> cadastrar(@RequestBody @Valid BeneficiarioRequest request) {
        BeneficiarioResponse response = service.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<BeneficiarioResponse>> listarBeneficiarios(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarBeneficiarios(busca, ativo, pageable));
    }

    @GetMapping("/{termoBusca}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BeneficiarioResponse> buscarPorCpfOuCarteirinha(@PathVariable String termoBusca) {
        BeneficiarioResponse response = service.buscarPorCpfOuCarteirinha(termoBusca);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BeneficiarioResponse> atualizarParcial(
            @PathVariable("id") UUID id,
            @RequestBody AtualizarBeneficiarioRequest request
    ) {
        BeneficiarioResponse response = service.atualizarParcial(id, request);
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

    @GetMapping("/{termoBusca}/elegibilidade")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ElegibilidadeResponse> verificarElegibilidade(
            @PathVariable String termoBusca,
            @RequestParam UUID prestadorId
    ) {
        ElegibilidadeResponse response = service.verificarElegibilidade(termoBusca, prestadorId);
        return ResponseEntity.ok(response);
    }
}
