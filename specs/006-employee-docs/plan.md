# Implementation Plan: Documentos de Funcionários

**Branch**: `006-employee-docs` | **Date**: 2026-05-01 | **Spec**: [spec.md](spec.md)

## Summary

Implementar o sub-módulo de Documentos dentro do BC `colaboradores/`, permitindo upload, listagem, consulta de metadados, download via URL pré-assinada e soft delete de documentos associados a funcionários. Armazenamento em MinIO via `DocumentoService` já existente no módulo `shared/`. Modelo polimórfico com `reference_entity`/`reference_id` para suportar futuras entidades proprietárias.

## Technical Context

**Language/Version**: Java 23
**Primary Dependencies**: Spring Boot 3.5.3, Hibernate Envers, IGRP framework (CommandBus/QueryBus), `cv.igrp.framework.filemanager:minio:0.1.0-beta.1`
**Storage**: PostgreSQL 17 (metadados) + MinIO/S3 (ficheiros)
**Testing**: `mvn test` (JUnit 5 + Spring Boot Test)
**Target Platform**: Servidor Linux (Docker), desenvolvimento local com docker-compose
**Project Type**: Microserviço REST (Spring Boot)
**Performance Goals**: Upload < 5 s para ficheiros até 10 MB; listagem < 1 s para até 50 documentos
**Constraints**: Ficheiros máx 10 MB; URL pré-assinada expira em `MINIO_PRESIGNED_URL_EXPIRATION_TIME` segundos (padrão 3600)
**Scale/Scope**: ~50 documentos por funcionário; sem paginação na v1

## Constitution Check

| Princípio | Estado | Observação |
|-----------|--------|------------|
| I. Arquitectura Hexagonal | ✅ PASS | domain → application → infrastructure; DocumentoService injectado nos handlers (camada application), não no domain |
| II. CQRS | ✅ PASS | Upload = Command; Listagem/Download = Query; lógica exclusivamente nos handlers |
| III. Controllers gerados | ✅ PASS | Controller segue padrão colaboradores/ (não IGRP Studio neste módulo) |
| IV. Envers | ✅ PASS | `DocumentoEntity` extends `AuditEntity` + `@Audited` |
| V. Segurança por perfil | ✅ PASS | Sem override — segue o comportamento existente do projecto |

## Project Structure

### Documentação

```text
specs/006-employee-docs/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/documentos.md
├── checklists/requirements.md
└── tasks.md  (gerado por /speckit-tasks)
```

### Código — Migração

```text
src/main/resources/db/migration/
└── V27__create_document_table.sql
```

### Código — BC colaboradores/

```text
src/main/java/cv/igrp/RH_Service/colaboradores/
├── domain/
│   ├── models/Documento.java
│   ├── valueobject/DocumentoId.java
│   ├── repository/DocumentoRepository.java
│   └── filter/DocumentoFilter.java
├── application/
│   ├── commands/
│   │   ├── UploadDocumentoCommand.java + Handler
│   │   └── DesativarDocumentoCommand.java + Handler
│   ├── queries/
│   │   ├── GetDocumentoByIdQuery.java + Handler
│   │   ├── GetDocumentosByFuncionarioQuery.java + Handler
│   │   └── GetDocumentoDownloadUrlQuery.java + Handler
│   └── dto/
│       ├── DocumentoResponse.java
│       ├── DocumentoUploadResponse.java
│       ├── DocumentoDownloadResponse.java
│       └── WrapperListaDocumentoDTO.java
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/DocumentoEntity.java
│   │   ├── repository/ColabsDocumentoEntityRepository.java
│   │   └── adapters/DocumentoRepositoryImpl.java
│   └── mappers/DocumentoMapper.java
└── interfaces/rest/DocumentoController.java
```

## Fases de Implementação

| Fase | Conteúdo | US |
|------|----------|-----|
| Setup | V27 migration — `t_document` defensiva | — |
| Foundational | DocumentoId, Documento domain, DocumentoFilter, DocumentoRepository port | — |
| US1 | Upload: entity+repo+mapper+impl, UploadCommand+Handler, Controller POST / | P1 |
| US2 | Listagem: DTOs, GetByFuncionario+GetById queries+handlers, Controller GET / e /{id} | P2 |
| US3 | Download: GetDownloadUrl query+handler, Controller GET /{id}/download | P3 |
| US4 | Soft delete: DesativarCommand+Handler, Controller DELETE /{id} | P4 |

## Decisões Técnicas Chave

| Decisão | Escolha |
|---------|---------|
| MinIO integration | `DocumentoService` (shared/) — não MinioClient directo |
| Folder MinIO | `DocumentoFolder.FUNCIONARIO` ("funcionario_documents") |
| Validação extensão | No handler — comparação com `DocumentType.allowedExtensions` (split por vírgula) |
| Tamanho máximo | 10 MB — validado no controller via `@RequestParam MultipartFile` |
| DocumentType acesso | Injectar `DocumentTypeRepository` de `parametrizacoes/` no handler |
| file_key | Exactamente como devolvido por `DocumentoService.save()` → `FileResponseDTO.getFileId()` |
| Soft delete | Apenas `is_active=false` — ficheiro permanece no MinIO |
| Download de inactivo | Handler verifica `isActive` e lança 404 antes de chamar MinIO |
