# Tasks: Colocações de Funcionários

**Input**: Design documents from `specs/007-colocacoes-funcionario/`  
**Prerequisites**: plan.md ✅ · spec.md ✅ · research.md ✅ · data-model.md ✅ · contracts/ ✅ · quickstart.md ✅

**Tests**: Não solicitados — apenas smoke tests manuais na fase de polish.

**Organization**: Tarefas agrupadas por user story para implementação e teste independentes.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode correr em paralelo (ficheiros diferentes, sem dependências incompletas)
- **[Story]**: User story correspondente (US1–US5)

---

## Phase 1: Setup

**Purpose**: Migração da base de dados e enumeração de tipos.

- [ ] T001 Criar migração Flyway `V28__create_employee_unit_assignments.sql` em `src/main/resources/db/migration/V28__create_employee_unit_assignments.sql` com tabela `employee_unit_assignments`, FKs para `t_funcionario`, `t_unidade_organica`, `t_cargo`, e índices `idx_eua_funcionario_current` (parcial WHERE is_active=TRUE) e `idx_eua_funcionario_history`
- [ ] T002 [P] Criar enum `TipoAfectacao` em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/models/TipoAfectacao.java` com valores INICIAL, TRANSFERENCIA, MOBILIDADE, REQUISICAO

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Value object, domain model e port — necessários para todas as user stories.

**⚠️ CRÍTICO**: Nenhuma user story pode começar antes desta fase estar completa.

- [ ] T003 Criar `ColocacaoId` em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/valueobject/ColocacaoId.java` seguindo exactamente o padrão de `FuncionarioId` (wraps ExternalID, métodos gerarNovo/from(UUID)/from(String), equals/hashCode)
- [ ] T004 Criar `Colocacao` domain model em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/models/Colocacao.java` com campos: id (ColocacaoId), funcionarioId (FuncionarioId), unitId (UUID, nullable), jobId (UUID, nullable), startDate (LocalDate), endDate (LocalDate, nullable), isCurrent (Boolean), isActive (Boolean), assignmentType (TipoAfectacao), notes (String, nullable); métodos estáticos `criar(...)` e `reconstituir(...)`, e métodos de instância `fechar(LocalDate)`, `atualizar(LocalDate endDate, String notes)`, `desativar()`
- [ ] T005 [P] Criar `ColocacaoFilter` em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/filter/ColocacaoFilter.java` com `@Data` Lombok: campos `Boolean isCurrent`
- [ ] T006 Criar port `ColocacaoRepository` em `src/main/java/cv/igrp/RH_Service/colaboradores/domain/repository/ColocacaoRepository.java` com métodos: `save(Colocacao)`, `findById(ColocacaoId)`, `findAllByFuncionarioId(FuncionarioId, ColocacaoFilter)`, `findCurrentByFuncionarioId(FuncionarioId)` (retorna Optional<Colocacao>), `fecharColocacaoAtual(FuncionarioId, LocalDate endDate)` (actualiza via JPQL), `existsByFuncionarioId(FuncionarioId): boolean` (para validação FR-016 — INICIAL só permitido se não existem colocações prévias)

**Checkpoint**: Domain puro completo — implementação de infra e handlers pode começar.

---

## Phase 3: User Story 1 — Registar Nova Colocação (P1) 🎯 MVP

**Goal**: Gestor regista colocação de funcionário; sistema fecha a anterior automaticamente.

**Independent Test**: `POST /api/v1/rh/funcionarios/{id}/colocacoes` devolve 201 com `isCurrent=true`; listagem mostra colocação anterior fechada.

