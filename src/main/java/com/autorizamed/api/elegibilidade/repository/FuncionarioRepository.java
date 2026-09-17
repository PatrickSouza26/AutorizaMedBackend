package com.autorizamed.api.elegibilidade.repository;

import com.autorizamed.api.elegibilidade.entity.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, UUID> {
    List<Funcionario> findByPrestadorId(UUID prestadorId);
}