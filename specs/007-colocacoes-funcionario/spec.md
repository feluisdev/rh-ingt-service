# Feature Specification: Colocações de Funcionários

**Feature Branch**: `007-colocacoes-funcionario`  
**Created**: 2026-05-01  
**Status**: Draft  

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Registar Nova Colocação (Priority: P1)

O gestor de RH regista a colocação inicial de um funcionário numa unidade orgânica (ou a transferência para uma nova unidade), sabendo que o sistema fecha automaticamente a colocação anterior e passa a registar a nova como activa.

**Why this priority**: É a operação fundacional — sem colocações registadas não existe informação de onde o funcionário está. Qualquer perfil de funcionário, relatório ou integração depende disto.

**Independent Test**: Pode ser testado de forma independente criando um funcionário e registando a sua colocação inicial; o sistema deve responder com a colocação activa e o histórico de uma entrada.

**Acceptance Scenarios**:

1. **Given** um funcionário existe e não tem colocação activa, **When** o gestor regista uma nova colocação com unidade e data de início, **Then** a colocação fica activa (`is_current=true`) e o histórico mostra uma entrada.
2. **Given** um funcionário tem uma colocação activa, **When** o gestor regista uma nova colocação, **Then** a colocação anterior é fechada automaticamente (`end_date=hoje`, `is_current=false`) e a nova fica activa.
3. **Given** um funcionário não existe, **When** se tenta registar uma colocação, **Then** o sistema devolve erro 404.
4. **Given** uma unidade orgânica não existe, **When** se tenta registar uma colocação nessa unidade, **Then** o sistema devolve erro 404.
5. **Given** um cargo é indicado mas não existe, **When** se tenta registar a colocação, **Then** o sistema devolve erro 404.
6. **Given** a data de início é futura, **When** se tenta registar a colocação, **Then** o sistema devolve erro de validação (400).
7. **Given** o funcionário já tem colocações registadas, **When** o gestor tenta registar uma colocação com `assignmentType=INICIAL`, **Then** o sistema rejeita com erro 400 ("Tipo INICIAL apenas permitido na primeira colocação do funcionário").
8. **Given** o funcionário existe mas está inactivo, **When** o gestor tenta registar uma colocação, **Then** o sistema rejeita com erro 400 ("Não é possível registar colocações para funcionários inactivos").

---

### User Story 2 - Consultar Colocação Actual e Histórico (Priority: P2)

O gestor de RH ou o sistema consulta onde um funcionário está actualmente colocado e visualiza o historial completo das suas colocações anteriores.

**Why this priority**: A consulta é a operação mais frequente — usada em perfis de funcionários, relatórios e validações. Depende de US1 para ter dados.

**Independent Test**: Pode ser testado de forma independente com dados de colocações pré-existentes, verificando que a colocação actual é devolvida correctamente e que o histórico lista todas as anteriores.

**Acceptance Scenarios**:

1. **Given** um funcionário tem uma colocação activa, **When** se consulta `/colocacoes/atual`, **Then** é devolvida a colocação com `is_current=true`.
2. **Given** um funcionário não tem colocação activa, **When** se consulta `/colocacoes/atual`, **Then** o sistema devolve 404.
3. **Given** um funcionário tem várias colocações, **When** se lista `/colocacoes`, **Then** são devolvidas todas, ordenadas por data de início descendente.
4. **Given** o filtro `current=true` é aplicado, **When** se lista `/colocacoes`, **Then** apenas a colocação activa é devolvida.
5. **Given** um funcionário não existe, **When** se consulta qualquer endpoint de colocações, **Then** o sistema devolve 404.

---

### User Story 3 - Corrigir Dados de uma Colocação (Priority: P3)

O gestor de RH corrige observações ou datas de uma colocação já registada, sem criar uma nova colocação nem fechar a actual.

**Why this priority**: Operação de correcção necessária para manter a integridade dos dados históricos. Não é bloqueante para as operações principais.

