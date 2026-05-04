# Guia de Utilização da API — Módulo de Recursos Humanos (SIPPROG)

> Versão 4.0 · Abril 2026  
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

- Um **Funcionário** exige uma **Situação Profissional** e um **Estado do Trabalhador**.
- Um **Contrato** exige um **Tipo de Contrato** e um **Funcionário**.
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
| **OptionEntity** (lookups simples) | `GET /reference/options?ccode=...` | Sexo, estado civil, nacionalidade, ilha, tipo de unidade, nível de habilitação, parentesco, etc. |
| **Tabelas dedicadas** | `/worker-states`, `/contract-types`, etc. | Catálogos com flags que alteram o comportamento do sistema (ex: `deducts_balance`, `is_core`). |

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
| **Base URL** | `https://api.sipprog.ingt.gov.cv/v1/rh` (produção) · `http://localhost:8091` (local) |
| **Formato de data** | ISO-8601: `YYYY-MM-DD` para datas; `YYYY-MM-DDTHH:mm:ss` para timestamps UTC |
| **Paginação** | `page` (1-based, default 1) e `size` (default 20, máx. 100) |
| **Ordenação** | `sort=campo,asc` ou `sort=campo,desc` |
| **IDs** | UUID (`xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`) |
| **Content-Type** | `application/json` (exceto upload de ficheiros: `multipart/form-data`) |

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
  "timestamp": "2026-04-22T10:00:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Campo obrigatório em falta.",
  "path": "/v1/rh/employees",
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
ProfessionalSituation ───────────────────────────────────────────────► Funcionário
                                                                        │
ContractType ────────────────────────────────────────────────────────► Contrato
DocumentType ────────────────────────────────────────────────────────► Documento
LeaveType ───────────────────────────────────────────────────────────► PedidoAusência
LeaveMobilitySubtype ────────────────────────────────────────────────► Licença/Mobilidade
PublicHoliday ───────────────────────────────────────────────────────► (cálculo de dias úteis)
                                                                        │
OrganizationalUnit ──────────────────────────────────────────────────► EnquadramentoProfissional
Job (Cargo) ─────────────────────────────────────────────────────────► EnquadramentoProfissional
Function (Função) ───────────────────────────────────────────────────► EnquadramentoProfissional
                                                                        │
Career ──────────────────────────────────────────────────────────────► EnquadramentoProfissional
  └── Category ────────────────────────────────────────────────────► EnquadramentoProfissional
        └── Grade ─────────────────────────────────────────────────► EnquadramentoProfissional
```

**Resumo da ordem obrigatória:**

```
1. OptionEntity (sexo, estado civil, etc.)
2. WorkerStates + ProfessionalSituations + ContractTypes + DocumentTypes
   + LeaveTypes + LeaveMobilitySubtypes + PublicHolidays
3. OrganizationalUnits + Jobs + Functions
4. Careers → Categories → Grades
5. Funcionário
6. Contrato (do funcionário)
7. Enquadramento Profissional (do funcionário)
8. Colocação em Unidade Orgânica (do funcionário)
9. Dossier: Dependentes, Habilitações, Formações, Processos Disciplinares
10. Documentos, Pedidos de Ausência, Licenças/Mobilidades
```

---

## 5. Fase 1 — Parametrizações (pré-requisitos)

### 5.1 OptionEntity — Lookups Genéricos

Antes de qualquer outra coisa, verifique se os grupos de opções base estão populados. Na maioria dos ambientes são populados por seed automático (Flyway `afterMigrate`).

**Consultar opções de um grupo:**

```http
GET /reference/options?ccode=MARITAL_STATUS
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

**Criar uma nova opção (caso necessário):**

```http
POST /reference/options
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

### 5.2 Estados do Trabalhador (`/worker-states`)

Controlam o estado laboral do funcionário. Os estados `ACTIVE` e `INACTIVE` são núcleo (`is_core = true`) e não podem ser desativados.

**Seed padrão já criado:** `ACTIVE`, `INACTIVE`, `SUSPENDED`.

**Criar um estado personalizado:**

```http
POST /worker-states
Content-Type: application/json

{
  "code": "LICENCA_SEM_VENCIMENTO",
  "name": "Em Licença Sem Vencimento",
  "isCore": false
}
```

> `DELETE /worker-states/{id}` falha com HTTP 409 se `is_core = true` ou se referenciado por funcionários activos.

---

### 5.3 Situações Profissionais (`/professional-situations`)

Determinam as regras de progressão PCFR (Efetivo vs Contratado têm direitos distintos).

**Seed padrão:** `EFETIVO`, `CONTRATADO`, `COMISSIONADO`, `ESTAGIARIO`.

```http
POST /professional-situations
Content-Type: application/json

