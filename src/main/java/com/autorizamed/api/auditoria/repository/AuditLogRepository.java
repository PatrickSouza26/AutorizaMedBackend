package com.autorizamed.api.auditoria.repository;

import com.autorizamed.api.auditoria.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:acao IS NULL OR a.acao = :acao) AND " +
            "(:tipoEntidade IS NULL OR a.entidadeTipo = :tipoEntidade) AND " +
            "(:usuarioIds IS NULL OR a.usuarioId IN :usuarioIds) AND " +
            "(CAST(:pesquisa AS string) IS NULL OR " +
            "  LOWER(a.usuarioNome) LIKE LOWER(CONCAT('%', CAST(:pesquisa AS string), '%')) OR " +
            "  LOWER(a.entidadeNome) LIKE LOWER(CONCAT('%', CAST(:pesquisa AS string), '%')) OR " +
            "  LOWER(a.descricao) LIKE LOWER(CONCAT('%', CAST(:pesquisa AS string), '%')))")
    Page<AuditLog> buscarLogsComFiltros(
            @Param("pesquisa") String pesquisa,
            @Param("acao") String acao,
            @Param("tipoEntidade") String tipoEntidade,
            @Param("usuarioIds") List<UUID> usuarioIds,
            Pageable pageable
    );

    Page<AuditLog> findByUsuarioId(UUID usuarioId, Pageable pageable);
}
