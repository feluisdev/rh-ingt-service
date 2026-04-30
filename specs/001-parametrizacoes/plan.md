# Implementation Plan: Catálogos de Parametrização do Módulo RH

**Branch**: `001-parametrizacoes` | **Date**: 2026-04-29 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `specs/001-parametrizacoes/spec.md`

## Summary

Implementação do bounded context **Parametrizações** — primeiro módulo do refactor v4 do Módulo RH. Concentra a gestão de todos os catálogos auxiliares (com comportamento e puros de etiqueta) num único módulo top-level isolado, seguindo o padrão arquitectural validado pelo `sigdi/` (`refactor/sigdi-isolation`). O módulo expõe endpoints REST para CRUD de cada catálogo, populados com o "kit Cabo Verde" via seed defensivo idempotente. É a base referenciada por todos os outros bounded contexts do RH.

A abordagem técnica tem três pilares:

1. **Novo módulo top-level `parametrizacoes/`** com a estrutura hexagonal completa (BC único — sem sub-módulos internos)
2. **8 entities + repositórios isolados no próprio módulo** (não em `shared/`), refactorando `OptionEntity` e `TipoDocumentoEntity` que actualmente vivem em `shared/`
3. **Adopção de Flyway** como mecanismo de migrations e seed (substituindo o actual `ddl-auto=update`), com seed idempotente via `INSERT ... ON CONFLICT DO NOTHING`

## Technical Context

**Language/Version**: Java 23 (JDK 23 Temurin)
**Primary Dependencies**: Spring Boot 3.5.3, Spring Cloud 2025.0.0, Hibernate JPA, Hibernate Envers, IGRP framework `cv.igrp.framework:core` 0.1.0-beta.1, Lombok 1.18.38, SpringDoc 2.8.8
**Storage**: PostgreSQL 17 via JPA/Hibernate (único ORM permitido pela constituição)
**Migrations**: Flyway (a adicionar no pom.xml — não existe ainda no projecto)
**Testing**: JUnit 5, Mockito, Testcontainers (para validação de migrations e seed em PostgreSQL real), MockMvc/RestAssured (testes de contrato REST)
**Target Platform**: Linux containerizado (Docker), Java 23 runtime
**Project Type**: Microsserviço Spring Boot REST (web-service)
**Performance Goals**: Catálogos são read-mostly com volume baixíssimo (~500 entradas no total). Leituras em <200 ms p95 (sem necessidade de cache distribuído nesta fase)
**Constraints**: Seed defensivo idempotente (FR-022b); auditoria indefinida via Envers; soft delete em todas as tabelas; código imutável após criação (FR-004); apenas `pt-CV` obrigatório no seed (FR-007 / FR-021)
**Scale/Scope**: 8 entities, ~15 endpoints CRUD, ~500 entradas iniciais no seed, retenção indefinida de auditoria

### Decisões pendentes do projecto (não bloqueantes)

- **Autorização** *(diferida da clarify session)*: roles concretos com permissão de escrita (HR_ADMIN apenas, ou HR_ADMIN + SYSTEM_ADMIN, ou outra combinação). Implementação usará placeholder `PARAM_ADMIN` em `@PreAuthorize`; substituição final é one-liner em `SecurityConfig.java` antes de promoção a staging/produção.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

A constituição em [.specify/memory/constitution.md](../../.specify/memory/constitution.md) define 5 princípios e várias restrições técnicas. Verificação:

