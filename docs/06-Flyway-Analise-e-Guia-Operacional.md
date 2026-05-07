# Flyway — Análise de Problemas e Guia Operacional

> **Última revisão:** 2026-05-04  
> **Âmbito:** `src/main/resources/db/migration/` — V1.0 a V34  
> **Contexto:** Análise realizada após falha de startup em BD fresca (ambiente externo)

---

## 1. Como o projecto funciona — contrato implícito

O projecto tem três perfis com comportamentos muito diferentes para JPA + Flyway:

| Perfil | `ddl-auto` | Ordem de arranque | BD fresca suportada? |
|---|---|---|---|
| `development` | `update` | Hibernate cria/altera tabelas **primeiro**, Flyway corre depois | ✅ Sim |
| `staging` | `validate` | Flyway cria tabelas, Hibernate valida | ❌ Não (ver §2.5) |
| `production` | `validate` | Flyway cria tabelas, Hibernate valida | ❌ Não (ver §2.5) |

**Porquê esta ordem em dev?** O Spring Boot configura `FlywayAutoConfiguration` com
`@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)`, o que faz com que o
`EntityManagerFactory` (e o DDL do Hibernate) seja criado **antes** do
`FlywayMigrationInitializer` correr as migrations. Em dev com `ddl-auto=update`, todas as
tabelas já existem quando o Flyway arranca.

**Consequência:** Várias migrations foram escritas assumindo que o Hibernate já criou certas
tabelas. Funcionam em dev, mas falham numa BD fresca onde o Flyway corre primeiro (e.g.,
colega com repositório diferente, pipeline CI/CD com BD limpa).

---

## 2. Problemas encontrados

### 🔴 2.1 — V1.0: INSERT em `t_institutions` sem DDL da tabela

**Ficheiro:** `V1.0__Seed_Institutional_Identity.sql`

**Problema:** A migration faz apenas um `INSERT INTO t_institutions`. A tabela nunca é criada
por nenhuma migration Flyway — depende do Hibernate ter-a criado via `InstitutionEntity`.

**Quando falha:**
- BD completamente fresca onde o Flyway corre antes do Hibernate
- Ambientes onde o módulo SIGDI não está activo (tabela não existe)

**Sintoma no log:**
```
Migration of schema "public" to version "1.0 - Seed Institutional Identity" failed!
Changes successfully rolled back.
```

**Efeito em cascata:** A falha do Flyway impede a criação do `EntityManagerFactory`, o que
impede o wiring de todos os repositórios JPA — a aplicação não arranca.

**Correcção:**

Substituir o conteúdo do ficheiro por:

```sql
-- V1.0__Seed_Institutional_Identity.sql
CREATE TABLE IF NOT EXISTS t_institutions (
    id                  UUID         NOT NULL,
    code                VARCHAR(255) NOT NULL,
    name                VARCHAR(255),
    type                VARCHAR(255),
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    deactivated_at      TIMESTAMPTZ,
    contact_email       VARCHAR(255),
    created_date        TIMESTAMPTZ,
    created_by          VARCHAR(255),
    last_modified_date  TIMESTAMPTZ,
    last_modified_by    VARCHAR(255),
    CONSTRAINT pk_institutions     PRIMARY KEY (id),
    CONSTRAINT uq_institutions_code UNIQUE (code)
);

INSERT INTO t_institutions (id, code, name, type, is_active, created_date, created_by)
VALUES ('00000000-0000-0000-0000-000000000001', 'SIGPROG_RH', 'SIPPROG', 'INTERNAL',
        true, CURRENT_TIMESTAMP, 'system')
ON CONFLICT (id) DO NOTHING;
```

Após alterar, em **todos os ambientes onde V1.0 já foi aplicado com sucesso**:

```bash
mvn flyway:repair
```

> `flyway:repair` recalcula o checksum do script no `flyway_schema_history` sem re-executar
> a migration. Obrigatório sempre que se altera um ficheiro de migration já aplicado.

---

### 🔴 2.2 — V3: `ALTER TABLE t_key_results` sem DDL da tabela

**Ficheiro:** `V3__sigdi_add_missing_columns.sql`

**Problema:** A migration faz `ALTER TABLE t_key_results ADD COLUMN IF NOT EXISTS ...` mas
`t_key_results` não é criada por nenhuma migration Flyway.

**Quando falha:**
- BD fresca (Flyway antes do Hibernate)
- Ambientes onde as entidades SIGDI não estão mapeadas / tabela não existe

**Correcção:**

Envolver em bloco defensivo:

