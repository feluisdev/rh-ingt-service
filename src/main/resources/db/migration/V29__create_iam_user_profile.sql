-- IAM User Profile — sincronização de claims JWT Keycloak para base de dados local
-- Liga o utilizador Keycloak (sub) ao funcionário RH (funcionario_id) via email

CREATE TABLE IF NOT EXISTS t_iam_user_profile (
    id                  UUID            NOT NULL,
    sub                 VARCHAR(255)    NOT NULL,
    username            VARCHAR(255)    NOT NULL,
    email               VARCHAR(255),
    first_name          VARCHAR(100),
    last_name           VARCHAR(100),
    full_name           VARCHAR(255),
    funcionario_id      UUID,
    created_date        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by          VARCHAR(255)    NOT NULL,
    last_modified_date  TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by    VARCHAR(255),
    CONSTRAINT pk_iam_user_profile      PRIMARY KEY (id),
    CONSTRAINT uq_iam_user_profile_sub  UNIQUE (sub),
    CONSTRAINT uq_iam_user_profile_user UNIQUE (username),
    CONSTRAINT fk_iam_user_profile_func FOREIGN KEY (funcionario_id) REFERENCES t_funcionario(id)
);

CREATE INDEX IF NOT EXISTS idx_iam_user_profile_sub
    ON t_iam_user_profile (sub);

CREATE INDEX IF NOT EXISTS idx_iam_user_profile_email
    ON t_iam_user_profile (email);

CREATE INDEX IF NOT EXISTS idx_iam_user_profile_funcionario
    ON t_iam_user_profile (funcionario_id);
