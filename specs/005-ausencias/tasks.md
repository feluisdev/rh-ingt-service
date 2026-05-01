# Tasks: Módulo de Ausências — BC Colaboradores

**Input**: Design documents from `specs/005-ausencias/`  
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅, quickstart.md ✅

**Organização**: 4 User Stories (P1→P4). US1 é o MVP — catálogos bloqueiam tudo o resto.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: pode correr em paralelo (ficheiros diferentes, sem dependências incompletas)
- **[Story]**: user story correspondente (US1–US4)

## Convenções de caminho (base: `src/main/java/cv/igrp/RH_Service/colaboradores/`)

- `app/cmd/` → `application/commands/`
- `app/qry/` → `application/queries/`
- `app/dto/` → `application/dto/`
- `domain/m/` → `domain/models/`
- `domain/vo/` → `domain/valueobject/`
- `domain/r/` → `domain/repository/`
- `domain/f/` → `domain/filter/`
- `domain/svc/` → `domain/service/`
- `infra/e/` → `infrastructure/persistence/entity/`
- `infra/r/` → `infrastructure/persistence/repository/`
- `infra/a/` → `infrastructure/persistence/adapters/`
- `infra/m/` → `infrastructure/mappers/`
- `rest/` → `interfaces/rest/`

---

## Phase 1: Setup

**Purpose**: Migração de base de dados para as 6 novas tabelas.

- [ ] T001 Criar migração `src/main/resources/db/migration/V26__create_ausencias_tables.sql` — **DEFENSIVA**: usar `CREATE TABLE IF NOT EXISTS`, `CREATE INDEX IF NOT EXISTS`, `CREATE SEQUENCE IF NOT EXISTS`; incluir `t_leave_type`, `t_leave_mobility_subtype`, `t_public_holiday` (+ `CREATE UNIQUE INDEX IF NOT EXISTS idx_feriado_nacional_data_unique ON t_public_holiday (data) WHERE is_national = true AND is_active = true`), `t_leave_request`, `t_leave_balance`, `t_leave_mobility`; seed com `INSERT INTO t_public_holiday ... ON CONFLICT DO NOTHING` para os 11 feriados nacionais CV 2026

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Value objects e serviço de domínio partilhados por todas as user stories.

**⚠️ CRÍTICO**: Nenhuma user story pode começar antes desta fase estar completa.

- [ ] T002 Criar `domain/svc/DiasUteisCalculator.java` — classe POJO pura (`final`, sem `@Component`) com método estático `int calcular(LocalDate inicio, LocalDate fim, Set<LocalDate> feriadosNacionais)` que itera dia a dia excluindo `SATURDAY`, `SUNDAY` e datas no set; lança `IgrpResponseStatusException.unprocessableEntity()` se `inicio > fim` ou resultado = 0; criar também `config/AusenciasConfig.java` (ou equivalente) com `@Bean DiasUteisCalculator diasUteisCalculator()` para injecção via constructor nos handlers
- [ ] T003 [P] Criar `domain/vo/TipoAusenciaId.java` — value object UUID seguindo o padrão `FuncionarioId.java` (gerarNovo/from(UUID)/from(String), equals+hashCode)
- [ ] T004 [P] Criar `domain/vo/SubtipoLicencaMobilidadeId.java` — mesmo padrão
- [ ] T005 [P] Criar `domain/vo/FeriadoId.java` — mesmo padrão
- [ ] T006 [P] Criar `domain/vo/PedidoAusenciaId.java` — mesmo padrão
- [ ] T007 [P] Criar `domain/vo/SaldoAusenciaId.java` — mesmo padrão
- [ ] T008 [P] Criar `domain/vo/LicencaMobilidadeId.java` — mesmo padrão

**Checkpoint**: Foundation pronta — todas as user stories podem começar.

---

## Phase 3: User Story 1 — Gestão de Catálogos de Ausência (Priority: P1) 🎯 MVP

**Goal**: CRUD completo de TipoAusencia, SubtipoLicencaMobilidade e Feriados com soft delete e filtros.

**Independent Test**: Executar cenários C1-S1 a C1-S5 do quickstart.md contra app em execução — verificar criação, unicidade de código, filtragem de feriados e seed dos 11 feriados nacionais.

