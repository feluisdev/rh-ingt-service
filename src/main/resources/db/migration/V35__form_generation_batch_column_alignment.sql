-- Migration: V35__form_generation_batch_column_alignment.sql
-- PRZ-07, Fase 119, plano 08. Corrige um defeito medido no plano 07 contra base real, que
-- quebra o PRZ-07 por construcao: uma falha parcial de geracao fica invisivel na aplicacao.
--
-- A causa e sistemica, e maior do que uma coluna: a V34 FOI APLICADA COM SUCESSO pelo Flyway,
-- e mesmo assim o esquema que ficou na base NAO E o que ela declara. Tres colunas de
-- t_form_generation_batch_item, e quatro de t_form_generation_batch, ficaram todas em
-- VARCHAR(255) -- exactamente o comprimento que o Hibernate atribui a um String sem anotacao
-- de comprimento -- em vez do comprimento que a V34 escreveu no CREATE TABLE. A causa e o
-- perfil development ter spring.jpa.hibernate.ddl-auto=update
-- (application-development.properties:38): o Hibernate corre depois do Flyway e reescreve o
-- tipo da coluna para o que a entidade JPA implicitamente pede, sem que o Flyway veja
-- diferenca nenhuma de o fazer (o proprio Flyway nao valida tipos de coluna contra o
-- ficheiro que os criou, so o checksum do ficheiro). E a divida sistemica ja registada neste
-- projecto -- tres regimes de esquema em conflito -- a produzir um defeito real: com
-- error_message em varchar(255), uma mensagem de excepcao mais longa faz o INSERT do item
-- falhar em commit, e o lote inteiro nunca se grava, incluindo os itens que correram bem.
-- Fica no log do servidor e invisivel na base e na API que o ecra consome -- exactamente o
-- modo de falha que o PRZ-07 proibe. Medido tres vezes em 119-07 (OBSERVACAO-BASE-REAL.md,
-- seccao 11).
--
-- Numeracao: V35, a seguir a V34. V24__update_kpi_criteria_fix.sql continua em disco, aplicada
-- na base do operador e nunca commitada -- mesma nota das V32/V33/V34, sem novidade aqui.
--
-- Forma: ALTER, nao CREATE. Ao contrario da V32/V33/V34, que criam tabelas que podiam nao
-- existir num ambiente limpo, estas duas tabelas ja existem desde a V34 nesta mesma fase --
-- nao ha caminho em que existam sem terem sido criadas com os tipos certos por essa migracao.
-- O que diverge e sempre o mesmo padrao: Hibernate ddl-auto=update a reescrever por cima.
--
-- Guardas: cada ALTER so corre se a coluna medida no information_schema ainda nao coincidir
-- com o que a V34 declarou -- para a migracao ser reexecutavel sem repetir trabalho, e para
-- nao falhar num ambiente onde o Hibernate nunca lhe tocou (por exemplo staging/production
-- com ddl-auto=validate, onde a V34 ja deixou o tipo certo).
--
-- Falha alta de proposito nas tres colunas que ENCOLHEM (outcome, skip_reason, e as quatro
-- de t_form_generation_batch): se existirem linhas com um valor mais comprido do que o novo
-- limite, o bloco RAISE EXCEPTION abaixo para a migracao antes de qualquer ALTER TYPE
-- correr -- precedente da V33. Nao ha truncagem silenciosa aqui; a Task 3 deste plano
-- acrescenta truncagem defensiva no codigo da aplicacao, para a proxima vez, mas esta
-- migracao nunca destroi dados existentes sem o dizer. error_message so ALARGA (varchar(255)
-- para TEXT), e alargar nunca perde dados.
--
-- Medido antes de escrever esta migracao (Task 1, 119-08): nenhuma das seis colunas que
-- encolhem tem, na base local, uma linha mais comprida do que o novo limite -- confirmado por
-- consulta directa via psycopg2 contra information_schema e MAX(LENGTH(coluna)). As duas
-- tabelas tinham 82 e 406 linhas (simulacao DRY_RUN do plano 07), nao vazias como se
-- presumia antes de medir.

