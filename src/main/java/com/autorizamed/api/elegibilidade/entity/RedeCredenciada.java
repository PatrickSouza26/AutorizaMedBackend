package com.autorizamed.api.elegibilidade.entity;

import jakarta.persistence.*;
import lombok.*;
import com.autorizamed.api.elegibilidade.enums.TipoPlano;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_rede_credenciada")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedeCredenciada {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestador_id", nullable = false)
    private Prestador prestador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_id", nullable = false)
    private Procedimento procedimento;

    @ElementCollection(targetClass = TipoPlano.class, fetch = FetchType.LAZY)
    @CollectionTable(
            name = "tb_rede_credenciada_planos",
            joinColumns = @JoinColumn(name = "rede_credenciada_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_plano", nullable = false)
    @Builder.Default
    private List<TipoPlano> planosAceitos = new ArrayList<>();
}