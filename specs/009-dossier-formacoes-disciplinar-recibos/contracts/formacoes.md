# Contract: Formações Profissionais

**Base path**: `api/v1/rh/funcionarios/{funcionarioId}/formacoes`

---

## GET /funcionarios/{funcionarioId}/formacoes

Lista as formações de um funcionário.

**Query params**: `year` (integer, opcional)

**Response 200**:
```json
{
  "content": [
    {
      "id": "uuid",
      "funcionarioId": "uuid",
      "name": "Gestão de Projetos com PRINCE2",
      "institution": "Instituto de Gestão Pública",
      "typeOptionKey": "PRESENCIAL",
      "startDate": "2026-03-01",
      "endDate": "2026-03-05",
      "durationHours": 40,
      "documentId": "uuid | null"
    }
  ],
  "totalElements": 1
}
```

---

## GET /funcionarios/{funcionarioId}/formacoes/{formacaoId}

**Response 200**: objecto formação (mesmo schema acima)
**Response 404**: formação não encontrada

---

## POST /funcionarios/{funcionarioId}/formacoes

**Request body**:
```json
{
  "name": "Gestão de Projetos com PRINCE2",
  "institution": "Instituto de Gestão Pública",
  "typeOptionKey": "PRESENCIAL",
  "startDate": "2026-03-01",
  "endDate": "2026-03-05",
  "durationHours": 40,
  "documentId": "uuid | null"
}
```

**Validações**: `name` obrigatório; `typeOptionKey` ∈ {PRESENCIAL, ELEARNING, SEMINARIO, CONGRESSO} se presente; funcionário activo.

**Response 201**: `{ "id": "uuid" }`
**Response 400**: validação falhou
**Response 403**: funcionário inactivo

---

## PUT /funcionarios/{funcionarioId}/formacoes/{formacaoId}

**Request body**: mesmo schema do POST (todos os campos opcionais na actualização)

**Response 200**: `{ "message": "Formação actualizada com sucesso" }`
**Response 404**: formação não encontrada

---

## DELETE /funcionarios/{funcionarioId}/formacoes/{formacaoId}

**Response 200**: `{ "message": "Formação removida com sucesso" }`
**Response 404**: formação não encontrada
