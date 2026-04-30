# API Documentation

This document provides documentation for the API endpoints of the RH Service project.

## Funcionario

The `Funcionario` endpoints are used to manage employees in the system.

### Create Funcionario

Creates a new funcionario.

- **Method:** `POST`
- **Path:** `/api/v1/funcionarios`
- **Request Body:** `FuncionarioRequestDTO`

**Example Request:**

```json
{
  "nome": "João da Silva",
  "email": "joao.silva@example.com",
  "numSegurado": "123456789",
  "nif": "987654321",
  "dataNascimento": "1990-01-01",
  "sexo": "MASCULINO",
  "estadoCivil": "CASADO",
  "nomePai": "Pai do João",
  "nomeMae": "Mãe do João",
  "habilitacoesLiterarias": "Licenciatura",
  "telefone": "912345678",
  "endereco": "Rua Exemplo, 123",
  "dataAdmissao": "2023-01-01",
  "iban": "PT50000700000001234567891"
}
```

### Get Funcionarios

Gets a list of funcionarios with optional filters.

- **Method:** `GET`
- **Path:** `/api/v1/funcionarios`
- **Query Parameters:**
  - `nome` (string, optional): Filter by name.
  - `email` (string, optional): Filter by email.
  - `numSegurado` (string, optional): Filter by social security number.
  - `nif` (string, optional): Filter by tax identification number.
  - `pagina` (string, optional, default: "0"): Page number.
  - `tamanho` (string, optional, default: "20"): Page size.

### Get Funcionario By Id

Gets a funcionario by their ID.

- **Method:** `GET`
- **Path:** `/api/v1/funcionarios/{funcionarioId}`

### Update Funcionario

Updates a funcionario.

- **Method:** `PUT`
- **Path:** `/api/v1/funcionarios/{funcionarioId}`
- **Request Body:** `FuncionarioRequestDTO`

**Example Request:**

```json
{
  "nome": "João da Silva",
  "email": "joao.silva@example.com",
  "numSegurado": "123456789",
  "nif": "987654321",
  "dataNascimento": "1990-01-01",
  "sexo": "MASCULINO",
  "estadoCivil": "CASADO",
  "nomePai": "Pai do João",
  "nomeMae": "Mãe do João",
  "habilitacoesLiterarias": "Licenciatura",
  "telefone": "912345678",
  "endereco": "Rua Exemplo, 123",
  "dataAdmissao": "2023-01-01",
  "iban": "PT50000700000001234567891"
}
```

### Inactivate Funcionario

Inactivates a funcionario.

- **Method:** `DELETE`
- **Path:** `/api/v1/funcionarios/{funcionarioId}`

### Activate Funcionario

Activates a funcionario.

- **Method:** `PATCH`
- **Path:** `/api/v1/funcionarios/{funcionarioId}`

### Get Funcionario Details

Gets detailed information about a funcionario.

- **Method:** `GET`
- **Path:** `/api/v1/funcionarios/{funcionarioId}/detalhes`

## Cargo

The `Cargo` endpoints are used to manage job positions in the system.

### Get Cargos

Gets a list of cargos with optional filters.

- **Method:** `GET`
- **Path:** `/api/v1/cargos`
- **Query Parameters:**
  - `nome` (string, optional): Filter by name.
  - `codigo` (string, optional): Filter by code.
  - `nivelHierarquico` (integer, optional): Filter by hierarchical level.
  - `salarioBaseMax` (integer, optional): Filter by maximum base salary.
  - `salarioBaseMin` (integer, optional): Filter by minimum base salary.
  - `estado` (string, optional): Filter by state.
  - `pagina` (string, optional, default: "0"): Page number.
  - `tamanho` (string, optional, default: "20"): Page size.

### Activate Cargo

Activates a cargo.

- **Method:** `PATCH`
- **Path:** `/api/v1/cargos/{cargoId}/ativar`

### Deactivate Cargo

Deactivates a cargo.

- **Method:** `PATCH`
- **Path:** `/api/v1/cargos/{cargoId}/desativar`

### Get Cargo By Id

Gets a cargo by its ID.

- **Method:** `GET`
- **Path:** `/api/v1/cargos/{cargoId}`

### Create Cargo

Creates a new cargo.

- **Method:** `POST`
- **Path:** `/api/v1/cargos`
- **Request Body:** `CargoRequestDTO`

