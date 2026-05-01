# Tasks: Documentos de Funcionários — BC Colaboradores

**Input**: Design documents from `specs/006-employee-docs/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅, quickstart.md ✅

**Organização**: 4 User Stories (P1→P4). US1 (upload) é o MVP — sem upload nada mais tem valor.

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
- `infra/e/` → `infrastructure/persistence/entity/`
- `infra/r/` → `infrastructure/persistence/repository/`
- `infra/a/` → `infrastructure/persistence/adapters/`
- `infra/m/` → `infrastructure/mappers/`
- `rest/` → `interfaces/rest/`

---

## Phase 1: Setup

**Purpose**: Migração de base de dados para a tabela `t_document`.

- [ ] T001 Criar migração `src/main/resources/db/migration/V27__create_document_table.sql` — **DEFENSIVA**: `CREATE TABLE IF NOT EXISTS t_document (id UUID NOT NULL, reference_entity VARCHAR(50) NOT NULL, reference_id UUID NOT NULL, document_type_id UUID NOT NULL, file_key VARCHAR(500) NOT NULL, original_filename VARCHAR(255) NOT NULL, content_type VARCHAR(100) NOT NULL, file_size BIGINT NOT NULL, description TEXT, is_active BOOLEAN NOT NULL DEFAULT TRUE, created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL, created_by VARCHAR(255) NOT NULL, last_modified_date TIMESTAMP WITHOUT TIME ZONE, last_modified_by VARCHAR(255), CONSTRAINT pk_document PRIMARY KEY (id), CONSTRAINT fk_document_type FOREIGN KEY (document_type_id) REFERENCES t_tipo_documento(id))` + `CREATE INDEX IF NOT EXISTS idx_document_reference ON t_document (reference_entity, reference_id, is_active)` + `CREATE INDEX IF NOT EXISTS idx_document_type ON t_document (document_type_id)`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Value object, modelo de domínio, filtro e port que bloqueiam todas as user stories.

**⚠️ CRÍTICO**: Nenhuma user story pode começar antes desta fase estar completa.

- [ ] T002 [P] Criar `domain/vo/DocumentoId.java` — value object UUID seguindo o padrão `FuncionarioId.java` (gerarNovo/from(UUID)/from(String), getValor/getStringValor, equals+hashCode)
- [ ] T003 [P] Criar `domain/m/Documento.java` — modelo de domínio com campos: `DocumentoId id`, `String referenceEntity`, `FuncionarioId referenceId`, `cv.igrp.RH_Service.parametrizacoes.domain.valueobject.DocumentTypeId documentTypeId`, `String fileKey`, `String originalFilename`, `String contentType`, `long fileSize`, `String description`, `Boolean isActive`; factory `criar(...)` com `isActive=true`; factory `reconstituir(...)`; método `desativar()` que sets `isActive=false`
- [ ] T004 [P] Criar `domain/f/DocumentoFilter.java` — campos: `UUID referenceId`, `UUID documentTypeId`, `Boolean active`
- [ ] T005 Criar `domain/r/DocumentoRepository.java` — port com: `save(Documento): Documento`, `findById(DocumentoId): Optional<Documento>`, `findAllByFuncionarioId(FuncionarioId, DocumentoFilter): List<Documento>`

**Checkpoint**: Foundation pronta — todas as user stories podem começar.

---

## Phase 3: User Story 1 — Upload de Documento (Priority: P1) 🎯 MVP

**Goal**: Gestor faz upload de um ficheiro associado a um funcionário; sistema valida extensão, armazena no MinIO via `DocumentoService` e persiste metadados.

**Independent Test**: Executar cenários C1-S1 a C1-S6 do quickstart.md — verificar 201 no upload válido, 400 em extensão inválida, 404 em funcionário inexistente.

**Depends on**: Phase 2 completa; `DocumentoService` e `DocumentoFolder` já existem em `shared/`

- [ ] T006 [P] [US1] Criar `infra/e/DocumentoEntity.java` — `@Entity(name="ColabsDocumentoEntity")`, `@Table(name="t_document")`, `@Audited`, `@EntityListeners(AuditingEntityListener.class)`, extends `AuditEntity`; campos JPA: `UUID id`, `String referenceEntity`, `UUID referenceId`, `UUID documentTypeId`, `String fileKey`, `String originalFilename`, `String contentType`, `long fileSize`, `String description`, `Boolean isActive`
- [ ] T007 [P] [US1] Criar `infra/r/ColabsDocumentoEntityRepository.java` — `extends JpaRepository<DocumentoEntity, UUID>` com métodos: `findAllByReferenceEntityAndReferenceId(String, UUID): List<DocumentoEntity>`, `findAllByReferenceEntityAndReferenceIdAndIsActive(String, UUID, Boolean): List<DocumentoEntity>`, `findByIdAndReferenceEntityAndReferenceId(UUID, String, UUID): Optional<DocumentoEntity>`
- [ ] T008 [P] [US1] Criar `app/dto/DocumentoUploadResponse.java` — campos: `String id`, `String fileKey`, `String originalFilename`, `String contentType`, `long fileSize`, `String message`
- [ ] T009 [US1] Criar `infra/m/DocumentoMapper.java` — `@Component("colabsDocumentoMapper")`; métodos `toDomain(DocumentoEntity): Documento` e `toEntity(Documento): DocumentoEntity`; injectar `cv.igrp.RH_Service.parametrizacoes.domain.repository.DocumentTypeRepository` e `cv.igrp.RH_Service.colaboradores.infrastructure.mappers.TipoAusenciaMapper`-equivalente para enriquecer DocumentType no toDTO (deixar toDTO incompleto até US2)
- [ ] T010 [US1] Criar `infra/a/DocumentoRepositoryImpl.java` — `@Repository("colabsDocumentoRepositoryImpl")`; implementa `DocumentoRepository`; método `findAllByFuncionarioId` aplica filtros em memória (documentTypeId, active) sobre `findAllByReferenceEntityAndReferenceId`
- [ ] T011 [P] [US1] Criar `app/cmd/UploadDocumentoCommand.java` — campos: `String funcionarioId`, `UUID documentTypeId`, `org.springframework.web.multipart.MultipartFile file`, `String description` (nullable)
- [ ] T012 [US1] Criar `app/cmd/UploadDocumentoCommandHandler.java` — `@Component("colabsUploadDocumentoCommandHandler")`; injeta `FuncionarioRepository`, `cv.igrp.RH_Service.parametrizacoes.domain.repository.DocumentTypeRepository`, `DocumentoRepository`, `cv.igrp.RH_Service.shared.domain.service.DocumentoService`; lógica: (1) valida que funcionário existe (404); (2) carrega DocumentType — 404 se não existe, 400 se `!active`; (3) extrai extensão do `originalFilename`, verifica contra `tipo.getAllowedExtensions()` (split por vírgula, case-insensitive) → 400 com mensagem "Extensão não permitida. Aceites: {lista}"; (4) chama `documentoService.save(DocumentoFolder.FUNCIONARIO, file)` e extrai `fileKey` de `FileResponseDTO.getFileId()`; (5) persiste `Documento.criar(...)` com `referenceEntity="FUNCIONARIO"` e `referenceId=FuncionarioId.from(funcionarioId)`; (6) retorna `ResponseEntity.status(201).body(DocumentoUploadResponse{...})`
- [ ] T013 [US1] Criar `rest/DocumentoController.java` — `@IgrpController`, `@RestController("colabsDocumentoController")`, `@RequestMapping("api/v1/rh/funcionarios/{funcionarioId}/documentos")`; endpoint `POST /` com `@RequestParam MultipartFile file`, `@RequestParam UUID documentTypeId`, `@RequestParam(required=false) String description`; validar `file.getSize() > 10*1024*1024 → 400`; delegar para `commandBus.send(new UploadDocumentoCommand(...))`

**Checkpoint**: US1 completa — upload funcional, cenários C1-S1 a C1-S6 devem passar.

---

## Phase 4: User Story 2 — Listagem e Metadados (Priority: P2)

**Goal**: Gestor lista documentos de um funcionário com filtros opcionais e consulta metadados de um documento individual.

**Independent Test**: Executar cenários C1-S4 e C1-S5 do quickstart.md — listar documentos após upload e filtrar por tipo.

**Depends on**: US1 completa (DocumentoEntity, Mapper, RepositoryImpl)

- [ ] T014 [P] [US2] Criar `app/dto/DocumentoResponse.java` — campos: `String id`, `String funcionarioId`, `String documentTypeId`, `Object documentType` (nested — usar `cv.igrp.RH_Service.parametrizacoes.application.dto.DocumentTypeResponseDTO` ou similar), `String originalFilename`, `String contentType`, `long fileSize`, `String description`, `Boolean isActive`
- [ ] T015 [P] [US2] Criar `app/dto/WrapperListaDocumentoDTO.java` — campos: `List<DocumentoResponse> content`, `int totalElements`
- [ ] T016 [US2] Completar `infra/m/DocumentoMapper.java` — adicionar método `toDTO(Documento): DocumentoResponse` injectando `cv.igrp.RH_Service.parametrizacoes.domain.repository.DocumentTypeRepository` para enriquecer campo `documentType` com os dados do catálogo
- [ ] T017 [P] [US2] Criar `app/qry/GetDocumentosByFuncionarioQuery.java` — campos: `String funcionarioId`, `UUID documentTypeId` (nullable), `Boolean active` (nullable)
- [ ] T018 [US2] Criar `app/qry/GetDocumentosByFuncionarioQueryHandler.java` — `@Component("colabsGetDocumentosByFuncionarioQueryHandler")`; injeta `DocumentoRepository`, `DocumentoMapper`; constrói `DocumentoFilter`, chama `findAllByFuncionarioId`, mapeia para DTO, retorna `WrapperListaDocumentoDTO`
- [ ] T019 [P] [US2] Criar `app/qry/GetDocumentoByIdQuery.java` — campos: `String funcionarioId`, `String documentoId`
- [ ] T020 [US2] Criar `app/qry/GetDocumentoByIdQueryHandler.java` — `@Component("colabsGetDocumentoByIdQueryHandler")`; usa `ColabsDocumentoEntityRepository.findByIdAndReferenceEntityAndReferenceId` para garantir que o documento pertence ao funcionário → 404 se não encontrado ou não pertence
- [ ] T021 [US2] Adicionar endpoints GET ao `rest/DocumentoController.java` — `GET /` com params opcionais `documentTypeId` e `active`; `GET /{documentoId}` retorna `DocumentoResponse`

**Checkpoint**: US2 completa — listagem e metadados funcionais.

---

## Phase 5: User Story 3 — Download via URL Pré-assinada (Priority: P3)

**Goal**: Gestor obtém URL temporária para download directo do ficheiro no MinIO.

**Independent Test**: Executar cenários C2-S1 e C2-S2 do quickstart.md — URL válida para documento activo, 404 para inactivo.

**Depends on**: US1 completa (fileKey persistido), US2 completa (verificação de pertença)

- [ ] T022 [P] [US3] Criar `app/dto/DocumentoDownloadResponse.java` — campos: `String url`, `long expiresIn`
- [ ] T023 [P] [US3] Criar `app/qry/GetDocumentoDownloadUrlQuery.java` — campos: `String funcionarioId`, `String documentoId`
- [ ] T024 [US3] Criar `app/qry/GetDocumentoDownloadUrlQueryHandler.java` — `@Component("colabsGetDocumentoDownloadUrlQueryHandler")`; injeta `ColabsDocumentoEntityRepository` e `cv.igrp.RH_Service.shared.domain.service.DocumentoService`; (1) busca documento por id+referenceEntity+referenceId → 404 se não existe; (2) verifica `isActive` → 404 se false; (3) chama `documentoService.getPresignedLink(documento.getFileKey())` e extrai URL de `FileUrlDTO`; (4) lê `MINIO_PRESIGNED_URL_EXPIRATION_TIME` de `${igrp.minio.url-expiration-time:3600}` para popular `expiresIn`; retorna `DocumentoDownloadResponse`
- [ ] T025 [US3] Adicionar endpoint GET `/{documentoId}/download` ao `rest/DocumentoController.java` — retorna `DocumentoDownloadResponse`

**Checkpoint**: US3 completa — download funcional, cenário C2-S1 deve retornar URL acessível.

---

## Phase 6: User Story 4 — Soft Delete (Priority: P4)

**Goal**: Gestor desactiva logicamente um documento; ficheiro permanece no MinIO mas desaparece das listagens activas.

**Independent Test**: Executar cenários C3-S1 a C3-S4 do quickstart.md — DELETE retorna 200, documento não aparece em `active=true`, download retorna 404.

**Depends on**: US1 completa (Documento.desativar(), DocumentoRepositoryImpl.save())

- [ ] T026 [P] [US4] Criar `app/cmd/DesativarDocumentoCommand.java` — campos: `String funcionarioId`, `String documentoId`
- [ ] T027 [US4] Criar `app/cmd/DesativarDocumentoCommandHandler.java` — `@Component("colabsDesativarDocumentoCommandHandler")`; busca documento por id+referenceEntity+referenceId → 404 se não existe; chama `documento.desativar()`; persiste; retorna `ResponseEntity.ok(Map.of("message","Documento desactivado com sucesso"))` — **idempotente**: se já `isActive=false`, executa igualmente sem erro
- [ ] T028 [US4] Adicionar endpoint `DELETE /{documentoId}` ao `rest/DocumentoController.java`

**Checkpoint**: US4 completa — cenários C3-S1 a C3-S4 devem passar.

---

## Phase 7: Polish & Validação Final

- [ ] T029 Compilar: `mvn -B -DskipTests clean package -Dmaven.compiler.release=21` — confirmar BUILD SUCCESS
- [ ] T030 Executar testes: `mvn test -Dmaven.compiler.release=21` — confirmar 0 falhas
- [ ] T031 Smoke tests: iniciar aplicação e executar todos os cenários quickstart C1-C3 (10 passos); verificar que migração V27 aplicou (`\d t_document` no psql) e que índices existem

---

## Dependencies & Execution Order

### Dependências de fase

- **Phase 1 (Setup)**: Sem dependências — começar imediatamente
- **Phase 2 (Foundational)**: Depende de Phase 1 — bloqueia todas as user stories
- **Phase 3 (US1)**: Depende de Phase 2
- **Phase 4 (US2)**: Depende de US1 (entity, mapper, repo impl)
- **Phase 5 (US3)**: Depende de US1 (fileKey) e US2 (verificação de pertença)
- **Phase 6 (US4)**: Depende de US1 (Documento.desativar, save)
- **Phase 7 (Polish)**: Depende de todas as user stories desejadas

### Dentro de cada story

- Entity + Spring Data repo + DTOs: paralelos
- Mapper (toEntity/toDomain): após entity
- RepositoryImpl: após entity + Spring Data repo + mapper
- Command/Query + Handler: após repositoryImpl e DTOs
- Controller endpoint: após command/query e handlers

### Parallel Example: US1

```text
Paralelos (ficheiros diferentes):
  T006 DocumentoEntity
  T007 ColabsDocumentoEntityRepository
  T008 DocumentoUploadResponse DTO
  T011 UploadDocumentoCommand