{
  "code": "EFETIVO",
  "name": "Funcionário Efetivo"
}
```

---

### 5.4 Tipos de Contrato (`/contract-types`)

Cada tipo representa uma figura legal distinta (Decreto-Lei 4/2024).

**Exemplos de código:** `NOMEACAO_DEFINITIVA`, `CFP`, `CTFP_TERMO_CERTO`, `CTFP_TERMO_INCERTO`, `COMISSAO_SERVICO`.

```http
POST /contract-types
Content-Type: application/json

{
  "code": "CTFP_TERMO_CERTO",
  "name": "Contrato de Trabalho em Funções Públicas a Termo Certo",
  "description": "CTFP celebrado por prazo determinado, nos termos do DL 4/2024."
}
```

> Bloqueado se referenciado por contratos activos.

---

### 5.5 Tipos de Ausência (`/leave-types`)

Os campos `deducts_balance` e `requires_approval` alteram completamente o fluxo de processamento do pedido de ausência.

**Seed padrão:** `FERIAS`, `DOENCA`, `MATERNIDADE`, `PATERNIDADE`, `LUTO`, `CASAMENTO`.

```http
POST /leave-types
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

### 5.6 Subtipos de Licença/Mobilidade (`/leave-mobility-subtypes`)

Distinguem licenças (sem vencimento, parental, formação) de mobilidades (comissão de serviço, requisição, destacamento).

```http
POST /leave-mobility-subtypes
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
| `canSelfSubmit` | Se `true`, o próprio colaborador pode submeter via `/me/leaves-mobilities`. |

---

### 5.7 Tipos de Documento (`/document-types`)

Controlam que extensões de ficheiro são aceites no upload.

```http
POST /document-types
Content-Type: application/json

{
  "code": "CNI",
  "name": "Cartão Nacional de Identidade",
  "categoryOptionKey": "PESSOAL",
  "allowedExtensions": "pdf,jpg,png"
}
```

---

### 5.8 Feriados (`/public-holidays`)

Usados no cálculo de dias úteis em pedidos de ausência. Feriados nacionais aplicam-se a todos; feriados municipais aplicam-se segundo o concelho da unidade orgânica do funcionário.

```http
POST /public-holidays
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

### 6.1 Unidades Orgânicas (`/organizational-units`)

Modelam a hierarquia da organização: Direção → Departamento → Divisão → Secção. A raiz tem `parentUnitId` a `null`.

```http
POST /organizational-units
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
POST /organizational-units
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

### 6.2 Cargos / Jobs (`/jobs`)

Designação oficial do cargo (ex: Diretor de Serviços, Coordenador, Técnico Superior).

```http
POST /jobs
Content-Type: application/json

{
  "code": "TECNICO_SUPERIOR",
  "name": "Técnico Superior",
  "description": "Técnico de nível superior com funções de análise e estudo."
}
```

---

### 6.3 Funções (`/functions`)

Função efectivamente exercida pelo colaborador dentro do cargo.

```http
POST /functions
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
POST /careers
Content-Type: application/json

{
  "code": "TECNICO_SUPERIOR_I",
  "name": "Carreira de Técnico Superior — Nível I",
  "description": "Conforme PCFR, Decreto-Lei 4/2024."
}
```

Listar as categorias de uma carreira:

```http
GET /careers/{careerId}/categories
```

---

### 7.2 Categorias (`/categories`)

Dependem de uma Carreira. O par `(career_id, code)` é único.

```http
POST /categories
Content-Type: application/json

{
  "careerId": "uuid-da-carreira",
  "code": "TSP",
  "name": "Técnico Superior Principal"
}
```

Listar os escalões de uma categoria:

```http
GET /categories/{categoryId}/grades
```

---

### 7.3 Escalões (`/grades`)

Dependem de uma Categoria. O par `(category_id, grade_number)` é único.

```http
POST /grades
Content-Type: application/json

{
  "categoryId": "uuid-da-categoria",
  "gradeNumber": 1,
  "name": "Escalão 1",
  "salaryIndex": 285.5
}
```

> `category_id` é imutável após criação.  
> `DELETE` é bloqueado se o escalão estiver referenciado por enquadramentos activos.

---

## 8. Fase 4 — Funcionário e Dossier

### 8.1 Criar o Funcionário (`/employees`)

Este é o passo central. Requer que já existam: **Estado do Trabalhador**, **Situação Profissional** e as opções de **Sexo**, **Estado Civil** e **Nacionalidade** em `option_entity`.

```http
POST /employees
Content-Type: application/json

