-- =============================================================
-- V44 — t_contrato.status sem buracos
--
-- Um contrato sem estado não é suspenso nem reactivado por ninguém: as duas
-- operações comparam o estado actual e, sendo nulo, não fazem nada — em
-- silêncio. Foi o que se viu ao exercitar a API contra a base de dados: um
-- colaborador passava a inactividade fora do quadro, a afectação era encerrada,
-- mas o contrato ficava como estava.
--
-- O estado é parte da identidade do contrato, não um extra: em execução,
-- suspenso ou terminado. Fica com valor por omissão e sem nulos.
--
-- Defensivo em todos os passos, no molde da V40 a V43.
-- =============================================================

DO $$ BEGIN
    IF to_regclass('public.t_contrato') IS NOT NULL
       AND EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='t_contrato'
                     AND column_name='status') THEN

        -- Contrato corrente sem estado estava em execução; os restantes já tinham
        -- terminado (é o que significa deixar de ser corrente).
        UPDATE t_contrato SET status = 'ATIVO'
         WHERE status IS NULL AND COALESCE(is_current, FALSE) = TRUE;

        UPDATE t_contrato SET status = 'CESSADO'
         WHERE status IS NULL;

        ALTER TABLE t_contrato ALTER COLUMN status SET DEFAULT 'ATIVO';
        ALTER TABLE t_contrato ALTER COLUMN status SET NOT NULL;
    END IF;
END $$;
