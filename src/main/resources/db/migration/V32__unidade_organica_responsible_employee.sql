-- Migration: V32__unidade_organica_responsible_employee.sql
-- ORG-01, Fase 109. NOVA BASELINE, nao um ALTER.
--
-- Numeracao: V32, e nao V24. V24__update_kpi_criteria_fix.sql existe em disco, esta
-- aplicada na base do operador e nunca foi commitada (git status: ??). Reutilizar o 24
-- faria o Flyway comparar o checksum do ficheiro novo contra o checksum ja registado na
-- base local e falhar a validacao. V31 e a mais alta commitada.
--
-- Forma: t_unidade_organica nunca teve migracao Flyway que a CRIASSE -- V7, V13 e V14
-- mexem-lhe e nenhuma a cria; foi sempre gerada por ddl-auto=update em development e
-- staging. Uma base production (ddl-auto=validate) nunca a teria. O perigo e de ORDEM:
-- o Flyway corre antes do ddl-auto, pelo que um ALTER puro falha num development limpo.
-- Reproduz a forma exata que o Hibernate ja gera a partir de OrganizationalUnitEntity +
-- AuditEntity, com responsible_employee_id incluido de origem. Precedente: V19, V20,
-- V22, V25, V26, V27.
--
-- responsible_employee_id e UUID nu, sem FK, espelhando parent_unit_id na mesma tabela e
-- evaluator_id/organic_unit_id em t_siadap_evaluations. Nenhum apontador cross-agregado
-- deste projeto tem FK; acrescentar uma criaria dependencia de ordem de criacao entre
-- t_unidade_organica e t_funcionario sem precedente que a justifique.

CREATE TABLE IF NOT EXISTS t_unidade_organica (
    id                       UUID PRIMARY KEY,
    code                     VARCHAR(50)  NOT NULL UNIQUE,
    name                     VARCHAR(200) NOT NULL,
    acronym                  VARCHAR(20)  NOT NULL,
    type                     VARCHAR(100),
    descricao                VARCHAR(255),
    parent_unit_id           UUID,
    responsible_employee_id  UUID,
    is_active                BOOLEAN      NOT NULL DEFAULT TRUE,
    created_date             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by               VARCHAR(255) NOT NULL DEFAULT 'system',
    last_modified_date       TIMESTAMP,
    last_modified_by         VARCHAR(255)
);

-- Guarda idempotente na tabela principal: se ela ja existia neste ambiente (criada pelo
-- ddl-auto=update antes desta migracao correr), o CREATE acima foi um no-op e a coluna
-- nova continuaria a faltar. Mesmo padrao de V19/V20/V22/V25/V31 -- que sao as que
-- de facto tem guarda ADD COLUMN; V26 e V27 criam tabelas novas e nao tem nenhuma.
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_unidade_organica'
          AND column_name = 'responsible_employee_id'
    ) THEN
        ALTER TABLE t_unidade_organica ADD COLUMN responsible_employee_id UUID;
    END IF;
END $$;

-- Sombra Envers. Colunas de negocio sem restricoes e sem FK: uma linha historica do
-- Envers tem de sobreviver a remocao da linha que referencia. Precedente V25/V26/V27.
-- O esquema e audit_schema, e nao os dois: ver EnversAuditSchemaResolutionTest, que mede
-- a resolucao de spring.jpa.properties.org.hibernate.envers.default_schema em cada perfil.
CREATE TABLE IF NOT EXISTS audit_schema.t_unidade_organica_aud (
    id                       UUID    NOT NULL,
    rev                      INTEGER NOT NULL,
    revtype                  SMALLINT,
    code                     VARCHAR(50),
    name                     VARCHAR(200),
    acronym                  VARCHAR(20),
    type                     VARCHAR(100),
    descricao                VARCHAR(255),
    parent_unit_id           UUID,
    responsible_employee_id  UUID,
    is_active                BOOLEAN,
    created_date             TIMESTAMP,
    created_by               VARCHAR(255),
    last_modified_date       TIMESTAMP,
    last_modified_by         VARCHAR(255),
    PRIMARY KEY (id, rev)
);

-- Guarda idempotente da sombra em audit_schema. Molde literal de V14, que faz o mesmo
-- percurso schemata -> tables -> columns sobre esta mesma tabela.
DO $$ BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.schemata WHERE schema_name = 'audit_schema'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'audit_schema' AND table_name = 't_unidade_organica_aud'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'audit_schema' AND table_name = 't_unidade_organica_aud'
          AND column_name = 'responsible_employee_id'
    ) THEN
        ALTER TABLE audit_schema.t_unidade_organica_aud ADD COLUMN responsible_employee_id UUID;
    END IF;
END $$;

-- Reparacao, nao criacao. Se algum ambiente tiver a sombra em public (cenario em que a
-- propriedade envers.default_schema nao resolvesse), esta guarda acrescenta-lhe a coluna.
-- Nao cria a tabela: um ambiente que nao a tenha em public nao ganha um artefacto vazio.
DO $$ BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 't_unidade_organica_aud'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_unidade_organica_aud'
          AND column_name = 'responsible_employee_id'
    ) THEN
        ALTER TABLE public.t_unidade_organica_aud ADD COLUMN responsible_employee_id UUID;
    END IF;
END $$;
