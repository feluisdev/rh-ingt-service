-- =============================================================
-- V41 — mobilidade transitória
-- A mobilidade transitória NÃO ocupa lugar do quadro no destino
-- (Lei n.º 20/X/2023, art. 135.º n.º 7) e tem duração máxima de um ano,
-- prorrogável uma única vez (art. 132.º n.º 5). Os limites ficam
-- parametrizados no subtipo, para não viverem no código.
--
-- Defensivo em todos os passos: as tabelas podem não existir (esquema
-- criado pelo ddl-auto) e as colunas podem já ter sido acrescentadas.
-- =============================================================

DO $$ BEGIN
    IF to_regclass('public.t_leave_mobility_subtype') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_mobility_subtype'
                         AND column_name='max_duration_days') THEN
            ALTER TABLE t_leave_mobility_subtype ADD COLUMN max_duration_days INTEGER;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_mobility_subtype'
                         AND column_name='max_extensions') THEN
            ALTER TABLE t_leave_mobility_subtype ADD COLUMN max_extensions INTEGER;
        END IF;
    END IF;
END $$;

DO $$ BEGIN
    IF to_regclass('audit_schema.t_leave_mobility_subtype_aud') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_mobility_subtype_aud'
                         AND column_name='max_duration_days') THEN
            ALTER TABLE audit_schema.t_leave_mobility_subtype_aud ADD COLUMN max_duration_days INTEGER;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_mobility_subtype_aud'
                         AND column_name='max_extensions') THEN
            ALTER TABLE audit_schema.t_leave_mobility_subtype_aud ADD COLUMN max_extensions INTEGER;
        END IF;
    END IF;
END $$;

-- Contagem de prorrogações já concedidas, por processo.
DO $$ BEGIN
    IF to_regclass('public.t_leave_mobility') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_mobility'
                         AND column_name='extensions_count') THEN
        ALTER TABLE t_leave_mobility ADD COLUMN extensions_count INTEGER DEFAULT 0;
    END IF;
END $$;

DO $$ BEGIN
    IF to_regclass('audit_schema.t_leave_mobility_aud') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_mobility_aud'
                         AND column_name='extensions_count') THEN
        ALTER TABLE audit_schema.t_leave_mobility_aud ADD COLUMN extensions_count INTEGER;
    END IF;
END $$;

-- Limites por omissão para os subtipos de mobilidade existentes:
-- um ano, prorrogável uma vez (art. 132.º n.º 5). Só preenche o que estiver vazio.
DO $$ BEGIN
    IF to_regclass('public.t_leave_mobility_subtype') IS NOT NULL
       AND EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='t_leave_mobility_subtype'
                     AND column_name='max_duration_days') THEN
        UPDATE t_leave_mobility_subtype
           SET max_duration_days = 365, max_extensions = 1
         WHERE record_type = 'MOBILIDADE' AND max_duration_days IS NULL;
    END IF;
END $$;
