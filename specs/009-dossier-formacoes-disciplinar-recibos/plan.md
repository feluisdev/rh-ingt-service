# Implementation Plan: Dossier — Formações, Processos Disciplinares e Recibos

**Feature**: 009-dossier-formacoes-disciplinar-recibos
**Branch**: `009-dossier-formacoes-disciplinar-recibos`
**Date**: 2026-05-02
**Spec**: [spec.md](./spec.md)

---

## Technical Context

| Item | Decision |
|---|---|
| Language | Java 23 |
| Framework | Spring Boot 3.5.3 + Spring Cloud 2025.0.0 |
| Architecture | DDD + Hexagonal (Ports & Adapters) + CQRS |
| Database | PostgreSQL 17 via JPA/Hibernate + Flyway (migrações defensivas) |
| Security | OAuth2/JWT via Keycloak (desactivado em dev/staging) |
| Storage | MinIO — reutilizado via `DocumentoService.getPresignedLink()` para download de recibos |
| New Tables | `t_training` (V30), `t_disciplinary_process` (V31), `t_payroll_slip` (V32) |
| Controllers | `FormacaoController`, `ProcessoDisciplinarController`, `ReciboController` — gerados via `igrp-spring-generator` |
| Me endpoints | `GET /me/payroll-slips` e `GET /me/payroll-slips/{id}/download` adicionados ao `MeController` existente |
| Employee Resolution | `CurrentEmployeeResolver` já implementado (#008) — reutilizado sem alteração |
| Role restriction | Processos disciplinares: CRUD aberto nesta iteração (restrição de perfil a implementar posteriormente) |

---

## Constitution Check

| Princípio | Estado | Notas |
|---|---|---|
| I. Arquitectura Hexagonal | ✅ PASS | Entities JPA em `infrastructure/persistence/entity/`; domain models em `domain/model/`; handlers em `application/`; controllers gerados em `interfaces/rest/` |
| II. CQRS | ✅ PASS | Commands para criar/actualizar/remover; Queries para listar/detalhe |
| III. IGRP Studio | ✅ PASS | Todos os controllers gerados por manifests `.igrpstudio/` via `igrp-spring-generator` |
| IV. Auditoria Envers | ✅ PASS | Todas as entidades com `@Audited`; tabelas de auditoria criadas nas migrações |
| V. Segurança por Perfil | ✅ PASS | Sem alterações à segurança; `CurrentEmployeeResolver` já lida com dev/prod |

---

## Phase 0: Research — Decisões Técnicas

Ver [research.md](./research.md) para detalhe completo.

| Decisão | Resumo |
|---------|--------|
| D1 | CRUD hexagonal idêntico a `dependentes/` e `qualificacoes/` |
| D2 | Três tabelas novas com FK `funcionario_id`; migrações V30–V32 defensivas |
| D3 | `typeOptionKey` de formação como String referenciando `option_entity(ccode=TRAINING_TYPE)`; seed incluído em V30 |
| D4 | `documentId` como UUID sem FK declarada (referência fraca ao `t_documento`) |
| D5 | Processos disciplinares: CRUD sem restrição de perfil nesta iteração |
| D6 | `/me/payroll-slips` adicionado ao manifest do `MeController` existente |
| D7 | Unicidade recibo: constraint DB + verificação aplicacional → HTTP 409 |

---

## Phase 1: Design & Contracts

### Estrutura de Ficheiros

```
src/main/resources/db/migration/
├── V30__create_training.sql
├── V31__create_disciplinary_process.sql
└── V32__create_payroll_slip.sql

src/main/java/cv/igrp/RH_Service/colaboradores/
├── domain/
│   ├── model/
│   │   ├── Formacao.java
│   │   ├── ProcessoDisciplinar.java
│   │   └── ReciboVencimento.java
│   ├── repository/
│   │   ├── FormacaoRepository.java
│   │   ├── ProcessoDisciplinarRepository.java
│   │   └── ReciboVencimentoRepository.java
│   ├── valueobject/
│   │   ├── FormacaoId.java
│   │   ├── ProcessoDisciplinarId.java
│   │   └── ReciboVencimentoId.java
│   └── filter/
│       ├── FormacaoFilter.java
│       └── ReciboFilter.java
│
├── application/
│   ├── commands/
│   │   ├── CriarFormacaoCommand.java + Handler
│   │   ├── AtualizarFormacaoCommand.java + Handler
│   │   ├── RemoverFormacaoCommand.java + Handler
│   │   ├── CriarProcessoDisciplinarCommand.java + Handler
│   │   ├── AtualizarProcessoDisciplinarCommand.java + Handler
│   │   ├── CriarReciboVencimentoCommand.java + Handler
│   │   └── (sem update/delete em recibos nesta iteração)
│   ├── queries/
│   │   ├── GetFormacaoQuery.java + Handler
│   │   ├── GetFormacoesByFuncionarioQuery.java + Handler
│   │   ├── GetProcessoDisciplinarQuery.java + Handler
│   │   ├── GetProcessosDisciplinaresByFuncionarioQuery.java + Handler
│   │   ├── GetReciboVencimentoQuery.java + Handler
│   │   ├── GetRecibosByFuncionarioQuery.java + Handler
│   │   ├── GetMePayrollSlipsQuery.java + Handler        ← área reservada
│   │   └── GetMePayrollSlipDownloadQuery.java + Handler ← área reservada
│   └── dto/
│       ├── FormacaoDTO.java
│       ├── WrapperListaFormacaoDTO.java
│       ├── CriarFormacaoRequest.java
│       ├── AtualizarFormacaoRequest.java
│       ├── ProcessoDisciplinarDTO.java
│       ├── WrapperListaProcessoDisciplinarDTO.java
│       ├── CriarProcessoDisciplinarRequest.java
│       ├── AtualizarProcessoDisciplinarRequest.java
│       ├── ReciboVencimentoDTO.java
│       ├── WrapperListaReciboDTO.java
│       └── CriarReciboRequest.java
│
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/
│   │   │   ├── FormacaoEntity.java
│   │   │   ├── ProcessoDisciplinarEntity.java
│   │   │   └── ReciboVencimentoEntity.java
│   │   └── repository/
│   │       ├── ColabsFormacaoEntityRepository.java
│   │       ├── ColabsProcessoDisciplinarEntityRepository.java
│   │       └── ColabsReciboVencimentoEntityRepository.java
│   └── mappers/
│       ├── FormacaoMapper.java
│       ├── ProcessoDisciplinarMapper.java
│       └── ReciboVencimentoMapper.java
│
└── interfaces/rest/
    ├── FormacaoController.java       ← gerado por igrp-spring-generator
    ├── ProcessoDisciplinarController.java ← gerado por igrp-spring-generator
    └── ReciboController.java         ← gerado por igrp-spring-generator
    (MeController.java actualizado via manifest — adicionar /me/payroll-slips)

.igrpstudio/colaboradores/
├── FormacaoController.json
├── ProcessoDisciplinarController.json
└── ReciboController.json
(MeController.json actualizado com novos endpoints)
```

### Módulos Reutilizados (sem modificação)

| Componente | Uso |
|---|---|
| `FuncionarioRepository` | Validação existência e `is_active` |
| `DocumentoRepository` | Validação de `documentId` antes de associar |
| `DocumentoService.getPresignedLink()` | Download de recibo PDF via MinIO |
| `CurrentEmployeeResolver` | Resolução de identidade em `/me/payroll-slips` |
| `IgrpResponseStatusException` | Erros padronizados (404, 400, 409) |

### Convenções de Bean Name

| Controller | Bean name |
|---|---|
| FormacaoController | `colabsFormacaoController` |
| ProcessoDisciplinarController | `colabsProcessoDisciplinarController` |
| ReciboController | `colabsReciboController` |
| Handlers | `colabsCriar/Atualizar/Remover/Get*Handler` |

### Fluxo de Criação (padrão para os três módulos)

```
POST /funcionarios/{funcionarioId}/formacoes
  → FormacaoController.criar(funcionarioId, request)
  → dispatch(CriarFormacaoCommand)
  → CriarFormacaoCommandHandler.handle(command)
      → FuncionarioRepository.findById(funcionarioId) → validar existência + is_active
      → [se documentId presente] DocumentoRepository.findById(documentId) → validar existência
      → Formacao.criar(...)  ← domain factory
      → FormacaoRepository.save(formacao)
      → return ResponseEntity.created(...)
```

### Fluxo /me/payroll-slips

```
GET /me/payroll-slips
  → MeController.getMePayrollSlips(query)
  → GetMePayrollSlipsQueryHandler.handle(query)
      → currentEmployeeResolver.resolve() → FuncionarioId
      → funcionarioRepository.findById(...) → validar is_active (403)
      → reciboRepository.findAllByFuncionarioId(funcionarioId, filter)
      → map → return 200

GET /me/payroll-slips/{id}/download
  → GetMePayrollSlipDownloadQueryHandler.handle(query)
      → currentEmployeeResolver.resolve() → FuncionarioId
      → reciboRepository.findById(ReciboVencimentoId) → 404 se não existe
      → validar recibo.getFuncionarioId().equals(funcionarioId) → 404 se não coincide
      → documentoService.getPresignedLink(recibo.getDocumentId().toString())
      → return { "downloadUrl": url }
```
