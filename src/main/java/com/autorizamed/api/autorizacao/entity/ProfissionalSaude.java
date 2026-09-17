package com.autorizamed.api.autorizacao.entity;

import com.autorizamed.api.auditoria.model.Auditavel;
import com.autorizamed.api.autorizacao.enums.SiglaConselho;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_profissional_solicitante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfissionalSaude {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SiglaConselho siglaConselho;

    @Column(nullable = false, length = 20)
    private String numeroConselho;

    @Column(nullable = false, length = 2)
    private String ufConselho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cbo_id", nullable = false)
    private Cbo cbo;

    @Column(nullable = false)
    boolean ativo;
}