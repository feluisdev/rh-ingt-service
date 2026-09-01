-- =============================================================
-- clean_movimentos.sql
-- Reset dos dados de MOVIMENTOS para arranque limpo do
-- Position Management (sem backfill).
--
-- Idempotente e defensivo: só toca no que existe; seguro para
-- correr várias vezes. NÃO mexe em funcionarios/contratos.
--
-- Correr:
--   docker exec -i postgres-ingt-rh psql -U postgres \
--     -d recursoshumanos_db < scripts/cleanup/clean_movimentos.sql
--
-- Noutro ambiente: mesma coisa com as credenciais desse ambiente.
-- =============================================================

DO $$
BEGIN
    -- Modelo novo
    IF to_regclass('public.t_assignment') IS NOT NULL THEN
        TRUNCATE TABLE t_assignment RESTART IDENTITY CASCADE;
        RAISE NOTICE 't_assignment limpa.';
    END IF;
    IF to_regclass('public.t_position') IS NOT NULL THEN
        TRUNCATE TABLE t_position RESTART IDENTITY CASCADE;
        RAISE NOTICE 't_position limpa.';
    END IF;

    -- Modelo antigo (deprecado, mantido como rede de segurança)
    IF to_regclass('public.t_employee_professional_assignments') IS NOT NULL THEN
        TRUNCATE TABLE t_employee_professional_assignments CASCADE;
        RAISE NOTICE 't_employee_professional_assignments limpa.';
    END IF;
    IF to_regclass('public.t_employee_unit_assignments') IS NOT NULL THEN
        TRUNCATE TABLE t_employee_unit_assignments CASCADE;
        RAISE NOTICE 't_employee_unit_assignments limpa.';
    END IF;

    -- Auditoria correspondente (opcional — histórico de auditoria dos movimentos)
    IF to_regclass('audit_schema.t_assignment_aud') IS NOT NULL THEN
        TRUNCATE TABLE audit_schema.t_assignment_aud;
    END IF;
    IF to_regclass('audit_schema.t_position_aud') IS NOT NULL THEN
        TRUNCATE TABLE audit_schema.t_position_aud;
    END IF;

    RAISE NOTICE 'Movimentos limpos — pronto para arranque Position Management.';
END $$;
