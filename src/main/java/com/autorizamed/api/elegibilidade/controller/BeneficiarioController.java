package com.autorizamed.api.elegibilidade.controller;

import com.autorizamed.api.elegibilidade.dto.request.AtualizarBeneficiarioRequest;
import com.autorizamed.api.elegibilidade.dto.request.BeneficiarioRequest;
import com.autorizamed.api.elegibilidade.dto.response.BeneficiarioResponse;
import com.autorizamed.api.elegibilidade.service.BeneficiarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/beneficiarios")
@RequiredArgsConstructor
public class BeneficiarioController {

    private final BeneficiarioService service;

    @PostMapping
    public ResponseEntity<BeneficiarioResponse> cadastrar(@RequestBody @Valid BeneficiarioRequest request) {
        BeneficiarioResponse response = service.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{termoBusca}")
    public ResponseEntity<BeneficiarioResponse> buscarPorCpfOuCarteirinha(@PathVariable String termoBusca) {
        BeneficiarioResponse response = service.buscarPorCpfOuCarteirinha(termoBusca);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BeneficiarioResponse> atualizarParcial(
            @PathVariable("id") UUID id,
            @RequestBody AtualizarBeneficiarioRequest request
    ) {
        BeneficiarioResponse response = service.atualizarParcial(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable("id") UUID id) {
        service.inativar(id);
        return ResponseEntity.noContent().build(); // 204 - NO CONTENT
    }
}
