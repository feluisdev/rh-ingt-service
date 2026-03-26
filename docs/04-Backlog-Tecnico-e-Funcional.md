# Documento de Backlog Técnico e Funcional - SIGDI

**Versão:** 1.1 (Revisada)\
**Status:** Planeamento de Sprint\
**Data:** 15/03/2026

***

## 1. Visão Geral do Backlog

Este documento consolida as Histórias de Usuário (User Stories) necessárias para cobrir os Requisitos Funcionais (RF) definidos no SRS. As histórias estão agrupadas por Módulo Funcional.

***

## MÓDULO 1: ESTRATÉGICO (Service-Strategy)

### US001 - Gestão de Identidade Institucional

*Rastreabilidade: RF001*

> **Como** Gestor Estratégico (Ana),\
> **eu quero** registar e versionar a Missão, Visão e Valores da instituição,\
> **para que** todos os colaboradores saibam o norte estratégico e eu possa manter um histórico de mudanças (Snapshots).

**Critérios de Aceite (Gherkin):**

* **Cenário 1: Versionamento de Identidade**

  * **Dado** que edito a Missão atual e clico em "Publicar Nova Versão"

  * **Quando** confirmo a operação

  * **Então** a versão anterior deve ser arquivada como "Histórico"

  * **E** a nova versão torna-se "Ativa" para todos os dashboards.

* **Cenário 2: Bloqueio de Histórico**

  * **Dado** que tento alterar uma versão "Histórica"

  * **Então** o sistema deve bloquear a edição (Read-only).

**Especificação Técnica:**

* **Endpoint:** `PUT /api/v1/strategy/identity/current`

* **Payload:**
  ```json
  {
    "mission": "Garantir a excelência...",
    "vision": "Ser referência em 2030...",
    "values": ["Inovação", "Transparência"],
    "versionComment": "Revisão 2026"
  }
  ```

* **Frontend:** Editor de Texto Rico (WYSIWYG) com controle de *diff* visual entre versões.

***

### US002 - Mapa Estratégico (BSC)

*Rastreabilidade: RF002*

> **Como** Gestor Estratégico,\
> **eu quero** desenhar visualmente o Mapa BSC conectando objetivos de diferentes perspetivas,\
> **para que** eu possa demonstrar as relações de causa-efeito da estratégia.

**Critérios de Aceite:**

* **Cenário 1: Criação de Vínculo**

  * **Dado** que arrasto um Objetivo da perspetiva "Pessoas" para "Processos"

  * **Quando** solto o elemento

  * **Então** o sistema deve criar uma seta de ligação entre eles.

* **Cenário 2: Visualização de Detalhes**

  * **Dado** que clico num objetivo no mapa

  * **Então** deve abrir um *side-panel* com os detalhes (KPIs vinculados).

**Especificação Técnica:**

* **Endpoint:** `POST /api/v1/strategy/maps/{id}/links`

* **Payload:** 
```json
{
  "sourceGoalId": "uuid-1",
  "targetGoalId": "uuid-2",
  "relationshipType": "CAUSE_EFFECT"
}
```

* **Frontend:** Biblioteca de grafos (ex: React Flow ou D3.js).

***

## MÓDULO 2: TÁTICO & OPERACIONAL (Service-Tactical)

### US003 - Criação de Atividade 5W2H (CRÍTICA)

*Rastreabilidade: RF004, RN01*

> **Como** Técnico Administrativo (Carlos),\
> **eu quero** criar uma nova atividade no PAA vinculada a um objetivo estratégico,\
> **para que** eu possa garantir a execução das metas e reservar dotação orçamental.

**Critérios de Aceite:**

* **Cenário 1: Sucesso na Criação**

  * **Dado** que preenchi o 5W2H e o valor orçado (100.000$00) está dentro do teto

  * **Quando** clico em "Salvar"

  * **Então** o sistema cria a atividade com status "Pendente".

* **Cenário 2: Bloqueio Financeiro**

  * **Dado** que o valor excede o saldo da rubrica

  * **Então** o sistema bloqueia e exibe "Saldo insuficiente".

**Especificação Técnica:**

* **Endpoint:** `POST /api/v1/tactical/activities`

* **Payload:**
  ```json
  {
    "strategicGoalId": "uuid-1234",
    "title": "Formação XPTO",
    "budget": { "amount": 100000, "economicClassifier": "02.02.01" }
  }
  ```

* **Erro 422:** `{"code": "BUDGET_EXCEEDED", "available": 5000}`

***

### US004 - Check-in de OKRs

*Rastreabilidade: RF003, RN02*

> **Como** Gestor de Equipa,\
> **eu quero** realizar o *check-in* mensal dos Resultados-Chave (Key Results),\
> **para que** o progresso do Objetivo Tático seja atualizado automaticamente.

**Critérios de Aceite:**

* **Cenário 1: Atualização de Progresso**

  * **Dado** que atualizo o KR "Treinar 10 pessoas" para "5 pessoas"

  * **Então** o progresso do KR deve ir para 50%

  * **E** o progresso do Objetivo Pai deve ser recalculado (Média Ponderada).

* **Cenário 2: Alerta de Risco**

  * **Dado** que o progresso é < 30% no último mês do trimestre

  * **Então** o sistema deve exibir um alerta vermelho (Risco Crítico).

