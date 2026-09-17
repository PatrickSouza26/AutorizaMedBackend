package com.autorizamed.api.elegibilidade.repository;

import com.autorizamed.api.elegibilidade.entity.Prestador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PrestadorRepository extends JpaRepository<Prestador, UUID> {
    Optional<Prestador> findByDocumento(String documento);
    long countByAtivoTrue();

    @Query(value = "SELECT nextval('seq_numero_prestador')", nativeQuery = true)
    Long getProximoValorPrestador();

    @Query("SELECT p FROM Prestador p WHERE " +
            "(:termo IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', CAST(:termo AS string), '%')) " +
            "OR p.documento LIKE CONCAT('%', CAST(:termo AS string), '%')) " +
            "AND (:ativo IS NULL OR p.ativo = :ativo)")
    Page<Prestador> buscarPorTermoEStatus(@Param("termo") String termo, @Param("ativo") Boolean ativo, Pageable pageable);
}