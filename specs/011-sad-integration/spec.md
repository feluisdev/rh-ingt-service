# Feature Specification: Integração SAD — Avaliações, Objectivos e Webhooks

**Feature Branch**: `011-sad-integration`  
**Created**: 2026-05-02  
**Status**: Draft  
**Input**: Módulo 6 da Spec Técnica v4.0 — proxy/read-through para o SAD (Sistema de Avaliação de Desempenho do SIGDI) + webhooks HMAC-SHA256 + mapeamento de identidades

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Colaborador consulta as suas avaliações de desempenho (Priority: P1)

Um colaborador autenticado acede à área self-service do RH e consulta a lista das suas avaliações de desempenho concluídas/homologadas, bem como o detalhe de uma avaliação específica. Os dados são lidos em tempo real a partir do SAD externo; rascunhos e avaliações não publicadas são ocultados.

**Why this priority**: É a funcionalidade mais visível para o colaborador e o valor central do módulo — acesso directo ao histórico de avaliações sem sair do portal RH.

**Independent Test**: Pode ser testado isoladamente fazendo chamadas autenticadas a `GET /me/external/evaluations` e `GET /me/external/evaluations/{externalId}` com mapeamento de identidade configurado, validando que apenas avaliações publicadas são devolvidas.

**Acceptance Scenarios**:

1. **Given** um colaborador activo com mapeamento SAD configurado, **When** faz `GET /me/external/evaluations`, **Then** recebe lista de avaliações com estado publicado/homologado (rascunhos omitidos), com dados actualizados do SAD.
2. **Given** um colaborador activo com mapeamento, **When** faz `GET /me/external/evaluations/{externalId}`, **Then** recebe o detalhe completo da avaliação indicada, validando que pertence ao próprio colaborador.
3. **Given** um colaborador sem mapeamento SAD configurado, **When** faz `GET /me/external/evaluations`, **Then** recebe lista vazia (sem erro).
4. **Given** um colaborador inactivo (`is_active = false`), **When** tenta aceder a qualquer endpoint `/me/external/*`, **Then** recebe HTTP 403.
5. **Given** o SAD está indisponível, **When** o colaborador tenta consultar avaliações, **Then** recebe HTTP 503 com mensagem descritiva (sem stack trace).

---

### User Story 2 — Colaborador consulta os seus objectivos no ciclo activo (Priority: P2)

Um colaborador autenticado consulta os seus objectivos de desempenho definidos no ciclo activo corrente no SAD. Os objectivos são lidos em tempo real do sistema externo; apenas o ciclo activo é considerado.

**Why this priority**: Complementa as avaliações — o colaborador pode acompanhar o progresso antes da avaliação final; menos crítico que o histórico de avaliações.

**Independent Test**: Pode ser testado chamando `GET /me/external/objectives` e validando que os objectivos retornados pertencem ao ciclo activo no SAD para aquele colaborador.

**Acceptance Scenarios**:

1. **Given** um colaborador activo com mapeamento e ciclo activo existente no SAD, **When** faz `GET /me/external/objectives`, **Then** recebe a lista de objectivos do ciclo activo.
2. **Given** um colaborador sem ciclo activo no SAD, **When** faz `GET /me/external/objectives`, **Then** recebe lista vazia (sem erro).
3. **Given** o SAD está indisponível, **When** o colaborador consulta objectivos, **Then** recebe HTTP 503 com mensagem descritiva.

---

### User Story 3 — Administrador RH lista ciclos de avaliação disponíveis (Priority: P3)

Um utilizador com papel administrativo RH consulta os ciclos de avaliação disponíveis no SAD para fins de gestão e configuração interna (ex: associar ciclos a períodos de contrato).

**Why this priority**: Funcionalidade administrativa de suporte; o colaborador não depende dela para o seu self-service.

**Independent Test**: Pode ser testado chamando `GET /external/evaluation-cycles` com credenciais administrativas e validando a lista de ciclos retornados pelo SAD.

**Acceptance Scenarios**:

1. **Given** um utilizador administrativo autenticado, **When** faz `GET /external/evaluation-cycles`, **Then** recebe lista de ciclos de avaliação disponíveis no SAD.
2. **Given** o SAD está indisponível, **When** o administrador consulta ciclos, **Then** recebe HTTP 503 com mensagem descritiva.

---

### User Story 4 — Sistema recebe webhooks do SAD e mantém mapeamento sincronizado (Priority: P2)

O RH-Service recebe notificações de eventos do SAD (`evaluation.published`, `cycle.closed`, `objective.updated`) via webhooks HTTP assinados com HMAC-SHA256. Cada webhook é validado antes de ser processado; eventos inválidos são rejeitados com HTTP 401.

**Why this priority**: Garantir que os dados consultados pelo colaborador são actualizados atempadamente; sem webhooks o sistema depende exclusivamente de chamadas on-demand.

**Independent Test**: Pode ser testado enviando payloads POST assinados correctamente para os endpoints de webhook e verificando que são aceites (HTTP 200) e que payloads com assinatura inválida são rejeitados (HTTP 401).

**Acceptance Scenarios**:

1. **Given** um evento `evaluation.published` com assinatura HMAC-SHA256 válida, **When** enviado para `POST /webhooks/sad/evaluation-published`, **Then** o sistema aceita (HTTP 200) e processa o evento.
2. **Given** um evento com assinatura inválida ou ausente, **When** enviado para qualquer endpoint de webhook, **Then** o sistema rejeita com HTTP 401.
3. **Given** um evento `cycle.closed` válido, **When** recebido, **Then** o sistema regista o encerramento do ciclo.
4. **Given** um evento `objective.updated` válido, **When** recebido, **Then** o sistema regista a actualização do objectivo.

