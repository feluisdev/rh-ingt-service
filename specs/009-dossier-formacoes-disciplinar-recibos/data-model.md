# Data Model: Dossier — Formações, Processos Disciplinares e Recibos

**Feature**: 009-dossier-formacoes-disciplinar-recibos | **Date**: 2026-05-02

---

## Entidades Novas

### Formação (Training)

**Tabela**: `t_training`

| Coluna | Tipo | Obrig. | Descrição |
|--------|------|--------|-----------|
| `id` | UUID PK | Sim | Identificador único |
| `funcionario_id` | UUID FK→t_funcionario | Sim | Funcionário a quem pertence |
| `name` | VARCHAR(255) | Sim | Designação da formação |
| `institution` | VARCHAR(255) | Não | Entidade formadora |
| `type_option_key` | VARCHAR(100) | Não | Tipo: PRESENCIAL, ELEARNING, SEMINARIO, CONGRESSO |
| `start_date` | DATE | Não | Data de início |
| `end_date` | DATE | Não | Data de fim |
| `duration_hours` | INTEGER | Não | Duração em horas |
| `document_id` | UUID | Não | Referência ao documento (certificado) — sem FK declarada |
| `created_date` | TIMESTAMP | Sim | Auditoria (Envers) |
| `created_by` | VARCHAR(255) | Sim | Auditoria (Envers) |
| `last_modified_date` | TIMESTAMP | Não | Auditoria (Envers) |
| `last_modified_by` | VARCHAR(255) | Não | Auditoria (Envers) |

**Índices**: `idx_training_funcionario (funcionario_id)`, `idx_training_year (funcionario_id, EXTRACT(YEAR FROM start_date))`

**Tabela de auditoria Envers**: `audit_schema.t_training_aud`

---

### Processo Disciplinar (DisciplinaryProcess)

**Tabela**: `t_disciplinary_process`

| Coluna | Tipo | Obrig. | Descrição |
|--------|------|--------|-----------|
| `id` | UUID PK | Sim | Identificador único |
| `funcionario_id` | UUID FK→t_funcionario | Sim | Funcionário visado |
| `process_number` | VARCHAR(100) | Não | Número do processo |
| `start_date` | DATE | Sim | Data de abertura |
| `end_date` | DATE | Não | Data de encerramento |
| `penalty` | VARCHAR(255) | Não | Pena aplicada |
| `penalty_start_date` | DATE | Não | Início do cumprimento da pena |
| `penalty_end_date` | DATE | Não | Fim do cumprimento da pena |
| `official_bulletin` | VARCHAR(100) | Não | Nº do Boletim Oficial |
| `notes` | TEXT | Não | Observações |
| `document_id` | UUID | Não | Referência ao processo digitalizado — sem FK declarada |
| `created_date` | TIMESTAMP | Sim | Auditoria (Envers) |
| `created_by` | VARCHAR(255) | Sim | Auditoria (Envers) |
| `last_modified_date` | TIMESTAMP | Não | Auditoria (Envers) |
| `last_modified_by` | VARCHAR(255) | Não | Auditoria (Envers) |

**Índices**: `idx_disciplinary_process_funcionario (funcionario_id)`

**Tabela de auditoria Envers**: `audit_schema.t_disciplinary_process_aud`

---

### Recibo de Vencimento (PayrollSlip)

**Tabela**: `t_payroll_slip`

| Coluna | Tipo | Obrig. | Descrição |
|--------|------|--------|-----------|
| `id` | UUID PK | Sim | Identificador único |
| `funcionario_id` | UUID FK→t_funcionario | Sim | Funcionário a quem pertence |
| `period_month` | SMALLINT | Sim | Mês de referência (1-12) |
| `period_year` | INTEGER | Sim | Ano de referência |
| `issue_date` | DATE | Sim | Data de emissão |
| `gross_salary` | NUMERIC(12,2) | Sim | Salário ilíquido (> 0) |
| `net_salary` | NUMERIC(12,2) | Sim | Salário líquido (> 0, ≤ gross_salary) |
| `document_id` | UUID | Sim | Referência ao PDF do recibo — sem FK declarada |
| `created_date` | TIMESTAMP | Sim | Auditoria (Envers) |
| `created_by` | VARCHAR(255) | Sim | Auditoria (Envers) |
| `last_modified_date` | TIMESTAMP | Não | Auditoria (Envers) |
| `last_modified_by` | VARCHAR(255) | Não | Auditoria (Envers) |

**Constraints**:
- `uq_payroll_slip_period UNIQUE (funcionario_id, period_month, period_year)`
- `chk_payroll_slip_month CHECK (period_month BETWEEN 1 AND 12)`
- `chk_payroll_slip_gross CHECK (gross_salary > 0)`
- `chk_payroll_slip_net CHECK (net_salary > 0 AND net_salary <= gross_salary)`

**Índices**: `idx_payroll_slip_funcionario (funcionario_id)`, `idx_payroll_slip_period (funcionario_id, period_year, period_month)`

**Tabela de auditoria Envers**: `audit_schema.t_payroll_slip_aud`

---

## Entidades Reutilizadas (sem alteração)

| Entidade | Tabela | Uso |
|----------|--------|-----|
| `FuncionarioEntity` | `t_funcionario` | FK e validação is_active |
| `DocumentoEntity` | `t_documento` | Validação de document_id antes de associar |
| `IAMUserProfileEntity` | `t_iam_user_profile` | Resolução de identidade em /me (já implementado) |

---

## Seed de Dados (Migration)

**option_entity** — inserir se não existir (ccode=`TRAINING_TYPE`):

| ccode | ckey | cvalue | locale |
|-------|------|--------|--------|
| TRAINING_TYPE | PRESENCIAL | Presencial | pt |
| TRAINING_TYPE | ELEARNING | E-Learning | pt |
| TRAINING_TYPE | SEMINARIO | Seminário | pt |
| TRAINING_TYPE | CONGRESSO | Congresso | pt |

---

## Migrações Flyway

| Versão | Ficheiro | Conteúdo |
|--------|----------|----------|
| V30 | `V30__create_training.sql` | `t_training` + audit table + índices + seed TRAINING_TYPE |
| V31 | `V31__create_disciplinary_process.sql` | `t_disciplinary_process` + audit table + índices |
| V32 | `V32__create_payroll_slip.sql` | `t_payroll_slip` + audit table + índices + constraints |

Todas com `IF NOT EXISTS` em todos os statements DDL.
