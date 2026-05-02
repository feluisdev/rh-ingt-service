-- Módulo de Processos Disciplinares — BC Colaboradores
CREATE TABLE IF NOT EXISTS t_disciplinary_process (
    id                  UUID            NOT NULL,
    funcionario_id      UUID            NOT NULL,
    process_number      VARCHAR(100),
    start_date          DATE            NOT NULL,
    end_date            DATE,
    penalty             VARCHAR(255),
    penalty_start_date  DATE,
    penalty_end_date    DATE,
    official_bulletin   VARCHAR(100),
    notes               TEXT,
    document_id         UUID,
    created_date        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by          VARCHAR(255)    NOT NULL,
    last_modified_date  TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by    VARCHAR(255),
    CONSTRAINT pk_disciplinary_process PRIMARY KEY (id),
    CONSTRAINT fk_dp_funcionario FOREIGN KEY (funcionario_id) REFERENCES t_funcionario(id)
);

CREATE INDEX IF NOT EXISTS idx_disciplinary_process_funcionario
    ON t_disciplinary_process (funcionario_id);
