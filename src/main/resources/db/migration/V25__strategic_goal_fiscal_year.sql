-- Migration: V25__strategic_goal_fiscal_year.sql
-- This is a NEW BASELINE migration, not an ALTER. `t_strategic_goals` has no prior
-- Flyway migration (verified: zero references across V1-V24) — it was created entirely
-- via Hibernate ddl-auto=update in dev/staging, exactly the same underlying condition
-- already fixed for `t_paa_submission_period` in V19__paa_submission_period_purpose.sql.
-- A bare `ALTER TABLE t_strategic_goals ADD COLUMN year INTEGER;` would fail outright on
-- any genuinely fresh database (Flyway running before Hibernate has ever created the
-- table), e.g. production (ddl-auto=validate) or a fresh CI database. This migration
-- creates both t_strategic_goals and its Envers audit shadow table for the first time,
-- with the new `year` (fiscal year, nullable) column included from the start, mirroring
-- V19's CREATE TABLE IF NOT EXISTS + idempotent-guard pattern exactly.
--
-- Column list/types reconstructed from StrategicGoalEntity.java's current JPA mappings.
-- `weight`'s NUMERIC precision/scale is not pinned in the entity annotations
-- (@Column(name="weight") with no explicit precision/scale); the live dev/staging DB was
-- not reachable to confirm (no running Postgres container for this project at execution
-- time), so NUMERIC(19,2) is kept to match the existing V19/V24 precedent for equivalent
-- monetary/weight columns in this schema — flagged for human verification.
-- No foreign keys are declared below (mirrors V19's own t_paa_submission_period baseline)
-- to avoid table-creation-ordering dependencies on t_institutional_identity.

CREATE TABLE IF NOT EXISTS t_strategic_goals (
    id                  UUID PRIMARY KEY,
    institution_id      UUID,
    identity_id         UUID,
    parent_goal_id      UUID,
    title               VARCHAR(255) NOT NULL,
    perspective         VARCHAR(30),
    weight              NUMERIC(19,2),
    status              VARCHAR(20),
    description         TEXT,
    position_x          DOUBLE PRECISION,
    position_y          DOUBLE PRECISION,
    year                INTEGER,
    created_date        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(255) NOT NULL DEFAULT 'system',
    last_modified_date  TIMESTAMP,
    last_modified_by    VARCHAR(255)
);

-- Idempotent guard: if the table already existed in this environment (created by
-- ddl-auto=update in dev/staging before this migration ran), ensure `year` is present
-- rather than assuming CREATE TABLE ran fresh.
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_strategic_goals' AND column_name = 'year'
    ) THEN
        ALTER TABLE t_strategic_goals ADD COLUMN year INTEGER;
    END IF;
END $$;

-- Envers audit table — full business-column replication, mirroring V19's convention.
CREATE TABLE IF NOT EXISTS audit_schema.t_strategic_goals_aud (
    id                  UUID         NOT NULL,
    rev                 INTEGER      NOT NULL,
    revtype             SMALLINT,
    institution_id      UUID,
    identity_id         UUID,
    parent_goal_id      UUID,
    title               VARCHAR(255),
    perspective         VARCHAR(30),
    weight              NUMERIC(19,2),
    status              VARCHAR(20),
    description         TEXT,
    position_x          DOUBLE PRECISION,
    position_y          DOUBLE PRECISION,
    year                INTEGER,
    created_date        TIMESTAMP,
    created_by          VARCHAR(255),
    last_modified_date  TIMESTAMP,
    last_modified_by    VARCHAR(255),
    PRIMARY KEY (id, rev)
);

-- Idempotent guard for the audit table, mirroring the main-table guard above.
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'audit_schema' AND table_name = 't_strategic_goals_aud' AND column_name = 'year'
    ) THEN
        ALTER TABLE audit_schema.t_strategic_goals_aud ADD COLUMN year INTEGER;
    END IF;
END $$;
