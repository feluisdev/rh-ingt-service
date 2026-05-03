# Quickstart — Parametrizações

**Feature**: Catálogos de Parametrização do Módulo RH
**Branch**: `001-parametrizacoes`
**Date**: 2026-04-29

Guia rápido para um developer começar a implementar e validar a feature.

---

## Pré-requisitos

| Ferramenta | Versão | Notas |
|---|---|---|
| JDK | 23 (Eclipse Temurin) | `JAVA_HOME` aponta para JDK 23 |
| Maven | 3.9+ | |
| PostgreSQL | 17 | Local ou Docker |
| MinIO | latest | Não relevante para esta feature |
| Docker / Docker Compose | latest | Útil para correr PostgreSQL local |

---

## Setup local (primeira vez)

```bash
# 1. Subir PostgreSQL local
docker-compose up -d postgres

# 2. Verificar variáveis de ambiente
cat .env  # confirmar POSTGRES_HOST, POSTGRES_DATABASE, etc.

# 3. Adicionar dependências Flyway no pom.xml (uma vez)
#    Já incluído nesta feature — verificar
mvn dependency:tree | grep flyway

# 4. Build inicial
mvn clean compile

# 5. Correr a aplicação — Flyway aplica todas as migrations + seed automaticamente
mvn spring-boot:run -Dspring-boot.run.profiles=development
```

A primeira execução cria as 8 tabelas e popula o seed "kit Cabo Verde". A consola deve mostrar:

```
Flyway Community Edition X.Y.Z by Redgate
Successfully validated 16 migrations
Current version of schema: 016
Migrating schema "public" to version V001 - create option entity
...
Successfully applied 16 migrations to schema "public" (execution time XX:XX.XXX)
```

---

## Verificar o seed

Após arranque, abrir Swagger UI: <http://localhost:8091/swagger-ui.html>

| Endpoint para validar | O que deves ver |
|---|---|
| `GET /reference/options?ccode=MARITAL_STATUS` | 5 entradas: Solteiro(a), Casado(a), União de Facto, Divorciado(a), Viúvo(a) |
| `GET /reference/options?ccode=SEX` | 2 entradas: Masculino, Feminino |
| `GET /reference/options?ccode=NATIONALITY` | ~15 entradas (CV, PT, BR, AO, MZ, ST, GW, TL, FR, US, ES, IT, DE, NL, GB) |
| `GET /reference/options?ccode=ISLAND` | 10 entradas (Santiago, São Vicente, Sal, Boa Vista, Fogo, Brava, Maio, Santo Antão, São Nicolau, Santa Luzia) |
| `GET /reference/options?ccode=CONCELHO` | 22 entradas (todos os concelhos de Cabo Verde) |
| `GET /worker-states` | 3 entradas (ACTIVE+isCore, INACTIVE+isCore, SUSPENDED) |
| `GET /professional-situations` | 4 entradas |
| `GET /contract-types` | 5 entradas (NOMEACAO_DEFINITIVA, CFP, CTFP_TC, CTFP_TI, COMISSAO_SERVICO) |
| `GET /leave-types` | 6 entradas com flags pré-configuradas |
| `GET /document-types` | ~10 tipos com extensões |
| `GET /public-holidays?year=2026` | ~10 feriados nacionais para 2026 |

---

## Validar idempotência do seed

```bash
# 1. Parar a aplicação
# 2. Anotar quantas linhas existem em cada tabela
psql -h localhost -U rh -d recursoshumanos_db -c "SELECT 'option_entity', COUNT(*) FROM t_option_entity UNION ALL SELECT 'worker_state', COUNT(*) FROM t_worker_state;"

# 3. Re-arrancar a aplicação
mvn spring-boot:run -Dspring-boot.run.profiles=development

# 4. Verificar que NÃO houve novos INSERTs
psql -h localhost -U rh -d recursoshumanos_db -c "SELECT 'option_entity', COUNT(*) FROM t_option_entity UNION ALL SELECT 'worker_state', COUNT(*) FROM t_worker_state;"
# Esperado: contagens iguais.
```