- [ ] T007 [P] [US1] Criar `ColocacaoEntity` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/entity/ColocacaoEntity.java` com `@Entity(name="ColabsColocacaoEntity")`, `@Table(name="employee_unit_assignments")`, `@Audited`, extends AuditEntity; campos: id (UUID @Id), funcionarioId (UUID), unitId (UUID nullable), jobId (UUID nullable), startDate (LocalDate), endDate (LocalDate nullable), isCurrent (Boolean), isActive (Boolean), assignmentType (String), notes (String nullable)
- [ ] T008 [P] [US1] Criar `ColabsColocacaoEntityRepository` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/repository/ColabsColocacaoEntityRepository.java` como interface Spring Data JPA (`JpaRepository<ColocacaoEntity, UUID>`); adicionar métodos: `findAllByFuncionarioIdAndIsActiveTrue(UUID funcionarioId)`, `findByFuncionarioIdAndIsCurrentTrueAndIsActiveTrue(UUID funcionarioId)`, `existsByFuncionarioId(UUID funcionarioId)` (para FR-016), `@Modifying @Query("UPDATE ColabsColocacaoEntity c SET c.isCurrent = false, c.endDate = :endDate WHERE c.funcionarioId = :funcionarioId AND c.isCurrent = true") void fecharColocacaoAtual(@Param("funcionarioId") UUID funcionarioId, @Param("endDate") LocalDate endDate)`
- [ ] T009 [US1] Criar `ColocacaoMapper` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/mappers/ColocacaoMapper.java` com métodos `toDomain(ColocacaoEntity)` usando `Colocacao.reconstituir(...)` e `toEntity(Colocacao)` com todos os campos incluindo auditoria; anotar com `@Component("colabsColocacaoMapper")`
- [ ] T010 [US1] Criar `ColocacaoRepositoryImpl` em `src/main/java/cv/igrp/RH_Service/colaboradores/infrastructure/persistence/adapters/ColocacaoRepositoryImpl.java` implementando `ColocacaoRepository`; `@Repository("colabsColocacaoRepositoryImpl")`; implementar todos os métodos do port incluindo `fecharColocacaoAtual` que delega para `@Modifying` do JPA repository; `findAllByFuncionarioId` aplica filtro `isCurrent` quando fornecido
- [ ] T011 [P] [US1] Criar `RegistarColocacaoCommand` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/RegistarColocacaoCommand.java` com `@Getter @RequiredArgsConstructor implements Command`: campos String funcionarioId, UUID unitId, UUID jobId, LocalDate startDate, String assignmentType, String notes
- [ ] T012 [P] [US1] Criar DTOs em `src/main/java/cv/igrp/RH_Service/colaboradores/application/dto/`: `RegistarColocacaoRequest.java` (`@Data`: UUID unitId, UUID jobId, LocalDate startDate, String assignmentType, String notes); `ColocacaoResponse.java` (`@Data @NoArgsConstructor @AllArgsConstructor`: String id, String funcionarioId, UUID unitId, UUID jobId, LocalDate startDate, LocalDate endDate, Boolean isCurrent, String assignmentType, String notes); `WrapperListaColocacaoDTO.java` (`@Data @NoArgsConstructor @AllArgsConstructor`: List<ColocacaoResponse> data, int total)
- [ ] T013 [US1] Criar `RegistarColocacaoCommandHandler` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/RegistarColocacaoCommandHandler.java` com `@Component("colabsRegistarColocacaoCommandHandler") @RequiredArgsConstructor @IgrpCommandHandler`; injectar: FuncionarioRepository, ColocacaoRepository, e para validação cross-BC injectar directamente os Spring Data JPA repositories de `OrganizationalUnitEntity` e `CargoEntity` (verificar nomes reais dos beans no contexto Spring antes de injectar); lógica completa em ordem: (1) validar funcionário existe — 404 se não encontrado [FR-004], (2) validar funcionário está ATIVO — 400 se `Estado.INATIVO` [FR-017], (3) validar unitId existe se fornecido — 404 se não encontrado [FR-005], (4) validar jobId existe se fornecido — 404 se não encontrado [FR-006], (5) validar startDate não é futuro — 400 se `startDate.isAfter(LocalDate.now())` [FR-007], (6) se `assignmentType == TipoAfectacao.INICIAL` e `colocacaoRepository.existsByFuncionarioId(funcionarioId)` → 400 "Tipo INICIAL apenas permitido na primeira colocação do funcionário" [FR-016], (7) chamar `colocacaoRepository.fecharColocacaoAtual(funcionarioId, LocalDate.now())`, (8) criar e salvar nova Colocacao, (9) retornar ResponseEntity.status(201).body(ColocacaoResponse)
- [ ] T014 [US1] Criar `ColocacaoController` em `src/main/java/cv/igrp/RH_Service/colaboradores/interfaces/rest/ColocacaoController.java` com `@IgrpController @RestController("colabsColocacaoController") @RequestMapping("api/v1/rh/funcionarios/{funcionarioId}/colocacoes") @Tag(name="Colocações")`; implementar apenas o endpoint `@PostMapping` que chama `commandBus.send(new RegistarColocacaoCommand(...))`

**Checkpoint**: POST /colocacoes funcional — US1 completamente testável.

---

## Phase 4: User Story 2 — Consultar Colocação Actual e Histórico (P2)

**Goal**: Gestor consulta onde o funcionário está actualmente e o histórico completo.

**Independent Test**: `GET /colocacoes/atual` devolve colocação com `isCurrent=true`; `GET /colocacoes` devolve lista ordenada.

- [ ] T015 [P] [US2] Criar `GetColocacoesByFuncionarioQuery` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/GetColocacoesByFuncionarioQuery.java` com `@Getter @RequiredArgsConstructor implements Query`: campos String funcionarioId, Boolean isCurrent
- [ ] T016 [P] [US2] Criar `GetColocacaoAtualQuery` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/GetColocacaoAtualQuery.java` com `@Getter @RequiredArgsConstructor implements Query`: campo String funcionarioId
- [ ] T017 [P] [US2] Criar `GetColocacaoByIdQuery` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/GetColocacaoByIdQuery.java` com `@Getter @RequiredArgsConstructor implements Query`: campos String funcionarioId, String colocacaoId
- [ ] T018 [US2] Criar `GetColocacoesByFuncionarioQueryHandler` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/GetColocacoesByFuncionarioQueryHandler.java`; `@Component("colabsGetColocacoesByFuncionarioQueryHandler")`; validar funcionário (404); criar ColocacaoFilter via setters com isCurrent do query; chamar `findAllByFuncionarioId`; mapear para WrapperListaColocacaoDTO; retornar ResponseEntity.ok(...)
- [ ] T019 [US2] Criar `GetColocacaoAtualQueryHandler` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/GetColocacaoAtualQueryHandler.java`; `@Component("colabsGetColocacaoAtualQueryHandler")`; validar funcionário (404); chamar `findCurrentByFuncionarioId`; 404 se não encontrado; mapear para ColocacaoResponse; retornar ResponseEntity.ok(...)
- [ ] T020 [US2] Criar `GetColocacaoByIdQueryHandler` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/GetColocacaoByIdQueryHandler.java`; `@Component("colabsGetColocacaoByIdQueryHandler")`; findById (404 se não existe); verificar que `colocacao.getFuncionarioId().getValor().toString().equals(query.getFuncionarioId())` (404 se não pertence); 404 se `isActive=false`; retornar ResponseEntity.ok(ColocacaoResponse)
- [ ] T021 [US2] Adicionar ao `ColocacaoController`: `@GetMapping` (lista, com `@RequestParam(required=false) Boolean isCurrent`), `@GetMapping("atual")`, `@GetMapping("{colocacaoId}")` — todos delegam para queryBus

**Checkpoint**: GET /colocacoes, /colocacoes/atual, /colocacoes/{id} funcionais — US2 testável.

---

## Phase 5: User Story 3 — Corrigir Dados de Colocação (P3)

**Goal**: Gestor corrige observações ou datas sem criar nova colocação.

**Independent Test**: `PUT /colocacoes/{id}` actualiza `notes` sem alterar `isCurrent`.

- [ ] T022 [P] [US3] Criar `AtualizarColocacaoCommand` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/AtualizarColocacaoCommand.java` com campos: String funcionarioId, String colocacaoId, LocalDate endDate, String notes; e `AtualizarColocacaoRequest.java` em dto/ com `@Data`: LocalDate endDate, String notes
- [ ] T023 [US3] Criar `AtualizarColocacaoCommandHandler` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/AtualizarColocacaoCommandHandler.java`; `@Component("colabsAtualizarColocacaoCommandHandler")`; findById (404); verificar ownership (404); validar endDate >= startDate se fornecido (400); chamar `colocacao.atualizar(endDate, notes)`; save; retornar ResponseEntity.ok(ColocacaoResponse)
- [ ] T024 [US3] Adicionar ao `ColocacaoController`: `@PutMapping("{colocacaoId}")` delegando para commandBus com AtualizarColocacaoCommand

**Checkpoint**: PUT /colocacoes/{id} funcional — US3 testável.

---

## Phase 6: User Story 4 — Soft Delete de Colocação Inactiva (P4)

**Goal**: Gestor remove logicamente colocações inactivas; sistema recusa eliminar a colocação actual.

**Independent Test**: DELETE numa colocação inactiva retorna 200; DELETE na colocação actual retorna 400.

- [ ] T025 [P] [US4] Criar `DesativarColocacaoCommand` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/DesativarColocacaoCommand.java` com campos: String funcionarioId, String colocacaoId
- [ ] T026 [US4] Criar `DesativarColocacaoCommandHandler` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/DesativarColocacaoCommandHandler.java`; `@Component("colabsDesativarColocacaoCommandHandler")`; findById (404); verificar ownership (404); se `Boolean.TRUE.equals(colocacao.getIsCurrent())` lançar badRequest (400) com "Não é possível remover a colocação actual"; se já inactivo retornar ok idempotente; chamar `colocacao.desativar()`; save; retornar ResponseEntity.ok(Map.of("message", "Colocação removida com sucesso"))
- [ ] T027 [US4] Adicionar ao `ColocacaoController`: `@DeleteMapping("{colocacaoId}")` delegando para commandBus com DesativarColocacaoCommand

**Checkpoint**: DELETE /colocacoes/{id} funcional — US4 testável.

---

## Phase 7: User Story 5 — Integração Automática com Mobilidade (P5)

**Goal**: Aprovação de LicencaMobilidade cria automaticamente uma Colocacao(MOBILIDADE).

**Independent Test**: Chamar o endpoint de activar licença cria colocação MOBILIDADE visível em GET /colocacoes/atual.

- [ ] T028 [US5] Modificar `AtivarLicencaMobilidadeCommandHandler` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/AtivarLicencaMobilidadeCommandHandler.java`: injectar `ColocacaoRepository`; após `licenca.ativar()` e `licencaRepository.save(licenca)`, chamar `colocacaoRepository.fecharColocacaoAtual(licenca.getFuncionarioId(), LocalDate.now())` e criar e salvar `Colocacao.criar(licenca.getFuncionarioId(), null, null, LocalDate.now(), TipoAfectacao.MOBILIDADE, "Mobilidade: " + licenca.getEntidadeDestino())` — usar `LocalDate.now()` (data de aprovação) e não `licenca.getDataInicio()` (data de início da licença); unit_id=null (entidade destino pode ser externa)

