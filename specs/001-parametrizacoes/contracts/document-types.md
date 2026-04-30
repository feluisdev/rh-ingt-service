# Contract: /document-types

CRUD para Tipos de Documento, com `allowed_extensions` e `category_option_id`.

## GET /document-types

**Query parameters**:

| Param | Tipo | Required | Descrição |
|---|---|---|---|
| `active` | boolean | No | Default `true` |
| `categoryOptionKey` | string | No | Filtra por chave da categoria (ex: `PESSOAL`) |
| `page`, `size`, `sort` | | No | |

**Response 200**:

```json
{
  "content": [
    { "id": "uuid", "code": "CNI",        "name": "Cartão Nacional de Identificação", "categoryOptionId": "uuid", "categoryOptionKey": "PESSOAL", "allowedExtensions": "pdf,jpg,png", "isActive": true },
    { "id": "uuid", "code": "PASSAPORTE", "name": "Passaporte",                       "categoryOptionId": "uuid", "categoryOptionKey": "PESSOAL", "allowedExtensions": "pdf,jpg",     "isActive": true },
    { "id": "uuid", "code": "CONTRATO",   "name": "Contrato",                         "categoryOptionId": "uuid", "categoryOptionKey": "CONTRATUAL", "allowedExtensions": "pdf",       "isActive": true }
  ],
  "page": 1, "size": 20, "totalElements": 3, "totalPages": 1
}
```

## GET /document-types/{id}

## POST /document-types

```json
{
  "code": "ATESTADO_MEDICO",
  "name": "Atestado Médico",
  "description": "Comprovativo médico para justificação de ausência",
  "categoryOptionId": "uuid-da-categoria",
  "allowedExtensions": "pdf,jpg,png"
}
```

**Validações**:

- `code` único, imutável.
- `categoryOptionId` (se preenchido) deve referenciar entry de `t_option_entity` com `ccode = 'DOC_CATEGORY'`.
- `allowedExtensions` formato: extensões em minúscula separadas por vírgula, sem espaços, sem ponto inicial. Regex: `^([a-z0-9]+)(,[a-z0-9]+)*$`.

**Response 201** — criado.
**Response 422** — `categoryOptionId` não é da categoria correcta, ou formato `allowedExtensions` inválido.

## PUT /document-types/{id}

Edita `name`, `description`, `categoryOptionId`, `allowedExtensions`. `code` imutável.

## DELETE /document-types/{id}

Soft delete. Bloqueado se referenciado por documentos activos (preparatório — `feat/documentos`).

## POST /document-types/{id}/activate
