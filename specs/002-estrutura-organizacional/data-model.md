# Data Model: Estrutura Organizacional

**Branch**: `002-estrutura-organizacional` | **Date**: 2026-04-30

---

## Diagrama de Entidades

```
option_entity (existente)
    id UUID PK
    ccode VARCHAR        ← 'UNIT_TYPE' para tipos de unidade
    ckey VARCHAR
    cvalue VARCHAR
    ...

t_unidade_organica
    id UUID PK
    code VARCHAR(50) UNIQUE NOT NULL
    name VARCHAR(200) NOT NULL
    acronym VARCHAR(20) NOT NULL
    unit_type_option_id UUID NOT NULL    ← referência soft a option_entity (sem FK constraint)
    parent_unit_id UUID NULL             ← auto-referencial; NULL = raiz
    is_active BOOLEAN NOT NULL DEFAULT TRUE
    created_at TIMESTAMP (via AuditEntity)
    updated_at TIMESTAMP (via AuditEntity)
    created_by VARCHAR (via AuditEntity)
    updated_by VARCHAR (via AuditEntity)

    FOREIGN KEY (parent_unit_id) REFERENCES t_unidade_organica(id)

t_cargo
    id UUID PK
    code VARCHAR(50) UNIQUE NOT NULL
    name VARCHAR(200) NOT NULL
    description TEXT
    is_active BOOLEAN NOT NULL DEFAULT TRUE
    created_at, updated_at, created_by, updated_by (via AuditEntity)

t_funcao
    id UUID PK
    code VARCHAR(50) UNIQUE NOT NULL
    name VARCHAR(200) NOT NULL
    description TEXT
    is_active BOOLEAN NOT NULL DEFAULT TRUE
    created_at, updated_at, created_by, updated_by (via AuditEntity)
```

---

## Entidades JPA

### OrganizationalUnitEntity

```java
@Audited
@Getter @Setter @IgrpEntity @Entity
@NoArgsConstructor @AllArgsConstructor
@Table(name = "t_unidade_organica",
    uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class OrganizationalUnitEntity extends AuditEntity {
    @Id @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "acronym", nullable = false, length = 20)
    private String acronym;

    @Column(name = "unit_type_option_id", nullable = false)
    private UUID unitTypeOptionId;   // soft ref, sem @ManyToOne

    @Column(name = "parent_unit_id")
    private UUID parentUnitId;       // nullable; null = raiz

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
```

### JobEntity

```java
@Audited @Getter @Setter @IgrpEntity @Entity
@NoArgsConstructor @AllArgsConstructor
@Table(name = "t_cargo")
public class JobEntity extends AuditEntity {
    @Id @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
```

### FunctionEntity

```java
@Audited @Getter @Setter @IgrpEntity @Entity
@NoArgsConstructor @AllArgsConstructor
@Table(name = "t_funcao")
public class FunctionEntity extends AuditEntity {
    @Id @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
```

---

## Domain Models

### OrganizationalUnit

```java
public class OrganizationalUnit {
    private OrganizationalUnitId id;
    private String code;
    private String name;
    private String acronym;
    private UUID unitTypeOptionId;
    private OrganizationalUnitId parentUnitId;   // null = raiz
    private boolean isActive;
}
```

### Job

```java
public class Job {
    private JobId id;
    private String code;
    private String name;
    private String description;
    private boolean isActive;
}
```

### Function

```java
public class Function {
    private FunctionId id;
    private String code;
    private String name;
    private String description;
    private boolean isActive;
}
```

---

## Value Objects (Typed IDs)

Seguindo o padrão de `WorkerStateId`, `ContractTypeId`, etc.:

```java
// OrganizationalUnitId.java
public final class OrganizationalUnitId {
    private final ExternalID valor;
    private OrganizationalUnitId(ExternalID valor) { ... }
    public static OrganizationalUnitId gerarNovo() { ... }
    public static OrganizationalUnitId from(UUID uuid) { ... }
    public static OrganizationalUnitId from(String str) { ... }
    public UUID getValor() { ... }
    public String getStringValor() { ... }
}

// JobId.java e FunctionId.java — mesmo padrão
```

