package com.autorizamed.api.autorizacao.service;

import com.autorizamed.api.autorizacao.dto.request.ProfissionalSaudeRequest;
import com.autorizamed.api.autorizacao.dto.request.SolicitarGuiaRequest;
import com.autorizamed.api.autorizacao.dto.response.GuiaResponse;
import com.autorizamed.api.autorizacao.entity.GuiaAutorizacao;
import com.autorizamed.api.autorizacao.entity.ProfissionalSaude;
import com.autorizamed.api.autorizacao.enums.StatusGuia;
import com.autorizamed.api.autorizacao.repository.GuiaAutorizacaoRepository;
import com.autorizamed.api.autorizacao.repository.ProfissionalSaudeRepository;
import com.autorizamed.api.elegibilidade.entity.Beneficiario;
import com.autorizamed.api.elegibilidade.entity.Prestador;
import com.autorizamed.api.elegibilidade.entity.Procedimento;
import com.autorizamed.api.elegibilidade.repository.BeneficiarioRepository;
import com.autorizamed.api.elegibilidade.repository.FuncionarioRepository;
import com.autorizamed.api.elegibilidade.repository.PrestadorRepository;
import com.autorizamed.api.elegibilidade.repository.ProcedimentoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GuiaAutorizacaoServiceTest {

    @Mock private GuiaAutorizacaoRepository guiaRepository;
    @Mock private BeneficiarioRepository beneficiarioRepository;
    @Mock private PrestadorRepository prestadorRepository;
    @Mock private ProcedimentoRepository procedimentoRepository;
    @Mock private ProfissionalSaudeRepository profissionalSaudeRepository;
    @Mock private FuncionarioRepository funcionarioRepository;

    @InjectMocks
    private GuiaAutorizacaoService service;

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando a carteirinha do paciente não existir")
    void deveLancarExcecaoQuandoPacienteNaoExistir() {

        SolicitarGuiaRequest request = mock(SolicitarGuiaRequest.class);
        when(request.carteirinhaBeneficiario()).thenReturn("123456789");

        when(beneficiarioRepository.findByCarteirinha("123456789"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            service.solicitar(request);
        });

        assertEquals("Erro crítico: Paciente não existe na base de dados.", exception.getMessage());

        verify(prestadorRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve retornar guia NEGADA quando o plano do paciente estiver inativo")
    void deveNegarGuiaQuandoPlanoInativo() {

        UUID prestadorIdFalso = UUID.randomUUID();

        SolicitarGuiaRequest request = mock(SolicitarGuiaRequest.class);
        when(request.carteirinhaBeneficiario()).thenReturn("123456789");
        when(request.prestadorId()).thenReturn(prestadorIdFalso);
        when(request.funcionarioId()).thenReturn(null);

        // Falso paciente inativo
        Beneficiario pacienteInativo = mock(Beneficiario.class);
        when(pacienteInativo.isPlanoAtivo()).thenReturn(false);

        // Falso prestador
        Prestador prestador = mock(Prestador.class);

        // Falso médico solicitante
        ProfissionalSaude medico = mock(ProfissionalSaude.class);
        ProfissionalSaudeRequest medicoReq = mock(ProfissionalSaudeRequest.class);
        when(request.profissionalSolicitante()).thenReturn(medicoReq);
        when(medicoReq.numeroConselho()).thenReturn("CRM123");

        // Falsos procedimentos
        SolicitarGuiaRequest.ItemProcedimentoRequest item = mock(SolicitarGuiaRequest.ItemProcedimentoRequest.class);
        when(item.codigoTuss()).thenReturn("10101012");
        when(request.procedimentos()).thenReturn(List.of(item));

        Procedimento procedimento = mock(Procedimento.class);
        when(procedimento.getCodigoTuss()).thenReturn("10101012");
        when(procedimentoRepository.findAllByCodigoTussIn(List.of("10101012")))
                .thenReturn(List.of(procedimento));


        when(beneficiarioRepository.findByCarteirinha("123456789")).thenReturn(Optional.of(pacienteInativo));
        when(prestadorRepository.findById(prestadorIdFalso)).thenReturn(Optional.of(prestador));
        when(profissionalSaudeRepository.findByNumeroConselho("CRM123")).thenReturn(Optional.of(medico));

        when(guiaRepository.save(any(GuiaAutorizacao.class))).thenAnswer(i -> i.getArguments()[0]);

        GuiaResponse response = service.solicitar(request);

        // Verificação
        assertNotNull(response);
        assertEquals(StatusGuia.NEGADA, response.statusAtual());
        assertEquals("O plano do paciente está inativo.", response.motivoNegativa());
    }
}