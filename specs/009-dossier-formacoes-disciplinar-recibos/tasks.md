# Tasks: Dossier — Formações, Processos Disciplinares e Recibos

**Feature**: 009-dossier-formacoes-disciplinar-recibos
**Branch**: `009-dossier-formacoes-disciplinar-recibos`
**Input**: specs/009-dossier-formacoes-disciplinar-recibos/
**Total tasks**: 44

---

## Phase 1: Setup — Migrações Flyway

**Purpose**: Criar as três tabelas novas de forma defensiva antes de qualquer código de domínio.

- [X] T001 Criar migração `src/main/resources/db/migration/V30__create_training.sql` com `CREATE TABLE IF NOT EXISTS t_training` (id UUID PK, funcionario_id FK→t_funcionario, name, institution, type_option_key, start_date, end_date, duration_hours, document_id, audit columns), tabela Envers `audit_schema.t_training_aud`, índices defensivos (`IF NOT EXISTS`), e seed `INSERT INTO option_entity (id, ccode, ckey, cvalue, locale, ...) ... ON CONFLICT DO NOTHING` para valores PRESENCIAL, ELEARNING, SEMINARIO, CONGRESSO
- [X] T002 Criar migração `src/main/resources/db/migration/V31__create_disciplinary_process.sql` com `CREATE TABLE IF NOT EXISTS t_disciplinary_process` (id UUID PK, funcionario_id FK→t_funcionario, process_number, start_date, end_date, penalty, penalty_start_date, penalty_end_date, official_bulletin, notes TEXT, document_id UUID, audit columns), tabela Envers `audit_schema.t_disciplinary_process_aud`, índices defensivos
- [X] T003 Criar migração `src/main/resources/db/migration/V32__create_payroll_slip.sql` com `CREATE TABLE IF NOT EXISTS t_payroll_slip` (id UUID PK, funcionario_id FK→t_funcionario, period_month SMALLINT, period_year INTEGER, issue_date DATE, gross_salary NUMERIC(12,2), net_salary NUMERIC(12,2), document_id UUID NOT NULL, audit columns), constraint `UNIQUE (funcionario_id, period_month, period_year)`, CHECK constraints (period_month 1-12, gross_salary>0, net_salary>0, net_salary<=gross_salary), tabela Envers, índices defensivos

---

## Phase 2: Foundational — Value Objects e Filtros Partilhados

**Purpose**: Value objects de identidade e filtros que todos os handlers vão usar.

**⚠️ CRÍTICO**: Completar antes de iniciar as user stories.

- [X] T004 [P] Criar `src/main/java/cv/igrp/RH_Service/colaboradores/domain/valueobject/FormacaoId.java`, `ProcessoDisciplinarId.java` e `ReciboVencimentoId.java` — seguir o padrão de `DocumentoId.java` existente (ExternalID wrapper, métodos `gerarNovo()`, `from(UUID)`, `from(String)`, `getValor()`, `getStringValor()`, equals+hashCode)
- [X] T005 [P] Criar `src/main/java/cv/igrp/RH_Service/colaboradores/domain/filter/FormacaoFilter.java` (campo `Integer year`) e `ReciboFilter.java` (campos `Integer periodYear`, `Integer periodMonth`) — seguir padrão de `DocumentoFilter.java` existente

**Checkpoint**: Value objects e filtros prontos — iniciar user stories.

---

## Phase 3: User Story 1 — Formações Profissionais (Priority: P1) 🎯 MVP

**Goal**: CRUD completo de formações profissionais de um funcionário com filtro por ano.

**Independent Test**: `POST /funcionarios/{id}/formacoes` → `GET /funcionarios/{id}/formacoes` → `PUT` → `DELETE`. Verificar cenários C1–C5 do quickstart.md.

### Infraestrutura JPA

- [X] T006 [P] [US1] Criar `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/entity/FormacaoEntity.java` — `@Entity @Table(name="t_training") @Audited`, estende `AuditEntity`, campos: id (UUID @Id), funcionarioId (UUID), name (String), institution (String), typeOptionKey (String), startDate (LocalDate), endDate (LocalDate), durationHours (Integer), documentId (UUID)
- [X] T007 [P] [US1] Criar `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/repository/ColabsFormacaoEntityRepository.java` — `JpaRepository<FormacaoEntity, UUID>`, métodos: `findAllByFuncionarioId(UUID funcionarioId)`, `findAllByFuncionarioIdAndStartDateYear(UUID funcionarioId, int year)` (usando `@Query` JPQL com `year(f.startDate)`)

