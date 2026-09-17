package com.autorizamed.api.autorizacao.repository;

import com.autorizamed.api.autorizacao.entity.Auditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface AuditorRepository extends JpaRepository<Auditor, UUID> {
    long countByAtivoTrue();

    @Query("SELECT a FROM Auditor a WHERE " +
            "(:termo IS NULL OR LOWER(a.nome) LIKE LOWER(CONCAT('%', CAST(:termo AS string), '%')) " +
            "OR a.cpf LIKE CONCAT('%', CAST(:termo AS string), '%') " +
            "OR a.numeroConselho LIKE CONCAT('%', CAST(:termo AS string), '%')) " +
            "AND (:ativo IS NULL OR a.ativo = :ativo)")
    Page<Auditor> buscarPorTermoEStatus(@Param("termo") String termo, @Param("ativo") Boolean ativo, Pageable pageable);
}