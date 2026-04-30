---
description: "Lista de tarefas para implementaÃ§Ã£o da feature ParametrizaÃ§Ãµes"
---

# Tasks: CatÃ¡logos de ParametrizaÃ§Ã£o do MÃ³dulo RH

**Input**: Design documents from `specs/001-parametrizacoes/`
**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/](./contracts/)

**Tests**: Esta feature inclui tarefas de testes â€” definidos pelo plano (R8) e pelos requisitos da spec. NÃ£o estritamente TDD-first, mas testes sÃ£o parte da definiÃ§Ã£o de "done" de cada user story.

**Organization**: Tasks organizadas por user story para entrega independente e MVP incremental.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode correr em paralelo (ficheiros distintos, sem dependÃªncias bloqueantes)
- **[Story]**: Mapeia a task para uma user story especÃ­fica (US1, US2, US3, US4)
- Caminhos de ficheiros sÃ£o absolutos no projecto (relativos Ã  raiz do repo)

---

## Phase 1: Setup (Infrastructure)

**Purpose**: Inicializar o mÃ³dulo `parametrizacoes` e adoptar Flyway.

- [x] T001 Verificar se `flyway-core` e `flyway-database-postgresql` jÃ¡ estÃ£o no `pom.xml` (podem ter sido adicionados upstream); se ausentes, adicionar alinhados com Spring Boot 3.5.3
- [x] T002 [P] Criar a estrutura de pastas Java do mÃ³dulo em `src/main/java/cv/igrp/RH_Service/parametrizacoes/` com subpastas `domain/{models,valueobject,repository,filter,service}`, `application/{commands,queries,dto,constants}`, `infrastructure/{mappers,persistence/{entity,repository,adapters}}`, `interfaces/rest/`
- [x] T003 [P] Criar a estrutura `.igrpstudio/parametrizacoes/` com `module.json`, `controllers/`, `dto/`, `models/` (verificar se jÃ¡ existe; criar `.gitkeep` em pastas vazias)
- [x] T004 [P] Verificar se `src/main/resources/db/migration/` jÃ¡ existe; criar se ausente
- [x] T004a Determinar a base de numeraÃ§Ã£o Flyway: listar `src/main/resources/db/migration/` ordenado (`ls -1 | sort`), identificar o maior nÃºmero de versÃ£o presente (ex: `V1.0__Seed_Institutional_Identity.sql` â†’ Ãºltimo Ã© `1`), e anotar `LAST_V`. Todas as 16 migrations desta feature sÃ£o criadas em sequÃªncia `LAST_V+1` â€¦ `LAST_V+16`, na ordem definida na secÃ§Ã£o "SequÃªncia de Migrations" abaixo. **LAST_V = 1 â†’ migraÃ§Ãµes comeÃ§am em V2__**
- [x] T005 Verificar se `spring.flyway.enabled=true` e `spring.flyway.baseline-on-migrate=true` jÃ¡ estÃ£o em `application-development.properties`; adicionar em `application.properties` se ausentes nos perfis staging/production
- [x] T006 Mudar `spring.jpa.hibernate.ddl-auto` de `update` para `validate` em `src/main/resources/application-development.properties` (Flyway passa a ser fonte Ãºnica de schema)
- [x] T007 [P] Verificar `pom.xml` tem `igrp.framework.core` e dependÃªncias de testes (JUnit 5, Mockito, Testcontainers jÃ¡ presentes); adicionar `org.testcontainers:postgresql` se ausente

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Refactorar `OptionEntity` e `TipoDocumentoEntity` para o novo mÃ³dulo. **Sem isto, US1 e US2 nÃ£o podem avanÃ§ar.**

**âš ï¸ CRITICAL**: Bloqueia todas as user stories.

- [x] T008 Editar `.igrpstudio/shared/models/OptionEntity.json`: alterar `"module":"shared"` para `"module":"parametrizacoes"`; mover ficheiro com `git mv` para `.igrpstudio/parametrizacoes/models/OptionEntity.json`
- [x] T009 Mover `OptionEntity.java` com `git mv` de `src/main/java/cv/igrp/RH_Service/shared/infrastructure/persistence/entity/` para `src/main/java/cv/igrp/RH_Service/parametrizacoes/infrastructure/persistence/entity/`; actualizar declaraÃ§Ã£o `package` no ficheiro
- [x] T010 Mover `OptionEntityRepository.java` com `git mv` de `src/main/java/cv/igrp/RH_Service/shared/infrastructure/persistence/repository/` para `src/main/java/cv/igrp/RH_Service/parametrizacoes/infrastructure/persistence/repository/`; actualizar `package` e import de `OptionEntity`
- [x] T011 Fix imports em todos os ficheiros do mÃ³dulo `funcionarios/` legacy que referenciam `cv.igrp.RH_Service.shared.infrastructure.persistence.entity.OptionEntity` ou `...repository.OptionEntityRepository` â€” substituir pelo path novo `cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.{entity,repository}`
- [x] T012 Editar `.igrpstudio/shared/models/TipoDocumentoEntity.json`: alterar `"module":"shared"` para `"module":"parametrizacoes"`; renomear `"name":"TipoDocumentoEntity"` para `"name":"DocumentTypeEntity"` (manter `"tableName"` igual para nÃ£o quebrar schema existente em dev); acrescentar atributos `allowedExtensions` (string nullable) e `categoryOptionId` (UUID, FKâ†’option_entity, nullable); mover ficheiro para `.igrpstudio/parametrizacoes/models/DocumentTypeEntity.json`
- [x] T013 Mover e renomear `TipoDocumentoEntity.java` â†’ `DocumentTypeEntity.java` em `parametrizacoes/infrastructure/persistence/entity/`; actualizar `package`, classe, imports, e adicionar campos `allowedExtensions` e `categoryOptionId` (com `@ManyToOne` para `OptionEntity`)
- [x] T014 Mover e renomear `TipoDocumentoEntityRepository.java` â†’ `DocumentTypeEntityRepository.java`; actualizar `package` e imports
- [x] T015 Fix imports em `funcionarios/` legacy que referenciam `TipoDocumentoEntity` ou `TipoDocumentoEntityRepository` â€” actualizar para `DocumentTypeEntity` no novo path
- [x] T016 Validar compilaÃ§Ã£o: `mvn clean compile` deve devolver `BUILD SUCCESS`
- [x] T017 Validar testes existentes: `mvn test` â€” 83/84 testes passam; 1 falha pre-existente em `sigdi` (`t_key_results.criteria_alcancado`) nÃ£o relacionada com esta feature

