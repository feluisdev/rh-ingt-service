---
description: "Lista de tarefas para implementação da feature Parametrizações"
---

# Tasks: Catálogos de Parametrização do Módulo RH

**Input**: Design documents from `specs/001-parametrizacoes/`
**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/](./contracts/)

**Tests**: Esta feature inclui tarefas de testes — definidos pelo plano (R8) e pelos requisitos da spec. Não estritamente TDD-first, mas testes são parte da definição de "done" de cada user story.

**Organization**: Tasks organizadas por user story para entrega independente e MVP incremental.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode correr em paralelo (ficheiros distintos, sem dependências bloqueantes)
- **[Story]**: Mapeia a task para uma user story específica (US1, US2, US3, US4)
- Caminhos de ficheiros são absolutos no projecto (relativos à raiz do repo)

---

## Phase 1: Setup (Infrastructure)

**Purpose**: Inicializar o módulo `parametrizacoes` e adoptar Flyway.

- [x] T001 Verificar se `flyway-core` e `flyway-database-postgresql` já estão no `pom.xml` (podem ter sido adicionados upstream); se ausentes, adicionar alinhados com Spring Boot 3.5.3
- [x] T002 [P] Criar a estrutura de pastas Java do módulo em `src/main/java/cv/igrp/RH_Service/parametrizacoes/` com subpastas `domain/{models,valueobject,repository,filter,service}`, `application/{commands,queries,dto,constants}`, `infrastructure/{mappers,persistence/{entity,repository,adapters}}`, `interfaces/rest/`
- [x] T003 [P] Criar a estrutura `.igrpstudio/parametrizacoes/` com `module.json`, `controllers/`, `dto/`, `models/` (verificar se já existe; criar `.gitkeep` em pastas vazias)
- [x] T004 [P] Verificar se `src/main/resources/db/migration/` já existe; criar se ausente
- [x] T004a Determinar a base de numeração Flyway: listar `src/main/resources/db/migration/` ordenado (`ls -1 | sort`), identificar o maior número de versão presente (ex: `V1.0__Seed_Institutional_Identity.sql` → último é `1`), e anotar `LAST_V`. Todas as 16 migrations desta feature são criadas em sequência `LAST_V+1` … `LAST_V+16`, na ordem definida na secção "Sequência de Migrations" abaixo. **LAST_V = 1 → migrações começam em V2__**
- [x] T005 Verificar se `spring.flyway.enabled=true` e `spring.flyway.baseline-on-migrate=true` já estão em `application-development.properties`; adicionar em `application.properties` se ausentes nos perfis staging/production
- [x] T006 Mudar `spring.jpa.hibernate.ddl-auto` de `update` para `validate` em `src/main/resources/application-development.properties` (Flyway passa a ser fonte única de schema)
- [x] T007 [P] Verificar `pom.xml` tem `igrp.framework.core` e dependências de testes (JUnit 5, Mockito, Testcontainers já presentes); adicionar `org.testcontainers:postgresql` se ausente

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Refactorar `OptionEntity` e `TipoDocumentoEntity` para o novo módulo. **Sem isto, US1 e US2 não podem avançar.**

**⚠️ CRITICAL**: Bloqueia todas as user stories.

- [x] T008 Editar `.igrpstudio/shared/models/OptionEntity.json`: alterar `"module":"shared"` para `"module":"parametrizacoes"`; mover ficheiro com `git mv` para `.igrpstudio/parametrizacoes/models/OptionEntity.json`
- [x] T009 Mover `OptionEntity.java` com `git mv` de `src/main/java/cv/igrp/RH_Service/shared/infrastructure/persistence/entity/` para `src/main/java/cv/igrp/RH_Service/parametrizacoes/infrastructure/persistence/entity/`; actualizar declaração `package` no ficheiro
- [x] T010 Mover `OptionEntityRepository.java` com `git mv` de `src/main/java/cv/igrp/RH_Service/shared/infrastructure/persistence/repository/` para `src/main/java/cv/igrp/RH_Service/parametrizacoes/infrastructure/persistence/repository/`; actualizar `package` e import de `OptionEntity`
- [x] T011 Fix imports em todos os ficheiros do módulo `funcionarios/` legacy que referenciam `cv.igrp.RH_Service.shared.infrastructure.persistence.entity.OptionEntity` ou `...repository.OptionEntityRepository` — substituir pelo path novo `cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.{entity,repository}`
- [x] T012 Editar `.igrpstudio/shared/models/TipoDocumentoEntity.json`: alterar `"module":"shared"` para `"module":"parametrizacoes"`; renomear `"name":"TipoDocumentoEntity"` para `"name":"DocumentTypeEntity"` (manter `"tableName"` igual para não quebrar schema existente em dev); acrescentar atributos `allowedExtensions` (string nullable) e `categoryOptionId` (UUID, FK→option_entity, nullable); mover ficheiro para `.igrpstudio/parametrizacoes/models/DocumentTypeEntity.json`
- [x] T013 Mover e renomear `TipoDocumentoEntity.java` → `DocumentTypeEntity.java` em `parametrizacoes/infrastructure/persistence/entity/`; actualizar `package`, classe, imports, e adicionar campos `allowedExtensions` e `categoryOptionId` (com `@ManyToOne` para `OptionEntity`)
- [x] T014 Mover e renomear `TipoDocumentoEntityRepository.java` → `DocumentTypeEntityRepository.java`; actualizar `package` e imports
- [x] T015 Fix imports em `funcionarios/` legacy que referenciam `TipoDocumentoEntity` ou `TipoDocumentoEntityRepository` — actualizar para `DocumentTypeEntity` no novo path
- [x] T016 Validar compilação: `mvn clean compile` deve devolver `BUILD SUCCESS`
- [x] T017 Validar testes existentes: `mvn test` — 83/84 testes passam; 1 falha pre-existente em `sigdi` (`t_key_results.criteria_alcancado`) não relacionada com esta feature