```sql
-- V3__sigdi_add_missing_columns.sql
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 't_key_results'
    ) THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name = 't_key_results'
                         AND column_name = 'criteria_superado') THEN
            ALTER TABLE t_key_results
                ADD COLUMN criteria_superado     VARCHAR(255),
                ADD COLUMN criteria_seguranca    VARCHAR(255),
                ADD COLUMN criteria_alcancado    VARCHAR(255),
                ADD COLUMN criteria_insuficiente VARCHAR(255);
        END IF;
    END IF;
END $$;
```

Após alterar: `mvn flyway:repair` nos ambientes afectados.

---

### 🔴 2.3 — V10: `ALTER COLUMN created_date` assume que o Hibernate já correu

**Ficheiro:** `V10__seed_option_entity_pt_cv.sql`

**Problema:** V2 cria `t_option_entity` com a coluna chamada `created_at`. O Hibernate com
`ddl-auto=update` adiciona `created_date` (da `AuditEntity`). V10 assume que `created_date`
já existe e tenta:

```sql
ALTER TABLE t_option_entity
    ALTER COLUMN created_date SET DEFAULT NOW(),
    ALTER COLUMN created_by   SET DEFAULT 'seed';
```

Numa BD fresca onde o Flyway corre antes do Hibernate, `created_date` não existe (só existe
`created_at` criada pelo V2) — o `ALTER COLUMN` falha.

**Quando falha:** BD fresca; pipeline CI/CD; qualquer ambiente onde a ordenação Hibernate →
Flyway não é garantida.

**Correcção:**

Substituir o bloco de ALTER por uma versão defensiva (o mesmo padrão que V34 usa para outras
tabelas):

```sql
-- Substituir os dois primeiros blocos de V10 por:

-- 1. Garante UNIQUE constraint (idempotente)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'uq_option_ccode_ckey_locale'
          AND table_name = 't_option_entity'
    ) THEN
        ALTER TABLE t_option_entity
            ADD CONSTRAINT uq_option_ccode_ckey_locale UNIQUE (ccode, ckey, locale);
    END IF;
END $$;

-- 2. Garante defaults independentemente do nome da coluna de auditoria
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 't_option_entity' AND column_name = 'created_date') THEN
        ALTER TABLE t_option_entity
            ALTER COLUMN created_date SET DEFAULT NOW(),
            ALTER COLUMN created_by   SET DEFAULT 'seed';
    ELSIF EXISTS (SELECT 1 FROM information_schema.columns
                  WHERE table_name = 't_option_entity' AND column_name = 'created_at') THEN
        ALTER TABLE t_option_entity
            ALTER COLUMN created_at SET DEFAULT NOW(),
            ALTER COLUMN created_by SET DEFAULT 'seed';
    END IF;
END $$;

-- (resto dos INSERTs mantém-se exactamente igual)
```

Após alterar: `mvn flyway:repair` nos ambientes afectados.

---

### 🟠 2.4 — V34: `t_cargo` não incluída na correcção de colunas de auditoria

**Ficheiro:** `V34__fix_audit_columns.sql`

**Problema:** V34 renomeia as colunas de auditoria (`created_at → created_date`, etc.) para
`t_option_entity`, `t_unidade_organica` e `t_funcao` — mas **esquece `t_cargo`**. V20 criou
`t_cargo` com os nomes antigos (`created_at`, `updated_at`, `updated_by`). O `CargoEntity`
estende `AuditEntity` que mapeia `created_date`, `last_modified_date`, `last_modified_by`. Em
staging/production com `ddl-auto=validate`, a validação Hibernate falha nesta tabela.

**Correcção:** Adicionar nova migration V35 (ver §3.1).

---

### 🟠 2.5 — Staging/Production não suportam BD fresca

**Problema de design:** Em staging/production com `ddl-auto=validate`, o Hibernate tenta
**validar** o schema no arranque. Numa BD fresca (sem tabelas), esta validação falha
**antes** do Flyway ter oportunidade de criar as tabelas. A aplicação não arranca.

**Comportamento actual:** Staging e production assumem sempre que a BD já foi populada num
ambiente inferior (dev → staging → prod). Nunca são "frescas".

**Mitigação para deploy em BD nova (staging/prod):**

```bash
# Opção A: arrancar temporariamente com ddl-auto=none
# Flyway cria as tabelas; Hibernate não valida.
# Depois voltar para ddl-auto=validate.
SERVICE_PROFILE=staging SPRING_JPA_HIBERNATE_DDL_AUTO=none java -jar RH-Service.jar

# Opção B: correr o Flyway manualmente antes de iniciar a aplicação
mvn flyway:migrate -Dflyway.url=... -Dflyway.user=... -Dflyway.password=...
```

