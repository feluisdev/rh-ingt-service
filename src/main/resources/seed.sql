-- Seed for RH Integration Milestone 6.0
-- 3 Departments with 10 Employees each
-- Fixed: Valid UUID format and 'A' for active status

-- 1. Create Cargos
INSERT INTO t_cargo (id, nome, codigo, descricao, salario_base, nivel_hierarquico, estado, created_date, created_by) VALUES
('a1e1e1e1-e1e1-4e1e-b1e1-a1e1e1e1e1a1', 'Técnico de Nível I', 'TEC01', 'Técnico operacional base', 50000.00, 1, 'A', CURRENT_TIMESTAMP, 'system'),
('a2e2e2e2-e2e2-4e2e-b2e2-a2e2e2e2e2a2', 'Técnico de Nível II', 'TEC02', 'Técnico sénior', 85000.00, 2, 'A', CURRENT_TIMESTAMP, 'system'),
('a3e3e3e3-e3e3-4e3e-b3e3-a3e3e3e3e3a3', 'Diretor de Serviço', 'DIR01', 'Direção e coordenação', 150000.00, 3, 'A', CURRENT_TIMESTAMP, 'system');

-- 2. Create Departments
INSERT INTO t_departamento (id, nome, codigo, descricao, localizacao, orcamento, estado, created_date, created_by) VALUES
('d1d1d1d1-d1d1-4d1d-b1d1-d1d1d1d1d1d1', 'Direção de Recursos Humanos', 'DRH', 'Gestão de Capital Humano', 'Edifício A, Piso 1', 500000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('d2d2d2d2-d2d2-4d2d-b2d2-d2d2d2d2d2d2', 'Direção de Finanças', 'DF', 'Gestão Financeira e Patrimonial', 'Edifício A, Piso 2', 800000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('d3d3d3d3-d3d3-4d3d-b3d3-d3d3d3d3d3d3', 'Direção de Tecnologias de Informação', 'DTI', 'Infraestrutura e Sistemas', 'Edifício B, Piso 0', 1200000.00, 'A', CURRENT_TIMESTAMP, 'system');

-- 3. Create Employees (30 total, 10 per dept)
-- DRH Employees
INSERT INTO t_funcionario (id, nome, nif, email, estado, created_date, created_by) VALUES
('f101e1e1-0000-4000-b000-000000000001', 'Ana Silva', '100000001', 'ana.silva@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f101e1e1-0000-4000-b000-000000000002', 'Bento Gomes', '100000002', 'bento.gomes@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f101e1e1-0000-4000-b000-000000000003', 'Carla Reis', '100000003', 'carla.reis@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f101e1e1-0000-4000-b000-000000000004', 'Duarte Lima', '100000004', 'duarte.lima@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f101e1e1-0000-4000-b000-000000000005', 'Elena Martins', '100000005', 'elena.martins@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f101e1e1-0000-4000-b000-000000000006', 'Fabio Costa', '100000006', 'fabio.costa@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f101e1e1-0000-4000-b000-000000000007', 'Gina Rocha', '100000007', 'gina.rocha@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f101e1e1-0000-4000-b000-000000000008', 'Helder Pires', '100000008', 'helder.pires@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f101e1e1-0000-4000-b000-000000000009', 'Ilda Neves', '100000009', 'ilda.neves@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f101e1e1-0000-4000-b000-000000000010', 'Joao Mendes', '100000010', 'joao.mendes@gov.cv', 'A', CURRENT_TIMESTAMP, 'system');

-- DF Employees
INSERT INTO t_funcionario (id, nome, nif, email, estado, created_date, created_by) VALUES
('f202e2e2-0000-4000-b000-000000000001', 'Katia Tavares', '200000001', 'katia.tavares@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f202e2e2-0000-4000-b000-000000000002', 'Luis Santos', '200000002', 'luis.santos@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f202e2e2-0000-4000-b000-000000000003', 'Maria Delgado', '200000003', 'maria.delgado@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f202e2e2-0000-4000-b000-000000000004', 'Nelson Vieira', '200000004', 'nelson.vieira@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f202e2e2-0000-4000-b000-000000000005', 'Olivia Dias', '200000005', 'olivia.dias@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f202e2e2-0000-4000-b000-000000000006', 'Paulo Borges', '200000006', 'paulo.borges@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f202e2e2-0000-4000-b000-000000000007', 'Quiteria Ramos', '200000007', 'quiteria.ramos@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f202e2e2-0000-4000-b000-000000000008', 'Ricardo Semedo', '200000008', 'ricardo.semedo@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f202e2e2-0000-4000-b000-000000000009', 'Sonia Cabral', '200000009', 'sonia.cabral@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f202e2e2-0000-4000-b000-000000000010', 'Tiago Almeida', '200000010', 'tiago.almeida@gov.cv', 'A', CURRENT_TIMESTAMP, 'system');

-- DTI Employees
INSERT INTO t_funcionario (id, nome, nif, email, estado, created_date, created_by) VALUES
('f303e3e3-0000-4000-b000-000000000001', 'Ulisses Correia', '300000001', 'ulisses.correia@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f303e3e3-0000-4000-b000-000000000002', 'Vera Monteiro', '300000002', 'vera.monteiro@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f303e3e3-0000-4000-b000-000000000003', 'Wilson Fortes', '300000003', 'wilson.fortes@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f303e3e3-0000-4000-b000-000000000004', 'Xavier Lopes', '300000004', 'xavier.lopes@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f303e3e3-0000-4000-b000-000000000005', 'Yara Sanches', '300000005', 'yara.sanches@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f303e3e3-0000-4000-b000-000000000006', 'Zuleica Varela', '300000006', 'zuleica.varela@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f303e3e3-0000-4000-b000-000000000007', 'Abel Ferreira', '300000007', 'abel.ferreira@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f303e3e3-0000-4000-b000-000000000008', 'Bruna Evora', '300000008', 'bruna.evora@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f303e3e3-0000-4000-b000-000000000009', 'Cidalia Lima', '300000009', 'cidalia.lima@gov.cv', 'A', CURRENT_TIMESTAMP, 'system'),
('f303e3e3-0000-4000-b000-000000000010', 'Daniel Soares', '300000010', 'daniel.soares@gov.cv', 'A', CURRENT_TIMESTAMP, 'system');

-- 4. Create Contracts (Linking Employees to Departments and Cargos)
-- DRH Contracts
INSERT INTO t_contrato_entity (id, id_funcionario, id_departamento, id_cargo, tipo_contrato, data_inicio, salario, estado, created_date, created_by) VALUES
('c101e1e1-0000-4000-b000-000000000001', 'f101e1e1-0000-4000-b000-000000000001', 'd1d1d1d1-d1d1-4d1d-b1d1-d1d1d1d1d1d1', 'a3e3e3e3-e3e3-4e3e-b3e3-a3e3e3e3e3a3', 'TEMPO_INDETERMINADO', '2020-01-01', 150000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c101e1e1-0000-4000-b000-000000000002', 'f101e1e1-0000-4000-b000-000000000002', 'd1d1d1d1-d1d1-4d1d-b1d1-d1d1d1d1d1d1', 'a2e2e2e2-e2e2-4e2e-b2e2-a2e2e2e2e2a2', 'TEMPO_INDETERMINADO', '2021-06-15', 85000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c101e1e1-0000-4000-b000-000000000003', 'f101e1e1-0000-4000-b000-000000000003', 'd1d1d1d1-d1d1-4d1d-b1d1-d1d1d1d1d1d1', 'a1e1e1e1-e1e1-4e1e-b1e1-a1e1e1e1e1a1', 'PRAZO_CERTO', '2023-01-10', 50000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c101e1e1-0000-4000-b000-000000000004', 'f101e1e1-0000-4000-b000-000000000004', 'd1d1d1d1-d1d1-4d1d-b1d1-d1d1d1d1d1d1', 'a1e1e1e1-e1e1-4e1e-b1e1-a1e1e1e1e1a1', 'TEMPO_INDETERMINADO', '2022-03-20', 55000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c101e1e1-0000-4000-b000-000000000005', 'f101e1e1-0000-4000-b000-000000000005', 'd1d1d1d1-d1d1-4d1d-b1d1-d1d1d1d1d1d1', 'a2e2e2e2-e2e2-4e2e-b2e2-a2e2e2e2e2a2', 'TEMPO_INDETERMINADO', '2019-11-05', 90000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c101e1e1-0000-4000-b000-000000000006', 'f101e1e1-0000-4000-b000-000000000006', 'd1d1d1d1-d1d1-4d1d-b1d1-d1d1d1d1d1d1', 'a1e1e1e1-e1e1-4e1e-b1e1-a1e1e1e1e1a1', 'TEMPO_INDETERMINADO', '2020-05-12', 52000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c101e1e1-0000-4000-b000-000000000007', 'f101e1e1-0000-4000-b000-000000000007', 'd1d1d1d1-d1d1-4d1d-b1d1-d1d1d1d1d1d1', 'a1e1e1e1-e1e1-4e1e-b1e1-a1e1e1e1e1a1', 'PRAZO_CERTO', '2024-02-01', 48000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c101e1e1-0000-4000-b000-000000000008', 'f101e1e1-0000-4000-b000-000000000008', 'd1d1d1d1-d1d1-4d1d-b1d1-d1d1d1d1d1d1', 'a2e2e2e2-e2e2-4e2e-b2e2-a2e2e2e2e2a2', 'TEMPO_INDETERMINADO', '2021-09-30', 87000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c101e1e1-0000-4000-b000-000000000009', 'f101e1e1-0000-4000-b000-000000000009', 'd1d1d1d1-d1d1-4d1d-b1d1-d1d1d1d1d1d1', 'a1e1e1e1-e1e1-4e1e-b1e1-a1e1e1e1e1a1', 'TEMPO_INDETERMINADO', '2022-07-22', 51000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c101e1e1-0000-4000-b000-000000000010', 'f101e1e1-0000-4000-b000-000000000010', 'd1d1d1d1-d1d1-4d1d-b1d1-d1d1d1d1d1d1', 'a1e1e1e1-e1e1-4e1e-b1e1-a1e1e1e1e1a1', 'TEMPO_INDETERMINADO', '2023-05-15', 53000.00, 'A', CURRENT_TIMESTAMP, 'system');

-- DF Contracts
INSERT INTO t_contrato_entity (id, id_funcionario, id_departamento, id_cargo, tipo_contrato, data_inicio, salario, estado, created_date, created_by) VALUES
('c202e2e2-0000-4000-b000-000000000001', 'f202e2e2-0000-4000-b000-000000000001', 'd2d2d2d2-d2d2-4d2d-b2d2-d2d2d2d2d2d2', 'a3e3e3e3-e3e3-4e3e-b3e3-a3e3e3e3e3a3', 'TEMPO_INDETERMINADO', '2018-04-10', 155000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c202e2e2-0000-4000-b000-000000000002', 'f202e2e2-0000-4000-b000-000000000002', 'd2d2d2d2-d2d2-4d2d-b2d2-d2d2d2d2d2d2', 'a2e2e2e2-e2e2-4e2e-b2e2-a2e2e2e2e2a2', 'TEMPO_INDETERMINADO', '2020-02-28', 88000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c202e2e2-0000-4000-b000-000000000003', 'f202e2e2-0000-4000-b000-000000000003', 'd2d2d2d2-d2d2-4d2d-b2d2-d2d2d2d2d2d2', 'a1e1e1e1-e1e1-4e1e-b1e1-a1e1e1e1e1a1', 'TEMPO_INDETERMINADO', '2022-11-11', 54000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c202e2e2-0000-4000-b000-000000000004', 'f202e2e2-0000-4000-b000-000000000004', 'd2d2d2d2-d2d2-4d2d-b2d2-d2d2d2d2d2d2', 'a1e1e1e1-e1e1-4e1e-b1e1-a1e1e1e1e1a1', 'PRAZO_CERTO', '2024-01-01', 50000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c202e2e2-0000-4000-b000-000000000005', 'f202e2e2-0000-4000-b000-000000000005', 'd2d2d2d2-d2d2-4d2d-b2d2-d2d2d2d2d2d2', 'a2e2e2e2-e2e2-4e2e-b2e2-a2e2e2e2e2a2', 'TEMPO_INDETERMINADO', '2021-03-14', 86000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c202e2e2-0000-4000-b000-000000000006', 'f202e2e2-0000-4000-b000-000000000006', 'd2d2d2d2-d2d2-4d2d-b2d2-d2d2d2d2d2d2', 'a1e1e1e1-e1e1-4e1e-b1e1-a1e1e1e1e1a1', 'TEMPO_INDETERMINADO', '2019-09-09', 57000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c202e2e2-0000-4000-b000-000000000007', 'f202e2e2-0000-4000-b000-000000000007', 'd2d2d2d2-d2d2-4d2d-b2d2-d2d2d2d2d2d2', 'a1e1e1e1-e1e1-4e1e-b1e1-a1e1e1e1e1a1', 'TEMPO_INDETERMINADO', '2023-02-14', 52500.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c202e2e2-0000-4000-b000-000000000008', 'f202e2e2-0000-4000-b000-000000000008', 'd2d2d2d2-d2d2-4d2d-b2d2-d2d2d2d2d2d2', 'a2e2e2e2-e2e2-4e2e-b2e2-a2e2e2e2e2a2', 'TEMPO_INDETERMINADO', '2020-07-07', 89000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c202e2e2-0000-4000-b000-000000000009', 'f202e2e2-0000-4000-b000-000000000009', 'd2d2d2d2-d2d2-4d2d-b2d2-d2d2d2d2d2d2', 'a1e1e1e1-e1e1-4e1e-b1e1-a1e1e1e1e1a1', 'TEMPO_INDETERMINADO', '2021-12-12', 55000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c202e2e2-0000-4000-b000-000000000010', 'f202e2e2-0000-4000-b000-000000000010', 'd2d2d2d2-d2d2-4d2d-b2d2-d2d2d2d2d2d2', 'a1e1e1e1-e1e1-4e1e-b1e1-a1e1e1e1e1a1', 'PRAZO_CERTO', '2024-03-01', 49000.00, 'A', CURRENT_TIMESTAMP, 'system');

-- DTI Contracts
INSERT INTO t_contrato_entity (id, id_funcionario, id_departamento, id_cargo, tipo_contrato, data_inicio, salario, estado, created_date, created_by) VALUES
('c303e3e3-0000-4000-b000-000000000001', 'f303e3e3-0000-4000-b000-000000000001', 'd3d3d3d3-d3d3-4d3d-b3d3-d3d3d3d3d3d3', 'a3e3e3e3-e3e3-4e3e-b3e3-a3e3e3e3e3a3', 'TEMPO_INDETERMINADO', '2015-05-20', 160000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c303e3e3-0000-4000-b000-000000000002', 'f303e3e3-0000-4000-b000-000000000002', 'd3d3d3d3-d3d3-4d3d-b3d3-d3d3d3d3d3d3', 'a2e2e2e2-e2e2-4e2e-b2e2-a2e2e2e2e2a2', 'TEMPO_INDETERMINADO', '2019-10-10', 95000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c303e3e3-0000-4000-b000-000000000003', 'f303e3e3-0000-4000-b000-000000000003', 'd3d3d3d3-d3d3-4d3d-b3d3-d3d3d3d3d3d3', 'a2e2e2e2-e2e2-4e2e-b2e2-a2e2e2e2e2a2', 'TEMPO_INDETERMINADO', '2021-01-01', 92000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c303e3e3-0000-4000-b000-000000000004', 'f303e3e3-0000-4000-b000-000000000004', 'd3d3d3d3-d3d3-4d3d-b3d3-d3d3d3d3d3d3', 'a1e1e1e1-e1e1-4e1e-b1e1-a1e1e1e1e1a1', 'TEMPO_INDETERMINADO', '2022-04-01', 60000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c303e3e3-0000-4000-b000-000000000005', 'f303e3e3-0000-4000-b000-000000000005', 'd3d3d3d3-d3d3-4d3d-b3d3-d3d3d3d3d3d3', 'a1e1e1e1-e1e1-4e1e-b1e1-a1e1e1e1e1a1', 'TEMPO_INDETERMINADO', '2023-08-01', 62000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c303e3e3-0000-4000-b000-000000000006', 'f303e3e3-0000-4000-b000-000000000006', 'd3d3d3d3-d3d3-4d3d-b3d3-d3d3d3d3d3d3', 'a1e1e1e1-e1e1-4e1e-b1e1-a1e1e1e1e1a1', 'TEMPO_INDETERMINADO', '2022-02-22', 61000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c303e3e3-0000-4000-b000-000000000007', 'f303e3e3-0000-4000-b000-000000000007', 'd3d3d3d3-d3d3-4d3d-b3d3-d3d3d3d3d3d3', 'a2e2e2e2-e2e2-4e2e-b2e2-a2e2e2e2e2a2', 'PRAZO_CERTO', '2024-04-15', 80000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c303e3e3-0000-4000-b000-000000000008', 'f303e3e3-0000-4000-b000-000000000008', 'd3d3d3d3-d3d3-4d3d-b3d3-d3d3d3d3d3d3', 'a1e1e1e1-e1e1-4e1e-b1e1-a1e1e1e1e1a1', 'TEMPO_INDETERMINADO', '2020-12-01', 59000.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c303e3e3-0000-4000-b000-000000000009', 'f303e3e3-0000-4000-b000-000000000009', 'd3d3d3d3-d3d3-4d3d-b3d3-d3d3d3d3d3d3', 'a1e1e1e1-e1e1-4e1e-b1e1-a1e1e1e1e1a1', 'TEMPO_INDETERMINADO', '2021-11-20', 58500.00, 'A', CURRENT_TIMESTAMP, 'system'),
('c303e3e3-0000-4000-b000-000000000010', 'f303e3e3-0000-4000-b000-000000000010', 'd3d3d3d3-d3d3-4d3d-b3d3-d3d3d3d3d3d3', 'a1e1e1e1-e1e1-4e1e-b1e1-a1e1e1e1e1a1', 'TEMPO_INDETERMINADO', '2023-09-01', 63000.00, 'A', CURRENT_TIMESTAMP, 'system');

-- 5. Set Department Heads
UPDATE t_departamento SET responsavel_id = 'f101e1e1-0000-4000-b000-000000000001' WHERE id = 'd1d1d1d1-d1d1-4d1d-b1d1-d1d1d1d1d1d1';
UPDATE t_departamento SET responsavel_id = 'f202e2e2-0000-4000-b000-000000000001' WHERE id = 'd2d2d2d2-d2d2-4d2d-b2d2-d2d2d2d2d2d2';
UPDATE t_departamento SET responsavel_id = 'f303e3e3-0000-4000-b000-000000000001' WHERE id = 'd3d3d3d3-d3d3-4d3d-b3d3-d3d3d3d3d3d3';
