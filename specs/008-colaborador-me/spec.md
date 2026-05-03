# Feature Specification: Meu Perfil — Área Reservada do Colaborador

**Feature Branch**: `008-colaborador-me`  
**Created**: 2026-05-02  
**Status**: Draft

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Consultar o Meu Perfil (Priority: P1)

O colaborador autenticado acede à sua área reservada e consulta o seu perfil completo: dados de identificação pessoal, unidade orgânica actual, cargo e função correntes, carreira/categoria/escalão e estado profissional.

**Why this priority**: É o ponto de entrada da área reservada e agrega a informação mais consultada pelos colaboradores. Bloqueia a percepção de valor do módulo inteiro.

**Independent Test**: `GET /me/profile` devolve 200 com nome, NIF, unidade actual, cargo, carreira e data de admissão do colaborador autenticado.

**Acceptance Scenarios**:

1. **Given** um colaborador activo autenticado, **When** acede a `GET /me/profile`, **Then** recebe os seus dados pessoais, unidade orgânica actual, cargo/função actuais, carreira/categoria/escalão e data de admissão.
2. **Given** um colaborador com `is_active = false`, **When** acede a qualquer endpoint `/me`, **Then** recebe HTTP 403.
3. **Given** um colaborador autenticado, **When** inclui o identificador de outro colaborador no pedido, **Then** o sistema ignora-o e devolve sempre os dados do próprio.
4. **Given** um colaborador sem colocação actual, **When** acede a `GET /me/profile`, **Then** os campos de unidade orgânica surgem nulos (não retorna erro).
5. **Given** um JWT sem correspondência a nenhum funcionário no sistema, **When** acede a qualquer endpoint `/me`, **Then** recebe HTTP 404 com mensagem "Funcionário não encontrado para o utilizador autenticado".

---

### User Story 2 — Gerir as Minhas Ausências (Priority: P2)

O colaborador consulta o histórico dos seus pedidos de ausência e os saldos disponíveis, submete novos pedidos e cancela pedidos pendentes que já não pretende.

**Why this priority**: A gestão de ausências é a funcionalidade self-service mais utilizada, reduz a carga sobre o RH e tem impacto directo na satisfação dos colaboradores.

**Independent Test**: `GET /me/leave-requests` lista os pedidos do próprio; `POST /me/leave-requests` cria um pedido; `PUT /me/leave-requests/{id}/cancel` cancela um pedido PENDING.

**Acceptance Scenarios**:

1. **Given** um colaborador autenticado, **When** chama `GET /me/leave-requests`, **Then** recebe apenas os seus pedidos (nunca de terceiros), com suporte a filtros por estado, tipo e ano.
2. **Given** um colaborador autenticado, **When** chama `GET /me/leave-balances`, **Then** recebe os saldos do ano corrente por tipo de ausência (total atribuído, utilizado, pendente, disponível).
3. **Given** um colaborador autenticado, **When** submete `POST /me/leave-requests` com dados válidos, **Then** o pedido é criado em estado PENDING e as regras de negócio da secção 2.6 são aplicadas (sobreposição de datas, saldo suficiente, etc.).
4. **Given** um pedido em estado PENDING do próprio, **When** o colaborador chama `PUT /me/leave-requests/{id}/cancel`, **Then** o pedido passa a CANCELLED.
5. **Given** um pedido em estado APPROVED ou REJECTED, **When** o colaborador tenta cancelar, **Then** recebe HTTP 400.
6. **Given** um pedido de outro colaborador, **When** o próprio tenta cancelar, **Then** recebe HTTP 404 (isolamento de dados).

---

### User Story 3 — Consultar as Minhas Licenças e Mobilidades (Priority: P3)

O colaborador consulta as suas licenças e mobilidades e, quando o subtipo o permite, auto-submete um novo registo.

**Why this priority**: Complementa a gestão de ausências e cobre casos de mobilidade; a auto-submissão depende de configuração do subtipo.

