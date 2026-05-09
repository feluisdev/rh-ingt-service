-- =============================================================
-- V12 — t_qualificacao
-- Entity: id, funcionario_id, level, course_name, institution,
--         country, start_date, end_date, completed, is_active, [audit]
-- DROP NOT NULL: nivel_academico, curso, pais, completed (algumas serão apagadas)
-- DROP (legados): nivel_academico, curso, instituicao, ano_conclusao, pais,
--                 carga_horaria, data_conclusao, data_inicio, estado, nivel,
--                 notafinal, situacao, id_funcionario
-- =============================================================

-- Remover NOT NULL antes de apagar colunas legadas
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_qualificacao' AND column_name='nivel_academico' AND is_nullable='NO') THEN
        ALTER TABLE t_qualificacao ALTER COLUMN nivel_academico DROP NOT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_qualificacao' AND column_name='curso' AND is_nullable='NO') THEN
        ALTER TABLE t_qualificacao ALTER COLUMN curso DROP NOT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_qualificacao' AND column_name='pais' AND is_nullable='NO') THEN
        ALTER TABLE t_qualificacao ALTER COLUMN pais DROP NOT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_qualificacao' AND column_name='completed' AND is_nullable='NO') THEN
        ALTER TABLE t_qualificacao ALTER COLUMN completed DROP NOT NULL;
    END IF;
END $$;

-- Remover colunas legadas sem mapeamento no entity
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_qualificacao' AND column_name='nivel_academico') THEN
        ALTER TABLE t_qualificacao DROP COLUMN nivel_academico;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_qualificacao' AND column_name='curso') THEN
        ALTER TABLE t_qualificacao DROP COLUMN curso;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_qualificacao' AND column_name='instituicao') THEN
        ALTER TABLE t_qualificacao DROP COLUMN instituicao;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_qualificacao' AND column_name='ano_conclusao') THEN
        ALTER TABLE t_qualificacao DROP COLUMN ano_conclusao;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_qualificacao' AND column_name='pais') THEN
        ALTER TABLE t_qualificacao DROP COLUMN pais;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_qualificacao' AND column_name='carga_horaria') THEN
        ALTER TABLE t_qualificacao DROP COLUMN carga_horaria;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_qualificacao' AND column_name='data_conclusao') THEN
        ALTER TABLE t_qualificacao DROP COLUMN data_conclusao;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_qualificacao' AND column_name='data_inicio') THEN
        ALTER TABLE t_qualificacao DROP COLUMN data_inicio;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_qualificacao' AND column_name='estado') THEN
        ALTER TABLE t_qualificacao DROP COLUMN estado;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_qualificacao' AND column_name='nivel') THEN
        ALTER TABLE t_qualificacao DROP COLUMN nivel;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_qualificacao' AND column_name='notafinal') THEN
        ALTER TABLE t_qualificacao DROP COLUMN notafinal;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_qualificacao' AND column_name='situacao') THEN
        ALTER TABLE t_qualificacao DROP COLUMN situacao;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_qualificacao' AND column_name='id_funcionario') THEN
        ALTER TABLE t_qualificacao DROP COLUMN id_funcionario;
    END IF;
END $$;

-- Auditoria
CREATE TABLE IF NOT EXISTS audit_schema.t_qualificacao_aud (
    id                  UUID        NOT NULL,
    rev                 INTEGER     NOT NULL,
    revtype             SMALLINT,
    PRIMARY KEY (id, rev)
);
