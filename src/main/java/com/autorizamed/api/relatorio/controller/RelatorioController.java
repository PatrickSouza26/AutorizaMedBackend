package com.autorizamed.api.relatorio.controller;

import com.autorizamed.api.relatorio.dto.AuditorProdutividadeDTO;
import com.autorizamed.api.relatorio.dto.response.*;
import com.autorizamed.api.relatorio.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    // DASHBOARD ADM
    @GetMapping("/dashboard-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardAdminResponse> buscarDashboardAdmin(
            @RequestParam(required = false, defaultValue = "estemes") String filtroTempo) {

        DashboardAdminResponse response = relatorioService.gerarDashboardAdministrativo(filtroTempo);
        return ResponseEntity.ok(response);
    }

    // RELATÓRIO OPERACIONAL E AUDITORIA
    @GetMapping("/operacional")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public ResponseEntity<RelatorioOperacionalResponse> buscarRelatorioOperacional(
            @RequestParam(required = false, defaultValue = "estemes") String filtroTempo) {

        RelatorioOperacionalResponse response = relatorioService.gerarRelatorioOperacional(filtroTempo);
        return ResponseEntity.ok(response);
    }

    // RANKING COMPLETO DE AUDITORES
    @GetMapping("/operacional/auditores")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public ResponseEntity<Page<AuditorProdutividadeDTO>> buscarRankingAuditores(
            @RequestParam(required = false, defaultValue = "estemes") String filtroTempo,
            @RequestParam(required = false, defaultValue = "0") int pagina,
            @RequestParam(required = false, defaultValue = "10") int tamanho) {

        var response = relatorioService.gerarRankingAuditores(filtroTempo, pagina, tamanho);
        return ResponseEntity.ok(response);
    }

    //DETALHES DO AUDITOR
    @GetMapping("/auditor/{auditorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public ResponseEntity<AuditorDetalhesResponse> buscarDetalhesAuditor(
            @PathVariable UUID auditorId,
            @RequestParam(required = false, defaultValue = "0") int pagina,
            @RequestParam(required = false, defaultValue = "10") int tamanho) {

        AuditorDetalhesResponse response = relatorioService.gerarDetalhesAuditor(auditorId, pagina, tamanho);
        return ResponseEntity.ok(response);
    }

    // FREQUÊNCIA E UTLIZAÇÃO
    @GetMapping("/frequencia-utilizacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public ResponseEntity<FrequenciaUtilizacaoResponse> buscarFrequenciaUtilizacao(
            @RequestParam(required = false, defaultValue = "estemes") String filtroTempo) {

        FrequenciaUtilizacaoResponse response = relatorioService.gerarTelaFrequencia(filtroTempo);
        return ResponseEntity.ok(response);
    }

    //DETALHES DO PRESTADOR
    @GetMapping("/prestador/{prestadorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'PRESTADOR', 'FUNCIONARIO_PRESTADOR')")
    public ResponseEntity<PrestadorDetalhesResponse> buscarDetalhesPrestador(
            @PathVariable UUID prestadorId) {

        PrestadorDetalhesResponse response = relatorioService.gerarDetalhesPrestador(prestadorId);
        return ResponseEntity.ok(response);
    }

    //DETALHES DO BENEFICIÁRIO
    @GetMapping("/beneficiario/{beneficiarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'PRESTADOR', 'FUNCIONARIO_PRESTADOR')")
    public ResponseEntity<BeneficiarioDetalhesResponse> buscarDetalhesBeneficiario(
            @PathVariable UUID beneficiarioId) {

        BeneficiarioDetalhesResponse response = relatorioService.gerarDetalhesBeneficiario(beneficiarioId);
        return ResponseEntity.ok(response);
    }

    //DASHBOARD PRESTADOR
    @GetMapping("/prestador/{prestadorId}/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRESTADOR', 'FUNCIONARIO_PRESTADOR')")
    public ResponseEntity<DashboardPrestadorResponse> buscarDashboardPrestador(
            @PathVariable UUID prestadorId,
            @RequestParam(defaultValue = "0") int paginaGuia,
            @RequestParam(defaultValue = "10") int tamanhoGuia,
            @RequestParam(defaultValue = "0") int paginaAviso,
            @RequestParam(defaultValue = "5") int tamanhoAviso) {

        PageRequest pageGuia = PageRequest.of(paginaGuia, tamanhoGuia, Sort.by("dataSolicitacao").descending());
        PageRequest pageAviso = PageRequest.of(paginaAviso, tamanhoAviso);

        DashboardPrestadorResponse response = relatorioService.gerarDashboardPrestador(prestadorId, pageGuia, pageAviso);

        return ResponseEntity.ok(response);
    }
}