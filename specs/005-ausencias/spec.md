# Feature Specification: Módulo de Ausências — BC Colaboradores

**Feature Branch**: `005-ausencias`  
**Created**: 2026-05-01  
**Status**: Draft  

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Gestão de Catálogos de Ausência (Priority: P1)

O gestor de RH configura os tipos de ausência (ex: Férias, Doença, Maternidade), os subtipos de licença/mobilidade e os feriados nacionais/municipais. Estes catálogos são os alicerces de todo o módulo — sem eles não é possível registar pedidos nem calcular dias úteis.

**Why this priority**: Os catálogos são pré-requisitos de todas as outras histórias. Sem TipoAusencia não há PedidoAusencia; sem Feriados não há cálculo correcto de dias úteis.

**Independent Test**: Pode ser testado criando, editando, activando e desactivando registos em cada catálogo e verificando que os dados persistem correctamente.

**Acceptance Scenarios**:

1. **Given** o gestor acede ao endpoint de tipos de ausência, **When** cria um novo tipo com `codigo=FERIAS`, `deducts_balance=true`, `requires_approval=true`, `max_days_per_year=22`, **Then** o tipo é guardado com UUID gerado e `is_active=true`.
2. **Given** existe um tipo de ausência activo, **When** o gestor o desactiva, **Then** o tipo deixa de aparecer nas listagens activas mas continua acessível por ID.
3. **Given** o gestor cria um subtipo de licença/mobilidade com `record_type=AMBOS`, `affects_pay=false`, `counts_for_seniority=true`, **Then** o subtipo é guardado correctamente com todos os campos booleanos.
4. **Given** o gestor regista um feriado nacional com `data=2026-01-01`, **When** tenta registar outro feriado nacional na mesma data, **Then** recebe erro 409 (conflito por índice único parcial).
5. **Given** o gestor filtra feriados por `ano=2026` e `is_national=true`, **Then** obtém os 11 feriados nacionais de Cabo Verde previamente semeados.

---

### User Story 2 - Submissão e Decisão de Pedidos de Ausência (Priority: P2)

O funcionário submete um pedido de ausência indicando tipo, datas de início e fim. O sistema calcula automaticamente os dias úteis (excluindo fins-de-semana e feriados nacionais activos). O gestor de RH aprova, rejeita ou o funcionário cancela o pedido.

**Why this priority**: É o fluxo nuclear do módulo — todo o restante (saldos, licenças) depende deste workflow estar funcional.

**Independent Test**: Pode ser testado criando um funcionário, um tipo de ausência e submetendo um pedido; verificando o cálculo de dias úteis, a transição de estados e as restrições de sobreposição.

**Acceptance Scenarios**:

1. **Given** um funcionário com saldo suficiente, **When** submete pedido de `FERIAS` de `2026-06-01` a `2026-06-05` (5 dias de semana), **Then** o pedido é criado com `estado=PENDENTE` e `numero_dias=5`.
2. **Given** um pedido que abrange um feriado nacional (ex: `2026-06-22`), **When** o sistema calcula os dias úteis, **Then** o feriado é excluído e `numero_dias` é menor que os dias corridos.
3. **Given** um funcionário já tem pedido `APROVADO` ou `PENDENTE` para `2026-06-01` a `2026-06-10`, **When** submete novo pedido que sobrepõe essas datas, **Then** recebe HTTP 409.
4. **Given** um pedido `PENDENTE`, **When** o gestor aprova com `observacoes_decisao`, **Then** o estado passa a `APROVADO`, `aprovado_por` e `data_decisao` são preenchidos, e o saldo `dias_pendentes` é incrementado.
5. **Given** um pedido `PENDENTE`, **When** o gestor rejeita, **Then** o estado passa a `REJEITADO` e o saldo não é afectado.
6. **Given** um pedido `APROVADO` com `deducts_balance=true` e saldo insuficiente para novo pedido, **When** o gestor tenta aprovar um segundo pedido do mesmo tipo, **Then** recebe HTTP 422.
7. **Given** um pedido `REJEITADO`, **When** se tenta cancelar, **Then** recebe HTTP 409.
8. **Given** um pedido `PENDENTE` ou `APROVADO`, **When** é cancelado, **Then** o estado passa a `CANCELADO` e os saldos são revertidos se aplicável.

---

### User Story 3 - Consulta e Gestão de Saldos de Ausência (Priority: P3)

O gestor de RH e o funcionário consultam os saldos anuais de ausência por tipo. O sistema mantém `dias_direito`, `dias_gozados` e `dias_pendentes` actualizados automaticamente conforme os pedidos evoluem.

**Why this priority**: Depende dos pedidos (US2) estar funcional; é leitura e manutenção de estado derivado, não bloqueia o workflow de submissão.

**Independent Test**: Pode ser testado criando saldos manualmente, aprovando e completando pedidos e verificando que os contadores evoluem correctamente.

**Acceptance Scenarios**:

