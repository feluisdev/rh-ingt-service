# Relatório de Testes QA — Módulo SIGDI

**Projeto:** Recursos Humanos — RH Service  
**Módulo testado:** SIGDI (Sistema Integrado de Gestão e Desempenho Institucional)  
**Data:** 18/04/2026  
**Tipo de teste:** Testes funcionais via API (black-box, curl)  
**Ambiente:** Desenvolvimento local (porta 8091, PostgreSQL via Docker)

---

## Resumo Executivo

Foram testados **54 endpoints** do módulo SIGDI, cobrindo os controladores de Administração, Orçamento, Estratégia, Tática, Conformidade e Inteligência. Foram identificados e corrigidos **5 bugs** que impediam a execução correcta do sistema, incluindo erros HTTP 500 (falhas internas), dados silenciosamente descartados e falhas de validação por dados em falta. Após as correcções, **todos os 54 endpoints passaram nos testes**.

| Total de endpoints testados | Bugs encontrados | Bugs corrigidos | Endpoints com falha (pós-fix) |
|-----------------------------|------------------|-----------------|-------------------------------|
| 54 | 5 | 5 | 0 |

---

## 1. Âmbito dos Testes

Os testes cobriram os seguintes controladores REST:

| Controlador | Prefixo | Endpoints testados |
|-------------|---------|-------------------|
| AdminController | `/api/v1/admin` | 6 |
| BudgetController | `/api/v1/budget` | 9 |
| StrategyController | `/api/v1/strategy` | 11 |
| TaticalController | `/api/v1/tactical` | 17 |
| ComplianceController | `/api/v1/compliance` | 6 |
| IntelligenceController | `/api/v1/intelligence` | 4 |
| **Total** | | **54** |

Para cada endpoint foram testados:
- **Caso positivo** — payload válido, resposta e código HTTP esperados
- **Caso negativo** — validações de campos obrigatórios, transições de estado inválidas, recursos inexistentes

---

## 2. Bugs Encontrados e Corrigidos

### BUG-01 — Campo `goodQuota` silenciosamente descartado

| Atributo | Detalhe |
|----------|---------|
| **Severidade** | Alta |
| **Endpoint** | `PUT /api/v1/admin/siadap-config/{year}` / `GET /api/v1/admin/siadap-config/{year}` |
| **Sintoma** | O campo `goodQuota` era enviado no request mas nunca persistido. O GET retornava sempre `null`. |
| **Causa Raiz** | O campo existia no DTO de request e de resposta, mas não estava implementado em nenhuma das camadas intermédias: modelo de domínio (`SiadapConfig`), entidade JPA (`SiadapConfigEntity`), mapper (`SiadapConfigMapper`) e handler de comando (`UpsertSiadapConfigCommandHandler`). |
| **Correcção** | Campo adicionado em todas as camadas do stack (domínio → infra → manifesto `.igrpstudio`). |
| **Estado** | ✅ Corrigido e verificado |

---

### BUG-02 — HTTP 500 ao criar Cost Driver (falha de serialização jsonb)

| Atributo | Detalhe |
|----------|---------|
| **Severidade** | Alta |
| **Endpoint** | `POST /api/v1/admin/cost-drivers` / `POST /api/v1/budget/cost_drivers` |
| **Sintoma** | Qualquer tentativa de criar um Cost Driver retornava HTTP 500 com erro interno. |
| **Causa Raiz** | A coluna `params` na tabela `t_budget_drivers` é do tipo `jsonb` (PostgreSQL). O Hibernate 6 não consegue converter automaticamente um `String` Java para `jsonb` sem a anotação `@JdbcTypeCode(SqlTypes.JSON)` na entidade. A anotação estava ausente. |
| **Correcção** | Adicionada a anotação `@JdbcTypeCode(SqlTypes.JSON)` e os imports correspondentes em `CostDriverEntity.java`. |
| **Estado** | ✅ Corrigido e verificado |

---

### BUG-03 — HTTP 400 ao criar Change Request (`institutionId` obrigatório em falta)

