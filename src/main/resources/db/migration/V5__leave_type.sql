-- =============================================================
-- V5 — t_leave_type
-- Entity: id, code, description, deducts_balance, requires_approval,
--         max_days_per_year, category, is_active, [audit]
-- DROP: category_option_id (legado — entity usa category VARCHAR)
-- =============================================================

CREATE TABLE IF NOT EXISTS t_leave_type (
    id                  UUID            NOT NULL,
    code                VARCHAR(255)    NOT NULL,
    description         VARCHAR(255),
    deducts_balance     BOOLEAN,
    requires_approval   BOOLEAN,
    max_days_per_year   INTEGER,
    category            VARCHAR(50),
    is_active           BOOLEAN,
    created_date        TIMESTAMP       NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(255)    NOT NULL DEFAULT 'system',
    last_modified_date  TIMESTAMP,
    last_modified_by    VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uq_leave_type_code UNIQUE (code)
);

-- Colunas que podem estar em falta
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_leave_type' AND column_name='max_days_per_year') THEN
        ALTER TABLE t_leave_type ADD COLUMN max_days_per_year INTEGER;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_leave_type' AND column_name='category') THEN
        ALTER TABLE t_leave_type ADD COLUMN category VARCHAR(50);
    END IF;
END $$;

-- Remover NOT NULL indevidos
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_leave_type' AND column_name='deducts_balance' AND is_nullable='NO') THEN
        ALTER TABLE t_leave_type ALTER COLUMN deducts_balance DROP NOT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_leave_type' AND column_name='requires_approval' AND is_nullable='NO') THEN
        ALTER TABLE t_leave_type ALTER COLUMN requires_approval DROP NOT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_leave_type' AND column_name='is_active' AND is_nullable='NO') THEN
        ALTER TABLE t_leave_type ALTER COLUMN is_active DROP NOT NULL;
    END IF;
END $$;

-- Remover colunas legadas sem mapeamento no entity
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_leave_type' AND column_name='category_option_id') THEN
        ALTER TABLE t_leave_type DROP COLUMN category_option_id;
    END IF;
END $$;

-- Tabela de auditoria
CREATE TABLE IF NOT EXISTS audit_schema.t_leave_type_aud (
    id                  UUID        NOT NULL,
    rev                 INTEGER     NOT NULL,
    revtype             SMALLINT,
    code                VARCHAR(255),
    description         VARCHAR(255),
    deducts_balance     BOOLEAN,
    requires_approval   BOOLEAN,
    max_days_per_year   INTEGER,
    category            VARCHAR(50),
    is_active           BOOLEAN,
    created_date        TIMESTAMP,
    created_by          VARCHAR(255),
    last_modified_date  TIMESTAMP,
    last_modified_by    VARCHAR(255),
    PRIMARY KEY (id, rev)
);

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_leave_type_aud' AND column_name='max_days_per_year') THEN
        ALTER TABLE audit_schema.t_leave_type_aud ADD COLUMN max_days_per_year INTEGER;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_leave_type_aud' AND column_name='category') THEN
        ALTER TABLE audit_schema.t_leave_type_aud ADD COLUMN category VARCHAR(50);
    END IF;
END $$;
