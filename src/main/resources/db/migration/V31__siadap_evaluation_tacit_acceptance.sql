-- Migration: V31__siadap_evaluation_tacit_acceptance.sql
-- SIA-03 (103-VALIDATION.md, criterio 4): distingue "autoavaliou-se" de "foi aceite
-- tacitamente" -- `self_evaluation_score IS NULL` nao serve como sinal, porque tambem
-- e null antes da pessoa decidir. Segue o precedente V21 (guarda idempotente na tabela
-- principal e na sua sombra Envers em audit_schema).

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_siadap_evaluations'
          AND column_name = 'self_evaluation_tacitly_accepted'
    ) THEN
        ALTER TABLE t_siadap_evaluations ADD COLUMN self_evaluation_tacitly_accepted BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'audit_schema' AND table_name = 't_siadap_evaluations_aud'
          AND column_name = 'self_evaluation_tacitly_accepted'
    ) THEN
        ALTER TABLE audit_schema.t_siadap_evaluations_aud ADD COLUMN self_evaluation_tacitly_accepted BOOLEAN;
    END IF;
END $$;