### Domínio

- [X] T008 [US1] Criar `src/main/java/cv/igrp/RH_Service/colaboradores/domain/model/Formacao.java` — domain model com campos tipados (FormacaoId, FuncionarioId), factory `Formacao.criar(...)` e `Formacao.atualizar(...)`, sem dependências de infraestrutura
- [X] T009 [US1] Criar `src/main/java/cv/igrp/RH_Service/colaboradores/domain/repository/FormacaoRepository.java` — interface port: `save(Formacao)`, `findById(FormacaoId)`, `findAllByFuncionarioId(FuncionarioId, FormacaoFilter)`, `deleteById(FormacaoId)`
- [X] T010 [US1] Criar `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/ColabsFormacaoRepositoryAdapter.java` — implementa `FormacaoRepository`, injeta `ColabsFormacaoEntityRepository` e `FormacaoMapper`, traduz domain↔entity

### Application

- [X] T011 [P] [US1] Criar DTOs em `src/main/java/cv/igrp/RH_Service/colaboradores/application/dto/`: `FormacaoDTO.java` (todos os campos), `WrapperListaFormacaoDTO.java` (content + totalElements), `CriarFormacaoRequest.java` (@NotBlank name, outros opcionais), `AtualizarFormacaoRequest.java` (todos opcionais)
- [X] T012 [P] [US1] Criar `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/mappers/FormacaoMapper.java` — métodos `toDTO(Formacao)`, `toDomain(FormacaoEntity)`, `toEntity(Formacao)`
- [X] T013 [P] [US1] Criar `CriarFormacaoCommand.java` e `CriarFormacaoCommandHandler.java` em `colaboradores/application/commands/` — handler: resolve FuncionarioId do path, valida funcionário activo, [se documentId presente] valida documento existente, cria e salva Formacao, retorna 201 com id; bean `@Component("colabsCriarFormacaoCommandHandler")`
- [X] T014 [P] [US1] Criar `AtualizarFormacaoCommand.java` e `AtualizarFormacaoCommandHandler.java` em `colaboradores/application/commands/` — handler: busca formação por id e funcionarioId, valida que funcionário está activo (403 se inactivo), actualiza campos não nulos, salva, retorna 200; bean `@Component("colabsAtualizarFormacaoCommandHandler")`
- [X] T015 [P] [US1] Criar `RemoverFormacaoCommand.java` e `RemoverFormacaoCommandHandler.java` em `colaboradores/application/commands/` — handler: valida existência, remove, retorna 200; bean `@Component("colabsRemoverFormacaoCommandHandler")`
- [X] T016 [P] [US1] Criar `GetFormacaoQuery.java` e `GetFormacaoQueryHandler.java` em `colaboradores/application/queries/` — handler: busca por FormacaoId + valida pertença ao funcionarioId, retorna DTO ou 404; bean `@Component("colabsGetFormacaoQueryHandler")`
- [X] T017 [US1] Criar `GetFormacoesByFuncionarioQuery.java` e `GetFormacoesByFuncionarioQueryHandler.java` em `colaboradores/application/queries/` — handler: valida funcionário, aplica FormacaoFilter (year), retorna WrapperListaFormacaoDTO; bean `@Component("colabsGetFormacoesByFuncionarioQueryHandler")`

### Controller

- [X] T018 [US1] Gerar `FormacaoController` via `igrp-spring-generator`: criar manifest `.igrpstudio/colaboradores/FormacaoController.json` (5 endpoints: GET list, GET by id, POST, PUT, DELETE sob `api/v1/rh/funcionarios/{funcionarioId}/formacoes`) com bean name `colabsFormacaoController`, gerar `src/main/java/cv/igrp/RH_Service/colaboradores/interfaces/rest/FormacaoController.java`

**Checkpoint**: US1 completa — testar C1–C5 do quickstart.md.

