-- SEED: Colaboradores
-- Description: Employees with their respective contracts, placements, and career mappings
--
-- Reference keys resolved against the current schema (several columns moved from free text
-- to foreign keys, and the assignment tables gained the t_ prefix):
--   worker_state_id   c1e1e1e1-...e1e1 = ACTIVE           (t_worker_state)
--   contract_type_id  b84e1b52-...e1e  = NOMEACAO_DEFINITIVA
--                     b84e1b52-...e20  = CTFP_TERMO_CERTO (t_contract_type)

-- Employees
INSERT INTO t_funcionario (id, numero_funcionario, nome_completo, data_nascimento, genero, estado_civil, nif, numero_documento, data_validade_doc, nacionalidade, email, telefone, worker_state_id, data_admissao, is_active, created_date, created_by) VALUES
('91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901', '0000001', 'Francisco Bastos', '1985-05-15', 'MASCULINO', 'SOLTEIRO', '123456789', 'BI001', '2030-01-01', 'CV', 'francisco.bastos@mffe.gov.cv', '+238900001', 'c1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', '2010-01-01', true, NOW(), 'system'),
('91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e902', '0000002', 'Maria Santos', '1990-10-20', 'FEMININO', 'CASADO', '987654321', 'BI002', '2030-06-01', 'CV', 'maria.santos@mffe.gov.cv', '+238900002', 'c1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', '2015-06-01', true, NOW(), 'system')
ON CONFLICT (numero_funcionario) DO NOTHING;

-- Contracts
INSERT INTO t_contrato (id, funcionario_id, contract_type_id, start_date, contract_number, is_current, created_date, created_by) VALUES
('a2e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ea01', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901', 'b84e1b52-2c6c-4b5a-9b5a-7e1e1e1e1e1e', '2010-01-01', 'CONT-001', true, NOW(), 'system'),
('a2e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ea02', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e902', 'b84e1b52-2c6c-4b5a-9b5a-7e1e1e1e1e20', '2015-06-01', 'CONT-002', true, NOW(), 'system')
ON CONFLICT (contract_number) DO NOTHING;

-- Placements (Colocação)
INSERT INTO t_employee_unit_assignments (id, funcionario_id, unit_id, job_id, start_date, is_current, is_active, assignment_type, created_date, created_by) VALUES
('b3e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1eb01', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901', '31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e303', '51e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e501', '2010-01-01', true, true, 'PERMANENT', NOW(), 'system'),
('b3e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1eb02', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e902', '31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e303', '51e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e502', '2015-06-01', true, true, 'PERMANENT', NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- Career Mapping (Enquadramento)
INSERT INTO t_employee_professional_assignments (id, funcionario_id, career_id, category_id, grade_id, cargo_id, unidade_organica_id, data_inicio, is_current, created_date, created_by) VALUES
('c4e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ec01', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901', '61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e601', '71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e701', '81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e802', '51e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e501', '31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e303', '2020-01-01', true, NOW(), 'system'),
('c4e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ec02', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e902', '61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e601', '71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e702', '81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e803', '51e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e502', '31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e303', '2021-01-01', true, NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- Documents (referencing funcionario via reference_entity/reference_id)
INSERT INTO t_document (id, reference_entity, reference_id, document_type_id, file_key, original_filename, content_type, file_size, is_active, created_date, created_by) VALUES
('d5e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ed01', 'FUNCIONARIO', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901', 'a1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2', 'docs/emp001/cni.pdf', 'cni_francisco.pdf', 'application/pdf', 102400, true, NOW(), 'system'),
('d5e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ed02', 'FUNCIONARIO', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e902', 'a1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2', 'docs/emp002/cni.pdf', 'cni_maria.pdf', 'application/pdf', 98304, true, NOW(), 'system')
ON CONFLICT (id) DO NOTHING;
