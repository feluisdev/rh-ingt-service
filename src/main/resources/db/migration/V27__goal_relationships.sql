-- Migration: V27__goal_relationships.sql
-- Formalizes `t_goal_relationships` (backing StrategyMapLinkEntity), a table that today
-- exists only via ddl-auto=update in dev/staging and is genuinely absent in production
-- (ddl-auto=validate never creates tables; grepping V1-V26 confirms zero references) --
-- closing RELA-01. This is a from-scratch baseline reproducing the exact shape ddl-auto
-- already generates -- no structural change (CONTEXT.md: "sem alterar a estrutura, só
-- formalizá-la").
--
-- Column/constraint fidelity notes:
-- * source_goal_id/target_goal_id are NOT declared NOT NULL: the entity's @JoinColumns
--   (StrategyMapLinkEntity.java) omit nullable=false, so JPA's spec default (nullable=true)
--   is what ddl-auto actually generates today. The @NotNull on the entity fields is a Bean
--   Validation annotation, not a DDL directive, and this project has a documented precedent
--   of Bean Validation not being reliably enforced (WR-01) -- so it must not be read as a
--   DDL contract. Tightening this would be a structural change, out of this phase's scope.
-- * REFERENCES t_strategic_goals(id) IS included on both goal-id columns: unlike V25's
--   institution_id (a bare @Column, FK omitted there to avoid ordering risk), these are real
--   @ManyToOne @JoinColumn relations, so Hibernate ddl-auto generates real FK constraints.
--   t_strategic_goals is created by V25 (< V27), so Flyway's strict ordering guarantees it
--   already exists -- no ordering risk here.
-- * uq_strategy_link_source_target mirrors the entity's own @UniqueConstraint exactly.
--
-- Defensive idempotent-guard pattern mirrors V19/V25/V26: ddl-auto=update is active in
-- development/staging (application-development.properties, application-staging.properties),
-- so a locally-booted app that creates this @Entity before this migration runs could
-- otherwise race Flyway and leave the audit shadow table missing.

CREATE TABLE IF NOT EXISTS t_goal_relationships (
    id                  UUID PRIMARY KEY,
    institution_id      UUID,
    source_goal_id      UUID REFERENCES t_strategic_goals(id),
    target_goal_id      UUID REFERENCES t_strategic_goals(id),
    relationship_type   VARCHAR(255),
    created_date        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(255) NOT NULL DEFAULT 'system',
    last_modified_date  TIMESTAMP,
    last_modified_by    VARCHAR(255),
    CONSTRAINT uq_strategy_link_source_target UNIQUE (source_goal_id, target_goal_id)
);

-- Envers audit shadow table, mirrors V25/V26's `_aud` convention: business columns
-- un-constrained, no foreign keys (Envers historical rows must remain even if a
-- referenced goal is later deleted).
CREATE TABLE IF NOT EXISTS audit_schema.t_goal_relationships_aud (
    id                  UUID         NOT NULL,
    rev                 INTEGER      NOT NULL,
    revtype             SMALLINT,
    institution_id      UUID,
    source_goal_id      UUID,
    target_goal_id      UUID,
    relationship_type   VARCHAR(255),
    created_date        TIMESTAMP,
    created_by          VARCHAR(255),
    last_modified_date  TIMESTAMP,
    last_modified_by    VARCHAR(255),
    PRIMARY KEY (id, rev)
);
