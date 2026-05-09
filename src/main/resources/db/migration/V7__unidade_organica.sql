-- =============================================================
-- V7 — t_unidade_organica
-- Entity: id, code, name, acronym, type, descricao, estado,
--         parent_unit_id, is_active, [audit]
-- DROP: unit_type_option_id (legado — substituído por type VARCHAR)
-- =============================================================

-- Remover coluna legada sem mapeamento no entity
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_unidade_organica' AND column_name='unit_type_option_id') THEN
        ALTER TABLE t_unidade_organica DROP COLUMN unit_type_option_id;
    END IF;
END $$;
