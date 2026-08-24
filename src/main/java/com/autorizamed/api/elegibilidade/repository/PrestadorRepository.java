package com.autorizamed.api.elegibilidade.repository;

import com.autorizamed.api.elegibilidade.entity.Prestador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PrestadorRepository extends JpaRepository<Prestador, UUID> {
    Optional<Prestador> findByDocumento(String documento);
}