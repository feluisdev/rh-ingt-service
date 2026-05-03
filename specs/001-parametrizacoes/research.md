# Phase 0 — Research

**Feature**: Catálogos de Parametrização do Módulo RH
**Branch**: `001-parametrizacoes`
**Date**: 2026-04-29

Este documento consolida as decisões técnicas tomadas com base na análise da spec, da constituição, do estado actual do projecto, e da experiência adquirida no `refactor/sigdi-isolation`. Não há `[NEEDS CLARIFICATION]` markers a resolver — toda a informação técnica está fechada.

---

## R1. Sistema de Migrations: adoptar Flyway

**Decision**: Adoptar **Flyway Community Edition** nesta feature como mecanismo único de gestão de schema e seed.

**Rationale**:

- O projecto actualmente usa `spring.jpa.hibernate.ddl-auto=update` em `development` e `validate` em `staging`/`production`. Em dev, Hibernate gera o schema; em prod, é esperado que o schema já exista — mas **não há mecanismo formal para criá-lo**. Isto é um vazio que bloqueia qualquer feature séria do refactor v4.
- A spec exige seed defensivo idempotente (FR-022b). Sem Flyway, a única alternativa seria um `ApplicationRunner` em Java a fazer `INSERT ... ON CONFLICT` — possível mas mais difícil de versionar e auditar.
- Flyway é o standard de facto da Spring Boot ecosystem; integra-se transparentemente; o starter `spring-boot-starter-data-jpa` já está presente, basta adicionar `flyway-core` + `flyway-database-postgresql`.
- Migrations versionadas (`V*__*.sql`) ficam sob controlo de git, são repetíveis em qualquer ambiente, e o histórico de alterações ao schema fica rastreável.

**Alternatives considered**:

- **Liquibase** — mais flexível (XML/YAML/JSON/SQL), mas mais complexo. Para um projecto que vai usar SQL puro, Flyway é mais directo.
- **Hibernate `ddl-auto=update` continuado** — fora de questão para produção (constituição exige `validate`); deixa o schema sem governança.
- **`@PostConstruct` ApplicationRunner em Java** — funciona para seed mas não para schema. Acabaria em duplicação de mecanismos.
- **Manual SQL scripts em `init.sql` do PostgreSQL** — não versionado, sem rastreabilidade.

**Consequências práticas**:

- Adicionar dependências em `pom.xml`: `flyway-core` e `flyway-database-postgresql` (versão alinhada com Spring Boot 3.5.3 — provavelmente Flyway 10.x).
- Criar `src/main/resources/db/migration/` com convenção `V{nnn}__{descricao}.sql`.
- Mudar `ddl-auto=update` para `validate` em `application-development.properties`.
- Configurar `spring.flyway.enabled=true` em todos os perfis.
- Primeira migration `V001` cria as tabelas — schema gerado por Hibernate em ambientes existentes deve ser compatível (mesmos nomes de tabela e colunas usadas pelas entities); Flyway precisa de `baseline-on-migrate=true` para ambientes que já tenham tabelas.

---

## R2. Refactor de `OptionEntity` (e tabela existente)

**Decision**: Mover `OptionEntity` de `shared/infrastructure/persistence/{entity,repository}/` para `parametrizacoes/infrastructure/persistence/{entity,repository}/`, seguindo o padrão validado pelo `refactor/sigdi-isolation`.

**Rationale**:

- O option_entity é específico do bounded context Parametrizações. Não é cross-cutting — nenhum outro módulo o consome directamente como entidade JPA; apenas via os endpoints REST de `/reference/options`.
- O refactor de sigdi (commit `1220a9e` e merge `079262a`) validou o procedimento: editar `module` no JSON, mover JSON, mover Java, fix de imports.
- A `OptionEntityRepository` actualmente no `shared/` também migra.

**Alternatives considered**:

- Manter em `shared/` — incoerente com a decisão arquitectural recente (cada módulo é dono das suas entities).
- Criar uma nova `ParameterOptionEntity` em `parametrizacoes/` e deprecar `OptionEntity` — mais código duplicado, mais migrations, sem benefício.

**Procedimento detalhado** (sequência exacta):

