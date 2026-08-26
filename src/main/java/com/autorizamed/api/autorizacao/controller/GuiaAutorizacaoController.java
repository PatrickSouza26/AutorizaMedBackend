package com.autorizamed.api.autorizacao.controller;

import com.autorizamed.api.autorizacao.dto.request.SolicitarGuiaRequest;
import com.autorizamed.api.autorizacao.dto.response.GuiaResponse;
import com.autorizamed.api.autorizacao.service.GuiaAutorizacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/guias")
@RequiredArgsConstructor
public class GuiaAutorizacaoController {

    private final GuiaAutorizacaoService service;

    @PostMapping("/solicitar")
    public ResponseEntity<GuiaResponse> solicitarGuia(@RequestBody @Valid SolicitarGuiaRequest request) {
        GuiaResponse response = service.solicitar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/{id}/anexos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> fazerUploadAnexo(
            @PathVariable("id") UUID id,
            @RequestParam("arquivo") MultipartFile arquivo
    ) {
        service.anexarArquivo(id, arquivo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}/responder-pendencia")
    public ResponseEntity<GuiaResponse> responderPendencia(@PathVariable("id") UUID id) {
        GuiaResponse response = service.responderPendencia(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/paciente/{carteirinha}")
    public ResponseEntity<List<GuiaResponse>> listarPorPaciente(@PathVariable String carteirinha) {
        List<GuiaResponse> response = service.listarPorCarteirinha(carteirinha);
        return ResponseEntity.ok(response);
    }

}