### TipoAusencia

- [ ] T009 [P] [US1] Criar `domain/m/TipoAusencia.java` — modelo de domínio com campos: `TipoAusenciaId id`, `String nome`, `String codigo`, `boolean deductsBalance`, `boolean requiresApproval`, `Integer maxDaysPerYear`, `String categoryOptionCkey`, `boolean isActive`; factory `criar(...)` e métodos `ativar()` / `desativar()`
- [ ] T010 [P] [US1] Criar `domain/f/TipoAusenciaFilter.java` — campos: `Boolean active`
- [ ] T011 [P] [US1] Criar `domain/r/TipoAusenciaRepository.java` — port com: `save`, `findById(TipoAusenciaId)`, `findAll(TipoAusenciaFilter)`, `existsByCodigo(String)`, `existsByCodigoAndIdNot(String, TipoAusenciaId)`
- [ ] T012 [P] [US1] Criar `infra/e/TipoAusenciaEntity.java` — `@Entity(name="ColabsTipoAusenciaEntity")`, `@Table(name="t_leave_type")`, `@Audited`, campos JPA mapeados, `@EntityListeners(AuditingEntityListener.class)`
- [ ] T013 [P] [US1] Criar `infra/r/ColabsTipoAusenciaEntityRepository.java` — `extends JpaRepository<TipoAusenciaEntity, UUID>` com `existsByCodigo`, `existsByCodigoAndIdNot`, `findAllByIsActive`
- [ ] T014 [P] [US1] Criar `infra/m/TipoAusenciaMapper.java` — `toDTO(TipoAusencia)→TipoAusenciaResponse`, `toDomain(TipoAusenciaEntity)→TipoAusencia`, `toEntity(TipoAusencia)→TipoAusenciaEntity`
- [ ] T015 [US1] Criar `infra/a/TipoAusenciaRepositoryImpl.java` — `@Repository("colabsTipoAusenciaRepositoryImpl")`, implementa `TipoAusenciaRepository`, injeta `ColabsTipoAusenciaEntityRepository` + `TipoAusenciaMapper`
- [ ] T016 [P] [US1] Criar DTOs: `app/dto/TipoAusenciaRequest.java`, `TipoAusenciaResponse.java`, `WrapperListaTipoAusenciaDTO.java`
- [ ] T017 [P] [US1] Criar `app/cmd/CreateTipoAusenciaCommand.java` + `CreateTipoAusenciaCommandHandler.java` — valida unicidade de `codigo` (409), persiste; retorna `201` com id
- [ ] T018 [P] [US1] Criar `app/cmd/UpdateTipoAusenciaCommand.java` + `UpdateTipoAusenciaCommandHandler.java` — valida unicidade de `codigo` excluindo o próprio (409)
- [ ] T019 [P] [US1] Criar `app/cmd/AtivarTipoAusenciaCommand.java` + `AtivarTipoAusenciaCommandHandler.java` — idempotente (200 se já activo)
- [ ] T020 [P] [US1] Criar `app/cmd/DesativarTipoAusenciaCommand.java` + `DesativarTipoAusenciaCommandHandler.java`
- [ ] T021 [P] [US1] Criar `app/qry/GetTipoAusenciaByIdQuery.java` + `GetTipoAusenciaByIdQueryHandler.java` — 404 se não encontrado
- [ ] T022 [P] [US1] Criar `app/qry/GetTiposAusenciaQuery.java` + `GetTiposAusenciaQueryHandler.java` — suporta filtro `active`
- [ ] T023 [US1] Criar `rest/TipoAusenciaController.java` — endpoints: `POST /parametrizacoes/tipos-ausencia`, `GET /parametrizacoes/tipos-ausencia`, `GET /parametrizacoes/tipos-ausencia/{id}`, `PUT /parametrizacoes/tipos-ausencia/{id}`, `PATCH /parametrizacoes/tipos-ausencia/{id}/ativar`, `PATCH /parametrizacoes/tipos-ausencia/{id}/desativar`; anotado `@IgrpController`, delega para command/query bus

### SubtipoLicencaMobilidade