**Example Request:**

```json
{
  "nome": "Desenvolvedor de Software",
  "codigo": "DS01",
  "nivelHierarquico": 3,
  "salarioBaseMin": 3000,
  "salarioBaseMax": 5000,
  "descricao": "Desenvolvimento e manutenção de software."
}
```

### Update Cargo

Updates a cargo.

- **Method:** `PUT`
- **Path:** `/api/v1/cargos/{cargoId}`
- **Request Body:** `CargoRequestDTO`

**Example Request:**

```json
{
  "nome": "Desenvolvedor de Software Sênior",
  "codigo": "DS02",
  "nivelHierarquico": 4,
  "salarioBaseMin": 4000,
  "salarioBaseMax": 6000,
  "descricao": "Desenvolvimento e manutenção de software, com foco em arquitetura."
}
```

## Contrato

The `Contrato` endpoints are used to manage employee contracts.

### Get Contrato

Gets a contract by its ID.

- **Method:** `GET`
- **Path:** `/api/v1/funcionarios/contratos/{contratoId}`

### Get Contratos

Gets a list of contracts for a given employee.

- **Method:** `GET`
- **Path:** `/api/v1/funcionarios/{funcionarioId}/contratos`

### Create Contrato

Creates a new contract for a given employee.

- **Method:** `POST`
- **Path:** `/api/v1/funcionarios/{funcionarioId}/contratos`
- **Request Body:** `ContratoRequestDTO`

**Example Request:**

```json
{
  "cargoId": "c1g2h3i4-j5k6-l7m8-n9o0-p1q2r3s4t5u6",
  "departamentoId": "d1e2f3g4-h5i6-j7k8-l9m0-n1o2p3q4r5s6",
  "tipoContrato": "CLT",
  "dataInicio": "2023-01-01",
  "dataFim": "2024-01-01",
  "salario": 5000,
  "observacoes": "Contrato de um ano."
}
```

### Update Contrato

Updates a contract.

- **Method:** `PUT`
- **Path:** `/api/v1/funcionarios/contratos/{contratoId}`
- **Request Body:** `ContratoRequestDTO`

**Example Request:**

```json
{
  "cargoId": "c1g2h3i4-j5k6-l7m8-n9o0-p1q2r3s4t5u6",
  "departamentoId": "d1e2f3g4-h5i6-j7k8-l9m0-n1o2p3q4r5s6",
  "tipoContrato": "CLT",
  "dataInicio": "2023-01-01",
  "dataFim": "2025-01-01",
  "salario": 5500,
  "observacoes": "Contrato estendido por mais um ano."
}
```

### Inactivate Contrato

Inactivates a contract.

- **Method:** `PATCH`
- **Path:** `/api/v1/funcionarios/{contratoId}/desativar`

### Activate Contrato

Activates a contract.

- **Method:** `PATCH`
- **Path:** `/api/v1/funcionarios/contratos/{contratoId}/ativar`

## Departamento

The `Departamento` endpoints are used to manage departments in the system.

### Get Departamentos

Gets a list of departments with optional filters.

- **Method:** `GET`
- **Path:** `/api/v1/departamentos`
- **Query Parameters:**
    - `tamanho` (string, optional, default: "20"): Page size.
    - `pagina` (string, optional, default: "0"): Page number.
    - `nome` (string, optional): Filter by name.
    - `localizacao` (string, optional): Filter by location.
    - `codigo` (string, optional): Filter by code.
    - `responsavelId` (string, optional): Filter by responsible person's ID.
    - `estado` (string, optional): Filter by state.

### Get Departamento By Id

Gets a department by its ID.

- **Method:** `GET`
- **Path:** `/api/v1/departamentos/{departamentoId}`

### Activate Departamento

Activates a department.

- **Method:** `PATCH`
- **Path:** `/api/v1/departamentos/{departamentoId}/ativar`

### Deactivate Departamento

Deactivates a department.

- **Method:** `PATCH`
- **Path:** `/api/v1/departamentos/{departamentoId}/desativar`

### Create Departamento

Creates a new department.

- **Method:** `POST`
- **Path:** `/api/v1/departamentos`
- **Request Body:** `DepartamentoRequestDTO`

**Example Request:**