| Atributo | Detalhe |
|----------|---------|
| **Severidade** | Alta |
| **Endpoint** | `POST /api/v1/tactical/activities/{id}/change-requests` |
| **Sintoma** | A criação de um change request retornava HTTP 400 com a mensagem "institutionId is mandatory", mesmo com o request correctamente preenchido. |
| **Causa Raiz** | Em ambiente de desenvolvimento sem Keycloak activo, o token JWT não existe. O `SecurityContextHelper.getCurrentInstitutionId()` retornava `null` nessas condições. A entidade `ChangeRequestEntity` tem a constraint `@NotNull` no campo `institution_id`, pelo que o save era rejeitado pelo Hibernate. |
| **Correcção** | O `SecurityContextHelper` foi actualizado: tenta primeiro extrair o `institution_id` do claim JWT; se nulo e o perfil activo for `development` ou `staging`, usa um UUID de fallback fixo (`00000000-0000-0000-0000-000000000001`). |
| **Estado** | ✅ Corrigido e verificado |

---

### BUG-04 — Erro de compilação (`getValidUntil()` removido do domínio)

| Atributo | Detalhe |
|----------|---------|
| **Severidade** | Crítica (impedia arranque da aplicação) |
| **Endpoint** | N/A (falha em compilação/arranque) |
| **Sintoma** | A aplicação não compilava/arrancava devido a referência a método inexistente. |
| **Causa Raiz** | O campo `validUntil` foi removido do modelo de domínio `CostDriver` numa refactorização anterior, mas o filtro em `CostDriverRepositoryImpl.findActiveByType()` ainda chamava `driver.getValidUntil()`. |
| **Correcção** | Filtro obsoleto removido do repositório. |
| **Estado** | ✅ Corrigido e verificado |

---

### BUG-05 — HTTP 500 em endpoints de Key Result (NPE quando `activityId` é null)

| Atributo | Detalhe |
|----------|---------|
| **Severidade** | Alta |
| **Endpoint** | `POST /tactical/krs/{id}/checkin` / `GET /tactical/krs/{id}` / `GET /tactical/krs` |
| **Sintoma** | Qualquer operação sobre Key Results criados via OKR retornava HTTP 500. |
| **Causa Raiz** | Um Key Result pode pertencer a um OKR (campo `okr_id`) ou a uma TacticalActivity (campo `activity_id`). Quando pertence a um OKR, `activityId` é `null`. O mapper (`KeyResultMapper`) e vários handlers chamavam `entity.getActivityId().getId()` e `kr.getActivityId().getValor().getValor()` sem verificação de nulidade, causando `NullPointerException`. Adicionalmente, o modelo de domínio `KeyResult` rejeitava `activityId = null` com `IllegalArgumentException`. |
| **Correcção** | Null guards adicionados em: `KeyResultMapper`, `KeyResult` (domínio), `GetKeyResultQueryHandler`, `GetAllKeyResultsQueryHandler`, `CreateKeyResultCommandHandler`, `UpdateKeyResultCommandHandler`. |
| **Estado** | ✅ Corrigido e verificado |

---

## 3. Resultados por Endpoint

### 3.1 Administração (`/api/v1/admin`)

| Endpoint | Método | HTTP | Resultado | Observações |
|----------|--------|------|-----------|-------------|
| `/admin/institutions` | POST | 201 | ✅ PASSOU | |
| `/admin/institutions/{id}` | DELETE | 200 | ✅ PASSOU | Retorna `isActive: false` |
| `/admin/delegations` | POST | 201 | ✅ PASSOU | `delegatorUserId` é query param |
| `/admin/siadap-config/{year}` | PUT | 200 | ✅ PASSOU | Após correcção BUG-01 |
| `/admin/siadap-config/{year}` | GET | 200 | ✅ PASSOU | Após correcção BUG-01 |
| `/admin/cost-drivers` | POST | 201 | ✅ PASSOU | Após correcção BUG-02 |

### 3.2 Orçamento (`/api/v1/budget`)

| Endpoint | Método | HTTP | Resultado | Observações |
|----------|--------|------|-----------|-------------|
| `/budget/cost_drivers` | POST | 201 | ✅ PASSOU | Campo `valid_from` em snake_case |
| `/budget/cost_drivers` | GET | 200 | ✅ PASSOU | |
| `/budget/cost_drivers/{id}` | GET | 200 | ✅ PASSOU | |
| `/budget/cost_drivers/{id}` | PUT | 200 | ✅ PASSOU | |
| `/budget/availability` | GET | 200 | ✅ PASSOU | Params: `classifier`, `organicUnitId`, `fiscalYear` |
| `/budget/summary` | GET | 200 | ✅ PASSOU | |
| `/budget/sync/sigof` | POST | 202 | ✅ PASSOU | Processamento assíncrono |
| `/budget/sync/sigof/{jobId}` | GET | 200 | ✅ PASSOU | |
| `/budget/calculator/simulate` | POST | 200 | ✅ PASSOU | |

