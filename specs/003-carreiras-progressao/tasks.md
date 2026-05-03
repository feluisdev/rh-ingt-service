# Tasks: Carreiras e Progressão

**Input**: Design documents from `specs/003-carreiras-progressao/`
**Branch**: `003-carreiras-progressao`
**Spec**: spec.md | **Plan**: plan.md | **Data Model**: data-model.md

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode correr em paralelo (ficheiros diferentes, sem dependências)
- **[Story]**: User story a que a tarefa pertence (US1, US2, US3)

---

## Phase 1: Setup

**Purpose**: Criar estrutura base do módulo `carreiras/`

- [X] T001 Criar estrutura de directorias do módulo `carreiras/` em `src/main/java/cv/igrp/RH_Service/carreiras/` (domain/models, domain/valueobject, domain/repository, domain/filter, application/dto, application/commands, application/queries, infrastructure/persistence/entity, infrastructure/persistence/repository, infrastructure/persistence/adapters, infrastructure/mappers, interfaces/rest)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Migration Flyway que cria as 3 tabelas — bloqueia todas as user stories

**⚠️ CRÍTICO**: Nenhuma user story pode ser implementada sem esta migração aplicada

- [X] T002 Criar migration Flyway `V23__create_carreiras_tables.sql` em `src/main/resources/db/migration/V23__create_carreiras_tables.sql` com as tabelas `t_career`, `t_category` e `t_grade` conforme data-model.md (incluindo FKs, constraints de unicidade e CHECK grade_number >= 1)

**Checkpoint**: Executar `mvn flyway:migrate` (ou arrancar a aplicação) e verificar que as 3 tabelas foram criadas sem erros.

---

## Phase 3: User Story 1 — Gestão de Carreiras (Priority: P1) 🎯 MVP

**Goal**: CRUD completo de Carreiras com soft delete, unicidade de code, e 6 endpoints REST funcionais

**Independent Test**: Criar, consultar, actualizar e desactivar uma carreira via `GET/POST/PUT/DELETE api/v1/rh/careers` sem necessidade de categorias ou escalões

### Domain Layer (US1)

- [X] T003 [P] [US1] Criar value object `CareerId` em `src/main/java/cv/igrp/RH_Service/carreiras/domain/valueobject/CareerId.java` — seguir padrão de `JobId` (métodos: `gerarNovo()`, `from(UUID)`, `from(String)`, `getValor()`, `getStringValor()`, equals/hashCode)
- [X] T004 [P] [US1] Criar domain model `Career` em `src/main/java/cv/igrp/RH_Service/carreiras/domain/models/Career.java` — campos: `CareerId id`, `String code`, `String name`, `String description`, `boolean active`; métodos: `criar(code, name, description)`, `reconstruir(id, code, name, description, active)`, `atualizar(code, name, description)`, `desativar()`, `reativar()` com guard clauses
- [X] T005 [P] [US1] Criar `CareerFilter` em `src/main/java/cv/igrp/RH_Service/carreiras/domain/filter/CareerFilter.java` — campo `Boolean isActive` nullable
- [X] T006 [P] [US1] Criar port `CareerRepository` em `src/main/java/cv/igrp/RH_Service/carreiras/domain/repository/CareerRepository.java` — métodos: `save`, `findById(CareerId)`, `findAll(CareerFilter)`, `existsByCode(String)`, `existsByCodeAndIdNot(String, CareerId)`, `existsActiveCategoriesByCareerId(CareerId)`

### Infrastructure Layer (US1)

- [X] T007 [P] [US1] Criar `CareerEntity` em `src/main/java/cv/igrp/RH_Service/carreiras/infrastructure/persistence/entity/CareerEntity.java` — `@Audited`, `@Entity`, `@Table(name="t_career")`, extends `AuditEntity`; campos: `UUID id`, `String code`, `String name`, `String description`, `Boolean isActive`
- [X] T008 [P] [US1] Criar `CareerEntityRepository` em `src/main/java/cv/igrp/RH_Service/carreiras/infrastructure/persistence/repository/CareerEntityRepository.java` — extends `JpaRepository<CareerEntity, UUID>` e `JpaSpecificationExecutor<CareerEntity>`; métodos derivados: `existsByCode(String)`, `existsByCodeAndIdNot(String, UUID)`
- [X] T009 [P] [US1] Criar `CareerMapper` em `src/main/java/cv/igrp/RH_Service/carreiras/infrastructure/mappers/CareerMapper.java` — métodos `toDomain(CareerEntity)` e `toEntity(Career)` seguindo padrão de `JobMapper`
- [X] T010 [US1] Implementar `CareerRepositoryImpl` em `src/main/java/cv/igrp/RH_Service/carreiras/infrastructure/persistence/adapters/CareerRepositoryImpl.java` — implementa `CareerRepository`; o método `existsActiveCategoriesByCareerId` será completado na US2 (retornar `false` por agora como stub)