**Checkpoint**: Foundation pronta — refactor de `OptionEntity` e `TipoDocumentoEntity → DocumentTypeEntity` validado, módulo `parametrizacoes/` com estrutura preparada. Pode arrancar trabalho das user stories.

---

## Phase 3: User Story 1 — Catálogos de Etiqueta para Formulários (Priority: P1) 🎯 MVP

**Goal**: Disponibilizar o catálogo genérico `option_entity` populado com o "kit Cabo Verde" para todos os 11 grupos, com endpoints REST `/reference/options` para CRUD e listagem com fallback de locale.

**Independent Test**: Após arranque, `GET /api/v1/rh/reference/options?ccode=MARITAL_STATUS` devolve 5 entradas em pt-CV; `POST /api/v1/rh/reference/options` cria nova entrada que aparece na listagem; re-arranque da aplicação não duplica nem remove dados.

### Schema & Seed (Migrations)

- [x] T018 [P] [US1] Criar `src/main/resources/db/migration/V{LAST_V+1}__create_option_entity.sql` (migration 1/16 desta feature) com `CREATE TABLE IF NOT EXISTS t_option_entity (id UUID PK, ccode, ckey, cvalue, locale, sort_order, active, description, created_at, created_by, updated_at, updated_by)` + `UNIQUE (ccode, ckey, locale)` + index `idx_option_ccode_locale_active`
- [x] T019 [P] [US1] Criar `src/main/resources/db/migration/V{LAST_V+9}__seed_option_entity_pt_cv.sql` (migration 9/16 desta feature) com `INSERT ... ON CONFLICT (ccode, ckey, locale) DO NOTHING` para os 11 grupos: `MARITAL_STATUS` (5 entradas), `SEX` (2), `NATIONALITY` (~15), `UNIT_TYPE` (4), `DOC_CATEGORY` (5), `LEAVE_CATEGORY` (4), `QUALIFICATION_LEVEL` (5), `RELATIONSHIP_TYPE` (5), `ISLAND` (10), `CONCELHO` (22), `TRAINING_TYPE` (4) — todos em `locale='pt-CV'`

### Domain Layer

- [x] T020 [P] [US1] Criar `parametrizacoes/domain/models/Option.java` com factory methods `criar()`, `reconstruir()`, `atualizar()`, `desativar()`, `reativar()`; validação de `ccode` ∈ conjunto fechado dos 11 grupos no `criar()`
- [x] T021 [P] [US1] Criar `parametrizacoes/domain/repository/OptionRepository.java` com métodos `save(Option)`, `findById(ExternalID)`, `findByCcodeAndLocale(ccode, locale, active)`, `existsByCcodeAndCkeyAndLocale(...)`, `delete(ExternalID)`
- [x] T022 [P] [US1] Criar `parametrizacoes/domain/filter/OptionFilter.java` com campos `ccode`, `locale`, `active`, `ckey` para queries paginadas
- [x] T023 [US1] Criar `parametrizacoes/domain/service/ReferenceLookupService.java` com método `findByCcode(ccode, locale)` que faz fallback automático para `pt-CV` quando o locale pedido não existe (depende de T020, T021)

### Infrastructure Layer

- [x] T024 [P] [US1] Criar `parametrizacoes/infrastructure/mappers/OptionMapper.java` para conversão `OptionEntity ↔ Option`
- [x] T025 [US1] Criar `parametrizacoes/infrastructure/persistence/adapters/OptionRepositoryImpl.java` que implementa `OptionRepository` usando `OptionEntityRepository` Spring Data + `OptionMapper` (depende de T021, T024)

### IGRP Manifests + Generation

- [x] T026 [P] [US1] Criar `.igrpstudio/parametrizacoes/dto/OptionRequestDTO.json` (campos: `ccode`, `ckey`, `cvalue`, `locale`, `sortOrder`, `description`)
- [x] T027 [P] [US1] Criar `.igrpstudio/parametrizacoes/dto/OptionResponseDTO.json` (incluindo `id`, `active`, `createdAt`, `updatedAt`)
- [x] T028 [P] [US1] Criar `.igrpstudio/parametrizacoes/dto/WrapperListaOptionDTO.json` (resposta paginada)
- [x] T029 [P] [US1] Criar `.igrpstudio/parametrizacoes/dto/OptionFilterDTO.json` (query params para `GET /reference/options`)
- [x] T030 [P] [US1] Criar `.igrpstudio/parametrizacoes/controllers/ReferenceOptionsController.json` com 6 operações: `GET /reference/options`, `GET /reference/options/{id}`, `POST /reference/options`, `PUT /reference/options/{id}`, `DELETE /reference/options/{id}`, `POST /reference/options/{id}/activate`
- [x] T031 [US1] Invocar skill `igrp-spring-generator` com `addController` para gerar `parametrizacoes/interfaces/rest/ReferenceOptionsController.java` e os DTOs em `parametrizacoes/application/dto/`

