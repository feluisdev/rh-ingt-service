-- =============================================================
-- V47 — largar o origin_assignment_id, que ficou sem dono
--
-- A coluna existia para a mobilidade transitória restaurar a afectação de
-- origem: fechava-se a afectação, abria-se outra no Lugar de destino, e
-- guardava-se aqui para onde voltar. Esse mecanismo foi removido por
-- contradizer o art. 135.º n.º 7 — na mobilidade transitória o titular
-- **mantém** o Lugar, logo não há nada a restaurar. Os métodos que a usavam
-- (afectarMobilidade, regressarDeMobilidade) já não existem.
--
-- Desde então nada a escreve (todos os caminhos passam null) e nada a lê.
-- Confirmado contra a base antes de largar: 0 linhas de t_assignment e 0
-- revisões de auditoria com valor.
--
-- Nenhum dos movimentos ainda por fazer precisa dela: o regresso de comissão
-- de serviço (art. 64.º n.º 2) não perde o Lugar, e a permuta é uma troca
-- atómica sem regresso. A ligação que a substituição precisa é outra, e tem
-- coluna própria desde a V46 (titular_assignment_id).
--
-- O DROP COLUMN do PostgreSQL leva atrás a chave estrangeira e os índices que
-- dependam da coluna, por isso não é preciso largá-los à mão.
--
-- Defensivo em todos os passos, no molde da V40 a V46: confirma-se que a
-- tabela existe antes de lhe tocar, e que a coluna existe antes de a largar.
-- =============================================================

DO $$ BEGIN
    IF to_regclass('public.t_assignment') IS NOT NULL
       AND EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='t_assignment'
                     AND column_name='origin_assignment_id') THEN

        ALTER TABLE t_assignment DROP COLUMN origin_assignment_id;
    END IF;
END $$;

-- A tabela sombra do Envers segue a principal: uma coluna que a entidade já
-- não tem deixa de poder ser auditada.
DO $$ BEGIN
    IF to_regclass('audit_schema.t_assignment_aud') IS NOT NULL
       AND EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='audit_schema' AND table_name='t_assignment_aud'
                     AND column_name='origin_assignment_id') THEN

        ALTER TABLE audit_schema.t_assignment_aud DROP COLUMN origin_assignment_id;
    END IF;
END $$;
