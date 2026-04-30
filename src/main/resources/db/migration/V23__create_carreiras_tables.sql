CREATE TABLE IF NOT EXISTS t_career (
    id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    last_modified_date TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by VARCHAR(255),
    CONSTRAINT pk_career PRIMARY KEY (id),
    CONSTRAINT uq_career_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS t_category (
    id UUID NOT NULL,
    career_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    last_modified_date TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by VARCHAR(255),
    CONSTRAINT pk_category PRIMARY KEY (id),
    CONSTRAINT fk_category_career FOREIGN KEY (career_id) REFERENCES t_career(id),
    CONSTRAINT uq_category_career_code UNIQUE (career_id, code)
);

CREATE TABLE IF NOT EXISTS t_grade (
    id UUID NOT NULL,
    category_id UUID NOT NULL,
    grade_number INTEGER NOT NULL,
    name VARCHAR(150) NOT NULL,
    salary_index NUMERIC(12,2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    last_modified_date TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by VARCHAR(255),
    CONSTRAINT pk_grade PRIMARY KEY (id),
    CONSTRAINT fk_grade_category FOREIGN KEY (category_id) REFERENCES t_category(id),
    CONSTRAINT uq_grade_cat_number UNIQUE (category_id, grade_number),
    CONSTRAINT chk_grade_number_pos CHECK (grade_number >= 1)
);
