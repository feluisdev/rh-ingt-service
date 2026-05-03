# API Contract: Tipos de Ausência

**Base path**: `api/v1/rh/parametrizacoes/tipos-ausencia`

## POST /tipos-ausencia — Criar tipo de ausência

**Request body**:
```json
{
  "nome": "string (max 150, required)",
  "codigo": "string (max 50, required, unique)",
  "deductsBalance": "boolean (required)",
  "requiresApproval": "boolean (required)",
  "maxDaysPerYear": "integer (optional, null = ilimitado)",
  "categoryOptionCkey": "string (Option ckey, optional)"
}
```

**Responses**:
- `201 Created`: `{ "id": "uuid", "message": "Criado com sucesso" }`
- `409 Conflict`: código já existe
- `400 Bad Request`: campos obrigatórios em falta

---

## GET /tipos-ausencia — Listar tipos de ausência

**Query params**: `active` (boolean, default true), `pagina` (default 0), `tamanho` (default 20)

**Response 200**:
```json
{
  "content": [
    {
      "id": "uuid",
      "nome": "string",
      "codigo": "string",
      "deductsBalance": true,
      "requiresApproval": true,
      "maxDaysPerYear": 22,
      "categoryOptionCkey": "string",
      "isActive": true
    }
  ],
  "totalElements": 0
}
```

---

## GET /tipos-ausencia/{id} — Obter por ID

**Response 200**: objecto completo (mesma estrutura da listagem)  
**Response 404**: tipo não encontrado

---

## PUT /tipos-ausencia/{id} — Actualizar

**Request body**: mesmos campos do POST (todos opcionais na actualização)

**Responses**:
- `200 OK`: `{ "message": "Actualizado com sucesso" }`
- `404 Not Found`
- `409 Conflict`: código já em uso por outro registo

---

## PATCH /tipos-ausencia/{id}/ativar — Activar

**Response 200**: `{ "message": "Activado com sucesso" }` (idempotente)

---

## PATCH /tipos-ausencia/{id}/desativar — Desactivar

**Response 200**: `{ "message": "Desactivado com sucesso" }`
