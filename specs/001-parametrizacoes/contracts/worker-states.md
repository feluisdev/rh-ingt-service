# Contract: /worker-states

CRUD para o catálogo de Estados do Trabalhador, com flag `is_core` que protege estados núcleo.

## GET /worker-states

Lista todos os estados do trabalhador.

**Query parameters**:

| Param | Tipo | Required | Descrição |
|---|---|---|---|
| `active` | boolean | No | Filtra por estado lógico (default `true`) |
| `isCore` | boolean | No | Filtra por núcleo / não núcleo |
| `page`, `size`, `sort` | | No | Paginação padrão |

**Response 200**:

```json
{
  "content": [
    { "id": "uuid", "code": "ACTIVE",   "name": "Activo",   "description": null, "isCore": true, "isActive": true },
    { "id": "uuid", "code": "INACTIVE", "name": "Inactivo", "description": null, "isCore": true, "isActive": true },
    { "id": "uuid", "code": "SUSPENDED","name": "Suspenso", "description": null, "isCore": false, "isActive": true }
  ],
  "page": 1, "size": 20, "totalElements": 3, "totalPages": 1
}
```

## GET /worker-states/{id}

Devolve estado por ID.

## POST /worker-states

**Request body**:

```json
{
  "code": "ON_LEAVE",
  "name": "De Licença",
  "description": "Funcionário em licença prolongada",
  "isCore": false
}
```

**Validações**:
- `code` único.
- `code` apenas letras maiúsculas, números e underscore.
- `isCore = true` apenas via SYSTEM_ADMIN (regra futura — placeholder agora).

**Response 201** — criado.
**Response 409** — `code` duplicado.

## PUT /worker-states/{id}

Edita `name`, `description`, `isCore`. **`code` é imutável**.

**Response 200** — actualizado.
**Response 404** — não encontrado.

## DELETE /worker-states/{id}

Soft delete. **Bloqueado se `isCore = true`** (FR-009).

**Response 204** — desactivado.
**Response 409** — `isCore = true` — corpo: `{"error": "WORKER_STATE_IS_CORE", "message": "Estado núcleo não pode ser desactivado"}`.

## POST /worker-states/{id}/activate

Reactiva estado desactivado.

**Response 200** — reactivado.