### Application Layer (US1)

- [X] T011 [P] [US1] Criar DTOs `CareerRequest`, `CareerResponse` e `WrapperListaCareerDTO` em `src/main/java/cv/igrp/RH_Service/carreiras/application/dto/` — `CareerResponse` inclui todos os campos + `createdDate`, `createdBy`; `WrapperListaCareerDTO` contém `List<CareerResponse> data` e `long totalElements`
- [X] T012 [P] [US1] Criar `CreateCareerCommand` + `CreateCareerCommandHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/commands/` — handler verifica `existsByCode` e lança HTTP 409 se duplicado; chama `Career.criar()` e `careerRepository.save()`; retorna `ResponseEntity<CareerResponse>` com status 201
- [X] T013 [P] [US1] Criar `UpdateCareerCommand` + `UpdateCareerCommandHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/commands/` — handler verifica existência (404), verifica `existsByCodeAndIdNot` (409), chama `career.atualizar()` e `save()`
- [X] T014 [P] [US1] Criar `DesativarCareerCommand` + `DesativarCareerCommandHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/commands/` — handler verifica existência (404), chama `existsActiveCategoriesByCareerId` (409 se verdadeiro), chama `career.desativar()` e `save()`
- [X] T015 [P] [US1] Criar `AtivarCareerCommand` + `AtivarCareerCommandHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/commands/` — handler verifica existência (404), chama `career.reativar()` e `save()`
- [X] T016 [P] [US1] Criar `GetCareersQuery` + `GetCareersQueryHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/queries/` — handler aplica `CareerFilter` e devolve `WrapperListaCareerDTO`
- [X] T017 [P] [US1] Criar `GetCareerByIdQuery` + `GetCareerByIdQueryHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/queries/` — lança HTTP 404 se não encontrado

### Controller (US1)

- [X] T018 [US1] Gerar manifest IGRP e `CareerController` com 6 operações (GET /careers, GET /careers/{id}, POST /careers, PUT /careers/{id}, DELETE /careers/{id}, PUT /careers/{id}/activate) usando o skill `igrp-spring-generator` com `addController` para o módulo `carreiras`; escrever manifest em `.igrpstudio/carreiras/CareerController.json` e controller em `src/main/java/cv/igrp/RH_Service/carreiras/interfaces/rest/CareerController.java`

**Checkpoint US1**: Arrancar a aplicação e validar os 6 endpoints via Swagger ou curl (cenário 1 do quickstart.md). `mvn test` deve passar sem regressões.

---

## Phase 4: User Story 2 — Gestão de Categorias (Priority: P2)

**Goal**: CRUD completo de Categorias com validação de carreira pai, unicidade `(career_id, code)`, imutabilidade de `career_id` e `code`, sub-recurso `GET /careers/{id}/categories`, e guard de desactivação contra escalões activos

**Independent Test**: Criar carreira de suporte, depois criar/consultar/actualizar/desactivar categorias via `api/v1/rh/categories` e `api/v1/rh/careers/{id}/categories`

### Domain Layer (US2)

- [X] T019 [P] [US2] Criar value object `CategoryId` em `src/main/java/cv/igrp/RH_Service/carreiras/domain/valueobject/CategoryId.java` — mesmo padrão de `CareerId`
- [X] T020 [P] [US2] Criar domain model `Category` em `src/main/java/cv/igrp/RH_Service/carreiras/domain/models/Category.java` — campos: `CategoryId id`, `CareerId careerId` (imutável — sem setter), `String code` (imutável), `String name`, `String description`, `boolean active`; métodos: `criar(careerId, code, name, description)`, `reconstruir(...)`, `atualizar(name, description)` (NÃO actualiza careerId nem code), `desativar()`, `reativar()`
- [X] T021 [P] [US2] Criar `CategoryFilter` em `src/main/java/cv/igrp/RH_Service/carreiras/domain/filter/CategoryFilter.java` — campos: `CareerId careerId` (nullable), `Boolean isActive` (nullable)
- [X] T022 [P] [US2] Criar port `CategoryRepository` em `src/main/java/cv/igrp/RH_Service/carreiras/domain/repository/CategoryRepository.java` — métodos: `save`, `findById(CategoryId)`, `findAll(CategoryFilter)`, `findByCareerId(CareerId)`, `existsByCodeAndCareerId(String, CareerId)`, `existsByCodeAndCareerIdAndIdNot(String, CareerId, CategoryId)`, `existsActiveGradesByCategoryId(CategoryId)`