**Critério de aceitação**: o re-arranque é instantâneo (Flyway diz "Schema is up to date") e nenhuma contagem aumenta.

---

## Testar uma alteração administrativa preservada por seed

```bash
# 1. Editar uma entrada via PUT /reference/options/{id}
curl -X PUT http://localhost:8091/api/v1/rh/reference/options/{id} \
     -H "Content-Type: application/json" \
     -d '{"cvalue":"Solteiro(a) — alterado","sortOrder":1,"description":"editado pelo admin"}'

# 2. Re-arrancar a aplicação
mvn spring-boot:run -Dspring-boot.run.profiles=development

# 3. Verificar que a alteração administrativa NÃO foi revertida
curl http://localhost:8091/api/v1/rh/reference/options/{id}
# Esperado: cvalue continua "Solteiro(a) — alterado"
```

---

## Correr os testes

```bash
# Unit tests (handlers, domain, services)
mvn test

# Apenas testes de uma classe
mvn test -Dtest=OptionEntityRepositoryTest

# Testes de integração com Testcontainers (correm migrations + seed em PostgreSQL real)
mvn verify -Pintegration-tests
```

---

## Pre-requisites para implementar (checklist do developer)

Antes de abrir cada PR de implementação:

- [ ] Manifests `.igrpstudio/parametrizacoes/` criados (8 controllers, 8 modelos, ~24 DTOs)
- [ ] Skill `igrp-spring-generator` invocado para gerar Java
- [ ] Migrations `V001..V016` em `src/main/resources/db/migration/`
- [ ] `pom.xml` actualizado com `flyway-core` e `flyway-database-postgresql`
- [ ] `application*.properties` com `spring.flyway.enabled=true` e `ddl-auto=validate`
- [ ] `OptionEntity` movida de `shared/` para `parametrizacoes/`
- [ ] Domain models em `parametrizacoes/domain/models/` com factory methods (`criar`, `reconstruir`, `atualizar`, `desativar`, `reativar`)
- [ ] Handlers em `parametrizacoes/application/commands` e `queries` (~40 no total)
- [ ] Adapters em `parametrizacoes/infrastructure/persistence/adapters/`
- [ ] Mappers em `parametrizacoes/infrastructure/mappers/`
- [ ] `mvn clean compile` BUILD SUCCESS
- [ ] `mvn test` 100% pass
- [ ] Swagger mostra todos os 8 controllers com endpoints documentados
- [ ] Idempotência do seed validada (re-arranque sem novas linhas)

---

## Troubleshooting

| Sintoma | Causa provável | Resolução |
|---|---|---|
| `mvn spring-boot:run` falha com "Schema validation failed" | Hibernate `validate` detectou drift entre código e schema | Rodar Flyway antes; verificar que migrations criam as colunas correctas |
| Flyway: "Found non-empty schema(s) ... but no schema history" | Ambiente já tinha tabelas antes de Flyway | Adicionar `spring.flyway.baseline-on-migrate=true` |
| Re-arranque cria novas linhas no seed | `INSERT` sem `ON CONFLICT DO NOTHING` | Rever migration de seed |
| 401 ao chamar `/reference/options` em dev | Segurança não devia estar activa em dev | Verificar `SERVICE_PROFILE=development` no `.env` |
| 500 ao chamar `/document-types` POST com `categoryOptionId` qualquer | Não está a validar que o option pertence a `ccode='DOC_CATEGORY'` | Acrescentar validação no handler |

---

## Auditoria — Consulta de Histórico (US4)

### Como funciona

Cada alteração em qualquer catálogo é registada automaticamente pelo Hibernate Envers nas tabelas `*_AUD` dentro do schema `audit_schema` (criado pela migration V19). A criação das tabelas de auditoria é feita pelo Hibernate no arranque (via `ddl-auto=update`), depois das migrations Flyway.

