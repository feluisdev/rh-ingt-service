# Quickstart: Meu Perfil — Área Reservada do Colaborador

**Feature**: 008-colaborador-me | **Date**: 2026-05-02

Cenários de teste manual para verificação end-to-end após implementação.

```bash
BASE="http://localhost:8091/api/v1/rh"
# Em dev, usar header X-Employee-Id para simular autenticação
EMP_ID="<uuid-de-funcionario-activo>"
HEADERS='-H "Content-Type: application/json" -H "X-Employee-Id: '"$EMP_ID"'"'
```

---

## Cenário 1 — Perfil do colaborador (US1-AC1)

```bash
curl -s -H "X-Employee-Id: $EMP_ID" "$BASE/me/profile"
```
**Esperado**: `200`, nome, NIF, unidade actual, cargo, carreira, data de admissão.

---

## Cenário 2 — Colaborador inactivo recebe 403 (US1-AC2)

```bash
INATIVO_ID="<uuid-de-funcionario-inactivo>"
curl -s -o /dev/null -w "%{http_code}" -H "X-Employee-Id: $INATIVO_ID" "$BASE/me/profile"
```
**Esperado**: `403`.

---

## Cenário 3 — JWT sem funcionário associado recebe 404 (US1-AC5)

```bash
UNKNOWN_EMP="00000000-0000-0000-0000-000000000000"
curl -s -o /dev/null -w "%{http_code}" -H "X-Employee-Id: $UNKNOWN_EMP" "$BASE/me/profile"
```
**Esperado**: `404` "Funcionário não encontrado para o utilizador autenticado".

---

## Cenário 4 — Listar pedidos de ausência (US2-AC1)

```bash
curl -s -H "X-Employee-Id: $EMP_ID" "$BASE/me/leave-requests"
```
**Esperado**: `200`, lista dos pedidos do próprio (nunca de terceiros).

---

## Cenário 5 — Filtro por estado PENDING (US2-AC1)

```bash
curl -s -H "X-Employee-Id: $EMP_ID" "$BASE/me/leave-requests?status=PENDING"
```
**Esperado**: `200`, apenas pedidos em estado PENDING.

---

## Cenário 6 — Submeter pedido de ausência (US2-AC3)

```bash
LEAVE_TYPE_ID="<uuid-de-tipo-ferias>"
curl -s -X POST -H "X-Employee-Id: $EMP_ID" -H "Content-Type: application/json" \
  -d "{\"leaveTypeId\":\"$LEAVE_TYPE_ID\",\"startDate\":\"2026-08-01\",\"endDate\":\"2026-08-10\"}" \
  "$BASE/me/leave-requests"
```
**Esperado**: `201`, `id` do pedido criado em estado PENDING.

---

## Cenário 7 — Cancelar pedido PENDING (US2-AC4)

```bash
PEDIDO_ID="<uuid-do-pedido-pending>"
curl -s -X PUT -H "X-Employee-Id: $EMP_ID" "$BASE/me/leave-requests/$PEDIDO_ID/cancel"
```
**Esperado**: `200`, "Pedido cancelado com sucesso".

---

## Cenário 8 — Saldos de ausência (US2-AC2)

```bash
curl -s -H "X-Employee-Id: $EMP_ID" "$BASE/me/leave-balances"
```
**Esperado**: `200`, saldos por tipo com `totalDays`, `usedDays`, `availableDays`.

---

## Cenário 9 — Listar licenças e mobilidades (US3-AC1)

```bash
curl -s -H "X-Employee-Id: $EMP_ID" "$BASE/me/leaves-mobilities"
```
**Esperado**: `200`, lista ordenada por data de início descendente.

---

## Cenário 10 — Listar documentos com filtro (US4-AC1)

```bash
curl -s -H "X-Employee-Id: $EMP_ID" "$BASE/me/documents"
```
**Esperado**: `200`, documentos do próprio.

---

## Cenário 11 — Download de documento próprio (US4-AC2)

```bash
DOC_ID="<uuid-de-documento-do-colaborador>"
curl -s -H "X-Employee-Id: $EMP_ID" "$BASE/me/documents/$DOC_ID/download"
```
**Esperado**: `200`, `downloadUrl` pré-assinado.

---

## Cenário 12 — Download de documento alheio retorna 404 (US4-AC3)

```bash
DOC_ALHEIO="<uuid-de-documento-de-outro-colaborador>"
curl -s -o /dev/null -w "%{http_code}" -H "X-Employee-Id: $EMP_ID" "$BASE/me/documents/$DOC_ALHEIO/download"
```
**Esperado**: `404` (isolamento de dados).
