-- =============================================================
-- V61 — processo disciplinar com tramitação (Estatuto Disciplinar)
-- -------------------------------------------------------------
-- t_disciplinary_process passa a guardar a tramitação ao lado do registo
-- antigo: espécie, fase, data da infracção, pena prevista, instrutor, pena
-- aplicada e duração, e quando os efeitos foram aplicados. Todas as colunas
-- são nulas: um processo antigo, sem fase, continua a ser só um registo.
-- Os actos vão para t_processo_disciplinar_acto, tabela nova criada pelo
-- ddl-auto (sem migração).
--
-- Idempotente e defensiva:
--   * a tabela (e a sua _aud do Envers) pode não existir ainda — nasce pelo
--     ddl-auto, depois do Flyway, e aí já com as colunas; só se altera o que
--     existe;
--   * ADD COLUMN IF NOT EXISTS — correr outra vez não muda nada.
-- =============================================================

DO $$
DECLARE
    v_tabela TEXT;
BEGIN
    FOREACH v_tabela IN ARRAY ARRAY['public.t_disciplinary_process', 'audit_schema.t_disciplinary_process_aud'] LOOP
        IF to_regclass(v_tabela) IS NOT NULL THEN
            EXECUTE format('ALTER TABLE %s ADD COLUMN IF NOT EXISTS especie VARCHAR(30)', v_tabela);
            EXECUTE format('ALTER TABLE %s ADD COLUMN IF NOT EXISTS fase VARCHAR(20)', v_tabela);
            EXECUTE format('ALTER TABLE %s ADD COLUMN IF NOT EXISTS data_infraccao DATE', v_tabela);
            EXECUTE format('ALTER TABLE %s ADD COLUMN IF NOT EXISTS pena_prevista VARCHAR(30)', v_tabela);
            EXECUTE format('ALTER TABLE %s ADD COLUMN IF NOT EXISTS instrutor_id UUID', v_tabela);
            EXECUTE format('ALTER TABLE %s ADD COLUMN IF NOT EXISTS instrutor_nome VARCHAR(200)', v_tabela);
            EXECUTE format('ALTER TABLE %s ADD COLUMN IF NOT EXISTS pena VARCHAR(30)', v_tabela);
            EXECUTE format('ALTER TABLE %s ADD COLUMN IF NOT EXISTS pena_duracao INTEGER', v_tabela);
            EXECUTE format('ALTER TABLE %s ADD COLUMN IF NOT EXISTS efeitos_aplicados_em TIMESTAMP(6)', v_tabela);
        END IF;
    END LOOP;

    IF to_regclass('public.t_disciplinary_process') IS NOT NULL THEN
        CREATE INDEX IF NOT EXISTS ix_disciplinary_process_fase ON public.t_disciplinary_process (fase);
    END IF;
END $$;
