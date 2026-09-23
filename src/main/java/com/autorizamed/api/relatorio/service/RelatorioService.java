package com.autorizamed.api.relatorio.service;

import com.autorizamed.api.autorizacao.dto.response.GuiaResponse;
import com.autorizamed.api.autorizacao.entity.Auditor;
import com.autorizamed.api.autorizacao.entity.GuiaAutorizacao;
import com.autorizamed.api.autorizacao.entity.ProcedimentoGuia;
import com.autorizamed.api.autorizacao.enums.StatusGuia;
import com.autorizamed.api.autorizacao.mapper.GuiaMapper;
import com.autorizamed.api.autorizacao.repository.AuditorRepository;
import com.autorizamed.api.autorizacao.repository.GuiaAutorizacaoRepository;
import com.autorizamed.api.autorizacao.repository.HistoricoGuiaRepository;
import com.autorizamed.api.autorizacao.repository.ProcedimentoGuiaRepository;
import com.autorizamed.api.elegibilidade.repository.BeneficiarioRepository;
import com.autorizamed.api.elegibilidade.repository.PrestadorRepository;
import com.autorizamed.api.notificacao.dto.AvisoResponse;
import com.autorizamed.api.notificacao.enums.TipoDestinatario;
import com.autorizamed.api.notificacao.repository.AvisoRepository;
import com.autorizamed.api.notificacao.service.AvisoService;
import com.autorizamed.api.relatorio.dto.*;
import com.autorizamed.api.relatorio.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Utils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final BeneficiarioRepository beneficiarioRepository;
    private final PrestadorRepository prestadorRepository;
    private final AuditorRepository auditorRepository;
    private final GuiaAutorizacaoRepository guiaRepository;
    private final ProcedimentoGuiaRepository procedimentoGuiaRepository;
    private final HistoricoGuiaRepository historicoGuiaRepository;
    private final AvisoService avisoService;


    //DASHBOARD ADMINISTRATIVO

    @Transactional(readOnly = true)
    public DashboardAdminResponse gerarDashboardAdministrativo(String filtroTempo) {
        LocalDateTime dataInicio = calcularDataInicio(filtroTempo);
        LocalDateTime dataFim = LocalDateTime.now();

        // BUSCAS GLOBAIS
        long totalBeneficiarios = beneficiarioRepository.countByPlanoAtivoTrue();
        long totalCredenciados = prestadorRepository.countByAtivoTrue();
        long totalAuditores = auditorRepository.countByAtivoTrue();

        // VOLUME DE SOLICITAÇÕES
        long solicitacoesPeriodo = guiaRepository.countByDataSolicitacaoBetween(dataInicio, dataFim);

        // STATUS E TAXA DE APROVAÇÃO
        List<StatusCountDTO> distribuicaoStatus = guiaRepository.agruparGuiasPorStatusNoPeriodo(dataInicio, dataFim);
        double taxaAprovacao = calcularTaxaAprovacao(distribuicaoStatus, solicitacoesPeriodo);

        // GUIAS ABERTAS X RESOLVIDAS
        List<StatusGuia> statusAbertas = List.of(StatusGuia.EM_ANALISE, StatusGuia.EM_AUDITORIA, StatusGuia.PENDENCIA, StatusGuia.PENDENCIA_RESPONDIDA);
        List<StatusGuia> statusResolvidas = List.of(StatusGuia.AUTORIZADA, StatusGuia.NEGADA);

        List<EvolucaoDiariaDTO> evolucaoAbertas = guiaRepository.evolucaoDiariaPorStatus(statusAbertas, dataInicio, dataFim);
        List<EvolucaoDiariaDTO> evolucaoResolvidas = guiaRepository.evolucaoDiariaPorStatus(statusResolvidas, dataInicio, dataFim);

        // PROCEDIMENTOS MAIS AUTORIZADOS
        List<TopProcedimentoDTO> topProcedimentos = procedimentoGuiaRepository
                .buscarTopProcedimentosAutorizadosNoPeriodo(dataInicio, dataFim, PageRequest.of(0, 5));

        return new DashboardAdminResponse(
                totalBeneficiarios,
                totalCredenciados,
                totalAuditores,
                solicitacoesPeriodo,
                taxaAprovacao,
                evolucaoAbertas,
                evolucaoResolvidas,
                distribuicaoStatus,
                topProcedimentos
        );
    }

    //RELATÓRIO OPERACIONAL E AUDITORIA
    @Transactional(readOnly = true)
    public RelatorioOperacionalResponse gerarRelatorioOperacional(String filtroTempo) {
        LocalDateTime dataInicio = calcularDataInicio(filtroTempo);
        LocalDateTime dataFim = LocalDateTime.now();

        // VOLUME E DISTRIBUIÇÃO(AGRUPAMENTO POR STATU)
        long totalGuias = guiaRepository.countByDataSolicitacaoBetween(dataInicio, dataFim);
        List<StatusCountDTO> distribuicao = guiaRepository.agruparGuiasPorStatusNoPeriodo(dataInicio, dataFim);

        // TAXAS PENDÊNCIAS E NEGADAS
        double taxaPendencias = calcularTaxaPorStatus(distribuicao, totalGuias,
                List.of(StatusGuia.PENDENCIA, StatusGuia.PENDENCIA_RESPONDIDA));
        double taxaNegativas = calcularTaxaPorStatus(distribuicao, totalGuias,
                List.of(StatusGuia.NEGADA));

        // SLA GERAL
        List<SlaGuiaDTO> slaGeral = historicoGuiaRepository.buscarDadosSlaGeralNoPeriodo(dataInicio, dataFim);
        String tempoMedioGeral = calcularTempoMedioFormatado(slaGeral);

        // GRÁFICOS
        List<StatusGuia> statusAbertas = List.of(StatusGuia.EM_ANALISE, StatusGuia.EM_AUDITORIA, StatusGuia.PENDENCIA, StatusGuia.PENDENCIA_RESPONDIDA);
        List<StatusGuia> statusResolvidas = List.of(StatusGuia.AUTORIZADA, StatusGuia.NEGADA);

        List<EvolucaoDiariaDTO> evolucaoAbertas = guiaRepository.evolucaoDiariaPorStatus(statusAbertas, dataInicio, dataFim);
        List<EvolucaoDiariaDTO> evolucaoResolvidas = guiaRepository.evolucaoDiariaPorStatus(statusResolvidas, dataInicio, dataFim);

        List<TopAuditorDTO> top3Auditores = guiaRepository.buscarTopAuditoresNoPeriodo(dataInicio, dataFim, PageRequest.of(0, 3));

        List<AuditorProdutividadeDTO> produtividade = top3Auditores.stream().map(auditor -> {

            // SLA EXATO DO AUDITOR
            List<SlaGuiaDTO> slaAuditor = historicoGuiaRepository.buscarDadosSlaPorAuditor(auditor.id(), dataInicio, dataFim);
            String slaFormatado = calcularTempoMedioFormatado(slaAuditor);

            // QUANTAS APROVADAS PRA CALCULO DE %
            long aprovadas = guiaRepository.countByAuditorIdAndStatusAndDataSolicitacaoBetween(
                    auditor.id(), StatusGuia.AUTORIZADA, dataInicio, dataFim);

            double txAprovacao = (auditor.totalAnalisado() == 0) ? 0.0 : (aprovadas * 100.0) / auditor.totalAnalisado();

            return new AuditorProdutividadeDTO(
                    auditor.id(),
                    auditor.nome(),
                    auditor.registro(),
                    auditor.totalAnalisado(),
                    slaFormatado,
                    txAprovacao
            );
        }).toList();

        return new RelatorioOperacionalResponse(
                totalGuias,
                taxaPendencias,
                taxaNegativas,
                tempoMedioGeral,
                evolucaoAbertas,
                evolucaoResolvidas,
                distribuicao,
                produtividade
        );
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<AuditorProdutividadeDTO> gerarRankingAuditores(String filtroTempo, int pagina, int tamanho) {
        LocalDateTime dataInicio = calcularDataInicio(filtroTempo);
        LocalDateTime dataFim = LocalDateTime.now();

        List<TopAuditorDTO> auditoresDto = guiaRepository.buscarTopAuditoresNoPeriodo(
                dataInicio, dataFim, PageRequest.of(pagina, tamanho)
        );

        List<AuditorProdutividadeDTO> produtividade = auditoresDto.stream().map(auditor -> {
            List<SlaGuiaDTO> slaAuditor = historicoGuiaRepository.buscarDadosSlaPorAuditor(auditor.id(), dataInicio, dataFim);
            String slaFormatado = calcularTempoMedioFormatado(slaAuditor);
            long aprovadas = guiaRepository.countByAuditorIdAndStatusAndDataSolicitacaoBetween(auditor.id(), StatusGuia.AUTORIZADA, dataInicio, dataFim);
            double txAprovacao = (auditor.totalAnalisado() == 0) ? 0.0 : (aprovadas * 100.0) / auditor.totalAnalisado();
            return new AuditorProdutividadeDTO(auditor.id(), auditor.nome(), auditor.registro(), auditor.totalAnalisado(), slaFormatado, txAprovacao);
        }).toList();

        return new org.springframework.data.domain.PageImpl<>(produtividade, PageRequest.of(pagina, tamanho), 100); 
    }


    @Transactional(readOnly = true)
    public AuditorDetalhesResponse gerarDetalhesAuditor(UUID auditorId, int pagina, int tamanhoPagina) {

        Auditor auditor = auditorRepository.findById(auditorId)
                .orElseThrow(() -> new RuntimeException("Auditor não encontrado"));
        
        String registro = auditor.getTipoConselho() + " " + auditor.getNumeroConselho() + "/" + auditor.getUfConselho();

        //GRÁFICO ÚLTIMOS 6 MESES
        LocalDateTime dataCorte = LocalDateTime.now()
                .minusMonths(6)
                .withDayOfMonth(1)
                .toLocalDate()
                .atStartOfDay();

        List<DecisaoAuditorDTO> graficoDecisoes = historicoGuiaRepository
                .agruparDecisoesPorMes(auditorId, dataCorte);

        //ÚLTIMAS AVALIAÇÕES(TABELA)
        List<StatusGuia> statusFinais = List.of(StatusGuia.AUTORIZADA, StatusGuia.NEGADA);
        PageRequest paginacao = PageRequest.of(pagina, tamanhoPagina);

        Page<GuiaAutorizacao> guiasPaginadas = guiaRepository
                .findByAuditorIdAndStatusInOrderByDataSolicitacaoDesc(auditorId, statusFinais, paginacao);

        // CONVERTE ENTIDADE -> DTO
        List<AvaliacaoRecenteDTO> ultimasAvaliacoes = guiasPaginadas.stream().map(guia -> {

            String procedimentoFormatado = formatarProcedimentosResumidos(guia.getProcedimentos());

            List<SlaGuiaDTO> slaDaGuia = historicoGuiaRepository.buscarDadosSlaPorAuditor(
                    auditorId,
                    guia.getDataSolicitacao().minusDays(1),
                    LocalDateTime.now()
            );

            List<SlaGuiaDTO> slaFiltrado = slaDaGuia.stream()
                    .filter(sla -> sla.idGuia().equals(guia.getId()))
                    .toList();

            String tempoAnalise = calcularTempoMedioFormatado(slaFiltrado);

            return new AvaliacaoRecenteDTO(
                    guia.getNumeroGuia(),
                    guia.getDataUltimaMovimentacao(),
                    guia.getBeneficiario().getNome(),
                    procedimentoFormatado,
                    tempoAnalise,
                    guia.getStatus()
            );
        }).toList();

        return new AuditorDetalhesResponse(auditor.getId(), auditor.getNome(), registro, auditor.getEspecialidade(), graficoDecisoes, ultimasAvaliacoes);
    }

    //FREQUÊNCIA E UTILIZAÇÃO
    @Transactional(readOnly = true)
    public FrequenciaUtilizacaoResponse gerarTelaFrequencia(String filtroTempo) {
        LocalDateTime dataInicioAtual = calcularDataInicio(filtroTempo);
        LocalDateTime dataFimAtual = LocalDateTime.now();

        // DESCOBRE DATAS DO PERIODO ANTERIOR
        LocalDateTime[] periodoAnterior = calcularPeriodoAnterior(filtroTempo, dataInicioAtual, dataFimAtual);
        LocalDateTime dataInicioAnterior = periodoAnterior[0];
        LocalDateTime dataFimAnterior = periodoAnterior[1];

        //TOP5 PROCEDIMENTOS AUTORIZADOS
        List<TopProcedimentoDTO> topProcedimentos = procedimentoGuiaRepository
                .buscarTopProcedimentosAutorizadosNoPeriodo(dataInicioAtual, dataFimAtual, PageRequest.of(0, 5));

        //TOP 5 PRESTADORES
        List<TopEntidadeDTO> prestadoresData = guiaRepository
                .buscarTopPrestadores(dataInicioAtual, dataFimAtual, PageRequest.of(0, 5));

        List<TopPrestadorDTO> topPrestadores = prestadoresData.stream().map(prestador -> {

            long guiasMesAnterior = guiaRepository.countByPrestadorIdAndDataSolicitacaoBetween(
                    prestador.id(), dataInicioAnterior, dataFimAnterior);

            double crescimento = calcularCrescimentoPercentual(prestador.totalGuias(), guiasMesAnterior);
            String tendencia = definirTendencia(crescimento);

            return new TopPrestadorDTO(
                    prestador.id(),
                    prestador.nome(),
                    prestador.documentoOuCarteirinha(),
                    prestador.totalGuias(),
                    crescimento,
                    tendencia
            );
        }).toList();

        // TOP 5 BENEFICIÁRIOS
        List<TopEntidadeDTO> beneficiariosData = guiaRepository
                .buscarTopBeneficiarios(dataInicioAtual, dataFimAtual, PageRequest.of(0, 5));

        List<TopBeneficiarioDTO> topBeneficiarios = beneficiariosData.stream().map(paciente -> {

            long guiasMesAnterior = guiaRepository.countByBeneficiarioIdAndDataSolicitacaoBetween(
                    paciente.id(), dataInicioAnterior, dataFimAnterior);

            String tendencia = definirTendencia(calcularCrescimentoPercentual(paciente.totalGuias(), guiasMesAnterior));

            List<TopProcedimentoDTO> procedimentosDoPaciente = procedimentoGuiaRepository
                    .buscarPrincipalProcedimentoPorBeneficiario(paciente.id(), dataInicioAtual, dataFimAtual, PageRequest.of(0, 1));

            String principalProcedimento = procedimentosDoPaciente.isEmpty() ? "Não identificado" : procedimentosDoPaciente.get(0).descricao();

            return new TopBeneficiarioDTO(
                    paciente.id(),
                    paciente.nome(),
                    paciente.documentoOuCarteirinha(),
                    paciente.totalGuias(),
                    principalProcedimento,
                    tendencia
            );
        }).toList();

        return new FrequenciaUtilizacaoResponse(topProcedimentos, topPrestadores, topBeneficiarios);
    }

    @Transactional(readOnly = true)
    public PrestadorDetalhesResponse gerarDetalhesPrestador(UUID prestadorId) {


        LocalDateTime dataCorte6Meses = LocalDateTime.now()
                .minusMonths(6)
                .withDayOfMonth(1)
                .toLocalDate()
                .atStartOfDay();

        //VOLUME DE GUIAS, AGRUPADO POR STATUS E MES
        List<HistoricoMensalDTO> graficoVolume = guiaRepository
                .historicoMensalPrestador(prestadorId, dataCorte6Meses);

        //PROCEDIMENTO FAVORITO (DATA ANTIGA PRA PEGAR TOTLA)
        LocalDateTime inicioGeral = LocalDateTime.of(2020, 1, 1, 0, 0);
        List<TopProcedimentoDTO> procedimentosFavoritos = procedimentoGuiaRepository
                .buscarProcedimentosFavoritosPorPrestador(
                        prestadorId,
                        inicioGeral,
                        LocalDateTime.now(),
                        PageRequest.of(0, 5) // Traz os Top 5 favoritos
                );

        return new PrestadorDetalhesResponse(graficoVolume, procedimentosFavoritos);
    }

    @Transactional(readOnly = true)
    public BeneficiarioDetalhesResponse gerarDetalhesBeneficiario(UUID beneficiarioId) {

        LocalDateTime dataCorte6Meses = LocalDateTime.now()
                .minusMonths(6)
                .withDayOfMonth(1)
                .toLocalDate()
                .atStartOfDay();

        //HISTÓRICO DE GUIAS AGRUPADO POR DATA E STATUS
        List<HistoricoMensalDTO> graficoVolume = guiaRepository
                .historicoMensalBeneficiario(beneficiarioId, dataCorte6Meses);

        // LINHA DO TEMPO(ÚLTIMOS 10)
        Page<GuiaAutorizacao> guiasRecentes = guiaRepository
                .findByBeneficiarioIdOrderByDataSolicitacaoDesc(beneficiarioId, PageRequest.of(0, 10));

        List<LinhaTempoGuiaDTO> linhaDoTempo = guiasRecentes.stream().map(guia -> {


            String procedimentoFormatado = formatarProcedimentosResumidos(guia.getProcedimentos());

            return new LinhaTempoGuiaDTO(
                    guia.getNumeroGuia(),
                    guia.getDataUltimaMovimentacao(),
                    guia.getStatus(),
                    procedimentoFormatado
            );
        }).toList();

        return new BeneficiarioDetalhesResponse(graficoVolume, linhaDoTempo);
    }

    //DASHBOARD PRESTADOR
    @Transactional(readOnly = true)
    public DashboardPrestadorResponse gerarDashboardPrestador(UUID prestadorId, Pageable pageableGuias, Pageable pageableAvisos) {

        List<StatusCountDTO> contagens = guiaRepository.contarStatusPorPrestador(prestadorId);

        long emAndamento = 0;
        long pendencias = 0;
        long autorizadas = 0;
        long negadas = 0;

        for (StatusCountDTO count : contagens) {
            switch (count.status()) {
                case EM_ANALISE, EM_AUDITORIA -> emAndamento += count.total();
                case PENDENCIA, PENDENCIA_RESPONDIDA -> pendencias += count.total();
                case AUTORIZADA -> autorizadas += count.total();
                case NEGADA -> negadas += count.total();
            }
        }

        // ÚLTIMAS SOLICITAÇÕES (PAGINADA)
        Page<GuiaAutorizacao> guiasPage = guiaRepository.findByPrestadorIdOrderByDataSolicitacaoDesc(prestadorId, pageableGuias);
        Page<GuiaResponse> ultimasSolicitacoes = guiasPage.map(GuiaMapper::converteEntidade);

        // AVISOS (PAGINADO)
        Page<AvisoResponse> avisosRecentes = avisoService.buscarAvisosPorDestinatario(
                prestadorId,
                TipoDestinatario.PRESTADOR,
                pageableAvisos
        );

        return new DashboardPrestadorResponse(
                emAndamento,
                pendencias,
                autorizadas,
                negadas,
                ultimasSolicitacoes,
                avisosRecentes
        );
    }

    private LocalDateTime calcularDataInicio(String filtro) {
        LocalDateTime agora = LocalDateTime.now();
        if (filtro == null) return YearMonth.now().atDay(1).atStartOfDay();

        return switch (filtro.toLowerCase()) {
            case "7dias" -> agora.minusDays(7);
            case "30dias" -> agora.minusDays(30);
            case "estemes" -> YearMonth.now().atDay(1).atStartOfDay();
            default -> LocalDateTime.of(2020, 1, 1, 0, 0);
        };
    }

    private double calcularTaxaAprovacao(List<StatusCountDTO> statusList, long totalSolicitacoes) {
        if (totalSolicitacoes == 0) return 0.0; // PROTEÇÃO CONTRA DIVISÃO POR ZERO

        long totalAprovadas = statusList.stream()
                .filter(dto -> dto.status() == StatusGuia.AUTORIZADA)
                .mapToLong(StatusCountDTO::total)
                .sum();

        return (totalAprovadas * 100.0) / totalSolicitacoes;
    }

    //MÉTODOS PARA TAXAS E SLA
    private double calcularTaxaPorStatus(List<StatusCountDTO> statusList, long total, List<StatusGuia> alvos) {
        if (total == 0) return 0.0;

        long somaAlvos = statusList.stream()
                .filter(dto -> alvos.contains(dto.status()))
                .mapToLong(StatusCountDTO::total)
                .sum();

        return (somaAlvos * 100.0) / total;
    }

    private String calcularTempoMedioFormatado(List<SlaGuiaDTO> slaList) {
        if (slaList == null || slaList.isEmpty()) return "00h 00m";

        long totalSegundos = 0;
        for (SlaGuiaDTO sla : slaList) {
            if (sla.dataInicioAnalise() != null && sla.dataFimAnalise() != null) {
                Duration duracao = Duration.between(sla.dataInicioAnalise(), sla.dataFimAnalise());
                totalSegundos += duracao.getSeconds();
            }
        }

        if (totalSegundos == 0) return "00h 00m";

        long mediaSegundos = totalSegundos / slaList.size();
        long horas = mediaSegundos / 3600;
        long minutosRestantes = (mediaSegundos % 3600) / 60;
        
        if (horas == 0 && minutosRestantes == 0) {
            return "< 1m";
        }

        return String.format("%02dh %02dm", horas, minutosRestantes);
    }

    private LocalDateTime[] calcularPeriodoAnterior(String filtroTempo, LocalDateTime dataInicioAtual, LocalDateTime dataFimAtual) {

        if (filtroTempo == null || filtroTempo.equalsIgnoreCase("estemes")) {
            return new LocalDateTime[]{
                    dataInicioAtual.minusMonths(1),
                    dataFimAtual.minusMonths(1)
            };
        }

        return switch (filtroTempo.toLowerCase()) {
            case "7dias" -> new LocalDateTime[]{
                    dataInicioAtual.minusDays(7),
                    dataFimAtual.minusDays(7)
            };

            case "30dias" -> new LocalDateTime[]{
                    dataInicioAtual.minusDays(30),
                    dataFimAtual.minusDays(30)
            };

            default -> new LocalDateTime[]{dataInicioAtual, dataFimAtual};
        };
    }

    private double calcularCrescimentoPercentual(long atual, long anterior) {
        if (anterior == 0 && atual > 0) return 100.0;
        if (anterior == 0 && atual == 0) return 0.0;

        return ((double) (atual - anterior) / anterior) * 100.0;
    }

    private String definirTendencia(double crescimentoPercentual) {
        if (crescimentoPercentual > 5.0) return "ALTA";
        if (crescimentoPercentual < -5.0) return "BAIXA";
        return "ESTAVEL";
    }

    private String formatarProcedimentosResumidos(List<ProcedimentoGuia> procedimentos) {
        if (procedimentos == null || procedimentos.isEmpty()) {
            return "Nenhum procedimento";
        }

        String procedimentoFormatado = procedimentos.get(0).getProcedimento().getDescricao();

        if (procedimentos.size() > 1) {
            procedimentoFormatado += " + " + (procedimentos.size() - 1);
        }

        return procedimentoFormatado;
    }
}