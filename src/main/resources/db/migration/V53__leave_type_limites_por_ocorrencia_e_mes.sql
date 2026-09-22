-- =============================================================
-- V53 — o limite de dias tem mais do que uma natureza
--
-- O catálogo só sabia dizer «X dias por ano» (`max_days_per_year`), e o
-- `CreatePedidoAusenciaCommandHandler` somava sempre o ano civil. Mas o
-- art. 15.º n.º 1 do DL n.º 3/2010 quase nunca fala em anos:
--
--   a) «até 6, POR OCASIÃO do casamento»
--   b) «até 8, por motivo de FALECIMENTO do cônjuge ou parente no 1.º grau»
--   c) «até 3, por falecimento de parente em qualquer outro grau»
--   d) «até 3 CONSECUTIVAS, por motivo de doença comprovada por declaração médica»
--   e) «mais de 3 e até 30 CONSECUTIVAS, por doença comprovada por atestado»
--   f) «duas por CADA prova ou exame»
--   h) «duas por OCASIÃO do nascimento de um filho»
--
-- Estes limites são por acontecimento, não por ano. Escrevê-los em
-- `max_days_per_year` diz uma coisa diferente e errada nos dois sentidos: quem
-- perde dois familiares no mesmo ano via o segundo luto recusado, e quem pedia
-- 8 dias seguidos de uma só vez passava, porque o total do ano ainda cabia.
-- Uma ausência dessas é justificada por lei; recusá-la é um defeito visível ao
-- balcão.
--
-- E há um terceiro prazo, que também não é anual:
--
--   o) «um POR MÊS por conta do período de férias»
--   q) «não podendo ultrapassar 6 dias em cada ano civil E UM DIA POR MÊS»
--
-- A alínea q) mostra porque é que isto não pode ser uma coluna só com uma
-- classificação ao lado: a mesma linha do catálogo tem limite anual E mensal ao
-- mesmo tempo. Logo são três valores independentes, e cada um vale quando está
-- preenchido — `NULL` quer dizer «a lei não põe limite desta natureza», que é o
-- caso da maioria das alíneas (greve, obrigações legais, prisão preventiva…).
--
-- NÃO se mexe nos valores que já lá estão. O catálogo é da instituição, e uma
-- migração que corrigisse o `max_days_per_year` do luto estaria a decidir por
-- ela. O seed novo traz a classificação certa para instalações novas; a base de
-- desenvolvimento é reposta pelo `scripts/repor_estado.sql`, como aconteceu com
-- o MOB_COMISSAO.
--
-- Defensiva e idempotente, no molde da V40 a V52. Sem o problema do
-- `ADD COLUMN ... DEFAULT` que custou a classificação da V49: estas colunas
-- nascem nulas de propósito, e nulo é uma resposta com significado.
-- =============================================================

-- -------------------------------------------------------------
-- 1. As duas colunas, na tabela e na sombra de auditoria
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_type') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_type'
                         AND column_name='max_days_per_occurrence') THEN
        ALTER TABLE t_leave_type ADD COLUMN max_days_per_occurrence INTEGER;
    END IF;

    IF to_regclass('public.t_leave_type') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_type'
                         AND column_name='max_days_per_month') THEN
        ALTER TABLE t_leave_type ADD COLUMN max_days_per_month INTEGER;
    END IF;
END $$;

DO $$ BEGIN
    IF to_regclass('audit_schema.t_leave_type_aud') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_type_aud'
                         AND column_name='max_days_per_occurrence') THEN
        ALTER TABLE audit_schema.t_leave_type_aud ADD COLUMN max_days_per_occurrence INTEGER;
    END IF;

    IF to_regclass('audit_schema.t_leave_type_aud') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_type_aud'
                         AND column_name='max_days_per_month') THEN
        ALTER TABLE audit_schema.t_leave_type_aud ADD COLUMN max_days_per_month INTEGER;
    END IF;
END $$;

-- -------------------------------------------------------------
-- 2. Invariantes
--
-- Um limite de zero dias não é um limite: é um tipo de ausência que ninguém
-- pode pedir, e isso diz-se desactivando a linha (`is_active`). Negativo não
-- tem leitura nenhuma. A restrição aceita NULL, que é o «sem limite».
--
-- A verificação de que nada viola a regra vem antes de a criar: uma migração
-- não deve rebentar por causa de dados que já lá estavam.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_type') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                       WHERE table_schema='public' AND table_name='t_leave_type'
                         AND constraint_name='ck_leave_type_limites_positivos')
       AND NOT EXISTS (SELECT 1 FROM t_leave_type
                       WHERE (max_days_per_year IS NOT NULL AND max_days_per_year <= 0)
                          OR (max_days_per_occurrence IS NOT NULL AND max_days_per_occurrence <= 0)
                          OR (max_days_per_month IS NOT NULL AND max_days_per_month <= 0)) THEN
        ALTER TABLE t_leave_type
            ADD CONSTRAINT ck_leave_type_limites_positivos CHECK (
                (max_days_per_year IS NULL OR max_days_per_year > 0)
                AND (max_days_per_occurrence IS NULL OR max_days_per_occurrence > 0)
                AND (max_days_per_month IS NULL OR max_days_per_month > 0));
    END IF;
END $$;
