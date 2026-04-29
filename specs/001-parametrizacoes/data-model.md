# Phase 1 — Data Model

**Feature**: Catálogos de Parametrização do Módulo RH
**Branch**: `001-parametrizacoes`
**Date**: 2026-04-29

Este documento detalha as 8 entidades JPA do módulo `parametrizacoes`, os seus atributos, relações, regras de validação e estados. As entidades estendem `cv.igrp.RH_Service.shared.config.AuditEntity` (auditoria automática `created_at/by`, `updated_at/by`) e são marcadas com `@Audited` para Hibernate Envers.

Todas as entidades:

- Usam `UUID` como PK (`id`), gerados via `gen_random_uuid()` na migration ou via `ExternalID.gerarNovo()` no domínio.
- Têm `is_active BOOLEAN NOT NULL DEFAULT TRUE` para soft delete.
- Têm a coluna `code` imutável após inserção (validado no domain layer).
- Estão sob audit via Envers (tabelas `*_AUD` geradas automaticamente).

---

## 1. OptionEntity (`t_option_entity`)

Catálogo genérico que serve 11 grupos de etiquetas configuráveis. Já existe — esta feature move-a de `shared/` para `parametrizacoes/` e garante que o seed está completo.

| Coluna | Tipo | Constraints | Descrição |
|---|---|---|---|
| `id` | UUID | PK | Identificador único |
| `ccode` | VARCHAR(50) | NOT NULL | Código do grupo (ex: `MARITAL_STATUS`, `SEX`, `NATIONALITY`) |
| `ckey` | VARCHAR(50) | NOT NULL | Chave da opção dentro do grupo (ex: `SOLTEIRO`, `M`, `CV`) |
| `cvalue` | VARCHAR(200) | NOT NULL | Label de exibição (ex: `Solteiro(a)`, `Masculino`) |
| `locale` | VARCHAR(10) | NOT NULL DEFAULT `'pt-CV'` | Idioma da entrada |
| `sort_order` | INTEGER | DEFAULT 0 | Ordem de exibição em dropdowns |
| `active` | BOOLEAN | NOT NULL DEFAULT TRUE | Estado lógico |
| `description` | TEXT | NULL | Descrição opcional |
| `created_at, created_by, updated_at, updated_by` | (auditoria) | | Herdados de `AuditEntity` |

**Constraints únicas**:
- `UQ (ccode, ckey, locale)` — uma entrada única por par grupo/chave/idioma.

**Índices**:
- `idx_option_ccode_locale_active (ccode, locale, active)` — para `ReferenceLookupService.findByCcode()`.

**Domain rules**:
- `ccode` é imutável após criação.
- `ckey` é imutável após criação.
- `locale` é imutável após criação.
- `cvalue`, `sort_order`, `active`, `description` são editáveis.
- Um `(ccode, ckey)` pode ter múltiplas linhas — uma por idioma.
- O idioma `pt-CV` é mandatório no seed (FR-021); outros idiomas opcionais.

**Grupos pré-definidos** (`ccode` aceites, sem possibilidade de extensão sem alteração de código):

`MARITAL_STATUS`, `SEX`, `NATIONALITY`, `UNIT_TYPE`, `DOC_CATEGORY`, `LEAVE_CATEGORY`, `QUALIFICATION_LEVEL`, `RELATIONSHIP_TYPE`, `ISLAND`, `CONCELHO`, `TRAINING_TYPE`.

---

## 2. WorkerStateEntity (`t_worker_state`)

Catálogo dos estados do trabalhador, com flag `is_core` que protege estados núcleo do sistema contra desactivação acidental.

| Coluna | Tipo | Constraints | Descrição |
|---|---|---|---|
| `id` | UUID | PK | |
| `code` | VARCHAR(30) | UNIQUE NOT NULL | `ACTIVE`, `INACTIVE`, `SUSPENDED` |
| `name` | VARCHAR(100) | NOT NULL | Designação |
| `description` | TEXT | NULL | Descrição |
| `is_core` | BOOLEAN | NOT NULL DEFAULT FALSE | `TRUE` = não pode ser desactivado |
| `is_active` | BOOLEAN | NOT NULL DEFAULT TRUE | Estado lógico |
| `created_at, created_by, updated_at, updated_by` | (auditoria) | | |

