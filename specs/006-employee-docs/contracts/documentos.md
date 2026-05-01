# API Contract: Documentos de Funcionários

**Base path**: `api/v1/rh/funcionarios/{funcionarioId}/documentos`

---

## POST / — Upload de documento

**Request**: `multipart/form-data`

| Campo | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `file` | File | Sim | Ficheiro a enviar (max 10 MB) |
| `documentTypeId` | UUID | Sim | ID do tipo de documento |
| `description` | String | Não | Observações opcionais |

**Responses**:

```
201 Created
{
  "id": "uuid",
  "fileKey": "funcionario_documents/bi_1746123456.pdf",
  "originalFilename": "bi_joao.pdf",
  "contentType": "application/pdf",
  "fileSize": 204800,
  "message": "Documento enviado com sucesso"
}

400 Bad Request — ficheiro > 10 MB, extensão não permitida, documentTypeId inválido/inactivo
404 Not Found  — funcionárioId não existe
```

---

## GET / — Listar documentos do funcionário

**Query params**:

| Param | Tipo | Obrigatório | Descrição |
|-------|------|-------------|-----------|
| `documentTypeId` | UUID | Não | Filtrar por tipo |
| `active` | Boolean | Não | Filtrar por estado (omitir = todos) |

**Response**:

```
200 OK
{
  "content": [
    {
      "id": "uuid",
      "documentTypeId": "uuid",
      "documentType": { "id": "uuid", "descricao": "Bilhete de Identidade", "codigo": "BI" },
      "originalFilename": "bi_joao.pdf",
      "contentType": "application/pdf",
      "fileSize": 204800,
      "description": null,
      "isActive": true,
      "createdDate": "2026-05-01T10:00:00Z"
    }
  ],
  "totalElements": 1
}
```

---

## GET /{documentoId} — Metadados de um documento

**Response**:

```
200 OK
{
  "id": "uuid",
  "funcionarioId": "uuid",
  "documentTypeId": "uuid",
  "documentType": { "id": "uuid", "descricao": "Bilhete de Identidade", "codigo": "BI" },
  "originalFilename": "bi_joao.pdf",
  "contentType": "application/pdf",
  "fileSize": 204800,
  "description": null,
  "isActive": true
}

404 Not Found — documentoId não existe ou não pertence ao funcionário
```

---

## GET /{documentoId}/download — URL pré-assinada

**Response**:

```
200 OK
{
  "url": "http://minio:9000/rh-bucket/funcionario_documents/bi_1746123456.pdf?X-Amz-...",
  "expiresIn": 3600
}

404 Not Found — documentoId não existe, não pertence ao funcionário, ou is_active=false
```

---

## DELETE /{documentoId} — Soft delete

**Response**:

```
200 OK
{ "message": "Documento desactivado com sucesso" }

404 Not Found — documentoId não existe ou não pertence ao funcionário
```
