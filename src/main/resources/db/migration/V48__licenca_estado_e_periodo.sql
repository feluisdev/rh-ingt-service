-- =============================================================
-- V48 — separar a decisão do período, na licença e na mobilidade
--
-- O art. 44.º do DL n.º 3/2010 trata dois factos em dois números seguidos: o
-- n.º 1 define a licença como «ausência prolongada do serviço» — um PERÍODO; o
-- n.º 2 diz que a concessão depende «do pedido do interessado e do DESPACHO da
-- autoridade competente» — um ACTO, instantâneo e datado. O despacho de hoje e a
-- ausência de Outubro não são a mesma coisa.
--
-- A coluna `status` guardava as duas. Daí vinham três defeitos:
--   * aprovar um despacho para Outubro punha a pessoa de licença HOJE;
--   * uma licença cujo fim já passou ficava ACTIVE para sempre, até alguém
--     carregar no /close — quando a lei diz que caduca «automaticamente»
--     (art. 46.º n.º 3);
--   * encerrar uma licença por começar fixava o fim em hoje, ANTES do início,
--     falseando as contagens de dias que o art. 47.º n.os 1 a 3 manda fazer
--     (desconto na antiguidade, férias proporcionais).
--
-- A partir daqui:
--   * `status` guarda só a decisão: PENDING · APPROVED · REJECTED · CANCELLED.
--     ACTIVE e CLOSED desaparecem — não eram decisões, eram o período
--     disfarçado de decisão.
--   * o estado temporal (por iniciar / em curso / terminada) DERIVA das datas e
--     não se guarda, como já manda a convenção do projecto.
--   * os efeitos no Lugar passam a ser aplicados na data efectiva por um job
--     diário. As duas marcas novas dizem que já foram aplicados; é o que torna
--     o job seguro de repetir.
--
-- Defensivo e idempotente em todos os passos, no molde da V40 a V47.
-- =============================================================

-- -------------------------------------------------------------
-- 1. Marcas de aplicação dos efeitos (idempotência do job)
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_mobility') IS NOT NULL THEN

        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_mobility'
                         AND column_name='efeito_entrada_aplicado_em') THEN
            ALTER TABLE t_leave_mobility ADD COLUMN efeito_entrada_aplicado_em TIMESTAMP;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_mobility'
                         AND column_name='efeito_regresso_aplicado_em') THEN
            ALTER TABLE t_leave_mobility ADD COLUMN efeito_regresso_aplicado_em TIMESTAMP;
        END IF;
    END IF;
END $$;

-- Auditoria Envers: as colunas novas têm de existir na tabela sombra.
DO $$ BEGIN
    IF to_regclass('audit_schema.t_leave_mobility_aud') IS NOT NULL THEN

        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_mobility_aud'
                         AND column_name='efeito_entrada_aplicado_em') THEN
            ALTER TABLE audit_schema.t_leave_mobility_aud ADD COLUMN efeito_entrada_aplicado_em TIMESTAMP;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_mobility_aud'
                         AND column_name='efeito_regresso_aplicado_em') THEN
            ALTER TABLE audit_schema.t_leave_mobility_aud ADD COLUMN efeito_regresso_aplicado_em TIMESTAMP;
        END IF;
    END IF;
END $$;

-- -------------------------------------------------------------
-- 2. Períodos impossíveis — fim antes do início
--
-- Produzidos pelo /close de uma licença que ainda não tinha começado. Pelo
-- art. 44.º n.º 1 não houve ausência nenhuma; mas apagar o registo perderia o
-- despacho, que existiu. Colapsa-se o período para um único dia, que é o mínimo
-- exprimível, e deixa-se o registo legível. Tem de correr ANTES da restrição
-- do ponto 5, senão a restrição não entra.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_mobility') IS NOT NULL THEN
        UPDATE t_leave_mobility
           SET data_fim = data_inicio
         WHERE data_fim IS NOT NULL
           AND data_fim < data_inicio;
    END IF;
END $$;

-- -------------------------------------------------------------
-- 3. ACTIVE e CLOSED passam a APPROVED
--
-- Os dois eram o mesmo despacho — deferido. O que os distinguia era o período,
-- que continua a estar nas datas e passa a ser lido de lá.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_mobility') IS NOT NULL THEN
        UPDATE t_leave_mobility SET status = 'APPROVED' WHERE status IN ('ACTIVE', 'CLOSED');
    END IF;
