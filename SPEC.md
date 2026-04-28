# Especificação Técnica (SPEC.md) - sipprog-rh-backend

## 1. Visão Geral e Requisitos
O projeto `sipprog-rh-backend` é um backend desenvolvido em Java (Spring Boot) responsável pelo Sistema de Gestão de Desempenho e Inteligência (SIGDI) e gestão de Funcionários.
O foco atual e a prioridade de negócio incidem sobre o módulo **SIGDI**, especificamente na integração total e correta com o frontend para os submódulos **Tático** (Tatical) e **Estratégico** (Strategy).

**Principais Requisitos Atuais:**
- **Integração Frontend (INT):** Ajustar DTOs de Request/Response nos `TaticalController` e `StrategyController` para garantir compatibilidade exata com o frontend. Implementar endpoints em falta para a gestão completa de atividades e OKRs. Garantir que fluxos de autenticação (OAuth2) e CORS não bloqueiem pedidos do frontend.
- **Consolidação e Qualidade (QUAL):** Refatorar o código para remover quaisquer dados mockados. Todo o fluxo deve utilizar a base de dados real (via JPA/Hibernate). Remover testes exploratórios antigos e criar testes baseados em lógicas de negócio reais. Otimizar as queries SQL nas listagens principais para evitar problemas de *N+1 queries* e usar paginação eficiente.

## 2. Arquitetura e Design
O projeto segue uma arquitetura baseada em **Domain-Driven Design (DDD)** combinada com **CQRS (Command Query Responsibility Segregation)** e estruturada de acordo com os princípios da **Clean Architecture**.

**Tecnologias Principais:**
- **Java** (Spring Boot 3 / Spring Web)
- **Spring Data JPA / Hibernate** (Persistência)
- **Spring Security / OAuth2** (Autenticação/Autorização)
- **iGRP Studio** (Ferramenta Low-code / Gerador de Base de Código). O diretório `.igrpstudio` funciona como a "Single Source of Truth" para definições de Controllers, Actions e DTOs via ficheiros JSON.

**Estrutura de Pastas (Baseada em DDD):**
`src/main/java/cv/igrp/RH_Service/`
- `/funcionarios` - Contexto de negócio de RH (Gestão de cargos, contratos, funcionários).
- `/sigdi` - Contexto de negócio principal (Prioridade).
  - `/application` - Camada de Aplicação. Contém `commands/` (operações de escrita), `queries/` (operações de leitura) e `dto/` (objetos de transferência de dados).
  - `/domain` - Camada de Domínio. Contém Entidades, Agregados, Value Objects e Exceções de Domínio.
  - `/infrastructure` - Camada de Infraestrutura. Contém os Repositórios (JPA), configurações de segurança, integrações externas.
  - `/interfaces` - Camada de Interfaces. Contém os controladores REST (`/rest`).

**Padrão CQRS:**
O fluxo de dados passa sempre pelo `CommandBus` (para mutações) ou `QueryBus` (para leituras), isolando completamente os controladores da lógica de negócio complexa.

## 3. Contratos Técnicos (API/Interfaces)
As APIs estão definidas sob o base path `api/v1/`. As definições completas encontram-se mapeadas nos ficheiros JSON dentro de `.igrpstudio/sigdi/dto` e `.igrpstudio/sigdi/actions`.

**Endpoints Táticos (`TaticalController` - `api/v1/tactical`):**
- `GET /activities` - Listagem de atividades táticas (paginada via `pageNumber` e `pageSize`). Retorna `WrapperListTaticalActivityDTO`.
- `POST /activities` - Criação de atividade tática.
- `PATCH /activities/{id}/status` - Atualização de estado.
- `GET /activities/{id}` - Detalhes da atividade (`TacticalActivityDetailDTO`).
- `POST /activities/{id}/submit` - Submete uma atividade DRAFT para aprovação.
- `POST /activities/{id}/approve` / `/reject` - Aprovação ou rejeição de atividade via workflow.
- Gestão de KRs e OKRs: `POST /krs`, `GET /krs`, `POST /krs/{id}/checkin`, `POST /okrs`.
- Gestão de Workflow e Change Requests: `GET /workflow/inbox`, `POST /activities/{id}/change-requests`.

