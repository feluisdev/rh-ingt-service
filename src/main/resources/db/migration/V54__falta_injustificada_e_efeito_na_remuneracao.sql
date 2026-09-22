-- =============================================================
-- V54 — a falta injustificada, e o efeito da ausência na remuneração
--
-- Duas coisas que vivem na mesma frase da lei e por isso vêm na mesma migração.
--
-- 1. FALTA INJUSTIFICADA (art. 43.º do DL n.º 3/2010)
--
-- O n.º 2 diz: «As faltas injustificadas, para além das consequências
-- disciplinares a que possam dar lugar, NÃO CONTAM PARA EFEITOS DE ANTIGUIDADE
-- e implicam a opção entre a perda das remunerações correspondentes aos dias de
-- ausência, ou o seu desconto nas férias.»
--
-- O desconto na antiguidade é IMPERATIVO — a lei não dá escolha à instituição —,
-- e por isso não entra aqui um booleano `counts_seniority`: um booleano deixaria
-- configurar o contrário da lei. O que falta é dizer QUAIS das linhas do
-- catálogo são injustificadas, e isso é exactamente o que o `regime` da V49 já
-- faz para as férias. Ganha um terceiro valor: o DL trata as injustificadas em
-- secção própria (a III do cap. III), com efeitos próprios.
--
-- A única escolha que a lei dá é outra — perder a remuneração OU descontar nas
-- férias — e é de CADA CASO, não do catálogo. Vai para o pedido.
--
-- 2. EFEITO NA REMUNERAÇÃO (art. 16.º)
--
-- Esta aplicação não calcula remuneração e não vai passar a calcular. O que
-- passa a fazer é GUARDAR A CLASSIFICAÇÃO, para o sistema que processa
-- vencimentos a poder ler. Um booleano não chega, porque o artigo tem quatro
-- efeitos distintos:
--
--   n.º 1     sem perda (regra das justificadas)
--   n.º 2 e 3 perda PARCIAL nas als. d), e), i), j) e t), com direito a subsídio
--             da previdência — o valor é a diferença, e essa conta é de quem
--             processa vencimentos, não nossa
--   n.º 4     perda TOTAL: greve (que, note-se, não desconta antiguidade)
--   n.º 5 e 6 perda do VENCIMENTO DE EXERCÍCIO: prisão preventiva, reparável em
--             caso de absolvição
--
-- E um quinto valor, que é honestidade e não invenção: DEPENDE_DA_OPCAO, para a
-- falta injustificada do art. 43.º n.º 2 — aí a lei não fixa o efeito, dá uma
-- opção, e quem a exerce di-lo no pedido.
--
-- NADA SE CLASSIFICA AQUI. Nem o regime das linhas existentes, nem o efeito na
-- remuneração: o catálogo é da instituição e estas duas colunas mandam descontar
-- antiguidade e mexer em salários. Decidir por ela em silêncio seria o pior sítio
-- para o fazer. As linhas antigas ficam em SEM_PERDA — que é a direcção segura,
-- porque afirma que não há perda em vez de a provocar — e no regime que já
-- tinham. O seed traz a classificação certa para instalações novas; a base de
-- desenvolvimento é reposta pelo `scripts/repor_estado.sql`.
--
-- Defensiva e idempotente. Guardas SEMPRE aninhados: o PL/pgSQL prepara a
-- instrução inteira quando a executa, por isso um `to_regclass(...) IS NOT NULL
-- AND NOT EXISTS (SELECT 1 FROM <tabela>)` rebenta com "relation does not exist"
-- quando a tabela falta — o curto-circuito lógico não salva. Foi o defeito que a
-- V51 e a V53 tinham e que esta migração não repete.
-- =============================================================

-- -------------------------------------------------------------
-- 1. Efeito na remuneração — a coluna, na tabela e na sombra de auditoria
--
-- Sem DEFAULT no ADD COLUMN, de propósito: um DEFAULT preencheria já todas as
-- linhas existentes e qualquer passo posterior que só olhe ao que está a nulo
-- nunca chegaria a ver nada. Foi o que custou a classificação inteira da V49.
-- O DEFAULT entra no fim, depois de a coluna estar preenchida.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_type') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_type'
                         AND column_name='efeito_remuneracao') THEN
        ALTER TABLE t_leave_type ADD COLUMN efeito_remuneracao VARCHAR(30);
    END IF;
END $$;

DO $$ BEGIN
    IF to_regclass('audit_schema.t_leave_type_aud') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_type_aud'
                         AND column_name='efeito_remuneracao') THEN
        ALTER TABLE audit_schema.t_leave_type_aud ADD COLUMN efeito_remuneracao VARCHAR(30);
    END IF;
END $$;

-- -------------------------------------------------------------
-- 2. As linhas que já lá estavam ficam em SEM_PERDA
--
-- Não é classificação: é a direcção segura. SEM_PERDA afirma que não há perda,
-- em vez de a provocar num sítio onde ninguém a decidiu. Só toca no que estiver
-- vazio, para uma segunda execução não desfazer o que a instituição corrigiu.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_type') IS NOT NULL THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='t_leave_type'
                     AND column_name='efeito_remuneracao') THEN
            UPDATE t_leave_type SET efeito_remuneracao = 'SEM_PERDA'
             WHERE efeito_remuneracao IS NULL;
        END IF;
    END IF;
END $$;

