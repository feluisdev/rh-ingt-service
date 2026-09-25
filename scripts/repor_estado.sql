-- Gerado a partir de testes_funcionais_README.md (seccao 'Repor o estado inicial').
-- Correr antes de cada execucao da bateria.

-- 0. Colaboradores que a bateria cria (F11 admite gente a cada execucao, para provar o
--    vencimento de ferias). Nunca eram apagados: ao fim de algumas corridas havia dezenas, e
--    como o F0.1 le a PRIMEIRA PAGINA de /funcionarios, o trio do seed saia dela -- a bateria
--    falhava do F0 em diante por dados acumulados, nao por codigo. Apaga-se antes de tudo o
--    resto, pela ordem das chaves estrangeiras.
DO $$
DECLARE extras uuid[];
BEGIN
  SELECT coalesce(array_agg(id), '{}') INTO extras FROM t_funcionario
   WHERE numero_funcionario NOT IN ('0000001','0000002','0000003');
  IF array_length(extras, 1) IS NULL THEN RETURN; END IF;

  UPDATE t_unidade_organica SET responsible_employee_id = NULL
   WHERE responsible_employee_id = ANY(extras);

  DELETE FROM t_leave_request  WHERE funcionario_id = ANY(extras);
  DELETE FROM t_leave_balance  WHERE funcionario_id = ANY(extras);
  DELETE FROM t_leave_mobility WHERE funcionario_id = ANY(extras);
  DELETE FROM t_assignment     WHERE funcionario_id = ANY(extras);
  DELETE FROM t_contrato       WHERE funcionario_id = ANY(extras);
  DELETE FROM t_dados_bancarios WHERE funcionario_id = ANY(extras);
  DELETE FROM t_dependente     WHERE funcionario_id = ANY(extras);
  DELETE FROM t_disciplinary_process WHERE funcionario_id = ANY(extras);
  DELETE FROM t_historico_estado_colaborador WHERE funcionario_id = ANY(extras);
  DELETE FROM t_payroll_slip   WHERE funcionario_id = ANY(extras);
  DELETE FROM t_qualificacao   WHERE funcionario_id = ANY(extras);
  DELETE FROM t_training       WHERE funcionario_id = ANY(extras);
  IF to_regclass('public.t_processo_aposentacao') IS NOT NULL THEN
    DELETE FROM t_processo_aposentacao WHERE funcionario_id = ANY(extras);
  END IF;
  IF to_regclass('public.t_prorrogacao_permanencia') IS NOT NULL THEN
    DELETE FROM t_prorrogacao_permanencia WHERE funcionario_id = ANY(extras);
  END IF;
  IF to_regclass('public.t_pedido_declaracao') IS NOT NULL THEN
    DELETE FROM t_pedido_declaracao WHERE funcionario_id = ANY(extras);
  END IF;
  IF to_regclass('public.t_documento_emitido') IS NOT NULL THEN
    DELETE FROM t_documento_emitido WHERE funcionario_id = ANY(extras);
  END IF;
  IF to_regclass('public.t_reclamacao_antiguidade') IS NOT NULL THEN
    DELETE FROM t_reclamacao_antiguidade WHERE funcionario_id = ANY(extras);
  END IF;
  -- Diario de factos para o salarial (FK para t_funcionario): sai antes dos colaboradores.
  IF to_regclass('public.t_facto_rh') IS NOT NULL THEN
    DELETE FROM t_facto_rh WHERE funcionario_id = ANY(extras);
  END IF;
  -- Tabelas do ddl-auto (assiduidade, ferias): o F29 pica e atribui horario a um admitido do F11.
  IF to_regclass('public.t_trabalho_suplementar') IS NOT NULL THEN
    DELETE FROM t_trabalho_suplementar WHERE funcionario_id = ANY(extras);
  END IF;
  IF to_regclass('public.t_marcacao_assiduidade') IS NOT NULL THEN
    DELETE FROM t_marcacao_assiduidade WHERE funcionario_id = ANY(extras);
  END IF;
  IF to_regclass('public.t_horario_colaborador') IS NOT NULL THEN
    DELETE FROM t_horario_colaborador WHERE funcionario_id = ANY(extras);
  END IF;
  IF to_regclass('public.t_ferias_ano') IS NOT NULL THEN
    DELETE FROM t_ferias_ano_alteracao WHERE ferias_ano_id IN (SELECT id FROM t_ferias_ano WHERE funcionario_id = ANY(extras));
    DELETE FROM t_ferias_ano_periodo WHERE ferias_ano_id IN (SELECT id FROM t_ferias_ano WHERE funcionario_id = ANY(extras));
    DELETE FROM t_ferias_ano WHERE funcionario_id = ANY(extras);
  END IF;
  DELETE FROM t_funcionario    WHERE id = ANY(extras);