---

## Phase 4: User Story 2 — Processos Disciplinares (Priority: P2)

**Goal**: CRUD de processos disciplinares (sem restrição de perfil nesta iteração).

**Independent Test**: `POST /funcionarios/{id}/processos-disciplinares` → `GET` → `PUT`. Verificar cenários C6–C8 do quickstart.md.

### Infraestrutura JPA

- [X] T019 [P] [US2] Criar `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/entity/ProcessoDisciplinarEntity.java` — `@Entity @Table(name="t_disciplinary_process") @Audited`, campos: id (UUID), funcionarioId (UUID), processNumber (String), startDate (LocalDate), endDate (LocalDate), penalty (String), penaltyStartDate (LocalDate), penaltyEndDate (LocalDate), officialBulletin (String), notes (String), documentId (UUID)
- [X] T020 [P] [US2] Criar `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/repository/ColabsProcessoDisciplinarEntityRepository.java` — `JpaRepository<ProcessoDisciplinarEntity, UUID>`, método `findAllByFuncionarioId(UUID)`

### Domínio

- [X] T021 [US2] Criar `src/main/java/cv/igrp/RH_Service/colaboradores/domain/model/ProcessoDisciplinar.java` — domain model com ProcessoDisciplinarId, FuncionarioId, factory `criar(...)` e `atualizar(...)`
- [X] T022 [US2] Criar `src/main/java/cv/igrp/RH_Service/colaboradores/domain/repository/ProcessoDisciplinarRepository.java` (port) + `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/ColabsProcessoDisciplinarRepositoryAdapter.java` (impl)

### Application

- [X] T023 [P] [US2] Criar DTOs em `colaboradores/application/dto/`: `ProcessoDisciplinarDTO.java`, `WrapperListaProcessoDisciplinarDTO.java`, `CriarProcessoDisciplinarRequest.java` (@NotNull startDate), `AtualizarProcessoDisciplinarRequest.java`
- [X] T024 [P] [US2] Criar `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/mappers/ProcessoDisciplinarMapper.java`
- [X] T025 [P] [US2] Criar `CriarProcessoDisciplinarCommand.java` e `CriarProcessoDisciplinarCommandHandler.java` em `colaboradores/application/commands/` — valida funcionário, cria processo, retorna 201; bean `colabsCriarProcessoDisciplinarCommandHandler`
- [X] T026 [P] [US2] Criar `AtualizarProcessoDisciplinarCommand.java` e `AtualizarProcessoDisciplinarCommandHandler.java` em `colaboradores/application/commands/` — busca por id e funcionarioId, valida que funcionário está activo (403 se inactivo), actualiza campos não nulos, retorna 200; bean `colabsAtualizarProcessoDisciplinarCommandHandler`
- [X] T027 [P] [US2] Criar `GetProcessoDisciplinarQuery.java` e `GetProcessoDisciplinarQueryHandler.java` em `colaboradores/application/queries/` — busca por id, valida pertença, retorna DTO ou 404; bean `colabsGetProcessoDisciplinarQueryHandler`
- [X] T028 [US2] Criar `GetProcessosDisciplinaresByFuncionarioQuery.java` e `GetProcessosDisciplinaresByFuncionarioQueryHandler.java` em `colaboradores/application/queries/` — lista por funcionarioId, retorna wrapper; bean `colabsGetProcessosDisciplinaresByFuncionarioQueryHandler`

### Controller

- [X] T029 [US2] Gerar `ProcessoDisciplinarController` via `igrp-spring-generator`: manifest `.igrpstudio/colaboradores/ProcessoDisciplinarController.json` (4 endpoints: GET list, GET by id, POST, PUT sob `api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares`) com bean `colabsProcessoDisciplinarController`, gerar controller em `colaboradores/interfaces/rest/ProcessoDisciplinarController.java`

**Checkpoint**: US2 completa — testar C6–C8 do quickstart.md.

---

## Phase 5: User Story 3 — Recibos de Vencimento (Priority: P3)

**Goal**: CRUD de recibos (HR) + self-service `/me/payroll-slips` com download. Desbloqueia a área reservada do funcionário para recibos.

