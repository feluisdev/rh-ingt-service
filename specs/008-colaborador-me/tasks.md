# Tasks: Meu Perfil — Área Reservada do Colaborador

**Input**: Design documents from `specs/008-colaborador-me/`  
**Prerequisites**: plan.md ✅ · spec.md ✅ · research.md ✅ · data-model.md ✅ · contracts/ ✅ · quickstart.md ✅

**Tests**: Não solicitados — apenas smoke tests manuais na fase de polish.

**Organization**: Tarefas agrupadas por user story para implementação e teste independentes.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode correr em paralelo (ficheiros diferentes, sem dependências incompletas)
- **[Story]**: User story correspondente (US1–US4)

---

## Phase 1: Setup

**Purpose**: Migração da base de dados — única tabela nova do módulo.

- [X] T001 Criar migração Flyway `V29__create_iam_user_profile.sql` em `src/main/resources/db/migration/V29__create_iam_user_profile.sql` com tabela `t_iam_user_profile` (campos: id UUID PK, sub VARCHAR(255) UNIQUE NOT NULL, username VARCHAR(255) UNIQUE NOT NULL, email VARCHAR(255) UNIQUE, first_name VARCHAR(100), last_name VARCHAR(100), full_name VARCHAR(255), funcionario_id UUID FK→t_funcionario NULLABLE, auditoria), índices em sub/email/funcionario_id

---

## Phase 2: Foundational (Infra IAM + Employee Resolver — Bloqueia Todos os US)

**Purpose**: Infra transversal de resolução de identidade reutilizada por todos os handlers `/me`.

**⚠️ CRÍTICO**: Nenhuma user story pode começar antes desta fase estar completa.

- [X] T002 [P] Criar `IAMUserProfileEntity.java` em `src/main/java/cv/igrp/RH_Service/shared/infrastructure/persistence/entity/IAMUserProfileEntity.java` com `@Entity @Audited @Table(name="t_iam_user_profile")` extends AuditEntity; campos: id (UUID @Id), sub (String unique not null), username (String unique not null), email (String unique), firstName, lastName, fullName (String), funcionarioId (UUID nullable FK→t_funcionario)
- [X] T003 [P] Criar `IAMUserProfileEntityRepository.java` em `src/main/java/cv/igrp/RH_Service/shared/infrastructure/persistence/repository/IAMUserProfileEntityRepository.java` estendendo `JpaRepository<IAMUserProfileEntity, UUID>` com métodos: `Optional<IAMUserProfileEntity> findBySub(String sub)`, `Optional<IAMUserProfileEntity> findByEmail(String email)`
- [X] T004 Criar `IAMUserProfileSyncFilter.java` em `src/main/java/cv/igrp/RH_Service/shared/infrastructure/security/IAMUserProfileSyncFilter.java` portado do projecto `igrp_platform_process_manager_studio`: extends `OncePerRequestFilter`; ao receber `JwtAuthenticationToken` extrai sub/email/preferred_username/given_name/family_name; se não existe registo: cria com `funcionario_id` resolvido via lookup `t_funcionario.email = jwt.email`; se existe: actualiza apenas se claims mudaram; continua chain mesmo em erro (warn log)
- [X] T005 [P] Criar interface `CurrentEmployeeResolver.java` em `src/main/java/cv/igrp/RH_Service/shared/domain/service/CurrentEmployeeResolver.java` com método `FuncionarioId resolve()` que lança `IgrpResponseStatusException` 404 se não encontrado
- [X] T006 Criar `CurrentEmployeeResolverImpl.java` em `src/main/java/cv/igrp/RH_Service/shared/infrastructure/security/CurrentEmployeeResolverImpl.java` com `@Component("currentEmployeeResolver")`; lógica: [dev] lê header `X-Employee-Id` → `FuncionarioId.from(uuid)`, se ausente usa primeiro funcionário activo da BD como fallback; [prod] obtém `sub` do JWT via `SecurityContextHolder` → `IAMUserProfileEntityRepository.findBySub(sub)` → verifica `funcionario_id != null` → `FuncionarioId.from(entity.getFuncionarioId())`; usa `SecurityContextHelper.isNonProduction()` para distinguir ambientes
- [X] T007 Actualizar `ApplicationAuditorAware.java` em `src/main/java/cv/igrp/RH_Service/shared/config/ApplicationAuditorAware.java` para priorizar: (1) `sub` claim do JWT se `JwtAuthenticationToken`; (2) `authentication.getName()`; (3) `"system"` — seguindo o padrão do projecto de referência `igrp_platform_process_manager_studio`
- [X] T008 Registar `IAMUserProfileSyncFilter` em `SecurityConfig.java` (ou equivalente em `src/main/java/cv/igrp/RH_Service/shared/config/`) antes do `AuthorizationFilter`; garantir que endpoints `api/v1/rh/me/**` requerem `ROLE_FUNCIONARIO` em produção

