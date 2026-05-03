# Quickstart: Módulo de Ausências

**Pré-requisitos**: Aplicação a correr em `http://localhost:8091`. Funcionário existente com ID conhecido. Carreira, Categoria, Escalão e Cargo existentes (para o enquadramento do funcionário).

---

## C1 — Configuração de Catálogos (US1)

### C1-S1: Criar tipo de ausência FÉRIAS

```http
POST /api/v1/rh/parametrizacoes/tipos-ausencia
{
  "nome": "Férias Anuais",
  "codigo": "FERIAS",
  "deductsBalance": true,
  "requiresApproval": true,
  "maxDaysPerYear": 22
}
```
Esperado: `201 Created` com `id` UUID.

### C1-S2: Criar tipo sem limite de dias (DOENÇA)

```http
POST /api/v1/rh/parametrizacoes/tipos-ausencia
{
  "nome": "Baixa por Doença",
  "codigo": "DOENCA",
  "deductsBalance": false,
  "requiresApproval": false,
  "maxDaysPerYear": null
}
```
Esperado: `201 Created`.

### C1-S3: Criar subtipo de mobilidade

```http
POST /api/v1/rh/parametrizacoes/subtipos-licenca-mobilidade
{
  "nome": "Mobilidade Voluntária",
  "codigo": "MOB_VOLUNTARIA",
  "recordType": "MOBILIDADE",
  "affectsPay": false,
  "countsForSeniority": true,
  "canSelfSubmit": false
}
```
Esperado: `201 Created`.

### C1-S4: Verificar feriados semeados

```http
GET /api/v1/rh/parametrizacoes/feriados?ano=2026&isNational=true
```
Esperado: `200 OK` com `totalElements=11`.

### C1-S5: Tentar criar feriado nacional duplicado

```http
POST /api/v1/rh/parametrizacoes/feriados
{
  "nome": "Ano Novo Duplicado",
  "data": "2026-01-01",
  "isNational": true
}
```
Esperado: `409 Conflict`.

---

## C2 — Saldos de Ausência (US3)

### C2-S1: Criar saldo de férias para funcionário

```http
POST /api/v1/rh/funcionarios/{funcionarioId}/saldos-ausencia
{
  "tipoAusenciaId": "{tipoFerias.id}",
  "ano": 2026,
  "diasDireito": 22
}
```
Esperado: `201 Created`.

### C2-S2: Tentar criar saldo duplicado

```http
POST /api/v1/rh/funcionarios/{funcionarioId}/saldos-ausencia
{
  "tipoAusenciaId": "{tipoFerias.id}",
  "ano": 2026,
  "diasDireito": 22
}
```
Esperado: `409 Conflict`.

### C2-S3: Consultar saldos

```http
GET /api/v1/rh/funcionarios/{funcionarioId}/saldos-ausencia?ano=2026
```
Esperado: `200 OK` com `diasDisponiveis=22`.

---

## C3 — Pedidos de Ausência (US2)

### C3-S1: Submeter pedido de férias (5 dias úteis)

```http
POST /api/v1/rh/funcionarios/{funcionarioId}/pedidos-ausencia
{
  "tipoAusenciaId": "{tipoFerias.id}",
  "dataInicio": "2026-06-01",
  "dataFim": "2026-06-05",
  "motivo": "Férias de Verão"
}
```
Esperado: `201 Created` com `numeroDias=5`, `estado=PENDENTE`.

### C3-S2: Tentar pedido com sobreposição

```http
POST /api/v1/rh/funcionarios/{funcionarioId}/pedidos-ausencia
{
  "tipoAusenciaId": "{tipoFerias.id}",
  "dataInicio": "2026-06-03",
  "dataFim": "2026-06-07"
}
```
Esperado: `409 Conflict` (sobreposição com pedido PENDENTE).

### C3-S3: Tentar pedido com datas inválidas

```http
POST /api/v1/rh/funcionarios/{funcionarioId}/pedidos-ausencia
{
  "tipoAusenciaId": "{tipoFerias.id}",
  "dataInicio": "2026-06-06",
  "dataFim": "2026-06-01"
}
```
Esperado: `422 Unprocessable Entity`.

### C3-S4: Aprovar pedido

```http
PATCH /api/v1/rh/funcionarios/{funcionarioId}/pedidos-ausencia/{pedidoId}/aprovar
{
  "aprovadoPorId": "{gestorId}",
  "observacoesDecisao": "Aprovado conforme plano de férias"
}
```
Esperado: `200 OK`. Verificar saldo: `diasPendentes=5`, `diasDisponiveis=17`.

### C3-S5: Tentar aprovar sem saldo (DOENÇA sem saldo criado)

```http
POST /api/v1/rh/funcionarios/{funcionarioId}/pedidos-ausencia
{
  "tipoAusenciaId": "{tipoFerias.id}",
  "dataInicio": "2026-07-01",
  "dataFim": "2026-07-25"
}
```
Depois:
```http
PATCH /api/v1/rh/funcionarios/{funcionarioId}/pedidos-ausencia/{pedido2Id}/aprovar
{
  "aprovadoPorId": "{gestorId}"
}
```
Esperado: `422 Unprocessable Entity` (saldo insuficiente — só 17 dias disponíveis para 18 dias úteis de pedido).

### C3-S6: Cancelar pedido aprovado

```http
PATCH /api/v1/rh/funcionarios/{funcionarioId}/pedidos-ausencia/{pedidoId}/cancelar
```
Esperado: `200 OK`. Verificar saldo: `diasPendentes=0`, `diasDisponiveis=22`.

### C3-S7: Tentar cancelar pedido já cancelado

```http
PATCH /api/v1/rh/funcionarios/{funcionarioId}/pedidos-ausencia/{pedidoId}/cancelar
```
Esperado: `409 Conflict`.

---

## C4 — Licenças e Mobilidade (US4)

### C4-S1: Registar mobilidade em curso

```http
POST /api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade
{
  "subtipoId": "{subtipoMobilidade.id}",
  "dataInicio": "2026-02-01",
  "entidadeDestino": "Ministério das Finanças"
}
```
Esperado: `201 Created` com `dataFim=null`.

### C4-S2: Encerrar mobilidade

```http
PUT /api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade/{licencaId}
{
  "subtipoId": "{subtipoMobilidade.id}",
  "dataInicio": "2026-02-01",
  "dataFim": "2026-12-31",
  "entidadeDestino": "Ministério das Finanças"
}
```
Esperado: `200 OK`.

### C4-S3: Listar licenças activas

```http
GET /api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade?active=true
```
Esperado: `200 OK` com `dataFim="2026-12-31"`.

---

## C5 — Cálculo de dias úteis com feriado

### C5-S1: Pedido que abrange o Dia da Independência (2026-07-05, domingo — não conta; usar 2026-07-06 segunda)

```http
POST /api/v1/rh/funcionarios/{funcionarioId}/pedidos-ausencia
{
  "tipoAusenciaId": "{tipoDoenca.id}",
  "dataInicio": "2026-07-01",
  "dataFim": "2026-07-08"
}
```
Esperado: `201 Created` com `numeroDias=6` (01-Qua, 02-Qui, 03-Sex, [04-Sab, 05-Dom], 06-Seg, 07-Ter, 08-Qua = 6 dias úteis — 07-05 é domingo, não afecta; o feriado 07-05 cai num domingo em 2026 por isso não reduz).

*Nota de teste*: Usar `2026-01-12 a 2026-01-14` — o dia 13 (Dia da Liberdade, terça-feira) deve ser excluído → `numeroDias=2` (12-Seg e 14-Qua).