### Infrastructure Layer (US2)

- [X] T023 [P] [US2] Criar `CategoryEntity` em `src/main/java/cv/igrp/RH_Service/carreiras/infrastructure/persistence/entity/CategoryEntity.java` — `@Audited`, `@Table(name="t_category")`; campos: `UUID id`, `UUID careerId`, `String code`, `String name`, `String description`, `Boolean isActive`
- [X] T024 [P] [US2] Criar `CategoryEntityRepository` em `src/main/java/cv/igrp/RH_Service/carreiras/infrastructure/persistence/repository/CategoryEntityRepository.java` — métodos derivados: `existsByCodeAndCareerId(String, UUID)`, `existsByCodeAndCareerIdAndIdNot(String, UUID, UUID)`, `findByCareerId(UUID)`, `existsByCareerIdAndIsActiveTrue(UUID)` (usado por CareerRepositoryImpl para guard de desactivação de carreira)
- [X] T025 [P] [US2] Criar `CategoryMapper` em `src/main/java/cv/igrp/RH_Service/carreiras/infrastructure/mappers/CategoryMapper.java`
- [X] T026 [US2] Implementar `CategoryRepositoryImpl` em `src/main/java/cv/igrp/RH_Service/carreiras/infrastructure/persistence/adapters/CategoryRepositoryImpl.java`
- [X] T027 [US2] Completar `CareerRepositoryImpl.existsActiveCategoriesByCareerId()` em `src/main/java/cv/igrp/RH_Service/carreiras/infrastructure/persistence/adapters/CareerRepositoryImpl.java` — usar `categoryEntityRepository.existsByCareerIdAndIsActiveTrue(id.getValor())`

### Application Layer (US2)

- [X] T028 [P] [US2] Criar DTOs `CategoryRequest`, `CategoryResponse` e `WrapperListaCategoryDTO` em `src/main/java/cv/igrp/RH_Service/carreiras/application/dto/` — `CategoryResponse` inclui `careerId`, `careerName` (para display)
- [X] T029 [P] [US2] Criar `CreateCategoryCommand` + `CreateCategoryCommandHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/commands/` — handler valida: carreira existe e está activa (404/409), par `(careerId, code)` único (409); chama `Category.criar()` e `save()`; retorna 201
- [X] T030 [P] [US2] Criar `UpdateCategoryCommand` + `UpdateCategoryCommandHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/commands/` — handler valida existência (404); rejeita HTTP 400 se request tenta alterar `careerId` ou `code`; chama `category.atualizar(name, description)` e `save()`
- [X] T031 [P] [US2] Criar `DesativarCategoryCommand` + `DesativarCategoryCommandHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/commands/` — handler valida existência (404), chama `existsActiveGradesByCategoryId` (409 se verdadeiro), chama `desativar()` e `save()`
- [X] T032 [P] [US2] Criar `AtivarCategoryCommand` + `AtivarCategoryCommandHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/commands/`
- [X] T033 [P] [US2] Criar `GetCategoriesQuery` + `GetCategoriesQueryHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/queries/` — aplica `CategoryFilter` (com `careerId` e `isActive`)
- [X] T034 [P] [US2] Criar `GetCategoryByIdQuery` + `GetCategoryByIdQueryHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/queries/`
- [X] T035 [P] [US2] Criar `GetCategoriesByCareerIdQuery` + `GetCategoriesByCareerIdQueryHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/queries/` — verifica existência da carreira (404), devolve lista de `CategoryResponse` filtrada por `careerId`

### Controllers (US2)