- [ ] T024 [P] [US1] Criar `domain/m/SubtipoLicencaMobilidade.java` — campos: `SubtipoLicencaMobilidadeId id`, `String nome`, `String codigo`, `String recordType` (LICENCA/MOBILIDADE/AMBOS), `boolean affectsPay`, `boolean countsForSeniority`, `boolean canSelfSubmit`, `boolean isActive`; métodos `ativar()` / `desativar()`
- [ ] T025 [P] [US1] Criar `domain/f/SubtipoLicencaMobilidadeFilter.java` — campos: `Boolean active`, `String recordType`
- [ ] T026 [P] [US1] Criar `domain/r/SubtipoLicencaMobilidadeRepository.java` — port com: `save`, `findById`, `findAll(filter)`, `existsByCodigo`, `existsByCodigoAndIdNot`
- [ ] T027 [P] [US1] Criar `infra/e/SubtipoLicencaMobilidadeEntity.java` — `@Entity(name="ColabsSubtipoLicencaMobilidadeEntity")`, `@Table(name="t_leave_mobility_subtype")`, `@Audited`
- [ ] T028 [P] [US1] Criar `infra/r/ColabsSubtipoLicencaMobilidadeEntityRepository.java` — `extends JpaRepository<SubtipoLicencaMobilidadeEntity, UUID>`
- [ ] T029 [P] [US1] Criar `infra/m/SubtipoLicencaMobilidadeMapper.java`
- [ ] T030 [US1] Criar `infra/a/SubtipoLicencaMobilidadeRepositoryImpl.java` — `@Repository("colabsSubtipoLicencaMobilidadeRepositoryImpl")`
- [ ] T031 [P] [US1] Criar DTOs: `SubtipoLicencaMobilidadeRequest.java`, `SubtipoLicencaMobilidadeResponse.java`, `WrapperListaSubtipoLicencaMobilidadeDTO.java`
- [ ] T032 [P] [US1] Criar `CreateSubtipoLicencaMobilidadeCommand.java` + Handler — valida `recordType` (400 se inválido), unicidade de `codigo` (409)
- [ ] T033 [P] [US1] Criar `UpdateSubtipoLicencaMobilidadeCommand.java` + Handler
- [ ] T034 [P] [US1] Criar `AtivarSubtipoLicencaMobilidadeCommand.java` + Handler (idempotente)
- [ ] T035 [P] [US1] Criar `DesativarSubtipoLicencaMobilidadeCommand.java` + Handler
- [ ] T036 [P] [US1] Criar `GetSubtipoLicencaMobilidadeByIdQuery.java` + Handler
- [ ] T037 [P] [US1] Criar `GetSubtiposLicencaMobilidadeQuery.java` + Handler — filtros `active` e `recordType`
- [ ] T038 [US1] Criar `rest/SubtipoLicencaMobilidadeController.java` — endpoints: `POST/GET /parametrizacoes/subtipos-licenca-mobilidade`, `GET/PUT/{id}`, `PATCH/{id}/ativar`, `PATCH/{id}/desativar`

### Feriado

