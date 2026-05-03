# Contract: Processos Disciplinares

**Base path**: `api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares`

**Nota**: Restrição de perfil (ADMIN/OPERADOR) não implementada nesta iteração — CRUD aberto.

---

## GET /funcionarios/{funcionarioId}/processos-disciplinares

Lista os processos disciplinares de um funcionário.

**Response 200**:
```json
{
  "content": [
    {
      "id": "uuid",
      "funcionarioId": "uuid",
      "processNumber": "PD-2026-001",
      "startDate": "2026-01-10",
      "endDate": null,
      "penalty": null,
      "penaltyStartDate": null,
      "penaltyEndDate": null,
      "officialBulletin": null,
      "notes": "Processo em instrução",
      "documentId": null
    }
  ],
  "totalElements": 1
}
```

---

## GET /funcionarios/{funcionarioId}/processos-disciplinares/{processoId}

**Response 200**: objecto processo (mesmo schema acima)
**Response 404**: processo não encontrado

---

## POST /funcionarios/{funcionarioId}/processos-disciplinares

**Request body**:
```json
{
  "processNumber": "PD-2026-001",
  "startDate": "2026-01-10",
  "endDate": null,
  "penalty": null,
  "penaltyStartDate": null,
  "penaltyEndDate": null,
  "officialBulletin": null,
  "notes": "Processo em instrução",
  "documentId": null
}
```

**Validações**: `startDate` obrigatório; funcionário deve existir.

**Response 201**: `{ "id": "uuid" }`
**Response 400**: validação falhou

---

## PUT /funcionarios/{funcionarioId}/processos-disciplinares/{processoId}

**Request body**: mesmo schema do POST (todos os campos opcionais na actualização)

**Response 200**: `{ "message": "Processo disciplinar actualizado com sucesso" }`
**Response 404**: processo não encontrado
