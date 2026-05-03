# Research: Estrutura Organizacional

**Branch**: `002-estrutura-organizacional` | **Date**: 2026-04-30

---

## Decisão 1: FK Auto-referencial em JPA — Abordagem de Mapeamento

**Decision**: Mapear `parent_unit_id` como campo `UUID` simples (sem `@ManyToOne`) na entidade JPA, seguindo o padrão já existente em `DocumentTypeEntity.categoryOptionId`.

**Rationale**: O projecto não usa navegação lazy/eager por associações JPA nos repositórios de catálogos. A FK é persistida como UUID e a validação da existência/estado da unidade-mãe é feita no handler da camada `application` através de uma chamada ao `OrganizationalUnitRepository`. Esta abordagem mantém a entidade JPA simples, sem dependências de `FetchType` ou N+1 potenciais, e é consistente com o padrão do módulo `parametrizacoes/`.

**Alternatives considered**:
- `@ManyToOne(fetch = LAZY)` com `@JoinColumn` — rejeitado porque introduz complexidade de sessão Hibernate e risco de `LazyInitializationException` fora de contexto transaccional; inconsistente com os outros catálogos.
- CTE/query recursiva para árvore — rejeitado; a spec define lista plana, não árvore aninhada.

---

## Decisão 2: Regra de Desactivação Hierárquica — Onde Validar

**Decision**: Validar a existência de sub-unidades activas na camada `application`, dentro do `DesativarOrganizationalUnitCommandHandler`, antes de persistir a alteração.

**Rationale**: A regra "não desactivar se existirem filhos activos" é uma regra de negócio pura, sem lógica de UI. Colocá-la no handler mantém o domínio limpo e o handler como única fonte de verdade (Princípio II da Constituição). A verificação usa `OrganizationalUnitRepository.existsActiveChildrenOf(OrganizationalUnitId)` — um método dedicado no port de repositório.

**Alternatives considered**:
- Constraint de base de dados (trigger) — rejeitado; viola o Princípio I (lógica de infraestrutura não pode conter lógica de negócio) e não produz mensagens de erro amigáveis.
- Validação no domain model — considerado; preferível em DDD puro mas o aggregate root de unidade orgânica não tem acesso directo aos seus filhos sem repositório, pelo que a validação no handler é o compromisso correcto para este projecto.

---

## Decisão 3: Reactivação de Unidade Orgânica — Validação da Unidade-mãe

**Decision**: No `AtivarOrganizationalUnitCommandHandler`, antes de reactivar, verificar se `parent_unit_id` é não nulo e se a unidade-mãe está activa. Se a mãe estiver inactiva, rejeitar com HTTP 409.

**Rationale**: A regra de reactivação é simétrica à regra de criação ("a unidade-mãe deve estar activa"). Aplicar a mesma validação evita estados inconsistentes na hierarquia (unidade activa filha de unidade inactiva). Clarificação acordada na sessão de clarificação 2026-04-30.

**Alternatives considered**:
- Sem validação de mãe ao reactivar — rejeitado; criaria hierarquias incoerentes onde uma sub-unidade fica activa mas a sua mãe inactiva.

---

## Decisão 4: DTOs de Auditoria — Reutilização vs. Duplicação

**Decision**: Criar DTOs de auditoria dedicados no módulo `estrutura/` (`AuditHistoryEntryDTO`, `WrapperListaAuditHistoryDTO`) em vez de reutilizar os de `parametrizacoes/`.

**Rationale**: Os módulos são bounded contexts independentes. Partilhar DTOs entre BCs cria acoplamento indesejável — uma alteração em `parametrizacoes` quebraria `estrutura`. A duplicação é intencional e controlada (classes pequenas, < 20 linhas cada).

**Alternatives considered**:
- Mover para `shared/` — considerado; viável mas introduz complexidade de gestão de versão do shared kernel. Deferido para uma refactorização futura se mais BCs precisarem do mesmo padrão.

---

## Decisão 5: Endpoint de Auditoria — Estrutura URL

**Decision**: Expor auditoria via endpoint dedicado em `api/v1/rh/estrutura/audit/{catalog}/{entityId}` onde `catalog` pode ser `organizational-units`, `jobs` ou `functions`.

**Rationale**: Segue exactamente o padrão de `api/v1/rh/catalogs/audit/{catalog}/{entityId}` do módulo `parametrizacoes/`. O `GetEstruturaAuditHistoryQueryHandler` usa um `CATALOG_MAP` idêntico mapeando o nome do catálogo para a classe da entidade JPA.

---

## Decisão 6: Flyway — Versões das Migrações

**Decision**: 
- `V20__create_estrutura_tables.sql` — DDL das 3 tabelas
- `V21__seed_unit_types.sql` — seed defensivo dos tipos de unidade orgânica (`UNIT_TYPE`) no `option_entity`

**Rationale**: A versão actual mais alta é V19 (`create_audit_schema`). O seed de `UNIT_TYPE` usa `INSERT ... ON CONFLICT DO NOTHING` (padrão do projecto) para ser idempotente — a spec assume que pode já existir.

---

## Decisão 7: Cache Caffeine — Aplicação aos Três Catálogos

**Decision**: Aplicar `@Cacheable` nos query handlers de listagem e detalhe dos três catálogos, e `@CacheEvict(allEntries=true)` nos command handlers de escrita, seguindo exactamente o padrão de `parametrizacoes/`.

**Rationale**: Os catálogos têm baixa taxa de mutação. Cache com TTL 60 s (Caffeine) reduz carga na base de dados sem inconsistência relevante. O nome das caches será: `organizationalUnitsCache`, `jobsCache`, `functionsCache`.