**Endpoints Estratégicos (`StrategyController` - `api/v1/strategy`):**
- `POST /identities` & `GET /identities/current` - Gestão da Identidade Estratégica (Missão, Visão, Valores).
- `POST /goals` & `GET /goals` & `PATCH /goals/{id}` & `DELETE /goals/{id}` - Gestão de Metas Estratégicas (`StategicGoalResponseDTO`). A exclusão (cancelamento) exige regras estritas (soft-cancel).
- `POST /map/links` & `GET /map/current` & `DELETE /map/links/{id}` - Gestão do Mapa Estratégico (relações entre metas e posições visuais no Canvas BSC via `PATCH /map/nodes/{goalId}/position`).

## 4. Regras de Negócio
As lógicas cruciais extraídas do contexto e código:
1. **Dados Reais Apenas (No Mocks):** A regra fundamental de qualidade proíbe a utilização de dados falsos ou "hardcoded" nos serviços e controladores. Todas as operações devem validar e persistir contra a base de dados.
2. **Workflow de Atividades Táticas:** Uma atividade possui ciclos de vida geridos através de endpoints específicos (`submit`, `approve`, `reject`). A aprovação/rejeição necessita de comentários (`WorkflowCommentDTO`). Atividades só podem transitar de `DRAFT` para submetidas e, subsequentemente, aprovadas/rejeitadas.
3. **Change Requests em Atividades:** Qualquer alteração a uma atividade já `APPROVED` tem de passar por um fluxo de *Change Request* (pedido de alteração), que também exige aprovação (`approveChangeRequest`, `rejectChangeRequest`).
4. **Soft-Delete de Metas Estratégicas:** Um `DELETE /goals/{id}` não elimina fisicamente a meta da base de dados se houver ligações de histórico (PAA), mas sim efetua um "soft-cancel". Retorna erro `422 Unprocessable Entity` se houver atividades PAA ativas associadas.
5. **Paginação e Performance:** Consultas de listas (como `activities`, `goals`, `workflow/inbox`) têm de ser obrigatoriamente paginadas e as queries JPA otimizadas para prevenir falhas de performance (*N+1 queries*).

## 5. Fluxo de Trabalho de IA (AI Guidelines)
Para garantir consistência nas futuras iterações e evitar alucinações, qualquer assistente de IA deve aderir estritamente às seguintes regras ao atuar no projeto `sipprog-rh-backend`:

- **Arquitetura (Strict DDD & CQRS):**
  - Nunca injetar Repositórios diretamente nos Controladores.
  - Qualquer novo endpoint deve criar um respetivo `Command` ou `Query` e o seu handler (`CommandHandler` / `QueryHandler`).
  - Injetar dependências apenas nos Handlers via construtor.
- **Modificações iGRP Studio:**
  - Os Controladores têm no topo a anotação `/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */`. Apenas adicione novos endpoints modificando os ficheiros JSON na pasta `.igrpstudio` se a intenção for propagar a configuração ao gerador. (Se a IA estiver a modificar diretamente o código localmente, deve garantir que as definições refletem no modelo mental do iGRP).
- **Contratos DTO:**
  - Não partilhar DTOs entre `Request` e `Response`.
  - Usar anotações Swagger/OpenAPI (`@Operation`, `@ApiResponse`, `@Schema`) em cada novo endpoint para manter a documentação viva e os contratos estritos com o frontend.
- **Regras de Código (Qualidade):**
  - **PROIBIDO MOCKAR:** Nunca escreva "mock data" para testes ou respostas de API. Conecte-se sempre à base de dados.
  - **JPA Performance:** Use `EntityGraph` ou `@Query("SELECT ... JOIN FETCH ...")` em queries que retornem listas para evitar problemas de N+1.
- **CORS e Segurança:**
  - Garantir que todos os controladores estão protegidos (via `@PreAuthorize` se aplicável).

## 6. Objetivo Final
Esta especificação atua como a única fonte da verdade para o backend do SIGDI (`sipprog-rh-backend`). Qualquer implementação futura (ex: "Criar novo endpoint de relatório no módulo Tático") deve consultar este documento para validar a arquitetura (criar Query/Handler em `application/queries`), verificar contratos (definir Request/Response DTO em `application/dto`) e alinhar-se às regras de qualidade (sem mocks, com otimização N+1).