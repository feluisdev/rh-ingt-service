-- Migration: V33__paa_submission_period_purpose_check.sql
-- PRZ-03, Fase 118. Fecha o terceiro dos tres caminhos silenciosos que entregavam
-- Purpose.PAA sem escolha explicita: a coluna t_paa_submission_period.purpose tinha
-- DEFAULT 'PAA' e nenhuma restricao CHECK contra o enum Purpose (V19, linha 12).
-- Os outros dois caminhos -- o DTO de criacao opcional e a sobrecarga de dominio
-- create(PaaLevel, ...) -- foram fechados em Java nesta mesma fase (planos 118-01
-- e 118-02); esta migracao fecha o caminho que nenhum codigo Java consegue fechar
-- sozinho: um INSERT directo, um script, ou um ddl-auto=update noutro ambiente que
-- reescreva a coluna sem esta migracao.
--
-- Numeracao: V33, e nao V24. V24__update_kpi_criteria_fix.sql existe em disco, esta
-- aplicada na base do operador e nunca foi commitada (git status: ??). Reutilizar o
-- 24 faria o Flyway comparar o checksum do ficheiro novo contra o checksum ja
-- registado na base local e falhar a validacao. V32 e a mais alta commitada.
--
-- Guardas: estes ambientes correram ddl-auto=update antes de existir Flyway, e o
-- Flyway corre antes do ddl-auto (precedente escrito na V19 e repetido na V32).
-- Uma migracao que assuma estado limpo esta errada neste repositorio -- por isso
-- cada alteracao abaixo confirma primeiro, via information_schema/pg_constraint,
-- que a coisa que vai mudar ainda nao mudou.
--
-- Falha alta de proposito: se existirem linhas com um purpose fora dos seis codigos
-- do enum, o ADD CONSTRAINT abaixo falha e a migracao para. Nao ha NOT VALID, nao ha
-- EXCEPTION WHEN, nao ha guarda que salte a criacao do CHECK perante dados em
-- infraccao. Esconder esse caso em SQL seria repetir o silencio que esta fase
-- elimina em Java; o operador tem de ver o erro e decidir o que fazer aos dados.

-- 1. Retirar o valor por omissao. A coluna mantem-se NOT NULL: quem grava passa a
--    ter de nomear o purpose, que e precisamente o requisito.
DO $$ BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 't_paa_submission_period'
          AND column_name = 'purpose' AND column_default IS NOT NULL
    ) THEN
        ALTER TABLE t_paa_submission_period ALTER COLUMN purpose DROP DEFAULT;
    END IF;
END $$;

-- 2. Restricao CHECK contra os seis codigos literais do enum Purpose
--    (sigdi/application/constants/Purpose.java, posicoes 1-6). PostgreSQL nao tem
--    ADD CONSTRAINT IF NOT EXISTS -- a guarda usa pg_constraint pelo conname, para
--    que a migracao seja reexecutavel sem repetir a criacao.
DO $$ BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_paa_submission_period_purpose'
    ) THEN
        ALTER TABLE t_paa_submission_period
            ADD CONSTRAINT chk_paa_submission_period_purpose
            CHECK (purpose IN (
                'PAA_BSC_OBJECTIVES',
                'PAA',
                'SIADAP',
                'SIADAP_INTERIM',
                'SIADAP_SELF_EVAL',
                'SIADAP_FINAL'
            ));
    END IF;
END $$;

-- Sombra Envers: audit_schema.t_paa_submission_period_aud NAO leva CHECK e nao e
-- alterada por esta migracao. Precedente escrito na V32: uma linha historica do
-- Envers tem de sobreviver a remocao da linha que referencia, e restringir a
-- sombra impediria o proprio registo de auditoria que ela existe para preservar.
-- A V19 ja declara a coluna da sombra como purpose VARCHAR(20) sem DEFAULT,
-- portanto nao ha DEFAULT nenhum a retirar-lhe aqui.
