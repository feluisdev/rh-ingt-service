-- =============================================================
-- V50 — acumulação de férias para o ano seguinte
--
-- O art. 7.º n.º 1 do DL n.º 3/2010 diz: «As férias devem ser gozadas no decurso
-- do ano civil em que se vencem, salvo se, por motivo de serviço, não puderem
-- ser gozadas nesse ano, caso em que pode haver acumulação de férias para o ano
-- seguinte.»
--
-- Depois da V49 o saldo do ano novo passou a nascer sozinho a 1 de Janeiro com o
-- direito inteiro — e os dias não gozados do ano anterior ficavam na linha
-- antiga, inalcançáveis: um pedido de 2027 só olha para o saldo de 2027. Os dias
-- não se perdiam da base, perdiam-se na prática, e o art. 2.º n.º 5 diz que o
-- direito é irrenunciável.
--
-- TRÊS COISAS QUE A LEI IMPÕE E QUE ESTAS COLUNAS GUARDAM:
--
--  * A acumulação NÃO é automática. Depende de «motivo de serviço», ou seja, de
--    um acto justificado — por isso há um motivo gravado, e não um job a
--    arrastar tudo em silêncio. O motivo é texto livre: é a instituição que o
--    escreve, e o projecto não valida motivos.
--
--  * É «para o ano SEGUINTE», uma só vez. `dias_transportados` na linha de
--    origem impede transportar duas vezes os mesmos dias, e os dias recebidos
--    (`dias_acumulados`) não voltam a ser transportáveis — o art. 8.º n.º 4 e o
--    art. 9.º confirmam o horizonte de um ano ao mandarem gozar o remanescente
--    «até ao termo do ano civil imediato».
--
--  * Os dias acumulados SOMAM-SE ao saldo disponível do ano de destino, mas
--    ficam à parte do direito do próprio ano: são coisas diferentes e um ecrã de
--    RH tem de as poder distinguir.
--
-- Defensivo e idempotente em todos os passos, no molde da V40 a V49.
-- =============================================================

-- -------------------------------------------------------------
-- 1. As colunas
--
-- DEFAULT 0 aqui é seguro (ao contrário do regime da V49): zero é o valor certo
-- para todas as linhas existentes — nenhuma teve acumulação até hoje — e não há
-- nenhum passo posterior que dependa de encontrá-las a nulo.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_balance') IS NOT NULL THEN

        -- Dias recebidos do ano anterior.
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_balance'
                         AND column_name='dias_acumulados') THEN
            ALTER TABLE t_leave_balance ADD COLUMN dias_acumulados INTEGER NOT NULL DEFAULT 0;
        END IF;

        -- Porque é que não puderam ser gozados no ano em que se venceram.
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_balance'
                         AND column_name='acumulacao_motivo') THEN
            ALTER TABLE t_leave_balance ADD COLUMN acumulacao_motivo TEXT;
        END IF;

        -- Dias cedidos ao ano seguinte. Vive na linha de ORIGEM e é o que impede
        -- transportar duas vezes o mesmo dia.
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_balance'
                         AND column_name='dias_transportados') THEN
            ALTER TABLE t_leave_balance ADD COLUMN dias_transportados INTEGER NOT NULL DEFAULT 0;
        END IF;
    END IF;
END $$;

-- Auditoria Envers: as colunas novas têm de existir na tabela sombra. Sem NOT NULL
-- nem DEFAULT — uma revisão antiga não tem estes valores, e inventá-los seria
-- afirmar que existiam.
DO $$ BEGIN
    IF to_regclass('audit_schema.t_leave_balance_aud') IS NOT NULL THEN

        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_balance_aud'
                         AND column_name='dias_acumulados') THEN
            ALTER TABLE audit_schema.t_leave_balance_aud ADD COLUMN dias_acumulados INTEGER;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_balance_aud'
                         AND column_name='acumulacao_motivo') THEN
            ALTER TABLE audit_schema.t_leave_balance_aud ADD COLUMN acumulacao_motivo TEXT;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_balance_aud'
                         AND column_name='dias_transportados') THEN
            ALTER TABLE audit_schema.t_leave_balance_aud ADD COLUMN dias_transportados INTEGER;
        END IF;
    END IF;
END $$;

-- -------------------------------------------------------------
-- 2. Invariantes
--
-- Contagens de dias não são negativas, e não se cede mais do que se tinha
-- direito nesse ano. Ambas só entram se nada as violar — uma migração não deve
-- rebentar por causa de dados que já lá estavam.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_balance') IS NOT NULL THEN

        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                       WHERE table_schema='public' AND table_name='t_leave_balance'
                         AND constraint_name='ck_leave_balance_acumulacao_positiva')
           AND NOT EXISTS (SELECT 1 FROM t_leave_balance
                           WHERE dias_acumulados < 0 OR dias_transportados < 0) THEN
            ALTER TABLE t_leave_balance
                ADD CONSTRAINT ck_leave_balance_acumulacao_positiva
                CHECK (dias_acumulados >= 0 AND dias_transportados >= 0);
        END IF;

        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                       WHERE table_schema='public' AND table_name='t_leave_balance'
                         AND constraint_name='ck_leave_balance_transporte_dentro_do_direito')
           AND NOT EXISTS (SELECT 1 FROM t_leave_balance
                           WHERE dias_transportados > dias_direito) THEN
            ALTER TABLE t_leave_balance
                ADD CONSTRAINT ck_leave_balance_transporte_dentro_do_direito
                CHECK (dias_transportados <= dias_direito);
        END IF;
    END IF;
END $$;