END $$;

-- -------------------------------------------------------------
-- 4. Efeitos já aplicados pelo modelo antigo
--
-- No modelo antigo a entrada em vigor era aplicada na aprovação e o regresso no
-- encerramento. Sem esta marcação o job voltaria a aplicá-los, e a segunda vez
-- não é inofensiva: encerraria de novo uma afectação e escreveria outro registo
-- no histórico de estados.
--
-- Não sabemos QUANDO foram aplicados — o modelo antigo não o guardava. O que
-- importa é que a marca não seja nula; usa-se o instante da migração e diz-se
-- aqui porquê, em vez de inventar uma data que passaria por verdadeira.
-- Idempotente: só marca o que ainda está por marcar.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_mobility') IS NOT NULL
       AND EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='t_leave_mobility'
                     AND column_name='efeito_entrada_aplicado_em') THEN

        -- Entrada: aplicada a tudo o que chegou a ser deferido e já começou.
        UPDATE t_leave_mobility
           SET efeito_entrada_aplicado_em = now()
         WHERE status = 'APPROVED'
           AND data_inicio <= CURRENT_DATE
           AND efeito_entrada_aplicado_em IS NULL;

        -- Regresso: aplicado ao que já tinha fim no passado (o antigo CLOSED
        -- fixava sempre o fim na data do encerramento, nunca no futuro).
        UPDATE t_leave_mobility
           SET efeito_regresso_aplicado_em = now()
         WHERE status = 'APPROVED'
           AND data_fim IS NOT NULL
           AND data_fim < CURRENT_DATE
           AND efeito_regresso_aplicado_em IS NULL;
    END IF;
END $$;

-- -------------------------------------------------------------
-- 5. Invariantes no esquema
--
-- Ambas as restrições só entram se nada as violar — uma migração não deve
-- rebentar por causa de dados que já lá estavam. Se sobrar alguma linha
-- violadora, a restrição fica por criar e a próxima execução tenta de novo.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_mobility') IS NOT NULL THEN

        -- O período não pode acabar antes de começar. É isto que torna
        -- impossível, por construção, a licença que acabava antes de começar.
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                       WHERE table_schema='public' AND table_name='t_leave_mobility'
                         AND constraint_name='ck_leave_mobility_periodo')
           AND NOT EXISTS (SELECT 1 FROM t_leave_mobility
                           WHERE data_fim IS NOT NULL AND data_fim < data_inicio) THEN
            ALTER TABLE t_leave_mobility
                ADD CONSTRAINT ck_leave_mobility_periodo CHECK (data_fim IS NULL OR data_fim >= data_inicio);
        END IF;

        -- O status guarda decisões, e só estas quatro.
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                       WHERE table_schema='public' AND table_name='t_leave_mobility'
                         AND constraint_name='ck_leave_mobility_status')
           AND NOT EXISTS (SELECT 1 FROM t_leave_mobility
                           WHERE status NOT IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')) THEN
            ALTER TABLE t_leave_mobility
                ADD CONSTRAINT ck_leave_mobility_status
                CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'));
        END IF;
    END IF;
END $$;

-- -------------------------------------------------------------
-- 6. Índices para o job
--
-- O job pergunta todos os dias "o que está deferido, já começou e ainda não
-- teve efeitos?" — e o mesmo para o regresso. Índices parciais, porque a
-- resposta normal é um punhado de linhas numa tabela que só cresce.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_leave_mobility') IS NOT NULL
       AND EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='t_leave_mobility'
                     AND column_name='efeito_entrada_aplicado_em') THEN

        CREATE INDEX IF NOT EXISTS ix_leave_mobility_entrada_por_aplicar
            ON t_leave_mobility (data_inicio)
            WHERE status = 'APPROVED' AND efeito_entrada_aplicado_em IS NULL;

        CREATE INDEX IF NOT EXISTS ix_leave_mobility_regresso_por_aplicar
            ON t_leave_mobility (data_fim)
            WHERE status = 'APPROVED' AND efeito_regresso_aplicado_em IS NULL;
    END IF;
END $$;