- [X] T036 [US2] Gerar manifest IGRP e `CategoryController` com 6 operações (GET /categories, GET /categories/{id}, POST /categories, PUT /categories/{id}, DELETE /categories/{id}, PUT /categories/{id}/activate) usando `igrp-spring-generator`; manifest em `.igrpstudio/carreiras/CategoryController.json`; controller em `src/main/java/cv/igrp/RH_Service/carreiras/interfaces/rest/CategoryController.java`
- [X] T037 [US2] Actualizar manifest do `CareerController` para adicionar operação `GET /careers/{id}/categories` (dispatcha `GetCategoriesByCareerIdQuery`) e regenerar `CareerController` em `src/main/java/cv/igrp/RH_Service/carreiras/interfaces/rest/CareerController.java`

**Checkpoint US2**: Validar cenário 2 do quickstart.md — criar carreira de suporte e testar CRUD de categorias, sub-recurso `/careers/{id}/categories`, e bloqueio de desactivação de carreira com categorias activas.

---

## Phase 5: User Story 3 — Gestão de Escalões (Priority: P3)

**Goal**: CRUD completo de Escalões com `grade_number` ≥ 1, `salary_index` opcional, imutabilidade de `category_id` e `grade_number`, sub-recurso `GET /categories/{id}/grades` ordenado, e guard de desactivação contra `employee_professional_assignments`

**Independent Test**: Criar carreira + categoria de suporte, depois criar/consultar/actualizar/desactivar escalões via `api/v1/rh/grades` e `api/v1/rh/categories/{id}/grades`

### Domain Layer (US3)

- [X] T038 [P] [US3] Criar value object `GradeId` em `src/main/java/cv/igrp/RH_Service/carreiras/domain/valueobject/GradeId.java`
- [X] T039 [P] [US3] Criar domain model `Grade` em `src/main/java/cv/igrp/RH_Service/carreiras/domain/models/Grade.java` — campos: `GradeId id`, `CategoryId categoryId` (imutável), `int gradeNumber` (imutável, ≥ 1 validado no handler), `String name`, `BigDecimal salaryIndex` (nullable), `boolean active`; métodos: `criar(categoryId, gradeNumber, name, salaryIndex)`, `reconstruir(...)`, `atualizar(name, salaryIndex)` (NÃO actualiza categoryId nem gradeNumber), `desativar()`, `reativar()`
- [X] T040 [P] [US3] Criar `GradeFilter` em `src/main/java/cv/igrp/RH_Service/carreiras/domain/filter/GradeFilter.java` — campos: `CategoryId categoryId` (nullable), `Boolean isActive` (nullable)
- [X] T041 [P] [US3] Criar port `GradeRepository` em `src/main/java/cv/igrp/RH_Service/carreiras/domain/repository/GradeRepository.java` — métodos: `save`, `findById(GradeId)`, `findAll(GradeFilter)`, `findByCategoryIdOrderByGradeNumber(CategoryId)`, `existsByGradeNumberAndCategoryId(int, CategoryId)`, `existsByGradeNumberAndCategoryIdAndIdNot(int, CategoryId, GradeId)`, `isReferencedByActiveAssignment(GradeId)`

### Infrastructure Layer (US3)

- [X] T042 [P] [US3] Criar `GradeEntity` em `src/main/java/cv/igrp/RH_Service/carreiras/infrastructure/persistence/entity/GradeEntity.java` — `@Audited`, `@Table(name="t_grade")`; campos: `UUID id`, `UUID categoryId`, `int gradeNumber`, `String name`, `BigDecimal salaryIndex`, `Boolean isActive`
- [X] T043 [P] [US3] Criar `GradeEntityRepository` em `src/main/java/cv/igrp/RH_Service/carreiras/infrastructure/persistence/repository/GradeEntityRepository.java` — métodos derivados: `existsByGradeNumberAndCategoryId(int, UUID)`, `existsByGradeNumberAndCategoryIdAndIdNot(int, UUID, UUID)`, `findByCategoryIdOrderByGradeNumber(UUID)`, `existsByCategoryIdAndIsActiveTrue(UUID)` (para guard de desactivação de categoria); método `@Query` nativa para `isReferencedByActiveAssignment(UUID gradeId)` — query defensiva em `employee_professional_assignments` com tratamento de `DataAccessException` (retornar `false` se tabela não existir)
- [X] T044 [P] [US3] Criar `GradeMapper` em `src/main/java/cv/igrp/RH_Service/carreiras/infrastructure/mappers/GradeMapper.java`
- [X] T045 [US3] Implementar `GradeRepositoryImpl` em `src/main/java/cv/igrp/RH_Service/carreiras/infrastructure/persistence/adapters/GradeRepositoryImpl.java` — incluindo implementação defensiva de `isReferencedByActiveAssignment` (captura `DataAccessException` e retorna `false`)
- [X] T046 [US3] Completar `CategoryRepositoryImpl.existsActiveGradesByCategoryId()` em `src/main/java/cv/igrp/RH_Service/carreiras/infrastructure/persistence/adapters/CategoryRepositoryImpl.java` — usar `gradeEntityRepository.existsByCategoryIdAndIsActiveTrue(id.getValor())`

