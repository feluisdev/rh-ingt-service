-- Migration: V19__paa_submission_period_purpose.sql
-- This is a NEW BASELINE migration, not an ALTER. `t_paa_submission_period` has no prior
-- Flyway migration — it was created entirely via Hibernate ddl-auto=update in dev/staging
-- (see 59-RESEARCH.md Pitfall 2), so a clean/production database (which runs
-- ddl-auto=validate + Flyway-only) never actually has this table. This migration creates
-- both t_paa_submission_period and its Envers audit shadow table for the first time,
-- with the new `purpose` (PAA | SIADAP) discriminator column included from the start.

CREATE TABLE IF NOT EXISTS t_paa_submission_period (
    id                  UUID PRIMARY KEY,
    type                VARCHAR(30)  NOT NULL,
    purpose             VARCHAR(20)  NOT NULL DEFAULT 'PAA',
    start_date          DATE         NOT NULL,
    end_date            DATE         NOT NULL,
    status              VARCHAR(20)  NOT NULL,
    year                INTEGER      NOT NULL,
    created_date        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(255) NOT NULL DEFAULT 'system',
    last_modified_date  TIMESTAMP,
    last_modified_by    VARCHAR(255)
);

-- Idempotent guard: if the table already existed in this environment (created by
-- ddl-auto=update in dev/staging before this migration ran), ensure `purpose` is
-- present with the correct default rather than assuming CREATE TABLE ran fresh.
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_paa_submission_period' AND column_name = 'purpose'
    ) THEN
        ALTER TABLE t_paa_submission_period ADD COLUMN purpose VARCHAR(20) NOT NULL DEFAULT 'PAA';
    END IF;
END $$;

-- Envers audit table — full business-column replication, following the newer V18
-- convention (59-RESEARCH.md Pitfall 6) rather than the older minimal id/rev/revtype pattern.
CREATE TABLE IF NOT EXISTS audit_schema.t_paa_submission_period_aud (
    id                  UUID         NOT NULL,
    rev                 INTEGER      NOT NULL,
    revtype             SMALLINT,
    type                VARCHAR(30),
    purpose             VARCHAR(20),
    start_date          DATE,
    end_date            DATE,
    status              VARCHAR(20),
    year                INTEGER,
    created_date        TIMESTAMP,
    created_by          VARCHAR(255),
    last_modified_date  TIMESTAMP,
    last_modified_by    VARCHAR(255),
    PRIMARY KEY (id, rev)
);
