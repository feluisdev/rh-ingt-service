# Feature Specification: Módulo de Colaboradores — Funcionário e Sub-domínios

**Feature Branch**: `004-colaboradores-funcionario`
**Created**: 2026-04-30
**Status**: Draft

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Gestão do Registo de Funcionário (Priority: P1)

O gestor de RH regista um novo funcionário no sistema, preenchendo os seus dados pessoais e de admissão. Após o registo, o funcionário fica acessível para consulta e para associação de enquadramento profissional, contratos e dependentes.

**Why this priority**: Sem o registo do funcionário nenhuma outra operação do módulo é possível. É o pré-requisito de todos os outros sub-domínios.

**Independent Test**: Criar um funcionário via POST, consultar via GET por ID e via listagem com filtros; actualizar dados; alterar situação profissional. Testável sem dependência de enquadramentos ou contratos.

**Acceptance Scenarios**:

1. **Given** um gestor de RH com acesso ao sistema, **When** submete dados válidos (nome, NIF, BI, data de admissão, género, situação profissional), **Then** o sistema gera automaticamente o `numero_funcionario` (ex: F000001), persiste o registo e retorna 201 com o ID e o número gerado.
2. **Given** um NIF já registado, **When** um segundo funcionário é criado com o mesmo NIF, **Then** o sistema retorna 409 Conflict.
3. **Given** um funcionário existente, **When** o gestor actualiza dados pessoais (telefone, morada, email), **Then** o sistema persiste e retorna 200 com os dados actualizados.
4. **Given** um funcionário activo, **When** o gestor altera a `situacao_profissional` para APOSENTADO com `data_saida`, **Then** o sistema aceita e reflecte o novo estado; o funcionário continua acessível para consulta histórica.
5. **Given** uma listagem de funcionários, **When** o gestor filtra por nome (parcial), NIF, situacao_profissional ou unidade_organica_id, **Then** apenas os funcionários correspondentes são retornados com paginação.

---

### User Story 2 — Enquadramento Profissional na Grelha PCFR (Priority: P2)

O gestor de RH enquadra um funcionário na grelha PCFR, associando-o a uma Carreira, Categoria, Escalão, Cargo e Unidade Orgânica. Ao criar um novo enquadramento, o anterior é automaticamente encerrado, preservando o histórico completo.

**Why this priority**: O enquadramento é o vínculo entre o funcionário e a estrutura organizacional e salarial. É necessário para cálculo de vencimentos e relatórios legais.

**Independent Test**: Criar dois enquadramentos sequenciais para o mesmo funcionário e verificar que o primeiro fica com `data_fim` preenchida e `is_current = false`, enquanto o segundo tem `is_current = true`.

**Acceptance Scenarios**:

1. **Given** um funcionário sem enquadramento, **When** o gestor cria um enquadramento com carreira, categoria, escalão, cargo, unidade orgânica e data de início válidos, **Then** o sistema persiste com `is_current = true` e retorna 201.
2. **Given** um funcionário com enquadramento actual, **When** o gestor cria um novo enquadramento com data de início posterior, **Then** o enquadramento anterior fica com `data_fim = data_inicio_novo - 1 dia` e `is_current = false`; o novo fica com `is_current = true`.
3. **Given** um funcionário com enquadramento, **When** o gestor consulta `GET /funcionarios/{id}/enquadramento`, **Then** retorna apenas o enquadramento com `is_current = true`.
4. **Given** um funcionário com enquadramento, **When** o gestor consulta `GET /funcionarios/{id}/enquadramentos/historico`, **Then** retorna todos os enquadramentos ordenados por data de início decrescente.
5. **Given** um ID de carreira inactiva, **When** o gestor tenta criar um enquadramento, **Then** o sistema retorna 422 indicando que a carreira não está activa.

---

### User Story 3 — Gestão de Contratos (Priority: P3)

O gestor de RH regista o vínculo laboral do funcionário através de contratos. Um funcionário pode ter múltiplos contratos ao longo do tempo, mas apenas um activo em simultâneo.

**Why this priority**: O contrato define o tipo de vínculo legal. Necessário para relatórios legais e para o módulo de ausências futuro.

**Independent Test**: Criar um contrato, verificar que é o único activo; tentar desactivar o único contrato activo e obter 409.

**Acceptance Scenarios**:

1. **Given** um funcionário existente, **When** o gestor cria um contrato com tipo, data de início e número de contrato, **Then** o sistema persiste e retorna 201.
2. **Given** um funcionário com um contrato activo, **When** o gestor tenta desactivar esse contrato, **Then** o sistema retorna 409 pois é o único contrato activo.
3. **Given** um funcionário com dois contratos (um activo, um encerrado), **When** o gestor tenta desactivar o contrato activo, **Then** o sistema retorna 409 pois continua a ser o único contrato activo, independentemente da situação profissional do funcionário.
4. **Given** um funcionário existente, **When** o gestor consulta `GET /funcionarios/{id}/contratos`, **Then** retorna todos os contratos ordenados por data de início decrescente.