**Independent Test**: `POST /funcionarios/{id}/recibos` → `GET /funcionarios/{id}/recibos` → `GET /me/payroll-slips` → `GET /me/payroll-slips/{id}/download`. Verificar cenários C9–C14 do quickstart.md.

### Infraestrutura JPA

- [X] T030 [P] [US3] Criar `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/entity/ReciboVencimentoEntity.java` — `@Entity @Table(name="t_payroll_slip") @Audited`, campos: id (UUID), funcionarioId (UUID), periodMonth (short), periodYear (int), issueDate (LocalDate), grossSalary (BigDecimal), netSalary (BigDecimal), documentId (UUID)
- [X] T031 [P] [US3] Criar `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/repository/ColabsReciboVencimentoEntityRepository.java` — `JpaRepository<ReciboVencimentoEntity, UUID>`, métodos: `findAllByFuncionarioId(UUID)`, `existsByFuncionarioIdAndPeriodMonthAndPeriodYear(UUID, short, int)`, query com filtros opcionais year/month

### Domínio

- [X] T032 [US3] Criar `src/main/java/cv/igrp/RH_Service/colaboradores/domain/model/ReciboVencimento.java` — domain model com ReciboVencimentoId, FuncionarioId, BigDecimal para salários, factory `criar(...)`
- [X] T033 [US3] Criar `src/main/java/cv/igrp/RH_Service/colaboradores/domain/repository/ReciboVencimentoRepository.java` (port) + `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/ColabsReciboVencimentoRepositoryAdapter.java` (impl com `findAllByFuncionarioId`, `existsByPeriod`, `findById`)

### Application

- [X] T034 [P] [US3] Criar DTOs em `colaboradores/application/dto/`: `ReciboVencimentoDTO.java`, `WrapperListaReciboDTO.java`, `CriarReciboRequest.java` (todos os campos obrigatórios com validações @NotNull, @Min(1)/@Max(12) para month, @Positive para salários)
- [X] T035 [P] [US3] Criar `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/mappers/ReciboVencimentoMapper.java`
- [X] T036 [US3] Criar `CriarReciboVencimentoCommand.java` e `CriarReciboVencimentoCommandHandler.java` em `colaboradores/application/commands/` — handler: valida funcionário, verifica unicidade (409 se já existe par mês/ano), valida net≤gross, valida documentId existente, cria e salva, retorna 201; bean `colabsCriarReciboVencimentoCommandHandler`
- [X] T037 [P] [US3] Criar `GetReciboVencimentoQuery.java` e `GetReciboVencimentoQueryHandler.java` em `colaboradores/application/queries/` — GET by id directo (sem validação de funcionário); bean `colabsGetReciboVencimentoQueryHandler`
- [X] T038 [US3] Criar `GetRecibosByFuncionarioQuery.java` e `GetRecibosByFuncionarioQueryHandler.java` em `colaboradores/application/queries/` — lista por funcionarioId com ReciboFilter (periodYear, periodMonth), retorna WrapperListaReciboDTO; bean `colabsGetRecibosByFuncionarioQueryHandler`
- [X] T039 [US3] Gerar `ReciboController` via `igrp-spring-generator`: manifest `.igrpstudio/colaboradores/ReciboController.json` (3 endpoints: GET list sob `funcionarios/{id}/recibos`, POST sob `funcionarios/{id}/recibos`, GET by id sob `recibos/{id}`) com bean `colabsReciboController`, gerar `colaboradores/interfaces/rest/ReciboController.java`

### Self-service /me/payroll-slips

- [X] T040 [US3] Criar `GetMePayrollSlipsQuery.java` e `GetMePayrollSlipsQueryHandler.java` em `colaboradores/application/queries/` — handler: `currentEmployeeResolver.resolve()` → FuncionarioId, valida is_active (403), lista recibos com ReciboFilter, retorna WrapperListaReciboDTO; bean `colabsGetMePayrollSlipsQueryHandler`
- [X] T041 [US3] Criar `GetMePayrollSlipDownloadQuery.java` e `GetMePayrollSlipDownloadQueryHandler.java` em `colaboradores/application/queries/` — handler: resolve funcionário, busca recibo por ReciboVencimentoId, valida ownership (recibo.getFuncionarioId().equals(funcionarioId) → 404 se não coincide), chama `documentoService.getPresignedLink(recibo.getDocumentId().toString())`, retorna `{ "downloadUrl": url }`; bean `colabsGetMePayrollSlipDownloadQueryHandler`
- [X] T042 [US3] Actualizar manifest `.igrpstudio/me/MeController.json` para adicionar 2 novos endpoints (`GET /me/payroll-slips` com query params periodYear/periodMonth e `GET /me/payroll-slips/{id}/download`), regenerar `colaboradores/interfaces/rest/MeController.java` via `igrp-spring-generator`

