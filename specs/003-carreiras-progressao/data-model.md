# Data Model: Carreiras e Progressão

**Feature**: 003-carreiras-progressao  
**Date**: 2026-04-30  
**Migration**: V23

## Diagrama de Entidades

```
t_career (Carreiras)
├── id              UUID          PK
├── code            VARCHAR(50)   NOT NULL UNIQUE
├── name            VARCHAR(150)  NOT NULL
├── description     TEXT
├── is_active       BOOLEAN       NOT NULL DEFAULT TRUE
├── created_date    TIMESTAMP     NOT NULL
├── created_by      VARCHAR(255)  NOT NULL
├── last_modified_date TIMESTAMP
└── last_modified_by   VARCHAR(255)

t_category (Categorias)
├── id              UUID          PK
├── career_id       UUID          NOT NULL FK → t_career(id)
├── code            VARCHAR(50)   NOT NULL
├── name            VARCHAR(150)  NOT NULL
├── description     TEXT
├── is_active       BOOLEAN       NOT NULL DEFAULT TRUE
├── created_date    TIMESTAMP     NOT NULL
├── created_by      VARCHAR(255)  NOT NULL
├── last_modified_date TIMESTAMP
├── last_modified_by   VARCHAR(255)
└── UNIQUE (career_id, code)

t_grade (Escalões)
├── id              UUID          PK
├── category_id     UUID          NOT NULL FK → t_category(id)
├── grade_number    INTEGER       NOT NULL  (≥ 1)
├── name            VARCHAR(150)  NOT NULL
├── salary_index    NUMERIC(12,2) NULL
├── is_active       BOOLEAN       NOT NULL DEFAULT TRUE
├── created_date    TIMESTAMP     NOT NULL
├── created_by      VARCHAR(255)  NOT NULL
├── last_modified_date TIMESTAMP
├── last_modified_by   VARCHAR(255)
└── UNIQUE (category_id, grade_number)
```

## Migration SQL (V23)

```sql
-- Bloco 3 do modelo relacional v4: Carreiras e Progressão (PCFR, Decreto-Lei 4/2024)

CREATE TABLE IF NOT EXISTS t_career (
    id                  UUID                         NOT NULL,
    code                VARCHAR(50)                  NOT NULL,
    name                VARCHAR(150)                 NOT NULL,
    description         TEXT,
    is_active           BOOLEAN                      NOT NULL DEFAULT TRUE,
    created_date        TIMESTAMP WITHOUT TIME ZONE  NOT NULL,
    created_by          VARCHAR(255)                 NOT NULL,
    last_modified_date  TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by    VARCHAR(255),
    CONSTRAINT pk_career   PRIMARY KEY (id),
    CONSTRAINT uq_career_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS t_category (
    id                  UUID                         NOT NULL,
    career_id           UUID                         NOT NULL,
    code                VARCHAR(50)                  NOT NULL,
    name                VARCHAR(150)                 NOT NULL,
    description         TEXT,
    is_active           BOOLEAN                      NOT NULL DEFAULT TRUE,
    created_date        TIMESTAMP WITHOUT TIME ZONE  NOT NULL,
    created_by          VARCHAR(255)                 NOT NULL,
    last_modified_date  TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by    VARCHAR(255),
    CONSTRAINT pk_category          PRIMARY KEY (id),
    CONSTRAINT fk_category_career   FOREIGN KEY (career_id) REFERENCES t_career(id),
    CONSTRAINT uq_category_career_code UNIQUE (career_id, code)
);

CREATE TABLE IF NOT EXISTS t_grade (
    id                  UUID                         NOT NULL,
    category_id         UUID                         NOT NULL,
    grade_number        INTEGER                      NOT NULL,
    name                VARCHAR(150)                 NOT NULL,
    salary_index        NUMERIC(12,2),
    is_active           BOOLEAN                      NOT NULL DEFAULT TRUE,
    created_date        TIMESTAMP WITHOUT TIME ZONE  NOT NULL,
    created_by          VARCHAR(255)                 NOT NULL,
    last_modified_date  TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by    VARCHAR(255),
    CONSTRAINT pk_grade              PRIMARY KEY (id),
    CONSTRAINT fk_grade_category     FOREIGN KEY (category_id) REFERENCES t_category(id),
    CONSTRAINT uq_grade_cat_number   UNIQUE (category_id, grade_number),
    CONSTRAINT chk_grade_number_pos  CHECK (grade_number >= 1)
);
```

