package com.autorizamed.api.elegibilidade.service;

import com.autorizamed.api.auditoria.annotation.AuditCreate;
import com.autorizamed.api.auditoria.annotation.AuditUpdate;
import com.autorizamed.api.auditoria.enums.AcaoAuditoria;
import com.autorizamed.api.comum.mapper.EnderecoMapper;
import com.autorizamed.api.comum.model.Endereco;
import com.autorizamed.api.elegibilidade.dto.request.AtualizarFuncionarioRequest;
import com.autorizamed.api.elegibilidade.dto.request.FuncionarioRequest;
import com.autorizamed.api.elegibilidade.dto.response.FuncionarioResponse;
import com.autorizamed.api.elegibilidade.entity.Funcionario;
import com.autorizamed.api.elegibilidade.entity.Prestador;
import com.autorizamed.api.elegibilidade.mapper.FuncionarioMapper;
import com.autorizamed.api.elegibilidade.repository.FuncionarioRepository;
import com.autorizamed.api.elegibilidade.repository.PrestadorRepository;
import com.autorizamed.api.seguranca.enums.RoleUsuario;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FuncionarioService {

    private final FuncionarioRepository repository;
    private final PrestadorRepository prestadorRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @AuditCreate(entidadeTipo = Funcionario.class)
    public FuncionarioResponse cadastrar(FuncionarioRequest request) {
        Prestador prestador = prestadorRepository.findById(request.prestadorId())
                .orElseThrow(() -> new EntityNotFoundException("Prestador não encontrado."));

        String senhaTemporaria = request.cpf();
        String senhaCriptografada = passwordEncoder.encode(senhaTemporaria);

        Endereco endereco = EnderecoMapper.converteDto(request.endereco());

        Funcionario funcionario = Funcionario.builder()
                .nome(request.nome())
                .email(request.email())
                .telefone(request.telefone())
                .login(request.email())
                .senha(senhaCriptografada)
                .role(RoleUsuario.FUNCIONARIO_PRESTADOR)
                .cpf(request.cpf())
                .prestador(prestador)
                .endereco(endereco)
                .ativo(true)
                .build();

        Funcionario salvo = repository.save(funcionario);

        return FuncionarioMapper.converteEntidade(salvo);
    }

    @Transactional
    @AuditUpdate(entidadeTipo = Funcionario.class)
    public FuncionarioResponse atualizarParcial(UUID id, AtualizarFuncionarioRequest request) {
        Funcionario funcionario = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado."));

        if (request.nome() != null) {
            funcionario.setNome(request.nome());
        }
        if (request.email() != null) {
            funcionario.setEmail(request.email());
        }
        if (request.telefone() != null) {
            funcionario.setTelefone(request.telefone());
        }
        if (request.endereco() != null) {
            funcionario.setEndereco(EnderecoMapper.converteDto(request.endereco()));
        }

        Funcionario funcAtualizado = repository.save(funcionario);
        return FuncionarioMapper.converteEntidade(funcAtualizado);
    }

    @Transactional
    @AuditUpdate(entidadeTipo = Funcionario.class, acao = AcaoAuditoria.INATIVACAO)
    public FuncionarioResponse inativar(UUID id) {
        Funcionario funcionario = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado."));
        funcionario.setAtivo(false);

        Funcionario funcAtualizado = repository.save(funcionario);
        return FuncionarioMapper.converteEntidade(funcAtualizado);
    }

    @Transactional
    @AuditUpdate(entidadeTipo = Funcionario.class, acao = AcaoAuditoria.REATIVACAO)
    public FuncionarioResponse reativar(UUID id) {
        Funcionario funcionario = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Funcionário não encontrado."));
        funcionario.setAtivo(true);

        Funcionario funcAtualizado = repository.save(funcionario);
        return FuncionarioMapper.converteEntidade(funcAtualizado);
    }

    public List<FuncionarioResponse> listarPorPrestador(UUID prestadorId) {
        return repository.findByPrestadorId(prestadorId).stream()
                .map(FuncionarioMapper::converteEntidade)
                .toList();
    }
}