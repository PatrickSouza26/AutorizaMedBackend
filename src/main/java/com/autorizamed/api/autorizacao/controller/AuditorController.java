package com.autorizamed.api.autorizacao.controller;

import com.autorizamed.api.autorizacao.dto.request.AvaliacaoAuditorRequest;
import com.autorizamed.api.autorizacao.dto.response.GuiaResponse;
import com.autorizamed.api.autorizacao.enums.CaraterSolicitacao;
import com.autorizamed.api.autorizacao.service.AuditorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditorController {

    private final AuditorService service;

    @GetMapping("/guias/fila")
    public ResponseEntity<Page<GuiaResponse>> listarFila(
            @RequestParam(value = "filtro", required = false) CaraterSolicitacao filtro,
            Pageable pageable
    ) {
        Page<GuiaResponse> fila = service.listarFilaDeTrabalho(filtro, pageable);
        return ResponseEntity.ok(fila);
    }

    @PatchMapping("/guias/{id}/iniciar-analise")
    public ResponseEntity<GuiaResponse> iniciarAuditoria(@PathVariable("id") UUID id) {
        GuiaResponse response = service.iniciarAuditoria(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/guias/{id}/negar")
    public ResponseEntity<GuiaResponse> negar(
            @PathVariable("id") UUID id,
            @RequestBody @Valid AvaliacaoAuditorRequest request
    ) {
        GuiaResponse response = service.negarGuia(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/guias/{id}/aprovar")
    public ResponseEntity<GuiaResponse> aprovar(@PathVariable("id") UUID id) {
        GuiaResponse response = service.aprovarGuia(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/guias/{id}/pendencia")
    public ResponseEntity<GuiaResponse> solicitarPendencia(
            @PathVariable("id") UUID id,
            @RequestBody @Valid AvaliacaoAuditorRequest request
    ) {
        GuiaResponse response = service.solicitarPendencia(id, request);
        return ResponseEntity.ok(response);
    }
}