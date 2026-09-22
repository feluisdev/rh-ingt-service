-- =============================================================
-- V49 — o regime legal do tipo de ausência
--
-- O DL n.º 3/2010 trata férias (cap. II) e faltas (cap. III) em capítulos
-- distintos, com regras que não se misturam: as férias VENCEM-SE a 1 de Janeiro
-- num número de dias fixado por lei (art. 2.º n.os 3 e 4); uma falta não se
-- vence — acontece.
--
-- Para o saldo de férias nascer sozinho é preciso saber QUAIS das linhas do
-- catálogo são férias. O catálogo é da instituição — códigos, nomes e motivos
-- são dela, e o projecto já decidiu não os validar —, mas o regime é da lei.
-- Sem esta coluna, a alternativa era procurar o código 'FERIAS' escrito no
-- código Java: exactamente o que se evitou na substituição (lida da situação
-- funcional) e no efeito da licença no Lugar (lido do subtipo).
--
-- A instituição MAPEIA as suas linhas nos regimes que a lei conhece; não lhe
-- pode inventar um novo. Por isso a restrição fecha a lista.
--
-- Defensivo e idempotente em todos os passos, no molde da V40 a V48.
-- =============================================================

-- -------------------------------------------------------------
-- 1. A coluna
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_type') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_type'
                         AND column_name='regime') THEN
        -- SEM DEFAULT, de proposito. Um DEFAULT aqui preencheria JA todas as linhas
        -- existentes, e o passo 2 -- que so mexe no que esta a nulo -- nunca chegaria a
        -- classificar as ferias: ficariam FALTA, e o vencimento anual deixava de encontrar
        -- o tipo a que se aplica, em silencio. O DEFAULT entra no passo 4, depois de estar
        -- tudo classificado.
        ALTER TABLE t_leave_type ADD COLUMN regime VARCHAR(20);
    END IF;
END $$;

-- Auditoria Envers: a coluna nova tem de existir na tabela sombra.
DO $$ BEGIN
    IF to_regclass('audit_schema.t_leave_type_aud') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_type_aud'
                         AND column_name='regime') THEN
        ALTER TABLE audit_schema.t_leave_type_aud ADD COLUMN regime VARCHAR(20);
    END IF;
END $$;

-- -------------------------------------------------------------
-- 2. Classificar o que já lá está
--
-- FALTA é a omissão certa: é o que a esmagadora maioria das linhas é, e é o
-- regime que NÃO produz efeitos automáticos — classificar mal para FALTA não
-- faz nascer saldos indevidos, ao passo que o contrário faria.
--
-- As férias identificam-se pelo código do seed. É a única vez que o código
-- aparece: daqui em diante quem manda é a coluna, e a instituição pode
-- reclassificar sem tocar em código. Só preenche o que estiver vazio, para uma
-- segunda execução não desfazer uma classificação já corrigida à mão.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_type') IS NOT NULL
       AND EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='t_leave_type'
                     AND column_name='regime') THEN

        UPDATE t_leave_type SET regime = 'FERIAS'
         WHERE regime IS NULL AND upper(code) = 'FERIAS';

        UPDATE t_leave_type SET regime = 'FALTA'
         WHERE regime IS NULL;
    END IF;
END $$;

-- -------------------------------------------------------------
-- 3. Invariantes
--
-- A lista é fechada porque os regimes são os da lei. Já a obrigatoriedade
-- (NOT NULL) só entra se nada estiver por classificar — uma migração não deve
-- rebentar por causa de dados que já lá estavam.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_type') IS NOT NULL THEN

        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                       WHERE table_schema='public' AND table_name='t_leave_type'
                         AND constraint_name='ck_leave_type_regime')
           AND NOT EXISTS (SELECT 1 FROM t_leave_type
                           WHERE regime IS NOT NULL AND regime NOT IN ('FERIAS', 'FALTA')) THEN
            ALTER TABLE t_leave_type
                ADD CONSTRAINT ck_leave_type_regime CHECK (regime IN ('FERIAS', 'FALTA'));
        END IF;

        IF NOT EXISTS (SELECT 1 FROM t_leave_type WHERE regime IS NULL)
           AND EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_type'
                         AND column_name='regime' AND is_nullable='YES') THEN
            ALTER TABLE t_leave_type ALTER COLUMN regime SET NOT NULL;
        END IF;
    END IF;
END $$;

-- -------------------------------------------------------------
-- 4. Só agora o valor por omissão
--
-- Depois de tudo classificado, para que um INSERT que omita a coluna continue a
-- funcionar apesar do NOT NULL — o seed de raiz, um script antigo, ou um cliente
-- da API que não conheça o campo. FALTA é a omissão segura: é o regime que não
-- produz efeitos automáticos.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_type') IS NOT NULL
       AND EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='t_leave_type'
                     AND column_name='regime' AND column_default IS NULL) THEN
        ALTER TABLE t_leave_type ALTER COLUMN regime SET DEFAULT 'FALTA';
    END IF;
END $$;

-- -------------------------------------------------------------
-- 5. O saldo de férias é procurado por funcionário e ano, todos os dias, pelo
--    job do vencimento. Índice parcial sobre a linha que interessa.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_type') IS NOT NULL THEN
        CREATE INDEX IF NOT EXISTS ix_leave_type_regime
            ON t_leave_type (regime) WHERE is_active = TRUE;
    END IF;
END $$;
