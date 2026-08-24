package com.autorizamed.api.elegibilidade.repository;

import com.autorizamed.api.elegibilidade.entity.Procedimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcedimentoRepository extends JpaRepository<Procedimento, UUID> {
    Optional<Procedimento> findByCodigoTuss(String codigoTuss);
    List<Procedimento> findAllByCodigoTussIn(List<String> codigosTuss);
}
