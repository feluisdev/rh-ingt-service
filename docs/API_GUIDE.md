# Guia de Utilização da API — Módulo de Recursos Humanos (SIPPROG)

> Versão 4.5 · Maio 2026  
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
12. [Referência Rápida de Endpoints](#12-referência-rápida-de-endpoints)
13. [Erros e Códigos HTTP](#13-erros-e-códigos-http)

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
| **Paginação** | `page` (1-based, default 1) e `size` (default 20, máx. 100) |
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
  "page": 1,
  "size": 20,
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
Function (Função) ───────────────────────────────────────────────────► Colocação
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
| `TRAINING_TYPE` | Tipo de Formação | `PRESENCIAL`, `ELEARNING`, `SEMINARIO`, `CONGRESSO` |
| `DOC_CATEGORY` | Categoria de Documento | `PESSOAL`, `CONTRATUAL`, `FORMACAO`, `DISCIPLINAR`, `AVALIACAO` |
| `LEAVE_CATEGORY` | Categoria de Ausência | `FERIAS`, `DOENCA`, `FAMILIA`, `OUTRO` |
| `ISLAND` | Ilha | `SANTIAGO`, `SAL`, `SAO_VICENTE`, `FOGO`, `BOA_VISTA` |
| `CONCELHO` | Concelho | `PRAIA`, `SANTA_CATARINA`, `MINDELO`, `SAO_DOMINGOS` |
| `CAREER_REGIME` | Regime da Carreira (PCFR) | `GERAL`, `ESPECIAL` |
| `BANCO` | Banco | `BCA`, `BCN`, `CECV`, `CAIXA` |
| `WORK_REGIME` | Regime de Trabalho | `TEMPO_INTEIRO`, `TEMPO_PARCIAL` |

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
  "description": "Funcionário Efetivo"
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
  "name": "Contrato de Trabalho em Funções Públicas a Termo Certo",
  "description": "CTFP celebrado por prazo determinado, nos termos do DL 4/2024.",
  "vinculoLaboralId": "uuid-do-vinculo-contratado"
}
```

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
  "name": "Férias Anuais",
  "categoryOptionKey": "FERIAS",
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
  "name": "Comissão de Serviço",
  "recordType": "MOBILIDADE",
  "affectsPay": false,
  "countsForSeniority": true,
  "canSelfSubmit": false
}
```

| Campo | Impacto |
|---|---|
| `recordType` | `LICENCA`, `MOBILIDADE` ou `AMBOS` — determina onde aparece na interface. |
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
  "categoryOptionId": "uuid-da-opcao-pessoal",
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
  "acronym": "DSGT",
  "unitTypeOptionKey": "DIRECAO",
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
  "acronym": "DAM",
  "unitTypeOptionKey": "DEPARTAMENTO",
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
  "description": "Técnico de nível superior com funções de análise e estudo."
}
```

---

### 6.3 Funções (`/estrutura/functions`)

Função efectivamente exercida pelo colaborador dentro do cargo.

```http
POST api/v1/rh/estrutura/functions
Content-Type: application/json

{
  "code": "COORD_PROJETO",
  "name": "Coordenador de Projeto",
  "description": "Coordenação e acompanhamento de projetos de modernização."
}
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
  "regimeOptionId": "uuid-da-opcao-regime"
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
  "name": "Escalão 1",
  "salaryIndex": 285.5,
  "salaryBase": 52800.00
}
```

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
  "regimeTrabalho": "TEMPO_INTEIRO",
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

---

### 8.4 Enquadramento Profissional (`/funcionarios/{funcionarioId}/enquadramentos`)

**Depende de:** Funcionário + Carreira + Categoria (da carreira) + Escalão (da categoria) + Cargo + Unidade Orgânica.

O trigger `fn_validate_professional_assignment` valida automaticamente que `categoryId` pertence à `careerId` e que `gradeId` pertence à `categoryId`. Se a hierarquia for incoerente, recebe HTTP 422.

Tal como nos contratos, ao criar um novo enquadramento, o anterior é **encerrado automaticamente**.

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

**Depende de:** Funcionário. O campo `levelOptionKey` usa o grupo `QUALIFICATION_LEVEL`.

```http
POST api/v1/rh/funcionarios/{funcionarioId}/qualificacoes
Content-Type: application/json

