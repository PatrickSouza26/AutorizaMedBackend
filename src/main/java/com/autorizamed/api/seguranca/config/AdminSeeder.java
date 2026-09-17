package com.autorizamed.api.seguranca.config;

import com.autorizamed.api.seguranca.entity.Usuario;
import com.autorizamed.api.seguranca.enums.RoleUsuario;
import com.autorizamed.api.seguranca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        if (repository.findByLogin("admin") == null) {

            Usuario admin = Usuario.builder()
                    .nome("Administrador do Sistema")
                    .login("admin")
                    .email("admin@gmail.com")
                    .senha(passwordEncoder.encode("admin"))
                    .role(RoleUsuario.ADMIN)
                    .build();

            repository.save(admin);

            System.out.println("✅ Usuário Admin Master criado com sucesso no banco de dados!");
        }
    }
}