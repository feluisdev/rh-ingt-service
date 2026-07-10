-- Migration: V21__siadap_evaluation_last_negotiation_comment.sql
-- WR-02 (60-REVIEW.md): the avaliado's negotiation-request comment was accepted by the API
-- but silently discarded — never persisted, never surfaced to the avaliador. Adds a single
-- "last comment" column (no full history — out of scope per REQUIREMENTS.md), following the
-- V20 precedent (idempotent ALTER guard on both the main table and its Envers audit shadow).

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_siadap_evaluations'
          AND column_name = 'last_negotiation_comment'
    ) THEN
        ALTER TABLE t_siadap_evaluations ADD COLUMN last_negotiation_comment TEXT;
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'audit_schema' AND table_name = 't_siadap_evaluations_aud'
          AND column_name = 'last_negotiation_comment'
    ) THEN
        ALTER TABLE audit_schema.t_siadap_evaluations_aud ADD COLUMN last_negotiation_comment TEXT;
    END IF;
END $$;
