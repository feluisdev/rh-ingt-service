-- Drop old v3 tables that have incompatible schemas with v4.
-- These were created by Hibernate ddl-auto and use different column names/structure.
DROP TABLE IF EXISTS t_qualificacao CASCADE;
DROP TABLE IF EXISTS t_dependente CASCADE;
DROP TABLE IF EXISTS t_funcionario CASCADE;

CREATE SEQUENCE IF NOT EXISTS seq_numero_funcionario
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    CACHE 1;

CREATE TABLE t_funcionario (
    id                      UUID            NOT NULL,
    numero_funcionario      VARCHAR(10)     NOT NULL,
    nome_completo           VARCHAR(200)    NOT NULL,
    data_nascimento         DATE            NOT NULL,
    genero                  VARCHAR(50)     NOT NULL,
    estado_civil            VARCHAR(50)     NOT NULL,
    nif                     VARCHAR(20)     NOT NULL,
    bi_numero               VARCHAR(50)     NOT NULL,
    bi_validade             DATE,
    nacionalidade           VARCHAR(50)     NOT NULL DEFAULT 'CV',
    email                   VARCHAR(200),
    telefone                VARCHAR(30),
    morada                  TEXT,
    foto_url                TEXT,
    situacao_profissional   VARCHAR(50)     NOT NULL,
    data_admissao           DATE            NOT NULL,
    data_saida              DATE,
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE,
    created_date            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by              VARCHAR(255)    NOT NULL,
    last_modified_date      TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by        VARCHAR(255),
    CONSTRAINT pk_funcionario PRIMARY KEY (id),
    CONSTRAINT uq_funcionario_numero UNIQUE (numero_funcionario),
    CONSTRAINT uq_funcionario_nif UNIQUE (nif),
    CONSTRAINT uq_funcionario_bi UNIQUE (bi_numero),
    CONSTRAINT uq_funcionario_email UNIQUE (email)
);

CREATE INDEX idx_funcionario_is_active ON t_funcionario (is_active);
CREATE INDEX idx_funcionario_situacao ON t_funcionario (situacao_profissional);
CREATE INDEX idx_funcionario_nif ON t_funcionario (nif);
CREATE INDEX idx_funcionario_bi ON t_funcionario (bi_numero);

CREATE TABLE IF NOT EXISTS employee_professional_assignments (
    id                  UUID    NOT NULL,
    funcionario_id      UUID    NOT NULL,
    career_id           UUID    NOT NULL,
    category_id         UUID    NOT NULL,
    grade_id            UUID    NOT NULL,
    cargo_id            UUID    NOT NULL,
    unidade_organica_id UUID    NOT NULL,
    data_inicio         DATE    NOT NULL,
    data_fim            DATE,
    is_current          BOOLEAN NOT NULL DEFAULT FALSE,
    created_date        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by          VARCHAR(255) NOT NULL,
    last_modified_date  TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by    VARCHAR(255),
    CONSTRAINT pk_enquadramento PRIMARY KEY (id),
    CONSTRAINT fk_enquadramento_funcionario FOREIGN KEY (funcionario_id) REFERENCES t_funcionario(id),
    CONSTRAINT fk_enquadramento_career FOREIGN KEY (career_id) REFERENCES t_career(id),
    CONSTRAINT fk_enquadramento_category FOREIGN KEY (category_id) REFERENCES t_category(id),
    CONSTRAINT fk_enquadramento_grade FOREIGN KEY (grade_id) REFERENCES t_grade(id),
    CONSTRAINT fk_enquadramento_cargo FOREIGN KEY (cargo_id) REFERENCES t_cargo(id),
    CONSTRAINT fk_enquadramento_unidade FOREIGN KEY (unidade_organica_id) REFERENCES t_unidade_organica(id)
);

CREATE INDEX IF NOT EXISTS idx_enquadramento_funcionario ON employee_professional_assignments (funcionario_id);
CREATE INDEX IF NOT EXISTS idx_enquadramento_is_current ON employee_professional_assignments (funcionario_id, is_current);

CREATE TABLE IF NOT EXISTS t_contrato (
    id                  UUID            NOT NULL,
    funcionario_id      UUID            NOT NULL,
    tipo_contrato       VARCHAR(50)     NOT NULL,
    data_inicio         DATE            NOT NULL,
    data_fim            DATE,
    numero_contrato     VARCHAR(100),
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_date        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by          VARCHAR(255)    NOT NULL,
    last_modified_date  TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by    VARCHAR(255),
    CONSTRAINT pk_contrato PRIMARY KEY (id),
    CONSTRAINT fk_contrato_funcionario FOREIGN KEY (funcionario_id) REFERENCES t_funcionario(id),
    CONSTRAINT uq_contrato_numero UNIQUE (numero_contrato)
);

CREATE INDEX IF NOT EXISTS idx_contrato_funcionario ON t_contrato (funcionario_id);

CREATE TABLE t_dependente (
    id                  UUID            NOT NULL,
    funcionario_id      UUID            NOT NULL,
    nome                VARCHAR(150)    NOT NULL,
    parentesco          VARCHAR(50)     NOT NULL,
    data_nascimento     DATE,
    nif                 VARCHAR(20),
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_date        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by          VARCHAR(255)    NOT NULL,
    last_modified_date  TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by    VARCHAR(255),
    CONSTRAINT pk_dependente PRIMARY KEY (id),
    CONSTRAINT fk_dependente_funcionario FOREIGN KEY (funcionario_id) REFERENCES t_funcionario(id)
);

CREATE INDEX idx_dependente_funcionario ON t_dependente (funcionario_id);

CREATE TABLE t_qualificacao (
    id                  UUID            NOT NULL,
    funcionario_id      UUID            NOT NULL,
    nivel_academico     VARCHAR(50)     NOT NULL,
    curso               VARCHAR(200)    NOT NULL,
    instituicao         VARCHAR(200),
    ano_conclusao       INTEGER,
    pais                VARCHAR(50)     NOT NULL DEFAULT 'CV',
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_date        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by          VARCHAR(255)    NOT NULL,
    last_modified_date  TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by    VARCHAR(255),
    CONSTRAINT pk_qualificacao PRIMARY KEY (id),
    CONSTRAINT fk_qualificacao_funcionario FOREIGN KEY (funcionario_id) REFERENCES t_funcionario(id)
);

CREATE INDEX idx_qualificacao_funcionario ON t_qualificacao (funcionario_id);