Após T006 + T007 + T002 (DocumentoId) + T003 (Documento):
  T009 DocumentoMapper (toDomain/toEntity)

Após T009:
  T010 DocumentoRepositoryImpl

Após T010 + T011 + T008:
  T012 UploadDocumentoCommandHandler

Após T012:
  T013 DocumentoController (POST /)
```

---

## Implementation Strategy

### MVP (US1 apenas)

1. Phase 1: Migração V27
2. Phase 2: Value object + domain model + filter + port
3. Phase 3: US1 (upload completo)
4. **PARAR e VALIDAR**: cenários C1-S1 a C1-S6
5. Demonstrar upload funcional

### Entrega incremental

1. Setup + Foundational → base pronta
2. US1 → upload → demo C1
3. US2 → listagem → demo C1-S4, C1-S5
4. US3 → download → demo C2
5. US4 → soft delete → demo C3
6. Validação final de todos os cenários

---

## Notes

- `DocumentoService` já existe em `shared/domain/service/` — **não criar nova classe MinIO**
- `DocumentoFolder.FUNCIONARIO` já existe em `shared/application/constants/` — usar directamente
- `DocumentTypeRepository` pertence a `parametrizacoes/` — injectar o port existente nos handlers
- `@Entity(name="ColabsDocumentoEntity")` obrigatório — evita conflito Hibernate
- `Colabs` prefix em todas as interfaces Spring Data JPA
- `@Repository("colabsDocumentoRepositoryImpl")` e `@Component("colabsXHandler")` — bean names explícitos
- `IgrpResponseStatusException` — sem método `unprocessableEntity()`; usar `of(HttpStatus.UNPROCESSABLE_ENTITY, msg)`
- Verificar nome real da tabela de DocumentType antes de criar FK — pode ser `t_tipo_documento` ou `t_document_type` (confirmar via `\d` no psql)
