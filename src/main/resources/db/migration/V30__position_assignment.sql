-- =============================================================
-- V30 — Position Management (Mapa de Pessoal)
--   t_position  (Lugar)      — camada estrutura/
--   t_assignment (Afectação) — camada colaboradores/ (funde
--                              enquadramento + colocação)
-- Defensivo + idempotente (padrão V8): CREATE IF NOT EXISTS +
-- ADD COLUMN IF NOT EXISTS + índices IF NOT EXISTS + _aud.
-- SEM FK de BD (convenção do projecto: integridade na app).
-- SEM dados (backfill vive em scripts/, fora do Flyway).
-- =============================================================

-- -------------------------------------------------------------
-- t_position (Lugar)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_position (
    id                   UUID PRIMARY KEY,
    numero_lugar         VARCHAR(60)  NOT NULL,
    job_id               UUID         NOT NULL,
    unidade_organica_id  UUID         NOT NULL,
    career_id            UUID,
    category_id          UUID,
    parent_position_id   UUID,
    manages_unit_id      UUID,
    estado               VARCHAR(20)  NOT NULL DEFAULT 'ATIVO',
    legal_base           VARCHAR(255),
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_date         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by           VARCHAR(255) NOT NULL DEFAULT 'system',
    last_modified_date   TIMESTAMP,
    last_modified_by     VARCHAR(255)
);

-- Colunas que o entity usa e que podem não existir ainda (tabela pré-existente)
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_position' AND column_name='numero_lugar') THEN
        ALTER TABLE t_position ADD COLUMN numero_lugar VARCHAR(60);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_position' AND column_name='job_id') THEN
        ALTER TABLE t_position ADD COLUMN job_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_position' AND column_name='unidade_organica_id') THEN
        ALTER TABLE t_position ADD COLUMN unidade_organica_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_position' AND column_name='career_id') THEN
        ALTER TABLE t_position ADD COLUMN career_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_position' AND column_name='category_id') THEN
        ALTER TABLE t_position ADD COLUMN category_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_position' AND column_name='parent_position_id') THEN
        ALTER TABLE t_position ADD COLUMN parent_position_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_position' AND column_name='manages_unit_id') THEN
        ALTER TABLE t_position ADD COLUMN manages_unit_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_position' AND column_name='estado') THEN
        ALTER TABLE t_position ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'ATIVO';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_position' AND column_name='legal_base') THEN
        ALTER TABLE t_position ADD COLUMN legal_base VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_position' AND column_name='is_active') THEN
        ALTER TABLE t_position ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
    END IF;
END $$;

-- Índices únicos (idempotentes)
CREATE UNIQUE INDEX IF NOT EXISTS ux_position_numero_lugar
    ON t_position (numero_lugar);
-- Um só responsável (Lugar gestor) por unidade
CREATE UNIQUE INDEX IF NOT EXISTS ux_position_manages_unit
    ON t_position (manages_unit_id)
    WHERE manages_unit_id IS NOT NULL AND is_active = TRUE;

-- Auditoria Envers
CREATE TABLE IF NOT EXISTS audit_schema.t_position_aud (
    id       UUID    NOT NULL,
    rev      INTEGER NOT NULL,
    revtype  SMALLINT,
    PRIMARY KEY (id, rev)
);
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_position_aud' AND column_name='numero_lugar') THEN
        ALTER TABLE audit_schema.t_position_aud ADD COLUMN numero_lugar VARCHAR(60);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_position_aud' AND column_name='job_id') THEN
        ALTER TABLE audit_schema.t_position_aud ADD COLUMN job_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_position_aud' AND column_name='unidade_organica_id') THEN
        ALTER TABLE audit_schema.t_position_aud ADD COLUMN unidade_organica_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_position_aud' AND column_name='career_id') THEN
        ALTER TABLE audit_schema.t_position_aud ADD COLUMN career_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_position_aud' AND column_name='category_id') THEN
        ALTER TABLE audit_schema.t_position_aud ADD COLUMN category_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_position_aud' AND column_name='parent_position_id') THEN
        ALTER TABLE audit_schema.t_position_aud ADD COLUMN parent_position_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_position_aud' AND column_name='manages_unit_id') THEN
        ALTER TABLE audit_schema.t_position_aud ADD COLUMN manages_unit_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_position_aud' AND column_name='estado') THEN
        ALTER TABLE audit_schema.t_position_aud ADD COLUMN estado VARCHAR(20);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_position_aud' AND column_name='legal_base') THEN
        ALTER TABLE audit_schema.t_position_aud ADD COLUMN legal_base VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_position_aud' AND column_name='is_active') THEN
        ALTER TABLE audit_schema.t_position_aud ADD COLUMN is_active BOOLEAN;
    END IF;
END $$;