**Checkpoint**: `CurrentEmployeeResolver` funcional — todos os handlers `/me` podem injectar e usar `resolver.resolve()`.

---

## Phase 3: User Story 1 — Perfil do Colaborador (P1) 🎯 MVP

**Goal**: Colaborador autentica-se e consulta o seu perfil completo.

**Independent Test**: `GET /me/profile` devolve 200 com nome, NIF, unidade actual, cargo, carreira e data de admissão; colaborador inactivo recebe 403; UUID sem funcionário associado recebe 404.

- [X] T009 [P] [US1] Criar `MeProfileResponse.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/dto/MeProfileResponse.java` com `@Getter @Setter @NoArgsConstructor`: campos String id, fullName, nif, email, phone, workerState, LocalDate admissionDate; objectos aninhados `UnitRef(id, name)` currentUnit, `JobRef(id, name)` currentJob, `FunctionRef(id, name)` currentFunction, `CareerRef(id, name)` career, `CategoryRef(id, name)` category, `GradeRef(id, gradeNumber)` grade — classes estáticas internas `@Getter @Setter @NoArgsConstructor`
- [X] T010 [P] [US1] Criar `GetMeProfileQuery.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/GetMeProfileQuery.java` com `@Getter @RequiredArgsConstructor implements Query` sem campos (o funcionarioId é resolvido internamente pelo handler via CurrentEmployeeResolver)
- [X] T011 [US1] Criar `GetMeProfileQueryHandler.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/GetMeProfileQueryHandler.java` com `@Component("colabsGetMeProfileQueryHandler") @RequiredArgsConstructor`; injectar: `CurrentEmployeeResolver`, `FuncionarioRepository`, `ColocacaoRepository`, `EnquadramentoRepository` (método `findCurrentByFuncionarioId()` — repositório real do codebase); lógica: (1) `funcionarioId = resolver.resolve()` (lança 404 se não encontrado), (2) `funcionario = funcionarioRepository.findById(funcionarioId)` (lança 404), (3) verificar `!Boolean.TRUE.equals(funcionario.getIsActive())` → lançar 403, (4) colocação actual via `colocacaoRepository.findCurrentByFuncionarioId(funcionarioId)` (pode ser null), (5) enquadramento actual via `enquadramentoRepository.findCurrentByFuncionarioId(funcionarioId)` (pode ser null), (6) montar e devolver `MeProfileResponse`
- [X] T012 [US1] Criar manifest `.igrpstudio/me/MeController.json` e gerar `MeController.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/interfaces/rest/MeController.java` via skill `igrp-spring-generator` com `@RequestMapping("api/v1/rh/me")` `@RestController("colabsMeController")`; incluir TODOS os 9 endpoints do contrato (GET /me/profile, GET/POST /me/leave-requests, PUT /me/leave-requests/{id}/cancel, GET /me/leave-balances, GET/POST /me/leaves-mobilities, GET /me/documents, GET /me/documents/{id}/download) — handlers dos US2-US4 serão adicionados progressivamente

**Checkpoint**: `GET /me/profile` funcional — US1 testável com cenários 1, 2, 3 do quickstart.

---

## Phase 4: User Story 2 — Gerir as Minhas Ausências (P2)

**Goal**: Colaborador lista pedidos, consulta saldos, submete novo pedido e cancela pendente.

**Independent Test**: `GET /me/leave-requests` lista apenas pedidos do próprio; `POST /me/leave-requests` cria em PENDING; `PUT /me/leave-requests/{id}/cancel` cancela PENDING; `GET /me/leave-balances` devolve saldos do ano corrente.