1. **Given** um saldo criado com `dias_direito=22`, `dias_gozados=0`, `dias_pendentes=0`, **When** um pedido de 5 dias é aprovado, **Then** `dias_pendentes` passa a 5.
2. **Given** `(funcionario_id, tipo_ausencia_id, ano)` já existe, **When** se tenta criar saldo duplicado, **Then** recebe HTTP 409.
3. **Given** o gestor consulta saldos de um funcionário, **Then** obtém lista com todos os tipos e anos registados.

---

### User Story 4 - Registo de Licenças e Mobilidade (Priority: P4)

O gestor de RH regista licenças e mobilidades dos funcionários (ex: licença sem vencimento, mobilidade para outro serviço), associando ao subtipo correspondente. Um registo sem `data_fim` indica que a situação está em curso.

**Why this priority**: Funcionalidade complementar — não bloqueia os fluxos principais de ausência; pode ser entregue numa iteração posterior sem impacto nas US1–US3.

**Independent Test**: Pode ser testado criando um subtipo de mobilidade e registando uma licença em curso (sem data_fim), depois encerrando-a com data_fim.

**Acceptance Scenarios**:

1. **Given** existe um subtipo de mobilidade, **When** o gestor regista uma mobilidade sem `data_fim`, **Then** o registo é criado com `is_active=true` e `data_fim=null`.
2. **Given** uma mobilidade em curso, **When** o gestor actualiza com `data_fim`, **Then** o registo reflecte o encerramento.
3. **Given** o gestor desactiva uma licença, **Then** `is_active=false` e o registo deixa de aparecer na listagem activa.

---

### Edge Cases

- O que acontece quando `data_fim < data_inicio` num pedido de ausência? → Deve retornar HTTP 422.
- O que acontece quando não existem feriados configurados para o ano do pedido? → O cálculo exclui apenas fins-de-semana.
- O que acontece quando `max_days_per_year` é atingido? → Bloqueio na submissão com HTTP 422.
- O que acontece quando se tenta activar um TipoAusencia já activo? → Retorna 200 (idempotente).
- Como se comporta o cálculo de `numero_dias` quando `data_inicio == data_fim` e é dia útil? → `numero_dias = 1`.
- O que acontece quando `data_inicio == data_fim` e é fim-de-semana ou feriado? → HTTP 422 (zero dias úteis).

## Requirements *(mandatory)*

### Functional Requirements

**Catálogos**

- **FR-001**: O sistema DEVE permitir criar, listar, editar, activar e desactivar TipoAusencia com os campos: `nome`, `codigo` (único), `deducts_balance`, `requires_approval`, `max_days_per_year` (opcional), `categoryOptionId`, `is_active`.
- **FR-002**: O sistema DEVE permitir criar, listar, editar, activar e desactivar SubtipoLicencaMobilidade com os campos: `nome`, `codigo` (único), `record_type` (LICENCA/MOBILIDADE/AMBOS), `affects_pay`, `counts_for_seniority`, `can_self_submit`, `is_active`.
- **FR-003**: O sistema DEVE permitir criar, listar, editar, activar e desactivar Feriados com os campos: `nome`, `data`, `is_national`, `municipio` (opcional, apenas para municipais), `is_active`.
- **FR-004**: O sistema DEVE impedir dois feriados nacionais activos na mesma data (índice único parcial).
- **FR-005**: O sistema DEVE semear automaticamente os 11 feriados nacionais de Cabo Verde para 2026 na migração de base de dados.
- **FR-006**: Os endpoints de feriados DEVEM suportar filtros por `ano` e `is_national`.

**Pedidos de Ausência**

- **FR-007**: O sistema DEVE permitir submeter um PedidoAusencia associado a um funcionário e tipo de ausência, com `data_inicio`, `data_fim` e `motivo` opcional.
- **FR-008**: O sistema DEVE calcular automaticamente `numero_dias` como o número de dias úteis entre `data_inicio` e `data_fim` (inclusive), excluindo sábados, domingos e feriados nacionais activos.
- **FR-009**: O sistema DEVE impedir pedidos com `data_fim < data_inicio` (HTTP 422).
- **FR-010**: O sistema DEVE impedir pedidos com zero dias úteis (HTTP 422).
- **FR-011**: O sistema DEVE impedir pedidos com datas que se sobreponham a pedidos `APROVADO` ou `PENDENTE` do mesmo funcionário (HTTP 409).
- **FR-012**: O sistema DEVE permitir aprovar um pedido `PENDENTE`, registando `aprovado_por`, `data_decisao` e `observacoes_decisao`.
- **FR-013**: O sistema DEVE bloquear a aprovação (HTTP 422) quando o tipo tem `deducts_balance=true` e: (a) não existe nenhum `SaldoAusencia` para o par `(funcionario_id, tipo_ausencia_id, ano)`, ou (b) o saldo disponível (`dias_direito - dias_gozados - dias_pendentes`) é inferior a `numero_dias` do pedido. O gestor deve criar o saldo manualmente antes de aprovar.
- **FR-014**: O sistema DEVE permitir rejeitar um pedido `PENDENTE`, registando `data_decisao` e `observacoes_decisao`.
- **FR-015**: O sistema DEVE permitir cancelar pedidos nos estados `PENDENTE` ou `APROVADO`; pedidos `REJEITADO` ou `CANCELADO` não podem ser cancelados (HTTP 409). O funcionário pode cancelar os seus próprios pedidos; o gestor de RH pode cancelar qualquer pedido.
- **FR-016**: Os endpoints de pedidos DEVEM suportar filtros por `estado`, `tipo_ausencia_id` e `ano`.

