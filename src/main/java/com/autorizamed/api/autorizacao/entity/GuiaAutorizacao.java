package com.autorizamed.api.autorizacao.entity;

import com.autorizamed.api.autorizacao.enums.CaraterSolicitacao;
import com.autorizamed.api.autorizacao.enums.StatusGuia;
import com.autorizamed.api.autorizacao.enums.TipoSolicitacao;
import com.autorizamed.api.elegibilidade.entity.Beneficiario;
import com.autorizamed.api.elegibilidade.entity.Funcionario;
import com.autorizamed.api.elegibilidade.entity.Prestador;
import com.autorizamed.api.seguranca.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

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
public class GuiaAutorizacao {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_solicitante_id", nullable = false)
    private ProfissionalSaude profissionalSolicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_executante_id")
    private ProfissionalSaude profissionalExecutante;

    @OneToMany(mappedBy = "guia", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProcedimentoGuia> procedimentos = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auditor_id")
    private Usuario auditor;

    @OneToMany(mappedBy = "guia", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataMovimentacao DESC")
    @Builder.Default
    private List<HistoricoGuia> historicos = new ArrayList<>();

    @OneToMany(mappedBy = "guia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AnexoGuia> anexos = new ArrayList<>();

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
    @Column(name = "tipo_solicitacao", nullable = false)
    private TipoSolicitacao tipoSolicitacao;

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
        if (this.dataSolicitacao == null) {
            this.dataSolicitacao = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = StatusGuia.EM_ANALISE;
        }
    }

    
    public void adicionarProcedimento(ProcedimentoGuia item) {
        procedimentos.add(item);
        item.setGuia(this);
    }

    public void adicionarHistorico(HistoricoGuia historico) {
        historicos.add(historico);
        historico.setGuia(this);
    }

    public void registrarHistorico(Usuario usuario, StatusGuia novoStatus, String parecer, String motivoNegativa) {
        StatusGuia statusAnterior = this.status != null ? this.status : novoStatus;

        HistoricoGuia historico = HistoricoGuia.builder()
                .usuario(usuario)
                .statusAnterior(statusAnterior)
                .statusNovo(novoStatus)
                .parecer(parecer)
                .motivoNegativa(motivoNegativa)
                .build();

        this.adicionarHistorico(historico);
        this.status = novoStatus;

        if (motivoNegativa != null) {
            this.motivoNegativa = motivoNegativa;
        }
    }

    public LocalDateTime getDataUltimaMovimentacao() {
        if (this.historicos == null || this.historicos.isEmpty()) {
            return this.dataSolicitacao;
        }

        return this.historicos.stream()
                .map(HistoricoGuia::getDataMovimentacao)
                .max(LocalDateTime::compareTo)
                .orElse(this.dataSolicitacao);
    }
}
