-- =============================================================
-- V45 — uma cadeira, um TITULAR (e não um ocupante)
--
-- O índice ux_assignment_position_current proíbe duas afectações correntes no
-- mesmo Lugar, seja qual for o título com que lá estão. Isso impede o que a lei
-- manda existir: o contrato a termo para substituir funcionário temporariamente
-- impedido (Lei n.º 20/X/2023, art. 73.º al. a) a c)) e a nomeação em
-- substituição (art. 91.º n.º 1 al. a)). Quem substitui não desaloja o titular
-- — o titular está impedido, não saiu.
--
-- O índice passa a ser parcial em assignment_type = 'PRINCIPAL', ficando a par
-- do ux_assignment_funcionario_current_principal, que já era parcial desde a
-- V37. O que continua garantido é o essencial: um Lugar não tem dois titulares.
--
-- O provimento do Lugar (provido/vago) continua a ser derivado, e passa a
-- derivar do titular: um Lugar com substituto e sem titular está vago, que é
-- exactamente o que a substituição pressupõe para caducar (art. 77.º n.º 2).
--
-- Defensivo em todos os passos, no molde da V40 a V44.
-- =============================================================

DO $$ BEGIN
    IF to_regclass('public.t_assignment') IS NOT NULL
       AND EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='t_assignment'
                     AND column_name='assignment_type') THEN

        -- A coluna admite nulo em bases antigas; um tipo em falta é o caso comum,
        -- o titular. Sem isto, o índice parcial deixaria essas linhas de fora e o
        -- Lugar aceitaria um segundo titular.
        UPDATE t_assignment SET assignment_type = 'PRINCIPAL'
         WHERE assignment_type IS NULL OR btrim(assignment_type) = '';

        ALTER TABLE t_assignment ALTER COLUMN assignment_type SET DEFAULT 'PRINCIPAL';
        ALTER TABLE t_assignment ALTER COLUMN assignment_type SET NOT NULL;

        DROP INDEX IF EXISTS ux_assignment_position_current;

        CREATE UNIQUE INDEX IF NOT EXISTS ux_assignment_position_titular
            ON t_assignment (position_id)
            WHERE is_current = TRUE AND assignment_type = 'PRINCIPAL';

        -- As consultas por Lugar passam todas a filtrar também pelo tipo.
        CREATE INDEX IF NOT EXISTS ix_assignment_position_current_type
            ON t_assignment (position_id, assignment_type)
            WHERE is_current = TRUE;
    END IF;
END $$;
