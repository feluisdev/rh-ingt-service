# Feature Specification: Dossier do Funcionário — Formações, Processos Disciplinares e Recibos

**Feature Branch**: `009-dossier-formacoes-disciplinar-recibos`
**Created**: 2026-05-02
**Status**: Draft

## Clarifications

### Session 2026-05-02

- Q: O FR-011 deve reflectir CRUD aberto nos processos disciplinares (sem restrição de perfil) nesta iteração, reservando a restrição ADMIN/OPERADOR para uma iteração posterior? → A: Sim — CRUD aberto nesta iteração; restrição de perfil implementada posteriormente.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Gestão de Formações Profissionais (Priority: P1)

O gestor de RH regista formações profissionais realizadas por um funcionário — cursos, seminários, congressos ou e-learning — para manter o dossier de desenvolvimento profissional actualizado. Pode consultar, criar, actualizar e remover formações, filtrando por ano quando necessário.

**Why this priority**: Formações são o sub-módulo mais simples e sem dependências. Fornece valor imediato ao dossier do funcionário sem necessidade de nova lógica de negócio complexa.

**Independent Test**: Pode ser testado criando, listando, actualizando e eliminando formações de um funcionário de teste sem necessidade dos outros sub-módulos.

**Acceptance Scenarios**:

1. **Given** um funcionário activo, **When** o gestor cria uma formação com nome e datas válidas, **Then** a formação é registada e visível na lista do funcionário.
2. **Given** uma formação registada, **When** o gestor actualiza os dados, **Then** as alterações são persistidas.
3. **Given** um funcionário com múltiplas formações, **When** o gestor filtra por ano, **Then** apenas as formações desse ano são devolvidas.
4. **Given** uma formação registada, **When** o gestor elimina, **Then** a formação deixa de aparecer na lista.
5. **Given** um funcionário inactivo, **When** o gestor tenta criar uma formação, **Then** a operação é recusada.
6. **Given** um `typeOptionKey` inválido, **When** o gestor cria uma formação, **Then** a validação rejeita com mensagem clara.

---

### User Story 2 — Gestão de Processos Disciplinares (Priority: P2)

O gestor de RH regista e gere processos disciplinares abertos contra funcionários. O processo inclui datas de abertura/encerramento, pena aplicada e referência ao Boletim Oficial. A restrição de acesso por perfil (ADMIN vs. OPERADOR) não está em âmbito nesta iteração — será implementada posteriormente.

**Why this priority**: Segue o mesmo padrão das formações. CRUD básico sem restrição de perfil nesta iteração.

**Independent Test**: Pode ser testado criando, listando e actualizando um processo disciplinar sem necessidade de autenticação por perfil.

**Acceptance Scenarios**:

1. **Given** um funcionário activo, **When** o gestor cria um processo disciplinar com data de abertura, **Then** o processo é registado com estado inicial.
2. **Given** um processo disciplinar registado, **When** o gestor actualiza com data de encerramento e pena, **Then** os dados são actualizados.
3. **Given** um funcionário sem processos, **When** é consultado, **Then** a lista devolvida está vazia.

---

### User Story 3 — Gestão de Recibos de Vencimento (Priority: P3)

O gestor de RH regista os recibos de vencimento mensais dos funcionários, associando-os ao PDF do recibo já carregado no sistema de documentos. O próprio funcionário autenticado pode consultar os seus recibos e fazer download do PDF através da área reservada `/me`.

**Why this priority**: Depende da infraestrutura de documentos já existente. Desbloqueia os endpoints `/me/payroll-slips` da área reservada.

**Independent Test**: Pode ser testado criando um recibo com referência a documento existente e verificando que o funcionário autenticado consegue listar e descarregar o seu próprio recibo.

**Acceptance Scenarios**:

1. **Given** um funcionário activo e um documento PDF previamente carregado, **When** o gestor cria um recibo com mês, ano, salários e referência ao documento, **Then** o recibo é registado.
2. **Given** um recibo já existente para o mesmo par (mês, ano, funcionário), **When** o gestor tenta criar outro, **Then** a operação é recusada por duplicado.
3. **Given** `grossSalary` inferior ou igual a zero, **When** o gestor cria um recibo, **Then** a validação rejeita.
4. **Given** `netSalary` superior a `grossSalary`, **When** o gestor cria um recibo, **Then** a validação rejeita.
5. **Given** o próprio funcionário autenticado, **When** acede a `/me/payroll-slips`, **Then** recebe apenas os seus próprios recibos.
6. **Given** o próprio funcionário autenticado, **When** pede download de um recibo alheio, **Then** recebe 404.

---

### Edge Cases

- Funcionário inactivo: operações de escrita (criar e actualizar formação, processo, recibo) são recusadas.
- `documentId` referencia documento inexistente: criar formação/processo/recibo com documento inválido deve ser rejeitado.
- `periodMonth` fora do intervalo 1-12: criação de recibo rejeitada.
- `typeOptionKey` de formação não pertencente ao ccode `TRAINING_TYPE`: valor inválido rejeitado.
- Download de recibo pelo funcionário autenticado: valida que o recibo pertence ao próprio antes de devolver URL.
- Paginação: listas com muitos registos devolvem resultados paginados.

