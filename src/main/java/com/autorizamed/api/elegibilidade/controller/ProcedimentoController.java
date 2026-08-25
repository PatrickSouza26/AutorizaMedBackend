package com.autorizamed.api.elegibilidade.controller;

import com.autorizamed.api.elegibilidade.dto.request.AtualizarProcedimentoRequest;
import com.autorizamed.api.elegibilidade.dto.request.ProcedimentoRequest;
import com.autorizamed.api.elegibilidade.dto.response.ProcedimentoResponse;
import com.autorizamed.api.elegibilidade.service.ProcedimentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/procedimentos")
@RequiredArgsConstructor
public class ProcedimentoController {

    private final ProcedimentoService service;

    @PostMapping
    public ResponseEntity<ProcedimentoResponse> cadastrar(@RequestBody @Valid ProcedimentoRequest request) {
        ProcedimentoResponse response = service.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{codigoTuss}")
    public ResponseEntity<ProcedimentoResponse> buscarPorCodigo(
            @PathVariable("codigoTuss") String codigoTuss
    ) {
        ProcedimentoResponse response = service.buscarPorCodigo(codigoTuss);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProcedimentoResponse> atualizarParcial(
            @PathVariable("id") UUID id,
            @RequestBody AtualizarProcedimentoRequest request
    ) {
        ProcedimentoResponse response = service.atualizarParcial(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable("id") UUID id) {
        service.inativar(id);
        return ResponseEntity.noContent().build(); // 204 - NO CONTENT
    }
}