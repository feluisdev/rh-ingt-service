-- Migration: V36__form_generation_batch_revert.sql
-- PRZ-04, Fase 120, plano 01. Da ao rasto do lote (Fase 119) o sitio onde dizer que foi
-- desfeito -- decisao do operador de 2026-08-28: desfazer e apagar mesmo (DELETE das
-- avaliacoes criadas pelo lote), e uma avaliacao que ja avancou de fase e saltada e
-- reportada em vez de apagada. Sem estas colunas a reversao seria invisivel no ecra: o
-- relatorio do lote continuaria a dizer "12 gerados" depois de os 12 terem sido apagados.
--
-- Esta migracao NAO cria tabela nenhuma, nem tabela-sombra de auditoria. As duas tabelas
-- do rasto (t_form_generation_batch, t_form_generation_batch_item) NAO sao auditadas pelo
-- Envers -- D-02 do 119-01, sem @Audited, sem AuditEntity, o lote e ele proprio o registo
-- de auditoria. Isto e diferente de t_siadap_evaluations, que e auditada e cuja tabela
-- _aud a V20/V21/V31 espelham -- aqui nao ha _aud para espelhar.
--
-- Numeracao: V36, a seguir a V35 (escrita e commitada, mas por aplicar, tal como esta).
-- V24__update_kpi_criteria_fix.sql continua em disco, aplicada na base do operador e nunca
-- commitada -- mesma nota das V32/V33/V34/V35, sem novidade aqui.
--
-- Guardas: cada ADD COLUMN guardado por IF NOT EXISTS sobre information_schema.columns,
-- molde da V20/V21/V31, para a migracao correr sem erro tanto num ambiente onde o
-- ddl-auto=update ja criou as colunas como num ambiente limpo onde so o Flyway correu.
--
-- Nao ha CHECK sobre a coluna status de t_form_generation_batch -- D-05 do 119-01 ja previu
-- este crescimento de vocabulario (REVERTED, PARTIALLY_REVERTED) e por isso nao a restringiu.

-- 1. t_form_generation_batch -- quatro colunas, todas anulaveis.
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_form_generation_batch'
          AND column_name = 'reverted_at'
    ) THEN
        ALTER TABLE t_form_generation_batch ADD COLUMN reverted_at TIMESTAMP;
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_form_generation_batch'
          AND column_name = 'reverted_by'
    ) THEN
        ALTER TABLE t_form_generation_batch ADD COLUMN reverted_by VARCHAR(255);
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_form_generation_batch'
          AND column_name = 'reverted_count'
    ) THEN
        ALTER TABLE t_form_generation_batch ADD COLUMN reverted_count INTEGER;
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_form_generation_batch'
          AND column_name = 'revert_blocked_count'
    ) THEN
        ALTER TABLE t_form_generation_batch ADD COLUMN revert_blocked_count INTEGER;
    END IF;
END $$;

-- 2. t_form_generation_batch_item -- duas colunas, ambas anulaveis.
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_form_generation_batch_item'
          AND column_name = 'reverted_at'
    ) THEN
        ALTER TABLE t_form_generation_batch_item ADD COLUMN reverted_at TIMESTAMP;
    END IF;
END $$;

DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_form_generation_batch_item'
          AND column_name = 'revert_skip_reason'
    ) THEN
        ALTER TABLE t_form_generation_batch_item ADD COLUMN revert_skip_reason VARCHAR(50);
    END IF;
END $$;
