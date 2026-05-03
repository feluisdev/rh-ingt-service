-- Adiciona tipos de unidade orgânica em falta (DIVISAO e SECCAO)
-- MINISTRY, DIRECTION, DEPARTMENT, SERVICE já existem em V10
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active) VALUES
    (gen_random_uuid(), 'UNIT_TYPE', 'DIVISION', 'Divisão', 'pt-CV', 5, true),
    (gen_random_uuid(), 'UNIT_TYPE', 'SECTION',  'Secção',  'pt-CV', 6, true)
ON CONFLICT (ccode, ckey, locale) DO NOTHING;
