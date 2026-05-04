-- Create SIGDI Base Tables (Missing in existing migrations)

-- 1. t_institutions
CREATE TABLE IF NOT EXISTS t_institutions (
    id UUID NOT NULL,
    code VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    type VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    deactivated_at TIMESTAMP WITH TIME ZONE,
    contact_email VARCHAR(255),
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    last_modified_date TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by VARCHAR(255),
    CONSTRAINT pk_institutions PRIMARY KEY (id),
    CONSTRAINT uq_institutions_code UNIQUE (code)
);

-- 2. t_institutional_identity
CREATE TABLE IF NOT EXISTS t_institutional_identity (
    id UUID NOT NULL,
    institution_id UUID,
    cycle_year INTEGER NOT NULL,
    mission TEXT NOT NULL,
    vision TEXT NOT NULL,
    values_json TEXT,
    version_comment VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    last_modified_date TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by VARCHAR(255),
    CONSTRAINT pk_institutional_identity PRIMARY KEY (id)
);

-- 3. t_strategic_goals
CREATE TABLE IF NOT EXISTS t_strategic_goals (
    id UUID NOT NULL,
    institution_id UUID,
    identity_id UUID,
    parent_goal_id UUID,
    title VARCHAR(255) NOT NULL,
    perspective VARCHAR(255),
    weight DECIMAL(19,2),
    status VARCHAR(255),
    description TEXT,
    position_x DOUBLE PRECISION,
    position_y DOUBLE PRECISION,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    last_modified_date TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by VARCHAR(255),
    CONSTRAINT pk_strategic_goals PRIMARY KEY (id)
);

-- 4. t_tactical_activities
CREATE TABLE IF NOT EXISTS t_tactical_activities (
    id UUID NOT NULL,
    strategic_goal_id UUID NOT NULL,
    institution_id UUID,
    organic_unit_id UUID,
    title VARCHAR(255),
    description_what TEXT,
    justification_why TEXT,
    location_where VARCHAR(255),
    responsible_who UUID,
    methodology_how TEXT,
    start_date DATE,
    end_date DATE,
    budget_estimated DECIMAL(19,2),
    budget_committed DECIMAL(19,2),
    budget_liquidated DECIMAL(19,2),
    budget_paid DECIMAL(19,2),
    fiscal_year INTEGER,
    economic_classifier VARCHAR(255),
    status VARCHAR(255),
    version INTEGER,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    last_modified_date TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by VARCHAR(255),
    CONSTRAINT pk_tactical_activities PRIMARY KEY (id)
);

-- 5. t_key_results
CREATE TABLE IF NOT EXISTS t_key_results (
    id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    institution_id UUID,
    activity_id UUID,
    okr_id UUID,
    target_value DECIMAL(19,2),
    current_value DECIMAL(19,2),
    metric_unit VARCHAR(255),
    weight DECIMAL(19,2),
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    last_modified_date TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by VARCHAR(255),
    CONSTRAINT pk_key_results PRIMARY KEY (id)
);
