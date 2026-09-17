package com.autorizamed.api.seguranca.filter;

import com.autorizamed.api.seguranca.repository.UsuarioRepository;
import com.autorizamed.api.seguranca.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioRepository repository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // EXCEÇÃO PARA LOGIN
        if (request.getRequestURI().contains("/api/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        //PEGA O TOKEN ENVIADO DO FRONT
        String tokenJWT = recuperarToken(request);

        // SE EXISTE, VALIDA
        if (tokenJWT != null) {


            String login = tokenService.getSubject(tokenJWT);
            UserDetails usuario = repository.findByLogin(login);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null) {
            return authorizationHeader.replace("Bearer ", "");
        }

        return null;
    }
}