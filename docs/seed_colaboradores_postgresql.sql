-- =============================================================================
-- SIPPROG - Sistema de Informação de Pessoal e Progressões
-- SEED DE COLABORADORES SIMULADOS (Compatível com PostgreSQL / psql)
-- =============================================================================
-- Este script realiza a carga de funcionários, contratos, colocações
-- e enquadramentos profissionais fictícios para testes.
-- Inclui o utilizador "igrp.user@nosi.cv" e garante 100% da integridade de FKs.
-- =============================================================================

-- =============================================================================
-- 1. Funcionários (t_funcionario)
-- =============================================================================
INSERT INTO t_funcionario (
    id, numero_funcionario, nome_completo, data_nascimento, genero, estado_civil,
    nif, document_type_id, numero_documento, data_emissao_doc, data_validade_doc,
    nacionalidade, email, telefone, morada, ilha, concelho, localidade,
    worker_state_id, data_admissao, is_active, created_date, created_by
) VALUES
-- Utilizador Requerido: igrp.user@nosi.cv
('91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e001', '0000000', 'Utilizador IGRP', '1988-08-08', 'M', 'SOLTEIRO',
 '999999999', 'a1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2', 'CNI99999', '2020-01-01', '2030-01-01',
 'CV', 'igrp.user@nosi.cv', '+2389999999', 'Palácio do Governo, Várzea', 'SANTIAGO', 'PRAIA', 'Praia',
 'c1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', '2018-01-01', true, NOW(), 'system'),

-- Colaborador 1: Francisco Bastos
('91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901', '0000001', 'Francisco Bastos', '1985-05-15', 'M', 'SOLTEIRO',
 '123456789', 'a1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'BI001', '2015-01-01', '2030-01-01',
 'CV', 'francisco.bastos@mffe.gov.cv', '+238900001', 'Fazenda, Praia', 'SANTIAGO', 'PRAIA', 'Praia',
 'c1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', '2010-01-01', true, NOW(), 'system'),

-- Colaborador 2: Maria Santos
('91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e902', '0000002', 'Maria Santos', '1990-10-20', 'F', 'CASADO',
 '987654321', 'a1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2', 'CNI002', '2020-06-01', '2030-06-01',
 'CV', 'maria.santos@mffe.gov.cv', '+238900002', 'Palmarejo, Praia', 'SANTIAGO', 'PRAIA', 'Praia',
 'c1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', '2015-06-01', true, NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- 2. Contratos (t_contrato)
-- =============================================================================
INSERT INTO t_contrato (
    id, funcionario_id, contract_type_id, contract_number, start_date, end_date,
    termination_reason, is_current, status, renewal_count, regime_trabalho,
    percentagem_tempo, legal_base, notes, created_date, created_by
) VALUES
-- Contrato do igrp.user@nosi.cv
('a2e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ea00', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e001', 'b84e1b52-2c6c-4b5a-9b5a-7e1e1e1e1e20', 'CONT-999', '2018-01-01', null,
 null, true, 'ACTIVE', 0, 'TEMPO_COMPLETO', 100.00, 'Estatuto Geral do Pessoal', 'Contrato simulado IGRP', NOW(), 'system'),

-- Contrato do Francisco Bastos
('a2e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ea01', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901', 'b84e1b52-2c6c-4b5a-9b5a-7e1e1e1e1e1e', 'CONT-001', '2010-01-01', null,
 null, true, 'ACTIVE', 0, 'TEMPO_COMPLETO', 100.00, 'Estatuto Geral do Pessoal', 'Contrato inicial nomeação definitiva', NOW(), 'system'),

-- Contrato da Maria Santos
('a2e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ea02', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e902', 'b84e1b52-2c6c-4b5a-9b5a-7e1e1e1e1e20', 'CONT-002', '2015-06-01', null,
 null, true, 'ACTIVE', 0, 'TEMPO_COMPLETO', 100.00, 'CTFP a Termo Certo', 'Contrato inicial termo certo', NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- 3. Colocações / Afetações (t_employee_unit_assignments)
-- =============================================================================
INSERT INTO t_employee_unit_assignments (
    id, funcionario_id, unit_id, job_id, start_date, end_date,
    is_current, is_active, assignment_type, notes, created_date, created_by
) VALUES
-- Colocação do igrp.user@nosi.cv (Gestor de RH em Serviço de RH)
('b3e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1eb00', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e001', '31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e303', '51e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e501', '2018-01-01', null,
 true, true, 'PERMANENT', null, NOW(), 'system'),

-- Colocação do Francisco Bastos (Gestor de RH em Serviço de RH)
('b3e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1eb01', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901', '31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e303', '51e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e501', '2010-01-01', null,
 true, true, 'PERMANENT', null, NOW(), 'system'),

-- Colocação da Maria Santos (Analista de Planeamento em Serviço de RH)
('b3e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1eb02', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e902', '31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e303', '51e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e502', '2015-06-01', null,
 true, true, 'PERMANENT', null, NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- 4. Enquadramento Profissional (t_employee_professional_assignments)
-- =============================================================================
INSERT INTO t_employee_professional_assignments (
    id, funcionario_id, career_id, category_id, grade_id, cargo_id, function_id,
    unidade_organica_id, data_inicio, data_fim, is_current, created_date, created_by
) VALUES
-- Enquadramento do igrp.user@nosi.cv
('c4e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ec00', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e001',
 '61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e601', '71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e701', '81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e802', -- Escalão 2
 '51e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e501', '41e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e402', -- Diretor de Serviço
 '31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e303', '2018-01-01', null, true, NOW(), 'system'),

-- Enquadramento do Francisco Bastos
('c4e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ec01', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901',
 '61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e601', '71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e701', '81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e802', -- Escalão 2
 '51e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e501', '41e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e402', -- Diretor de Serviço
 '31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e303', '2010-01-01', null, true, NOW(), 'system'),

-- Enquadramento da Maria Santos
('c4e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ec02', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e902',
 '61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e601', '71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e702', '81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e803', -- Escalão 1 (Cat 2)
 '51e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e502', '41e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e403', -- Técnico
 '31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e303', '2015-06-01', null, true, NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- 5. Documentos do Trabalhador (t_document)
-- =============================================================================
INSERT INTO t_document (
    id, reference_entity, reference_id, document_type_id, file_key,
    original_filename, content_type, file_size, description, is_active, created_date, created_by
) VALUES
-- CNI do igrp.user@nosi.cv
('d5e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ed00', 'FUNCIONARIO', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e001', 'a1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2',
 'docs/emp000/cni.pdf', 'cni_igrp_user.pdf', 'application/pdf', 123456, 'CNI de Simulação do utilizador IGRP', true, NOW(), 'system'),

-- CNI do Francisco Bastos
('d5e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ed01', 'FUNCIONARIO', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901', 'a1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2',
 'docs/emp001/cni.pdf', 'cni_francisco.pdf', 'application/pdf', 102400, 'CNI Francisco Bastos', true, NOW(), 'system'),

-- CNI da Maria Santos
('d5e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ed02', 'FUNCIONARIO', '91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e902', 'a1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2',
 'docs/emp002/cni.pdf', 'cni_maria.pdf', 'application/pdf', 98304, 'CNI Maria Santos', true, NOW(), 'system')
ON CONFLICT (id) DO NOTHING;
