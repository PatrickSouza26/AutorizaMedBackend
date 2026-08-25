package com.autorizamed.api.elegibilidade.repository;

import com.autorizamed.api.elegibilidade.entity.RedeCredenciada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RedeCredenciadaRepository extends JpaRepository<RedeCredenciada, UUID> {
    Optional<RedeCredenciada> findByPrestadorIdAndProcedimentoId(UUID prestadorId, UUID procedimentoId);
    List<RedeCredenciada> findByPrestadorId(UUID prestadorId);
}
