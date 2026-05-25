---
title: Especificação Técnica — Módulo de Recursos Humanos v4.0
---

> **Módulo de Recursos Humanos**
>
> *Sistema Integrado de Planeamento, Procedimentos e Gestão (SIPPROG)*
>
> COUNTRY: Cabo Verde
>
> PROJECT: SIPPROG — Sistema de Informação para Modernização do INGT
>
> **Apresentado pelo Consultor:** TA Digital

*Abril 2026*

---

## Identificação do Documento

| Campo | Valor |
|---|---|
| **Nº de Identificação** | ET-RH-BCK-V4.0 |
| **Dono** | INGT — Instituto Nacional de Gestão do Território |
| **Autor** | TA Digital |
| **Contribuintes** | Equipa de desenvolvimento TA, Departamento de RH do INGT |
| **Versão** | 4.0 |
| **Data** | Abril de 2026 |
| **Status** | Draft |

---

## Histórico de Versões

| Versão | Data | Autor | Descrição |
|---|---|---|---|
| 1.0 | 22-04-2026 | TA Digital | Reestruturação conforme menu SIPPROG: Cargos e Funções movidos para Estrutura Organizacional; Mobilidade integrada em Colaboradores; Avaliação de Desempenho tratada como ponto de integração externo. |
| 3.0 | Abril 2026 | TA Digital | Consolidação dos módulos; introdução dos históricos independentes (contratos, enquadramento, colocação); modelo de documentos polimórfico; endpoints de referência /reference/*. |
| 4.0 | Abril 2026 | TA Digital | Adoção do Modelo Relacional v4.0: migração de lookups sem lógica para `option_entity`; separação clara entre `employee_contracts`, `employee_professional_assignments` e `employee_unit_assignments`; novos sub-recursos do dossier: dependentes, habilitações, formações, processos disciplinares; refatoração da secção Parametrizações conforme decisão OptionEntity; Modelo de Dados actualizado para 26 tabelas. |
| 4.1 | Maio 2026 | TA Digital | Actualização conforme implementação real: campos de identificação do funcionário actualizados (`nomeCompleto`, `genero`, `estadoCivil`, `numeroDocumento`, `document_type_id`, `ilha`, `concelho`, `localidade`); `workerStateId` removido do payload de criação (atribuído automaticamente a ATIVO); `professionalSituationId` nullable até ao primeiro contrato; parâmetros de filtro `GET /funcionarios` actualizados para UUIDs. |
| 4.2 | Maio 2026 | TA Digital | Contrato: adicionados `status` (ATIVO/SUSPENSO/CESSADO) e `renewalCount`; novos endpoints `PUT /contratos/{id}/suspend` e `PUT /contratos/{id}/activate`; corrigido esquema `t_contrato` com nomes reais de tabela/colunas; lógica de fecho do contrato anterior documentada como aplicacional (não trigger de BD); secção 9.3 actualizada. |
| 4.3 | Maio 2026 | TA Digital | Contrato: adicionados `regimeTrabalho` (enum `RegimeTrabalho`, base legal LGTFP art.123-129) e `percentagemTempo`; validação dinâmica via `RegimeTrabalho.codigosValidos()`. Nova entidade `t_dados_bancarios` (banco/conta/IBAN/INPS) com API completa em secção 2.11. Manifestos `.igrpstudio` actualizados: `RegimeTrabalho.json`, `ContratoEntity.json`, `DadosBancariosEntity.json`. |
| 4.4 | Maio 2026 | TA Digital | Normalização de paths REST: todos os sub-recursos de funcionário migrados para prefixo `/funcionarios/{funcionarioId}/X` (contratos, enquadramentos, dependentes, qualificações, recibos, dados-bancários, documentos); `funcionarioId` removido dos request bodies e passado via path variable; endpoints proxy em `FuncionarioController` eliminados; tabela `VALID_CCODES` (Option) alargada com `CAREER_REGIME`, `BANCO` e `WORK_REGIME`; manifestos `.igrpstudio` actualizados. |
| 4.5 | Maio 2026 | TA Digital | Alinhamento ubíquo e nomenclatura de tabelas: `professional_situations` → `t_vinculo_laboral` em todas as camadas; `professional_situation_id` removido de `t_funcionario`; `contract_types.professional_situation_id` → `vinculo_laboral_id`; prefixo `t_` adicionado a todas as tabelas; nomes JPA reais documentados (`t_employee_professional_assignments`, `t_employee_unit_assignments`, `t_qualificacao`, `t_training`, `t_disciplinary_process`, etc.); `nullable = false` removido dos `@Column` para compatibilidade com `ddl-auto=update`; secção `GET /vinculos-laborais` substituiu `/professional-situations`. |

---

## Notas de Migração: o que mudou de v3.0 para v4.0

Esta secção resume, em forma de checklist, as alterações concretas entre a v3.0 e a v4.0 da especificação. Serve de guia para quem já conhece a v3 e precisa identificar rapidamente o impacto da nova versão na implementação.

### 1. Parametrizações — migração para OptionEntity

Catálogos que eram tabelas dedicadas em v3 e passaram a ser servidos por `option_entity`:

| v3.0 (tabela dedicada) | v4.0 (OptionEntity) | `ccode` |
|---|---|---|
| `marital_statuses` | `option_entity` | `MARITAL_STATUS` |
| (campo `sexo` enum) | `option_entity` | `SEX` |
| (campo `nacionalidade` string) | `option_entity` | `NATIONALITY` |
| (campo `unitType` enum) | `option_entity` | `UNIT_TYPE` |
| (categoria de ausência hardcoded) | `option_entity` | `LEAVE_CATEGORY` |
| (categoria de documento hardcoded) | `option_entity` | `DOC_CATEGORY` |
| (sem equivalente) | `option_entity` | `QUALIFICATION_LEVEL`, `RELATIONSHIP_TYPE`, `TRAINING_TYPE`, `ISLAND`, `CONCELHO` |

Catálogos que **continuam como tabelas dedicadas** porque têm flags que alteram o comportamento do sistema:

- `t_worker_state` (flag `is_core` protege estados núcleo)
- `t_vinculo_laboral` (EFETIVO vs CONTRATADO têm regras PCFR distintas)
- `t_contract_type` (tem historial próprio em `t_contrato`)
- `t_tipo_documento` (`allowed_extensions` valida upload)
- `t_leave_type` (`deducts_balance`, `requires_approval` alteram fluxo)
- `t_leave_mobility_subtype` (`affects_pay`, `counts_for_seniority`, `can_self_submit`)

**Endpoint novo:** `GET /reference/options?ccode={code}` substitui os endpoints individuais como `GET /marital-statuses`. Ver secção 5.9. Os endpoints antigos podem ser mantidos como aliases durante uma fase de transição.

### 2. Históricos independentes — separação explícita

Na v3, o enquadramento profissional misturava progressão de carreira com tipo de contrato. Na v4 são **três tabelas independentes**:

| Tabela | Muda quando | Marcador de actual |
|---|---|---|
| `employee_contracts` | Renovação de CTFP, mudança para nomeação definitiva, comissão de serviço | `is_current = true` |
| `employee_professional_assignments` | Promoção de categoria, progressão de escalão, mudança de cargo/função | `is_current = true` |
| `employee_unit_assignments` | Mobilidade interna, destacamento, cedência | `end_date IS NULL` |

**Impacto na API:** novo sub-recurso `/funcionarios/{id}/contratos` (secção 2.3). O enquadramento profissional deixa de carregar o tipo de contrato.

### 3. Novos sub-recursos do dossier (secção 2.10)

Tabelas e endpoints novos, ausentes em v3:

| Recurso | Tabela | Endpoint base |
|---|---|---|
| Dependentes | `employee_dependents` | `/funcionarios/{id}/dependentes` |
| Habilitações Literárias | `qualifications` | `/funcionarios/{id}/qualificacoes` |
| Formações Profissionais | `trainings` | `/funcionarios/{id}/formacoes` |
| Processos Disciplinares | `disciplinary_processes` | `/funcionarios/{id}/processos-disciplinares` |

### 4. Documentos polimórficos

A v3 tinha documentos associados apenas ao funcionário. A v4 introduz polimorfismo controlado em `documents`:

```
reference_entity  → 'leave_requests', 'trainings', 'disciplinary_processes', ...
reference_id      → ID do registo associado
```

Isto unifica os dois conceitos do modelo INPS (`DocumentoPessoalEntity` + `DocumentoEntity`) numa única tabela. Adicionados os campos: `storage_key`, `mime_type`, `size_bytes`, `uploaded_by`. Removida a duplicação de tabelas de anexos por entidade.

### 5. Endereço e contacto no `employees`

A v3 tratava endereço como entidade separada. A v4 inline no `employees`: `address_street`, `address_island`, `address_concelho`. Justificação: cada funcionário tem **um** endereço corrente; histórico de endereços não é requisito do dossier.

### 6. Funcionário com fotografia

Novo campo `photo_document_id` em `employees` referencia directamente a tabela `documents` (em vez de campo `fotografia` string que guardava URL/caminho).

### 7. Modelo de Dados — secção 8 reescrita

| v3 | v4 |
|---|---|
| 7 domínios funcionais | **9 blocos funcionais, 26 tabelas** |
| Diagrama em ficheiro PNG anexo | Diagrama Mermaid embebido em `Modelo_Relacional_RH_v4.0.md` |
| Descrição em tabelas HTML | Descrição em Markdown + referência cruzada para o documento de modelo relacional |

### 8. Triggers — adições

O encerramento do contrato anterior ao criar um novo continua a ser feito no `CreateContratoCommandHandler` (lógica aplicacional). Não existe trigger de BD para este efeito — a consistência é garantida pela aplicação.

### 9. Sumário de impacto na implementação

| Área | Acção |
|---|---|
| Migrations | Drop de `marital_statuses`; seed inicial de `option_entity` para os 11 grupos. |
| Migrations | Criar `employee_contracts`, `employee_dependents`, `qualifications`, `trainings`, `disciplinary_processes`, `public_holidays`. |
| Migrations | Acrescentar campos de endereço e `photo_document_id` em `employees`. |
| Migrations | Reestruturar `documents` (adicionar `storage_key`, `mime_type`, `size_bytes`, `reference_entity`, `reference_id`). |
| Handler | `CreateContratoCommandHandler` encerra contrato anterior e calcula `renewalCount`. |
| Endpoints | Manter aliases temporários para `/marital-statuses` etc. apontando para `/reference/options`. |
| OpenAPI | Regenerar contratos REST das secções 2.3, 2.10 e 5.9. |

### 10. O que **não** mudou

- Estrutura de Carreiras e Progressão (`careers → categories → grades`).
- Estrutura Organizacional (`organizational_units`, `jobs`, `functions`).
- Lógica de pedidos de ausência e aprovação.
- Integração com o SAD (Capítulo 6).
- Perfis de acesso e modelo de autorização.

---

## Índice

- 0\. [Introdução](#0-introdução)
  - 0.1 Visão Geral da API
  - 0.2 Base URL e Versionamento
  - 0.3 Autenticação e Perfis de Acesso
  - 0.4 Convenções Gerais
- 1\. [Módulo Meu Perfil](#1-módulo-meu-perfil)
- 2\. [Módulo Colaboradores](#2-módulo-colaboradores)
  - 2.1 Funcionários — CRUD
  - 2.2 Contratos do Funcionário
  - 2.3 Enquadramento Profissional
  - 2.4 Atribuição a Unidades Organizacionais
  - 2.5 Pedidos de Ausência
  - 2.6 Saldos de Ausências
  - 2.7 Licenças e Mobilidade
  - 2.8 Recibos de Vencimento
  - 2.9 Dossier do Funcionário (Dependentes, Habilitações, Formações, Processos Disciplinares)
  - 2.10 Documentos do Colaborador
  - 2.11 Regras e Validações de Negócio
- 3\. [Módulo Estrutura Organizacional](#3-módulo-estrutura-organizacional)
- 4\. [Módulo Carreiras e Progressão](#4-módulo-carreiras-e-progressão)
- 5\. [Módulo Parametrizações](#5-módulo-parametrizações)
- 6\. [Integração com o Sistema de Avaliação de Desempenho](#6-integração-com-o-sistema-de-avaliação-de-desempenho)
- 7\. [API de Anexos / Documentos](#7-api-de-anexos--documentos)
- 8\. [Modelo de Dados](#8-modelo-de-dados)
- 9\. [Triggers e Funções de Base de Dados](#9-triggers-e-funções-de-base-de-dados)
- 10\. [Considerações de Implementação](#10-considerações-de-implementação)

---

# 0. Introdução

## 0.1 Visão Geral da API

A API do Módulo de Recursos Humanos do SIPPROG (Sistema de Informação do Pessoal e Progressões) constitui o backend que disponibiliza as funcionalidades de gestão integrada de funcionários do INGT, organizada conforme a estrutura de menu do produto: Meu Perfil, Colaboradores, Estrutura Organizacional, Carreiras e Progressão e Parametrizações. A funcionalidade de Avaliação de Desempenho é externa ao módulo e é integrada por consumo, tal como descrito no Capítulo 6.

A API foi concebida segundo o paradigma REST, utilizando JSON como formato de troca de dados, e disponibiliza operações para o cadastro de colaboradores, organização hierárquica em unidades orgânicas, definição de cargos e funções, gestão de carreiras (categorias e escalões), processos de ausência (férias, doença, licenças e mobilidade), recibos de vencimento, dossier completo do colaborador (habilitações, formações, processos disciplinares, dependentes) e a área reservada do colaborador. Todas as operações de escrita são auditadas por triggers de base de dados que registam o histórico completo de alterações na tabela `change_history`.

**Alteração v4.0:** O modelo de dados foi reestruturado em 26 tabelas organizadas em 9 blocos funcionais. Os lookups sem lógica de negócio (estado civil, sexo, nacionalidade, ilha, tipo de unidade orgânica, etc.) passaram para `option_entity`. As tabelas com flags de comportamento mantêm-se dedicadas. Ver Capítulo 8 e o documento `Modelo_Relacional_RH_v4.0.md`.

## 0.2 Base URL e Versionamento

```
https://api.sipprog.ingt.gov.cv/v1/rh
```

O versionamento da API é feito por prefixo no caminho (`/v1`, `/v2`, …). Versões maiores são introduzidas apenas quando há quebra de retrocompatibilidade. Alterações compatíveis (novos campos opcionais, novos endpoints) são entregues sem alteração da versão.

## 0.3 Autenticação e Perfis de Acesso

A autenticação é baseada em JSON Web Tokens (JWT) emitidos pelo módulo de autenticação central do SIPPROG. Todas as requisições devem incluir o cabeçalho `Authorization` com o token Bearer. O token transporta o identificador do utilizador (`sub`), o `employee_id`, o perfil de acesso e a unidade orgânica de origem.

```
Authorization: Bearer {token}
```

| Perfil | Descrição |
|---|---|
| `ROLE_FUNCIONARIO` | Acesso à Área Reservada do Colaborador (`/me/*`) e leituras restritas. |
| `ROLE_CHEFIA` | Aprova pedidos de ausência e licenças/mobilidades dos seus subordinados. |
| `ROLE_HR_OPERATOR` | Operações correntes sobre colaboradores, ausências e enquadramentos. |
| `ROLE_HR_ADMIN` | Operações administrativas plenas: parametrizações, estrutura, carreiras, dossier. |
| `ROLE_SYSTEM_ADMIN` | Configurações de sistema, integrações e auditoria. |

## 0.4 Convenções Gerais

| Aspecto | Convenção |
|---|---|
| Formato de data | ISO-8601 (`YYYY-MM-DD` para datas; `YYYY-MM-DDTHH:mm:ss` para timestamps UTC). |
| Paginação | Parâmetros `page` (1-based) e `size` (default 20, máximo 100); resposta inclui `totalElements` e `totalPages`. |
| Ordenação | Parâmetro `sort=campo,asc\|desc`; suporta múltiplos campos. |
| Códigos HTTP | 200 OK, 201 Created, 204 No Content, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict, 422 Unprocessable Entity, 500 Internal Error. |
| Erros | Resposta JSON com `timestamp`, `status`, `error`, `message`, `path` e `fields[]` com erros de validação por campo. |
| Soft delete | Operações `DELETE` marcam `is_active = false`; não há remoção física exposta. |
| Auditoria | Todas as escritas registam `created_by`/`updated_by` e geram entrada em `change_history` via trigger. |
| Referências a Options | Os campos que referenciam `option_entity` guardam o `ckey` como string (ex: `"LICENCIATURA"`, `"CONJUGE"`). **Não** são UUIDs. O frontend obtém os valores disponíveis via `GET /reference/options?ccode={code}`. A API valida que o ckey enviado existe no ccode esperado antes de persistir. |

---

# 1. Módulo Meu Perfil

## 1.1 Visão Geral e Modelo de Autorização

O módulo Meu Perfil corresponde à área reservada do colaborador, acessível a partir do menu lateral do SIPPROG. Todos os endpoints estão sob o prefixo `/me` e exigem o perfil `ROLE_FUNCIONARIO`. O middleware de autenticação injeta automaticamente o `employee_id` a partir da claim `sub` do JWT, ignorando qualquer tentativa de envio de identificador de outro colaborador. Aplica-se sempre o princípio do mínimo privilégio: nenhum dado de terceiros é retornado. O acesso exige `is_active = TRUE`; colaboradores INACTIVE recebem HTTP 403.

## 1.2 Perfil do Colaborador

### GET /me/profile

Retorna a informação de cabeçalho apresentada na página de perfil: identificação pessoal, unidade orgânica principal, cargo e função atuais, dados de contacto e enquadramento profissional corrente.

**Resposta (200 OK)**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "fullName": "Alex Jailson Barbosa Andrade",
  "nif": "17361994",
  "email": "alex.andrade@ingt.gov.cv",
  "phone": "+238 261 2345",
  "currentJob": { "id": "uuid-cargo", "name": "Tecnico Superior" },
  "currentUnit": { "id": "uuid-unidade", "name": "Direccao de Servicos de Gestao Territorial" },
  "career": { "id": "uuid-carreira", "name": "Nivel Tecnico I" },
  "category": { "id": "uuid-categoria", "name": "Tecnico Superior Principal" },
  "grade": { "id": "uuid-escalao", "gradeNumber": 3 },
  "admissionDate": "2018-09-01",
  "workerState": "ATIVO"
}
```

## 1.3 Os Meus Recibos

### GET /me/recibos

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `periodYear` | integer | Não | Filtra por ano de referência. |
| `periodMonth` | integer | Não | Filtra por mês (1-12). |

### GET /me/recibos/{id}/download

Devolve o ficheiro PDF do recibo. Valida que o registo pertence ao colaborador autenticado.

## 1.4 As Minhas Férias e Ausências

### GET /me/leave-requests

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `status` | string | Não | `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`. |
| `leaveTypeId` | integer | Não | Filtra por tipo de ausência. |
| `year` | integer | Não | Filtra por ano. |

### GET /me/leave-balances

Devolve, por tipo de ausência que desconte saldo, o total atribuído, utilizado, em pendentes e disponível para o ano corrente.

### POST /me/leave-requests

Submeter pedido de ausência. Aplica as regras de negócio da secção 2.11.

### PUT /me/leave-requests/{id}/cancel

Cancelar pedido próprio (apenas estado PENDING).

## 1.5 As Minhas Licenças e Mobilidades

### GET /me/leaves-mobilities

Devolve a lista das licenças e mobilidades do próprio, ordenadas por data de início descendente.

### GET /me/leaves-mobilities/{id}

### POST /me/leaves-mobilities

Disponibilizado quando a parametrização do subtipo permite a auto-submissão pelo colaborador (`canSelfSubmit = true`). Caso contrário, o registo tem de ser criado pelo RH.

## 1.6 As Minhas Avaliações (consumo do sistema externo)

Esta secção é alimentada por integração com o Sistema de Avaliação de Desempenho (SAD), descrito no Capítulo 6.

### GET /me/external/evaluations

### GET /me/external/evaluations/{externalId}

### GET /me/external/objectives

## 1.7 Os Meus Documentos

### GET /me/documents

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `documentTypeId` | integer | Não | Filtra por tipo de documento. |

### GET /me/documents/{id}/download

## 1.8 Regras Específicas

| Regra | Descrição |
|---|---|
| Isolamento de dados | Todos os endpoints `/me` filtram por `employee_id = sub do token`, mesmo que outro identificador seja enviado pelo cliente. |
| Estado do colaborador | `is_active = TRUE` é obrigatório; colaboradores INACTIVE recebem HTTP 403. |
| Visibilidade de avaliações | Apenas avaliações concluídas/homologadas no SAD são devolvidas; rascunhos são omitidos. |
| Visibilidade de recibos | Apenas recibos com `is_active = TRUE` são devolvidos. |
| Auditoria | Todas as ações de escrita via `/me` são registadas em `change_history` com origem `self-service`. |

---

# 2. Módulo Colaboradores

## 2.1 Visão Geral

O módulo Colaboradores agrega o conjunto de operações relativas aos funcionários do INGT, abrangendo o cadastro, a vida profissional (contratos, enquadramento e atribuições orgânicas), os processos de ausência e licença, os recibos de vencimento, o dossier completo (habilitações, formações, processos disciplinares, dependentes) e os documentos pessoais. Todos os sub-recursos do dossier são acessíveis sob o prefixo `/funcionarios/{funcionarioId}/`.

## 2.2 Funcionários (Employees) — CRUD

### GET /funcionarios

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `nome` | string | Não | Pesquisa parcial por nome. |
| `nif` | string | Não | NIF exacto. |
| `workerStateId` | UUID | Não | Filtra por estado do trabalhador (UUID do registo em `worker_states`). |
| `unidadeOrganicaId` | UUID | Não | Filtra por unidade orgânica (via enquadramento actual). |
| `careerId` | UUID | Não | Filtra por carreira (via enquadramento actual). |
| `active` | boolean | Não | Filtra por `is_active`. Default: `true`. |
| `pagina` | string | Não | Página (0-based, default `0`). |
| `tamanho` | string | Não | Tamanho da página (default `20`). |

**Resposta (200 OK)**

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "numeroFuncionario": "F000001",
      "nomeCompleto": "Alex Jailson Barbosa Andrade",
      "nif": "17361994",
      "genero": "Masculino",
      "estadoCivil": "Solteiro",
      "nacionalidade": "CV",
      "documentTypeId": null,
      "numeroDocumento": null,
      "dataEmissaoDoc": null,
      "dataValidadeDoc": null,
      "email": "alex.andrade@ingt.gov.cv",
      "telefone": "+238 261 2345",
      "morada": "Achada Santo António",
      "ilha": "Santiago",
      "concelho": "Praia",
      "localidade": "Praia",
      "workerStateId": "aaaa-...",
      "dataAdmissao": "2018-09-01",
      "isActive": true
    }
  ],
  "totalElements": 132,
  "pageNumber": 0,
  "pageSize": 20,
  "totalPages": 7
}
```

### GET /funcionarios/{id}

Devolve o detalhe completo, incluindo enquadramento corrente, contrato corrente, unidade principal e contactos.

### POST /funcionarios

O estado do trabalhador (`workerStateId`) é atribuído automaticamente a `ATIVO` pelo sistema — não faz parte do payload de criação.

**Corpo da Requisição**

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `nomeCompleto` | string | Sim | Nome completo (máx. 200 caracteres). |
| `dataNascimento` | date | Sim | Data de nascimento (`YYYY-MM-DD`). |
| `genero` | string | Sim | Género (valor livre, ex: `Masculino`, `Feminino`). |
| `estadoCivil` | string | Sim | Estado civil (valor livre, ex: `Solteiro`, `Casado`). |
| `nif` | string | Sim | NIF único (máx. 20 caracteres). |
| `documentTypeId` | UUID | Não | ID do tipo de documento de identificação (FK→t_tipo_documento). |
| `numeroDocumento` | string | Não | Número do BI/Passaporte/outro (máx. 50 caract.); único no sistema. |
| `dataEmissaoDoc` | date | Não | Data de emissão do documento. |
| `dataValidadeDoc` | date | Não | Data de validade do documento. |
| `nacionalidade` | string | Não | Código de nacionalidade (ex: `CV`, `PT`). Default: `CV`. |
| `dataAdmissao` | date | Sim | Data de admissão. |
| `email` | string | Não | Email (máx. 200 caracteres); único no sistema. |
| `telefone` | string | Não | Telefone (máx. 30 caracteres). |
| `morada` | string | Não | Morada/endereço. |
| `ilha` | string | Não | Ilha (valor livre, máx. 100 caract., ex: `Santiago`, `São Vicente`). |
| `concelho` | string | Não | Concelho (valor livre, máx. 100 caract., ex: `Praia`, `Mindelo`). |
| `localidade` | string | Não | Localidade (máx. 100 caracteres). |

### PUT /funcionarios/{id}

Os campos `nif` e `numeroFuncionario` são imutáveis após a criação. As restantes propriedades aceitam os mesmos campos do `POST`.

### DELETE /funcionarios/{id}

Soft delete (`is_active = false`). Bloqueado se existirem pedidos PENDING.

---

## 2.3 Contratos do Funcionário

Historial independente do ciclo de vida contratual: nomeação definitiva, CTFP, comissão de serviço, etc. Não confundir com o enquadramento de carreira (secção 2.4).

Ao criar um novo contrato, o `CreateContratoCommandHandler` executa automaticamente:
1. Encerra o contrato anterior (`status = 'CESSADO'`, `is_current = false`, `end_date = startDate − 1 dia`, `terminationReason = 'SUBSTITUICAO'`), se existir.
2. Calcula `renewalCount`: incrementa relativamente ao contrato anterior se o tipo for renovável e o mesmo; caso contrário reinicia a 0.
3. O vínculo laboral implícito é classificado pelo `t_contract_type.vinculo_laboral_id` — não actualiza campos em `t_funcionario`.

### GET /funcionarios/{funcionarioId}/contratos

Devolve o histórico completo de contratos, ordenado por `start_date` descendente.

### GET /funcionarios/{funcionarioId}/contratos/{id}

### POST /funcionarios/{funcionarioId}/contratos

**Corpo da Requisição**

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `contractTypeId` | UUID | Sim | Tipo de contrato (`contract_types`). |
| `contractNumber` | string | Não | Nº do instrumento contratual (ex: CTFP/CFP). Distinto do despacho. Único na tabela. |
| `startDate` | date | Sim | Data de início (≥ data de admissão do funcionário). |
| `endDate` | date | Não | Data de fim (`null` = contrato activo; obrigatório para `CTFP_TERMO_CERTO`). |
| `legalBase` | string | Não | Nº de despacho / Boletim Oficial que autoriza o contrato. |
| `regimeTrabalho` | string | Não | Regime de trabalho (enum `RegimeTrabalho`): `TEMPO_COMPLETO`, `TEMPO_PARCIAL`, `ISENCAO_HORARIO`, `DEDICACAO_EXCLUSIVA`. |
| `percentagemTempo` | decimal | Cond. | Percentagem de tempo. Obrigatório se `regimeTrabalho = TEMPO_PARCIAL`; proibido nos restantes. |
| `notes` | text | Não | Observações. |

Ao criar um novo contrato, o anterior (se existir `is_current = true`) é encerrado automaticamente (`end_date = startDate − 1 dia`, `is_current = false`).

### PUT /funcionarios/{funcionarioId}/contratos/{id}

Actualiza campos editáveis (`endDate`, `legalBase`, `notes`). Os campos `contractTypeId` e `startDate` são imutáveis após criação.

### PUT /funcionarios/{funcionarioId}/contratos/{id}/close

Encerra manualmente o contrato activo (`status → CESSADO`, `is_current → false`).

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `endDate` | date | Sim | Data de fim do contrato. |
| `terminationReason` | string | Sim | Motivo de cessação (LGTFP): `CADUCIDADE`, `ACORDO_MUTUO`, `RESCISAO_UNILATERAL_ENTIDADE`, `APOSENTACAO`, `FALECIMENTO`, `DEMISSAO`. |
| `notes` | text | Não | Observações adicionais. |

### PUT /funcionarios/{funcionarioId}/contratos/{id}/suspend

Suspende o contrato activo (`status: ATIVO → SUSPENSO`). Tipicamente accionado durante uma licença sem vencimento. Devolve erro 409 se o contrato não estiver ATIVO.

### PUT /funcionarios/{funcionarioId}/contratos/{id}/activate

Reactiva um contrato suspenso (`status: SUSPENSO → ATIVO`). Devolve erro 409 se o contrato não estiver SUSPENSO.

**Campos de resposta comuns a GET e POST:**

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | UUID | Identificador do contrato. |
| `funcionarioId` | UUID | Identificador do funcionário. |
| `contractTypeId` | UUID | Tipo de contrato. |
| `contractNumber` | string | Nº do instrumento contratual. |
| `startDate` | date | Data de início. |
| `endDate` | date | Data de fim (`null` = sem prazo definido). |
| `terminationReason` | string | Motivo de cessação (preenchido ao encerrar). |
| `isCurrent` | boolean | Indica se é o contrato activo do funcionário. |
| `status` | string | Ciclo de vida: `ATIVO`, `SUSPENSO`, `CESSADO`. |
| `renewalCount` | integer | Nº de renovações consecutivas do mesmo tipo renovável. |
| `legalBase` | string | Nº de despacho / Boletim Oficial. |
| `notes` | text | Observações. |

---

## 2.4 Enquadramento Profissional do Colaborador

Materializa a relação entre o funcionário e a sua carreira, categoria, escalão, cargo e função num dado período. O sistema mantém historial completo garantindo apenas um enquadramento activo (`is_current = true`) por funcionário. As validações asseguram a consistência hierárquica carreira → categoria → escalão.

### GET /funcionarios/{funcionarioId}/enquadramentos

### GET /funcionarios/{funcionarioId}/enquadramentos/{id}

### POST /funcionarios/{funcionarioId}/enquadramentos

**Corpo da Requisição**

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `careerId` | integer | Sim | Carreira. |
| `categoryId` | integer | Sim | Categoria (deve pertencer à carreira — validado por trigger). |
| `gradeId` | integer | Sim | Escalão (deve pertencer à categoria — validado por trigger). |
| `jobId` | integer | Não | Cargo exercido. |
| `functionId` | integer | Não | Função exercida. |
| `startDate` | date | Sim | Data de início (≥ admissão). |
| `legalBase` | string | Não | Despacho ou base legal da progressão. |
| `notes` | text | Não | Observações. |

Ao criar um novo enquadramento, o anterior é encerrado automaticamente (`end_date = startDate − 1 dia`).

### PUT /funcionarios/{funcionarioId}/enquadramentos/{id}

---

## 2.5 Atribuição a Unidades Organizacionais

Um colaborador pode estar atribuído a uma ou mais unidades, sendo uma delas obrigatoriamente marcada como principal (`isPrimary = true`). As atribuições têm vigência (`startDate`, `endDate`).

### GET /funcionarios/{funcionarioId}/colocacoes

### POST /funcionarios/{funcionarioId}/colocacoes

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `unitId` | integer | Sim | Unidade orgânica. |
| `isPrimary` | boolean | Sim | Atribuição principal. |
| `startDate` | date | Sim | Data de início. |
| `endDate` | date | Não | Data de fim (vazio se em curso). |

### PUT /funcionarios/{funcionarioId}/colocacoes/{id}/close

---

## 2.6 Pedidos de Ausência (Leave Requests)

Suporta o ciclo de vida completo dos pedidos de ausência: submissão, aprovação ou rejeição pela chefia, e cancelamento.

### GET /leave-requests

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `employeeId` | integer | Não | Filtra por funcionário. |
| `approverId` | integer | Não | Filtra por aprovador. |
| `status` | string | Não | `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`. |
| `leaveTypeId` | integer | Não | Filtra por tipo. |
| `startDateFrom` | date | Não | Data inicial mínima. |
| `startDateTo` | date | Não | Data inicial máxima. |

### GET /leave-requests/{id}

### POST /leave-requests

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `employeeId` | integer | Sim | Funcionário requerente. |
| `leaveTypeId` | integer | Sim | Tipo de ausência. |
| `startDate` | date | Sim | Data de início. |
| `endDate` | date | Sim | Data de fim. |
| `justification` | string | Sim | Justificação textual. |
| `attachmentDocumentId` | integer | Não | Documento de suporte previamente carregado. |

### PUT /leave-requests/{id}/approve

### PUT /leave-requests/{id}/reject

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `rejectionReason` | string | Sim | Justificação obrigatória da rejeição. |

### PUT /leave-requests/{id}/cancel

---

## 2.7 Saldos de Ausências (Leave Balances)

### GET /funcionarios/{funcionarioId}/saldos-ausencia

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `year` | integer | Não | Ano de referência (default ano corrente). |

### PUT /funcionarios/{funcionarioId}/saldos-ausencia/{balanceId}

Permite ao RH ajustar o saldo (`assignedDays` e `usedDays`). Operação auditada.

---

## 2.8 Licenças e Mobilidade do Colaborador

Gestão integrada das licenças (sem vencimento, para formação, parental, etc.) e mobilidades (comissão de serviço, requisição, destacamento, mobilidade interna). O campo `recordType` discrimina o caso e o subtipo determina o comportamento detalhado.

### GET /leaves-mobilities

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `employeeId` | integer | Não | Filtra por colaborador. |
| `recordType` | string | Não | `LICENCA` ou `MOBILIDADE`. |
| `status` | string | Não | `PENDING`, `ACTIVE`, `CLOSED`, `CANCELLED`. |
| `subtypeId` | integer | Não | Filtra por subtipo. |

### GET /leaves-mobilities/{id}

### POST /leaves-mobilities

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `employeeId` | integer | Sim | Colaborador alvo. |
| `recordType` | string | Sim | `LICENCA` ou `MOBILIDADE`. |
| `subtypeId` | integer | Sim | Subtipo (ver `/reference/leave-mobility-subtypes`). |
| `startDate` | date | Sim | Data de início. |
| `endDate` | date | Não | Data de fim (condicional a `isTemporary`). |
| `isTemporary` | boolean | Sim | Indica se há reversão prevista. |
| `targetUnitId` | integer | Condicional | Unidade de destino (obrigatório em mobilidade). |
| `targetFunctionId` | integer | Não | Função de destino (em mobilidade). |
| `legalBase` | string | Não | Despacho ou base legal. |
| `justification` | string | Sim | Justificação. |
| `attachmentDocumentId` | integer | Não | Documento de suporte. |

### PUT /leaves-mobilities/{id}

### PUT /leaves-mobilities/{id}/approve

Ao aprovar uma mobilidade, a função `fn_apply_mobility` encerra a atribuição organizacional anterior e cria a nova em `employee_unit_assignments`.

### PUT /leaves-mobilities/{id}/reject

### PUT /leaves-mobilities/{id}/close

Encerra o registo (estado `CLOSED`). Em mobilidades temporárias, restaura a atribuição organizacional anterior do colaborador.

### PUT /leaves-mobilities/{id}/cancel

### GET /funcionarios/{funcionarioId}/ausencias

---

## 2.9 Recibos de Vencimento (Payroll Slips)

### GET /funcionarios/{funcionarioId}/recibos

### GET /funcionarios/{funcionarioId}/recibos/{id}

### POST /funcionarios/{funcionarioId}/recibos

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `periodMonth` | integer | Sim | Mês de referência (1-12). |
| `periodYear` | integer | Sim | Ano de referência. |
| `issueDate` | date | Sim | Data de emissão. |
| `grossSalary` | number | Sim | Salário ilíquido. |
| `netSalary` | number | Sim | Salário líquido. |
| `documentId` | integer | Sim | Identificador do PDF do recibo. |

---

## 2.10 Dossier do Funcionário

### 2.10.1 Dependentes (Employee Dependents)

Cônjuge, filhos e outros dependentes para efeitos de INPS e subsídios familiares.

#### GET /funcionarios/{funcionarioId}/dependentes

#### GET /funcionarios/{funcionarioId}/dependentes/{id}

#### POST /funcionarios/{funcionarioId}/dependentes

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `fullName` | string | Sim | Nome completo do dependente. |
| `birthDate` | date | Não | Data de nascimento. |
| `relationshipType` | string | Sim | Tipo de parentesco — ckey de `option_entity` ccode=`RELATIONSHIP_TYPE`: `CONJUGE`, `FILHO`, `PAI`, `MAE`, `IRMAO`, etc. |
| `nif` | string | Não | NIF do dependente. |

#### PUT /funcionarios/{funcionarioId}/dependentes/{id}

#### DELETE /funcionarios/{funcionarioId}/dependentes/{id}

Soft delete.

### 2.10.2 Habilitações Literárias (Qualifications)

#### GET /funcionarios/{funcionarioId}/qualificacoes

#### GET /funcionarios/{funcionarioId}/qualificacoes/{id}

#### POST /funcionarios/{funcionarioId}/qualificacoes

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `level` | string | Sim | Nível académico — ckey de `option_entity` ccode=`QUALIFICATION_LEVEL`: `BASICO`, `SECUNDARIO`, `LICENCIATURA`, `MESTRADO`, `DOUTORAMENTO`. |
| `courseName` | string | Não | Designação do curso / área de estudo. |
| `institution` | string | Não | Instituição de ensino. |
| `country` | string | Não | País da instituição — ckey de `option_entity` ccode=`NATIONALITY` (ex: `CV`, `PT`). |
| `startDate` | date | Não | Data de início do curso. |
| `endDate` | date | Não | Data de conclusão. |
| `completed` | boolean | Não | `true` = concluído com certificado; `false` = em curso. |

Documentos (diploma, certidão) são associados após criação via `POST /funcionarios/{id}/documentos` com `referenceEntity=qualifications` e `referenceId={qualificationId}`.

#### PUT /funcionarios/{funcionarioId}/qualificacoes/{id}

#### DELETE /funcionarios/{funcionarioId}/qualificacoes/{id}

### 2.10.3 Formações Profissionais (Trainings)

#### GET /funcionarios/{funcionarioId}/formacoes

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `year` | integer | Não | Filtra por ano. |

#### GET /funcionarios/{funcionarioId}/formacoes/{id}

#### POST /funcionarios/{funcionarioId}/formacoes

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `name` | string | Sim | Designação da formação. |
| `institution` | string | Não | Entidade formadora. |
| `trainingType` | string | Não | Tipo — ckey de `option_entity` ccode=`TRAINING_TYPE`: `PRESENCIAL`, `ELEARNING`, `SEMINARIO`, `CONGRESSO`. |
| `startDate` | date | Não | Data de início. |
| `endDate` | date | Não | Data de fim. |
| `durationHours` | integer | Não | Duração em horas. |

Documentos (certificado de participação) são associados após criação via `POST /funcionarios/{id}/documentos` com `referenceEntity=trainings` e `referenceId={trainingId}`.

#### PUT /funcionarios/{funcionarioId}/formacoes/{id}

#### DELETE /funcionarios/{funcionarioId}/formacoes/{id}

### 2.10.4 Processos Disciplinares (Disciplinary Processes)

> **Acesso restrito:** `ROLE_HR_ADMIN` e `ROLE_SYSTEM_ADMIN`. Operações de leitura permitidas a `ROLE_HR_OPERATOR`.

#### GET /funcionarios/{funcionarioId}/processos-disciplinares

#### GET /funcionarios/{funcionarioId}/processos-disciplinares/{id}

#### POST /funcionarios/{funcionarioId}/processos-disciplinares

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `processNumber` | string | Não | Número do processo. |
| `startDate` | date | Sim | Data de abertura do processo. |
| `endDate` | date | Não | Data de encerramento. |
| `penalty` | string | Não | Pena aplicada (repreensão, suspensão, etc.). |
| `penaltyStartDate` | date | Não | Início do cumprimento da pena. |
| `penaltyEndDate` | date | Não | Fim do cumprimento da pena. |
| `officialBulletin` | string | Não | Nº Boletim Oficial. |
| `notes` | text | Não | Observações. |

Documentos (processo digitalizado) são associados após criação via `POST /funcionarios/{id}/documentos` com `referenceEntity=disciplinary_processes` e `referenceId={processId}`.

#### PUT /funcionarios/{funcionarioId}/processos-disciplinares/{id}

---

## 2.11 Dados Bancários do Funcionário

Dados bancários para processamento de vencimento e declarações INPS. Um funcionário pode ter vários registos activos (conta principal, poupança). O campo `banco` referencia a `option_entity` com `ccode=BANCO`.

### GET /funcionarios/{funcionarioId}/dados-bancarios

Devolve todos os dados bancários activos do funcionário.

### GET /funcionarios/{funcionarioId}/dados-bancarios/{id}

### POST /funcionarios/{funcionarioId}/dados-bancarios

**Corpo da Requisição**

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `banco` | string | Não | Banco (ckey `option_entity` ccode=`BANCO`). |
| `numeroConta` | string | Não | Número de conta. |
| `iban` | string | Não | IBAN/NIB (máx. 34 chars). |
| `numeroSegurancaSocial` | string | Não | Número de beneficiário INPS. |

### PUT /funcionarios/{funcionarioId}/dados-bancarios/{id}

Actualiza os campos preenchidos (null mantém o valor existente).

### DELETE /funcionarios/{funcionarioId}/dados-bancarios/{id}

Soft delete — marca `is_active = false`.

### PUT /funcionarios/{funcionarioId}/dados-bancarios/{id}/activate

Reactiva um registo desactivado.

---

## 2.12 Documentos do Colaborador

Gestão dos documentos pessoais associados ao funcionário (CNI, contratos, certidões). Ver Capítulo 7 para detalhe da API genérica de Anexos.

### GET /funcionarios/{funcionarioId}/documentos

### POST /funcionarios/{funcionarioId}/documentos

---

## 2.12 Regras e Validações de Negócio

### Criação de Funcionário

| Regra | Descrição |
|---|---|
| `nif` | Único no sistema. Validação de formato (Cabo Verde). |
| `nib` | Único. 21 dígitos. |
| `birthDate` | Funcionário maior de 18 anos. |
| `admissionDate` | Não pode ser superior à data corrente. |
| `sexOptionKey` | Apenas `M` ou `F`. |

### Contratos

| Regra | Descrição |
|---|---|
| Um corrente | Apenas um `is_current = true` por funcionário; o anterior é encerrado ao criar novo. |
| Sequência de datas | `startDate` do novo contrato ≥ `startDate` do contrato anterior. |

### Enquadramento Profissional

| Regra | Descrição |
|---|---|
| `categoryId` | Deve pertencer à carreira indicada (validado por trigger `fn_validate_professional_assignment`). |
| `gradeId` | Deve pertencer à categoria indicada. |
| `startDate` | Não pode ser anterior à admissão. |
| Único corrente | Apenas um `is_current = true` por funcionário; o anterior é encerrado automaticamente. |

### Pedido de Ausência

| Regra | Descrição |
|---|---|
| Datas | `endDate ≥ startDate`. Cálculo de dias úteis exclui sábados, domingos e feriados configurados. |
| Sobreposição | Não permitidos pedidos sobrepostos para o mesmo funcionário, exceto CANCELLED ou REJECTED. |
| Saldo | Para tipos com `deducts_balance = true`, dias solicitados ≤ `availableDays` do saldo. |
| Aprovação | Apenas tipos com `requires_approval = true` requerem aprovação; restantes vão direto para APPROVED. |
| Anexos | Tipos como DOENCA exigem anexo. |

### Inativação de Funcionário

| Regra | Descrição |
|---|---|
| Pedidos pendentes | Não permitido se existirem pedidos PENDING. |
| Soft delete | `is_active = false`; histórico preservado. |

### Recibos

| Regra | Descrição |
|---|---|
| Unicidade | `(employee_id, period_month, period_year)` único. |
| `periodMonth` | Inteiro entre 1 e 12. |
| Salários | `grossSalary > 0`; `netSalary > 0`; `netSalary ≤ grossSalary`. |

### Licenças e Mobilidade

| Regra | Descrição |
|---|---|
| Subtipo compatível | O subtipo deve pertencer ao `recordType` selecionado. |
| Período | `startDate` obrigatória; `endDate` condicional a `isTemporary`. |
| Sobreposição | Não permitidas licenças sobrepostas do mesmo tipo para o mesmo funcionário. |
| Integração orgânica | Aprovação de mobilidade não temporária encerra a atribuição anterior em `employee_unit_assignments`. |
| Reativação | Encerramento de mobilidade temporária restaura a atribuição anterior. |

---

# 3. Módulo Estrutura Organizacional

## 3.1 Visão Geral

Este módulo gere a base estrutural sobre a qual o INGT organiza os seus recursos humanos: as Unidades Orgânicas (organigrama), os Cargos (designações oficiais como Diretor ou Coordenador) e as Funções (papéis efetivamente exercidos). Estes três catálogos são consumidos pelos módulos de Colaboradores e Carreiras nos enquadramentos e atribuições. O tipo de unidade orgânica (Direção, Departamento, Divisão, Secção) é armazenado em `option_entity` com `ccode = 'UNIT_TYPE'`.

## 3.2 Unidades Orgânicas (Organizational Units)

Suporta a modelação de Direções, Departamentos, Divisões e Secções de forma encadeada através do campo `parentUnitId`. A raiz da hierarquia tem `parentUnitId = null`.

### GET /organizational-units

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `parentUnitId` | integer | Não | Filtra unidades filhas. |
| `unitType` | string | Não | `DIRECAO`, `DEPARTAMENTO`, `DIVISAO`, `SECCAO`. |
| `isActive` | boolean | Não | Filtra por estado. |

### GET /organizational-units/{id}

### POST /organizational-units

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `code` | string | Sim | Código único e estável. |
| `name` | string | Sim | Designação oficial. |
| `acronym` | string | Não | Sigla. |
| `type` | string | Não | Tipo de unidade: ex. `DIRECAO`, `DEPARTAMENTO`, `DIVISAO`, `SECCAO`. |
| `descricao` | string | Não | Descrição da unidade orgânica. |
| `estado` | boolean | Não | Estado operacional da unidade. |
| `parentUnitId` | integer | Não | Unidade-pai (nulo para topo). |

### PUT /organizational-units/{id}

### DELETE /organizational-units/{id}

Soft delete. Bloqueado se existirem unidades filhas ativas ou colaboradores atribuídos.

## 3.3 Cargos (Jobs)

Designação oficial atribuída ao funcionário (ex.: Diretor de Serviços, Coordenador, Técnico).

### GET /jobs

### GET /jobs/{id}

### POST /jobs

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `code` | string | Sim | Código único (máx. 50). |
| `name` | string | Sim | Designação (máx. 150). |
| `description` | string | Não | Descrição. |
| `nivel` | integer | Não | Nível hierárquico do cargo. |
| `isActive` | boolean | Não | Estado inicial (default true). |

### PUT /jobs/{id}

### DELETE /jobs/{id}

Soft delete. Rejeitado se associado a atribuições profissionais ativas.

## 3.4 Funções (Functions)

Função efetivamente exercida pelo colaborador dentro de um cargo.

**Relação entre função e cargo (`job_id`):**

O campo `jobId` (FK→`t_job`, nullable) associa uma função a um cargo específico:

- `jobId` preenchido → função específica de um cargo. O funcionário com esse cargo **herda** automaticamente todas as funções a ele ligadas — ou seja, pode exercer qualquer uma delas.
- `jobId = null` → função genérica, válida para qualquer cargo.

Esta relação serve dois propósitos: organizar o catálogo de funções por cargo (para apresentação na UI) e validar o enquadramento profissional (ao registar `functionId` num enquadramento, a aplicação verifica que a função pertence ao cargo indicado).

**Distinção entre herança e enquadramento:**

"Herdar as funções do cargo" significa que o funcionário *pode* exercê-las. O campo `functionId` no enquadramento regista *qual* está efectivamente a exercer naquele período — informação necessária para despachos oficiais, historial profissional e relatórios RH. É opcional: nem todos os funcionários têm função específica registada.

### GET /functions

### GET /functions/{id}

### POST /functions

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `code` | string | Sim | Código único (máx. 50). |
| `name` | string | Sim | Designação (máx. 150). |
| `description` | string | Não | Descrição. |
| `jobId` | UUID | Não | Cargo ao qual a função pertence. `null` = função genérica compatível com qualquer cargo. |
| `isActive` | boolean | Não | Estado inicial (default true). |

### PUT /functions/{id}

### DELETE /functions/{id}

## 3.5 Regras Transversais

| Regra | Descrição |
|---|---|
| Unicidade | `code` é globalmente único em `jobs`, `functions` e `organizational_units`. |
| Imutabilidade | `code` é imutável após criação. |
| Soft delete | `DELETE = isActive = false`; sem remoção física. |
| Integridade | Desativação rejeitada (HTTP 409) quando referenciado por registos ativos. |
| Hierarquia | Ciclo proibido em `parentUnitId`; profundidade máxima recomendada de 5 níveis. |
| Permissões | Leitura: qualquer autenticado. Escrita: `ROLE_HR_ADMIN` ou `ROLE_SYSTEM_ADMIN`. |

---

# 4. Módulo Carreiras e Progressão

## 4.1 Visão Geral

Consolida os catálogos relativos à progressão funcional dos colaboradores conforme o PCFR (Plano de Carreiras, Funções e Remunerações, Decreto-Lei 4/2024): Carreiras, Categorias e Escalões. Hierarquia: `careers → categories → grades`. O índice salarial (`salaryIndex`) e o salário base (`salaryBase`) são definidos ao nível do Escalão. O regime da carreira é um campo de texto livre (`regime`) diretamente em `careers`, sem referência a `option_entity`. A ordem de progressão dentro de uma carreira é definida ao nível da Categoria.

## 4.2 Carreiras (Careers)

### GET /careers

### GET /careers/{id}

### GET /careers/{id}/categories

### POST /careers

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `code` | string | Sim | Código único (máx. 50). |
| `name` | string | Sim | Designação (máx. 150). |
| `description` | string | Não | Descrição. |
| `regime` | string | Não | Regime da carreira: ex. `GERAL`, `ESPECIAL` (valor direto, sem referência a option_entity). |
| `isActive` | boolean | Não | Estado inicial. |

### PUT /careers/{id}

### DELETE /careers/{id}

## 4.3 Categorias (Categories)

Níveis profissionais dentro de uma carreira. O par `(career_id, code)` é único.

### GET /categories

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `careerId` | integer | Não | Filtra por carreira. |
| `isActive` | boolean | Não | Filtra por estado. |

### GET /categories/{id}

### GET /categories/{id}/grades

### POST /categories

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `careerId` | integer | Sim | Carreira. |
| `code` | string | Sim | Código único na carreira. |
| `name` | string | Sim | Designação. |
| `description` | string | Não | Descrição. |
| `ordemProgressao` | integer | Não | Ordem de progressão dentro da carreira (1, 2, 3, …). |
| `isActive` | boolean | Não | Estado inicial. |

### PUT /categories/{id}

### DELETE /categories/{id}

## 4.4 Escalões (Grades)

Posição remuneratória dentro de uma categoria. O par `(category_id, grade_number)` é único.

### GET /grades

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `categoryId` | integer | Não | Filtra por categoria. |
| `isActive` | boolean | Não | Filtra por estado. |

### GET /grades/{id}

### POST /grades

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `categoryId` | integer | Sim | Categoria. |
| `gradeNumber` | integer | Sim | Número do escalão (≥ 1). |
| `codigo` | string | Não | Código alfanumérico do escalão (máx. 50). |
| `name` | string | Sim | Designação. |
| `salaryIndex` | number | Não | Índice salarial da grelha PCFR. |
| `salaryBase` | number | Não | Salário base em CVE correspondente ao índice salarial. |
| `isActive` | boolean | Não | Estado inicial. |

### PUT /grades/{id}

### DELETE /grades/{id}

## 4.5 Regras Transversais

| Regra | Descrição |
|---|---|
| Unicidade | `careers.code` globalmente único; `categories.(career_id, code)` único; `grades.(category_id, grade_number)` único. |
| Imutabilidade | `code`, `career_id` em categorias e `category_id` em escalões são imutáveis. |
| Soft delete | `DELETE = isActive = false`; HTTP 409 se referenciado por enquadramento activo. |
| Permissões | Leitura: qualquer autenticado. Escrita: `ROLE_HR_ADMIN`. |

---

# 5. Módulo Parametrizações

## 5.1 Visão Geral

O módulo Parametrizações agrupa dois tipos de catálogos:

1. **Tabelas dedicadas com comportamento** — catálogos cujos campos afetam o fluxo de processamento do sistema (flags booleanos, limites, etc.): `t_worker_state`, `t_vinculo_laboral`, `t_contract_type`, `t_tipo_documento`, `t_leave_type`, `t_leave_mobility_subtype`.

2. **OptionEntity (lookups genéricos)** — catálogos que são apenas labels configuráveis sem lógica de negócio: estado civil, sexo, nacionalidade, ilha, concelho, tipo de unidade orgânica, nível de habilitação, tipo de parentesco, tipo de formação, categoria de documento. Servidos pelo endpoint `/reference/options`.

Todos os catálogos são administráveis de forma autónoma sem necessidade de novo deploy.

## 5.2 Estados do Trabalhador (Worker States)

`t_worker_state` — tabela dedicada porque `is_core = true` protege estados núcleo do sistema (ex: ACTIVE não pode ser desativado).

### GET /worker-states

### GET /worker-states/{id}

### POST /worker-states

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `code` | string | Sim | Código único: `ACTIVE`, `INACTIVE`, `SUSPENDED`. |
| `name` | string | Sim | Designação. |
| `isCore` | boolean | Não | `true` = não pode ser desactivado (default false). |

### PUT /worker-states/{id}

### DELETE /worker-states/{id}

Bloqueado se `is_core = true`.

## 5.3 Vínculos Laborais

`t_vinculo_laboral` — tabela dedicada porque `EFETIVO` vs `CONTRATADO` têm regras distintas no PCFR (antiguidade, progressão, direitos). Os campos `counts_seniority` e `eligible_for_progression` são usados pelo sistema nos cálculos de progressão de carreira.

### GET /vinculos-laborais

### GET /vinculos-laborais/{id}

### POST /vinculos-laborais

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `code` | string | Sim | Ex: `EFETIVO`, `CONTRATADO`, `COMISSIONADO`, `ESTAGIARIO`. |
| `name` | string | Sim | Designação. |
| `countsSeniority` | boolean | Sim | Conta para antiguidade e progressão na carreira (PCFR). Default `true`. |
| `eligibleForProgression` | boolean | Sim | Elegível para progressão de categoria/escalão (PCFR). Default `true`. |

### PUT /vinculos-laborais/{id}

### DELETE /vinculos-laborais/{id}

Bloqueado se referenciado por tipos de contrato activos.

## 5.4 Tipos de Contrato (Contract Types)

`contract_types` — tabela dedicada porque alimenta `employee_contracts` e cada tipo tem implicações legais distintas (renovabilidade, prazo, direitos, lei aplicável). O campo `vinculoLaboralId` define o vínculo laboral que o tipo de contrato implica — parametrizável pelo administrador RH com base na LGTFP. O vínculo laboral é uma classificação do tipo de contrato, não um campo em `t_funcionario`.

### GET /contract-types

### GET /contract-types/{id}

### POST /contract-types

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `code` | string | Sim | Ex: `NOMEACAO_DEFINITIVA`, `CFP`, `CTFP_TERMO_CERTO`, `CTFP_TERMO_INCERTO`, `COMISSAO_SERVICO`. |
| `name` | string | Sim | Designação (máx. 150). |
| `description` | text | Não | Descrição. |
| `vinculoLaboralId` | UUID | Não | Vínculo laboral correspondente (LGTFP). Classifica o vínculo implícito no tipo de contrato. |
| `isRenewable` | boolean | Não | Indica se o contrato é renovável (LGTFP). Default `false`. |
| `maxRenewals` | integer | Não | Número máximo de renovações permitidas por lei (`null` = sem limite). |
| `maxDurationMonths` | integer | Não | Duração máxima legal em meses (`null` = indefinido). O sistema alerta quando o limite se aproxima. |

### PUT /contract-types/{id}

### DELETE /contract-types/{id}

Bloqueado se referenciado por contratos activos.

## 5.5 Tipos de Ausência (Leave Types)

`leave_types` — tabela dedicada porque `deducts_balance` e `requires_approval` alteram completamente o fluxo de processamento do pedido de ausência.

### GET /leave-types

### GET /leave-types/{id}

### POST /leave-types

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `code` | string | Sim | Ex: `FERIAS`, `DOENCA`, `MATERNIDADE`, `PATERNIDADE`, `LUTO`, `CASAMENTO`. |
| `name` | string | Sim | Designação. |
| `categoryOptionKey` | string | Não | Categoria agrupadora (`option_entity` ccode=`LEAVE_CATEGORY`). |
| `deductsBalance` | boolean | Sim | Desconta saldo anual (default true). |
| `requiresApproval` | boolean | Sim | Exige aprovação da chefia (default true). |
| `maxDaysPerYear` | integer | Não | Limite legal de dias por ano (`null` = sem limite). |

### PUT /leave-types/{id}

### DELETE /leave-types/{id}

## 5.6 Subtipos de Licença/Mobilidade (Leave Mobility Subtypes)

`leave_mobility_subtypes` — tabela dedicada porque `affects_pay`, `counts_for_seniority` e `can_self_submit` alteram o processamento salarial, cálculo de antiguidade e o perfil que pode iniciar o processo.

### GET /leave-mobility-subtypes

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `recordType` | string | Não | `LICENCA`, `MOBILIDADE`, `AMBOS`. |

### GET /leave-mobility-subtypes/{id}

### POST /leave-mobility-subtypes

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `code` | string | Sim | Código único. |
| `name` | string | Sim | Designação (máx. 150). |
| `recordType` | string | Sim | `LICENCA`, `MOBILIDADE` ou `AMBOS`. |
| `affectsPay` | boolean | Sim | Afecta remuneração (default false). |
| `countsForSeniority` | boolean | Sim | Conta para antiguidade (default true). |
| `canSelfSubmit` | boolean | Sim | Colaborador pode submeter (default false). |

### PUT /leave-mobility-subtypes/{id}

### DELETE /leave-mobility-subtypes/{id}

## 5.7 Tipos de Documento (Document Types)

`document_types` — tabela dedicada porque `allowed_extensions` determina validação no upload e `category` (string ckey, ccode=`DOC_CATEGORY`) agrupa tipos por secção do dossier.

### GET /document-types

### GET /document-types/{id}

### POST /document-types

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `code` | string | Sim | Ex: `CNI`, `PASSAPORTE`, `CONTRATO`, `CERTIDAO`, `HABILITACAO`, `FORMACAO`, `DISCIPLINAR`, `RECIBO`, `JUSTIFICATIVO`, `OUTRO`. |
| `name` | string | Sim | Designação. |
| `categoryOptionKey` | string | Não | Categoria (`option_entity` ccode=`DOC_CATEGORY`): `PESSOAL`, `CONTRATUAL`, `FORMACAO`, `DISCIPLINAR`, `AVALIACAO`. |
| `allowedExtensions` | string | Não | Ex: `pdf,jpg,png`. |

### PUT /document-types/{id}

### DELETE /document-types/{id}

## 5.8 Feriados (Public Holidays)

### GET /public-holidays

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `year` | integer | Não | Filtra por ano. |
| `isNational` | boolean | Não | `true` = nacionais; `false` = municipais. |

### POST /public-holidays

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `holidayDate` | date | Sim | Data do feriado. |
| `name` | string | Sim | Designação. |
| `isNational` | boolean | Sim | Nacional (`true`) ou municipal (`false`). |

### PUT /public-holidays/{id}

### DELETE /public-holidays/{id}

## 5.9 Endpoints de Referência — OptionEntity (/reference/*)

Os lookups sem lógica de negócio (estado civil, sexo, nacionalidade, ilha, concelho, tipo de unidade, nível de habilitação, tipo de parentesco, tipo de formação, categoria de ausência, categoria de documento) são expostos através do endpoint genérico de `option_entity`.

### GET /reference/options

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `ccode` | string | Sim | Categoria do grupo. Ver tabela abaixo. |
| `locale` | string | Não | Idioma (default `pt-CV`). |

**Grupos disponíveis:**

| `ccode` | Descrição | Exemplos de `ckey` |
|---|---|---|
| `MARITAL_STATUS` | Estado Civil | `SOLTEIRO`, `CASADO`, `UNIAO_FACTO`, `DIVORCIADO`, `VIUVO` |
| `SEX` | Sexo | `M`, `F` |
| `NATIONALITY` | Nacionalidade | `CV`, `PT`, `SN`, `BR` |
| `UNIT_TYPE` | Tipo de Unidade Orgânica | `DIRECAO`, `DEPARTAMENTO`, `DIVISAO`, `SECCAO` |
| `DOC_CATEGORY` | Categoria de Documento | `PESSOAL`, `CONTRATUAL`, `FORMACAO`, `DISCIPLINAR`, `AVALIACAO` |
| `LEAVE_CATEGORY` | Categoria de Ausência | `FERIAS`, `DOENCA`, `FAMILIA`, `OUTRO` |
| `QUALIFICATION_LEVEL` | Nível de Habilitação | `BASICO`, `SECUNDARIO`, `LICENCIATURA`, `MESTRADO`, `DOUTORAMENTO` |
| `RELATIONSHIP_TYPE` | Tipo de Parentesco | `CONJUGE`, `FILHO`, `PAI`, `MAE`, `IRMAO` |
| `ISLAND` | Ilha de Cabo Verde | `SANTIAGO`, `SAL`, `BOA_VISTA`, `SAO_VICENTE`, `FOGO` |
| `CONCELHO` | Concelho | `PRAIA`, `SANTA_CATARINA`, `SAO_DOMINGOS`, `MINDELO` |
| `TRAINING_TYPE` | Tipo de Formação | `PRESENCIAL`, `ELEARNING`, `SEMINARIO`, `CONGRESSO` |
| `CAREER_REGIME` | Regime de Carreira | `GERAL`, `ESPECIAL` |
| `BANCO` | Banco (para dados bancários) | `BCA`, `BCN`, `CECV`, `BAI` |
| `WORK_REGIME` | Regime de Trabalho (LGTFP art.123-129) | `TEMPO_INTEIRO`, `TEMPO_PARCIAL`, `EXCLUSIVIDADE` |

**Resposta (200 OK)**

```json
[
  { "ckey": "SOLTEIRO",    "cvalue": "Solteiro(a)",       "sortOrder": 1 },
  { "ckey": "CASADO",      "cvalue": "Casado(a)",          "sortOrder": 2 },
  { "ckey": "UNIAO_FACTO", "cvalue": "União de facto",     "sortOrder": 3 },
  { "ckey": "DIVORCIADO",  "cvalue": "Divorciado(a)",      "sortOrder": 4 },
  { "ckey": "VIUVO",       "cvalue": "Viúvo(a)",           "sortOrder": 5 }
]
```

### POST /reference/options

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `ccode` | string | Sim | Categoria. |
| `ckey` | string | Sim | Chave da opção (única dentro do ccode + locale). |
| `cvalue` | string | Sim | Label de exibição. |
| `locale` | string | Não | Idioma (default `pt-CV`). |
| `sortOrder` | integer | Não | Ordem de exibição. |
| `description` | string | Não | Descrição opcional. |

### PUT /reference/options/{id}

### DELETE /reference/options/{id}

## 5.10 Regras Transversais de Parametrizações

| Regra | Descrição |
|---|---|
| Unicidade | `code` único por tabela. Em `option_entity`: `(ccode, ckey, locale)` único. |
| Imutabilidade | `code` e `ckey` são imutáveis após criação. |
| Soft delete | `is_active = false`; sem remoção física. |
| Integridade | Desativação rejeitada (HTTP 409) quando referenciado por registos ativos. |
| Permissões | Leitura: qualquer autenticado. Escrita: `ROLE_HR_ADMIN`. |

---

# 6. Integração com o Sistema de Avaliação de Desempenho

## 6.1 Visão Geral e Arquitetura de Integração

A Avaliação de Desempenho é externa ao Módulo RH e é gerida pelo Sistema de Avaliação de Desempenho (SAD). A integração é assente em dois mecanismos: leituras diretas (read-through com cache) para os endpoints de consulta de avaliações, e webhooks do SAD para notificação proativa de eventos.

```
SIPPROG (/me/external/*, GET /funcionarios/{id}/evaluations)
        ↕ REST + JWT (OAuth2 client_credentials)
SAD (Sistema de Avaliação de Desempenho — externo)
        ↕ Webhook → POST /webhooks/sad/*
```

## 6.2 Endpoints de Leitura (Read-Through)

### GET /funcionarios/{funcionarioId}/external/evaluations

### GET /funcionarios/{funcionarioId}/external/evaluations/{externalId}

### GET /external/evaluation-cycles

Lista os ciclos abertos e fechados conhecidos no SAD.

## 6.3 Sincronização de Catálogos (Webhooks)

| Webhook | Endpoint no SIPPROG | Eventos |
|---|---|---|
| Avaliação publicada | `POST /webhooks/sad/evaluation-published` | `evaluation.published` |
| Ciclo encerrado | `POST /webhooks/sad/cycle-closed` | `cycle.closed` |
| Objetivo atualizado | `POST /webhooks/sad/objective-updated` | `objective.updated` |

## 6.4 Mapeamento de Identidades

Como o `employee_id` no SIPPROG e o `employeeId` no SAD podem divergir, mantém-se uma tabela auxiliar `employee_external_mapping` com `(employee_id, system_code, external_id, external_username)`.

## 6.5 Tratamento de Falhas e Cache

| Cenário | Comportamento |
|---|---|
| Cache HIT | Resposta do cache com cabeçalho `X-Cache: HIT` e `X-Cache-Age`. |
| Cache MISS | Pedido ao SAD; resposta armazenada por TTL (default 300 s). |
| SAD indisponível | HTTP 503 com `Retry-After` se sem cache; resposta stale (`X-Cache: STALE`) se houver cache expirado. |
| Circuit breaker | Após 5 falhas consecutivas, circuito abre por 60 s; responde apenas com cache stale. |
| Logs | Cada chamada ao SAD é registada em `integration_logs` com latência, status e correlação ID. |

## 6.6 Segurança da Integração

| Aspecto | Detalhe |
|---|---|
| Autenticação outbound | OAuth 2.0 `client_credentials`; tokens cacheados até expirar. |
| Autenticação inbound (webhooks) | HMAC SHA-256 sobre o body; segredo partilhado; tolerância de relógio de 5 minutos. |
| TLS | TLS 1.2+ obrigatório, validação de certificado. |
| Auditoria | Cada webhook recebido é registado em `integration_logs` com payload (PII redatada). |

---

# 7. API de Anexos / Documentos

## 7.1 Visão Geral

API genérica para gestão de ficheiros associados a entidades do Módulo RH. Os ficheiros são armazenados em storage de objetos compatível com S3 (MinIO) e os metadados na tabela `documents`.

A tabela `documents` é polimórfica: os campos `reference_entity` e `reference_id` associam o documento ao registo de origem (ex: `leave_requests`, `trainings`, `disciplinary_processes`). Se `reference_entity IS NULL`, o documento pertence directamente ao funcionário.

| Limite | Valor |
|---|---|
| Tamanho máximo por ficheiro | 10 MB (configurável). |
| Tipos aceites | PDF, JPG, PNG, DOCX, XLSX (definidos por `allowed_extensions` do `document_type`). |
| Antivírus | Verificação assíncrona após upload; ficheiros suspeitos são bloqueados. |

## 7.2 Upload

### POST /documents

Multipart/form-data com os campos: `file`, `documentTypeId`, `employeeId`, `description`.

**Resposta (201 Created)**

```json
{
  "id": 1234,
  "fileName": "cni_alex.pdf",
  "documentType": "CNI",
  "mimeType": "application/pdf",
  "sizeBytes": 245680,
  "uploadedAt": "2026-04-22T10:14:00Z",
  "employeeId": 42,
  "storageKey": "employees/42/cni_alex_1234.pdf"
}
```

## 7.3 Download

### GET /documents/{id}/download

Devolve o ficheiro com `Content-Type` e `Content-Disposition` apropriados. Permissões aplicadas conforme o dono do documento.

## 7.4 Listagem por Funcionário

### GET /funcionarios/{funcionarioId}/documentos

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `documentTypeId` | integer | Não | Filtra por tipo. |
| `referenceEntity` | string | Não | Filtra por entidade de origem (ex: `leave_requests`). |

## 7.5 Associação a Entidade

### PUT /documents/{id}/reference

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `referenceEntity` | string | Sim | Nome da tabela de origem. |
| `referenceId` | integer | Sim | ID do registo associado. |

---

# 8. Modelo de Dados

## 8.1 Visão Geral

O modelo físico do Módulo RH é implementado em PostgreSQL 17 e organiza-se em **9 blocos funcionais** e **26 tabelas**, cobrindo o ciclo completo do dossier do funcionário público.

O modelo completo com definição de todas as colunas, tipos, constraints e o diagrama Mermaid ERD encontra-se no documento `Modelo_Relacional_RH_v4.0.md`, anexo a esta especificação.

**Princípios fundamentais do modelo v4.0:**

| Princípio | Descrição |
|---|---|
| OptionEntity para lookups | Estado civil, sexo, nacionalidade, ilha, concelho, tipo de unidade, nível de habilitação, tipo de parentesco, tipo de formação → `t_option_entity`. |
| Tabelas dedicadas com comportamento | `t_worker_state`, `t_vinculo_laboral`, `t_contract_type`, `t_tipo_documento`, `t_leave_type`, `t_leave_mobility_subtype`. |
| Três históricos independentes | `t_contrato`, `t_employee_professional_assignments`, `t_employee_unit_assignments` — cada um com o seu próprio ciclo de vida. |
| Documentos polimórficos | `t_document` com `reference_entity` + `reference_id` associa ficheiros a qualquer entidade. |
| Soft delete universal | Nenhuma tabela de negócio usa `DELETE` físico. |
| Auditoria completa | `created_at`, `created_by`, `updated_at`, `updated_by` em todas as tabelas de negócio + `change_history` via trigger. |

## 8.2 Blocos Funcionais

| Bloco | Tabelas | Descrição |
|---|---|---|
| 0 — OptionEntity | `t_option_entity` | Lookups genéricos sem lógica de negócio. |
| 1 — Parametrizações | `t_worker_state`, `t_vinculo_laboral`, `t_contract_type`, `t_tipo_documento`, `t_leave_type`, `t_leave_mobility_subtype` | Catálogos com flags de comportamento. |
| 2 — Estrutura Organizacional | `t_unidade_organica`, `t_job`, `t_funcao` | Hierarquia orgânica, cargos e funções. |
| 3 — Carreiras e Progressão | `t_career`, `t_category`, `t_grade` | Grelha PCFR — hierarquia carreira → categoria → escalão. |
| 4 — Núcleo do Funcionário | `t_funcionario`, `t_dependente`, `t_dados_bancarios` | Dados pessoais, contacto e dependentes. |
| 5 — Historial Profissional | `t_contrato`, `t_employee_professional_assignments`, `t_employee_unit_assignments` | Três históricos independentes com `is_current`. |
| 6 — Dossier | `t_qualificacao`, `t_training`, `t_disciplinary_process` | Habilitações, formações e processos disciplinares. |
| 7 — Documentos | `t_document` | Tabela genérica polimórfica para todos os ficheiros. |
| 8 — Ausências e Recibos | `t_leave_balance`, `t_leave_request`, `t_leave_mobility`, `t_payroll_slip` | Gestão de ausências, licenças e recibos. |
| 9 — Sistema | `t_public_holiday`, `change_history`, `employee_external_mapping` | Feriados, auditoria e mapeamentos externos. |

## 8.3 Diagrama de Entidades e Relacionamentos (ERD)

O diagrama ERD do módulo é apresentado no documento `Modelo_Relacional_RH_v4.0.md`, secção 4, em formato Mermaid. As tabelas estão agrupadas por bloco funcional e as relações representadas em notação crow's foot, indicando cardinalidade e obrigatoriedade.

## 8.4 Descrição das Tabelas Principais

### t_option_entity (Opções / Lookups Genéricos)

| Coluna | Tipo | Descrição |
|---|---|---|
| `id` | UUID PK | Identificador único. |
| `ccode` | VARCHAR(50) NOT NULL | Categoria: `MARITAL_STATUS`, `SEX`, `NATIONALITY`, etc. |
| `ckey` | VARCHAR(50) | Chave da opção: `SOLTEIRO`, `M`, `CV`, etc. |
| `cvalue` | VARCHAR(200) | Label de exibição. |
| `locale` | VARCHAR(10) | Idioma: `pt-CV`, `en`. |
| `sort_order` | INTEGER | Ordem de exibição. |
| `active` | BOOLEAN DEFAULT TRUE | Estado lógico. |
| `description` | TEXT | Descrição opcional. |

### t_funcionario (Funcionários)

| Coluna | Tipo | Descrição |
|---|---|---|
| `id` | UUID PK | Identificador único. |
| `full_name` | VARCHAR(200) NOT NULL | Nome completo. |
| `nif` | VARCHAR(20) UNIQUE NOT NULL | NIF único. |
| `birth_date` | DATE NOT NULL | Data de nascimento. |
| `sex` | VARCHAR(10) | ckey ccode=`SEX`: `M`, `F`. |
| `marital_status` | VARCHAR(30) | ckey ccode=`MARITAL_STATUS`: `SOLTEIRO`, `CASADO`, etc. |
| `nationality` | VARCHAR(10) | ckey ccode=`NATIONALITY`: `CV`, `PT`, etc. |
| `worker_state_id` | UUID FK→t_worker_state | Estado do trabalhador. |
| `admission_date` | DATE NOT NULL | Data de admissão. |
| `email` | VARCHAR(150) | Email institucional. |
| `phone` | VARCHAR(30) | Telefone. |
| `nib` | VARCHAR(30) | IBAN para pagamentos. |
| `address_street` | VARCHAR(200) | Morada. |
| `address_island` | VARCHAR(50) | ckey ccode=`ISLAND`: `SANTIAGO`, `SAL`, etc. |
| `address_concelho` | VARCHAR(50) | ckey ccode=`CONCELHO`: `PRAIA`, `MINDELO`, etc. |
| `photo_document_id` | BIGINT FK→documents | Fotografia do funcionário. |
| `is_active` | BOOLEAN DEFAULT TRUE | Estado lógico. |
| `created_at/by, updated_at/by` | AUDITORIA | Timestamps e autores. |

### t_contrato (Contratos do Funcionário)

| Coluna | Tipo | Descrição |
|---|---|---|
| `id` | UUID PK | Identificador único. |
| `funcionario_id` | UUID NOT NULL FK→t_funcionario | Funcionário. |
| `contract_type_id` | UUID FK→t_contract_type | Tipo de contrato. |
| `contract_number` | VARCHAR(100) UNIQUE | Nº do instrumento contratual (nullable). |
| `start_date` | DATE NOT NULL | Data de início. |
| `end_date` | DATE | Data de fim (`null` = sem prazo definido). |
| `termination_reason` | VARCHAR(50) | Motivo de cessação (preenchido ao encerrar). |
| `is_current` | BOOLEAN NOT NULL | Apenas 1 TRUE por funcionário. |
| `status` | VARCHAR(20) NOT NULL | Ciclo de vida: `ATIVO`, `SUSPENSO`, `CESSADO`. |
| `renewal_count` | INTEGER NOT NULL | Nº de renovações do mesmo tipo renovável. |
| `legal_base` | VARCHAR(200) | Nº despacho / Boletim Oficial. |
| `notes` | TEXT | Observações. |

### t_employee_professional_assignments (Enquadramento Profissional)

| Coluna | Tipo | Descrição |
|---|---|---|
| `id` | UUID PK | Identificador único. |
| `funcionario_id` | UUID NOT NULL FK→t_funcionario | Funcionário. |
| `career_id` | BIGINT NOT NULL FK→t_career | Carreira. |
| `category_id` | BIGINT NOT NULL FK→t_category | Categoria (validada vs career por trigger). |
| `grade_id` | BIGINT NOT NULL FK→t_grade | Escalão (validado vs category por trigger). |
| `job_id` | BIGINT FK→t_job | Cargo exercido. |
| `function_id` | BIGINT FK→t_funcao | Função específica que o funcionário está a exercer neste período (opcional). O funcionário herda todas as funções do seu cargo — este campo regista qual está efectivamente a desempenhar. |
| `start_date` | DATE NOT NULL | Data de início. |
| `end_date` | DATE | Data de fim (`null` = enquadramento actual). |
| `is_current` | BOOLEAN NOT NULL DEFAULT FALSE | Apenas 1 TRUE por funcionário. |
| `legal_base` | VARCHAR(200) | Despacho de progressão. |
| `notes` | TEXT | Observações. |

### t_employee_unit_assignments (Colocações / Mobilidade)

| Coluna | Tipo | Descrição |
|---|---|---|
| `id` | UUID PK | Identificador único. |
| `funcionario_id` | UUID NOT NULL FK→t_funcionario | Funcionário. |
| `unit_id` | BIGINT NOT NULL FK→t_unidade_organica | Unidade orgânica. |
| `is_current` | BOOLEAN NOT NULL | Colocação activa principal. |
| `start_date` | DATE NOT NULL | Data de início. |
| `end_date` | DATE | Data de fim (`null` = colocação actual). |
| `notes` | TEXT | Observações. |

### t_document (Documentos do Dossier)

| Coluna | Tipo | Descrição |
|---|---|---|
| `id` | UUID PK | Identificador único. |
| `funcionario_id` | UUID FK→t_funcionario | Funcionário (`null` se documento do sistema). |
| `document_type_id` | BIGINT NOT NULL FK→t_tipo_documento | Tipo de documento. |
| `file_name` | VARCHAR(255) NOT NULL | Nome original do ficheiro. |
| `storage_key` | VARCHAR(500) NOT NULL | Chave no MinIO/S3. |
| `mime_type` | VARCHAR(100) NOT NULL | `application/pdf`, `image/jpeg`, etc. |
| `size_bytes` | BIGINT NOT NULL | Tamanho em bytes. |
| `description` | TEXT | Descrição. |
| `reference_entity` | VARCHAR(100) | Tabela de origem: `leave_requests`, `trainings`, etc. |
| `reference_id` | BIGINT | ID do registo associado (polimorfismo). |
| `uploaded_at` | TIMESTAMP NOT NULL | Data/hora de upload. |
| `uploaded_by` | BIGINT NOT NULL | ID do utilizador que fez o upload. |
| `is_active` | BOOLEAN DEFAULT TRUE | Estado lógico. |

## 8.5 Query do Estado Completo Actual de um Funcionário

```sql
SELECT
    f.*,
    ct.code          AS contract_type_code,
    ec.start_date    AS contract_start,
    c.name           AS career,
    cat.name         AS category,
    g.grade_number   AS grade,
    g.salary_index,
    j.name           AS job,
    func.name        AS function_name,
    ou.name          AS unit
FROM t_funcionario f
LEFT JOIN t_contrato ec
       ON ec.funcionario_id = f.id AND ec.is_current = true
LEFT JOIN t_contract_type ct
       ON ct.id = ec.contract_type_id
LEFT JOIN t_employee_professional_assignments epa
       ON epa.funcionario_id = f.id AND epa.is_current = true
LEFT JOIN t_career c     ON c.id   = epa.career_id
LEFT JOIN t_category cat ON cat.id = epa.category_id
LEFT JOIN t_grade g      ON g.id   = epa.grade_id
LEFT JOIN t_job j        ON j.id   = epa.cargo_id
LEFT JOIN t_funcao func  ON func.id = epa.function_id
LEFT JOIN t_employee_unit_assignments eua
       ON eua.funcionario_id = f.id AND eua.is_current = true AND eua.end_date IS NULL
LEFT JOIN t_unidade_organica ou
       ON ou.id = eua.unit_id
WHERE f.id = :funcionarioId;
```

## 8.6 Tabelas Externas Referenciadas (Avaliação de Desempenho)

As tabelas relativas à avaliação de desempenho não fazem parte deste esquema. São mantidas no sistema externo SAD. A tabela de mapeamento `employee_external_mapping` faz a ponte entre os identificadores do SIPPROG e os do SAD.

---

# 9. Triggers e Funções de Base de Dados

## 9.1 fn_audit_generic

Trigger `AFTER INSERT OR UPDATE OR DELETE` em todas as tabelas de negócio. Regista em `change_history`:

| Campo | Valor |
|---|---|
| `table_name` | Nome da tabela afetada. |
| `record_id` | PK do registo. |
| `operation` | `INSERT`, `UPDATE`, `DELETE`. |
| `changed_at` | Timestamp UTC. |
| `changed_by` | Utilizador (do token JWT, via `current_setting('app.current_user')`). |
| `old_values` | JSONB com valores anteriores (`null` em INSERT). |
| `new_values` | JSONB com novos valores (`null` em DELETE). |

## 9.2 fn_validate_professional_assignment

Trigger `BEFORE INSERT OR UPDATE` em `t_employee_professional_assignments`.

- Valida que `category_id` pertence à `career_id` indicada.
- Valida que `grade_id` pertence à `category_id` indicada.
- Rejeita com EXCEPTION se a hierarquia for incoerente.
- Garante que apenas um registo tem `is_current = true` por `funcionario_id`: ao activar o novo, desactiva o anterior (`is_current = false`, `end_date = new.start_date - 1`).

## 9.3 Fecho de Contrato Anterior (lógica aplicacional)

A lógica de encerramento do contrato anterior está no `CreateContratoCommandHandler`:

1. Pesquisa o contrato actual com `contratoRepository.findCurrentByFuncionarioId(funcionarioId)`.
2. Chama `contratoActual.encerrar(startDate − 1 dia, "SUBSTITUICAO")` — define `status = 'CESSADO'`, `is_current = false`.
3. Calcula `renewalCount`: se o `contract_type` for renovável e coincidir com o do contrato anterior, incrementa; caso contrário reinicia a 0.
4. Persiste ambos os contratos.

## 9.4 fn_apply_mobility

Função chamada pela aprovação de uma mobilidade (`PUT /leaves-mobilities/{id}/approve`):

1. Encerra a atribuição organizacional principal actual do funcionário (`end_date = start_date da mobilidade`).
2. Cria nova atribuição em `t_employee_unit_assignments` com a `destination_unit_id`.
3. Em mobilidade temporária, guarda a referência da atribuição anterior para restauro no encerramento.

## 9.5 fn_set_updated_at

Trigger `BEFORE UPDATE` em todas as tabelas. Define `updated_at = NOW()` automaticamente, sem necessidade de o definir na aplicação.

---

# 10. Considerações de Implementação

## 10.1 Migrations e Seed

- Migrations geridas por Flyway com nomenclatura `V{n}__{descricao}.sql`.
- Scripts de seed (`afterMigrate`) populam `t_option_entity` com os grupos base (MARITAL_STATUS, SEX, NATIONALITY, ISLAND, CONCELHO, UNIT_TYPE, DOC_CATEGORY, LEAVE_CATEGORY, QUALIFICATION_LEVEL, RELATIONSHIP_TYPE, TRAINING_TYPE).
- Seed de `t_worker_state` com ACTIVE (`is_core = true`), INACTIVE (`is_core = true`) e SUSPENDED.
- Seed de `t_vinculo_laboral` com EFETIVO, CONTRATADO, COMISSIONADO, ESTAGIARIO.
- Seed de `t_leave_type` com FERIAS, DOENCA, MATERNIDADE, PATERNIDADE, LUTO, CASAMENTO.

## 10.2 Validação em Camadas

| Camada | Responsabilidade |
|---|---|
| Controller / DTO | Validação de formato, campos obrigatórios (`@NotNull`, `@NotBlank`, `@Size`). |
| Handler (Application) | Regras de negócio: sobreposição de ausências, saldo, sequência de datas. |
| Trigger (Base de Dados) | Consistência estrutural: hierarquia carreira/categoria/escalão, unicidade `is_current`. |

## 10.3 Soft Delete e Auditoria

- Nenhuma tabela de negócio usa `DELETE` físico.
- `DELETE` nos endpoints HTTP = `is_active = false`.
- `change_history` é append-only; não deve ter triggers de UPDATE/DELETE.
- O utilizador actual é injetado via `SET LOCAL app.current_user = '...'` em cada transação.

## 10.4 Gestão de Ficheiros

- Ficheiros armazenados em MinIO (S3-compatible), configurado via `MINIO_URL` e `MINIO_BUCKET`.
- A chave `storage_key` em `documents` é o único vínculo ao storage; a URL pré-assinada é gerada no momento do download.
- Validação do `mime_type` e `allowed_extensions` é feita na camada de aplicação antes do upload para o MinIO.
- Verificação assíncrona por antivírus após upload; documento marcado `is_active = false` se suspeito.

## 10.5 Cálculo de Dias Úteis e Calendário

O cálculo de `working_days` em `leave_requests` exclui:
- Sábados e domingos.
- Feriados nacionais registados em `public_holidays` (`is_national = true`).
- Feriados municipais (`is_national = false`) configurados para o concelho da unidade orgânica do funcionário.

## 10.6 Estratégia de Testes

| Nível | Ferramenta | Cobertura |
|---|---|---|
| Unitário | JUnit 5 + Mockito | Handlers de comando/query; validações de negócio. |
| Integração | Testcontainers (PostgreSQL) | Triggers, constraints, queries complexas. |
| API | RestAssured / MockMvc | Contratos REST, códigos HTTP, formatos de resposta. |
| Carga | k6 | Endpoints críticos (`GET /funcionarios`, `POST /leave-requests`). |

## 10.7 Observabilidade

- Métricas expostas em `/actuator/prometheus` (Micrometer + Prometheus).
- Tracing distribuído com OpenTelemetry; `traceId` incluído em todas as respostas de erro.
- Logs estruturados em JSON (Logback + ECS format).
- Dashboard Grafana com SLIs: latência p95, taxa de erros, throughput por endpoint.

## 10.8 Integrações Externas

| Sistema | Protocolo | Sentido |
|---|---|---|
| SAD (Avaliação de Desempenho) | REST + JWT OAuth2 | Leitura (read-through) |
| SAD (Webhooks) | HTTP POST + HMAC SHA-256 | Inbound (SAD → SIPPROG) |
| Keycloak (Auth) | OAuth2 / OIDC | Token validation |
| MinIO (Storage) | S3 API | Leitura/escrita de ficheiros |
| Kafka (Eventos) | Kafka Producer/Consumer | Publicação de eventos de domínio (ex: `employee.created`, `contract.updated`) |