- [X] T013 [P] [US2] Criar `SelfServiceCriarPedidoAusenciaRequest.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/dto/SelfServiceCriarPedidoAusenciaRequest.java` com `@Getter @Setter @NoArgsConstructor`: UUID leaveTypeId (`@NotNull`), LocalDate startDate (`@NotNull`), LocalDate endDate (`@NotNull`), String notes
- [X] T014 [P] [US2] Criar `SelfServiceCriarPedidoAusenciaCommand.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/SelfServiceCriarPedidoAusenciaCommand.java` com `@Getter @RequiredArgsConstructor implements Command`: campos `FuncionarioId funcionarioId`, `SelfServiceCriarPedidoAusenciaRequest request`
- [X] T015 [P] [US2] Criar `SelfServiceCancelarPedidoAusenciaCommand.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/SelfServiceCancelarPedidoAusenciaCommand.java` com `@Getter @RequiredArgsConstructor implements Command`: campos `FuncionarioId funcionarioId`, `String pedidoId`
- [X] T016 [P] [US2] Criar `GetMeLeaveRequestsQuery.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/GetMeLeaveRequestsQuery.java` com `@Getter @RequiredArgsConstructor implements Query`: campos `String status`, `String leaveTypeId`, `Integer year`
- [X] T017 [P] [US2] Criar `GetMeLeaveBalancesQuery.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/GetMeLeaveBalancesQuery.java` com `@Getter @RequiredArgsConstructor implements Query` sem campos
- [X] T018 [US2] Criar `SelfServiceCriarPedidoAusenciaCommandHandler.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/SelfServiceCriarPedidoAusenciaCommandHandler.java` com `@Component("colabsSelfServiceCriarPedidoAusenciaCommandHandler") @RequiredArgsConstructor`; injectar `CurrentEmployeeResolver`, `FuncionarioRepository`, `PedidoAusenciaRepository`, `SaldoAusenciaRepository`, `LeaveTypeRepository`; resolver funcionário (404/403); aplicar mesmas validações de negócio que `CreatePedidoAusenciaCommandHandler` (sobreposição de datas, saldo, datas válidas); criar e salvar pedido em estado PENDING; retornar 201 — [FR-014] o audit trail (created_by = sub JWT) é preenchido automaticamente pelo `ApplicationAuditorAware` actualizado em T007
- [X] T019 [US2] Criar `SelfServiceCancelarPedidoAusenciaCommandHandler.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/SelfServiceCancelarPedidoAusenciaCommandHandler.java`; injectar `CurrentEmployeeResolver`, `FuncionarioRepository`, `PedidoAusenciaRepository`; resolver funcionário (404/403); findById pedido (404 se não existe ou `pedido.getFuncionarioId() != funcionarioId`); verificar estado PENDING (400 se outro estado); cancelar e salvar; retornar 200 — [FR-014] audit trail via `ApplicationAuditorAware` (T007)
- [X] T020 [US2] Criar `GetMeLeaveRequestsQueryHandler.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/GetMeLeaveRequestsQueryHandler.java`; injectar `CurrentEmployeeResolver`, `FuncionarioRepository`, `PedidoAusenciaRepository`; resolver funcionário (404/403); chamar `findAllByFuncionarioId` com filtros de status/leaveTypeId/year; mapear para DTO e devolver wrapper
- [X] T021 [US2] Criar `GetMeLeaveBalancesQueryHandler.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/GetMeLeaveBalancesQueryHandler.java`; injectar `CurrentEmployeeResolver`, `FuncionarioRepository`, `SaldoAusenciaRepository`; resolver funcionário (404/403); chamar `findByFuncionarioIdAndYear` para o ano corrente (`LocalDate.now().getYear()`); mapear para `MeLeaveBalanceResponse` e devolver lista

**Checkpoint**: Cenários 4, 5, 6, 7, 8 do quickstart passam — US2 testável.

---

## Phase 5: User Story 3 — Licenças e Mobilidades (P3)

**Goal**: Colaborador lista as suas licenças e auto-submete quando o subtipo permite.

