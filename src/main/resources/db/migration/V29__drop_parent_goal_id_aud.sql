-- =============================================================
-- V29 — audit_schema.t_strategic_goals_aud: remover coluna parent_goal_id (RELA-04)
-- Idempotente: verifica schema, tabela e coluna antes de executar.
-- =============================================================

DO $$ BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.schemata
        WHERE schema_name = 'audit_schema'
    ) AND EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'audit_schema'
          AND table_name   = 't_strategic_goals_aud'
    ) AND EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'audit_schema'
          AND table_name   = 't_strategic_goals_aud'
          AND column_name  = 'parent_goal_id'
    ) THEN
        ALTER TABLE audit_schema.t_strategic_goals_aud DROP COLUMN parent_goal_id;
    END IF;
END $$;
