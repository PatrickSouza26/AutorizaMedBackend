package com.autorizamed.api.elegibilidade.controller;

import com.autorizamed.api.elegibilidade.dto.request.AtualizarPrestadorRequest;
import com.autorizamed.api.elegibilidade.dto.request.PrestadorRequest;
import com.autorizamed.api.elegibilidade.dto.response.PrestadorResponse;
import com.autorizamed.api.elegibilidade.service.PrestadorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/prestadores")
@RequiredArgsConstructor
public class PrestadorController {

    private final PrestadorService service;

    @PostMapping
    public ResponseEntity<PrestadorResponse> cadastrar(@RequestBody @Valid PrestadorRequest request) {
        PrestadorResponse response = service.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{documento}")
    public ResponseEntity<PrestadorResponse> buscarPorDocumento(@PathVariable String documento) {
        PrestadorResponse response = service.buscarPorDocumento(documento);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PrestadorResponse> atualizarParcial(
            @PathVariable("id") UUID id,
            @RequestBody AtualizarPrestadorRequest request
    ) {
        PrestadorResponse response = service.atualizarParcial(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable("id") UUID id) {
        service.inativar(id);
        return ResponseEntity.noContent().build(); // 204 - NO CONTENT
    }
}