**Independent Test**: Pode ser testado de forma independente actualizando as `notes` de uma colocação existente e verificando que os dados são actualizados sem alterar `is_current`.

**Acceptance Scenarios**:

1. **Given** uma colocação existe, **When** o gestor actualiza as observações, **Then** os dados são actualizados sem alterar o estado de `is_current`.
2. **Given** a `end_date` fornecida é anterior à `start_date`, **When** se tenta actualizar, **Then** o sistema devolve erro de validação (400).
3. **Given** a colocação não existe, **When** se tenta actualizar, **Then** o sistema devolve 404.

---

### User Story 4 - Soft Delete de Colocação Inactiva (Priority: P4)

O gestor de RH remove logicamente uma colocação que foi registada por engano, desde que não seja a colocação actualmente activa.

**Why this priority**: Operação de manutenção de dados. Não é possível eliminar a colocação actual para proteger a integridade do historial.

**Independent Test**: Pode ser testado de forma independente tentando apagar uma colocação inactiva (deve ter sucesso) e tentando apagar a colocação activa (deve falhar).

**Acceptance Scenarios**:

1. **Given** uma colocação inactiva existe, **When** o gestor a apaga, **Then** fica marcada como inactiva (`is_active=false`) e não aparece no histórico normal.
2. **Given** a colocação é a activa do funcionário (`is_current=true`), **When** se tenta apagar, **Then** o sistema recusa com erro 400.
3. **Given** a colocação não existe, **When** se tenta apagar, **Then** o sistema devolve 404.

---

### User Story 5 - Criação Automática de Colocação na Aprovação de Mobilidade (Priority: P5)

Quando uma licença/mobilidade é aprovada pelo gestor, o sistema cria automaticamente uma nova colocação do tipo MOBILIDADE para o funcionário, registando a nova afectação resultante da mobilidade.

**Why this priority**: Integração funcional entre o módulo de ausências/mobilidade e o de colocações. Mantém a consistência automática sem intervenção manual do gestor.

**Independent Test**: Pode ser testado aprovando uma licença de mobilidade e verificando que uma nova colocação do tipo MOBILIDADE é criada automaticamente no histórico do funcionário.

**Acceptance Scenarios**:

1. **Given** uma licença de mobilidade existe em estado PENDENTE, **When** o gestor a aprova, **Then** uma colocação do tipo MOBILIDADE é criada automaticamente e a colocação anterior é fechada.
2. **Given** a mobilidade aprovada não tem unidade de destino definida, **When** a aprovação é processada, **Then** o sistema cria a colocação com `unit_id=null` (sem unidade), sem falhar; o gestor pode depois corrigir a unidade via PUT.

---

### Edge Cases

- O que acontece se um funcionário tiver duas colocações com `is_current=true` em simultâneo (inconsistência de dados)?
- Como o sistema responde se a data de início da nova colocação for igual à data de início da colocação anterior?
- O que acontece ao histórico de colocações se um funcionário for desactivado?
- Como se comporta a listagem com filtros combinados (`current=false` + `active=false`)?

## Clarifications

### Session 2026-05-01