-- -------------------------------------------------------------
-- 3. Invariantes do efeito na remuneração
--
-- A lista é fechada porque os efeitos são os do art. 16.º e do art. 43.º n.º 2.
-- O NOT NULL só entra se nada estiver por preencher, e o DEFAULT só no fim —
-- para que um INSERT que omita a coluna continue a funcionar (o seed de raiz, um
-- script antigo, um cliente da API que não conheça o campo).
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_type') IS NOT NULL THEN

        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                       WHERE table_schema='public' AND table_name='t_leave_type'
                         AND constraint_name='ck_leave_type_efeito_remuneracao')
           AND NOT EXISTS (SELECT 1 FROM t_leave_type
                           WHERE efeito_remuneracao IS NOT NULL
                             AND efeito_remuneracao NOT IN ('SEM_PERDA', 'PERDA_PARCIAL',
                                 'PERDA_TOTAL', 'PERDA_VENCIMENTO_EXERCICIO', 'DEPENDE_DA_OPCAO')) THEN
            ALTER TABLE t_leave_type
                ADD CONSTRAINT ck_leave_type_efeito_remuneracao CHECK (
                    efeito_remuneracao IN ('SEM_PERDA', 'PERDA_PARCIAL', 'PERDA_TOTAL',
                                           'PERDA_VENCIMENTO_EXERCICIO', 'DEPENDE_DA_OPCAO'));
        END IF;

        IF NOT EXISTS (SELECT 1 FROM t_leave_type WHERE efeito_remuneracao IS NULL)
           AND EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_type'
                         AND column_name='efeito_remuneracao' AND is_nullable='YES') THEN
            ALTER TABLE t_leave_type ALTER COLUMN efeito_remuneracao SET NOT NULL;
        END IF;

        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='t_leave_type'
                     AND column_name='efeito_remuneracao' AND column_default IS NULL) THEN
            ALTER TABLE t_leave_type ALTER COLUMN efeito_remuneracao SET DEFAULT 'SEM_PERDA';
        END IF;
    END IF;
END $$;

-- -------------------------------------------------------------
-- 4. O regime abre-se ao terceiro valor (art. 43.º)
--
-- A restrição da V49 fechava a lista em FERIAS · FALTA. Troca-se pela lista das
-- três secções que a lei distingue. Não se reclassifica linha nenhuma: quem é
-- injustificada di-lo a instituição, e enquanto ninguém o disser nada desconta —
-- que é a direcção certa, porque descontar por omissão tiraria antiguidade a
-- quem a tem.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_type') IS NOT NULL THEN
        IF EXISTS (SELECT 1 FROM information_schema.table_constraints
                   WHERE table_schema='public' AND table_name='t_leave_type'
                     AND constraint_name='ck_leave_type_regime') THEN
            ALTER TABLE t_leave_type DROP CONSTRAINT ck_leave_type_regime;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM t_leave_type
                       WHERE regime IS NOT NULL
                         AND regime NOT IN ('FERIAS', 'FALTA', 'FALTA_INJUSTIFICADA')) THEN
            ALTER TABLE t_leave_type
                ADD CONSTRAINT ck_leave_type_regime
                CHECK (regime IN ('FERIAS', 'FALTA', 'FALTA_INJUSTIFICADA'));
        END IF;
    END IF;
END $$;

-- -------------------------------------------------------------
-- 5. A opção do art. 43.º n.º 2, no pedido
--
-- «Implicam a opção entre a perda das remunerações correspondentes aos dias de
-- ausência, OU o seu desconto nas férias.» É a única escolha que a lei dá, e é
-- de cada caso — por isso vive no pedido e não no catálogo.
--
-- Fica nula em tudo o que não seja falta injustificada; é o domínio que o
-- garante, e não uma restrição na base, porque saber se o tipo é injustificado
-- obriga a ir a outra tabela.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_request') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_request'
                         AND column_name='opcao_falta_injustificada') THEN
        ALTER TABLE t_leave_request ADD COLUMN opcao_falta_injustificada VARCHAR(30);
    END IF;
END $$;

DO $$ BEGIN
    IF to_regclass('audit_schema.t_leave_request_aud') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_request_aud'
                         AND column_name='opcao_falta_injustificada') THEN
        ALTER TABLE audit_schema.t_leave_request_aud ADD COLUMN opcao_falta_injustificada VARCHAR(30);
    END IF;
END $$;

DO $$ BEGIN
    IF to_regclass('public.t_leave_request') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                       WHERE table_schema='public' AND table_name='t_leave_request'
                         AND constraint_name='ck_leave_request_opcao_injustificada')
           AND NOT EXISTS (SELECT 1 FROM t_leave_request
                           WHERE opcao_falta_injustificada IS NOT NULL
                             AND opcao_falta_injustificada NOT IN ('PERDA_REMUNERACAO', 'DESCONTO_FERIAS')) THEN
            ALTER TABLE t_leave_request
                ADD CONSTRAINT ck_leave_request_opcao_injustificada CHECK (
                    opcao_falta_injustificada IS NULL
                    OR opcao_falta_injustificada IN ('PERDA_REMUNERACAO', 'DESCONTO_FERIAS'));
        END IF;
    END IF;
END $$;

-- -------------------------------------------------------------
-- 6. A antiguidade passa a perguntar quais pedidos são de falta injustificada.
--    Índice parcial sobre a coluna que a pergunta usa.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_type') IS NOT NULL THEN
        CREATE INDEX IF NOT EXISTS ix_leave_type_regime_injustificada
            ON t_leave_type (regime) WHERE regime = 'FALTA_INJUSTIFICADA';
    END IF;
END $$;
