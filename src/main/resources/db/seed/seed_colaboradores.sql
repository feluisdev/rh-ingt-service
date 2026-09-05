-- SEED: Colaboradores
-- Description: Employees with their respective contracts, positions and assignments
--
-- Reference keys resolved against the current schema (several columns moved from free text
-- to foreign keys). A colocacao e o enquadramento deixaram de existir como tabelas
-- proprias: sao agora Lugar (t_position) + Afectacao (t_assignment) -- ver Position
-- Management (V37) e o drop do legado (V38):
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

-- Mapa de Pessoal (Lugar) + Afectacao
--
-- O modelo de colocacao/enquadramento (t_employee_unit_assignments +
-- t_employee_professional_assignments) foi eliminado no Bloco B e as tabelas
-- largadas pela V38. Passou a haver duas entidades: o Lugar (t_position), que
-- e a cadeira no mapa de pessoal e onde vive a unidade organica, e a Afectacao
-- (t_assignment), que liga o colaborador ao Lugar num periodo.
--
-- Os dados aqui sao os mesmos das duas tabelas antigas, reunidos: o job e a
-- unidade vinham da colocacao, a carreira/categoria/escalao do enquadramento.
--
-- data_inicio usa a data da COLOCACAO (2010/2015), nao a do enquadramento
-- (2020/2021): e a data de entrada na unidade, e e ela que responde a
-- "quem esteve nesta unidade no ano X" -- a consulta que a geracao de fichas
-- SIADAP faz (findAllByUnidadeOrganicaCoveringYear). Com a data do
-- enquadramento, um periodo de 2015 nao encontraria a Maria.
INSERT INTO t_position (id, numero_lugar, job_id, unidade_organica_id, career_id, category_id, estado, is_active, created_date, created_by) VALUES
('d5e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ed01', 'LUG-0001', '51e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e501', '31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e303', '61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e601', '71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e701', 'ATIVO', true, NOW(), 'system'),
('d5e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ed02', 'LUG-0002', '51e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e502', '31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e303', '61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e601', '71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e702', 'ATIVO', true, NOW(), 'system')
ON CONFLICT (numero_lugar) DO NOTHING;

INSERT INTO t_assignment (id, funcionario_id, position_id, grade_id, assignment_type, origem, data_inicio, is_current, is_active, created_date, created_by) VALUES
('e6e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ee01', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901', 'd5e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ed01', '81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e802', 'PRINCIPAL', 'ADMISSAO', '2010-01-01', true, true, NOW(), 'system'),
('e6e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ee02', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e902', 'd5e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ed02', '81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e803', 'PRINCIPAL', 'ADMISSAO', '2015-06-01', true, true, NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- Documents (referencing funcionario via reference_entity/reference_id)
INSERT INTO t_document (id, reference_entity, reference_id, document_type_id, file_key, original_filename, content_type, file_size, is_active, created_date, created_by) VALUES
('d5e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ed01', 'FUNCIONARIO', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901', 'a1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2', 'docs/emp001/cni.pdf', 'cni_francisco.pdf', 'application/pdf', 102400, true, NOW(), 'system'),
('d5e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ed02', 'FUNCIONARIO', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e902', 'a1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2', 'docs/emp002/cni.pdf', 'cni_maria.pdf', 'application/pdf', 98304, true, NOW(), 'system')
ON CONFLICT (id) DO NOTHING;
