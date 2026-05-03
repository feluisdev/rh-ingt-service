# API Contract: Saldos de Ausência

**Base path**: `api/v1/rh/funcionarios/{funcionarioId}/saldos-ausencia`

## POST /saldos-ausencia — Criar saldo

**Request body**:
```json
{
  "tipoAusenciaId": "uuid (required)",
  "ano": "integer (required)",
  "diasDireito": "integer (required, >= 0)"
}
```

**Responses**:
- `201 Created`: `{ "id": "uuid", "message": "Criado com sucesso" }`
- `409 Conflict`: já existe saldo para `(funcionarioId, tipoAusenciaId, ano)`
- `400 Bad Request`: campos obrigatórios em falta ou `diasDireito < 0`

---

## GET /saldos-ausencia — Listar saldos do funcionário

**Query params**: `ano` (integer, opcional), `tipoAusenciaId` (uuid, opcional), `pagina`, `tamanho`

**Response 200**:
```json
{
  "content": [
    {
      "id": "uuid",
      "tipoAusencia": { "id": "uuid", "nome": "Férias", "codigo": "FERIAS" },
      "ano": 2026,
      "diasDireito": 22,
      "diasGozados": 0,
      "diasPendentes": 5,
      "diasDisponiveis": 17
    }
  ],
  "totalElements": 1
}
```

*Nota*: `diasDisponiveis = diasDireito - diasGozados - diasPendentes` (calculado na resposta)

---

## GET /saldos-ausencia/{id} — Obter por ID

**Response 200**: objecto completo  
**Response 404**: saldo não encontrado

---

## PUT /saldos-ausencia/{id} — Actualizar dias de direito

**Request body**:
```json
{
  "diasDireito": "integer (required, >= 0)"
}
```

**Responses**:
- `200 OK`: `{ "message": "Actualizado com sucesso" }`
- `404 Not Found`
- `400 Bad Request`: `diasDireito < 0`