**Checkpoint**: Activar licença cria colocação MOBILIDADE — US5 testável.

---

## Phase 8: Polish & Validação

**Purpose**: Build e testes manuais de todos os cenários.

- [ ] T029 Executar `mvn -B -DskipTests clean package` e confirmar BUILD SUCCESS
- [ ] T030 Executar os 12 cenários de smoke test do `specs/007-colocacoes-funcionario/quickstart.md` contra o serviço em execução e confirmar todos passam
- [ ] T031 Marcar todas as tasks como [X] em tasks.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sem dependências — pode começar imediatamente
- **Foundational (Phase 2)**: Depende do Setup — **bloqueia todas as user stories**
- **US1 (Phase 3)**: Depende do Foundational — bloqueia US2, US3, US4
- **US2 (Phase 4)**: Depende de US1 (reutiliza ColocacaoRepositoryImpl e ColocacaoMapper)
- **US3 (Phase 5)**: Depende de US1 (infra partilhada); independente de US2
- **US4 (Phase 6)**: Depende de US1 (infra partilhada); independente de US2/US3
- **US5 (Phase 7)**: Depende de US1 (ColocacaoRepository); independente de US2/US3/US4
- **Polish (Phase 8)**: Depende de todas as user stories

### User Story Dependencies

- **US1 (P1)**: Entidade, mapper, adapter, command handler, controller POST
- **US2 (P2)**: Queries + handlers + GET endpoints — reutiliza infra de US1
- **US3 (P3)**: AtualizarCommand + handler + PUT endpoint — reutiliza infra de US1
- **US4 (P4)**: DesativarCommand + handler + DELETE endpoint — reutiliza infra de US1
- **US5 (P5)**: Modificação de handler existente — requer ColocacaoRepository de US1

