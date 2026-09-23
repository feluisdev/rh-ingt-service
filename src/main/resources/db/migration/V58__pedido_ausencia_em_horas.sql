-- =============================================================
-- V58 — pedido de ausência em horas
--
-- A lei tem ausências de parte do dia: o tratamento ambulatório «durante o tempo
-- necessário» (DL n.º 3/2010, art. 37.º), as consultas pré-natais e a doação de sangue
-- (art. 15.º als. u) e k)), o crédito de horas sindical (al. r)), e a dispensa de
-- amamentação — duas horas por dia, em dois períodos (Lei n.º 20/X/2023, art. 172.º
-- n.º 3). O art. 38.º n.º 2 converte as horas em faltas pelo art. 13.º.
--
-- Não há tabela nova: é o pedido de ausência que já existe, com duas colunas
-- OPCIONAIS. Sem elas, o pedido continua de dias inteiros, como sempre foi. Com elas,
-- as horas valem em cada dia do intervalo dataInicio–dataFim (a amamentação é um só
-- pedido para meses).
--
--   t_leave_request.hora_inicio / hora_fim  — as duas ou nenhuma, início antes do fim
--   t_leave_type.max_minutos_por_dia        — tecto diário do tipo (amamentação: 120);
--                                             nulo = sem tecto
--
-- Colunas sem DEFAULT e sem preencher: nulo é o que as linhas existentes são (dias
-- inteiros, sem tecto em minutos). Defensiva e idempotente, com o guarda to_regclass
-- ANINHADO (ver V49 e V55).
-- =============================================================

DO $$ BEGIN
    IF to_regclass('public.t_leave_request') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_request'
                         AND column_name='hora_inicio') THEN
            ALTER TABLE t_leave_request ADD COLUMN hora_inicio TIME;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_request'
                         AND column_name='hora_fim') THEN
            ALTER TABLE t_leave_request ADD COLUMN hora_fim TIME;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                       WHERE table_schema='public' AND table_name='t_leave_request'
                         AND constraint_name='ck_leave_request_horas') THEN
            -- Só entra se nada o violar: a migração não rebenta por dados que lá estejam
            -- (colunas criadas antes pelo Hibernate e já com linhas, por exemplo).
            IF NOT EXISTS (SELECT 1 FROM t_leave_request
                           WHERE NOT ((hora_inicio IS NULL AND hora_fim IS NULL)
                                   OR (hora_inicio IS NOT NULL AND hora_fim IS NOT NULL AND hora_inicio < hora_fim))) THEN
                ALTER TABLE t_leave_request ADD CONSTRAINT ck_leave_request_horas
                    CHECK ((hora_inicio IS NULL AND hora_fim IS NULL)
                        OR (hora_inicio IS NOT NULL AND hora_fim IS NOT NULL AND hora_inicio < hora_fim));
            END IF;
        END IF;
    END IF;
END $$;

DO $$ BEGIN
    IF to_regclass('audit_schema.t_leave_request_aud') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_request_aud'
                         AND column_name='hora_inicio') THEN
            ALTER TABLE audit_schema.t_leave_request_aud ADD COLUMN hora_inicio TIME;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_request_aud'
                         AND column_name='hora_fim') THEN
            ALTER TABLE audit_schema.t_leave_request_aud ADD COLUMN hora_fim TIME;
        END IF;
    END IF;
END $$;

DO $$ BEGIN
    IF to_regclass('public.t_leave_type') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_leave_type'
                         AND column_name='max_minutos_por_dia') THEN
            ALTER TABLE t_leave_type ADD COLUMN max_minutos_por_dia INTEGER;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                       WHERE table_schema='public' AND table_name='t_leave_type'
                         AND constraint_name='ck_leave_type_max_minutos_positivo') THEN
            IF NOT EXISTS (SELECT 1 FROM t_leave_type WHERE max_minutos_por_dia <= 0) THEN
                ALTER TABLE t_leave_type ADD CONSTRAINT ck_leave_type_max_minutos_positivo
                    CHECK (max_minutos_por_dia IS NULL OR max_minutos_por_dia > 0);
            END IF;
        END IF;
    END IF;
END $$;

DO $$ BEGIN
    IF to_regclass('audit_schema.t_leave_type_aud') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_leave_type_aud'
                         AND column_name='max_minutos_por_dia') THEN
            ALTER TABLE audit_schema.t_leave_type_aud ADD COLUMN max_minutos_por_dia INTEGER;
        END IF;
    END IF;
END $$;
