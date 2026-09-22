-- =============================================================
-- V51 — suspensão de férias
--
-- O art. 8.º do DL n.º 3/2010 manda suspender as férias por maternidade,
-- paternidade ou adopção (n.º 1), por doença e assistência inadiável a
-- familiares doentes (n.º 2) e, por razões imperiosas de serviço, por despacho
-- fundamentado do dirigente (n.º 5). O n.º 3 diz QUANDO: «as férias são
-- suspensas a partir da data da entrada no serviço do documento comprovativo».
--
-- Hoje um pedido de férias e um de doença não se falam: quem adoecesse a meio
-- das férias perdia-as, porque os dias já tinham sido contados como gozados.
--
-- DUAS COISAS QUE ESTAS COLUNAS GUARDAM:
--
--  * A DATA em que a suspensão produz efeito -- é a partir dela que os dias
--    deixam de ser férias, e é dela que sai quantos voltam ao saldo.
--
--  * O MOTIVO. A lei não suspende férias por qualquer razão: são as do art. 8.º.
--    O texto é da instituição e não se valida, mas tem de existir -- tal como na
--    acumulação (art. 7.º n.º 1).
--
-- O que NÃO precisa de coluna nenhuma: para onde vão os dias recuperados. Voltam
-- ao saldo do próprio ano, e passá-los ao ano seguinte é a acumulação da V50 --
-- que é exactamente o que o art. 9.º n.º 1, remetendo para o art. 8.º n.º 4,
-- autoriza ao mandar gozá-los «até ao termo do ano civil imediato».
--
-- Defensivo e idempotente em todos os passos, no molde da V40 a V50.
-- =============================================================

DO $$ BEGIN
    IF to_regclass('public.t_leave_request') IS NOT NULL THEN

        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_request'
                         AND column_name='suspenso_em') THEN
            ALTER TABLE t_leave_request ADD COLUMN suspenso_em DATE;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_request'
                         AND column_name='suspensao_motivo') THEN
            ALTER TABLE t_leave_request ADD COLUMN suspensao_motivo TEXT;
        END IF;
    END IF;
END $$;

-- Auditoria Envers: as colunas novas têm de existir na tabela sombra.
DO $$ BEGIN
    IF to_regclass('audit_schema.t_leave_request_aud') IS NOT NULL THEN

        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_request_aud'
                         AND column_name='suspenso_em') THEN
            ALTER TABLE audit_schema.t_leave_request_aud ADD COLUMN suspenso_em DATE;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_request_aud'
                         AND column_name='suspensao_motivo') THEN
            ALTER TABLE audit_schema.t_leave_request_aud ADD COLUMN suspensao_motivo TEXT;
        END IF;
    END IF;
END $$;

-- -------------------------------------------------------------
-- O período não pode acabar antes de começar. A suspensão encurta a data de fim,
-- e é a mesma armadilha que a V48 fechou na licença: suspender no primeiro dia
-- deixaria o fim na véspera do início. Aqui o domínio recusa-o, e a restrição
-- garante-o também no esquema.
--
-- Só entra se nada a violar -- uma migração não deve rebentar por causa de dados
-- que já lá estavam.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_request') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                       WHERE table_schema='public' AND table_name='t_leave_request'
                         AND constraint_name='ck_leave_request_periodo')
       AND NOT EXISTS (SELECT 1 FROM t_leave_request
                       WHERE data_fim IS NOT NULL AND data_inicio IS NOT NULL
                         AND data_fim < data_inicio) THEN
        ALTER TABLE t_leave_request
            ADD CONSTRAINT ck_leave_request_periodo CHECK (data_fim IS NULL OR data_inicio IS NULL OR data_fim >= data_inicio);
    END IF;
END $$;
