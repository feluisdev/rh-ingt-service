-- SEED: Parametrizacoes
-- Description: Basic configuration data for HR Service

-- Contract Types
INSERT INTO t_contract_type (id, code, description, is_active, created_date, created_by) VALUES
('b84e1b52-2c6c-4b5a-9b5a-7e1e1e1e1e1e', 'NOM_DEF', 'Nomeação Definitiva', true, NOW(), 'system'),
('b84e1b52-2c6c-4b5a-9b5a-7e1e1e1e1e1f', 'NOM_PROV', 'Nomeação Provisória', true, NOW(), 'system'),
('b84e1b52-2c6c-4b5a-9b5a-7e1e1e1e1e20', 'CONT_TRAB', 'Contrato de Trabalho', true, NOW(), 'system'),
('b84e1b52-2c6c-4b5a-9b5a-7e1e1e1e1e21', 'CONT_PREST', 'Contrato de Prestação de Serviço', true, NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- Document Types
INSERT INTO t_tipo_documento (id, codigo, descricao, is_active, created_date, created_by) VALUES
('a1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'BI', 'Bilhete de Identidade', true, NOW(), 'system'),
('a1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2', 'CNI', 'Cartão Nacional de Identificação', true, NOW(), 'system'),
('a1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e3', 'PASS', 'Passaporte', true, NOW(), 'system'),
('a1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e4', 'CRES', 'Cartão de Residência', true, NOW(), 'system')
ON CONFLICT (codigo) DO NOTHING;

-- Worker States
INSERT INTO t_worker_state (id, code, description, is_core, is_active, created_date, created_by) VALUES
('c1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'ATIVO', 'Ativo', true, true, NOW(), 'system'),
('c1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2', 'INATIVO', 'Inativo', false, true, NOW(), 'system'),
('c1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e3', 'SUSPENSO', 'Suspenso', false, true, NOW(), 'system'),
('c1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e4', 'APOSENTADO', 'Aposentado', false, true, NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- Vínculos Laborais
INSERT INTO t_vinculo_laboral (id, code, description, is_active, created_date, created_by) VALUES
('d1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'EFETIVO', 'Efetivo', true, NOW(), 'system'),
('d1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2', 'REQUISICAO', 'Em Requisição', true, NOW(), 'system'),
('d1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e3', 'COM_SERVICO', 'Em Comissão de Serviço', true, NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- Leave Types
INSERT INTO t_leave_type (id, code, description, deducts_balance, requires_approval, is_active, created_date, created_by) VALUES
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'FALTA', 'Falta', true, false, true, NOW(), 'system'),
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2', 'LICENCA', 'Licença', false, true, true, NOW(), 'system'),
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e3', 'MOBILIDADE', 'Mobilidade', false, true, true, NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- Leave Mobility Subtypes
-- record_type: LICENCA | MOBILIDADE | AMBOS
INSERT INTO t_leave_mobility_subtype (id, code, description, record_type, affects_pay, counts_for_seniority, can_self_submit, is_active, created_date, created_by) VALUES
('f1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'FALTA_JUST', 'Falta Justificada', 'LICENCA', false, true, true, true, NOW(), 'system'),
('f1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2', 'FALTA_INJUST', 'Falta Injustificada', 'LICENCA', true, false, false, true, NOW(), 'system'),
('f1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e3', 'LIC_MATER', 'Licença Maternidade', 'LICENCA', false, true, false, true, NOW(), 'system'),
('f1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e4', 'LIC_PATER', 'Licença Paternidade', 'LICENCA', false, true, false, true, NOW(), 'system'),
('f1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e5', 'MOB_INTERNA', 'Mobilidade Interna', 'MOBILIDADE', false, true, false, true, NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- Public Holidays
INSERT INTO t_public_holiday (id, name, holiday_date, is_national, description, is_active, created_date, created_by) VALUES
('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e101', 'Ano Novo', '2026-01-01', true, 'Dia de Ano Novo', true, NOW(), 'seed'),
('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e102', 'Dia da Liberdade e Democracia', '2026-01-13', true, 'Dia da Liberdade e Democracia', true, NOW(), 'seed'),
('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e103', 'Dia dos Heróis Nacionais', '2026-01-20', true, 'Dia dos Heróis Nacionais', true, NOW(), 'seed'),
('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e104', 'Dia do Trabalhador', '2026-05-01', true, 'Dia do Trabalhador', true, NOW(), 'seed'),
('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e105', 'Dia da Independência', '2026-07-05', true, 'Dia da Independência Nacional', true, NOW(), 'seed'),
('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e106', 'Dia de Todos os Santos', '2026-11-01', true, 'Dia de Todos os Santos', true, NOW(), 'seed'),
('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e107', 'Dia de Natal', '2026-12-25', true, 'Natal', true, NOW(), 'seed')
ON CONFLICT (id) DO NOTHING;

-- Options (Unit Types)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active, description, created_date, created_by) VALUES
('21e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e201', 'UNIT_TYPE', 'MINISTRY', 'Ministério', 'pt', 1, true, 'Ministério', NOW(), 'system'),
('21e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e202', 'UNIT_TYPE', 'DIRECTION', 'Direção Geral', 'pt', 2, true, 'Direção Geral', NOW(), 'system'),
('21e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e203', 'UNIT_TYPE', 'SERVICE', 'Serviço', 'pt', 3, true, 'Serviço', NOW(), 'system'),
('21e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e204', 'UNIT_TYPE', 'SECTION', 'Secção', 'pt', 4, true, 'Secção', NOW(), 'system')
ON CONFLICT (id) DO NOTHING;