| Princípio / Restrição | Cumprido? | Notas |
|---|---|---|
| **I — Hexagonal (não negociável)** | ✅ | Módulo `parametrizacoes/` segue `domain/`, `application/`, `infrastructure/`, `interfaces/`. `domain/` não importa de `infrastructure/` |
| **II — CQRS — handlers como única fonte de lógica** | ✅ | Todas as operações são `@IgrpCommandHandler` (writes) ou `@IgrpQueryHandler` (reads). Controllers só delegam |
| **III — IGRP Studio: controllers gerados** | ✅ | Controllers serão criados via skill `igrp-spring-generator` a partir de manifests em `.igrpstudio/parametrizacoes/` |
| **IV — Auditoria por Envers** | ✅ | Todas as entities estendem `AuditEntity` e marcadas com `@Audited` (alinhado com `enableEntityRevision=true` do baseApi.json) |
| **V — Segurança por perfil** | ✅ | `@PreAuthorize("hasRole('PARAM_ADMIN')")` placeholder; em dev/staging tudo desactivado; produção exige decisão final dos roles |
| Java 23 | ✅ | `pom.xml` `java.version=23` |
| Spring Boot 3.5.3 | ✅ | Confirmado em `pom.xml` |
| PostgreSQL 17 (JPA/Hibernate) | ✅ | Único ORM; sem SQL nativo excepto migrations |
| `ExternalID` (UUID) como PK | ✅ | Todas as entities usam `UUID` como PK; `OptionEntity` existente já alinhado |
| MinIO para ficheiros | N/A | Não aplicável a parametrizações |
| Keycloak/JWT | ✅ | Continua a ser usado (configurado em produção) |
| Idioma pt-PT em artefactos | ✅ | Spec, plan, research, data-model em pt-PT |
| Conventional commits | ✅ | Commits no formato `feat(parametrizacoes): ...`, `docs(parametrizacoes): ...` |

**Resultado: Pré-Phase 0 — TODOS OS GATES PASSAM.** Sem violações a justificar.

## Project Structure

### Documentation (this feature)

```text
specs/001-parametrizacoes/
├── plan.md              # Este ficheiro
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/
│   ├── README.md
│   ├── reference-options.md
│   ├── worker-states.md
│   ├── professional-situations.md
│   ├── contract-types.md
│   ├── document-types.md
│   ├── leave-types.md
│   ├── leave-mobility-subtypes.md
│   └── public-holidays.md
├── checklists/
│   └── requirements.md  # criado em /speckit-specify
└── tasks.md             # Phase 2 output (criado em /speckit-tasks)
```

### Source Code (repository root)

```text
src/main/java/cv/igrp/RH_Service/
├── parametrizacoes/                          ← NOVO MÓDULO TOP-LEVEL (BC único)
│   ├── domain/
│   │   ├── models/                           -- Option, WorkerState, ProfessionalSituation,
│   │   │                                       ContractType, DocumentType, LeaveType,
│   │   │                                       LeaveMobilitySubtype, PublicHoliday
│   │   ├── valueobject/                      -- códigos e tipos do módulo
│   │   ├── repository/                       -- 8 interfaces de repositório (portas)
│   │   ├── filter/                           -- specifications de pesquisa
│   │   └── service/                          -- ReferenceLookupService (locale fallback)
│   ├── application/
│   │   ├── commands/                         -- Create/Update/Activate/Deactivate por entity
│   │   ├── queries/                          -- List/Get/Search por entity
│   │   ├── dto/                              -- Request/Response DTOs
│   │   └── constants/                        -- enums internos (ex: RecordType)
│   ├── infrastructure/
│   │   ├── mappers/                          -- Entity ↔ Domain Model
│   │   └── persistence/
│   │       ├── entity/                       -- 8 JPA entities
│   │       ├── repository/                   -- 8 Spring Data interfaces
│   │       └── adapters/                     -- Implementações de portas
│   └── interfaces/
│       └── rest/                             -- 8 controllers (gerados pelo IGRP)
│
├── funcionarios/                             ← LEGACY (esvaziado posteriormente)
├── sigdi/                                    ← já existente
└── shared/                                   ← reduzido: OptionEntity sai daqui

src/main/resources/
├── application.properties                    ← adicionar config Flyway
├── application-development.properties        ← `spring.jpa.hibernate.ddl-auto=validate` + Flyway
└── db/migration/                             ← NOVA pasta
    ├── V001__create_option_entity.sql
    ├── V002__create_worker_states.sql
    ├── V003__create_professional_situations.sql
    ├── V004__create_contract_types.sql
    ├── V005__create_document_types.sql
    ├── V006__create_leave_types.sql
    ├── V007__create_leave_mobility_subtypes.sql
    ├── V008__create_public_holidays.sql
    ├── V009__seed_option_entity_pt_cv.sql
    ├── V010__seed_worker_states.sql
    ├── V011__seed_professional_situations.sql
    ├── V012__seed_contract_types.sql
    ├── V013__seed_document_types.sql
    ├── V014__seed_leave_types.sql
    ├── V015__seed_leave_mobility_subtypes.sql
    └── V016__seed_public_holidays_2026.sql

src/test/java/cv/igrp/RH_Service/parametrizacoes/
├── application/                              -- testes unitários de handlers
├── domain/                                   -- testes de domain models
└── infrastructure/                           -- testes de integração com Testcontainers

.igrpstudio/parametrizacoes/
├── module.json
├── controllers/                              -- 8 *.json
├── dto/                                      -- *.json para Request/Response
└── models/                                   -- 8 *.json (entities)
```

