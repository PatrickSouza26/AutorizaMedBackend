package com.autorizamed.api.elegibilidade.repository;

import com.autorizamed.api.elegibilidade.entity.RedeCredenciada;
import com.autorizamed.api.elegibilidade.enums.TipoPlano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RedeCredenciadaRepository extends JpaRepository<RedeCredenciada, UUID> {
    Optional<RedeCredenciada> findByPrestadorIdAndProcedimentoId(UUID prestadorId, UUID procedimentoId);
    List<RedeCredenciada> findByPrestadorId(UUID prestadorId);

    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END 
        FROM RedeCredenciada r JOIN r.planosAceitos p 
        WHERE r.prestador.id = :prestadorId 
          AND p = :plano 
          AND r.ativo = true
    """)
    boolean prestadorAtende(
            @Param("prestadorId") UUID prestadorId,
            @Param("plano") TipoPlano plano
    );
}
