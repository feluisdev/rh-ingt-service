-- SEED: Estrutura Organizacional
-- Description: Organizational units, functions, and jobs

-- Organizational Units (Hierarchical)
INSERT INTO t_unidade_organica (id, code, name, acronym, type, parent_unit_id, is_active, created_date, created_by) VALUES
-- Level 1: Ministry
('31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e301', 'MIN_FIN', 'Ministério das Finanças e do Fomento Empresarial', 'MFFE', 'MINISTRY', NULL, true, NOW(), 'system'),
-- Level 2: Direction
('31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e302', 'DGP', 'Direção Geral do Planeamento, Orçamento e Gestão', 'DGPOG', 'DIRECTION', '31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e301', true, NOW(), 'system'),
-- Level 3: Service
('31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e303', 'SERV_RH', 'Serviço de Recursos Humanos', 'SRH', 'SERVICE', '31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e302', true, NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- Functions
INSERT INTO t_funcao (id, code, name, description, is_active, created_date, created_by) VALUES
('41e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e401', 'DIR_GERAL', 'Diretor Geral', 'Diretor Geral de Unidade', true, NOW(), 'system'),
('41e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e402', 'DIR_SERVICO', 'Diretor de Serviço', 'Responsável pelo Serviço', true, NOW(), 'system'),
('41e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e403', 'TECNICO', 'Técnico', 'Técnico de Nível Superior', true, NOW(), 'system'),
('41e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e404', 'COORD', 'Coordenador', 'Coordenador de Equipa', true, NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- Jobs
INSERT INTO t_job (id, code, name, description, is_active, created_date, created_by) VALUES
('51e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e501', 'GESTOR_RH', 'Gestor de Recursos Humanos', 'Gestão de processos de RH', true, NOW(), 'system'),
('51e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e502', 'ANALISTA_PLAN', 'Analista de Planeamento', 'Análise estratégica e tática', true, NOW(), 'system'),
('51e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e503', 'TEC_FIN', 'Técnico de Finanças', 'Gestão financeira e orçamental', true, NOW(), 'system')
ON CONFLICT (code) DO NOTHING;