1. Editar `.igrpstudio/shared/models/OptionEntity.json`: `"module":"shared"` → `"module":"parametrizacoes"`.
2. `git mv .igrpstudio/shared/models/OptionEntity.json .igrpstudio/parametrizacoes/models/OptionEntity.json`.
3. `git mv src/main/java/cv/igrp/RH_Service/shared/infrastructure/persistence/entity/OptionEntity.java src/main/java/cv/igrp/RH_Service/parametrizacoes/infrastructure/persistence/entity/OptionEntity.java`.
4. Idem `OptionEntityRepository.java` para `parametrizacoes/infrastructure/persistence/repository/`.
5. Actualizar `package` em ambos os ficheiros Java.
6. Fix imports em todos os consumidores (handlers, mappers, adapters em `funcionarios/` legacy).
7. Compilar (`mvn clean compile`) e testar (`mvn test`).

**Risco**: o módulo legacy `funcionarios/` actualmente importa `OptionEntity` em handlers (ex: `CreateOptionCommandHandler`, `UpdateOptionCommandHandler`). Esses imports terão de ser actualizados. **Acceptance**: o módulo legacy deve continuar a compilar e funcionar enquanto o refactor é executado feature a feature.

---

## R3. Refactor de `TipoDocumentoEntity` → `DocumentTypeEntity`

**Decision**: Mover `TipoDocumentoEntity` para `parametrizacoes/`, **renomear** classe para `DocumentTypeEntity` mas **manter** nome de tabela `t_tipo_documento` (ou equivalente) para evitar quebra de schema existente.

**Rationale**:

- A spec v4 usa terminologia inglesa nas entidades (`document_types`); manter a terminologia portuguesa cria inconsistência.
- O nome da tabela vira `t_document_type` na migration nova — mas como os ambientes dev existentes já têm `t_tipo_documento`, é necessário cuidado: a migration `V001` deve renomear (`ALTER TABLE t_tipo_documento RENAME TO t_document_type`) ou criar nova tabela e migrar dados, conforme contexto.
- Aproveita-se o refactor para acrescentar campos novos: `allowed_extensions VARCHAR(200)`, `category_option_id UUID FK→t_option_entity`.

**Alternatives considered**:

- Manter classe `TipoDocumentoEntity` em PT — deixa código bilingue inconsistente.
- Criar nova tabela `t_document_type` lado a lado com `t_tipo_documento` — gera duplicação, complica refactor de `feat/documentos` mais tarde.

**Procedimento**:

1. Migration `V005__create_document_types.sql` faz `ALTER TABLE` se a tabela existir, ou `CREATE TABLE` caso contrário.
2. Mover JSON manifest e Java files conforme R2.
3. Acrescentar atributos `allowedExtensions` e `categoryOptionId` no JSON manifest e re-gerar (manualmente alterar o Java).
4. Actualizar imports e refactor handlers em `funcionarios/` legacy.

---

## R4. Modelo de Localização (i18n) para `option_entity`

**Decision**: Manter o modelo actual da `OptionEntity` — uma linha por `(ccode, ckey, locale)`. Implementar lookup com fallback automático para `pt-CV` quando o locale pedido não existe.

**Rationale**:

- A `OptionEntity` actual já tem campo `locale`; basta usá-lo.
- Forma natural de adicionar tradução nova: criar nova linha com mesmo `ckey` mas `locale` diferente. Não há reestruturação.
- Fallback no service garante que não há "rotura visual" no frontend (sempre devolve algo).

**Implementação**:

```text
ReferenceLookupService.findByCcode(ccode, locale):
    1. Query: WHERE ccode = :ccode AND locale = :locale AND active = true
    2. Se vazio: WHERE ccode = :ccode AND locale = 'pt-CV' AND active = true
    3. Devolve resultado ordenado por sort_order
```

**Alternatives considered**:

- Coluna por idioma (`label_pt`, `label_en`, ...) — não escala; cada idioma novo requer ALTER TABLE.
- Tabela separada de traduções com FK para `option_entity` — mais normalizado mas mais joins. Não compensa para volume baixo.

---

## R5. Estratégia de Cache