### Application Layer (Commands + Queries)

- [x] T032 [P] [US1] Criar `parametrizacoes/application/commands/CreateOptionCommand.java` + `CreateOptionCommandHandler.java` — validação de `ccode` no conjunto fechado, unicidade `(ccode, ckey, locale)`, `active=true` por default
- [x] T033 [P] [US1] Criar `parametrizacoes/application/commands/UpdateOptionCommand.java` + `UpdateOptionCommandHandler.java` — apenas `cvalue`, `sortOrder`, `description` editáveis (campos imutáveis rejeitados em 400)
- [x] T034 [P] [US1] Criar `parametrizacoes/application/commands/DesativarOptionCommand.java` + `DesativarOptionCommandHandler.java` — verificação de referências activas (preparatória; em US1 ainda não há referências externas porque outros catálogos vêm depois)
- [x] T035 [P] [US1] Criar `parametrizacoes/application/commands/AtivarOptionCommand.java` + `AtivarOptionCommandHandler.java` — reactiva entrada; falha se já está activa (409)
- [x] T036 [P] [US1] Criar `parametrizacoes/application/queries/ListOptionsQuery.java` + `ListOptionsQueryHandler.java` — paginação + filtros via `OptionFilter`
- [x] T037 [P] [US1] Criar `parametrizacoes/application/queries/GetOptionQuery.java` + `GetOptionQueryHandler.java` — devolve por ID; 404 se inexistente
- [x] T038 [P] [US1] Criar `parametrizacoes/application/queries/FindByCcodeQuery.java` + `FindByCcodeQueryHandler.java` — usa `ReferenceLookupService` com fallback de locale
- [x] T039 [US1] Wire dos handlers no `ReferenceOptionsController` (substituir TODOs gerados pelo IGRP por chamadas a `commandBus.dispatch(...)` e `queryBus.dispatch(...)`)

### Tests for User Story 1

- [x] T040 [P] [US1] Criar `src/test/java/cv/igrp/RH_Service/parametrizacoes/domain/models/OptionTest.java` — valida factory methods, validação de `ccode`, comportamento de `desativar()` / `reativar()`
- [x] T041 [P] [US1] Criar `src/test/java/cv/igrp/RH_Service/parametrizacoes/domain/service/ReferenceLookupServiceTest.java` — valida fallback `pt-CV` quando locale pedido não existe
- [x] T042 [P] [US1] Criar `src/test/java/cv/igrp/RH_Service/parametrizacoes/application/commands/CreateOptionCommandHandlerTest.java` — valida unicidade, default de `active=true`, rejeição de `ccode` inválido
- [x] T043 [P] [US1] Criar `src/test/java/cv/igrp/RH_Service/parametrizacoes/application/commands/UpdateOptionCommandHandlerTest.java` — valida que `ccode`, `ckey`, `locale` são imutáveis
- [x] T044 [P] [US1] Criar `src/test/java/cv/igrp/RH_Service/parametrizacoes/infrastructure/migrations/OptionMigrationIT.java` (Testcontainers + PostgreSQL real) — corre Flyway duas vezes e valida idempotência (contagens iguais)
- [x] T045 [US1] Criar `src/test/java/cv/igrp/RH_Service/parametrizacoes/interfaces/rest/ReferenceOptionsControllerIT.java` (MockMvc) — testes de cada endpoint com cenários happy path + 400/404/409

- [x] T045a Correr `mvn clean compile` — BUILD SUCCESS obrigatório; corrigir qualquer erro antes de avançar
- [x] T045b Correr `mvn test` — 20 novos testes passam (unit); ControllerIT e MigrationIT requerem contexto Spring/Docker (excluídos de unit run)
- [x] T045c Commit da fase: `git commit -m "feat(parametrizacoes): implement option entity catalog (US1)"`

**Checkpoint**: User Story 1 funcional. Catálogo de etiquetas operacional via REST com fallback de locale e idempotência validada. **MVP entregável.**

---

## Phase 4: User Story 2 — Catálogos com Comportamento de Negócio (Priority: P1)

**Goal**: Implementar 6 catálogos com comportamento próprio: `worker_states`, `professional_situations`, `contract_types`, `document_types` (refactor), `leave_types`, `leave_mobility_subtypes`. Cada um com flags que afectam processamento, e com seed completo segundo a legislação cabo-verdiana.

**Independent Test**: Após arranque, `GET /worker-states` devolve 3 entradas com `ACTIVE` e `INACTIVE` marcados como `isCore=true`; tentativa de `DELETE /worker-states/{id}` em estado núcleo retorna 409; `GET /leave-types` devolve 6 tipos com flags `deductsBalance` / `requiresApproval` configuradas.

### Schema & Seed Migrations (paralelas — ficheiros distintos)