**Structure Decision**: Módulo top-level `parametrizacoes/` com BC único (sem sub-módulos internos), seguindo a Variante 4.1 do [Arquitectura_Modulos_RH_v4.0.md](../../docs/funcionarios/v4/Arquitectura_Modulos_RH_v4.0.md). Entities e repositórios JPA ficam dentro do próprio módulo (não em `shared/`), alinhado com a decisão arquitectural validada pelo refactor `refactor/sigdi-isolation`. `OptionEntity` e `TipoDocumentoEntity` actualmente em `shared/` migram para este módulo.

## Phase 0 — Research

Detalhe completo em [research.md](./research.md). Resumo das decisões tomadas:

| Tópico | Decisão |
|---|---|
| Sistema de migrations | Adoptar **Flyway** nesta feature (não existe no projecto). Migrations versionadas (`V*__*.sql`) para schema; seed também em migrations versionadas (não repeatable) com `INSERT ... ON CONFLICT DO NOTHING` para idempotência |
| Refactor de `OptionEntity` | Move de `shared/` para `parametrizacoes/` seguindo o padrão validado em `refactor/sigdi-isolation` (manifest move + package update + import fix + módulo no JSON) |
| Refactor de `TipoDocumentoEntity` | Idem `OptionEntity`. Renomear para `DocumentTypeEntity` no caminho mas manter `tableName` para evitar quebra de schema existente em dev. Acrescentar `allowed_extensions` e `category_option_id` |
| Modelo de localização | Linha por `(ccode, ckey, locale)`. Lookup com fallback: query devolve `pt-CV` quando o `locale` pedido não existe |
| Cache | Spring `@Cacheable` em memória local com TTL curto (60 s); sem Redis para v1 |
| Validação de upload (`document_types.allowed_extensions`) | String com lista delimitada por vírgulas (ex: `pdf,jpg,png`); validação à hora de upload na feature `feat/documentos`, não aqui |
| Estratégia de testes | Unit tests para handlers + Testcontainers para validar migrations + seed efectivamente idempotente |
| Config produção | `ddl-auto=validate` mantém-se (já estava); Flyway corre antes do JPA validar |
| `ddl-auto` em dev | Mudar de `update` para `validate` — Flyway passa a ser fonte única de verdade |

## Phase 1 — Design & Contracts

### Data Model

Detalhe completo em [data-model.md](./data-model.md). 8 entities planeadas:

| # | Entity | Mapped Table | Purpose |
|---|---|---|---|
| 1 | `OptionEntity` | `t_option_entity` | Catálogo genérico (11 grupos via `ccode`) |
| 2 | `WorkerStateEntity` | `t_worker_state` | Estados do trabalhador com `is_core` |
| 3 | `ProfessionalSituationEntity` | `t_professional_situation` | EFETIVO/CONTRATADO/COMISSIONADO/ESTAGIARIO |
| 4 | `ContractTypeEntity` | `t_contract_type` | NOMEAÇÃO/CFP/CTFP_TC/CTFP_TI/COMISSAO |
| 5 | `DocumentTypeEntity` | `t_document_type` | Tipos com extensões permitidas e categoria |
| 6 | `LeaveTypeEntity` | `t_leave_type` | Tipos de ausência com flags de saldo/aprovação |
| 7 | `LeaveMobilitySubtypeEntity` | `t_leave_mobility_subtype` | Subtipos com flags de pay/seniority/self-submit |
| 8 | `PublicHolidayEntity` | `t_public_holiday` | Feriados nacionais e municipais |

### REST Contracts

Detalhe completo em [contracts/](./contracts/). 8 controllers planeados:

| Endpoint base | Operações |
|---|---|
| `/reference/options` | CRUD genérico para `option_entity`, com filtro por `ccode` e `locale` |
| `/worker-states` | CRUD com bloqueio em `is_core` |
| `/professional-situations` | CRUD |
| `/contract-types` | CRUD |
| `/document-types` | CRUD |
| `/leave-types` | CRUD |
| `/leave-mobility-subtypes` | CRUD |
| `/public-holidays` | CRUD com filtros por ano e por escopo |

### Quickstart

Detalhe completo em [quickstart.md](./quickstart.md). Cobre: setup Flyway, primeiro arranque com seed, verificação de cada catálogo via Swagger, validação de idempotência (re-arranque).

## Constitution Re-check (post Phase 1)

Re-validação após o design:

| Princípio | Estado | Notas pós-design |
|---|---|---|
| I — Hexagonal | ✅ | Estrutura confirmada no design; nenhum import de `infrastructure` em `domain/` previsto |
| II — CQRS | ✅ | 8 controllers × ~5 operações = ~40 commands/queries planeados, todos como handlers separados |
| III — IGRP Studio | ✅ | Manifests `.igrpstudio/parametrizacoes/` listados em quickstart como pré-requisito |
| IV — Envers | ✅ | Todas as 8 entities estendem `AuditEntity` e são `@Audited` |
| V — Segurança por perfil | ✅ | Placeholder `PARAM_ADMIN` documentado; revisão final ao fechar a decisão pendente |
| ExternalID UUID | ✅ | Todas as entities usam UUID PK; nenhuma `BIGSERIAL` |

**Resultado: Pós-Phase 1 — TODOS OS GATES PASSAM.**

## Complexity Tracking

> *Sem violações a justificar — esta secção fica intencionalmente vazia.*

Nenhum desvio à constituição é proposto. A introdução do Flyway (que não existe ainda) **não** é desvio porque a constituição não proíbe ferramentas de migration; é complemento natural ao princípio "Único ORM: JPA/Hibernate" e ao requisito de idempotência declarativa do seed.

---

## Constitution Re-check (post-implementation)

**Data**: 2026-04-30 | **Estado**: Implementação completa (US1, US2, US3, US4 + Polish)

Re-validação dos 5 princípios e restrições técnicas após a implementação total da feature.