{
  "fullName": "Alex Jailson Barbosa Andrade",
  "nif": "17361994",
  "birthDate": "1990-03-15",
  "sexOptionKey": "M",
  "maritalStatusOptionKey": "CASADO",
  "nationalityOptionKey": "CV",
  "admissionDate": "2018-09-01",
  "workerStateId": "uuid-do-estado-active",
  "professionalSituationId": "uuid-da-situacao-efetivo",
  "email": "alex.andrade@ingt.gov.cv",
  "phone": "+238 261 2345",
  "nib": "CV64000300004569832014185",
  "addressStreet": "Rua da Independência, Nº 12",
  "addressIslandOptionKey": "SANTIAGO",
  "addressConcelhoOptionKey": "PRAIA"
}
```

**Regras de negócio na criação:**

| Campo | Regra |
|---|---|
| `nif` | Único no sistema; imutável após criação. |
| `nib` | 21 dígitos; único. |
| `birthDate` | Funcionário deve ter ≥ 18 anos. |
| `admissionDate` | Não pode ser posterior à data actual. |
| `sexOptionKey` | Apenas `M` ou `F`. |

---

### 8.2 Contrato do Funcionário (`/employees/{id}/contracts`)

**Depende de:** Funcionário + Tipo de Contrato.

Ao criar um novo contrato, o anterior (se existir) é **encerrado automaticamente** (`end_date = novo_start_date − 1 dia`, `is_current = false`). Este comportamento é garantido por trigger na base de dados.

```http
POST /employees/{employeeId}/contracts
Content-Type: application/json

{
  "contractTypeId": "uuid-do-tipo-ctfp-termo-certo",
  "startDate": "2018-09-01",
  "endDate": "2020-08-31",
  "legalBase": "Despacho Nº 15/2018 — Boletim Oficial Nº 35/2018",
  "notes": "Contrato inicial por 2 anos."
}
```

Para renovar / mudar tipo de contrato, basta criar um novo — o anterior fecha automaticamente:

```http
POST /employees/{employeeId}/contracts
Content-Type: application/json

{
  "contractTypeId": "uuid-do-tipo-nomeacao-definitiva",
  "startDate": "2020-09-01",
  "legalBase": "Despacho Nº 22/2020"
}
```

Consultar o histórico completo:

```http
GET /employees/{employeeId}/contracts
```

---

### 8.3 Enquadramento Profissional (`/employees/{id}/professional-assignments`)

**Depende de:** Funcionário + Carreira + Categoria (da carreira) + Escalão (da categoria) + Cargo (opcional) + Função (opcional).

O trigger `fn_validate_professional_assignment` valida automaticamente que `categoryId` pertence à `careerId` e que `gradeId` pertence à `categoryId`. Se a hierarquia for incoerente, recebe HTTP 422.

Tal como nos contratos, ao criar um novo enquadramento, o anterior é **encerrado automaticamente**.

```http
POST /employees/{employeeId}/professional-assignments
Content-Type: application/json

{
  "careerId": "uuid-da-carreira",
  "categoryId": "uuid-da-categoria-tsp",
  "gradeId": "uuid-do-escalao-1",
  "jobId": "uuid-do-cargo-tecnico-superior",
  "functionId": "uuid-da-funcao-coord-projeto",
  "startDate": "2018-09-01",
  "legalBase": "Despacho Nº 15/2018",
  "notes": "Enquadramento inicial."
}
```

Para registar uma progressão (promoção de categoria ou escalão):

```http
POST /employees/{employeeId}/professional-assignments
Content-Type: application/json

{
  "careerId": "uuid-da-carreira",
  "categoryId": "uuid-da-nova-categoria",
  "gradeId": "uuid-do-novo-escalao",
  "startDate": "2024-01-01",
  "legalBase": "Despacho de Progressão Nº 5/2024"
}
```

---

### 8.4 Colocação em Unidade Orgânica (`/employees/{id}/unit-assignments`)

**Depende de:** Funcionário + Unidade Orgânica.

Um funcionário pode ter múltiplas atribuições, mas apenas uma principal (`isPrimary = true`).

```http
POST /employees/{employeeId}/unit-assignments
Content-Type: application/json

