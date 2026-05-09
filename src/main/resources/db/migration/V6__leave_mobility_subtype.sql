-- =============================================================
-- V6 — t_leave_mobility_subtype
-- Subtipos de licença/mobilidade.
-- record_type ∈ {LICENCA, MOBILIDADE, AMBOS}.
-- =============================================================

CREATE TABLE IF NOT EXISTS t_leave_mobility_subtype (
    id                      UUID            NOT NULL,
    code                    VARCHAR(255)    NOT NULL,
    name                    VARCHAR(255)    NOT NULL,
    description             VARCHAR(255),
    record_type             VARCHAR(50),
    affects_pay             BOOLEAN,
    counts_for_seniority    BOOLEAN,
    can_self_submit         BOOLEAN,
    is_active               BOOLEAN,
    created_date            TIMESTAMP       NOT NULL DEFAULT NOW(),
    created_by              VARCHAR(255)    NOT NULL DEFAULT 'system',
    last_modified_date      TIMESTAMP,
    last_modified_by        VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uq_leave_mobility_subtype_code UNIQUE (code)
);

-- Remover NOT NULL indevidos
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_leave_mobility_subtype' AND column_name='record_type' AND is_nullable='NO') THEN
        ALTER TABLE t_leave_mobility_subtype ALTER COLUMN record_type DROP NOT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_leave_mobility_subtype' AND column_name='affects_pay' AND is_nullable='NO') THEN
        ALTER TABLE t_leave_mobility_subtype ALTER COLUMN affects_pay DROP NOT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_leave_mobility_subtype' AND column_name='counts_for_seniority' AND is_nullable='NO') THEN
        ALTER TABLE t_leave_mobility_subtype ALTER COLUMN counts_for_seniority DROP NOT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_leave_mobility_subtype' AND column_name='can_self_submit' AND is_nullable='NO') THEN
        ALTER TABLE t_leave_mobility_subtype ALTER COLUMN can_self_submit DROP NOT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_leave_mobility_subtype' AND column_name='is_active' AND is_nullable='NO') THEN
        ALTER TABLE t_leave_mobility_subtype ALTER COLUMN is_active DROP NOT NULL;
    END IF;
END $$;

-- Tabela de auditoria
CREATE TABLE IF NOT EXISTS audit_schema.t_leave_mobility_subtype_aud (
    id                      UUID        NOT NULL,
    rev                     INTEGER     NOT NULL,
    revtype                 SMALLINT,
    code                    VARCHAR(255),
    name                    VARCHAR(255),
    description             VARCHAR(255),
    record_type             VARCHAR(50),
    affects_pay             BOOLEAN,
    counts_for_seniority    BOOLEAN,
    can_self_submit         BOOLEAN,
    is_active               BOOLEAN,
    created_date            TIMESTAMP,
    created_by              VARCHAR(255),
    last_modified_date      TIMESTAMP,
    last_modified_by        VARCHAR(255),
    PRIMARY KEY (id, rev)
);
