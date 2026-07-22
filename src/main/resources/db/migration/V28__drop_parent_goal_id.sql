-- =============================================================
-- V28 — t_strategic_goals: remover coluna parent_goal_id (hierarquia pai/filho morta, RELA-04)
-- parentGoalId nunca foi lido/escrito por nenhum handler -- a coluna é sempre NULL em todas
-- as linhas existentes. Sem FK a limpar (V25 nunca declarou REFERENCES nesta coluna).
-- Esta migration é idempotente: só executa se a tabela e a coluna existirem.
-- =============================================================

DO $$ BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name   = 't_strategic_goals'
    ) AND EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name   = 't_strategic_goals'
          AND column_name  = 'parent_goal_id'
    ) THEN
        ALTER TABLE t_strategic_goals DROP COLUMN parent_goal_id;
    END IF;
END $$;
