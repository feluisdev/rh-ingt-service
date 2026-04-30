# Tasks: Estrutura Organizacional

**Input**: Design documents from `specs/002-estrutura-organizacional/`  
**Prerequisites**: plan.md ✅ spec.md ✅ research.md ✅ data-model.md ✅ contracts/ ✅ quickstart.md ✅

**Referência de código**: módulo `parametrizacoes/` em `src/main/java/cv/igrp/RH_Service/parametrizacoes/`  
**Package base**: `cv.igrp.RH_Service.estrutura`  
**Base REST path**: `api/v1/rh/estrutura/`

---

## Phase 1: Setup

**Purpose**: Criar estrutura de directórios do módulo e inicializar o manifest IGRP

- [x] T001 Criar estrutura de directórios do módulo `estrutura/` em `src/main/java/cv/igrp/RH_Service/estrutura/` com subpastas: `application/commands/`, `application/queries/`, `application/dto/`, `domain/models/`, `domain/filter/`, `domain/repository/`, `domain/valueobject/`, `infrastructure/persistence/entity/`, `infrastructure/persistence/repository/`, `infrastructure/persistence/adapters/`, `infrastructure/mappers/`, `interfaces/rest/`
- [x] T002 Criar `.igrpstudio/estrutura/module.json` com conteúdo `{"type": "module", "name": "estrutura"}` e directório `.igrpstudio/estrutura/controllers/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Migrações Flyway e DTOs partilhados — OBRIGATÓRIOS antes de qualquer user story

⚠️ **CRITICAL**: Nenhuma user story pode ser implementada antes desta fase estar completa

- [x] T003 Criar migração Flyway `src/main/resources/db/migration/V20__create_estrutura_tables.sql` com DDL das tabelas `t_unidade_organica` (com auto-referência `parent_unit_id`), `t_cargo` e `t_funcao` conforme `data-model.md`
- [x] T004 Criar migração Flyway `src/main/resources/db/migration/V21__seed_unit_types.sql` com INSERT idempotente dos 4 tipos de unidade orgânica (DIRECCAO, DEPARTAMENTO, DIVISAO, SECCAO) na tabela `option_entity` com `ccode='UNIT_TYPE'` usando `ON CONFLICT DO NOTHING`
- [x] T005 [P] Criar `AuditHistoryEntryDTO` em `src/main/java/cv/igrp/RH_Service/estrutura/application/dto/AuditHistoryEntryDTO.java` (campos: `revisionId int`, `revisionDate String`, `type String`) — idêntico ao de `parametrizacoes` mas no package `estrutura`
- [x] T006 [P] Criar `WrapperListaAuditHistoryDTO` em `src/main/java/cv/igrp/RH_Service/estrutura/application/dto/WrapperListaAuditHistoryDTO.java` com campos `content List<AuditHistoryEntryDTO>` e `totalElements long`

**Checkpoint**: Migrações e DTOs base prontos — implementação das user stories pode iniciar

---

## Phase 3: User Story 1 — Gestão de Unidades Orgânicas (Priority: P1) 🎯 MVP

**Goal**: CRUD completo de unidades orgânicas com hierarquia auto-referencial, regra de bloqueio de desactivação quando existem sub-unidades activas, e regra de reactivação condicional (mãe deve estar activa)

**Independent Test**: Criar unidade raiz + sub-unidade → tentar desactivar raiz com filho activo (deve falhar 409) → desactivar sub-unidade → desactivar raiz (deve passar) → confirmar auditoria registada

### Implementação — Domain Layer (US1)

- [x] T007 [P] [US1] Criar `OrganizationalUnitId` em `src/main/java/cv/igrp/RH_Service/estrutura/domain/valueobject/OrganizationalUnitId.java` seguindo o padrão de `WorkerStateId` (factory methods `gerarNovo()`, `from(UUID)`, `from(String)`; wraps `ExternalID`)
- [x] T008 [P] [US1] Criar `OrganizationalUnit` domain model em `src/main/java/cv/igrp/RH_Service/estrutura/domain/models/OrganizationalUnit.java` com campos: `OrganizationalUnitId id`, `String code`, `String name`, `String acronym`, `UUID unitTypeOptionId`, `OrganizationalUnitId parentUnitId` (nullable), `boolean isActive`
- [x] T009 [P] [US1] Criar `OrganizationalUnitFilter` em `src/main/java/cv/igrp/RH_Service/estrutura/domain/filter/OrganizationalUnitFilter.java` com campos: `Boolean isActive`, `UUID parentUnitId` (ambos opcionais)
- [x] T010 [US1] Criar `OrganizationalUnitRepository` port em `src/main/java/cv/igrp/RH_Service/estrutura/domain/repository/OrganizationalUnitRepository.java` com métodos: `save(OrganizationalUnit)`, `findById(OrganizationalUnitId)`, `findByCode(String)`, `findAll(OrganizationalUnitFilter, Pageable)`, `existsByCode(String)`, `existsActiveChildrenOf(OrganizationalUnitId)` (para regra de desactivação), `existsByCodeAndIdNot(String, OrganizationalUnitId)` (para update)

### Implementação — Infrastructure Layer (US1)

- [x] T011 [P] [US1] Criar `OrganizationalUnitEntity` em `src/main/java/cv/igrp/RH_Service/estrutura/infrastructure/persistence/entity/OrganizationalUnitEntity.java` com `@Audited`, `@IgrpEntity`, `@Entity`, `@Table(name="t_unidade_organica")`, estendendo `AuditEntity`; campos: `UUID id`, `String code`, `String name`, `String acronym`, `UUID unitTypeOptionId`, `UUID parentUnitId` (nullable), `Boolean isActive`
- [x] T012 [US1] Criar `OrganizationalUnitEntityRepository` em `src/main/java/cv/igrp/RH_Service/estrutura/infrastructure/persistence/repository/OrganizationalUnitEntityRepository.java` estendendo `JpaRepository<OrganizationalUnitEntity, UUID>` com métodos: `existsByCode(String)`, `existsByCodeAndIdNot(String, UUID)`, `existsByParentUnitIdAndIsActiveTrue(UUID)` (para `existsActiveChildrenOf`)
- [x] T013 [US1] Criar `OrganizationalUnitMapper` em `src/main/java/cv/igrp/RH_Service/estrutura/infrastructure/mappers/OrganizationalUnitMapper.java` com métodos `toDomain(OrganizationalUnitEntity)` e `toEntity(OrganizationalUnit)` mapeando `OrganizationalUnitId ↔ UUID` via `OrganizationalUnitId.from()` e `.getValor()`
- [x] T014 [US1] Criar `OrganizationalUnitRepositoryImpl` em `src/main/java/cv/igrp/RH_Service/estrutura/infrastructure/persistence/adapters/OrganizationalUnitRepositoryImpl.java` implementando `OrganizationalUnitRepository`; o método `existsActiveChildrenOf` delega em `existsByParentUnitIdAndIsActiveTrue(parentId.getValor())`; `findAll` usa `Specification` ou query derivada com filtros opcionais de `isActive` e `parentUnitId`

### Implementação — Application Layer (US1)

- [x] T015 [P] [US1] Criar DTOs em `src/main/java/cv/igrp/RH_Service/estrutura/application/dto/`: `OrganizationalUnitRequest.java` (campos: `code`, `name`, `acronym`, `unitTypeOptionId UUID`, `parentUnitId UUID` nullable), `OrganizationalUnitResponse.java` (mesmos campos + `id UUID`, `isActive`), `WrapperListaOrganizationalUnitDTO.java` (campos: `content List<OrganizationalUnitResponse>`, `totalElements long`)
- [x] T016 [US1] Criar `CreateOrganizationalUnitCommand` + `CreateOrganizationalUnitCommandHandler` em `src/main/java/cv/igrp/RH_Service/estrutura/application/commands/`; handler valida: (1) `code` único via `existsByCode()` → HTTP 409 se duplicado; (2) se `parentUnitId` não nulo → `findById()` e verificar `isActive` → HTTP 404 se não existe, HTTP 409 se inactiva; (3) `unitTypeOptionId` pertence a `ccode='UNIT_TYPE'` via `OptionRepository.findByCcodeAndLocale()` → HTTP 400 se inválido; gera novo `OrganizationalUnitId.gerarNovo()`; anotado com `@IgrpCommandHandler` e `@CacheEvict(cacheNames="organizationalUnitsCache", allEntries=true)`
- [x] T017 [US1] Criar `UpdateOrganizationalUnitCommand` + `UpdateOrganizationalUnitCommandHandler` em `src/main/java/cv/igrp/RH_Service/estrutura/application/commands/`; handler valida existência da entidade (HTTP 404), unicidade do `code` excluindo o próprio ID via `existsByCodeAndIdNot()` (HTTP 409), e as mesmas validações de `parentUnitId` e `unitTypeOptionId` do create; anotado com `@IgrpCommandHandler` e `@CacheEvict`
- [x] T018 [US1] Criar `DesativarOrganizationalUnitCommand` + `DesativarOrganizationalUnitCommandHandler` em `src/main/java/cv/igrp/RH_Service/estrutura/application/commands/`; handler verifica existência (HTTP 404), verifica que já não está inactiva (idempotência), e chama `existsActiveChildrenOf()` — se true lança `IgrpResponseStatusException.conflict("Não é possível desactivar: existem sub-unidades activas")` (HTTP 409); anotado com `@IgrpCommandHandler` e `@CacheEvict`
- [x] T019 [US1] Criar `AtivarOrganizationalUnitCommand` + `AtivarOrganizationalUnitCommandHandler` em `src/main/java/cv/igrp/RH_Service/estrutura/application/commands/`; handler verifica existência (HTTP 404), se `parentUnitId != null` verifica que a unidade-mãe está activa (HTTP 409 se inactiva); define `isActive = true` e persiste; anotado com `@IgrpCommandHandler` e `@CacheEvict`
- [x] T020 [US1] Criar `GetOrganizationalUnitsQuery` + `GetOrganizationalUnitsQueryHandler` em `src/main/java/cv/igrp/RH_Service/estrutura/application/queries/`; query tem campos `Boolean isActive`, `UUID parentUnitId`, `int pagina`, `int tamanho`; handler usa `OrganizationalUnitRepository.findAll()` com o filtro, mapeia para `WrapperListaOrganizationalUnitDTO`; anotado com `@IgrpQueryHandler` e `@Cacheable(cacheNames="organizationalUnitsCache")`
- [x] T021 [US1] Criar `GetOrganizationalUnitByIdQuery` + `GetOrganizationalUnitByIdQueryHandler` em `src/main/java/cv/igrp/RH_Service/estrutura/application/queries/`; query tem campo `String unitId`; handler chama `findById()`, lança HTTP 404 se não encontrado, mapeia para `OrganizationalUnitResponse`; anotado com `@IgrpQueryHandler`
- [x] T022 [US1] Copiar `specs/002-estrutura-organizacional/contracts/OrganizationalUnitController.json` para `.igrpstudio/estrutura/controllers/OrganizationalUnitController.json` e executar o skill `igrp-spring-generator` para gerar `src/main/java/cv/igrp/RH_Service/estrutura/interfaces/rest/OrganizationalUnitController.java`

**Checkpoint**: US1 completa — endpoints `api/v1/rh/estrutura/organizational-units` funcionais; testar com quickstart.md secção 2

---

## Phase 4: User Story 2 — Gestão de Cargos (Priority: P1)

**Goal**: CRUD completo do catálogo de cargos com soft delete bidirecional (desactivar + activar) e unicidade de código

**Independent Test**: Criar cargo → listar activos → duplicar código (409) → desactivar → activar → confirmar auditoria

### Implementação — Domain Layer (US2)

- [x] T023 [P] [US2] Criar `JobId` em `src/main/java/cv/igrp/RH_Service/estrutura/domain/valueobject/JobId.java` seguindo o padrão de `OrganizationalUnitId`
- [x] T024 [P] [US2] Criar `Job` domain model em `src/main/java/cv/igrp/RH_Service/estrutura/domain/models/Job.java` com campos: `JobId id`, `String code`, `String name`, `String description`, `boolean isActive`
- [x] T025 [P] [US2] Criar `JobFilter` em `src/main/java/cv/igrp/RH_Service/estrutura/domain/filter/JobFilter.java` com campo `Boolean isActive`
- [x] T026 [US2] Criar `JobRepository` port em `src/main/java/cv/igrp/RH_Service/estrutura/domain/repository/JobRepository.java` com: `save(Job)`, `findById(JobId)`, `findByCode(String)`, `findAll(JobFilter, Pageable)`, `existsByCode(String)`, `existsByCodeAndIdNot(String, JobId)`

### Implementação — Infrastructure Layer (US2)

- [x] T027 [P] [US2] Criar `JobEntity` em `src/main/java/cv/igrp/RH_Service/estrutura/infrastructure/persistence/entity/JobEntity.java` com `@Audited`, `@Table(name="t_cargo")`, campos: `UUID id`, `String code`, `String name`, `String description`, `Boolean isActive`
- [x] T028 [US2] Criar `JobEntityRepository` em `src/main/java/cv/igrp/RH_Service/estrutura/infrastructure/persistence/repository/JobEntityRepository.java` com `existsByCode(String)` e `existsByCodeAndIdNot(String, UUID)`
- [x] T029 [US2] Criar `JobMapper` em `src/main/java/cv/igrp/RH_Service/estrutura/infrastructure/mappers/JobMapper.java` com `toDomain(JobEntity)` e `toEntity(Job)`
- [x] T030 [US2] Criar `JobRepositoryImpl` em `src/main/java/cv/igrp/RH_Service/estrutura/infrastructure/persistence/adapters/JobRepositoryImpl.java` implementando `JobRepository`

### Implementação — Application Layer (US2)

- [x] T031 [P] [US2] Criar DTOs em `src/main/java/cv/igrp/RH_Service/estrutura/application/dto/`: `JobRequest.java` (code, name, description), `JobResponse.java` (id, code, name, description, isActive), `WrapperListaJobDTO.java` (content, totalElements)
- [x] T032 [US2] Criar `CreateJobCommand` + `CreateJobCommandHandler` em `src/main/java/cv/igrp/RH_Service/estrutura/application/commands/`; handler valida unicidade de `code` (HTTP 409); `@IgrpCommandHandler` + `@CacheEvict(cacheNames="jobsCache", allEntries=true)`
- [x] T033 [US2] Criar `UpdateJobCommand` + `UpdateJobCommandHandler` em `src/main/java/cv/igrp/RH_Service/estrutura/application/commands/`; valida existência (404) e unicidade excluindo próprio ID (409); `@CacheEvict`
- [x] T034 [US2] Criar `DesativarJobCommand` + `DesativarJobCommandHandler` + `AtivarJobCommand` + `AtivarJobCommandHandler` em `src/main/java/cv/igrp/RH_Service/estrutura/application/commands/`; sem restrições hierárquicas; `@CacheEvict`
- [x] T035 [US2] Criar `GetJobsQuery` + `GetJobsQueryHandler` + `GetJobByIdQuery` + `GetJobByIdQueryHandler` em `src/main/java/cv/igrp/RH_Service/estrutura/application/queries/`; `@Cacheable(cacheNames="jobsCache")` no GetJobsQueryHandler
- [x] T036 [US2] Copiar `specs/002-estrutura-organizacional/contracts/JobController.json` para `.igrpstudio/estrutura/controllers/JobController.json` e executar o skill `igrp-spring-generator` para gerar `src/main/java/cv/igrp/RH_Service/estrutura/interfaces/rest/JobController.java`

**Checkpoint**: US2 completa — endpoints `api/v1/rh/estrutura/jobs` funcionais; testar com quickstart.md secção 3

---

## Phase 5: User Story 3 — Gestão de Funções (Priority: P2)

**Goal**: CRUD completo do catálogo de funções — padrão idêntico ao de Cargos

**Independent Test**: Criar função → listar → duplicar código (409) → desactivar/activar → confirmar auditoria

### Implementação — Domain Layer (US3)

- [x] T037 [P] [US3] Criar `FunctionId` em `src/main/java/cv/igrp/RH_Service/estrutura/domain/valueobject/FunctionId.java`; criar `OrgFunction` domain model em `src/main/java/cv/igrp/RH_Service/estrutura/domain/models/OrgFunction.java` (id, code, name, description, isActive); criar `FunctionFilter` em `src/main/java/cv/igrp/RH_Service/estrutura/domain/filter/FunctionFilter.java` (isActive)
- [x] T038 [US3] Criar `FunctionRepository` port em `src/main/java/cv/igrp/RH_Service/estrutura/domain/repository/FunctionRepository.java` com mesmos métodos que `JobRepository` adaptados para `OrgFunction`/`FunctionId`

### Implementação — Infrastructure Layer (US3)

- [x] T039 [P] [US3] Criar `FunctionEntity` em `src/main/java/cv/igrp/RH_Service/estrutura/infrastructure/persistence/entity/FunctionEntity.java` com `@Audited`, `@Table(name="t_funcao")`, campos: `UUID id`, `String code`, `String name`, `String description`, `Boolean isActive`
- [x] T040 [US3] Criar `FunctionEntityRepository` em `src/main/java/cv/igrp/RH_Service/estrutura/infrastructure/persistence/repository/FunctionEntityRepository.java`
- [x] T041 [US3] Criar `FunctionMapper` em `src/main/java/cv/igrp/RH_Service/estrutura/infrastructure/mappers/FunctionMapper.java`
- [x] T042 [US3] Criar `FunctionRepositoryImpl` em `src/main/java/cv/igrp/RH_Service/estrutura/infrastructure/persistence/adapters/FunctionRepositoryImpl.java`

### Implementação — Application Layer (US3)

- [x] T043 [P] [US3] Criar DTOs em `src/main/java/cv/igrp/RH_Service/estrutura/application/dto/`: `FunctionRequest.java`, `FunctionResponse.java`, `WrapperListaFunctionDTO.java`
- [x] T044 [US3] Criar `CreateFunctionCommand` + `CreateFunctionCommandHandler` + `UpdateFunctionCommand` + `UpdateFunctionCommandHandler` em `src/main/java/cv/igrp/RH_Service/estrutura/application/commands/`; padrão idêntico a Jobs; `@CacheEvict(cacheNames="functionsCache", allEntries=true)`
- [x] T045 [US3] Criar `DesativarFunctionCommand` + `DesativarFunctionCommandHandler` + `AtivarFunctionCommand` + `AtivarFunctionCommandHandler` em `src/main/java/cv/igrp/RH_Service/estrutura/application/commands/`
- [x] T046 [US3] Criar `GetFunctionsQuery` + `GetFunctionsQueryHandler` + `GetFunctionByIdQuery` + `GetFunctionByIdQueryHandler` em `src/main/java/cv/igrp/RH_Service/estrutura/application/queries/`; `@Cacheable(cacheNames="functionsCache")`
- [x] T047 [US3] Copiar `specs/002-estrutura-organizacional/contracts/FunctionController.json` para `.igrpstudio/estrutura/controllers/FunctionController.json` e executar o skill `igrp-spring-generator` para gerar `src/main/java/cv/igrp/RH_Service/estrutura/interfaces/rest/FunctionController.java`

**Checkpoint**: US3 completa — endpoints `api/v1/rh/estrutura/functions` funcionais; testar com quickstart.md secção 4

---

## Phase 6: Polish & Auditoria (Cross-Cutting)

**Purpose**: Endpoint de auditoria Envers para os três catálogos + testes manuais finais

- [x] T048 Criar `GetEstruturaAuditHistoryQuery` em `src/main/java/cv/igrp/RH_Service/estrutura/application/queries/GetEstruturaAuditHistoryQuery.java` com campos `String catalog` e `String entityId`
- [x] T049 Criar `GetEstruturaAuditHistoryQueryHandler` em `src/main/java/cv/igrp/RH_Service/estrutura/application/queries/GetEstruturaAuditHistoryQueryHandler.java`; implementar com `@PersistenceContext EntityManager`, `@Transactional(readOnly=true)`, `@IgrpQueryHandler`; `CATALOG_MAP` mapeia `"organizational-units" → OrganizationalUnitEntity.class`, `"jobs" → JobEntity.class`, `"functions" → FunctionEntity.class`; usar `AuditReaderFactory.get(entityManager).createQuery().forRevisionsOfEntity(entityClass, false, true).add(AuditEntity.id().eq(uuid)).getResultList()`; retornar `WrapperListaAuditHistoryDTO` — seguir exactamente o padrão de `GetAuditHistoryQueryHandler` de `parametrizacoes`
- [x] T050 Gerado `EstruturaAuditHistoryController.json` em `.igrpstudio/estrutura/controllers/` e `EstruturaAuditHistoryController.java` em `src/main/java/cv/igrp/RH_Service/estrutura/interfaces/rest/`
- [x] T051 N/A — módulo `estrutura` não usa cache (apenas `reference-options` de `parametrizacoes` usa Caffeine); sem alterações necessárias no `application.yml`
- [x] T052 Executar `mvn test` e verificar que todos os testes passam sem regressões
- [x] T053 Arrancar a aplicação e executar todos os cenários de `specs/002-estrutura-organizacional/quickstart.md` (secções 2, 3, 4, 5); registar e corrigir todos os erros encontrados

---

## Dependencies & Execution Order

### Dependências de Fase

- **Setup (Phase 1)**: Sem dependências — iniciar imediatamente
- **Foundational (Phase 2)**: Depende de Setup — BLOQUEIA todas as user stories
- **US1 (Phase 3)**: Depende de Foundational
- **US2 (Phase 4)**: Depende de Foundational (independente de US1)
- **US3 (Phase 5)**: Depende de Foundational (independente de US1 e US2)
- **Polish (Phase 6)**: Depende de US1 + US2 + US3

### Dependências dentro de US1

```
T007, T008, T009 [P] → T010 → T011 [P] → T012 → T013 → T014 → T015 [P]
→ T016 → T017 → T018 → T019 → T020 → T021 → T022
```

### Dependências dentro de US2

```
T023, T024, T025 [P] → T026 → T027 [P] → T028 → T029 → T030 → T031 [P]
→ T032 → T033 → T034 → T035 → T036
```

### Dependências dentro de US3

```
T037 [P] → T038 → T039 [P] → T040 → T041 → T042 → T043 [P]
→ T044 → T045 → T046 → T047
```

---

## Parallel Opportunities

### Dentro de US1 (executar em paralelo):
```
Task: T007 — OrganizationalUnitId value object
Task: T008 — OrganizationalUnit domain model
Task: T009 — OrganizationalUnitFilter
(depois de T007-T009 completos)
Task: T011 — OrganizationalUnitEntity JPA
Task: T015 — DTOs (Request, Response, Wrapper)
```

### Dentro de US2 (executar em paralelo):
```
Task: T023 — JobId
Task: T024 — Job domain model
Task: T025 — JobFilter
(depois)
Task: T027 — JobEntity
Task: T031 — DTOs
```

### US2 e US3 podem ser desenvolvidas em paralelo após US1 estar completa (ou mesmo em paralelo com US1 se a equipa tiver capacidade).

---

## Implementation Strategy

### MVP (apenas US1)

1. Completar Phase 1: Setup
2. Completar Phase 2: Foundational
3. Completar Phase 3: US1 (T007–T022)
4. **PARAR E VALIDAR**: testar US1 com quickstart.md secção 2
5. Demo da gestão de unidades orgânicas

### Entrega Incremental

1. Setup + Foundational → base pronta
2. US1 → testar → entregar gestão de unidades orgânicas
3. US2 → testar → entregar gestão de cargos
4. US3 → testar → entregar gestão de funções
5. Polish + Audit → endpoint de auditoria cross-cutting

---

## Notes

- `[P]` = ficheiros diferentes, sem dependências entre si — executar em paralelo
- `[USn]` = mapeia para user story n da spec.md
- O padrão de implementação segue sempre `parametrizacoes/` — consultar os ficheiros existentes antes de criar novos
- Para cada `@CacheEvict`, verificar como a cache está registada no `application.yml` antes de usar o nome
- O IGRP Spring Generator é invocado via o skill `igrp-spring-generator` após cada manifest copiado para `.igrpstudio/estrutura/controllers/`
- `GetEstruturaAuditHistoryQueryHandler` OBRIGATORIAMENTE tem `@Transactional(readOnly=true)` — sem isso o `AuditReader` lança `IllegalStateException: entity manager closed`
- Executar `mvn test` após cada fase para detectar regressões cedo
