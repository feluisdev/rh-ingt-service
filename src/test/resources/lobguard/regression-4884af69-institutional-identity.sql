-- FIXTURE DE REGRESSAO -- NAO E UM SEED. NUNCA EXECUTAR ESTE FICHEIRO.
--
-- Copia literal do INSERT introduzido pelo commit 4884af69 e revertido por 5e819bfb.
-- Escreve texto nas colunas mission, vision e values_json de t_institutional_identity,
-- que a InstitutionalIdentityEntity anota com @Lob: no PostgreSQL o Hibernate espera
-- ali o OID de um objeto grande e le a coluna com getLong(). Com esta linha na base,
-- todas as leituras da identidade rebentam com SQLState 22003 -> HTTP 400, incluindo
-- o GetCurrentStrategyMapQueryHandler e o CreateStrategicGoalCommandHandler.
--
-- Existe para que o LobColumnSqlGuardTest prove, em cada build, que a guarda deteta
-- o defeito real e nao apenas que passa sobre codigo limpo. O varrimento do
-- repositorio exclui de proposito a pasta src/test/resources/lobguard/.
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
