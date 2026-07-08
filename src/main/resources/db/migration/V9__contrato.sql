-- =============================================================
-- V9 — t_contrato
-- Entity: id, funcionario_id, contract_type_id, contract_number,
--         start_date, end_date, termination_reason, is_current, status,
--         renewal_count, regime_trabalho, percentagem_tempo,
--         legal_base, notes, [audit]
-- DROP NOT NULL: is_current (entity tem como nullable)
-- DROP (legados): tipo_contrato, data_inicio, data_fim, numero_contrato, is_active
-- =============================================================
-- =============================================================

CREATE TABLE IF NOT EXISTS t_contrato (
    id UUID PRIMARY KEY,
    funcionario_id UUID NOT NULL,
    contract_type_id UUID,
    contract_number VARCHAR(100) UNIQUE,
    start_date DATE,
    end_date DATE,
    termination_reason VARCHAR(50),
    is_current BOOLEAN,
    status VARCHAR(20),
    renewal_count INTEGER,
    regime_trabalho VARCHAR(30),
    percentagem_tempo NUMERIC(5,2),
    legal_base VARCHAR(200),
    notes TEXT,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    last_modified_date TIMESTAMP,
    last_modified_by VARCHAR(255)
);

-- Remover NOT NULL de is_current (entity não impõe)
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_contrato' AND column_name='is_current' AND is_nullable='NO') THEN
        ALTER TABLE t_contrato ALTER COLUMN is_current DROP NOT NULL;
    END IF;
END $$;

-- Remover NOT NULL antes de apagar colunas (PostgreSQL remove coluna independentemente,
-- mas fazemos DROP NOT NULL primeiro para maior compatibilidade)
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_contrato' AND column_name='tipo_contrato' AND is_nullable='NO') THEN
        ALTER TABLE t_contrato ALTER COLUMN tipo_contrato DROP NOT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_contrato' AND column_name='data_inicio' AND is_nullable='NO') THEN
        ALTER TABLE t_contrato ALTER COLUMN data_inicio DROP NOT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_contrato' AND column_name='is_active' AND is_nullable='NO') THEN
        ALTER TABLE t_contrato ALTER COLUMN is_active DROP NOT NULL;
    END IF;
END $$;

-- Remover colunas legadas sem mapeamento no entity
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_contrato' AND column_name='tipo_contrato') THEN
        ALTER TABLE t_contrato DROP COLUMN tipo_contrato;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_contrato' AND column_name='data_inicio') THEN
        ALTER TABLE t_contrato DROP COLUMN data_inicio;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_contrato' AND column_name='data_fim') THEN
        ALTER TABLE t_contrato DROP COLUMN data_fim;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_contrato' AND column_name='numero_contrato') THEN
        ALTER TABLE t_contrato DROP COLUMN numero_contrato;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_contrato' AND column_name='is_active') THEN
        ALTER TABLE t_contrato DROP COLUMN is_active;
    END IF;
END $$;

-- Auditoria
CREATE TABLE IF NOT EXISTS audit_schema.t_contrato_aud (
    id                  UUID        NOT NULL,
    rev                 INTEGER     NOT NULL,
    revtype             SMALLINT,
    PRIMARY KEY (id, rev)
);
