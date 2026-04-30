# Feature Specification: Estrutura Organizacional

**Feature Branch**: `002-estrutura-organizacional`  
**Created**: 2026-04-30  
**Status**: Draft  

## Contexto

O módulo de Estrutura Organizacional gere os catálogos de referência que definem o enquadramento hierárquico e funcional dos colaboradores no sistema RH do INGT. Inclui três catálogos: Unidades Orgânicas (com hierarquia auto-referencial), Cargos e Funções. Estes catálogos são referenciados pelo historial profissional dos colaboradores (`employee_professional_assignments`).

---

## Clarifications

### Session 2026-04-30

- Q: O endpoint de listagem de unidades orgânicas deve devolver lista plana ou estrutura em árvore? → A: Lista plana — cada unidade inclui `parent_unit_id` como UUID; o cliente constrói a árvore se necessário. Sem endpoint de árvore aninhada nesta versão.
- Q: Pode um registo desactivado ser reactivado? → A: Sim, reactivação permitida em todos os catálogos. Para unidades orgânicas, a unidade-mãe tem de estar activa antes de reactivar uma sub-unidade.

---

## User Scenarios & Testing

### User Story 1 — Gestão de Unidades Orgânicas (Priority: P1)

O administrador de RH gere a estrutura hierárquica da organização, criando e mantendo unidades orgânicas (Direcção, Departamento, Divisão, Secção). Cada unidade pode ter uma unidade-mãe (hierarquia auto-referencial), um tipo (opção do catálogo UNIT_TYPE), um código único e um acrónimo. A hierarquia pode ter profundidade arbitrária. Uma unidade só pode ser desactivada se não tiver sub-unidades activas.

**Why this priority**: É o catálogo mais complexo e o que define a estrutura organizacional base. Necessário antes de poder enquadrar colaboradores em unidades.

**Independent Test**: Pode ser testado de forma completa criando unidades raiz e sub-unidades, navegando na hierarquia, e tentando desactivar unidades com e sem filhos activos — entrega valor imediato ao permitir configurar a estrutura da organização.

**Acceptance Scenarios**:

1. **Given** nenhuma unidade orgânica existe, **When** o administrador cria uma unidade raiz com `code`, `name`, `acronym`, `unit_type_option_id` válidos e `parent_unit_id` nulo, **Then** a unidade é criada com `is_active = true` e é visível na listagem.

2. **Given** uma unidade raiz existe, **When** o administrador cria uma sub-unidade com `parent_unit_id` apontando para a unidade raiz, **Then** a sub-unidade é criada e a relação hierárquica é registada.

3. **Given** uma unidade tem sub-unidades activas, **When** o administrador tenta desactivar essa unidade, **Then** o sistema rejeita com erro de validação (HTTP 409) indicando que existem sub-unidades activas.

4. **Given** uma unidade não tem sub-unidades activas, **When** o administrador a desactiva, **Then** a unidade fica com `is_active = false` e desaparece das listagens activas.

5. **Given** um `code` de unidade já existe, **When** o administrador tenta criar outra unidade com o mesmo `code`, **Then** o sistema rejeita com erro de conflito (HTTP 409).

6. **Given** uma listagem de unidades é solicitada, **When** são fornecidos filtros de paginação e estado, **Then** o sistema devolve apenas as unidades que correspondem aos critérios.

7. **Given** uma unidade existe, **When** é consultado o histórico de auditoria dessa unidade, **Then** são devolvidas as revisões registadas (criação, alterações) com data e tipo de operação.

---

### User Story 2 — Gestão de Cargos (Priority: P1)

O administrador de RH gere o catálogo de cargos funcionais que podem ser atribuídos a colaboradores. Cada cargo tem um código único, nome, descrição e estado activo/inactivo.

**Why this priority**: Os cargos são referenciados directamente pelo historial profissional dos colaboradores. Devem estar disponíveis antes de se poder registar colocações de colaboradores.

**Independent Test**: Pode ser testado de forma independente através de CRUD completo do catálogo de cargos, verificando unicidade de código e soft delete — entrega o catálogo pronto a referenciar por colocações.

**Acceptance Scenarios**:

1. **Given** nenhum cargo existe, **When** o administrador cria um cargo com `code`, `name` e `description` válidos, **Then** o cargo é criado com `is_active = true`.

2. **Given** um `code` de cargo já existe, **When** o administrador tenta criar outro cargo com o mesmo `code`, **Then** o sistema rejeita com HTTP 409.

3. **Given** um cargo existe e não está referenciado por historial profissional activo, **When** o administrador o desactiva, **Then** o cargo fica com `is_active = false`.

4. **Given** uma listagem de cargos é solicitada, **When** o estado `is_active` é passado como filtro, **Then** o sistema devolve apenas cargos no estado pedido.

5. **Given** um cargo existe, **When** é consultado o histórico de auditoria, **Then** são devolvidas as revisões registadas.

---

### User Story 3 — Gestão de Funções (Priority: P2)

O administrador de RH gere o catálogo de funções que podem ser atribuídas a colaboradores. Cada função tem um código único, nome, descrição e estado activo/inactivo.

**Why this priority**: Segue o mesmo padrão que os Cargos mas é referenciado de forma independente. Prioridade P2 pois a estrutura de Cargos é tratada primeiro por convenção de implementação incremental.

**Independent Test**: Pode ser testado de forma independente através de CRUD completo do catálogo de funções — entrega o catálogo pronto a referenciar por colocações.

**Acceptance Scenarios**:

1. **Given** nenhuma função existe, **When** o administrador cria uma função com `code`, `name` e `description` válidos, **Then** a função é criada com `is_active = true`.

2. **Given** um `code` de função já existe, **When** o administrador tenta criar outra função com o mesmo `code`, **Then** o sistema rejeita com HTTP 409.

3. **Given** uma função existe, **When** o administrador a desactiva, **Then** a função fica com `is_active = false`.

4. **Given** uma listagem de funções é solicitada com filtro de estado, **Then** o sistema devolve apenas funções no estado pedido.

5. **Given** uma função existe, **When** é consultado o histórico de auditoria, **Then** são devolvidas as revisões registadas.

---

### Edge Cases

- O que acontece quando se tenta desactivar uma unidade orgânica que tem sub-unidades activas? → Rejeitado com HTTP 409 e mensagem explícita.
- O que acontece quando `parent_unit_id` aponta para uma unidade inexistente? → Rejeitado com HTTP 404 ou 400 (referência inválida).
- O que acontece quando `parent_unit_id` aponta para uma unidade inactiva? → O sistema deve rejeitar a criação (a unidade-mãe deve estar activa).
- O que acontece quando se tenta criar um cargo/função com `code` duplicado? → HTTP 409.
- O que acontece quando `unit_type_option_id` não pertence ao grupo `UNIT_TYPE`? → Rejeitado com HTTP 400 (referência de opção inválida).
- O que acontece quando se tenta actualizar o `parent_unit_id` criando um ciclo na hierarquia? → Não é necessário validar ciclos nesta versão (profundidade determinada pelos dados, não pelo sistema).
- Como se comporta a paginação quando não existem registos? → Devolve lista vazia com `totalElements = 0`.
- O que acontece quando se tenta reactivar uma sub-unidade orgânica cuja unidade-mãe está inactiva? → Rejeitado com HTTP 409 (a unidade-mãe deve estar activa antes de reactivar a sub-unidade).

---

## Requirements

### Functional Requirements