## Regras de Negócio por Entidade

### Carreira (t_career)

| Regra | Detalhe |
|---|---|
| Unicidade | `code` globalmente único |
| Imutabilidade | Nenhum campo imutável (code pode ser alterado, validando unicidade) |
| Desactivação bloqueada | Se existirem categorias com `is_active = true` referenciando esta carreira |

### Categoria (t_category)

| Regra | Detalhe |
|---|---|
| Unicidade | Par `(career_id, code)` único |
| Imutabilidade | `career_id` e `code` — imutáveis após criação (v4 spec § 4.5) |
| Desactivação bloqueada | Se existirem escalões com `is_active = true` nesta categoria |

### Escalão (t_grade)

| Regra | Detalhe |
|---|---|
| Unicidade | Par `(category_id, grade_number)` único |
| Imutabilidade | `category_id` e `grade_number` — imutáveis após criação |
| `grade_number` | Inteiro ≥ 1 (CHECK constraint na BD) |
| `salary_index` | Opcional (NULL permitido) |
| Desactivação bloqueada | Se existirem registos em `employee_professional_assignments` com `grade_id = id AND is_current = true`; se a tabela não existir → permitido |

## Estrutura Java (Hexagonal)

```
carreiras/
├── domain/
│   ├── models/
│   │   ├── Career.java
│   │   ├── Category.java
│   │   └── Grade.java
│   ├── valueobject/
│   │   ├── CareerId.java
│   │   ├── CategoryId.java
│   │   └── GradeId.java
│   ├── repository/
│   │   ├── CareerRepository.java
│   │   ├── CategoryRepository.java
│   │   └── GradeRepository.java
│   └── filter/
│       ├── CareerFilter.java
│       ├── CategoryFilter.java
│       └── GradeFilter.java
├── application/
│   ├── dto/
│   │   ├── CareerRequest.java / CareerResponse.java
│   │   ├── CategoryRequest.java / CategoryResponse.java
│   │   ├── GradeRequest.java / GradeResponse.java
│   │   ├── WrapperListaCareerDTO.java
│   │   ├── WrapperListaCategoryDTO.java
│   │   ├── WrapperListaGradeDTO.java
│   │   └── WrapperListaAuditHistoryDTO.java (reutilizar de estrutura/)
│   ├── commands/
│   │   ├── CreateCareer* / UpdateCareer* / DesativarCareer* / AtivarCareer*
│   │   ├── CreateCategory* / UpdateCategory* / DesativarCategory* / AtivarCategory*
│   │   └── CreateGrade* / UpdateGrade* / DesativarGrade* / AtivarGrade*
│   └── queries/
│       ├── GetCareers* / GetCareerById* / GetCategoriesByCareerId*
│       ├── GetCategories* / GetCategoryById* / GetGradesByCategoryId*
│       ├── GetGrades* / GetGradeById*
│       └── GetCarreirasAuditHistory*
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/ — CareerEntity, CategoryEntity, GradeEntity
│   │   ├── repository/ — CareerEntityRepository, CategoryEntityRepository, GradeEntityRepository
│   │   └── adapters/ — CareerRepositoryImpl, CategoryRepositoryImpl, GradeRepositoryImpl
│   └── mappers/ — CareerMapper, CategoryMapper, GradeMapper
└── interfaces/rest/
    ├── CareerController.java        (gerado — IGRP Studio)
    ├── CategoryController.java      (gerado — IGRP Studio)
    ├── GradeController.java         (gerado — IGRP Studio)
    └── CarreirasAuditHistoryController.java (gerado — IGRP Studio)
```
