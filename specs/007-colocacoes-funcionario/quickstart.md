# Quickstart: Colocações de Funcionários

**Feature**: 007-colocacoes-funcionario  
**Date**: 2026-05-01

Cenários de teste manual para verificação end-to-end após implementação.

---

## Pré-requisitos

```bash
# Serviço a correr na porta 8091
# PostgreSQL acessível
# Variáveis de ambiente: SERVICE_PROFILE=development

BASE="http://localhost:8091/api/v1/rh"
FUNC_ID="<uuid-de-funcionario-existente>"
UNIT_ID="<uuid-de-unidade-organica-existente>"
CARGO_ID="<uuid-de-cargo-existente>"
```

---

## Cenário 1 — Registar colocação inicial (US1-AC1)

```bash
curl -s -X POST "$BASE/funcionarios/$FUNC_ID/colocacoes" \
  -H "Content-Type: application/json" \
  -d '{
    "unitId": "'"$UNIT_ID"'",
    "startDate": "2026-01-01",
    "assignmentType": "INICIAL",
    "notes": "Colocação inicial"
  }' | jq .
```

**Esperado**: `201`, `isCurrent: true`, `endDate: null`.

---

## Cenário 2 — Criar segunda colocação fecha a anterior (US1-AC2)

```bash
curl -s -X POST "$BASE/funcionarios/$FUNC_ID/colocacoes" \
  -H "Content-Type: application/json" \
  -d '{
    "unitId": "'"$UNIT_ID"'",
    "startDate": "2026-05-01",
    "assignmentType": "TRANSFERENCIA"
  }' | jq .
```

**Esperado**: `201`, nova colocação com `isCurrent: true`. Listar histórico deve mostrar a anterior com `endDate` preenchido e `isCurrent: false`.

---

## Cenário 3 — Consultar colocação actual (US2-AC1)

```bash
curl -s "$BASE/funcionarios/$FUNC_ID/colocacoes/atual" | jq .
```

**Esperado**: `200`, colocação com `isCurrent: true`.

---

## Cenário 4 — Listar histórico completo (US2-AC3)

```bash
curl -s "$BASE/funcionarios/$FUNC_ID/colocacoes" | jq '.data | length'
```

**Esperado**: `200`, array com 2 entradas (cenários 1 e 2).

---

## Cenário 5 — Filtro `current=true` (US2-AC4)

```bash
curl -s "$BASE/funcionarios/$FUNC_ID/colocacoes?current=true" | jq '.data | length'
```

**Esperado**: `200`, array com 1 entrada (apenas a actual).

---

## Cenário 6 — Corrigir observações (US3-AC1)

```bash
COL_ID="<uuid-da-colocacao-actual>"
curl -s -X PUT "$BASE/funcionarios/$FUNC_ID/colocacoes/$COL_ID" \
  -H "Content-Type: application/json" \
  -d '{"notes": "Correcção de observações via despacho 5/2026"}' | jq .
```

**Esperado**: `200`, `notes` actualizado, `isCurrent` inalterado.

---

## Cenário 7 — Rejeitar date inválida (US3-AC2)

```bash
curl -s -X PUT "$BASE/funcionarios/$FUNC_ID/colocacoes/$COL_ID" \
  -H "Content-Type: application/json" \
  -d '{"endDate": "2025-01-01"}' | jq .
```

**Esperado**: `400` (endDate anterior a startDate).

---

## Cenário 8 — Soft delete de colocação inactiva (US4-AC1)

```bash
# Obter ID de colocação antiga (não-actual)
OLD_ID=$(curl -s "$BASE/funcionarios/$FUNC_ID/colocacoes?current=false" | jq -r '.data[0].id')
curl -s -X DELETE "$BASE/funcionarios/$FUNC_ID/colocacoes/$OLD_ID" | jq .
```

**Esperado**: `200`, mensagem de sucesso.

---

## Cenário 9 — Rejeitar soft delete da colocação actual (US4-AC2)

```bash
curl -s -X DELETE "$BASE/funcionarios/$FUNC_ID/colocacoes/$COL_ID" | jq .
```

**Esperado**: `400` (não é permitido eliminar a colocação actual).

---

## Cenário 10 — Funcionário inexistente (US1-AC3)

```bash
curl -s -X POST "$BASE/funcionarios/00000000-0000-0000-0000-000000000000/colocacoes" \
  -H "Content-Type: application/json" \
  -d '{"startDate": "2026-01-01", "assignmentType": "INICIAL"}' | jq .
```

**Esperado**: `404`.

---

## Cenário 11 — Data futura rejeitada (US1-AC6)

```bash
curl -s -X POST "$BASE/funcionarios/$FUNC_ID/colocacoes" \
  -H "Content-Type: application/json" \
  -d '{"startDate": "2030-01-01", "assignmentType": "INICIAL"}' | jq .
```

**Esperado**: `400` (data de início no futuro).

---

## Cenário 12 — Integração com mobilidade (US5)

```bash
# Assumindo que existe uma LicencaMobilidade pendente
LICENCA_ID="<uuid-de-licenca>"
curl -s -X PUT "$BASE/funcionarios/$FUNC_ID/licencas-mobilidades/$LICENCA_ID/ativar" | jq .

# Verificar que colocação MOBILIDADE foi criada automaticamente
curl -s "$BASE/funcionarios/$FUNC_ID/colocacoes/atual" | jq '.assignmentType'
```

**Esperado**: `"MOBILIDADE"`.