---

### User Story 4 — Dependentes e Qualificações (Priority: P4)

O gestor de RH regista os familiares a cargo (dependentes) e as habilitações académicas/profissionais (qualificações) de um funcionário.

**Why this priority**: Necessário para cálculo de benefícios e perfil completo do colaborador. Pode ser implementado após as user stories anteriores sem bloquear funcionalidade core.

**Independent Test**: Adicionar dependente e qualificação a um funcionário, listar via sub-recurso, actualizar e remover (soft delete).

**Acceptance Scenarios**:

1. **Given** um funcionário existente, **When** o gestor adiciona um dependente (nome, parentesco, data nascimento), **Then** o sistema persiste e retorna 201.
2. **Given** um dependente existente, **When** o gestor o desactiva, **Then** o sistema marca `is_active = false` e retorna 200; o dependente não aparece na listagem activa mas permanece no histórico.
3. **Given** um funcionário existente, **When** o gestor adiciona uma qualificação (nível académico, curso, instituição, ano de conclusão), **Then** o sistema persiste e retorna 201.
4. **Given** um funcionário com dependentes e qualificações, **When** o gestor consulta `GET /funcionarios/{id}/dependentes` e `GET /funcionarios/{id}/qualificacoes`, **Then** cada sub-recurso retorna apenas os registos activos por omissão.

---

### Edge Cases

- Funcionário com NIF ou BI duplicado deve retornar 409 Conflict com mensagem clara.
- `numero_funcionario` é gerado automaticamente e nunca pode ser alterado após criação.
- `nif` e `bi_numero` vêm de um sistema externo (lookup) e são mutáveis para permitir correcção de ligação errada; qualquer alteração fica registada em auditoria.
- Criação de enquadramento com `data_inicio` anterior ao enquadramento actual existente deve ser rejeitada com 422.
- Tentativa de associar carreira/categoria/escalão inactivos a um enquadramento deve retornar 422.
- Cargo e unidade orgânica são validados individualmente (activos/existentes); não existe validação cruzada entre si pois `t_cargo` é catálogo global sem associação directa a unidades orgânicas.
- Desactivação do único contrato activo deve retornar 409 **sempre**, independentemente da situação profissional do funcionário.
- Listagem de funcionários sem filtros deve retornar apenas `is_active = true` por omissão; parâmetro `active=false` devolve os inactivos.
- `data_saida` só pode ser preenchida se `situacao_profissional` for INATIVO ou APOSENTADO.

## Requirements *(mandatory)*

### Functional Requirements

**Funcionário:**

- **FR-001**: O sistema DEVE gerar automaticamente um `numero_funcionario` único com formato F seguido de 6 dígitos (ex: F000001) no momento de criação, imutável após esse momento.
- **FR-002**: O sistema DEVE impedir o registo de dois funcionários com o mesmo NIF ou o mesmo número de BI, retornando 409 Conflict.
- **FR-003**: O gestor DEVE poder filtrar a listagem de funcionários por nome (pesquisa parcial), NIF, situação profissional, unidade orgânica e carreira, com paginação.
- **FR-004**: A situação profissional DEVE ser gerida por um catálogo de opções (ATIVO, INATIVO, APOSENTADO, CEDIDO, etc.); a transição de estado DEVE ser registada com data de saída quando aplicável. O campo `data_saida` só é aceite quando `situacao_profissional` ≠ ATIVO; caso contrário retornar 400.
- **FR-005**: O sistema DEVE manter auditoria de todas as alterações ao registo do funcionário.

**Enquadramento Profissional:**

- **FR-006**: O sistema DEVE permitir criar um enquadramento profissional associando um funcionário a Carreira, Categoria, Escalão (todos activos), Cargo e Unidade Orgânica, com data de início.
- **FR-007**: Ao criar um novo enquadramento, o sistema DEVE encerrar automaticamente o enquadramento anterior (`data_fim = data_inicio_novo - 1 dia`, `is_current = false`).
- **FR-008**: O sistema DEVE manter o histórico completo de enquadramentos por funcionário, acessível via `GET /funcionarios/{id}/enquadramentos/historico`.
- **FR-009**: O sistema DEVE rejeitar enquadramentos que referenciem entidades inactivas (carreira, categoria, escalão, cargo ou unidade orgânica inactivos) com HTTP 422.
- **FR-010**: A data de início de um novo enquadramento DEVE ser posterior à data de início do enquadramento actual, caso exista.

**Contrato:**

- **FR-011**: O sistema DEVE permitir registar múltiplos contratos por funcionário, mas apenas um pode ter `is_active = true` em simultâneo. Se já existir um contrato activo, a criação de um novo contrato activo deve ser rejeitada com 409 — o gestor deve encerrar explicitamente o contrato anterior antes de criar um novo.
- **FR-012**: O sistema DEVE impedir a desactivação do único contrato activo de um funcionário, retornando 409 Conflict.
- **FR-013**: O `numero_contrato`, quando fornecido, DEVE ser único globalmente.

