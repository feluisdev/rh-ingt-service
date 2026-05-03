-- Módulo de Recibos de Vencimento — BC Colaboradores
CREATE TABLE IF NOT EXISTS t_payroll_slip (
    id                  UUID            NOT NULL,
    funcionario_id      UUID            NOT NULL,
    period_month        SMALLINT        NOT NULL,
    period_year         INTEGER         NOT NULL,
    issue_date          DATE            NOT NULL,
    gross_salary        NUMERIC(12,2)   NOT NULL,
    net_salary          NUMERIC(12,2)   NOT NULL,
    document_id         UUID            NOT NULL,
    created_date        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by          VARCHAR(255)    NOT NULL,
    last_modified_date  TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by    VARCHAR(255),
    CONSTRAINT pk_payroll_slip        PRIMARY KEY (id),
    CONSTRAINT fk_ps_funcionario      FOREIGN KEY (funcionario_id) REFERENCES t_funcionario(id),
    CONSTRAINT uq_payroll_slip_period UNIQUE (funcionario_id, period_month, period_year),
    CONSTRAINT chk_ps_month           CHECK (period_month BETWEEN 1 AND 12),
    CONSTRAINT chk_ps_gross           CHECK (gross_salary > 0),
    CONSTRAINT chk_ps_net             CHECK (net_salary > 0 AND net_salary <= gross_salary)
);

CREATE INDEX IF NOT EXISTS idx_payroll_slip_funcionario
    ON t_payroll_slip (funcionario_id);

CREATE INDEX IF NOT EXISTS idx_payroll_slip_period
    ON t_payroll_slip (funcionario_id, period_year, period_month);
