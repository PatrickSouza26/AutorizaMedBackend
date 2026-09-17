package com.autorizamed.api.notificacao.entity;

import com.autorizamed.api.notificacao.enums.TipoDestinatario;
import com.autorizamed.api.notificacao.enums.TipoAviso;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "avisos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aviso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "destinatario_id", nullable = false)
    private UUID destinatarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_destinatario", nullable = false)
    private TipoDestinatario tipoDestinatario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_aviso", nullable = false)
    @Builder.Default
    private TipoAviso tipoAviso = TipoAviso.INFO;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mensagem;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(nullable = false)
    private boolean lido = false;

    @Column(name = "guia_id")
    private UUID guiaId;

    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
    }
}