**Checkpoint**: US3 completa — testar C9–C14 do quickstart.md.

---

## Phase 6: Polish & Validação

- [X] T043 Compilar com `JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-23.0.2.7-hotspot" mvn -B -DskipTests clean compile` — corrigir quaisquer erros de compilação
- [X] T044 Marcar todas as tasks como [X] em tasks.md e criar commit `feat(colaboradores): implement dossier módulos — formações, processos disciplinares e recibos (#009)`

---

## Dependências e Ordem de Execução

### Dependências entre Fases

- **Phase 1** (Migrações): sem dependências — início imediato
- **Phase 2** (Value objects + filtros): sem dependências — pode correr em paralelo com Phase 1
- **Phase 3** (US1 Formações): depende de Phase 1 + Phase 2
- **Phase 4** (US2 Processos): depende de Phase 1 + Phase 2; independente de US1
- **Phase 5** (US3 Recibos): depende de Phase 1 + Phase 2; independente de US1/US2
- **Phase 6** (Polish): depende de US1 + US2 + US3

### Dependências dentro de cada US

1. Entity JPA [P] + JPA Repository [P] → podem correr em paralelo
2. Domain Model → depende de ValueObjects (Phase 2)
3. Domain Repository Port → depende de Domain Model
4. Repository Adapter → depende de Port + JPA Repository
5. DTOs [P] + Mapper [P] → podem correr em paralelo após Entity
6. Commands/Queries [P] → podem correr em paralelo após Repository Adapter + DTOs + Mapper
7. Controller → depende de todos os Handlers

### Oportunidades de Paralelismo

```
# Phase 1 — Migrações em paralelo:
T001 (V30 training) || T002 (V31 disciplinary) || T003 (V32 payroll)

# Phase 2 — em paralelo entre si e com Phase 1:
T004 (value objects) || T005 (filtros)

# Dentro de US1 (após Phase 2):
T006 (FormacaoEntity) || T007 (JPA repo)
→ T008 (domain model)
→ T009 (port) → T010 (adapter)
T011 (DTOs) || T012 (mapper)
T013 (criar cmd) || T014 (atualizar cmd) || T015 (remover cmd) || T016 (get by id query)
→ T017 (get list query)
→ T018 (controller)
```

---

## Estratégia de Implementação

### MVP (apenas US1 — Formações)

1. Phase 1: T001 (apenas V30)
2. Phase 2: T004 + T005
3. Phase 3: T006–T018
4. Validar C1–C5 do quickstart.md

### Entrega Incremental

1. Phase 1 + Phase 2 → Foundation pronta
2. US1 (Formações) → MVP validado
3. US2 (Processos Disciplinares) → independente
4. US3 (Recibos + /me/payroll-slips) → desbloqueia área reservada

---

## Notas

- `[P]` = ficheiros diferentes, sem dependência de tasks incompletas → executar em paralelo
- Todas as migrações com `IF NOT EXISTS` em todos os DDL statements
- Bean names com prefixo `colabs` (ex: `colabsCriarFormacaoCommandHandler`)
- Pattern de repositório: JPA repo (`ColabsXEntityRepository`) + Adapter (`ColabsXRepositoryAdapter`) que implementa o port de domínio (`XRepository`)
- `typeOptionKey` de formação: validar contra `option_entity` onde `ccode='TRAINING_TYPE'` OU aceitar qualquer string não vazia (seed garante os valores padrão)
- Recibos: verificação de unicidade aplicacional (query antes de persistir) → 409 CONFLICT; constraint DB é rede de segurança