> **Regra operacional:** Para novos deployments em staging/prod, usar sempre uma BD
> previamente migrada ou seguir uma das opções acima.

---

### 🟠 2.6 — V24: `DROP TABLE CASCADE` pode causar perda de dados ao reactivar Flyway

**Ficheiro:** `V24__create_colaboradores_tables.sql`

**Problema:** V24 faz:
```sql
DROP TABLE IF EXISTS t_qualificacao CASCADE;
DROP TABLE IF EXISTS t_dependente CASCADE;
DROP TABLE IF EXISTS t_funcionario CASCADE;
```

Se alguém desactivou o Flyway (`spring.flyway.enabled=false`), inseriu dados reais de
funcionários via Hibernate, e depois reactivou o Flyway num ambiente sem `flyway_schema_history`,
o V24 **apaga todos os dados de funcionários**.

**Mitigação actual:** `baseline-on-migrate=true` — se a BD não está vazia e não tem
`flyway_schema_history`, o Flyway baseia em V1.0 e V24 **não corre novamente**. A protecção
existe, mas depende de nunca se limpar a tabela de histórico.

**Regra operacional:**
> Nunca executar `flyway:clean`, nunca apagar `flyway_schema_history`, e nunca usar
> `flyway:repair` com `--delete-failed-migrations` em ambientes com dados reais.

---

### 🟡 2.7 — V28: FK `job_id` aponta para `t_cargo` em vez de `t_job`

**Ficheiro:** `V28__create_employee_unit_assignments.sql`

**Problema:**
```sql
CONSTRAINT fk_eua_job FOREIGN KEY (job_id) REFERENCES t_cargo(id)
```

`t_cargo` é a tabela do `CargoEntity` (módulo funcionários — cargo/posição de um funcionário).
`t_job` (criada em V22) é a tabela do `JobEntity` (módulo estrutura — definição de função).
O nome `job_id` sugere que a referência correcta é `t_job`.

**Correcção:** Verificar com a equipa e, se confirmado, incluir no V35/V36:

```sql
ALTER TABLE employee_unit_assignments
    DROP CONSTRAINT IF EXISTS fk_eua_job;

ALTER TABLE employee_unit_assignments
    ADD CONSTRAINT fk_eua_job FOREIGN KEY (job_id) REFERENCES t_job(id);
```

---

### 🟡 2.8 — V10 + V30: `TRAINING_TYPE` com dois conjuntos de chaves paralelos

**Ficheiros:** `V10__seed_option_entity_pt_cv.sql`, `V30__create_training.sql`

**Problema:**

| Migration | Chaves inseridas |
|---|---|
| V10 | `INTERNAL`, `EXTERNAL`, `ONLINE`, `SCHOLARSHIP` |
| V30 | `PRESENCIAL`, `ELEARNING`, `SEMINARIO`, `CONGRESSO` |

Resultado: 8 tipos de formação activos, dois conjuntos com nomenclaturas diferentes. As
inserções são idempotentes e não causam erro, mas a aplicação tem de usar um conjunto
consistente.

**Correcção:** Depois de confirmar qual o conjunto usado no código, remover o obsoleto no V35:

```sql
-- Remover chaves inglesas se a aplicação usa as chaves portuguesas do V30
DELETE FROM t_option_entity
WHERE ccode = 'TRAINING_TYPE'
  AND ckey IN ('INTERNAL', 'EXTERNAL', 'ONLINE', 'SCHOLARSHIP');
```

---

### 🔴 2.10 — V11, V12, V13, V15, V16: Seeds omitem colunas NOT NULL sem DEFAULT

**Ficheiros:** `V11` a `V16` (todos os seeds de catálogos excepto V14 e V18)

**Problema:** Quando o Hibernate cria as tabelas **sem defaults** nas colunas de auditoria
(comportamento normal de `ddl-auto=update`, pois o `@CreatedDate` é preenchido pela aplicação
em runtime), as colunas `created_date` e `created_by` ficam como `NOT NULL` sem valor por
omissão. Se o Flyway corre posteriormente (cenário "reactivar Flyway depois de Hibernate"),
as migrations V4/V5/V6/V8/V9 são no-ops (`CREATE TABLE IF NOT EXISTS`) e os defaults nunca
são aplicados. Os seeds seguintes falham com:

```
ERROR: null value in column "created_date" violates not-null constraint
```

**Mapeamento do problema por migration:**

