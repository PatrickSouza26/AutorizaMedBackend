package com.autorizamed.api.ia.seeder;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiretrizAnsSeeder implements CommandLineRunner {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            // VERIFICAÇÃO SE BANCO VETORIAL JÁ EXISTE
            Integer totalVetores = jdbcTemplate.queryForObject("SELECT count(*) FROM tb_diretriz_ans", Integer.class);
            if (totalVetores != null && totalVetores > 0) {
                log.info("Base vetorial da ANS já contém {} diretrizes cadastradas. Ignorando seeder.", totalVetores);
                return;
            }
        } catch (Exception e) {
            log.info("Tabela vetorial ainda não existe ou vazia. Iniciando carga inicial...");
        }

        log.info("🧠 Iniciando ingestão das Diretrizes Oficiais da ANS no pgvector...");

        // DIRETRIZES
        List<DiretrizDTO> diretrizes = List.of(
                new DiretrizDTO(
                        "41101010",
                        "DUT 42",
                        "Ressonância Magnética do Crânio",
                        "Diretriz de Utilização da ANS (DUT nº 42) - Ressonância Magnética de Crânio (TUSS 41101010): " +
                                "Cobertura obrigatória exclusivamente quando preenchido ao menos um dos seguintes critérios clínicos: " +
                                "a) Suspeita ou acompanhamento de tumores do Sistema Nervoso Central (SNC); " +
                                "b) Acidente Vascular Cerebral (AVC) agudo ou transitório; " +
                                "c) Esclerose múltipla ou doenças desmielinizantes; " +
                                "d) Epilepsia de difícil controle ou refratária; " +
                                "e) Cefaleia crônica progressiva acompanhada de sinais de alarme neurológico. " +
                                "ATENÇÃO: A cobertura NÃO é indicada nem obrigatória para sintomas gastrointestinais, dores abdominais agudas, diarreia ou cefaleia tensional sem déficit neurológico."
                ),
                new DiretrizDTO(
                        "41001015",
                        "DUT 01",
                        "Tomografia Computadorizada de Abdome Total",
                        "Diretriz de Utilização da ANS (DUT nº 01) - Tomografia Computadorizada de Abdome Total (TUSS 41001015): " +
                                "Cobertura obrigatória para: " +
                                "a) Dor abdominal aguda com suspeita de abdome agudo inflamatório, obstrutivo ou perfurativo (apendicite, diverticulite); " +
                                "b) Febre de origem indeterminada persistente com foco intra-abdominal suspeito; " +
                                "c) Estadiamento e seguimento oncológico de neoplasias abdominais; " +
                                "d) Trauma abdominal contuso com alteração ao exame físico ou instabilidade hemodinâmica."
                ),
                new DiretrizDTO(
                        "40101010",
                        "DUT 108",
                        "Ecocardiograma Transtorácico",
                        "Diretriz de Utilização da ANS (DUT nº 108) - Ecocardiograma Transtorácico (TUSS 40101010): " +
                                "Cobertura obrigatória para: " +
                                "a) Avaliação de sopros cardíacos patológicos; " +
                                "b) Diagnóstico e monitoramento de Insuficiência Cardíaca congestiva (IC); " +
                                "c) Avaliação de toxicidade cardíaca pré e pós-quimioterapia cardiotóxica; " +
                                "d) Investigação de síncope com suspeita de etiologia cardíaca. " +
                                "Não possui indicação para check-up de rotina em pacientes assintomáticos sem comorbidades."
                ),
                new DiretrizDTO(
                        "40201015",
                        "DUT 65",
                        "Colonoscopia",
                        "Diretriz de Utilização da ANS (DUT nº 65) - Colonoscopia Diagnóstica (TUSS 40201015): " +
                                "Cobertura obrigatória para: " +
                                "a) Sangramento retal ativo ou pesquisa de sangue oculto nas fezes positiva; " +
                                "b) Anemia ferropriva de causa não esclarecida; " +
                                "c) Diarreia crônica intratável com duração superior a 4 semanas sem causa infecciosa; " +
                                "d) Rastreamento de câncer colorretal a partir dos 45 anos ou antes se histórico familiar de primeiro grau. " +
                                "Contraindicada e não coberta para quadro agudo de diverticulite em fase inflamatória ativa."
                )
        );

        //VETORIZAÇÃO E GRAVAÇÃO NO POSTGRE
        for (DiretrizDTO item : diretrizes) {
            try {
                Metadata metadata = Metadata.from("codigo_tuss", item.codigoTuss())
                        .add("numero_dut", item.numeroDut())
                        .add("nome_procedimento", item.nomeProcedimento());

                TextSegment segment = TextSegment.from(item.textoRegra(), metadata);

                //MODELO DE VETORIZAÇÃO EM 768 DIMENSÕES DO GOOGLE
                Embedding embedding = embeddingModel.embed(segment).content();

                embeddingStore.add(embedding, segment);

                log.info("✅ {} ({}) vetorizada e persistida no pgvector!", item.numeroDut(), item.nomeProcedimento());
            } catch (Exception ex) {
                log.error("Erro ao vetorizar {}: {}", item.numeroDut(), ex.getMessage());
            }
        }

        log.info("🚀 Base vetorial da ANS carregada com sucesso no PostgreSQL!");
    }

    private record DiretrizDTO(String codigoTuss, String numeroDut, String nomeProcedimento, String textoRegra) {}
}