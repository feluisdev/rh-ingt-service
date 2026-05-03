# API Contracts: Colocações de Funcionários

**Base path**: `/api/v1/rh/funcionarios/{funcionarioId}/colocacoes`  
**Tag Swagger**: `Colocações`

---

## POST /colocacoes — Registar nova colocação

**Descrição**: Regista uma nova colocação para o funcionário. Fecha automaticamente a colocação actual (se existir).

**Request Body** (`application/json`):
```json
{
  "unitId": "uuid | null",
  "jobId": "uuid | null",
  "startDate": "2026-01-15",
  "assignmentType": "INICIAL | TRANSFERENCIA | MOBILIDADE | REQUISICAO",
  "notes": "string | null"
}
```

**Validações**:
- `startDate` obrigatório, não pode ser futuro
- `assignmentType` obrigatório, valor do enum `TipoAfectacao`
- `unitId` (se fornecido): deve existir em `t_unidade_organica`
- `jobId` (se fornecido): deve existir em `t_cargo`
- `funcionarioId` (path): deve existir em `t_funcionario`

**Response 201**:
```json
{
  "id": "uuid",
  "funcionarioId": "uuid",
  "unitId": "uuid | null",
  "jobId": "uuid | null",
  "startDate": "2026-01-15",
  "endDate": null,
  "isCurrent": true,
  "assignmentType": "INICIAL",
  "notes": null
}
```

**Errors**:
- `404` — funcionário, unidade ou cargo não encontrado
- `400` — `startDate` futuro, `assignmentType` inválido

---

## GET /colocacoes — Listar histórico de colocações

**Query params**:
- `current` (Boolean, opcional) — se `true`, retorna apenas a colocação actual; se `false`, retorna apenas as inactivas

**Response 200**:
```json
{
  "data": [
    {
      "id": "uuid",
      "funcionarioId": "uuid",
      "unitId": "uuid | null",
      "jobId": "uuid | null",
      "startDate": "2025-06-01",
      "endDate": "2026-01-14",
      "isCurrent": false,
      "assignmentType": "INICIAL",
      "notes": null
    }
  ],
  "total": 2
}
```

**Notas**:
- Por omissão retorna todas as colocações activas (`is_active=true`), ordenadas por `start_date DESC`
- Colocações soft-deleted (`is_active=false`) são excluídas por omissão

---

## GET /colocacoes/atual — Obter colocação actual

**Response 200**:
```json
{
  "id": "uuid",
  "funcionarioId": "uuid",
  "unitId": "uuid | null",
  "jobId": "uuid | null",
  "startDate": "2026-01-15",
  "endDate": null,
  "isCurrent": true,
  "assignmentType": "TRANSFERENCIA",
  "notes": "Transferência por despacho 12/2026"
}
```

**Errors**:
- `404` — funcionário sem colocação actual

---

## GET /colocacoes/{colocacaoId} — Obter colocação por ID

**Response 200**: mesmo schema que `GET /colocacoes/atual`

**Errors**:
- `404` — colocação não encontrada ou não pertence ao funcionário

---

## PUT /colocacoes/{colocacaoId} — Corrigir dados de colocação

**Request Body** (`application/json`):
```json
{
  "endDate": "2026-03-31 | null",
  "notes": "string | null"
}
```

**Validações**:
- `endDate` (se fornecido) >= `startDate` da colocação
- Não altera `isCurrent`, `assignmentType`, `unitId`, `jobId`, `startDate`

**Response 200**: schema `ColocacaoResponse` com dados actualizados

**Errors**:
- `404` — colocação não encontrada ou não pertence ao funcionário
- `400` — `endDate` anterior a `startDate`

---

## DELETE /colocacoes/{colocacaoId} — Soft delete de colocação inactiva

**Response 200**:
```json
{ "message": "Colocação removida com sucesso" }
```

**Errors**:
- `404` — colocação não encontrada ou não pertence ao funcionário
- `400` — tentativa de eliminar a colocação actual (`isCurrent=true`)
