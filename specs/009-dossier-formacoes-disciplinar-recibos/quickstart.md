# Quickstart: Dossier — Formações, Processos Disciplinares e Recibos

**Feature**: 009-dossier-formacoes-disciplinar-recibos | **Date**: 2026-05-02

```bash
BASE="http://localhost:8091/api/v1/rh"
EMP_ID="<uuid-de-funcionario-activo>"
H='-H "Content-Type: application/json"'
```

---

## Formações Profissionais

### C1 — Criar formação

```bash
curl -s -X POST -H "Content-Type: application/json" \
  -d '{"name":"PRINCE2 Foundation","institution":"INA","typeOptionKey":"PRESENCIAL","startDate":"2026-03-01","endDate":"2026-03-05","durationHours":40}' \
  "$BASE/funcionarios/$EMP_ID/formacoes"
```
**Esperado**: `201`, `{ "id": "<uuid-formacao>" }`

---

### C2 — Listar formações

```bash
curl -s "$BASE/funcionarios/$EMP_ID/formacoes"
```
**Esperado**: `200`, lista com a formação criada.

---

### C3 — Filtrar formações por ano

```bash
curl -s "$BASE/funcionarios/$EMP_ID/formacoes?year=2026"
```
**Esperado**: `200`, apenas formações de 2026.

---

### C4 — Actualizar formação

```bash
FORMACAO_ID="<uuid-formacao>"
curl -s -X PUT -H "Content-Type: application/json" \
  -d '{"durationHours":35}' \
  "$BASE/funcionarios/$EMP_ID/formacoes/$FORMACAO_ID"
```
**Esperado**: `200`, mensagem de sucesso.

---

### C5 — Eliminar formação

```bash
curl -s -X DELETE "$BASE/funcionarios/$EMP_ID/formacoes/$FORMACAO_ID"
```
**Esperado**: `200`, mensagem de sucesso.

---

## Processos Disciplinares

### C6 — Criar processo disciplinar

```bash
curl -s -X POST -H "Content-Type: application/json" \
  -d '{"processNumber":"PD-2026-001","startDate":"2026-01-10","notes":"Processo em instrução"}' \
  "$BASE/funcionarios/$EMP_ID/processos-disciplinares"
```
**Esperado**: `201`, `{ "id": "<uuid-processo>" }`

---

### C7 — Listar processos disciplinares

```bash
curl -s "$BASE/funcionarios/$EMP_ID/processos-disciplinares"
```
**Esperado**: `200`, lista com o processo criado.

---

### C8 — Actualizar processo com encerramento

```bash
PROCESSO_ID="<uuid-processo>"
curl -s -X PUT -H "Content-Type: application/json" \
  -d '{"endDate":"2026-04-30","penalty":"Repreensão escrita"}' \
  "$BASE/funcionarios/$EMP_ID/processos-disciplinares/$PROCESSO_ID"
```
**Esperado**: `200`, mensagem de sucesso.

---

## Recibos de Vencimento

### C9 — Criar recibo

```bash
DOC_ID="<uuid-documento-pdf>"
curl -s -X POST -H "Content-Type: application/json" \
  -d "{\"periodMonth\":4,\"periodYear\":2026,\"issueDate\":\"2026-04-30\",\"grossSalary\":85000.00,\"netSalary\":72000.00,\"documentId\":\"$DOC_ID\"}" \
  "$BASE/funcionarios/$EMP_ID/recibos"
```
**Esperado**: `201`, `{ "id": "<uuid-recibo>" }`

---

### C10 — Criar recibo duplicado → 409

```bash
curl -s -o /dev/null -w "%{http_code}" -X POST -H "Content-Type: application/json" \
  -d "{\"periodMonth\":4,\"periodYear\":2026,\"issueDate\":\"2026-04-30\",\"grossSalary\":85000.00,\"netSalary\":72000.00,\"documentId\":\"$DOC_ID\"}" \
  "$BASE/funcionarios/$EMP_ID/recibos"
```
**Esperado**: `409`

---

### C11 — netSalary > grossSalary → 400

```bash
curl -s -o /dev/null -w "%{http_code}" -X POST -H "Content-Type: application/json" \
  -d "{\"periodMonth\":5,\"periodYear\":2026,\"issueDate\":\"2026-05-31\",\"grossSalary\":50000.00,\"netSalary\":60000.00,\"documentId\":\"$DOC_ID\"}" \
  "$BASE/funcionarios/$EMP_ID/recibos"
```
**Esperado**: `400`

---

### C12 — Listar recibos do próprio (área reservada)

```bash
curl -s -H "X-Employee-Id: $EMP_ID" "$BASE/me/payroll-slips"
```
**Esperado**: `200`, lista dos recibos do próprio.

---

### C13 — Download de recibo próprio

```bash
RECIBO_ID="<uuid-recibo>"
curl -s -H "X-Employee-Id: $EMP_ID" "$BASE/me/payroll-slips/$RECIBO_ID/download"
```
**Esperado**: `200`, `{ "downloadUrl": "..." }`

---

### C14 — Download de recibo alheio → 404

```bash
curl -s -o /dev/null -w "%{http_code}" -H "X-Employee-Id: $EMP_ID" \
  "$BASE/me/payroll-slips/00000000-0000-0000-0000-000000000099/download"
```
**Esperado**: `404`
