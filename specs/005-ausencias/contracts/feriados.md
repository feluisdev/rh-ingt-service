# API Contract: Feriados

**Base path**: `api/v1/rh/parametrizacoes/feriados`

## POST /feriados — Criar feriado

**Request body**:
```json
{
  "nome": "string (max 150, required)",
  "data": "date (YYYY-MM-DD, required)",
  "isNational": "boolean (required)",
  "municipioCkey": "string (Option ckey, optional — apenas para municipais)"
}
```

**Responses**:
- `201 Created`: `{ "id": "uuid", "message": "Criado com sucesso" }`
- `409 Conflict`: já existe feriado nacional activo nessa data
- `400 Bad Request`: campos obrigatórios em falta

---

## GET /feriados — Listar feriados

**Query params**: `ano` (integer, opcional), `isNational` (boolean, opcional), `active` (boolean, default true), `pagina`, `tamanho`

**Response 200**:
```json
{
  "content": [
    {
      "id": "uuid",
      "nome": "string",
      "data": "2026-01-01",
      "isNational": true,
      "municipioCkey": null,
      "isActive": true
    }
  ],
  "totalElements": 11
}
```

---

## GET /feriados/{id} — Obter por ID

**Response 200**: objecto completo  
**Response 404**: feriado não encontrado

---

## PUT /feriados/{id} — Actualizar

**Request body**: mesmos campos do POST

**Responses**:
- `200 OK`: `{ "message": "Actualizado com sucesso" }`
- `404 Not Found`
- `409 Conflict`: conflito de data para feriado nacional

---

## PATCH /feriados/{id}/ativar — Activar

**Response 200**: `{ "message": "Activado com sucesso" }` (idempotente)  
**Response 409**: já existe feriado nacional activo nessa data

---

## PATCH /feriados/{id}/desativar — Desactivar

**Response 200**: `{ "message": "Desactivado com sucesso" }`
