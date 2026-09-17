-- =============================================================
-- V40 — t_worker_state.ends_employment
-- Marca os estados que terminam a relação de emprego público
-- (Lei n.º 20/X/2023, art. 93.º, 96.º e 97.º). Substitui a comparação
-- com os códigos "INACTIVE"/"RETIRED" escrita no MudarEstadoColaborador:
-- passar a um estado com ends_employment=true cessa o contrato, encerra
-- a afectação corrente (o Lugar fica vago) e regista o histórico.
--
-- Defensivo em todos os passos: a tabela pode não existir (esquema criado
-- pelo ddl-auto), a coluna pode já ter sido acrescentada à mão, e o UPDATE
-- só corre se houver tabela e coluna.
-- =============================================================

DO $$ BEGIN
    IF to_regclass('public.t_worker_state') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='public' AND table_name='t_worker_state'
                         AND column_name='ends_employment') THEN
        ALTER TABLE t_worker_state ADD COLUMN ends_employment BOOLEAN;
    END IF;
END $$;

DO $$ BEGIN
    IF to_regclass('audit_schema.t_worker_state_aud') IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema='audit_schema' AND table_name='t_worker_state_aud'
                         AND column_name='ends_employment') THEN
        ALTER TABLE audit_schema.t_worker_state_aud ADD COLUMN ends_employment BOOLEAN;
    END IF;
END $$;

-- Retrocompatibilidade: os estados que o código tratava como cessação passam
-- a dizê-lo pelos dados. Os restantes ficam a false (não cessam nada).
DO $$ BEGIN
    IF to_regclass('public.t_worker_state') IS NOT NULL
       AND EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='t_worker_state'
                     AND column_name='ends_employment') THEN

        UPDATE t_worker_state SET ends_employment = TRUE
         WHERE code IN ('INACTIVE', 'RETIRED') AND ends_employment IS DISTINCT FROM TRUE;

        UPDATE t_worker_state SET ends_employment = FALSE
         WHERE code NOT IN ('INACTIVE', 'RETIRED') AND ends_employment IS NULL;
    END IF;
END $$;
