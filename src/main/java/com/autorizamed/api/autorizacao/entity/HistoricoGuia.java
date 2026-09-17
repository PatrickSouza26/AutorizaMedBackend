package com.autorizamed.api.autorizacao.entity;

import com.autorizamed.api.autorizacao.enums.StatusGuia;
import com.autorizamed.api.seguranca.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_historico_guia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoGuia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guia_id", nullable = false)
    private GuiaAutorizacao guia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusGuia statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusGuia statusNovo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataMovimentacao;

    @Column(columnDefinition = "TEXT")
    private String parecer;

    @Column(length = 255)
    private String motivoNegativa;

    @PrePersist
    protected void onCreate() {
        this.dataMovimentacao = LocalDateTime.now();
    }
}