**Decision**: Spring `@Cacheable` em memória local (Caffeine ou ConcurrentMapCacheManager) com TTL curto (60 s) para os endpoints de leitura `/reference/options` e os GET dos catálogos com comportamento. Sem Redis nesta fase.

**Rationale**:

- Volume é baixo (~500 entradas no total). Memória local serve com folga.
- A frequência de leitura é alta (cada formulário do sistema invoca várias chamadas de `/reference/options`). Cache reduz pressão na BD.
- TTL curto (60 s) garante que alterações administrativas se reflectem em <1 min sem necessidade de invalidação manual.
- Multi-instância: cada instância tem o seu cache local; até 60 s de divergência tolerável para parametrizações.

**Alternatives considered**:

- Redis — sobre-engenharia para volume baixo e perfil de leitura; adia para quando houver outro driver.
- Sem cache — pressão desnecessária na BD, mais latência percebida.
- Cache invalidação por evento (eviction explícito em escritas) — mais complexo, ROI baixo dado o TTL curto.

---

## R6. Estratégia de Seed Defensivo

**Decision**: Seed em migrations Flyway versionadas (não repeatable), usando `INSERT ... ON CONFLICT (ccode, ckey, locale) DO NOTHING` para `option_entity` e `INSERT ... ON CONFLICT (code) DO NOTHING` para tabelas com chave única `code`.

**Rationale**:

- `ON CONFLICT DO NOTHING` é a forma mais directa de tornar um INSERT idempotente em PostgreSQL.
- Migrations versionadas aparecem no histórico do Flyway (`flyway_schema_history`) — auditável.
- Re-execução em qualquer ambiente é segura: dados pré-existentes não são tocados; faltantes são inseridos.
- Alterações administrativas posteriores ao seed (ex: admin renomeia "Solteiro" para "Solteiro(a)") não são revertidas por re-execução do seed (porque `ON CONFLICT DO NOTHING`).

**Alternatives considered**:

- Repeatable migrations (`R__seed_*.sql`) — corre quando o checksum muda; útil mas a semântica é "re-corre se diferente", não "preserva alterações administrativas". Ficaria a sobrescrever dados editados pelo admin. **Rejeitado** por violar FR-022b.
- ApplicationRunner em Java — viola separação SQL/Java; perde a rastreabilidade da migration history.
- `MERGE INTO` (PostgreSQL 15+) — funciona mas mais verboso; `INSERT ... ON CONFLICT` é o idiomático.

**Padrão da migration de seed**:

```sql
-- V010__seed_worker_states.sql
INSERT INTO t_worker_state (id, code, name, is_core, is_active, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'ACTIVE',   'Activo',    TRUE, TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'INACTIVE', 'Inactivo',  TRUE, TRUE, NOW(), NOW()),
    (gen_random_uuid(), 'SUSPENDED','Suspenso', FALSE, TRUE, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;
```

---

## R7. Caching de configuração de produção

**Decision**: Manter `ddl-auto=validate` em produção e staging (já está). Adicionar `spring.flyway.enabled=true` em todos os perfis. Em desenvolvimento, mudar `ddl-auto=update` para `validate` para que o Flyway seja a única fonte de schema.

**Rationale**:

- `update` em dev mascarava drift entre código JPA e schema real; em produção `validate` rejeita imediatamente. Melhor unificar comportamento.
- Flyway corre antes do JPA validar, garantindo schema sempre actualizado quando a aplicação arranca.
- `baseline-on-migrate=true` apenas para ambientes que já têm tabelas pré-existentes (dev local antigo); novos ambientes recebem schema limpo via Flyway.

---

## R8. Estratégia de Testes

**Decision**: Três níveis:

| Nível | Ferramenta | O que valida |
|---|---|---|
| Unit | JUnit 5 + Mockito | Handlers (commands/queries) e domain services com mocks de repositórios |
| Integração de schema | Testcontainers (PostgreSQL real) + Flyway | Que todas as migrations correm sem erro; que seed é idempotente (re-execução não falha nem duplica) |
| API REST | MockMvc / RestAssured | Que cada controller responde com os códigos HTTP esperados; validação de DTOs |