**Saldos de Ausência**

- **FR-017**: O sistema DEVE manter um SaldoAusencia por combinação única de `(funcionario_id, tipo_ausencia_id, ano)`.
- **FR-018**: Ao aprovar um pedido, o sistema DEVE incrementar `dias_pendentes` no saldo correspondente. O saldo deve existir previamente (ver FR-013); o sistema não cria saldos automaticamente na aprovação.
- **FR-019**: Ao cancelar um pedido `APROVADO`, o sistema DEVE decrementar `dias_pendentes` no saldo correspondente.
- **FR-020**: O sistema DEVE expor os saldos de um funcionário por endpoint dedicado.

**Licenças e Mobilidade**

- **FR-021**: O sistema DEVE permitir registar, listar, editar, activar e desactivar registos de LicencaMobilidade associados a um funcionário e subtipo.
- **FR-022**: Um registo de LicencaMobilidade sem `data_fim` representa uma situação em curso.

**Transversais**

- **FR-023**: Todas as entidades DEVEM ser auditadas (histórico completo de alterações).
- **FR-024**: Todos os IDs DEVEM ser UUID.
- **FR-025**: O soft delete DEVE ser implementado via `is_active` em todas as entidades.

### Key Entities

- **TipoAusencia**: Catálogo de tipos de ausência com regras de comportamento (deduz saldo, requer aprovação, limite anual).
- **SubtipoLicencaMobilidade**: Catálogo de subtipos para licenças/mobilidades com flags de impacto (vencimento, antiguidade, auto-submissão).
- **Feriado**: Calendário de feriados nacionais e municipais usado no cálculo de dias úteis.
- **PedidoAusencia**: Pedido formal de um funcionário para um período de ausência; percorre o ciclo PENDENTE → APROVADO/REJEITADO/CANCELADO.
- **SaldoAusencia**: Contador anual de dias de ausência por funcionário e tipo; mantido sincronizado com as decisões sobre pedidos.
- **LicencaMobilidade**: Registo de licenças especiais ou mobilidade para outro serviço; pode estar em curso (sem data_fim).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Um gestor consegue configurar um tipo de ausência completo (com todos os campos) em menos de 2 minutos.
- **SC-002**: O cálculo de dias úteis para qualquer pedido de ausência é apresentado imediatamente após submissão, sem atraso perceptível.
- **SC-003**: 100% dos pedidos com sobreposição de datas aprovadas são bloqueados pelo sistema sem necessidade de verificação manual.
- **SC-004**: Os saldos de ausência reflectem o estado correcto (aprovações, cancelamentos) em tempo real, sem necessidade de reconciliação manual.
- **SC-005**: O módulo suporta o ciclo completo de um pedido (submissão → aprovação → consulta de saldo) sem erros para os tipos de ausência configurados.
- **SC-006**: Os 11 feriados nacionais de Cabo Verde para 2026 estão disponíveis no sistema imediatamente após a instalação.

## Clarifications

### Session 2026-05-01

- Q: Quando `deducts_balance=true` e não existe SaldoAusencia para o ano, o que acontece ao aprovar? → A: Bloquear com HTTP 422; o gestor deve criar o saldo manualmente antes de aprovar.
- Q: A verificação de sobreposição de datas aplica-se apenas a pedidos APROVADO ou também a PENDENTE? → A: Bloquear sobreposição com pedidos APROVADO e PENDENTE (HTTP 409).
- Q: Quem pode cancelar um pedido de ausência? → A: O funcionário pode cancelar os seus próprios pedidos; o gestor de RH pode cancelar qualquer pedido.

## Assumptions

- O funcionário que submete o pedido é identificado pelo `funcionario_id` fornecido na rota; autenticação e autorização são geridas pela camada de segurança existente.
- O cálculo de dias úteis usa os feriados nacionais activos (`is_national=true AND is_active=true`); feriados municipais não são considerados no cálculo automático.
- O workflow de aprovação é de dois estados: ou o gestor aprova ou rejeita; não existe fluxo de aprovação multi-nível nesta fase.
- A actualização automática de `dias_gozados` (quando o período de ausência termina) está fora do âmbito desta fase — apenas `dias_pendentes` é gerido automaticamente.
- O `municipio` nos feriados municipais referencia o `ckey` de uma Option existente no catálogo partilhado.
- Os endpoints de catálogos (`/parametrizacoes/`) seguem a mesma estrutura REST dos módulos `estrutura/` e `carreiras/` já implementados.
- A auditoria Envers regista automaticamente todas as alterações; não é necessário endpoint de auditoria dedicado nesta fase (já existe o padrão do BC colaboradores).
