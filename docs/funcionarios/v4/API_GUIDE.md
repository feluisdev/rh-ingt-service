# Guia de Utilização da API — Módulo de Recursos Humanos (SIPPROG)

> Versão 4.8 · Junho 2026  
> Sistema de Informação do Pessoal e Progressões — INGT, Cabo Verde

---

## Índice

1. [Conceitos Essenciais](#1-conceitos-essenciais)
2. [Autenticação](#2-autenticação)
3. [Convenções](#3-convenções)
4. [Ordem de Criação — Visão Geral](#4-ordem-de-criação--visão-geral)
5. [Fase 1 — Parametrizações (pré-requisitos)](#5-fase-1--parametrizações-pré-requisitos)
6. [Fase 2 — Estrutura Organizacional](#6-fase-2--estrutura-organizacional)
7. [Fase 3 — Carreiras e Progressão](#7-fase-3--carreiras-e-progressão)
8. [Fase 4 — Funcionário e Dossier](#8-fase-4--funcionário-e-dossier)
9. [Fluxos Operacionais](#9-fluxos-operacionais)
10. [Área Reservada do Colaborador (`/me`)](#10-área-reservada-do-colaborador-me)
11. [API de Documentos / Ficheiros](#11-api-de-documentos--ficheiros)
12. [Endpoints Combobox (Dropdowns)](#12-endpoints-combobox-dropdowns)
13. [Estruturas de Resposta (Response DTOs)](#13-estruturas-de-resposta-response-dtos)
14. [Referência Rápida de Endpoints](#14-referência-rápida-de-endpoints)
15. [Erros e Códigos HTTP](#15-erros-e-códigos-http)

---

## 1. Conceitos Essenciais

### Porquê existe uma ordem de criação?

A API foi desenhada com integridade referencial estrita. Vários recursos só podem ser criados se outros já existirem. Por exemplo:

- Um **Funcionário** exige um **Estado do Trabalhador**.
- Um **Contrato** exige um **Tipo de Contrato** (que referencia um **Vínculo Laboral**) e um **Funcionário**.
- Um **Enquadramento Profissional** exige uma **Carreira**, uma **Categoria** (que pertence à carreira), um **Escalão** (que pertence à categoria) e um **Funcionário**.

A hierarquia de dependências tem três camadas:

```
Camada 0 — OptionEntity (lookups genéricos sem lógica)
Camada 1 — Parametrizações (catálogos com comportamento)
Camada 2 — Estrutura Organizacional + Carreiras
Camada 3 — Funcionário
Camada 4 — Sub-recursos do Funcionário (contratos, enquadramento, colocações, dossier, ausências)
```

### Dois tipos de catálogo

| Tipo | Endpoint base | Quando usar |
|---|---|---|
| **OptionEntity** (lookups simples) | `GET api/v1/rh/reference/options?ccode=...` | Sexo, estado civil, nacionalidade, ilha, tipo de unidade, nível de habilitação, parentesco, etc. |
| **Tabelas dedicadas** | `api/v1/rh/catalogs/worker-states`, `api/v1/rh/catalogs/contract-types`, etc. | Catálogos com flags que alteram o comportamento do sistema (ex: `deducts_balance`, `is_core`). |

### Soft Delete

Nenhum recurso é apagado fisicamente. `DELETE` marca o registo como inativo (`is_active = false`). Para reativar, usa-se `PATCH /{id}/activate` ou `POST /{id}/activate` conforme o endpoint.

---

## 2. Autenticação

Todas as chamadas requerem token JWT emitido pelo Keycloak:

```http
Authorization: Bearer {token}
```

### Perfis de Acesso

| Perfil | Permissões |
|---|---|
| `ROLE_FUNCIONARIO` | Leitura da área reservada `/me` apenas. |
| `ROLE_CHEFIA` | Aprovação de pedidos de ausência e licenças da sua equipa. |
| `ROLE_HR_OPERATOR` | Operações correntes: funcionários, ausências, enquadramentos. |
| `ROLE_HR_ADMIN` | Operações plenas: parametrizações, estrutura, carreiras, dossier. |
| `ROLE_SYSTEM_ADMIN` | Configurações de sistema, auditoria, integrações. |

> Em ambiente `development` / `staging` a segurança está desativada — todas as chamadas são aceites sem token.

---

## 3. Convenções

| Aspecto | Detalhe |
|---|---|
| **Base URL** | `http://localhost:8091` (local) |
| **Prefixo de API** | Todos os endpoints têm o prefixo `api/v1/rh/` |
| **Formato de data** | ISO-8601: `YYYY-MM-DD` para datas; `YYYY-MM-DDTHH:mm:ss` para timestamps UTC |
| **Paginação** | `pagina` (0-based, default 0) e `tamanho` (default 20) |
| **Ordenação** | `sort=campo,asc` ou `sort=campo,desc` |
| **IDs** | UUID (`xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`) |
| **Content-Type** | `application/json` (exceto upload de ficheiros: `multipart/form-data`) |

**Exemplo de chamada completa (local):**
```
GET http://localhost:8091/api/v1/rh/funcionarios
```

### Estrutura de resposta de lista

```json
{
  "content": [ ... ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 132,
  "totalPages": 7
}
```

### Estrutura de resposta de erro

```json
{
  "timestamp": "2026-05-08T10:00:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Campo obrigatório em falta.",
  "path": "/api/v1/rh/funcionarios",
  "fields": [
    { "field": "nif", "message": "O NIF é obrigatório." }
  ]
}
```

---

## 4. Ordem de Criação — Visão Geral

O diagrama abaixo representa as dependências entre recursos. Uma seta `A → B` significa "B depende de A existir primeiro".

```
OptionEntity ──────────────────────────────────────────────────────────┐
                                                                        │
WorkerState ──────────────────────────────────────────────────────────► Funcionário
                                                                        │
VinculoLaboral ──────────────────────────────────────────────────────► ContractType
ContractType ────────────────────────────────────────────────────────► Contrato
DocumentType ────────────────────────────────────────────────────────► Documento
LeaveType ───────────────────────────────────────────────────────────► PedidoAusência
LeaveMobilitySubtype ────────────────────────────────────────────────► Licença/Mobilidade
PublicHoliday ───────────────────────────────────────────────────────► (cálculo de dias úteis)
                                                                        │
OrganizationalUnit ──────────────────────────────────────────────────► Colocação
Job (Cargo) ─────────────────────────────────────────────────────────► Enquadramento
Function (Função) ───────────────────────────────────────────────────► Enquadramento
                                                                        │
Career ──────────────────────────────────────────────────────────────► Enquadramento
  └── Category ────────────────────────────────────────────────────► Enquadramento
        └── Grade ─────────────────────────────────────────────────► Enquadramento
```

**Resumo da ordem obrigatória:**

```
1. OptionEntity (sexo, estado civil, etc.)
2. WorkerStates + VinculosLaborais + ContractTypes + DocumentTypes
   + LeaveTypes + LeaveMobilitySubtypes + PublicHolidays
3. OrganizationalUnits + Jobs + Functions
4. Careers → Categories → Grades
5. Funcionário
6. Dados Bancários (do funcionário)
7. Contrato (do funcionário)
8. Enquadramento Profissional (do funcionário)
9. Colocação em Unidade Orgânica (do funcionário)
10. Dossier: Dependentes, Qualificações, Formações, Processos Disciplinares
11. Documentos, Pedidos de Ausência, Licenças/Mobilidades, Recibos
```

---

## 5. Fase 1 — Parametrizações (pré-requisitos)

### 5.1 OptionEntity — Lookups Genéricos

Antes de qualquer outra coisa, verifique se os grupos de opções base estão populados. Na maioria dos ambientes são populados por seed automático (Flyway `afterMigrate`).

**Consultar opções de um grupo:**

```http
GET api/v1/rh/reference/options?ccode=MARITAL_STATUS
```

**Grupos disponíveis e os seus `ckey` comuns:**

| `ccode` | Descrição | `ckey` típicos |
|---|---|---|
| `SEX` | Sexo | `M`, `F` |
| `MARITAL_STATUS` | Estado Civil | `SOLTEIRO`, `CASADO`, `UNIAO_FACTO`, `DIVORCIADO`, `VIUVO` |
| `NATIONALITY` | Nacionalidade | `CV`, `PT`, `SN`, `BR` |
| `UNIT_TYPE` | Tipo de Unidade Orgânica | `DIRECAO`, `DEPARTAMENTO`, `DIVISAO`, `SECCAO` |
| `QUALIFICATION_LEVEL` | Nível de Habilitação | `BASICO`, `SECUNDARIO`, `LICENCIATURA`, `MESTRADO`, `DOUTORAMENTO` |
| `RELATIONSHIP_TYPE` | Parentesco | `CONJUGE`, `FILHO`, `PAI`, `MAE`, `IRMAO` |
| `TRAINING_TYPE` | Tipo de Formação | `CURSO`, `SEMINARIO`, `WORKSHOP`, `CONGRESSO`, `CONFERENCIA`, `ELEARNING` |
| `DOC_CATEGORY` | Categoria de Documento | `IDENTIFICACAO`, `CONTRATO`, `FORMACAO`, `DISCIPLINAR`, `MEDICO`, `FINANCEIRO`, `OUTRO` |
| `LEAVE_CATEGORY` | Categoria de Ausência | `GOZAMENTO`, `SAUDE`, `FAMILIAR`, `PESSOAL`, `LEGAL` |
| `ISLAND` | Ilha | `SANTIAGO`, `SAL`, `SAO_VICENTE`, `FOGO`, `BOA_VISTA` |
| `CONCELHO` | Concelho | `PRAIA`, `SANTA_CATARINA`, `MINDELO`, `SAO_DOMINGOS` |
| `CAREER_REGIME` | Regime da Carreira (PCFR) | `GERAL`, `ESPECIAL` |
| `BANCO` | Banco | `BCA`, `BCN`, `CECV`, `CAIXA` |
| `WORK_REGIME` | Regime de Trabalho | `TEMPO_COMPLETO`, `TEMPO_PARCIAL`, `ISENCAO_HORARIO`, `DEDICACAO_EXCLUSIVA` |
| `WORKER_STATE_REASON` | Motivo de Mudança de Estado | `DISCIPLINARY_SUSPENSION`, `MEDICAL_SUSPENSION`, `AGE_RETIREMENT`, `CONTRACT_TERMINATION`, etc. |
| `RECORD_TYPE` | Tipo de Registo de Licença/Mobilidade | `LICENCA`, `MOBILIDADE`, `AMBOS` |

**Criar uma nova opção (caso necessário):**

```http
POST api/v1/rh/reference/options
Content-Type: application/json

{
  "ccode": "MARITAL_STATUS",
  "ckey": "SEPARADO",
  "cvalue": "Separado(a)",
  "locale": "pt-CV",
  "sortOrder": 6
}
```

> `ckey` é imutável após criação. `(ccode, ckey, locale)` é único.

---

### 5.2 Estados do Trabalhador (`/catalogs/worker-states`)

Controlam o estado laboral do funcionário. Os estados `ACTIVE` e `INACTIVE` são núcleo (`is_core = true`) e não podem ser desativados.

**Seed padrão já criado:** `ACTIVE`, `INACTIVE`, `SUSPENDED`.

**Criar um estado personalizado:**

```http
POST api/v1/rh/catalogs/worker-states
Content-Type: application/json

{
  "code": "LICENCA_SEM_VENCIMENTO",
  "description": "Em Licença Sem Vencimento",
  "isCore": false
}
```

> `DELETE api/v1/rh/catalogs/worker-states/{id}` falha com HTTP 409 se `is_core = true` ou se referenciado por funcionários activos.

---

### 5.3 Vínculos Laborais (`/catalogs/vinculos-laborais`)

Determinam a natureza do vínculo laboral (Efetivo vs Contratado têm direitos distintos). Cada tipo de contrato referencia um vínculo laboral.

**Seed padrão:** `EFETIVO`, `CONTRATADO`, `COMISSIONADO`, `ESTAGIARIO`.

```http
POST api/v1/rh/catalogs/vinculos-laborais
Content-Type: application/json

{
  "code": "EFETIVO",
  "description": "Funcionário Efetivo",
  "countsSeniority": true,
  "eligibleForProgression": true
}
```

---

### 5.4 Tipos de Contrato (`/catalogs/contract-types`)

Cada tipo representa uma figura legal distinta (Decreto-Lei 4/2024) e está associado a um vínculo laboral.

**Exemplos de código:** `NOMEACAO_DEFINITIVA`, `CFP`, `CTFP_TERMO_CERTO`, `CTFP_TERMO_INCERTO`, `COMISSAO_SERVICO`.

```http
POST api/v1/rh/catalogs/contract-types
Content-Type: application/json

{
  "code": "CTFP_TERMO_CERTO",
  "description": "CTFP celebrado por prazo determinado, nos termos do DL 4/2024.",
  "vinculoLaboralId": "uuid-do-vinculo-contratado",
  "isRenewable": true,
  "maxRenewals": 3,
  "maxDurationMonths": 36,
  "requiresCareerStructure": true
}
```

> `requiresCareerStructure: true` — o sistema obriga `careerId`, `categoryId` e `gradeId` ao criar enquadramentos para funcionários com este tipo de contrato. Use `false` para tarefeiros, prestadores de serviços e outros vínculos sem grelha salarial.

> Bloqueado se referenciado por contratos activos.

---

### 5.5 Tipos de Ausência (`/catalogs/leave-types`)

Os campos `deducts_balance` e `requires_approval` alteram completamente o fluxo de processamento do pedido de ausência.

**Seed padrão:** `FERIAS`, `DOENCA`, `MATERNIDADE`, `PATERNIDADE`, `LUTO`, `CASAMENTO`.

```http
POST api/v1/rh/catalogs/leave-types
Content-Type: application/json

{
  "code": "FERIAS",
  "description": "Férias Anuais",
  "category": "GOZAMENTO",
  "deductsBalance": true,
  "requiresApproval": true,
  "maxDaysPerYear": 22
}
```

| Campo | Impacto |
|---|---|
| `deductsBalance: true` | O sistema valida o saldo disponível antes de aprovar. |
| `requiresApproval: true` | Pedido fica em `PENDING` até aprovação pela chefia. Se `false`, vai direto para `APPROVED`. |
| `maxDaysPerYear` | Limite legal; `null` = sem limite. |

---

### 5.6 Subtipos de Licença/Mobilidade (`/catalogs/leave-mobility-subtypes`)

Distinguem licenças (sem vencimento, parental, formação) de mobilidades (comissão de serviço, requisição, destacamento).

```http
POST api/v1/rh/catalogs/leave-mobility-subtypes
Content-Type: application/json

{
  "code": "COMISSAO_SERVICO",
  "description": "Comissão de Serviço",
  "recordType": "MOBILIDADE",
  "affectsPay": false,
  "countsForSeniority": true,
  "canSelfSubmit": false
}
```

| Campo | Impacto |
|---|---|
| `recordType` | `LICENCA`, `MOBILIDADE` ou `AMBOS` — determina onde aparece na interface. Valores disponíveis via `GET /reference/options?ccode=RECORD_TYPE`. |
| `affectsPay` | Se `true`, o processamento salarial é afectado. |
| `countsForSeniority` | Se `false`, o período não conta para antiguidade. |
| `canSelfSubmit` | Se `true`, o próprio colaborador pode submeter via `/me`. |

---

### 5.7 Tipos de Documento (`/catalogs/document-types`)

Controlam que extensões de ficheiro são aceites no upload.

```http
POST api/v1/rh/catalogs/document-types
Content-Type: application/json

{
  "codigo": "CNI",
  "descricao": "Cartão Nacional de Identidade",
  "category": "IDENTIFICACAO",
  "allowedExtensions": "pdf,jpg,png"
}
```

> Os campos usam nomenclatura portuguesa: `codigo` e `descricao` (não `code`/`name`).

---

### 5.8 Feriados (`/catalogs/public-holidays`)

Usados no cálculo de dias úteis em pedidos de ausência. Feriados nacionais aplicam-se a todos; feriados municipais aplicam-se segundo o concelho da unidade orgânica do funcionário.

```http
POST api/v1/rh/catalogs/public-holidays
Content-Type: application/json

{
  "holidayDate": "2026-07-05",
  "name": "Dia da Independência de Cabo Verde",
  "isNational": true
}
```

> Restrição de unicidade: só pode existir um feriado nacional ativo por data.

---

## 6. Fase 2 — Estrutura Organizacional

### 6.1 Unidades Orgânicas (`/estrutura/organizational-units`)

Modelam a hierarquia da organização: Direção → Departamento → Divisão → Secção. A raiz tem `parentUnitId` a `null`.

```http
POST api/v1/rh/estrutura/organizational-units
Content-Type: application/json

{
  "code": "DSGT",
  "name": "Direção de Serviços de Gestão Territorial",
  "descricao": "Direcção responsável pela gestão territorial",
  "acronym": "DSGT",
  "unitType": "DIRECAO",
  "parentUnitId": null
}
```

Para criar uma sub-unidade, indique o ID da unidade-pai:

```http
POST api/v1/rh/estrutura/organizational-units
Content-Type: application/json

{
  "code": "DAM",
  "name": "Departamento de Administração e Modernização",
  "descricao": "Departamento de apoio administrativo e modernização",
  "acronym": "DAM",
  "unitType": "DEPARTAMENTO",
  "parentUnitId": "uuid-da-direcao-mae"
}
```

> `DELETE` é bloqueado (HTTP 409) se existirem sub-unidades ativas ou colaboradores atribuídos.  
> `code` é imutável após criação.

---

### 6.2 Cargos (`/estrutura/jobs`)

Designação oficial do cargo (ex: Diretor de Serviços, Coordenador, Técnico Superior).

```http
POST api/v1/rh/estrutura/jobs
Content-Type: application/json

{
  "code": "TECNICO_SUPERIOR",
  "name": "Técnico Superior",
  "description": "Técnico de nível superior com funções de análise e estudo.",
  "nivel": 3
}
```

---

### 6.3 Funções (`/estrutura/functions`)

Função efectivamente exercida pelo colaborador dentro do cargo. Cada função pode estar associada a um cargo específico via `jobId` — ao criar um enquadramento com cargo + função, o sistema valida que a função pertence ao cargo. Funções com `jobId = null` são genéricas e compatíveis com qualquer cargo.

```http
POST api/v1/rh/estrutura/functions
Content-Type: application/json

{
  "code": "COORD_PROJETO",
  "name": "Coordenador de Projeto",
  "description": "Coordenação e acompanhamento de projetos de modernização.",
  "jobId": "uuid-do-cargo-tecnico-superior"
}
```

Listar funções de um cargo específico:

```http
GET api/v1/rh/estrutura/functions?jobId={cargoId}
```

---

## 7. Fase 3 — Carreiras e Progressão

A hierarquia é sempre: **Carreira → Categoria → Escalão**. Não é possível criar uma Categoria sem Carreira, nem um Escalão sem Categoria.

### 7.1 Carreiras (`/careers`)

```http
POST api/v1/rh/careers
Content-Type: application/json

{
  "code": "TECNICO_SUPERIOR_I",
  "name": "Carreira de Técnico Superior — Nível I",
  "description": "Conforme PCFR, Decreto-Lei 4/2024.",
  "regime": "GERAL"
}
```

> `regime` aceita os `ckey` do grupo `CAREER_REGIME` de `option_entity` (ex: `GERAL`, `ESPECIAL`).

**Resposta:**

```json
{
  "id": "uuid",
  "code": "TECNICO_SUPERIOR_I",
  "name": "Carreira de Técnico Superior — Nível I",
  "description": "Conforme PCFR, Decreto-Lei 4/2024.",
  "regime": "GERAL",
  "regimeDesc": "Regime Geral",
  "isActive": true,
  "estadoDesc": "Ativo",
  "nCategorias": 0
}
```

Listar as categorias de uma carreira:

```http
GET api/v1/rh/careers/{careerId}/categories
```

---

### 7.2 Categorias (`/categories`)

Dependem de uma Carreira. O par `(career_id, code)` é único.

```http
POST api/v1/rh/categories
Content-Type: application/json

{
  "careerId": "uuid-da-carreira",
  "code": "TSP",
  "name": "Técnico Superior Principal",
  "description": "Categoria de progressão para técnicos superiores",
  "ordemProgressao": 1
}
```

Listar os escalões de uma categoria:

```http
GET api/v1/rh/categories/{categoryId}/grades
```

---

### 7.3 Escalões (`/grades`)

Dependem de uma Categoria. O par `(category_id, grade_number)` é único.

```http
POST api/v1/rh/grades
Content-Type: application/json

{
  "categoryId": "uuid-da-categoria",
  "gradeNumber": 1,
  "codigo": "ESC_1",
  "name": "Escalão 1",
  "salaryIndex": 285.5,
  "salaryBase": 52800.00
}
```

> `codigo` é um identificador textual opcional do escalão (ex: `ESC_1`, `A1`).

> `category_id` é imutável após criação.  
> `DELETE` é bloqueado se o escalão estiver referenciado por enquadramentos activos.

---

## 8. Fase 4 — Funcionário e Dossier

### 8.1 Criar o Funcionário (`/funcionarios`)

Este é o passo central. Requer que já exista um **Estado do Trabalhador** e as opções de **Sexo**, **Estado Civil** e **Nacionalidade** em `option_entity`.

```http
POST api/v1/rh/funcionarios
Content-Type: application/json

{
  "nomeCompleto": "Alex Jailson Barbosa Andrade",
  "dataNascimento": "1990-03-15",
  "genero": "M",
  "estadoCivil": "CASADO",
  "nif": "17361994",
  "documentTypeId": "uuid-do-tipo-cni",
  "numeroDocumento": "1234567",
  "dataEmissaoDoc": "2015-06-01",
  "dataValidadeDoc": "2025-06-01",
  "nacionalidade": "CV",
  "email": "alex.andrade@ingt.gov.cv",
  "telefone": "+238 261 2345",
  "morada": "Rua da Independência, Nº 12",
  "ilha": "SANTIAGO",
  "concelho": "PRAIA",
  "localidade": "Achada Santo António",
  "dataAdmissao": "2018-09-01"
}
```

**Regras de negócio na criação:**

| Campo | Regra |
|---|---|
| `nif` | Único no sistema; imutável após criação. |
| `dataNascimento` | Funcionário deve ter ≥ 18 anos. |
| `dataAdmissao` | Não pode ser posterior à data actual. |
| `genero` | Valores do grupo `SEX` (`M` ou `F`). |
| `estadoCivil` | Valores do grupo `MARITAL_STATUS`. |
| `nacionalidade` | Valores do grupo `NATIONALITY`. |
| `ilha` / `concelho` | Valores dos grupos `ISLAND` / `CONCELHO`. |

#### Registo Completo — `POST /funcionarios/registar`

Para registar um colaborador com contrato, enquadramento e dados bancários numa única chamada:

```http
POST api/v1/rh/funcionarios/registar
Content-Type: application/json

{
  "funcionario": { /* campos de FuncionarioRequestDTO */ },
  "contrato": { /* campos de ContratoRequestDTO (opcional) */ },
  "enquadramento": { /* campos de EnquadramentoRequestDTO (opcional) */ },
  "dadosBancarios": { /* campos de DadosBancariosRequestDTO (opcional) */ },
  "dossier": [
    {
      "documentTypeId": "uuid-do-tipo",
      "fileKey": "storage/key/do/ficheiro",
      "originalFilename": "cni.pdf",
      "contentType": "application/pdf",
      "fileSize": 204800,
      "description": "CNI"
    }
  ]
}
```

> Se `contrato` for fornecido, `dataAdmissao` e `dataInicio` do enquadramento são derivados de `contrato.startDate`.

#### Consultar Detalhe Completo — `GET /funcionarios/{id}/details`

Retorna o colaborador com contrato actual, enquadramento actual, dados bancários e documentos numa única resposta:

```http
GET api/v1/rh/funcionarios/{funcionarioId}/details
```

**Resposta:**

```json
{
  "funcionario": { /* FuncionarioResponseDTO */ },
  "contrato": { /* ContratoResponseDTO (actual) ou null */ },
  "enquadramento": { /* EnquadramentoResponseDTO (actual) ou null */ },
  "dadosBancarios": { /* DadosBancariosResponseDTO ou null */ },
  "documentos": [ /* lista de DocumentoResponseDTO */ ]
}
```

#### Actualizar Funcionário — `PUT /funcionarios/{id}`

O payload é um wrapper que agrupa dados pessoais e bancários:

```http
PUT api/v1/rh/funcionarios/{funcionarioId}
Content-Type: application/json

{
  "dadosPessoais": {
    "nomeCompleto": "Alex Jailson Barbosa Andrade",
    "dataNascimento": "1990-03-15",
    "genero": "M",
    "estadoCivil": "CASADO",
    "nif": "17361994",
    "documentTypeId": "uuid-do-tipo-cni",
    "numeroDocumento": "1234567",
    "dataEmissaoDoc": "2015-06-01",
    "dataValidadeDoc": "2025-06-01",
    "nacionalidade": "CV",
    "email": "alex.andrade@ingt.gov.cv",
    "telefone": "+238 261 2345",
    "morada": "Rua da Independência, Nº 12",
    "ilha": "SANTIAGO",
    "concelho": "PRAIA",
    "localidade": "Achada Santo António",
    "dataAdmissao": "2018-09-01"
  },
  "dadosBancarios": {
    "banco": "BCA",
    "numeroConta": "000300004569832014",
    "iban": "CV64000300004569832014185",
    "numeroSegurancaSocial": "123456789"
  }
}
```

> `dadosPessoais` é obrigatório; `dadosBancarios` é opcional. O `nif` dentro de `dadosPessoais` é imutável — deve corresponder ao valor existente.

---

### 8.2 Dados Bancários (`/funcionarios/{funcionarioId}/dados-bancarios`)

Registo dos dados bancários e de segurança social do funcionário.

```http
POST api/v1/rh/funcionarios/{funcionarioId}/dados-bancarios
Content-Type: application/json

{
  "banco": "BCA",
  "numeroConta": "000300004569832014",
  "iban": "CV64000300004569832014185",
  "numeroSegurancaSocial": "123456789"
}
```

> `banco` usa valores do grupo `BANCO` de `option_entity`.

---

### 8.3 Contrato do Funcionário (`/funcionarios/{funcionarioId}/contratos`)

**Depende de:** Funcionário + Tipo de Contrato.

Ao criar um novo contrato, o anterior (se existir) é **encerrado automaticamente** (`end_date = novo_start_date − 1 dia`, `is_current = false`). Este comportamento é garantido por trigger na base de dados.

```http
POST api/v1/rh/funcionarios/{funcionarioId}/contratos
Content-Type: application/json

{
  "contractTypeId": "uuid-do-tipo-ctfp-termo-certo",
  "contractNumber": "CTFP/2018/001",
  "startDate": "2018-09-01",
  "endDate": "2020-08-31",
  "regimeTrabalho": "TEMPO_COMPLETO",
  "percentagemTempo": 100.00,
  "legalBase": "Despacho Nº 15/2018 — Boletim Oficial Nº 35/2018",
  "notes": "Contrato inicial por 2 anos."
}
```

Para renovar / mudar tipo de contrato, basta criar um novo — o anterior fecha automaticamente:

```http
POST api/v1/rh/funcionarios/{funcionarioId}/contratos
Content-Type: application/json

{
  "contractTypeId": "uuid-do-tipo-nomeacao-definitiva",
  "startDate": "2020-09-01",
  "legalBase": "Despacho Nº 22/2020"
}
```

> `regimeTrabalho` usa valores do grupo `WORK_REGIME` de `option_entity`.

Consultar o histórico completo:

```http
GET api/v1/rh/funcionarios/{funcionarioId}/contratos
```

#### Gestão do ciclo de vida do contrato

**Cessar (encerrar) um contrato:**

```http
PUT api/v1/rh/funcionarios/{funcionarioId}/contratos/{contratoId}/close
Content-Type: application/json

{
  "endDate": "2026-06-30",
  "terminationReason": "Termo do prazo contratual",
  "notes": "Não renovado por decisão administrativa."
}
```

> Ao cessar um contrato, o enquadramento activo é encerrado automaticamente na mesma data.

**Suspender um contrato:**

```http
PUT api/v1/rh/funcionarios/{funcionarioId}/contratos/{contratoId}/suspend
```

**Reactivar um contrato suspenso:**

```http
PUT api/v1/rh/funcionarios/{funcionarioId}/contratos/{contratoId}/activate
```

#### Documentos associados ao contrato

```http
POST api/v1/rh/funcionarios/{funcionarioId}/contratos/{contratoId}/documentos
GET  api/v1/rh/funcionarios/{funcionarioId}/contratos/{contratoId}/documentos
GET  api/v1/rh/funcionarios/{funcionarioId}/contratos/{contratoId}/documentos/{docId}
GET  api/v1/rh/funcionarios/{funcionarioId}/contratos/{contratoId}/documentos/{docId}/download
DELETE api/v1/rh/funcionarios/{funcionarioId}/contratos/{contratoId}/documentos/{docId}
```

> O POST de documento usa o mesmo `UploadDocumentoRequestDTO` descrito na secção 8.10. Os parâmetros de consulta opcionais na listagem são `documentTypeId` (UUID) e `active` (boolean).

---

### 8.4 Enquadramento Profissional (`/funcionarios/{funcionarioId}/enquadramentos`)

**Depende de:** Funcionário (com contrato ATIVO) + Cargo + Unidade Orgânica. Carreira/Categoria/Escalão são obrigatórios apenas quando `contractType.requiresCareerStructure = true`.

**Validações cruzadas com o contrato:**
- O funcionário deve ter um contrato com `status = ATIVO`. Caso contrário → HTTP 422.
- `dataInicio` deve ser ≥ `contrato.startDate` e ≤ `contrato.endDate` (se definido) → HTTP 422.
- Se `contractType.requiresCareerStructure = true`: `careerId`, `categoryId` e `gradeId` são obrigatórios → HTTP 422.
- Ao criar um novo enquadramento, o anterior é **encerrado automaticamente** (`dataFim = dataInicio - 1 dia`).
- Ao **cessar um contrato** (`PUT .../close`), o enquadramento activo é encerrado automaticamente na mesma data.

**Trabalhador de carreira** (`requiresCareerStructure = true`):

```http
POST api/v1/rh/funcionarios/{funcionarioId}/enquadramentos
Content-Type: application/json

{
  "careerId": "uuid-da-carreira",
  "categoryId": "uuid-da-categoria-tsp",
  "gradeId": "uuid-do-escalao-1",
  "cargoId": "uuid-do-cargo-tecnico-superior",
  "functionId": "uuid-da-funcao-coord-projeto",
  "unidadeOrganicaId": "uuid-da-unidade-organica",
  "dataInicio": "2018-09-01"
}
```

**Trabalhador sem carreira** (`requiresCareerStructure = false`, ex: tarefeiro):

```http
POST api/v1/rh/funcionarios/{funcionarioId}/enquadramentos
Content-Type: application/json

{
  "cargoId": "uuid-do-cargo",
  "unidadeOrganicaId": "uuid-da-unidade-organica",
  "dataInicio": "2024-03-01"
}
```

Para registar uma progressão (promoção de categoria ou escalão):

```http
POST api/v1/rh/funcionarios/{funcionarioId}/enquadramentos
Content-Type: application/json

{
  "careerId": "uuid-da-carreira",
  "categoryId": "uuid-da-nova-categoria",
  "gradeId": "uuid-do-novo-escalao",
  "cargoId": "uuid-do-cargo",
  "unidadeOrganicaId": "uuid-da-unidade-organica",
  "dataInicio": "2024-01-01"
}
```

---

### 8.5 Colocação em Unidade Orgânica (`/funcionarios/{funcionarioId}/colocacoes`)

**Depende de:** Funcionário + Unidade Orgânica.

Regista a atribuição formal do funcionário a uma unidade orgânica, com o cargo e tipo de afectação.

```http
POST api/v1/rh/funcionarios/{funcionarioId}/colocacoes
Content-Type: application/json

{
  "unitId": "uuid-da-unidade-dsgt",
  "jobId": "uuid-do-cargo",
  "startDate": "2018-09-01",
  "assignmentType": "PRIMARIA",
  "notes": "Colocação inicial."
}
```

| Campo | Detalhe |
|---|---|
| `assignmentType` | Tipo de afectação (ex: `PRIMARIA`, `SECUNDARIA`, `TEMPORARIA`). |
| `jobId` | Cargo exercido nesta unidade (opcional). |

**Actualizar colocação:**

```http
PUT api/v1/rh/funcionarios/{funcionarioId}/colocacoes/{colocacaoId}
Content-Type: application/json

{
  "endDate": "2026-06-30",
  "notes": "Transferência para outra unidade."
}
```

**Encerrar colocação:**

```http
PUT api/v1/rh/funcionarios/{funcionarioId}/colocacoes/{colocacaoId}/close
```

**Desactivar colocação (soft delete):**

```http
DELETE api/v1/rh/funcionarios/{funcionarioId}/colocacoes/{colocacaoId}
```

---

### 8.6 Dependentes (`/funcionarios/{funcionarioId}/dependentes`)

**Depende de:** Funcionário. O campo `relationshipType` usa os valores do grupo `RELATIONSHIP_TYPE` de `option_entity`.

```http
POST api/v1/rh/funcionarios/{funcionarioId}/dependentes
Content-Type: application/json

{
  "fullName": "Maria Andrade",
  "birthDate": "2015-06-20",
  "relationshipType": "FILHO",
  "nif": "11223344"
}
```

---

### 8.7 Qualificações Literárias (`/funcionarios/{funcionarioId}/qualificacoes`)

**Depende de:** Funcionário. O campo `level` usa os `ckey` do grupo `QUALIFICATION_LEVEL`. O campo `country` usa os `ckey` do grupo `NATIONALITY`.

```http
POST api/v1/rh/funcionarios/{funcionarioId}/qualificacoes
Content-Type: application/json

{
  "level": "LICENCIATURA",
  "courseName": "Engenharia Informática",
  "institution": "Universidade de Cabo Verde",
  "country": "CV",
  "startDate": "2008-09-01",
  "endDate": "2012-07-30",
  "completed": true
}
```

**Resposta:**

```json
{
  "id": "uuid",
  "funcionarioId": "uuid",
  "level": "LICENCIATURA",
  "levelDesc": "Licenciatura",
  "courseName": "Engenharia Informática",
  "institution": "Universidade de Cabo Verde",
  "country": "CV",
  "countryDesc": "Cabo Verde",
  "startDate": "2008-09-01",
  "endDate": "2012-07-30",
  "completed": true,
  "completedDesc": "Sim",
  "isActive": true,
  "estadoDesc": "Ativo"
}
```

#### Documentos associados à qualificação

```http
POST   api/v1/rh/funcionarios/{funcionarioId}/qualificacoes/{qualificacaoId}/documentos
GET    api/v1/rh/funcionarios/{funcionarioId}/qualificacoes/{qualificacaoId}/documentos
GET    api/v1/rh/funcionarios/{funcionarioId}/qualificacoes/{qualificacaoId}/documentos/{docId}
GET    api/v1/rh/funcionarios/{funcionarioId}/qualificacoes/{qualificacaoId}/documentos/{docId}/download
DELETE api/v1/rh/funcionarios/{funcionarioId}/qualificacoes/{qualificacaoId}/documentos/{docId}
```

> O POST de documento usa o mesmo `UploadDocumentoRequestDTO` descrito na secção 8.10. Parâmetros de consulta opcionais na listagem: `documentTypeId` (UUID) e `active` (boolean).

---

### 8.8 Formações Profissionais (`/funcionarios/{funcionarioId}/formacoes`)

**Depende de:** Funcionário. `trainingType` usa os `ckey` do grupo `TRAINING_TYPE`.

```http
POST api/v1/rh/funcionarios/{funcionarioId}/formacoes
Content-Type: application/json

{
  "name": "Gestão de Projetos com PRINCE2",
  "institution": "IFP — Instituto de Formação Profissional",
  "trainingType": "CURSO",
  "startDate": "2023-03-06",
  "endDate": "2023-03-10",
  "durationHours": 40
}
```

**Resposta:**

```json
{
  "id": "uuid",
  "funcionarioId": "uuid",
  "name": "Gestão de Projetos com PRINCE2",
  "institution": "IFP — Instituto de Formação Profissional",
  "trainingType": "CURSO",
  "trainingTypeDesc": "Curso",
  "startDate": "2023-03-06",
  "endDate": "2023-03-10",
  "durationHours": 40
}
```

#### Documentos associados à formação

```http
POST   api/v1/rh/funcionarios/{funcionarioId}/formacoes/{formacaoId}/documentos
GET    api/v1/rh/funcionarios/{funcionarioId}/formacoes/{formacaoId}/documentos
GET    api/v1/rh/funcionarios/{funcionarioId}/formacoes/{formacaoId}/documentos/{docId}
GET    api/v1/rh/funcionarios/{funcionarioId}/formacoes/{formacaoId}/documentos/{docId}/download
DELETE api/v1/rh/funcionarios/{funcionarioId}/formacoes/{formacaoId}/documentos/{docId}
```

> O POST de documento usa o mesmo `UploadDocumentoRequestDTO` descrito na secção 8.10. Parâmetros de consulta opcionais na listagem: `documentTypeId` (UUID) e `active` (boolean).

---

### 8.9 Processos Disciplinares (`/funcionarios/{funcionarioId}/processos-disciplinares`)

**Acesso restrito:** `ROLE_HR_ADMIN` e `ROLE_SYSTEM_ADMIN`.

```http
POST api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares
Content-Type: application/json

{
  "processNumber": "PD/2024/001",
  "startDate": "2024-02-10",
  "endDate": "2024-03-20",
  "penalty": "Repreensão escrita",
  "penaltyStartDate": "2024-04-01",
  "penaltyEndDate": "2024-04-01",
  "officialBulletin": "BO Nº 12/2024",
  "notes": "Processo de averiguações concluído."
}
```

**Resposta:**

```json
{
  "id": "uuid",
  "funcionarioId": "uuid",
  "processNumber": "PD/2024/001",
  "startDate": "2024-02-10",
  "endDate": "2024-03-20",
  "penalty": "Repreensão escrita",
  "penaltyStartDate": "2024-04-01",
  "penaltyEndDate": "2024-04-01",
  "officialBulletin": "BO Nº 12/2024",
  "notes": "Processo de averiguações concluído."
}
```

**Actualizar processo disciplinar:**

```http
PUT api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}
Content-Type: application/json

{
  "processNumber": "PD/2024/001",
  "startDate": "2024-02-10",
  "endDate": "2024-04-15",
  "penalty": "Suspensão de 30 dias",
  "penaltyStartDate": "2024-05-01",
  "penaltyEndDate": "2024-05-30",
  "officialBulletin": "BO Nº 15/2024",
  "notes": "Pena agravada após recurso."
}
```

#### Documentos associados ao processo disciplinar

```http
POST   api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/documentos
GET    api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/documentos
GET    api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/documentos/{docId}
GET    api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/documentos/{docId}/download
DELETE api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/documentos/{docId}
```

> O POST de documento usa o mesmo `UploadDocumentoRequestDTO` descrito na secção 8.10. Parâmetros de consulta opcionais na listagem: `documentTypeId` (UUID) e `active` (boolean).

---

## 9. Fluxos Operacionais

### 9.1 Pedidos de Ausência

**Depende de:** Funcionário + Tipo de Ausência.

O ciclo de vida de um pedido é: `PENDING → APPROVED | REJECTED → CANCELLED`.

**1. Submeter um pedido:**

```http
POST api/v1/rh/funcionarios/{funcionarioId}/pedidos-ausencia
Content-Type: application/json

{
  "tipoAusenciaId": "uuid-do-tipo-ferias",
  "dataInicio": "2026-08-10",
  "dataFim": "2026-08-21",
  "motivo": "Período de férias anuais."
}
```

**Regras aplicadas automaticamente:**
- `dataFim ≥ dataInicio`.
- Cálculo de dias úteis exclui sábados, domingos e feriados (`t_public_holiday`).
- Se `deducts_balance = true`: verifica se `dias_pedido ≤ saldo_disponível`.
- Se `requires_approval = false`: o pedido vai direto para `APPROVED`.
- Não são permitidos pedidos sobrepostos para o mesmo funcionário (excepto `CANCELLED` ou `REJECTED`).

**2. Aprovar (pela chefia — `ROLE_CHEFIA`):**

```http
PATCH api/v1/rh/funcionarios/{funcionarioId}/pedidos-ausencia/{id}/aprovar
Content-Type: application/json

{
  "aprovadoPorId": "uuid-do-utilizador-que-aprova",
  "observacoesDecisao": "Aprovado."
}
```

**3. Rejeitar com justificação:**

```http
PATCH api/v1/rh/funcionarios/{funcionarioId}/pedidos-ausencia/{id}/rejeitar
Content-Type: application/json

{
  "aprovadoPorId": "uuid-do-utilizador-que-rejeita",
  "observacoesDecisao": "Período de maior afluência de trabalho. Reagendar para setembro."
}
```

**4. Cancelar (apenas pedidos em `PENDING`):**

```http
PATCH api/v1/rh/funcionarios/{funcionarioId}/pedidos-ausencia/{id}/cancelar
```

**Consultar saldo de ausências:**

```http
GET api/v1/rh/funcionarios/{funcionarioId}/saldos-ausencia?ano=2026
```

**Inicializar saldo (ROLE_HR_ADMIN):**

```http
POST api/v1/rh/funcionarios/{funcionarioId}/saldos-ausencia
Content-Type: application/json

{
  "tipoAusenciaId": "uuid-do-tipo-ferias",
  "ano": 2026,
  "diasDireito": 22
}
```

**Ajustar dias de direito do saldo (ROLE_HR_ADMIN):**

```http
PUT api/v1/rh/funcionarios/{funcionarioId}/saldos-ausencia/{saldoId}?diasDireito=22
```

---

### 9.2 Licenças e Mobilidades

**Depende de:** Funcionário + Subtipo de Licença/Mobilidade + (em mobilidades) Unidade Orgânica de destino.

O ciclo de vida é: `PENDING → ACTIVE | CANCELLED → CLOSED`.

**1. Registar uma licença sem vencimento:**

```http
POST api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade
Content-Type: application/json

{
  "subtipoId": "uuid-do-subtipo-licenca-sem-vencimento",
  "dataInicio": "2026-09-01",
  "dataFim": "2027-08-31",
  "justification": "Licença para frequência de doutoramento.",
  "observacoes": "Autorizada por despacho interno."
}
```

> O `recordType` (LICENCA/MOBILIDADE) é determinado pelo subtipo — não precisa de ser enviado no pedido.

**2. Registar uma mobilidade (comissão de serviço):**

```http
POST api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade
Content-Type: application/json

{
  "subtipoId": "uuid-do-subtipo-comissao-servico",
  "dataInicio": "2026-07-01",
  "dataFim": "2027-06-30",
  "destinationUnitId": "uuid-da-unidade-destino",
  "entidadeDestino": "Ministério das Finanças",
  "despachoNumero": "Despacho Nº 8/2026",
  "justification": "Reforço temporário por necessidade de serviço.",
  "observacoes": "Autorizado pelo Conselho de Ministros."
}
```

**3. Aprovar a mobilidade:**

Ao aprovar, o sistema executa automaticamente `fn_apply_mobility`: encerra a atribuição orgânica actual e cria uma nova na unidade de destino.

```http
PUT api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade/{id}/approve
```

**4. Rejeitar:**

```http
PUT api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade/{id}/reject
Content-Type: application/json

{
  "rejectionReason": "Documentação insuficiente. Resubmeter com despacho ministerial."
}
```

**5. Encerrar a mobilidade:**

Em mobilidades, o encerramento **restaura a atribuição orgânica anterior** do colaborador.

```http
PUT api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade/{id}/close
```

**6. Cancelar (enquanto `PENDING` ou `ACTIVE`):**

```http
PUT api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade/{id}/cancel
```

**7. Documentos associados a uma licença/mobilidade:**

```http
POST api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade/{id}/documentos
Content-Type: application/json

{
  "documentTypeId": "uuid-do-tipo",
  "fileKey": "storage/key/do/ficheiro",
  "originalFilename": "despacho.pdf",
  "contentType": "application/pdf",
  "fileSize": 204800,
  "description": "Despacho de autorização"
}

GET    api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade/{id}/documentos
GET    api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade/{id}/documentos/{docId}
GET    api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade/{id}/documentos/{docId}/download
DELETE api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade/{id}/documentos/{docId}
```

---

### 9.3 Recibos de Vencimento

```http
POST api/v1/rh/funcionarios/{funcionarioId}/recibos
Content-Type: application/json

{
  "periodMonth": 4,
  "periodYear": 2026,
  "issueDate": "2026-04-30",
  "grossSalary": 95000.00,
  "netSalary": 78500.00,
  "documentId": "uuid-do-pdf-do-recibo"
}
```

**Regras:**
- `(funcionario_id, period_month, period_year)` é único.
- `grossSalary > 0`, `netSalary > 0`, `netSalary ≤ grossSalary`.

#### Documentos associados ao recibo

```http
POST   api/v1/rh/funcionarios/{funcionarioId}/recibos/{reciboId}/documentos
GET    api/v1/rh/funcionarios/{funcionarioId}/recibos/{reciboId}/documentos
GET    api/v1/rh/funcionarios/{funcionarioId}/recibos/{reciboId}/documentos/{docId}
GET    api/v1/rh/funcionarios/{funcionarioId}/recibos/{reciboId}/documentos/{docId}/download
DELETE api/v1/rh/funcionarios/{funcionarioId}/recibos/{reciboId}/documentos/{docId}
```

> O POST de documento usa o mesmo `UploadDocumentoRequestDTO`. Parâmetros de consulta opcionais na listagem: `documentTypeId` (UUID) e `active` (boolean).

---

### 9.4 Mudança de Estado do Colaborador

A mudança de estado laboral (ACTIVE, SUSPENDED, RETIRED, INACTIVE) é feita por um endpoint dedicado — **não** pelo `PUT /funcionarios/{id}`.

**Estados disponíveis** (seed padrão): `ACTIVE` (core), `INACTIVE` (core), `SUSPENDED`, `RETIRED`.

**Consultar os estados disponíveis:**

```http
GET api/v1/rh/catalogs/worker-states
```

**Consultar os motivos disponíveis:**

```http
GET api/v1/rh/reference/options?ccode=WORKER_STATE_REASON
```

**Mudar o estado:**

```http
PATCH api/v1/rh/funcionarios/{funcionarioId}/worker-state
Content-Type: application/json

{
  "workerStateId": "uuid-do-estado-suspended",
  "motivoCkey": "MEDICAL_SUSPENSION",
  "dataEfectividade": "2026-05-20",
  "observacao": "Internamento hospitalar. Regresso previsto em 30 dias."
}
```

**Efeitos automáticos:**

| Novo Estado | Contrato | Colocação |
|---|---|---|
| `SUSPENDED` | `status → SUSPENSO` | Sem alteração |
| `ACTIVE` (reactivação) | `status: SUSPENSO → ATIVO` | Sem alteração |
| `RETIRED` | `status → CESSADO` | Fechada |
| `INACTIVE` | `status → CESSADO` | Fechada |

**Erros comuns:**

| HTTP | Motivo |
|---|---|
| 409 | Tentativa de transitar para o mesmo estado actual |
| 404 | `workerStateId` não existe |

**Consultar o histórico de mudanças:**

```http
GET api/v1/rh/funcionarios/{funcionarioId}/worker-state/historico
```

**Resposta:**
```json
[
  {
    "id": "uuid-do-historico",
    "funcionarioId": "uuid-do-funcionario",
    "estadoAnteriorId": "uuid-estado-anterior",
    "estadoAnteriorDescricao": "ACTIVE",
    "estadoNovoId": "uuid-estado-novo",
    "estadoNovoDescricao": "SUSPENDED",
    "motivoCkey": "MEDICAL_SUSPENSION",
    "motivoDescricao": "Suspensão por Motivo de Saúde",
    "dataEfectividade": "2026-05-20",
    "observacao": "Suspensão temporária por motivo de saúde",
    "registadoPor": "rh.admin@ingt.gov.cv",
    "registadoEm": "2026-05-20T09:15:00"
  }
]
```

---

## 10. Área Reservada do Colaborador (`/me`)

Todos os endpoints `/me` são restritos ao colaborador autenticado. O `funcionario_id` é extraído automaticamente do JWT — não é possível consultar dados de outro colaborador.

| Endpoint | Descrição |
|---|---|
| `GET api/v1/rh/me/profile` | Perfil completo: dados pessoais, enquadramento actual, cargo, função, unidade. |
| `GET api/v1/rh/me/leave-requests` | Os meus pedidos de ausência (filtros: `status`, `leaveTypeId`, `year`). |
| `GET api/v1/rh/me/leave-balances` | Os meus saldos de ausência por tipo. |
| `POST api/v1/rh/me/leave-requests` | Submeter pedido de ausência. |
| `PUT api/v1/rh/me/leave-requests/{id}/cancel` | Cancelar pedido próprio (apenas `PENDING`). |
| `GET api/v1/rh/me/leaves-mobilities` | As minhas licenças e mobilidades. |
| `GET api/v1/rh/me/leaves-mobilities/{id}` | Detalhe de uma licença/mobilidade. |
| `POST api/v1/rh/me/leaves-mobilities` | Submeter licença/mobilidade (apenas subtipos com `canSelfSubmit = true`). |
| `GET api/v1/rh/me/payroll-slips` | Os meus recibos (filtros: `periodYear`, `periodMonth`). |
| `GET api/v1/rh/me/payroll-slips/{id}/download` | Download do PDF do recibo. |
| `GET api/v1/rh/me/documents` | Os meus documentos pessoais. |
| `GET api/v1/rh/me/documents/{id}/download` | Download de documento pessoal. |

**Submeter pedido de ausência (self-service):**

> **Nota:** os campos do pedido self-service usam nomes em inglês, diferindo do endpoint administrativo.

```http
POST api/v1/rh/me/leave-requests
Content-Type: application/json

{
  "leaveTypeId": "uuid-do-tipo-ferias",
  "startDate": "2026-08-10",
  "endDate": "2026-08-21",
  "notes": "Período de férias anuais."
}
```

| Campo self-service | Equivalente admin |
|---|---|
| `leaveTypeId` (UUID) | `tipoAusenciaId` (String) |
| `startDate` | `dataInicio` |
| `endDate` | `dataFim` |
| `notes` | `motivo` |

**Submeter licença/mobilidade (self-service):**

```http
POST api/v1/rh/me/leaves-mobilities
Content-Type: application/json

{
  "subtipoId": "uuid-do-subtipo",
  "dataInicio": "2026-09-01",
  "dataFim": "2027-08-31",
  "entidadeDestino": "Ministério das Finanças",
  "despachoNumero": "Despacho Nº 10/2026",
  "observacoes": "Licença para formação.",
  "justification": "Mestrado em Gestão Pública."
}
```

> Apenas subtipos com `canSelfSubmit = true` são aceites neste endpoint. O campo `destinationUnitId` **não está disponível** no self-service — apenas no endpoint administrativo.

**Restrições:**
- O colaborador deve ter `is_active = true`; se estiver inactivo, recebe HTTP 403.
- Em ambiente `development` o JWT não é validado — qualquer chamada é aceite.

---

## 11. API de Documentos / Ficheiros

O fluxo de documentos é em duas etapas: (1) o frontend carrega o ficheiro directamente para o MinIO via URL pré-assinada; (2) regista os metadados na API. Os documentos são associados ao funcionário e podem ser referenciados em entidades específicas (formações, processos disciplinares, licenças, etc.).

### Registar metadados de documento

Após o upload do ficheiro para o MinIO, regista os metadados:

```http
POST api/v1/rh/funcionarios/{funcionarioId}/documentos
Content-Type: application/json

{
  "documentTypeId": "uuid-do-tipo-cni",
  "fileKey": "funcionarios/{funcionarioId}/cni_alex.pdf",
  "originalFilename": "cni_alex.pdf",
  "contentType": "application/pdf",
  "fileSize": 245680,
  "description": "Cópia do CNI frente e verso"
}
```

**Resposta:**

```json
{
  "id": "uuid-do-documento",
  "referenceEntity": "FUNCIONARIO",
  "referenceId": "uuid-do-funcionario",
  "documentTypeId": "uuid-do-tipo-cni",
  "documentType": {
    "id": "uuid",
    "codigo": "CNI",
    "descricao": "Cartão Nacional de Identidade",
    "allowedExtensions": "pdf,jpg,png"
  },
  "originalFilename": "cni_alex.pdf",
  "contentType": "application/pdf",
  "fileSize": 245680,
  "description": "Cópia do CNI frente e verso",
  "isActive": true,
  "estadoDesc": "Ativo"
}
```

**Limites:** Extensões aceites definidas pelo `document_type.allowed_extensions`.

### Download (URL pré-assinada)

```http
GET api/v1/rh/funcionarios/{funcionarioId}/documentos/{documentId}/download
```

Devolve uma URL pré-assinada do MinIO — o ficheiro não é enviado directamente pelo servidor.

### Listar documentos de um funcionário

```http
GET api/v1/rh/funcionarios/{funcionarioId}/documentos
```

Filtros: `documentTypeId`, `active`.

### Desativar documento

```http
DELETE api/v1/rh/funcionarios/{funcionarioId}/documentos/{documentId}
```

---

## 12. Endpoints Combobox (Dropdowns)

Cada módulo principal expõe um endpoint `/combobox` para popular dropdowns no frontend. Retornam listas simplificadas (id + label) sem paginação.

| Endpoint | Descrição |
|---|---|
| `GET api/v1/rh/reference/options/combobox?ccode={code}` | Opções de um grupo |
| `GET api/v1/rh/careers/combobox` | Carreiras activas |
| `GET api/v1/rh/categories/combobox?careerId={id}` | Categorias de uma carreira |
| `GET api/v1/rh/grades/combobox?categoryId={id}` | Escalões de uma categoria |
| `GET api/v1/rh/estrutura/jobs/combobox` | Cargos activos |
| `GET api/v1/rh/estrutura/functions/combobox?jobId={id}` | Funções (filtro por cargo opcional) |
| `GET api/v1/rh/estrutura/organizational-units/combobox` | Unidades orgânicas activas |
| `GET api/v1/rh/funcionarios/combobox` | Funcionários activos |

---

## 13. Estruturas de Resposta (Response DTOs)

Esta secção documenta as estruturas de resposta de cada recurso, essenciais para o desenho do frontend. Todos os campos `*Desc` são labels legíveis derivados de chaves de catálogo.

### 13.1 FuncionarioResponseDTO

```json
{
  "id": "uuid",
  "numeroFuncionario": "F-00001",
  "nomeCompleto": "Alex Jailson Barbosa Andrade",
  "dataNascimento": "1990-03-15",
  "genero": "M",
  "estadoCivil": "CASADO",
  "nif": "17361994",
  "documentTypeId": "uuid",
  "documentTypeName": "Cartão Nacional de Identidade",
  "numeroDocumento": "1234567",
  "dataEmissaoDoc": "2015-06-01",
  "dataValidadeDoc": "2025-06-01",
  "nacionalidade": "CV",
  "email": "alex.andrade@ingt.gov.cv",
  "telefone": "+238 261 2345",
  "morada": "Rua da Independência, Nº 12",
  "ilha": "SANTIAGO",
  "concelho": "PRAIA",
  "localidade": "Achada Santo António",
  "workerStateId": "uuid",
  "workerStateName": "Ativo",
  "dataAdmissao": "2018-09-01",
  "isActive": true,
  "estadoDesc": "Ativo"
}
```

### 13.2 ContratoResponseDTO

```json
{
  "id": "uuid",
  "funcionarioId": "uuid",
  "contractTypeId": "uuid",
  "contractTypeName": "Nomeação Definitiva",
  "contractNumber": "CTFP/2018/001",
  "startDate": "2018-09-01",
  "endDate": null,
  "terminationReason": null,
  "isCurrent": true,
  "isCurrentDesc": "Sim",
  "status": "ATIVO",
  "renewalCount": 0,
  "regimeTrabalho": "TEMPO_COMPLETO",
  "percentagemTempo": 100.00,
  "legalBase": "Despacho Nº 15/2018",
  "notes": null
}
```

### 13.3 EnquadramentoResponseDTO

```json
{
  "id": "uuid",
  "funcionarioId": "uuid",
  "careerId": "uuid",
  "careerName": "Técnico Superior — Nível I",
  "categoryId": "uuid",
  "categoryName": "Técnico Superior Principal",
  "gradeId": "uuid",
  "gradeName": "Escalão 1",
  "cargoId": "uuid",
  "cargoName": "Técnico Superior",
  "functionId": "uuid",
  "functionName": "Coordenador de Projeto",
  "unidadeOrganicaId": "uuid",
  "unitName": "DSGT",
  "dataInicio": "2018-09-01",
  "dataFim": null,
  "isCurrent": true,
  "isCurrentDesc": "Sim"
}
```

> Endpoints adicionais: `GET .../enquadramentos/atual` retorna apenas o enquadramento corrente.

### 13.4 ColocacaoResponseDTO

```json
{
  "id": "uuid",
  "funcionarioId": "uuid",
  "unitId": "uuid",
  "unitName": "DSGT",
  "jobId": "uuid",
  "jobName": "Técnico Superior",
  "startDate": "2018-09-01",
  "endDate": null,
  "isCurrent": true,
  "isCurrentDesc": "Sim",
  "isActive": true,
  "estadoDesc": "Ativo",
  "assignmentType": "PRIMARIA",
  "notes": null
}
```

> Endpoints adicionais: `GET .../colocacoes/atual` retorna apenas a colocação corrente.

### 13.5 DadosBancariosResponseDTO

```json
{
  "id": "uuid",
  "funcionarioId": "uuid",
  "banco": "BCA",
  "bancoDesc": "Banco Comercial do Atlântico",
  "numeroConta": "000300004569832014",
  "iban": "CV64000300004569832014185",
  "numeroSegurancaSocial": "123456789",
  "isActive": true,
  "estadoDesc": "Ativo"
}
```

### 13.6 DependenteResponseDTO

```json
{
  "id": "uuid",
  "funcionarioId": "uuid",
  "fullName": "Maria Andrade",
  "relationshipType": "FILHO",
  "relationshipTypeDesc": "Filho(a)",
  "birthDate": "2015-06-20",
  "nif": "11223344",
  "isActive": true,
  "estadoDesc": "Ativo"
}
```

### 13.7 PedidoAusenciaResponseDTO

```json
{
  "id": "uuid",
  "funcionarioId": "uuid",
  "tipoAusencia": {
    "id": "uuid",
    "codigo": "FERIAS",
    "nome": "Férias Anuais",
    "deductsBalance": true,
    "requiresApproval": true,
    "maxDaysPerYear": 22,
    "categoryOptionCkey": "GOZAMENTO",
    "isActive": true,
    "estadoDesc": "Ativo"
  },
  "dataInicio": "2026-08-10",
  "dataFim": "2026-08-21",
  "numeroDias": 8,
  "motivo": "Período de férias anuais.",
  "estado": "PENDING",
  "aprovadoPor": null,
  "dataDecisao": null,
  "observacoesDecisao": null,
  "isActive": true,
  "estadoDesc": "Pendente"
}
```

### 13.8 SaldoAusenciaResponseDTO

```json
{
  "id": "uuid",
  "funcionarioId": "uuid",
  "tipoAusenciaId": "uuid",
  "tipoAusencia": { "id": "uuid", "codigo": "FERIAS", "nome": "Férias Anuais" },
  "ano": 2026,
  "diasDireito": 22,
  "diasGozados": 8,
  "diasPendentes": 5,
  "diasDisponiveis": 9
}
```

> Todos os campos de dias são **inteiros** (não decimais).

### 13.9 LicencaMobilidadeResponseDTO

```json
{
  "id": "uuid",
  "funcionarioId": "uuid",
  "subtipoId": "uuid",
  "subtipo": {
    "id": "uuid",
    "codigo": "COMISSAO_SERVICO",
    "nome": "Comissão de Serviço",
    "recordType": "MOBILIDADE",
    "affectsPay": false,
    "countsForSeniority": true,
    "canSelfSubmit": false,
    "isActive": true,
    "estadoDesc": "Ativo"
  },
  "dataInicio": "2026-07-01",
  "dataFim": "2027-06-30",
  "entidadeDestino": "Ministério das Finanças",
  "despachoNumero": "Despacho Nº 8/2026",
  "observacoes": "Autorizado pelo Conselho de Ministros.",
  "isActive": true,
  "estadoDesc": "Ativo",
  "status": "ACTIVE",
  "destinationUnitId": "uuid",
  "destinationUnitName": "DGPCP",
  "justification": "Reforço temporário por necessidade de serviço.",
  "rejectionReason": null
}
```

### 13.10 MeProfileResponseDTO

```json
{
  "id": "uuid",
  "fullName": "Alex Jailson Barbosa Andrade",
  "nif": "17361994",
  "email": "alex.andrade@ingt.gov.cv",
  "phone": "+238 261 2345",
  "workerState": "Ativo",
  "admissionDate": "2018-09-01",
  "currentUnit": { "id": "uuid", "name": "DSGT" },
  "currentJob": { "id": "uuid", "name": "Técnico Superior" },
  "career": { "id": "uuid", "name": "Técnico Superior — Nível I" },
  "category": { "id": "uuid", "name": "Técnico Superior Principal" },
  "grade": { "id": "uuid", "gradeNumber": 1 }
}
```

### 13.11 DocumentoResponseDTO

```json
{
  "id": "uuid",
  "referenceEntity": "FUNCIONARIO",
  "referenceId": "uuid",
  "documentTypeId": "uuid",
  "documentType": {
    "id": "uuid",
    "codigo": "CNI",
    "descricao": "Cartão Nacional de Identidade",
    "allowedExtensions": "pdf,jpg,png"
  },
  "originalFilename": "cni_alex.pdf",
  "contentType": "application/pdf",
  "fileSize": 245680,
  "description": "Cópia do CNI frente e verso",
  "isActive": true,
  "estadoDesc": "Ativo"
}
```

### 13.12 ReciboVencimentoDTO

```json
{
  "id": "uuid",
  "funcionarioId": "uuid",
  "periodMonth": 4,
  "periodYear": 2026,
  "issueDate": "2026-04-30",
  "grossSalary": 95000.00,
  "netSalary": 78500.00,
  "documentId": "uuid-do-pdf"
}
```

---

## 14. Referência Rápida de Endpoints

### Parametrizações

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `api/v1/rh/reference/options?ccode={code}` | Listar opções de um grupo |
| `GET` | `api/v1/rh/reference/options/{id}` | Detalhe de opção |
| `POST` | `api/v1/rh/reference/options` | Criar opção |
| `PUT` | `api/v1/rh/reference/options/{id}` | Actualizar opção |
| `DELETE` | `api/v1/rh/reference/options/{id}` | Desativar opção |
| `GET/POST/PUT/DELETE` | `api/v1/rh/catalogs/worker-states/{id?}` | Estados do trabalhador |
| `GET/POST/PUT/DELETE` | `api/v1/rh/catalogs/vinculos-laborais/{id?}` | Vínculos laborais |
| `GET/POST/PUT/DELETE` | `api/v1/rh/catalogs/contract-types/{id?}` | Tipos de contrato |
| `GET/POST/PUT/DELETE` | `api/v1/rh/catalogs/leave-types/{id?}` | Tipos de ausência |
| `GET/POST/PUT/DELETE` | `api/v1/rh/catalogs/leave-mobility-subtypes/{id?}` | Subtipos de licença/mobilidade |
| `GET/POST/PUT/DELETE` | `api/v1/rh/catalogs/document-types/{id?}` | Tipos de documento |
| `GET/POST/PUT/DELETE` | `api/v1/rh/catalogs/public-holidays/{id?}` | Feriados (filtros: `year`, `isNational`, `dateFrom`, `dateTo`) |

### Estrutura Organizacional

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `api/v1/rh/estrutura/organizational-units` | Listar unidades (filtros: `active`, `parentUnitId`, `code`, `nome`) |
| `GET` | `api/v1/rh/estrutura/organizational-units/{id}` | Detalhe de unidade |
| `GET` | `api/v1/rh/estrutura/organizational-units/combobox` | Combobox de unidades |
| `POST` | `api/v1/rh/estrutura/organizational-units` | Criar unidade |
| `PUT` | `api/v1/rh/estrutura/organizational-units/{id}` | Actualizar unidade |
| `DELETE` | `api/v1/rh/estrutura/organizational-units/{id}/deactivate` | Desativar unidade |
| `PATCH` | `api/v1/rh/estrutura/organizational-units/{id}/activate` | Reactivar unidade |
| `GET/POST/PUT` | `api/v1/rh/estrutura/jobs/{id?}` | Cargos (filtros: `active`, `code`, `nome`) |
| `GET` | `api/v1/rh/estrutura/jobs/combobox` | Combobox de cargos |
| `DELETE` | `api/v1/rh/estrutura/jobs/{id}/deactivate` | Desativar cargo |
| `PATCH` | `api/v1/rh/estrutura/jobs/{id}/activate` | Reactivar cargo |
| `GET/POST/PUT` | `api/v1/rh/estrutura/functions/{id?}` | Funções (filtros: `active`, `jobId`, `code`, `nome`) |
| `GET` | `api/v1/rh/estrutura/functions/combobox` | Combobox de funções |
| `DELETE` | `api/v1/rh/estrutura/functions/{id}/deactivate` | Desativar função |
| `PATCH` | `api/v1/rh/estrutura/functions/{id}/activate` | Reactivar função |

### Carreiras

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `api/v1/rh/careers` | Listar carreiras (filtros: `active`, `code`, `nome`) |
| `GET` | `api/v1/rh/careers/{id}` | Detalhe de carreira |
| `GET` | `api/v1/rh/careers/{id}/categories` | Categorias de uma carreira |
| `GET` | `api/v1/rh/careers/combobox` | Combobox de carreiras |
| `POST` | `api/v1/rh/careers` | Criar carreira |
| `PUT` | `api/v1/rh/careers/{id}` | Actualizar carreira |
| `DELETE` | `api/v1/rh/careers/{id}` | Desativar carreira |
| `PUT` | `api/v1/rh/careers/{id}/activate` | Reactivar carreira |
| `GET` | `api/v1/rh/categories?careerId={id}` | Listar categorias (filtros: `careerId`, `active`, `code`, `nome`) |
| `GET` | `api/v1/rh/categories/{id}` | Detalhe de categoria |
| `GET` | `api/v1/rh/categories/{id}/grades` | Escalões de uma categoria |
| `GET` | `api/v1/rh/categories/combobox` | Combobox de categorias |
| `POST` | `api/v1/rh/categories` | Criar categoria (requer `careerId`) |
| `PUT` | `api/v1/rh/categories/{id}` | Actualizar categoria |
| `DELETE` | `api/v1/rh/categories/{id}` | Desativar categoria |
| `PUT` | `api/v1/rh/categories/{id}/activate` | Reactivar categoria |
| `GET` | `api/v1/rh/grades?categoryId={id}` | Listar escalões (filtros: `categoryId`, `active`) |
| `GET` | `api/v1/rh/grades/{id}` | Detalhe de escalão |
| `GET` | `api/v1/rh/grades/combobox` | Combobox de escalões |
| `POST` | `api/v1/rh/grades` | Criar escalão (requer `categoryId`) |
| `PUT` | `api/v1/rh/grades/{id}` | Actualizar escalão |
| `DELETE` | `api/v1/rh/grades/{id}` | Desativar escalão |
| `PUT` | `api/v1/rh/grades/{id}/activate` | Reactivar escalão |

### Funcionários

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `api/v1/rh/funcionarios` | Listar funcionários (filtros: `nome`, `nif`, `workerStateId`, `unidadeOrganicaId`, `careerId`, `active`) |
| `GET` | `api/v1/rh/funcionarios/{id}` | Dados pessoais do funcionário |
| `GET` | `api/v1/rh/funcionarios/{id}/details` | Detalhe completo (pessoais + contrato + enquadramento + bancários + documentos) |
| `GET` | `api/v1/rh/funcionarios/combobox` | Combobox de funcionários |
| `POST` | `api/v1/rh/funcionarios` | Criar funcionário (só dados pessoais) |
| `POST` | `api/v1/rh/funcionarios/registar` | Registo completo (pessoais + contrato + enquadramento + bancários + dossier) |
| `PUT` | `api/v1/rh/funcionarios/{id}` | Actualizar (wrapper: `dadosPessoais` + `dadosBancarios`; NIF imutável) |
| `PATCH` | `api/v1/rh/funcionarios/{id}/worker-state` | Mudar estado laboral (com efeitos em cascata) |
| `GET` | `api/v1/rh/funcionarios/{id}/worker-state/historico` | Histórico de mudanças de estado |
| `GET/POST/PUT/DELETE` | `api/v1/rh/funcionarios/{id}/dados-bancarios/{did?}` | Dados bancários e segurança social |
| `PUT` | `api/v1/rh/funcionarios/{id}/dados-bancarios/{did}/activate` | Reactivar dados bancários |
| `GET` | `api/v1/rh/funcionarios/{id}/contratos` | Histórico de contratos |
| `GET` | `api/v1/rh/funcionarios/{id}/contratos/{cid}` | Detalhe de contrato |
| `POST` | `api/v1/rh/funcionarios/{id}/contratos` | Novo contrato (encerra o anterior) |
| `PUT` | `api/v1/rh/funcionarios/{id}/contratos/{cid}` | Actualizar contrato |
| `DELETE` | `api/v1/rh/funcionarios/{id}/contratos/{cid}` | Desativar contrato |
| `PUT` | `api/v1/rh/funcionarios/{id}/contratos/{cid}/close` | Cessar contrato (body: `endDate`, `terminationReason`, `notes`) |
| `PUT` | `api/v1/rh/funcionarios/{id}/contratos/{cid}/suspend` | Suspender contrato |
| `PUT` | `api/v1/rh/funcionarios/{id}/contratos/{cid}/activate` | Reactivar contrato suspenso |
| `POST` | `api/v1/rh/funcionarios/{id}/contratos/{cid}/documentos` | Associar documento ao contrato |
| `GET` | `api/v1/rh/funcionarios/{id}/contratos/{cid}/documentos` | Documentos do contrato |
| `GET` | `api/v1/rh/funcionarios/{id}/enquadramentos` | Histórico de enquadramentos |
| `GET` | `api/v1/rh/funcionarios/{id}/enquadramentos/atual` | Enquadramento actual |
| `GET` | `api/v1/rh/funcionarios/{id}/enquadramentos/{eid}` | Detalhe de enquadramento |
| `POST` | `api/v1/rh/funcionarios/{id}/enquadramentos` | Novo enquadramento (encerra o anterior) |
| `GET` | `api/v1/rh/funcionarios/{id}/colocacoes` | Colocações orgânicas |
| `GET` | `api/v1/rh/funcionarios/{id}/colocacoes/atual` | Colocação actual |
| `GET` | `api/v1/rh/funcionarios/{id}/colocacoes/{cid}` | Detalhe de colocação |
| `POST` | `api/v1/rh/funcionarios/{id}/colocacoes` | Nova colocação (encerra a anterior) |
| `PUT` | `api/v1/rh/funcionarios/{id}/colocacoes/{cid}` | Actualizar colocação (body: `endDate`, `notes`) |
| `PUT` | `api/v1/rh/funcionarios/{id}/colocacoes/{cid}/close` | Encerrar colocação |
| `DELETE` | `api/v1/rh/funcionarios/{id}/colocacoes/{cid}` | Desativar colocação |
| `GET/POST/PUT/DELETE` | `api/v1/rh/funcionarios/{id}/dependentes/{did?}` | Dependentes |
| `GET/POST/PUT/DELETE` | `api/v1/rh/funcionarios/{id}/qualificacoes/{qid?}` | Qualificações |
| `POST/GET/DELETE` | `api/v1/rh/funcionarios/{id}/qualificacoes/{qid}/documentos/{docId?}` | Documentos da qualificação |
| `GET/POST/PUT/DELETE` | `api/v1/rh/funcionarios/{id}/formacoes/{tid?}` | Formações |
| `POST/GET/DELETE` | `api/v1/rh/funcionarios/{id}/formacoes/{tid}/documentos/{docId?}` | Documentos da formação |
| `GET/POST/PUT` | `api/v1/rh/funcionarios/{id}/processos-disciplinares/{pid?}` | Processos disciplinares |
| `POST/GET/DELETE` | `api/v1/rh/funcionarios/{id}/processos-disciplinares/{pid}/documentos/{docId?}` | Documentos do processo |
| `GET` | `api/v1/rh/funcionarios/{id}/documentos` | Documentos do funcionário (filtros: `documentTypeId`, `active`) |
| `GET` | `api/v1/rh/funcionarios/{id}/documentos/{did}` | Metadados de documento |
| `GET` | `api/v1/rh/funcionarios/{id}/documentos/{did}/download` | URL pré-assinada de download |
| `POST` | `api/v1/rh/funcionarios/{id}/documentos` | Registar metadados de documento (JSON, não multipart) |
| `DELETE` | `api/v1/rh/funcionarios/{id}/documentos/{did}` | Desativar documento |
| `GET` | `api/v1/rh/funcionarios/{id}/saldos-ausencia?ano={ano}` | Saldos de ausência (filtros: `ano`, `tipoAusenciaId`) |
| `POST` | `api/v1/rh/funcionarios/{id}/saldos-ausencia` | Inicializar saldo de ausência |
| `PUT` | `api/v1/rh/funcionarios/{id}/saldos-ausencia/{saldoId}?diasDireito={n}` | Ajustar dias de direito |
| `GET/POST` | `api/v1/rh/funcionarios/{id}/pedidos-ausencia/{pid?}` | Pedidos de ausência (filtros: `estado`, `tipoAusenciaId`, `ano`) |
| `PATCH` | `api/v1/rh/funcionarios/{id}/pedidos-ausencia/{pid}/aprovar` | Aprovar pedido (body: `aprovadoPorId`, `observacoesDecisao`) |
| `PATCH` | `api/v1/rh/funcionarios/{id}/pedidos-ausencia/{pid}/rejeitar` | Rejeitar pedido (body: `aprovadoPorId`, `observacoesDecisao`) |
| `PATCH` | `api/v1/rh/funcionarios/{id}/pedidos-ausencia/{pid}/cancelar` | Cancelar pedido |
| `GET/POST` | `api/v1/rh/funcionarios/{id}/licencas-mobilidade/{lid?}` | Licenças e mobilidades |
| `PUT` | `api/v1/rh/funcionarios/{id}/licencas-mobilidade/{lid}` | Actualizar licença/mobilidade |
| `PUT` | `api/v1/rh/funcionarios/{id}/licencas-mobilidade/{lid}/approve` | Aprovar licença/mobilidade |
| `PUT` | `api/v1/rh/funcionarios/{id}/licencas-mobilidade/{lid}/reject` | Rejeitar (body: `rejectionReason`) |
| `PUT` | `api/v1/rh/funcionarios/{id}/licencas-mobilidade/{lid}/close` | Encerrar licença/mobilidade |
| `PUT` | `api/v1/rh/funcionarios/{id}/licencas-mobilidade/{lid}/cancel` | Cancelar licença/mobilidade |
| `POST/GET` | `api/v1/rh/funcionarios/{id}/licencas-mobilidade/{lid}/documentos` | Documentos da licença/mobilidade |
| `GET` | `api/v1/rh/funcionarios/{id}/licencas-mobilidade/{lid}/documentos/{docId}` | Detalhe de documento da licença |
| `GET` | `api/v1/rh/funcionarios/{id}/licencas-mobilidade/{lid}/documentos/{docId}/download` | Download de documento da licença |
| `DELETE` | `api/v1/rh/funcionarios/{id}/licencas-mobilidade/{lid}/documentos/{docId}` | Desativar documento da licença |
| `GET` | `api/v1/rh/funcionarios/{id}/recibos` | Listar recibos (filtros: `periodYear`, `periodMonth`) |
| `GET` | `api/v1/rh/funcionarios/{id}/recibos/{rid}` | Detalhe de recibo |
| `POST` | `api/v1/rh/funcionarios/{id}/recibos` | Criar recibo |
| `POST/GET` | `api/v1/rh/funcionarios/{id}/recibos/{rid}/documentos` | Documentos do recibo |
| `GET` | `api/v1/rh/funcionarios/{id}/recibos/{rid}/documentos/{docId}` | Detalhe de documento do recibo |
| `GET` | `api/v1/rh/funcionarios/{id}/recibos/{rid}/documentos/{docId}/download` | Download de documento do recibo |
| `DELETE` | `api/v1/rh/funcionarios/{id}/recibos/{rid}/documentos/{docId}` | Desativar documento do recibo |

### Auditoria

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `api/v1/rh/colaboradores/audit/{catalog}/{entityId}` | Histórico de alterações — colaboradores |
| `GET` | `api/v1/rh/carreiras/audit/{catalog}/{entityId}` | Histórico de alterações — carreiras |
| `GET` | `api/v1/rh/estrutura/audit/{catalog}/{entityId}` | Histórico de alterações — estrutura organizacional |
| `GET` | `api/v1/rh/catalogs/audit/{catalog}/{entityId}` | Histórico de alterações — catálogos/parametrizações |

> `{catalog}` é o nome da entidade auditada (ex: `funcionarios`, `contratos`, `careers`, `worker-states`). `{entityId}` é o UUID da entidade.

---

## 15. Erros e Códigos HTTP

| Código | Significado | Causas comuns |
|---|---|---|
| `200 OK` | Sucesso (leitura / actualização). | — |
| `201 Created` | Recurso criado com sucesso. | — |
| `204 No Content` | Sucesso sem corpo de resposta. | Soft delete, activar. |
| `400 Bad Request` | Payload malformado. | JSON inválido, tipos errados. |
| `401 Unauthorized` | Token ausente ou expirado. | Header `Authorization` em falta. |
| `403 Forbidden` | Sem permissão para a operação. | Perfil insuficiente; funcionário inactivo em `/me`. |
| `404 Not Found` | Recurso não encontrado. | ID inexistente ou desactivado. |
| `409 Conflict` | Conflito de integridade. | Desactivar recurso referenciado por registos activos; `is_core = true`. |
| `422 Unprocessable Entity` | Regra de negócio violada. | Sobreposição de ausências; saldo insuficiente; hierarquia carreira/categoria/escalão incoerente. |
| `500 Internal Server Error` | Erro não esperado. | Contactar equipa de suporte com `traceId` da resposta. |

---

*Documento atualizado em Junho de 2026 — Módulo RH v4.8 — SIPPROG/INGT*
