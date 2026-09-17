package com.autorizamed.api.autorizacao.repository;

import com.autorizamed.api.autorizacao.entity.Cbo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CboRepository extends JpaRepository<Cbo, UUID> {
    Optional<Cbo> findByCodigoContainingIgnoreCase(String codigo);
    Optional<Cbo> findByCodigo(String codigo);
}