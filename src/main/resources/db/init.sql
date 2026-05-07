CREATE SCHEMA IF NOT EXISTS audit_schema;

DO $$ BEGIN
  DELETE FROM flyway_schema_history;
EXCEPTION WHEN undefined_table THEN NULL;
END $$;
