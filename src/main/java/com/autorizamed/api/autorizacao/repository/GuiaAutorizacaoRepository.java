package com.autorizamed.api.autorizacao.repository;

import com.autorizamed.api.autorizacao.entity.GuiaAutorizacao;
import com.autorizamed.api.autorizacao.enums.StatusGuia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GuiaAutorizacaoRepository extends JpaRepository<GuiaAutorizacao, UUID> {
    List<GuiaAutorizacao> findByBeneficiarioCarteirinha(String carteirinha);
    List<GuiaAutorizacao> findByStatus(StatusGuia status);
}
