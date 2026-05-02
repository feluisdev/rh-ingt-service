# Implementation Plan: Meu Perfil — Área Reservada do Colaborador

**Feature**: 008-colaborador-me  
**Branch**: `008-colaborador-me`  
**Date**: 2026-05-02  
**Spec**: [spec.md](./spec.md)

---

## Technical Context

| Item | Decision |
|---|---|
| Language | Java 23 |
| Framework | Spring Boot 3.5.3 + Spring Cloud 2025.0.0 |
| Architecture | DDD + Hexagonal (Ports & Adapters) + CQRS |
| Database | PostgreSQL 17 via JPA/Hibernate + Flyway |
| Security | OAuth2/JWT via Keycloak (disabled in dev/staging) |
| Storage | MinIO (documentos download via URL pré-assinado — já implementado) |
| IAM Sync | `IAMUserProfileSyncFilter` — porta do projecto `igrp_platform_process_manager_studio` |
| Employee Resolution | `CurrentEmployeeResolver` — interface em `shared/domain/service/`, impl em `shared/infrastructure/security/` |
| New Tables | `t_iam_user_profile` (V29 migration) — única tabela nova |
| Controller | `MeController` — gerado via `igrp-spring-generator` |
| Commands | Prefixo `SelfService*` — separados dos handlers HR existentes |

---

## Constitution Check

| Princípio | Estado | Notas |
|---|---|---|
| I. Arquitectura Hexagonal | ✅ PASS | `CurrentEmployeeResolver` como interface em `shared/domain/service/` (port); impl em `shared/infrastructure/security/` (adapter); handlers em `application/` |
| II. CQRS | ✅ PASS | Comandos `SelfService*Command` para escritas; Queries `GetMe*Query` para leituras |
| III. IGRP Studio | ✅ PASS | `MeController` gerado por manifest `.igrpstudio/me/` — nunca editado à mão |
| IV. Auditoria Envers | ✅ PASS | `t_iam_user_profile` auditada (`@Audited`); `ApplicationAuditorAware` actualizado para ler `sub` do JWT |
| V. Segurança por Perfil | ✅ PASS | Prod: JWT sub → resolve funcionário; Dev: header `X-Employee-Id` (padrão `X-Institution-Id`) |

---

## Phase 0: Research — Decisões Técnicas

### D1 — IAM User Profile Sync

**Decision**: Portar o `IAMUserProfileSyncFilter` do projecto `igrp_platform_process_manager_studio` com adição do campo `funcionario_id FK→t_funcionario`.  
**Rationale**: Padrão já validado em produção no mesmo stack. Evita reinventar. A única adição é a FK para ligar o utilizador Keycloak ao funcionário RH.  
**Alternatives considered**: (a) `keycloak_sub` em `t_funcionario` — rejeitado (viola SRP, polui o aggregate root); (b) `employee_external_mapping` do Bloco 9 — reservado para integração SAD.

### D2 — CurrentEmployeeResolver

**Decision**: Interface `CurrentEmployeeResolver` em `shared/domain/service/` com implementação `CurrentEmployeeResolverImpl` em `shared/infrastructure/security/`.  
**Rationale**: Interface no domínio = injectável em handlers sem depender de JWT/security; implementação na infra = conhece Spring Security. Fácil de mockar em testes. Segue o padrão Port & Adapter da constituição.  
**Assinatura**:
```
CurrentEmployeeResolver.resolve() → FuncionarioId   (lança 404 se não encontrado)
```

### D3 — Ligação Keycloak → Funcionário

**Decision**: Campo `funcionario_id UUID FK→t_funcionario` em `t_iam_user_profile`, preenchido no primeiro login via lookup por email (`t_funcionario.email = jwt.email`).  
**Rationale**: Email é o atributo de identidade mais estável e já presente nos dois sistemas; não requer configuração manual.  
**Edge case**: Se não existir funcionário com o email do JWT → `funcionario_id = null`; `resolve()` lança HTTP 404 "Funcionário não encontrado para o utilizador autenticado".

### D4 — ApplicationAuditorAware

**Decision**: Actualizar `ApplicationAuditorAware` para usar `sub` do JWT como auditor (padrão do projecto de referência), com fallback para `authentication.getName()` e depois `"system"`.  
**Rationale**: `sub` é o identificador estável do Keycloak; `authentication.getName()` pode variar por configuração.

### D5 — Dev Mode

**Decision**: Em `development`/`staging`, `CurrentEmployeeResolverImpl` lê o header HTTP `X-Employee-Id` (UUID do funcionário) directamente, sem JWT.  
**Rationale**: Mesmo padrão do `X-Institution-Id` já em `SecurityContextHelper`. Permite testar `/me` em dev com `curl -H "X-Employee-Id: <uuid>"` sem Keycloak.  
**Fallback**: Se header ausente em dev, usa primeiro funcionário activo da BD (conveniência).

