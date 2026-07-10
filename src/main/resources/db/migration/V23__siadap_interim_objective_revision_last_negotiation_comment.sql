-- Migration: V23__siadap_interim_objective_revision_last_negotiation_comment.sql
-- 62-RESEARCH.md: adds a single "last comment" column to t_siadap_interim_objective_revisions,
-- mirroring V21's last_negotiation_comment column on t_siadap_evaluations (no full history —
-- out of scope per REQUIREMENTS.md). This is a simple 2-guard idempotent ALTER, NOT a
-- new-baseline dance like V22: V22 already created audit_schema.t_siadap_interim_objective_revisions_aud
-- as a full new-baseline table, so its existence under audit_schema is already guaranteed in every
-- environment — this answers CONTEXT.md's own open question about whether that guard is now moot.

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_siadap_interim_objective_revisions'
          AND column_name = 'last_negotiation_comment'
    ) THEN
        ALTER TABLE t_siadap_interim_objective_revisions ADD COLUMN last_negotiation_comment TEXT;
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'audit_schema' AND table_name = 't_siadap_interim_objective_revisions_aud'
          AND column_name = 'last_negotiation_comment'
    ) THEN
        ALTER TABLE audit_schema.t_siadap_interim_objective_revisions_aud ADD COLUMN last_negotiation_comment TEXT;
    END IF;
END $$;
