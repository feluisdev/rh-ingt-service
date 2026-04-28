# Documento de Backlog Técnico e Funcional — SIGDI

**Versão:** 2.1 | **Status:** Planeamento de Sprint | **Data:** 30/03/2026

***

## 1. Visão Geral

Este documento consolida todas as Histórias de Usuário (User Stories) necessárias para cobrir os Requisitos Funcionais (RF) definidos no SRS v2.0. As histórias novas (US009–US013) cobrem os gaps identificados na revisão: workflow de aprovação, change requests, multi-tenancy, simulação Sage e SIADAP.

***

## MÓDULO 1: ESTRATÉGICO (Service-Strategy)

### US001 — Gestão de Identidade Institucional

*Rastreabilidade: RF001*

> **Como** Gestor Estratégico,
> **eu quero** registar e versionar a Missão, Visão e Valores da instituição,
> **para que** todos os colaboradores conheçam o norte estratégico e eu mantenha um histórico imutável de versões.

**Critérios de Aceite:**

* **Cenário 1:** Dado que edito a Missão e clico "Publicar Nova Versão", quando confirmo, então a versão anterior fica arquivada como "Histórico" (read-only) e a nova torna-se "Ativa".

* **Cenário 2:** Dado que acedo a uma versão histórica, então todos os campos estão em modo read-only sem botão de edição.

* **Cenário 3:** Dado que publico sem preencher Missão (< 20 chars), então o sistema bloqueia com mensagem de erro inline.

**Especificação Técnica:**

* Endpoint: `POST /api/v1/strategy/identities`

* Payload: `{ mission, vision, values: string[], versionComment }`

* O campo `is_active` é gerido pelo backend — o anterior é desativado atomicamente na mesma transação.

***

### US002 — Mapa Estratégico (BSC)

*Rastreabilidade: RF002*

> **Como** Gestor Estratégico,
> **eu quero** desenhar visualmente o Mapa BSC conectando objetivos nas 4 perspetivas,
> **para que** demonstre as relações de causa-efeito da estratégia à equipa e à tutela.

**Critérios de Aceite:**

* **Cenário 1:** Dado que arrasto o handle de um objetivo "Perspetiva Pessoas" para um objetivo "Perspetiva Processos", então é criada uma seta de ligação entre eles.

* **Cenário 2:** Dado que clico num objetivo no mapa, então abre um side-panel com KPIs vinculados, progresso e ações rápidas.

* **Cenário 3:** Dado que o mapa tem 100 nós, então o canvas renderiza sem lag perceptível (< 100ms por interação).

* **Cenário 4:** Dado que clico "Exportar Mapa", então recebo um PNG de alta resolução (min. 2000px de largura).

**Especificação Técnica:**

* Endpoints: `GET /v1/strategy/map/current`, `POST /v1/strategy/map/links`, `DELETE /v1/strategy/map/links/{id}`

* Frontend: React Flow com `memo` nos nós customizados. Virtualização para > 50 nós.

***

### US003 — Objetivos Estratégicos

*Rastreabilidade: RF002, RN02*

> **Como** Gestor Estratégico,
> **eu quero** criar e gerir objetivos estratégicos com KPIs e pesos,
> **para que** o progresso seja calculado automaticamente de forma ponderada.

**Critérios de Aceite:**

* **Cenário 1:** Dado que crio um objetivo com peso 2.0 e outro com peso 1.0 como filhos do mesmo pai, quando atualizo o progresso do primeiro para 100% e do segundo para 0%, então o pai deve mostrar 66.7% (média ponderada).

* **Cenário 2:** Dado que cancelo um objetivo filho, então ele é excluído do cálculo de progresso do pai.

**Especificação Técnica:**

* Endpoint: `POST /v1/strategy/goals`

* Payload: `{ title, perspective, description, weight: decimal, parentGoalId?: uuid }`

***

## MÓDULO 2: TÁTICO & OPERACIONAL (Service-Tactical)

### US004 — Criação de Atividade 5W2H (CRÍTICA)

*Rastreabilidade: RF004, RN01, RN06*

> **Como** Técnico Administrativo (Carlos),
> **eu quero** criar uma nova atividade no PAA vinculada a um objetivo estratégico,
> **para que** possa garantir a execução das metas e reservar dotação orçamental.