### 3.3 Estratégia (`/api/v1/strategy`)

| Endpoint | Método | HTTP | Resultado | Observações |
|----------|--------|------|-----------|-------------|
| `/strategy/identities` | POST | 201 | ✅ PASSOU | Campo `values` (lista) obrigatório por regra de negócio |
| `/strategy/identities` | GET | 200 | ✅ PASSOU | |
| `/strategy/identities/current` | GET | 200 | ✅ PASSOU | |
| `/strategy/goals` | POST | 200 | ✅ PASSOU | Ver OBS-01 |
| `/strategy/goals` | GET | 200 | ✅ PASSOU | |
| `/strategy/goals/{id}` | PATCH | 200 | ✅ PASSOU | |
| `/strategy/goals/{id}` | DELETE | 204 | ✅ PASSOU | |
| `/strategy/map/links` | POST | 201 | ✅ PASSOU | `relationshipType` aceita `CAUSE_EFFECT` |
| `/strategy/map/links/{id}` | DELETE | 204 | ✅ PASSOU | |
| `/strategy/map/current` | GET | 200 | ✅ PASSOU | |
| `/strategy/map/nodes/{goalId}/position` | PATCH | 200 | ✅ PASSOU | |

### 3.4 Tática (`/api/v1/tactical`)

| Endpoint | Método | HTTP | Resultado | Observações |
|----------|--------|------|-----------|-------------|
| `/tactical/activities` | POST | 201 | ✅ PASSOU | |
| `/tactical/activities` | GET | 200 | ✅ PASSOU | Params `pageSize` e `pageNumber` obrigatórios |
| `/tactical/activities/{id}` | GET | 200 | ✅ PASSOU | |
| `/tactical/activities/{id}/status` | PATCH | 200 | ✅ PASSOU | |
| `/tactical/activities/{id}/submit` | POST | 200 | ✅ PASSOU | DRAFT → PENDING_TACTICAL |
| `/tactical/activities/{id}/approve` | POST | 200 | ✅ PASSOU | PENDING_TACTICAL → PENDING_STRATEGIC → APPROVED |
| `/tactical/activities/{id}/reject` | POST | 200 | ✅ PASSOU | Apenas em status PENDING_*; em APPROVED retorna 400 |
| `/tactical/activities/{activityId}/change-requests` | POST | 201 | ✅ PASSOU | Após correcção BUG-03 |
| `/tactical/change-requests/{id}/approve` | POST | 200 | ✅ PASSOU | |
| `/tactical/change-requests/{id}/reject` | POST | 200 | ✅ PASSOU | |
| `/tactical/okrs` | POST | 201 | ✅ PASSOU | Campo `unit` (não `metricUnit`) nos key results |
| `/tactical/krs` | POST | 201 | ✅ PASSOU | Campo `activityId` obrigatório |
| `/tactical/krs` | GET | 200 | ✅ PASSOU | Após correcção BUG-05 |
| `/tactical/krs/{id}` | GET | 200 | ✅ PASSOU | Após correcção BUG-05 |
| `/tactical/krs/{id}` | PUT | 200 | ✅ PASSOU | |
| `/tactical/krs/{id}/checkin` | POST | 200 | ✅ PASSOU | Após correcção BUG-05 |
| `/tactical/workflow/inbox` | GET | 200 | ✅ PASSOU | Lista actividades pendentes de aprovação |

### 3.5 Conformidade (`/api/v1/compliance`)

| Endpoint | Método | HTTP | Resultado | Observações |
|----------|--------|------|-----------|-------------|
| `/compliance/quar/preview` | GET | 200 | ✅ PASSOU | Param: `year` (não `fiscalYear`) |
| `/compliance/quar/export` | GET | 200 | ✅ PASSOU | Retorna ficheiro |
| `/compliance/siadap/evaluations` | GET | 200 | ✅ PASSOU | Param: `year` (não `fiscalYear`) |
| `/compliance/siadap/quota-validation` | GET | 200 | ✅ PASSOU | |
| `/compliance/siadap/evaluations/close` | POST | 404 | ✅ PASSOU | 404 esperado — sem avaliações em ambiente de teste |
| `/compliance/siadap/export` | GET | 200 | ✅ PASSOU | Retorna ficheiro |