**Critério de aceitação**:

- Mínimo 80% de cobertura nos handlers e services (lógica de negócio).
- 100% de cobertura nos endpoints REST (cenários happy path + 4 erros principais: 400, 401, 403, 409).
- 1 teste integração que corre todo o seed duas vezes e valida que o número de linhas no fim é igual (idempotência).

**Alternatives considered**:

- Apenas mocks (sem Testcontainers) — não valida migrations nem comportamento real do PostgreSQL (`ON CONFLICT`, sequências, etc.). Inaceitável para feature de fundação.

---

## R9. IGRP Studio: criação do módulo `parametrizacoes`

**Decision**: Criar o módulo IGRP via `igrp-spring-generator` antes de qualquer geração de controllers/DTOs.

**Procedimento**:

1. Confirmar que `.igrpstudio/parametrizacoes/` existe com `module.json` (foi criado automaticamente quando movemos os manifests do sigdi via `addModule`, mas verificar).
2. Criar manifests `.igrpstudio/parametrizacoes/models/*.json` para cada uma das 8 entities.
3. Criar manifests `.igrpstudio/parametrizacoes/dto/*.json` para Request/Response DTOs.
4. Criar manifests `.igrpstudio/parametrizacoes/controllers/*.json` para os 8 controllers.
5. Invocar o skill para gerar os ficheiros Java em `src/main/java/cv/igrp/RH_Service/parametrizacoes/`.

**Confirmação do skill**: o skill `igrp-spring-generator` lê o campo `"module"` do manifest e coloca o output em `<group>/<packageName>/<module>/...`. Validado pelo refactor sigdi.

---

## R10. Migração dos enums actuais (fora de escopo desta feature)

**Decision**: Os enums actuais (`Sexo`, `EstadoCivil`, `GrauParentesco`, `TipoContrato`) **NÃO são tocados** nesta feature. Esta feature apenas garante que os catálogos novos existem e estão populados.

**Rationale**:

- Os enums são consumidos pelo módulo legacy `funcionarios/`. Tocar neles implica refactor do `funcionarios/`, que tem o seu próprio escopo nas features posteriores (`feat/colaboradores-core`, `feat/vida-profissional`, `feat/dossier`).
- Manter os enums activos durante a transição garante que o `funcionarios/` continua funcional.
- Nas features posteriores, cada handler do `funcionarios/` que usa um enum será migrado para usar a referência ao novo catálogo (FK UUID), e o enum eliminado.

**Acceptance**: ao fim desta feature, ambos os "mundos" coexistem — os enums em `shared/application/constants/` continuam usados pelo legacy; os novos catálogos estão prontos para serem consumidos pelas features seguintes.

---

## Resolved Questions

Todas as `[NEEDS CLARIFICATION]` foram resolvidas na fase `/speckit-clarify` ou através de defaults bem fundamentados nesta research.md. Recap:

| Item | Estado |
|---|---|
| Modelo de localização (i18n) | ✅ Resolved (R4) — uma linha por (ccode, ckey, locale), fallback `pt-CV` |
| Reactivação de entradas desactivadas | ✅ Resolved (spec FR-002a) |
| Retenção de auditoria | ✅ Resolved (spec FR-020a) — indefinida via Envers |
| Âmbito do seed inicial | ✅ Resolved (spec FR-021/FR-022) — kit Cabo Verde completo |
| Idempotência do seed | ✅ Resolved (R6) — `ON CONFLICT DO NOTHING` |
| Migrations: Flyway? | ✅ Resolved (R1) — adoptar nesta feature |
| Cache de catálogos | ✅ Resolved (R5) — Spring `@Cacheable` local TTL 60s |
| Refactor `OptionEntity` | ✅ Resolved (R2) — move para `parametrizacoes/` seguindo padrão sigdi |
| Refactor `TipoDocumentoEntity` | ✅ Resolved (R3) — renomear classe, manter table |
| Autorização — perfis concretos | ⏳ **Diferido** (decisão do projecto) — placeholder `PARAM_ADMIN` |
| Migração dos enums legacy | ✅ Resolved (R10) — fora de escopo desta feature |

Pronto para Phase 1 e implementação.
