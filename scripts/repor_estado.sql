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
  DELETE FROM t_funcionario    WHERE id = ANY(extras);
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
delete from t_historico_estado_colaborador;

-- 5. Lixo de catalogo deixado por execucoes anteriores.
delete from t_option_entity where ccode like 'TESTE%';
delete from t_worker_state where code like 'TESTE%';
delete from t_leave_mobility_subtype where code like 'TESTE%' or code like '%_TESTE' or code like 'LIC_TST_%' or code like 'MOB_TST_%';
--    O F11 cria um tipo de ausencia a cada execucao (REG_TST_<hora>) para provar que a
--    classificacao do regime e da instituicao. Nunca era apagado: ao fim de algumas corridas
--    havia quinze, e o catalogo que o front-end carrega enchia-se de lixo. Mesma falha que os
--    colaboradores extras tinham no ponto 0.
delete from t_leave_type where code like 'REG_TST_%' or code like 'TESTE%';

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
