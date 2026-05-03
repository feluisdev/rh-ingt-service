-- The t_cargo table pre-existed from the funcionarios module. V20's CREATE TABLE IF NOT EXISTS was
-- skipped, and Hibernate ddl-auto=update added spurious columns (code, name, description, is_active)
-- that conflict with the old CargoEntity (which requires 'nome' NOT NULL). This migration removes
-- those columns and creates the proper t_job table for the estrutura module.

-- Remove spurious columns added to t_cargo by Hibernate (from JobEntity before table rename)
ALTER TABLE t_cargo DROP CONSTRAINT IF EXISTS uk1bcawodnv81vutui7f46m3ns8;
ALTER TABLE t_cargo DROP COLUMN IF EXISTS code;
ALTER TABLE t_cargo DROP COLUMN IF EXISTS name;
ALTER TABLE t_cargo DROP COLUMN IF EXISTS description;
ALTER TABLE t_cargo DROP COLUMN IF EXISTS is_active;

-- Create proper job table for the estrutura module
CREATE TABLE IF NOT EXISTS t_job (
    id                  UUID                         NOT NULL,
    code                VARCHAR(50)                  NOT NULL,
    name                VARCHAR(200)                 NOT NULL,
    description         TEXT,
    is_active           BOOLEAN                      NOT NULL DEFAULT TRUE,
    created_date        TIMESTAMP WITHOUT TIME ZONE  NOT NULL,
    created_by          VARCHAR(255)                 NOT NULL,
    last_modified_date  TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by    VARCHAR(255),
    CONSTRAINT pk_job PRIMARY KEY (id),
    CONSTRAINT uq_job_code UNIQUE (code)
);
