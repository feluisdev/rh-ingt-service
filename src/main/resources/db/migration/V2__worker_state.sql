-- =============================================================
-- V2 — t_worker_state
-- Catálogo de estados do trabalhador (ATIVO, INATIVO, ...).
-- is_core=true bloqueia desactivação (estados protegidos do sistema).
-- =============================================================

CREATE TABLE IF NOT EXISTS t_worker_state (
    id                  UUID                NOT NULL,
    code                VARCHAR(255)        NOT NULL,
    description         VARCHAR(255),
    is_core             BOOLEAN,
    is_active           BOOLEAN,
    created_date        TIMESTAMP           NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(255)        NOT NULL DEFAULT 'system',
    last_modified_date  TIMESTAMP,
    last_modified_by    VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uq_worker_state_code UNIQUE (code)
);

-- Colunas que podem estar em falta
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_worker_state' AND column_name='is_core') THEN
        ALTER TABLE t_worker_state ADD COLUMN is_core BOOLEAN;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_worker_state' AND column_name='description') THEN
        ALTER TABLE t_worker_state ADD COLUMN description VARCHAR(255);
    END IF;
END $$;

-- Remover NOT NULL indevidos (entity declara estes campos como nullable)
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_worker_state' AND column_name='is_core' AND is_nullable='NO') THEN
        ALTER TABLE t_worker_state ALTER COLUMN is_core DROP NOT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_worker_state' AND column_name='is_active' AND is_nullable='NO') THEN
        ALTER TABLE t_worker_state ALTER COLUMN is_active DROP NOT NULL;
    END IF;
END $$;

-- Tabela de auditoria
CREATE TABLE IF NOT EXISTS audit_schema.t_worker_state_aud (
    id                  UUID        NOT NULL,
    rev                 INTEGER     NOT NULL,
    revtype             SMALLINT,
    code                VARCHAR(255),
    description         VARCHAR(255),
    is_core             BOOLEAN,
    is_active           BOOLEAN,
    created_date        TIMESTAMP,
    created_by          VARCHAR(255),
    last_modified_date  TIMESTAMP,
    last_modified_by    VARCHAR(255),
    PRIMARY KEY (id, rev)
);

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_worker_state_aud' AND column_name='is_core') THEN
        ALTER TABLE audit_schema.t_worker_state_aud ADD COLUMN is_core BOOLEAN;
    END IF;
END $$;
