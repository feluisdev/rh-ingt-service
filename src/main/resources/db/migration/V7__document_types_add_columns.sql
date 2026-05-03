-- t_tipo_documento existe desde a criação original via ddl-auto=update.
-- Defensivo: CREATE TABLE IF NOT EXISTS (no-op se já existe) +
-- ADD COLUMN IF NOT EXISTS para colunas adicionadas no refactor.


CREATE TABLE IF NOT EXISTS t_tipo_documento (
    id                 UUID         NOT NULL,
    descricao          VARCHAR(255),
    codigo             VARCHAR(255),
    created_date       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by         VARCHAR(255) NOT NULL DEFAULT 'system',
    last_modified_date TIMESTAMP WITH TIME ZONE,
    last_modified_by   VARCHAR(255),
    CONSTRAINT pk_tipo_documento PRIMARY KEY (id),
    CONSTRAINT uq_tipo_documento_codigo UNIQUE (codigo)
);

ALTER TABLE t_tipo_documento
    ADD COLUMN IF NOT EXISTS allowed_extensions  VARCHAR(200),
    ADD COLUMN IF NOT EXISTS category_option_id  UUID,
    ADD COLUMN IF NOT EXISTS is_active           BOOLEAN NOT NULL DEFAULT TRUE;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_tipo_doc_category'
          AND table_name = 't_tipo_documento'
    ) THEN
        ALTER TABLE t_tipo_documento
            ADD CONSTRAINT fk_tipo_doc_category
                FOREIGN KEY (category_option_id) REFERENCES t_option_entity(id)
                ON DELETE SET NULL;
    END IF;
END $$;