**Independent Test**: `GET /me/leaves-mobilities` lista as licenças/mobilidades do próprio; `POST /me/leaves-mobilities` cria quando `canSelfSubmit=true`.

**Acceptance Scenarios**:

1. **Given** um colaborador autenticado, **When** chama `GET /me/leaves-mobilities`, **Then** recebe as suas licenças/mobilidades ordenadas por data de início descendente.
2. **Given** um subtipo com `canSelfSubmit = true`, **When** o colaborador submete `POST /me/leaves-mobilities`, **Then** o registo é criado com sucesso.
3. **Given** um subtipo com `canSelfSubmit = false`, **When** o colaborador tenta `POST /me/leaves-mobilities`, **Then** recebe HTTP 403 com mensagem explicativa.

---

### User Story 4 — Consultar os Meus Documentos (Priority: P4)

O colaborador consulta a lista dos seus documentos e obtém o URL de download de um documento específico.

**Why this priority**: Acesso aos documentos pessoais é uma necessidade básica do colaborador; requer validação de ownership no download.

**Independent Test**: `GET /me/documents` lista documentos do próprio; `GET /me/documents/{id}/download` devolve URL apenas para documentos do próprio.

**Acceptance Scenarios**:

1. **Given** um colaborador autenticado, **When** chama `GET /me/documents`, **Then** recebe apenas os seus documentos, com suporte a filtro por tipo.
2. **Given** um colaborador autenticado, **When** chama `GET /me/documents/{id}/download` para um documento seu, **Then** recebe o URL de download pré-assinado.
3. **Given** um documento pertencente a outro colaborador, **When** o próprio tenta obter o URL de download, **Then** recebe HTTP 404 (isolamento de dados).

---

### Edge Cases

