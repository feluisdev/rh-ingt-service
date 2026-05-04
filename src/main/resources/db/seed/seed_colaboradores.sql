-- SEED: Colaboradores
-- Description: Employees with their respective contracts, placements, and career mappings

-- Employees
INSERT INTO t_funcionario (id, numero_funcionario, nome_completo, data_nascimento, genero, estado_civil, nif, bi_numero, bi_validade, nacionalidade, email, telefone, situacao_profissional, data_admissao, is_active, created_date, created_by) VALUES
('91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901', '0000001', 'Francisco Bastos', '1985-05-15', 'MASCULINO', 'SOLTEIRO', '123456789', 'BI001', '2030-01-01', 'CV', 'francisco.bastos@mffe.gov.cv', '+238900001', 'EFETIVO', '2010-01-01', true, NOW(), 'system'),
('91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e902', '0000002', 'Maria Santos', '1990-10-20', 'FEMININO', 'CASADO', '987654321', 'BI002', '2030-06-01', 'CV', 'maria.santos@mffe.gov.cv', '+238900002', 'EFETIVO', '2015-06-01', true, NOW(), 'system')
ON CONFLICT (numero_funcionario) DO NOTHING;

-- Contracts
INSERT INTO t_contrato (id, funcionario_id, tipo_contrato, data_inicio, numero_contrato, is_active, created_date, created_by) VALUES
('a2e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ea01', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901', 'NOM_DEF', '2010-01-01', 'CONT-001', true, NOW(), 'system'),
('a2e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ea02', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e902', 'CONT_TRAB', '2015-06-01', 'CONT-002', true, NOW(), 'system')
ON CONFLICT (numero_contrato) DO NOTHING;

-- Placements (Colocação)
INSERT INTO employee_unit_assignments (id, funcionario_id, unit_id, job_id, start_date, is_current, is_active, assignment_type, created_date, created_by) VALUES
('b3e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1eb01', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901', '31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e303', '51e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e501', '2010-01-01', true, true, 'PERMANENT', NOW(), 'system'),
('b3e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1eb02', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e902', '31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e303', '51e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e502', '2015-06-01', true, true, 'PERMANENT', NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- Career Mapping (Enquadramento)
INSERT INTO employee_professional_assignments (id, funcionario_id, career_id, category_id, grade_id, cargo_id, unidade_organica_id, data_inicio, is_current, created_date, created_by) VALUES
('c4e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ec01', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901', '61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e601', '71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e701', '81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e802', '51e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e501', '31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e303', '2020-01-01', true, NOW(), 'system'),
('c4e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ec02', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e902', '61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e601', '71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e702', '81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e803', '51e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e502', '31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e303', '2021-01-01', true, NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- Documents (referencing funcionario via reference_entity/reference_id)
INSERT INTO t_document (id, reference_entity, reference_id, document_type_id, file_key, original_filename, content_type, file_size, is_active, created_date, created_by) VALUES
('d5e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ed01', 'FUNCIONARIO', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901', 'a1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2', 'docs/emp001/cni.pdf', 'cni_francisco.pdf', 'application/pdf', 102400, true, NOW(), 'system'),
('d5e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ed02', 'FUNCIONARIO', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e902', 'a1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2', 'docs/emp002/cni.pdf', 'cni_maria.pdf', 'application/pdf', 98304, true, NOW(), 'system')
ON CONFLICT (id) DO NOTHING;
