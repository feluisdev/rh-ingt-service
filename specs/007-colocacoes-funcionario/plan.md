# Implementation Plan: Colocações de Funcionários

**Branch**: `007-colocacoes-funcionario` | **Date**: 2026-05-01 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `specs/007-colocacoes-funcionario/spec.md`

## Summary

Implementar o sub-módulo de Colocações dentro do BC `colaboradores/`. Regista e gere o histórico de afectações de funcionários a unidades orgânicas (`employee_unit_assignments`), garante que apenas existe uma colocação activa por funcionário em simultâneo, e cria automaticamente uma colocação do tipo `MOBILIDADE` quando uma `LicencaMobilidade` é aprovada.

Stack: Java 23 · Spring Boot 3.5.3 · PostgreSQL 17 · Hibernate Envers · Arquitectura hexagonal idêntica ao padrão `colaboradores/` já implementado.

## Technical Context

**Language/Version**: Java 23  
**Primary Dependencies**: Spring Boot 3.5.3, Spring Data JPA, Hibernate Envers, Lombok, Apache Commons  
**Storage**: PostgreSQL 17 — nova tabela `employee_unit_assignments` (Flyway V28)  
**Testing**: `mvn test`  
**Target Platform**: Linux server (Docker)  
**Project Type**: REST microservice (módulo dentro de RH-Service)  
**Performance Goals**: Consulta colocação actual < 500 ms p95 (SC-002)  
**Constraints**: Unicidade `is_current=true` por funcionário garantida via JPQL UPDATE atómico na mesma transacção  
**Scale/Scope**: Histórico de colocações por funcionário (esperado < 50 registos por funcionário)

## Constitution Check

| Princípio | Status | Notas |
|---|---|---|
| I. Arquitectura Hexagonal | ✅ PASS | Domain puro, infra isolada, handlers com lógica |
| II. CQRS — handlers | ✅ PASS | Commands (Registar, Atualizar, Desativar) + Queries separadas |
| III. IGRP Studio — controllers gerados | ✅ PASS | `ColocacaoController` criado manualmente (padrão `DocumentoController`) |
| IV. Auditoria Envers | ✅ PASS | `ColocacaoEntity extends AuditEntity`, `@Audited` |
| V. Segurança por perfil | ✅ PASS | Sem alterações ao modelo de segurança |
| Chave UUID | ✅ PASS | `ColocacaoId` wraps `ExternalID` |
| Idioma artefactos | ✅ PASS | Todos os artefactos em pt-PT |

## Project Structure

### Documentation (this feature)

```text
specs/007-colocacoes-funcionario/
├── spec.md
├── plan.md              ← este ficheiro
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── colocacoes.md
├── checklists/
│   └── requirements.md
└── tasks.md             ← gerado por /speckit-tasks
```

### Source Code

```text
src/main/java/cv/igrp/RH_Service/colaboradores/
├── domain/
│   ├── valueobject/
│   │   └── ColocacaoId.java
│   ├── models/
│   │   ├── Colocacao.java
│   │   └── TipoAfectacao.java                    (enum)
│   ├── filter/
│   │   └── ColocacaoFilter.java
│   └── repository/
│       └── ColocacaoRepository.java              (port)
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/
│   │   │   └── ColocacaoEntity.java
│   │   ├── repository/
│   │   │   └── ColabsColocacaoEntityRepository.java
│   │   └── adapters/
│   │       └── ColocacaoRepositoryImpl.java
│   └── mappers/
│       └── ColocacaoMapper.java
├── application/
│   ├── commands/
│   │   ├── RegistarColocacaoCommand.java
│   │   ├── RegistarColocacaoCommandHandler.java
│   │   ├── AtualizarColocacaoCommand.java
│   │   ├── AtualizarColocacaoCommandHandler.java
│   │   ├── DesativarColocacaoCommand.java
│   │   ├── DesativarColocacaoCommandHandler.java
│   │   └── AtivarLicencaMobilidadeCommandHandler.java  (MODIFICADO)
│   ├── queries/
│   │   ├── GetColocacaoAtualQuery.java
│   │   ├── GetColocacaoAtualQueryHandler.java
│   │   ├── GetColocacoesByFuncionarioQuery.java
│   │   ├── GetColocacoesByFuncionarioQueryHandler.java
│   │   ├── GetColocacaoByIdQuery.java
│   │   └── GetColocacaoByIdQueryHandler.java
│   └── dto/
│       ├── ColocacaoResponse.java
│       ├── RegistarColocacaoRequest.java
│       ├── AtualizarColocacaoRequest.java
│       └── WrapperListaColocacaoDTO.java
└── interfaces/
    └── rest/
        └── ColocacaoController.java

src/main/resources/db/migration/
└── V28__create_employee_unit_assignments.sql
```

## Implementation Phases

### Phase 1 — Setup (Migração + Value Object + Enum)
- Migração Flyway V28
- `ColocacaoId` value object
- `TipoAfectacao` enum

### Phase 2 — Foundational (Domain + Port)
- `Colocacao` domain model (`criar`, `fechar`, `atualizar`, `desativar`)
- `ColocacaoFilter`
- `ColocacaoRepository` port (com `fecharColocacaoAtual` via JPQL)

### Phase 3 — US1: Registar Colocação
- `ColocacaoEntity` + `ColabsColocacaoEntityRepository` + `ColocacaoMapper` + `ColocacaoRepositoryImpl`
- `RegistarColocacaoCommand` + `RegistarColocacaoCommandHandler`
- `RegistarColocacaoRequest` + `ColocacaoResponse`
- `ColocacaoController` (POST endpoint)

### Phase 4 — US2: Listagem e Consulta
- `GetColocacoesByFuncionarioQuery` + Handler
- `GetColocacaoAtualQuery` + Handler
- `GetColocacaoByIdQuery` + Handler
- `WrapperListaColocacaoDTO`
- `ColocacaoController` (GET endpoints)

### Phase 5 — US3: Correcção de dados
- `AtualizarColocacaoCommand` + Handler
- `AtualizarColocacaoRequest`
- `ColocacaoController` (PUT endpoint)

### Phase 6 — US4: Soft delete
- `DesativarColocacaoCommand` + Handler
- `ColocacaoController` (DELETE endpoint)

### Phase 7 — US5: Integração com Mobilidade
- Modificar `AtivarLicencaMobilidadeCommandHandler` para injectar `ColocacaoRepository` e criar `Colocacao` tipo `MOBILIDADE`

### Phase 8 — Polish: Build e testes manuais
- `mvn -B -DskipTests clean package`
- Smoke tests via curl (12 cenários do quickstart.md)

## Key Design Decisions

1. **`fecharColocacaoAtual`**: Método no port que executa `UPDATE ... SET is_current=false, end_date=:hoje WHERE funcionario_id=:id AND is_current=true` — atómico dentro da mesma transacção que cria a nova colocação.

2. **Validação cross-BC**: Para validar `unit_id` e `job_id`, o `RegistarColocacaoCommandHandler` usa directamente Spring Data JPA repositories das entidades (`OrganizationalUnitEntity`, `CargoEntity`) — abordagem pragmática sem criar domain ports completos para esses BCs.

3. **`@Entity(name="ColabsColocacaoEntity")`**: Obrigatório para evitar conflito de nomes no Hibernate (padrão estabelecido).

4. **Integração mobilidade**: `AtivarLicencaMobilidadeCommandHandler` cria `Colocacao` com `unit_id=null` (mobilidade pode ser para entidade externa). O `funcionarioId` vem de `licenca.getFuncionarioId()`.
