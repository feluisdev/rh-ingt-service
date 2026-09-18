-- =============================================================
-- V42 — t_worker_state.situacao_funcional
-- Alinha o catálogo de estados com as seis situações administrativas
-- da Lei n.º 20/X/2023, art. 117.º:
--   ACTIVIDADE_NO_QUADRO       (art. 118.º)
--   ACTIVIDADE_FORA_QUADRO     (art. 119.º)
--   INACTIVIDADE_NO_QUADRO     (art. 120.º) — não conta para antiguidade (n.º 2)
--   INACTIVIDADE_FORA_QUADRO   (art. 121.º) — abre vaga (n.º 2)
--   DISPONIBILIDADE            (art. 122.º) — aguarda vaga, com abonos
--   APOSENTACAO                (art. 117.º al. f; cessa o vínculo, art. 93.º al. b)
--
-- A situação é o único dado guardado: os efeitos (abrir vaga, suspender o
-- contrato, contar antiguidade) são derivados dela, porque a lei é que os fixa.
-- NULL = estado fora do quadro das seis situações, tipicamente um estado de
-- cessação como INACTIVE (exoneração, caducidade, mútuo acordo…). Estados com
-- situação nula não produzem efeitos automáticos, como já hoje acontece.
--
-- Defensivo em todos os passos, no molde da V40.
-- =============================================================

DO $$ BEGIN
    IF to_regclass('public.t_worker_state') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_worker_state'
                         AND column_name='situacao_funcional') THEN
        ALTER TABLE t_worker_state ADD COLUMN situacao_funcional VARCHAR(40);
    END IF;
END $$;

DO $$ BEGIN
    IF to_regclass('audit_schema.t_worker_state_aud') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_worker_state_aud'
                         AND column_name='situacao_funcional') THEN
        ALTER TABLE audit_schema.t_worker_state_aud ADD COLUMN situacao_funcional VARCHAR(40);
    END IF;
END $$;

-- Retrocompatibilidade: os quatro estados de origem passam a dizer, pelos dados,
-- em que situação da lei se encontram. O SUSPENDED corresponde à suspensão
-- disciplinar de exercício e vencimento (art. 120.º n.º 1 al. c). O INACTIVE é
-- cessação e fica sem situação. Estados criados pelo utilizador ficam a NULL,
-- para o RH classificar.
DO $$ BEGIN
    IF to_regclass('public.t_worker_state') IS NOT NULL
       AND EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='t_worker_state'
                     AND column_name='situacao_funcional') THEN

        UPDATE t_worker_state SET situacao_funcional = 'ACTIVIDADE_NO_QUADRO'
         WHERE code = 'ACTIVE' AND situacao_funcional IS NULL;

        UPDATE t_worker_state SET situacao_funcional = 'INACTIVIDADE_NO_QUADRO'
         WHERE code = 'SUSPENDED' AND situacao_funcional IS NULL;

        UPDATE t_worker_state SET situacao_funcional = 'APOSENTACAO'
         WHERE code = 'RETIRED' AND situacao_funcional IS NULL;
    END IF;
END $$;
