package com.autorizamed.api.autorizacao.controller;

import com.autorizamed.api.autorizacao.dto.request.SolicitarGuiaRequest;
import com.autorizamed.api.autorizacao.dto.response.GuiaResponse;
import com.autorizamed.api.autorizacao.dto.response.AnexoDownloadDTO;
import com.autorizamed.api.autorizacao.service.GuiaAutorizacaoService;
import com.autorizamed.api.seguranca.entity.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.core.io.ByteArrayResource;
import com.autorizamed.api.autorizacao.entity.AnexoGuia;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/guias")
@RequiredArgsConstructor
public class GuiaAutorizacaoController {

    private final GuiaAutorizacaoService service;

    @PostMapping("/solicitar")
    @PreAuthorize("hasAnyRole('PRESTADOR', 'FUNCIONARIO_PRESTADOR')")
    public ResponseEntity<GuiaResponse> solicitarGuia(@RequestBody @Valid SolicitarGuiaRequest request) {
        GuiaResponse response = service.solicitar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/anexos/{idAnexo}")
    @PreAuthorize("hasAnyRole('PRESTADOR', 'FUNCIONARIO_PRESTADOR', 'AUDITOR', 'ADMIN')")
    public ResponseEntity<ByteArrayResource> baixarAnexo(@PathVariable("idAnexo") UUID idAnexo) {
        AnexoDownloadDTO dto = service.baixarAnexo(idAnexo);
        ByteArrayResource resource = new ByteArrayResource(dto.dados());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(dto.tipoArquivo()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dto.nomeArquivo() + "\"")
                .body(resource);
    }

    @PostMapping(value = "/{idGuia}/anexos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('PRESTADOR', 'FUNCIONARIO_PRESTADOR')")
    public ResponseEntity<Void> fazerUploadAnexo(
            @PathVariable("idGuia") UUID idGuia,
            @RequestParam("arquivo") MultipartFile arquivo
    ) {
        service.anexarArquivo(idGuia, arquivo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{idGuia}/responder-pendencia")
    @PreAuthorize("hasAnyRole('PRESTADOR', 'FUNCIONARIO_PRESTADOR')")
    public ResponseEntity<GuiaResponse> responderPendencia(
            @PathVariable("idGuia") UUID idGuia,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        GuiaResponse response = service.responderPendencia(idGuia, usuarioLogado);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESTADOR', 'FUNCIONARIO_PRESTADOR', 'AUDITOR', 'ADMIN')")
    public ResponseEntity<GuiaResponse> buscarPorId(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        GuiaResponse response = service.buscarPorId(id, usuarioLogado);
        return ResponseEntity.ok(response);
    }

    
    @GetMapping
    @PreAuthorize("hasAnyRole('PRESTADOR', 'FUNCIONARIO_PRESTADOR', 'AUDITOR', 'ADMIN')")
    public ResponseEntity<List<GuiaResponse>> listarGuias(@AuthenticationPrincipal Usuario usuarioLogado) {
        List<GuiaResponse> response = service.listarGuias(usuarioLogado);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/beneficiario")
    @PreAuthorize("hasAnyRole('PRESTADOR', 'FUNCIONARIO_PRESTADOR', 'AUDITOR', 'ADMIN')")
    public ResponseEntity<List<GuiaResponse>> listarPorPaciente(@RequestParam("carteirinha") String carteirinha) {
        List<GuiaResponse> response = service.listarPorCarteirinha(carteirinha);
        return ResponseEntity.ok(response);
    }

}
