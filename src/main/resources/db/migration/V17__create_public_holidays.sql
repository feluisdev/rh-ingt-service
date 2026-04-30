CREATE TABLE IF NOT EXISTS t_public_holiday (
    id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    holiday_date DATE NOT NULL,
    is_national BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_date TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    last_modified_date TIMESTAMP,
    last_modified_by VARCHAR(255),
    CONSTRAINT pk_public_holiday PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_public_holiday_national_date
    ON t_public_holiday (holiday_date)
    WHERE is_national = TRUE AND is_active = TRUE;

CREATE INDEX IF NOT EXISTS idx_public_holiday_date ON t_public_holiday (holiday_date);
CREATE INDEX IF NOT EXISTS idx_public_holiday_active ON t_public_holiday (is_active);