**Critérios de Aceite:**

* **Cenário 1:** Dado que preencho todos os campos obrigatórios e o orçamento está dentro do teto, quando clico "Submeter", então a atividade é criada com status `PENDING_TACTICAL` e Ana recebe notificação.

* **Cenário 2:** Dado que o orçamento excede o saldo da rubrica, então o botão "Submeter" está desabilitado e é exibido "Saldo insuficiente: disponível 50.000$00, solicitado 65.000$00".

* **Cenário 3:** Dado que a data de início é anterior ao início do ciclo PAA, então o campo mostra erro inline "Data fora do ciclo PAA 2026 (01/01 – 31/12)".

* **Cenário 4:** Dado que salvo como rascunho com orçamento a exceder, então a atividade é criada com status `DRAFT` e é exibido um banner de aviso no topo da página de detalhe.

**Especificação Técnica:**

* Endpoint: `POST /api/v1/tactical/activities`

* Payload:

```json
{
  "strategicGoalId": "uuid",
  "organicUnitId": "uuid",
  "title": "string",
  "justification_why": "string",
  "responsible_who": "uuid",
  "location_where": "string",
  "methodology_how": "string",
  "start_date": "date",
  "end_date": "date",
  "budget": {
    "amount": "decimal",
    "economicClassifier": "string"
  }
}
```

* Erro 422: `{ "code": "BUDGET_EXCEEDED", "available": 50000, "requested": 65000 }`

* Erro 422: `{ "code": "DATE_OUT_OF_CYCLE", "cycleStart": "2026-01-01", "cycleEnd": "2026-12-31" }`

***

### US005 — Workflow de Aprovação do PAA (CRÍTICA — NOVA)

*Rastreabilidade: RF011, RN10, RN11, RN12*

> **Como** Gestor Tático (Ana),
> **eu quero** receber, rever e aprovar/rejeitar atividades submetidas pelos técnicos,
> **para que** apenas atividades validadas passem para a execução orçamental.

**Critérios de Aceite:**

* **Cenário 1:** Dado que uma atividade é submetida por Carlos, então Ana recebe notificação (in-app + email) com link direto para a atividade.

* **Cenário 2:** Dado que Ana aprova, então o estado muda para `PENDING_STRATEGIC` e o Gestor Estratégico recebe notificação.

* **Cenário 3:** Dado que Ana rejeita sem comentário, então o botão "Confirmar Rejeição" está desabilitado até um comentário ser preenchido.

* **Cenário 4:** Dado que a atividade está pendente há 3 dias úteis sem resposta, então Ana recebe um lembrete por email. Se não responder em mais 2 dias úteis, o item escala para o Gestor Estratégico com notificação a Ana.

* **Cenário 5:** Dado que Ana tem uma delegação ativa para Rui (de 01/04 a 10/04), quando Rui aprova uma atividade durante este período, então o log de auditoria regista `approved_by: Rui (delegated from Ana)`.

**Especificação Técnica:**

* Endpoint aprovação: `POST /api/v1/tactical/activities/{id}/approve`

* Endpoint rejeição: `POST /api/v1/tactical/activities/{id}/reject`

* Payload rejeição: `{ "comment": "string (min 10 chars)" }`

* Job de escalada: tarefa agendada (Spring Scheduler) que corre a cada hora e verifica `pending_since > SLA_HOURS`.

* Endpoint delegação: `POST /api/v1/admin/delegations`

* Payload delegação: `{ "delegateUserId": "uuid", "startDate": "date", "endDate": "date", "scope": "ALL_APPROVALS" | "TACTICAL" | "STRATEGIC" }`

***

### US006 — Check-in de OKRs

*Rastreabilidade: RF003, RN02*

> **Como** Gestor de Equipa,
> **eu quero** realizar o check-in mensal dos Resultados-Chave,
> **para que** o progresso do Objetivo Tático seja atualizado automaticamente.

**Critérios de Aceite:**

* **Cenário 1:** Dado que o KR "Treinar 10 pessoas" tem valor atual 0 e eu adiciono 5, então o KR vai para 50% e o Objetivo Pai recalcula (média ponderada).

