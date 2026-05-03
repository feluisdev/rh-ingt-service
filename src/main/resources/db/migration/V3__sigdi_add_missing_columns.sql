-- Colunas adicionadas às entidades sigdi após criação inicial das tabelas.
-- Totalmente defensivo: ADD COLUMN IF NOT EXISTS é idempotente.

ALTER TABLE t_key_results
    ADD COLUMN IF NOT EXISTS criteria_superado    VARCHAR(255),
    ADD COLUMN IF NOT EXISTS criteria_seguranca   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS criteria_alcancado   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS criteria_insuficiente VARCHAR(255);
