ALTER TABLE t_key_results DROP COLUMN IF EXISTS criteria_superado;
ALTER TABLE t_key_results DROP COLUMN IF EXISTS criteria_seguranca;
ALTER TABLE t_key_results DROP COLUMN IF EXISTS criteria_alcancado;
ALTER TABLE t_key_results DROP COLUMN IF EXISTS criteria_insuficiente;

ALTER TABLE t_key_results ADD COLUMN criteria_superado NUMERIC(19, 2);
ALTER TABLE t_key_results ADD COLUMN criteria_seguranca_min NUMERIC(19, 2);
ALTER TABLE t_key_results ADD COLUMN criteria_seguranca_max NUMERIC(19, 2);
ALTER TABLE t_key_results ADD COLUMN criteria_alcancado_min NUMERIC(19, 2);
ALTER TABLE t_key_results ADD COLUMN criteria_alcancado_max NUMERIC(19, 2);
ALTER TABLE t_key_results ADD COLUMN criteria_insuficiente NUMERIC(19, 2);
