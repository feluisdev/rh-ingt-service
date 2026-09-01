
ALTER TABLE t_key_results DROP COLUMN IF EXISTS criteria_superado;
ALTER TABLE t_key_results DROP COLUMN IF EXISTS criteria_seguranca;
ALTER TABLE t_key_results DROP COLUMN IF EXISTS criteria_alcancado;
ALTER TABLE t_key_results DROP COLUMN IF EXISTS criteria_insuficiente;
ALTER TABLE t_key_results DROP COLUMN IF EXISTS criteria_seguranca_min;
ALTER TABLE t_key_results DROP COLUMN IF EXISTS criteria_seguranca_max;
ALTER TABLE t_key_results DROP COLUMN IF EXISTS criteria_alcancado_min;
ALTER TABLE t_key_results DROP COLUMN IF EXISTS criteria_alcancado_max;

ALTER TABLE t_key_results ADD COLUMN IF NOT EXISTS criteria_superado NUMERIC(19, 2);
ALTER TABLE t_key_results ADD COLUMN IF NOT EXISTS criteria_seguranca_min NUMERIC(19, 2);
ALTER TABLE t_key_results ADD COLUMN IF NOT EXISTS criteria_seguranca_max NUMERIC(19, 2);
ALTER TABLE t_key_results ADD COLUMN IF NOT EXISTS criteria_alcancado_min NUMERIC(19, 2);
ALTER TABLE t_key_results ADD COLUMN IF NOT EXISTS criteria_alcancado_max NUMERIC(19, 2);
ALTER TABLE t_key_results ADD COLUMN IF NOT EXISTS criteria_insuficiente NUMERIC(19, 2);

ALTER TABLE audit_schema.t_key_results_aud DROP COLUMN IF EXISTS criteria_superado;
ALTER TABLE audit_schema.t_key_results_aud DROP COLUMN IF EXISTS criteria_seguranca;
ALTER TABLE audit_schema.t_key_results_aud DROP COLUMN IF EXISTS criteria_alcancado;
ALTER TABLE audit_schema.t_key_results_aud DROP COLUMN IF EXISTS criteria_insuficiente;
ALTER TABLE audit_schema.t_key_results_aud DROP COLUMN IF EXISTS criteria_seguranca_min;
ALTER TABLE audit_schema.t_key_results_aud DROP COLUMN IF EXISTS criteria_seguranca_max;
ALTER TABLE audit_schema.t_key_results_aud DROP COLUMN IF EXISTS criteria_alcancado_min;
ALTER TABLE audit_schema.t_key_results_aud DROP COLUMN IF EXISTS criteria_alcancado_max;

ALTER TABLE audit_schema.t_key_results_aud ADD COLUMN IF NOT EXISTS criteria_superado NUMERIC(19, 2);
ALTER TABLE audit_schema.t_key_results_aud ADD COLUMN IF NOT EXISTS criteria_seguranca_min NUMERIC(19, 2);
ALTER TABLE audit_schema.t_key_results_aud ADD COLUMN IF NOT EXISTS criteria_seguranca_max NUMERIC(19, 2);
ALTER TABLE audit_schema.t_key_results_aud ADD COLUMN IF NOT EXISTS criteria_alcancado_min NUMERIC(19, 2);
ALTER TABLE audit_schema.t_key_results_aud ADD COLUMN IF NOT EXISTS criteria_alcancado_max NUMERIC(19, 2);
ALTER TABLE audit_schema.t_key_results_aud ADD COLUMN IF NOT EXISTS criteria_insuficiente NUMERIC(19, 2);