package com.autorizamed.api.ia.service;

import com.autorizamed.api.autorizacao.entity.GuiaAutorizacao;
import com.autorizamed.api.autorizacao.enums.StatusGuia;
import com.autorizamed.api.autorizacao.repository.GuiaAutorizacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuditoriaIaServiceTest {

    @Autowired
    private AuditoriaIaService auditoriaIaService;

    @Autowired
    private GuiaAutorizacaoRepository guiaRepository;

    @Test
    @Transactional
    @DisplayName("Deve executar o fluxo completo do serviço de IA (Resiliência + Banco de Dados + Eventos)")
    void testarFluxoCompletoServicoIa() {
        GuiaAutorizacao guia = guiaRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nenhuma guia encontrada no banco para teste."));

        System.out.println("=================================================");
        System.out.println("🩺 Testando Guia ID: " + guia.getId());
        System.out.println("Paciente: " + guia.getBeneficiario().getNome());
        System.out.println("Indicação Clínica: " + guia.getIndicacaoClinica());
        System.out.println("Status Inicial: " + guia.getStatus());
        System.out.println("=================================================");

        auditoriaIaService.avaliarGuiaResiliente(guia.getId());

        GuiaAutorizacao guiaAtualizada = guiaRepository.findById(guia.getId()).orElseThrow();

        System.out.println("=================================================");
        System.out.println("📊 RESULTADO PÓS-IA NO BANCO:");
        System.out.println("Status Final: " + guiaAtualizada.getStatus());
        System.out.println("Alerta Sistema: " + guiaAtualizada.getAlertaSistema());
        System.out.println("Total de Históricos: " + guiaAtualizada.getHistoricos().size());
        System.out.println("=================================================");

        assertNotNull(guiaAtualizada.getStatus());
        assertFalse(guiaAtualizada.getHistoricos().isEmpty(), "Deveria ter registrado o histórico da ação!");
    }
}