| Seed | Tabela | DDL com defaults? | ALTER defensivo antes do INSERT? |
|---|---|---|---|
| V11 | `t_worker_state` | V4 (IF NOT EXISTS = no-op) | ❌ Não tem |
| V12 | `t_professional_situation` | V5 (IF NOT EXISTS = no-op) | ❌ Não tem |
| V13 | `t_contract_type` | V6 (IF NOT EXISTS = no-op) | ❌ Não tem |
| V14 | `t_tipo_documento` | V7 | ✅ Tem (ALTER antes do INSERT) |
| V15 | `t_leave_type` | V8 (IF NOT EXISTS = no-op) | ❌ Não tem |
| V16 | `t_leave_mobility_subtype` | V9 (IF NOT EXISTS = no-op) | ❌ Não tem |
| V18 | `t_public_holiday` | V17 | ✅ Tem (ALTER antes do INSERT) |

**Correcção:** Modificar V11, V12, V13, V15, V16 para incluir explicitamente os valores
de auditoria no INSERT (abordagem mais robusta do que ALTER, funciona em qualquer estado):

```sql
-- V11__seed_worker_states.sql  (exemplo corrigido)
INSERT INTO t_worker_state (id, code, description, is_core, is_active, created_date, created_by) VALUES
    (gen_random_uuid(), 'ACTIVE',     'Activo',    TRUE,  TRUE, NOW(), 'seed'),
    (gen_random_uuid(), 'INACTIVE',   'Inactivo',  TRUE,  TRUE, NOW(), 'seed'),
    (gen_random_uuid(), 'SUSPENDED',  'Suspenso',  FALSE, TRUE, NOW(), 'seed'),
    (gen_random_uuid(), 'ON_LEAVE',   'De licença',FALSE, TRUE, NOW(), 'seed'),
    (gen_random_uuid(), 'RETIRED',    'Aposentado',FALSE, TRUE, NOW(), 'seed')
ON CONFLICT (code) DO NOTHING;
```

Aplicar o mesmo padrão a V12, V13, V15, V16 — adicionar `created_date, created_by` na
lista de colunas e `NOW(), 'seed'` em cada linha de valores.

Após alterar: `mvn flyway:repair` nos ambientes onde estas migrations já foram aplicadas.

---

### 🔵 2.11 — V18: `ON CONFLICT DO NOTHING` sem target explícito

**Ficheiro:** `V18__seed_public_holidays_2026.sql`

**Problema:** `ON CONFLICT DO NOTHING` sem especificar o target de conflito. É válido em
PostgreSQL 10+ (captura qualquer violação de constraint), mas é menos explícito e pode
silenciar conflitos inesperados.

**Correcção cosmética** (não urgente):
```sql
-- Substituir
ON CONFLICT DO NOTHING;
-- Por
ON CONFLICT ON CONSTRAINT pk_public_holiday DO NOTHING;
```

---

## 3. Migrations novas a criar

### 3.1 — V35: Correcções acumuladas

```sql
-- V35__fixup_cargo_audit_and_constraints.sql

-- 1. Corrigir colunas de auditoria em t_cargo (omitidas no V34)
DO $$
DECLARE tbl TEXT := 't_cargo';
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = tbl AND column_name = 'created_at') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = tbl AND column_name = 'created_date') THEN
            EXECUTE format('ALTER TABLE %I DROP COLUMN created_at', tbl);
        ELSE
            EXECUTE format('ALTER TABLE %I RENAME COLUMN created_at TO created_date', tbl);
        END IF;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = tbl AND column_name = 'updated_at') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = tbl AND column_name = 'last_modified_date') THEN
            EXECUTE format('ALTER TABLE %I DROP COLUMN updated_at', tbl);
        ELSE
            EXECUTE format('ALTER TABLE %I RENAME COLUMN updated_at TO last_modified_date', tbl);
        END IF;
    END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = tbl AND column_name = 'updated_by') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = tbl AND column_name = 'last_modified_by') THEN
            EXECUTE format('ALTER TABLE %I DROP COLUMN updated_by', tbl);
        ELSE
            EXECUTE format('ALTER TABLE %I RENAME COLUMN updated_by TO last_modified_by', tbl);
        END IF;
    END IF;
END $$;

-- 2. Remover TRAINING_TYPE com chaves inglesas (duplicado do V10, substituído pelo V30)
-- ATENÇÃO: só executar depois de confirmar que nenhum código referencia estas chaves
DELETE FROM t_option_entity
WHERE ccode = 'TRAINING_TYPE'
  AND ckey IN ('INTERNAL', 'EXTERNAL', 'ONLINE', 'SCHOLARSHIP');
```

