package com.autorizamed.api.elegibilidade.entity;

import com.autorizamed.api.auditoria.model.Auditavel;
import com.autorizamed.api.seguranca.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;
import com.autorizamed.api.elegibilidade.enums.TipoPlano;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "tb_beneficiario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Beneficiario extends Usuario {

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false, unique = true, length = 20)
    private String carteirinha;

    @Column(nullable = false)
    private boolean planoAtivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPlano tipoPlano;

    @Column(name = "data_adesao", nullable = false)
    private LocalDate dataAdesao;

    public boolean temCarencia() {
//        if (this.dataAdesao == null) return false;
//
//        return LocalDate.now().isBefore(this.dataAdesao.plusDays(30));
        return false;
    }

}