- [ ] T039 [P] [US1] Criar `domain/m/Feriado.java` — campos: `FeriadoId id`, `String nome`, `LocalDate data`, `boolean isNational`, `String municipioCkey`, `boolean isActive`; métodos `ativar()` / `desativar()`
- [ ] T040 [P] [US1] Criar `domain/f/FeriadoFilter.java` — campos: `Integer ano`, `Boolean isNational`, `Boolean active`
- [ ] T041 [P] [US1] Criar `domain/r/FeriadoRepository.java` — port com: `save`, `findById`, `findAll(filter)`, `existsNacionalActivoByData(LocalDate)`, `findAllNacionaisActivosByAno(int): List<LocalDate>` (usado pelo DiasUteisCalculator)
- [ ] T042 [P] [US1] Criar `infra/e/FeriadoEntity.java` — `@Entity(name="ColabsFeriadoEntity")`, `@Table(name="t_public_holiday")`, `@Audited`
- [ ] T043 [P] [US1] Criar `infra/r/ColabsFeriadoEntityRepository.java` — com queries: `existsByDataAndIsNationalTrueAndIsActiveTrue`, `findByIsNationalTrueAndIsActiveTrue`
- [ ] T044 [P] [US1] Criar `infra/m/FeriadoMapper.java`
- [ ] T045 [US1] Criar `infra/a/FeriadoRepositoryImpl.java` — `@Repository("colabsFeriadoRepositoryImpl")`
- [ ] T046 [P] [US1] Criar DTOs: `FeriadoRequest.java`, `FeriadoResponse.java`, `WrapperListaFeriadoDTO.java`
- [ ] T047 [P] [US1] Criar `CreateFeriadoCommand.java` + Handler — verifica unicidade de feriado nacional activo por data (409)
- [ ] T048 [P] [US1] Criar `UpdateFeriadoCommand.java` + Handler — re-verifica unicidade se `data` ou `isNational` mudar
- [ ] T049 [P] [US1] Criar `AtivarFeriadoCommand.java` + Handler — verifica conflito de data para nacionais antes de activar (409); idempotente se já activo
- [ ] T050 [P] [US1] Criar `DesativarFeriadoCommand.java` + Handler
- [ ] T051 [P] [US1] Criar `GetFeriadoByIdQuery.java` + Handler
- [ ] T052 [P] [US1] Criar `GetFeriadosQuery.java` + Handler — filtros `ano` (extrai por ano de `data`), `isNational`, `active`
- [ ] T053 [US1] Criar `rest/FeriadoController.java` — endpoints: `POST/GET /parametrizacoes/feriados`, `GET/PUT/{id}`, `PATCH/{id}/ativar`, `PATCH/{id}/desativar`

**Checkpoint**: US1 completa — catálogos funcionais, seed de feriados aplicado, cenários C1-S1 a C1-S5 devem passar.

---

## Phase 4: User Story 2 — Submissão e Decisão de Pedidos de Ausência (Priority: P2)

**Goal**: Workflow completo de pedidos: submissão com cálculo de dias úteis, aprovação, rejeição e cancelamento.

**Independent Test**: Executar cenários C3-S1 a C3-S7 do quickstart.md — verificar cálculo de dias úteis, bloqueio de sobreposição, transições de estado e idempotência de cancelamento.

**Depends on**: US1 completa (TipoAusenciaId, FeriadoRepository)

