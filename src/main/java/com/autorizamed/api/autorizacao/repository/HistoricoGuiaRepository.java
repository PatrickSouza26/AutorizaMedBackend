package com.autorizamed.api.autorizacao.repository;

import com.autorizamed.api.autorizacao.entity.HistoricoGuia;
import com.autorizamed.api.relatorio.dto.DecisaoAuditorDTO;
import com.autorizamed.api.relatorio.dto.SlaGuiaDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface HistoricoGuiaRepository extends JpaRepository<HistoricoGuia, UUID> {
    // SLA GERAL
    @Query("""
        SELECT new com.autorizamed.api.relatorio.dto.SlaGuiaDTO(
            h.guia.id,
            MIN(CASE WHEN h.statusNovo IN (com.autorizamed.api.autorizacao.enums.StatusGuia.EM_ANALISE, com.autorizamed.api.autorizacao.enums.StatusGuia.EM_AUDITORIA) THEN h.dataMovimentacao END),
            MAX(CASE WHEN h.statusNovo IN (com.autorizamed.api.autorizacao.enums.StatusGuia.AUTORIZADA, com.autorizamed.api.autorizacao.enums.StatusGuia.NEGADA) THEN h.dataMovimentacao END)
        )
        FROM HistoricoGuia h
        WHERE h.dataMovimentacao >= :dataInicio AND h.dataMovimentacao <= :dataFim
        GROUP BY h.guia.id
        HAVING MIN(CASE WHEN h.statusNovo IN (com.autorizamed.api.autorizacao.enums.StatusGuia.EM_ANALISE, com.autorizamed.api.autorizacao.enums.StatusGuia.EM_AUDITORIA) THEN h.dataMovimentacao END) IS NOT NULL
           AND MAX(CASE WHEN h.statusNovo IN (com.autorizamed.api.autorizacao.enums.StatusGuia.AUTORIZADA, com.autorizamed.api.autorizacao.enums.StatusGuia.NEGADA) THEN h.dataMovimentacao END) IS NOT NULL
    """)
    List<SlaGuiaDTO> buscarDadosSlaGeralNoPeriodo(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    // SLA POR AUDITOR
    @Query("""
        SELECT new com.autorizamed.api.relatorio.dto.SlaGuiaDTO(
            h.guia.id,
            MIN(CASE WHEN h.statusNovo IN (com.autorizamed.api.autorizacao.enums.StatusGuia.EM_ANALISE, com.autorizamed.api.autorizacao.enums.StatusGuia.EM_AUDITORIA) THEN h.dataMovimentacao END),
            MAX(CASE WHEN h.statusNovo IN (com.autorizamed.api.autorizacao.enums.StatusGuia.AUTORIZADA, com.autorizamed.api.autorizacao.enums.StatusGuia.NEGADA) THEN h.dataMovimentacao END)
        )
        FROM HistoricoGuia h
        WHERE h.guia.auditor.id = :auditorId
          AND h.dataMovimentacao >= :dataInicio AND h.dataMovimentacao <= :dataFim
        GROUP BY h.guia.id
        HAVING MIN(CASE WHEN h.statusNovo IN (com.autorizamed.api.autorizacao.enums.StatusGuia.EM_ANALISE, com.autorizamed.api.autorizacao.enums.StatusGuia.EM_AUDITORIA) THEN h.dataMovimentacao END) IS NOT NULL
           AND MAX(CASE WHEN h.statusNovo IN (com.autorizamed.api.autorizacao.enums.StatusGuia.AUTORIZADA, com.autorizamed.api.autorizacao.enums.StatusGuia.NEGADA) THEN h.dataMovimentacao END) IS NOT NULL
    """)
    List<SlaGuiaDTO> buscarDadosSlaPorAuditor(
            @Param("auditorId") UUID auditorId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    // HISTÓRICO DE DECISÃOL DOS ÚLTIMOS 6 MESES
    @Query("""
        SELECT new com.autorizamed.api.relatorio.dto.DecisaoAuditorDTO(
            YEAR(h.dataMovimentacao),
            MONTH(h.dataMovimentacao),
            h.statusNovo,
            COUNT(h)
        )
        FROM HistoricoGuia h
        WHERE h.usuario.id = :auditorId
          AND h.statusNovo IN ('AUTORIZADA', 'NEGADA')
          AND h.dataMovimentacao >= :dataCorte
        GROUP BY YEAR(h.dataMovimentacao), MONTH(h.dataMovimentacao), h.statusNovo
        ORDER BY YEAR(h.dataMovimentacao) ASC, MONTH(h.dataMovimentacao) ASC
    """)
    List<DecisaoAuditorDTO> agruparDecisoesPorMes(
            @Param("auditorId") UUID auditorId,
            @Param("dataCorte") LocalDateTime dataCorte
    );
}