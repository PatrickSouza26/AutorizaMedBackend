package com.autorizamed.api.elegibilidade.service;

import com.autorizamed.api.auditoria.annotation.AuditCreate;
import com.autorizamed.api.auditoria.annotation.AuditUpdate;
import com.autorizamed.api.auditoria.enums.AcaoAuditoria;
import com.autorizamed.api.comum.mapper.EnderecoMapper;
import com.autorizamed.api.comum.model.Endereco;
import com.autorizamed.api.elegibilidade.dto.request.AtualizarPrestadorRequest;
import com.autorizamed.api.elegibilidade.dto.request.PrestadorRequest;
import com.autorizamed.api.elegibilidade.dto.response.PrestadorResponse;
import com.autorizamed.api.elegibilidade.entity.Prestador;
import com.autorizamed.api.elegibilidade.mapper.PrestadorMapper;
import com.autorizamed.api.elegibilidade.repository.PrestadorRepository;
import com.autorizamed.api.seguranca.entity.Usuario;
import com.autorizamed.api.seguranca.enums.RoleUsuario;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrestadorService {

    private final PrestadorRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @AuditCreate(entidadeTipo = Prestador.class)
    public PrestadorResponse cadastrar(PrestadorRequest request) {
        if (repository.findByDocumento(request.documento()).isPresent()) {
            throw new IllegalArgumentException("Já existe um prestador cadastrado com este documento (CRM/CNPJ).");
        }

        String senhaTemporaria = request.documento();
        String senhaCriptografada = passwordEncoder.encode(senhaTemporaria);

        Long proximoNumero = repository.getProximoValorPrestador();
        String numeroPrestador = String.format("2%04d", proximoNumero);

        Endereco endereco = EnderecoMapper.converteDto(request.endereco());

        Prestador prestador = Prestador.builder()
                .nome(request.nome())
                .email(request.email())
                .telefone(request.telefone())
                .login(request.login())
                .senha(senhaCriptografada)
                .role(RoleUsuario.PRESTADOR)
                .endereco(endereco)
                .documento(request.documento())
                .numeroPrestador(numeroPrestador)
                .ativo(true)
                .build();

        prestador = repository.save(prestador);
        return PrestadorMapper.converteEntidade(prestador);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<PrestadorResponse> listarPrestadores(String termoBusca, Boolean ativo, org.springframework.data.domain.Pageable pageable) {
        return repository.buscarPorTermoEStatus(termoBusca, ativo, pageable)
                .map(PrestadorMapper::converteEntidade);
    }

    @Transactional(readOnly = true)
    public PrestadorResponse buscarPorDocumento(String documento) {
        Prestador prestador = repository.findByDocumento(documento)
                .orElseThrow(() -> new EntityNotFoundException("Prestador não encontrado pelo documento informado."));
        return PrestadorMapper.converteEntidade(prestador);
    }

    @Transactional
    @AuditUpdate(entidadeTipo = Prestador.class)
    public PrestadorResponse atualizarParcial(UUID id, AtualizarPrestadorRequest request, Usuario usuarioLogado) {

        boolean eAdm = usuarioLogado.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        if (!eAdm && !usuarioLogado.getId().equals(id)) {
            throw new AccessDeniedException("Você não tem permissão para alterar os dados de outro prestador.");
        }

        Prestador prestador = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prestador não encontrado pelo ID informado."));

        boolean houveAlteracao = false;

        if (request.nome() != null && !request.nome().isBlank() && !request.nome().equals(prestador.getNome())) {
            prestador.setNome(request.nome());
            houveAlteracao = true;
        }

        if (request.telefone() != null && !request.telefone().isBlank() && !request.telefone().equals(prestador.getTelefone())) {
            prestador.setTelefone(request.telefone());
            houveAlteracao = true;
        }

        if (request.endereco() != null) {
            Endereco novoEndereco = EnderecoMapper.converteDto(request.endereco());

            if (!novoEndereco.equals(prestador.getEndereco())) {
                prestador.setEndereco(novoEndereco);
                houveAlteracao = true;
            }
        }

        if (houveAlteracao) {
            prestador = repository.save(prestador);
        }

        return PrestadorMapper.converteEntidade(prestador);
    }

    @Transactional
    @AuditUpdate(entidadeTipo = Prestador.class, acao = AcaoAuditoria.INATIVACAO)
    public PrestadorResponse inativar(UUID id) {
        Prestador prestador = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prestador não encontrado pelo ID informado."));

        if (!prestador.isAtivo()) {
            throw new IllegalArgumentException("Este prestador já está inativo no sistema.");
        }

        prestador.setAtivo(false);
        Prestador prestadorSalvo = repository.save(prestador);
        return PrestadorMapper.converteEntidade(prestadorSalvo);
    }

    @Transactional
    @AuditUpdate(entidadeTipo = Prestador.class, acao = AcaoAuditoria.REATIVACAO)
    public PrestadorResponse reativar(UUID id) {
        Prestador prestador = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Prestador não encontrado pelo ID informado."));

        if (prestador.isAtivo()) {
            throw new IllegalArgumentException("Este prestador já está ativo no sistema.");
        }

        prestador.setAtivo(true);
        Prestador prestadorSalvo = repository.save(prestador);
        return PrestadorMapper.converteEntidade(prestadorSalvo);
    }
}