-- ============================================================
-- MASTER SEED - RH Service
-- Executa todos os seeds na ordem correta (respeitando FK deps)
-- ============================================================
-- Ordem de dependências:
--   1. parametrizacoes  → sem dependências externas
--   2. carreiras        → sem dependências externas
--   3. estrutura        → depende de t_option_entity (parametrizacoes)
--   4. colaboradores    → depende de t_job, t_unidade_organica (estrutura),
--                         t_career, t_category, t_grade (carreiras),
--                         t_tipo_documento (parametrizacoes)
--   5. identidade       → sem dependências externas; t_institutional_identity
--                         depende de t_institutions, ambas no mesmo ficheiro
--
-- A identidade institucional é dado de base, não dado de teste: sem uma
-- identidade ativa, CreateStrategicGoalCommandHandler devolve 404 e não se
-- consegue criar um único objetivo estratégico. As tabelas de dados do SIGDI
-- (t_strategic_goals, t_goal_relationships, t_okrs, t_paa_submission_period)
-- ficam vazias de propósito -- é o teste funcional que as deve preencher.

\i 'seed_parametrizacoes.sql'
\i 'seed_carreiras.sql'
\i 'seed_estrutura.sql'
\i 'seed_colaboradores.sql'
\i 'seed_identidade.sql'
