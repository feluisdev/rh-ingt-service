# Contract: /contract-types

CRUD para Tipos de Contrato. Cada tipo tem implicações legais distintas (Decreto-Lei 4/2024).

## GET /contract-types

**Query parameters**: `active`, `page`, `size`, `sort`.

**Response 200**:

```json
{
  "content": [
    { "id": "uuid", "code": "NOMEACAO_DEFINITIVA", "name": "Nomeação Definitiva", "description": "Vínculo permanente conforme Decreto-Lei 4/2024", "isActive": true },
    { "id": "uuid", "code": "CFP",                 "name": "Contrato de Função Pública", "description": "...", "isActive": true },
    { "id": "uuid", "code": "CTFP_TERMO_CERTO",    "name": "CTFP a Termo Certo", "description": "...", "isActive": true },
    { "id": "uuid", "code": "CTFP_TERMO_INCERTO",  "name": "CTFP a Termo Incerto", "description": "...", "isActive": true },
    { "id": "uuid", "code": "COMISSAO_SERVICO",    "name": "Comissão de Serviço", "description": "...", "isActive": true }
  ],
  "page": 1, "size": 20, "totalElements": 5, "totalPages": 1
}
```

## GET /contract-types/{id}

## POST /contract-types

```json
{
  "code": "ESTAGIO_CURRICULAR",
  "name": "Estágio Curricular",
  "description": "Estágio para fim de formação académica"
}
```

**Validações**: `code` único, imutável após criação. **Response 201** / **409**.

## PUT /contract-types/{id}

Edita `name`, `description`. `code` imutável.

## DELETE /contract-types/{id}

Soft delete. Bloqueado se referenciado por contratos activos (entrará em vigor com `feat/vida-profissional` quando `t_employee_contract.contract_type_id` for criado).

## POST /contract-types/{id}/activate