- [ ] T046 [P] [US2] Criar `V{LAST_V+2}__create_worker_states.sql` (migration 2/16) em `src/main/resources/db/migration/` — `t_worker_state` com `is_core BOOLEAN`, UNIQUE `code`
- [ ] T047 [P] [US2] Criar `V{LAST_V+3}__create_professional_situations.sql` (migration 3/16) — `t_professional_situation`, UNIQUE `code`
- [ ] T048 [P] [US2] Criar `V{LAST_V+4}__create_contract_types.sql` (migration 4/16) — `t_contract_type`, UNIQUE `code`
- [ ] T049 [P] [US2] Criar `V{LAST_V+5}__create_document_types.sql` (migration 5/16) — se tabela `t_tipo_documento` existir, `ALTER TABLE` para renomear e adicionar `allowed_extensions VARCHAR(200)`, `category_option_id UUID FK→t_option_entity`; senão `CREATE TABLE t_document_type`
- [ ] T050 [P] [US2] Criar `V{LAST_V+6}__create_leave_types.sql` (migration 6/16) — `t_leave_type` com flags `deducts_balance`, `requires_approval`, `max_days_per_year`, `category_option_id` FK
- [ ] T051 [P] [US2] Criar `V{LAST_V+7}__create_leave_mobility_subtypes.sql` (migration 7/16) — `t_leave_mobility_subtype` com `record_type`, `affects_pay`, `counts_for_seniority`, `can_self_submit`
- [ ] T052 [P] [US2] Criar `V{LAST_V+10}__seed_worker_states.sql` (migration 10/16) — 3 entradas com `ON CONFLICT (code) DO NOTHING`: ACTIVE/is_core=TRUE, INACTIVE/is_core=TRUE, SUSPENDED/is_core=FALSE
- [ ] T053 [P] [US2] Criar `V{LAST_V+11}__seed_professional_situations.sql` (migration 11/16) — 4: EFETIVO, CONTRATADO, COMISSIONADO, ESTAGIARIO
- [ ] T054 [P] [US2] Criar `V{LAST_V+12}__seed_contract_types.sql` (migration 12/16) — 5 conforme Decreto-Lei 4/2024
- [ ] T055 [P] [US2] Criar `V{LAST_V+13}__seed_document_types.sql` (migration 13/16) — 10 tipos com `allowed_extensions` e `category_option_id` apontando para `t_option_entity` ccode='DOC_CATEGORY'
- [ ] T056 [P] [US2] Criar `V{LAST_V+14}__seed_leave_types.sql` (migration 14/16) — 6 tipos da Lei 20/X/2023 com flags pré-configuradas
- [ ] T057 [P] [US2] Criar `V{LAST_V+15}__seed_leave_mobility_subtypes.sql` (migration 15/16) — ~6 subtipos base

### Domain Models (paralelos — ficheiros distintos)

- [ ] T058 [P] [US2] Criar `parametrizacoes/domain/models/WorkerState.java` — factory methods + `desativar()` que lança exception se `isCore=true`
- [ ] T059 [P] [US2] Criar `parametrizacoes/domain/models/ProfessionalSituation.java` com factory methods padrão
- [ ] T060 [P] [US2] Criar `parametrizacoes/domain/models/ContractType.java`
- [ ] T061 [P] [US2] Criar `parametrizacoes/domain/models/DocumentType.java` com `allowedExtensions` e `categoryOptionId`; validação de `categoryOptionId` referenciar option `ccode='DOC_CATEGORY'`
- [ ] T062 [P] [US2] Criar `parametrizacoes/domain/models/LeaveType.java` com flags + validação de `categoryOptionId` referenciar `ccode='LEAVE_CATEGORY'`
- [ ] T063 [P] [US2] Criar `parametrizacoes/domain/models/LeaveMobilitySubtype.java` com flags e validação de `recordType` ∈ {LICENCA, MOBILIDADE, AMBOS}

### Repository Interfaces (paralelas)

- [ ] T064 [P] [US2] Criar `parametrizacoes/domain/repository/WorkerStateRepository.java`
- [ ] T065 [P] [US2] Criar `parametrizacoes/domain/repository/ProfessionalSituationRepository.java`
- [ ] T066 [P] [US2] Criar `parametrizacoes/domain/repository/ContractTypeRepository.java`
- [ ] T067 [P] [US2] Criar `parametrizacoes/domain/repository/DocumentTypeRepository.java`
- [ ] T068 [P] [US2] Criar `parametrizacoes/domain/repository/LeaveTypeRepository.java`
- [ ] T069 [P] [US2] Criar `parametrizacoes/domain/repository/LeaveMobilitySubtypeRepository.java`

### Mappers e Adapters (paralelos)

- [ ] T070 [P] [US2] Criar `WorkerStateMapper.java` + `WorkerStateRepositoryImpl.java` em `parametrizacoes/infrastructure/`
- [ ] T071 [P] [US2] Criar `ProfessionalSituationMapper.java` + `ProfessionalSituationRepositoryImpl.java`
- [ ] T072 [P] [US2] Criar `ContractTypeMapper.java` + `ContractTypeRepositoryImpl.java`
- [ ] T073 [P] [US2] Criar `DocumentTypeMapper.java` + `DocumentTypeRepositoryImpl.java`
- [ ] T074 [P] [US2] Criar `LeaveTypeMapper.java` + `LeaveTypeRepositoryImpl.java`
- [ ] T075 [P] [US2] Criar `LeaveMobilitySubtypeMapper.java` + `LeaveMobilitySubtypeRepositoryImpl.java`

### IGRP Manifests (paralelos)