{
  "unitId": "uuid-da-unidade-dsgt",
  "isPrimary": true,
  "startDate": "2018-09-01"
}
```

Para encerrar uma colocação (ex: ao transferir para outra unidade):

```http
PUT /employees/{employeeId}/unit-assignments/{assignmentId}/close
```

---

### 8.5 Dependentes (`/employees/{id}/dependents`)

**Depende de:** Funcionário. O campo `relationshipOptionKey` usa o grupo `RELATIONSHIP_TYPE` de `option_entity`.

```http
POST /employees/{employeeId}/dependents
Content-Type: application/json

{
  "fullName": "Maria Andrade",
  "birthDate": "2015-06-20",
  "relationshipOptionKey": "FILHO",
  "nif": "11223344"
}
```

---

### 8.6 Habilitações Literárias (`/employees/{id}/qualifications`)

**Depende de:** Funcionário. O campo `levelOptionKey` usa o grupo `QUALIFICATION_LEVEL`.

```http
POST /employees/{employeeId}/qualifications
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

> `documentId` refere um documento previamente carregado via `POST /documents` (ver [secção 11](#11-api-de-documentos--ficheiros)).

---

### 8.7 Formações Profissionais (`/employees/{id}/trainings`)

**Depende de:** Funcionário. `typeOptionKey` usa o grupo `TRAINING_TYPE`.

```http
POST /employees/{employeeId}/trainings
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

### 8.8 Processos Disciplinares (`/employees/{id}/disciplinary-processes`)

**Acesso restrito:** `ROLE_HR_ADMIN` e `ROLE_SYSTEM_ADMIN`.

```http
POST /employees/{employeeId}/disciplinary-processes
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

**Depende de:** Funcionário + Tipo de Ausência + (opcional) Documento de suporte.

O ciclo de vida de um pedido é: `PENDING → APPROVED | REJECTED → CANCELLED`.

**1. Submeter um pedido (pelo RH ou pelo próprio via `/me`):**

```http
POST /leave-requests
Content-Type: application/json

{
  "employeeId": "uuid-do-funcionario",
  "leaveTypeId": "uuid-do-tipo-ferias",
  "startDate": "2026-08-10",
  "endDate": "2026-08-21",
  "justification": "Período de férias anuais.",
  "attachmentDocumentId": null
}
```

**Regras aplicadas automaticamente:**
- `endDate ≥ startDate`.
- Cálculo de dias úteis exclui sábados, domingos e feriados (`public_holidays`).
- Se `deducts_balance = true`: verifica se `dias_pedido ≤ saldo_disponível`.
- Se `requires_approval = false`: o pedido vai direto para `APPROVED`.
- Não são permitidos pedidos sobrepostos para o mesmo funcionário (excepto `CANCELLED` ou `REJECTED`).

**2. Aprovar (pela chefia — `ROLE_CHEFIA`):**

```http
PUT /leave-requests/{id}/approve
```

**3. Rejeitar com justificação:**

```http
PUT /leave-requests/{id}/reject
Content-Type: application/json

{
  "rejectionReason": "Período de maior afluência de trabalho. Reagendar para setembro."
}
```

**4. Cancelar (apenas pedidos em `PENDING`):**

```http
PUT /leave-requests/{id}/cancel
```

**Consultar saldo de ausências de um funcionário:**

```http
GET /employees/{employeeId}/leave-balances?year=2026
```

**Ajustar saldo manualmente (ROLE_HR_ADMIN):**

```http
PUT /employees/{employeeId}/leave-balances/{balanceId}
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
POST /leaves-mobilities
Content-Type: application/json

{
  "employeeId": "uuid-do-funcionario",
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
POST /leaves-mobilities
Content-Type: application/json

{
  "employeeId": "uuid-do-funcionario",
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
PUT /leaves-mobilities/{id}/approve
```

**4. Encerrar a mobilidade:**

Em mobilidades temporárias, o encerramento **restaura a atribuição orgânica anterior** do colaborador.

```http
PUT /leaves-mobilities/{id}/close
```

---

### 9.3 Recibos de Vencimento

```http
POST /employees/{employeeId}/payroll-slips
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
- `(employee_id, period_month, period_year)` é único.
- `grossSalary > 0`, `netSalary > 0`, `netSalary ≤ grossSalary`.

---

## 10. Área Reservada do Colaborador (`/me`)

Todos os endpoints `/me` são restritos ao colaborador autenticado. O `employee_id` é extraído automaticamente do JWT — não é possível consultar dados de outro colaborador.

| Endpoint | Descrição |
|---|---|
| `GET /me/profile` | Perfil completo: dados pessoais, enquadramento actual, cargo, função, unidade. |
| `GET /me/leave-requests` | Os meus pedidos de ausência (filtros: `status`, `leaveTypeId`, `year`). |
| `GET /me/leave-balances` | Os meus saldos de ausência por tipo. |
| `POST /me/leave-requests` | Submeter pedido de ausência. |
| `PUT /me/leave-requests/{id}/cancel` | Cancelar pedido próprio (apenas `PENDING`). |
| `GET /me/leaves-mobilities` | As minhas licenças e mobilidades. |
| `POST /me/leaves-mobilities` | Submeter licença/mobilidade (apenas subtipos com `canSelfSubmit = true`). |
| `GET /me/payroll-slips` | Os meus recibos (filtros: `periodYear`, `periodMonth`). |
| `GET /me/payroll-slips/{id}/download` | Download do PDF do recibo. |
| `GET /me/documents` | Os meus documentos pessoais. |
| `GET /me/documents/{id}/download` | Download de documento pessoal. |

**Restrições:**
- O colaborador deve ter `is_active = true`; se estiver inactivo, recebe HTTP 403.
- Avaliações de desempenho (`/me/external/evaluations`) são servidas por integração com o SAD externo — apenas avaliações concluídas/homologadas são visíveis.

---

## 11. API de Documentos / Ficheiros

A tabela `documents` é polimórfica: pode associar ficheiros ao funcionário directamente ou a qualquer entidade (pedido de ausência, formação, processo disciplinar, etc.).

### Upload de ficheiro

```http
POST /documents
Content-Type: multipart/form-data

file: [binário do ficheiro]
documentTypeId: uuid-do-tipo-cni
employeeId: uuid-do-funcionario
description: "Cópia do CNI frente e verso"
```

**Resposta:**

```json
{
  "id": "uuid-do-documento",
  "fileName": "cni_alex.pdf",
  "documentType": "CNI",
  "mimeType": "application/pdf",
  "sizeBytes": 245680,
  "uploadedAt": "2026-04-22T10:14:00Z",
  "employeeId": "uuid-do-funcionario",
  "storageKey": "employees/{id}/cni_alex.pdf"
}
```

**Limites:** 10 MB por ficheiro. Extensões aceites definidas pelo `document_type.allowedExtensions`.

### Download

```http
GET /documents/{documentId}/download
```

Devolve o ficheiro com `Content-Type` e `Content-Disposition` apropriados. Gerada uma URL pré-assinada para o MinIO — o ficheiro não é enviado directamente pelo servidor.

### Listar documentos de um funcionário

```http
GET /employees/{employeeId}/documents?documentTypeId=uuid&referenceEntity=leave_requests
```

### Associar documento a uma entidade (polimorfismo)

Após o upload, pode associar o documento a um pedido de ausência, formação, etc.:

```http
PUT /documents/{documentId}/reference
Content-Type: application/json

{
  "referenceEntity": "leave_requests",
  "referenceId": "uuid-do-pedido-de-ausencia"
}
```

---

## 12. Referência Rápida de Endpoints

### Parametrizações

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/reference/options?ccode={code}` | Listar opções de um grupo |
| `POST` | `/reference/options` | Criar opção |
| `PUT` | `/reference/options/{id}` | Actualizar opção |
| `DELETE` | `/reference/options/{id}` | Desativar opção |
| `POST` | `/reference/options/{id}/activate` | Reativar opção |
| `GET/POST/PUT/DELETE` | `/worker-states/{id?}` | Estados do trabalhador |
| `GET/POST/PUT/DELETE` | `/professional-situations/{id?}` | Situações profissionais |
| `GET/POST/PUT/DELETE` | `/contract-types/{id?}` | Tipos de contrato |
| `GET/POST/PUT/DELETE` | `/leave-types/{id?}` | Tipos de ausência |
| `GET/POST/PUT/DELETE` | `/leave-mobility-subtypes/{id?}` | Subtipos de licença/mobilidade |
| `GET/POST/PUT/DELETE` | `/document-types/{id?}` | Tipos de documento |
| `GET/POST/PUT/DELETE` | `/public-holidays/{id?}` | Feriados |

### Estrutura Organizacional

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/organizational-units` | Listar unidades (filtros: `parentUnitId`, `unitType`, `isActive`) |
| `GET` | `/organizational-units/{id}` | Detalhe de unidade |
| `POST` | `/organizational-units` | Criar unidade |
| `PUT` | `/organizational-units/{id}` | Actualizar unidade |
| `DELETE` | `/organizational-units/{id}` | Desativar unidade |
| `GET/POST/PUT/DELETE` | `/jobs/{id?}` | Cargos |
| `GET/POST/PUT/DELETE` | `/functions/{id?}` | Funções |

### Carreiras

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/careers` | Listar carreiras |
| `GET` | `/careers/{id}/categories` | Categorias de uma carreira |
| `POST` | `/careers` | Criar carreira |
| `GET` | `/categories?careerId={id}` | Listar categorias |
| `GET` | `/categories/{id}/grades` | Escalões de uma categoria |
| `POST` | `/categories` | Criar categoria (requer `careerId`) |
| `GET` | `/grades?categoryId={id}` | Listar escalões |
| `POST` | `/grades` | Criar escalão (requer `categoryId`) |

### Funcionários

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/employees` | Listar funcionários (filtros: `search`, `unitId`, `workerStateId`, `careerId`) |
| `GET` | `/employees/{id}` | Detalhe completo |
| `POST` | `/employees` | Criar funcionário |
| `PUT` | `/employees/{id}` | Actualizar (NIF imutável) |
| `DELETE` | `/employees/{id}` | Desativar (bloqueado se pedidos PENDING) |
| `GET` | `/employees/{id}/contracts` | Histórico de contratos |
| `POST` | `/employees/{id}/contracts` | Novo contrato (encerra o anterior) |
| `GET` | `/employees/{id}/professional-assignments` | Histórico de enquadramentos |
| `POST` | `/employees/{id}/professional-assignments` | Novo enquadramento (encerra o anterior) |
| `GET` | `/employees/{id}/unit-assignments` | Colocações orgânicas |
| `POST` | `/employees/{id}/unit-assignments` | Nova colocação |
| `PUT` | `/employees/{id}/unit-assignments/{aid}/close` | Encerrar colocação |
| `GET/POST/PUT/DELETE` | `/employees/{id}/dependents/{did?}` | Dependentes |
| `GET/POST/PUT/DELETE` | `/employees/{id}/qualifications/{qid?}` | Habilitações |
| `GET/POST/PUT/DELETE` | `/employees/{id}/trainings/{tid?}` | Formações |
| `GET/POST/PUT/DELETE` | `/employees/{id}/disciplinary-processes/{pid?}` | Processos disciplinares |
| `GET` | `/employees/{id}/documents` | Documentos do funcionário |
| `GET` | `/employees/{id}/leave-balances` | Saldos de ausência |
| `PUT` | `/employees/{id}/leave-balances/{bid}` | Ajustar saldo |
| `GET` | `/employees/{id}/leaves-mobilities` | Licenças/mobilidades do funcionário |
| `GET` | `/employees/{id}/payroll-slips` | Recibos |
| `POST` | `/employees/{id}/payroll-slips` | Registar recibo |

### Pedidos de Ausência

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/leave-requests` | Listar pedidos (filtros: `employeeId`, `status`, `leaveTypeId`) |
| `GET` | `/leave-requests/{id}` | Detalhe |
| `POST` | `/leave-requests` | Submeter pedido |
| `PUT` | `/leave-requests/{id}/approve` | Aprovar |
| `PUT` | `/leave-requests/{id}/reject` | Rejeitar (requer `rejectionReason`) |
| `PUT` | `/leave-requests/{id}/cancel` | Cancelar |

### Licenças e Mobilidades

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/leaves-mobilities` | Listar (filtros: `employeeId`, `recordType`, `status`) |
| `POST` | `/leaves-mobilities` | Criar |
| `PUT` | `/leaves-mobilities/{id}/approve` | Aprovar (trigger fn_apply_mobility) |
| `PUT` | `/leaves-mobilities/{id}/reject` | Rejeitar |
| `PUT` | `/leaves-mobilities/{id}/close` | Encerrar (restaura colocação anterior se temporária) |
| `PUT` | `/leaves-mobilities/{id}/cancel` | Cancelar |

### Documentos

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/documents` | Upload (multipart/form-data) |
| `GET` | `/documents/{id}/download` | Download |
| `PUT` | `/documents/{id}/reference` | Associar a entidade |

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

*Documento gerado em Maio de 2026 — Módulo RH v4.0 — SIPPROG/INGT*