### Application Layer (US3)

- [X] T047 [P] [US3] Criar DTOs `GradeRequest`, `GradeResponse` e `WrapperListaGradeDTO` em `src/main/java/cv/igrp/RH_Service/carreiras/application/dto/` — `GradeResponse` inclui `categoryId`, `categoryName`, `gradeNumber`, `salaryIndex`
- [X] T048 [P] [US3] Criar `CreateGradeCommand` + `CreateGradeCommandHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/commands/` — handler valida: `gradeNumber >= 1` (400), categoria existe e está activa (404/409), par `(categoryId, gradeNumber)` único (409); chama `Grade.criar()` e `save()`; retorna 201
- [X] T049 [P] [US3] Criar `UpdateGradeCommand` + `UpdateGradeCommandHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/commands/` — handler valida existência (404); rejeita HTTP 400 se request tenta alterar `categoryId` ou `gradeNumber`; chama `grade.atualizar(name, salaryIndex)` e `save()`
- [X] T050 [P] [US3] Criar `DesativarGradeCommand` + `DesativarGradeCommandHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/commands/` — handler valida existência (404), chama `isReferencedByActiveAssignment` (409 se verdadeiro), chama `desativar()` e `save()`
- [X] T051 [P] [US3] Criar `AtivarGradeCommand` + `AtivarGradeCommandHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/commands/`
- [X] T052 [P] [US3] Criar `GetGradesQuery` + `GetGradesQueryHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/queries/` — aplica `GradeFilter`
- [X] T053 [P] [US3] Criar `GetGradeByIdQuery` + `GetGradeByIdQueryHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/queries/`
- [X] T054 [P] [US3] Criar `GetGradesByCategoryIdQuery` + `GetGradesByCategoryIdQueryHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/queries/` — verifica existência da categoria (404), devolve lista de `GradeResponse` ordenada por `gradeNumber`

### Controllers (US3)

- [X] T055 [US3] Gerar manifest IGRP e `GradeController` com 6 operações (GET /grades, GET /grades/{id}, POST /grades, PUT /grades/{id}, DELETE /grades/{id}, PUT /grades/{id}/activate) usando `igrp-spring-generator`; manifest em `.igrpstudio/carreiras/GradeController.json`; controller em `src/main/java/cv/igrp/RH_Service/carreiras/interfaces/rest/GradeController.java`
- [X] T056 [US3] Actualizar manifest do `CategoryController` para adicionar operação `GET /categories/{id}/grades` (dispatcha `GetGradesByCategoryIdQuery`) e regenerar `CategoryController` em `src/main/java/cv/igrp/RH_Service/carreiras/interfaces/rest/CategoryController.java`

**Checkpoint US3**: Validar cenário 3 do quickstart.md — criar carreira + categoria de suporte e testar CRUD de escalões, sub-recurso `/categories/{id}/grades`, gradeNumber inválido (400), e bloqueio de desactivação de categoria com escalões activos.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Auditoria transversal e validação final

