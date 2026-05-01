# Data Model: Módulo de Ausências

**Feature**: 005-ausencias  
**Data**: 2026-05-01  
**Migração**: V26__create_ausencias_tables.sql

---

## Entidades

### TipoAusencia (`t_leave_type`)

| Campo | Tipo | Restrições |
|---|---|---|
| `id` | UUID | PK, NOT NULL |
| `nome` | VARCHAR(150) | NOT NULL |
| `codigo` | VARCHAR(50) | NOT NULL, UNIQUE |
| `deducts_balance` | BOOLEAN | NOT NULL, DEFAULT false |
| `requires_approval` | BOOLEAN | NOT NULL, DEFAULT true |
| `max_days_per_year` | INTEGER | NULL (ilimitado) |
| `category_option_ckey` | VARCHAR(100) | NULL, referência a Option ckey |
| `is_active` | BOOLEAN | NOT NULL, DEFAULT true |
| `created_at` | TIMESTAMP | NOT NULL (Envers) |
| `updated_at` | TIMESTAMP | NOT NULL (Envers) |

**Índices**: `UNIQUE (codigo)`

---

### SubtipoLicencaMobilidade (`t_leave_mobility_subtype`)

| Campo | Tipo | Restrições |
|---|---|---|
| `id` | UUID | PK, NOT NULL |
| `nome` | VARCHAR(150) | NOT NULL |
| `codigo` | VARCHAR(50) | NOT NULL, UNIQUE |
| `record_type` | VARCHAR(20) | NOT NULL — valores: LICENCA, MOBILIDADE, AMBOS |
| `affects_pay` | BOOLEAN | NOT NULL, DEFAULT false |
| `counts_for_seniority` | BOOLEAN | NOT NULL, DEFAULT false |
| `can_self_submit` | BOOLEAN | NOT NULL, DEFAULT false |
| `is_active` | BOOLEAN | NOT NULL, DEFAULT true |
| `created_at` | TIMESTAMP | NOT NULL (Envers) |
| `updated_at` | TIMESTAMP | NOT NULL (Envers) |

**Índices**: `UNIQUE (codigo)`

---

### Feriado (`t_public_holiday`)

| Campo | Tipo | Restrições |
|---|---|---|
| `id` | UUID | PK, NOT NULL |
| `nome` | VARCHAR(150) | NOT NULL |
| `data` | DATE | NOT NULL |
| `is_national` | BOOLEAN | NOT NULL, DEFAULT true |
| `municipio_ckey` | VARCHAR(100) | NULL (apenas para municipais) |
| `is_active` | BOOLEAN | NOT NULL, DEFAULT true |
| `created_at` | TIMESTAMP | NOT NULL (Envers) |
| `updated_at` | TIMESTAMP | NOT NULL (Envers) |

**Índices**:
- `CREATE UNIQUE INDEX idx_feriado_nacional_data_unique ON t_public_holiday (data) WHERE is_national = true AND is_active = true`

**Seed data** (V26): 11 feriados nacionais de Cabo Verde para 2026:
- 2026-01-01 Ano Novo
- 2026-01-13 Dia da Liberdade e da Democracia
- 2026-02-20 Dia Nacional dos Heróis
- 2026-04-01 Quarta-feira de Cinzas (variável)
- 2026-04-03 Sexta-feira Santa (variável)
- 2026-05-01 Dia do Trabalho
- 2026-06-01 Dia das Crianças
- 2026-07-05 Dia da Independência Nacional
- 2026-08-15 Assunção de Nossa Senhora
- 2026-11-01 Dia de Todos os Santos
- 2026-12-25 Natal

---

### PedidoAusencia (`t_leave_request`)

| Campo | Tipo | Restrições |
|---|---|---|
| `id` | UUID | PK, NOT NULL |
| `funcionario_id` | UUID | NOT NULL, FK → t_funcionario(id) |
| `tipo_ausencia_id` | UUID | NOT NULL, FK → t_leave_type(id) |
| `data_inicio` | DATE | NOT NULL |
| `data_fim` | DATE | NOT NULL |
| `numero_dias` | INTEGER | NOT NULL (calculado) |
| `motivo` | TEXT | NULL |
| `estado` | VARCHAR(20) | NOT NULL, DEFAULT 'PENDENTE' — valores: PENDENTE, APROVADO, REJEITADO, CANCELADO |
| `aprovado_por` | UUID | NULL, FK → t_funcionario(id) |
| `data_decisao` | DATE | NULL |
| `observacoes_decisao` | TEXT | NULL |
| `is_active` | BOOLEAN | NOT NULL, DEFAULT true |
| `created_at` | TIMESTAMP | NOT NULL (Envers) |
| `updated_at` | TIMESTAMP | NOT NULL (Envers) |