**Checkpoint**: Foundation pronta â€” refactor de `OptionEntity` e `TipoDocumentoEntity â†’ DocumentTypeEntity` validado, mÃ³dulo `parametrizacoes/` com estrutura preparada. Pode arrancar trabalho das user stories.

---

## Phase 3: User Story 1 â€” CatÃ¡logos de Etiqueta para FormulÃ¡rios (Priority: P1) ðŸŽ¯ MVP

**Goal**: Disponibilizar o catÃ¡logo genÃ©rico `option_entity` populado com o "kit Cabo Verde" para todos os 11 grupos, com endpoints REST `/reference/options` para CRUD e listagem com fallback de locale.

**Independent Test**: ApÃ³s arranque, `GET /api/v1/rh/reference/options?ccode=MARITAL_STATUS` devolve 5 entradas em pt-CV; `POST /api/v1/rh/reference/options` cria nova entrada que aparece na listagem; re-arranque da aplicaÃ§Ã£o nÃ£o duplica nem remove dados.

### Schema & Seed (Migrations)

- [x] T018 [P] [US1] Criar `src/main/resources/db/migration/V{LAST_V+1}__create_option_entity.sql` (migration 1/16 desta feature) com `CREATE TABLE IF NOT EXISTS t_option_entity (id UUID PK, ccode, ckey, cvalue, locale, sort_order, active, description, created_at, created_by, updated_at, updated_by)` + `UNIQUE (ccode, ckey, locale)` + index `idx_option_ccode_locale_active`
- [x] T019 [P] [US1] Criar `src/main/resources/db/migration/V{LAST_V+9}__seed_option_entity_pt_cv.sql` (migration 9/16 desta feature) com `INSERT ... ON CONFLICT (ccode, ckey, locale) DO NOTHING` para os 11 grupos: `MARITAL_STATUS` (5 entradas), `SEX` (2), `NATIONALITY` (~15), `UNIT_TYPE` (4), `DOC_CATEGORY` (5), `LEAVE_CATEGORY` (4), `QUALIFICATION_LEVEL` (5), `RELATIONSHIP_TYPE` (5), `ISLAND` (10), `CONCELHO` (22), `TRAINING_TYPE` (4) â€” todos em `locale='pt-CV'`

### Domain Layer

- [x] T020 [P] [US1] Criar `parametrizacoes/domain/models/Option.java` com factory methods `criar()`, `reconstruir()`, `atualizar()`, `desativar()`, `reativar()`; validaÃ§Ã£o de `ccode` âˆˆ conjunto fechado dos 11 grupos no `criar()`
- [x] T021 [P] [US1] Criar `parametrizacoes/domain/repository/OptionRepository.java` com mÃ©todos `save(Option)`, `findById(ExternalID)`, `findByCcodeAndLocale(ccode, locale, active)`, `existsByCcodeAndCkeyAndLocale(...)`, `delete(ExternalID)`
- [x] T022 [P] [US1] Criar `parametrizacoes/domain/filter/OptionFilter.java` com campos `ccode`, `locale`, `active`, `ckey` para queries paginadas
- [x] T023 [US1] Criar `parametrizacoes/domain/service/ReferenceLookupService.java` com mÃ©todo `findByCcode(ccode, locale)` que faz fallback automÃ¡tico para `pt-CV` quando o locale pedido nÃ£o existe (depende de T020, T021)

### Infrastructure Layer

- [x] T024 [P] [US1] Criar `parametrizacoes/infrastructure/mappers/OptionMapper.java` para conversÃ£o `OptionEntity â†” Option`
- [x] T025 [US1] Criar `parametrizacoes/infrastructure/persistence/adapters/OptionRepositoryImpl.java` que implementa `OptionRepository` usando `OptionEntityRepository` Spring Data + `OptionMapper` (depende de T021, T024)

### IGRP Manifests + Generation

- [x] T026 [P] [US1] Criar `.igrpstudio/parametrizacoes/dto/OptionRequestDTO.json` (campos: `ccode`, `ckey`, `cvalue`, `locale`, `sortOrder`, `description`)
- [x] T027 [P] [US1] Criar `.igrpstudio/parametrizacoes/dto/OptionResponseDTO.json` (incluindo `id`, `active`, `createdAt`, `updatedAt`)
- [x] T028 [P] [US1] Criar `.igrpstudio/parametrizacoes/dto/WrapperListaOptionDTO.json` (resposta paginada)
- [x] T029 [P] [US1] Criar `.igrpstudio/parametrizacoes/dto/OptionFilterDTO.json` (query params para `GET /reference/options`)
- [x] T030 [P] [US1] Criar `.igrpstudio/parametrizacoes/controllers/ReferenceOptionsController.json` com 6 operaÃ§Ãµes: `GET /reference/options`, `GET /reference/options/{id}`, `POST /reference/options`, `PUT /reference/options/{id}`, `DELETE /reference/options/{id}`, `POST /reference/options/{id}/activate`
- [x] T031 [US1] Invocar skill `igrp-spring-generator` com `addController` para gerar `parametrizacoes/interfaces/rest/ReferenceOptionsController.java` e os DTOs em `parametrizacoes/application/dto/`

