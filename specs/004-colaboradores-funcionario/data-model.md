# Data Model: Módulo Colaboradores

## Entidades

### 1. Funcionário (`t_funcionario`)

Agregado raiz. Representa um colaborador da instituição.

| Campo | Tipo | Restrições | Notas |
|-------|------|-----------|-------|
| `id` | UUID | PK, NOT NULL | ExternalID |
| `numero_funcionario` | VARCHAR(10) | UNIQUE, NOT NULL, imutável | Gerado: F + 6 dígitos via seq |
| `nome_completo` | VARCHAR(200) | NOT NULL | |
| `data_nascimento` | DATE | NOT NULL | |
| `genero` | VARCHAR(50) | NOT NULL | Option ckey |
| `estado_civil` | VARCHAR(50) | NOT NULL | Option ckey |
| `nif` | VARCHAR(20) | UNIQUE, NOT NULL | 9 dígitos numéricos |
| `bi_numero` | VARCHAR(50) | UNIQUE, NOT NULL | |
| `bi_validade` | DATE | nullable | |
| `nacionalidade` | VARCHAR(50) | NOT NULL, default 'CV' | Option ckey |
| `email` | VARCHAR(200) | UNIQUE, nullable | |
| `telefone` | VARCHAR(30) | nullable | |
| `morada` | TEXT | nullable | |
| `foto_url` | TEXT | nullable | URL para MinIO |
| `situacao_profissional` | VARCHAR(50) | NOT NULL | Option ckey |
| `data_admissao` | DATE | NOT NULL | |
| `data_saida` | DATE | nullable | Preenchido no desligamento |
| `is_active` | BOOLEAN | NOT NULL | Derivado: true quando situacao_profissional='ATIVO' |
| `created_date` | TIMESTAMP | NOT NULL | Envers/Audit |
| `created_by` | VARCHAR(255) | NOT NULL | Envers/Audit |
| `last_modified_date` | TIMESTAMP | nullable | Envers/Audit |
| `last_modified_by` | VARCHAR(255) | nullable | Envers/Audit |

**Índices**: `idx_funcionario_nif`, `idx_funcionario_bi`, `idx_funcionario_situacao`, `idx_funcionario_is_active`

**Regras de domínio**:
- `numero_funcionario` gerado por `nextval('seq_numero_funcionario')`, imutável após criação
- `is_active` actualizado automaticamente pelo domínio quando `situacao_profissional` muda
- `data_saida` só preenchida quando `situacao_profissional != 'ATIVO'`
- NIF e BI mutáveis (lookup externo); unicidade validada excluindo o próprio registo no update

---

### 2. EnquadramentoProfissional (`employee_professional_assignments`)

Posição do funcionário na grelha PCFR num período.

| Campo | Tipo | Restrições | Notas |
|-------|------|-----------|-------|
| `id` | UUID | PK, NOT NULL | ExternalID |
| `funcionario_id` | UUID | FK→t_funcionario, NOT NULL | |
| `career_id` | UUID | FK→t_career, NOT NULL | |
| `category_id` | UUID | FK→t_category, NOT NULL | |
| `grade_id` | UUID | FK→t_grade, NOT NULL | |
| `cargo_id` | UUID | FK→t_cargo, NOT NULL | |
| `unidade_organica_id` | UUID | FK→t_unidade_organica, NOT NULL | |
| `data_inicio` | DATE | NOT NULL | |
| `data_fim` | DATE | nullable | NULL = enquadramento actual |
| `is_current` | BOOLEAN | NOT NULL, default FALSE | Apenas 1 TRUE por funcionário |
| `created_date` | TIMESTAMP | NOT NULL | |
| `created_by` | VARCHAR(255) | NOT NULL | |
| `last_modified_date` | TIMESTAMP | nullable | |
| `last_modified_by` | VARCHAR(255) | nullable | |

**Índices**: `idx_enquadramento_funcionario`, `idx_enquadramento_is_current`

**Regras de domínio**:
- Ao criar novo enquadramento: UPDATE anterior (data_fim = nova_data_inicio - 1 dia, is_current = false) + INSERT novo (is_current = true) — mesma transacção
- `data_inicio` do novo deve ser > `data_inicio` do enquadramento actual
- Carreira, Categoria, Escalão, Cargo e Unidade Orgânica devem estar activos no momento de criação
- Cargo e Unidade Orgânica são registados independentemente (sem validação cruzada — `t_cargo` é catálogo global)
- Não existe update de enquadramento; apenas criação (o novo substitui o anterior)

---

### 3. Contrato (`t_contrato`)

Vínculo laboral formal do funcionário.