**Índices**:
- `INDEX (funcionario_id, estado)` — para queries de sobreposição e filtros
- `INDEX (funcionario_id, data_inicio, data_fim)` — para detecção de sobreposição

**Regras de negócio**:
- `data_fim >= data_inicio` (validado no domínio)
- `numero_dias >= 1` (validado no domínio)
- Sobreposição verificada contra pedidos `APROVADO` e `PENDENTE` do mesmo funcionário
- Transições válidas: `PENDENTE → APROVADO`, `PENDENTE → REJEITADO`, `PENDENTE → CANCELADO`, `APROVADO → CANCELADO`

---

### SaldoAusencia (`t_leave_balance`)

| Campo | Tipo | Restrições |
|---|---|---|
| `id` | UUID | PK, NOT NULL |
| `funcionario_id` | UUID | NOT NULL, FK → t_funcionario(id) |
| `tipo_ausencia_id` | UUID | NOT NULL, FK → t_leave_type(id) |
| `ano` | INTEGER | NOT NULL |
| `dias_direito` | INTEGER | NOT NULL, DEFAULT 0 |
| `dias_gozados` | INTEGER | NOT NULL, DEFAULT 0 |
| `dias_pendentes` | INTEGER | NOT NULL, DEFAULT 0 |
| `created_at` | TIMESTAMP | NOT NULL (Envers) |
| `updated_at` | TIMESTAMP | NOT NULL (Envers) |

**Índices**:
- `UNIQUE (funcionario_id, tipo_ausencia_id, ano)`

**Regras de negócio**:
- `dias_direito >= 0`, `dias_gozados >= 0`, `dias_pendentes >= 0`
- Saldo disponível = `dias_direito - dias_gozados - dias_pendentes`
- Não existe criação automática na aprovação — deve ser criado manualmente pelo gestor

---

### LicencaMobilidade (`t_leave_mobility`)

| Campo | Tipo | Restrições |
|---|---|---|
| `id` | UUID | PK, NOT NULL |
| `funcionario_id` | UUID | NOT NULL, FK → t_funcionario(id) |
| `subtipo_id` | UUID | NOT NULL, FK → t_leave_mobility_subtype(id) |
| `data_inicio` | DATE | NOT NULL |
| `data_fim` | DATE | NULL (NULL = em curso) |
| `entidade_destino` | VARCHAR(200) | NULL |
| `despacho_numero` | VARCHAR(100) | NULL |
| `observacoes` | TEXT | NULL |
| `is_active` | BOOLEAN | NOT NULL, DEFAULT true |
| `created_at` | TIMESTAMP | NOT NULL (Envers) |
| `updated_at` | TIMESTAMP | NOT NULL (Envers) |

**Índices**:
- `INDEX (funcionario_id, is_active)` — para listagens activas

---

## Relacionamentos

```
t_funcionario (existente)
    │
    ├──< t_leave_request (funcionario_id)
    │       └──> t_leave_type (tipo_ausencia_id)
    │
    ├──< t_leave_balance (funcionario_id)
    │       └──> t_leave_type (tipo_ausencia_id)
    │
    └──< t_leave_mobility (funcionario_id)
            └──> t_leave_mobility_subtype (subtipo_id)

t_leave_request.aprovado_por ──> t_funcionario (self-reference, nullable)
```

---

## Value Objects (domínio)

| Value Object | Wraps |
|---|---|
| `TipoAusenciaId` | ExternalID (UUID) |
| `SubtipoLicencaMobilidadeId` | ExternalID (UUID) |
| `FeriadoId` | ExternalID (UUID) |
| `PedidoAusenciaId` | ExternalID (UUID) |
| `SaldoAusenciaId` | ExternalID (UUID) |
| `LicencaMobilidadeId` | ExternalID (UUID) |

---

## Serviço de Domínio

**`DiasUteisCalculator`** — `colaboradores/domain/service/`
- `int calcular(LocalDate inicio, LocalDate fim, Set<LocalDate> feriadosNacionais)`
- Lança `DomainException` se `inicio > fim` ou se resultado = 0
