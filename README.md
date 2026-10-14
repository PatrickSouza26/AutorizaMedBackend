# 🏥 AutorizaMed - Plataforma Inteligente de Regulação e Auditoria Médica

> Sistema corporativo de autorização de guias médicas com **Pipeline Autônomo de Inteligência Artificial Generativa**, arquitetura orientada a eventos, **RAG regulatório nativo com pgvector** e engenharia de resiliência empresarial.

---

## 📌 1. Visão Geral do Projeto

No ecossistema de Saúde Suplementar brasileiro, a regulação de procedimentos exige análise técnica e jurídica rigorosa contra as normas da **Agência Nacional de Saúde Suplementar (ANS)**. O processo tradicional de auditoria humana enfrenta gargalos operacionais críticos:
* **Tempo de espera elevado** para procedimentos simples que poderiam ser autorizados imediatamente.
* **Sobrecarga dos médicos auditores** com triagens manuais repetitivas.
* **Risco de inconsistências e glosas** por divergências de cobertura contratual e regulatória.

O **AutorizaMed** moderniza essa esteira através de um **Agente Autônomo de Triagem Clínica** construído com **Spring Boot 4**, **LangChain4j**, **Google Gemini** e **pgvector**, implementando o modelo *Human-in-the-Loop* (humano no circuito) com foco em confiabilidade, auditoria e velocidade.

---

## 🏗️ 2. Arquitetura da Solução de IA

O sistema adota uma **Arquitetura Orientada a Eventos (EDA)** e **Totalmente Desacoplada**, garantindo que a requisição de cadastro da guia retorne em milissegundos para o usuário enquanto o processamento cognitivo ocorre em segundo plano.

```mermaid
flowchart TD
    subgraph FRONTEND["Frontend (Next.js)"]
        SOL["Solicitação de Guia (Prestador)"]
        PAINEL["Painel do Auditor (Copiloto)"]
    end

    subgraph CORE["Núcleo da Aplicação (Spring Boot)"]
        SVC["GuiaAutorizacaoService"]
        EVENT["GuiaEmAnaliseEvent"]
        LISTENER["AuditoriaIaListener (@Async)"]
    end

    subgraph RESILIENCIA["Camada de Resiliência (Resilience4j)"]
        RETRY["@Retry (Backoff Exponencial)"]
        CB["@CircuitBreaker (Disjuntor)"]
        FALLBACK["Fallback de Emergência"]
    end

    subgraph RAG_LAYER["RAG Regulatório (LangChain4j + pgvector)"]
        EMB_MODEL["gemini-embedding-001 (768d)"]
        PGV[("PostgreSQL 18 + pgvector\n(tb_diretriz_ans)")]
        PROMPT["Grounding / Ancoragem"]
    end

    subgraph LLM["Modelo de Decisão"]
        GEMINI["Google Gemini 2.5 Flash"]
    end

    SOL -->|POST /guias| SVC
    SVC -->|commit no banco| EVENT
    EVENT -->|TransactionPhase.AFTER_COMMIT| LISTENER
    LISTENER --> RETRY
    RETRY --> CB
    CB -->|1. Busca Semântica| EMB_MODEL
    EMB_MODEL -->|2. Cosine Similarity| PGV
    PGV -->|3. Diretrizes DUT ANS| PROMPT
    PROMPT -->|4. Análise Clínica| GEMINI
    
    GEMINI -->|Aprovado (Fast-Track)| SVC
    GEMINI -->|Divergente| PAINEL
    CB -.->|Falha/Timeout| FALLBACK
    FALLBACK -.->|Triagem Manual Segura| PAINEL
```

---

## 💎 3. Diferenciais de Engenharia de Software

### 🧠 A. RAG Regulatório Nativo com `pgvector`
* **Zero Overhead de SaaS Externo:** O banco vetorial é mantido na mesma instância do PostgreSQL através da extensão nativa `vector(768)`, aproveitando a infraestrutura relacional, segurança ACID e backups unificados.
* **Busca Semântica por Similaridade de Cosseno:** Utiliza o modelo `gemini-embedding-001` para mapear sintomas e procedimentos em um espaço vetorial de 768 dimensões, recuperando com precisão as **Diretrizes de Utilização (DUTs)** da ANS pertinentes ao caso.
* **Ancoragem Estrita (*Grounding*):** Elimina alucinações ao forçar a IA a justificar pareceres médicos fundamentando-se explicitamente nas normas regulatórias recuperadas do banco.

