-- Migration: V34__form_generation_batch.sql
-- PRZ-06, PRZ-07, Fase 119. O rasto que torna a geracao em massa reversivel: cada
-- formulario gerado (ou tentativa falhada) fica com uma linha propria, e a Fase 120
-- desfaz lotes a partir deste rasto.
--
-- Numeracao: V34, e nao um numero abaixo. V24__update_kpi_criteria_fix.sql existe em
-- disco, esta aplicada na base do operador e nunca foi commitada (git status: ??).
-- Reutilizar um numero abaixo de 34 faria o Flyway comparar o checksum do ficheiro novo
-- contra o checksum ja registado na base local e falhar a validacao. V33 e a mais alta
-- commitada.
--
-- Forma: criacao idempotente (IF NOT EXISTS) com a forma completa, e nao ALTER -- o Flyway corre
-- antes do ddl-auto (precedente escrito na V19 e repetido na V32/V33). Uma migracao que
-- assuma tabelas preexistentes falha num ambiente limpo.
--
-- D-02: estas duas tabelas NAO tem sombra Envers (audit_schema.*_aud) e as entidades
-- correspondentes NAO levam @Audited nem extendem AuditEntity. O lote e ele proprio o
-- registo de auditoria -- uma sombra Envers de um rasto append-only nao acrescentaria
-- nada e obrigaria a criar audit_schema.t_form_generation_batch_aud na mesma migracao.
-- As colunas de autoria (generated_by, generated_at, created_at) sao escritas
-- explicitamente pelo codigo da aplicacao, e nao resolvidas no flush por um
-- AuditingEntityListener.
--
-- D-03: t_form_generation_batch_item.batch_id leva uma cascata de remocao para
-- t_form_generation_batch(id). E a unica chave estrangeira que este projecto acrescenta
-- -- justifica-se porque as duas tabelas nascem juntas, nesta mesma migracao, e o item
-- nao tem vida sem o lote: a Fase 120 desfaz um lote inteiro, e sem cascata apagar o
-- lote deixaria linhas filhas orfas a apontar para um pai que nao existe. Todos os
-- outros apontadores desta tabela (employee_id, unit_id, period_id,
-- generated_form_id, evaluator_id) sao UUID nu sem FK, seguindo o precedente da V32.
--
-- D-04: sem restricao unica em period_id -- um lote PARTIAL tem de poder ser retomado
-- por um lote novo sobre o mesmo periodo. A garantia contra avaliacoes duplicadas nao
-- esta aqui, esta no plano 03, via SiadapEvaluationRepository.findByEmployeeAndYear.
-- Cria-se apenas um indice nao unico em period_id, porque e por ai que os planos 04 e
-- 05 consultam.
--
-- D-05: sem CHECK sobre status e outcome. Ao contrario da V33, que acrescentou um CHECK
-- ao purpose de uma tabela ja existente com dados, estas tabelas nascem vazias e os
-- seus dois enums sao novos e vao crescer (a Fase 120 acrescenta pelo menos um estado
-- de reversao). Um CHECK aqui obrigaria a uma migracao por cada valor novo, sem fechar
-- caminho silencioso nenhum -- so o codigo desta aplicacao escreve nestas tabelas.

CREATE TABLE IF NOT EXISTS t_form_generation_batch (
    id               UUID         PRIMARY KEY,
    period_id        UUID         NOT NULL,
    purpose          VARCHAR(20)  NOT NULL,
    type             VARCHAR(30)  NOT NULL,
    year             INTEGER      NOT NULL,
    generation_mode  VARCHAR(20)  NOT NULL,
    status           VARCHAR(30)  NOT NULL,
    created_count    INTEGER      NOT NULL DEFAULT 0,
    failed_count     INTEGER      NOT NULL DEFAULT 0,
    skipped_count    INTEGER      NOT NULL DEFAULT 0,
    pending_count    INTEGER      NOT NULL DEFAULT 0,
    generated_at     TIMESTAMP    NOT NULL,
    finished_at      TIMESTAMP,
    generated_by     VARCHAR(255) NOT NULL
);

-- employee_id e anulavel de proposito: uma linha de unidade saltada (por exemplo
-- UNIT_WITHOUT_RESPONSIBLE) nao tem colaborador nenhum a apontar -- e o proprio
-- registo de que a unidade foi saltada, nao de uma tentativa sobre uma pessoa.
CREATE TABLE IF NOT EXISTS t_form_generation_batch_item (
    id                 UUID        PRIMARY KEY,
    batch_id           UUID        NOT NULL REFERENCES t_form_generation_batch(id) ON DELETE CASCADE,
    employee_id        UUID,
    employee_name      VARCHAR(255),
    unit_id            UUID,
    unit_name          VARCHAR(255),
    outcome            VARCHAR(30) NOT NULL,
    generated_form_id  UUID,
    evaluator_id       UUID,
    skip_reason        VARCHAR(50),
    error_message      TEXT,
    created_at         TIMESTAMP   NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_form_generation_batch_period
    ON t_form_generation_batch(period_id);

CREATE INDEX IF NOT EXISTS idx_form_generation_batch_item_batch
    ON t_form_generation_batch_item(batch_id);
