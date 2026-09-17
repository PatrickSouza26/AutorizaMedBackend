package com.autorizamed.api.elegibilidade.repository;

import com.autorizamed.api.elegibilidade.entity.Beneficiario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface BeneficiarioRepository extends JpaRepository<Beneficiario, UUID> {
    Optional<Beneficiario> findByCarteirinha(String carteirinha);
    Optional<Beneficiario> findByCpf(String cpf);
    long countByPlanoAtivoTrue();

    @Query(value = "SELECT nextval('seq_carteirinha_beneficiario')", nativeQuery = true)
    Long getProximoValorCarteirinha();

    @Query("SELECT b FROM Beneficiario b WHERE " +
            "(:termo IS NULL OR LOWER(b.nome) LIKE LOWER(CONCAT('%', CAST(:termo AS string), '%')) " +
            "OR b.cpf LIKE CONCAT('%', CAST(:termo AS string), '%') " +
            "OR b.carteirinha LIKE CONCAT('%', CAST(:termo AS string), '%')) " +
            "AND (:ativo IS NULL OR b.planoAtivo = :ativo)")
    Page<Beneficiario> buscarPorTermoEStatus(
            @Param("termo") String termo,
            @Param("ativo") Boolean ativo,
            Pageable pageable);
}
