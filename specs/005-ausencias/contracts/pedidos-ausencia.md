# API Contract: Pedidos de Ausência

**Base path**: `api/v1/rh/funcionarios/{funcionarioId}/pedidos-ausencia`

## POST /pedidos-ausencia — Submeter pedido

**Request body**:
```json
{
  "tipoAusenciaId": "uuid (required)",
  "dataInicio": "date (YYYY-MM-DD, required)",
  "dataFim": "date (YYYY-MM-DD, required)",
  "motivo": "string (optional)"
}
```

**Responses**:
- `201 Created`: `{ "id": "uuid", "numeroDias": 5, "estado": "PENDENTE", "message": "Pedido submetido com sucesso" }`
- `409 Conflict`: sobreposição de datas com pedido APROVADO ou PENDENTE
- `422 Unprocessable Entity`: `dataFim < dataInicio`, zero dias úteis, `max_days_per_year` excedido
- `404 Not Found`: funcionário ou tipo de ausência não existe

---

## GET /pedidos-ausencia — Listar pedidos do funcionário

**Query params**: `estado` (PENDENTE/APROVADO/REJEITADO/CANCELADO), `tipoAusenciaId` (uuid), `ano` (integer), `pagina`, `tamanho`

**Response 200**:
```json
{
  "content": [
    {
      "id": "uuid",
      "tipoAusencia": { "id": "uuid", "nome": "Férias", "codigo": "FERIAS" },
      "dataInicio": "2026-06-01",
      "dataFim": "2026-06-05",
      "numeroDias": 5,
      "motivo": "string",
      "estado": "PENDENTE",
      "aprovadoPor": null,
      "dataDecisao": null,
      "observacoesDecisao": null,
      "isActive": true
    }
  ],
  "totalElements": 1
}
```

---

## GET /pedidos-ausencia/{id} — Obter por ID

**Response 200**: objecto completo (mesma estrutura da listagem)  
**Response 404**: pedido não encontrado

---

## PATCH /pedidos-ausencia/{id}/aprovar — Aprovar pedido

**Request body**:
```json
{
  "aprovadoPorId": "uuid (required)",
  "observacoesDecisao": "string (optional)"
}
```

**Responses**:
- `200 OK`: `{ "message": "Aprovado com sucesso" }`
- `409 Conflict`: pedido não está em estado PENDENTE
- `422 Unprocessable Entity`: saldo insuficiente ou SaldoAusencia não existe para o ano

---

## PATCH /pedidos-ausencia/{id}/rejeitar — Rejeitar pedido

**Request body**:
```json
{
  "aprovadoPorId": "uuid (required)",
  "observacoesDecisao": "string (optional)"
}
```

**Responses**:
- `200 OK`: `{ "message": "Rejeitado com sucesso" }`
- `409 Conflict`: pedido não está em estado PENDENTE

---

## PATCH /pedidos-ausencia/{id}/cancelar — Cancelar pedido

**Request body**: vazio

**Responses**:
- `200 OK`: `{ "message": "Cancelado com sucesso" }`
- `409 Conflict`: pedido está em estado REJEITADO ou CANCELADO (não pode ser cancelado)
