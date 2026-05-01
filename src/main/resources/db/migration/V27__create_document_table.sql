-- Módulo de Documentos — BC Colaboradores
-- Tabela polimórfica para documentos associados a qualquer entidade do sistema.
-- Nesta versão: apenas FUNCIONARIO como reference_entity.

CREATE TABLE IF NOT EXISTS t_document (
    id                      UUID            NOT NULL,
    reference_entity        VARCHAR(50)     NOT NULL,
    reference_id            UUID            NOT NULL,
    document_type_id        UUID            NOT NULL,
    file_key                VARCHAR(500)    NOT NULL,
    original_filename       VARCHAR(255)    NOT NULL,
    content_type            VARCHAR(100)    NOT NULL,
    file_size               BIGINT          NOT NULL,
    description             TEXT,
    is_active               BOOLEAN         NOT NULL DEFAULT TRUE,
    created_date            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by              VARCHAR(255)    NOT NULL,
    last_modified_date      TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by        VARCHAR(255),
    CONSTRAINT pk_document PRIMARY KEY (id),
    CONSTRAINT fk_document_type FOREIGN KEY (document_type_id) REFERENCES t_tipo_documento(id)
);

CREATE INDEX IF NOT EXISTS idx_document_reference
    ON t_document (reference_entity, reference_id, is_active);

CREATE INDEX IF NOT EXISTS idx_document_type
    ON t_document (document_type_id);