{
  "levelOptionKey": "LICENCIATURA",
  "courseName": "Engenharia Informática",
  "institution": "Universidade de Cabo Verde",
  "countryOptionKey": "CV",
  "startDate": "2008-09-01",
  "endDate": "2012-07-30",
  "completed": true,
  "documentId": "uuid-do-certificado-carregado"
}
```

> `documentId` refere um documento previamente carregado via `POST api/v1/rh/funcionarios/{id}/documentos`.

---

### 8.8 Formações Profissionais (`/funcionarios/{funcionarioId}/formacoes`)

**Depende de:** Funcionário. `typeOptionKey` usa o grupo `TRAINING_TYPE`.

```http
POST api/v1/rh/funcionarios/{funcionarioId}/formacoes
Content-Type: application/json

{
  "name": "Gestão de Projetos com PRINCE2",
  "institution": "IFP — Instituto de Formação Profissional",
  "typeOptionKey": "PRESENCIAL",
  "startDate": "2023-03-06",
  "endDate": "2023-03-10",
  "durationHours": 40,
  "documentId": "uuid-do-certificado"
}
```

---

### 8.9 Processos Disciplinares (`/funcionarios/{funcionarioId}/processos-disciplinares`)

**Acesso restrito:** `ROLE_HR_ADMIN` e `ROLE_SYSTEM_ADMIN`.

```http
POST api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares
Content-Type: application/json

{
  "processNumber": "PD/2024/001",
  "startDate": "2024-02-10",
  "penalty": "Repreensão escrita",
  "penaltyStartDate": "2024-04-01",
  "penaltyEndDate": "2024-04-01",
  "officialBulletin": "BO Nº 12/2024",
  "notes": "Processo de averiguações concluído.",
  "documentId": "uuid-do-processo-digitalizado"
}
```

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
  "leaveTypeId": "uuid-do-tipo-ferias",
  "startDate": "2026-08-10",
  "endDate": "2026-08-21",
  "justification": "Período de férias anuais."
}
```

**Regras aplicadas automaticamente:**
- `endDate ≥ startDate`.
- Cálculo de dias úteis exclui sábados, domingos e feriados (`t_public_holiday`).
- Se `deducts_balance = true`: verifica se `dias_pedido ≤ saldo_disponível`.
- Se `requires_approval = false`: o pedido vai direto para `APPROVED`.
- Não são permitidos pedidos sobrepostos para o mesmo funcionário (excepto `CANCELLED` ou `REJECTED`).

**2. Aprovar (pela chefia — `ROLE_CHEFIA`):**

```http
PUT api/v1/rh/funcionarios/{funcionarioId}/pedidos-ausencia/{id}/approve
```

**3. Rejeitar com justificação:**

```http
PUT api/v1/rh/funcionarios/{funcionarioId}/pedidos-ausencia/{id}/reject
Content-Type: application/json

{
  "rejectionReason": "Período de maior afluência de trabalho. Reagendar para setembro."
}
```

**4. Cancelar (apenas pedidos em `PENDING`):**

```http
PUT api/v1/rh/funcionarios/{funcionarioId}/pedidos-ausencia/{id}/cancel
```

**Consultar saldo de ausências:**

```http
GET api/v1/rh/funcionarios/{funcionarioId}/saldos-ausencia?year=2026
```

**Ajustar saldo manualmente (ROLE_HR_ADMIN):**

```http
PUT api/v1/rh/funcionarios/{funcionarioId}/saldos-ausencia/{balanceId}
Content-Type: application/json

{
  "assignedDays": 22,
  "usedDays": 5
}
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
  "recordType": "LICENCA",
  "subtypeId": "uuid-do-subtipo-licenca-sem-vencimento",
  "startDate": "2026-09-01",
  "endDate": "2027-08-31",
  "isTemporary": true,
  "justification": "Licença para frequência de doutoramento."
}
```

**2. Registar uma mobilidade (comissão de serviço):**

```http
POST api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade
Content-Type: application/json

{
  "recordType": "MOBILIDADE",
  "subtypeId": "uuid-do-subtipo-comissao-servico",
  "startDate": "2026-07-01",
  "endDate": "2027-06-30",
  "isTemporary": true,
  "targetUnitId": "uuid-da-unidade-destino",
  "targetFunctionId": "uuid-da-funcao-destino",
  "legalBase": "Despacho Nº 8/2026",
  "justification": "Reforço temporário por necessidade de serviço."
}
```

**3. Aprovar a mobilidade:**

Ao aprovar, o sistema executa automaticamente `fn_apply_mobility`: encerra a atribuição orgânica actual e cria uma nova na unidade de destino.

```http
PUT api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade/{id}/approve
```

**4. Encerrar a mobilidade:**

Em mobilidades temporárias, o encerramento **restaura a atribuição orgânica anterior** do colaborador.

