# API Contract: Funcionários

**Base path**: `api/v1/rh/funcionarios`

## POST /funcionarios — Criar funcionário

**Request body**:
```json
{
  "nomeCompleto": "string (max 200, required)",
  "dataNascimento": "date (YYYY-MM-DD, required)",
  "genero": "string (Option ckey, required)",
  "estadoCivil": "string (Option ckey, required)",
  "nif": "string (9 dígitos, required)",
  "biNumero": "string (required)",
  "biValidade": "date (YYYY-MM-DD, optional)",
  "nacionalidade": "string (Option ckey, default 'CV')",
  "email": "string (optional, unique)",
  "telefone": "string (optional)",
  "morada": "string (optional)",
  "fotoUrl": "string (optional)",
  "situacaoProfissional": "string (Option ckey, required)",
  "dataAdmissao": "date (YYYY-MM-DD, required)",
  "dataSaida": "date (YYYY-MM-DD, optional)"
}
```

**Responses**:
- `201 Created`: `{ "id": "uuid", "numeroFuncionario": "F000001", "message": "Criado com sucesso" }`
- `409 Conflict`: NIF ou BI já existem
- `400 Bad Request`: campos obrigatórios em falta

---

## GET /funcionarios — Listar funcionários

**Query params**: `nome` (ILIKE), `nif`, `situacaoProfissional`, `unidadeOrganicaId` (UUID), `careerId` (UUID), `active` (boolean, default true), `pagina` (default 0), `tamanho` (default 20)

**Response 200**:
```json
{
  "content": [
    {
      "id": "uuid",
      "numeroFuncionario": "F000001",
      "nomeCompleto": "string",
      "nif": "string",
      "situacaoProfissional": "string",
      "dataAdmissao": "date",
      "isActive": true
    }
  ],
  "totalElements": 0
}
```

---

## GET /funcionarios/{id} — Obter funcionário por ID

**Response 200**: Objecto completo com todos os campos.
**Response 404**: Funcionário não encontrado.

---

## PUT /funcionarios/{id} — Actualizar funcionário

**Request body**: Mesmos campos do POST excepto `numeroFuncionario` (imutável, ignorado se enviado).
`is_active` é derivado automaticamente de `situacaoProfissional`.

**Response 200**: Objecto actualizado.
**Response 409**: NIF ou BI já existe noutro funcionário.
**Response 404**: Funcionário não encontrado.

---

## GET /funcionarios/{id}/enquadramento — Enquadramento actual

Ver contracts/enquadramentos.md

## GET /funcionarios/{id}/enquadramentos/historico — Histórico de enquadramentos

Ver contracts/enquadramentos.md

## GET /funcionarios/{id}/contratos — Contratos

Ver contracts/contratos.md

## GET /funcionarios/{id}/dependentes — Dependentes

Ver contracts/dependentes.md

## GET /funcionarios/{id}/qualificacoes — Qualificações

Ver contracts/qualificacoes.md
