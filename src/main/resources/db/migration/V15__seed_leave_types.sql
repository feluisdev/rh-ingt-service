-- Tipos de ausência conforme Lei do Trabalho e regime da função pública de Cabo Verde
INSERT INTO t_leave_type (id, code, description, deducts_balance, requires_approval, max_days_per_year, category_option_id, is_active) VALUES
    (gen_random_uuid(), 'FERIAS_ANUAIS',     'Férias Anuais',            FALSE, TRUE,  22,
     (SELECT id FROM t_option_entity WHERE ccode='LEAVE_CATEGORY' AND ckey='ANNUAL' LIMIT 1),   TRUE),
    (gen_random_uuid(), 'BAIXA_MEDICA',      'Baixa Médica',             FALSE, TRUE,  NULL,
     (SELECT id FROM t_option_entity WHERE ccode='LEAVE_CATEGORY' AND ckey='MEDICAL' LIMIT 1),  TRUE),
    (gen_random_uuid(), 'LIC_MATERNIDADE',   'Licença de Maternidade',   FALSE, TRUE,  60,
     (SELECT id FROM t_option_entity WHERE ccode='LEAVE_CATEGORY' AND ckey='MATERNITY' LIMIT 1),TRUE),
    (gen_random_uuid(), 'LIC_PATERNIDADE',   'Licença de Paternidade',   FALSE, TRUE,  5,
     (SELECT id FROM t_option_entity WHERE ccode='LEAVE_CATEGORY' AND ckey='OTHER' LIMIT 1),    TRUE),
    (gen_random_uuid(), 'LUTO',              'Licença por Luto',         FALSE, TRUE,  3,
     (SELECT id FROM t_option_entity WHERE ccode='LEAVE_CATEGORY' AND ckey='OTHER' LIMIT 1),    TRUE),
    (gen_random_uuid(), 'SEM_VENCIMENTO',    'Licença sem Vencimento',   TRUE,  TRUE,  NULL,
     (SELECT id FROM t_option_entity WHERE ccode='LEAVE_CATEGORY' AND ckey='OTHER' LIMIT 1),    TRUE)
ON CONFLICT (code) DO NOTHING;
