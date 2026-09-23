package com.autorizamed.api.auditoria.controller;

import com.autorizamed.api.auditoria.dto.AuditLogResponseDTO;
import com.autorizamed.api.auditoria.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESTADOR')")
    public ResponseEntity<Page<AuditLogResponseDTO>> buscarLogs(
            @RequestParam(required = false) String pesquisa,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) String tipoEntidade,
            @PageableDefault(size = 10, sort = "data", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(auditService.buscarLogs(pesquisa, acao, tipoEntidade, pageable));
    }
}