**Independent Test**: `GET /me/leaves-mobilities` lista apenas as do próprio; `POST /me/leaves-mobilities` cria quando `canSelfSubmit=true`; retorna 403 quando `canSelfSubmit=false`.

- [X] T022 [P] [US3] Criar `SelfServiceCriarLicencaMobilidadeRequest.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/dto/SelfServiceCriarLicencaMobilidadeRequest.java` com `@Getter @Setter @NoArgsConstructor`: UUID subtipoId (`@NotNull`), LocalDate dataInicio (`@NotNull`), LocalDate dataFim, String entidadeDestino, String observacoes
- [X] T023 [P] [US3] Criar `SelfServiceCriarLicencaMobilidadeCommand.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/SelfServiceCriarLicencaMobilidadeCommand.java` com `@Getter @RequiredArgsConstructor implements Command`: campos `FuncionarioId funcionarioId`, `SelfServiceCriarLicencaMobilidadeRequest request`
- [X] T024 [P] [US3] Criar `GetMeLeaveMobilitiesQuery.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/GetMeLeaveMobilitiesQuery.java` com `@Getter @RequiredArgsConstructor implements Query` sem campos
- [X] T025 [US3] Criar `SelfServiceCriarLicencaMobilidadeCommandHandler.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/commands/SelfServiceCriarLicencaMobilidadeCommandHandler.java`; injectar `CurrentEmployeeResolver`, `FuncionarioRepository`, `LicencaMobilidadeRepository`, `SubtipoLicencaMobilidadeRepository`; resolver funcionário (404/403); findById subtipo (404); verificar `Boolean.TRUE.equals(subtipo.getCanSelfSubmit())` → 403 se false; validar datas; criar e salvar licença em estado activo=false (pendente aprovação); retornar 201 — [FR-014] audit trail via `ApplicationAuditorAware` (T007)
- [X] T026 [US3] Criar `GetMeLeaveMobilitiesQueryHandler.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/GetMeLeaveMobilitiesQueryHandler.java`; injectar `CurrentEmployeeResolver`, `FuncionarioRepository`, `LicencaMobilidadeRepository`; resolver funcionário (404/403); chamar `findAllByFuncionarioId` ordenadas por `dataInicio` descendente; mapear para DTO e devolver

**Checkpoint**: Cenários 9 do quickstart passa — US3 testável.

---

## Phase 6: User Story 4 — Os Meus Documentos (P4)

**Goal**: Colaborador lista os seus documentos e obtém URL de download com validação de ownership.

**Independent Test**: `GET /me/documents` lista apenas documentos do próprio; `GET /me/documents/{id}/download` devolve URL para documentos próprios e 404 para documentos alheios.

- [X] T027 [P] [US4] Criar `MeDocumentResponse.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/dto/MeDocumentResponse.java` com `@Getter @Setter @NoArgsConstructor`: String id, documentTypeId, documentTypeName, fileName; LocalDateTime uploadedAt
- [X] T028 [P] [US4] Criar `GetMeDocumentsQuery.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/GetMeDocumentsQuery.java` com `@Getter @RequiredArgsConstructor implements Query`: campo `String documentTypeId` (nullable, para filtro)
- [X] T029 [P] [US4] Criar `GetMeDocumentDownloadUrlQuery.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/GetMeDocumentDownloadUrlQuery.java` com `@Getter @RequiredArgsConstructor implements Query`: campo `String documentoId`
- [X] T030 [US4] Criar `GetMeDocumentsQueryHandler.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/GetMeDocumentsQueryHandler.java`; injectar `CurrentEmployeeResolver`, `FuncionarioRepository`, `DocumentoRepository`; resolver funcionário (404/403); chamar `findAllByFuncionarioId` com filtro opcional por `documentTypeId`; mapear para `MeDocumentResponse` e devolver wrapper
- [X] T031 [US4] Criar `GetMeDocumentDownloadUrlQueryHandler.java` em `src/main/java/cv/igrp/RH_Service/colaboradores/application/queries/GetMeDocumentDownloadUrlQueryHandler.java`; injectar `CurrentEmployeeResolver`, `FuncionarioRepository`, `DocumentoRepository`, `DocumentoService` (já existe em `src/main/java/cv/igrp/RH_Service/shared/domain/service/DocumentoService.java` com método `getPresignedLink(String fileId)`); resolver funcionário (404/403); findById documento (404); verificar `documento.getFuncionarioId().equals(funcionarioId)` → 404 se não pertence; chamar `documentoService.getPresignedLink(documento.getFileKey())` para gerar URL pré-assinado; retornar response com `downloadUrl` e `expiresAt`

