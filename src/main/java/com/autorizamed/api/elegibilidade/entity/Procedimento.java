package com.autorizamed.api.elegibilidade.entity;

import com.autorizamed.api.elegibilidade.enums.CategoriaProcedimento;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_procedimento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Procedimento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigoTuss;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaProcedimento categoria;

    @Column(nullable = false)
    private boolean requerAutorizacao;

    @Column(nullable = false)
    private boolean ativo;

}
