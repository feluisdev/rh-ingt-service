-- Módulo de Ausências — BC Colaboradores
-- t_leave_type, t_leave_mobility_subtype, t_public_holiday já existem desde V8, V9 e V17.
-- Este script cria apenas as 3 tabelas novas do workflow de ausências.

-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS t_leave_request (
    id                      UUID            NOT NULL,
    funcionario_id          UUID            NOT NULL,
    tipo_ausencia_id        UUID            NOT NULL,
    data_inicio             DATE            NOT NULL,
    data_fim                DATE            NOT NULL,
    numero_dias             INTEGER         NOT NULL,
    motivo                  TEXT,
    estado                  VARCHAR(20)     NOT NULL DEFAULT 'PENDENTE',
    aprovado_por            UUID,
    data_decisao            DATE,
    observacoes_decisao     TEXT,
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE,
    created_date            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by              VARCHAR(255)    NOT NULL,
    last_modified_date      TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by        VARCHAR(255),
    CONSTRAINT pk_leave_request PRIMARY KEY (id),
    CONSTRAINT fk_leave_request_funcionario FOREIGN KEY (funcionario_id) REFERENCES t_funcionario(id),
    CONSTRAINT fk_leave_request_tipo FOREIGN KEY (tipo_ausencia_id) REFERENCES t_leave_type(id),
    CONSTRAINT fk_leave_request_aprovado_por FOREIGN KEY (aprovado_por) REFERENCES t_funcionario(id)
);

CREATE INDEX IF NOT EXISTS idx_leave_request_funcionario_estado
    ON t_leave_request (funcionario_id, estado);
CREATE INDEX IF NOT EXISTS idx_leave_request_overlap
    ON t_leave_request (funcionario_id, data_inicio, data_fim);

-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS t_leave_balance (
    id                      UUID            NOT NULL,
    funcionario_id          UUID            NOT NULL,
    tipo_ausencia_id        UUID            NOT NULL,
    ano                     INTEGER         NOT NULL,
    dias_direito            INTEGER         NOT NULL DEFAULT 0,
    dias_gozados            INTEGER         NOT NULL DEFAULT 0,
    dias_pendentes          INTEGER         NOT NULL DEFAULT 0,
    created_date            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by              VARCHAR(255)    NOT NULL,
    last_modified_date      TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by        VARCHAR(255),
    CONSTRAINT pk_leave_balance PRIMARY KEY (id),
    CONSTRAINT fk_leave_balance_funcionario FOREIGN KEY (funcionario_id) REFERENCES t_funcionario(id),
    CONSTRAINT fk_leave_balance_tipo FOREIGN KEY (tipo_ausencia_id) REFERENCES t_leave_type(id),
    CONSTRAINT uq_leave_balance_func_tipo_ano UNIQUE (funcionario_id, tipo_ausencia_id, ano)
);

CREATE INDEX IF NOT EXISTS idx_leave_balance_funcionario ON t_leave_balance (funcionario_id, ano);

-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS t_leave_mobility (
    id                      UUID            NOT NULL,
    funcionario_id          UUID            NOT NULL,
    subtipo_id              UUID            NOT NULL,
    data_inicio             DATE            NOT NULL,
    data_fim                DATE,
    entidade_destino        VARCHAR(200),
    despacho_numero         VARCHAR(100),
    observacoes             TEXT,
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE,
    created_date            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by              VARCHAR(255)    NOT NULL,
    last_modified_date      TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by        VARCHAR(255),
    CONSTRAINT pk_leave_mobility PRIMARY KEY (id),
    CONSTRAINT fk_leave_mobility_funcionario FOREIGN KEY (funcionario_id) REFERENCES t_funcionario(id),
    CONSTRAINT fk_leave_mobility_subtipo FOREIGN KEY (subtipo_id) REFERENCES t_leave_mobility_subtype(id)
);

CREATE INDEX IF NOT EXISTS idx_leave_mobility_funcionario_active
    ON t_leave_mobility (funcionario_id, is_active);
