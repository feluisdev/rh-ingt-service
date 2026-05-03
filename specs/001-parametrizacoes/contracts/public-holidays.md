# Contract: /public-holidays

CRUD para Feriados Nacionais e Municipais. Alimenta o cálculo de dias úteis em pedidos de ausência (`feat/ausencias`).

## GET /public-holidays

**Query parameters**:

| Param | Tipo | Required | Descrição |
|---|---|---|---|
| `year` | integer | No | Filtra por ano |
| `isNational` | boolean | No | Filtra nacionais (`true`) ou municipais (`false`) |
| `active` | boolean | No | Default `true` |
| `dateFrom`, `dateTo` | date | No | Janela temporal (formato `YYYY-MM-DD`) |
| `page`, `size`, `sort` | | No | Default sort: `holidayDate,asc` |

**Response 200**:

```json
{
  "content": [
    { "id": "uuid", "holidayDate": "2026-01-01", "name": "Ano Novo",            "isNational": true,  "description": null, "isActive": true },
    { "id": "uuid", "holidayDate": "2026-01-13", "name": "Dia da Liberdade",    "isNational": true,  "description": null, "isActive": true },
    { "id": "uuid", "holidayDate": "2026-01-20", "name": "Dia dos Heróis",      "isNational": true,  "description": null, "isActive": true },
    { "id": "uuid", "holidayDate": "2026-05-01", "name": "Dia do Trabalhador", "isNational": true,  "description": null, "isActive": true }
  ],
  "page": 1, "size": 20, "totalElements": 4, "totalPages": 1
}
```

## GET /public-holidays/{id}

## POST /public-holidays

```json
{
  "holidayDate": "2026-07-25",
  "name": "Dia de Santiago",
  "isNational": false,
  "description": "Feriado municipal do concelho da Praia"
}
```

**Validações**:

- `holidayDate` obrigatório (formato `YYYY-MM-DD`).
- `name` obrigatório.
- Se `isNational = true`, não pode existir outro feriado nacional com a mesma data (DB constraint via partial unique index).
- Datas passadas são permitidas (registo histórico) com aviso UI.

**Response 201** — criado.
**Response 409** — feriado nacional duplicado para a data.

## PUT /public-holidays/{id}

Edita todos os campos excepto `id`.

## DELETE /public-holidays/{id}

Soft delete.

## POST /public-holidays/{id}/activate