---

## 4. Quadro resumo

| # | Migration | Problema | Severidade | Acção |
|---|---|---|---|---|
| 1 | V1.0 | INSERT sem `CREATE TABLE t_institutions` | 🔴 Crítico | Modificar + `flyway repair` |
| 2 | V3 | `ALTER TABLE t_key_results` sem DDL | 🔴 Crítico | Modificar + `flyway repair` |
| 3 | V10 | `ALTER COLUMN created_date` assume Hibernate | 🔴 Crítico | Modificar + `flyway repair` |
| 4 | V11–V16 | Seeds omitem `created_date`/`created_by` NOT NULL | 🔴 Crítico | Modificar + `flyway repair` |
| 5 | V34 | `t_cargo` esquecida na correcção de audit cols | 🟠 Grave | Adicionar V35 |
| 6 | Design | Staging/prod não suportam BD fresca | 🟠 Grave | Documentar procedimento |
| 7 | V24 | `DROP TABLE CASCADE` apaga dados ao reactivar | 🟠 Grave | Regra operacional |
| 8 | V28 | `job_id` referencia `t_cargo` em vez de `t_job` | 🟡 Médio | Verificar + V35/V36 |
| 9 | V10+V30 | `TRAINING_TYPE` duplicado (EN + PT) | 🟡 Médio | DELETE no V35 |
| 10 | V18 | `ON CONFLICT` sem target explícito | 🔵 Menor | Cosmético |

---

## 5. Guia: como corrigir ficheiros de migration

### 5.1 — Os três cenários possíveis

Antes de qualquer acção, determinar em que estado está a migration em cada ambiente:

| Estado | O que aconteceu | O que fazer |
|---|---|---|
| **Nunca aplicada** | A BD está fresca ou a migration falhou e nunca completou | Editar o ficheiro directamente. Não precisa de `repair`. |
| **Aplicada com sucesso** | O Flyway já correu esta migration e registou o checksum | Editar o ficheiro **e** correr `flyway:repair` nesse ambiente. |
| **Falhou a meio** | O Flyway tentou correr, falhou, e ficou marcada como `FAILED` | Corrigir o problema na BD manualmente, depois editar o ficheiro, depois `flyway:repair`. |

---

### 5.2 — Cenário A: migration nunca aplicada (BD fresca ou ambiente novo)

Simplesmente editar o ficheiro `.sql`. O Flyway vai correr o script pela primeira vez com o
novo conteúdo. Nenhuma acção adicional é necessária.

```
Situação típica:
  - Colega com repositório limpo / BD vazia
  - Pipeline CI/CD com BD de testes recriada
  - Novo ambiente de staging
```

---

### 5.3 — Cenário B: migration já aplicada com sucesso (o caso mais comum)

O Flyway guarda um checksum (hash MD5) do ficheiro no momento em que a migration correu.
Se o ficheiro for alterado depois, o Flyway detecta a diferença e **rejeita o arranque**:

```
ERROR: Validate failed: Migrations have failed validation
Migration checksum mismatch for migration version 11
-> Applied to database : 123456789
-> Resolved locally    : 987654321
```

**Procedimento:**

```bash
# Passo 1 — Editar o ficheiro .sql localmente
# (fazer a correcção necessária)

# Passo 2 — Correr flyway:repair em CADA ambiente onde a migration está aplicada
mvn flyway:repair \
    -Dflyway.url=jdbc:postgresql://HOST:PORT/recursoshumanos_db \
    -Dflyway.user=POSTGRES_USER \
    -Dflyway.password=POSTGRES_PASSWORD

# Passo 3 — Reiniciar a aplicação normalmente
```

`flyway:repair` **não re-executa** a migration. Apenas actualiza o checksum registado em
`flyway_schema_history` para corresponder ao ficheiro actual. A BD mantém-se exactamente
como estava.

> **Atenção:** Se a alteração ao ficheiro mudar o comportamento da migration (e.g., adicionou
> um `CREATE TABLE` que antes não existia), a BD não fica automaticamente corrigida. O
> `repair` só corrige o registo histórico. O estado real da BD tem de ser corrigido
> separadamente (normalmente via uma nova migration V35+).

---

### 5.4 — Cenário C: migration falhou a meio (marcada como FAILED)

Quando uma migration falha, o Flyway marca-a como `FAILED` em `flyway_schema_history` e
**bloqueia todas as migrations seguintes** até o problema ser resolvido. O arranque falha com:

```
ERROR: Flyway detected a failed migration to version 11
```

**Procedimento:**

