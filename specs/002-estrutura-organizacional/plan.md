# Implementation Plan: Estrutura Organizacional

**Branch**: `002-estrutura-organizacional` | **Date**: 2026-04-30 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `specs/002-estrutura-organizacional/spec.md`

## Summary

Implementação do módulo `estrutura/` no RH-Service, gerindo três catálogos de referência hierárquicos: Unidades Orgânicas (auto-referencial com regra de bloqueio de desactivação), Cargos e Funções. O módulo segue exactamente o padrão hexagonal já estabelecido em `parametrizacoes/`, com JPA/Envers, Caffeine cache, IGRP Studio para controllers, e Flyway para migrações.

## Technical Context

**Language/Version**: Java 23  
**Primary Dependencies**: Spring Boot 3.5.3, Spring Data JPA, Hibernate Envers, Caffeine Cache, IGRP Framework (`cv.igrp.framework:core`)  
**Storage**: PostgreSQL 17 — 3 novas tabelas: `t_unidade_organica`, `t_cargo`, `t_funcao`  
**Testing**: JUnit 5 via `mvn test`  
**Target Platform**: Linux server (Docker)  
**Project Type**: REST microservice (módulo dentro do RH-Service)  
**Performance Goals**: Listagem de 1 000 registos em < 2 s; operações CRUD em < 30 s  
**Constraints**: Envers obrigatório em todas as entidades; UUID como PK; soft delete via `is_active`  
**Scale/Scope**: Catálogos de dimensão pequena (dezenas a poucas centenas de registos)

## Constitution Check

| Princípio | Status | Evidência |
|-----------|--------|-----------|
| I — Arquitetura Hexagonal | ✅ PASS | Módulo `estrutura/` com `domain/`, `application/`, `infrastructure/`, `interfaces/rest/` — idêntico a `parametrizacoes/` |
| II — CQRS | ✅ PASS | Lógica de negócio exclusivamente em `@IgrpCommandHandler` / `@IgrpQueryHandler` |
| III — IGRP Studio | ✅ PASS | Controllers gerados a partir de manifests em `.igrpstudio/estrutura/` |
| IV — Auditoria Envers | ✅ PASS | `@Audited` em todas as entidades; `AuditHistory` endpoint exposto |
| V — Segurança por Perfil | ✅ PASS | Sem alterações ao mecanismo de segurança; comportamento herdado da configuração partilhada |

**Restrições Técnicas**:
- UUID como PK (via `ExternalID`) — ✅
- PostgreSQL 17 via JPA/Hibernate — ✅
- Sem SQL nativo (apenas JPQL/Spring Data) — ✅ (excepto Flyway DDL)
- Idioma dos artefactos: pt-PT — ✅