### 🛡️ B. Engenharia de Resiliência Corporativa (`Resilience4j`)
* **Retry com Backoff Exponencial:** Intercepta falhas transitórias de rede ou *rate limits* com retentativas espaçadas (2s, 4s, 8s).
* **Circuit Breaker (Disjuntor de Proteção):** Monitora a taxa de falhas da API externa. Se a taxa de erro ultrapassar 50% em uma janela deslizante, o circuito abre temporariamente (30s), impedindo sobrecarga no servidor e consumo desnecessário de CPU.
* **Fallback Degradado Gracioso:** Em caso de indisponibilidade severa da IA, a guia não é travada: o sistema anexa um alerta de contingência e a encaminha silenciosamente para a esteira humana.

### ⚡ C. Execução Assíncrona e Consistência Transacional
* **`@TransactionalEventListener(phase = AFTER_COMMIT)`:** Assegura que o evento de auditoria inteligente só seja disparado após a guia estar 100% persistida no banco relacional, eliminando condições de corrida (*race conditions*).
* **Processamento `@Async` Não-Bloqueante:** O prestador recebe a confirmação de protocolo em milissegundos; a IA trabalha de forma assíncrona em uma thread dedicada.

### 🩺 D. Copiloto Clínico no Frontend (*Human-in-the-Loop*)
* A interface do auditor exibe um card com destaque visual apresentando o parecer gerado pela IA.
* **Ação Rápida em 1 Clique:** O auditor pode revisar e importar o parecer técnico diretamente para a justificativa oficial da negativa ou pendência, acelerando o tempo de ciclo operacional.

---

## 🛠️ 4. Stack Tecnológica

| Camada | Tecnologias |
| :--- | :--- |
| **Backend** | Java 17, Spring Boot 4.1.0, Spring Data JPA, Spring Security (JWT), HikariCP |
| **Inteligência Artificial** | LangChain4j 0.35.0, Google Gemini 2.5 Flash, `gemini-embedding-001` |
| **Banco de Dados** | PostgreSQL 18.3, Extensão `pgvector` (0.8.6) |
| **Tolerância a Falhas** | Resilience4j 2.4.0 (Circuit Breaker, Retry, Fallback) |
| **Frontend** | Next.js 14, React, TypeScript, CSS Modules, Lucide Icons |
| **Testes** | JUnit 5, AssertJ, Spring Boot Test |

---

## 🚀 5. Como Executar o Projeto Localmente

### Pré-requisitos
* **Java 17+** e **Maven 3.8+**
* **PostgreSQL 18** com a extensão `pgvector` instalada
* **Node.js 18+** e **npm**
* Chave de API gratuita do [Google AI Studio](https://aistudio.google.com/)

---

### Passo 1: Configurar a Base de Dados
No seu terminal PostgreSQL ou cliente SQL (DBeaver/pgAdmin):
```sql
CREATE DATABASE autorizamed;
\c autorizamed;
CREATE EXTENSION IF NOT EXISTS vector;
```

---

### Passo 2: Configurar o Backend
No arquivo `api/src/main/resources/application.properties`, configure as credenciais do banco e sua chave do Google Gemini:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/autorizamed
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

langchain4j.google-ai-gemini.chat-model.api-key=SUA_CHAVE_GEMINI_AQUI
langchain4j.google-ai-gemini.chat-model.model-name=gemini-2.5-flash
langchain4j.google-ai-gemini.chat-model.temperature=0.0
```

Compile e execute o backend:
```bash
cd api
mvn clean spring-boot:run
```

> [!NOTE]
> **Carga Automática (*Zero-Setup*):** Na primeira inicialização, o componente `DiretrizAnsSeeder` identifica que a base vetorial está vazia e ingere automaticamente as Diretrizes Clínicas da ANS (DUT 42, DUT 01, DUT 108, DUT 65) no `pgvector`.

---

### Passo 3: Executar o Frontend
```bash
cd front
npm install
npm run dev
```
Acesse a aplicação no navegador em: `http://localhost:3000`.

---

## 🧪 6. Testes Automatizados

O projeto conta com suite de testes de integração cobrindo o ciclo completo do RAG, a resiliência e a persistência no banco:

```bash
cd api
mvn test -Dtest=AuditoriaIaServiceTest
```

**Cenários cobertos:**
1. **Recuperação e Decisão Semântica:** Validação de pertinência clínica cruzando procedimentos contra a base vetorial da ANS.
2. **Disparo do Circuit Breaker:** Teste de retentativas automáticas e ativação do modo de contingência em caso de indisponibilidade da IA.
