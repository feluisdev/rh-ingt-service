# Tasks: Módulo Colaboradores — Funcionário e Sub-domínios

**Input**: Design documents from `specs/004-colaboradores-funcionario/`
**Branch**: `004-colaboradores-funcionario`

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode correr em paralelo (ficheiros diferentes, sem dependências)
- **[Story]**: User story a que a tarefa pertence

---

## Phase 1: Setup

**Purpose**: Criar a infraestrutura de base de dados para o BC colaboradores

- [X] T001 Criar migração Flyway V24 **defensiva** (`CREATE SEQUENCE IF NOT EXISTS`, `CREATE TABLE IF NOT EXISTS`) com sequence `seq_numero_funcionario` e tabelas `t_funcionario`, `employee_professional_assignments`, `t_contrato`, `t_dependente`, `t_qualificacao`; incluir índices `idx_funcionario_is_active`, `idx_funcionario_situacao`, `idx_funcionario_nif`, `idx_funcionario_bi` para garantir SC-002 em `src/main/resources/db/migration/V24__create_colaboradores_tables.sql`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Value objects de identidade — pré-requisito de todos os agregados

**⚠️ CRÍTICO**: Nenhuma user story pode começar sem estes value objects

- [X] T002 [P] Criar `FuncionarioId` value object (padrão ExternalID) em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/valueobject/FuncionarioId.java`
- [X] T003 [P] Criar `EnquadramentoId` value object em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/valueobject/EnquadramentoId.java`
- [X] T004 [P] Criar `ContratoId` value object em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/valueobject/ContratoId.java`
- [X] T005 [P] Criar `DependenteId` value object em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/valueobject/DependenteId.java`
- [X] T006 [P] Criar `QualificacaoId` value object em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/valueobject/QualificacaoId.java`

**Checkpoint**: Value objects prontos — implementação das user stories pode começar

---

## Phase 3: User Story 1 — Gestão do Registo de Funcionário (Priority: P1) 🎯 MVP

**Goal**: CRUD completo de Funcionário com geração automática de `numero_funcionario`, filtros e auditoria

**Independent Test**: POST cria funcionário com F000001, GET lista com filtros, PUT actualiza dados e `is_active` deriva de `situacao_profissional`

### Implementação US1

- [X] T007 [US1] Criar domain model `Funcionario` com métodos `criar()`, `atualizar()` (inclui derivação automática de `is_active` a partir de `situacao_profissional`) em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/models/Funcionario.java`
- [X] T008 [US1] Criar `FuncionarioFilter` com campos `nome`, `nif`, `situacaoProfissional`, `unidadeOrganicaId` (UUID), `careerId` (UUID), `isActive`, `page`, `size` em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/filter/FuncionarioFilter.java`
- [X] T009 [US1] Criar port `FuncionarioRepository` com métodos `save`, `findById`, `findAll(FuncionarioFilter)`, `existsByNif`, `existsByNifAndIdNot`, `existsByBiNumero`, `existsByBiNumeroAndIdNot` em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/repository/FuncionarioRepository.java`
- [X] T010 [US1] Criar `FuncionarioEntity` com `@Audited @Entity @Table("t_funcionario")` e todos os campos do data-model em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/entity/FuncionarioEntity.java`
- [X] T011 [US1] Criar `FuncionarioEntityRepository` com métodos `existsByNif`, `existsByNifAndIdNot`, `existsByBiNumero`, `existsByBiNumeroAndIdNot` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/repository/FuncionarioEntityRepository.java`
- [X] T012 [US1] Criar `FuncionarioMapper` (toDomain / toEntity) em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/mappers/FuncionarioMapper.java`
- [X] T013 [US1] Criar `FuncionarioRepositoryImpl` implementando `FuncionarioRepository` com `@Repository`, injectar `FuncionarioEntityRepository` e `JdbcTemplate` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/adapters/FuncionarioRepositoryImpl.java`
- [X] T014 [P] [US1] Criar `FuncionarioRequest` com validações Bean Validation em `src/main/java/cv/igrp/RH_Service/colaboradores/application/dto/FuncionarioRequest.java`
- [X] T015 [P] [US1] Criar `FuncionarioResponse` e `WrapperListaFuncionarioDTO` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/dto/`
- [X] T016 [US1] Criar `CreateFuncionarioCommand` + `CreateFuncionarioCommandHandler` — gerar `numero_funcionario` via `JdbcTemplate.queryForObject("SELECT nextval('seq_numero_funcionario')", Long.class)` formatado como `F%06d`; validar unicidade NIF e BI; retornar 201 com id e numeroFuncionario em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/`
- [X] T017 [US1] Criar `UpdateFuncionarioCommand` + `UpdateFuncionarioCommandHandler` — validar unicidade NIF/BI excluindo o próprio; `is_active` derivado automaticamente de `situacao_profissional`; `numero_funcionario` imutável (ignorado se enviado); validar que `data_saida` só é aceite quando `situacao_profissional` ≠ ATIVO (retornar 400 caso contrário) em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/`
- [X] T018 [US1] Criar `GetFuncionariosQuery` + `GetFuncionariosQueryHandler` com filtros por nome (ILIKE `%nome%`), nif, situacaoProfissional, unidadeOrganicaId, isActive (default true), paginação; filtro `careerId` requer subquery/EXISTS em `employee_professional_assignments WHERE is_current=true AND career_id=?` — implementar via Specification JPA com subquery em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/`
- [X] T019 [US1] Criar `GetFuncionarioByIdQuery` + `GetFuncionarioByIdQueryHandler` retornando 404 se não encontrado em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/`
- [X] T020 [US1] Gerar `FuncionarioController` via `igrp-spring-generator` com endpoints: `POST /api/v1/rh/funcionarios`, `GET /api/v1/rh/funcionarios`, `GET /api/v1/rh/funcionarios/{id}`, `PUT /api/v1/rh/funcionarios/{id}` em `src/main/java/cv/igrp/RH_Service/colaboradores/interfaces/rest/FuncionarioController.java`

**Checkpoint**: Funcionário CRUD completo — testar cenários C1-S1 a C1-S9 do quickstart.md

---

## Phase 4: User Story 2 — Enquadramento Profissional na Grelha PCFR (Priority: P2)

**Goal**: Criar enquadramentos com encerramento atómico do anterior; histórico completo; sub-recursos

**Independent Test**: POST cria enquadramento (is_current=true); segundo POST fecha o anterior (data_fim, is_current=false); GET /funcionarios/{id}/enquadramento retorna actual; GET .../historico retorna todos

### Implementação US2

- [X] T021 [US2] Criar domain model `EnquadramentoProfissional` com campos `funcionarioId`, `careerId`, `categoryId`, `gradeId`, `cargoId`, `unidadeOrganicaId`, `dataInicio`, `dataFim`, `isCurrent` e método `criar()`, `encerrar(dataFim)` em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/models/EnquadramentoProfissional.java`
- [X] T022 [US2] Criar `EnquadramentoFilter` com campos `funcionarioId` (UUID), `isCurrent` em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/filter/EnquadramentoFilter.java`
- [X] T023 [US2] Criar port `EnquadramentoRepository` com métodos `save`, `findById`, `findCurrentByFuncionarioId`, `findAllByFuncionarioIdOrderByDataInicioDesc` em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/repository/EnquadramentoRepository.java`
- [X] T024 [US2] Criar `EnquadramentoEntity` com `@Audited @Entity @Table("employee_professional_assignments")` e todos os campos FK (UUID simples, sem @ManyToOne) em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/entity/EnquadramentoEntity.java`
- [X] T025 [US2] Criar `EnquadramentoEntityRepository` com métodos `findByFuncionarioIdAndIsCurrentTrue`, `findByFuncionarioIdOrderByDataInicioDesc` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/repository/EnquadramentoEntityRepository.java`
- [X] T026 [US2] Criar `EnquadramentoMapper` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/mappers/EnquadramentoMapper.java`
- [X] T027 [US2] Criar `EnquadramentoRepositoryImpl` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/adapters/EnquadramentoRepositoryImpl.java`
- [X] T028 [P] [US2] Criar `EnquadramentoRequest`, `EnquadramentoResponse`, `WrapperListaEnquadramentoDTO` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/dto/`
- [X] T029 [US2] Criar `CreateEnquadramentoCommand` + `CreateEnquadramentoCommandHandler` — validar funcionário existe; validar carreira, categoria, escalão, cargo e unidade orgânica activos individualmente (sem validação cruzada cargo↔unidade — t_cargo é catálogo global); validar data_inicio > data_inicio do enquadramento actual (se existir); numa transacção: encerrar enquadramento actual (data_fim = nova_data_inicio - 1 dia, isCurrent=false) e criar novo (isCurrent=true); retornar 201 em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/`
- [X] T030 [US2] Criar `GetEnquadramentoAtualQuery` + `GetEnquadramentoAtualQueryHandler` — retornar enquadramento com `is_current=true`; 404 se nenhum em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/`
- [X] T031 [US2] Criar `GetEnquadramentosHistoricoQuery` + `GetEnquadramentosHistoricoQueryHandler` — retornar todos os enquadramentos do funcionário ordenados por data_inicio desc em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/`
- [X] T032 [US2] Criar `GetEnquadramentoByIdQuery` + `GetEnquadramentoByIdQueryHandler` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/`
- [X] T033 [US2] Gerar `EnquadramentoController` via `igrp-spring-generator` com endpoints próprios: `POST /api/v1/rh/enquadramentos`, `GET /api/v1/rh/enquadramentos/{id}` em `src/main/java/cv/igrp/RH_Service/colaboradores/interfaces/rest/EnquadramentoController.java`
- [X] T034 [US2] Adicionar sub-recursos ao `FuncionarioController` (sem duplicação): `GET /api/v1/rh/funcionarios/{id}/enquadramento` e `GET /api/v1/rh/funcionarios/{id}/enquadramentos/historico` em `src/main/java/cv/igrp/RH_Service/colaboradores/interfaces/rest/FuncionarioController.java`

**Checkpoint**: Enquadramento CRUD completo — testar cenários C2-S1 a C2-S7 do quickstart.md

---

## Phase 5: User Story 3 — Gestão de Contratos (Priority: P3)

**Goal**: CRUD de contratos com guarda: apenas 1 activo por funcionário; 409 ao criar segundo activo; 409 ao desactivar único activo

**Independent Test**: POST cria contrato; segundo POST com activo existente retorna 409; DELETE do único activo retorna 409

### Implementação US3

- [X] T035 [US3] Criar domain model `Contrato` com campos `funcionarioId`, `tipoContrato`, `dataInicio`, `dataFim`, `numeroContrato`, `isActive` e métodos `criar()`, `atualizar()`, `desativar()`, `ativar()` em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/models/Contrato.java`
- [X] T036 [US3] Criar `ContratoFilter` com campo `funcionarioId` (UUID) em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/filter/ContratoFilter.java`
- [X] T037 [US3] Criar port `ContratoRepository` com métodos `save`, `findById`, `findAllByFuncionarioIdOrderByDataInicioDesc`, `existsActiveByFuncionarioId`, `existsByNumeroContrato`, `existsByNumeroContratoAndIdNot`, `countActiveByFuncionarioId` em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/repository/ContratoRepository.java`
- [X] T038 [US3] Criar `ContratoEntity` com `@Audited @Entity @Table("t_contrato")` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/entity/ContratoEntity.java`
- [X] T039 [US3] Criar `ContratoEntityRepository` com métodos `existsByFuncionarioIdAndIsActiveTrue`, `countByFuncionarioIdAndIsActiveTrue`, `existsByNumeroContrato`, `existsByNumeroContratoAndIdNot`, `findByFuncionarioIdOrderByDataInicioDesc` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/repository/ContratoEntityRepository.java`
- [X] T040 [US3] Criar `ContratoMapper` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/mappers/ContratoMapper.java`
- [X] T041 [US3] Criar `ContratoRepositoryImpl` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/adapters/ContratoRepositoryImpl.java`
- [X] T042 [P] [US3] Criar `ContratoRequest`, `ContratoResponse`, `WrapperListaContratoDTO` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/dto/`
- [X] T043 [US3] Criar `CreateContratoCommand` + `CreateContratoCommandHandler` — validar funcionário existe; 409 se já existe contrato activo; 409 se `numeroContrato` duplicado; retornar 201 em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/`
- [X] T044 [US3] Criar `UpdateContratoCommand` + `UpdateContratoCommandHandler` — `funcionarioId` imutável; validar `numeroContrato` único excluindo próprio em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/`
- [X] T045 [US3] Criar `DesativarContratoCommand` + `DesativarContratoCommandHandler` — 409 se é o único contrato activo do funcionário (sempre, independentemente da situação profissional) em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/`
- [X] T046 [US3] Criar `AtivarContratoCommand` + `AtivarContratoCommandHandler` — 409 se já existe outro contrato activo para o mesmo funcionário em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/`
- [X] T047 [US3] Criar `GetContratosByFuncionarioQuery` + `GetContratosByFuncionarioQueryHandler` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/`
- [X] T048 [US3] Criar `GetContratoByIdQuery` + `GetContratoByIdQueryHandler` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/`
- [X] T049 [US3] Gerar `ContratoController` via `igrp-spring-generator` com endpoints próprios: `POST /api/v1/rh/contratos`, `GET /api/v1/rh/contratos/{id}`, `PUT /api/v1/rh/contratos/{id}`, `DELETE /api/v1/rh/contratos/{id}`, `PUT /api/v1/rh/contratos/{id}/activate` em `src/main/java/cv/igrp/RH_Service/colaboradores/interfaces/rest/ContratoController.java`
- [X] T050 [US3] Adicionar sub-recurso ao `FuncionarioController` (sem duplicação): `GET /api/v1/rh/funcionarios/{id}/contratos` em `src/main/java/cv/igrp/RH_Service/colaboradores/interfaces/rest/FuncionarioController.java`

**Checkpoint**: Contratos completos — testar cenários C3-S1 a C3-S6 do quickstart.md

---

## Phase 6: User Story 4 — Dependentes e Qualificações (Priority: P4)

**Goal**: CRUD de dependentes e qualificações com soft delete; sub-recursos por funcionário

**Independent Test**: POST cria dependente; DELETE marca is_active=false; GET lista apenas activos; mesmo para qualificações

### Implementação US4 — Dependentes

- [X] T051a [P] [US4] Criar `DependenteFilter` com campos `funcionarioId` (UUID), `isActive` em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/filter/DependenteFilter.java`
- [X] T051b [P] [US4] Criar `QualificacaoFilter` com campos `funcionarioId` (UUID), `isActive` em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/filter/QualificacaoFilter.java`
- [X] T051 [P] [US4] Criar domain model `Dependente` com campos `funcionarioId`, `nome`, `parentesco`, `dataNascimento`, `nif`, `isActive` e métodos `criar()`, `atualizar()`, `desativar()`, `ativar()` em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/models/Dependente.java`
- [X] T052 [P] [US4] Criar port `DependenteRepository` com métodos `save`, `findById`, `findAllByFuncionarioId(UUID, boolean isActive)` em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/repository/DependenteRepository.java`
- [X] T053 [P] [US4] Criar `DependenteEntity` com `@Audited @Entity @Table("t_dependente")` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/entity/DependenteEntity.java`
- [X] T054 [P] [US4] Criar `DependenteEntityRepository` com métodos `findByFuncionarioIdAndIsActiveTrue`, `findByFuncionarioId` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/repository/DependenteEntityRepository.java`
- [X] T055 [P] [US4] Criar `DependenteMapper` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/mappers/DependenteMapper.java`
- [X] T056 [P] [US4] Criar `DependenteRepositoryImpl` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/adapters/DependenteRepositoryImpl.java`
- [X] T057 [P] [US4] Criar `DependenteRequest`, `DependenteResponse`, `WrapperListaDependenteDTO` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/dto/`
- [X] T058 [US4] Criar commands + handlers para Dependente: `CreateDependenteCommand+Handler`, `UpdateDependenteCommand+Handler`, `DesativarDependenteCommand+Handler`, `AtivarDependenteCommand+Handler` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/`
- [X] T059 [US4] Criar `GetDependentesByFuncionarioQuery` + handler e `GetDependenteByIdQuery` + handler em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/`

