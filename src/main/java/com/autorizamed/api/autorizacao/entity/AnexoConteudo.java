package com.autorizamed.api.autorizacao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_anexo_conteudo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnexoConteudo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Lob
    @Column(nullable = false)
    private byte[] dados;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anexo_id", nullable = false)
    private AnexoGuia anexo;
}
