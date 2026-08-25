package com.autorizamed.api.elegibilidade.repository;

import com.autorizamed.api.elegibilidade.entity.Beneficiario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface BeneficiarioRepository extends JpaRepository<Beneficiario, UUID> {
    Optional<Beneficiario> findByCarteirinha(String carteirinha);
    Optional<Beneficiario> findByCpf(String cpf);
    @Query(value = "SELECT nextval('seq_carteirinha_beneficiario')", nativeQuery = true)
    Long getProximoValorCarteirinha();
}