* **Cenário 2:** Dado que o progresso do KR é < 30% com < 30 dias para o fim do trimestre, então o sistema exibe badge vermelho "Risco Crítico" e envia notificação ao gestor.

* **Cenário 3:** Dado que faço upload de um ficheiro como evidência, então o ficheiro é guardado no MinIO/S3 e o URL público fica associado ao check-in.

**Especificação Técnica:**

* Endpoint: `POST /api/v1/tactical/krs/{id}/checkin`

* Payload: `{ "valueAdded": number, "comment": "string", "evidenceUrl"?: "string" }`

***

### US007 — Change Request de Atividade Aprovada (NOVA)

*Rastreabilidade: RF014, RN05*

> **Como** Técnico (Carlos),
> **eu quero** solicitar alterações a atividades já aprovadas,
> **para que** mudanças legítimas sejam registadas com rastreabilidade completa.

**Critérios de Aceite:**

* **Cenário 1:** Dado que Carlos tenta editar diretamente um campo de atividade `APPROVED`, então vê um modal explicativo e a opção "Criar Pedido de Alteração".

* **Cenário 2:** Dado que Carlos submete um CR sem justificativa (< 50 chars), então o botão "Submeter CR" está desabilitado.

* **Cenário 3:** Dado que o gestor aprova o CR, então o campo é atualizado e o log de auditoria regista `old_value`, `new_value` e `change_request_id`.

* **Cenário 4:** Dado que o gestor rejeita o CR, então a atividade permanece inalterada e Carlos recebe notificação com o motivo.

**Especificação Técnica:**

* Endpoint: `POST /api/v1/tactical/activities/{id}/change-requests`

* Payload: `{ "fieldName": "string", "currentValue": "any", "proposedValue": "any", "justification": "string (min 50)" }`

* Endpoint aprovação CR: `POST /api/v1/tactical/change-requests/{id}/approve`

***

## MÓDULO 3: INTEGRAÇÃO & ORÇAMENTO (Service-Budget-Adapter)

### US008 — Sincronização Bidirecional SIGOF

*Rastreabilidade: RF005, RF006, RN03, RI01*

> **Como** Gestor Financeiro,
> **eu quero** que o sistema sincronize diariamente a execução financeira com o SIGOF,
> **para que** veja no SIGDI o que foi efetivamente pago vs. o que foi planeado.

**Critérios de Aceite:**

* **Cenário 1:** Dado que o job corre às 03:00 AM, quando consulta o SIGOF via PDEX, então atualiza `committed`, `liquidated` e `paid` de cada atividade por `economic_classifier` e `organic_unit_code`.

* **Cenário 2:** Dado que o SIGOF retorna erro HTTP 5xx, então o sistema tenta novamente com backoff exponencial (3 tentativas: 5min, 15min, 30min). Se falhar, envia alerta ao Admin e regista falha no log. O sistema não para.

* **Cenário 3:** Dado que o `liquidated` de uma atividade atinge 70% do `committed`, então o responsável recebe notificação de aviso. Ao atingir 90%, recebe alerta urgente.

* **Cenário 4:** Dado que o Circuit Breaker está aberto (SIGOF inacessível), então o sistema serve dados em cache com banner "Dados orçamentais de \[data/hora]. SIGOF temporariamente indisponível."

**Especificação Técnica:**

* Job: Spring Batch com step de retry configurado.

* Circuit Breaker: Resilience4j com `failureRateThreshold=50`, `waitDurationInOpenState=5min`.

* Consumer-Driven Contract Test: arquivo Pact versionado e executado no CI/CD.

* Payload SIGOF (contrato proposto — sujeito a validação com NOSi):

```json
{
  "institution_code": "string",
  "fiscal_year": 2026,
  "economic_classifier": "02.02.01",
  "organic_unit_code": "string",
  "budget_allocated": "decimal",
  "committed": "decimal",
  "liquidated": "decimal",
  "paid": "decimal",
  "reference_date": "2026-03-30"
}
```

***

### US009 — Calculadora Driver-Based

*Rastreabilidade: RF007, RF018*

> **Como** Técnico de Planeamento,
> **eu quero** que o sistema calcule o custo de deslocações automaticamente,
> **para que** não precise consultar o Boletim Oficial manualmente.