| Campo | Tipo | Restrições | Notas |
|-------|------|-----------|-------|
| `id` | UUID | PK, NOT NULL | |
| `funcionario_id` | UUID | FK→t_funcionario, NOT NULL | |
| `tipo_contrato` | VARCHAR(50) | NOT NULL | Option ckey: EFETIVO, TERMO_CERTO, TERMO_INCERTO, CEDENCIA |
| `data_inicio` | DATE | NOT NULL | |
| `data_fim` | DATE | nullable | |
| `numero_contrato` | VARCHAR(100) | UNIQUE, nullable | Livre; quando fornecido, único globalmente |
| `is_active` | BOOLEAN | NOT NULL, default TRUE | |
| `created_date` | TIMESTAMP | NOT NULL | |
| `created_by` | VARCHAR(255) | NOT NULL | |
| `last_modified_date` | TIMESTAMP | nullable | |
| `last_modified_by` | VARCHAR(255) | nullable | |

**Regras de domínio**:
- Apenas 1 contrato com `is_active = true` por funcionário em simultâneo
- Criar novo contrato quando já existe activo → 409 Conflict (o gestor deve encerrar primeiro)
- Desactivar o único contrato activo → 409 Conflict (sempre, independentemente da situação profissional)

---

### 4. Dependente (`t_dependente`)

Familiar a cargo do funcionário.

| Campo | Tipo | Restrições | Notas |
|-------|------|-----------|-------|
| `id` | UUID | PK, NOT NULL | |
| `funcionario_id` | UUID | FK→t_funcionario, NOT NULL | |
| `nome` | VARCHAR(150) | NOT NULL | |
| `parentesco` | VARCHAR(50) | NOT NULL | Option ckey: CONJUGE, FILHO, PAI, MAE, OUTRO |
| `data_nascimento` | DATE | nullable | |
| `nif` | VARCHAR(20) | nullable | NIF do dependente (sem unicidade global) |
| `is_active` | BOOLEAN | NOT NULL, default TRUE | Soft delete |
| `created_date` | TIMESTAMP | NOT NULL | |
| `created_by` | VARCHAR(255) | NOT NULL | |
| `last_modified_date` | TIMESTAMP | nullable | |
| `last_modified_by` | VARCHAR(255) | nullable | |

---

### 5. Qualificação (`t_qualificacao`)

Habilitação académica ou profissional do funcionário.

| Campo | Tipo | Restrições | Notas |
|-------|------|-----------|-------|
| `id` | UUID | PK, NOT NULL | |
| `funcionario_id` | UUID | FK→t_funcionario, NOT NULL | |
| `nivel_academico` | VARCHAR(50) | NOT NULL | Option ckey |
| `curso` | VARCHAR(200) | NOT NULL | |
| `instituicao` | VARCHAR(200) | nullable | |
| `ano_conclusao` | INTEGER | nullable | |
| `pais` | VARCHAR(50) | NOT NULL, default 'CV' | Option ckey |
| `is_active` | BOOLEAN | NOT NULL, default TRUE | Soft delete |
| `created_date` | TIMESTAMP | NOT NULL | |
| `created_by` | VARCHAR(255) | NOT NULL | |
| `last_modified_date` | TIMESTAMP | nullable | |
| `last_modified_by` | VARCHAR(255) | nullable | |

---

## Relações

```
t_funcionario (1) ──── (N) employee_professional_assignments
t_funcionario (1) ──── (N) t_contrato
t_funcionario (1) ──── (N) t_dependente
t_funcionario (1) ──── (N) t_qualificacao

employee_professional_assignments (N) ──── (1) t_career
employee_professional_assignments (N) ──── (1) t_category
employee_professional_assignments (N) ──── (1) t_grade
employee_professional_assignments (N) ──── (1) t_cargo
employee_professional_assignments (N) ──── (1) t_unidade_organica
```

## Sequence

```sql
CREATE SEQUENCE IF NOT EXISTS seq_numero_funcionario
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    CACHE 1;
```

## Value Objects (domain layer)

| Value Object | Wraps | Módulo |
|---|---|---|
| `FuncionarioId` | ExternalID (UUID) | colaboradores |
| `EnquadramentoId` | ExternalID (UUID) | colaboradores |
| `ContratoId` | ExternalID (UUID) | colaboradores |
| `DependenteId` | ExternalID (UUID) | colaboradores |
| `QualificacaoId` | ExternalID (UUID) | colaboradores |
| `CareerId` | ExternalID (UUID) | carreiras (reutilizar) |
| `CategoryId` | ExternalID (UUID) | carreiras (reutilizar) |
| `GradeId` | ExternalID (UUID) | carreiras (reutilizar) |
| `JobId` | ExternalID (UUID) | estrutura (reutilizar) |
| `OrganizationalUnitId` | ExternalID (UUID) | estrutura (reutilizar) |
