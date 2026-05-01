-- Módulo de Colocações — BC Colaboradores
-- Historial de afectações de funcionários a unidades orgânicas.

CREATE TABLE IF NOT EXISTS employee_unit_assignments (
    id                  UUID            NOT NULL,
    funcionario_id      UUID            NOT NULL,
    unit_id             UUID,
    job_id              UUID,
    start_date          DATE            NOT NULL,
    end_date            DATE,
    is_current          BOOLEAN         NOT NULL DEFAULT FALSE,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    assignment_type     VARCHAR(50)     NOT NULL,
    notes               TEXT,
    created_date        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by          VARCHAR(255)    NOT NULL,
    last_modified_date  TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by    VARCHAR(255),
    CONSTRAINT pk_employee_unit_assignments PRIMARY KEY (id),
    CONSTRAINT fk_eua_funcionario FOREIGN KEY (funcionario_id) REFERENCES t_funcionario(id),
    CONSTRAINT fk_eua_unit        FOREIGN KEY (unit_id)         REFERENCES t_unidade_organica(id),
    CONSTRAINT fk_eua_job         FOREIGN KEY (job_id)          REFERENCES t_cargo(id)
);

CREATE INDEX IF NOT EXISTS idx_eua_funcionario_current
    ON employee_unit_assignments (funcionario_id, is_current)
    WHERE is_active = TRUE;

CREATE INDEX IF NOT EXISTS idx_eua_funcionario_history
    ON employee_unit_assignments (funcionario_id, start_date DESC);