```json
{
    "nome": "Departamento de TI",
    "localizacao": "Edifício Principal, 3º Andar",
    "codigo": "TI01",
    "responsavelId": "f1g2h3i4-j5k6-l7m8-n9o0-p1q2r3s4t5u6"
}
```

## Dependente

The `Dependente` endpoints are used to manage employee dependents.

### Create Dependente

Creates a new dependent for a given employee.

- **Method:** `POST`
- **Path:** `/api/v1/funcionarios/{funcionarioId}/dependentes`
- **Request Body:** `DependenteRequestDTO`

**Example Request:**

```json
{
    "nome": "Maria da Silva",
    "dataNascimento": "2010-05-10",
    "grauParentesco": "FILHO",
    "sexo": "FEMININO"
}
```

### Get Dependentes By Funcionario

Gets a list of dependents for a given employee.

- **Method:** `GET`
- **Path:** `/api/v1/funcionarios/{funcionarioId}/dependentes`

### Get Dependente By Id

Gets a dependent by their ID.

- **Method:** `GET`
- **Path:** `/api/v1/funcionarios/dependentes/{dependenteId}`

### Update Dependente

Updates a dependent.

- **Method:** `PUT`
- **Path:** `/api/v1/funcionarios/dependentes/{dependenteId}`
- **Request Body:** `DependenteRequestDTO`

**Example Request:**

```json
{
    "nome": "Maria da Silva Souza",
    "dataNascimento": "2010-05-10",
    "grauParentesco": "FILHO",
    "sexo": "FEMININO"
}
```

### Inactivate Dependente

Inactivates a dependent.

- **Method:** `DELETE`
- **Path:** `/api/v1/funcionarios/dependentes/{dependenteId}`

### Activate Dependente

Activates a dependent.

- **Method:** `PATCH`
- **Path:** `/api/v1/funcionarios/dependentes/{dependenteId}`

## Documento

The `Documento` endpoints are used to manage documents.

### Get Documentos

Gets a list of documents with optional filters.

- **Method:** `GET`
- **Path:** `/api/v1/Documento`
- **Query Parameters:**
    - `documentoId` (string, optional): Filter by document ID.
    - `idTipoDocumento` (string, optional): Filter by document type ID.
    - `estado` (string, optional): Filter by state.
    - `pagina` (string, optional, default: "0"): Page number.
    - `tamanho` (string, optional, default: "20"): Page size.

### Creating and Updating Documents

Creating and updating documents is done through the `Funcionario` endpoint, by including an `anexo` object in the request body.

**Note:** The `anexo` object is a list of documents.

**To create a new document**, include the following information in the `anexo` object:

```json
"anexo": [
    {
        "idTipodocumento": "4b086b5e-ca69-4e80-a67d-875bb6ed71cc",
        "url": "url",
        "observacao": "obs"
    }
]
```

**To update an existing document**, include the `documentoId` in the `anexo` object:

```json
"anexo": [
    {
        "documentoId": "d347e5bb-df85-424e-904b-8b917a5ff0d1",
        "idTipodocumento": "4b086b5e-ca69-4e80-a67d-875bb6ed71cc",
        "url": "url",
        "observacao": "obs"
    }
]
```

## Qualificacao

The `Qualificacao` endpoints are used to manage employee qualifications.

### Create Qualificacao

Creates a new qualification for a given employee.

- **Method:** `POST`
- **Path:** `/api/v1/funcionarios/{funcionarioId}/qualificacoes`
- **Request Body:** `QualificacaoRequestDTO`

**Example Request:**

```json
{
    "nome": "Certificação Java",
    "tipo": "Certificação",
    "instituicao": "Oracle",
    "dataInicio": "2022-01-01",
    "dataFim": "2022-12-31",
    "nivel": "Avançado"
}
```

### Get Qualificacoes Funcionario

Gets a list of qualifications for a given employee.

- **Method:** `GET`
- **Path:** `/api/v1/funcionarios/{funcionarioId}/qualificacoes`

### Get Qualificacao

Gets a qualification by its ID.

- **Method:** `GET`
- **Path:** `/api/v1/funcionarios/qualificacoes/{qualificacaoId}`

### Update Qualificacao

Updates a qualification.

- **Method:** `PUT`
- **Path:** `/api/v1/funcionarios/qualificacoes/{qualificacaoId}`
- **Request Body:** `QualificacaoRequestDTO`

