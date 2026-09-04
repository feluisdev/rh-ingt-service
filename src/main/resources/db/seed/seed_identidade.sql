-- SEED: Instituição
-- Description: The institution row. The institutional identity is NOT seeded here --
--              see the note below, it cannot be.
--
-- Dados de base, nao dados de teste. Fica na mesma familia das 4 perspetivas BSC
-- semeadas pela V26: configuracao que tem de existir para os fluxos correrem.
--
-- A instituicao e o MFFE para coincidir com seed_estrutura.sql, onde a unidade de
-- nivel 1 e o Ministerio das Financas e do Fomento Empresarial.

INSERT INTO t_institutions (id, code, name, type, is_active, created_date, created_by) VALUES
('e6e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ee01', 'MFFE', 'Ministério das Finanças e do Fomento Empresarial', 'MINISTERIO', true, NOW(), 'system')
ON CONFLICT (code) DO NOTHING;

-- ============================================================================
-- A IDENTIDADE INSTITUCIONAL NAO SE SEMEIA POR SQL. Medido a 2026-09-01.
-- ============================================================================
--
-- InstitutionalIdentityEntity anota mission, vision e values_json com @Lob sobre
-- String. No PostgreSQL isso significa ARMAZENAMENTO EM OBJETO GRANDE: o Hibernate
-- cria uma entrada em pg_largeobject e grava na coluna `text` apenas o OID, como
-- texto. Ao ler, chama getLong() sobre essa coluna para obter o OID e so depois vai
-- buscar o conteudo.
--
-- Um INSERT em SQL cru mete o texto diretamente na coluna. O getLong() rebenta com
--
--     SQLState 22003 -- Bad value for type long : Assegurar a gestao rigorosa...
--     DataIntegrityViolationException -> HTTP 400 "Erro de dados. Dados invalidos."
--
-- e TODAS as leituras da identidade passam a falhar -- incluindo o
-- GetCurrentStrategyMapQueryHandler e o CreateStrategicGoalCommandHandler, o que
-- bloqueia o percurso BSC inteiro. Verificado ao vivo: uma linha escrita por SQL
-- guarda 91 caracteres de texto na coluna; uma escrita pela API guarda 5 -- o OID.
--
-- Cria-se pela API, que e o unico caminho que produz a representacao certa:
--
--     curl -X POST http://localhost:8084/api/v1/strategy/identities \
--          -H "Content-Type: application/json; charset=utf-8" \
--          --data-binary @identity.json
--
-- O scripts/reset-db.ps1 faz isto na fase -SeedApi, depois do terceiro arranque.
-- Passa o corpo por FICHEIRO e nao por argumento: os acentos portugueses partem-se
-- na linha de comandos e o servico devolve
-- "JSON parse error: Invalid UTF-8 middle byte".
--
-- Nota lateral, tambem medida: DROP SCHEMA CASCADE nao apaga objetos grandes --
-- eles pertencem a base de dados e nao ao esquema. Cada reset deixa os anteriores
-- orfaos em pg_largeobject_metadata. O -Wipe do reset-db.ps1 passou a limpa-los.
