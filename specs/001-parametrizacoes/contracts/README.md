# Phase 1 — REST API Contracts

**Feature**: Catálogos de Parametrização do Módulo RH
**Branch**: `001-parametrizacoes`
**Date**: 2026-04-29

Esta pasta documenta os contratos REST de cada catálogo de parametrização. Cada ficheiro descreve um controller IGRP que será gerado pelo skill `igrp-spring-generator` a partir de manifests `.igrpstudio/parametrizacoes/controllers/*.json`.

## Convenções globais

| Aspecto | Valor |
|---|---|
| Base URL | `/api/v1/rh` (do servidor) |
| Authentication | JWT Bearer (deactivated em dev/staging — Princípio V) |
| Authorization (escrita) | `@PreAuthorize("hasRole('PARAM_ADMIN')")` — placeholder até decisão final |
| Authorization (leitura) | `@PreAuthorize("isAuthenticated()")` |
| Content-Type | `application/json` |
| Pagination | `page` (1-based, default 1), `size` (default 20, máx 100) |
| Sort | `sort=campo,asc\|desc` |
| Errors | JSON com `timestamp`, `status`, `error`, `message`, `path`, `fields[]` |

## Códigos HTTP padrão

| Código | Quando |
|---|---|
| 200 OK | GET com sucesso |
| 201 Created | POST com sucesso |
| 204 No Content | DELETE soft (deactivation) com sucesso |
| 400 Bad Request | Validação de payload falhou |
| 401 Unauthorized | Sem token |
| 403 Forbidden | Token sem perfil necessário |
| 404 Not Found | ID inexistente |
| 409 Conflict | Code duplicado, deactivação bloqueada (referenciado), `is_core` |
| 422 Unprocessable Entity | Regra de negócio violada |

## Lista de contratos

| Endpoint | Ficheiro | Notas |
|---|---|---|
| `/reference/options` | [reference-options.md](./reference-options.md) | CRUD genérico filtrável por `ccode` e `locale` |
| `/worker-states` | [worker-states.md](./worker-states.md) | CRUD com bloqueio em `is_core` |
| `/professional-situations` | [professional-situations.md](./professional-situations.md) | CRUD |
| `/contract-types` | [contract-types.md](./contract-types.md) | CRUD |
| `/document-types` | [document-types.md](./document-types.md) | CRUD com `allowed_extensions` |
| `/leave-types` | [leave-types.md](./leave-types.md) | CRUD com flags `deducts_balance`, `requires_approval` |
| `/leave-mobility-subtypes` | [leave-mobility-subtypes.md](./leave-mobility-subtypes.md) | CRUD com flags |
| `/public-holidays` | [public-holidays.md](./public-holidays.md) | CRUD com filtros por ano e por escopo |
