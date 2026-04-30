# Research: Carreiras e Progressão

**Feature**: 003-carreiras-progressao  
**Date**: 2026-04-30

## Decisões Técnicas

### 1. Arquitectura do módulo

**Decision**: Hexagonal idêntica ao módulo `estrutura/` (domain → application → infrastructure → interfaces).  
**Rationale**: O padrão está estabelecido, testado, e aprovado na constituição. Não há razão para desvio.  
**Alternatives considered**: Nenhuma — constituição não negociável.

### 2. Tabelas e nomes de PKs

**Decision**: UUIDs em todas as PKs (`t_career`, `t_category`, `t_grade`). FKs também UUID.  
**Rationale**: Consistente com todas as entidades do projecto. A v4 doc usa BIGINT mas o projecto usa ExternalID (UUID wrapper) — prevalece o padrão do projecto.  
**Alternatives considered**: BIGINT auto-increment — rejeitado (inconsistente com ExternalID).

### 3. Imutabilidade de campos

**Decision**:
- `career_id` em Category — imutável após criação (sem setter no domain model; handler rejeita tentativa de alteração via HTTP 400 se diferir do valor original)
- `code` em Category — imutável após criação (conforme v4 spec secção 4.5)
- `category_id` em Grade — imutável após criação
- `grade_number` em Grade — imutável após criação (parte da constraint de unicidade; alterar seria equivalente a criar um novo escalão)

**Rationale**: A v4 spec § 4.5 é explícita: "code, career_id em categorias e category_id em escalões são imutáveis." Adicionamos `grade_number` por consistência — é a identidade remuneratória do escalão.

### 4. Validação "escalão em uso" (FR-018)

**Decision**: Query defensiva em `employee_professional_assignments` usando `@Query` nativa com `EXISTS`. Se a tabela não existir ou estiver vazia, a desactivação é permitida.  
**Rationale**: Acordado na clarificação — não bloquear operações legítimas antes do módulo `colaboradores/` existir.  
**Implementation note**: Usar `try/catch` ou verificar existência da tabela via `information_schema` antes de executar a query. Alternativa mais simples: `SELECT EXISTS (SELECT 1 FROM employee_professional_assignments WHERE grade_id = ? AND is_current = true)` — se a tabela não existir, a query falha com `PSQLException`; capturar e tratar como "não referenciado".

### 5. Sub-recursos hierárquicos

**Decision**: 
- `GET /careers/{id}/categories` → método adicional no `CareerController` IGRP + `GetCategoriesByCareerIdQuery`
- `GET /categories/{id}/grades` → método adicional no `CategoryController` IGRP + `GetGradesByCategoryIdQuery`

**Rationale**: Mantém a responsabilidade no controller da entidade-pai, tal como o padrão do OrganizationalUnit com as suas sub-unidades.

### 6. Migração Flyway

**Decision**: V23 — cria `t_career`, `t_category`, `t_grade` numa única migração.  
**Rationale**: As três tabelas são interdependentes (FKs em cascata) e devem ser criadas atomicamente. Próximo número disponível após V22.

### 7. Auditoria Envers

**Decision**: `@Audited` em `CareerEntity`, `CategoryEntity`, `GradeEntity`. Endpoint de auditoria em `CarreirasAuditHistoryController` cobre as três entidades.  
**Rationale**: Constituição § IV — obrigatório.

### 8. Checagem de desactivação de Carreira

**Decision**: Verificar se existem `CategoryEntity` com `is_active = true` e `career_id` igual ao da carreira a desactivar. Se existirem → HTTP 409.  
**Rationale**: FR-007. Mesma lógica que `OrganizationalUnit` com sub-unidades activas.

### 9. Checagem de desactivação de Categoria

**Decision**: Verificar se existem `GradeEntity` com `is_active = true` e `category_id` igual ao da categoria a desactivar. Se existirem → HTTP 409.  
**Rationale**: FR-013.
