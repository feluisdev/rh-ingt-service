-- =============================================================
-- V14 — audit_schema.t_unidade_organica_aud: remover coluna estado (legado)
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
          AND table_name   = 't_unidade_organica_aud'
    ) AND EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'audit_schema'
          AND table_name   = 't_unidade_organica_aud'
          AND column_name  = 'estado'
    ) THEN
        ALTER TABLE audit_schema.t_unidade_organica_aud DROP COLUMN estado;
    END IF;
END $$;