**Domain rules**:
- `code` é imutável após criação.
- Quando `is_core = TRUE`, qualquer tentativa de `is_active = FALSE` deve falhar com `IgrpResponseStatusException(409, "Estado núcleo não pode ser desactivado")`.
- `is_core` só pode ser alterado por administrador de sistema (não administrador de RH normal).

**Seed obrigatório**:
- `ACTIVE` — `is_core = TRUE`
- `INACTIVE` — `is_core = TRUE`
- `SUSPENDED` — `is_core = FALSE`

---

## 3. ProfessionalSituationEntity (`t_professional_situation`)

Situações profissionais distintas no PCFR (Decreto-Lei 4/2024).

| Coluna | Tipo | Constraints | Descrição |
|---|---|---|---|
| `id` | UUID | PK | |
| `code` | VARCHAR(30) | UNIQUE NOT NULL | `EFETIVO`, `CONTRATADO`, `COMISSIONADO`, `ESTAGIARIO` |
| `name` | VARCHAR(100) | NOT NULL | |
| `description` | TEXT | NULL | |
| `is_active` | BOOLEAN | NOT NULL DEFAULT TRUE | |
| `created_at, created_by, updated_at, updated_by` | (auditoria) | | |

**Domain rules**:
- `code` imutável após criação.
- Não há flag `is_core` aqui; mas o `ON DELETE` é bloqueado se referenciado por funcionário activo (verificado em features posteriores).

**Seed**:
- `EFETIVO`, `CONTRATADO`, `COMISSIONADO`, `ESTAGIARIO`.

---

## 4. ContractTypeEntity (`t_contract_type`)

Tipos de contrato com implicações legais distintas.

| Coluna | Tipo | Constraints | Descrição |
|---|---|---|---|
| `id` | UUID | PK | |
| `code` | VARCHAR(50) | UNIQUE NOT NULL | `NOMEACAO_DEFINITIVA`, `CFP`, `CTFP_TERMO_CERTO`, `CTFP_TERMO_INCERTO`, `COMISSAO_SERVICO` |
| `name` | VARCHAR(150) | NOT NULL | |
| `description` | TEXT | NULL | Base legal aplicável |
| `is_active` | BOOLEAN | NOT NULL DEFAULT TRUE | |
| `created_at, created_by, updated_at, updated_by` | (auditoria) | | |

**Domain rules**:
- `code` imutável após criação.
- Bloqueio de desactivação se referenciado por contratos em curso (`employee_contracts.is_current = TRUE` na feature `feat/vida-profissional`).

**Seed**:
- Os 5 tipos do Decreto-Lei 4/2024.

---

## 5. DocumentTypeEntity (`t_document_type`)

Tipos de documento com extensões permitidas e categoria. Esta tabela é a evolução de `t_tipo_documento` existente — refactor inclui rename + colunas novas.

| Coluna | Tipo | Constraints | Descrição |
|---|---|---|---|
| `id` | UUID | PK | |
| `code` | VARCHAR(30) | UNIQUE NOT NULL | `CNI`, `PASSAPORTE`, `CONTRATO`, `CERTIDAO`, `HABILITACAO`, ... |
| `name` | VARCHAR(100) | NOT NULL | |
| `description` | TEXT | NULL | |
| `category_option_id` | UUID | FK→t_option_entity (`ccode='DOC_CATEGORY'`) | Categoria do documento |
| `allowed_extensions` | VARCHAR(200) | NULL | Lista delimitada por vírgulas: `pdf,jpg,png` |
| `is_active` | BOOLEAN | NOT NULL DEFAULT TRUE | |
| `created_at, created_by, updated_at, updated_by` | (auditoria) | | |

**Domain rules**:
- `code` imutável.
- `allowed_extensions` opcional; quando preenchido, validação à hora de upload aplica-se na feature `feat/documentos`.
- `category_option_id` referencia uma entry de `t_option_entity` cujo `ccode = 'DOC_CATEGORY'`.

**Seed**:
- `CNI`, `PASSAPORTE`, `CONTRATO`, `CERTIDAO`, `HABILITACAO`, `FORMACAO`, `DISCIPLINAR`, `RECIBO`, `JUSTIFICATIVO`, `OUTRO` — com extensões e categorias apropriadas.

---

## 6. LeaveTypeEntity (`t_leave_type`)

Tipos de ausência com flags que controlam o processamento.