- [ ] T054 [P] [US2] Criar `domain/m/PedidoAusencia.java` — campos: `PedidoAusenciaId id`, `FuncionarioId funcionarioId`, `TipoAusenciaId tipoAusenciaId`, `LocalDate dataInicio`, `LocalDate dataFim`, `int numeroDias`, `String motivo`, `String estado` (PENDENTE/APROVADO/REJEITADO/CANCELADO), `FuncionarioId aprovadoPor`, `LocalDate dataDecisao`, `String observacoesDecisao`, `boolean isActive`; factory `criar(...)`; métodos de domínio: `aprovar(FuncionarioId, LocalDate, String)`, `rejeitar(FuncionarioId, LocalDate, String)`, `cancelar()` — cada método valida a transição e lança `IgrpResponseStatusException.conflict()` se inválida
- [ ] T055 [P] [US2] Criar `domain/f/PedidoAusenciaFilter.java` — campos: `UUID funcionarioId`, `String estado`, `UUID tipoAusenciaId`, `Integer ano`
- [ ] T056 [P] [US2] Criar `domain/r/PedidoAusenciaRepository.java` — port com: `save`, `findById`, `findAllByFuncionarioId(FuncionarioId, PedidoAusenciaFilter)`, `existsOverlapForFuncionario(FuncionarioId, LocalDate, LocalDate)` (verifica APROVADO e PENDENTE)
- [ ] T057 [P] [US2] Criar `infra/e/PedidoAusenciaEntity.java` — `@Entity(name="ColabsPedidoAusenciaEntity")`, `@Table(name="t_leave_request")`, `@Audited`; coluna `estado VARCHAR(20)`, FKs para `t_funcionario` (funcionario_id e aprovado_por), FK para `t_leave_type`
- [ ] T058 [P] [US2] Criar `infra/r/ColabsPedidoAusenciaEntityRepository.java` — com query JPQL para verificar sobreposição: `existsByFuncionarioIdAndEstadoInAndDataInicioLessThanEqualAndDataFimGreaterThanEqual`
- [ ] T059 [P] [US2] Criar `infra/m/PedidoAusenciaMapper.java` — inclui mapeamento de `TipoAusencia` nested no response
- [ ] T060 [US2] Criar `infra/a/PedidoAusenciaRepositoryImpl.java` — `@Repository("colabsPedidoAusenciaRepositoryImpl")`
- [ ] T061 [P] [US2] Criar DTOs: `PedidoAusenciaRequest.java` (tipoAusenciaId, dataInicio, dataFim, motivo), `AprovarPedidoRequest.java` (aprovadoPorId, observacoesDecisao), `RejeitarPedidoRequest.java` (aprovadoPorId, observacoesDecisao), `PedidoAusenciaResponse.java`, `WrapperListaPedidoAusenciaDTO.java`
- [ ] T062 [US2] Criar `app/cmd/CreatePedidoAusenciaCommand.java` + `CreatePedidoAusenciaCommandHandler.java` — (1) valida funcionário existe; (2) valida tipo activo; (3) carrega feriados nacionais do ano via `FeriadoRepository.findAllNacionaisActivosByAno`; (4) chama `DiasUteisCalculator.calcular` (422 se zero dias); (5) verifica sobreposição com pedidos APROVADO e PENDENTE do mesmo funcionário (409); (6) verifica `maxDaysPerYear` se não-nulo: soma `numeroDias` de todos os pedidos do mesmo funcionário+tipo+ano com estado ≠ REJEITADO e ≠ CANCELADO — se soma + novos dias > limite, 422; (7) persiste com `estado=PENDENTE`
- [ ] T063 [US2] Criar `app/cmd/AprovarPedidoAusenciaCommand.java` + `AprovarPedidoAusenciaCommandHandler.java` — (1) 404 se pedido não existe; (2) chama `pedido.aprovar(...)` (409 se não PENDENTE); (3) se `tipoAusencia.deductsBalance=true`: busca `SaldoAusencia` via `SaldoAusenciaRepository` (injectado) — 422 se não existe; 422 se `saldo.saldoDisponivel() < pedido.getNumeroDias()`; chama `saldo.incrementarPendentes(pedido.getNumeroDias())` e persiste saldo; (4) persiste pedido aprovado — **Nota**: depende de `SaldoAusenciaRepository` port (T071); implementar T071 antes de T063
- [ ] T064 [P] [US2] Criar `app/cmd/RejeitarPedidoAusenciaCommand.java` + `RejeitarPedidoAusenciaCommandHandler.java` — chama `pedido.rejeitar(...)` (409 se não PENDENTE)
- [ ] T065 [US2] Criar `app/cmd/CancelarPedidoAusenciaCommand.java` + `CancelarPedidoAusenciaCommandHandler.java` — o command inclui `funcionarioId` do solicitante; o handler: (1) 404 se pedido não existe; (2) valida que `command.getSolicitanteId()` equals `pedido.getFuncionarioId()` (funcionário cancela o seu próprio) — autorização de gestor é gerida pela camada de segurança REST; (3) chama `pedido.cancelar()` (409 se REJEITADO ou CANCELADO); (4) persiste
- [ ] T066 [P] [US2] Criar `app/qry/GetPedidoAusenciaByIdQuery.java` + Handler — 404 se não encontrado
- [ ] T067 [P] [US2] Criar `app/qry/GetPedidosByFuncionarioQuery.java` + Handler — filtros estado, tipoAusenciaId, ano
- [ ] T068 [US2] Criar `rest/PedidoAusenciaController.java` — endpoints sob `/funcionarios/{funcionarioId}/pedidos-ausencia`: `POST /`, `GET /`, `GET /{id}`, `PATCH /{id}/aprovar`, `PATCH /{id}/rejeitar`, `PATCH /{id}/cancelar`

**Checkpoint**: US2 completa — workflow de pedidos funcional, cenários C3-S1 a C3-S7 devem passar.

---

## Phase 5: User Story 3 — Consulta e Gestão de Saldos de Ausência (Priority: P3)

