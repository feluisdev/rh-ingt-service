# Contract: /reference/options

CRUD para o catálogo genérico de etiquetas (`option_entity`). Serve 11 grupos via campo `ccode`.

## GET /reference/options

Lista entradas filtradas por grupo e idioma. Suporta paginação.

**Query parameters**:

| Param | Tipo | Required | Descrição |
|---|---|---|---|
| `ccode` | string | Yes | Grupo (`MARITAL_STATUS`, `SEX`, `NATIONALITY`, `UNIT_TYPE`, `DOC_CATEGORY`, `LEAVE_CATEGORY`, `QUALIFICATION_LEVEL`, `RELATIONSHIP_TYPE`, `ISLAND`, `CONCELHO`, `TRAINING_TYPE`) |
| `locale` | string | No | Default `pt-CV`. Quando o locale pedido não tem entrada, faz fallback automático para `pt-CV` |
| `active` | boolean | No | Default `true` |
| `page`, `size`, `sort` | | No | Paginação padrão |

**Response 200**:

```json
{
  "content": [
    { "id": "uuid", "ccode": "MARITAL_STATUS", "ckey": "SOLTEIRO", "cvalue": "Solteiro(a)", "locale": "pt-CV", "sortOrder": 1, "active": true, "description": null },
    { "id": "uuid", "ccode": "MARITAL_STATUS", "ckey": "CASADO", "cvalue": "Casado(a)", "locale": "pt-CV", "sortOrder": 2, "active": true, "description": null }
  ],
  "page": 1,
  "size": 20,
  "totalElements": 5,
  "totalPages": 1
}
```

## GET /reference/options/{id}

Devolve uma entrada por ID.

**Response 200** — objecto único.
**Response 404** — ID inexistente.

## POST /reference/options

Cria nova entrada.

**Request body**:

```json
{
  "ccode": "MARITAL_STATUS",
  "ckey": "UNIAO_FACTO",
  "cvalue": "União de Facto",
  "locale": "pt-CV",
  "sortOrder": 3,
  "description": null
}
```

**Validações**:

- `ccode` ∈ conjunto fechado dos 11 grupos.
- `(ccode, ckey, locale)` único.
- Todos os campos obrigatórios excepto `description`.

**Response 201** — entrada criada.
**Response 409** — `(ccode, ckey, locale)` já existe.
**Response 422** — `ccode` não pertence ao conjunto fechado.

## PUT /reference/options/{id}

Actualiza uma entrada existente. **Apenas `cvalue`, `sortOrder` e `description` são editáveis** — `ccode`, `ckey` e `locale` são imutáveis.

**Request body**:

```json
{
  "cvalue": "Solteiro(a)",
  "sortOrder": 1,
  "description": "Estado civil de não casado"
}
```

**Response 200** — entrada actualizada.
**Response 400** — tentativa de alterar campos imutáveis (rejeita silenciosamente os imutáveis ou retorna 400 dependendo da decisão final).
**Response 404** — ID inexistente.

## DELETE /reference/options/{id}

Soft delete — marca `active = false`. **Bloqueado se referenciado por entry activa noutro módulo** (verificado via consulta a `t_document_type.category_option_id`, `t_leave_type.category_option_id`, e em features posteriores: `t_employee.sex_option_id`, etc.).

**Response 204** — desactivada.
**Response 409** — referenciada por registos activos (lista os IDs no body).
**Response 404** — ID inexistente.

## POST /reference/options/{id}/activate

Reactiva uma entrada previamente desactivada. **Operação livre** (FR-002a) — sem restrições adicionais. Auditada.

**Response 200** — entrada reactivada.
**Response 404** — ID inexistente.
**Response 409** — entrada já está activa.
