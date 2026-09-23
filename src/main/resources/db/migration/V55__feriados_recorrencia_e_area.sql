-- =============================================================
-- V55 — feriados: recorrência e área geográfica
--
-- A contagem de dias úteis (férias, faltas, suspensão) tira os feriados que o
-- catálogo lhe der. Três defeitos, todos por esta tabela:
--
--   1. Os feriados tinham data de um ano só. O seed traz os de 2026 e, a 1 de
--      Janeiro de 2027, a aplicação deixava de conhecer feriado nenhum — contava
--      dias úteis a mais em todos os pedidos, em silêncio. Um feriado de data fixa
--      (1 de Janeiro, 5 de Julho, 25 de Dezembro...) passa a poder ser marcado como
--      RECORRENTE e vale todos os anos a partir do da sua data. Os móveis (Sexta-feira
--      Santa, Corpus Christi) continuam a ser carregados ano a ano: dependem da Páscoa,
--      e qual das festas móveis é feriado é matéria de lei.
--   2. Só os nacionais contavam. O calendário é da INSTITUIÇÃO: ela cataloga os
--      nacionais e o municipal do sítio onde está, e conta-se tudo o que estiver activo.
--   3. Uma instituição com serviços em vários concelhos precisa de limitar um feriado
--      a uma área. A área é uma entrada do catálogo genérico (t_option_entity, ccode
--      AREA_GEOGRAFICA) — não um concelho de Cabo Verde, para a aplicação ser instalável
--      noutro país — e é a UNIDADE ORGÂNICA que diz em que área está. Feriado sem área
--      vale para toda a gente, que é o caso normal.
--
-- A tabela t_public_holiday nunca teve migração: foi criada pelo Hibernate
-- (ddl-auto). Esta passa a ser dona dela, com CREATE TABLE IF NOT EXISTS na forma
-- que o Hibernate lhe deu — numa base já criada não muda nada.
--
-- A migração NÃO marca nenhum feriado como recorrente: o catálogo é da instituição,
-- e FALSE é o que já lá estava (cada linha vale só na sua data). O seed traz os de
-- data fixa marcados; o repor_estado.sql alinha a base de desenvolvimento.
--
-- Defensivo e idempotente em todos os passos, com o guarda to_regclass ANINHADO
-- (ver V49 e a correcção da V51/V53).
-- =============================================================

-- -------------------------------------------------------------
-- 1. A tabela, na forma em que o Hibernate a criou
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_public_holiday (
    id                  UUID            NOT NULL,
    name                VARCHAR(150)    NOT NULL,
    holiday_date        DATE            NOT NULL,
    is_national         BOOLEAN         NOT NULL,
    description         VARCHAR(500),
    is_active           BOOLEAN         NOT NULL,
    created_date        TIMESTAMP(6)    NOT NULL,
    created_by          VARCHAR(255)    NOT NULL,
    last_modified_date  TIMESTAMP(6),
    last_modified_by    VARCHAR(255),
    PRIMARY KEY (id)
);

DO $$ BEGIN
    IF to_regclass('audit_schema.revinfo') IS NOT NULL THEN
        CREATE TABLE IF NOT EXISTS audit_schema.t_public_holiday_aud (
            id                  UUID            NOT NULL,
            rev                 INTEGER         NOT NULL,
            revtype             SMALLINT,
            name                VARCHAR(150),
            holiday_date        DATE,
            is_national         BOOLEAN,
            description         VARCHAR(500),
            is_active           BOOLEAN,
            created_date        TIMESTAMP(6),
            created_by          VARCHAR(255),
            last_modified_date  TIMESTAMP(6),
            last_modified_by    VARCHAR(255),
            PRIMARY KEY (rev, id)
        );
    END IF;
END $$;

