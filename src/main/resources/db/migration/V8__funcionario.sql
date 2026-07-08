-- =============================================================
-- V8 — t_funcionario
-- Entity: id, numero_funcionario, nome_completo, data_nascimento,
--         genero, estado_civil, nif, document_type_id, numero_documento,
--         data_emissao_doc, data_validade_doc, nacionalidade, email,
--         telefone, morada, ilha, concelho, localidade, worker_state_id,
--         data_admissao, is_active, [audit]
-- ADD: document_type_id, numero_documento, data_emissao_doc,
--      data_validade_doc, ilha, localidade, worker_state_id
-- DROP (legados): bi_numero, bi_validade, foto_url, situacao_profissional,
--                 data_saida, endereco, estado, nib, nome, num_segurado,
--                 sexo, professional_situation_id
-- =============================================================

CREATE TABLE IF NOT EXISTS t_funcionario (
    id UUID PRIMARY KEY,
    numero_funcionario VARCHAR(10) UNIQUE NOT NULL,
    nome_completo VARCHAR(200) NOT NULL,
    data_nascimento DATE NOT NULL,
    genero VARCHAR(50) NOT NULL,
    estado_civil VARCHAR(50) NOT NULL,
    nif VARCHAR(20) UNIQUE NOT NULL,
    document_type_id UUID,
    numero_documento VARCHAR(50) UNIQUE,
    data_emissao_doc DATE,
    data_validade_doc DATE,
    nacionalidade VARCHAR(50) NOT NULL,
    email VARCHAR(200) UNIQUE,
    telefone VARCHAR(30),
    morada VARCHAR(255),
    ilha VARCHAR(100),
    concelho VARCHAR(100),
    localidade VARCHAR(100),
    worker_state_id UUID,
    data_admissao DATE NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL DEFAULT 'system',
    last_modified_date TIMESTAMP,
    last_modified_by VARCHAR(255)
);

-- Colunas novas que o entity usa e que podem não existir ainda
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='document_type_id') THEN
        ALTER TABLE t_funcionario ADD COLUMN document_type_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='numero_documento') THEN
        ALTER TABLE t_funcionario ADD COLUMN numero_documento VARCHAR(50);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='data_emissao_doc') THEN
        ALTER TABLE t_funcionario ADD COLUMN data_emissao_doc DATE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='data_validade_doc') THEN
        ALTER TABLE t_funcionario ADD COLUMN data_validade_doc DATE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='ilha') THEN
        ALTER TABLE t_funcionario ADD COLUMN ilha VARCHAR(100);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='localidade') THEN
        ALTER TABLE t_funcionario ADD COLUMN localidade VARCHAR(100);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='worker_state_id') THEN
        ALTER TABLE t_funcionario ADD COLUMN worker_state_id UUID;
    END IF;
END $$;

-- Remover NOT NULL de coluna legada antes de a apagar
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='situacao_profissional' AND is_nullable='NO') THEN
        ALTER TABLE t_funcionario ALTER COLUMN situacao_profissional DROP NOT NULL;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='bi_numero' AND is_nullable='NO') THEN
        ALTER TABLE t_funcionario ALTER COLUMN bi_numero DROP NOT NULL;
    END IF;
END $$;

-- Remover colunas legadas sem mapeamento no entity
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='bi_numero') THEN
        ALTER TABLE t_funcionario DROP COLUMN bi_numero;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='bi_validade') THEN
        ALTER TABLE t_funcionario DROP COLUMN bi_validade;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='foto_url') THEN
        ALTER TABLE t_funcionario DROP COLUMN foto_url;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='situacao_profissional') THEN
        ALTER TABLE t_funcionario DROP COLUMN situacao_profissional;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='data_saida') THEN
        ALTER TABLE t_funcionario DROP COLUMN data_saida;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='endereco') THEN
        ALTER TABLE t_funcionario DROP COLUMN endereco;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='estado') THEN
        ALTER TABLE t_funcionario DROP COLUMN estado;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='nib') THEN
        ALTER TABLE t_funcionario DROP COLUMN nib;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='nome') THEN
        ALTER TABLE t_funcionario DROP COLUMN nome;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='num_segurado') THEN
        ALTER TABLE t_funcionario DROP COLUMN num_segurado;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='sexo') THEN
        ALTER TABLE t_funcionario DROP COLUMN sexo;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='t_funcionario' AND column_name='professional_situation_id') THEN
        ALTER TABLE t_funcionario DROP COLUMN professional_situation_id;
    END IF;
END $$;

-- Auditoria: garantir colunas novas na _aud
CREATE TABLE IF NOT EXISTS audit_schema.t_funcionario_aud (
    id                  UUID        NOT NULL,
    rev                 INTEGER     NOT NULL,
    revtype             SMALLINT,
    PRIMARY KEY (id, rev)
);

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_funcionario_aud' AND column_name='document_type_id') THEN
        ALTER TABLE audit_schema.t_funcionario_aud ADD COLUMN document_type_id UUID;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_funcionario_aud' AND column_name='numero_documento') THEN
        ALTER TABLE audit_schema.t_funcionario_aud ADD COLUMN numero_documento VARCHAR(50);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_funcionario_aud' AND column_name='data_emissao_doc') THEN
        ALTER TABLE audit_schema.t_funcionario_aud ADD COLUMN data_emissao_doc DATE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_funcionario_aud' AND column_name='data_validade_doc') THEN
        ALTER TABLE audit_schema.t_funcionario_aud ADD COLUMN data_validade_doc DATE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_funcionario_aud' AND column_name='ilha') THEN
        ALTER TABLE audit_schema.t_funcionario_aud ADD COLUMN ilha VARCHAR(100);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_funcionario_aud' AND column_name='localidade') THEN
        ALTER TABLE audit_schema.t_funcionario_aud ADD COLUMN localidade VARCHAR(100);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='audit_schema' AND table_name='t_funcionario_aud' AND column_name='worker_state_id') THEN
        ALTER TABLE audit_schema.t_funcionario_aud ADD COLUMN worker_state_id UUID;
    END IF;
END $$;