- O que acontece quando o colaborador não tem colocação actual? → campos de unidade devolvidos como `null` no perfil.
- O que acontece quando o colaborador não tem enquadramento profissional actual? → campos de cargo/função devolvidos como `null`.
- O que acontece se o JWT não tiver correspondência a nenhum funcionário? → HTTP 404 "Funcionário não encontrado para o utilizador autenticado".
- O que acontece se o colaborador tentar aceder a recursos de outro com IDs na query string? → sistema ignora e filtra sempre pelo `funcionarioId` resolvido pelo `CurrentEmployeeResolver`.
- O que acontece se o pedido de ausência tem sobreposição de datas? → HTTP 400 com mensagem de negócio.
- O que acontece se o saldo de ausência for insuficiente? → HTTP 400 para tipos que descontam saldo.
- O que acontece se o utilizador autenticado não estiver sincronizado no perfil IAM local? → o `IAMUserProfileSyncFilter` sincroniza automaticamente no primeiro pedido.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE disponibilizar um endpoint `GET /me/profile` que devolva o perfil completo do colaborador autenticado, incluindo dados pessoais, unidade orgânica actual, cargo e função actuais, carreira/categoria/escalão e data de admissão.
- **FR-002**: O sistema DEVE garantir que todos os endpoints `/me` filtram exclusivamente pelos dados do colaborador resolvido pelo serviço `CurrentEmployeeResolver`, ignorando qualquer identificador enviado pelo cliente.
- **FR-003**: O sistema DEVE devolver HTTP 403 quando um colaborador com `is_active = false` acede a qualquer endpoint `/me`.
- **FR-004**: O sistema DEVE disponibilizar `GET /me/leave-requests` com filtros por estado (`PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`), tipo de ausência e ano.
- **FR-005**: O sistema DEVE disponibilizar `GET /me/leave-balances` que devolva, por tipo de ausência com desconto de saldo, o total atribuído, utilizado, em pendentes e disponível para o ano corrente.
- **FR-006**: O sistema DEVE disponibilizar `POST /me/leave-requests` através de um comando self-service dedicado (`SelfServiceCriarPedidoAusenciaCommand`) que injeta o `FuncionarioId` resolvido pelo `CurrentEmployeeResolver` e aplica as mesmas regras de negócio da criação pelo RH (sobreposição de datas, saldo, feriados, etc.), sem acoplamento ao handler HR existente.
- **FR-007**: O sistema DEVE disponibilizar `PUT /me/leave-requests/{id}/cancel` que apenas cancele pedidos em estado `PENDING` pertencentes ao próprio colaborador.
- **FR-008**: O sistema DEVE devolver HTTP 400 quando o colaborador tenta cancelar um pedido em estado diferente de `PENDING`.
- **FR-009**: O sistema DEVE devolver HTTP 404 quando o colaborador tenta operar sobre pedidos de ausência de outros colaboradores.
- **FR-010**: O sistema DEVE disponibilizar `GET /me/leaves-mobilities` que devolva as licenças/mobilidades do próprio, ordenadas por data de início descendente.
- **FR-011**: O sistema DEVE disponibilizar `POST /me/leaves-mobilities` apenas quando o subtipo de mobilidade tem `canSelfSubmit = true`; caso contrário devolve HTTP 403.
- **FR-012**: O sistema DEVE disponibilizar `GET /me/documents` com filtro opcional por tipo de documento, devolvendo apenas documentos pertencentes ao colaborador autenticado.
- **FR-013**: O sistema DEVE disponibilizar `GET /me/documents/{id}/download` que valide que o documento pertence ao colaborador autenticado antes de devolver o URL de download; caso contrário devolve HTTP 404.
- **FR-014**: O sistema DEVE registar todas as acções de escrita via `/me` em `change_history` com origem `self-service`.
- **FR-015**: O sistema DEVE expor um serviço `CurrentEmployeeResolver` em `shared/` que resolva o `FuncionarioId` do colaborador autenticado a partir do contexto de segurança (claim `sub` do JWT Keycloak), reutilizável por todos os handlers do módulo `/me`.
- **FR-016**: O sistema DEVE sincronizar automaticamente o perfil IAM (claims JWT: `sub`, `email`, `preferred_username`, `given_name`, `family_name`) para uma tabela local `t_iam_user_profile` em cada pedido autenticado, seguindo o padrão `IAMUserProfileSyncFilter` do projecto de referência.
- **FR-017**: O `CurrentEmployeeResolver` DEVE devolver HTTP 404 com mensagem "Funcionário não encontrado para o utilizador autenticado" quando o `sub` do JWT não tem correspondência a nenhum funcionário activo no sistema.
- **FR-018**: Todas as operações de escrita self-service (`POST /me/leave-requests`, `PUT /me/leave-requests/{id}/cancel`, `POST /me/leaves-mobilities`) DEVEM usar comandos dedicados com prefixo `SelfService` que recebem o `FuncionarioId` já resolvido, sem dependência directa nos handlers do RH — garantindo separação de responsabilidades em DDD.

### Key Entities