- **FR-001**: O sistema DEVE permitir criar unidades orgânicas com `code` único, `name`, `acronym`, `unit_type_option_id` (FK → option onde ccode='UNIT_TYPE') e `parent_unit_id` nullable.
- **FR-002**: O sistema DEVE impedir a desactivação de uma unidade orgânica que tenha sub-unidades com `is_active = true`.
- **FR-003**: O sistema DEVE permitir listar unidades orgânicas com suporte a paginação e filtro por `is_active`, devolvendo uma lista plana onde cada unidade inclui `parent_unit_id` como referência. Não é exposta estrutura em árvore aninhada.
- **FR-004**: O sistema DEVE permitir consultar os detalhes de uma unidade orgânica por identificador.
- **FR-005**: O sistema DEVE permitir actualizar os campos de uma unidade orgânica (excluindo o `id`).
- **FR-006**: O sistema DEVE implementar soft delete para unidades orgânicas (`is_active = false`) e permitir a sua reactivação (`is_active = true`), exigindo que a unidade-mãe esteja activa no momento da reactivação.
- **FR-007**: O sistema DEVE registar auditoria (via Envers) em todas as operações de escrita sobre unidades orgânicas.
- **FR-008**: O sistema DEVE permitir criar cargos com `code` único, `name` e `description`.
- **FR-009**: O sistema DEVE permitir listar cargos com suporte a paginação e filtro por `is_active`.
- **FR-010**: O sistema DEVE implementar soft delete para cargos (`is_active = false`) e permitir a sua reactivação sem restrições adicionais.
- **FR-011**: O sistema DEVE registar auditoria (via Envers) em todas as operações de escrita sobre cargos.
- **FR-012**: O sistema DEVE permitir criar funções com `code` único, `name` e `description`.
- **FR-013**: O sistema DEVE permitir listar funções com suporte a paginação e filtro por `is_active`.
- **FR-014**: O sistema DEVE implementar soft delete para funções (`is_active = false`) e permitir a sua reactivação sem restrições adicionais.
- **FR-015**: O sistema DEVE registar auditoria (via Envers) em todas as operações de escrita sobre funções.
- **FR-016**: O sistema DEVE expor histórico de auditoria por entidade e por identificador para os três catálogos.
- **FR-017**: O sistema DEVE garantir unicidade de `code` dentro de cada catálogo (unidades orgânicas, cargos, funções separadamente).
- **FR-018**: O sistema DEVE validar que `unit_type_option_id` pertence ao grupo de opções com `ccode = 'UNIT_TYPE'`.

### Key Entities

- **OrganizationalUnit**: Unidade orgânica com hierarquia auto-referencial. Atributos: `id` (UUID), `code` (único), `name`, `acronym`, `unit_type_option_id` (FK → Option), `parent_unit_id` (FK → organizational_units, nullable), `is_active`. Relaciona-se com sub-unidades (1:N para si própria).
- **Job**: Cargo funcional. Atributos: `id` (UUID), `code` (único), `name`, `description`, `is_active`. Referenciado por `employee_professional_assignments`.
- **Function**: Função profissional. Atributos: `id` (UUID), `code` (único), `name`, `description`, `is_active`. Referenciado por `employee_professional_assignments`.

---

## Success Criteria

### Measurable Outcomes

- **SC-001**: O administrador consegue criar, consultar, actualizar e desactivar qualquer dos três catálogos em menos de 30 segundos por operação.
- **SC-002**: Uma tentativa de desactivar uma unidade com sub-unidades activas é rejeitada em 100% dos casos com mensagem de erro clara.
- **SC-003**: O histórico de auditoria de qualquer entidade fica disponível imediatamente após a operação que o gerou.
- **SC-004**: A listagem de qualquer catálogo com 1 000 registos é devolvida em menos de 2 segundos.
- **SC-005**: 100% das operações de escrita geram entradas de auditoria correctas (tipo INSERT, UPDATE ou DELETE).
- **SC-006**: Todos os erros de validação (código duplicado, referência inválida, desactivação bloqueada) são comunicados ao utilizador com mensagens descritivas no idioma português.

---

## Assumptions

- O catálogo de tipos de unidade (`UNIT_TYPE`) já existe na tabela `option_entity` e contém as opções necessárias (Direcção, Departamento, Divisão, Secção).
- O padrão hexagonal (domain/application/infrastructure/interfaces) do módulo `parametrizacoes/` já implementado serve de referência directa para este módulo.
- Os endpoints REST seguem a convenção `api/v1/rh/` do projecto, com prefixo do bounded context `estrutura/`.
- Não é necessário validar ciclos na hierarquia de unidades orgânicas (responsabilidade do administrador).
- A tabela `employee_professional_assignments` que referencia `jobs` e `functions` será implementada em BC futuro; neste módulo os catálogos são criados mas a FK não é ainda aplicada.
- Cache (Caffeine) será aplicada às operações de leitura dos três catálogos, seguindo o padrão do módulo `parametrizacoes/`.
- Não há suporte a múltiplos idiomas (locale) nestes catálogos — os nomes são em português.
- O suporte móvel está fora do âmbito desta feature.