**Example Request:**

```json
{
    "nome": "Certificação Java 11",
    "tipo": "Certificação",
    "instituicao": "Oracle",
    "dataInicio": "2022-01-01",
    "dataFim": "2022-12-31",
    "nivel": "Especialista"
}
```

### Inactivate Qualificacao

Inactivates a qualification.

- **Method:** `DELETE`
- **Path:** `/api/v1/funcionarios/qualificacoes/{qualificacaoId}`

### Activate Qualificacao

Activates a qualification.

- **Method:** `PATCH`
- **Path:** `/api/v1/funcionarios/qualificacoes/{qualificacaoId}`

## TipoDocumento

The `TipoDocumento` endpoints are used to manage document types.

### Get TipoDocumento

Gets a list of document types with optional filters.

- **Method:** `GET`
- **Path:** `/api/v1/tipoDocumentos`
- **Query Parameters:**
    - `codigo` (string, optional): Filter by code.
    - `descricao` (string, optional): Filter by description.
    - `estado` (string, optional): Filter by state.
    - `pagina` (string, optional, default: "0"): Page number.
    - `tamanho` (string, optional, default: "20"): Page size.

### Inactivate TipoDocumento

Inactivates a document type.

- **Method:** `DELETE`
- **Path:** `/api/v1/tipoDocumentos/{TipoDocumentoId}`

### Get TipoDocumento By Id

Gets a document type by its ID.

- **Method:** `GET`
- **Path:** `/api/v1/tipoDocumentos/{tipoDocumentoId}`

### Update TipoDocumento

Updates a document type.

- **Method:** `PUT`
- **Path:** `/api/v1/tipoDocumentos/{tipoDocumentoId}`
- **Request Body:** `TipoDocumentoRequestDTO`

**Example Request:**

```json
{
    "codigo": "RG",
    "descricao": "Registro Geral"
}
```

### Create TipoDocumento

Creates a new document type.

- **Method:** `POST`
- **Path:** `/api/v1/tipoDocumentos`
- **Request Body:** `TipoDocumentoRequestDTO`

**Example Request:**

```json
{
    "codigo": "CPF",
    "descricao": "Cadastro de Pessoa Física"
}
```

---

## Parametrizações

### Reference Options (Etiquetas)

| Método | Path | Descrição |
|--------|------|-----------|
| `GET` | `api/v1/rh/catalogs/reference/options` | Listar opções (filtros: ccode, locale, active, ckey) |
| `GET` | `api/v1/rh/catalogs/reference/options/{id}` | Obter opção por ID |
| `GET` | `api/v1/rh/catalogs/reference/options/by-ccode` | Listar por ccode + locale (com fallback pt-CV) |
| `POST` | `api/v1/rh/catalogs/reference/options` | Criar opção |
| `PUT` | `api/v1/rh/catalogs/reference/options/{id}` | Actualizar opção |
| `DELETE` | `api/v1/rh/catalogs/reference/options/{id}` | Desativar opção |
| `POST` | `api/v1/rh/catalogs/reference/options/{id}/activate` | Reativar opção |

### Worker States (Estados do Trabalhador)

| Método | Path | Descrição |
|--------|------|-----------|
| `GET` | `api/v1/rh/catalogs/worker-states` | Listar estados (filtros: code, isActive) |
| `GET` | `api/v1/rh/catalogs/worker-states/{id}` | Obter estado por ID |
| `POST` | `api/v1/rh/catalogs/worker-states` | Criar estado |
| `PUT` | `api/v1/rh/catalogs/worker-states/{id}` | Actualizar estado |
| `DELETE` | `api/v1/rh/catalogs/worker-states/{id}` | Desativar estado (bloqueia se isCore=true) |
| `PATCH` | `api/v1/rh/catalogs/worker-states/{id}/activate` | Reativar estado |

### Professional Situations (Situações Profissionais)

| Método | Path | Descrição |
|--------|------|-----------|
| `GET` | `api/v1/rh/catalogs/professional-situations` | Listar situações (filtros: code, isActive) |
| `GET` | `api/v1/rh/catalogs/professional-situations/{id}` | Obter situação por ID |
| `POST` | `api/v1/rh/catalogs/professional-situations` | Criar situação |
| `PUT` | `api/v1/rh/catalogs/professional-situations/{id}` | Actualizar situação |
| `DELETE` | `api/v1/rh/catalogs/professional-situations/{id}` | Desativar situação |
| `PATCH` | `api/v1/rh/catalogs/professional-situations/{id}/activate` | Reativar situação |

