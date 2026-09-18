-- =============================================================
-- V43 — efeito da licença no Lugar e no regresso
--
-- Até aqui nenhuma licença tocava no Lugar. A lei distingue:
--   • licença sem vencimento até 90 dias (DL 3/2010, art. 46.º) e até 3 anos
--     (art. 48.º): MANTÉM o lugar;
--   • licença de longa duração (art. 50.º a 53.º): ABRE VAGA e suspende o
--     vínculo; no regresso, o funcionário tem direito a uma vaga existente ou
--     à primeira que ocorra — é a DISPONIBILIDADE (Lei 20/X/2023, art. 122.º);
--   • acompanhamento de cônjuge no estrangeiro: abre vaga além de um ano
--     (art. 56.º n.º 2, e Lei 20/X/2023 art. 118.º n.º 2);
--   • organismo internacional, como funcionário do organismo: abre vaga (art. 62.º);
--   • formação: abre vaga além de seis meses (art. 67.º n.º 3, e art. 118.º n.º 2).
--
-- Três colunas no subtipo, porque o que varia é o motivo e o prazo, não a regra:
--   position_effect     MANTEM | ABRE_VAGA
--   vacancy_after_days  abre vaga só se a duração exceder N dias (NULL = logo)
--   return_effect       REGRESSA_LUGAR | DISPONIBILIDADE
--
-- Aproveita-se para fechar a ambiguidade do record_type: AMBOS desaparece.
-- Licença e mobilidade têm efeitos opostos no vínculo (Lei 20/X/2023, art. 171.º
-- n.º 2: a licença suspende-o; na mobilidade o funcionário continua a exercer
-- funções), logo um registo não pode ser as duas coisas.
--
-- Defensivo em todos os passos, no molde da V40/V41.
-- =============================================================

DO $$ BEGIN
    IF to_regclass('public.t_leave_mobility_subtype') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_mobility_subtype'
                         AND column_name='position_effect') THEN
            ALTER TABLE t_leave_mobility_subtype ADD COLUMN position_effect VARCHAR(20);
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_mobility_subtype'
                         AND column_name='vacancy_after_days') THEN
            ALTER TABLE t_leave_mobility_subtype ADD COLUMN vacancy_after_days INTEGER;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_mobility_subtype'
                         AND column_name='return_effect') THEN
            ALTER TABLE t_leave_mobility_subtype ADD COLUMN return_effect VARCHAR(20);
        END IF;
    END IF;
END $$;

DO $$ BEGIN
    IF to_regclass('audit_schema.t_leave_mobility_subtype_aud') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_mobility_subtype_aud'
                         AND column_name='position_effect') THEN
            ALTER TABLE audit_schema.t_leave_mobility_subtype_aud ADD COLUMN position_effect VARCHAR(20);
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_mobility_subtype_aud'
                         AND column_name='vacancy_after_days') THEN
            ALTER TABLE audit_schema.t_leave_mobility_subtype_aud ADD COLUMN vacancy_after_days INTEGER;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_mobility_subtype_aud'
                         AND column_name='return_effect') THEN
            ALTER TABLE audit_schema.t_leave_mobility_subtype_aud ADD COLUMN return_effect VARCHAR(20);
        END IF;
    END IF;
END $$;

-- Por omissão nada muda: mantém o Lugar e regressa a ele. Só os subtipos que a
-- lei manda abrir vaga é que são marcados, e só quando existem no catálogo.
DO $$ BEGIN
    IF to_regclass('public.t_leave_mobility_subtype') IS NOT NULL
       AND EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='t_leave_mobility_subtype'
                     AND column_name='position_effect') THEN

        UPDATE t_leave_mobility_subtype
           SET position_effect = 'MANTEM', return_effect = 'REGRESSA_LUGAR'
         WHERE position_effect IS NULL;

        -- Licença de longa duração (DL 3/2010, art. 50.º a 53.º): abre vaga e o
        -- regresso faz-se pela disponibilidade, à espera de vaga.
        UPDATE t_leave_mobility_subtype
           SET position_effect = 'ABRE_VAGA', return_effect = 'DISPONIBILIDADE'
         WHERE code IN ('LIC_LONGA_DURACAO', 'LIC_SEM_VENC_LONGA');

        -- Acompanhamento de cônjuge no estrangeiro: abre vaga além de um ano.
        UPDATE t_leave_mobility_subtype
           SET position_effect = 'ABRE_VAGA', vacancy_after_days = 365,
               return_effect = 'DISPONIBILIDADE'
         WHERE code IN ('LIC_ACOMP_CONJUGE');

        -- Formação no exterior: abre vaga além de seis meses.
        UPDATE t_leave_mobility_subtype
           SET position_effect = 'ABRE_VAGA', vacancy_after_days = 180,
               return_effect = 'DISPONIBILIDADE'
         WHERE code IN ('LIC_FORMACAO');
    END IF;
END $$;

-- record_type: AMBOS desaparece. Os registos que o usavam eram tratados como
-- mobilidade pelo código, por isso é isso que passam a dizer.
DO $$ BEGIN
    IF to_regclass('public.t_leave_mobility_subtype') IS NOT NULL THEN
        UPDATE t_leave_mobility_subtype SET record_type = 'MOBILIDADE' WHERE record_type = 'AMBOS';

        -- A licença parental é uma ausência, não uma licença: não suspende o
        -- vínculo (Lei 20/X/2023, art. 171.º n.º 2 vs art. 172.º). Vive em
        -- t_leave_type (MATERNIDADE, PATERNIDADE). O subtipo é desactivado em vez
        -- de apagado, para não partir registos históricos que lhe apontem.
        UPDATE t_leave_mobility_subtype SET is_active = FALSE WHERE code = 'LIC_PARENTAL';
    END IF;
END $$;