| Coluna | Tipo | Constraints | Descrição |
|---|---|---|---|
| `id` | UUID | PK | |
| `code` | VARCHAR(30) | UNIQUE NOT NULL | `FERIAS`, `DOENCA`, `MATERNIDADE`, ... |
| `name` | VARCHAR(100) | NOT NULL | |
| `description` | TEXT | NULL | |
| `category_option_id` | UUID | FK→t_option_entity (`ccode='LEAVE_CATEGORY'`) NULL | Categoria agrupadora |
| `deducts_balance` | BOOLEAN | NOT NULL DEFAULT TRUE | Desconta saldo anual |
| `requires_approval` | BOOLEAN | NOT NULL DEFAULT TRUE | Exige aprovação chefia |
| `max_days_per_year` | INTEGER | NULL | `null` = sem limite legal |
| `is_active` | BOOLEAN | NOT NULL DEFAULT TRUE | |
| `created_at, created_by, updated_at, updated_by` | (auditoria) | | |

**Domain rules**:
- `code` imutável.
- `category_option_id` referencia entry de `t_option_entity` com `ccode='LEAVE_CATEGORY'` (validado por trigger ou no serviço).
- `max_days_per_year` quando preenchido é validado em `feat/ausencias` no momento do pedido.

**Seed** (segundo Lei 20/X/2023):
- `FERIAS` (deducts=true, requires_approval=true, max=22)
- `DOENCA` (deducts=false, requires_approval=true, max=null)
- `MATERNIDADE` (deducts=false, requires_approval=false, max=120)
- `PATERNIDADE` (deducts=false, requires_approval=false, max=20)
- `LUTO` (deducts=false, requires_approval=false, max=5)
- `CASAMENTO` (deducts=false, requires_approval=false, max=8)

---

## 7. LeaveMobilitySubtypeEntity (`t_leave_mobility_subtype`)

Subtipos detalhados de licença e mobilidade.

| Coluna | Tipo | Constraints | Descrição |
|---|---|---|---|
| `id` | UUID | PK | |
| `code` | VARCHAR(50) | UNIQUE NOT NULL | |
| `name` | VARCHAR(150) | NOT NULL | |
| `description` | TEXT | NULL | |
| `record_type` | VARCHAR(20) | NOT NULL | `LICENCA`, `MOBILIDADE`, `AMBOS` |
| `affects_pay` | BOOLEAN | NOT NULL DEFAULT FALSE | |
| `counts_for_seniority` | BOOLEAN | NOT NULL DEFAULT TRUE | |
| `can_self_submit` | BOOLEAN | NOT NULL DEFAULT FALSE | Colaborador pode submeter directamente |
| `is_active` | BOOLEAN | NOT NULL DEFAULT TRUE | |
| `created_at, created_by, updated_at, updated_by` | (auditoria) | | |

**Domain rules**:
- `code` imutável.
- `record_type` aceita apenas valores do enum interno `RecordType`.

**Seed**:
- Subtipos base segundo Lei 20/X/2023: licença sem vencimento, licença para formação, licença parental complementar; mobilidade interna, comissão de serviço, requisição, destacamento.

---

## 8. PublicHolidayEntity (`t_public_holiday`)

Feriados nacionais e municipais.

| Coluna | Tipo | Constraints | Descrição |
|---|---|---|---|
| `id` | UUID | PK | |
| `holiday_date` | DATE | NOT NULL | |
| `name` | VARCHAR(100) | NOT NULL | |
| `is_national` | BOOLEAN | NOT NULL DEFAULT TRUE | `TRUE` = nacional, `FALSE` = municipal |
| `description` | TEXT | NULL | |
| `is_active` | BOOLEAN | NOT NULL DEFAULT TRUE | |
| `created_at, created_by, updated_at, updated_by` | (auditoria) | | |

**Constraints únicas**:
- `UQ (holiday_date)` quando `is_national = TRUE` — não pode haver duas entradas nacionais para a mesma data. Implementado via partial unique index:
  ```sql
  CREATE UNIQUE INDEX idx_public_holiday_national_date ON t_public_holiday (holiday_date) WHERE is_national = TRUE AND is_active = TRUE;
  ```

**Domain rules**:
- Datas passadas são permitidas (registo histórico) com aviso ao utilizador (regra de UI, não DB).