**Especificação Técnica:**

* **Endpoint:** `POST /api/v1/tactical/okrs/{id}/checkin`

* **Payload:** 
```json
{
  "value": 5,
  "comment": "Metade da turma formada.",
  "evidenceUrl": "http://..."
}
```

***

## MÓDULO 3: INTEGRAÇÃO & ORÇAMENTO (Service-Budget-Adapter)

### US005 - Sincronização Bidirecional SIGOF

*Rastreabilidade: RF005, RF006, RN03*

> **Como** Gestor Financeiro,\
> **eu quero** que o sistema sincronize diariamente a execução financeira com o SIGOF,\
> **para que** eu veja no SIGDI o que foi realmente pago vs. o que foi planeado.

**Critérios de Aceite:**

* **Cenário 1: Sincronização Noturna**

  * **Dado** que o Job Noturno roda às 03:00 AM

  * **Quando** consulta a API do SIGOF via PDEX

  * **Então** deve atualizar as colunas "Cabimentado", "Liquidado" e "Pago" de cada atividade baseada no Classificador Económico.

* **Cenário 2: Tolerância a Falhas**

  * **Dado** que o SIGOF retorna erro 500

  * **Então** o sistema deve tentar novamente 3 vezes e, se falhar, alertar o Admin (não parar o sistema).

**Especificação Técnica:**

* **Job:** `Scheduled Task (Spring Batch)`

* **Integração:** Client REST com *Circuit Breaker*.

* **Payload SIGOF (Simulado):** 
```json
[
  {
    "classifier": "02.02.01",
    "committed": 50000,
    "paid": 20000
  }
]
```

***

### US006 - Cálculo Automático (Driver-Based)

*Rastreabilidade: RF007*

> **Como** Técnico de Planeamento,\
> **eu quero** que o sistema calcule o custo de deslocações baseado em parâmetros oficiais (drivers),\
> **para que** eu não precise consultar o BO (Boletim Oficial) para saber o valor da diária.

**Critérios de Aceite:**

* **Cenário 1: Cálculo de Diárias**

  * **Dado** que seleciono "Deslocação Nacional" para "Ilha do Sal" por "3 dias"

  * **Então** o sistema deve buscar o valor da diária na tabela de parâmetros (ex: 11.000$00) e preencher o total (33.000$00).

**Especificação Técnica:**

* **Endpoint:** `POST /api/v1/budget/calculator/simulate`

* **Payload:** 
```json
{
  "driverType": "PER_DIEM",
  "params": {
    "destination": "SAL",
    "days": 3,
    "level": "SENIOR"
  }
}
```

***

## MÓDULO 4: INTELIGÊNCIA & COMPLIANCE (Service-Intelligence)

### US007 - Simulação de Cenários (Sage IA)

*Rastreabilidade: RF008*

> **Como** Diretor Nacional,\
> **eu quero** simular um corte orçamental linear de 10%,\
> **para que** a IA me diga quais atividades se tornam inviáveis e sugira cortes.

**Critérios de Aceite:**

* **Cenário 1: Análise de Impacto**

  * **Dado** que aplico um cenário "Corte -10%"

  * **Então** o sistema deve marcar em vermelho atividades de baixa prioridade que perderiam financiamento

  * **E** gerar um relatório de impacto em PDF.

**Especificação Técnica:**

* **Endpoint:** `POST /api/v1/intelligence/scenarios`

* **Payload:** 
```json
{
  "type": "BUDGET_CUT",
  "percentage": 10.0,
  "scope": "GLOBAL"
}
```

* **Response:** Lista de IDs de atividades afetadas e sugestão de ação ("Adiar", "Cancelar").

***

### US008 - Geração do QUAR e Avaliação SIADAP

*Rastreabilidade: RF009, RF010, RN04*

> **Como** Gestor de RH,\
> **eu quero** gerar o relatório QUAR e a pontuação SIADAP automaticamente,\
> **para que** eu possa cumprir os prazos legais sem preenchimento manual de Excel.

**Critérios de Aceite:**

* **Cenário 1: Exportação Legal**

  * **Dado** que é fim de ciclo anual

  * **Quando** clico em "Gerar QUAR"

  * **Então** deve baixar um PDF oficial com as metas Previstas vs. Realizadas.

* **Cenário 2: Validação de Quotas**

  * **Dado** que calculo o SIADAP

  * **Então** o sistema deve aplicar a quota de 25% para menções "Excelente" e impedir que eu dê nota máxima a mais pessoas do que o permitido.

**Especificação Técnica:**

* **Endpoint:** `GET /api/v1/compliance/quar/export?year=2026&format=pdf`

* **Regra de Negócio (Backend):** Validação estrita da quota de mérito antes de fechar a avaliação.

***

## Definição de Pronto (DoD) Global

Para qualquer User Story acima ser considerada "Done":

1. [ ] Implementação completa no Backend e Frontend.
2. [ ] Testes Unitários (Jest/JUnit) com cobertura > 80%.
3. [ ] Testes de Integração (API) passando no pipeline CI/CD.
4. [ ] Documentação OpenAPI (Swagger) atualizada.
5. [ ] Aprovação do PO no ambiente de Staging.

