# Quickstart & Integration Scenarios: Estrutura Organizacional

**Branch**: `002-estrutura-organizacional` | **Date**: 2026-04-30

Pré-requisito: app a correr com `SERVICE_PROFILE=development` em `http://localhost:8091`.

---

## 1. Verificar Tipos de Unidade Orgânica (Seed)

```bash
curl -s http://localhost:8091/api/v1/rh/reference/options?ccode=UNIT_TYPE&pagina=0&tamanho=20 | python -m json.tool
# Esperado: lista com DIRECCAO, DEPARTAMENTO, DIVISAO, SECCAO
```

---

## 2. Gestão de Unidades Orgânicas (US1)

### 2.1 Criar Unidade Raiz (Direcção)

```bash
curl -s -X POST http://localhost:8091/api/v1/rh/estrutura/organizational-units \
  -H "Content-Type: application/json" \
  -d '{
    "code": "DGA",
    "name": "Direcção Geral de Administração",
    "acronym": "DGA",
    "unitTypeOptionId": "<UUID_DO_TIPO_DIRECCAO>",
    "parentUnitId": null,
    "isActive": true
  }' | python -m json.tool
# Esperado: 201 Created com id da unidade criada
```

### 2.2 Criar Sub-unidade (Departamento filho de DGA)

```bash
curl -s -X POST http://localhost:8091/api/v1/rh/estrutura/organizational-units \
  -H "Content-Type: application/json" \
  -d '{
    "code": "DRHU",
    "name": "Departamento de Recursos Humanos",
    "acronym": "DRHU",
    "unitTypeOptionId": "<UUID_DO_TIPO_DEPARTAMENTO>",
    "parentUnitId": "<UUID_DA_DGA>",
    "isActive": true
  }' | python -m json.tool
# Esperado: 201 Created
```

### 2.3 Listar Unidades (lista plana)

```bash
curl -s "http://localhost:8091/api/v1/rh/estrutura/organizational-units?active=true&pagina=0&tamanho=20" | python -m json.tool
# Esperado: lista com DGA e DRHU, cada uma com parentUnitId preenchido/nulo
```

### 2.4 Tentar Desactivar Unidade com Filhos Activos (deve falhar)

```bash
curl -s -X PATCH http://localhost:8091/api/v1/rh/estrutura/organizational-units/<UUID_DA_DGA>/deactivate | python -m json.tool
# Esperado: HTTP 409 com mensagem de erro explicativa
```

### 2.5 Desactivar Sub-unidade (sem filhos)

```bash
curl -s -X PATCH http://localhost:8091/api/v1/rh/estrutura/organizational-units/<UUID_DO_DRHU>/deactivate | python -m json.tool
# Esperado: HTTP 200
```

### 2.6 Tentar Reactivar Sub-unidade com Mãe Inactiva

```bash
# Primeiro desactivar DGA (agora sem filhos activos)
curl -s -X PATCH http://localhost:8091/api/v1/rh/estrutura/organizational-units/<UUID_DA_DGA>/deactivate
# Depois tentar reactivar DRHU (mãe = DGA inactiva)
curl -s -X PATCH http://localhost:8091/api/v1/rh/estrutura/organizational-units/<UUID_DO_DRHU>/activate | python -m json.tool
# Esperado: HTTP 409 — mãe inactiva
```

### 2.7 Código Duplicado

```bash
curl -s -X POST http://localhost:8091/api/v1/rh/estrutura/organizational-units \
  -H "Content-Type: application/json" \
  -d '{"code": "DGA", "name": "Outra", "acronym": "X", "unitTypeOptionId": "<UUID>", "parentUnitId": null}' | python -m json.tool
# Esperado: HTTP 409
```

### 2.8 Auditoria de Unidade Orgânica

```bash
curl -s http://localhost:8091/api/v1/rh/estrutura/audit/organizational-units/<UUID_DA_DGA> | python -m json.tool
# Esperado: lista de revisões com revisionDate e type (INSERT, UPDATE, DELETE)
```