**Checkpoint**: Cenários 10, 11, 12 do quickstart passam — US4 testável.

---

## Phase 7: Polish & Validação

**Purpose**: Build, smoke tests e commit final.

- [X] T032 Executar `mvn -B -DskipTests compile` com Java 23 e confirmar BUILD SUCCESS sem erros de compilação
- [X] T033 Executar os 12 cenários de smoke test do `specs/008-colaborador-me/quickstart.md` contra o serviço em execução (reiniciar serviço para aplicar migração V29) — usar header `X-Employee-Id` em dev
- [X] T034 Marcar todas as tasks como [X] em tasks.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sem dependências — pode começar imediatamente
- **Foundational (Phase 2)**: Depende do Setup — **bloqueia todas as user stories**
- **US1 (Phase 3)**: Depende do Foundational — bloqueia teste dos outros US (MeController criado aqui)
- **US2 (Phase 4)**: Depende do Foundational; independente de US1 para handlers, mas usa MeController criado em US1
- **US3 (Phase 5)**: Depende do Foundational; independente de US1/US2
- **US4 (Phase 6)**: Depende do Foundational; independente de US1/US2/US3
- **Polish (Phase 7)**: Depende de todas as user stories

### Parallel Opportunities

**Phase 2 (Foundational)**:
```
Paralelo: T002 (IAMUserProfileEntity) + T003 (IAMUserProfileEntityRepository) + T005 (interface CurrentEmployeeResolver)
Sequencial: T004 (IAMUserProfileSyncFilter) → T006 (CurrentEmployeeResolverImpl) → T007 (ApplicationAuditorAware) → T008 (SecurityConfig)
```

**Phase 3 (US1)**:
```
Paralelo: T009 (MeProfileResponse) + T010 (GetMeProfileQuery)
Sequencial: T011 (GetMeProfileQueryHandler) → T012 (MeController — gerado com todos os endpoints)
```

**Phase 4 (US2)**:
```
Paralelo: T013 (Request DTO) + T014 (CriarCommand) + T015 (CancelarCommand) + T016 (LeaveRequestsQuery) + T017 (BalancesQuery)
Sequencial: T018 (CriarHandler) → T019 (CancelarHandler) → T020 (LeaveRequestsQueryHandler) → T021 (BalancesQueryHandler)
```

---

## Implementation Strategy

### MVP First (US1)

1. Completar Phase 1 (Setup — V29 migration)
2. Completar Phase 2 (Foundational — IAM sync + CurrentEmployeeResolver)
3. Completar Phase 3 (US1 — GET /me/profile)
4. **VALIDAR**: cenários 1, 2, 3 do quickstart
5. Continuar US2 → US3 → US4

### Incremental Delivery

1. Setup + Foundational → IAM sync funcional
2. US1 → GET /me/profile funcional (MVP — o mais valioso)
3. US2 → Gestão de ausências self-service
4. US3 → Licenças e mobilidades
5. US4 → Documentos com download seguro

---

## Notes

- `CurrentEmployeeResolver` é a peça central — todos os handlers `/me` injectam e chamam `resolver.resolve()` como primeiro passo
- Verificação `is_active` (403) é feita em TODOS os handlers após resolver o funcionário
- Verificação de ownership (404) é obrigatória em `cancel`, `download` e qualquer operação por ID
- `IAMUserProfileSyncFilter` deve continuar o filter chain mesmo em caso de erro (nunca bloquear o request)
- `ApplicationAuditorAware` actualizado afecta toda a auditoria do projecto — verificar que não quebra comportamento existente
- `MeController` gerado via `igrp-spring-generator` na T012 com TODOS os 9 endpoints — não editar manualmente depois
- Subtipo `canSelfSubmit` verificado no handler antes de criar licença (FR-011)