END $$;

-- 0b. O que os dados de demonstracao (scripts/dados_demonstracao.ps1) criam alem dos colaboradores:
--     os Lugares DEMO-* e as unidades DEMO_*. Os colaboradores ja sairam no ponto 0.
DO $$ BEGIN
  UPDATE t_unidade_organica SET responsible_employee_id = NULL
   WHERE responsible_employee_id IS NOT NULL
     AND responsible_employee_id NOT IN (SELECT id FROM t_funcionario);
  DELETE FROM t_position WHERE numero_lugar LIKE 'DEMO-%';
  IF to_regclass('public.t_unidade_organica_horario') IS NOT NULL THEN
    DELETE FROM t_unidade_organica_horario WHERE unidade_id IN (SELECT id FROM t_unidade_organica WHERE code LIKE 'DEMO\_%');
  END IF;
  DELETE FROM t_unidade_organica WHERE code LIKE 'DEMO\_%';
END $$;

-- 1. Afectacoes: fica so a do seed, com o escalao de partida.
--    A clausula 'not in' apanha tambem as substituicoes e o que os movimentos criaram.
delete from t_assignment where id not in (
  'e6e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ee01'::uuid,
  'e6e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ee02'::uuid,
  'e6e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ee03'::uuid);
update t_assignment set data_fim=null, is_current=true, assignment_type='PRINCIPAL', titular_assignment_id=null,
       position_id='d5e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ed01'::uuid, grade_id='81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e801'::uuid
 where funcionario_id='91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901'::uuid;
update t_assignment set data_fim=null, is_current=true, assignment_type='PRINCIPAL', titular_assignment_id=null,
       position_id='d5e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ed02'::uuid, grade_id='81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e803'::uuid
 where funcionario_id='91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e902'::uuid;
update t_assignment set data_fim=null, is_current=true, assignment_type='PRINCIPAL', titular_assignment_id=null,
       position_id='d5e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ed03'::uuid, grade_id='81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e803'::uuid
 where funcionario_id='91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e903'::uuid;

-- 2. Lugares: o LUG-0006 e congelado de proposito; os outros voltam a ATIVO.
update t_position set estado='ATIVO', is_active=true where numero_lugar <> 'LUG-0006';
update t_position set estado='CONGELADO', is_active=true where numero_lugar = 'LUG-0006';
--    V59: o motivo do estado (congelar/descongelar) volta a nulo, como nos Lugares de antes.
update t_position set estado_motivo=null, estado_despacho=null, estado_desde=null;
--    A promocao sem positionId reclassifica o Lugar: repor a categoria de origem.
update t_position set category_id='71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e701'::uuid where numero_lugar in ('LUG-0001','LUG-0004');
update t_position set category_id='71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e702'::uuid where numero_lugar in ('LUG-0002','LUG-0003','LUG-0005','LUG-0006');
--    O LUG-0007 e da outra carreira (Regime Especial): a mudanca de carreira
--    ocupa-o, e sem repor carreira E categoria a execucao seguinte nao tem destino.
update t_position set career_id='61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e602'::uuid,
       category_id='71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e703'::uuid where numero_lugar = 'LUG-0007';
--    O LUG-0008 vive noutra unidade (DGP) e e o destino da consolidacao da mobilidade.
update t_position set unidade_organica_id='31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e302'::uuid,
       career_id='61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e601'::uuid,
       category_id='71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e702'::uuid where numero_lugar = 'LUG-0008';

-- 3. Estado e contratos.
update t_funcionario set worker_state_id=(select id from t_worker_state where code='ACTIVE'), is_active=true;
update t_contrato set status='ATIVO', is_current=true, end_date=null;

-- 4. Registos que a bateria cria.
delete from t_leave_mobility; delete from t_leave_request; delete from t_leave_balance;
--    O mapa de ferias (tabelas do ddl-auto: podem ainda nao existir numa base acabada de criar).
DO $$ BEGIN
  IF to_regclass('public.t_ferias_ano') IS NOT NULL THEN
    DELETE FROM t_ferias_ano_alteracao; DELETE FROM t_ferias_ano_periodo; DELETE FROM t_ferias_ano;
  END IF;
  IF to_regclass('public.t_ferias_mapa') IS NOT NULL THEN
    DELETE FROM t_ferias_mapa;
  END IF;
