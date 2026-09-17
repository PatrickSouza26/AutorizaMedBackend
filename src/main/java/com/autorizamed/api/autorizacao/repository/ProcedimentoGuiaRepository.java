package com.autorizamed.api.autorizacao.repository;

import com.autorizamed.api.autorizacao.entity.ProcedimentoGuia;
import com.autorizamed.api.relatorio.dto.TopProcedimentoDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ProcedimentoGuiaRepository extends JpaRepository<ProcedimentoGuia, UUID> {

    @Query("""
        SELECT new com.autorizamed.api.relatorio.dto.TopProcedimentoDTO(
            p.codigoTuss, p.descricao, SUM(pg.quantidade)
        )
        FROM ProcedimentoGuia pg
        JOIN pg.procedimento p
        JOIN pg.guia g
        WHERE g.status = 'AUTORIZADA' 
          AND g.dataSolicitacao >= :dataInicio AND g.dataSolicitacao <= :dataFim
        GROUP BY p.id, p.codigoTuss, p.descricao
        ORDER BY SUM(pg.quantidade) DESC
    """)
    List<TopProcedimentoDTO> buscarTopProcedimentosAutorizadosNoPeriodo(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable
    );

    @Query("""
        SELECT new com.autorizamed.api.relatorio.dto.TopProcedimentoDTO(
            p.codigoTuss, p.descricao, SUM(pg.quantidade)
        )
        FROM ProcedimentoGuia pg
        JOIN pg.procedimento p
        JOIN pg.guia g
        WHERE g.prestador.id = :prestadorId
          AND g.dataSolicitacao >= :dataInicio AND g.dataSolicitacao <= :dataFim
        GROUP BY p.id, p.codigoTuss, p.descricao
        ORDER BY SUM(pg.quantidade) DESC
    """)
    List<TopProcedimentoDTO> buscarProcedimentosFavoritosPorPrestador(
            @Param("prestadorId") UUID prestadorId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable
    );

    @Query("""
        SELECT new com.autorizamed.api.relatorio.dto.TopProcedimentoDTO(
            p.codigoTuss, p.descricao, SUM(pg.quantidade)
        )
        FROM ProcedimentoGuia pg
        JOIN pg.procedimento p
        JOIN pg.guia g
        WHERE g.beneficiario.id = :beneficiarioId
          AND g.dataSolicitacao >= :dataInicio AND g.dataSolicitacao <= :dataFim
        GROUP BY p.id, p.codigoTuss, p.descricao
        ORDER BY SUM(pg.quantidade) DESC
    """)
    List<TopProcedimentoDTO> buscarPrincipalProcedimentoPorBeneficiario(
            @Param("beneficiarioId") UUID beneficiarioId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            Pageable pageable
    );
}
