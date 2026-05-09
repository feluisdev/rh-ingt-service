-- =============================================================
-- V4 — t_tipo_documento
-- Entity: id, descricao, codigo, allowed_extensions, category, is_active, [audit]
-- DROP: estado (legado), category_option_id (legado — entity usa category VARCHAR)
-- =============================================================

CREATE TABLE IF NOT EXISTS t_tipo_documento (
    id                  UUID            NOT NULL,
    descricao           VARCHAR(255),
    codigo              VARCHAR(255),
    allowed_extensions  VARCHAR(200),
    category            VARCHAR(50),
    is_active           BOOLEAN,
    created_date        TIMESTAMP       NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(255)    NOT NULL DEFAULT 'system',
    last_modified_date  TIMESTAMP,
    last_modified_by    VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uq_tipo_documento_codigo UNIQUE (codigo)
);

-- Colunas que podem estar em falta
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_tipo_documento' AND column_name='allowed_extensions') THEN
        ALTER TABLE t_tipo_documento ADD COLUMN allowed_extensions VARCHAR(200);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_tipo_documento' AND column_name='category') THEN
        ALTER TABLE t_tipo_documento ADD COLUMN category VARCHAR(50);
    END IF;
END $$;

-- Remover NOT NULL indevidos
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_tipo_documento' AND column_name='is_active' AND is_nullable='NO') THEN
        ALTER TABLE t_tipo_documento ALTER COLUMN is_active DROP NOT NULL;
    END IF;
END $$;

-- Remover colunas legadas sem mapeamento no entity
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_tipo_documento' AND column_name='estado') THEN
        ALTER TABLE t_tipo_documento DROP COLUMN estado;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_tipo_documento' AND column_name='category_option_id') THEN
        ALTER TABLE t_tipo_documento DROP COLUMN category_option_id;
    END IF;
END $$;

-- Tabela de auditoria
CREATE TABLE IF NOT EXISTS audit_schema.t_tipo_documento_aud (
    id                  UUID        NOT NULL,
    rev                 INTEGER     NOT NULL,
    revtype             SMALLINT,
    descricao           VARCHAR(255),
    codigo              VARCHAR(255),
    allowed_extensions  VARCHAR(200),
    category            VARCHAR(50),
    is_active           BOOLEAN,
    created_date        TIMESTAMP,
    created_by          VARCHAR(255),
    last_modified_date  TIMESTAMP,
    last_modified_by    VARCHAR(255),
    PRIMARY KEY (id, rev)
);

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_tipo_documento_aud' AND column_name='allowed_extensions') THEN
        ALTER TABLE audit_schema.t_tipo_documento_aud ADD COLUMN allowed_extensions VARCHAR(200);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_tipo_documento_aud' AND column_name='category') THEN
        ALTER TABLE audit_schema.t_tipo_documento_aud ADD COLUMN category VARCHAR(50);
    END IF;
END $$;
