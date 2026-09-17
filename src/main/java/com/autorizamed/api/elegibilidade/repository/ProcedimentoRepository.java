package com.autorizamed.api.elegibilidade.repository;

import com.autorizamed.api.elegibilidade.entity.Procedimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcedimentoRepository extends JpaRepository<Procedimento, UUID> {
    Optional<Procedimento> findByCodigoTuss(String codigoTuss);
    List<Procedimento> findAllByCodigoTussIn(List<String> codigosTuss);
//    List<Procedimento> findByCodigoTussContainingIgnoreCaseOrDescricaoContainingIgnoreCase(String termoBusca);

    @Query("SELECT p FROM Procedimento p WHERE LOWER(p.codigoTuss) LIKE LOWER(CONCAT('%', :termo, '%')) OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :termo, '%'))")
    List<Procedimento> buscarPorCodigoOuDescricao(@Param("termo") String termo);
}