---

## 3. Gestão de Cargos (US2)

### 3.1 Criar Cargo

```bash
curl -s -X POST http://localhost:8091/api/v1/rh/estrutura/jobs \
  -H "Content-Type: application/json" \
  -d '{"code": "DIR_GERAL", "name": "Director Geral", "description": "Cargo de chefia máxima"}' | python -m json.tool
# Esperado: 201 Created
```

### 3.2 Listar Cargos Activos

```bash
curl -s "http://localhost:8091/api/v1/rh/estrutura/jobs?active=true&pagina=0&tamanho=20" | python -m json.tool
# Esperado: lista com Director Geral
```

### 3.3 Código Duplicado

```bash
curl -s -X POST http://localhost:8091/api/v1/rh/estrutura/jobs \
  -H "Content-Type: application/json" \
  -d '{"code": "DIR_GERAL", "name": "Outro"}' | python -m json.tool
# Esperado: HTTP 409
```

### 3.4 Desactivar e Reactivar Cargo

```bash
curl -s -X PATCH http://localhost:8091/api/v1/rh/estrutura/jobs/<UUID_DO_CARGO>/deactivate | python -m json.tool
# Esperado: HTTP 200
curl -s -X PATCH http://localhost:8091/api/v1/rh/estrutura/jobs/<UUID_DO_CARGO>/activate | python -m json.tool
# Esperado: HTTP 200
```

### 3.5 Auditoria de Cargo

```bash
curl -s http://localhost:8091/api/v1/rh/estrutura/audit/jobs/<UUID_DO_CARGO> | python -m json.tool
# Esperado: revisões INSERT + UPDATE + UPDATE (create, deactivate, activate)
```

---

## 4. Gestão de Funções (US3)

```bash
# Criar função
curl -s -X POST http://localhost:8091/api/v1/rh/estrutura/functions \
  -H "Content-Type: application/json" \
  -d '{"code": "GESTOR_RH", "name": "Gestor de Recursos Humanos", "description": "Gestão de pessoal"}' | python -m json.tool
# Esperado: 201 Created

# Listar
curl -s "http://localhost:8091/api/v1/rh/estrutura/functions?active=true&pagina=0&tamanho=20" | python -m json.tool

# Auditoria
curl -s http://localhost:8091/api/v1/rh/estrutura/audit/functions/<UUID_DA_FUNCAO> | python -m json.tool
```

---

## 5. Verificar Auditoria no Schema Envers (SQL)

```sql
-- Verificar revisões de unidades orgânicas
SELECT r.id, r.timestamp, a.rev_type, a.code, a.name, a.is_active
FROM audit_schema.t_unidade_organica_aud a
JOIN audit_schema.revinfo r ON a.rev = r.id
ORDER BY r.timestamp DESC;

-- Verificar revisões de cargos
SELECT r.id, r.timestamp, a.rev_type, a.code, a.name
FROM audit_schema.t_cargo_aud a
JOIN audit_schema.revinfo r ON a.rev = r.id
ORDER BY r.timestamp DESC;
```

---

## Troubleshooting

| Sintoma | Causa Provável | Solução |
|---------|----------------|---------|
| `IllegalStateException: The associated entity manager is closed` no audit endpoint | `@Transactional(readOnly=true)` ausente no handler | Verificar `GetEstruturaAuditHistoryQueryHandler` |
| HTTP 500 ao criar com `unit_type_option_id` inválido | Falta validação do ccode no handler | Verificar `CreateOrganizationalUnitCommandHandler` |
| HTTP 409 inesperado ao desactivar unidade sem filhos | Bug em `existsActiveChildrenOf()` | Verificar a query JPQL no repositório |
| Seed UNIT_TYPE não aparece | Migration V21 não executou | Verificar `flyway_schema_history`; executar `mvn flyway:migrate` |