```bash
# Passo 1 — Perceber o que falhou (verificar o log de arranque)
# Passo 2 — Corrigir o estado da BD manualmente se necessário
#           (e.g., apagar registos parciais, corrigir constraint, etc.)
# Passo 3 — Editar o ficheiro .sql com a correcção
# Passo 4 — Correr flyway:repair para limpar o registo FAILED e actualizar o checksum
mvn flyway:repair \
    -Dflyway.url=jdbc:postgresql://HOST:PORT/recursoshumanos_db \
    -Dflyway.user=USER \
    -Dflyway.password=PASSWORD

# Passo 5 — Reiniciar a aplicação; o Flyway vai re-tentar a migration corrigida
```

> `flyway:repair` num registo `FAILED` apaga esse registo do histórico, permitindo que o
> Flyway tente novamente. **Não corrige a BD** — essa parte é responsabilidade do developer.

**Caso especial — seed que falhou por campo NOT NULL vazio:**

Seeds são só `INSERT`s. Quando falham, o PostgreSQL faz **rollback automático** da
transacção — nenhum dado fica inserido a meio. A BD está limpa. Não é necessário limpar
nada manualmente. Basta:

1. Corrigir o `.sql` (adicionar as colunas NOT NULL em falta, ex: `created_date, NOW()`)
2. `flyway:repair`
3. Reiniciar — o Flyway re-tenta o seed com o ficheiro corrigido

Os `ON CONFLICT DO NOTHING` garantem que, mesmo que o seed tivesse ficado parcialmente
aplicado, re-correr não duplica dados.

---

### 5.5 — Quando NÃO usar `flyway:repair`

| Situação | Acção correcta |
|---|---|
| Quero desfazer uma migration já aplicada | Criar nova migration `VX__rollback_xxx.sql` com o SQL inverso. O Flyway não tem rollback nativo. |
| A migration alterou dados errados | Criar nova migration que corrija os dados. |
| Quero apagar o histórico e recomeçar | Usar `flyway:clean` — **destrói toda a BD**. Nunca usar em produção. |
| Migration falhou mas a BD está correcta | `flyway:repair` resolve (apaga o registo FAILED). |
| Ficheiro foi alterado mas a BD não precisa de mudança | `flyway:repair` resolve (actualiza checksum). |

---

### 5.6 — Referência rápida dos comandos Flyway úteis

```bash
# Ver estado actual de todas as migrations
mvn flyway:info \
    -Dflyway.url=jdbc:postgresql://HOST:PORT/recursoshumanos_db \
    -Dflyway.user=USER -Dflyway.password=PASSWORD

# Validar checksums sem arriscar nada (modo read-only)
mvn flyway:validate \
    -Dflyway.url=... -Dflyway.user=... -Dflyway.password=...

# Aplicar migrations pendentes manualmente (sem iniciar a aplicação)
mvn flyway:migrate \
    -Dflyway.url=... -Dflyway.user=... -Dflyway.password=...

# Corrigir checksums / limpar registos FAILED
mvn flyway:repair \
    -Dflyway.url=... -Dflyway.user=... -Dflyway.password=...

# ⚠️ DESTRUTIVO — apaga toda a BD (nunca usar em prod/staging com dados)
mvn flyway:clean \
    -Dflyway.url=... -Dflyway.user=... -Dflyway.password=...
```

**Via variáveis de ambiente** (alternativa, evita expor credenciais na linha de comandos):

```bash
export FLYWAY_URL=jdbc:postgresql://localhost:5432/recursoshumanos_db
export FLYWAY_USER=postgres
export FLYWAY_PASSWORD=secret
mvn flyway:repair
```

---

### 5.7 — Fluxo de decisão resumido

```
Preciso de corrigir uma migration?
│
├─ Está aplicada em algum ambiente?
│   ├─ NÃO → Editar o ficheiro directamente. Pronto.
│   └─ SIM → A correcção muda o comportamento (DDL/DML)?
│               ├─ NÃO (só texto, formatação, comentários)
│               │     → Editar + flyway:repair em cada ambiente. Pronto.
│               └─ SIM → A BD precisa de ser alterada também?
│                           ├─ NÃO (e.g., tornar idempotente algo que já está no estado certo)
│                           │     → Editar + flyway:repair. Pronto.
│                           └─ SIM → Criar nova migration VX__ para alterar a BD
│                                    + Editar a migration original (para futuros envs frescos)
│                                    + flyway:repair nos ambientes existentes.
```

---

## 5.8 — Casos gerais adicionais (situações menos óbvias)

---

