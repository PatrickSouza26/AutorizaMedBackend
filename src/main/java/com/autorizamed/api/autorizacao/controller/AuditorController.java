package com.autorizamed.api.autorizacao.controller;

import com.autorizamed.api.autorizacao.dto.request.AuditorRequest;
import com.autorizamed.api.autorizacao.dto.request.AvaliacaoAuditorRequest;
import com.autorizamed.api.autorizacao.dto.response.AuditorResponse;
import com.autorizamed.api.autorizacao.dto.response.FilaKpiResponse;
import com.autorizamed.api.autorizacao.dto.response.GuiaResponse;
import com.autorizamed.api.autorizacao.enums.CaraterSolicitacao;
import com.autorizamed.api.autorizacao.service.AuditorService;
import com.autorizamed.api.seguranca.entity.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auditoria-medica")
@RequiredArgsConstructor
public class AuditorController {

    private final AuditorService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditorResponse> cadastrar(@RequestBody @Valid AuditorRequest request) {
        AuditorResponse response = service.cadastrarAuditor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditorResponse>> listarAuditores(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listarAuditores(busca, ativo, pageable));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditorResponse> atualizar(@PathVariable("id") UUID id, @RequestBody @Valid AuditorRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PatchMapping("/{id}/inativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> inativar(@PathVariable("id") UUID id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reativar(@PathVariable("id") UUID id) {
        service.reativar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/guias/fila")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<Page<GuiaResponse>> listarFila(
            @RequestParam(value = "filtro", required = false) CaraterSolicitacao filtro,
            @RequestParam(value = "busca", required = false) String busca,
            Pageable pageable
    ) {
        Page<GuiaResponse> fila = service.listarFilaDeTrabalho(filtro, busca, pageable);
        return ResponseEntity.ok(fila);
    }

    @GetMapping("/guias/fila/kpis")
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    public ResponseEntity<FilaKpiResponse> buscarKpisFila() {
        return ResponseEntity.ok(service.buscarKpisFila());
    }

    @PatchMapping("/guias/{id}/iniciar-analise")
    @PreAuthorize("hasRole('AUDITOR')")
    public ResponseEntity<GuiaResponse> iniciarAuditoria(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        UUID idAuditor = usuarioLogado.getId();
        GuiaResponse response = service.iniciarAuditoria(id, idAuditor);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/guias/{id}/negar")
    @PreAuthorize("hasRole('AUDITOR')")
    public ResponseEntity<GuiaResponse> negar(
            @PathVariable("id") UUID id,
            @RequestBody @Valid AvaliacaoAuditorRequest request,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        UUID idAuditor = usuarioLogado.getId();
        GuiaResponse response = service.negarGuia(id, idAuditor, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/guias/{id}/aprovar")
    @PreAuthorize("hasRole('AUDITOR')")
    public ResponseEntity<GuiaResponse> aprovar(
            @PathVariable("id") UUID id,
            @RequestBody(required = false) AvaliacaoAuditorRequest request,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        UUID idAuditor = usuarioLogado.getId();
        GuiaResponse response = service.aprovarGuia(id, idAuditor, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/guias/{id}/pendencia")
    @PreAuthorize("hasRole('AUDITOR')")
    public ResponseEntity<GuiaResponse> solicitarPendencia(
            @PathVariable("id") UUID id,
            @RequestBody @Valid AvaliacaoAuditorRequest request,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        UUID idAuditor = usuarioLogado.getId();
        GuiaResponse response = service.solicitarPendencia(id, idAuditor, request);
        return ResponseEntity.ok(response);
    }
}