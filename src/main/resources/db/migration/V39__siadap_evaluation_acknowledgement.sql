-- Migration: V39__siadap_evaluation_acknowledgement.sql
-- CIK-01: Suporta o registo formal de tomada de conhecimento e contraditório do avaliado

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_siadap_evaluations'
          AND column_name = 'acknowledgement_status'
    ) THEN
        ALTER TABLE t_siadap_evaluations ADD COLUMN acknowledgement_status VARCHAR(30);
        ALTER TABLE t_siadap_evaluations ADD COLUMN acknowledgement_comment TEXT;
        ALTER TABLE t_siadap_evaluations ADD COLUMN acknowledged_at TIMESTAMP;
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'audit_schema' AND table_name = 't_siadap_evaluations_aud'
          AND column_name = 'acknowledgement_status'
    ) THEN
        ALTER TABLE audit_schema.t_siadap_evaluations_aud ADD COLUMN acknowledgement_status VARCHAR(30);
        ALTER TABLE audit_schema.t_siadap_evaluations_aud ADD COLUMN acknowledgement_comment TEXT;
        ALTER TABLE audit_schema.t_siadap_evaluations_aud ADD COLUMN acknowledged_at TIMESTAMP;
    END IF;
END $$;
