package com.autorizamed.api.autorizacao.repository;

import com.autorizamed.api.autorizacao.entity.GuiaAutorizacao;
import com.autorizamed.api.autorizacao.enums.StatusGuia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GuiaAutorizacaoRepository extends JpaRepository<GuiaAutorizacao, UUID> {
    List<GuiaAutorizacao> findByBeneficiarioCarteirinha(String carteirinha);
    List<GuiaAutorizacao> findByStatus(StatusGuia status);
    Optional<GuiaAutorizacao> findByNumeroGuia (String numeroGuia);

    @Query(value = "SELECT nextval('seq_numero_guia')", nativeQuery = true)
    Long getProximoNumeroGuia();

    @Query("SELECT COUNT(g) FROM GuiaAutorizacao g JOIN g.procedimentos p " +
            "WHERE g.beneficiario.id = :beneficiarioId " +
            "AND p.id = :procedimentoId " +
            "AND g.status NOT IN ('NEGADA', 'CANCELADA') " +
            "AND g.dataSolicitacao >= :dataCorte")
    long contarProcedimentoRecente(
            @Param("beneficiarioId") UUID beneficiarioId,
            @Param("procedimentoId") UUID procedimentoId,
            @Param("dataCorte") LocalDateTime dataCorte
    );
    //BUSCA TODAS
    @Query("SELECT g FROM GuiaAutorizacao g " +
            "WHERE g.status IN ('EM_ANALISE', 'PENDENCIA_RESPONDIDA') " +
            "ORDER BY " +
            "CASE g.caraterSolicitacao WHEN 'URGENCIA' THEN 1 ELSE 2 END ASC, " +
            "CASE g.status WHEN 'PENDENCIA_RESPONDIDA' THEN 1 ELSE 2 END ASC, " +
            "g.dataLimiteAprovacao ASC")
    Page<GuiaAutorizacao> buscarFilaAuditoria(Pageable pageable);

    //BUSCA URGENCIAS
    @Query("SELECT g FROM GuiaAutorizacao g " +
            "WHERE g.status IN ('EM_ANALISE', 'PENDENCIA_RESPONDIDA') " +
            "AND g.caraterSolicitacao = 'URGENCIA' " +
            "ORDER BY " +
            "CASE g.status WHEN 'PENDENCIA_RESPONDIDA' THEN 1 ELSE 2 END ASC, " +
            "g.dataLimiteAprovacao ASC")
    Page<GuiaAutorizacao> buscarFilaAuditoriaSomenteUrgencia(Pageable pageable);

    //BUSCA ELETIVAS
    @Query("SELECT g FROM GuiaAutorizacao g " +
            "WHERE g.status IN ('EM_ANALISE', 'PENDENCIA_RESPONDIDA') " +
            "AND g.caraterSolicitacao = 'ELETIVA' " +
            "ORDER BY " +
            "CASE g.status WHEN 'PENDENCIA_RESPONDIDA' THEN 1 ELSE 2 END ASC, " +
            "g.dataLimiteAprovacao ASC")
    Page<GuiaAutorizacao> buscarFilaAuditoriaSomenteEletiva(Pageable pageable);

}
