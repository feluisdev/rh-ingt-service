# Implementation Plan: Carreiras e Progressão

**Branch**: `003-carreiras-progressao` | **Date**: 2026-04-30 | **Spec**: [spec.md](spec.md)

## Summary

Implementar o BC `carreiras/` do sistema RH do INGT seguindo o PCFR (Decreto-Lei 4/2024). Três catálogos hierárquicos — Carreira → Categoria → Escalão — com soft delete, Envers, e arquitectura hexagonal idêntica ao módulo `estrutura/`. Próxima migration: V23.

## Technical Context

**Language/Version**: Java 23  
**Primary Dependencies**: Spring Boot 3.5.3, Spring Cloud 2025.0.0, Hibernate 6 + Envers, Flyway, Lombok, IGRP Framework (`cv.igrp.framework:core`)  
**Storage**: PostgreSQL 17 via JPA/Hibernate — tabelas `t_career`, `t_category`, `t_grade`  
**Testing**: JUnit 5 + Spring Boot Test (`mvn test`)  
**Target Platform**: Linux server (Docker)  
**Project Type**: REST microservice (hexagonal + CQRS)  
**Performance Goals**: Respostas < 2 s para listagens hierárquicas com até 500 registos por nível (SC-005)  
**Constraints**: UUIDs como PK; sem DELETE físico; FKs `career_id` e `code` imutáveis em Category; `category_id` e `grade_number` imutáveis em Grade  
**Scale/Scope**: Catálogos de referência (~50–200 registos cada); leitura intensiva, escrita rara

## Constitution Check

| Princípio | Estado | Notas |
|---|---|---|
| I — Hexagonal | ✅ PASS | 4 camadas: domain / application / infrastructure / interfaces |
| II — CQRS | ✅ PASS | Toda a lógica em `@IgrpCommandHandler` / `@IgrpQueryHandler` |
| III — IGRP Studio | ✅ PASS | Controllers gerados via `igrp-spring-generator`; manifests em `.igrpstudio/carreiras/` |
| IV — Envers | ✅ PASS | `@Audited` em `CareerEntity`, `CategoryEntity`, `GradeEntity` |
| V — Segurança por perfil | ✅ PASS | `development` desactivado; `production` via Keycloak |
| UUID como PK | ✅ PASS | `ExternalID` (wrapper UUID) em todas as entidades |

## Project Structure

### Documentação

```
specs/003-carreiras-progressao/
├── plan.md           ← este ficheiro
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── careers.md
│   ├── categories.md
│   └── grades.md
└── tasks.md          ← gerado por /speckit-tasks
```

### Código Fonte

```
src/main/java/cv/igrp/RH_Service/carreiras/
├── domain/
│   ├── models/
│   │   ├── Career.java
│   │   ├── Category.java
│   │   └── Grade.java
│   ├── valueobject/
│   │   ├── CareerId.java
│   │   ├── CategoryId.java
│   │   └── GradeId.java
│   ├── repository/
│   │   ├── CareerRepository.java       (port)
│   │   ├── CategoryRepository.java     (port)
│   │   └── GradeRepository.java        (port)
│   └── filter/
│       ├── CareerFilter.java
│       ├── CategoryFilter.java
│       └── GradeFilter.java
├── application/
│   ├── dto/
│   │   ├── CareerRequest.java
│   │   ├── CareerResponse.java
│   │   ├── CategoryRequest.java
│   │   ├── CategoryResponse.java
│   │   ├── GradeRequest.java
│   │   ├── GradeResponse.java
│   │   ├── WrapperListaCareerDTO.java
│   │   ├── WrapperListaCategoryDTO.java
│   │   ├── WrapperListaGradeDTO.java
│   │   └── AuditHistoryEntryDTO.java   (reutilizar de estrutura se package permitir, ou duplicar)
│   ├── commands/
│   │   ├── CreateCareerCommand.java + CreateCareerCommandHandler.java
│   │   ├── UpdateCareerCommand.java + UpdateCareerCommandHandler.java
│   │   ├── DesativarCareerCommand.java + DesativarCareerCommandHandler.java
│   │   ├── AtivarCareerCommand.java + AtivarCareerCommandHandler.java
│   │   ├── CreateCategoryCommand.java + CreateCategoryCommandHandler.java
│   │   ├── UpdateCategoryCommand.java + UpdateCategoryCommandHandler.java
│   │   ├── DesativarCategoryCommand.java + DesativarCategoryCommandHandler.java
│   │   ├── AtivarCategoryCommand.java + AtivarCategoryCommandHandler.java
│   │   ├── CreateGradeCommand.java + CreateGradeCommandHandler.java
│   │   ├── UpdateGradeCommand.java + UpdateGradeCommandHandler.java
│   │   ├── DesativarGradeCommand.java + DesativarGradeCommandHandler.java
│   │   └── AtivarGradeCommand.java + AtivarGradeCommandHandler.java
│   └── queries/
│       ├── GetCareersQuery.java + GetCareersQueryHandler.java
│       ├── GetCareerByIdQuery.java + GetCareerByIdQueryHandler.java
│       ├── GetCategoriesByCareerIdQuery.java + GetCategoriesByCareerIdQueryHandler.java
│       ├── GetCategoriesQuery.java + GetCategoriesQueryHandler.java
│       ├── GetCategoryByIdQuery.java + GetCategoryByIdQueryHandler.java
│       ├── GetGradesByCategoryIdQuery.java + GetGradesByCategoryIdQueryHandler.java
│       ├── GetGradesQuery.java + GetGradesQueryHandler.java
│       ├── GetGradeByIdQuery.java + GetGradeByIdQueryHandler.java
│       └── GetCarreirasAuditHistoryQuery.java + GetCarreirasAuditHistoryQueryHandler.java
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/
│   │   │   ├── CareerEntity.java        (@Audited, @Table("t_career"))
│   │   │   ├── CategoryEntity.java      (@Audited, @Table("t_category"))
│   │   │   └── GradeEntity.java         (@Audited, @Table("t_grade"))
│   │   ├── repository/
│   │   │   ├── CareerEntityRepository.java   (JpaRepository + JpaSpecificationExecutor)
│   │   │   ├── CategoryEntityRepository.java
│   │   │   └── GradeEntityRepository.java
│   │   └── adapters/
│   │       ├── CareerRepositoryImpl.java
│   │       ├── CategoryRepositoryImpl.java
│   │       └── GradeRepositoryImpl.java
│   └── mappers/
│       ├── CareerMapper.java
│       ├── CategoryMapper.java
│       └── GradeMapper.java
└── interfaces/rest/
    ├── CareerController.java              (IGRP Studio — não editar)
    ├── CategoryController.java            (IGRP Studio — não editar)
    ├── GradeController.java               (IGRP Studio — não editar)
    └── CarreirasAuditHistoryController.java (IGRP Studio — não editar)

src/main/resources/db/migration/
└── V23__create_carreiras_tables.sql
```

