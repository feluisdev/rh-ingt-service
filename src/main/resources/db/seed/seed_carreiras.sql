-- SEED: Carreiras e Categorias
-- Description: Career regimes, categories, and salary grades

-- Careers
INSERT INTO t_career (id, code, name, description, is_active, created_date, created_by) VALUES
('61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e601', 'REG_GERAL', 'Regime Geral', 'Carreira de Regime Geral', true, NOW(), 'system'),
('61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e602', 'REG_ESP', 'Regime Especial', 'Carreira de Regime Especial', true, NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- Categories (Technical Upper)
INSERT INTO t_category (id, career_id, code, name, description, is_active, created_date, created_by) VALUES
('71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e701', '61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e601', 'TEC_SUP', 'Técnico Superior', 'Categoria de Nível Superior', true, NOW(), 'system'),
('71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e702', '61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e601', 'ASS_TEC', 'Assistente Técnico', 'Categoria de Nível Médio', true, NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- Grades (Escalões)
INSERT INTO t_grade (id, category_id, grade_number, name, salary_index, is_active, created_date, created_by) VALUES
('81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e801', '71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e701', 1, 'Escalão 1', 100.00, true, NOW(), 'system'),
('81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e802', '71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e701', 2, 'Escalão 2', 110.00, true, NOW(), 'system'),
('81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e803', '71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e702', 1, 'Escalão 1', 80.00, true, NOW(), 'system')
ON CONFLICT (id) DO NOTHING;
