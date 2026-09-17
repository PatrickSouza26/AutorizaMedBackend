package com.autorizamed.api.autorizacao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_anexo_guia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnexoGuia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nomeArquivo;

    @Column(nullable = false)
    private String tipoArquivo;

    @OneToOne(mappedBy = "anexo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AnexoConteudo conteudo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guia_id", nullable = false)
    private GuiaAutorizacao guia;
}