### Application Layer (Commands + Queries)

- [x] T032 [P] [US1] Criar `parametrizacoes/application/commands/CreateOptionCommand.java` + `CreateOptionCommandHandler.java` â€” validaÃ§Ã£o de `ccode` no conjunto fechado, unicidade `(ccode, ckey, locale)`, `active=true` por default
- [x] T033 [P] [US1] Criar `parametrizacoes/application/commands/UpdateOptionCommand.java` + `UpdateOptionCommandHandler.java` â€” apenas `cvalue`, `sortOrder`, `description` editÃ¡veis (campos imutÃ¡veis rejeitados em 400)
- [x] T034 [P] [US1] Criar `parametrizacoes/application/commands/DesativarOptionCommand.java` + `DesativarOptionCommandHandler.java` â€” verificaÃ§Ã£o de referÃªncias activas (preparatÃ³ria; em US1 ainda nÃ£o hÃ¡ referÃªncias externas porque outros catÃ¡logos vÃªm depois)
- [x] T035 [P] [US1] Criar `parametrizacoes/application/commands/AtivarOptionCommand.java` + `AtivarOptionCommandHandler.java` â€” reactiva entrada; falha se jÃ¡ estÃ¡ activa (409)
- [x] T036 [P] [US1] Criar `parametrizacoes/application/queries/ListOptionsQuery.java` + `ListOptionsQueryHandler.java` â€” paginaÃ§Ã£o + filtros via `OptionFilter`
- [x] T037 [P] [US1] Criar `parametrizacoes/application/queries/GetOptionQuery.java` + `GetOptionQueryHandler.java` â€” devolve por ID; 404 se inexistente
- [x] T038 [P] [US1] Criar `parametrizacoes/application/queries/FindByCcodeQuery.java` + `FindByCcodeQueryHandler.java` â€” usa `ReferenceLookupService` com fallback de locale
- [x] T039 [US1] Wire dos handlers no `ReferenceOptionsController` (substituir TODOs gerados pelo IGRP por chamadas a `commandBus.dispatch(...)` e `queryBus.dispatch(...)`)

### Tests for User Story 1

- [x] T040 [P] [US1] Criar `src/test/java/cv/igrp/RH_Service/parametrizacoes/domain/models/OptionTest.java` â€” valida factory methods, validaÃ§Ã£o de `ccode`, comportamento de `desativar()` / `reativar()`
- [x] T041 [P] [US1] Criar `src/test/java/cv/igrp/RH_Service/parametrizacoes/domain/service/ReferenceLookupServiceTest.java` â€” valida fallback `pt-CV` quando locale pedido nÃ£o existe
- [x] T042 [P] [US1] Criar `src/test/java/cv/igrp/RH_Service/parametrizacoes/application/commands/CreateOptionCommandHandlerTest.java` â€” valida unicidade, default de `active=true`, rejeiÃ§Ã£o de `ccode` invÃ¡lido
- [x] T043 [P] [US1] Criar `src/test/java/cv/igrp/RH_Service/parametrizacoes/application/commands/UpdateOptionCommandHandlerTest.java` â€” valida que `ccode`, `ckey`, `locale` sÃ£o imutÃ¡veis
- [x] T044 [P] [US1] Criar `src/test/java/cv/igrp/RH_Service/parametrizacoes/infrastructure/migrations/OptionMigrationIT.java` (Testcontainers + PostgreSQL real) â€” corre Flyway duas vezes e valida idempotÃªncia (contagens iguais)
- [x] T045 [US1] Criar `src/test/java/cv/igrp/RH_Service/parametrizacoes/interfaces/rest/ReferenceOptionsControllerIT.java` (MockMvc) â€” testes de cada endpoint com cenÃ¡rios happy path + 400/404/409

- [x] T045a Correr `mvn clean compile` â€” BUILD SUCCESS obrigatÃ³rio; corrigir qualquer erro antes de avanÃ§ar
- [x] T045b Correr `mvn test` â€” 20 novos testes passam (unit); ControllerIT e MigrationIT requerem contexto Spring/Docker (excluÃ­dos de unit run)
- [x] T045c Commit da fase: `git commit -m "feat(parametrizacoes): implement option entity catalog (US1)"`

**Checkpoint**: User Story 1 funcional. CatÃ¡logo de etiquetas operacional via REST com fallback de locale e idempotÃªncia validada. **MVP entregÃ¡vel.**

---

## Phase 4: User Story 2 â€” CatÃ¡logos com Comportamento de NegÃ³cio (Priority: P1)

**Goal**: Implementar 6 catÃ¡logos com comportamento prÃ³prio: `worker_states`, `professional_situations`, `contract_types`, `document_types` (refactor), `leave_types`, `leave_mobility_subtypes`. Cada um com flags que afectam processamento, e com seed completo segundo a legislaÃ§Ã£o cabo-verdiana.

**Independent Test**: ApÃ³s arranque, `GET /worker-states` devolve 3 entradas com `ACTIVE` e `INACTIVE` marcados como `isCore=true`; tentativa de `DELETE /worker-states/{id}` em estado nÃºcleo retorna 409; `GET /leave-types` devolve 6 tipos com flags `deductsBalance` / `requiresApproval` configuradas.