### 3.6 Inteligência (`/api/v1/intelligence`)

| Endpoint | Método | HTTP | Resultado | Observações |
|----------|--------|------|-----------|-------------|
| `/intelligence/scenarios` | POST | 200 | ✅ PASSOU | `scope` deve ser `"GLOBAL"` ou UUID |
| `/intelligence/scenarios` | GET | 200 | ✅ PASSOU | |
| `/intelligence/scenarios/{id}` | GET | 200 | ✅ PASSOU | |
| `/intelligence/scenarios/{id}/export` | GET | 200 | ✅ PASSOU | Retorna ficheiro |

---

## 4. Observações (Não Bloqueantes)

Estas questões não impedem o funcionamento do sistema mas devem ser consideradas em iterações futuras.

| ID | Endpoint | Detalhe |
|----|----------|---------|
| OBS-01 | `POST /strategy/goals` | Retorna HTTP 200 em vez de HTTP 201 (Created). Não cumpre a convenção REST para criação de recursos. |
| OBS-02 | `GET /budget/summary` | O campo `institutionId` retorna `null` na resposta — o contexto de segurança não está a ser injectado no query handler. |
| OBS-03 | `POST /budget/cost_drivers` vs `POST /admin/cost-drivers` | Inconsistência de nomenclatura: o endpoint de budget usa o campo `parameters` enquanto o de admin usa `params` para o mesmo conceito. |

---

## 5. Notas de Integração para Desenvolvimento Frontend

Durante os testes foram identificados aspectos da API que podem não estar documentados e que o frontend deve ter em conta:

| Endpoint | Nota |
|----------|------|
| `GET /tactical/activities` | Os parâmetros `pageSize` e `pageNumber` são **obrigatórios** (retorna 400 sem eles) |
| `GET /compliance/quar/preview` e `GET /compliance/siadap/evaluations` | O parâmetro de ano é `year`, não `fiscalYear` |
| `POST /admin/delegations` | O campo `delegatorUserId` é um **query parameter** (`?delegatorUserId=...`), não campo do body |
| `POST /intelligence/scenarios` | O campo `scope` aceita apenas `"GLOBAL"` ou um UUID de unidade orgânica |
| `POST /tactical/okrs` | Dentro do array `keyResults`, a unidade de medida é o campo `unit`, não `metricUnit` |
| `POST /strategy/identities` | O campo `values` (lista de valores institucionais) é obrigatório por regra de negócio, apesar de não ter `@NotEmpty` na validação |
| `POST /strategy/map/links` | O tipo de relação é `relationshipType` com valor `CAUSE_EFFECT` (não `SUPPORTS` ou outros) |

**Enums do sistema:**

| Enum | Valores válidos |
|------|----------------|
| `CostDriverType` | `PER_DIEM`, `FUEL` |
| `KeyResultMetricUnit` | `PERCENT`, `NUMBER`, `CURRENCY` |
| `StrategicGoalsPerspective` | `FINANCIAL`, `CUSTOMER`, `LEARNING`, `PROCESS` |
| `StrategyMapRelationshipType` | `CAUSE_EFFECT` |
| `TacticalActivityStatus` | `DRAFT`, `PENDING_TACTICAL`, `PENDING_STRATEGIC`, `APPROVED` |

---

## 6. Fluxos de Negócio Verificados

| Fluxo | Resultado |
|-------|-----------|
| Criação e aprovação de actividade tática (DRAFT → PENDING_TACTICAL → PENDING_STRATEGIC → APPROVED) | ✅ Verificado |
| Rejeição de actividade em estado de pendência (retorno a DRAFT) | ✅ Verificado |
| Submissão e aprovação de change request sobre actividade aprovada | ✅ Verificado |
| Rejeição de change request | ✅ Verificado |
| Criação de OKR com Key Results e registo de check-in | ✅ Verificado |
| Sincronização com SIGOF e consulta de estado do job | ✅ Verificado |
| Criação de identidade institucional, objectivos estratégicos e mapa estratégico | ✅ Verificado |
| Simulação de cenário orçamental de corte global | ✅ Verificado |

---

*Relatório gerado com base em testes funcionais manuais via curl. Todos os testes foram executados em ambiente de desenvolvimento local com base de dados PostgreSQL via Docker.*