### Contract Types (Tipos de Contrato)

| Método | Path | Descrição |
|--------|------|-----------|
| `GET` | `api/v1/rh/catalogs/contract-types` | Listar tipos (filtros: code, isActive) |
| `GET` | `api/v1/rh/catalogs/contract-types/{id}` | Obter tipo por ID |
| `POST` | `api/v1/rh/catalogs/contract-types` | Criar tipo |
| `PUT` | `api/v1/rh/catalogs/contract-types/{id}` | Actualizar tipo |
| `DELETE` | `api/v1/rh/catalogs/contract-types/{id}` | Desativar tipo |
| `PATCH` | `api/v1/rh/catalogs/contract-types/{id}/activate` | Reativar tipo |

### Document Types (Tipos de Documento)

| Método | Path | Descrição |
|--------|------|-----------|
| `GET` | `api/v1/rh/catalogs/document-types` | Listar tipos (filtros: code, isActive) |
| `GET` | `api/v1/rh/catalogs/document-types/{id}` | Obter tipo por ID |
| `POST` | `api/v1/rh/catalogs/document-types` | Criar tipo (categoryOptionId, allowedExtensions) |
| `PUT` | `api/v1/rh/catalogs/document-types/{id}` | Actualizar tipo |
| `DELETE` | `api/v1/rh/catalogs/document-types/{id}` | Desativar tipo |
| `PATCH` | `api/v1/rh/catalogs/document-types/{id}/activate` | Reativar tipo |

### Leave Types (Tipos de Licença)

| Método | Path | Descrição |
|--------|------|-----------|
| `GET` | `api/v1/rh/catalogs/leave-types` | Listar tipos (filtros: code, isActive) |
| `GET` | `api/v1/rh/catalogs/leave-types/{id}` | Obter tipo por ID |
| `POST` | `api/v1/rh/catalogs/leave-types` | Criar tipo (deductsBalance, requiresApproval, maxDaysPerYear) |
| `PUT` | `api/v1/rh/catalogs/leave-types/{id}` | Actualizar tipo |
| `DELETE` | `api/v1/rh/catalogs/leave-types/{id}` | Desativar tipo |
| `PATCH` | `api/v1/rh/catalogs/leave-types/{id}/activate` | Reativar tipo |

### Leave & Mobility Subtypes (Subtipos de Licença/Mobilidade)

| Método | Path | Descrição |
|--------|------|-----------|
| `GET` | `api/v1/rh/catalogs/leave-mobility-subtypes` | Listar subtipos (filtros: code, isActive) |
| `GET` | `api/v1/rh/catalogs/leave-mobility-subtypes/{id}` | Obter subtipo por ID |
| `POST` | `api/v1/rh/catalogs/leave-mobility-subtypes` | Criar subtipo (recordType ∈ {LICENCA, MOBILIDADE, AMBOS}) |
| `PUT` | `api/v1/rh/catalogs/leave-mobility-subtypes/{id}` | Actualizar subtipo |
| `DELETE` | `api/v1/rh/catalogs/leave-mobility-subtypes/{id}` | Desativar subtipo |
| `PATCH` | `api/v1/rh/catalogs/leave-mobility-subtypes/{id}/activate` | Reativar subtipo |

### Public Holidays (Feriados)

| Método | Path | Descrição |
|--------|------|-----------|
| `GET` | `api/v1/rh/catalogs/public-holidays` | Listar feriados (filtros: year, isNational, dateFrom, dateTo, isActive) |
| `GET` | `api/v1/rh/catalogs/public-holidays/{id}` | Obter feriado por ID |
| `POST` | `api/v1/rh/catalogs/public-holidays` | Criar feriado (holidayDate ISO-8601, isNational) |
| `PUT` | `api/v1/rh/catalogs/public-holidays/{id}` | Actualizar feriado |
| `DELETE` | `api/v1/rh/catalogs/public-holidays/{id}` | Desativar feriado |
| `PATCH` | `api/v1/rh/catalogs/public-holidays/{id}/activate` | Reativar feriado |
