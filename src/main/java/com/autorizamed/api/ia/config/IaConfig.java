package com.autorizamed.api.ia.config;

import com.autorizamed.api.ia.service.AssistenteAuditoria;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.Duration;

@Configuration
public class IaConfig {
    @Value("${langchain4j.google-ai-gemini.chat-model.api-key}")
    private String apiKey;
    @Value("${langchain4j.google-ai-gemini.chat-model.model-name}")
    private String modelName;
    @Value("${langchain4j.google-ai-gemini.chat-model.temperature}")
    private Double temperature;
    @Bean
    public ChatLanguageModel geminiChatModel() {
        GoogleAiGeminiChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(60))
                .logRequestsAndResponses(true)
                .build();

        try {
            Field serviceField = GoogleAiGeminiChatModel.class.getDeclaredField("geminiService");
            serviceField.setAccessible(true);
            Object proxy = serviceField.get(model);
            InvocationHandler handler = Proxy.getInvocationHandler(proxy);
            Field retrofitField = handler.getClass().getDeclaredField("this$0");
            retrofitField.setAccessible(true);
            Retrofit retrofit = (Retrofit) retrofitField.get(handler);
            Field callFactoryField = Retrofit.class.getDeclaredField("callFactory");
            callFactoryField.setAccessible(true);
            OkHttpClient oldClient = (OkHttpClient) callFactoryField.get(retrofit);
            OkHttpClient hardenedClient = oldClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(60))
                    .readTimeout(Duration.ofSeconds(60))
                    .writeTimeout(Duration.ofSeconds(60))
                    .callTimeout(Duration.ofSeconds(60))
                    .build();
            callFactoryField.set(retrofit, hardenedClient);
        } catch (Exception e) {

        }
        return model;

    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return GoogleAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-embedding-001")
                .outputDimensionality(768)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(DataSource dataSource) {
        return PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .table("tb_diretriz_ans")
                .dimension(768)
                .createTable(true)
                .build();
    }

    @Bean
    public AssistenteAuditoria assistenteAuditoria(
            ChatLanguageModel chatLanguageModel,
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore) {
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.5)
                .build();
        return AiServices.builder(AssistenteAuditoria.class)
                .chatLanguageModel(chatLanguageModel)
                .contentRetriever(contentRetriever)
                .build();
    }
}