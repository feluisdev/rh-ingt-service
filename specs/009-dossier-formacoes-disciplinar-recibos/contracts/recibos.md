# Contract: Recibos de Vencimento

**Base paths**:
- `api/v1/rh/funcionarios/{funcionarioId}/recibos`
- `api/v1/rh/recibos/{id}`
- `api/v1/rh/me/payroll-slips` (área reservada)

---

## GET /funcionarios/{funcionarioId}/recibos

Lista os recibos de um funcionário.

**Response 200**:
```json
{
  "content": [
    {
      "id": "uuid",
      "funcionarioId": "uuid",
      "periodMonth": 4,
      "periodYear": 2026,
      "issueDate": "2026-04-30",
      "grossSalary": 85000.00,
      "netSalary": 72000.00,
      "documentId": "uuid"
    }
  ],
  "totalElements": 1
}
```

---

## GET /recibos/{id}

Detalhe de um recibo por ID directo.

**Response 200**: objecto recibo (mesmo schema acima)
**Response 404**: recibo não encontrado

---

## POST /funcionarios/{funcionarioId}/recibos

**Request body**:
```json
{
  "periodMonth": 4,
  "periodYear": 2026,
  "issueDate": "2026-04-30",
  "grossSalary": 85000.00,
  "netSalary": 72000.00,
  "documentId": "uuid"
}
```

**Validações**:
- Todos os campos obrigatórios
- `periodMonth` ∈ [1, 12]
- `grossSalary > 0`, `netSalary > 0`, `netSalary ≤ grossSalary`
- Par `(funcionarioId, periodMonth, periodYear)` deve ser único

**Response 201**: `{ "id": "uuid" }`
**Response 400**: validação falhou
**Response 409**: recibo duplicado para o mesmo mês/ano

---

## GET /me/payroll-slips

Lista os recibos do próprio funcionário autenticado.

**Query params**: `periodYear` (integer, opcional), `periodMonth` (integer, opcional)

**Response 200**: mesmo schema de listagem acima (filtrado ao funcionário autenticado)

---

## GET /me/payroll-slips/{id}/download

Obtém URL de download do PDF do recibo. Valida que o recibo pertence ao funcionário autenticado.

**Response 200**:
```json
{
  "downloadUrl": "https://minio.example.com/presigned-url..."
}
```
**Response 404**: recibo não encontrado ou não pertence ao utilizador autenticado
