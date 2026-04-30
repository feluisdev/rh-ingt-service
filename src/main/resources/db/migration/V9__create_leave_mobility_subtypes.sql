CREATE TABLE IF NOT EXISTS t_leave_mobility_subtype (
    id                    UUID         NOT NULL,
    code                  VARCHAR(50)  NOT NULL,
    description           VARCHAR(255),
    record_type           VARCHAR(20)  NOT NULL DEFAULT 'LICENCA',
    affects_pay           BOOLEAN      NOT NULL DEFAULT FALSE,
    counts_for_seniority  BOOLEAN      NOT NULL DEFAULT TRUE,
    can_self_submit       BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_date          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by            VARCHAR(255) NOT NULL DEFAULT 'system',
    last_modified_date    TIMESTAMP WITH TIME ZONE,
    last_modified_by      VARCHAR(255),
    CONSTRAINT pk_leave_mobility_subtype PRIMARY KEY (id),
    CONSTRAINT uq_leave_mobility_code UNIQUE (code),
    CONSTRAINT chk_record_type CHECK (record_type IN ('LICENCA', 'MOBILIDADE', 'AMBOS'))
);
