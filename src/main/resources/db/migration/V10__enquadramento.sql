-- =============================================================
-- V10 — t_employee_professional_assignments (enquadramento)
-- Histórico de enquadramentos profissionais.
-- career_id, category_id, grade_id: nullable para suportar
-- trabalhadores sem estrutura de carreira (tarefeiros, prestadores).
-- =============================================================
-- =============================================================

CREATE TABLE IF NOT EXISTS t_employee_professional_assignments (
    id UUID PRIMARY KEY,
    funcionario_id UUID NOT NULL,
    career_id UUID,
    category_id UUID,
    grade_id UUID,
    cargo_id UUID NOT NULL,
    function_id UUID,
    unidade_organica_id UUID NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE,
    is_current BOOLEAN NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    last_modified_date TIMESTAMP,
    last_modified_by VARCHAR(255)
);

-- Remover NOT NULL indevidos
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_employee_professional_assignments' AND column_name='career_id' AND is_nullable='NO') THEN
        ALTER TABLE t_employee_professional_assignments ALTER COLUMN career_id DROP NOT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_employee_professional_assignments' AND column_name='category_id' AND is_nullable='NO') THEN
        ALTER TABLE t_employee_professional_assignments ALTER COLUMN category_id DROP NOT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_employee_professional_assignments' AND column_name='grade_id' AND is_nullable='NO') THEN
        ALTER TABLE t_employee_professional_assignments ALTER COLUMN grade_id DROP NOT NULL;
    END IF;
END $$;

-- Auditoria
CREATE TABLE IF NOT EXISTS audit_schema.t_employee_professional_assignments_aud (
    id                  UUID        NOT NULL,
    rev                 INTEGER     NOT NULL,
    revtype             SMALLINT,
    PRIMARY KEY (id, rev)
);
