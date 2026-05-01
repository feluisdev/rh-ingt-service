# API Contract: Contratos

**Base path**: `api/v1/rh/contratos` e sub-recurso `api/v1/rh/funcionarios/{id}/contratos`

## POST /contratos — Criar contrato

**Request body**:
```json
{
  "funcionarioId": "uuid (required)",
  "tipoContrato": "string (Option ckey, required)",
  "dataInicio": "date (YYYY-MM-DD, required)",
  "dataFim": "date (optional)",
  "numeroContrato": "string (optional, unique)"
}
```

**Responses**:
- `201 Created`: `{ "id": "uuid", "message": "Criado com sucesso" }`
- `409 Conflict`: já existe contrato activo para este funcionário, ou numero_contrato duplicado
- `404 Not Found`: funcionário não existe

---

## GET /funcionarios/{id}/contratos — Listar contratos do funcionário

**Response 200**: lista ordenada por `data_inicio` decrescente; inclui activos e inactivos.

---

## GET /contratos/{id} — Obter contrato por ID

**Response 200**: objecto completo. **Response 404**: não encontrado.

---

## PUT /contratos/{id} — Actualizar contrato

**Request body**: `tipoContrato`, `dataInicio`, `dataFim`, `numeroContrato` (campos imutáveis: `funcionarioId`).

**Responses**:
- `200 OK`: objecto actualizado
- `409 Conflict`: numero_contrato duplicado

---

## DELETE /contratos/{id} — Desactivar contrato (soft delete)

**Response 200**: `{ "message": "Desactivado com sucesso" }`
**Response 409**: é o único contrato activo do funcionário (sempre bloqueado, independentemente da situação profissional)

## PUT /contratos/{id}/activate — Reactivar contrato

**Response 200**: `{ "message": "Activado com sucesso" }`
**Response 409**: já existe outro contrato activo para o mesmo funcionário
