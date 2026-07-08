-- =============================================================
-- V11 — t_dependente
-- Entity: id, funcionario_id, full_name, relationship_type,
--         birth_date, nif, is_active, [audit]
-- DROP NOT NULL: nome, parentesco (já serão apagadas)
-- DROP (legados): nome, parentesco, data_nascimento, cpf, estado,
--                 id_funcionario
-- =============================================================
-- =============================================================

CREATE TABLE IF NOT EXISTS t_dependente (
    id UUID PRIMARY KEY,
    funcionario_id UUID NOT NULL,
    full_name VARCHAR(200),
    relationship_type VARCHAR(50),
    birth_date DATE,
    nif VARCHAR(20),
    is_active BOOLEAN NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    last_modified_date TIMESTAMP,
    last_modified_by VARCHAR(255)
);

-- Remover NOT NULL antes de apagar colunas legadas
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_dependente' AND column_name='nome' AND is_nullable='NO') THEN
        ALTER TABLE t_dependente ALTER COLUMN nome DROP NOT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_dependente' AND column_name='parentesco' AND is_nullable='NO') THEN
        ALTER TABLE t_dependente ALTER COLUMN parentesco DROP NOT NULL;
    END IF;
END $$;

-- Remover colunas legadas sem mapeamento no entity
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_dependente' AND column_name='nome') THEN
        ALTER TABLE t_dependente DROP COLUMN nome;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_dependente' AND column_name='parentesco') THEN
        ALTER TABLE t_dependente DROP COLUMN parentesco;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_dependente' AND column_name='data_nascimento') THEN
        ALTER TABLE t_dependente DROP COLUMN data_nascimento;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_dependente' AND column_name='cpf') THEN
        ALTER TABLE t_dependente DROP COLUMN cpf;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_dependente' AND column_name='estado') THEN
        ALTER TABLE t_dependente DROP COLUMN estado;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_dependente' AND column_name='id_funcionario') THEN
        ALTER TABLE t_dependente DROP COLUMN id_funcionario;
    END IF;
END $$;

-- Auditoria
CREATE TABLE IF NOT EXISTS audit_schema.t_dependente_aud (
    id                  UUID        NOT NULL,
    rev                 INTEGER     NOT NULL,
    revtype             SMALLINT,
    PRIMARY KEY (id, rev)
);