### Schema & Seed Migrations (paralelas â€” ficheiros distintos)

- [x] T046 [P] [US2] Criar `V{LAST_V+2}__create_worker_states.sql` (migration 2/16) em `src/main/resources/db/migration/` â€” `t_worker_state` com `is_core BOOLEAN`, UNIQUE `code`
- [x] T047 [P] [US2] Criar `V{LAST_V+3}__create_professional_situations.sql` (migration 3/16) â€” `t_professional_situation`, UNIQUE `code`
- [x] T048 [P] [US2] Criar `V{LAST_V+4}__create_contract_types.sql` (migration 4/16) â€” `t_contract_type`, UNIQUE `code`
- [x] T049 [P] [US2] Criar `V{LAST_V+5}__create_document_types.sql` (migration 5/16) â€” se tabela `t_tipo_documento` existir, `ALTER TABLE` para renomear e adicionar `allowed_extensions VARCHAR(200)`, `category_option_id UUID FKâ†’t_option_entity`; senÃ£o `CREATE TABLE t_document_type`
- [x] T050 [P] [US2] Criar `V{LAST_V+6}__create_leave_types.sql` (migration 6/16) â€” `t_leave_type` com flags `deducts_balance`, `requires_approval`, `max_days_per_year`, `category_option_id` FK
- [x] T051 [P] [US2] Criar `V{LAST_V+7}__create_leave_mobility_subtypes.sql` (migration 7/16) â€” `t_leave_mobility_subtype` com `record_type`, `affects_pay`, `counts_for_seniority`, `can_self_submit`
- [x] T052 [P] [US2] Criar `V{LAST_V+10}__seed_worker_states.sql` (migration 10/16) â€” 3 entradas com `ON CONFLICT (code) DO NOTHING`: ACTIVE/is_core=TRUE, INACTIVE/is_core=TRUE, SUSPENDED/is_core=FALSE
- [x] T053 [P] [US2] Criar `V{LAST_V+11}__seed_professional_situations.sql` (migration 11/16) â€” 4: EFETIVO, CONTRATADO, COMISSIONADO, ESTAGIARIO
- [x] T054 [P] [US2] Criar `V{LAST_V+12}__seed_contract_types.sql` (migration 12/16) â€” 5 conforme Decreto-Lei 4/2024
- [x] T055 [P] [US2] Criar `V{LAST_V+13}__seed_document_types.sql` (migration 13/16) â€” 10 tipos com `allowed_extensions` e `category_option_id` apontando para `t_option_entity` ccode='DOC_CATEGORY'
- [x] T056 [P] [US2] Criar `V{LAST_V+14}__seed_leave_types.sql` (migration 14/16) â€” 6 tipos da Lei 20/X/2023 com flags prÃ©-configuradas
- [x] T057 [P] [US2] Criar `V{LAST_V+15}__seed_leave_mobility_subtypes.sql` (migration 15/16) â€” ~6 subtipos base

### Domain Models (paralelos â€” ficheiros distintos)

- [x] T058 [P] [US2] Criar `parametrizacoes/domain/models/WorkerState.java` â€” factory methods + `desativar()` que lanÃ§a exception se `isCore=true`
- [x] T059 [P] [US2] Criar `parametrizacoes/domain/models/ProfessionalSituation.java` com factory methods padrÃ£o
- [x] T060 [P] [US2] Criar `parametrizacoes/domain/models/ContractType.java`
- [x] T061 [P] [US2] Criar `parametrizacoes/domain/models/DocumentType.java` com `allowedExtensions` e `categoryOptionId`; validaÃ§Ã£o de `categoryOptionId` referenciar option `ccode='DOC_CATEGORY'`
- [x] T062 [P] [US2] Criar `parametrizacoes/domain/models/LeaveType.java` com flags + validaÃ§Ã£o de `categoryOptionId` referenciar `ccode='LEAVE_CATEGORY'`
- [x] T063 [P] [US2] Criar `parametrizacoes/domain/models/LeaveMobilitySubtype.java` com flags e validaÃ§Ã£o de `recordType` âˆˆ {LICENCA, MOBILIDADE, AMBOS}

### Repository Interfaces (paralelas)

- [x] T064 [P] [US2] Criar `parametrizacoes/domain/repository/WorkerStateRepository.java`
- [x] T065 [P] [US2] Criar `parametrizacoes/domain/repository/ProfessionalSituationRepository.java`
- [x] T066 [P] [US2] Criar `parametrizacoes/domain/repository/ContractTypeRepository.java`
- [x] T067 [P] [US2] Criar `parametrizacoes/domain/repository/DocumentTypeRepository.java`
- [x] T068 [P] [US2] Criar `parametrizacoes/domain/repository/LeaveTypeRepository.java`
- [x] T069 [P] [US2] Criar `parametrizacoes/domain/repository/LeaveMobilitySubtypeRepository.java`

### Mappers e Adapters (paralelos)

- [x] T070 [P] [US2] Criar `WorkerStateMapper.java` + `WorkerStateRepositoryImpl.java` em `parametrizacoes/infrastructure/`
- [x] T071 [P] [US2] Criar `ProfessionalSituationMapper.java` + `ProfessionalSituationRepositoryImpl.java`
- [x] T072 [P] [US2] Criar `ContractTypeMapper.java` + `ContractTypeRepositoryImpl.java`
- [x] T073 [P] [US2] Criar `DocumentTypeMapper.java` + `DocumentTypeRepositoryImpl.java`
- [x] T074 [P] [US2] Criar `LeaveTypeMapper.java` + `LeaveTypeRepositoryImpl.java`
- [x] T075 [P] [US2] Criar `LeaveMobilitySubtypeMapper.java` + `LeaveMobilitySubtypeRepositoryImpl.java`

