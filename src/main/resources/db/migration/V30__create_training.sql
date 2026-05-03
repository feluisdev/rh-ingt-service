-- Módulo de Formações Profissionais — BC Colaboradores
CREATE TABLE IF NOT EXISTS t_training (
    id                  UUID            NOT NULL,
    funcionario_id      UUID            NOT NULL,
    name                VARCHAR(255)    NOT NULL,
    institution         VARCHAR(255),
    type_option_key     VARCHAR(100),
    start_date          DATE,
    end_date            DATE,
    duration_hours      INTEGER,
    document_id         UUID,
    created_date        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by          VARCHAR(255)    NOT NULL,
    last_modified_date  TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by    VARCHAR(255),
    CONSTRAINT pk_training PRIMARY KEY (id),
    CONSTRAINT fk_training_funcionario FOREIGN KEY (funcionario_id) REFERENCES t_funcionario(id)
);

CREATE INDEX IF NOT EXISTS idx_training_funcionario
    ON t_training (funcionario_id);

CREATE INDEX IF NOT EXISTS idx_training_year
    ON t_training (funcionario_id, EXTRACT(YEAR FROM start_date));

INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active) VALUES
    (gen_random_uuid(), 'TRAINING_TYPE', 'PRESENCIAL',  'Presencial',  'pt-CV', 1, true),
    (gen_random_uuid(), 'TRAINING_TYPE', 'ELEARNING',   'E-Learning',  'pt-CV', 2, true),
    (gen_random_uuid(), 'TRAINING_TYPE', 'SEMINARIO',   'Seminário',   'pt-CV', 3, true),
    (gen_random_uuid(), 'TRAINING_TYPE', 'CONGRESSO',   'Congresso',   'pt-CV', 4, true)
ON CONFLICT (ccode, ckey, locale) DO NOTHING;