**Dependentes e Qualificações:**

- **FR-014**: O sistema DEVE suportar soft delete (is_active = false) para dependentes e qualificações; registos inactivos não aparecem na listagem por omissão.
- **FR-015**: A listagem de dependentes e qualificações por funcionário DEVE ser acessível via sub-recursos (`/funcionarios/{id}/dependentes`, `/funcionarios/{id}/qualificacoes`).

### Key Entities

- **Funcionário**: Pessoa ao serviço da instituição; identificado por NIF, BI e número de funcionário gerado; tem situação profissional, dados de contacto, e ligações a enquadramentos, contratos, dependentes e qualificações.
- **EnquadramentoProfissional**: Posição do funcionário na grelha PCFR em determinado período; liga ao Funcionário, Carreira, Categoria, Escalão, Cargo e Unidade Orgânica; apenas um é corrente por funcionário.
- **Contrato**: Vínculo laboral formal com tipo, datas e número; apenas um activo por funcionário.
- **Dependente**: Familiar a cargo do funcionário; tem nome, parentesco e data de nascimento; soft delete.
- **Qualificação**: Habilitação académica ou profissional; tem nível académico, curso e instituição; soft delete.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: O gestor de RH consegue registar um novo funcionário completo (dados pessoais + enquadramento + contrato) em menos de 5 minutos.
- **SC-002**: A pesquisa de funcionários com qualquer combinação de filtros devolve resultados em menos de 2 segundos para até 10 000 registos.
- **SC-003**: O histórico de enquadramentos profissionais está sempre completo e consistente — nenhum período fica sem cobertura após a criação do primeiro enquadramento.
- **SC-004**: 100% das regras de unicidade (NIF, BI, numero_funcionario, numero_contrato) são garantidas pelo sistema sem intervenção manual.
- **SC-005**: Todas as alterações a dados de funcionários ficam registadas em auditoria com utilizador e timestamp.

## Assumptions

- Os valores dos campos Option (genero, estado_civil, situacao_profissional, parentesco, nivel_academico, nacionalidade, pais) existem já na tabela `t_option` e são referenciados pelo `ckey`; a validação de existência não é feita em runtime nesta fase (assume-se dados válidos).
- A geração do `numero_funcionario` é feita por sequência de base de dados ou lógica atómica no handler para garantir unicidade sem colisões em concorrência.
- A foto do funcionário (`foto_url`) é um URL para armazenamento externo (MinIO); o upload em si não faz parte deste BC.
- A autorização de acesso (quem pode criar/editar/ver funcionários) é gerida pela camada de segurança OAuth2 + Keycloak e está fora do âmbito desta especificação.
- O campo `data_saida` é preenchido manualmente pelo gestor; não existe lógica automática de desligamento.
- `numero_contrato` é opcional; quando não fornecido, o campo fica nulo (sem geração automática).
- `is_active` no Funcionário é derivado automaticamente: `true` quando `situacao_profissional = ATIVO`, `false` nos restantes casos (INATIVO, APOSENTADO, CEDIDO, etc.); não é gerido manualmente.
- A listagem de funcionários inclui apenas `is_active = true` por omissão; o parâmetro `active` permite filtrar inactivos.
- Escalões inactivos num enquadramento já criado não são retroactivamente invalidados — a verificação de activo é apenas no momento de criação.

## Clarifications

### Sessão 2026-04-30

- Q: Deve existir validação em runtime da existência do ckey no t_option para campos Option? → A: Não nesta fase; assume-se dados válidos vindos de UI controlada.
- Q: O `numero_contrato` é gerado automaticamente ou livre? → A: Campo livre e opcional; quando fornecido deve ser único.
- Q: A desactivação do único contrato activo deve ser sempre bloqueada (409) ou permitida quando o funcionário está INATIVO/APOSENTADO? → A: Sempre bloquear, independentemente da situação profissional do funcionário.
- Q: Quando `is_active` muda para `false` no Funcionário — é derivado automaticamente ou gerido manualmente? → A: Derivado automaticamente de `situacao_profissional`: `true` quando ATIVO, `false` em todos os outros estados.
- Q: O sistema deve validar que o cargo pertence à unidade orgânica no enquadramento? → A: Sim, validar sempre; retornar 422 se inconsistente.
- Q: O que acontece ao criar um novo contrato quando já existe um contrato activo? → A: Rejeitar com 409; o gestor deve encerrar explicitamente o contrato anterior antes de criar um novo.
- Q: NIF e BI são imutáveis após criação? → A: Mutáveis — vêm de lookup em sistema externo; correcção de ligação errada deve ser possível; auditoria Envers regista qualquer alteração.
