# Research: Dossier — Formações, Processos Disciplinares e Recibos

**Feature**: 009-dossier-formacoes-disciplinar-recibos | **Date**: 2026-05-02

Não existem NEEDS CLARIFICATION — o stack e os padrões estão completamente definidos pela constituição e pelos módulos anteriores. Este documento regista as decisões técnicas aplicáveis.

---

## D1 — Padrão CRUD Hexagonal (reutilização directa)

**Decision**: Os três sub-módulos seguem exactamente o padrão já estabelecido nos módulos `dependentes/`, `qualificacoes/` e `documentos/` do BC `colaboradores/`.

**Rationale**: Consistência máxima. Cada sub-módulo tem: entity JPA + `@Audited`, repositório JPA, domain model, domain repository port, mapper, DTOs, commands/queries + handlers, controller gerado por manifest IGRP Studio.

**Alternatives considered**: Partilhar um controller genérico de dossier — rejeitado (viola coesão e dificulta evolução independente de cada sub-módulo).

---

## D2 — Tabelas novas vs. reutilização

**Decision**: Três tabelas novas: `t_training`, `t_disciplinary_process`, `t_payroll_slip`. Todas com FK `funcionario_id → t_funcionario(id)`.

**Rationale**: As tabelas ainda não existem na BD (verificado no modelo relacional v4). Migrações Flyway defensivas (`IF NOT EXISTS` em todos os DDL).

**Alternatives considered**: Reutilizar `t_documento` como tabela genérica de dossier — rejeitado (tipos de dados muito diferentes, perderia validações específicas).

---

## D3 — Tipo de Formação via option_entity

**Decision**: O campo `typeOptionKey` em formações referencia a tabela `option_entity` com `ccode = TRAINING_TYPE`. O valor é armazenado como `String` na entidade (não FK directa).

**Rationale**: Padrão já usado em `DocumentTypeEntity` (categoryOptionId) e `LeaveMobilitySubtypeEntity`. Evita JOIN adicional em queries de listagem.

**Seed**: Migration V30 insere os 4 valores de seed (`PRESENCIAL`, `ELEARNING`, `SEMINARIO`, `CONGRESSO`) em `option_entity` se não existirem.

---

## D4 — documentId como referência fraca

**Decision**: Os campos `documentId` em formações, processos e `documentId` em recibos são `UUID` nullable (excepto recibos onde é obrigatório), armazenados como coluna simples sem FK declarada no DDL.

**Rationale**: O mesmo padrão usado em `t_documento.reference_id` (polimórfico). Evita FK rígida que impediria remoção de documentos; a integridade é garantida pela lógica aplicacional (verificar existência do documento antes de associar).

**Alternatives considered**: FK com `ON DELETE SET NULL` — rejeitado por acoplamento excessivo entre sub-módulos independentes.

---

## D5 — Restrição de acesso em Processos Disciplinares

**Decision**: As operações de escrita em processos disciplinares verificam `ROLE_HR_ADMIN` ou `ROLE_SYSTEM_ADMIN` no `SecurityContextHelper`. A leitura permite `ROLE_HR_OPERATOR`.

**Rationale**: A spec v4 é explícita nesta restrição. Em dev (sem security), o header `X-Employee-Id` não tem informação de perfil — handlers de processos disciplinares não verificam roles em modo dev para permitir testes.

**Implementation**: Verificação feita no handler com `SecurityContextHelper.hasAnyRole(...)`.

---

## D6 — /me/payroll-slips adicionado ao MeController existente

**Decision**: Os dois endpoints de recibos self-service (`GET /me/payroll-slips`, `GET /me/payroll-slips/{id}/download`) são adicionados ao `MeController` existente (atualização do manifest IGRP Studio).

**Rationale**: O `MeController` é o único ponto de entrada da área reservada do funcionário. Adicionar endpoints ao manifest existente é o fluxo natural.

**Handlers**: `GetMePayrollSlipsQueryHandler` e `GetMePayrollSlipDownloadQueryHandler` — seguem o mesmo padrão dos handlers de documentos já implementados.

---

## D7 — Unicidade de recibos

**Decision**: Constraint UNIQUE em `t_payroll_slip (funcionario_id, period_month, period_year)`. O handler de criação verifica duplicado antes de persistir (query prévia) e retorna HTTP 409 se existir.

**Rationale**: Verificação aplicacional além da constraint de BD dá melhor mensagem de erro ao utilizador. A constraint de BD é a rede de segurança.
