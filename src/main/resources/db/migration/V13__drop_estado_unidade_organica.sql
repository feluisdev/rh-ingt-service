-- =============================================================
-- V13 — t_unidade_organica: remover coluna estado (legado)
-- O campo `estado` (Boolean) foi substituído por `is_active` (boolean).
-- Esta migration é idempotente: só executa se a tabela e a coluna existirem.
-- =============================================================

DO $$ BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name   = 't_unidade_organica'
    ) AND EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name   = 't_unidade_organica'
          AND column_name  = 'estado'
    ) THEN
        ALTER TABLE t_unidade_organica DROP COLUMN estado;
    END IF;
END $$;
