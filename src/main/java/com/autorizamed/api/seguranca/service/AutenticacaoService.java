package com.autorizamed.api.seguranca.service;

import com.autorizamed.api.auditoria.annotation.AuditLogin;
import com.autorizamed.api.elegibilidade.entity.Funcionario;
import com.autorizamed.api.elegibilidade.entity.Prestador;
import com.autorizamed.api.seguranca.dto.response.LoginResponse;
import com.autorizamed.api.seguranca.entity.Usuario;
import com.autorizamed.api.seguranca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AutenticacaoService implements UserDetailsService {

    private final TokenService tokenService;
    private final UsuarioRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByLogin(username);
    }

    @AuditLogin
    public LoginResponse gerarTokenAposAutenticacao(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();

        String tokenJWT = tokenService.gerarToken(usuarioLogado);

        UUID prestadorId = null;

        if (usuarioLogado instanceof Prestador prestador) {
            prestadorId = prestador.getId();
        } else if (usuarioLogado instanceof Funcionario funcionario) {
            if (funcionario.getPrestador() != null) {
                prestadorId = funcionario.getPrestador().getId();
            }
        }

        return new LoginResponse(
                tokenJWT,
                usuarioLogado.getId(),
                usuarioLogado.getNome(),
                usuarioLogado.getLogin(),
                usuarioLogado.getRole().name(),
                prestadorId
        );
    }
}