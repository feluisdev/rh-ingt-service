# Feature Specification: Carreiras e Progressão

**Feature Branch**: `003-carreiras-progressao`  
**Created**: 2026-04-30  
**Status**: Draft  

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Gestão de Carreiras (Priority: P1)

O gestor de RH precisa de manter o catálogo de carreiras (ex.: Técnica Superior, Técnica, Administrativa) que serve de raiz da hierarquia PCFR. Sem carreiras definidas não é possível criar categorias nem escalões, e portanto nenhum enquadramento profissional pode ser registado.

**Why this priority**: É o fundamento da hierarquia — todas as outras entidades dependem dela. Deve ser implementada e testada em isolamento antes das restantes.

**Independent Test**: Pode ser testada completamente criando, consultando, actualizando e desactivando carreiras sem necessidade de categorias ou escalões.

**Acceptance Scenarios**:

1. **Given** não existe nenhuma carreira, **When** o gestor cria uma carreira com code="TS" e name="Técnica Superior", **Then** a carreira é registada com `is_active=true` e pode ser consultada.
2. **Given** existe uma carreira "TS", **When** o gestor tenta criar outra carreira com code="TS", **Then** o sistema recusa com erro de conflito (duplicado).
3. **Given** existe uma carreira activa sem categorias, **When** o gestor a desactiva, **Then** `is_active` passa a `false` e a carreira não aparece nas listagens activas.
4. **Given** existe uma carreira com categorias activas, **When** o gestor tenta desactivá-la, **Then** o sistema recusa com erro de conflito (tem dependentes activos).
5. **Given** existe uma carreira, **When** é consultada pelo ID, **Then** devolve todos os campos incluindo o histórico de auditoria.

---

### User Story 2 — Gestão de Categorias (Priority: P2)

O gestor de RH define os níveis profissionais dentro de cada carreira (ex.: Técnico Superior de 1.ª classe, de 2.ª classe). Cada categoria pertence a uma carreira e o código é único dentro dessa carreira. A carreira associada não pode ser alterada após criação.

**Why this priority**: Depende de carreiras (P1) mas é pré-requisito dos escalões (P3). Sem categorias não existem posições remuneratórias.

**Independent Test**: Pode ser testada criando uma carreira de suporte, depois criando/consultando/actualizando/desactivando categorias nessa carreira.

**Acceptance Scenarios**:

1. **Given** existe a carreira "Técnica Superior", **When** o gestor cria a categoria com code="TS1" e career_id válido, **Then** a categoria é registada vinculada à carreira.
2. **Given** já existe a categoria "TS1" na carreira "TS", **When** o gestor tenta criar outra categoria "TS1" na mesma carreira, **Then** o sistema recusa com conflito. O mesmo code "TS1" numa carreira diferente deve ser aceite.
3. **Given** existe uma categoria activa com escalões activos, **When** o gestor tenta desactivá-la, **Then** o sistema recusa com conflito.
4. **Given** existe uma categoria, **When** o gestor tenta alterar o campo `career_id`, **Then** o sistema recusa (campo imutável).
5. **Given** existe uma carreira, **When** é pedida a lista de categorias dessa carreira via `/careers/{id}/categories`, **Then** são devolvidas apenas as categorias dessa carreira.

---

### User Story 3 — Gestão de Escalões (Priority: P3)

O gestor de RH define as posições remuneratórias dentro de cada categoria (ex.: Escalão 1, Escalão 2, …). Cada escalão tem um número de ordem (`grade_number`) único dentro da categoria e um índice salarial opcional conforme a grelha PCFR. A categoria associada não pode ser alterada após criação.

**Why this priority**: Depende de categorias (P2). O índice salarial é o dado de referência que alimenta o cálculo de remuneração no módulo de colaboradores.

**Independent Test**: Pode ser testada criando carreira + categoria de suporte, depois gerindo escalões nessa categoria.

**Acceptance Scenarios**:

1. **Given** existe a categoria "TS1", **When** o gestor cria escalão com grade_number=1 e salary_index=100.50, **Then** o escalão é registado com o índice salarial correcto.
2. **Given** já existe o escalão nº 1 na categoria "TS1", **When** o gestor tenta criar outro escalão nº 1 na mesma categoria, **Then** o sistema recusa com conflito.
3. **Given** existe um escalão referenciado num enquadramento profissional activo, **When** o gestor tenta desactivá-lo, **Then** o sistema recusa com conflito (está em uso).
4. **Given** existe um escalão, **When** o gestor tenta alterar o campo `category_id`, **Then** o sistema recusa (campo imutável).
5. **Given** existe uma categoria, **When** é pedida a lista de escalões via `/categories/{id}/grades`, **Then** são devolvidos os escalões dessa categoria ordenados por `grade_number`.

---

### Edge Cases

