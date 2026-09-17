package com.autorizamed.api.notificacao.repository;

import com.autorizamed.api.notificacao.entity.Aviso;
import com.autorizamed.api.notificacao.enums.TipoDestinatario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AvisoRepository extends JpaRepository<Aviso, UUID> {

    Page<Aviso> findByDestinatarioIdAndTipoDestinatarioOrderByDataCriacaoDesc(
            UUID destinatarioId,
            TipoDestinatario tipo,
            Pageable pageable
    );

    long countByDestinatarioIdAndTipoDestinatarioAndLidoFalse(
            UUID destinatarioId,
            TipoDestinatario tipo
    );

    @Modifying
    @Query("UPDATE Aviso a SET a.lido = true WHERE a.destinatarioId = :destinatarioId AND a.tipoDestinatario = :tipo AND a.lido = false")
    void marcarTodosComoLidos(
            @Param("destinatarioId") UUID destinatarioId,
            @Param("tipo") TipoDestinatario tipo
    );
}