**Critérios de Aceite:**

* **Cenário 1:** Dado que seleciono "Deslocação Nacional → Ilha do Sal → 3 dias → Técnico Sénior", então o sistema busca o driver vigente e preenche o total automaticamente (ex: 11.000$00 × 3 = 33.000$00).

* **Cenário 2:** Dado que o driver foi atualizado após a criação da atividade, então a atividade mostra um badge "Driver atualizado — reveja o orçamento" mas não recalcula automaticamente.

**Especificação Técnica:**

* Endpoint: `POST /api/v1/budget/calculator/simulate`

* Payload: `{ "driverType": "PER_DIEM", "params": { "destination": "SAL", "days": 3, "level": "SENIOR" } }`

* Response: `{ "unitCost": 11000, "totalCost": 33000, "driverVersion": "2026-01-01", "currency": "CVE" }`

***

## MÓDULO 4: INTELIGÊNCIA & COMPLIANCE (Service-Intelligence + Service-Compliance)

### US010 — Simulação de Cenários Sage (NOVA — FASE 2)

*Rastreabilidade: RF008*

> **Como** Diretor Nacional,
> **eu quero** simular cenários de corte orçamental com priorização automática,
> **para que** tome decisões informadas antes de implementar mudanças reais.

**Critérios de Aceite:**

* **Cenário 1:** Dado que aplico "Corte linear 10% — escopo global", então o sistema identifica atividades de baixa prioridade (critério: menor peso estratégico + menor % execução), marca em vermelho as inviáveis e gera um relatório de impacto.

* **Cenário 2:** Dado que guardo o cenário, então fica disponível para consulta futura sem afetar os dados reais.

* **Cenário 3:** Dado que exporto o relatório, então recebo um PDF com: resumo do cenário, lista de atividades afetadas, sugestões (adiar/cancelar/reduzir) e valor libertado.

**Especificação Técnica:**

* Endpoint simulação: `POST /api/v1/intelligence/scenarios`

* Endpoint guardar: `POST /api/v1/intelligence/scenarios/{id}/save`

* Endpoint exportar: `GET /api/v1/intelligence/scenarios/{id}/export?format=pdf`

* Request:

```json
{
  "type": "BUDGET_CUT",
  "percentage": 10.0,
  "scope": "GLOBAL",
  "priorityCriteria": "STRATEGIC_WEIGHT"
}
```

* Response:

```json
{
  "scenarioId": "uuid",
  "totalImpact": 250000,
  "impactedActivities": [
    {
      "activityId": "uuid",
      "title": "string",
      "currentBudget": 50000,
      "simulatedBudget": 45000,
      "viability": "VIABLE" | "AT_RISK" | "INFEASIBLE",
      "suggestedAction": "NONE" | "DEFER" | "REDUCE_SCOPE" | "CANCEL",
      "actionReason": "string"
    }
  ]
}
```

* **Importante:** O algoritmo é determinístico (baseado em regras de priorização, não ML). O modelo ML é previsto apenas na Fase 3 (RF015).

***

### US011 — Geração do QUAR e Avaliação SIADAP

*Rastreabilidade: RF009, RF010, RN04, RN09*

> **Como** Gestor de RH,
> **eu quero** gerar o relatório QUAR e as pontuações SIADAP automaticamente,
> **para que** cumpra os prazos legais sem preenchimento manual.

**Critérios de Aceite:**

* **Cenário 1:** Dado que todos os OKRs do ciclo têm check-in final, quando clico "Gerar QUAR", então recebo o PDF oficial em ≤ 30 segundos.

* **Cenário 2:** Dado que existem OKRs sem check-in, então o sistema avisa com lista dos OKRs pendentes e permite gerar "mesmo assim" (com campos incompletos sinalizados no PDF).

* **Cenário 3:** Dado que tento fechar as avaliações SIADAP com a Unidade X a exceder a quota de "Excelente", então o botão "Fechar Avaliações" está desabilitado com tooltip explicativo.

* **Cenário 4:** Dado que a Unidade tem 3 colaboradores (< 4), então o sistema aplica a regra de arredondamento: 1 colaborador pode receber "Excelente" mesmo representando 33%.

**Especificação Técnica:**

