-- SEED: Parametrizacoes
-- Dados base de configuração do RH-Service (SIPPROG/INGT)
-- Ordem de inserção respeita dependências FK:
--   worker_state → vinculo_laboral → contract_type → tipo_documento
--   → leave_type → leave_mobility_subtype → public_holiday → t_option_entity

-- =============================================================
-- 1. Estados do Trabalhador (t_worker_state)
-- ACTIVE e INACTIVE são estados núcleo (is_core=true) — bloqueiam DELETE/desactivação.
-- =============================================================
-- ends_employment=true → o estado cessa a relação de emprego público (V40)
-- situacao_funcional → situação perante o quadro (Lei 20/X/2023, art. 117.º; V42).
-- Dela derivam os efeitos: abrir vaga, suspender o contrato e contar antiguidade.
-- Fica NULL nos estados de cessação que não são aposentação — quem cessa deixa de
-- ter situação perante o quadro.
INSERT INTO t_worker_state (id, code, description, is_core, is_active, ends_employment, situacao_funcional, created_date, created_by) VALUES
('c1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'ACTIVE',            'Ativo',                       true,  true, false, 'ACTIVIDADE_NO_QUADRO',     NOW(), 'system'),
('c1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2', 'INACTIVE',          'Inativo',                     true,  true, true,  NULL,                       NOW(), 'system'),
('c1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e3', 'SUSPENDED',         'Suspenso',                    false, true, false, 'INACTIVIDADE_NO_QUADRO',   NOW(), 'system'),
('c1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e4', 'RETIRED',           'Aposentado',                  false, true, true,  'APOSENTACAO',              NOW(), 'system'),
-- Art. 119.º — comissão de serviço, requisição, cedência, cargos políticos: mantém o Lugar.
('c1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e5', 'ACTIVE_OUTSIDE',    'Ativo fora do quadro',        false, true, false, 'ACTIVIDADE_FORA_QUADRO',   NOW(), 'system'),
-- Art. 121.º — licença de longa duração, pena de inactividade, doença > 30 dias: abre vaga.
('c1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e6', 'INACTIVE_OUTSIDE',  'Inativo fora do quadro',      false, true, false, 'INACTIVIDADE_FORA_QUADRO', NOW(), 'system'),
-- Art. 122.º — aguarda vaga na sua categoria, com contagem de tempo e abonos.
('c1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e7', 'AVAILABLE',         'Disponibilidade',             false, true, false, 'DISPONIBILIDADE',          NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- =============================================================
-- 2. Vínculos Laborais (t_vinculo_laboral)
-- counts_seniority=false  → tempo não conta para antiguidade PCFR
-- eligible_for_progression=false → não pode progredir na grelha de carreira
-- =============================================================
INSERT INTO t_vinculo_laboral (id, code, description, counts_seniority, eligible_for_progression, is_active, created_date, created_by) VALUES
('d1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'EFETIVO',     'Efetivo',                true,  true,  true, NOW(), 'system'),
('d1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2', 'CONTRATADO',  'Contratado',             true,  true,  true, NOW(), 'system'),
('d1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e3', 'COMISSIONADO','Em Comissão de Serviço', false, false, true, NOW(), 'system'),
('d1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e4', 'ESTAGIARIO',  'Estagiário',             false, false, true, NOW(), 'system'),
('d1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e5', 'PRESTADOR',   'Prestador de Serviços',  false, false, true, NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- =============================================================
-- 3. Tipos de Contrato (t_contract_type)
-- Figuras legais do Decreto-Lei n.º 4/2024 (PCFR)
-- requires_career_structure=false → enquadramento sem grelha PCFR obrigatória
-- =============================================================
INSERT INTO t_contract_type (id, code, description, vinculo_laboral_id, is_renewable, max_renewals, max_duration_months, requires_career_structure, is_active, created_date, created_by) VALUES
('b84e1b52-2c6c-4b5a-9b5a-7e1e1e1e1e1e', 'NOMEACAO_DEFINITIVA', 'Nomeação Definitiva',   'd1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', false, null, null, true,  true, NOW(), 'system'),
('b84e1b52-2c6c-4b5a-9b5a-7e1e1e1e1e1f', 'NOMEACAO_PROVISORIA', 'Nomeação Provisória',   'd1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', false, null, null, true,  true, NOW(), 'system'),
('b84e1b52-2c6c-4b5a-9b5a-7e1e1e1e1e20', 'CTFP_TERMO_CERTO',   'CTFP a Termo Certo',    'd1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2', true,  3,    24,   true,  true, NOW(), 'system'),
('b84e1b52-2c6c-4b5a-9b5a-7e1e1e1e1e21', 'CTFP_TERMO_INCERTO', 'CTFP a Termo Incerto',  'd1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2', false, null, null, true,  true, NOW(), 'system'),
('b84e1b52-2c6c-4b5a-9b5a-7e1e1e1e1e22', 'COMISSAO_SERVICO',   'Comissão de Serviço',   'd1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e3', true,  null, 36,   false, true, NOW(), 'system'),
('b84e1b52-2c6c-4b5a-9b5a-7e1e1e1e1e23', 'ESTAGIO',            'Estágio',               'd1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e4', false, null, 12,   false, true, NOW(), 'system'),
('b84e1b52-2c6c-4b5a-9b5a-7e1e1e1e1e24', 'PRESTACAO_SERVICOS', 'Prestação de Serviços', 'd1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e5', true,  null, null, false, true, NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- =============================================================
-- 4. Tipos de Documento (t_tipo_documento)
-- =============================================================
INSERT INTO t_tipo_documento (id, codigo, descricao, is_active, created_date, created_by) VALUES
('a1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'BI',   'Bilhete de Identidade',           true, NOW(), 'system'),
('a1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2', 'CNI',  'Cartão Nacional de Identificação', true, NOW(), 'system'),
('a1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e3', 'PASS', 'Passaporte',                       true, NOW(), 'system'),
('a1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e4', 'CRES', 'Cartão de Residência',             true, NOW(), 'system')
ON CONFLICT (codigo) DO NOTHING;

-- =============================================================
-- 5. Tipos de Ausência (t_leave_type)
-- deducts_balance=true  → valida saldo disponível antes de aprovar
-- requires_approval=true → fica PENDING até chefia aprovar; false → APPROVED automático
-- Os limites de dias tem TRES naturezas, e a lei quase nunca fala em anos (V53):
--   max_days_per_year       -> tecto do ano civil      (art. 15.o n.o 1 al. j) e al. q))
--   max_days_per_occurrence -> tecto de CADA pedido    (al. a) a al. f) e al. h))
--   max_days_per_month      -> tecto do mes civil      (al. o) e al. q))
-- Nulo em qualquer deles quer dizer que a lei nao poe limite DESSA natureza -- e o caso da
-- maioria das alineas (greve, obrigacoes legais, prisao preventiva, calamidade publica).
-- A al. q) tem limite anual E mensal ao mesmo tempo: e por isso que sao tres valores
-- independentes e nao um valor com uma classificacao ao lado.
-- regime: o capitulo do DL n.o 3/2010 a que a linha obedece. FERIAS (cap. II) vence-se
--         sozinho a 1 de Janeiro, proporcional no ano de ingresso; FALTA (cap. III) nao.
--         E por esta coluna -- nunca pelo codigo -- que o vencimento sabe quais sao ferias,
--         e e por isso que a instituicao a pode mudar sem tocar em codigo. Ver V49.
-- =============================================================
INSERT INTO t_leave_type (id, code, description, deducts_balance, requires_approval, max_days_per_year, max_days_per_occurrence, max_days_per_month, category, regime, is_active, created_date, created_by) VALUES
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'FERIAS',              'Férias',                          true,  true,  22,   null, null, 'GOZAMENTO', 'FERIAS', true, NOW(), 'system'),
-- Doenca: a al. d) trata ate 3 dias consecutivos com declaracao medica e a al. e) mais de 3 e
-- ate 30 com atestado. Sao duas alineas com prova diferente, logo duas linhas -- o tecto de 30
-- e por OCORRENCIA, e nao ha tecto anual: ninguem adoece por quota.
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e2', 'DOENCA',              'Doença (declaração médica)',      false, false, null, 3,    null, 'SAUDE',     'FALTA',  true, NOW(), 'system'),
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e8', 'DOENCA_ATESTADO',     'Doença (atestado médico)',        false, false, null, 30,   null, 'SAUDE',     'FALTA',  true, NOW(), 'system'),
-- Parentalidade: al. t). A duracao vem do art. 17.o e seguintes, nao de um tecto de pedido.
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e3', 'MATERNIDADE',         'Licença de Maternidade',          false, false, null, null, null, 'FAMILIAR',  'FALTA',  true, NOW(), 'system'),
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e4', 'PATERNIDADE',         'Licença de Paternidade',          false, false, null, null, null, 'FAMILIAR',  'FALTA',  true, NOW(), 'system'),
-- Luto: a lei distingue o grau de parentesco -- 8 dias pelo conjuge ou 1.o grau da linha recta
-- (al. b), 3 pelos restantes graus (al. c) --, e conta por FALECIMENTO, nao por ano. A linha
-- antiga dizia 5 dias por ano: recusava o segundo luto do mesmo ano e deixava passar oito dias
-- seguidos de uma so vez. Mantem-se o codigo LUTO no 1.o grau para nao orfanar historico.
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e5', 'LUTO',                'Luto (cônjuge ou 1.º grau)',      false, false, null, 8,    null, 'FAMILIAR',  'FALTA',  true, NOW(), 'system'),
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e9', 'LUTO_OUTRO_GRAU',     'Luto (outros graus)',             false, false, null, 3,    null, 'FAMILIAR',  'FALTA',  true, NOW(), 'system'),
-- Al. a): 6 dias por OCASIAO do casamento. Quem case duas vezes tem direito duas vezes.
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1ea', 'CASAMENTO',           'Casamento',                       false, false, null, 6,    null, 'FAMILIAR',  'FALTA',  true, NOW(), 'system'),
-- Al. h): duas por ocasiao do nascimento de um filho. Nao se confunde com a paternidade.
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1eb', 'NASCIMENTO_FILHO',    'Nascimento de filho',             false, false, null, 2,    null, 'FAMILIAR',  'FALTA',  true, NOW(), 'system'),
-- Al. f): duas por CADA prova ou exame -- a do dia e a da vespera.
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1ec', 'PROVA_EXAME',         'Prova ou exame',                  false, false, null, 2,    null, 'PESSOAL',   'FALTA',  true, NOW(), 'system'),
-- Al. j): ate 15 POR ANO, para assistencia inadiavel a membro do agregado familiar. Aqui o
-- tecto e mesmo anual -- e a unica alinea do artigo que o diz por extenso, alem da al. q).
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1ed', 'ASSISTENCIA_FAMILIA', 'Assistência a familiar',          false, false, 15,   null, null, 'FAMILIAR',  'FALTA',  true, NOW(), 'system'),
-- Al. q): «nao podendo em caso algum ultrapassar 6 dias em cada ano civil E UM DIA POR MES».
-- E esta linha que obriga a ter as duas colunas: os dois tectos valem ao mesmo tempo.
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1ee', 'AUTORIZADA_DIRIGENTE','Falta autorizada pelo dirigente', false, true,  6,    null, 1,    'PESSOAL',   'FALTA',  true, NOW(), 'system'),
-- Al. o): um por mes por conta do periodo de ferias, se ja as gozou. Desconta saldo.
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1ef', 'CONTA_FERIAS',        'Por conta do período de férias',  true,  true,  null, null, 1,    'GOZAMENTO', 'FALTA',  true, NOW(), 'system'),
-- Al. p): greve. Perde remuneracao mas NAO desconta antiguidade (art. 16.o n.o 4) -- e por isso
-- que nao tem tecto nenhum: o direito a greve nao tem quota.
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1f0', 'GREVE',               'Greve',                           false, false, null, null, null, 'PESSOAL',   'FALTA',  true, NOW(), 'system'),
-- Al. m) e al. n): obrigacoes legais ou imposicao de autoridade, e prisao preventiva. Sem tecto:
-- a duracao e a que a autoridade determinar.
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1f1', 'OBRIGACAO_LEGAL',     'Obrigação legal ou judicial',     false, false, null, null, null, 'PESSOAL',   'FALTA',  true, NOW(), 'system'),
-- Genericas, para o que a instituicao nao queira desdobrar. A injustificada nao tem tecto
-- porque nao e um direito: e o registo de uma falta que aconteceu.
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e6', 'FALTA_JUSTIFICADA',   'Falta Justificada',               true,  false, null, null, null, 'PESSOAL',   'FALTA',  true, NOW(), 'system'),
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e7', 'FALTA_INJUSTIFICADA', 'Falta Injustificada',             true,  false, null, null, null, 'PESSOAL',   'FALTA',  true, NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- =============================================================
-- 6. Subtipos de Licença e Mobilidade (t_leave_mobility_subtype)
-- record_type: LICENCA | MOBILIDADE (o valor AMBOS foi removido na V43 —
--   a licença suspende o vínculo e a mobilidade não, art. 171.º n.º 2)
-- affects_pay=true    → processamento salarial afectado
-- counts_for_seniority=false → período não conta para antiguidade PCFR
-- position_effect: MANTEM | ABRE_VAGA · vacancy_after_days: abre vaga só além
--   de N dias · return_effect: REGRESSA_LUGAR | DISPONIBILIDADE (V43)
-- A licença parental não vive aqui: é uma ausência (t_leave_type MATERNIDADE /
--   PATERNIDADE), porque não suspende o vínculo (Lei 20/X/2023, art. 171.º n.º 2
--   vs art. 172.º).
-- =============================================================
-- name repeats the label held in description because V6 declares name NOT NULL while
-- LeaveMobilitySubtypeEntity maps no such field -- the entity only knows code/description.
-- Until that mismatch is settled (V6 even carries an orphan "Remover NOT NULL indevidos"
-- comment with no matching statement), any insert has to fill name by hand.
INSERT INTO t_leave_mobility_subtype (id, code, name, description, record_type, affects_pay, counts_for_seniority, can_self_submit, is_active, position_effect, vacancy_after_days, return_effect, max_duration_days, max_extensions, created_date, created_by) VALUES
-- Licença sem vencimento até 3 anos (DL 3/2010, art. 48.º e 49.º): mantém o lugar.
('f1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1', 'LIC_SEM_VENCIMENTO',   'Licença sem Vencimento',          'Licença sem Vencimento',          'LICENCA',    true,  false, false, true, 'MANTEM',    NULL, 'REGRESSA_LUGAR',  1095, NULL, NOW(), 'system'),
-- Licença sem vencimento ATÉ 90 DIAS (art. 45.º n.º 1 al. a), regime nos art. 46.º e 47.º).
-- É outra modalidade, e não a mesma com prazo menor: exige um ano de serviço efectivo (a de
-- três anos exige três, art. 48.º n.º 1) e não pode voltar a pedir-se nos dois anos seguintes
-- (art. 46.º n.º 2). O Lugar mantém-se: o art. 46.º n.º 3 deixa preenchê-lo por contrato a
-- prazo que CADUCA com o regresso do titular -- que é a substituição, não uma vaga.
-- Sem limite de prorrogações porque a lei não limita o NÚMERO, limita o TOTAL: 90 dias,
-- «a gozar seguida ou interpoladamente» (n.º 1).
-- Art. 47.º n.º 1: perda total das remunerações e desconto na antiguidade para todos os efeitos.
('f1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1ea', 'LIC_SEM_VENC_90',      'Licença sem Vencimento (90 dias)','Licença sem Vencimento até 90 dias','LICENCA',   true,  false, false, true, 'MANTEM',    NULL, 'REGRESSA_LUGAR',  90,   NULL, NOW(), 'system'),
-- Licença EXTRAORDINÁRIA (art. 45.º n.º 1 al. f); regime no art. 64.º deste diploma).
-- O art. 64.º remete o regime inteiro para «o diploma que estabelece o regime de mobilidade dos
-- funcionários na Administração Pública», que não temos -- por isso NÃO SE INVENTA prazo nem
-- número de prorrogações, tal como não se inventou o tempo mínimo da consolidação.
-- Quem a pode pedir é só quem está na situação de DISPONIBILIDADE, e isso o catálogo não
-- consegue exprimir hoje: não há coluna que diga a situação exigida. Fica registado no
-- api_guide como lacuna conhecida, em vez de ficar uma regra a fingir que existe.
-- MANTEM porque quem está em disponibilidade já não tem Lugar (art. 122.º): não há afectação
-- para abrir vaga. E conta para antiguidade por omissão -- não porque se saiba que conta, mas
-- porque descontar o que não se sabe tiraria tempo a quem o tem, que é a regra que o
-- AntiguidadeService já segue para os estados sem situação classificada.
('f1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1eb', 'LIC_EXTRAORDINARIA',   'Licença Extraordinária',          'Licença Extraordinária',          'LICENCA',    false, true,  false, true, 'MANTEM',    NULL, 'DISPONIBILIDADE', NULL, NULL, NOW(), 'system'),
-- Formação (art. 67.º n.º 3 e Lei 20/X/2023 art. 118.º n.º 2): abre vaga além de 6 meses.
('f1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e3', 'LIC_FORMACAO',         'Licença para Formação',           'Licença para Formação',           'LICENCA',    false, true,  true,  true, 'ABRE_VAGA', 180,  'DISPONIBILIDADE', NULL, NULL, NOW(), 'system'),
-- Longa duração (art. 50.º a 53.º): abre vaga e o regresso é pela disponibilidade.
('f1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e7', 'LIC_LONGA_DURACAO',    'Licença de Longa Duração',        'Licença de Longa Duração',        'LICENCA',    true,  false, false, true, 'ABRE_VAGA', NULL, 'DISPONIBILIDADE', NULL, NULL, NOW(), 'system'),
-- Acompanhamento de cônjuge no estrangeiro (art. 56.º n.º 2): abre vaga além de 1 ano.
('f1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e8', 'LIC_ACOMP_CONJUGE',    'Acompanhamento de Cônjuge',       'Acompanhamento de Cônjuge',       'LICENCA',    true,  false, false, true, 'ABRE_VAGA', 365,  'DISPONIBILIDADE', NULL, NULL, NOW(), 'system'),
-- Organismo internacional, como funcionário do organismo (art. 62.º): abre vaga.
('f1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e9', 'LIC_ORG_INTERNACIONAL','Organismo Internacional',         'Organismo Internacional',         'LICENCA',    true,  false, false, true, 'ABRE_VAGA', NULL, 'DISPONIBILIDADE', NULL, NULL, NOW(), 'system'),
-- Mobilidade: mantém sempre o Lugar (art. 135.º n.º 7); um ano, prorrogável uma vez (art. 132.º n.º 5).
-- Comissão de serviço: três anos, sucessivamente renovável (art. 60.º n.º 1) -- e não um ano com
-- uma prorrogação, que é a regra da mobilidade comum (art. 132.º n.º 5). NULL em max_extensions
-- quer dizer sem limite, que é o que «sucessivamente» diz.
-- REGRESSA_OU_CESSA por causa do art. 64.º n.º 2: cessada a comissão, regressa quem tinha
-- situação anterior; quem foi recrutado PARA a comissão não tem para onde voltar e cessa.
('f1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e4', 'MOB_COMISSAO',         'Comissão de Serviço',             'Comissão de Serviço',             'MOBILIDADE', false, true,  false, true, 'MANTEM',    NULL, 'REGRESSA_OU_CESSA', 1095, NULL, NOW(), 'system'),
('f1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e5', 'MOB_REQUISICAO',       'Requisição',                      'Requisição',                      'MOBILIDADE', false, true,  false, true, 'MANTEM',    NULL, 'REGRESSA_LUGAR',  365,  1,    NOW(), 'system'),
('f1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e6', 'MOB_DESTACAMENTO',     'Destacamento',                    'Destacamento',                    'MOBILIDADE', false, true,  false, true, 'MANTEM',    NULL, 'REGRESSA_LUGAR',  365,  1,    NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- =============================================================
-- 7. Feriados Nacionais de Cabo Verde 2026 (t_public_holiday)
-- 11 feriados nacionais (is_national=true)
-- Sexta-feira Santa 2026: 03 Abr (Páscoa a 05 Abr); Corpus Christi: 04 Jun
-- =============================================================
INSERT INTO t_public_holiday (id, name, holiday_date, is_national, description, is_active, created_date, created_by) VALUES
('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e101', 'Ano Novo',                      '2026-01-01', true, 'Feriado nacional', true, NOW(), 'seed'),
('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e102', 'Dia da Liberdade e Democracia', '2026-01-13', true, 'Feriado nacional', true, NOW(), 'seed'),
('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e103', 'Dia dos Heróis Nacionais',      '2026-01-20', true, 'Feriado nacional', true, NOW(), 'seed'),
('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e104', 'Sexta-feira Santa',             '2026-04-03', true, 'Feriado nacional', true, NOW(), 'seed'),
('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e105', 'Dia do Trabalhador',            '2026-05-01', true, 'Feriado nacional', true, NOW(), 'seed'),
('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e106', 'Dia da Criança',                '2026-06-01', true, 'Feriado nacional', true, NOW(), 'seed'),
('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e107', 'Corpus Christi',                '2026-06-04', true, 'Feriado nacional', true, NOW(), 'seed'),
('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e108', 'Dia da Independência',          '2026-07-05', true, 'Feriado nacional', true, NOW(), 'seed'),
('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e109', 'Assunção de Nossa Senhora',     '2026-08-15', true, 'Feriado nacional', true, NOW(), 'seed'),
('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e110', 'Dia de Todos os Santos',        '2026-11-01', true, 'Feriado nacional', true, NOW(), 'seed'),
('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e111', 'Natal',                         '2026-12-25', true, 'Feriado nacional', true, NOW(), 'seed')
ON CONFLICT (id) DO NOTHING;

-- =============================================================
-- 8. Lookups Genéricos (t_option_entity)
-- Os campos do funcionário que referenciam estes grupos guardam o ckey
-- directamente como VARCHAR — não há FK, a validação é aplicacional.
-- =============================================================

-- UNIT_TYPE — tipo de unidade orgânica
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active, description, created_date, created_by) VALUES
('21e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e201', 'UNIT_TYPE', 'MINISTRY',   'Ministério',    'pt', 1, true, 'Ministério',    NOW(), 'system'),
('21e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e202', 'UNIT_TYPE', 'DIRECTION',  'Direção Geral', 'pt', 2, true, 'Direção Geral', NOW(), 'system'),
('21e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e203', 'UNIT_TYPE', 'SERVICE',    'Serviço',       'pt', 3, true, 'Serviço',       NOW(), 'system'),
('21e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e204', 'UNIT_TYPE', 'SECTION',    'Secção',        'pt', 4, true, 'Secção',        NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- MARITAL_STATUS — estado civil (t_funcionario.estado_civil)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active, description, created_date, created_by) VALUES
('22000001-0000-0000-0000-000000000001', 'MARITAL_STATUS', 'SOLTEIRO',    'Solteiro(a)',    'pt', 1, true, 'Solteiro(a)',    NOW(), 'system'),
('22000001-0000-0000-0000-000000000002', 'MARITAL_STATUS', 'CASADO',      'Casado(a)',      'pt', 2, true, 'Casado(a)',      NOW(), 'system'),
('22000001-0000-0000-0000-000000000003', 'MARITAL_STATUS', 'DIVORCIADO',  'Divorciado(a)',  'pt', 3, true, 'Divorciado(a)', NOW(), 'system'),
('22000001-0000-0000-0000-000000000004', 'MARITAL_STATUS', 'VIUVO',       'Viúvo(a)',       'pt', 4, true, 'Viúvo(a)',       NOW(), 'system'),
('22000001-0000-0000-0000-000000000005', 'MARITAL_STATUS', 'UNIAO_FACTO', 'União de Facto', 'pt', 5, true, 'União de Facto', NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- SEX — género (t_funcionario.genero)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active, description, created_date, created_by) VALUES
('23000001-0000-0000-0000-000000000001', 'SEX', 'M', 'Masculino', 'pt', 1, true, 'Masculino', NOW(), 'system'),
('23000001-0000-0000-0000-000000000002', 'SEX', 'F', 'Feminino',  'pt', 2, true, 'Feminino',  NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- NATIONALITY — nacionalidade (t_funcionario.nacionalidade, t_qualificacao.pais_instituicao)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active, description, created_date, created_by) VALUES
('24000001-0000-0000-0000-000000000001', 'NATIONALITY', 'CV',    'Cabo-verdiana', 'pt', 1,  true, 'Cabo-verdiana', NOW(), 'system'),
('24000001-0000-0000-0000-000000000002', 'NATIONALITY', 'PT',    'Portuguesa',    'pt', 2,  true, 'Portuguesa',    NOW(), 'system'),
('24000001-0000-0000-0000-000000000003', 'NATIONALITY', 'SN',    'Senegalesa',    'pt', 3,  true, 'Senegalesa',    NOW(), 'system'),
('24000001-0000-0000-0000-000000000004', 'NATIONALITY', 'GW',    'Guineense',     'pt', 4,  true, 'Guineense',     NOW(), 'system'),
('24000001-0000-0000-0000-000000000005', 'NATIONALITY', 'BR',    'Brasileira',    'pt', 5,  true, 'Brasileira',    NOW(), 'system'),
('24000001-0000-0000-0000-000000000006', 'NATIONALITY', 'FR',    'Francesa',      'pt', 6,  true, 'Francesa',      NOW(), 'system'),
('24000001-0000-0000-0000-000000000007', 'NATIONALITY', 'ES',    'Espanhola',     'pt', 7,  true, 'Espanhola',     NOW(), 'system'),
('24000001-0000-0000-0000-000000000008', 'NATIONALITY', 'US',    'Americana',     'pt', 8,  true, 'Americana',     NOW(), 'system'),
('24000001-0000-0000-0000-000000000099', 'NATIONALITY', 'OTHER', 'Outra',         'pt', 99, true, 'Outra',         NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- ISLAND — ilha (t_funcionario.ilha)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active, description, created_date, created_by) VALUES
('25000001-0000-0000-0000-000000000001', 'ISLAND', 'SANTIAGO',    'Santiago',    'pt', 1, true, 'Ilha de Santiago',    NOW(), 'system'),
('25000001-0000-0000-0000-000000000002', 'ISLAND', 'SAO_VICENTE', 'São Vicente', 'pt', 2, true, 'Ilha de São Vicente', NOW(), 'system'),
('25000001-0000-0000-0000-000000000003', 'ISLAND', 'SANTO_ANTAO', 'Santo Antão', 'pt', 3, true, 'Ilha de Santo Antão', NOW(), 'system'),
('25000001-0000-0000-0000-000000000004', 'ISLAND', 'SAL',         'Sal',         'pt', 4, true, 'Ilha do Sal',         NOW(), 'system'),
('25000001-0000-0000-0000-000000000005', 'ISLAND', 'BOA_VISTA',   'Boa Vista',   'pt', 5, true, 'Ilha da Boa Vista',   NOW(), 'system'),
('25000001-0000-0000-0000-000000000006', 'ISLAND', 'SAO_NICOLAU', 'São Nicolau', 'pt', 6, true, 'Ilha de São Nicolau', NOW(), 'system'),
('25000001-0000-0000-0000-000000000007', 'ISLAND', 'FOGO',        'Fogo',        'pt', 7, true, 'Ilha do Fogo',        NOW(), 'system'),
('25000001-0000-0000-0000-000000000008', 'ISLAND', 'MAIO',        'Maio',        'pt', 8, true, 'Ilha do Maio',        NOW(), 'system'),
('25000001-0000-0000-0000-000000000009', 'ISLAND', 'BRAVA',       'Brava',       'pt', 9, true, 'Ilha Brava',          NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- CONCELHO — município (t_funcionario.concelho) — 22 concelhos de Cabo Verde
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active, description, created_date, created_by) VALUES
-- Santiago (9)
('26000001-0000-0000-0000-000000000001', 'CONCELHO', 'PRAIA',                'Praia',                      'pt', 1,  true, 'Praia (Santiago)',                NOW(), 'system'),
('26000001-0000-0000-0000-000000000002', 'CONCELHO', 'SAO_DOMINGOS',         'São Domingos',               'pt', 2,  true, 'São Domingos (Santiago)',         NOW(), 'system'),
('26000001-0000-0000-0000-000000000003', 'CONCELHO', 'SANTA_CRUZ',           'Santa Cruz',                 'pt', 3,  true, 'Santa Cruz (Santiago)',           NOW(), 'system'),
('26000001-0000-0000-0000-000000000004', 'CONCELHO', 'TARRAFAL_STGO',        'Tarrafal',                   'pt', 4,  true, 'Tarrafal (Santiago)',             NOW(), 'system'),
('26000001-0000-0000-0000-000000000005', 'CONCELHO', 'SANTA_CATARINA_STGO',  'Santa Catarina',             'pt', 5,  true, 'Santa Catarina (Santiago)',       NOW(), 'system'),
('26000001-0000-0000-0000-000000000006', 'CONCELHO', 'SAO_SALVADOR',         'São Salvador do Mundo',      'pt', 6,  true, 'São Salvador do Mundo',          NOW(), 'system'),
('26000001-0000-0000-0000-000000000007', 'CONCELHO', 'SAO_LOURENCO',         'São Lourenço dos Órgãos',    'pt', 7,  true, 'São Lourenço dos Órgãos',        NOW(), 'system'),
('26000001-0000-0000-0000-000000000008', 'CONCELHO', 'SAO_MIGUEL',           'São Miguel',                 'pt', 8,  true, 'São Miguel (Santiago)',           NOW(), 'system'),
('26000001-0000-0000-0000-000000000009', 'CONCELHO', 'RIBEIRA_GRANDE_STGO',  'Ribeira Grande de Santiago', 'pt', 9,  true, 'Ribeira Grande de Santiago',     NOW(), 'system'),
-- São Vicente (1)
('26000001-0000-0000-0000-000000000010', 'CONCELHO', 'SAO_VICENTE_C',        'São Vicente',                'pt', 10, true, 'São Vicente (Mindelo)',           NOW(), 'system'),
-- Santo Antão (3)
('26000001-0000-0000-0000-000000000011', 'CONCELHO', 'PORTO_NOVO',           'Porto Novo',                 'pt', 11, true, 'Porto Novo (Santo Antão)',        NOW(), 'system'),
('26000001-0000-0000-0000-000000000012', 'CONCELHO', 'RIBEIRA_GRANDE_SA',    'Ribeira Grande',             'pt', 12, true, 'Ribeira Grande (Santo Antão)',    NOW(), 'system'),
('26000001-0000-0000-0000-000000000013', 'CONCELHO', 'PAUL',                 'Paul',                       'pt', 13, true, 'Paul (Santo Antão)',              NOW(), 'system'),
-- Sal (1)
('26000001-0000-0000-0000-000000000014', 'CONCELHO', 'SAL_C',                'Sal',                        'pt', 14, true, 'Sal',                            NOW(), 'system'),
-- Boa Vista (1)
('26000001-0000-0000-0000-000000000015', 'CONCELHO', 'BOA_VISTA_C',          'Boa Vista',                  'pt', 15, true, 'Boa Vista',                      NOW(), 'system'),
-- Maio (1)
('26000001-0000-0000-0000-000000000016', 'CONCELHO', 'MAIO_C',               'Maio',                       'pt', 16, true, 'Maio',                           NOW(), 'system'),
-- São Nicolau (2)
('26000001-0000-0000-0000-000000000017', 'CONCELHO', 'RIBEIRA_BRAVA_SN',     'Ribeira Brava',              'pt', 17, true, 'Ribeira Brava (São Nicolau)',     NOW(), 'system'),
('26000001-0000-0000-0000-000000000018', 'CONCELHO', 'TARRAFAL_SN',          'Tarrafal de São Nicolau',    'pt', 18, true, 'Tarrafal de São Nicolau',        NOW(), 'system'),
-- Fogo (3)
('26000001-0000-0000-0000-000000000019', 'CONCELHO', 'SAO_FILIPE',           'São Filipe',                 'pt', 19, true, 'São Filipe (Fogo)',               NOW(), 'system'),
('26000001-0000-0000-0000-000000000020', 'CONCELHO', 'SANTA_CATARINA_FOGO',  'Santa Catarina do Fogo',     'pt', 20, true, 'Santa Catarina do Fogo',         NOW(), 'system'),
('26000001-0000-0000-0000-000000000021', 'CONCELHO', 'MOSTEIROS',            'Mosteiros',                  'pt', 21, true, 'Mosteiros (Fogo)',                NOW(), 'system'),
-- Brava (1)
('26000001-0000-0000-0000-000000000022', 'CONCELHO', 'BRAVA_C',              'Brava',                      'pt', 22, true, 'Brava',                          NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- RELATIONSHIP_TYPE — parentesco (t_dependente.relationship_type)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active, description, created_date, created_by) VALUES
('27000001-0000-0000-0000-000000000001', 'RELATIONSHIP_TYPE', 'CONJUGE', 'Cônjuge',    'pt', 1, true, 'Cônjuge ou companheiro(a)',       NOW(), 'system'),
('27000001-0000-0000-0000-000000000002', 'RELATIONSHIP_TYPE', 'FILHO',   'Filho(a)',   'pt', 2, true, 'Filho(a) biológico ou adoptado', NOW(), 'system'),
('27000001-0000-0000-0000-000000000003', 'RELATIONSHIP_TYPE', 'PAI',     'Pai',        'pt', 3, true, 'Pai',                            NOW(), 'system'),
('27000001-0000-0000-0000-000000000004', 'RELATIONSHIP_TYPE', 'MAE',     'Mãe',        'pt', 4, true, 'Mãe',                            NOW(), 'system'),
('27000001-0000-0000-0000-000000000005', 'RELATIONSHIP_TYPE', 'ENTEADO', 'Enteado(a)', 'pt', 5, true, 'Enteado(a)',                     NOW(), 'system'),
('27000001-0000-0000-0000-000000000006', 'RELATIONSHIP_TYPE', 'IRMAO',   'Irmão/Irmã', 'pt', 6, true, 'Irmão ou irmã',                  NOW(), 'system'),
('27000001-0000-0000-0000-000000000007', 'RELATIONSHIP_TYPE', 'OUTRO',   'Outro',      'pt', 7, true, 'Outro parentesco',               NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- QUALIFICATION_LEVEL — nível académico (t_qualificacao.level)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active, description, created_date, created_by) VALUES
('28000001-0000-0000-0000-000000000001', 'QUALIFICATION_LEVEL', 'BASICO',           'Ensino Básico',    'pt', 1, true, '1.º ao 6.º ano',   NOW(), 'system'),
('28000001-0000-0000-0000-000000000002', 'QUALIFICATION_LEVEL', 'SECUNDARIO',       'Ensino Secundário','pt', 2, true, '7.º ao 12.º ano',  NOW(), 'system'),
('28000001-0000-0000-0000-000000000003', 'QUALIFICATION_LEVEL', 'BACHAREL',         'Bacharelato',      'pt', 3, true, 'Bacharelato',      NOW(), 'system'),
('28000001-0000-0000-0000-000000000004', 'QUALIFICATION_LEVEL', 'LICENCIATURA',     'Licenciatura',     'pt', 4, true, 'Licenciatura',     NOW(), 'system'),
('28000001-0000-0000-0000-000000000005', 'QUALIFICATION_LEVEL', 'MESTRADO',         'Mestrado',         'pt', 5, true, 'Mestrado',         NOW(), 'system'),
('28000001-0000-0000-0000-000000000006', 'QUALIFICATION_LEVEL', 'DOUTORAMENTO',     'Doutoramento',     'pt', 6, true, 'Doutoramento',     NOW(), 'system'),
('28000001-0000-0000-0000-000000000007', 'QUALIFICATION_LEVEL', 'POS_DOUTORAMENTO', 'Pós-doutoramento', 'pt', 7, true, 'Pós-doutoramento', NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- TRAINING_TYPE — tipo de formação (t_training.training_type)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active, description, created_date, created_by) VALUES
('29000001-0000-0000-0000-000000000001', 'TRAINING_TYPE', 'CURSO',       'Curso',       'pt', 1, true, 'Curso formal',       NOW(), 'system'),
('29000001-0000-0000-0000-000000000002', 'TRAINING_TYPE', 'SEMINARIO',   'Seminário',   'pt', 2, true, 'Seminário',          NOW(), 'system'),
('29000001-0000-0000-0000-000000000003', 'TRAINING_TYPE', 'WORKSHOP',    'Workshop',    'pt', 3, true, 'Workshop / Oficina', NOW(), 'system'),
('29000001-0000-0000-0000-000000000004', 'TRAINING_TYPE', 'CONGRESSO',   'Congresso',   'pt', 4, true, 'Congresso',          NOW(), 'system'),
('29000001-0000-0000-0000-000000000005', 'TRAINING_TYPE', 'CONFERENCIA', 'Conferência', 'pt', 5, true, 'Conferência',        NOW(), 'system'),
('29000001-0000-0000-0000-000000000006', 'TRAINING_TYPE', 'ELEARNING',   'E-Learning',  'pt', 6, true, 'Formação online',    NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- DOC_CATEGORY — categoria de documento (t_tipo_documento.category)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active, description, created_date, created_by) VALUES
('2a000001-0000-0000-0000-000000000001', 'DOC_CATEGORY', 'IDENTIFICACAO', 'Identificação', 'pt', 1, true, 'Documentos de identidade',  NOW(), 'system'),
('2a000001-0000-0000-0000-000000000002', 'DOC_CATEGORY', 'CONTRATO',      'Contrato',      'pt', 2, true, 'Documentos contratuais',    NOW(), 'system'),
('2a000001-0000-0000-0000-000000000003', 'DOC_CATEGORY', 'FORMACAO',      'Formação',      'pt', 3, true, 'Certificados de formação',  NOW(), 'system'),
('2a000001-0000-0000-0000-000000000004', 'DOC_CATEGORY', 'DISCIPLINAR',   'Disciplinar',   'pt', 4, true, 'Documentos disciplinares',  NOW(), 'system'),
('2a000001-0000-0000-0000-000000000005', 'DOC_CATEGORY', 'MEDICO',        'Médico',        'pt', 5, true, 'Documentos médicos',        NOW(), 'system'),
('2a000001-0000-0000-0000-000000000006', 'DOC_CATEGORY', 'FINANCEIRO',    'Financeiro',    'pt', 6, true, 'Documentos financeiros',    NOW(), 'system'),
('2a000001-0000-0000-0000-000000000007', 'DOC_CATEGORY', 'OUTRO',         'Outro',         'pt', 7, true, 'Outros documentos',         NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- LEAVE_CATEGORY — categoria de tipo de ausência (t_leave_type.category)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active, description, created_date, created_by) VALUES
('2b000001-0000-0000-0000-000000000001', 'LEAVE_CATEGORY', 'GOZAMENTO', 'Gozamento', 'pt', 1, true, 'Férias e descanso',             NOW(), 'system'),
('2b000001-0000-0000-0000-000000000002', 'LEAVE_CATEGORY', 'SAUDE',     'Saúde',     'pt', 2, true, 'Ausência por motivo de saúde', NOW(), 'system'),
('2b000001-0000-0000-0000-000000000003', 'LEAVE_CATEGORY', 'FAMILIAR',  'Familiar',  'pt', 3, true, 'Eventos familiares',            NOW(), 'system'),
('2b000001-0000-0000-0000-000000000004', 'LEAVE_CATEGORY', 'PESSOAL',   'Pessoal',   'pt', 4, true, 'Faltas pessoais',               NOW(), 'system'),
('2b000001-0000-0000-0000-000000000005', 'LEAVE_CATEGORY', 'LEGAL',     'Legal',     'pt', 5, true, 'Obrigações legais',             NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- CAREER_REGIME — regime da carreira PCFR (t_career.regime)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active, description, created_date, created_by) VALUES
('2c000001-0000-0000-0000-000000000001', 'CAREER_REGIME', 'GERAL',     'Regime Geral',     'pt', 1, true, 'PCFR Regime Geral',     NOW(), 'system'),
('2c000001-0000-0000-0000-000000000002', 'CAREER_REGIME', 'ESPECIAL',  'Regime Especial',  'pt', 2, true, 'PCFR Regime Especial',  NOW(), 'system'),
('2c000001-0000-0000-0000-000000000003', 'CAREER_REGIME', 'DIRIGENTE', 'Regime Dirigente', 'pt', 3, true, 'PCFR Regime Dirigente', NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- BANCO — banco para dados bancários (t_dados_bancarios.banco)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active, description, created_date, created_by) VALUES
('2d000001-0000-0000-0000-000000000001', 'BANCO', 'BCA',        'Banco Comercial do Atlântico',    'pt', 1, true, 'BCA',       NOW(), 'system'),
('2d000001-0000-0000-0000-000000000002', 'BANCO', 'BCN',        'Banco Cabo-verdiano de Negócios', 'pt', 2, true, 'BCN',       NOW(), 'system'),
('2d000001-0000-0000-0000-000000000003', 'BANCO', 'CAIXA_CV',   'Caixa Económica de Cabo Verde',   'pt', 3, true, 'Caixa',     NOW(), 'system'),
('2d000001-0000-0000-0000-000000000004', 'BANCO', 'ECOBANK',    'EcoBank Cabo Verde',              'pt', 4, true, 'EcoBank',   NOW(), 'system'),
('2d000001-0000-0000-0000-000000000005', 'BANCO', 'NOVO_BANCO', 'Novo Banco',                      'pt', 5, true, 'Novo Banco',NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- WORK_REGIME — regime de trabalho (t_contrato.regime_trabalho)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active, description, created_date, created_by) VALUES
('2e000001-0000-0000-0000-000000000001', 'WORK_REGIME', 'TEMPO_COMPLETO',      'Tempo Completo',      'pt', 1, true, 'Horário normal completo',          NOW(), 'system'),
('2e000001-0000-0000-0000-000000000002', 'WORK_REGIME', 'TEMPO_PARCIAL',       'Tempo Parcial',       'pt', 2, true, 'Percentagem acordada do horário',  NOW(), 'system'),
('2e000001-0000-0000-0000-000000000003', 'WORK_REGIME', 'ISENCAO_HORARIO',     'Isenção de Horário',  'pt', 3, true, 'Sem horário fixo obrigatório',     NOW(), 'system'),
('2e000001-0000-0000-0000-000000000004', 'WORK_REGIME', 'DEDICACAO_EXCLUSIVA', 'Dedicação Exclusiva', 'pt', 4, true, 'Exclusividade ao serviço público', NOW(), 'system')
ON CONFLICT (id) DO NOTHING;

