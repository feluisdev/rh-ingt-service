-- =============================================================
-- V57 — horário da unidade orgânica
--
-- Assiduidade, primeiro passo (Lei n.º 20/X/2023, arts. 164.º a 166.º). Cada
-- colaborador trabalha num horário do catálogo da instituição (t_horario, tabela
-- nova, criada pelo ddl-auto). Quem não tiver um atribuído segue o horário da
-- unidade orgânica onde exerce funções, ou o da unidade-mãe mais próxima que o
-- tenha — a unidade de topo faz de horário da instituição.
--
-- Esta migração só acrescenta a coluna à unidade orgânica, que é tabela que já
-- existe. Nula quer dizer «o da unidade-mãe». Sem FK: t_horario nasce pelo
-- Hibernate, depois do Flyway, e numa base vazia ainda não existe aqui. A
-- existência e o estado do horário validam-se na aplicação.
--
-- t_unidade_organica também nasce pelo Hibernate numa base vazia (a V7 só lhe
-- tira uma coluna), por isso o guarda to_regclass ANINHADO (ver V49 e V55).
-- Defensiva e idempotente.
-- =============================================================

DO $$ BEGIN
    IF to_regclass('public.t_unidade_organica') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_unidade_organica'
                         AND column_name='horario_id') THEN
            ALTER TABLE t_unidade_organica ADD COLUMN horario_id UUID;
        END IF;
    END IF;
END $$;

DO $$ BEGIN
    IF to_regclass('audit_schema.t_unidade_organica_aud') IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_unidade_organica_aud'
                         AND column_name='horario_id') THEN
            ALTER TABLE audit_schema.t_unidade_organica_aud ADD COLUMN horario_id UUID;
        END IF;
    END IF;
END $$;