**Goal**: CRUD de saldos anuais e sincronização automática com aprovações e cancelamentos.

**Independent Test**: Executar cenários C2-S1 a C2-S3 e verificar que após C3-S4 (aprovação) `diasPendentes=5` e após C3-S6 (cancelamento) `diasPendentes=0`.

**Depends on**: US1 (TipoAusenciaId), US2 (AprovarPedidoAusenciaCommandHandler, CancelarPedidoAusenciaCommandHandler)

- [ ] T069 [P] [US3] Criar `domain/m/SaldoAusencia.java` — campos: `SaldoAusenciaId id`, `FuncionarioId funcionarioId`, `TipoAusenciaId tipoAusenciaId`, `int ano`, `int diasDireito`, `int diasGozados`, `int diasPendentes`; métodos: `incrementarPendentes(int dias)`, `decrementarPendentes(int dias)`, `saldoDisponivel()` = `diasDireito - diasGozados - diasPendentes`
- [ ] T070 [P] [US3] Criar `domain/f/SaldoAusenciaFilter.java` — campos: `UUID funcionarioId`, `Integer ano`, `UUID tipoAusenciaId`
- [ ] T071 [P] [US3] Criar `domain/r/SaldoAusenciaRepository.java` — port com: `save`, `findById`, `findAllByFuncionarioId(FuncionarioId, SaldoAusenciaFilter)`, `findByFuncionarioIdAndTipoAusenciaIdAndAno(FuncionarioId, TipoAusenciaId, int): Optional<SaldoAusencia>`, `existsByFuncionarioIdAndTipoAusenciaIdAndAno(...): boolean`
- [ ] T072 [P] [US3] Criar `infra/e/SaldoAusenciaEntity.java` — `@Entity(name="ColabsSaldoAusenciaEntity")`, `@Table(name="t_leave_balance")`, `@Audited`; unique constraint `(funcionario_id, tipo_ausencia_id, ano)`
- [ ] T073 [P] [US3] Criar `infra/r/ColabsSaldoAusenciaEntityRepository.java` — com `findByFuncionarioIdAndTipoAusenciaIdAndAno`, `existsByFuncionarioIdAndTipoAusenciaIdAndAno`
- [ ] T074 [P] [US3] Criar `infra/m/SaldoAusenciaMapper.java` — response inclui `diasDisponiveis` calculado
- [ ] T075 [US3] Criar `infra/a/SaldoAusenciaRepositoryImpl.java` — `@Repository("colabsSaldoAusenciaRepositoryImpl")`
- [ ] T076 [P] [US3] Criar DTOs: `SaldoAusenciaRequest.java` (tipoAusenciaId, ano, diasDireito), `SaldoAusenciaResponse.java` (inclui diasDisponiveis), `WrapperListaSaldoAusenciaDTO.java`
- [ ] T077 [P] [US3] Criar `app/cmd/CreateSaldoAusenciaCommand.java` + Handler — valida funcionário e tipo existem; verifica unicidade (409); persiste com `diasGozados=0`, `diasPendentes=0`
- [ ] T078 [P] [US3] Criar `app/cmd/UpdateSaldoAusenciaCommand.java` + Handler — actualiza apenas `diasDireito` (validar >= 0)
- [ ] T079 [P] [US3] Criar `app/qry/GetSaldoAusenciaByIdQuery.java` + Handler
- [ ] T080 [P] [US3] Criar `app/qry/GetSaldosByFuncionarioQuery.java` + Handler — filtros ano, tipoAusenciaId
- [ ] T081 [US3] Criar `rest/SaldoAusenciaController.java` — endpoints sob `/funcionarios/{funcionarioId}/saldos-ausencia`: `POST /`, `GET /`, `GET /{id}`, `PUT /{id}`
- [ ] T082 [US3] Completar `AprovarPedidoAusenciaCommandHandler.java` — verificar que a injecção de `SaldoAusenciaRepositoryImpl` (T075) está disponível e que o handler compila com a implementação concreta; fazer smoke test: criar tipo com `deductsBalance=true`, criar saldo com `diasDireito=5`, submeter pedido de 3 dias, aprovar → verificar `diasPendentes=3`
- [ ] T083 [US3] Actualizar `CancelarPedidoAusenciaCommandHandler.java` — adicionar: se pedido estava `APROVADO` e `tipoAusencia.deductsBalance=true`, busca `SaldoAusencia` via `SaldoAusenciaRepository.findByFuncionarioIdAndTipoAusenciaIdAndAno` e chama `saldo.decrementarPendentes(pedido.getNumeroDias())` e persiste; injectar `SaldoAusenciaRepository` no handler

