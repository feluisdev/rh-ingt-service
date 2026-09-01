-- Migration: V26__bsc_perspective_config.sql
-- Creates the single source of configuration for the 4 fixed BSC perspectives' label and
-- display order (PERSP-01/PERSP-03). Exactly 4 rows, one per fixed internal code
-- (FINANCIAL/CUSTOMER/PROCESS/LEARNING) -- no insert/delete operation is ever exposed above
-- this table (CONTEXT.md: "sem operação exposta de criar/apagar linha").
--
-- IMPORTANT -- seed order source: the order below (Financial=1, Customer=2, Process=3,
-- Learning=4) matches StrategyMapPage.tsx's PERSPECTIVES const and CONTEXT.md's locked
-- decision, so first-deploy behaviour is identical to today with no manual config step.
-- This is intentionally NOT the same order as StrategicGoalsPerspective.java's enum
-- declaration (FINANCIAL, CUSTOMER, LEARNING, PROCESS) -- that enum's declaration order is a
-- different, currently-inconsistent order, independently tracked and to be reconciled by
-- RELA-03 (Phase 76). Do not "fix" this seed to match the enum -- the enum is the one that is
-- wrong relative to the UI, not this seed.
--
-- Defensive idempotent-guard pattern mirrors V19/V20/V22/V25: ddl-auto=update is active in
-- development/staging (application-development.properties, application-staging.properties),
-- so a locally-booted app that creates this @Entity before this migration runs could otherwise
-- race Flyway and leave the table un-seeded / without the audit shadow table.
--
-- Structural isolation (PERSP-03): this table has no foreign key to/from t_strategic_goals and
-- is never referenced by it -- t_strategic_goals.perspective is a bare VARCHAR(30) matched only
-- by the immutable `code` string. Relabeling/reordering here cannot migrate or affect any
-- existing strategic goal.

CREATE TABLE IF NOT EXISTS t_bsc_perspective_config (
    id                  UUID PRIMARY KEY,
    code                VARCHAR(30) NOT NULL UNIQUE,
    label               VARCHAR(60) NOT NULL,
    display_order       INTEGER NOT NULL UNIQUE,
    created_date        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(255) NOT NULL DEFAULT 'system',
    last_modified_date  TIMESTAMP,
    last_modified_by    VARCHAR(255)
);

-- Idempotent seed: only inserts rows for codes that don't already exist, so re-running this
-- migration against a ddl-auto-created (but empty) table neither errors nor duplicates rows.
--
-- created_date is supplied explicitly instead of relying on the column DEFAULT declared above.
-- The DEFAULT only exists when this migration created the table. When ddl-auto=update got
-- there first, Hibernate emits the NOT NULL but no DEFAULT, so omitting the column here fails
-- with a not-null violation -- which is exactly what happens on a from-scratch database, where
-- V24 forces the app to boot with Flyway disabled (t_key_results is created by Hibernate, not
-- by any migration) and Hibernate therefore creates this table before Flyway ever sees it.
INSERT INTO t_bsc_perspective_config (id, code, label, display_order, created_date, created_by)
SELECT gen_random_uuid(), v.code, v.label, v.display_order, CURRENT_TIMESTAMP, 'system'
FROM (VALUES
    ('FINANCIAL', 'Financeira',                  1),
    ('CUSTOMER',  'Cliente / Mercado',            2),
    ('PROCESS',   'Processos Internos',           3),
    ('LEARNING',  'Aprendizagem e Crescimento',   4)
) AS v(code, label, display_order)
WHERE NOT EXISTS (SELECT 1 FROM t_bsc_perspective_config WHERE code = v.code);

-- Envers audit shadow table, mirrors V25's t_strategic_goals_aud convention.
CREATE TABLE IF NOT EXISTS audit_schema.t_bsc_perspective_config_aud (
    id                  UUID         NOT NULL,
    rev                 INTEGER      NOT NULL,
    revtype             SMALLINT,
    code                VARCHAR(30),
    label               VARCHAR(60),
    display_order       INTEGER,
    created_date        TIMESTAMP,
    created_by          VARCHAR(255),
    last_modified_date  TIMESTAMP,
    last_modified_by    VARCHAR(255),
    PRIMARY KEY (id, rev)
);