### Parallel Opportunities por User Story

**Phase 3 (US1)**:
```
Paralelo: T007 (ColocacaoEntity) + T008 (JPA Repository) + T011 (Command) + T012 (DTOs)
Sequencial depois: T009 (Mapper) → T010 (RepositoryImpl) → T013 (CommandHandler) → T014 (Controller POST)
```

**Phase 4 (US2)**:
```
Paralelo: T015 + T016 + T017 (três Queries)
Sequencial depois: T018 → T019 → T020 → T021 (handlers + controller)
```

---

## Implementation Strategy

### MVP First (US1)

1. Completar Phase 1 (Setup)
2. Completar Phase 2 (Foundational)
3. Completar Phase 3 (US1 — POST /colocacoes)
4. **VALIDAR**: POST cria colocação, anterior é fechada automaticamente
5. Continuar com US2 → US3 → US4 → US5

### Incremental Delivery

1. Setup + Foundational → Base de dados e domínio prontos
2. US1 → POST /colocacoes funcional (MVP!)
3. US2 → GET endpoints funcionais
4. US3 → PUT (correcção) funcional
5. US4 → DELETE (soft delete) funcional
6. US5 → Integração mobilidade funcional

---

## Notes

- `@Entity(name="ColabsColocacaoEntity")` obrigatório — evita conflito Hibernate
- `fecharColocacaoAtual` usa `@Modifying @Query` JPQL — executar na mesma transacção que o `save` da nova colocação (via `@Transactional` no handler)
- Para validação cross-BC (unit_id, job_id): injectar directamente os Spring Data JPA repositories das entidades — não criar domain ports completos para estrutura/cargo
- `unit_id` nullable: colocação MOBILIDADE pode ter unit_id=null (mobilidade para entidade externa)
- Soft delete idempotente: se `isActive` já for false, retornar ok sem erro
