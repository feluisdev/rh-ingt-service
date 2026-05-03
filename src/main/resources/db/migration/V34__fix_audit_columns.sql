-- Fix audit column names to match Hibernate AuditEntity convention.
--
-- Two possible states depending on prior ddl-auto setting:
--   validate/none:  only created_at/updated_at exist  → rename them
--   update:         Hibernate already added created_date/last_modified_date as new
--                   columns alongside the old ones    → drop the old ones
--
-- This script handles both cases safely (idempotent).

DO $$
DECLARE
    tbl TEXT;
BEGIN
    FOREACH tbl IN ARRAY ARRAY['t_option_entity', 't_unidade_organica', 't_funcao'] LOOP

        -- created_at / created_date
        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = tbl AND column_name = 'created_at') THEN
            IF EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name = tbl AND column_name = 'created_date') THEN
                -- Both exist (update environment): drop the legacy column
                EXECUTE format('ALTER TABLE %I DROP COLUMN created_at', tbl);
            ELSE
                -- Only old column exists (validate/none environment): rename it
                EXECUTE format('ALTER TABLE %I RENAME COLUMN created_at TO created_date', tbl);
            END IF;
        END IF;

        -- updated_at / last_modified_date
        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = tbl AND column_name = 'updated_at') THEN
            IF EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name = tbl AND column_name = 'last_modified_date') THEN
                EXECUTE format('ALTER TABLE %I DROP COLUMN updated_at', tbl);
            ELSE
                EXECUTE format('ALTER TABLE %I RENAME COLUMN updated_at TO last_modified_date', tbl);
            END IF;
        END IF;

    END LOOP;
END $$;
