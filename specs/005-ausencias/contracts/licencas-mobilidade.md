# API Contract: Licenças e Mobilidade

**Base path**: `api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade`

## POST /licencas-mobilidade — Registar licença/mobilidade

**Request body**:
```json
{
  "subtipoId": "uuid (required)",
  "dataInicio": "date (YYYY-MM-DD, required)",
  "dataFim": "date (YYYY-MM-DD, optional — null = em curso)",
  "entidadeDestino": "string (max 200, optional)",
  "despachoNumero": "string (max 100, optional)",
  "observacoes": "string (optional)"
}
```

**Responses**:
- `201 Created`: `{ "id": "uuid", "message": "Criado com sucesso" }`
- `404 Not Found`: funcionário ou subtipo não existe
- `400 Bad Request`: `dataFim < dataInicio`

---

## GET /licencas-mobilidade — Listar

**Query params**: `active` (boolean, default true), `subtipoId` (uuid, opcional), `pagina`, `tamanho`

**Response 200**:
```json
{
  "content": [
    {
      "id": "uuid",
      "subtipo": {
        "id": "uuid",
        "nome": "string",
        "codigo": "string",
        "recordType": "MOBILIDADE"
      },
      "dataInicio": "2026-01-15",
      "dataFim": null,
      "entidadeDestino": "string",
      "despachoNumero": "string",
      "observacoes": "string",
      "isActive": true
    }
  ],
  "totalElements": 1
}
```

---

## GET /licencas-mobilidade/{id} — Obter por ID

**Response 200**: objecto completo  
**Response 404**: registo não encontrado

---

## PUT /licencas-mobilidade/{id} — Actualizar

**Request body**: mesmos campos do POST

**Responses**:
- `200 OK`: `{ "message": "Actualizado com sucesso" }`
- `404 Not Found`
- `400 Bad Request`: `dataFim < dataInicio`

---

## PATCH /licencas-mobilidade/{id}/ativar — Activar

**Response 200**: `{ "message": "Activado com sucesso" }` (idempotente)

---

## PATCH /licencas-mobilidade/{id}/desativar — Desactivar

**Response 200**: `{ "message": "Desactivado com sucesso" }`
