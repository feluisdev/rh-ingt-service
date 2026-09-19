-- =============================================================
-- V46 — a substituição ligada ao titular que cobre
--
-- A V45 abriu espaço para uma segunda afectação corrente no mesmo Lugar. Falta
-- dizer **quem** se substitui: sem essa ligação não se sabe quando a
-- substituição termina, e a lei é clara — o contrato de substituição caduca
-- quando cessa a situação que a justificou (art. 77.º n.º 2).
--
-- titular_assignment_id aponta para a afectação PRINCIPAL do titular impedido.
-- Não se reaproveitou o origin_assignment_id: esse foi feito para a mobilidade
-- restaurar a afectação de origem e hoje não é lido por ninguém (a mobilidade
-- transitória deixou de mexer na afectação). Guardar dois sentidos na mesma
-- coluna era ganhar uma coluna e perder a leitura.
--
-- O índice único parcial garante que um titular impedido tem, no máximo, um
-- substituto de cada vez.
--
-- Defensivo em todos os passos, no molde da V40 a V45.
-- =============================================================

DO $$ BEGIN
    IF to_regclass('public.t_assignment') IS NOT NULL THEN

        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_assignment'
                         AND column_name='titular_assignment_id') THEN
            ALTER TABLE t_assignment ADD COLUMN titular_assignment_id UUID;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                       WHERE table_schema='public' AND table_name='t_assignment'
                         AND constraint_name='fk_assignment_titular') THEN
            ALTER TABLE t_assignment
                ADD CONSTRAINT fk_assignment_titular
                FOREIGN KEY (titular_assignment_id) REFERENCES t_assignment (id);
        END IF;

        -- Um substituto de cada vez por titular impedido.
        CREATE UNIQUE INDEX IF NOT EXISTS ux_assignment_substituto_corrente
            ON t_assignment (titular_assignment_id)
            WHERE is_current = TRUE AND assignment_type = 'SUBSTITUICAO';

        -- Encerrar as substituições de um titular é uma consulta por esta coluna.
        CREATE INDEX IF NOT EXISTS ix_assignment_titular
            ON t_assignment (titular_assignment_id)
            WHERE titular_assignment_id IS NOT NULL;
    END IF;
END $$;

-- Auditoria Envers: a coluna nova tem de existir na tabela sombra.
DO $$ BEGIN
    IF to_regclass('audit_schema.t_assignment_aud') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_assignment_aud'
                         AND column_name='titular_assignment_id') THEN
        ALTER TABLE audit_schema.t_assignment_aud ADD COLUMN titular_assignment_id UUID;
    END IF;
END $$;