### Implementação US4 — Qualificações

- [X] T060 [P] [US4] Criar domain model `Qualificacao` com campos `funcionarioId`, `nivelAcademico`, `curso`, `instituicao`, `anoConclusao`, `pais`, `isActive` e métodos `criar()`, `atualizar()`, `desativar()`, `ativar()` em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/models/Qualificacao.java`
- [X] T061 [P] [US4] Criar port `QualificacaoRepository` com métodos `save`, `findById`, `findAllByFuncionarioId(UUID, boolean isActive)` em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/repository/QualificacaoRepository.java`
- [X] T062 [P] [US4] Criar `QualificacaoEntity` com `@Audited @Entity @Table("t_qualificacao")` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/entity/QualificacaoEntity.java`
- [X] T063 [P] [US4] Criar `QualificacaoEntityRepository` com métodos `findByFuncionarioIdAndIsActiveTrue`, `findByFuncionarioId` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/repository/QualificacaoEntityRepository.java`
- [X] T064 [P] [US4] Criar `QualificacaoMapper` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/mappers/QualificacaoMapper.java`
- [X] T065 [P] [US4] Criar `QualificacaoRepositoryImpl` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/adapters/QualificacaoRepositoryImpl.java`
- [X] T066 [P] [US4] Criar `QualificacaoRequest`, `QualificacaoResponse`, `WrapperListaQualificacaoDTO` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/dto/`
- [X] T067 [US4] Criar commands + handlers para Qualificacao: `CreateQualificacaoCommand+Handler`, `UpdateQualificacaoCommand+Handler`, `DesativarQualificacaoCommand+Handler`, `AtivarQualificacaoCommand+Handler` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/`
- [X] T068 [US4] Criar `GetQualificacoesByFuncionarioQuery` + handler e `GetQualificacaoByIdQuery` + handler em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/`
- [X] T069 [US4] Gerar `DependenteController` via `igrp-spring-generator` com endpoints próprios: `POST /api/v1/rh/dependentes`, `GET /api/v1/rh/dependentes/{id}`, `PUT /api/v1/rh/dependentes/{id}`, `DELETE /api/v1/rh/dependentes/{id}`, `PUT /api/v1/rh/dependentes/{id}/activate` em `src/main/java/cv/igrp/RH_Service/colaboradores/interfaces/rest/DependenteController.java`
- [X] T070 [US4] Gerar `QualificacaoController` via `igrp-spring-generator` com endpoints próprios: `POST /api/v1/rh/qualificacoes`, `GET /api/v1/rh/qualificacoes/{id}`, `PUT /api/v1/rh/qualificacoes/{id}`, `DELETE /api/v1/rh/qualificacoes/{id}`, `PUT /api/v1/rh/qualificacoes/{id}/activate` em `src/main/java/cv/igrp/RH_Service/colaboradores/interfaces/rest/QualificacaoController.java`
- [X] T071 [US4] Adicionar sub-recursos ao `FuncionarioController` (sem duplicação): `GET /api/v1/rh/funcionarios/{id}/dependentes` e `GET /api/v1/rh/funcionarios/{id}/qualificacoes` em `src/main/java/cv/igrp/RH_Service/colaboradores/interfaces/rest/FuncionarioController.java`

**Checkpoint**: Dependentes e qualificações completos — testar cenários C4-S1 a C4-S6 do quickstart.md

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Auditoria Envers e validação final de todos os cenários

- [X] T072 Criar `AuditHistoryEntryDTO` e `WrapperListaAuditHistoryDTO` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/dto/`
- [X] T073 Criar `GetColaboradoresAuditHistoryQuery` + `GetColaboradoresAuditHistoryQueryHandler` — suportar catálogos: `funcionarios`→`FuncionarioEntity`, `enquadramentos`→`EnquadramentoEntity`, `contratos`→`ContratoEntity`, `dependentes`→`DependenteEntity`, `qualificacoes`→`QualificacaoEntity`; padrão idêntico ao `GetCarreirasAuditHistoryQueryHandler` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/`
- [X] T074 Gerar `ColaboradoresAuditHistoryController` via `igrp-spring-generator` com endpoint `GET /api/v1/rh/colaboradores/audit/{catalog}/{entityId}` em `src/main/java/cv/igrp/RH_Service/colaboradores/interfaces/rest/ColaboradoresAuditHistoryController.java`
- [X] T075 Executar e validar todos os cenários do quickstart.md (C1 a C5 — 32 passos)

---

## Dependencies & Execution Order

### Dependências entre fases

- **Phase 1 (Setup)**: Sem dependências — pode começar imediatamente
- **Phase 2 (Foundational)**: Depende do Phase 1 (migration deve existir para que JPA valide as entidades)
- **Phase 3 (US1)**: Depende do Phase 2 (FuncionarioId pronto)
- **Phase 4 (US2)**: Depende do Phase 3 (Funcionário deve existir para criar enquadramento; reutiliza FuncionarioRepository)
- **Phase 5 (US3)**: Depende do Phase 3 (idem)
- **Phase 6 (US4)**: Depende do Phase 3 (idem)
- **Phase 7 (Polish)**: Depende de todas as fases anteriores

### Dentro de cada user story

- Value object → Domain model → Filter → Repository port → Entity JPA → JPA Repository → Mapper → Adapter → DTOs → Handlers → Controller

### Oportunidades de paralelismo

- T002–T006 (value objects) podem correr em paralelo
- T014–T015 (DTOs US1) podem correr em paralelo entre si
- T051–T057 (Dependente infra) e T060–T066 (Qualificacao infra) podem correr em paralelo entre si
- US3 e US4 podem ser implementadas em paralelo após US1 estar completa

---

## Parallel Example: User Story 4

```
# Dependente e Qualificação podem avançar em paralelo:
T051–T057: Dependente (domain, infra, DTOs)
T060–T066: Qualificacao (domain, infra, DTOs)

