package com.autorizamed.api.elegibilidade.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_prestador")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prestador {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    // CRM OU CNPJ
    @Column(nullable = false, unique = true, length = 20)
    private String documento;

    @Column(nullable = false, length = 200)
    private String endereco;

    @Column(length = 20)
    private String telefone;
}
