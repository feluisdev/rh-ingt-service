# API Contract: Dependentes e Qualificações

## Dependentes

**Base path**: `api/v1/rh/dependentes` e sub-recurso `api/v1/rh/funcionarios/{id}/dependentes`

### POST /dependentes — Criar dependente

```json
{
  "funcionarioId": "uuid (required)",
  "nome": "string (max 150, required)",
  "parentesco": "string (Option ckey, required)",
  "dataNascimento": "date (optional)",
  "nif": "string (optional)"
}
```

**Responses**: `201 Created` | `404 Not Found` (funcionário)

### GET /funcionarios/{id}/dependentes — Listar dependentes activos

**Response 200**: lista de dependentes com `is_active = true` por omissão. Parâmetro `active=false` inclui inactivos.

### PUT /dependentes/{id} — Actualizar dependente

Campos: `nome`, `parentesco`, `dataNascimento`, `nif`. `funcionarioId` imutável.

**Response 200**: objecto actualizado. **Response 404**: não encontrado.

### DELETE /dependentes/{id} — Desactivar dependente (soft delete)

**Response 200**: `{ "message": "Desactivado com sucesso" }`

### PUT /dependentes/{id}/activate — Reactivar dependente

**Response 200**: `{ "message": "Activado com sucesso" }`

---

## Qualificações

**Base path**: `api/v1/rh/qualificacoes` e sub-recurso `api/v1/rh/funcionarios/{id}/qualificacoes`

### POST /qualificacoes — Criar qualificação

```json
{
  "funcionarioId": "uuid (required)",
  "nivelAcademico": "string (Option ckey, required)",
  "curso": "string (max 200, required)",
  "instituicao": "string (max 200, optional)",
  "anoConclusao": "integer (optional)",
  "pais": "string (Option ckey, default 'CV')"
}
```

**Responses**: `201 Created` | `404 Not Found` (funcionário)

### GET /funcionarios/{id}/qualificacoes — Listar qualificações activas

**Response 200**: lista com `is_active = true` por omissão.

### PUT /qualificacoes/{id} — Actualizar qualificação

Todos os campos excepto `funcionarioId`.

**Response 200**: objecto actualizado.

### DELETE /qualificacoes/{id} — Desactivar qualificação (soft delete)

**Response 200**: `{ "message": "Desactivado com sucesso" }`

### PUT /qualificacoes/{id}/activate — Reactivar qualificação

**Response 200**: `{ "message": "Activado com sucesso" }`