Nenhuma violação. Sem necessidade de justificação em Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/002-estrutura-organizacional/
├── plan.md              # Este ficheiro
├── research.md          # Decisões técnicas (Phase 0)
├── data-model.md        # Modelo de dados (Phase 1)
├── quickstart.md        # Cenários de integração (Phase 1)
├── contracts/           # Manifests IGRP (Phase 1)
│   ├── OrganizationalUnitController.json
│   ├── JobController.json
│   ├── FunctionController.json
│   └── EstruturaCatalogAuditController.json
├── checklists/
│   └── requirements.md
└── tasks.md             # Gerado por /speckit-tasks
```

### Source Code

```text
src/main/java/cv/igrp/RH_Service/
└── estrutura/
    ├── application/
    │   ├── commands/
    │   │   ├── CreateOrganizationalUnitCommand.java
    │   │   ├── CreateOrganizationalUnitCommandHandler.java
    │   │   ├── UpdateOrganizationalUnitCommand.java
    │   │   ├── UpdateOrganizationalUnitCommandHandler.java
    │   │   ├── DesativarOrganizationalUnitCommand.java
    │   │   ├── DesativarOrganizationalUnitCommandHandler.java
    │   │   ├── AtivarOrganizationalUnitCommand.java
    │   │   ├── AtivarOrganizationalUnitCommandHandler.java
    │   │   ├── CreateJobCommand.java
    │   │   ├── CreateJobCommandHandler.java
    │   │   ├── UpdateJobCommand.java
    │   │   ├── UpdateJobCommandHandler.java
    │   │   ├── DesativarJobCommand.java
    │   │   ├── DesativarJobCommandHandler.java
    │   │   ├── AtivarJobCommand.java
    │   │   ├── AtivarJobCommandHandler.java
    │   │   ├── CreateFunctionCommand.java
    │   │   ├── CreateFunctionCommandHandler.java
    │   │   ├── UpdateFunctionCommand.java
    │   │   ├── UpdateFunctionCommandHandler.java
    │   │   ├── DesativarFunctionCommand.java
    │   │   ├── DesativarFunctionCommandHandler.java
    │   │   ├── AtivarFunctionCommand.java
    │   │   └── AtivarFunctionCommandHandler.java
    │   ├── queries/
    │   │   ├── GetOrganizationalUnitsQuery.java
    │   │   ├── GetOrganizationalUnitsQueryHandler.java
    │   │   ├── GetOrganizationalUnitByIdQuery.java
    │   │   ├── GetOrganizationalUnitByIdQueryHandler.java
    │   │   ├── GetJobsQuery.java
    │   │   ├── GetJobsQueryHandler.java
    │   │   ├── GetJobByIdQuery.java
    │   │   ├── GetJobByIdQueryHandler.java
    │   │   ├── GetFunctionsQuery.java
    │   │   ├── GetFunctionsQueryHandler.java
    │   │   ├── GetFunctionByIdQuery.java
    │   │   ├── GetFunctionByIdQueryHandler.java
    │   │   ├── GetEstruturaAuditHistoryQuery.java
    │   │   └── GetEstruturaAuditHistoryQueryHandler.java
    │   └── dto/
    │       ├── OrganizationalUnitRequest.java
    │       ├── OrganizationalUnitResponse.java
    │       ├── WrapperListaOrganizationalUnitDTO.java
    │       ├── JobRequest.java
    │       ├── JobResponse.java
    │       ├── WrapperListaJobDTO.java
    │       ├── FunctionRequest.java
    │       ├── FunctionResponse.java
    │       ├── WrapperListaFunctionDTO.java
    │       ├── AuditHistoryEntryDTO.java          # pode reutilizar de parametrizacoes (shared)
    │       └── WrapperListaAuditHistoryDTO.java   # pode reutilizar de parametrizacoes (shared)
    ├── domain/
    │   ├── models/
    │   │   ├── OrganizationalUnit.java
    │   │   ├── Job.java
    │   │   └── Function.java
    │   ├── filter/
    │   │   ├── OrganizationalUnitFilter.java
    │   │   ├── JobFilter.java
    │   │   └── FunctionFilter.java
    │   ├── repository/
    │   │   ├── OrganizationalUnitRepository.java
    │   │   ├── JobRepository.java
    │   │   └── FunctionRepository.java
    │   └── valueobject/
    │       ├── OrganizationalUnitId.java
    │       ├── JobId.java
    │       └── FunctionId.java
    └── infrastructure/
        ├── persistence/
        │   ├── entity/
        │   │   ├── OrganizationalUnitEntity.java
        │   │   ├── JobEntity.java
        │   │   └── FunctionEntity.java
        │   ├── repository/
        │   │   ├── OrganizationalUnitEntityRepository.java
        │   │   ├── JobEntityRepository.java
        │   │   └── FunctionEntityRepository.java
        │   └── adapters/
        │       ├── OrganizationalUnitRepositoryImpl.java
        │       ├── JobRepositoryImpl.java
        │       └── FunctionRepositoryImpl.java
        └── mappers/
            ├── OrganizationalUnitMapper.java
            ├── JobMapper.java
            └── FunctionMapper.java

.igrpstudio/estrutura/
├── module.json
├── controllers/
│   ├── OrganizationalUnitController.json
│   ├── JobController.json
│   ├── FunctionController.json
│   └── EstruturaCatalogAuditController.json
└── dto/  (gerado automaticamente pelo igrp-spring-generator)

src/main/java/cv/igrp/RH_Service/estrutura/interfaces/rest/
├── OrganizationalUnitController.java   (gerado)
├── JobController.java                  (gerado)
├── FunctionController.java             (gerado)
└── EstruturaCatalogAuditController.java (gerado)

src/main/resources/db/migration/
├── V20__create_estrutura_tables.sql
└── V21__seed_unit_types.sql
```

**Structure Decision**: Módulo único `estrutura/` seguindo exactamente o mesmo padrão de `parametrizacoes/`. Sem sub-módulos separados porque os três catálogos partilham o mesmo BC, as mesmas convenções e o mesmo grupo de endpoints REST.

## Fases de Implementação

### Fase 0 — Investigação Técnica
- Padrão de FK auto-referencial em JPA (sem `@ManyToOne`, apenas UUID)
- Regra de deactivação hierárquica (validação na camada application)
- Reuso vs. duplicação dos DTOs de auditoria (`parametrizacoes` vs. `shared`)
- Decisão: nova tabela de auditoria a registar em `GetEstruturaAuditHistoryQueryHandler`

→ Resultado em `research.md`

### Fase 1 — Design e Contratos
- Modelo de dados completo em `data-model.md`
- Manifests IGRP em `contracts/`
- Cenários de integração em `quickstart.md`

### Fase 2 — Implementação (via /speckit-tasks + /speckit-implement)
- Migrações Flyway (DDL + seed)
- Entities, Value Objects, Domain Models
- Repository ports + adapters
- Commands + Queries (handlers)
- IGRP manifests + controller generation
- Testes manuais + correção de bugs

## Constitution Re-check (pós-design)

Após geração de `data-model.md` e contratos:

| Princípio | Status |
|-----------|--------|
| I — Hexagonal | ✅ PASS — camadas respeitadas, sem bypass |
| II — CQRS | ✅ PASS — handlers exclusivos para lógica |
| III — IGRP Studio | ✅ PASS — manifests definidos antes de controllers |
| IV — Envers | ✅ PASS — `@Audited` nas 3 entidades; endpoint de auditoria incluído |
| V — Segurança | ✅ PASS — sem alterações; herdado da configuração partilhada |
