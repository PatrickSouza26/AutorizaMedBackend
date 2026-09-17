package com.autorizamed.api.autorizacao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_cbo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cbo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false)
    private boolean ativo;

}