- [ ] T076 [P] [US2] Criar `.igrpstudio/parametrizacoes/models/WorkerStateEntity.json`
- [ ] T077 [P] [US2] Criar `.igrpstudio/parametrizacoes/models/ProfessionalSituationEntity.json`
- [ ] T078 [P] [US2] Criar `.igrpstudio/parametrizacoes/models/ContractTypeEntity.json`
- [ ] T079 [P] [US2] Criar `.igrpstudio/parametrizacoes/models/LeaveTypeEntity.json`
- [ ] T080 [P] [US2] Criar `.igrpstudio/parametrizacoes/models/LeaveMobilitySubtypeEntity.json` *(DocumentTypeEntity já criado em T012)*
- [ ] T081 [P] [US2] Criar `.igrpstudio/parametrizacoes/dto/` para os 6 catálogos: `*RequestDTO.json`, `*ResponseDTO.json`, `WrapperLista*DTO.json`, `*FilterDTO.json` (24 ficheiros)
- [ ] T082 [P] [US2] Criar `.igrpstudio/parametrizacoes/controllers/WorkerStateController.json`
- [ ] T083 [P] [US2] Criar `.igrpstudio/parametrizacoes/controllers/ProfessionalSituationController.json`
- [ ] T084 [P] [US2] Criar `.igrpstudio/parametrizacoes/controllers/ContractTypeController.json`
- [ ] T085 [P] [US2] Criar `.igrpstudio/parametrizacoes/controllers/DocumentTypeController.json`
- [ ] T086 [P] [US2] Criar `.igrpstudio/parametrizacoes/controllers/LeaveTypeController.json`
- [ ] T087 [P] [US2] Criar `.igrpstudio/parametrizacoes/controllers/LeaveMobilitySubtypeController.json`

### Run IGRP Generator

- [ ] T088 [US2] Invocar skill `igrp-spring-generator` para gerar `*Entity.java`, `*EntityRepository.java`, controllers e DTOs em massa para os 6 catálogos (depende de T046–T087)

### Application Layer — Commands + Queries (4 commands + 2 queries por entity)

- [ ] T089 [P] [US2] WorkerState: `Create/Update/Desativar/Ativar` commands + `List/Get` queries em `parametrizacoes/application/{commands,queries}/`
- [ ] T090 [P] [US2] ProfessionalSituation: idem
- [ ] T091 [P] [US2] ContractType: idem
- [ ] T092 [P] [US2] DocumentType: idem com validação extra de `categoryOptionId` (deve ser ccode='DOC_CATEGORY') e formato `allowedExtensions`
- [ ] T093 [P] [US2] LeaveType: idem com validação extra de `categoryOptionId` (deve ser ccode='LEAVE_CATEGORY') e `maxDaysPerYear ≥ 0`
- [ ] T094 [P] [US2] LeaveMobilitySubtype: idem com validação de `recordType`

### Wire Controllers

- [ ] T095 [US2] Wire dos 6 controllers gerados — substituir TODOs por chamadas a `commandBus`/`queryBus`. Confirmação: cada controller delega para o handler correcto, sem lógica de negócio inline

### Tests for User Story 2

- [ ] T096 [P] [US2] Testes unitários dos 6 domain models — em particular `WorkerStateTest.deactivateCoreThrowsException()`, `DocumentTypeTest.invalidCategoryRejected()`, `LeaveTypeTest.invalidMaxDaysRejected()`
- [ ] T097 [P] [US2] Testes unitários dos handlers críticos — Create/Update/Desativar de cada catálogo (~12 classes de teste)
- [ ] T098 [P] [US2] Integration test: migrations de schema e seed do US2 (T046–T057) idempotentes (Testcontainers) — `MigrationsIdempotencyIT`; corre Flyway duas vezes e valida que contagens das 6 tabelas são iguais
- [ ] T099 [US2] Integration tests dos 6 controllers via MockMvc — cobre 200/201/204/400/404/409 para cada operação

- [ ] T099a Correr `mvn clean compile` — BUILD SUCCESS obrigatório; corrigir qualquer erro antes de avançar
- [ ] T099b Correr `mvn test` — todos os testes devem passar (novos + anteriores); falha bloqueia avanço
- [ ] T099c Commit da fase: `git commit -m "feat(parametrizacoes): implement behavioral catalogs (US2)"`

**Checkpoint**: 6 catálogos com comportamento operacionais. Bloqueio de `is_core` validado. Validação cruzada de `category_option_id` validada. Sistema arranca com seed completo.

---

## Phase 5: User Story 3 — Feriados Nacionais e Municipais (Priority: P2)

**Goal**: Disponibilizar o catálogo de feriados com flag `is_national` e suporte de filtros por ano e por escopo, alimentando o cálculo de dias úteis em features posteriores.

**Independent Test**: Após arranque, `GET /public-holidays?year=2026` devolve ~10 feriados nacionais oficiais; `POST` cria um feriado municipal; tentativa de criar dois nacionais com a mesma data devolve 409.

