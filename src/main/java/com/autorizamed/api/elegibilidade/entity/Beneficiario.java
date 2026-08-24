package com.autorizamed.api.elegibilidade.entity;

import jakarta.persistence.*;
import lombok.*;
import com.autorizamed.api.elegibilidade.enums.TipoPlano;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tb_beneficiario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Beneficiario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 14) // Considera a máscara 000.000.000-00
    private String cpf;

    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Column(length = 200)
    private String endereco;

    @Column(nullable = false, unique = true, length = 20)
    private String carteirinha;

    @Column(nullable = false)
    private boolean planoAtivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPlano tipoPlano;

}