# Depois:
T058: Handlers Dependente
T067: Handlers Qualificacao
T059: Queries Dependente
T068: Queries Qualificacao
T069–T070: Controllers (gerados independentemente)
```

---

## Implementation Strategy

### MVP First (User Story 1 — Funcionário)

1. Phase 1: Migration V24
2. Phase 2: Value objects (T002–T006)
3. Phase 3: Funcionário completo (T007–T020)
4. **STOP e VALIDAR**: testar C1-S1 a C1-S9

### Incremental Delivery

1. Setup + Foundational → migration e value objects prontos
2. US1 (Funcionário) → MVP testável
3. US2 (Enquadramento) → PCFR funcional
4. US3 (Contratos) → vínculo laboral
5. US4 (Dependentes + Qualificações) → perfil completo
6. Polish (Auditoria) → rastreabilidade total

---

## Notas

- Controllers gerados via `igrp-spring-generator` — nunca editar manualmente
- `numero_funcionario` gerado por sequence PostgreSQL — atomicamente no handler
- `is_active` no Funcionário derivado automaticamente no domain model a partir de `situacao_profissional`
- Cargo e Unidade Orgânica no enquadramento são validados individualmente (activos/existentes) mas sem validação cruzada entre si — `t_cargo` é catálogo global
- `GradeRepositoryImpl.isReferencedByActiveAssignment` já usa `JdbcTemplate` para evitar contaminação da sessão Hibernate — mesmo padrão a seguir se necessário noutras tabelas futuras