- [ ] T100 [P] [US3] Criar `V{LAST_V+8}__create_public_holidays.sql` (migration 8/16) em `src/main/resources/db/migration/` — `t_public_holiday` com `holiday_date DATE`, `is_national BOOLEAN`; partial unique index `idx_public_holiday_national_date ON t_public_holiday (holiday_date) WHERE is_national = TRUE AND is_active = TRUE`
- [ ] T101 [P] [US3] Criar `V{LAST_V+16}__seed_public_holidays_2026.sql` (migration 16/16) — 10 feriados nacionais oficiais com `ON CONFLICT DO NOTHING`
- [ ] T102 [P] [US3] Domain model `PublicHoliday.java` em `parametrizacoes/domain/models/`
- [ ] T103 [P] [US3] Repository `PublicHolidayRepository.java` em `parametrizacoes/domain/repository/`
- [ ] T104 [P] [US3] Mapper + Adapter em `parametrizacoes/infrastructure/`
- [ ] T105 [P] [US3] IGRP manifests: `PublicHolidayEntity.json`, controller `PublicHolidayController.json`, DTOs (Request, Response, WrapperLista, Filter)
- [ ] T106 [US3] Run `igrp-spring-generator` para `PublicHoliday`
- [ ] T107 [P] [US3] Commands + Handlers (Create/Update/Desativar/Ativar) em `application/commands/`
- [ ] T108 [P] [US3] Queries + Handlers (List com filtros `year`, `isNational`, `dateFrom`, `dateTo`; Get) em `application/queries/`
- [ ] T109 [US3] Wire `PublicHolidayController` aos handlers
- [ ] T110 [P] [US3] Test: `PublicHolidayTest` (factory methods, soft delete, partial unique index)
- [ ] T111 [P] [US3] Integration test: tentativa de duplicar feriado nacional na mesma data falha com 409 — `PublicHolidayControllerIT`

- [ ] T111a Correr `mvn clean compile` — BUILD SUCCESS obrigatório; corrigir qualquer erro antes de avançar
- [ ] T111b Correr `mvn test` — todos os testes devem passar; falha bloqueia avanço
- [ ] T111c Commit da fase: `git commit -m "feat(parametrizacoes): implement public holidays catalog (US3)"`

**Checkpoint**: Catálogo de feriados operacional. Pré-requisito para `feat/ausencias` validado.

---

## Phase 6: User Story 4 — Auditoria Completa de Alterações (Priority: P2)

**Goal**: Garantir que cada alteração em qualquer catálogo é auditada (autor, momento, valor anterior, valor novo) com retenção indefinida, e disponibilizar consulta do histórico por entrada.

**Independent Test**: Após uma alteração via PUT em qualquer catálogo, a tabela `*_AUD` correspondente do Envers contém uma nova linha com o utilizador e timestamp; `GET /audit/{table}/{id}` devolve o histórico de alterações daquela entrada.

- [ ] T112 [US4] Verificar configuração Envers em `application*.properties`: descomentar `spring.jpa.properties.org.hibernate.envers.track_entities_changed_in_revision=true` e `global_with_modified_flag=true` em todos os perfis
- [ ] T113 [US4] Validar que `ApplicationAuditorAware` (em `shared/config/`) injecta o utilizador autenticado em `created_by`/`updated_by` via `SecurityContextHelper`
- [ ] T114 [US4] Confirmar que cada uma das 8 entities está marcada com `@Audited` (Envers gera `*_AUD` automaticamente)
- [ ] T115 [P] [US4] Criar `parametrizacoes/application/queries/GetAuditHistoryQuery.java` + `GetAuditHistoryQueryHandler.java` — devolve histórico de alterações de uma entrada por `(tableName, entityId)` usando `AuditReader` do Envers
- [ ] T116 [P] [US4] Criar manifest `.igrpstudio/parametrizacoes/controllers/AuditHistoryController.json` com `GET /audit/{table}/{id}` (limitado a entries dos 8 catálogos)
- [ ] T117 [US4] Run `igrp-spring-generator` para `AuditHistoryController` + DTOs (`AuditHistoryEntryDTO`, `WrapperListaAuditHistoryDTO`)
- [ ] T118 [US4] Wire `AuditHistoryController` ao handler
- [ ] T119 [P] [US4] Integration test: `AuditTrailIT` — cria entrada, edita, desactiva, reactiva; valida que histórico contém 4 revisions com utilizadores e tipos de operação correctos
- [ ] T120 [US4] Documentar política de retenção indefinida em `quickstart.md` (secção troubleshooting/auditoria)

- [ ] T120a Correr `mvn clean compile` — BUILD SUCCESS obrigatório; corrigir qualquer erro antes de avançar
- [ ] T120b Correr `mvn test` — todos os testes devem passar; falha bloqueia avanço
- [ ] T120c Commit da fase: `git commit -m "feat(parametrizacoes): implement audit history (US4)"`

**Checkpoint**: Auditoria operacional para os 8 catálogos. Histórico consultável via API.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Caching, segurança placeholder, validação final, documentação.

