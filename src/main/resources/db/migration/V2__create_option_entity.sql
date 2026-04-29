-- Catálogo genérico de etiquetas parametrizáveis (option_entity)
CREATE TABLE IF NOT EXISTS t_option_entity (
    id            UUID         NOT NULL,
    ccode         VARCHAR(50)  NOT NULL,
    ckey          VARCHAR(100) NOT NULL,
    cvalue        VARCHAR(255) NOT NULL,
    locale        VARCHAR(10)  NOT NULL DEFAULT 'pt-CV',
    sort_order    INTEGER      NOT NULL DEFAULT 0,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    description   TEXT,
    created_at    TIMESTAMP WITH TIME ZONE,
    created_by    VARCHAR(255),
    updated_at    TIMESTAMP WITH TIME ZONE,
    updated_by    VARCHAR(255),
    CONSTRAINT pk_option_entity PRIMARY KEY (id),
    CONSTRAINT uq_option_ccode_ckey_locale UNIQUE (ccode, ckey, locale)
);

CREATE INDEX IF NOT EXISTS idx_option_ccode_locale_active
    ON t_option_entity (ccode, locale, active);