END $$;
delete from t_historico_estado_colaborador;

-- 5. Lixo de catalogo deixado por execucoes anteriores.
delete from t_option_entity where ccode like 'TESTE%';
delete from t_worker_state where code like 'TESTE%';
delete from t_leave_mobility_subtype where code like 'TESTE%' or code like '%_TESTE' or code like 'LIC_TST_%' or code like 'MOB_TST_%';
--    O F11 cria um tipo de ausencia a cada execucao (REG_TST_<hora>) para provar que a
--    classificacao do regime e da instituicao. Nunca era apagado: ao fim de algumas corridas
--    havia quinze, e o catalogo que o front-end carrega enchia-se de lixo. Mesma falha que os
--    colaboradores extras tinham no ponto 0. O F20 cria outro (CNT_TST_<hora>), em dias uteis.
delete from t_leave_type where code like 'REG_TST_%' or code like 'CNT_TST_%' or code like 'TESTE%';

-- 6. Catalogo que o seed nao consegue corrigir sozinho.
--    O seed_parametrizacoes usa ON CONFLICT (code) DO NOTHING: numa base ja criada, a linha
--    antiga fica como estava. A comissao de servico foi reclassificada (art. 64.o n.o 2:
--    REGRESSA_OU_CESSA) e a duracao corrigida para tres anos sucessivamente renovaveis
--    (art. 60.o n.o 1), e sem isto o F17 corre contra o catalogo velho e nao prova nada.
update t_leave_mobility_subtype
   set return_effect='REGRESSA_OU_CESSA', max_duration_days=1095, max_extensions=NULL
 where code='MOB_COMISSAO';

--    Pela mesma razao, os limites do art. 15.o nas linhas que ja existiam antes da V53. O luto
--    dizia 5 dias POR ANO, o que recusava o segundo funeral do mesmo ano e deixava passar oito
--    dias seguidos de uma so vez; a lei conta 8 por falecimento do conjuge ou 1.o grau (al. b).
--    A doenca com declaracao medica sao 3 consecutivas (al. d).
update t_leave_type set max_days_per_year=NULL, max_days_per_occurrence=8 where code='LUTO';
update t_leave_type set max_days_per_occurrence=3 where code='DOENCA';

--    E, pela mesma razao, a classificacao da V54 nas linhas que ja existiam. A migracao NAO as
--    classifica de proposito -- estas duas colunas mandam descontar antiguidade e mexer em
--    salarios, e decidir por uma instituicao em silencio seria o pior sitio para o fazer.
update t_leave_type set regime='FALTA_INJUSTIFICADA', efeito_remuneracao='DEPENDE_DA_OPCAO'
 where code='FALTA_INJUSTIFICADA';
update t_leave_type set efeito_remuneracao='PERDA_PARCIAL'
 where code in ('DOENCA','DOENCA_ATESTADO','MATERNIDADE','PATERNIDADE','ASSISTENCIA_FAMILIA');
update t_leave_type set efeito_remuneracao='PERDA_TOTAL' where code='GREVE';
--    E a falta injustificada deixa de descontar saldo do proprio tipo: um saldo de faltas
--    injustificadas seria uma quota de faltar. O que ela desconta, se for essa a opcao do
--    art. 43.o n.o 2, sao ferias.
update t_leave_type set deducts_balance=false where code='FALTA_INJUSTIFICADA';

--    E, pela mesma razao, a recorrencia da V55. A migracao nao marca feriado nenhum como
--    recorrente (o catalogo e da instituicao); o seed ja os traz marcados, mas numa base ja
--    criada o ON CONFLICT deixa-os como estavam -- e a 1 de Janeiro de 2027 a contagem de dias
--    uteis ficava sem feriados. Os moveis de 2027 entram pela mesma razao.
update t_public_holiday set is_recurring=true, area_ckey=null
 where id in ('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e101','11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e102',
              '11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e103','11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e105',
              '11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e106','11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e108',
              '11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e109','11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e110',
              '11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e111');
update t_public_holiday set is_active=true where created_by='seed';
insert into t_public_holiday (id, name, holiday_date, is_national, is_recurring, description, is_active, created_date, created_by) values
('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e112', 'Sexta-feira Santa', '2027-03-26', true, false, 'Feriado nacional', true, now(), 'seed'),
('11e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e113', 'Corpus Christi',    '2027-05-27', true, false, 'Feriado nacional', true, now(), 'seed')
on conflict (id) do nothing;

