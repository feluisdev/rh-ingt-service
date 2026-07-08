-- Migration: V22__siadap_interim_objective_revision_code.sql
-- 61-CONTEXT.md: adds nullable objective_code (references t_evaluation_objectives.objective_code
-- by value, not FK — no cross-table constraint, matching the codebase's existing pattern of not
-- enforcing this relationship at the DB layer). See 61-RESEARCH.md Pitfall 2 for why the audit
-- table below needs a new-baseline CREATE, not a blind ALTER: V18 created
-- t_siadap_interim_objective_revisions_aud UNQUALIFIED (implicitly public schema), but the
-- active Envers config (org.hibernate.envers.default_schema=audit_schema) targets audit_schema
-- at runtime — the same class of mismatch V20 already fixed for t_siadap_evaluations_aud, never
-- applied to this table family.

-- 1. Main table (confirmed exists, created unqualified/public by V18) — idempotent ALTER.
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_siadap_interim_objective_revisions'
          AND column_name = 'objective_code'
    ) THEN
        ALTER TABLE t_siadap_interim_objective_revisions ADD COLUMN objective_code VARCHAR(50);
    END IF;
END $$;

-- 2. audit_schema shadow table — NEW BASELINE (may not exist yet under this schema in every
-- environment; CREATE TABLE IF NOT EXISTS is safe regardless).
CREATE TABLE IF NOT EXISTS audit_schema.t_siadap_interim_objective_revisions_aud (
    id                       UUID NOT NULL,
    rev                      INTEGER NOT NULL,
    revtype                  SMALLINT,
    evaluation_id            UUID,
    current_objective_text   TEXT,
    revision_justification   TEXT,
    new_objective_smart      TEXT,
    approval_status          VARCHAR(50),
    objective_code           VARCHAR(50),
    created_by               VARCHAR(255),
    created_date             TIMESTAMP,
    last_modified_by         VARCHAR(255),
    last_modified_date       TIMESTAMP,
    PRIMARY KEY (id, rev)
);

-- 3. Idempotent guard in case the audit_schema table already existed (e.g. auto-created by
-- Hibernate ddl-auto=update in dev/staging) without the new column.
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'audit_schema' AND table_name = 't_siadap_interim_objective_revisions_aud'
          AND column_name = 'objective_code'
    ) THEN
        ALTER TABLE audit_schema.t_siadap_interim_objective_revisions_aud ADD COLUMN objective_code VARCHAR(50);
    END IF;
END $$;

-- 4. Defensive: keep the legacy unqualified/public shadow table (V18's original) in sync too,
-- ONLY if it exists — harmless no-op in environments where it was never created.
DO $$ BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 't_siadap_interim_objective_revisions_aud'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_siadap_interim_objective_revisions_aud'
          AND column_name = 'objective_code'
    ) THEN
        ALTER TABLE public.t_siadap_interim_objective_revisions_aud ADD COLUMN objective_code VARCHAR(50);
    END IF;
END $$;
