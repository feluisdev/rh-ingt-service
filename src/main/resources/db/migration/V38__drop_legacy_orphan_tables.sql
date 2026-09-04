-- =============================================================
-- V38 — DROP de tabelas legadas órfãs (sem entity JPA, sem uso)
-- -------------------------------------------------------------
-- Todas estas tabelas foram materializadas por Hibernate ddl-auto a partir de
-- entities que já não existem (ou nunca chegaram a ser usadas). Nenhuma é criada
-- por qualquer migração Flyway e nenhuma entity aponta para elas após o refactor
-- Position Management (Blocos A/B). Dump de segurança dos dados:
--   scripts/cleanup/backup_legacy_before_drop_2026-09-02.sql
--
--   t_employee_professional_assignments  <- ex-EnquadramentoEntity  (Bloco B)
--   t_employee_unit_assignments          <- ex-ColocacaoEntity      (Bloco B)
--   t_contrato_entity                    <- duplicado de t_contrato (FK -> t_cargo)
--   t_cargo                              <- legado, substituído por t_job
--   t_documento                          <- legado, substituído por t_document
--   t_professional_situation             <- catálogo dormente, sem entity/FK
--
-- Idempotente e defensiva: DROP ... IF EXISTS CASCADE. Numa BD nova (Flyway do
-- zero) estas tabelas não existem — a migração é um no-op.
-- =============================================================

-- Ordem: t_contrato_entity antes de t_cargo (FK). CASCADE cobre qualquer resíduo.
DROP TABLE IF EXISTS t_contrato_entity                     CASCADE;
DROP TABLE IF EXISTS t_cargo                               CASCADE;
DROP TABLE IF EXISTS t_documento                           CASCADE;
DROP TABLE IF EXISTS t_professional_situation              CASCADE;
DROP TABLE IF EXISTS t_employee_professional_assignments   CASCADE;
DROP TABLE IF EXISTS t_employee_unit_assignments           CASCADE;

-- Tabelas de auditoria Envers correspondentes (audit_schema).
DROP TABLE IF EXISTS audit_schema.t_employee_professional_assignments_aud CASCADE;
DROP TABLE IF EXISTS audit_schema.t_employee_unit_assignments_aud         CASCADE;

DO $$ BEGIN
    RAISE NOTICE 'V38: tabelas legadas órfãs largadas (ou inexistentes) — schema limpo.';
END $$;
