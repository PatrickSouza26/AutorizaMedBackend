package com.autorizamed.api.autorizacao.repository;

import com.autorizamed.api.autorizacao.entity.ProfissionalSaude;
import com.autorizamed.api.autorizacao.enums.SiglaConselho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfissionalSaudeRepository extends JpaRepository<ProfissionalSaude, UUID> {
    Optional<ProfissionalSaude> findByNumeroConselhoAndUfConselhoAndSiglaConselho(String numeroConselho, String ufConselho, SiglaConselho siglaConselho);
    Optional<ProfissionalSaude> findByNumeroConselhoContainingIgnoreCase(String numeroConselho);
    Optional<ProfissionalSaude> findByNumeroConselho(String numeroConselho);
}