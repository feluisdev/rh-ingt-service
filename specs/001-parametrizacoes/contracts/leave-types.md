# Contract: /leave-types

CRUD para Tipos de Ausência com flags `deducts_balance`, `requires_approval`, `max_days_per_year`.

## GET /leave-types

**Query parameters**:

| Param | Tipo | Required | Descrição |
|---|---|---|---|
| `active` | boolean | No | Default `true` |
| `deductsBalance` | boolean | No | Filtra por flag |
| `requiresApproval` | boolean | No | Filtra por flag |
| `categoryOptionKey` | string | No | Filtra por chave de categoria (ex: `FERIAS`, `DOENCA`) |
| `page`, `size`, `sort` | | No | |

**Response 200**:

```json
{
  "content": [
    { "id": "uuid", "code": "FERIAS",      "name": "Férias",        "categoryOptionKey": "FERIAS",  "deductsBalance": true,  "requiresApproval": true,  "maxDaysPerYear": 22,  "isActive": true },
    { "id": "uuid", "code": "DOENCA",      "name": "Doença",        "categoryOptionKey": "DOENCA",  "deductsBalance": false, "requiresApproval": true,  "maxDaysPerYear": null,"isActive": true },
    { "id": "uuid", "code": "MATERNIDADE", "name": "Maternidade",   "categoryOptionKey": "FAMILIA", "deductsBalance": false, "requiresApproval": false, "maxDaysPerYear": 120, "isActive": true },
    { "id": "uuid", "code": "PATERNIDADE", "name": "Paternidade",   "categoryOptionKey": "FAMILIA", "deductsBalance": false, "requiresApproval": false, "maxDaysPerYear": 20,  "isActive": true },
    { "id": "uuid", "code": "LUTO",        "name": "Luto",          "categoryOptionKey": "FAMILIA", "deductsBalance": false, "requiresApproval": false, "maxDaysPerYear": 5,   "isActive": true },
    { "id": "uuid", "code": "CASAMENTO",   "name": "Casamento",     "categoryOptionKey": "FAMILIA", "deductsBalance": false, "requiresApproval": false, "maxDaysPerYear": 8,   "isActive": true }
  ],
  "page": 1, "size": 20, "totalElements": 6, "totalPages": 1
}
```

## GET /leave-types/{id}

## POST /leave-types

```json
{
  "code": "FORMACAO",
  "name": "Licença para Formação",
  "description": "Tempo dedicado a formação aprovada pelo INGT",
  "categoryOptionId": "uuid-da-categoria-OUTRO",
  "deductsBalance": false,
  "requiresApproval": true,
  "maxDaysPerYear": 30
}
```

**Validações**:

- `code` único, imutável.
- `categoryOptionId` (se preenchido) deve ter `ccode='LEAVE_CATEGORY'` em `t_option_entity`.
- `maxDaysPerYear` ≥ 0 ou `null`.

**Response 201** / **409** / **422**.

## PUT /leave-types/{id}

Edita `name`, `description`, `categoryOptionId`, `deductsBalance`, `requiresApproval`, `maxDaysPerYear`. `code` imutável.

⚠️ **Importante**: alterar `deductsBalance` ou `requiresApproval` afecta o comportamento de novos pedidos a partir desse momento. Pedidos pendentes mantêm a regra ao momento da submissão (controlado em `feat/ausencias`).

## DELETE /leave-types/{id}

Soft delete. Bloqueado se referenciado por pedidos activos (verificado em `feat/ausencias`).

## POST /leave-types/{id}/activate
