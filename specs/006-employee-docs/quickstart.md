# Quickstart: Documentos de Funcionários

**Pré-requisitos**: Aplicação a correr em `http://localhost:8091`. Funcionário existente com ID conhecido. MinIO acessível. Tipo de documento activo com extensões `pdf,jpg,png`.

---

## C1 — Upload e Listagem (US1 + US2)

### C1-S1: Upload de BI em PDF

```http
POST /api/v1/rh/funcionarios/{funcionarioId}/documentos
Content-Type: multipart/form-data

file=<ficheiro.pdf>
documentTypeId=<uuid-tipo-bi>
description=Bilhete de Identidade emitido em 2024
```
Esperado: `201 Created` com `id`, `fileKey`, `originalFilename`, `fileSize`.

### C1-S2: Tentar upload com extensão não permitida

```http
POST /api/v1/rh/funcionarios/{funcionarioId}/documentos
Content-Type: multipart/form-data

file=<ficheiro.docx>
documentTypeId=<uuid-tipo-bi>
```
Esperado: `400 Bad Request` — extensão `.docx` não está em `allowedExtensions`.

### C1-S3: Tentar upload com ficheiro > 10 MB

Esperado: `400 Bad Request`.

### C1-S4: Listar documentos do funcionário

```http
GET /api/v1/rh/funcionarios/{funcionarioId}/documentos
```
Esperado: `200 OK` com `totalElements >= 1` e documento do C1-S1 presente.

### C1-S5: Filtrar por tipo de documento

```http
GET /api/v1/rh/funcionarios/{funcionarioId}/documentos?documentTypeId=<uuid-tipo-bi>
```
Esperado: apenas documentos do tipo BI.

### C1-S6: Funcionário inexistente

```http
POST /api/v1/rh/funcionarios/00000000-0000-0000-0000-000000000000/documentos
```
Esperado: `404 Not Found`.

---

## C2 — Download (US3)

### C2-S1: Obter URL pré-assinada

```http
GET /api/v1/rh/funcionarios/{funcionarioId}/documentos/{documentoId}/download
```
Esperado: `200 OK` com `{ "url": "http://...", "expiresIn": 3600 }`. A URL deve ser acessível directamente no browser.

### C2-S2: Download de documento inactivo

Após soft delete (ver C3-S1):
```http
GET /api/v1/rh/funcionarios/{funcionarioId}/documentos/{documentoId}/download
```
Esperado: `404 Not Found`.

---

## C3 — Soft Delete (US4)

### C3-S1: Desactivar documento

```http
DELETE /api/v1/rh/funcionarios/{funcionarioId}/documentos/{documentoId}
```
Esperado: `200 OK`.

### C3-S2: Verificar que não aparece na listagem activa

```http
GET /api/v1/rh/funcionarios/{funcionarioId}/documentos?active=true
```
Esperado: documento removido não aparece.

### C3-S3: Desactivar novamente (idempotente)

```http
DELETE /api/v1/rh/funcionarios/{funcionarioId}/documentos/{documentoId}
```
Esperado: `200 OK` (idempotente).

### C3-S4: Consultar metadados após soft delete

```http
GET /api/v1/rh/funcionarios/{funcionarioId}/documentos/{documentoId}
```
Esperado: `404 Not Found` (documento inactivo não é devolvido).