### Caso G1 — Precisas de inserir uma migration entre duas existentes

**Exemplo:** Já tens V10 e V11 aplicadas e percebes que falta um passo entre elas.

O Flyway rejeita migrations "fora de ordem" por omissão:
```
ERROR: Detected resolved migration not applied to database: V10.5
```

**Opções:**

| Opção | Quando usar | Como fazer |
|---|---|---|
| Usar o próximo número disponível (ex: V35) | Sempre que possível — é a forma mais simples e segura | Criar `V35__xxx.sql` normalmente. A BD é corrigida para a frente. |
| Activar `out-of-order` | Só em equipas pequenas com ambientes controlados | Adicionar `spring.flyway.out-of-order=true` nas properties. O Flyway aplica V10.5 mesmo depois de V11 já ter corrido. |

> **Regra prática:** Nunca tentar encaixar uma migration entre versões já aplicadas em
> produção. Usar sempre um número superior e corrigir o estado com SQL de "fixup".

---

### Caso G2 — Queres desfazer completamente o que uma migration já fez (rollback)

O Flyway Community **não tem rollback nativo**. A única forma é criar uma nova migration
com o SQL inverso.

**Exemplos de SQL inverso:**

```sql
-- Se a migration criou uma tabela → a nova migration apaga-a
DROP TABLE IF EXISTS t_exemplo CASCADE;

-- Se a migration adicionou uma coluna → a nova migration remove-a
ALTER TABLE t_exemplo DROP COLUMN IF EXISTS nova_coluna;

-- Se a migration inseriu dados → a nova migration apaga-os
DELETE FROM t_option_entity WHERE ccode = 'CATEGORIA_ERRADA';

-- Se a migration criou um índice → a nova migration remove-o
DROP INDEX IF EXISTS idx_exemplo_coluna;
```

**Procedimento:**

```bash
# 1. Criar VX__rollback_nome_da_migration_original.sql com o SQL inverso
# 2. Commitar e fazer deploy normalmente
# 3. O Flyway aplica a nova migration → BD volta ao estado anterior
```

> **Atenção com dados:** Se a migration original inseriu registos que a aplicação já
> referencia noutras tabelas (FKs), o DELETE pode falhar por constraint. Tratar as
> dependências primeiro ou usar `CASCADE`.

---

### Caso G3 — A migration foi aplicada em staging mas não em dev (ambientes desfasados)

Acontece quando alguém aplica uma migration directamente em staging sem ter passado por dev,
ou quando há branches diferentes com históricos diferentes.

**Como diagnosticar:**

```bash
# Correr flyway:info em cada ambiente e comparar
mvn flyway:info -Dflyway.url=jdbc:postgresql://DEV_HOST:5432/recursoshumanos_db ...
mvn flyway:info -Dflyway.url=jdbc:postgresql://STAGING_HOST:5432/recursoshumanos_db ...
```

O output mostra o estado de cada migration (`Pending`, `Success`, `Failed`, `Missing`):

```
+-----------+------------------------------+---------------------+---------+
| Version   | Description                  | Installed On        | State   |
+-----------+------------------------------+---------------------+---------+
| 1.0       | Seed Institutional Identity  | 2026-01-10 10:00:00 | Success |  ← dev e staging
| ...       | ...                          | ...                 | ...     |
| 35        | Fixup cargo audit            | 2026-05-01 09:00:00 | Success |  ← só staging
| 35        | Fixup cargo audit            |                     | Pending |  ← dev ainda não tem
+-----------+------------------------------+---------------------+---------+
```

**Como resolver:**

```
┌─ Migration existe no ficheiro mas falta na BD de dev?
│   └─ Correr mvn flyway:migrate em dev → aplica as pendentes. Normal.
│
├─ Migration está na BD de staging mas o ficheiro foi apagado do repositório?
│   └─ Situação perigosa. Restaurar o ficheiro do git history.
│      git log --all -- src/main/resources/db/migration/VX__nome.sql
│      git checkout <commit> -- src/main/resources/db/migration/VX__nome.sql
│
└─ Checksum diferente entre ambientes para a mesma versão?
    └─ Alguém editou o ficheiro após aplicar nalgum ambiente.
       Decidir qual a versão correcta do ficheiro e correr flyway:repair
       nos ambientes com checksum desactualizado.
```

---

### Caso G4 — `flyway:validate` falha no arranque sem mensagem clara

O Flyway valida todas as migrations antes de aplicar qualquer nova. Quando falha, a
aplicação não arranca. Os erros mais comuns e o que verificar:

