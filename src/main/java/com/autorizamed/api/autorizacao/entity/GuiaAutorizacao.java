package com.autorizamed.api.autorizacao.entity;

import com.autorizamed.api.autorizacao.enums.CaraterSolicitacao;
import com.autorizamed.api.autorizacao.enums.SiglaConselho;
import com.autorizamed.api.autorizacao.enums.StatusGuia;
import com.autorizamed.api.elegibilidade.entity.Beneficiario;
import com.autorizamed.api.elegibilidade.entity.Prestador;
import com.autorizamed.api.elegibilidade.entity.Procedimento;
import jakarta.persistence.*;
import lombok.*;
import com.autorizamed.api.auditoria.model.Auditavel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Entity
@Table(name = "tb_guia_autorizacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuiaAutorizacao implements Auditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String numeroGuia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiario_id", nullable = false)
    private Beneficiario beneficiario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestador_id", nullable = false)
    private Prestador prestador;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tb_guia_procedimento",
            joinColumns = @JoinColumn(name = "guia_id"),
            inverseJoinColumns = @JoinColumn(name = "procedimento_id")
    )
    @Builder.Default
    private List<Procedimento> procedimentos = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SiglaConselho siglaConselho;

    @Column(nullable = false, length = 2)
    private String ufConselho;

    @Column(nullable = false, length = 20)
    private String cbosProfissional;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaraterSolicitacao caraterSolicitacao;

    @Column(nullable = false)
    private LocalDateTime dataLimiteAprovacao;

    @Column(nullable = false)
    private boolean atendimentoRn;

    @Column(nullable = false)
    private boolean declaracaoAcidente;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String indicacaoClinica;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusGuia status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataSolicitacao;

    @Column(length = 255)
    private String motivoNegativa;

    @Column(length = 255)
    private String alertaSistema;

    @Column(length = 255)
    private String parecerAuditoria;


    @PrePersist
    protected void onCreate() {
        this.dataSolicitacao = LocalDateTime.now();
        if (this.status == null) {
            this.status = StatusGuia.EM_ANALISE;
        }
    }

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public String getNomeEntidade() {
        return "GuiaAutorizacao - " + this.id;
    }
}