- [ ] T121 [P] Adicionar `@Cacheable("reference-options")` em `FindByCcodeQueryHandler` e `ListOptionsQueryHandler` (TTL local 60 s via Spring Cache `ConcurrentMapCacheManager` ou Caffeine)
- [ ] T122 [P] Adicionar `@CacheEvict` nos handlers de `Create/Update/Desativar/Ativar` para invalidar cache quando há escrita
- [ ] T122a [P] Criar `src/test/java/cv/igrp/RH_Service/parametrizacoes/infrastructure/cache/ReferenceLookupCacheIT.java` — valida SC-005: após `Create/Update/Deactivate` em catálogo, leitura subsequente reflecte a alteração em ≤ 60 s. Cenários: (1) cache hit antes de escrita; (2) escrita invalida cache imediatamente via `@CacheEvict`; (3) sem `@CacheEvict` o cache reflecte a alteração após expiração natural do TTL
- [ ] T123 [P] Adicionar `@PreAuthorize("hasRole('PARAM_ADMIN')")` em todos os endpoints de escrita dos 8 controllers (placeholder até decisão final dos roles)
- [ ] T124 [P] Adicionar `@PreAuthorize("isAuthenticated()")` em endpoints de leitura
- [ ] T125 Configurar Spring Cache em `application.properties` (`spring.cache.type=caffeine`) e adicionar dependência `caffeine` em `pom.xml` se ausente
- [ ] T126 Correr `mvn clean compile` final — confirmar `BUILD SUCCESS`
- [ ] T127 Correr `mvn test` final — confirmar 100% pass com testes novos somados aos 84 existentes
- [ ] T128 Executar todo o quickstart.md em ambiente local — start aplicação, validar via Swagger UI todos os endpoints dos 8 catálogos, validar idempotência (re-arranque sem novas linhas), validar preservação de alterações administrativas
- [ ] T129 [P] Actualizar `endpoints.md` na raiz do projecto com os ~30 endpoints novos do módulo `parametrizacoes`
- [ ] T130 [P] Actualizar `CLAUDE.md` em "Key Domain Models" com as 8 entidades novas
- [ ] T131 Final Constitution Check — re-validar os 5 princípios + restrições técnicas após implementação completa; documentar em `plan.md` na secção "Constitution Re-check (post-implementation)"

---

## Sequência de Migrations

> Referência para resolver `V{LAST_V+N}__` — executar T004a primeiro para descobrir `LAST_V`.
> A ordem numérica deve ser respeitada: schemas (offsets 1–8) antes de seeds (offsets 9–16).

| Offset | Task | Nome do ficheiro (sem prefixo Vnnn) | Tipo |
|--------|------|--------------------------------------|------|
| +1  | T018 | `create_option_entity.sql`            | Schema |
| +2  | T046 | `create_worker_states.sql`            | Schema |
| +3  | T047 | `create_professional_situations.sql`  | Schema |
| +4  | T048 | `create_contract_types.sql`           | Schema |
| +5  | T049 | `create_document_types.sql`           | Schema |
| +6  | T050 | `create_leave_types.sql`              | Schema |
| +7  | T051 | `create_leave_mobility_subtypes.sql`  | Schema |
| +8  | T100 | `create_public_holidays.sql`          | Schema |
| +9  | T019 | `seed_option_entity_pt_cv.sql`        | Seed |
| +10 | T052 | `seed_worker_states.sql`              | Seed |
| +11 | T053 | `seed_professional_situations.sql`    | Seed |
| +12 | T054 | `seed_contract_types.sql`             | Seed |
| +13 | T055 | `seed_document_types.sql`             | Seed |
| +14 | T056 | `seed_leave_types.sql`                | Seed |
| +15 | T057 | `seed_leave_mobility_subtypes.sql`    | Seed |
| +16 | T101 | `seed_public_holidays_2026.sql`       | Seed |

**Exemplo**: se `LAST_V = 1` (apenas `V1.0__Seed_Institutional_Identity.sql` existe), então migration 1/16 = `V2__create_option_entity.sql`, ..., migration 16/16 = `V17__seed_public_holidays_2026.sql`.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sem dependências — pode arrancar imediatamente
- **Foundational (Phase 2)**: Depende de Setup — **bloqueia** todas as user stories
- **User Stories (Phase 3-6)**:
  - US1 e US2 são P1 — podem arrancar em paralelo após Phase 2
  - US3 e US4 são P2 — podem arrancar após Phase 2; US4 (auditoria) beneficia de ter US1+US2 implementados primeiro para gerar revisions de teste
- **Polish (Phase 7)**: Depende de US1, US2 (US3 e US4 desejáveis mas não bloqueantes para parte do polish)

### User Story Dependencies

- **US1 (P1)**: Pode arrancar imediatamente após Phase 2 — sem dependências de outras stories
- **US2 (P1)**: Pode arrancar após Phase 2 — algumas validações cruzadas dependem de `option_entity` populado, mas como US1 também executa o seed em paralelo, fica resolvido na sequência natural
- **US3 (P2)**: Independente — pode arrancar após Phase 2
- **US4 (P2)**: Beneficia de ter US1+US2+US3 implementados (mais entities para auditar), mas a configuração base do Envers e o controller genérico são independentes

### Within Each User Story

- Migrations antes de domain models (schema deve estar definido)
- Domain models antes de repositórios (interfaces dependem do tipo do aggregate)
- Repository interfaces antes de adapters
- Mappers antes de adapters (adapters usam mappers)
- IGRP manifests antes de gerar Java
- Generator run antes de wire dos controllers
- Handlers antes de wire dos controllers
- Implementation antes de tests
- Story complete antes de avançar para a próxima

### Parallel Opportunities

