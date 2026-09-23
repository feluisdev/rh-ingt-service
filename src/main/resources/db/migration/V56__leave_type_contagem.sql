-- =============================================================
-- V56 — como se contam os dias de uma ausência
--
-- DL n.º 3/2010, art. 76.º: «Os dias de descanso semanal ou complementar e os
-- feriados, quando intercalados no decurso de uma licença ou de uma sucessão de
-- faltas da mesma natureza, integram-se no cômputo dos respectivos períodos de
-- duração, salvo se a lei se referir expressamente a dias úteis.»
--
-- A regra é DIAS SEGUIDOS; dias úteis é a excepção, e tem de estar escrita. As
-- férias estão (art. 2.º n.º 3: 22 dias úteis), e os dois direitos do
-- trabalhador-estudante também (art. 77.º n.os 2 e 3). A maioria das faltas do
-- art. 15.º não: «até 8 por falecimento», «até 3 consecutivas por doença», e o
-- art. 21.º diz dos seminários «5 dias consecutivos».
--
-- Até aqui tudo se contava em dias úteis, o que dava mais dias do que a lei: um
-- luto de 8 que começasse a uma sexta estendia-se por 10 a 12 dias de calendário.
--
-- O modo de contagem é da LEI, disposição a disposição; a instituição diz a que
-- disposição corresponde cada uma das suas linhas — como no regime (V49).
--
-- A migração NÃO reclassifica nada: as linhas existentes ficam DIAS_UTEIS, que é
-- como sempre se contaram. Mudar a contagem muda o número de dias de pedidos
-- futuros, e isso não se decide em silêncio. O seed traz as linhas classificadas;
-- o repor_estado.sql alinha a base de desenvolvimento.
--
-- Defensivo e idempotente, com o guarda to_regclass ANINHADO.
-- =============================================================

-- 1. A coluna, sem DEFAULT (ver V49: o DEFAULT entra só no fim)
DO $$ BEGIN
    IF to_regclass('public.t_leave_type') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_type'
                         AND column_name='contagem') THEN
            ALTER TABLE t_leave_type ADD COLUMN contagem VARCHAR(20);
        END IF;
    END IF;
END $$;

DO $$ BEGIN
    IF to_regclass('audit_schema.t_leave_type_aud') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_type_aud'
                         AND column_name='contagem') THEN
            ALTER TABLE audit_schema.t_leave_type_aud ADD COLUMN contagem VARCHAR(20);
        END IF;
    END IF;
END $$;

-- 2. O que já existe conta-se como sempre se contou
DO $$ BEGIN
    IF to_regclass('public.t_leave_type') IS NOT NULL THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='t_leave_type'
                     AND column_name='contagem') THEN
            UPDATE t_leave_type SET contagem = 'DIAS_UTEIS' WHERE contagem IS NULL;
        END IF;
    END IF;
END $$;

-- 3. Invariantes: os dois modos da lei, e obrigatório se nada estiver por preencher
DO $$ BEGIN
    IF to_regclass('public.t_leave_type') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                       WHERE table_schema='public' AND table_name='t_leave_type'
                         AND constraint_name='ck_leave_type_contagem') THEN
            IF NOT EXISTS (SELECT 1 FROM t_leave_type
                           WHERE contagem IS NOT NULL
                             AND contagem NOT IN ('DIAS_UTEIS', 'DIAS_SEGUIDOS')) THEN
                ALTER TABLE t_leave_type ADD CONSTRAINT ck_leave_type_contagem
                    CHECK (contagem IN ('DIAS_UTEIS', 'DIAS_SEGUIDOS'));
            END IF;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM t_leave_type WHERE contagem IS NULL) THEN
            IF EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_type'
                         AND column_name='contagem' AND is_nullable='YES') THEN
                ALTER TABLE t_leave_type ALTER COLUMN contagem SET NOT NULL;
            END IF;
        END IF;
    END IF;
END $$;

-- 4. Só agora o valor por omissão, para um INSERT que omita a coluna continuar a
--    funcionar: o seed de raiz, um script antigo, um cliente que não a conheça.
DO $$ BEGIN
    IF to_regclass('public.t_leave_type') IS NOT NULL THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='t_leave_type'
                     AND column_name='contagem' AND column_default IS NULL) THEN
            ALTER TABLE t_leave_type ALTER COLUMN contagem SET DEFAULT 'DIAS_UTEIS';
        END IF;
    END IF;
END $$;
