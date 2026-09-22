-- =============================================================
-- V52 — a acumulação é uma forma de prestar a mobilidade
--
-- O art. 134.º n.º 2 da Lei n.º 20/X/2023 classifica a mobilidade geral
-- QUANTO À FORMA DE PRESTAÇÃO:
--   a) a tempo inteiro, "em regime de exclusividade";
--   b) em regime de acumulação, "quando o funcionário passa a exercer funções
--      noutro serviço, em acumulação com as do serviço de origem".
--
-- Até aqui isto não existia no modelo, e o que existia estava errado: havia um
-- TipoAfectacao.ACUMULACAO que citava este mesmo artigo para dizer "exercício
-- cumulativo de outro Lugar". O artigo não diz isso — e como a mobilidade
-- transitória é "sem ocupação do lugar do quadro" (art. 135.º n.º 7), uma
-- mobilidade em acumulação NÃO CRIA AFECTAÇÃO NENHUMA. Vive aqui.
--
-- Não confundir com o art. 21.º (acumulação de funções públicas), que é outro
-- instituto: regime de permissão, com incompatibilidade, manifesto interesse
-- público e, em regra, não remunerada. Fica fora deste âmbito.
--
-- TEMPO_INTEIRO por omissão porque a exclusividade é a regra (art. 20.º), e
-- porque é o que todas as linhas existentes são: nenhuma foi criada em
-- acumulação, que até hoje não tinha como ser expressa. Aqui o DEFAULT na
-- própria criação da coluna é o que se quer — ao contrário da V49, onde
-- preencher tudo com a omissão escondeu uma classificação por fazer.
--
-- Defensivo no molde da V40 a V51.
-- =============================================================

DO $$ BEGIN
    IF to_regclass('public.t_leave_mobility') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_mobility'
                         AND column_name='forma_prestacao') THEN
        ALTER TABLE t_leave_mobility
            ADD COLUMN forma_prestacao VARCHAR(20) NOT NULL DEFAULT 'TEMPO_INTEIRO';
    END IF;
END $$;

-- O conjunto é fechado pela lei, não pela instituição: vale uma restrição.
DO $$ BEGIN
    IF to_regclass('public.t_leave_mobility') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM pg_constraint
                       WHERE conname='ck_leave_mobility_forma_prestacao') THEN
        ALTER TABLE t_leave_mobility
            ADD CONSTRAINT ck_leave_mobility_forma_prestacao
            CHECK (forma_prestacao IN ('TEMPO_INTEIRO', 'ACUMULACAO'));
    END IF;
END $$;

-- Auditoria (Envers): a coluna nunca leva NOT NULL nem DEFAULT no _aud, porque
-- as revisões antigas não a têm e uma revisão de DELETE grava tudo a nulo.
DO $$ BEGIN
    IF to_regclass('audit_schema.t_leave_mobility_aud') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_mobility_aud'
                         AND column_name='forma_prestacao') THEN
        ALTER TABLE audit_schema.t_leave_mobility_aud ADD COLUMN forma_prestacao VARCHAR(20);
    END IF;
END $$;