**Seed para 2026** (feriados nacionais oficiais de Cabo Verde):
- 1 Jan (Ano Novo), 13 Jan (Dia da Liberdade), 20 Jan (Dia dos Heróis Nacionais), 1 Mai (Dia do Trabalhador), 1 Jun (Dia da Criança), 5 Jul (Dia da Independência), 15 Ago (Assunção), 12 Set (Dia da Nacionalidade), 1 Nov (Todos os Santos), 25 Dez (Natal). Mais Sexta-feira Santa (data variável — calculada ou seed manual por ano).

---

## Domain Models (camada de domínio pura)

Cada entity tem o seu domain model correspondente em `parametrizacoes/domain/models/`. Padrão rico observado no [Funcionario.java](../../src/main/java/cv/igrp/RH_Service/funcionarios/domain/models/Funcionario.java):

```java
public class Option {
    private ExternalID id;
    private String ccode;
    private String ckey;
    private String cvalue;
    private String locale;
    private Integer sortOrder;
    private boolean active;
    private String description;

    private Option(...) { ... }

    public static Option criar(String ccode, String ckey, String cvalue,
                               String locale, Integer sortOrder, String description) {
        Objects.requireNonNull(ccode, "ccode é obrigatório");
        Objects.requireNonNull(ckey, "ckey é obrigatório");
        Objects.requireNonNull(cvalue, "cvalue é obrigatório");
        // valida ccode contra o conjunto fechado de grupos válidos
        // ... constrói com active=true por defeito
    }

    public static Option reconstruir(...) { ... }

    public void atualizar(String cvalue, Integer sortOrder, String description) {
        // ccode, ckey, locale são imutáveis — não estão na assinatura
        if (cvalue == null || cvalue.isBlank()) {
            throw new IllegalArgumentException("cvalue não pode ser vazio");
        }
        this.cvalue = cvalue;
        this.sortOrder = sortOrder;
        this.description = description;
    }

    public void desativar() { this.active = false; }
    public void reativar() { this.active = true; }
}
```

Padrão equivalente para `WorkerState`, `ProfessionalSituation`, `ContractType`, `DocumentType`, `LeaveType`, `LeaveMobilitySubtype`, `PublicHoliday`.

`WorkerState.desativar()` deve verificar `is_core`:

```java
public void desativar() {
    if (this.isCore) {
        throw new IgrpResponseStatusException(HttpStatus.CONFLICT,
            "Estado núcleo do sistema não pode ser desactivado");
    }
    this.isActive = false;
}
```

---

## State Transitions

Todas as entidades têm o mesmo ciclo de vida:

```text
   [criação]
       │
       ▼
   ACTIVE ◄───► INACTIVE   (via desativar() / reativar())
                     │
                     ▼
              (nunca apagada)
```

Não há transições proibidas (excepto desactivação de `is_core` em `WorkerState`). A reactivação é livre por decisão da clarify session.

---

## Relationships

| De | Para | Cardinalidade | Notas |
|---|---|---|---|
| `t_document_type.category_option_id` | `t_option_entity.id` | N→1 | FK opcional. Filtra por `ccode='DOC_CATEGORY'` no domain service |
| `t_leave_type.category_option_id` | `t_option_entity.id` | N→1 | FK opcional. Filtra por `ccode='LEAVE_CATEGORY'` no domain service |

Nenhuma outra relação dentro do módulo. As FKs externas (de `funcionarios/`, `colaboradores/`, etc.) entram nas features posteriores.

---

## Validation Rules Summary

| Entity | Validation Rule | Where Enforced |
|---|---|---|
| Todas | `code` imutável após criação | Domain layer (não há setter público) |
| Todas | Soft delete only (`is_active=false`) | Application layer (não há `DELETE` SQL) |
| `OptionEntity` | `ccode` ∈ conjunto fechado de 11 valores | Domain layer (validação no `criar()`) |
| `OptionEntity` | `(ccode, ckey, locale)` único | DB constraint + verificação prévia no handler |
| `WorkerStateEntity` | `is_core=true` bloqueia `desativar()` | Domain layer (`Option.desativar()` lança exception) |
| `LeaveTypeEntity` | `category_option_id` aponta para `ccode='LEAVE_CATEGORY'` | Application layer (verificação no handler) |
| `DocumentTypeEntity` | Idem com `ccode='DOC_CATEGORY'` | Application layer |
| `PublicHolidayEntity` | Não pode haver dois nacionais com mesma data | DB partial unique index |
| Todas | Bloqueio de desactivação se referenciado por registos activos | Application layer (verificação no `Deactivate*Handler`) |
