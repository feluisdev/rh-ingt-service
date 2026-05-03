# API Contract: Subtipos de Licença/Mobilidade

**Base path**: `api/v1/rh/parametrizacoes/subtipos-licenca-mobilidade`

## POST /subtipos-licenca-mobilidade — Criar subtipo

**Request body**:
```json
{
  "nome": "string (max 150, required)",
  "codigo": "string (max 50, required, unique)",
  "recordType": "string (required) — LICENCA | MOBILIDADE | AMBOS",
  "affectsPay": "boolean (required)",
  "countsForSeniority": "boolean (required)",
  "canSelfSubmit": "boolean (required)"
}
```

**Responses**:
- `201 Created`: `{ "id": "uuid", "message": "Criado com sucesso" }`
- `409 Conflict`: código já existe
- `400 Bad Request`: campos obrigatórios em falta ou `recordType` inválido

---

## GET /subtipos-licenca-mobilidade — Listar

**Query params**: `active` (boolean, default true), `recordType` (LICENCA/MOBILIDADE/AMBOS, opcional), `pagina`, `tamanho`

**Response 200**:
```json
{
  "content": [
    {
      "id": "uuid",
      "nome": "string",
      "codigo": "string",
      "recordType": "LICENCA",
      "affectsPay": false,
      "countsForSeniority": true,
      "canSelfSubmit": false,
      "isActive": true
    }
  ],
  "totalElements": 0
}
```

---

## GET /subtipos-licenca-mobilidade/{id} — Obter por ID

**Response 200**: objecto completo  
**Response 404**: subtipo não encontrado

---

## PUT /subtipos-licenca-mobilidade/{id} — Actualizar

**Request body**: mesmos campos do POST

**Responses**:
- `200 OK`: `{ "message": "Actualizado com sucesso" }`
- `404 Not Found`
- `409 Conflict`: código já em uso

---

## PATCH /subtipos-licenca-mobilidade/{id}/ativar — Activar

**Response 200**: `{ "message": "Activado com sucesso" }` (idempotente)

---

## PATCH /subtipos-licenca-mobilidade/{id}/desativar — Desactivar

**Response 200**: `{ "message": "Desactivado com sucesso" }`