**Erro 1 — Checksum mismatch:**
```
Migration checksum mismatch for migration version 11
-> Applied to database : 123456789
-> Resolved locally    : 987654321
```
→ O ficheiro foi modificado após ter sido aplicado. Correr `flyway:repair`.

**Erro 2 — Migration não aplicada detectada (out-of-order):**
```
Detected resolved migration not applied to database: V10.5
```
→ Foi adicionado um ficheiro com versão inferior à última aplicada. Duas opções:
- Apagar o ficheiro e criar como V35+ (recomendado)
- Activar `spring.flyway.out-of-order=true` (só se souber o que está a fazer)

**Erro 3 — Migration FAILED bloqueada:**
```
Flyway detected a failed migration to version 22
```
→ Uma migration anterior ficou marcada como FAILED. Seguir o §5.4.

**Erro 4 — Ficheiro desapareceu do classpath:**
```
Detected applied migration not resolved locally: V20
```
→ O ficheiro `V20__xxx.sql` foi apagado do repositório mas já foi aplicado na BD.
Restaurar o ficheiro ou, se for intencional, usar:
```bash
# Permite ignorar migrations aplicadas que já não existem no classpath
spring.flyway.ignore-missing-migrations=true
```

---

## 6. Regras para novas migrations

Com base nos problemas encontrados, seguir estas regras ao escrever novas migrations:

1. **DDL antes de DML** — Se uma migration faz seed numa tabela, essa tabela deve ser criada
   na mesma migration (ou numa anterior) com `CREATE TABLE IF NOT EXISTS`.

2. **Sempre defensivo para DDL** — Usar `CREATE TABLE IF NOT EXISTS`, `ADD COLUMN IF NOT
   EXISTS`, `DROP COLUMN IF EXISTS`, `DROP CONSTRAINT IF EXISTS`.

3. **Para ALTER em colunas de auditoria** — Verificar se a coluna existe antes de alterar
   (padrão do V34). As colunas podem ter nomes diferentes consoante o ambiente veio do
   Hibernate ou do Flyway.

4. **Seeds idempotentes** — Todos os `INSERT` devem ter `ON CONFLICT (...) DO NOTHING`
   com o target explícito (coluna ou constraint). Usar sempre o nome da constraint ou a
   coluna de unicidade:
   ```sql
   -- ✅ Correcto (target explícito)
   ON CONFLICT (code) DO NOTHING
   ON CONFLICT ON CONSTRAINT uq_worker_state_code DO NOTHING
   -- ⚠️ Aceitável mas menos explícito
   ON CONFLICT DO NOTHING
   ```

5. **Incluir SEMPRE colunas NOT NULL nos INSERTs de seed** — Mesmo que a tabela tenha
   DEFAULT declarado no DDL, ao reactivar Flyway numa BD Hibernate-only as colunas de
   auditoria ficam sem default. Regra prática: incluir sempre `created_date` e `created_by`
   explicitamente em todos os `INSERT` de seed:
   ```sql
   -- ✅ Correcto — funciona em qualquer estado da BD
   INSERT INTO t_worker_state (id, code, description, is_core, is_active, created_date, created_by)
   VALUES (gen_random_uuid(), 'ACTIVE', 'Activo', TRUE, TRUE, NOW(), 'seed')
   ON CONFLICT (code) DO NOTHING;

   -- ❌ Frágil — falha se a tabela não tiver DEFAULT nas colunas de auditoria
   INSERT INTO t_worker_state (id, code, description, is_core, is_active)
   VALUES (gen_random_uuid(), 'ACTIVE', 'Activo', TRUE, TRUE)
   ON CONFLICT (code) DO NOTHING;
   ```

6. **Não depender do Hibernate** — As migrations devem funcionar numa BD completamente
   fresca sem o Hibernate ter corrido. O Hibernate pode correr depois e validar/actualizar,
   mas não pode ser pré-condição.

7. **Nunca DROP sem IF EXISTS** — E documentar claramente o impacto em dados existentes.

8. **Foreign keys entre tabelas novas** — Criar as tabelas referenciadas na mesma migration
   ou em migrations anteriores. Nunca assumir que uma tabela existe por ter sido criada pelo
   Hibernate.

9. **Testar em BD fresca antes de commitar** — Antes de fazer push de uma migration nova,
   verificar que corre correctamente numa BD completamente vazia:
   ```bash
   # Destruir a BD de testes local e recriar
   docker-compose down -v && docker-compose up -d
   # Arrancar a aplicação em dev — se arrancar limpo, a migration está correcta
   mvn spring-boot:run -Dspring-boot.run.profiles=development
   ```
