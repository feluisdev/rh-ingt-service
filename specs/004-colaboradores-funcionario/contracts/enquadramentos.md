# API Contract: Enquadramentos Profissionais

**Base paths**:
- `api/v1/rh/funcionarios/{funcionarioId}/enquadramento` — enquadramento actual
- `api/v1/rh/funcionarios/{funcionarioId}/enquadramentos/historico` — histórico completo
- `api/v1/rh/enquadramentos` — CRUD directo

## POST /enquadramentos — Criar enquadramento

**Request body**:
```json
{
  "funcionarioId": "uuid (required)",
  "careerId": "uuid (required)",
  "categoryId": "uuid (required)",
  "gradeId": "uuid (required)",
  "cargoId": "uuid (required)",
  "unidadeOrganicaId": "uuid (required)",
  "dataInicio": "date (YYYY-MM-DD, required)"
}
```

**Responses**:
- `201 Created`: `{ "id": "uuid", "message": "Criado com sucesso" }`
- `404 Not Found`: funcionário não existe
- `422 Unprocessable Entity`: entidade referenciada inactiva, ou data_inicio ≤ data_inicio do enquadramento actual
- `409 Conflict`: enquadramento actual já tem a mesma data_inicio

**Efeito colateral**: enquadramento anterior é automaticamente encerrado (`data_fim = data_inicio - 1 dia`, `is_current = false`).

---

## GET /funcionarios/{id}/enquadramento — Enquadramento actual

**Response 200**:
```json
{
  "id": "uuid",
  "funcionarioId": "uuid",
  "careerId": "uuid",
  "careerName": "string",
  "categoryId": "uuid",
  "categoryName": "string",
  "gradeId": "uuid",
  "gradeNumber": 1,
  "cargoId": "uuid",
  "cargoName": "string",
  "unidadeOrganicaId": "uuid",
  "unidadeOrganicaName": "string",
  "dataInicio": "date",
  "dataFim": null,
  "isCurrent": true
}
```
**Response 404**: funcionário sem enquadramento actual.

---

## GET /funcionarios/{id}/enquadramentos/historico — Histórico

**Response 200**: lista de todos os enquadramentos ordenados por `data_inicio` decrescente.

---

## GET /enquadramentos/{id} — Obter enquadramento por ID

**Response 200**: objecto completo. **Response 404**: não encontrado.