### IGRP Manifests (paralelos)

- [x] T076 [P] [US2] Criar `.igrpstudio/parametrizacoes/models/WorkerStateEntity.json`
- [x] T077 [P] [US2] Criar `.igrpstudio/parametrizacoes/models/ProfessionalSituationEntity.json`
- [x] T078 [P] [US2] Criar `.igrpstudio/parametrizacoes/models/ContractTypeEntity.json`
- [x] T079 [P] [US2] Criar `.igrpstudio/parametrizacoes/models/LeaveTypeEntity.json`
- [x] T080 [P] [US2] Criar `.igrpstudio/parametrizacoes/models/LeaveMobilitySubtypeEntity.json` *(DocumentTypeEntity jÃ¡ criado em T012)*
- [x] T081 [P] [US2] Criar `.igrpstudio/parametrizacoes/dto/` para os 6 catÃ¡logos: `*RequestDTO.json`, `*ResponseDTO.json`, `WrapperLista*DTO.json`, `*FilterDTO.json` (24 ficheiros)
- [x] T082 [P] [US2] Criar `.igrpstudio/parametrizacoes/controllers/WorkerStateController.json`
- [x] T083 [P] [US2] Criar `.igrpstudio/parametrizacoes/controllers/ProfessionalSituationController.json`
- [x] T084 [P] [US2] Criar `.igrpstudio/parametrizacoes/controllers/ContractTypeController.json`
- [x] T085 [P] [US2] Criar `.igrpstudio/parametrizacoes/controllers/DocumentTypeController.json`
- [x] T086 [P] [US2] Criar `.igrpstudio/parametrizacoes/controllers/LeaveTypeController.json`
- [x] T087 [P] [US2] Criar `.igrpstudio/parametrizacoes/controllers/LeaveMobilitySubtypeController.json`

### Run IGRP Generator

- [x] T088 [US2] Invocar skill `igrp-spring-generator` para gerar `*Entity.java`, `*EntityRepository.java`, controllers e DTOs em massa para os 6 catÃ¡logos (depende de T046â€“T087)

### Application Layer â€” Commands + Queries (4 commands + 2 queries por entity)

- [x] T089 [P] [US2] WorkerState: `Create/Update/Desativar/Ativar` commands + `List/Get` queries em `parametrizacoes/application/{commands,queries}/`
- [x] T090 [P] [US2] ProfessionalSituation: idem
- [x] T091 [P] [US2] ContractType: idem
- [x] T092 [P] [US2] DocumentType: idem com validaÃ§Ã£o extra de `categoryOptionId` (deve ser ccode='DOC_CATEGORY') e formato `allowedExtensions`
- [x] T093 [P] [US2] LeaveType: idem com validaÃ§Ã£o extra de `categoryOptionId` (deve ser ccode='LEAVE_CATEGORY') e `maxDaysPerYear â‰¥ 0`
- [x] T094 [P] [US2] LeaveMobilitySubtype: idem com validaÃ§Ã£o de `recordType`

### Wire Controllers

- [x] T095 [US2] Wire dos 6 controllers gerados â€” substituir TODOs por chamadas a `commandBus`/`queryBus`. ConfirmaÃ§Ã£o: cada controller delega para o handler correcto, sem lÃ³gica de negÃ³cio inline

### Tests for User Story 2

- [x] T096 [P] [US2] Testes unitÃ¡rios dos 6 domain models â€” em particular `WorkerStateTest.deactivateCoreThrowsException()`, `DocumentTypeTest.invalidCategoryRejected()`, `LeaveTypeTest.invalidMaxDaysRejected()`
- [x] T097 [P] [US2] Testes unitÃ¡rios dos handlers crÃ­ticos â€” Create/Update/Desativar de cada catÃ¡logo (~12 classes de teste)
- [x] T098 [P] [US2] Integration test: migrations de schema e seed do US2 (T046â€“T057) idempotentes (Testcontainers) â€” `MigrationsIdempotencyIT`; corre Flyway duas vezes e valida que contagens das 6 tabelas sÃ£o iguais
- [x] T099 [US2] Integration tests dos 6 controllers via MockMvc â€” cobre 200/201/204/400/404/409 para cada operaÃ§Ã£o

- [x] T099a Correr `mvn clean compile` â€” BUILD SUCCESS obrigatÃ³rio; corrigir qualquer erro antes de avanÃ§ar
- [x] T099b Correr `mvn test` â€” todos os testes devem passar (novos + anteriores); falha bloqueia avanÃ§o
- [x] T099c Commit da fase: `git commit -m "feat(parametrizacoes): implement behavioral catalogs (US2)"`

**Checkpoint**: 6 catÃ¡logos com comportamento operacionais. Bloqueio de `is_core` validado. ValidaÃ§Ã£o cruzada de `category_option_id` validada. Sistema arranca com seed completo.

---

## Phase 5: User Story 3 â€” Feriados Nacionais e Municipais (Priority: P2)

**Goal**: Disponibilizar o catÃ¡logo de feriados com flag `is_national` e suporte de filtros por ano e por escopo, alimentando o cÃ¡lculo de dias Ãºteis em features posteriores.

**Independent Test**: ApÃ³s arranque, `GET /public-holidays?year=2026` devolve ~10 feriados nacionais oficiais; `POST` cria um feriado municipal; tentativa de criar dois nacionais com a mesma data devolve 409.