- [X] T057 Criar `AuditHistoryEntryDTO` e `WrapperListaAuditHistoryDTO` em `src/main/java/cv/igrp/RH_Service/carreiras/application/dto/` — campos: `revisionId` (Long), `revisionDate` (LocalDateTime), `revisionType` (String: INSERT/UPDATE/DELETE), `entity` (Object); se `estrutura.application.dto.AuditHistoryEntryDTO` for acessível via import directo, reutilizar em vez de duplicar
- [X] T058 Criar `GetCarreirasAuditHistoryQuery` + `GetCarreirasAuditHistoryQueryHandler` em `src/main/java/cv/igrp/RH_Service/carreiras/application/queries/` — handler usa `AuditReaderFactory` do Envers para devolver revisões de `CareerEntity`, `CategoryEntity` ou `GradeEntity` conforme `entityType` e `entityId` fornecidos
- [X] T059 Gerar manifest IGRP e `CarreirasAuditHistoryController` com 3 operações (GET /careers/{id}/history, GET /categories/{id}/history, GET /grades/{id}/history) usando `igrp-spring-generator`; manifest em `.igrpstudio/carreiras/CarreirasAuditHistoryController.json`; controller em `src/main/java/cv/igrp/RH_Service/carreiras/interfaces/rest/CarreirasAuditHistoryController.java`
- [X] T060 Executar `mvn test` e corrigir todas as regressões — confirmar que os 102 testes existentes continuam a passar com o novo módulo `carreiras/` adicionado
- [X] T061 Executar testes de integração manuais de todos os cenários do `specs/003-carreiras-progressao/quickstart.md` (cenários 1–4) contra a aplicação em execução em `http://localhost:8091`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: Sem dependências — pode começar imediatamente
- **Phase 2 (Foundational)**: Depende de Phase 1 — **bloqueia todas as user stories**
- **Phase 3 (US1)**: Depende de Phase 2; sem dependências de US2/US3
- **Phase 4 (US2)**: Depende de US1 completo (usa `CareerRepository` para validar `careerId`)
- **Phase 5 (US3)**: Depende de US2 completo (usa `CategoryRepository` para validar `categoryId`)
- **Phase 6 (Polish)**: Depende de US1 + US2 + US3 completos

### User Story Dependencies

- **US1 (Carreiras)**: Autónoma após Phase 2
- **US2 (Categorias)**: Requer US1 completo — `CreateCategoryCommandHandler` valida que a carreira existe
- **US3 (Escalões)**: Requer US2 completo — `CreateGradeCommandHandler` valida que a categoria existe

### Within Each User Story

- Domain layer (value objects + models + ports + filters) → Infrastructure layer → Application layer → Controller
- Dentro de cada camada, os ficheiros marcados [P] podem ser criados em paralelo

### Parallel Opportunities por User Story

```
# US1 — tarefas paralelas no domain layer:
T003 CareerId.java
T004 Career.java
T005 CareerFilter.java
T006 CareerRepository.java

# US1 — tarefas paralelas no infrastructure layer (após T006):
T007 CareerEntity.java
T008 CareerEntityRepository.java
T009 CareerMapper.java

# US1 — tarefas paralelas no application layer (após T010):
T011 DTOs
T012 CreateCareerCommand+Handler
T013 UpdateCareerCommand+Handler
T014 DesativarCareerCommand+Handler
T015 AtivarCareerCommand+Handler
T016 GetCareersQuery+Handler
T017 GetCareerByIdQuery+Handler

# Mesmo padrão para US2 (T019-T035) e US3 (T038-T054)
```

---

## Implementation Strategy

### MVP (US1 apenas — Carreiras funcionais)

1. Completar Phase 1 (Setup)
2. Completar Phase 2 (Migration V23)
3. Completar Phase 3 (US1 — Carreiras)
4. **PARAR E VALIDAR**: testar 6 endpoints de Carreira
5. Avançar para US2 se aprovado

### Entrega Incremental

1. Setup + V23 migration → base pronta
2. US1 (Carreiras) → MVP validado
3. US2 (Categorias) → hierarquia carreira→categoria funcional
4. US3 (Escalões) → hierarquia completa carreira→categoria→escalão
5. Polish → auditoria + testes finais

---

## Notes

- [P] = ficheiros diferentes, sem dependências entre si na mesma fase
- Seguir padrão byte-a-byte do módulo `estrutura/` para domain models, mappers e adapters
- `salary_index` deve ser `BigDecimal` (não `double`) para precisão monetária
- A query defensiva em `employee_professional_assignments` (T043/T045) deve capturar `DataAccessException` e retornar `false` — a tabela não existe até o módulo `colaboradores/` ser implementado
- `career_id` e `code` são imutáveis em Category; `category_id` e `grade_number` são imutáveis em Grade — validar no handler com HTTP 400 se diferem dos valores originais
- Manifests IGRP escritos em `.igrpstudio/carreiras/` — usar `igrp-spring-generator` com `addController`