-- -------------------------------------------------------------
-- 2. Recorrência
--
-- Sem DEFAULT ao acrescentar (ver V49: um DEFAULT preencheria já as linhas e o
-- passo seguinte não teria nada que fazer — aqui daria o mesmo resultado, mas a
-- ordem certa é a mesma em todas as migrações). Depois: preencher, NOT NULL,
-- e só então o valor por omissão.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_public_holiday') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_public_holiday'
                         AND column_name='is_recurring') THEN
            ALTER TABLE t_public_holiday ADD COLUMN is_recurring BOOLEAN;
        END IF;

        UPDATE t_public_holiday SET is_recurring = FALSE WHERE is_recurring IS NULL;

        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='t_public_holiday'
                     AND column_name='is_recurring' AND is_nullable='YES') THEN
            ALTER TABLE t_public_holiday ALTER COLUMN is_recurring SET NOT NULL;
        END IF;

        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='t_public_holiday'
                     AND column_name='is_recurring' AND column_default IS NULL) THEN
            ALTER TABLE t_public_holiday ALTER COLUMN is_recurring SET DEFAULT FALSE;
        END IF;
    END IF;
END $$;

-- -------------------------------------------------------------
-- 3. Área geográfica do feriado (ckey de AREA_GEOGRAFICA; nulo = vale para todos)
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_public_holiday') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_public_holiday'
                         AND column_name='area_ckey') THEN
            ALTER TABLE t_public_holiday ADD COLUMN area_ckey VARCHAR(100);
        END IF;
    END IF;
END $$;

DO $$ BEGIN
    IF to_regclass('audit_schema.t_public_holiday_aud') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_public_holiday_aud'
                         AND column_name='is_recurring') THEN
            ALTER TABLE audit_schema.t_public_holiday_aud ADD COLUMN is_recurring BOOLEAN;
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_public_holiday_aud'
                         AND column_name='area_ckey') THEN
            ALTER TABLE audit_schema.t_public_holiday_aud ADD COLUMN area_ckey VARCHAR(100);
        END IF;
    END IF;
END $$;

-- -------------------------------------------------------------
-- 4. Um feriado nacional é de todo o território: não tem área.
--    Só entra se nada o violar — a migração não rebenta por dados que lá estejam.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_public_holiday') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                       WHERE table_schema='public' AND table_name='t_public_holiday'
                         AND constraint_name='ck_public_holiday_nacional_sem_area') THEN
            IF NOT EXISTS (SELECT 1 FROM t_public_holiday
                           WHERE is_national AND area_ckey IS NOT NULL) THEN
                ALTER TABLE t_public_holiday
                    ADD CONSTRAINT ck_public_holiday_nacional_sem_area
                    CHECK (NOT is_national OR area_ckey IS NULL);
            END IF;
        END IF;
    END IF;
END $$;

-- -------------------------------------------------------------
-- 5. A contagem de dias úteis procura os activos por data a cada pedido.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_public_holiday') IS NOT NULL THEN
        CREATE INDEX IF NOT EXISTS ix_public_holiday_activos
            ON t_public_holiday (holiday_date) WHERE is_active = TRUE;
    END IF;
END $$;

-- -------------------------------------------------------------
-- 6. Área geográfica da unidade orgânica
--
-- t_unidade_organica também nasce pelo Hibernate numa base vazia (a V7 só lhe
-- tira uma coluna), por isso o guarda. Nula quer dizer «a unidade herda a da
-- unidade-mãe»; nula até ao topo quer dizer que só contam os feriados sem área.
-- -------------------------------------------------------------
DO $$ BEGIN
    IF to_regclass('public.t_unidade_organica') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_unidade_organica'
                         AND column_name='area_ckey') THEN
            ALTER TABLE t_unidade_organica ADD COLUMN area_ckey VARCHAR(100);
        END IF;
    END IF;
END $$;

DO $$ BEGIN
    IF to_regclass('audit_schema.t_unidade_organica_aud') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_unidade_organica_aud'
                         AND column_name='area_ckey') THEN
            ALTER TABLE audit_schema.t_unidade_organica_aud ADD COLUMN area_ckey VARCHAR(100);
        END IF;
    END IF;
END $$;