-- -------------------------------------------------------------
-- t_assignment (Afectação)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_assignment (
    id                    UUID PRIMARY KEY,
    funcionario_id        UUID         NOT NULL,
    position_id           UUID         NOT NULL,
    grade_id              UUID,
    function_id           UUID,
    assignment_type       VARCHAR(20)  NOT NULL DEFAULT 'PRINCIPAL',
    origem                VARCHAR(20)  NOT NULL,
    origin_assignment_id  UUID,
    data_inicio           DATE         NOT NULL,
    data_fim              DATE,
    is_current            BOOLEAN      NOT NULL DEFAULT TRUE,
    is_active             BOOLEAN      NOT NULL DEFAULT TRUE,
    notes                 TEXT,
    created_date          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by            VARCHAR(255) NOT NULL DEFAULT 'system',
    last_modified_date    TIMESTAMP,
    last_modified_by      VARCHAR(255)
);

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_assignment' AND column_name='funcionario_id') THEN
        ALTER TABLE t_assignment ADD COLUMN funcionario_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_assignment' AND column_name='position_id') THEN
        ALTER TABLE t_assignment ADD COLUMN position_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_assignment' AND column_name='grade_id') THEN
        ALTER TABLE t_assignment ADD COLUMN grade_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_assignment' AND column_name='function_id') THEN
        ALTER TABLE t_assignment ADD COLUMN function_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_assignment' AND column_name='assignment_type') THEN
        ALTER TABLE t_assignment ADD COLUMN assignment_type VARCHAR(20) NOT NULL DEFAULT 'PRINCIPAL';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_assignment' AND column_name='origem') THEN
        ALTER TABLE t_assignment ADD COLUMN origem VARCHAR(20);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_assignment' AND column_name='origin_assignment_id') THEN
        ALTER TABLE t_assignment ADD COLUMN origin_assignment_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_assignment' AND column_name='data_inicio') THEN
        ALTER TABLE t_assignment ADD COLUMN data_inicio DATE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_assignment' AND column_name='data_fim') THEN
        ALTER TABLE t_assignment ADD COLUMN data_fim DATE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_assignment' AND column_name='is_current') THEN
        ALTER TABLE t_assignment ADD COLUMN is_current BOOLEAN NOT NULL DEFAULT TRUE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_assignment' AND column_name='is_active') THEN
        ALTER TABLE t_assignment ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_assignment' AND column_name='notes') THEN
        ALTER TABLE t_assignment ADD COLUMN notes TEXT;
    END IF;
END $$;

-- Um ocupante corrente por Lugar (uma cadeira, um ocupante)
CREATE UNIQUE INDEX IF NOT EXISTS ux_assignment_position_current
    ON t_assignment (position_id)
    WHERE is_current = TRUE;
-- Uma afectação PRINCIPAL corrente por funcionário
CREATE UNIQUE INDEX IF NOT EXISTS ux_assignment_funcionario_current_principal
    ON t_assignment (funcionario_id)
    WHERE is_current = TRUE AND assignment_type = 'PRINCIPAL';
-- Consultas correntes
CREATE INDEX IF NOT EXISTS ix_assignment_funcionario
    ON t_assignment (funcionario_id);
CREATE INDEX IF NOT EXISTS ix_assignment_position
    ON t_assignment (position_id);

-- Auditoria Envers
CREATE TABLE IF NOT EXISTS audit_schema.t_assignment_aud (
    id       UUID    NOT NULL,
    rev      INTEGER NOT NULL,
    revtype  SMALLINT,
    PRIMARY KEY (id, rev)
);
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_assignment_aud' AND column_name='funcionario_id') THEN
        ALTER TABLE audit_schema.t_assignment_aud ADD COLUMN funcionario_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_assignment_aud' AND column_name='position_id') THEN
        ALTER TABLE audit_schema.t_assignment_aud ADD COLUMN position_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_assignment_aud' AND column_name='grade_id') THEN
        ALTER TABLE audit_schema.t_assignment_aud ADD COLUMN grade_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_assignment_aud' AND column_name='function_id') THEN
        ALTER TABLE audit_schema.t_assignment_aud ADD COLUMN function_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_assignment_aud' AND column_name='assignment_type') THEN
        ALTER TABLE audit_schema.t_assignment_aud ADD COLUMN assignment_type VARCHAR(20);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_assignment_aud' AND column_name='origem') THEN
        ALTER TABLE audit_schema.t_assignment_aud ADD COLUMN origem VARCHAR(20);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_assignment_aud' AND column_name='origin_assignment_id') THEN
        ALTER TABLE audit_schema.t_assignment_aud ADD COLUMN origin_assignment_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_assignment_aud' AND column_name='data_inicio') THEN
        ALTER TABLE audit_schema.t_assignment_aud ADD COLUMN data_inicio DATE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_assignment_aud' AND column_name='data_fim') THEN
        ALTER TABLE audit_schema.t_assignment_aud ADD COLUMN data_fim DATE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_assignment_aud' AND column_name='is_current') THEN
        ALTER TABLE audit_schema.t_assignment_aud ADD COLUMN is_current BOOLEAN;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_assignment_aud' AND column_name='is_active') THEN
        ALTER TABLE audit_schema.t_assignment_aud ADD COLUMN is_active BOOLEAN;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_assignment_aud' AND column_name='notes') THEN
        ALTER TABLE audit_schema.t_assignment_aud ADD COLUMN notes TEXT;
    END IF;
END $$;
