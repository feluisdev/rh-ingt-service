-- =============================================================
-- V39 — seq_numero_funcionario (reposição)
-- -------------------------------------------------------------
-- A sequência era criada pela antiga V24__create_colaboradores_tables.sql,
-- removida no reset de migrações (commit 03867b3d). A V8 recriou t_funcionario
-- mas não a sequência, pelo que FuncionarioService.criarFuncionario falha com
-- "relation seq_numero_funcionario does not exist".
--
-- Idempotente e defensiva:
--   * CREATE SEQUENCE IF NOT EXISTS — no-op onde a sequência já existe;
--   * alinha a sequência com o maior numero_funcionario existente (formato
--     F%06d) para não colidir com a restrição UNIQUE;
--   * nunca recua a sequência se ela já estiver mais à frente.
-- =============================================================

CREATE SEQUENCE IF NOT EXISTS seq_numero_funcionario
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    CACHE 1;

DO $$
DECLARE
    v_max  BIGINT;
    v_last BIGINT;
BEGIN
    SELECT COALESCE(MAX(substring(numero_funcionario FROM 2)::BIGINT), 0)
      INTO v_max
      FROM t_funcionario
     WHERE numero_funcionario ~ '^F[0-9]+$';

    SELECT COALESCE(last_value, 0)
      INTO v_last
      FROM pg_sequences
     WHERE schemaname = current_schema()
       AND sequencename = 'seq_numero_funcionario';

    IF GREATEST(v_max, COALESCE(v_last, 0)) > 0 THEN
        PERFORM setval('seq_numero_funcionario', GREATEST(v_max, COALESCE(v_last, 0)), true);
    END IF;
END $$;
