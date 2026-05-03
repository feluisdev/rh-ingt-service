-- Fix audit column names to match Hibernate AuditEntity convention
-- Affected tables had created_at/updated_at instead of created_date/last_modified_date
-- Without this fix, ddl-auto=validate fails at startup in staging/production

ALTER TABLE t_option_entity RENAME COLUMN created_at TO created_date;
ALTER TABLE t_option_entity RENAME COLUMN updated_at TO last_modified_date;

ALTER TABLE t_unidade_organica RENAME COLUMN created_at TO created_date;
ALTER TABLE t_unidade_organica RENAME COLUMN updated_at TO last_modified_date;

ALTER TABLE t_funcao RENAME COLUMN created_at TO created_date;
ALTER TABLE t_funcao RENAME COLUMN updated_at TO last_modified_date;
