# Data Model: Colocações de Funcionários

**Feature**: 007-colocacoes-funcionario  
**Date**: 2026-05-01

---

## Entidade Principal: Colocacao

**Tabela**: `employee_unit_assignments`

| Campo | Tipo Java | Coluna SQL | Nulável | Descrição |
|---|---|---|---|---|
| `id` | `ColocacaoId` | `id UUID PK` | Não | Identificador único |
| `funcionarioId` | `FuncionarioId` | `funcionario_id UUID FK→t_funcionario` | Não | Funcionário afecto |
| `unitId` | `UUID` | `unit_id UUID FK→t_unidade_organica` | Sim | Unidade destino (null para mobilidade externa) |
| `jobId` | `UUID` | `job_id UUID FK→t_cargo` | Sim | Cargo exercido na unidade |
| `startDate` | `LocalDate` | `start_date DATE` | Não | Início da colocação |
| `endDate` | `LocalDate` | `end_date DATE` | Sim | Fim (null = colocação actual) |
| `isCurrent` | `Boolean` | `is_current BOOLEAN DEFAULT FALSE` | Não | Flag da colocação activa |
| `isActive` | `Boolean` | `is_active BOOLEAN DEFAULT TRUE` | Não | Soft delete |
| `assignmentType` | `TipoAfectacao` (enum) | `assignment_type VARCHAR(50)` | Não | Tipo de afectação |
| `notes` | `String` | `notes TEXT` | Sim | Observações |
| *(auditoria)* | `AuditEntity` | `created_date, created_by, last_modified_date, last_modified_by` | — | Campos Envers |

---

## Enum: TipoAfectacao

```
INICIAL        — Primeira colocação do funcionário
TRANSFERENCIA  — Transferência para outra unidade
MOBILIDADE     — Criada automaticamente ao aprovar uma LicencaMobilidade
REQUISICAO     — Requisição para outra entidade/unidade
```

---

## Relacionamentos

```
Colocacao (N) ──── (1) Funcionario        [t_funcionario.id]
Colocacao (N) ──── (1) UnidadeOrganica    [t_unidade_organica.id]  (opcional)
Colocacao (N) ──── (1) Cargo              [t_cargo.id]              (opcional)
LicencaMobilidade (1) ──── cria ──── Colocacao (1)                 (trigger lógico via handler)
```

---

## Regras de integridade

- **Unicidade de colocação actual**: `is_current=true` → no máximo 1 por `funcionario_id` (garantido por lógica aplicacional via JPQL UPDATE atómico + índice parcial DB).
- **Datas**: `start_date <= today`; se `end_date != null` → `end_date >= start_date`.
- **Soft delete**: `is_active=false` exclui da listagem normal; `is_current=true` não pode ser soft-deleted.

---

## Migrações Flyway

**V28**: `V28__create_employee_unit_assignments.sql`

```sql
CREATE TABLE IF NOT EXISTS employee_unit_assignments (
    id                  UUID            NOT NULL,
    funcionario_id      UUID            NOT NULL,
    unit_id             UUID,
    job_id              UUID,
    start_date          DATE            NOT NULL,
    end_date            DATE,
    is_current          BOOLEAN         NOT NULL DEFAULT FALSE,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    assignment_type     VARCHAR(50)     NOT NULL,
    notes               TEXT,
    created_date        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by          VARCHAR(255)    NOT NULL,
    last_modified_date  TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by    VARCHAR(255),
    CONSTRAINT pk_employee_unit_assignments PRIMARY KEY (id),
    CONSTRAINT fk_eua_funcionario FOREIGN KEY (funcionario_id) REFERENCES t_funcionario(id),
    CONSTRAINT fk_eua_unit       FOREIGN KEY (unit_id)         REFERENCES t_unidade_organica(id),
    CONSTRAINT fk_eua_job        FOREIGN KEY (job_id)          REFERENCES t_cargo(id)
);

CREATE INDEX IF NOT EXISTS idx_eua_funcionario_current
    ON employee_unit_assignments (funcionario_id, is_current)
    WHERE is_active = TRUE;

CREATE INDEX IF NOT EXISTS idx_eua_funcionario_history
    ON employee_unit_assignments (funcionario_id, start_date DESC);
```

---

## Estrutura de Classes Java

```
colaboradores/
├── domain/
│   ├── valueobject/
│   │   └── ColocacaoId.java                    (UUID wrapper, padrão FuncionarioId)
│   ├── models/
│   │   ├── Colocacao.java                      (aggregate root)
│   │   └── TipoAfectacao.java                  (enum)
│   ├── filter/
│   │   └── ColocacaoFilter.java                (@Data: funcionarioId, isCurrent)
│   └── repository/
│       └── ColocacaoRepository.java            (port: save, findById, findAllByFuncionarioId, fecharColocacaoAtual, findCurrentByFuncionarioId)
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/
│   │   │   └── ColocacaoEntity.java            (@Entity(name="ColabsColocacaoEntity"), @Audited)
│   │   ├── repository/
│   │   │   └── ColabsColocacaoEntityRepository.java  (Spring Data JPA)
│   │   └── adapters/
│   │       └── ColocacaoRepositoryImpl.java    (@Repository("colabsColocacaoRepositoryImpl"))
│   └── mappers/
│       └── ColocacaoMapper.java                (toDomain / toEntity)
├── application/
│   ├── commands/
│   │   ├── RegistarColocacaoCommand.java
│   │   ├── RegistarColocacaoCommandHandler.java
│   │   ├── AtualizarColocacaoCommand.java
│   │   ├── AtualizarColocacaoCommandHandler.java
│   │   ├── DesativarColocacaoCommand.java
│   │   └── DesativarColocacaoCommandHandler.java
│   ├── queries/
│   │   ├── GetColocacaoAtualQuery.java
│   │   ├── GetColocacaoAtualQueryHandler.java
│   │   ├── GetColocacoesByFuncionarioQuery.java
│   │   ├── GetColocacoesByFuncionarioQueryHandler.java
│   │   ├── GetColocacaoByIdQuery.java
│   │   └── GetColocacaoByIdQueryHandler.java
│   └── dto/
│       ├── ColocacaoResponse.java
│       ├── RegistarColocacaoRequest.java
│       ├── AtualizarColocacaoRequest.java
│       └── WrapperListaColocacaoDTO.java
└── interfaces/
    └── rest/
        └── ColocacaoController.java
```

---

## Modificações em ficheiros existentes

| Ficheiro | Modificação |
|---|---|
| `AtivarLicencaMobilidadeCommandHandler.java` | Injectar `ColocacaoRepository` + criar `Colocacao.tipo(MOBILIDADE)` após `licenca.ativar()` |
