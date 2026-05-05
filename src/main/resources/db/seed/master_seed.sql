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

\i 'seed_parametrizacoes.sql'
\i 'seed_carreiras.sql'
\i 'seed_estrutura.sql'
\i 'seed_colaboradores.sql'
