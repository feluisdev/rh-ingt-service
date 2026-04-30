CREATE TABLE IF NOT EXISTS t_worker_state (
    id                 UUID         NOT NULL,
    code               VARCHAR(50)  NOT NULL,
    description        VARCHAR(255),
    is_core            BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_date       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by         VARCHAR(255) NOT NULL DEFAULT 'system',
    last_modified_date TIMESTAMP WITH TIME ZONE,
    last_modified_by   VARCHAR(255),
    CONSTRAINT pk_worker_state PRIMARY KEY (id),
    CONSTRAINT uq_worker_state_code UNIQUE (code)
);
