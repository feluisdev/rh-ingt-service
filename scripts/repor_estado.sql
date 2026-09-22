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
delete from t_leave_mobility_subtype where code like 'TESTE%' or code like 'LIC_TST_%' or code like 'MOB_TST_%';

-- 6. Catalogo que o seed nao consegue corrigir sozinho.
--    O seed_parametrizacoes usa ON CONFLICT (code) DO NOTHING: numa base ja criada, a linha
--    antiga fica como estava. A comissao de servico foi reclassificada (art. 64.o n.o 2:
--    REGRESSA_OU_CESSA) e a duracao corrigida para tres anos sucessivamente renovaveis
--    (art. 60.o n.o 1), e sem isto o F17 corre contra o catalogo velho e nao prova nada.
update t_leave_mobility_subtype
   set return_effect='REGRESSA_OU_CESSA', max_duration_days=1095, max_extensions=NULL
 where code='MOB_COMISSAO';