-- 1. t_form_generation_batch -- quatro colunas divergentes (purpose, type,
--    generation_mode, status). generated_by ja estava correcto (VARCHAR(255) NOT NULL,
--    coincide com o que a entidade JPA e o Hibernate ja pedem) e nao e tocado.
DO $$
DECLARE
    max_len INTEGER;
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_form_generation_batch'
          AND column_name = 'purpose' AND (character_maximum_length IS NULL OR character_maximum_length <> 20)
    ) THEN
        SELECT MAX(LENGTH(purpose)) INTO max_len FROM t_form_generation_batch;
        IF max_len IS NOT NULL AND max_len > 20 THEN
            RAISE EXCEPTION 'V35: t_form_generation_batch.purpose tem dados mais compridos que 20 (max=%), migracao abortada', max_len;
        END IF;
        ALTER TABLE t_form_generation_batch ALTER COLUMN purpose TYPE VARCHAR(20);
    END IF;
END $$;

DO $$
DECLARE
    max_len INTEGER;
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_form_generation_batch'
          AND column_name = 'type' AND (character_maximum_length IS NULL OR character_maximum_length <> 30)
    ) THEN
        SELECT MAX(LENGTH(type)) INTO max_len FROM t_form_generation_batch;
        IF max_len IS NOT NULL AND max_len > 30 THEN
            RAISE EXCEPTION 'V35: t_form_generation_batch.type tem dados mais compridos que 30 (max=%), migracao abortada', max_len;
        END IF;
        ALTER TABLE t_form_generation_batch ALTER COLUMN type TYPE VARCHAR(30);
    END IF;
END $$;

DO $$
DECLARE
    max_len INTEGER;
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_form_generation_batch'
          AND column_name = 'generation_mode' AND (character_maximum_length IS NULL OR character_maximum_length <> 20)
    ) THEN
        SELECT MAX(LENGTH(generation_mode)) INTO max_len FROM t_form_generation_batch;
        IF max_len IS NOT NULL AND max_len > 20 THEN
            RAISE EXCEPTION 'V35: t_form_generation_batch.generation_mode tem dados mais compridos que 20 (max=%), migracao abortada', max_len;
        END IF;
        ALTER TABLE t_form_generation_batch ALTER COLUMN generation_mode TYPE VARCHAR(20);
    END IF;
END $$;

DO $$
DECLARE
    max_len INTEGER;
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_form_generation_batch'
          AND column_name = 'status' AND (character_maximum_length IS NULL OR character_maximum_length <> 30)
    ) THEN
        SELECT MAX(LENGTH(status)) INTO max_len FROM t_form_generation_batch;
        IF max_len IS NOT NULL AND max_len > 30 THEN
            RAISE EXCEPTION 'V35: t_form_generation_batch.status tem dados mais compridos que 30 (max=%), migracao abortada', max_len;
        END IF;
        ALTER TABLE t_form_generation_batch ALTER COLUMN status TYPE VARCHAR(30);
    END IF;
END $$;

-- 2. t_form_generation_batch_item -- as tres colunas medidas no 119-07.
DO $$
DECLARE
    max_len INTEGER;
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_form_generation_batch_item'
          AND column_name = 'outcome' AND (character_maximum_length IS NULL OR character_maximum_length <> 30)
    ) THEN
        SELECT MAX(LENGTH(outcome)) INTO max_len FROM t_form_generation_batch_item;
        IF max_len IS NOT NULL AND max_len > 30 THEN
            RAISE EXCEPTION 'V35: t_form_generation_batch_item.outcome tem dados mais compridos que 30 (max=%), migracao abortada', max_len;
        END IF;
        ALTER TABLE t_form_generation_batch_item ALTER COLUMN outcome TYPE VARCHAR(30);
    END IF;
END $$;

DO $$
DECLARE
    max_len INTEGER;
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_form_generation_batch_item'
          AND column_name = 'skip_reason' AND (character_maximum_length IS NULL OR character_maximum_length <> 50)
    ) THEN
        SELECT MAX(LENGTH(skip_reason)) INTO max_len FROM t_form_generation_batch_item;
        IF max_len IS NOT NULL AND max_len > 50 THEN
            RAISE EXCEPTION 'V35: t_form_generation_batch_item.skip_reason tem dados mais compridos que 50 (max=%), migracao abortada', max_len;
        END IF;
        ALTER TABLE t_form_generation_batch_item ALTER COLUMN skip_reason TYPE VARCHAR(50);
    END IF;
END $$;

-- error_message alarga para TEXT -- nunca encolhe, nunca perde dados, sem guarda de
-- comprimento nenhuma necessaria. So corre se ainda nao for text (idempotente).
DO $$ BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_form_generation_batch_item'
          AND column_name = 'error_message' AND data_type <> 'text'
    ) THEN
        ALTER TABLE t_form_generation_batch_item ALTER COLUMN error_message TYPE TEXT;
    END IF;
END $$;
