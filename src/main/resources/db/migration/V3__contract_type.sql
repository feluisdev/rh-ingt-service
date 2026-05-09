-- =============================================================
-- V3 — t_contract_type
-- Entity: id, code, description, vinculo_laboral_id, is_renewable,
--         max_renewals, max_duration_months, requires_career_structure,
--         is_active, [audit]
-- DROP: professional_situation_id (legado, sem mapeamento no entity)
-- =============================================================

CREATE TABLE IF NOT EXISTS t_contract_type (
    id                          UUID            NOT NULL,
    code                        VARCHAR(255)    NOT NULL,
    description                 VARCHAR(255),
    vinculo_laboral_id          UUID,
    is_renewable                BOOLEAN,
    max_renewals                INTEGER,
    max_duration_months         INTEGER,
    requires_career_structure   BOOLEAN,
    is_active                   BOOLEAN,
    created_date                TIMESTAMP       NOT NULL DEFAULT NOW(),
    created_by                  VARCHAR(255)    NOT NULL DEFAULT 'system',
    last_modified_date          TIMESTAMP,
    last_modified_by            VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uq_contract_type_code UNIQUE (code)
);

-- Colunas que podem estar em falta
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_contract_type' AND column_name='requires_career_structure') THEN
        ALTER TABLE t_contract_type ADD COLUMN requires_career_structure BOOLEAN;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_contract_type' AND column_name='vinculo_laboral_id') THEN
        ALTER TABLE t_contract_type ADD COLUMN vinculo_laboral_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_contract_type' AND column_name='max_renewals') THEN
        ALTER TABLE t_contract_type ADD COLUMN max_renewals INTEGER;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_contract_type' AND column_name='max_duration_months') THEN
        ALTER TABLE t_contract_type ADD COLUMN max_duration_months INTEGER;
    END IF;
END $$;

-- Remover NOT NULL indevidos
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_contract_type' AND column_name='is_active' AND is_nullable='NO') THEN
        ALTER TABLE t_contract_type ALTER COLUMN is_active DROP NOT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_contract_type' AND column_name='is_renewable' AND is_nullable='NO') THEN
        ALTER TABLE t_contract_type ALTER COLUMN is_renewable DROP NOT NULL;
    END IF;
END $$;

-- Remover colunas legadas sem mapeamento no entity
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_contract_type' AND column_name='professional_situation_id') THEN
        ALTER TABLE t_contract_type DROP COLUMN professional_situation_id;
    END IF;
END $$;

-- Tabela de auditoria
CREATE TABLE IF NOT EXISTS audit_schema.t_contract_type_aud (
    id                          UUID        NOT NULL,
    rev                         INTEGER     NOT NULL,
    revtype                     SMALLINT,
    code                        VARCHAR(255),
    description                 VARCHAR(255),
    vinculo_laboral_id          UUID,
    is_renewable                BOOLEAN,
    max_renewals                INTEGER,
    max_duration_months         INTEGER,
    requires_career_structure   BOOLEAN,
    is_active                   BOOLEAN,
    created_date                TIMESTAMP,
    created_by                  VARCHAR(255),
    last_modified_date          TIMESTAMP,
    last_modified_by            VARCHAR(255),
    PRIMARY KEY (id, rev)
);

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_contract_type_aud' AND column_name='requires_career_structure') THEN
        ALTER TABLE audit_schema.t_contract_type_aud ADD COLUMN requires_career_structure BOOLEAN;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_contract_type_aud' AND column_name='vinculo_laboral_id') THEN
        ALTER TABLE audit_schema.t_contract_type_aud ADD COLUMN vinculo_laboral_id UUID;
    END IF;
END $$;
