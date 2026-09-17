# AutorizaMed API
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=Spring-Security&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)

AutorizaMed é um sistema completo e robusto para autorização de guias médicas, gerenciamento de elegibilidade, auditoria clínica e relatórios operacionais. Desenvolvido com as melhores práticas de mercado utilizando o ecossistema Spring Boot, a API oferece uma solução corporativa escalável para operadoras de saúde, prestadores (clínicas/hospitais) e auditores médicos.

## 🚀 Principais Funcionalidades

### 🔐 Segurança e Controle de Acesso (RBAC)
- Autenticação JWT (JSON Web Token) segura.
- Perfis de acesso distintos: `ADMIN`, `AUDITOR`, `PRESTADOR`, `FUNCIONARIO_PRESTADOR`, `BENEFICIARIO`.
- Filtros de segurança granulares protegendo rotas por roles.

### 🏥 Gestão de Elegibilidade
- Controle completo de Beneficiários, Prestadores, Funcionários e Rede Credenciada.
- Validação de regras de negócio complexas, como carência, validade da carteirinha e tipo de plano (Premium, Intermediário, Básico).

### 📋 Guias de Autorização Médica
- Fluxo completo da guia: Solicitação -> Análise -> Pendência -> Auditoria -> Autorizada/Negada.
- Anexos e laudos vinculados diretamente à guia.
- Histórico imutável de todas as movimentações e mudanças de status.
- Controle rigoroso de SLAs (Data limite de aprovação).

### 📊 Relatórios e Dashboards
- Dashboards customizados por perfil (Admin vs Prestador).
- KPIs de SLA, produtividade de auditores, frequências de utilização (Top Procedimentos, Prestadores, Beneficiários).
- Acompanhamento da linha do tempo evolutiva das Guias.

### 👁️ Auditoria Transparente (AOP)
- Sistema avançado de rastreabilidade usando Aspect-Oriented Programming (AOP).
- Registro automático de ações (`CREATE`, `UPDATE`, `INACTIVATE`, `LOGIN`) no sistema, garantindo compliance e segurança da informação sem poluir as regras de negócio.

## 💻 Tecnologias Utilizadas

- **Linguagem:** Java 17
- **Framework:** Spring Boot 3
- **Persistência:** Spring Data JPA / Hibernate
- **Banco de Dados:** PostgreSQL
- **Segurança:** Spring Security + Auth0 Java JWT
- **Boilerplate Reduction:** Lombok
- **Arquitetura:** Camadas (Controllers, Services, Repositories, Entities, DTOs, Mappers, Events, Aspects)

## 🏗️ Arquitetura e Destaques Técnicos (Deep Dive)

O projeto vai muito além de um CRUD tradicional, apresentando soluções arquiteturais para problemas reais do dia a dia de sistemas de alta volumetria e complexidade no setor de saúde:

- **Otimização de Memória no Hibernate (JPA Lazy Loading):** Para evitar problemas de `OutOfMemoryError` comuns em sistemas médicos, os arquivos binários pesados dos anexos (laudos/imagens) foram extraídos para uma entidade separada (`AnexoConteudo`) e mapeados via `@OneToOne(fetch = FetchType.LAZY)`. Isso garante que listar guias não sobrecarregue o banco ou a JVM.
- **Consultas Analíticas de Alta Performance (JPA Projections):** No módulo de relatórios (`GuiaAutorizacaoRepository`), o sistema evita buscar a entidade inteira do banco (`SELECT *`), utilizando *Constructor Expressions* (`new com.autorizamed.api.relatorio.dto...`) direto na Query JPQL. Isso economiza drasticamente o uso de CPU e Memória para a geração de Dashboards.
- **Motor de Regras de Negócio Avançado:** O `GuiaAutorizacaoService` possui validações de contratos complexas, cruzando `RedeCredenciada` (o prestador atende aquele `Procedimento`?) com o `TipoPlano` do beneficiário (Básico, Intermediário, Premium).
- **Controle de Frequência Antifraude:** O sistema implementa uma checagem de frequência (`contarProcedimentoRecente`), barrando ou enviando para auditoria pacientes que solicitam exames idênticos dentro de um intervalo de 30 dias.
- **Separação de Entidades e Contratos:** Isolamento estrito usando Records e DTOs, blindando o backend contra vazamento de atributos internos e ataques de *Mass Assignment*.
- **Confiabilidade com Transactional Event Listeners:** O uso de `ApplicationEventPublisher` aliado a `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` garante que disparos de notificações (Avisos) só ocorram se a transação do banco for concluída com sucesso, evitando falsos positivos e "side effects" perigosos em caso de Rollbacks.

## 🚦 Como Executar

### Pré-requisitos
- Java 17+
- Maven
- PostgreSQL rodando na porta 5432 (ou altere no `application.properties`)

### Passos
1. Clone o repositório.
2. Crie um banco de dados chamado `autorizamed` no PostgreSQL.
3. Configure as credenciais no arquivo `src/main/resources/application.properties`:
   ```properties
   spring.datasource.username=postgres
   spring.datasource.password=sua_senha
   ```
4. Execute o comando:
   ```bash
   ./mvnw spring-boot:run
   ```
5. A API estará disponível em `http://localhost:8080`.

**Nota de Teste:** O sistema possui um `DataInitializer` que povoará o banco de dados com administradores, prestadores, beneficiários e dezenas de guias em diferentes status para testes imediatos. Um arquivo `schema.sql` também está disponível na raiz caso deseje popular via script direto.

## 🗺️ Roadmap e Evolução Futura (AI Integration)

O projeto está em evolução contínua, com foco atual na transição para um sistema inteligente. As próximas atualizações arquiteturais incluem:

- [ ] **Auditoria Automatizada por IA:** Integração com modelos LLM via **LangChain4j** para pré-análise de guias, extraindo justificativas clínicas estruturadas e sugerindo aprovação ou negação[cite: 1].
- [ ] **Copilot do Auditor (UX Fluida):** Implementação de **Server-Sent Events (SSE)** no backend para realizar o streaming da análise da IA em tempo real para o frontend em Next.js[cite: 1].
- [ ] **Resiliência de Integração:** Blindagem da comunicação com provedores de IA utilizando **Resilience4j** (Circuit Breakers e Exponential Backoff) para garantir alta disponibilidade do sistema central[cite: 1].
- [ ] **Observabilidade de Custos:** Criação de um sistema de *Token Tracking* via logs para auditar o consumo e o custo de cada análise gerada[cite: 1].

## 📄 Licença

Este projeto é destinado a fins de portfólio. Todos os direitos reservados.
