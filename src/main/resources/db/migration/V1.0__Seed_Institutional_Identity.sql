-- Seed script for Institutional Identity (Flyway Migration)

-- 1. Ensure the Institution exists (UUID used in QA/Staging)
INSERT INTO t_institutions (id, code, name, type, is_active, created_date, created_by)
VALUES ('00000000-0000-0000-0000-000000000001', 'SIGPROG_RH', 'SIPPROG', 'INTERNAL', true, CURRENT_TIMESTAMP, 'system')
ON CONFLICT (id) DO NOTHING;


