package com.autorizamed.api.autorizacao.repository;

import com.autorizamed.api.autorizacao.entity.AnexoGuia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnexoGuiaRepository extends JpaRepository<AnexoGuia, UUID> {
}
