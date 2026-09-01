-- SEED: Identidade Institucional
-- Description: Institution and its active institutional identity (mission/vision/values)
--
-- Dados de base, nao dados de teste. CreateStrategicGoalCommandHandler exige uma
-- identidade ativa (identityRepository.findActive(), implementado como
-- findFirstByIsActiveTrue) e usa o seu institution_id para carimbar cada objetivo
-- estrategico criado. Sem esta linha, criar um objetivo devolve 404 "Identidade
-- Institucional ativa nao encontrada" e o percurso BSC nao arranca de todo.
--
-- Fica na mesma familia das 4 perspetivas BSC semeadas pela V26: configuracao que
-- tem de existir para os fluxos correrem, e nao registo que os fluxos produzam.
-- As tabelas de dados do SIGDI -- t_strategic_goals, t_goal_relationships, t_okrs,
-- t_paa_submission_period -- continuam intencionalmente vazias.
--
-- A instituicao e o MFFE para coincidir com seed_estrutura.sql, onde a unidade de
-- nivel 1 e o Ministerio das Financas e do Fomento Empresarial.
--
-- Apenas UMA identidade pode estar ativa: findFirstByIsActiveTrue devolve a primeira
-- que encontrar, sem ordenacao definida. Se acrescentares outra, desativa esta.

INSERT INTO t_institutions (id, code, name, type, is_active, created_date, created_by) VALUES
('e6e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ee01', 'MFFE', 'Ministério das Finanças e do Fomento Empresarial', 'MINISTERIO', true, NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- cycle_year fixo em 2026: e o ano com que os periodos de submissao do teste
-- funcional sao abertos. O handler nao cruza cycle_year com o ano do periodo --
-- so exige que a identidade esteja ativa -- mas manter os dois alinhados evita
-- um cenario de teste a olhar para anos diferentes sem dar por isso.
INSERT INTO t_institutional_identity (id, institution_id, cycle_year, mission, vision, values_json, is_active, version_comment, created_date, created_by) VALUES
('e7e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ee01',
 'e6e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ee01',
 2026,
 'Assegurar a gestão rigorosa das finanças públicas e promover o desenvolvimento empresarial.',
 'Ser uma administração financeira de referência, transparente e orientada para resultados.',
 '["Rigor","Transparência","Responsabilidade","Orientação para resultados"]',
 true,
 'Seed inicial para testes funcionais',
 NOW(), 'system')
ON CONFLICT (id) DO NOTHING;
