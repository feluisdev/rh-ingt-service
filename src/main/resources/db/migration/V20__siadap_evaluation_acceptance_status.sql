-- Migration: V20__siadap_evaluation_acceptance_status.sql
-- This is a NEW BASELINE migration, not an ALTER. `t_siadap_evaluations` has no prior
-- Flyway migration — it was created entirely via Hibernate ddl-auto=update in dev/staging
-- (see 60-RESEARCH.md Pitfall 1), so a clean/production database (which runs
-- ddl-auto=validate + Flyway-only) never actually has this table. This migration creates
-- both t_siadap_evaluations and its Envers audit shadow table for the first time,
-- with the new `acceptance_status` (dual-acceptance state machine) column included from
-- the start, following the V19 (t_paa_submission_period) precedent.
--
-- Column list reconstructed from SiadapEvaluationEntity's @Column annotations
-- (Assumption A1, 60-RESEARCH.md).

CREATE TABLE IF NOT EXISTS t_siadap_evaluations (
    id                     UUID PRIMARY KEY,
    employee_id            VARCHAR(255) NOT NULL,
    year                   VARCHAR(10),
    organic_unit_id        VARCHAR(255),
    evaluator_id           VARCHAR(255),
    evaluation_phase       VARCHAR(30),
    acceptance_status      VARCHAR(30),
    self_evaluation_score  NUMERIC(4,2),
    objectives_score       NUMERIC,
    competencies_score     NUMERIC,
    final_score            NUMERIC,
    results_weight         NUMERIC(5,2),
    competencies_weight    NUMERIC(5,2),
    merit_rating           VARCHAR(50),
    validated_quota        BOOLEAN NOT NULL DEFAULT FALSE,
    created_date           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by             VARCHAR(255) NOT NULL DEFAULT 'system',
    last_modified_date     TIMESTAMP,
    last_modified_by       VARCHAR(255)
);

-- Idempotent guard: if the table already existed in this environment (created by
-- ddl-auto=update in dev/staging before this migration ran), ensure `acceptance_status`
-- is present rather than assuming CREATE TABLE ran fresh.
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_siadap_evaluations' AND column_name = 'acceptance_status'
    ) THEN
        ALTER TABLE t_siadap_evaluations ADD COLUMN acceptance_status VARCHAR(30);
    END IF;
END $$;

-- Envers audit table — explicitly qualified under audit_schema, following the newer V19
-- convention (60-RESEARCH.md Pitfall 6), not V18's unqualified (implicitly public) style.
CREATE TABLE IF NOT EXISTS audit_schema.t_siadap_evaluations_aud (
    id                     UUID         NOT NULL,
    rev                    INTEGER      NOT NULL,
    revtype                SMALLINT,
    employee_id            VARCHAR(255),
    year                   VARCHAR(10),
    organic_unit_id        VARCHAR(255),
    evaluator_id           VARCHAR(255),
    evaluation_phase       VARCHAR(30),
    acceptance_status      VARCHAR(30),
    self_evaluation_score  NUMERIC(4,2),
    objectives_score       NUMERIC,
    competencies_score     NUMERIC,
    final_score            NUMERIC,
    results_weight         NUMERIC(5,2),
    competencies_weight    NUMERIC(5,2),
    merit_rating           VARCHAR(50),
    validated_quota        BOOLEAN,
    created_date           TIMESTAMP,
    created_by             VARCHAR(255),
    last_modified_date     TIMESTAMP,
    last_modified_by       VARCHAR(255),
    PRIMARY KEY (id, rev)
);

-- Idempotent guard for the audit table, mirroring the main-table guard above.
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'audit_schema' AND table_name = 't_siadap_evaluations_aud' AND column_name = 'acceptance_status'
    ) THEN
        ALTER TABLE audit_schema.t_siadap_evaluations_aud ADD COLUMN acceptance_status VARCHAR(30);
    END IF;
END $$;
