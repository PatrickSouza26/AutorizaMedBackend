package com.autorizamed.api.elegibilidade.entity;

import com.autorizamed.api.seguranca.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Entity
@Table(name = "tb_prestador")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Prestador extends Usuario {

    @Column(nullable = false, unique = true, length = 20)
    private String documento;

    @Column(nullable = false, unique = true, length = 30)
    private String numeroPrestador;

    @Column(nullable = false)
    private boolean ativo;

}