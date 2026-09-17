package com.autorizamed.api.auditoria.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@Table(name= "tb_audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String acao;

    @Column
    private LocalDateTime data;

    @Column(name= "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "usuario_nome", nullable = false)
    private String usuarioNome;

    @Column(name = "usuario_ip", nullable = false)
    private String usuarioIp;

    @Column(name = "entidade_id")
    private UUID entidadeId;

    @Column(name = "entidade_nome", nullable = false)
    private String entidadeNome;

    @Column(name = "entidade_tipo", nullable = false)
    private String entidadeTipo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descricao;

    public AuditLog(){
        this.data = LocalDateTime.now();
    }
}