- [x] T100 [P] [US3] Criar `V{LAST_V+8}__create_public_holidays.sql` (migration 8/16) em `src/main/resources/db/migration/` â€” `t_public_holiday` com `holiday_date DATE`, `is_national BOOLEAN`; partial unique index `idx_public_holiday_national_date ON t_public_holiday (holiday_date) WHERE is_national = TRUE AND is_active = TRUE`
- [x] T101 [P] [US3] Criar `V{LAST_V+16}__seed_public_holidays_2026.sql` (migration 16/16) â€” 10 feriados nacionais oficiais com `ON CONFLICT DO NOTHING`
- [x] T102 [P] [US3] Domain model `PublicHoliday.java` em `parametrizacoes/domain/models/`
- [x] T103 [P] [US3] Repository `PublicHolidayRepository.java` em `parametrizacoes/domain/repository/`
- [x] T104 [P] [US3] Mapper + Adapter em `parametrizacoes/infrastructure/`
- [x] T105 [P] [US3] IGRP manifests: `PublicHolidayEntity.json`, controller `PublicHolidayController.json`, DTOs (Request, Response, WrapperLista, Filter)
- [x] T106 [US3] Run `igrp-spring-generator` para `PublicHoliday`
- [x] T107 [P] [US3] Commands + Handlers (Create/Update/Desativar/Ativar) em `application/commands/`
- [x] T108 [P] [US3] Queries + Handlers (List com filtros `year`, `isNational`, `dateFrom`, `dateTo`; Get) em `application/queries/`
- [x] T109 [US3] Wire `PublicHolidayController` aos handlers
- [x] T110 [P] [US3] Test: `PublicHolidayTest` (factory methods, soft delete, partial unique index)
- [x] T111 [P] [US3] Integration test: tentativa de duplicar feriado nacional na mesma data falha com 409 â€” `PublicHolidayControllerIT`

- [x] T111a Correr `mvn clean compile` â€” BUILD SUCCESS obrigatÃ³rio; corrigir qualquer erro antes de avanÃ§ar
- [x] T111b Correr `mvn test` â€” todos os testes devem passar; falha bloqueia avanÃ§o
- [x] T111c Commit da fase: `git commit -m "feat(parametrizacoes): implement public holidays catalog (US3)"`

**Checkpoint**: CatÃ¡logo de feriados operacional. PrÃ©-requisito para `feat/ausencias` validado.

---

## Phase 6: User Story 4 â€” Auditoria Completa de AlteraÃ§Ãµes (Priority: P2)

**Goal**: Garantir que cada alteraÃ§Ã£o em qualquer catÃ¡logo Ã© auditada (autor, momento, valor anterior, valor novo) com retenÃ§Ã£o indefinida, e disponibilizar consulta do histÃ³rico por entrada.

**Independent Test**: ApÃ³s uma alteraÃ§Ã£o via PUT em qualquer catÃ¡logo, a tabela `*_AUD` correspondente do Envers contÃ©m uma nova linha com o utilizador e timestamp; `GET /audit/{table}/{id}` devolve o histÃ³rico de alteraÃ§Ãµes daquela entrada.

- [ ] T112 [US4] Verificar configuraÃ§Ã£o Envers em `application*.properties`: descomentar `spring.jpa.properties.org.hibernate.envers.track_entities_changed_in_revision=true` e `global_with_modified_flag=true` em todos os perfis
- [ ] T113 [US4] Validar que `ApplicationAuditorAware` (em `shared/config/`) injecta o utilizador autenticado em `created_by`/`updated_by` via `SecurityContextHelper`
- [ ] T114 [US4] Confirmar que cada uma das 8 entities estÃ¡ marcada com `@Audited` (Envers gera `*_AUD` automaticamente)
- [ ] T115 [P] [US4] Criar `parametrizacoes/application/queries/GetAuditHistoryQuery.java` + `GetAuditHistoryQueryHandler.java` â€” devolve histÃ³rico de alteraÃ§Ãµes de uma entrada por `(tableName, entityId)` usando `AuditReader` do Envers
- [ ] T116 [P] [US4] Criar manifest `.igrpstudio/parametrizacoes/controllers/AuditHistoryController.json` com `GET /audit/{table}/{id}` (limitado a entries dos 8 catÃ¡logos)
- [ ] T117 [US4] Run `igrp-spring-generator` para `AuditHistoryController` + DTOs (`AuditHistoryEntryDTO`, `WrapperListaAuditHistoryDTO`)
- [ ] T118 [US4] Wire `AuditHistoryController` ao handler
- [ ] T119 [P] [US4] Integration test: `AuditTrailIT` â€” cria entrada, edita, desactiva, reactiva; valida que histÃ³rico contÃ©m 4 revisions com utilizadores e tipos de operaÃ§Ã£o correctos
- [ ] T120 [US4] Documentar polÃ­tica de retenÃ§Ã£o indefinida em `quickstart.md` (secÃ§Ã£o troubleshooting/auditoria)

- [ ] T120a Correr `mvn clean compile` â€” BUILD SUCCESS obrigatÃ³rio; corrigir qualquer erro antes de avanÃ§ar
- [ ] T120b Correr `mvn test` â€” todos os testes devem passar; falha bloqueia avanÃ§o
- [ ] T120c Commit da fase: `git commit -m "feat(parametrizacoes): implement audit history (US4)"`

**Checkpoint**: Auditoria operacional para os 8 catÃ¡logos. HistÃ³rico consultÃ¡vel via API.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Caching, seguranÃ§a placeholder, validaÃ§Ã£o final, documentaÃ§Ã£o.

