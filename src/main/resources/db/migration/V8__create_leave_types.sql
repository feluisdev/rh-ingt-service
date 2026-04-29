CREATE TABLE IF NOT EXISTS t_leave_type (
    id                   UUID         NOT NULL,
    code                 VARCHAR(50)  NOT NULL,
    description          VARCHAR(255),
    deducts_balance      BOOLEAN      NOT NULL DEFAULT TRUE,
    requires_approval    BOOLEAN      NOT NULL DEFAULT TRUE,
    max_days_per_year    INTEGER,
    category_option_id   UUID,
    is_active            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_date         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by           VARCHAR(255) NOT NULL DEFAULT 'system',
    last_modified_date   TIMESTAMP WITH TIME ZONE,
    last_modified_by     VARCHAR(255),
    CONSTRAINT pk_leave_type PRIMARY KEY (id),
    CONSTRAINT uq_leave_type_code UNIQUE (code),
    CONSTRAINT fk_leave_type_category
        FOREIGN KEY (category_option_id) REFERENCES t_option_entity(id)
        ON DELETE SET NULL
);