```http
PUT api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade/{id}/close
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
| `POST api/v1/rh/me/leaves-mobilities` | Submeter licença/mobilidade (apenas subtipos com `canSelfSubmit = true`). |
| `GET api/v1/rh/me/payroll-slips` | Os meus recibos (filtros: `periodYear`, `periodMonth`). |
| `GET api/v1/rh/me/payroll-slips/{id}/download` | Download do PDF do recibo. |
| `GET api/v1/rh/me/documents` | Os meus documentos pessoais. |
| `GET api/v1/rh/me/documents/{id}/download` | Download de documento pessoal. |

**Restrições:**
- O colaborador deve ter `is_active = true`; se estiver inactivo, recebe HTTP 403.
- Em ambiente `development` o JWT não é validado — qualquer chamada é aceite.

---

## 11. API de Documentos / Ficheiros

Os documentos são associados ao funcionário e podem depois ser referenciados em entidades específicas (pedidos de ausência, formações, processos disciplinares, etc.).

### Upload de ficheiro

```http
POST api/v1/rh/funcionarios/{funcionarioId}/documentos
Content-Type: multipart/form-data

file: [binário do ficheiro]
documentTypeId: uuid-do-tipo-cni
description: "Cópia do CNI frente e verso"
```

**Resposta:**

```json
{
  "id": "uuid-do-documento",
  "fileName": "cni_alex.pdf",
  "mimeType": "application/pdf",
  "sizeBytes": 245680,
  "uploadedAt": "2026-05-08T10:14:00Z",
  "funcionarioId": "uuid-do-funcionario",
  "storageKey": "funcionarios/{id}/cni_alex.pdf"
}
```

**Limites:** 10 MB por ficheiro. Extensões aceites definidas pelo `document_type.allowed_extensions`.

### Download

```http
GET api/v1/rh/funcionarios/{funcionarioId}/documentos/{documentId}/download
```

Devolve o ficheiro com `Content-Type` e `Content-Disposition` apropriados. Gerada uma URL pré-assinada para o MinIO — o ficheiro não é enviado directamente pelo servidor.

### Listar documentos de um funcionário

```http
GET api/v1/rh/funcionarios/{funcionarioId}/documentos
```

---

## 12. Referência Rápida de Endpoints

### Parametrizações

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `api/v1/rh/reference/options?ccode={code}` | Listar opções de um grupo |
| `POST` | `api/v1/rh/reference/options` | Criar opção |
| `PUT` | `api/v1/rh/reference/options/{id}` | Actualizar opção |
| `DELETE` | `api/v1/rh/reference/options/{id}` | Desativar opção |
| `GET/POST/PUT/DELETE` | `api/v1/rh/catalogs/worker-states/{id?}` | Estados do trabalhador |
| `GET/POST/PUT/DELETE` | `api/v1/rh/catalogs/vinculos-laborais/{id?}` | Vínculos laborais |
| `GET/POST/PUT/DELETE` | `api/v1/rh/catalogs/contract-types/{id?}` | Tipos de contrato |
| `GET/POST/PUT/DELETE` | `api/v1/rh/catalogs/leave-types/{id?}` | Tipos de ausência |
| `GET/POST/PUT/DELETE` | `api/v1/rh/catalogs/leave-mobility-subtypes/{id?}` | Subtipos de licença/mobilidade |
| `GET/POST/PUT/DELETE` | `api/v1/rh/catalogs/document-types/{id?}` | Tipos de documento |
| `GET/POST/PUT/DELETE` | `api/v1/rh/catalogs/public-holidays/{id?}` | Feriados |

### Estrutura Organizacional

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `api/v1/rh/estrutura/organizational-units` | Listar unidades (filtros: `parentUnitId`, `unitType`, `isActive`) |
| `GET` | `api/v1/rh/estrutura/organizational-units/{id}` | Detalhe de unidade |
| `POST` | `api/v1/rh/estrutura/organizational-units` | Criar unidade |
| `PUT` | `api/v1/rh/estrutura/organizational-units/{id}` | Actualizar unidade |
| `DELETE` | `api/v1/rh/estrutura/organizational-units/{id}` | Desativar unidade |
| `GET/POST/PUT/DELETE` | `api/v1/rh/estrutura/jobs/{id?}` | Cargos |
| `GET/POST/PUT/DELETE` | `api/v1/rh/estrutura/functions/{id?}` | Funções |

### Carreiras

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `api/v1/rh/careers` | Listar carreiras |
| `GET` | `api/v1/rh/careers/{id}/categories` | Categorias de uma carreira |
| `POST` | `api/v1/rh/careers` | Criar carreira |
| `GET` | `api/v1/rh/categories?careerId={id}` | Listar categorias |
| `GET` | `api/v1/rh/categories/{id}/grades` | Escalões de uma categoria |
| `POST` | `api/v1/rh/categories` | Criar categoria (requer `careerId`) |
| `GET` | `api/v1/rh/grades?categoryId={id}` | Listar escalões |
| `POST` | `api/v1/rh/grades` | Criar escalão (requer `categoryId`) |

### Funcionários

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `api/v1/rh/funcionarios` | Listar funcionários (filtros: `search`, `unitId`, `workerStateId`, `careerId`) |
| `GET` | `api/v1/rh/funcionarios/{id}` | Detalhe completo |
| `POST` | `api/v1/rh/funcionarios` | Criar funcionário |
| `PUT` | `api/v1/rh/funcionarios/{id}` | Actualizar (NIF imutável) |
| `DELETE` | `api/v1/rh/funcionarios/{id}` | Desativar (bloqueado se pedidos PENDING) |
| `GET/POST` | `api/v1/rh/funcionarios/{id}/dados-bancarios` | Dados bancários e segurança social |
| `GET` | `api/v1/rh/funcionarios/{id}/contratos` | Histórico de contratos |
| `POST` | `api/v1/rh/funcionarios/{id}/contratos` | Novo contrato (encerra o anterior) |
| `GET` | `api/v1/rh/funcionarios/{id}/enquadramentos` | Histórico de enquadramentos |
| `POST` | `api/v1/rh/funcionarios/{id}/enquadramentos` | Novo enquadramento (encerra o anterior) |
| `GET` | `api/v1/rh/funcionarios/{id}/colocacoes` | Colocações orgânicas |
| `POST` | `api/v1/rh/funcionarios/{id}/colocacoes` | Nova colocação |
| `GET/POST/PUT/DELETE` | `api/v1/rh/funcionarios/{id}/dependentes/{did?}` | Dependentes |
| `GET/POST/PUT/DELETE` | `api/v1/rh/funcionarios/{id}/qualificacoes/{qid?}` | Qualificações |
| `GET/POST/PUT/DELETE` | `api/v1/rh/funcionarios/{id}/formacoes/{tid?}` | Formações |
| `GET/POST/PUT/DELETE` | `api/v1/rh/funcionarios/{id}/processos-disciplinares/{pid?}` | Processos disciplinares |
| `GET` | `api/v1/rh/funcionarios/{id}/documentos` | Documentos do funcionário |
| `POST` | `api/v1/rh/funcionarios/{id}/documentos` | Upload de documento |
| `GET` | `api/v1/rh/funcionarios/{id}/saldos-ausencia` | Saldos de ausência |
| `PUT` | `api/v1/rh/funcionarios/{id}/saldos-ausencia/{bid}` | Ajustar saldo |
| `GET/POST` | `api/v1/rh/funcionarios/{id}/pedidos-ausencia/{pid?}` | Pedidos de ausência |
| `PUT` | `api/v1/rh/funcionarios/{id}/pedidos-ausencia/{pid}/approve` | Aprovar pedido |
| `PUT` | `api/v1/rh/funcionarios/{id}/pedidos-ausencia/{pid}/reject` | Rejeitar pedido |
| `PUT` | `api/v1/rh/funcionarios/{id}/pedidos-ausencia/{pid}/cancel` | Cancelar pedido |
| `GET/POST` | `api/v1/rh/funcionarios/{id}/licencas-mobilidade/{lid?}` | Licenças e mobilidades |
| `PUT` | `api/v1/rh/funcionarios/{id}/licencas-mobilidade/{lid}/approve` | Aprovar licença/mobilidade |
| `PUT` | `api/v1/rh/funcionarios/{id}/licencas-mobilidade/{lid}/close` | Encerrar licença/mobilidade |
| `PUT` | `api/v1/rh/funcionarios/{id}/licencas-mobilidade/{lid}/cancel` | Cancelar licença/mobilidade |
| `GET/POST` | `api/v1/rh/funcionarios/{id}/recibos/{rid?}` | Recibos de vencimento |

### Auditoria

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `api/v1/rh/colaboradores/audit` | Histórico de alterações — colaboradores |
| `GET` | `api/v1/rh/carreiras/audit` | Histórico de alterações — carreiras |
| `GET` | `api/v1/rh/estrutura/audit` | Histórico de alterações — estrutura organizacional |
| `GET` | `api/v1/rh/catalogs/audit` | Histórico de alterações — catálogos/parametrizações |

---

## 13. Erros e Códigos HTTP

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

*Documento atualizado em Maio de 2026 — Módulo RH v4.5 — SIPPROG/INGT*