## Requirements *(mandatory)*

### Functional Requirements

**Formações Profissionais**

- **FR-001**: O sistema DEVE permitir listar as formações de um funcionário, com filtro opcional por ano.
- **FR-002**: O sistema DEVE permitir consultar o detalhe de uma formação por identificador.
- **FR-003**: O sistema DEVE permitir criar uma formação com nome obrigatório, tipo (PRESENCIAL, ELEARNING, SEMINARIO, CONGRESSO), instituição, datas e duração em horas opcionais, e referência opcional a documento já carregado.
- **FR-004**: O sistema DEVE permitir actualizar os dados de uma formação existente.
- **FR-005**: O sistema DEVE permitir eliminar uma formação.
- **FR-006**: A criação e actualização de formações DEVE ser recusada se o funcionário estiver inactivo.

**Processos Disciplinares**

- **FR-007**: O sistema DEVE permitir listar os processos disciplinares de um funcionário.
- **FR-008**: O sistema DEVE permitir consultar o detalhe de um processo disciplinar.
- **FR-009**: O sistema DEVE permitir criar um processo disciplinar com data de abertura obrigatória e campos opcionais: número do processo, data de encerramento, pena, datas da pena, número do Boletim Oficial, observações e documento digitalizado.
- **FR-010**: O sistema DEVE permitir actualizar um processo disciplinar existente.
- **FR-011**: O sistema DEVE permitir CRUD completo de processos disciplinares sem restrição de perfil nesta iteração. A restrição de acesso (escrita: ADMIN; leitura: OPERADOR) será implementada numa iteração posterior.

**Recibos de Vencimento**

- **FR-012**: O sistema DEVE permitir listar os recibos de vencimento de um funcionário.
- **FR-013**: O sistema DEVE permitir consultar o detalhe de um recibo por identificador directo.
- **FR-014**: O sistema DEVE permitir criar um recibo com mês (1-12), ano, data de emissão, salário ilíquido e líquido (ambos positivos, líquido ≤ ilíquido) e referência obrigatória a documento PDF já carregado.
- **FR-015**: O sistema DEVE rejeitar criação de recibo duplicado para o mesmo par (funcionário, mês, ano).
- **FR-016**: O funcionário autenticado na área reservada DEVE conseguir listar os seus próprios recibos com filtros por ano e mês.
- **FR-017**: O funcionário autenticado DEVE conseguir obter o URL de download do PDF do seu próprio recibo, com validação de pertença.

### Key Entities

- **Formação (Training)**: Registo de uma acção de formação profissional de um funcionário. Atributos: nome, tipo, instituição, data início, data fim, duração em horas, referência a documento (opcional).
- **Processo Disciplinar (DisciplinaryProcess)**: Registo de um processo disciplinar aberto a um funcionário. Atributos: número do processo, data abertura, data encerramento, pena, datas da pena, boletim oficial, observações, referência a documento (opcional).
- **Recibo de Vencimento (PayrollSlip)**: Registo mensal do vencimento de um funcionário. Atributos: mês, ano, data emissão, salário ilíquido, salário líquido, referência a documento PDF (obrigatório). Unicidade por (funcionário, mês, ano).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Todos os endpoints de formações, processos e recibos respondem em menos de 500 ms em condições normais.
- **SC-002**: As listagens suportam paginação e devolvem resultados correctamente filtrados.
- **SC-003**: 100% das operações de escrita validam existência e estado activo do funcionário antes de persistir.
- **SC-004**: *(Diferido)* A restrição de perfil nos processos disciplinares será validada numa iteração posterior, após implementação do controlo de acesso por papel.
- **SC-005**: A unicidade de recibos por (funcionário, mês, ano) é garantida — zero duplicados possíveis.
- **SC-006**: O funcionário autenticado consegue aceder apenas aos seus próprios recibos via área reservada.

## Assumptions

- Os módulos reutilizam a infraestrutura de persistência e segurança já existente no BC `colaboradores/`.
- O upload de documentos não está em âmbito — `documentId` referencia sempre um documento já carregado via o módulo de Documentos existente.
- A autenticação e resolução de identidade do funcionário na área reservada reutiliza o `CurrentEmployeeResolver` implementado no #008.
- O tipo de formação é validado contra os valores existentes na tabela `option_entity` com `ccode = TRAINING_TYPE`; se não existirem registos de seed, a validação aceita qualquer valor não nulo e o seed é feito na migração.
- Não existe endpoint de aprovação/rejeição de processos disciplinares neste âmbito — apenas CRUD básico conforme v4.
- Os perfis de acesso (`ROLE_HR_ADMIN`, `ROLE_HR_OPERATOR`, `ROLE_SYSTEM_ADMIN`) já existem no sistema de autenticação e serão usados numa iteração futura para restringir o acesso aos processos disciplinares.
- A paginação segue o padrão já implementado nos outros módulos do BC `colaboradores/`.