### Endpoint de consulta

```
GET /api/v1/rh/catalogs/audit/{catalog}/{entityId}
```

| Parâmetro | Valores aceites |
|---|---|
| `catalog` | `reference-options`, `worker-states`, `professional-situations`, `contract-types`, `document-types`, `leave-types`, `leave-mobility-subtypes`, `public-holidays` |
| `entityId` | UUID da entrada no catálogo |

**Exemplo:**
```bash
curl http://localhost:8091/api/v1/rh/catalogs/audit/worker-states/{id}
```

**Resposta esperada:**
```json
{
  "content": [
    { "revisionNumber": 1, "revisionDate": "2026-04-30T10:00:00Z", "modificationType": "INSERT" },
    { "revisionNumber": 2, "revisionDate": "2026-04-30T10:05:00Z", "modificationType": "UPDATE" }
  ],
  "totalElements": 2
}
```

### Política de retenção

As tabelas `*_AUD` no schema `audit_schema` têm **retenção indefinida** — nenhum job de limpeza está configurado. Não há limite de revisões por entrada. Os registos de auditoria nunca são apagados automaticamente e devem ser preservados para fins de conformidade.

### Verificar tabelas de auditoria directamente

```sql
-- Listar todos os schemas de auditoria
SELECT schemaname, tablename FROM pg_tables WHERE schemaname = 'audit_schema';

-- Ver histórico de um worker state específico
SELECT * FROM audit_schema.t_worker_state_aud WHERE id = '<uuid>';

-- Contar revisões totais por tabela
SELECT 'option_entity', COUNT(*) FROM audit_schema.t_option_entity_aud
UNION ALL SELECT 'worker_state', COUNT(*) FROM audit_schema.t_worker_state_aud;
```

### Troubleshooting de auditoria

| Sintoma | Causa provável | Resolução |
|---|---|---|
| `audit_schema` não existe no arranque | Migration V19 não correu | Verificar que `V19__create_audit_schema.sql` está em `db/migration/`; verificar log do Flyway |
| Tabelas `*_AUD` não criadas | `ddl-auto` não é `update` ou `create` | Em dev, `ddl-auto=update` cria as `*_AUD` automaticamente; em produção usar `validate` + script DDL manual |
| `GET /audit/{catalog}/{id}` devolve 400 | Valor de `catalog` inválido | Usar apenas os 8 valores aceites listados acima |
| `GET /audit/{catalog}/{id}` devolve lista vazia | Entidade existe mas nunca foi alterada via JPA | Confirmar que a entity tem `@Audited`; confirmar que a escrita usou o repositório JPA (não SQL nativo) |
| `created_by` e `updated_by` são `null` no histórico | `ApplicationAuditorAware` não tem utilizador no contexto | Em dev, segurança desactivada — o auditor pode ser `anonymous`; verificar `SecurityContextHelper` |

---

## Referências internas

- **Spec funcional**: [spec.md](./spec.md)
- **Modelo relacional v4**: [Modelo_Relacional_RH_v4.0.md](../../docs/funcionarios/v4/Modelo_Relacional_RH_v4.0.md)
- **Especificação técnica v4**: [Especificacao_Tecnica_Modulo_RH_v4.0.md](../../docs/funcionarios/v4/Especificacao_Tecnica_Modulo_RH_v4.0.md)
- **Arquitectura de módulos v4**: [Arquitectura_Modulos_RH_v4.0.md](../../docs/funcionarios/v4/Arquitectura_Modulos_RH_v4.0.md)
- **Constituição**: [.specify/memory/constitution.md](../../.specify/memory/constitution.md)
- **Skill IGRP**: [.claude/skills/igrp-spring-generator/](../../.claude/skills/igrp-spring-generator/)