### D6 — SelfService Commands

**Decision**: Comandos com prefixo `SelfService*` que recebem `FuncionarioId` já resolvido — não reutilizam handlers HR.  
**Rationale**: Separação de responsabilidades clara. Handler HR recebe `funcionarioId` do body/path; handler self-service recebe `FuncionarioId` do `CurrentEmployeeResolver`. Lógica de domínio partilhada via repositórios e modelos de domínio.

---

## Phase 1: Design & Contracts

### Estrutura de Ficheiros

```
src/main/resources/db/migration/
└── V29__create_iam_user_profile.sql

src/main/java/cv/igrp/RH_Service/
├── shared/
│   ├── domain/service/
│   │   └── CurrentEmployeeResolver.java              ← interface (domain port)
│   ├── infrastructure/security/
│   │   ├── IAMUserProfileSyncFilter.java             ← portado do projecto referência
│   │   └── CurrentEmployeeResolverImpl.java          ← impl (infra adapter)
│   ├── infrastructure/persistence/entity/
│   │   └── IAMUserProfileEntity.java
│   └── infrastructure/persistence/repository/
│       └── IAMUserProfileEntityRepository.java
│
└── colaboradores/
    ├── application/
    │   ├── commands/
    │   │   ├── SelfServiceCriarPedidoAusenciaCommand.java
    │   │   ├── SelfServiceCriarPedidoAusenciaCommandHandler.java
    │   │   ├── SelfServiceCancelarPedidoAusenciaCommand.java
    │   │   ├── SelfServiceCancelarPedidoAusenciaCommandHandler.java
    │   │   ├── SelfServiceCriarLicencaMobilidadeCommand.java
    │   │   └── SelfServiceCriarLicencaMobilidadeCommandHandler.java
    │   ├── queries/
    │   │   ├── GetMeProfileQuery.java + Handler
    │   │   ├── GetMeLeaveRequestsQuery.java + Handler
    │   │   ├── GetMeLeaveBalancesQuery.java + Handler
    │   │   ├── GetMeLeaveMobilitiesQuery.java + Handler
    │   │   ├── GetMeDocumentsQuery.java + Handler
    │   │   └── GetMeDocumentDownloadUrlQuery.java + Handler
    │   └── dto/
    │       ├── MeProfileResponse.java
    │       ├── MeLeaveRequestFilter.java
    │       ├── MeLeaveBalanceResponse.java
    │       ├── MeLicencaMobilidadeResponse.java
    │       ├── MeDocumentResponse.java
    │       └── SelfServiceCriarPedidoAusenciaRequest.java
    └── interfaces/rest/
        └── MeController.java                         ← gerado por igrp-spring-generator
```

### Módulos Reutilizados (sem modificação)

| Repositório | Origem | Uso em /me |
|---|---|---|
| `FuncionarioRepository` | colaboradores/domain | Perfil + validação `is_active` |
| `ColocacaoRepository` | colaboradores/domain | Colocação actual no perfil |
| `EnquadramentoRepository` | colaboradores/domain | Cargo/função no perfil — método `findCurrentByFuncionarioId()` |
| `PedidoAusenciaRepository` | colaboradores/domain | GET/POST ausências |
| `SaldoAusenciaRepository` | colaboradores/domain | GET saldos |
| `LicencaMobilidadeRepository` | colaboradores/domain | GET/POST licenças |
| `DocumentoRepository` | colaboradores/domain | GET documentos |
| `SubtipoLicencaMobilidadeRepository` | colaboradores/domain | Verificar `canSelfSubmit` |

### Fluxo de Resolução (todos os handlers /me)

```
Request JWT
  → IAMUserProfileSyncFilter (sync claims → t_iam_user_profile)
  → MeController
  → Handler.handle(query/command)
      → currentEmployeeResolver.resolve()
          [prod] JWT sub → IAMUserProfileEntity.sub → funcionario_id → FuncionarioId
          [dev]  X-Employee-Id header → FuncionarioId directo
      → funcionarioRepo.findById(funcionarioId) → validar is_active (403 se false)
      → lógica de negócio com FuncionarioId garantido
```

### SecurityConfig — Alterações

Registar `IAMUserProfileSyncFilter` antes do `AuthorizationFilter` (mesmo padrão do projecto de referência). Endpoints `/me/**` requerem `ROLE_FUNCIONARIO` em produção.

### ApplicationAuditorAware — Alterações

Actualizar para priorizar `sub` do JWT (padrão do projecto de referência):
1. `sub` claim do JWT (se JwtAuthenticationToken)
2. `authentication.getName()` (fallback)
3. `"system"` (background tasks / dev)
