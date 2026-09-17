package com.autorizamed.api.seguranca.controller;

import com.autorizamed.api.seguranca.dto.request.AutenticacaoRequest;
import com.autorizamed.api.seguranca.dto.response.LoginResponse;
import com.autorizamed.api.seguranca.service.AutenticacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AutenticacaoController {

    private final AuthenticationManager manager;
    private final AutenticacaoService autenticacaoService;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> efetuarLogin(@RequestBody @Valid AutenticacaoRequest dados) {

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(dados.login(), dados.senha());

        Authentication authentication = manager.authenticate(authToken);

        LoginResponse loginResponse = autenticacaoService.gerarTokenAposAutenticacao(authentication);

        return ResponseEntity.ok(loginResponse);
    }
}