- O que acontece ao tentar desactivar uma carreira que tem categorias inactivas mas nenhuma activa? → Deve ser permitido (apenas categorias activas bloqueiam).
- O que acontece ao criar um escalão com `grade_number=0` ou negativo? → Recusado (grade_number ≥ 1).
- O que acontece ao consultar categorias de uma carreira inexistente? → HTTP 404.
- O que acontece ao reactivar uma carreira previamente desactivada? → Permitido (sem restrições).
- `salary_index` nulo é válido? → Sim, o campo é opcional.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE permitir criar uma carreira com `code` (único globalmente, máx. 50 caracteres) e `name` (máx. 150 caracteres).
- **FR-002**: O sistema DEVE impedir a criação de duas carreiras com o mesmo `code`.
- **FR-003**: O sistema DEVE permitir listar carreiras com filtro opcional por `isActive`.
- **FR-004**: O sistema DEVE permitir consultar uma carreira por ID.
- **FR-005**: O sistema DEVE permitir actualizar `name` e `description` de uma carreira existente.
- **FR-006**: O sistema DEVE realizar soft delete de uma carreira (marcar `is_active=false`); eliminação física nunca ocorre.
- **FR-007**: O sistema DEVE impedir a desactivação de uma carreira que possui categorias activas (HTTP 409).
- **FR-008**: O sistema DEVE permitir criar uma categoria associada a uma carreira, com `code` único dentro dessa carreira e `name`.
- **FR-009**: O sistema DEVE impedir a criação de duas categorias com o mesmo `code` dentro da mesma carreira.
- **FR-010**: O sistema DEVE impedir a alteração do campo `career_id` de uma categoria após a sua criação.
- **FR-011**: O sistema DEVE permitir listar categorias com filtro opcional por `careerId` e `isActive`.
- **FR-012**: O sistema DEVE disponibilizar um sub-recurso `/careers/{id}/categories` que devolve as categorias de uma carreira.
- **FR-013**: O sistema DEVE realizar soft delete de uma categoria; DEVE impedir a desactivação se a categoria tiver escalões activos (HTTP 409).
- **FR-014**: O sistema DEVE permitir criar um escalão associado a uma categoria, com `grade_number` (inteiro ≥ 1) único dentro dessa categoria, `name`, e `salary_index` opcional.
- **FR-015**: O sistema DEVE impedir a criação de dois escalões com o mesmo `grade_number` dentro da mesma categoria.
- **FR-016**: O sistema DEVE impedir a alteração do campo `category_id` de um escalão após a sua criação.
- **FR-017**: O sistema DEVE disponibilizar um sub-recurso `/categories/{id}/grades` que devolve os escalões de uma categoria, ordenados por `grade_number`.
- **FR-018**: O sistema DEVE realizar soft delete de um escalão; DEVE impedir a desactivação se o escalão estiver referenciado em enquadramentos profissionais activos (HTTP 409).
- **FR-019**: O sistema DEVE registar auditoria (criado por, criado em, modificado por, modificado em) em todas as entidades.
- **FR-020**: O sistema DEVE expor histórico de revisões (criação, alterações, desactivação) para cada entidade.

### Key Entities

- **Carreira**: Catálogo raiz da hierarquia PCFR. Atributos: identificador único, código (único), nome, descrição, estado activo/inactivo, auditoria de criação e alteração.
- **Categoria**: Nível profissional dentro de uma carreira. Atributos: identificador único, referência à carreira (imutável), código (único na carreira), nome, descrição, estado activo/inactivo, auditoria.
- **Escalão**: Posição remuneratória dentro de uma categoria. Atributos: identificador único, referência à categoria (imutável), número de escalão (inteiro ≥ 1, único na categoria), nome, índice salarial (decimal opcional), estado activo/inactivo, auditoria.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: O gestor de RH consegue criar, consultar, actualizar e desactivar uma carreira em menos de 1 minuto por operação.
- **SC-002**: O sistema impede 100% das tentativas de criar registos duplicados (code de carreira; par carreira+code de categoria; par categoria+número de escalão).
- **SC-003**: O sistema impede 100% das tentativas de desactivação inválida (carreira com categorias activas; categoria com escalões activos; escalão referenciado em enquadramento activo).
- **SC-004**: O histórico de auditoria está disponível para 100% das entidades criadas ou alteradas.
- **SC-005**: A navegação hierárquica (carreira → categorias → escalões) devolve resultados correctos em menos de 2 segundos para catálogos com até 500 registos por nível.

---

## Clarifications

### Session 2026-04-30

- Q: Quando `employee_professional_assignments` ainda não tem registos (ou a tabela está vazia), como deve o sistema comportar-se na validação de "escalão em uso"? → A: A desactivação é permitida quando não existem enquadramentos activos referenciadores — a restrição só actua com dados reais.

---

## Assumptions

- O módulo `carreiras/` será consumido pelo módulo `colaboradores/` (enquadramento profissional); a tabela `employee_professional_assignments` existe no esquema mas ainda não está implementada — a validação de "escalão em uso" é feita por query directa à tabela. Se a tabela não contiver registos referenciadores (ou ainda não existir), a desactivação é permitida; a restrição actua apenas quando existem enquadramentos activos reais.
- A autenticação é gerida externamente (Keycloak); em ambiente de desenvolvimento está desactivada.
- Não existe paginação cursorial — listagens usam paginação por offset/limit consistente com os outros módulos do projecto.
- `salary_index` é um valor decimal de referência; o cálculo salarial efectivo não faz parte deste módulo.
- A reactivação de registos desactivados não tem restrições de negócio — é sempre permitida.
- A ordem de exibição dos escalões é sempre ascendente por `grade_number`.