-- 7. Feriados e areas que a bateria cria (V55). Os feriados de teste sao os que o seed nao
--    trouxe; as areas de teste comecam por TST_. A area das unidades volta a nula: e o caso
--    normal, e o que o F0 a F19 assumem.
delete from t_public_holiday where created_by <> 'seed';
delete from t_option_entity where ccode='AREA_GEOGRAFICA' and ckey like 'TST_%';
update t_unidade_organica set area_ckey=null;

--    E a contagem da V56 (art. 76.o), pela mesma razao: a migracao deixa tudo em DIAS_UTEIS, que e
--    como sempre se contou; o seed classifica pela lei. As tres linhas novas do ponto 2 entram
--    aqui porque o ON CONFLICT do seed nao as traz a uma base ja criada.
update t_leave_type set contagem='DIAS_SEGUIDOS'
 where code not in ('FERIAS','CONTA_FERIAS','PATERNIDADE','TE_PESQUISA','TE_LICENCA');
update t_leave_type set contagem='DIAS_UTEIS'
 where code in ('FERIAS','CONTA_FERIAS','PATERNIDADE','TE_PESQUISA','TE_LICENCA');
insert into t_leave_type (id, code, description, deducts_balance, requires_approval, max_days_per_year, max_days_per_occurrence, max_days_per_month, category, regime, efeito_remuneracao, contagem, is_active, created_date, created_by) values
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1f2', 'SEMINARIO',   'Seminarios, estudos e pesquisas',  false, true, null, 5,    null, 'PESSOAL', 'FALTA', 'SEM_PERDA',   'DIAS_SEGUIDOS', true, now(), 'system'),
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1f3', 'TE_PESQUISA', 'Trabalhador-estudante: pesquisas', false, true, 6,    null, null, 'PESSOAL', 'FALTA', 'SEM_PERDA',   'DIAS_UTEIS',    true, now(), 'system'),
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1f4', 'TE_LICENCA',  'Trabalhador-estudante: licenca',   false, true, 10,   null, null, 'PESSOAL', 'FALTA', 'PERDA_TOTAL', 'DIAS_UTEIS',    true, now(), 'system')
on conflict (code) do nothing;

-- 8. Parametros do mapa de ferias (t_parametro_ferias). Tabela do ddl-auto: pode ainda nao existir
--    numa base acabada de criar, e numa ja criada o seed nao a enche. Volta a haver so a linha
--    da lei (DL n.o 3/2010, desde 2010).
DO $$ BEGIN
  IF to_regclass('public.t_parametro_ferias') IS NOT NULL THEN
    DELETE FROM t_parametro_ferias WHERE id <> '3a000001-0000-0000-0000-000000000001';
    INSERT INTO t_parametro_ferias (id, vigente_desde, prazo_preferencia, prazo_mapa, fixacao_inicio, fixacao_fim, periodo_minimo_interpolado, fundamento, created_date, created_by)
    VALUES ('3a000001-0000-0000-0000-000000000001', 2010, '01-31', '03-31', '05-01', '10-31', 11, 'DL n.o 3/2010, arts. 5.o e 6.o', now(), 'system')
    ON CONFLICT (id) DO UPDATE SET vigente_desde=2010, prazo_preferencia='01-31', prazo_mapa='03-31',
      fixacao_inicio='05-01', fixacao_fim='10-31', periodo_minimo_interpolado=11;
  END IF;
END $$;

-- 8b. Notificacoes e diario de factos (tabelas do ddl-auto): sao efeito dos testes e dos dados
--     de demonstracao, e saem todos -- a base volta ao seed.
DO $$ BEGIN
  IF to_regclass('public.t_processo_aposentacao') IS NOT NULL THEN
    DELETE FROM t_processo_aposentacao;
  END IF;
  IF to_regclass('public.t_prorrogacao_permanencia') IS NOT NULL THEN
    DELETE FROM t_prorrogacao_permanencia;
  END IF;
  IF to_regclass('public.t_pedido_declaracao') IS NOT NULL THEN
    DELETE FROM t_pedido_declaracao;
  END IF;
  IF to_regclass('public.t_documento_emitido') IS NOT NULL THEN
    DELETE FROM t_documento_emitido;
  END IF;
  IF to_regclass('public.t_numeracao_documento') IS NOT NULL THEN
    DELETE FROM t_numeracao_documento;
  END IF;
  IF to_regclass('public.t_reclamacao_antiguidade') IS NOT NULL THEN
    DELETE FROM t_reclamacao_antiguidade;
  END IF;
  IF to_regclass('public.t_lista_antiguidade_linha') IS NOT NULL THEN
    DELETE FROM t_lista_antiguidade_linha;
  END IF;
  IF to_regclass('public.t_lista_antiguidade') IS NOT NULL THEN
    DELETE FROM t_lista_antiguidade;
  END IF;
  IF to_regclass('public.t_notificacao_envio') IS NOT NULL THEN
    DELETE FROM t_notificacao_envio;
  END IF;
  IF to_regclass('public.t_notificacao') IS NOT NULL THEN
    DELETE FROM t_notificacao;
  END IF;
  IF to_regclass('public.t_facto_rh') IS NOT NULL THEN
    DELETE FROM t_facto_rh;
  END IF;
