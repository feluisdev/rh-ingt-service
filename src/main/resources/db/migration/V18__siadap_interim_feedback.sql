-- Migration: V18__siadap_interim_feedback.sql
-- Creates the four tables required for the SIADAP Interim Feedback (Ficha de Feedback Intercalar)
-- linked to an existing evaluation in t_siadap_evaluations.

-- Main table for the interim feedback record
CREATE TABLE IF NOT EXISTS t_siadap_interim_feedback (
    evaluation_id         UUID PRIMARY KEY,
    objectives_synthesis  TEXT,
    observed_facts_star   TEXT,
    difficulties_obstacles TEXT,
    feedback_and_action   TEXT,
    created_by            VARCHAR(255),
    created_date          TIMESTAMP,
    last_modified_by      VARCHAR(255),
    last_modified_date    TIMESTAMP
);

-- Audit table (Envers)
CREATE TABLE IF NOT EXISTS t_siadap_interim_feedback_aud (
    evaluation_id         UUID NOT NULL,
    rev                   INTEGER NOT NULL,
    revtype               SMALLINT,
    objectives_synthesis  TEXT,
    observed_facts_star   TEXT,
    difficulties_obstacles TEXT,
    feedback_and_action   TEXT,
    created_by            VARCHAR(255),
    created_date          TIMESTAMP,
    last_modified_by      VARCHAR(255),
    last_modified_date    TIMESTAMP,
    PRIMARY KEY (evaluation_id, rev)
);

-- Table for behavioral competency observations
CREATE TABLE IF NOT EXISTS t_siadap_interim_competency_observations (
    id               UUID PRIMARY KEY,
    evaluation_id    UUID NOT NULL,
    competency_code  VARCHAR(100) NOT NULL,
    observed_evidence TEXT,
    created_by       VARCHAR(255),
    created_date     TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS t_siadap_interim_competency_observations_aud (
    id               UUID NOT NULL,
    rev              INTEGER NOT NULL,
    revtype          SMALLINT,
    evaluation_id    UUID,
    competency_code  VARCHAR(100),
    observed_evidence TEXT,
    created_by       VARCHAR(255),
    created_date     TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP,
    PRIMARY KEY (id, rev)
);

-- Table for improvement actions
CREATE TABLE IF NOT EXISTS t_siadap_interim_actions (
    id               UUID PRIMARY KEY,
    evaluation_id    UUID NOT NULL,
    action_agreed    TEXT,
    responsible      VARCHAR(200),
    deadline         VARCHAR(100),
    needed_support   TEXT,
    created_by       VARCHAR(255),
    created_date     TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS t_siadap_interim_actions_aud (
    id               UUID NOT NULL,
    rev              INTEGER NOT NULL,
    revtype          SMALLINT,
    evaluation_id    UUID,
    action_agreed    TEXT,
    responsible      VARCHAR(200),
    deadline         VARCHAR(100),
    needed_support   TEXT,
    created_by       VARCHAR(255),
    created_date     TIMESTAMP,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP,
    PRIMARY KEY (id, rev)
);

-- Table for objective revisions
CREATE TABLE IF NOT EXISTS t_siadap_interim_objective_revisions (
    id                       UUID PRIMARY KEY,
    evaluation_id            UUID NOT NULL,
    current_objective_text   TEXT,
    revision_justification   TEXT,
    new_objective_smart      TEXT,
    approval_status          VARCHAR(50),
    created_by               VARCHAR(255),
    created_date             TIMESTAMP,
    last_modified_by         VARCHAR(255),
    last_modified_date       TIMESTAMP
);

CREATE TABLE IF NOT EXISTS t_siadap_interim_objective_revisions_aud (
    id                       UUID NOT NULL,
    rev                      INTEGER NOT NULL,
    revtype                  SMALLINT,
    evaluation_id            UUID,
    current_objective_text   TEXT,
    revision_justification   TEXT,
    new_objective_smart      TEXT,
    approval_status          VARCHAR(50),
    created_by               VARCHAR(255),
    created_date             TIMESTAMP,
    last_modified_by         VARCHAR(255),
    last_modified_date       TIMESTAMP,
    PRIMARY KEY (id, rev)
);
