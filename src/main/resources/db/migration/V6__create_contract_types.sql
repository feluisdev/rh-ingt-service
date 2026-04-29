CREATE TABLE IF NOT EXISTS t_contract_type (
    id                 UUID         NOT NULL,
    code               VARCHAR(50)  NOT NULL,
    description        VARCHAR(255),
    is_active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_date       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by         VARCHAR(255) NOT NULL DEFAULT 'system',
    last_modified_date TIMESTAMP WITH TIME ZONE,
    last_modified_by   VARCHAR(255),
    CONSTRAINT pk_contract_type PRIMARY KEY (id),
    CONSTRAINT uq_contract_type_code UNIQUE (code)
);
