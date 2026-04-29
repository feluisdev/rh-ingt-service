# Arquitectura de Módulos — Módulo RH v4.0

| Metadado | Detalhe |
|---|---|
| **Documento** | Arquitectura de Módulos RH v4.0 |
| **Projeto** | SIPPROG — Sistema de Informação do Pessoal e Progressões |
| **Entidade** | INGT — Instituto Nacional de Gestão do Território |
| **Versão** | 4.0 |
| **Data** | Abril 2026 |
| **Status** | Draft |

---

## Índice

1. [Decisão Arquitectural](#1-decisão-arquitectural)
2. [Hierarquia de Módulos](#2-hierarquia-de-módulos)
3. [Mapeamento BC → Módulo → Tabelas](#3-mapeamento-bc--módulo--tabelas)
4. [Padrão de Sub-módulo (Hexagonal)](#4-padrão-de-sub-módulo-hexagonal)
5. [Relacionamentos entre Módulos](#5-relacionamentos-entre-módulos)
6. [Ordem de Implementação por Feature](#6-ordem-de-implementação-por-feature)
7. [Manifests IGRP Studio](#7-manifests-igrp-studio)

---

## 1. Decisão Arquitectural

O Módulo RH segue o padrão **"colaboradores/ umbrella"** — análogo ao `sigdi/` que já existe no projecto:

- **5 módulos top-level**: `parametrizacoes`, `estrutura`, `carreiras`, `colaboradores`, `sigdi`
- O módulo `colaboradores/` agrega 6 sub-módulos com isolamento hexagonal próprio
- Cada sub-módulo é um **Bounded Context** com a sua linguagem ubiquitária e regras
- Independência é **lógica** (cada BC respeita as suas fronteiras), não obrigatoriamente **física**
- Se um sub-módulo evoluir para linguagem totalmente distinta, pode ser promovido a top-level no futuro

**Razões da escolha:**

| Razão | Detalhe |
|---|---|
| Coesão semântica | Tudo que orbita o funcionário está debaixo do mesmo guarda-chuva |
| Padrão validado | `sigdi/` já usa exactamente esta abordagem (strategy/tatical/intelligence/compliance/budget) |
| Evita proliferação | 5 módulos top-level vs. 10 da abordagem alternativa |
| Compatível com IGRP Studio | Um `module.json` por módulo top-level com sub-pastas internas |
| Compatível com SpecKit | Cada sub-módulo = uma feature SpecKit independente |

---

## 2. Hierarquia de Módulos

**Padrão validado do `sigdi/`:** dentro de um módulo, **só `domain/` é dividido por sub-módulo**. As camadas `application/`, `infrastructure/` e `interfaces/rest/` são partilhadas (flat) ao nível do módulo top-level — apenas `infrastructure/mappers/` e `infrastructure/persistence/adapters/` têm sub-pastas internas para amarrar mapeamento/adapter à entidade específica de cada sub-domínio.

**Decisão actual sobre entities e repositórios JPA:** cada módulo é dono das entities e repositórios Spring Data específicos do seu domínio. Ficam em `{modulo}/infrastructure/persistence/entity/` e `{modulo}/infrastructure/persistence/repository/`. O `shared/` recebe apenas o que for **genuinamente cross-cutting** — tipicamente classes-base e value objects (e.g. `AuditEntity`, `ExternalID`, `Email`, `Nif`, `Nib`), e eventualmente entities consumidas por múltiplos módulos sem pertencer a nenhum.

> Esta é uma **decisão arquitectural**, não uma lei. Outras configurações são válidas (e.g. `shared/` central com todas as entities) — depende de como se desenha a arquitectura. Para o RH-Service escolhemos isolamento por módulo, validado pelo refactor `refactor/sigdi-isolation`.

```
src/main/java/cv/igrp/RH_Service/
│
├── parametrizacoes/                ← MÓDULO TOP-LEVEL (transversal)
│   ├── domain/                     -- aqui não há sub-módulos: BC único
│   │   ├── models/
│   │   ├── valueobject/
│   │   ├── repository/
│   │   ├── filter/
│   │   └── service/
│   ├── application/
│   │   ├── commands/
│   │   ├── queries/
│   │   ├── dto/
│   │   └── constants/
│   ├── infrastructure/
│   │   ├── mappers/
│   │   └── persistence/
│   │       ├── entity/             -- entities JPA do módulo (gerados pelo IGRP)
│   │       ├── repository/         -- Spring Data interfaces
│   │       └── adapters/
│   └── interfaces/
│       └── rest/                   -- gerado pelo igrp-spring-generator
│
├── estrutura/                      ← MÓDULO TOP-LEVEL (catálogo orgânico)
│   └── … (mesma estrutura — BC único)
│
├── carreiras/                      ← MÓDULO TOP-LEVEL (catálogo PCFR)
│   └── … (mesma estrutura — BC único)
│
├── colaboradores/                  ← MÓDULO TOP-LEVEL (UMBRELLA com 6 sub-domínios)
│   │
│   ├── domain/                     ← ÚNICA CAMADA DIVIDIDA POR SUB-MÓDULO
│   │   ├── core/                   -- employees, dependents
│   │   │   ├── models/
│   │   │   ├── valueobject/
│   │   │   ├── repository/
│   │   │   ├── filter/
│   │   │   └── service/
│   │   ├── vida_profissional/      -- contracts, prof_assignments, unit_assignments
│   │   │   └── … (mesma sub-estrutura)
│   │   ├── dossier/                -- qualifications, trainings, disciplinary_processes
│   │   │   └── …
│   │   ├── documentos/             -- documents polimórficos
│   │   │   └── …
│   │   ├── ausencias/              -- leave_*, leaves_mobilities
│   │   │   └── …
│   │   ├── recibos/                -- payroll_slips
│   │   │   └── …
│   │   └── shared/                 -- value objects partilhados pelos sub-domínios
│   │       └── valueobject/
│   │
│   ├── application/                ← FLAT PARTILHADO (handlers orquestram cross-sub-domínio)
│   │   ├── commands/               -- todos os commands juntos
│   │   ├── queries/                -- todas as queries juntas
│   │   ├── dto/                    -- todos os DTOs juntos
│   │   ├── constants/
│   │   └── port/                   -- portas para sistemas externos (ex: payroll)
│   │
│   ├── infrastructure/
│   │   ├── mappers/                ← DIVIDIDO POR SUB-DOMÍNIO
│   │   │   ├── core/
│   │   │   ├── vida_profissional/
│   │   │   ├── dossier/
│   │   │   ├── documentos/
│   │   │   ├── ausencias/
│   │   │   └── recibos/
│   │   ├── persistence/
│   │   │   ├── entity/             -- FLAT: entities JPA do módulo colaboradores (gerados pelo IGRP)
│   │   │   ├── repository/         -- FLAT: Spring Data interfaces do módulo
│   │   │   └── adapters/           ← DIVIDIDO POR SUB-DOMÍNIO
│   │   │       ├── core/
│   │   │       ├── vida_profissional/
│   │   │       ├── dossier/
│   │   │       ├── documentos/
│   │   │       ├── ausencias/
│   │   │       └── recibos/
│   │   ├── client/                 -- FLAT: integrações externas
│   │   └── messaging/              -- FLAT: produtores/consumidores Kafka
│   │
│   └── interfaces/
│       └── rest/                   ← FLAT: UM CONTROLLER POR SUB-DOMÍNIO
│           ├── CoreController.java          (gerado)
│           ├── VidaProfissionalController.java  (gerado)
│           ├── DossierController.java       (gerado)
│           ├── DocumentosController.java    (gerado)
│           ├── AusenciasController.java     (gerado)
│           └── RecibosController.java       (gerado)
│
├── sigdi/                          ← MÓDULO TOP-LEVEL (já existe — mesmo padrão; entities isoladas)
│   ├── domain/
│   │   ├── strategy/
│   │   ├── tatical/
│   │   ├── intelligence/
│   │   ├── compliance/
│   │   ├── budget/
│   │   ├── admin/
│   │   └── shared/
│   ├── application/                -- flat partilhado
│   ├── infrastructure/
│   │   ├── mappers/{sub}/          -- dividido por sub-domínio
│   │   ├── persistence/adapters/{sub}/  -- dividido por sub-domínio
│   │   ├── persistence/entity/     -- flat: entities JPA do sigdi
│   │   ├── persistence/repository/ -- flat: Spring Data interfaces do sigdi
│   │   ├── client/                 -- flat
│   │   └── messaging/              -- flat
│   └── interfaces/rest/            -- flat: StrategyController, TaticalController, ...
│
├── funcionarios/                   ← MÓDULO LEGACY (deprecado, esvaziado feature a feature)
│
└── shared/                         ← MÓDULO TRANSVERSAL (cross-cutting puro)
    ├── application/constants/      -- constantes verdadeiramente transversais
    ├── config/                     -- AuditEntity, ApplicationAuditorAware
    ├── domain/
    │   ├── events/
    │   ├── exceptions/
    │   ├── pagination/
    │   ├── service/                -- DocumentoService (storage abstraction)
    │   └── valueobject/            -- ExternalID, Email, Nif, Nib, ...
    ├── infrastructure/
    │   └── spring/                 -- SpringCommandBus, SpringQueryBus
    ├── interfaces/rest/            -- controllers transversais (e.g. DocumentController)
    └── security/
```

> **Nota sobre `shared/`:** o `shared/` é uma decisão arquitectural. Entities e repositórios podem ficar em `shared/` **quando são verdadeiramente transversais** (e.g. um anexo genérico usado por vários módulos, um AuditTrail consumido por todos, uma entidade de configuração global). A regra prática: se uma entity é específica do domínio de um módulo, fica nesse módulo; se é consumida por dois ou mais módulos sem pertencer a nenhum em particular, é candidata a `shared/`. A escolha pertence a quem desenha a arquitectura — este documento descreve a **decisão actual** para o RH-Service, não uma lei universal.

---

## 3. Mapeamento BC → Módulo → Tabelas

| Bounded Context | Módulo | Tabelas v4.0 | Notas |
|---|---|---|---|
| Parametrizações | `parametrizacoes/` | `option_entity`, `worker_states`, `professional_situations`, `contract_types`, `document_types`, `leave_types`, `leave_mobility_subtypes`, `public_holidays` | Transversal — todos os outros módulos consomem |
| Estrutura Organizacional | `estrutura/` | `organizational_units`, `jobs`, `functions` | Catálogo orgânico — referenciado por colaboradores/* |
| Carreiras e Progressão | `carreiras/` | `careers`, `categories`, `grades` | Grelha PCFR (Decreto-Lei 4/2024) |
| Funcionário — Núcleo | `colaboradores/core/` | `employees`, `employee_dependents` | Aggregate root: `Funcionario` |
| Vida Profissional | `colaboradores/vida_profissional/` | `employee_contracts`, `employee_professional_assignments`, `employee_unit_assignments` | Três históricos independentes com `is_current` |
| Dossier | `colaboradores/dossier/` | `qualifications`, `trainings`, `disciplinary_processes` | Cadastro académico/formativo/disciplinar |
| Documentos | `colaboradores/documentos/` | `documents`, mapeamento de `document_types` (FK) | Polimórfico via `reference_entity` + `reference_id` |
| Ausências e Licenças | `colaboradores/ausencias/` | `leave_balances`, `leave_requests`, `leaves_mobilities` | Workflow de aprovação pela chefia |
| Recibos | `colaboradores/recibos/` | `payroll_slips` | Storage de PDF gerado pelo sistema salarial externo |

Total: **9 BCs** organizados em **5 módulos top-level**, com **26 tabelas** (excluindo `change_history` e `employee_external_mapping`).

---

## 4. Padrão de Módulo IGRP (Hexagonal com Sub-domínios)

Existem **duas variantes** do padrão consoante o módulo tenha um ou múltiplos BCs internos:

### 4.1 Módulo com BC único (parametrizacoes, estrutura, carreiras)

```
{modulo}/
├── domain/                    ← MODELOS DE DOMÍNIO PUROS (sem dependências de infra)
│   ├── models/                -- Aggregate roots e entidades de domínio
│   ├── valueobject/           -- Value Objects específicos do BC
│   ├── repository/            -- Interfaces de repositório (portas)
│   ├── filter/                -- Filtros de pesquisa (specifications)
│   └── service/               -- Serviços de domínio (lógica que não cabe num aggregate)
│
├── application/               ← ORQUESTRAÇÃO E CASOS DE USO
│   ├── commands/              -- Command + CommandHandler (CQRS write)
│   ├── queries/               -- Query + QueryHandler (CQRS read)
│   ├── dto/                   -- Request/Response DTOs
│   └── constants/             -- Constantes específicas do BC
│
├── infrastructure/            ← ADAPTADORES (saídas do hexágono)
│   ├── persistence/
│   │   └── adapters/          -- Implementações dos repositórios (JPA → Domain)
│   ├── mappers/               -- Conversão Entity ↔ Domain Model
│   └── client/                -- Adapters de clientes externos
│
└── interfaces/                ← ENTRADAS DO HEXÁGONO
    └── rest/                  -- Controllers IGRP (gerados pelo igrp-spring-generator)
```

### 4.2 Módulo umbrella com múltiplos BCs (colaboradores, sigdi)

A diferença fundamental: **só `domain/` e as sub-pastas `mappers/` e `persistence/adapters/` são divididas por sub-domínio**. Tudo o resto é flat.

```
{modulo_umbrella}/
├── domain/                    ← DIVIDIDO POR SUB-DOMÍNIO (cada um tem linguagem ubíqua própria)
│   ├── {sub_dominio_A}/
│   │   ├── models/
│   │   ├── valueobject/
│   │   ├── repository/
│   │   ├── filter/
│   │   └── service/
│   ├── {sub_dominio_B}/
│   │   └── …
│   └── shared/                -- value objects e tipos partilhados pelos sub-domínios
│
├── application/               ← FLAT (handlers podem orquestrar cross-sub-domínio)
│   ├── commands/              -- todos os commands juntos, independentemente do sub-domínio
│   ├── queries/
│   ├── dto/                   -- DTOs partilhados (a API REST é única)
│   ├── constants/
│   └── port/                  -- portas para sistemas externos (ex: payroll)
│
├── infrastructure/
│   ├── mappers/               ← DIVIDIDO POR SUB-DOMÍNIO (mapper amarrado a entity↔domain específicos)
│   │   ├── {sub_dominio_A}/
│   │   └── {sub_dominio_B}/
│   ├── persistence/
│   │   ├── entity/            -- FLAT (entities JPA do módulo; entities transversais podem ficar em shared/)
│   │   ├── repository/        -- FLAT (Spring Data interfaces)
│   │   └── adapters/          ← DIVIDIDO POR SUB-DOMÍNIO (adapter implementa port do sub-domínio)
│   │       ├── {sub_dominio_A}/
│   │       └── {sub_dominio_B}/
│   ├── client/                -- FLAT
│   └── messaging/             -- FLAT
│
└── interfaces/
    └── rest/                  ← FLAT: UM CONTROLLER POR SUB-DOMÍNIO (gerado pelo igrp-spring-generator)
        ├── {SubDominioA}Controller.java
        └── {SubDominioB}Controller.java
```

### 4.3 Por que esta divisão assimétrica funciona

| Camada | Estado | Razão |
|---|---|---|
| `domain/{sub}/` | dividido | Cada BC tem a sua **linguagem ubíqua** e os seus invariantes — isolamento DDD obrigatório |
| `application/` | flat | Handlers podem **orquestrar across BCs** (ex: aprovar mobilidade altera `vida_profissional` E cria evento em `ausencias`); DTOs partilhados porque a API REST é uma só |
| `infrastructure/mappers/{sub}/` | dividido | Cada mapper está **amarrado a uma entity JPA específica** e ao seu domain model |
| `infrastructure/persistence/adapters/{sub}/` | dividido | Cada adapter implementa uma **porta de repositório** específica do sub-domínio |
| `infrastructure/persistence/entity/` e `repository/` | flat dentro do módulo | Entities JPA pertencem ao módulo dono do domínio (apenas entities transversais ficam em `shared/`, conforme decisão arquitectural). Cada entity tem um único repositório Spring Data |
| `infrastructure/client/`, `messaging/` | flat | Adapters externos são por integração, não por BC |
| `interfaces/rest/` | flat | IGRP Studio gera **um controller por área** ao nível do módulo (`.igrpstudio/{modulo}/controllers/{Sub}Controller.json`); cada controller delega para os handlers respectivos |

### 4.4 Regras críticas (não negociáveis)

| Regra | Aplica-se a |
|---|---|
| `domain/` não importa de `infrastructure/` | Princípio I |
| Lógica de negócio só em handlers, nunca em controllers | Princípio II |
| Controllers em `interfaces/rest/` são gerados — nunca editar manualmente | Princípio III |
| Entities JPA específicas do domínio de um módulo vivem nesse módulo (`{modulo}/infrastructure/persistence/entity/`); entities transversais podem ficar em `shared/` | Decisão arquitectural actual |
| Domain models seguem padrão rico: factory methods (`criar`, `reconstruir`), VOs, encapsulamento | Padrão a codificar no Princípio VI |
| Sub-domínio dentro de `domain/{sub}/` não importa de outro sub-domínio do mesmo módulo, excepto via `domain/shared/` | Coerência DDD |
| Handlers em `application/` podem usar repositórios de múltiplos sub-domínios | Necessário para orquestração |

---

## 5. Relacionamentos entre Módulos

```
                        ┌──────────────────────────────┐
                        │        parametrizacoes        │
                        │  (option_entity + dedicated)  │
                        └──────────────────────────────┘
                                    ▲       ▲
                                    │       │ consumido por TODOS
                                    │       │
            ┌───────────────────────┘       └───────────────┐
            │                                                │
            ▼                                                ▼
┌──────────────────────┐                       ┌─────────────────────────┐
│       estrutura       │◄──────────────────────│     carreiras            │
│  (units, jobs, fns)   │                       │  (careers, cats, grades) │
└──────────────────────┘                       └─────────────────────────┘
            ▲                                                ▲
            │                                                │
            │  unit_id                                      │  career/category/grade
            │                                                │
            └─────────────┬─────────────────────┬───────────┘
                          │                     │
                          │ employee_unit_      │ employee_professional_
                          │ assignments         │ assignments
                          │                     │
                          ▼                     ▼
              ┌─────────────────────────────────────────┐
              │         colaboradores/                  │
              │  ┌────────┐  ┌─────────────────────┐    │
              │  │  core  │◄─┤  vida_profissional  │    │
              │  └────────┘  └─────────────────────┘    │
              │      ▲                                  │
              │      │                                  │
              │  ┌──┴──────┐  ┌──────────┐  ┌────────┐ │
              │  │ dossier │  │ ausencias│  │recibos │ │
              │  └────┬────┘  └────┬─────┘  └────┬───┘ │
              │       │            │              │     │
              │       └────────┬───┴──────────────┘     │
              │                ▼                        │
              │          ┌────────────┐                 │
              │          │ documentos │                 │
              │          │(polimórfico)                 │
              │          └────────────┘                 │
              └─────────────────────────────────────────┘

                          ┌──────────────┐
                          │    sigdi/    │  ← já existe (planeamento estratégico)
                          └──────────────┘

                          ┌──────────────┐
                          │   shared/    │  ← cross-cutting
                          └──────────────┘
```

**Direcção das dependências:**

| De | Para | Como |
|---|---|---|
| `colaboradores/core` | `parametrizacoes` | Lookup via `OptionRef` (FK UUID) |
| `colaboradores/vida_profissional` | `parametrizacoes` (`contract_types`) | FK directo |
| `colaboradores/vida_profissional` | `estrutura`, `carreiras` | FK directo (via repository do BC consumidor) |
| `colaboradores/ausencias` | `parametrizacoes` (`leave_types`, `leave_mobility_subtypes`, `public_holidays`) | FK directo |
| `colaboradores/dossier` | `parametrizacoes` | OptionRef para níveis e tipos |
| `colaboradores/documentos` | `parametrizacoes` (`document_types`) | FK directo |
| Qualquer BC | `colaboradores/documentos` | Via `reference_entity` + `reference_id` |

**O que NÃO é permitido:**

- `colaboradores/core` importar entidades JPA de `colaboradores/vida_profissional` directamente
- Aggregate de um BC manipular aggregate de outro BC sem passar por porta (interface de repositório)
- BC de catálogo (parametrizacoes, estrutura, carreiras) depender de BCs operacionais

---

## 6. Ordem de Implementação por Feature

Cada feature SpecKit corresponde a um módulo ou sub-módulo. A ordem respeita as dependências:

| # | Feature SpecKit | Cria/Refactora | Branch | Razão da ordem |
|---|---|---|---|---|
| 1 | `feat/parametrizacoes` | Módulo top-level `parametrizacoes/` (option_entity seed + tabelas dedicadas) | `feat/parametrizacoes` | Base referenciada por todos |
| 2 | `feat/estrutura` | Módulo top-level `estrutura/` (organizational_units, jobs, functions) | `feat/estrutura` | Catálogo orgânico, sem dependências |
| 3 | `feat/carreiras` | Módulo top-level `carreiras/` (careers, categories, grades) | `feat/carreiras` | Catálogo PCFR, sem dependências |
| 4 | `feat/colaboradores-core` | Módulo top-level `colaboradores/` com sub-módulo `core/` (employees, dependents) | `feat/colaboradores-core` | Aggregate central |
| 5 | `feat/documentos` | Sub-módulo `colaboradores/documentos/` | `feat/documentos` | Pré-requisito para dossier, ausências e recibos |
| 6 | `feat/vida-profissional` | Sub-módulo `colaboradores/vida_profissional/` | `feat/vida-profissional` | Depende de carreiras + estrutura + core |
| 7 | `feat/dossier` | Sub-módulo `colaboradores/dossier/` | `feat/dossier` | Depende de core + documentos |
| 8 | `feat/ausencias` | Sub-módulo `colaboradores/ausencias/` | `feat/ausencias` | Depende de core + parametrizacoes (leave_types) + documentos |
| 9 | `feat/recibos` | Sub-módulo `colaboradores/recibos/` | `feat/recibos` | Depende de core + documentos |

**Nota sobre o módulo legacy `funcionarios/`:** vai sendo esvaziado feature a feature. Ao terminar a feature 9, a pasta `funcionarios/` é removida.

---

## 7. Manifests IGRP Studio

A organização do `.igrpstudio/` reflecte a estrutura **flat ao nível do módulo** (igual ao `sigdi/` que já existe):

```
.igrpstudio/
├── parametrizacoes/
│   ├── module.json
│   ├── controllers/                -- um por área lógica (OptionController, WorkerStateController, ...)
│   └── dto/
│
├── estrutura/
│   ├── module.json
│   ├── controllers/                -- OrganizationalUnitController, JobController, FunctionController
│   └── dto/
│
├── carreiras/
│   ├── module.json
│   ├── controllers/                -- CareerController, CategoryController, GradeController
│   └── dto/
│
├── colaboradores/                  ← UM ÚNICO MÓDULO IGRP
│   ├── module.json                 -- manifest único do módulo
│   ├── controllers/                -- FLAT: um controller por sub-domínio
│   │   ├── CoreController.json
│   │   ├── VidaProfissionalController.json
│   │   ├── DossierController.json
│   │   ├── DocumentosController.json
│   │   ├── AusenciasController.json
│   │   └── RecibosController.json
│   └── dto/                        -- FLAT: todos os DTOs do módulo
│
├── sigdi/                          ← já existe (mesmo padrão flat)
│   ├── module.json
│   ├── controllers/                -- StrategyController, TaticalController, IntelligenceController, ...
│   └── dto/
│
└── shared/                         ← apenas para manifests verdadeiramente transversais
    └── models/                     -- *.json para entities cross-cutting (se houver)
```

**Pontos chave:**

- Cada módulo top-level tem **um único `module.json`** e uma única pasta `controllers/`
- Os controllers são organizados por **área lógica** (sub-domínio), com um ficheiro `*.json` por área
- DTOs são partilhados ao nível do módulo (uma única pasta `dto/`)
- Cada entity é declarada em `.igrpstudio/{modulo}/models/{Entity}.json` com o campo `"module": "{modulo}"` — o gerador coloca o Java em `{modulo}/infrastructure/persistence/entity/` e o repositório em `{modulo}/infrastructure/persistence/repository/`
- O skill `igrp-spring-generator` é invocado uma vez por controller, gerando o Java em `interfaces/rest/`

**Geração via skill:** todos os controllers e DTOs novos serão criados via `igrp-spring-generator` (Princípio III da constituição). Como o padrão de IGRP é flat por módulo, **não há necessidade de adaptações ao skill** — basta criar manifests de controllers separados por área dentro do mesmo módulo.

---

## Apêndice A — Resumo das decisões tomadas

| Decisão | Escolha | Razão |
|---|---|---|
| PK das entidades | `ExternalID` (UUID) | Princípio da constituição; alinhado com o existente |
| Estrutura dos BCs | Opção A — `colaboradores/` umbrella | Coesão + padrão validado pelo `sigdi/` |
| Localização das entities JPA | **Cada módulo é dono das suas**; `shared/` apenas para transversais | Isolamento, validado pelo refactor `refactor/sigdi-isolation`. Decisão arquitectural — outras configurações são válidas |
| Idioma dos artefactos | pt-PT | Princípio da constituição |
| Migração de enums | Hard-cut por feature, populando dados em migration | Evita estado misto |
| Manifests IGRP | Um por módulo top-level, sub-pastas dentro | Compatível com IGRP Studio |
| Soft delete | `is_active = false` em todas as tabelas | Princípio do projecto |
| Auditoria | Hibernate Envers + `change_history` via trigger | Princípio IV |
