-- Fix audit column names to match Hibernate AuditEntity convention.
--
-- Affected tables (V2 and V20 migrations used legacy names):
--   t_option_entity, t_unidade_organica, t_funcao
--
-- Legacy names  →  AuditEntity names
--   created_at        →  created_date
--   updated_at        →  last_modified_date
--   updated_by        →  last_modified_by   ← also required by entity
--
-- Two possible states depending on prior ddl-auto setting:
--   validate/none:  only old column exists       → rename it
--   update:         Hibernate added new column   → drop the old one
--
-- Idempotent: safe to run on any environment.

DO $$
DECLARE
    tbl TEXT;
BEGIN
    FOREACH tbl IN ARRAY ARRAY['t_option_entity', 't_unidade_organica', 't_funcao'] LOOP

        -- created_at → created_date
        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = tbl AND column_name = 'created_at') THEN
            IF EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name = tbl AND column_name = 'created_date') THEN
                EXECUTE format('ALTER TABLE %I DROP COLUMN created_at', tbl);
            ELSE
                EXECUTE format('ALTER TABLE %I RENAME COLUMN created_at TO created_date', tbl);
            END IF;
        END IF;

        -- updated_at → last_modified_date
        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = tbl AND column_name = 'updated_at') THEN
            IF EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name = tbl AND column_name = 'last_modified_date') THEN
                EXECUTE format('ALTER TABLE %I DROP COLUMN updated_at', tbl);
            ELSE
                EXECUTE format('ALTER TABLE %I RENAME COLUMN updated_at TO last_modified_date', tbl);
            END IF;
        END IF;

        -- updated_by → last_modified_by
        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = tbl AND column_name = 'updated_by') THEN
            IF EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_name = tbl AND column_name = 'last_modified_by') THEN
                EXECUTE format('ALTER TABLE %I DROP COLUMN updated_by', tbl);
            ELSE
                EXECUTE format('ALTER TABLE %I RENAME COLUMN updated_by TO last_modified_by', tbl);
            END IF;
        END IF;

    END LOOP;
END $$;