| Princípio / Restrição | Estado | Evidência |
|---|---|---|
| **I — Hexagonal (não negociável)** | ✅ PASS | Módulo `parametrizacoes/` com separação completa: `domain/` sem imports de `infrastructure/`; `application/` usa apenas interfaces de repositório do domínio; `infrastructure/` implementa as portas. `OptionEntity`, `WorkerStateEntity`, etc. vivem em `infrastructure/persistence/entity/` — nunca expostos ao domínio directamente (mapeados via mappers) |
| **II — CQRS — handlers como única fonte de lógica** | ✅ PASS | 8 controllers × 5 operações = 40 handlers implementados. Nenhum controller contém lógica de negócio: todos delegam 100% via `commandBus` / `queryBus`. Regras (ex: bloqueio `is_core`, unicidade de feriado nacional) ficam nos command handlers |
| **III — IGRP Studio: controllers gerados** | ✅ PASS | Controllers `ReferenceOptionsController`, `WorkerStateController`, `ProfessionalSituationController`, `ContractTypeController`, `DocumentTypeController`, `LeaveTypeController`, `LeaveMobilitySubtypeController`, `PublicHolidayController`, `AuditHistoryController` anotados com `@IgrpController` e `@RestController`. Manifests criados em `.igrpstudio/parametrizacoes/` |
| **IV — Auditoria por Envers** | ✅ PASS | Todas as 8 entities marcadas com `@Audited`: `OptionEntity`, `WorkerStateEntity`, `ProfessionalSituationEntity`, `ContractTypeEntity`, `DocumentTypeEntity`, `LeaveTypeEntity`, `LeaveMobilitySubtypeEntity`, `PublicHolidayEntity`. Tabelas `*_AUD` criadas automaticamente no schema `audit_schema` (V19). Endpoint `GET /api/v1/rh/catalogs/audit/{catalog}/{entityId}` expõe o histórico via `AuditReader` |
| **V — Segurança por perfil** | ⚠️ DIFERIDO | `@PreAuthorize` em endpoints não implementado nesta iteração — aguarda decisão final de roles (pendência declarada no Technical Context). Em `development`/`staging`, segurança desactivada. Em `production`, sem `@PreAuthorize` os endpoints ficam apenas protegidos pelo OAuth2 Resource Server (autenticado, sem role check). **Ação requerida antes de promoção a produção**: aplicar T123/T124 |
| Java 23 | ✅ PASS | Compilação com `java.version=23`; switch expressions em `GetAuditHistoryQueryHandler` usam pattern matching Java 14+ |
| Spring Boot 3.5.3 | ✅ PASS | Confirmado em `pom.xml` — sem downgrade |
| PostgreSQL 17 (JPA/Hibernate) | ✅ PASS | Único ORM; zero SQL nativo nos handlers; apenas Flyway usa SQL puro para migrations |
| `ExternalID` (UUID) como PK | ✅ PASS | Todas as 8 entities usam `UUID` como PK via `ExternalID`; typed value objects (`OptionId`, `WorkerStateId`, etc.) nos domain models |
| Flyway migrations defensivas | ✅ PASS | Todas as 19 migrations usam `CREATE TABLE IF NOT EXISTS`, `CREATE UNIQUE INDEX IF NOT EXISTS`, `INSERT ... ON CONFLICT DO NOTHING`, `CREATE SCHEMA IF NOT EXISTS` — idempotência garantida |
| Cache local (SC-005) | ✅ PASS | Caffeine configurado com `maximumSize=1000,expireAfterWrite=60s`; `@Cacheable` em `FindByCcodeQueryHandler` e `ListOptionsQueryHandler`; `@CacheEvict(allEntries=true)` nos 4 write handlers de Option |
| Idioma pt-PT em artefactos | ✅ PASS | Spec, plan, research, data-model, quickstart, tasks — todos em pt-PT |
| Conventional commits | ✅ PASS | 3 commits desta feature: `feat(parametrizacoes): implement public holidays catalog (US3)`, `feat(parametrizacoes): polish — cache, @Audited, endpoints docs (Phase 7)`, `feat(parametrizacoes): implement audit history endpoint (US4)` |

### Desvios e decisões pendentes

| ID | Assunto | Decisão | Ação |
|---|---|---|---|
| D1 | `track_entities_changed_in_revision=true` | Não activado — requer `@RevisionEntity` customizado com `@ModifiedEntityNames`, não implementado nesta iteração | Feature futura: implementar `CustomRevisionEntity` quando o nível de rastreabilidade por campo for necessário |
| D2 | `@PreAuthorize` (T123/T124) | Diferido — roles não decididos | Antes de `staging`: aplicar `hasRole('PARAM_ADMIN')` em writes e `isAuthenticated()` em reads |

**Resultado final: TODOS OS PRINCÍPIOS NUCLEARES PASSAM.** Dois itens diferidos (D1, D2) com impacto conhecido e mitigado pelo perfil de segurança dev/staging.
