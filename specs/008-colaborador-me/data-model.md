# Data Model: Meu Perfil — Área Reservada do Colaborador

**Feature**: 008-colaborador-me | **Date**: 2026-05-02

---

## Nova Tabela: t_iam_user_profile (V29)

```sql
CREATE TABLE IF NOT EXISTS t_iam_user_profile (
    id              UUID            NOT NULL,
    sub             VARCHAR(255)    NOT NULL,       -- Keycloak subject (UUID do utilizador no Keycloak)
    username        VARCHAR(255)    NOT NULL,       -- preferred_username do JWT
    email           VARCHAR(255),                  -- email do JWT
    first_name      VARCHAR(100),                  -- given_name do JWT
    last_name       VARCHAR(100),                  -- family_name do JWT
    full_name       VARCHAR(255),                  -- concatenação firstName + lastName
    funcionario_id  UUID,                          -- FK para t_funcionario (resolvido por email no 1º login)
    created_date        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by          VARCHAR(255)               NOT NULL,
    last_modified_date  TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by    VARCHAR(255),
    CONSTRAINT pk_iam_user_profile PRIMARY KEY (id),
    CONSTRAINT uq_iam_user_profile_sub      UNIQUE (sub),
    CONSTRAINT uq_iam_user_profile_username UNIQUE (username),
    CONSTRAINT fk_iam_user_profile_funcionario
        FOREIGN KEY (funcionario_id) REFERENCES t_funcionario(id)
);

CREATE INDEX IF NOT EXISTS idx_iam_user_profile_sub
    ON t_iam_user_profile (sub);
CREATE INDEX IF NOT EXISTS idx_iam_user_profile_email
    ON t_iam_user_profile (email);
CREATE INDEX IF NOT EXISTS idx_iam_user_profile_funcionario
    ON t_iam_user_profile (funcionario_id);
```

---

## Entidade JPA: IAMUserProfileEntity

```
@Entity(name = "IAMUserProfileEntity")
@Table(name = "t_iam_user_profile")
@Audited
extends AuditEntity

Campos:
  id              UUID @Id
  sub             String (unique, not null)
  username        String (unique, not null)
  email           String (unique)
  firstName       String
  lastName        String
  fullName        String (computed: firstName + " " + lastName)
  funcionarioId   UUID (FK → t_funcionario, nullable)
```

---

## Serviço de Domínio: CurrentEmployeeResolver

```
Interface: shared/domain/service/CurrentEmployeeResolver.java
  resolve() → FuncionarioId
    throws IgrpResponseStatusException(404) se não encontrado

Implementação: shared/infrastructure/security/CurrentEmployeeResolverImpl.java
  @Component("currentEmployeeResolver")
  @RequiredArgsConstructor

  Lógica:
    [dev/staging]
      1. Ler header X-Employee-Id
      2. Se presente: return FuncionarioId.from(UUID.fromString(header))
      3. Se ausente: return FuncionarioId do primeiro funcionário activo (fallback)

    [production]
      1. Obter Authentication do SecurityContextHolder
      2. Se JwtAuthenticationToken: extrair jwt.getSubject() (sub)
      3. IAMUserProfileEntityRepository.findBySub(sub)
      4. Se vazio ou funcionarioId == null: throw 404
      5. return FuncionarioId.from(iamProfile.getFuncionarioId())
```

---

## Agregações Read-Only (sem novas tabelas)

### MeProfileResponse — fontes de dados

```
t_funcionario        → id, nome, nif, email, telefone, dataAdmissao, isActive
employee_unit_assignments (is_current=true, is_active=true)
                     → unitId (→ t_unidade_organica.nome)
employee_professional_assignments (is_current=true, is_active=true)
                     → jobId (→ t_cargo.nome), funcionId (→ t_funcao.nome)
                     → careerId (→ t_career.nome), categoryId (→ t_category.nome)
                     → gradeId (→ t_grade.gradeNumber)
```

---

## Relações

```
t_iam_user_profile ──FK──→ t_funcionario (funcionario_id, nullable)
t_iam_user_profile   [sub = Keycloak user UUID]
t_funcionario        [email = link para resolução no 1º login]
```

---

## Sem Alterações nas Tabelas Existentes

Todas as tabelas de ausências, licenças, documentos e colocações são reutilizadas com filtro adicional por `funcionario_id` — sem migrações adicionais.