- [ ] T121 [P] Adicionar `@Cacheable("reference-options")` em `FindByCcodeQueryHandler` e `ListOptionsQueryHandler` (TTL local 60 s via Spring Cache `ConcurrentMapCacheManager` ou Caffeine)
- [ ] T122 [P] Adicionar `@CacheEvict` nos handlers de `Create/Update/Desativar/Ativar` para invalidar cache quando hÃ¡ escrita
- [ ] T122a [P] Criar `src/test/java/cv/igrp/RH_Service/parametrizacoes/infrastructure/cache/ReferenceLookupCacheIT.java` â€” valida SC-005: apÃ³s `Create/Update/Deactivate` em catÃ¡logo, leitura subsequente reflecte a alteraÃ§Ã£o em â‰¤ 60 s. CenÃ¡rios: (1) cache hit antes de escrita; (2) escrita invalida cache imediatamente via `@CacheEvict`; (3) sem `@CacheEvict` o cache reflecte a alteraÃ§Ã£o apÃ³s expiraÃ§Ã£o natural do TTL
- [ ] T123 [P] Adicionar `@PreAuthorize("hasRole('PARAM_ADMIN')")` em todos os endpoints de escrita dos 8 controllers (placeholder atÃ© decisÃ£o final dos roles)
- [ ] T124 [P] Adicionar `@PreAuthorize("isAuthenticated()")` em endpoints de leitura
- [ ] T125 Configurar Spring Cache em `application.properties` (`spring.cache.type=caffeine`) e adicionar dependÃªncia `caffeine` em `pom.xml` se ausente
- [ ] T126 Correr `mvn clean compile` final â€” confirmar `BUILD SUCCESS`
- [ ] T127 Correr `mvn test` final â€” confirmar 100% pass com testes novos somados aos 84 existentes
- [ ] T128 Executar todo o quickstart.md em ambiente local â€” start aplicaÃ§Ã£o, validar via Swagger UI todos os endpoints dos 8 catÃ¡logos, validar idempotÃªncia (re-arranque sem novas linhas), validar preservaÃ§Ã£o de alteraÃ§Ãµes administrativas
- [ ] T129 [P] Actualizar `endpoints.md` na raiz do projecto com os ~30 endpoints novos do mÃ³dulo `parametrizacoes`
- [ ] T130 [P] Actualizar `CLAUDE.md` em "Key Domain Models" com as 8 entidades novas
- [ ] T131 Final Constitution Check â€” re-validar os 5 princÃ­pios + restriÃ§Ãµes tÃ©cnicas apÃ³s implementaÃ§Ã£o completa; documentar em `plan.md` na secÃ§Ã£o "Constitution Re-check (post-implementation)"

---

## SequÃªncia de Migrations

> ReferÃªncia para resolver `V{LAST_V+N}__` â€” executar T004a primeiro para descobrir `LAST_V`.
> A ordem numÃ©rica deve ser respeitada: schemas (offsets 1â€“8) antes de seeds (offsets 9â€“16).

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

**Exemplo**: se `LAST_V = 1` (apenas `V1.0__Seed_Institutional_Identity.sql` existe), entÃ£o migration 1/16 = `V2__create_option_entity.sql`, ..., migration 16/16 = `V17__seed_public_holidays_2026.sql`.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sem dependÃªncias â€” pode arrancar imediatamente
- **Foundational (Phase 2)**: Depende de Setup â€” **bloqueia** todas as user stories
- **User Stories (Phase 3-6)**:
  - US1 e US2 sÃ£o P1 â€” podem arrancar em paralelo apÃ³s Phase 2
  - US3 e US4 sÃ£o P2 â€” podem arrancar apÃ³s Phase 2; US4 (auditoria) beneficia de ter US1+US2 implementados primeiro para gerar revisions de teste
- **Polish (Phase 7)**: Depende de US1, US2 (US3 e US4 desejÃ¡veis mas nÃ£o bloqueantes para parte do polish)

### User Story Dependencies

- **US1 (P1)**: Pode arrancar imediatamente apÃ³s Phase 2 â€” sem dependÃªncias de outras stories
- **US2 (P1)**: Pode arrancar apÃ³s Phase 2 â€” algumas validaÃ§Ãµes cruzadas dependem de `option_entity` populado, mas como US1 tambÃ©m executa o seed em paralelo, fica resolvido na sequÃªncia natural
- **US3 (P2)**: Independente â€” pode arrancar apÃ³s Phase 2
- **US4 (P2)**: Beneficia de ter US1+US2+US3 implementados (mais entities para auditar), mas a configuraÃ§Ã£o base do Envers e o controller genÃ©rico sÃ£o independentes

### Within Each User Story

- Migrations antes de domain models (schema deve estar definido)
- Domain models antes de repositÃ³rios (interfaces dependem do tipo do aggregate)
- Repository interfaces antes de adapters
- Mappers antes de adapters (adapters usam mappers)
- IGRP manifests antes de gerar Java
- Generator run antes de wire dos controllers
- Handlers antes de wire dos controllers
- Implementation antes de tests
- Story complete antes de avanÃ§ar para a prÃ³xima

### Parallel Opportunities