**Checkpoint**: US3 completa — saldos sincronizados, cenários C2-S1 a C2-S3 devem passar.

---

## Phase 6: User Story 4 — Registo de Licenças e Mobilidade (Priority: P4)

**Goal**: CRUD de licenças e mobilidades de funcionários, com suporte a situação em curso (sem data_fim).

**Independent Test**: Executar cenários C4-S1 a C4-S3 do quickstart.md — verificar criação sem data_fim, encerramento com data_fim e listagem activa.

**Depends on**: US1 completa (SubtipoLicencaMobilidadeId)

- [ ] T084 [P] [US4] Criar `domain/m/LicencaMobilidade.java` — campos: `LicencaMobilidadeId id`, `FuncionarioId funcionarioId`, `SubtipoLicencaMobilidadeId subtipoId`, `LocalDate dataInicio`, `LocalDate dataFim` (nullable), `String entidadeDestino`, `String despachoNumero`, `String observacoes`, `boolean isActive`; métodos `ativar()` / `desativar()`
- [ ] T085 [P] [US4] Criar `domain/f/LicencaMobilidadeFilter.java` — campos: `UUID funcionarioId`, `Boolean active`, `UUID subtipoId`
- [ ] T086 [P] [US4] Criar `domain/r/LicencaMobilidadeRepository.java` — port com: `save`, `findById`, `findAllByFuncionarioId(FuncionarioId, LicencaMobilidadeFilter)`
- [ ] T087 [P] [US4] Criar `infra/e/LicencaMobilidadeEntity.java` — `@Entity(name="ColabsLicencaMobilidadeEntity")`, `@Table(name="t_leave_mobility")`, `@Audited`; FK para `t_leave_mobility_subtype`
- [ ] T088 [P] [US4] Criar `infra/r/ColabsLicencaMobilidadeEntityRepository.java` — com `findByFuncionarioIdAndIsActiveTrue`
- [ ] T089 [P] [US4] Criar `infra/m/LicencaMobilidadeMapper.java` — inclui subtipo nested no response
- [ ] T090 [US4] Criar `infra/a/LicencaMobilidadeRepositoryImpl.java` — `@Repository("colabsLicencaMobilidadeRepositoryImpl")`
- [ ] T091 [P] [US4] Criar DTOs: `LicencaMobilidadeRequest.java`, `LicencaMobilidadeResponse.java` (subtipo nested), `WrapperListaLicencaMobilidadeDTO.java`
- [ ] T092 [P] [US4] Criar `app/cmd/CreateLicencaMobilidadeCommand.java` + Handler — valida funcionário e subtipo existem; valida `dataFim >= dataInicio` se fornecido (400)
- [ ] T093 [P] [US4] Criar `app/cmd/UpdateLicencaMobilidadeCommand.java` + Handler — valida `dataFim >= dataInicio` se fornecido
- [ ] T094 [P] [US4] Criar `app/cmd/AtivarLicencaMobilidadeCommand.java` + Handler (idempotente)
- [ ] T095 [P] [US4] Criar `app/cmd/DesativarLicencaMobilidadeCommand.java` + Handler
- [ ] T096 [P] [US4] Criar `app/qry/GetLicencaMobilidadeByIdQuery.java` + Handler
- [ ] T097 [P] [US4] Criar `app/qry/GetLicencasByFuncionarioQuery.java` + Handler — filtros active, subtipoId
- [ ] T098 [US4] Criar `rest/LicencaMobilidadeController.java` — endpoints sob `/funcionarios/{funcionarioId}/licencas-mobilidade`: `POST /`, `GET /`, `GET /{id}`, `PUT /{id}`, `PATCH /{id}/ativar`, `PATCH /{id}/desativar`

