package com.autorizamed.api.config;

import com.autorizamed.api.autorizacao.entity.GuiaAutorizacao;
import com.autorizamed.api.autorizacao.entity.ProcedimentoGuia;
import com.autorizamed.api.autorizacao.enums.CaraterSolicitacao;
import com.autorizamed.api.autorizacao.enums.StatusGuia;
import com.autorizamed.api.autorizacao.enums.TipoSolicitacao;
import com.autorizamed.api.autorizacao.repository.GuiaAutorizacaoRepository;
import com.autorizamed.api.autorizacao.entity.Cbo;
import com.autorizamed.api.autorizacao.entity.ProfissionalSaude;
import com.autorizamed.api.autorizacao.enums.SiglaConselho;
import com.autorizamed.api.autorizacao.repository.CboRepository;
import com.autorizamed.api.autorizacao.repository.ProfissionalSaudeRepository;
import com.autorizamed.api.elegibilidade.entity.*;
import com.autorizamed.api.elegibilidade.enums.CategoriaProcedimento;
import com.autorizamed.api.elegibilidade.enums.TipoPlano;
import com.autorizamed.api.elegibilidade.repository.*;
import com.autorizamed.api.notificacao.entity.Aviso;
import com.autorizamed.api.notificacao.enums.TipoDestinatario;
import com.autorizamed.api.notificacao.repository.AvisoRepository;