---

## Repository Ports

### OrganizationalUnitRepository

```java
public interface OrganizationalUnitRepository {
    OrganizationalUnit save(OrganizationalUnit unit);
    Optional<OrganizationalUnit> findById(OrganizationalUnitId id);
    Optional<OrganizationalUnit> findByCode(String code);
    Page<OrganizationalUnit> findAll(OrganizationalUnitFilter filter, Pageable pageable);
    boolean existsActiveChildrenOf(OrganizationalUnitId parentId);  // para regra de desactivação
    boolean existsByCode(String code);
}
```

### JobRepository / FunctionRepository

```java
public interface JobRepository {
    Job save(Job job);
    Optional<Job> findById(JobId id);
    Optional<Job> findByCode(String code);
    Page<Job> findAll(JobFilter filter, Pageable pageable);
    boolean existsByCode(String code);
}
// FunctionRepository — mesmo padrão com FunctionId, Function, FunctionFilter
```

---

## Flyway Migrations

### V20__create_estrutura_tables.sql

```sql
CREATE TABLE IF NOT EXISTS t_unidade_organica (
    id                  UUID        NOT NULL PRIMARY KEY,
    code                VARCHAR(50) NOT NULL,
    name                VARCHAR(200) NOT NULL,
    acronym             VARCHAR(20) NOT NULL,
    unit_type_option_id UUID        NOT NULL,
    parent_unit_id      UUID        REFERENCES t_unidade_organica(id),
    is_active           BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    CONSTRAINT uq_unidade_organica_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS t_cargo (
    id          UUID        NOT NULL PRIMARY KEY,
    code        VARCHAR(50) NOT NULL,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    CONSTRAINT uq_cargo_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS t_funcao (
    id          UUID        NOT NULL PRIMARY KEY,
    code        VARCHAR(50) NOT NULL,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    CONSTRAINT uq_funcao_code UNIQUE (code)
);
```

### V21__seed_unit_types.sql

```sql
-- Seed dos tipos de unidade orgânica (idempotente)
INSERT INTO option_entity (id, ccode, ckey, cvalue, locale, is_active)
VALUES
    (gen_random_uuid(), 'UNIT_TYPE', 'DIRECCAO',    'Direcção',    'pt-CV', true),
    (gen_random_uuid(), 'UNIT_TYPE', 'DEPARTAMENTO', 'Departamento', 'pt-CV', true),
    (gen_random_uuid(), 'UNIT_TYPE', 'DIVISAO',     'Divisão',     'pt-CV', true),
    (gen_random_uuid(), 'UNIT_TYPE', 'SECCAO',      'Secção',      'pt-CV', true)
ON CONFLICT DO NOTHING;
```

---

## Tabelas de Auditoria (Envers — geradas automaticamente)

As seguintes tabelas são criadas automaticamente pelo Hibernate Envers no schema `audit_schema`:

- `audit_schema.t_unidade_organica_aud`
- `audit_schema.t_cargo_aud`
- `audit_schema.t_funcao_aud`

---

## Regras de Validação de Negócio

| Regra | Onde Validar | Detalhe |
|-------|-------------|---------|
| `code` único por catálogo | Application (handler) | `existsByCode()` antes de criar; HTTP 409 se duplicado |
| `parent_unit_id` válido e activo (criação) | Application | `findById()` + verificar `isActive`; HTTP 404 se não existe, HTTP 409 se inactiva |
| Desactivação bloqueada com filhos activos | Application | `existsActiveChildrenOf()` antes de desactivar; HTTP 409 |
| Reactivação: mãe deve estar activa | Application | Verificar `parentUnitId != null && !parent.isActive()`; HTTP 409 |
| `unit_type_option_id` pertence a `UNIT_TYPE` | Application | `OptionRepository.findByCcodeAndId()`; HTTP 400 se inválido |
