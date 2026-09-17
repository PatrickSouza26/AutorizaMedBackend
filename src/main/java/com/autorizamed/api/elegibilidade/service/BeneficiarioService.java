package com.autorizamed.api.elegibilidade.service;

import com.autorizamed.api.auditoria.annotation.AuditCreate;
import com.autorizamed.api.auditoria.annotation.AuditUpdate;
import com.autorizamed.api.auditoria.enums.AcaoAuditoria;
import com.autorizamed.api.comum.mapper.EnderecoMapper;
import com.autorizamed.api.comum.model.Endereco;
import com.autorizamed.api.elegibilidade.dto.request.AtualizarBeneficiarioRequest;
import com.autorizamed.api.elegibilidade.dto.request.BeneficiarioRequest;
import com.autorizamed.api.elegibilidade.dto.response.BeneficiarioResponse;
import com.autorizamed.api.elegibilidade.dto.response.ElegibilidadeResponse;
import com.autorizamed.api.elegibilidade.entity.Beneficiario;
import com.autorizamed.api.elegibilidade.mapper.BeneficiarioMapper;
import com.autorizamed.api.elegibilidade.repository.BeneficiarioRepository;
import com.autorizamed.api.elegibilidade.repository.RedeCredenciadaRepository;
import com.autorizamed.api.seguranca.enums.RoleUsuario;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Service
@RequiredArgsConstructor
public class BeneficiarioService {

    private final BeneficiarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final RedeCredenciadaRepository redeCredenciadaRepository;

    @Transactional
    @AuditCreate(entidadeTipo = Beneficiario.class)
    public BeneficiarioResponse cadastrar(BeneficiarioRequest request) {

        if (repository.findByCpf(request.cpf()).isPresent()) {
            throw new IllegalArgumentException("Já existe um beneficiário cadastrado com este CPF.");
        }

        Long proximoNumero = repository.getProximoValorCarteirinha();
        String carteirinhaGerada = String.format("1000%05d", proximoNumero);

        String senhaCriptografada = passwordEncoder.encode(request.cpf().replaceAll("\\D", ""));
        Endereco endereco = EnderecoMapper.converteDto(request.endereco());

        Beneficiario beneficiario = Beneficiario.builder()
                .nome(request.nome())
                .email(request.email())
                .telefone(request.telefone())
                .login(request.email())
                .senha(senhaCriptografada)
                .role(RoleUsuario.BENEFICIARIO)
                .endereco(endereco)
                .cpf(request.cpf())
                .dataNascimento(request.dataNascimento())
                .carteirinha(carteirinhaGerada)
                .tipoPlano(request.tipoPlano())
                .dataAdesao(request.dataAdesao())
                .planoAtivo(true)
                .build();

        Beneficiario beneficiarioSalvo = repository.save(beneficiario);

        return BeneficiarioMapper.converteEntidade(beneficiarioSalvo);
    }

    @Transactional(readOnly = true)
    public Page<BeneficiarioResponse> listarBeneficiarios(String termoBusca, Boolean ativo, Pageable pageable) {
        return repository.buscarPorTermoEStatus(termoBusca, ativo, pageable)
                .map(BeneficiarioMapper::converteEntidade);
    }

    @Transactional(readOnly = true)
    public BeneficiarioResponse buscarPorCpfOuCarteirinha(String termoBusca) {
        Beneficiario beneficiario = buscarEntidade(termoBusca);
        return BeneficiarioMapper.converteEntidade(beneficiario);
    }

    @Transactional
    @AuditUpdate(entidadeTipo = Beneficiario.class)
    public BeneficiarioResponse atualizarParcial(UUID id, AtualizarBeneficiarioRequest request) {
        Beneficiario beneficiario = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Beneficiário não encontrado."));

        boolean houveAlteracao = false;

        if (request.nome() != null && !request.nome().isBlank() && !request.nome().equals(beneficiario.getNome())) {
            beneficiario.setNome(request.nome());
            houveAlteracao = true;
        }

        if (request.endereco() != null) {
            Endereco novoEndereco = EnderecoMapper.converteDto(request.endereco());

            if (!novoEndereco.equals(beneficiario.getEndereco())) {
                beneficiario.setEndereco(novoEndereco);
                houveAlteracao = true;
            }
        }

        if (request.tipoPlano() != null && request.tipoPlano() != beneficiario.getTipoPlano()) {
            beneficiario.setTipoPlano(request.tipoPlano());
            houveAlteracao = true;
        }

        if (houveAlteracao) {
            beneficiario = repository.save(beneficiario);
        }

        return BeneficiarioMapper.converteEntidade(beneficiario);
    }

    @Transactional
    @AuditUpdate(entidadeTipo = Beneficiario.class, acao = AcaoAuditoria.INATIVACAO)
    public BeneficiarioResponse inativar(UUID id) {
        Beneficiario beneficiario = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Beneficiário não encontrado pelo ID informado."));

        if (!beneficiario.isPlanoAtivo()) {
            throw new IllegalArgumentException("O plano deste beneficiário já está inativo.");
        }

        beneficiario.setPlanoAtivo(false);
        Beneficiario beneficiarioSalvo = repository.save(beneficiario);

        return BeneficiarioMapper.converteEntidade(beneficiarioSalvo);
    }

    @Transactional
    @AuditUpdate(entidadeTipo = Beneficiario.class, acao = AcaoAuditoria.REATIVACAO)
    public BeneficiarioResponse reativar(UUID id) {
        Beneficiario beneficiario = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Beneficiário não encontrado pelo ID informado."));

        if (beneficiario.isPlanoAtivo()) {
            throw new IllegalArgumentException("O plano deste beneficiário já está ativo.");
        }

        beneficiario.setPlanoAtivo(true);
        Beneficiario beneficiarioSalvo = repository.save(beneficiario);

        return BeneficiarioMapper.converteEntidade(beneficiarioSalvo);
    }
    @Transactional(readOnly = true)
    public ElegibilidadeResponse verificarElegibilidade(String termoBusca, UUID prestadorId) {
        Beneficiario beneficiario = buscarEntidade(termoBusca);
        BeneficiarioResponse beneficiarioResponse = BeneficiarioMapper.converteEntidade(beneficiario);

        if (!beneficiario.isPlanoAtivo()) {
            return new ElegibilidadeResponse(
                    false,
                    "Plano inativo ou cancelado.",
                    beneficiarioResponse
            );
        }

        if (beneficiario.temCarencia()) {
            return new ElegibilidadeResponse(
                    false,
                    "Paciente em período de carência contratual.",
                    beneficiarioResponse
            );
        }

        boolean prestadorAtende = redeCredenciadaRepository.prestadorAtende(prestadorId, beneficiario.getTipoPlano());

        if (!prestadorAtende) {
            return new ElegibilidadeResponse(
                    false,
                    "O prestador selecionado não atende este plano.",
                    beneficiarioResponse
            );
        }

        return new ElegibilidadeResponse(
                true,
                "Paciente elegível para atendimento.",
                beneficiarioResponse
        );
    }
    private Beneficiario buscarEntidade(String termoBusca) {
        String termoLimpo = termoBusca.replaceAll("[^0-9]", "");

        if (termoLimpo.startsWith("1000") && termoLimpo.length() >= 9) {
            return repository.findByCarteirinha(termoBusca)
                    .orElseThrow(() -> new EntityNotFoundException("Beneficiário não encontrado pela carteirinha informada."));
        } else {
            return repository.findByCpf(termoBusca)
                    .orElseThrow(() -> new EntityNotFoundException("Beneficiário não encontrado pelo CPF informado."));
        }
    }
}
