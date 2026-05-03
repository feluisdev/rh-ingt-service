# Contract: /professional-situations

CRUD para Situações Profissionais (Efetivo, Contratado, Comissionado, Estagiário) — categorias do PCFR (Decreto-Lei 4/2024).

## GET /professional-situations

**Query parameters**:

| Param | Tipo | Required | Descrição |
|---|---|---|---|
| `active` | boolean | No | Default `true` |
| `page`, `size`, `sort` | | No | Paginação padrão |

**Response 200**:

```json
{
  "content": [
    { "id": "uuid", "code": "EFETIVO",      "name": "Efetivo",      "description": null, "isActive": true },
    { "id": "uuid", "code": "CONTRATADO",   "name": "Contratado",   "description": null, "isActive": true },
    { "id": "uuid", "code": "COMISSIONADO", "name": "Comissionado", "description": null, "isActive": true },
    { "id": "uuid", "code": "ESTAGIARIO",   "name": "Estagiário",   "description": null, "isActive": true }
  ],
  "page": 1, "size": 20, "totalElements": 4, "totalPages": 1
}
```

## GET /professional-situations/{id}

## POST /professional-situations

```json
{
  "code": "VOLUNTARIADO",
  "name": "Voluntariado",
  "description": "Colaboração voluntária sem vínculo formal"
}
```

**Validações**: `code` único e imutável.

**Response 201** / **409** (code duplicado).

## PUT /professional-situations/{id}

Edita `name`, `description`. `code` imutável.

## DELETE /professional-situations/{id}

Soft delete. Bloqueado se referenciado por funcionário activo (verificação preparatória — entra em `feat/colaboradores-core`).

## POST /professional-situations/{id}/activate