| Grupo | Tasks | Justificação |
|---|---|---|
| Setup [P] | T002, T003, T004, T007 | Ficheiros e pastas distintas |
| Phase 2 fix imports | T011 e T015 | Diferentes ficheiros legacy |
| US1 migrations | T018, T019 | Ficheiros SQL distintos |
| US1 domain | T020, T021, T022 | Classes Java distintas |
| US1 IGRP manifests | T026–T030 | JSON distintos |
| US1 commands/queries | T032–T038 | Classes distintas; desativar T034 não bloqueia ativar T035 (entries diferentes) |
| US1 tests | T040–T044 | Classes de teste distintas |
| US2 migrations | T046–T057 | 12 ficheiros SQL distintos |
| US2 domain models | T058–T063 | 6 classes distintas |
| US2 repos | T064–T069 | 6 interfaces distintas |
| US2 mappers/adapters | T070–T075 | 12 ficheiros, 6 entities |
| US2 IGRP manifests | T076–T087 | ~30 JSON distintos |
| US2 handlers | T089–T094 | 6 entities × ~6 handlers |
| US2 tests | T096–T098 | Classes distintas |
| US3 | T100–T108 sequência por dependência mas várias [P] dentro |
| US4 | T115, T116 [P] |
| Polish | T121, T122, T123, T124, T129, T130 [P] |

---

## Parallel Example: Setup + Phase 2

```bash
# Phase 1 (paralelizáveis):
Task: T002 - Criar estrutura de pastas Java do parametrizacoes/
Task: T003 - Criar estrutura .igrpstudio/parametrizacoes/
Task: T004 - Criar src/main/resources/db/migration/

# Phase 2 (sequencial: T008→T009→T010→T011, e T012→T013→T014→T015 também sequenciais por entity):
Task: T008–T011 (refactor OptionEntity, sequencial)
Task: T012–T015 (refactor TipoDocumentoEntity → DocumentTypeEntity, sequencial)
# T011 e T015 podem ser paralelos entre si pois tocam ficheiros legacy distintos
```

## Parallel Example: User Story 2 — Implementation in Bulk

```bash
# Após T088 (run generator), os 6 conjuntos de handlers podem ser implementados em paralelo:
Task: T089 - WorkerState handlers
Task: T090 - ProfessionalSituation handlers
Task: T091 - ContractType handlers
Task: T092 - DocumentType handlers
Task: T093 - LeaveType handlers
Task: T094 - LeaveMobilitySubtype handlers
```

---

## Implementation Strategy

### MVP First — User Story 1 Only

1. Phase 1 (Setup): T001–T007
2. Phase 2 (Foundational): T008–T017 (refactor `OptionEntity` e `DocumentTypeEntity`)
3. Phase 3 (US1): T018–T045
4. **STOP & VALIDATE**: Correr quickstart.md secções "Verificar o seed" e "Validar idempotência"; demonstrar `GET /reference/options?ccode=MARITAL_STATUS` no Swagger
5. Eventualmente deploy/demo

Se US1 funcional → MVP entregue. Catálogo de etiquetas operacional para qualquer outra feature do refactor v4.

### Incremental Delivery

- **Iteração 1**: MVP US1 → demo + validação
- **Iteração 2**: US2 → catálogos com comportamento (~60 tasks); demo
- **Iteração 3**: US3 → feriados; demo
- **Iteração 4**: US4 → auditoria + Polish

### Parallel Team Strategy (se houver 2+ devs)

- **Dev A**: Phase 1 + Phase 2 + US1 (por dependência sequencial)
- **Dev B**: Após Phase 2 — US2 (catálogos com comportamento)
- **Dev C**: Após Phase 2 — US3 + US4 (feriados + auditoria)

US1, US2, US3 e US4 são independentes após Phase 2 — podem ser desenvolvidas em paralelo sem conflitos de ficheiros.

---

## Notas

- `[P]` = ficheiro distinto, sem dependências bloqueantes — pode correr em paralelo
- `[Story]` = US1/US2/US3/US4 mapeia para a user story da spec
- Cada user story tem pelo menos 1 teste de integração para validação independente
- Commits frequentes (idealmente após cada `T###` ou grupo lógico de [P])
- Quickstart.md é a referência de "como validar manualmente" cada checkpoint
- Não acumular dependências cruzadas que quebrem a independência dos stories
- A decisão de autorização (perfis concretos) está diferida — `T123` usa placeholder `PARAM_ADMIN` que será substituído num commit futuro

## Resumo Estatístico

| Phase | Tasks | Paralelizáveis | Notas |
|---|---|---|---|
| 1 — Setup | T001–T007 (7) | 4 | Dependências mínimas |
| 2 — Foundational | T008–T017 (10) | 1 | Maioria sequencial (refactor com fix imports) |
| 3 — US1 (P1, MVP) | T018–T045 (28) | ~22 | Catálogo de etiquetas + seed kit CV |
| 4 — US2 (P1) | T046–T099 (54) | ~46 | 6 catálogos com comportamento |
| 5 — US3 (P2) | T100–T111 (12) | ~9 | Feriados |
| 6 — US4 (P2) | T112–T120 (9) | ~3 | Auditoria via Envers |
| 7 — Polish | T121–T131 + T122a (12) | ~7 | Cache, teste de invalidação, security placeholder, docs |
| **TOTAL** | **132 tasks** | **~92 [P]** | |

**MVP Scope (US1)**: 45 tasks (Phase 1 + Phase 2 + US1).