* QUAR: `GET /api/v1/compliance/quar/export?year=2026&format=pdf&unitId=xxx`

* Validação quotas: `GET /api/v1/compliance/siadap/quota-validation?year=2026`

* Fechar avaliação: `POST /api/v1/compliance/siadap/evaluations/close?year=2026&unitId=xxx`

* Response erro de quota:

```json
{
  "status": "error",
  "violations": [
    {
      "unitId": "uuid",
      "unitName": "Unidade X",
      "excellentAllowed": 3,
      "excellentAssigned": 4,
      "message": "Excede quota de Excelente em 1 colaborador"
    }
  ]
}
```

***

## MÓDULO 5: ADMINISTRAÇÃO (Service-Admin)

### US012 — Multi-Tenancy e Isolamento de Dados (CRÍTICA — NOVA)

*Rastreabilidade: RF016, RNF003*

> **Como** Admin NOSi,
> **eu quero** criar e gerir instituições isoladas na plataforma,
> **para que** os dados de cada instituição sejam completamente inacessíveis por outras instituições.

**Critérios de Aceite:**

* **Cenário 1:** Dado que crio a "Instituição A" e a "Instituição B" e faço login como utilizador da A, então não consigo aceder, listar ou pesquisar dados da B por nenhuma API.

* **Cenário 2:** Dado que desativo a "Instituição A", então todos os seus utilizadores perdem acesso imediatamente (tokens invalidados). Os dados são mantidos por 5 anos (retenção legal) mas inacessíveis via interface.

* **Cenário 3:** Dado que executo o teste de penetração cross-tenant (automatizado no CI/CD), então 100% dos endpoints retornam 403/404 quando tentam aceder a dados de outra instituição.

**Especificação Técnica:**

* Todas as tabelas de dados contêm `institution_id UUID NOT NULL`.

* PostgreSQL Row-Level Security ativado em todas as tabelas de dados:

```sql
ALTER TABLE t_tactical_activities ENABLE ROW LEVEL SECURITY;
CREATE POLICY institution_isolation ON t_tactical_activities
  USING (institution_id = current_setting('app.current_institution_id')::uuid);
```

* A aplicação define `SET app.current_institution_id = '{jwt.institution_id}'` em cada conexão.

* Endpoint criar instituição: `POST /api/v1/admin/institutions`

* Endpoint desativar: `DELETE /api/v1/admin/institutions/{id}`

***

### US013 — Configuração de Parâmetros SIADAP (NOVA)

*Rastreabilidade: RF017*

> **Como** Admin da Instituição,
> **eu quero** configurar os parâmetros SIADAP por ano fiscal,
> **para que** o sistema aplique as regras corretas para cada ciclo de avaliação.

**Critérios de Aceite:**

* **Cenário 1:** Dado que configuro os parâmetros para 2027 enquanto o ciclo 2026 está aberto, então as mudanças não afetam o ciclo 2026.

* **Cenário 2:** Dado que não configuro parâmetros para o ano X, então o sistema usa os parâmetros do ano X-1 como fallback e exibe aviso "Usando parâmetros de 2026. Configure 2027 em Administração → Parâmetros SIADAP."

**Especificação Técnica:**

* Endpoint: `PUT /api/v1/admin/siadap-config/{year}`

* Payload:

```json
{
  "year": 2027,
  "goodScore": 1.5,
  "excellentScore": 3.0,
  "excellentQuota": 0.25,
  "minimumCollaboratorsForQuota": 4,
  "resultsWeight": 0.60,
  "competenciesWeight": 0.40
}
```

***

## Definição de Pronto (DoD) Global

Uma User Story é "Done" quando:

1. Implementação completa em Backend e Frontend.
2. Testes unitários com cobertura > 80% (relatório gerado no CI/CD).
3. Testes de integração de API passando (contrato OpenAPI validado).
4. Multi-tenancy validado: dado de instituição A não acessível por token de instituição B.
5. Documentação OpenAPI (Swagger) atualizada e publicada.
6. Logs de auditoria gerados para todas as operações de escrita (verificado em teste automatizado).
7. Aprovação do PO em ambiente de Staging com dados de teste realistas.
8. Sem regressões de acessibilidade (WCAG 2.1 AA — validado com axe-core no CI/CD).