- **Perfil do Colaborador**: Agregação read-only de dados de funcionário, colocação actual, enquadramento profissional actual, carreira/categoria/escalão; não é uma entidade persistida separadamente.
- **IAM User Profile**: Registo local que sincroniza claims do JWT Keycloak (`sub`, `email`, `username`, `firstName`, `lastName`); tabela `t_iam_user_profile`; serve de ponte entre a identidade Keycloak e o `FuncionarioId` do sistema RH.
- **CurrentEmployeeResolver**: Serviço de domínio partilhado (`shared/`) que encapsula a lógica de resolução do `FuncionarioId` a partir do `sub` JWT; injectável em qualquer handler do módulo `/me`.
- **Pedido de Ausência (próprio)**: Pedido filtrado pelo `FuncionarioId` resolvido; partilha regras de negócio com criação pelo RH.
- **Saldo de Ausência (próprio)**: Saldo do colaborador para o ano corrente; apenas leitura.
- **Licença/Mobilidade (própria)**: Registo condicionado a `canSelfSubmit` do subtipo.
- **Documento (próprio)**: Documento com validação de ownership no acesso e download.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: O colaborador obtém o seu perfil completo em menos de 2 segundos após autenticação.
- **SC-002**: Zero registos de dados de terceiros são devolvidos em qualquer endpoint `/me`, independentemente dos parâmetros enviados pelo cliente.
- **SC-003**: 100% dos pedidos de colaboradores inactivos são rejeitados com HTTP 403, sem exposição de dados.
- **SC-004**: O colaborador consegue submeter um pedido de ausência em menos de 1 minuto através da área reservada.
- **SC-005**: A taxa de erros de isolamento de dados (colaborador A acede a dados de B) é zero em testes de penetração.
- **SC-006**: O perfil IAM é sincronizado automaticamente no primeiro pedido autenticado, sem intervenção manual do administrador.

---

## Assumptions

- O `sub` do JWT Keycloak é um UUID que identifica o utilizador no Keycloak, não o UUID do funcionário no sistema RH — a ligação é feita via `t_iam_user_profile` e o `CurrentEmployeeResolver`.
- A ligação entre o utilizador Keycloak e o funcionário RH é estabelecida via email (`t_iam_user_profile.email` = `t_funcionario.email`) na primeira sincronização.
- O `IAMUserProfileSyncFilter` segue o padrão do projecto de referência (`igrp_platform_process_manager_studio`) e é adicionado ao `shared/security/` do projecto RH.
- O `ApplicationAuditorAware` é actualizado para usar o `sub` do JWT (ou `username`) como autor de auditoria, em linha com o projecto de referência.
- Toda a infra de persistência (repositórios de ausências, licenças, documentos) já está implementada e é reutilizada com filtro adicional por `FuncionarioId`.
- A tabela `t_iam_user_profile` é nova (nova migração Flyway) e não interfere com tabelas existentes.
- `/me/payroll-slips` está fora de âmbito desta iteração (tabela `t_recibo` ainda não existe).
- `/me/external/evaluations` (integração SAD) está fora de âmbito desta iteração.
- O download de documentos gera um URL pré-assinado via MinIO (já implementado); a validação de ownership é feita antes de chamar o serviço de storage.
- Em perfil `development` (segurança desactivada), o `CurrentEmployeeResolver` usa o header de debug `X-Employee-Id` (UUID do funcionário) para simular a identidade, seguindo o padrão `X-Institution-Id` já existente no `SecurityContextHelper`.

## Clarifications

### Session 2026-05-02

- Q: Como identificar o funcionário a partir do JWT? → A: O `sub` do JWT é o UUID Keycloak (não o UUID do funcionário). Implementar `IAMUserProfileSyncFilter` + tabela `t_iam_user_profile` seguindo o padrão do projecto de referência `igrp_platform_process_manager_studio`. Criar serviço `CurrentEmployeeResolver` em `shared/` que resolve o `FuncionarioId` a partir do `sub`, reutilizável por todos os handlers, dentro dos padrões DDD do projecto.
- Q: Como ligar o perfil IAM ao Funcionário? → A: Criar `CurrentEmployeeResolver` como serviço de domínio partilhado em `shared/` que encapsula toda a lógica de resolução; segue padrões DDD e hexagonal do projecto RH; a ligação é feita via email no primeiro login (configurável no serviço).
- Q: `POST /me/leave-requests` — reutilizar handler existente ou criar comando dedicado? → A: Criar novos comandos self-service (`SelfServiceCriarPedidoAusenciaCommand`, etc.) que injectam o `FuncionarioId` já resolvido pelo `CurrentEmployeeResolver` e reutilizam a lógica de domínio internamente — separação limpa em DDD, sem acoplamento ao handler do RH.