---

### Edge Cases

- O que acontece quando o colaborador tem mapeamento SAD mas o `external_employee_id` já não existe no SAD? → Retorna lista vazia (sem erro 404 propagado ao utilizador).
- O que acontece quando o `X-SAD-Signature` está presente mas malformado (não é hex válido)? → HTTP 401.
- O que acontece quando o body do webhook é vazio? → HTTP 400.
- O que acontece quando `SAD_BASE_URL` não está configurado? → Erro de arranque da aplicação (falha rápida); não deve chegar a produção sem esta variável.
- O que acontece quando o token JWT do colaborador não permite resolver o `employee_id`? → HTTP 403.
- O que acontece quando há múltiplos mapeamentos para o mesmo `employee_id`? → Usa o mais recente (ou o único activo).

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE expor `GET /me/external/evaluations` que devolve a lista de avaliações publicadas/homologadas do colaborador autenticado, lidas do SAD em tempo real.
- **FR-002**: O sistema DEVE expor `GET /me/external/evaluations/{externalId}` que devolve o detalhe de uma avaliação específica, validando que pertence ao colaborador autenticado.
- **FR-003**: O sistema DEVE expor `GET /me/external/objectives` que devolve os objectivos do colaborador no ciclo activo corrente no SAD.
- **FR-004**: O sistema DEVE expor `GET /external/evaluation-cycles` (acesso administrativo) que devolve a lista de ciclos de avaliação disponíveis no SAD.
- **FR-005**: O sistema DEVE expor `POST /webhooks/sad/evaluation-published`, `POST /webhooks/sad/cycle-closed` e `POST /webhooks/sad/objective-updated` para receber eventos do SAD.
- **FR-006**: Os endpoints `/me/external/*` DEVEM validar que o colaborador está activo (`is_active = true`); colaboradores inactivos recebem HTTP 403.
- **FR-007**: Os endpoints `/me/external/*` DEVEM resolver a identidade do colaborador via token JWT e traduzir para o `external_employee_id` correspondente na tabela `employee_external_mapping`.
- **FR-008**: Os webhooks DEVEM validar a assinatura HMAC-SHA256 no header `X-SAD-Signature` usando o segredo configurado; payloads com assinatura inválida ou ausente DEVEM ser rejeitados com HTTP 401.
- **FR-009**: Falhas de comunicação com o SAD (timeout, connection refused, 5xx) DEVEM resultar em HTTP 503 com mensagem descritiva; o stack trace NÃO deve ser exposto ao cliente.
- **FR-010**: Apenas avaliações com estado publicado/homologado DEVEM ser devolvidas; rascunhos e avaliações em curso DEVEM ser filtrados.
- **FR-011**: A URL base do SAD DEVE ser configurável via variável de ambiente `SAD_BASE_URL`; o token de serviço via `SAD_SERVICE_TOKEN`; o segredo de webhook via `SAD_WEBHOOK_SECRET`.

### Key Entities

- **Mapeamento de Identidades** (`employee_external_mapping`): associa um `employee_id` (UUID interno do RH) a um `external_employee_id` (identificador do colaborador no SAD). Permite tradução bidirecional de identidades.
- **Avaliação de Desempenho** (dado externo, read-only): identificada por `externalId`; tem estado (publicado, rascunho, etc.), data, ciclo associado, pontuação/resultado.
- **Objectivo** (dado externo, read-only): pertence a um ciclo activo; tem descrição, peso, estado de conclusão.
- **Ciclo de Avaliação** (dado externo, read-only): período de avaliação com data de início/fim e estado (activo, encerrado).

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Colaborador activo com mapeamento configurado obtém resposta do endpoint de avaliações em menos de 3 segundos em condições normais de rede.
- **SC-002**: Endpoints `/me/external/*` retornam HTTP 403 em 100% dos casos para colaboradores inactivos.
- **SC-003**: Webhooks com assinatura HMAC-SHA256 inválida são rejeitados com HTTP 401 em 100% dos casos.
- **SC-004**: Falhas de comunicação com o SAD resultam em HTTP 503 em 100% dos casos, sem stack trace na resposta.
- **SC-005**: Rascunhos e avaliações não publicadas são omitidos em 100% das respostas do endpoint de avaliações.
- **SC-006**: Os três endpoints de webhook (`evaluation-published`, `cycle-closed`, `objective-updated`) processam eventos válidos com HTTP 200.

---

## Assumptions

- A tabela `employee_external_mapping` já existe no modelo relacional v4 e apenas precisa de ser mapeada para o domínio; não é necessário criar endpoints CRUD para ela neste módulo.
- O SAD usa HTTPS com certificado válido; não é necessário tratamento de certificados auto-assinados.
- O módulo `sigdi/` já existe no projecto com outros endpoints (budget, compliance, strategy); este módulo é adicional dentro do mesmo Bounded Context, seguindo o mesmo padrão hexagonal com prefixo de bean `colabs`.
- A autenticação com o SAD é feita exclusivamente via token de serviço estático (`SAD_SERVICE_TOKEN`); não há fluxo OAuth2 entre o RH e o SAD.
- Os endpoints `/me/external/*` só devolvem dados — não criam nem modificam dados no SAD.
- A filtragem de avaliações publicadas/homologadas é feita no lado do RH-Service interpretando o campo de estado devolvido pelo SAD, não por filtragem no lado do SAD.
- O campo `external_employee_id` é um identificador opaco (string); o RH-Service não interpreta a sua estrutura interna.
- Não há cache de dados do SAD neste módulo (v1); cada pedido vai ao SAD em tempo real.
- O formato de dados do SAD (JSON) está documentado na Spec Técnica v4.0 e não precisa de negociação de conteúdo.
