-- SEED: Carreiras e Categorias
-- Description: Career regimes, categories, and salary grades

-- Careers
INSERT INTO t_career (id, code, name, description, is_active, created_date, created_by) VALUES
('61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e601', 'REG_GERAL', 'Regime Geral', 'Carreira de Regime Geral', true, NOW(), 'system'),
('61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e602', 'REG_ESP', 'Regime Especial', 'Carreira de Regime Especial', true, NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- Categories (Technical Upper)
-- ordem_progressao diz qual e a categoria imediatamente superior. Sem ela a
-- promocao recusa sempre ("a categoria seguinte e a de ordem (nao definida)"),
-- e o caminho fica por exercitar. Assistente Tecnico (1) -> Tecnico Superior (2).
INSERT INTO t_category (id, career_id, code, name, description, ordem_progressao, is_active, created_date, created_by) VALUES
('71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e701', '61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e601', 'TEC_SUP', 'Técnico Superior', 'Categoria de Nível Superior', 2, true, NOW(), 'system'),
('71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e702', '61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e601', 'ASS_TEC', 'Assistente Técnico', 'Categoria de Nível Médio', 1, true, NOW(), 'system')
ON CONFLICT (id) DO UPDATE SET ordem_progressao = EXCLUDED.ordem_progressao;

-- Grades (Escalões)
-- Cada categoria tem pelo menos dois escalões: com um só, ninguém progride e o
-- caminho da progressão fica por exercitar (o seed anterior punha o Francisco
-- Bastos logo no último escalão do Técnico Superior).
INSERT INTO t_grade (id, category_id, grade_number, name, salary_index, is_active, created_date, created_by) VALUES
('81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e801', '71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e701', 1, 'Escalão 1', 100.00, true, NOW(), 'system'),
('81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e802', '71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e701', 2, 'Escalão 2', 110.00, true, NOW(), 'system'),
('81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e805', '71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e701', 3, 'Escalão 3', 120.00, true, NOW(), 'system'),
('81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e803', '71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e702', 1, 'Escalão 1', 80.00, true, NOW(), 'system'),
('81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e804', '71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e702', 2, 'Escalão 2', 88.00, true, NOW(), 'system')
ON CONFLICT (id) DO NOTHING;
