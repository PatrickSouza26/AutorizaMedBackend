package com.autorizamed.api.autorizacao.entity;

import com.autorizamed.api.auditoria.model.Auditavel;
import com.autorizamed.api.autorizacao.enums.SiglaConselho;
import com.autorizamed.api.seguranca.entity.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tb_auditor")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Auditor extends Usuario{

    @Column(nullable = false, unique = true)
    private String cpf;

    @Column(nullable = false)
    private String numeroConselho;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SiglaConselho tipoConselho;

    @Column(nullable = false, length = 2)
    private String ufConselho;

    private String especialidade;

    @Column
    private boolean ativo;
}
