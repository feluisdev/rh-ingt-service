CREATE TABLE IF NOT EXISTS t_unidade_organica (
    id                  UUID         NOT NULL,
    code                VARCHAR(50)  NOT NULL,
    name                VARCHAR(200) NOT NULL,
    acronym             VARCHAR(20)  NOT NULL,
    unit_type_option_id UUID         NOT NULL,
    parent_unit_id      UUID         REFERENCES t_unidade_organica(id),
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    CONSTRAINT pk_unidade_organica PRIMARY KEY (id),
    CONSTRAINT uq_unidade_organica_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS t_cargo (
    id          UUID         NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    CONSTRAINT pk_cargo PRIMARY KEY (id),
    CONSTRAINT uq_cargo_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS t_funcao (
    id          UUID         NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP,
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255),
    CONSTRAINT pk_funcao PRIMARY KEY (id),
    CONSTRAINT uq_funcao_code UNIQUE (code)
);
