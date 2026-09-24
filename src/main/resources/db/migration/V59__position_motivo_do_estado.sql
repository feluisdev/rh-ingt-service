-- =============================================================
-- V59 — motivo do estado do Lugar (congelar e descongelar)
--
-- Congelar um Lugar tira-o da dotação: deixa de poder ser ocupado. É um acto
-- administrativo (falta de dotação orçamental, reestruturação) e passa a exigir
-- motivo; descongelar também. Guarda-se o último: o motivo, o despacho e a data em
-- que o Lugar passou ao estado actual. O histórico completo fica na auditoria
-- (Envers), que versiona a linha a cada mudança.
--
-- Um Lugar que não pode voltar não se congela: extingue-se (EXTINTO é terminal).
--
-- Só acrescenta colunas nulas a uma tabela que já existe (os Lugares antigos ficam
-- sem motivo, que é a verdade: ninguém o registou). t_position nasce pelo Hibernate
-- numa base vazia, por isso o guarda to_regclass ANINHADO (ver V49, V55 e V57), e a
-- tabela de auditoria recebe as mesmas colunas. Defensiva e idempotente: pode correr
-- mais do que uma vez sem efeito.
-- =============================================================

DO $$ BEGIN
    IF to_regclass('public.t_position') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_position'
                         AND column_name='estado_motivo') THEN
            ALTER TABLE t_position ADD COLUMN estado_motivo VARCHAR(500);
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_position'
                         AND column_name='estado_despacho') THEN
            ALTER TABLE t_position ADD COLUMN estado_despacho VARCHAR(120);
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_position'
                         AND column_name='estado_desde') THEN
            ALTER TABLE t_position ADD COLUMN estado_desde DATE;
        END IF;
    END IF;
END $$;

DO $$ BEGIN
    IF to_regclass('audit_schema.t_position_aud') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_position_aud'
                         AND column_name='estado_motivo') THEN
            ALTER TABLE audit_schema.t_position_aud ADD COLUMN estado_motivo VARCHAR(500);
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_position_aud'
                         AND column_name='estado_despacho') THEN
            ALTER TABLE audit_schema.t_position_aud ADD COLUMN estado_despacho VARCHAR(120);
        END IF;
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_position_aud'
                         AND column_name='estado_desde') THEN
            ALTER TABLE audit_schema.t_position_aud ADD COLUMN estado_desde DATE;
        END IF;
    END IF;
END $$;