import com.autorizamed.api.seguranca.entity.Usuario;
import com.autorizamed.api.autorizacao.entity.Auditor;
import com.autorizamed.api.seguranca.enums.RoleUsuario;
import com.autorizamed.api.seguranca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UsuarioRepository usuarioRepository;
    private final PrestadorRepository prestadorRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    private final ProcedimentoRepository procedimentoRepository;
    private final RedeCredenciadaRepository redeCredenciadaRepository;
    private final AvisoRepository avisoRepository;
    private final PasswordEncoder passwordEncoder;
    private final CboRepository cboRepository;
    private final ProfissionalSaudeRepository profissionalSaudeRepository;
    private final GuiaAutorizacaoRepository guiaAutorizacaoRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (guiaAutorizacaoRepository.count() > 50) {
                System.out.println("====== BANCO JA POSSUI GUIAS O SUFICIENTE, IGNORANDO SEEDER ======");
                return;
            }

            Usuario admin = (Usuario) usuarioRepository.findByLogin("admin2");
            if (admin == null) {
                admin = Usuario.builder()
                        .nome("Administrador do Sistema")
                        .email("admin@autorizamed.com")
                        .login("admin2")
                        .senha(passwordEncoder.encode("123456"))
                        .role(RoleUsuario.ADMIN)
                        .build();
                usuarioRepository.save(admin);
            }

            Prestador prestador = (Prestador) usuarioRepository.findByLogin("clinica");
            if (prestador == null) {
                prestador = new Prestador();
                prestador.setAtivo(true);
                prestador.setNome("Clinica Sao Lucas");
                prestador.setEmail("clinica@saolucas.com");
                prestador.setLogin("clinica");
                prestador.setSenha(passwordEncoder.encode("123456"));
                prestador.setRole(RoleUsuario.PRESTADOR);
                prestador.setDocumento("12345678000199");
                prestador.setNumeroPrestador("PREST-001");
                prestadorRepository.save(prestador);
            }

            Funcionario funcionario = (Funcionario) usuarioRepository.findByLogin("maria.recep");
            if (funcionario == null) {
                funcionario = new Funcionario();
                funcionario.setAtivo(true);
                funcionario.setNome("Maria Recepcionista");
                funcionario.setEmail("maria@saolucas.com");
                funcionario.setLogin("maria.recep");
                funcionario.setSenha(passwordEncoder.encode("123456"));
                funcionario.setRole(RoleUsuario.FUNCIONARIO_PRESTADOR);
                funcionario.setCpf("11144477733");
                funcionario.setPrestador(prestador);
                usuarioRepository.save(funcionario);
            }

            Beneficiario beneficiario = (Beneficiario) usuarioRepository.findByLogin("joao.silva");
            if (beneficiario == null) {
                beneficiario = new Beneficiario();
                beneficiario.setNome("Joao da Silva");
                beneficiario.setEmail("joao@email.com");
                beneficiario.setLogin("joao.silva");
                beneficiario.setSenha(passwordEncoder.encode("123456"));
                beneficiario.setRole(RoleUsuario.BENEFICIARIO);
                beneficiario.setCpf("99988877766");
                beneficiario.setDataNascimento(LocalDate.of(1990, 5, 15));
                beneficiario.setCarteirinha("100012345");
                beneficiario.setPlanoAtivo(true);
                beneficiario.setTipoPlano(TipoPlano.INTERMEDIARIO);
                beneficiario.setDataAdesao(LocalDate.now().minusMonths(2));
                beneficiarioRepository.save(beneficiario);
            }

            Beneficiario beneficiario2 = (Beneficiario) usuarioRepository.findByLogin("maria.souza");
            if (beneficiario2 == null) {
                beneficiario2 = new Beneficiario();
                beneficiario2.setNome("Maria de Souza");
                beneficiario2.setEmail("maria.souza@email.com");
                beneficiario2.setLogin("maria.souza");
                beneficiario2.setSenha(passwordEncoder.encode("123456"));
                beneficiario2.setRole(RoleUsuario.BENEFICIARIO);
                beneficiario2.setCpf("88877766655");
                beneficiario2.setDataNascimento(LocalDate.of(1985, 3, 20));
                beneficiario2.setCarteirinha("100054321");
                beneficiario2.setPlanoAtivo(true);
                beneficiario2.setTipoPlano(TipoPlano.PREMIUM);
                beneficiario2.setDataAdesao(LocalDate.now().minusMonths(12));
                beneficiarioRepository.save(beneficiario2);
            }

            Procedimento procedimento = procedimentoRepository.findAll().stream().findFirst().orElseGet(() -> {
                Procedimento p = Procedimento.builder()
                        .codigoTuss("10101012")
                        .descricao("Consulta em consultorio (no horario normal ou pre-estabelecido)")
                        .categoria(CategoriaProcedimento.CONSULTA)
                        .requerAutorizacao(false)
                        .ativo(true)
                        .build();
                return procedimentoRepository.save(p);
            });

            if (redeCredenciadaRepository.count() == 0) {
                RedeCredenciada redeCredenciada = RedeCredenciada.builder()
                        .prestador(prestador)
                        .procedimento(procedimento)
                        .ativo(true)
                        .planosAceitos(List.of(TipoPlano.PREMIUM, TipoPlano.INTERMEDIARIO))
                        .build();
                redeCredenciadaRepository.save(redeCredenciada);
            }

            Cbo cbo = cboRepository.findAll().stream().findFirst().orElseGet(() -> {
                Cbo c = Cbo.builder()
                        .codigo("225125")
                        .titulo("Medico Clinico")
                        .ativo(true)
                        .build();
                return cboRepository.save(c);
            });

            ProfissionalSaude profissionalSaude = profissionalSaudeRepository.findAll().stream().findFirst().orElseGet(() -> {
                ProfissionalSaude ps = ProfissionalSaude.builder()
                        .nome("Dr. Joao")
                        .siglaConselho(SiglaConselho.CRM)
                        .numeroConselho("52123123")
                        .ufConselho("RJ")
                        .cbo(cbo)
                        .ativo(true)
                        .build();
                return profissionalSaudeRepository.save(ps);
            });

            if (avisoRepository.count() == 0) {
                Aviso aviso = Aviso.builder()
                        .destinatarioId(prestador.getId())
                        .tipoDestinatario(TipoDestinatario.PRESTADOR)
                        .titulo("Bem-vindo ao Autorizamed")
                        .mensagem("Sistema inicializado com sucesso e pronto para uso.")
                        .dataCriacao(LocalDateTime.now())
                        .lido(false)
                        .build();
                avisoRepository.save(aviso);
            }

            Auditor auditor = (Auditor) usuarioRepository.findByLogin("auditor");
            if (auditor == null) {
                auditor = new Auditor();
                auditor.setNome("Auditor Chefe");
                auditor.setEmail("auditor@autorizamed.com");
                auditor.setLogin("auditor");
                auditor.setSenha(passwordEncoder.encode("123456"));
                auditor.setRole(RoleUsuario.AUDITOR);
                auditor.setAtivo(true);
                auditor.setCpf("11122233344");
                auditor.setNumeroConselho("123456");
                auditor.setTipoConselho(SiglaConselho.CRM);
                auditor.setUfConselho("SP");
                usuarioRepository.save(auditor);
            }

            StatusGuia[] statuses = StatusGuia.values();
            TipoSolicitacao[] tipos = TipoSolicitacao.values();

            // GERAR 80 GUIAS ALEATORIAS PARA POPULAR BEM A BASE DE DADOS
            for (int i = 1; i <= 80; i++) {
                GuiaAutorizacao guia = new GuiaAutorizacao();
                guia.setNumeroGuia("GUIA-MASS-" + System.currentTimeMillis() + "-" + i);

                // Variar beneficiario
                guia.setBeneficiario(i % 3 == 0 ? beneficiario2 : beneficiario);

                guia.setPrestador(prestador);
                guia.setProfissionalSolicitante(profissionalSaude);

                StatusGuia status;
                if (i <= 20) {
                    status = StatusGuia.EM_ANALISE;
                } else if (i > 20 && i <= 35) {
                    status = StatusGuia.PENDENCIA_RESPONDIDA;
                } else if (i > 35 && i <= 45) {
                    status = StatusGuia.PENDENCIA;
                } else {
                    status = statuses[i % statuses.length];
                }

                if (status == StatusGuia.AUTORIZADA || status == StatusGuia.NEGADA || status == StatusGuia.EM_AUDITORIA) {
                    guia.setAuditor(auditor);
                }

                guia.setStatus(status);

                guia.setCaraterSolicitacao(i % 5 == 0 ? CaraterSolicitacao.URGENCIA : CaraterSolicitacao.ELETIVA);
                guia.setTipoSolicitacao(tipos[i % tipos.length]);

                if (i % 7 == 0) {
                    guia.setDataLimiteAprovacao(LocalDateTime.now().minusDays(2)); // Atrasada
                } else if (i % 4 == 0) {
                    guia.setDataLimiteAprovacao(LocalDateTime.now().plusDays(1)); // Amanhã
                } else if (i % 3 == 0) {
                    guia.setDataLimiteAprovacao(LocalDateTime.now()); // Hoje
                } else {
                    guia.setDataLimiteAprovacao(LocalDateTime.now().plusDays(3)); // Futuro
                }

                
                guia.setIndicacaoClinica("Descricao detalhada do caso clinico do paciente gerada automaticamente - caso numero " + i);

                java.time.LocalDateTime randomDate = java.time.LocalDateTime.now().minusDays(i % 30);
                guia.setDataSolicitacao(randomDate);
                
                ProcedimentoGuia pg = new ProcedimentoGuia();
                pg.setProcedimento(procedimento);
                pg.setQuantidade(1);
                guia.adicionarProcedimento(pg);


                guia.setAtendimentoRn(i % 15 == 0);
                guia.setDeclaracaoAcidente(i % 25 == 0);

                if (status == StatusGuia.NEGADA) {
                    guia.setMotivoNegativa("Falta de cobertura contratual / Rol ANS");
                }
                if (status == StatusGuia.PENDENCIA || status == StatusGuia.PENDENCIA_RESPONDIDA) {
                    guia.setParecerAuditoria("Favor anexar laudo de ressonancia, exame anterior incompleto.");
                }

                guiaAutorizacaoRepository.save(guia);
            }

            System.out.println("====== 80 GUIAS POPULADAS COM SUCESSO E VARIEDADE DE STATUS! ======");
        };
    }
}