**Checkpoint**: US4 completa — cenários C4-S1 a C4-S3 devem passar.

---

## Phase 7: Polish & Validação Final

- [ ] T099 Compilar o projecto: `mvn clean package -DskipTests -Dmaven.compiler.release=21` e confirmar BUILD SUCCESS
- [ ] T100 Executar testes unitários: `mvn test -Dmaven.compiler.release=21` e confirmar 0 falhas
- [ ] T101 Iniciar aplicação com `java -jar target/RH-Service-*.jar` e executar todos os cenários quickstart C1–C5 (18 passos)
- [ ] T102 Verificar que a migração V26 aplicou correctamente via `psql -c "\d t_leave_type"` e que o índice único parcial `idx_feriado_nacional_data_unique` existe

---

## Dependencies & Execution Order

### Dependências de fase

- **Phase 1 (Setup)**: Sem dependências — começar imediatamente
- **Phase 2 (Foundational)**: Depende de Phase 1 — bloqueia todas as user stories
- **Phase 3 (US1)**: Depende de Phase 2 — catálogos necessários para US2, US3 e US4
- **Phase 4 (US2)**: Depende de US1 (TipoAusenciaId, FeriadoRepository)
- **Phase 5 (US3)**: Depende de US1 + US2 (handlers de aprovação/cancelamento)
- **Phase 6 (US4)**: Depende de US1 (SubtipoLicencaMobilidadeId) — independente de US2 e US3
- **Phase 7 (Polish)**: Depende de todas as user stories desejadas

### Dependências por user story

- **US1 (P1)**: Pode começar após Foundational — base para tudo
- **US2 (P2)**: Após US1 — depende de TipoAusenciaId e FeriadoRepository
- **US3 (P3)**: Após US1 + US2 — actualiza handlers de US2
- **US4 (P4)**: Após US1 — independente de US2 e US3

### Dentro de cada story

- VOs e filtros: paralelos entre si
- Domain model + repository port: paralelos
- JPA entity + Spring Data repo + mapper: paralelos
- Repository Impl: após JPA entity + Spring Data repo + mapper
- DTOs: paralelos com qualquer coisa
- Commands + handlers: paralelos entre si (após domain model + repository impl)
- Query + handlers: paralelos entre si
- Controller: após todos os commands e queries

---

## Parallel Example: User Story 1 (TipoAusencia)

```
# Podem correr em paralelo:
T009 TipoAusencia domain model
T010 TipoAusenciaFilter
T011 TipoAusenciaRepository port
T012 TipoAusenciaEntity (JPA)
T013 ColabsTipoAusenciaEntityRepository
T014 TipoAusenciaMapper
T016 DTOs TipoAusencia

# Após T012 + T013 + T014:
T015 TipoAusenciaRepositoryImpl

# Após T015 (e T011 para o port):
T017, T018, T019, T020 Commands (paralelos entre si)
T021, T022 Queries (paralelas entre si)

# Após todos os commands e queries:
T023 TipoAusenciaController
```

---

## Implementation Strategy

### MVP (US1 apenas)

1. Phase 1: Migração V26
2. Phase 2: Value objects + DiasUteisCalculator
3. Phase 3: US1 (TipoAusencia + SubtipoLicencaMobilidade + Feriado)
4. **PARAR e VALIDAR**: C1-S1 a C1-S5
5. Demonstrar catálogos configurados

### Entrega incremental

1. Setup + Foundational → base pronta
2. US1 → catálogos → demo C1
3. US2 → pedidos → demo C2 + C3
4. US3 → saldos → validação completa C2 + C3 com contadores
5. US4 → licenças → demo C4
6. Validação final C5 (cálculo feriados)

---

## Notes

- Prefixo `Colabs` obrigatório em todas as interfaces Spring Data JPA — evita conflitos de bean com módulos legados
- `@Entity(name="ColabsXEntity")` obrigatório em todas as entidades — evita conflitos de Hibernate
- `DiasUteisCalculator` é um POJO puro (sem `@Component`) — injectado como new ou como `@Bean` de configuração
- Commit após cada fase ou grupo lógico de tarefas
- Parar em cada checkpoint para validar a story independentemente