| Grupo | Tasks | JustificaÃ§Ã£o |
|---|---|---|
| Setup [P] | T002, T003, T004, T007 | Ficheiros e pastas distintas |
| Phase 2 fix imports | T011 e T015 | Diferentes ficheiros legacy |
| US1 migrations | T018, T019 | Ficheiros SQL distintos |
| US1 domain | T020, T021, T022 | Classes Java distintas |
| US1 IGRP manifests | T026â€“T030 | JSON distintos |
| US1 commands/queries | T032â€“T038 | Classes distintas; desativar T034 nÃ£o bloqueia ativar T035 (entries diferentes) |
| US1 tests | T040â€“T044 | Classes de teste distintas |
| US2 migrations | T046â€“T057 | 12 ficheiros SQL distintos |
| US2 domain models | T058â€“T063 | 6 classes distintas |
| US2 repos | T064â€“T069 | 6 interfaces distintas |
| US2 mappers/adapters | T070â€“T075 | 12 ficheiros, 6 entities |
| US2 IGRP manifests | T076â€“T087 | ~30 JSON distintos |
| US2 handlers | T089â€“T094 | 6 entities Ã— ~6 handlers |
| US2 tests | T096â€“T098 | Classes distintas |
| US3 | T100â€“T108 sequÃªncia por dependÃªncia mas vÃ¡rias [P] dentro |
| US4 | T115, T116 [P] |
| Polish | T121, T122, T123, T124, T129, T130 [P] |

---

## Parallel Example: Setup + Phase 2

```bash
# Phase 1 (paralelizÃ¡veis):
Task: T002 - Criar estrutura de pastas Java do parametrizacoes/
Task: T003 - Criar estrutura .igrpstudio/parametrizacoes/
Task: T004 - Criar src/main/resources/db/migration/

# Phase 2 (sequencial: T008â†’T009â†’T010â†’T011, e T012â†’T013â†’T014â†’T015 tambÃ©m sequenciais por entity):
Task: T008â€“T011 (refactor OptionEntity, sequencial)
Task: T012â€“T015 (refactor TipoDocumentoEntity â†’ DocumentTypeEntity, sequencial)
# T011 e T015 podem ser paralelos entre si pois tocam ficheiros legacy distintos
```

## Parallel Example: User Story 2 â€” Implementation in Bulk

```bash
# ApÃ³s T088 (run generator), os 6 conjuntos de handlers podem ser implementados em paralelo:
Task: T089 - WorkerState handlers
Task: T090 - ProfessionalSituation handlers
Task: T091 - ContractType handlers
Task: T092 - DocumentType handlers
Task: T093 - LeaveType handlers
Task: T094 - LeaveMobilitySubtype handlers
```

---

## Implementation Strategy

### MVP First â€” User Story 1 Only

1. Phase 1 (Setup): T001â€“T007
2. Phase 2 (Foundational): T008â€“T017 (refactor `OptionEntity` e `DocumentTypeEntity`)
3. Phase 3 (US1): T018â€“T045
4. **STOP & VALIDATE**: Correr quickstart.md secÃ§Ãµes "Verificar o seed" e "Validar idempotÃªncia"; demonstrar `GET /reference/options?ccode=MARITAL_STATUS` no Swagger
5. Eventualmente deploy/demo

Se US1 funcional â†’ MVP entregue. CatÃ¡logo de etiquetas operacional para qualquer outra feature do refactor v4.

### Incremental Delivery

- **IteraÃ§Ã£o 1**: MVP US1 â†’ demo + validaÃ§Ã£o
- **IteraÃ§Ã£o 2**: US2 â†’ catÃ¡logos com comportamento (~60 tasks); demo
- **IteraÃ§Ã£o 3**: US3 â†’ feriados; demo
- **IteraÃ§Ã£o 4**: US4 â†’ auditoria + Polish

### Parallel Team Strategy (se houver 2+ devs)

- **Dev A**: Phase 1 + Phase 2 + US1 (por dependÃªncia sequencial)
- **Dev B**: ApÃ³s Phase 2 â€” US2 (catÃ¡logos com comportamento)
- **Dev C**: ApÃ³s Phase 2 â€” US3 + US4 (feriados + auditoria)

US1, US2, US3 e US4 sÃ£o independentes apÃ³s Phase 2 â€” podem ser desenvolvidas em paralelo sem conflitos de ficheiros.

---

## Notas

- `[P]` = ficheiro distinto, sem dependÃªncias bloqueantes â€” pode correr em paralelo
- `[Story]` = US1/US2/US3/US4 mapeia para a user story da spec
- Cada user story tem pelo menos 1 teste de integraÃ§Ã£o para validaÃ§Ã£o independente
- Commits frequentes (idealmente apÃ³s cada `T###` ou grupo lÃ³gico de [P])
- Quickstart.md Ã© a referÃªncia de "como validar manualmente" cada checkpoint
- NÃ£o acumular dependÃªncias cruzadas que quebrem a independÃªncia dos stories
- A decisÃ£o de autorizaÃ§Ã£o (perfis concretos) estÃ¡ diferida â€” `T123` usa placeholder `PARAM_ADMIN` que serÃ¡ substituÃ­do num commit futuro

## Resumo EstatÃ­stico

| Phase | Tasks | ParalelizÃ¡veis | Notas |
|---|---|---|---|
| 1 â€” Setup | T001â€“T007 (7) | 4 | DependÃªncias mÃ­nimas |
| 2 â€” Foundational | T008â€“T017 (10) | 1 | Maioria sequencial (refactor com fix imports) |
| 3 â€” US1 (P1, MVP) | T018â€“T045 (28) | ~22 | CatÃ¡logo de etiquetas + seed kit CV |
| 4 â€” US2 (P1) | T046â€“T099 (54) | ~46 | 6 catÃ¡logos com comportamento |
| 5 â€” US3 (P2) | T100â€“T111 (12) | ~9 | Feriados |
| 6 â€” US4 (P2) | T112â€“T120 (9) | ~3 | Auditoria via Envers |
| 7 â€” Polish | T121â€“T131 + T122a (12) | ~7 | Cache, teste de invalidaÃ§Ã£o, security placeholder, docs |
| **TOTAL** | **132 tasks** | **~92 [P]** | |

**MVP Scope (US1)**: 45 tasks (Phase 1 + Phase 2 + US1).

