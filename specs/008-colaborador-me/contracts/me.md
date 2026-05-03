# API Contracts: Meu Perfil — /me

**Base path**: `api/v1/rh/me`  
**Autenticação**: `ROLE_FUNCIONARIO` (prod); header `X-Employee-Id` (dev)  
**Isolamento**: Todos os endpoints filtram pelo funcionário do token — nunca aceita IDs externos.

---

## GET /me/profile

**Descrição**: Perfil completo do colaborador autenticado.

**Response 200**:
```json
{
  "id": "uuid",
  "fullName": "Nome Completo",
  "nif": "12345678",
  "email": "colaborador@ingt.gov.cv",
  "phone": "+238 261 0000",
  "workerState": "ACTIVE",
  "admissionDate": "2018-09-01",
  "currentUnit": { "id": "uuid", "name": "Direcção de Serviços" },
  "currentJob": { "id": "uuid", "name": "Técnico Superior" },
  "currentFunction": { "id": "uuid", "name": "Coordenador de Projecto" },
  "career": { "id": "uuid", "name": "Nível Técnico I" },
  "category": { "id": "uuid", "name": "Técnico Superior Principal" },
  "grade": { "id": "uuid", "gradeNumber": 3 }
}
```

**Errors**: `403` (inactivo), `404` (sem funcionário associado ao JWT)

---

## GET /me/leave-requests

**Descrição**: Pedidos de ausência do próprio.

**Query params**: `status` (PENDING|APPROVED|REJECTED|CANCELLED), `leaveTypeId` (UUID), `year` (int)

**Response 200**:
```json
{
  "content": [
    {
      "id": "uuid",
      "leaveTypeId": "uuid",
      "leaveTypeName": "Férias",
      "startDate": "2026-07-01",
      "endDate": "2026-07-15",
      "status": "APPROVED",
      "notes": "Aprovado por despacho"
    }
  ],
  "totalElements": 1
}
```

**Errors**: `403` (inactivo), `404` (sem funcionário)

---

## POST /me/leave-requests

**Descrição**: Submeter pedido de ausência próprio.

**Request body**:
```json
{
  "leaveTypeId": "uuid",
  "startDate": "2026-08-01",
  "endDate": "2026-08-10",
  "notes": "Férias de verão"
}
```

**Response 201**: `{ "id": "uuid", "message": "Pedido submetido com sucesso" }`

**Errors**: `400` (sobreposição de datas, saldo insuficiente, data inválida), `403` (inactivo), `404` (tipo de ausência não encontrado)

---

## PUT /me/leave-requests/{id}/cancel

**Descrição**: Cancelar pedido próprio em estado PENDING.

**Response 200**: `{ "message": "Pedido cancelado com sucesso" }`

**Errors**: `400` (estado diferente de PENDING), `403` (inactivo), `404` (pedido não pertence ao colaborador)

---

## GET /me/leave-balances

**Descrição**: Saldos de ausência do próprio para o ano corrente.

**Response 200**:
```json
{
  "content": [
    {
      "leaveTypeId": "uuid",
      "leaveTypeName": "Férias",
      "totalDays": 22,
      "usedDays": 5,
      "pendingDays": 3,
      "availableDays": 14
    }
  ]
}
```

**Errors**: `403` (inactivo), `404` (sem funcionário)

---

## GET /me/leaves-mobilities

**Descrição**: Licenças e mobilidades do próprio, ordenadas por data de início descendente.

**Response 200**:
```json
{
  "content": [
    {
      "id": "uuid",
      "subtipoId": "uuid",
      "subtipoNome": "Mobilidade Interna",
      "dataInicio": "2026-01-01",
      "dataFim": "2026-06-30",
      "entidadeDestino": "ANMCV",
      "isActive": true
    }
  ],
  "totalElements": 1
}
```

**Errors**: `403` (inactivo), `404` (sem funcionário)

---

## POST /me/leaves-mobilities

**Descrição**: Auto-submissão de licença/mobilidade (apenas quando `canSelfSubmit=true` no subtipo).

**Request body**:
```json
{
  "subtipoId": "uuid",
  "dataInicio": "2026-06-01",
  "dataFim": "2026-12-31",
  "entidadeDestino": "Câmara Municipal de Praia",
  "observacoes": "Mobilidade voluntária"
}
```

**Response 201**: `{ "id": "uuid", "message": "Licença/mobilidade submetida com sucesso" }`

**Errors**: `400` (datas inválidas), `403` (inactivo ou `canSelfSubmit=false`), `404` (subtipo não encontrado)

---

## GET /me/documents

**Descrição**: Documentos do próprio, com filtro opcional por tipo.

**Query params**: `documentTypeId` (UUID, opcional)

**Response 200**:
```json
{
  "content": [
    {
      "id": "uuid",
      "documentTypeId": "uuid",
      "documentTypeName": "Bilhete de Identidade",
      "fileName": "bi_colaborador.pdf",
      "uploadedAt": "2026-01-15T10:30:00"
    }
  ],
  "totalElements": 1
}
```

**Errors**: `403` (inactivo), `404` (sem funcionário)

---

## GET /me/documents/{id}/download

**Descrição**: URL de download pré-assinado do documento. Valida que o documento pertence ao colaborador autenticado.

**Response 200**:
```json
{
  "downloadUrl": "https://minio.../...",
  "expiresAt": "2026-05-02T15:00:00"
}
```

**Errors**: `403` (inactivo), `404` (documento não existe ou não pertence ao colaborador)
