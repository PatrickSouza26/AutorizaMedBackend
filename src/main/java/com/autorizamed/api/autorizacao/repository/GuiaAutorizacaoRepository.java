package com.autorizamed.api.autorizacao.repository;

import com.autorizamed.api.autorizacao.entity.GuiaAutorizacao;
import com.autorizamed.api.autorizacao.enums.StatusGuia;
import com.autorizamed.api.relatorio.dto.*;
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

    @Query("SELECT g FROM GuiaAutorizacao g " +
            "WHERE g.status IN ('EM_ANALISE', 'PENDENCIA_RESPONDIDA') " +
            "AND (LOWER(g.numeroGuia) LIKE LOWER(CONCAT('%', :busca, '%')) OR LOWER(g.beneficiario.nome) LIKE LOWER(CONCAT('%', :busca, '%'))) " +
            "ORDER BY " +
            "CASE g.caraterSolicitacao WHEN 'URGENCIA' THEN 1 ELSE 2 END ASC, " +
            "CASE g.status WHEN 'PENDENCIA_RESPONDIDA' THEN 1 ELSE 2 END ASC, " +
            "g.dataLimiteAprovacao ASC")
    Page<GuiaAutorizacao> buscarFilaAuditoriaComBusca(@Param("busca") String busca, Pageable pageable);

    //BUSCA URGENCIAS
    @Query("SELECT g FROM GuiaAutorizacao g " +
            "WHERE g.status IN ('EM_ANALISE', 'PENDENCIA_RESPONDIDA') " +
            "AND g.caraterSolicitacao = 'URGENCIA' " +
            "ORDER BY " +
            "CASE g.status WHEN 'PENDENCIA_RESPONDIDA' THEN 1 ELSE 2 END ASC, " +
            "g.dataLimiteAprovacao ASC")
    Page<GuiaAutorizacao> buscarFilaAuditoriaSomenteUrgencia(Pageable pageable);

    @Query("SELECT g FROM GuiaAutorizacao g " +
            "WHERE g.status IN ('EM_ANALISE', 'PENDENCIA_RESPONDIDA') " +
            "AND g.caraterSolicitacao = 'URGENCIA' " +
            "AND (LOWER(g.numeroGuia) LIKE LOWER(CONCAT('%', :busca, '%')) OR LOWER(g.beneficiario.nome) LIKE LOWER(CONCAT('%', :busca, '%'))) " +
            "ORDER BY " +
            "CASE g.status WHEN 'PENDENCIA_RESPONDIDA' THEN 1 ELSE 2 END ASC, " +
            "g.dataLimiteAprovacao ASC")
    Page<GuiaAutorizacao> buscarFilaAuditoriaSomenteUrgenciaComBusca(@Param("busca") String busca, Pageable pageable);

    //BUSCA ELETIVAS
    @Query("SELECT g FROM GuiaAutorizacao g " +
            "WHERE g.status IN ('EM_ANALISE', 'PENDENCIA_RESPONDIDA') " +
            "AND g.caraterSolicitacao = 'ELETIVA' " +
            "ORDER BY " +
            "CASE g.status WHEN 'PENDENCIA_RESPONDIDA' THEN 1 ELSE 2 END ASC, " +
            "g.dataLimiteAprovacao ASC")
    Page<GuiaAutorizacao> buscarFilaAuditoriaSomenteEletiva(Pageable pageable);

    @Query("SELECT g FROM GuiaAutorizacao g " +
            "WHERE g.status IN ('EM_ANALISE', 'PENDENCIA_RESPONDIDA') " +
            "AND g.caraterSolicitacao = 'ELETIVA' " +
            "AND (LOWER(g.numeroGuia) LIKE LOWER(CONCAT('%', :busca, '%')) OR LOWER(g.beneficiario.nome) LIKE LOWER(CONCAT('%', :busca, '%'))) " +
            "ORDER BY " +
            "CASE g.status WHEN 'PENDENCIA_RESPONDIDA' THEN 1 ELSE 2 END ASC, " +
            "g.dataLimiteAprovacao ASC")
    Page<GuiaAutorizacao> buscarFilaAuditoriaSomenteEletivaComBusca(@Param("busca") String busca, Pageable pageable);

    //KPIS PARA A FILA
    @Query("SELECT COUNT(g) FROM GuiaAutorizacao g " +
            "WHERE g.status IN ('EM_ANALISE', 'PENDENCIA_RESPONDIDA')")
    long contarFilaAuditoria();

    @Query("SELECT COUNT(g) FROM GuiaAutorizacao g " +
            "WHERE g.status IN ('EM_ANALISE', 'PENDENCIA_RESPONDIDA') " +
            "AND g.caraterSolicitacao = 'URGENCIA'")
    long contarFilaAuditoriaUrgencias();

    @Query("SELECT COUNT(g) FROM GuiaAutorizacao g " +
            "WHERE g.status IN ('EM_ANALISE', 'PENDENCIA_RESPONDIDA') " +
            "AND g.dataSolicitacao >= :inicioDoDia AND g.dataSolicitacao <= :fimDoDia")
    long contarFilaAuditoriaHoje(@Param("inicioDoDia") LocalDateTime inicioDoDia, @Param("fimDoDia") LocalDateTime fimDoDia);

    //RELATÓRIOS PARA DASHBOARD

    // SOLICITAÇÕES NO PERIODO
    long countByDataSolicitacaoBetween(LocalDateTime dataInicio, LocalDateTime dataFim);

    // AGRUPADO POR STATUS (GRÁFICO)
    @Query("""
        SELECT new com.autorizamed.api.relatorio.dto.StatusCountDTO(g.status, COUNT(g))
        FROM GuiaAutorizacao g
        WHERE g.dataSolicitacao >= :dataInicio AND g.dataSolicitacao <= :dataFim
        GROUP BY g.status
    """)
    List<StatusCountDTO> agruparGuiasPorStatusNoPeriodo(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    // AGRUPADO POR ABERTA OU RESOLVIDA
    @Query("""
        SELECT new com.autorizamed.api.relatorio.dto.EvolucaoDiariaDTO(DAY(g.dataSolicitacao), COUNT(g))
        FROM GuiaAutorizacao g
        WHERE g.status IN :statusList
          AND g.dataSolicitacao >= :dataInicio AND g.dataSolicitacao <= :dataFim
        GROUP BY DAY(g.dataSolicitacao)
        ORDER BY DAY(g.dataSolicitacao) ASC
    """)
    List<EvolucaoDiariaDTO> evolucaoDiariaPorStatus(
            @Param("statusList") List<StatusGuia> statusList,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );

    //PRODUTIVIDADE DOS AUDITORES
    @Query("""
        SELECT new com.autorizamed.api.relatorio.dto.TopAuditorDTO(
            g.auditor.id, g.auditor.nome, CONCAT(g.auditor.tipoConselho, ' ', g.auditor.numeroConselho, '/', g.auditor.ufConselho), COUNT(g)
        )
        FROM GuiaAutorizacao g
        WHERE g.status IN ('AUTORIZADA', 'NEGADA')
          AND g.dataSolicitacao >= :dataInicio AND g.dataSolicitacao <= :dataFim
        GROUP BY g.auditor.id, g.auditor.nome, g.auditor.tipoConselho, g.auditor.numeroConselho, g.auditor.ufConselho
        ORDER BY COUNT(g) DESC
    """)
    List<TopAuditorDTO> buscarTopAuditoresNoPeriodo(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable
    );

    //ÚLTIMAS AVALIAÇÕES DO AUDITOR
    Page<GuiaAutorizacao> findByAuditorIdAndStatusInOrderByDataSolicitacaoDesc(
            UUID auditorId,
            List<StatusGuia> status,
            Pageable pageable
    );

    @Query("SELECT g FROM GuiaAutorizacao g " +
           "WHERE g.auditor.id = :auditorId " +
           "AND g.status = :status " +
           "ORDER BY g.caraterSolicitacao DESC, g.dataLimiteAprovacao ASC")
    Page<GuiaAutorizacao> buscarMinhasAnalisesOrdenadas(
            @Param("auditorId") UUID auditorId,
            @Param("status") StatusGuia status,
            Pageable pageable
    );

    //FREQUÊNCIA DE UTIILIZAÇÃO (PRESTADOR + BENEFICIÁRIO)
    @Query("""
        SELECT new com.autorizamed.api.relatorio.dto.TopEntidadeDTO(
            g.prestador.id, g.prestador.nome, g.prestador.documento, COUNT(g)
        )
        FROM GuiaAutorizacao g
        WHERE g.dataSolicitacao >= :dataInicio AND g.dataSolicitacao <= :dataFim
        GROUP BY g.prestador.id, g.prestador.nome, g.prestador.documento
        ORDER BY COUNT(g) DESC
    """)
    List<TopEntidadeDTO> buscarTopPrestadores(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable
    );

    @Query("""
        SELECT new com.autorizamed.api.relatorio.dto.TopEntidadeDTO(
            g.beneficiario.id, g.beneficiario.nome, g.beneficiario.carteirinha, COUNT(g)
        )
        FROM GuiaAutorizacao g
        WHERE g.dataSolicitacao >= :dataInicio AND g.dataSolicitacao <= :dataFim
        GROUP BY g.beneficiario.id, g.beneficiario.nome, g.beneficiario.carteirinha
        ORDER BY COUNT(g) DESC
    """)
    List<TopEntidadeDTO> buscarTopBeneficiarios(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable
    );

    @Query("""
        SELECT new com.autorizamed.api.relatorio.dto.HistoricoMensalDTO(
            YEAR(g.dataSolicitacao), MONTH(g.dataSolicitacao), g.status, COUNT(g)
        )
        FROM GuiaAutorizacao g
        WHERE g.prestador.id = :prestadorId
          AND g.dataSolicitacao >= :dataCorte
        GROUP BY YEAR(g.dataSolicitacao), MONTH(g.dataSolicitacao), g.status
        ORDER BY YEAR(g.dataSolicitacao) ASC, MONTH(g.dataSolicitacao) ASC
    """)
    List<HistoricoMensalDTO> historicoMensalPrestador(
            @Param("prestadorId") UUID prestadorId,
            @Param("dataCorte") LocalDateTime dataCorte
    );

    @Query("""
        SELECT new com.autorizamed.api.relatorio.dto.HistoricoMensalDTO(
            YEAR(g.dataSolicitacao), MONTH(g.dataSolicitacao), g.status, COUNT(g)
        )
        FROM GuiaAutorizacao g
        WHERE g.beneficiario.id = :beneficiarioId
          AND g.dataSolicitacao >= :dataCorte
        GROUP BY YEAR(g.dataSolicitacao), MONTH(g.dataSolicitacao), g.status
        ORDER BY YEAR(g.dataSolicitacao) ASC, MONTH(g.dataSolicitacao) ASC
    """)
    List<HistoricoMensalDTO> historicoMensalBeneficiario(
            @Param("beneficiarioId") UUID beneficiarioId,
            @Param("dataCorte") LocalDateTime dataCorte
    );

    //LINHA DO TEMPO DO BENEFICIÁRIO
    Page<GuiaAutorizacao> findByBeneficiarioIdOrderByDataSolicitacaoDesc(
            UUID beneficiarioId,
            Pageable pageable
    );

    long countByAuditorIdAndStatusAndDataSolicitacaoBetween(
            UUID auditorId,
            StatusGuia status,
            LocalDateTime inicio,
            LocalDateTime fim
    );

    long countByPrestadorIdAndDataSolicitacaoBetween(
            UUID prestadorId,
            LocalDateTime inicio,
            LocalDateTime fim
    );

    long countByBeneficiarioIdAndDataSolicitacaoBetween(
            UUID beneficiarioId,
            LocalDateTime inicio,
            LocalDateTime fim
    );

    @Query("""
        SELECT new com.autorizamed.api.relatorio.dto.StatusCountDTO(g.status, COUNT(g))
        FROM GuiaAutorizacao g
        WHERE g.prestador.id = :prestadorId
        GROUP BY g.status
    """)
    List<StatusCountDTO> contarStatusPorPrestador(@Param("prestadorId") UUID prestadorId);

    Page<GuiaAutorizacao> findByPrestadorIdOrderByDataSolicitacaoDesc(
            UUID prestadorId,
            Pageable pageable
    );

    Page<GuiaAutorizacao> findByFuncionarioIdOrderByDataSolicitacaoDesc(
            UUID funcionarioId,
            Pageable pageable
    );

}
