# Contract: /leave-mobility-subtypes

CRUD para Subtipos de Licença e Mobilidade, com flags `affects_pay`, `counts_for_seniority`, `can_self_submit`.

## GET /leave-mobility-subtypes

**Query parameters**:

| Param | Tipo | Required | Descrição |
|---|---|---|---|
| `active` | boolean | No | Default `true` |
| `recordType` | string | No | `LICENCA`, `MOBILIDADE`, `AMBOS` |
| `affectsPay` | boolean | No | |
| `countsForSeniority` | boolean | No | |
| `canSelfSubmit` | boolean | No | |
| `page`, `size`, `sort` | | No | |

**Response 200**:

```json
{
  "content": [
    { "id": "uuid", "code": "LIC_SEM_VENC", "name": "Licença sem vencimento", "recordType": "LICENCA",    "affectsPay": true,  "countsForSeniority": false, "canSelfSubmit": false, "isActive": true },
    { "id": "uuid", "code": "MOB_INTERNA",  "name": "Mobilidade Interna",     "recordType": "MOBILIDADE", "affectsPay": false, "countsForSeniority": true,  "canSelfSubmit": false, "isActive": true },
    { "id": "uuid", "code": "COMISSAO_S",   "name": "Comissão de Serviço",    "recordType": "AMBOS",      "affectsPay": false, "countsForSeniority": true,  "canSelfSubmit": false, "isActive": true }
  ],
  "page": 1, "size": 20, "totalElements": 3, "totalPages": 1
}
```

## GET /leave-mobility-subtypes/{id}

## POST /leave-mobility-subtypes

```json
{
  "code": "LIC_FORMACAO",
  "name": "Licença para Formação Profissional",
  "description": "Frequência de curso longo aprovado",
  "recordType": "LICENCA",
  "affectsPay": false,
  "countsForSeniority": true,
  "canSelfSubmit": true
}
```

**Validações**:

- `code` único, imutável.
- `recordType` ∈ {`LICENCA`, `MOBILIDADE`, `AMBOS`}.

**Response 201** / **409** / **422**.

## PUT /leave-mobility-subtypes/{id}

Edita `name`, `description`, `recordType`, `affectsPay`, `countsForSeniority`, `canSelfSubmit`. `code` imutável.

## DELETE /leave-mobility-subtypes/{id}

Soft delete. Bloqueado se referenciado por registos activos (verificado em `feat/ausencias`).

## POST /leave-mobility-subtypes/{id}/activate