-- WORKER_STATE_REASON — motivos de mudança de estado do colaborador
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active, description, created_date, created_by) VALUES
('2f000001-0000-0000-0000-000000000001', 'WORKER_STATE_REASON', 'DISCIPLINARY_SUSPENSION', 'Suspensão disciplinar',         'pt', 1, true, 'Suspensão na sequência de processo disciplinar',      NOW(), 'system'),
('2f000001-0000-0000-0000-000000000002', 'WORKER_STATE_REASON', 'MEDICAL_SUSPENSION',      'Suspensão por razões médicas',  'pt', 2, true, 'Incapacidade prolongada por doença ou acidente',      NOW(), 'system'),
('2f000001-0000-0000-0000-000000000003', 'WORKER_STATE_REASON', 'OWN_REQUEST_SUSPENSION',  'Suspensão a pedido próprio',    'pt', 3, true, 'Licença sem vencimento ou situação assimilada',       NOW(), 'system'),
('2f000001-0000-0000-0000-000000000004', 'WORKER_STATE_REASON', 'AGE_RETIREMENT',          'Aposentação por limite de idade','pt',4, true, 'Limite de idade previsto no Estatuto do Pessoal',    NOW(), 'system'),
('2f000001-0000-0000-0000-000000000005', 'WORKER_STATE_REASON', 'DISABILITY_RETIREMENT',   'Aposentação por invalidez',     'pt', 5, true, 'Incapacidade permanente para o exercício de funções', NOW(), 'system'),
('2f000001-0000-0000-0000-000000000006', 'WORKER_STATE_REASON', 'VOLUNTARY_RETIREMENT',    'Aposentação voluntária',        'pt', 6, true, 'Reforma antecipada por vontade do trabalhador',       NOW(), 'system'),
('2f000001-0000-0000-0000-000000000007', 'WORKER_STATE_REASON', 'CONTRACT_TERMINATION',    'Extinção do vínculo',           'pt', 7, true, 'Término natural ou administrativo do contrato',       NOW(), 'system'),
('2f000001-0000-0000-0000-000000000008', 'WORKER_STATE_REASON', 'MUTUAL_AGREEMENT',        'Rescisão por mútuo acordo',     'pt', 8, true, 'Acordo entre as partes para cessação do contrato',    NOW(), 'system'),
('2f000001-0000-0000-0000-000000000009', 'WORKER_STATE_REASON', 'DISCIPLINARY_DISMISSAL',  'Rescisão disciplinar',          'pt', 9, true, 'Demissão na sequência de processo disciplinar grave',  NOW(), 'system'),
('2f000001-0000-0000-0000-000000000010', 'WORKER_STATE_REASON', 'DEATH',                   'Falecimento',                   'pt',10, true, 'Cessação por morte do trabalhador',                   NOW(), 'system'),
('2f000001-0000-0000-0000-000000000011', 'WORKER_STATE_REASON', 'SUSPENSION_RETURN',       'Regresso de suspensão',         'pt',11, true, 'Retorno ao serviço activo após suspensão',            NOW(), 'system'),
('2f000001-0000-0000-0000-000000000012', 'WORKER_STATE_REASON', 'REINTEGRATION',           'Reintegração',                  'pt',12, true, 'Reintegração na sequência de decisão judicial/adm.',  NOW(), 'system')
ON CONFLICT (id) DO NOTHING;