- Q: Quando uma mobilidade é aprovada sem `unit_id`, o sistema cria `Colocacao(unit_id=null)` ou copia a unidade anterior? → A: Cria `Colocacao(unit_id=null)`; o gestor corrige via PUT se necessário.
- Q: Deve o sistema rejeitar `assignmentType=INICIAL` se o funcionário já tem colocações registadas? → A: Sim, rejeita com 400 — INICIAL só é válido para a primeira colocação do funcionário.
- Q: Deve o sistema rejeitar a criação de colocações para funcionários inactivos? → A: Sim, rejeita com 400 — funcionários inactivos não recebem novas colocações.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE permitir registar uma colocação de um funcionário numa unidade orgânica, com data de início, tipo de afectação e cargo opcional.
- **FR-002**: Ao criar uma nova colocação, o sistema DEVE fechar automaticamente a colocação anterior activa do funcionário (definindo data de fim e marcando como inactiva).
- **FR-003**: Apenas uma colocação por funcionário DEVE ter o estado de colocação actual (`is_current=true`) em simultâneo.
- **FR-004**: O sistema DEVE validar que o funcionário existe antes de criar ou consultar colocações.
- **FR-005**: O sistema DEVE validar que a unidade orgânica existe antes de criar uma colocação.
- **FR-006**: O sistema DEVE validar que o cargo (se fornecido) existe antes de criar uma colocação.
- **FR-007**: A data de início de uma colocação NÃO DEVE ser posterior à data actual.
- **FR-008**: A data de fim (se fornecida) DEVE ser igual ou posterior à data de início.
- **FR-009**: O sistema DEVE disponibilizar um endpoint dedicado para obter a colocação actual de um funcionário.
- **FR-010**: O sistema DEVE disponibilizar a listagem do histórico completo de colocações de um funcionário, com filtro opcional por estado actual.
- **FR-011**: O sistema DEVE permitir corrigir observações e datas de uma colocação existente sem criar uma nova.
- **FR-012**: O sistema DEVE permitir soft delete de colocações inactivas (não-actuais), recusando a eliminação da colocação activa.
- **FR-013**: O tipo de afectação DEVE ser um dos valores pré-definidos: INICIAL, TRANSFERENCIA, MOBILIDADE, REQUISICAO.
- **FR-016**: O tipo `INICIAL` DEVE ser rejeitado (400) se o funcionário já tem pelo menos uma colocação registada (activa ou inactiva).
- **FR-017**: O sistema DEVE rejeitar (400) a criação de colocações para funcionários com estado INATIVO.
- **FR-014**: Quando uma licença/mobilidade é aprovada, o sistema DEVE criar automaticamente uma colocação do tipo MOBILIDADE.
- **FR-015**: O sistema DEVE manter auditoria completa de todas as alterações às colocações.

### Key Entities

- **Colocação** (`employee_unit_assignments`): Registo de afectação de um funcionário a uma unidade orgânica num período de tempo. Atributos principais: funcionário, unidade, cargo (opcional), data de início, data de fim, tipo, estado actual, observações, activo.
- **Funcionário**: Entidade existente — a colocação é sempre subordinada a um funcionário.
- **Unidade Orgânica**: Entidade existente — destino da colocação.
- **Cargo**: Entidade existente (opcional) — cargo exercido na unidade durante a colocação.
- **TipoAfectacao** (enum): INICIAL, TRANSFERENCIA, MOBILIDADE, REQUISICAO.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: O gestor de RH consegue registar a colocação inicial de um funcionário em menos de 1 minuto.
- **SC-002**: A consulta da colocação actual de um funcionário devolve resultado em menos de 500 ms para 95% dos pedidos.
- **SC-003**: A criação de uma nova colocação fecha automaticamente a anterior sem intervenção adicional do utilizador (0 passos manuais).
- **SC-004**: 100% das aprovações de mobilidade resultam na criação automática de uma colocação do tipo MOBILIDADE.
- **SC-005**: O histórico de colocações de um funcionário está sempre completo e ordenado cronologicamente, sem gaps nem duplicados.
- **SC-006**: Tentativas de eliminar a colocação activa são rejeitadas em 100% dos casos com mensagem clara.

## Assumptions

- Os utilizadores que gerem colocações têm o perfil de gestor de RH; não são previstas permissões granulares por tipo de operação nesta fase.
- A unidade de destino numa mobilidade é conhecida no momento da aprovação; se não estiver definida, a colocação pode ser registada sem unidade.
- O sistema não envia notificações ao funcionário quando a sua colocação muda (fora de âmbito nesta versão).
- A consulta de colocações de um funcionário devolve apenas registos activos (`is_active=true`) por omissão; registos soft-deleted são excluídos.
- Não é previsto suporte a colocações em múltiplas unidades em simultâneo (um funcionário, uma colocação activa de cada vez).
- O módulo de Licenças/Mobilidade já está implementado; a integração é feita estendendo o handler de aprovação existente.