END $$;

-- 9. Horarios e marcacoes (assiduidade). Tabelas do ddl-auto: podem ainda nao existir numa
--    base acabada de criar. O catalogo de horarios e da instituicao e o seed nao traz nenhum: a
--    bateria cria os seus e aqui saem todos, com as atribuicoes e o horario das unidades (V57).
--    O trabalho suplementar (F30) sai primeiro.
DO $$ BEGIN
  IF to_regclass('public.t_trabalho_suplementar') IS NOT NULL THEN
    DELETE FROM t_trabalho_suplementar;
  END IF;
  IF to_regclass('public.t_marcacao_assiduidade') IS NOT NULL THEN
    DELETE FROM t_marcacao_assiduidade;
  END IF;
  IF to_regclass('public.t_horario_colaborador') IS NOT NULL THEN
    DELETE FROM t_horario_colaborador;
  END IF;
  IF to_regclass('public.t_horario_base') IS NOT NULL THEN
    DELETE FROM t_horario_base;
  END IF;
  IF to_regclass('public.t_unidade_organica_horario') IS NOT NULL THEN
    DELETE FROM t_unidade_organica_horario;
  END IF;
  IF to_regclass('public.t_horario_bloco') IS NOT NULL THEN
    DELETE FROM t_horario_bloco;
  END IF;
  IF to_regclass('public.t_horario') IS NOT NULL THEN
    DELETE FROM t_horario;
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.columns
             WHERE table_schema='public' AND table_name='t_unidade_organica' AND column_name='horario_id') THEN
    UPDATE t_unidade_organica SET horario_id = NULL;
  END IF;
END $$;

-- 10. Ausencias em horas (V58). O seed nao traz os cinco tipos novos a uma base ja criada (ON
--     CONFLICT DO NOTHING), e a bateria precisa deles. Os pedidos de teste saem no ponto 4.
insert into t_leave_type (id, code, description, deducts_balance, requires_approval, max_days_per_year, max_days_per_occurrence, max_days_per_month, max_minutos_por_dia, category, regime, efeito_remuneracao, contagem, is_active, created_date, created_by) values
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1f5', 'DISPENSA_AMAMENTACAO',   'Dispensa para amamentacao', false, true, null, 183,  null, 120,  'FAMILIAR', 'FALTA', 'SEM_PERDA', 'DIAS_SEGUIDOS', true, now(), 'system'),
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1f6', 'TRATAMENTO_AMBULATORIO', 'Tratamento ambulatorio',    false, true, null, null, null, null, 'SAUDE',    'FALTA', 'SEM_PERDA', 'DIAS_SEGUIDOS', true, now(), 'system'),
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1f7', 'CONSULTA_PRE_NATAL',     'Consulta pre-natal',        false, true, null, null, null, null, 'SAUDE',    'FALTA', 'SEM_PERDA', 'DIAS_SEGUIDOS', true, now(), 'system'),
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1f8', 'DOACAO_SANGUE',          'Doacao de sangue',          false, true, null, null, null, null, 'SAUDE',    'FALTA', 'SEM_PERDA', 'DIAS_SEGUIDOS', true, now(), 'system'),
('e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1f9', 'CREDITO_SINDICAL',       'Credito de horas sindical', false, true, null, null, null, null, 'PESSOAL',  'FALTA', 'SEM_PERDA', 'DIAS_SEGUIDOS', true, now(), 'system')
on conflict (code) do update set max_minutos_por_dia = excluded.max_minutos_por_dia, max_days_per_occurrence = excluded.max_days_per_occurrence,
    deducts_balance = false, regime = 'FALTA', max_days_per_year = null, max_days_per_month = null, is_active = true;