## Detalhes de Implementação por Camada

### Domain Models — pontos críticos

**Career.java**
- `criar(code, name, description)` → `CareerId.gerarNovo()`
- `atualizar(code, name, description)` — code mutável, validado no handler
- `desativar()` / `reativar()` — guardam com guard clause

**Category.java**
- `criar(careerId, code, name, description)` — careerId e code imutáveis
- `atualizar(name, description)` — APENAS name e description
- Handler rejeita HTTP 400 se request tentar alterar careerId ou code

**Grade.java**
- `criar(categoryId, gradeNumber, name, salaryIndex)` — categoryId e gradeNumber imutáveis
- `atualizar(name, salaryIndex)` — APENAS name e salaryIndex
- `salaryIndex` como `BigDecimal` (nullable)
- Handler rejeita HTTP 400 se request tentar alterar categoryId ou gradeNumber

### Repository Ports — métodos chave

**CareerRepository**
```
save(Career) / findById(CareerId) / findByCode(String) / findAll(CareerFilter)
existsByCode(String) / existsByCodeAndIdNot(String, CareerId)
existsActiveCategoriesByCareerId(CareerId)   ← para guard de desactivação
```

**CategoryRepository**
```
save(Category) / findById(CategoryId) / findAll(CategoryFilter)
existsByCodeAndCareerId(String, CareerId)
existsByCodeAndCareerIdAndIdNot(String, CareerId, CategoryId)
findByCareerId(CareerId)                     ← sub-recurso
existsActiveGradesByCategoryId(CategoryId)   ← para guard de desactivação
```

**GradeRepository**
```
save(Grade) / findById(GradeId) / findAll(GradeFilter)
existsByGradeNumberAndCategoryId(int, CategoryId)
existsByGradeNumberAndCategoryIdAndIdNot(int, CategoryId, GradeId)
findByCategoryIdOrderByGradeNumber(CategoryId)  ← sub-recurso
isGradeReferencedByActiveAssignment(GradeId)    ← query defensiva em employee_professional_assignments
```

### Validação defensiva em employee_professional_assignments

```java
// GradeEntityRepository.java
@Query(value = """
    SELECT CASE WHEN EXISTS (
        SELECT 1 FROM employee_professional_assignments
        WHERE grade_id = :gradeId AND is_current = true
    ) THEN true ELSE false END
    """, nativeQuery = true)
boolean isReferencedByActiveAssignment(@Param("gradeId") UUID gradeId);
```

Se a tabela não existir, capturar `DataAccessException` no adapter e retornar `false` (permitir desactivação).

### IGRP Studio Manifests (`.igrpstudio/carreiras/`)

Gerar via `igrp-spring-generator`:
- `CareerController`: 7 endpoints (GET list, GET by id, GET categories, POST, PUT, DELETE, PUT activate)
- `CategoryController`: 7 endpoints (GET list, GET by id, GET grades, POST, PUT, DELETE, PUT activate)
- `GradeController`: 6 endpoints (GET list, GET by id, POST, PUT, DELETE, PUT activate)
- `CarreirasAuditHistoryController`: 3 endpoints (GET career history, GET category history, GET grade history)

## Ordem de Implementação

1. **V23 migration** — criar `t_career`, `t_category`, `t_grade`
2. **Domain layer** — Value objects → Domain models → Repository ports → Filters
3. **Infrastructure layer** — Entities → JPA repositories → Mappers → Adapters
4. **Application layer** — DTOs → Commands/Queries → Handlers
5. **IGRP manifests + controllers** — via `igrp-spring-generator`
6. **Testes de integração** — cenários do quickstart.md

## Dependências entre User Stories

```
US1 (Carreiras) → US2 (Categorias) → US3 (Escalões)
```

Cada US é independentemente testável mas US2 requer dados de US1 e US3 requer dados de US2.
