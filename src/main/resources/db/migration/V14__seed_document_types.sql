-- Seed de tipos de documento — category_option_id referencia t_option_entity (ccode='DOC_CATEGORY')
-- Usa subquery para localizar o ID da categoria; se a seed do option_entity não existir, insere NULL.

-- t_tipo_documento criada pelo Hibernate sem defaults nas colunas de auditoria NOT NULL.
ALTER TABLE t_tipo_documento
    ALTER COLUMN created_date SET DEFAULT NOW(),
    ALTER COLUMN created_by   SET DEFAULT 'seed';

INSERT INTO t_tipo_documento (id, descricao, codigo, allowed_extensions, category_option_id, is_active) VALUES
    (gen_random_uuid(), 'Bilhete de Identidade',        'BI',           'pdf,jpg,png',
     (SELECT id FROM t_option_entity WHERE ccode='DOC_CATEGORY' AND ckey='IDENTIFICATION' LIMIT 1), TRUE),
    (gen_random_uuid(), 'Passaporte',                   'PASSAPORTE',   'pdf,jpg,png',
     (SELECT id FROM t_option_entity WHERE ccode='DOC_CATEGORY' AND ckey='IDENTIFICATION' LIMIT 1), TRUE),
    (gen_random_uuid(), 'NIF',                          'NIF',          'pdf,jpg,png',
     (SELECT id FROM t_option_entity WHERE ccode='DOC_CATEGORY' AND ckey='IDENTIFICATION' LIMIT 1), TRUE),
    (gen_random_uuid(), 'NISS',                         'NISS',         'pdf,jpg,png',
     (SELECT id FROM t_option_entity WHERE ccode='DOC_CATEGORY' AND ckey='IDENTIFICATION' LIMIT 1), TRUE),
    (gen_random_uuid(), 'Diploma / Certificado',        'DIPLOMA',      'pdf,jpg,png',
     (SELECT id FROM t_option_entity WHERE ccode='DOC_CATEGORY' AND ckey='ACADEMIC' LIMIT 1), TRUE),
    (gen_random_uuid(), 'Curriculum Vitae',             'CV',           'pdf,doc,docx',
     (SELECT id FROM t_option_entity WHERE ccode='DOC_CATEGORY' AND ckey='PROFESSIONAL' LIMIT 1), TRUE),
    (gen_random_uuid(), 'Declaração Médica',            'DEC_MEDICA',   'pdf,jpg,png',
     (SELECT id FROM t_option_entity WHERE ccode='DOC_CATEGORY' AND ckey='MEDICAL' LIMIT 1), TRUE),
    (gen_random_uuid(), 'Atestado Médico',              'ATESTADO',     'pdf,jpg,png',
     (SELECT id FROM t_option_entity WHERE ccode='DOC_CATEGORY' AND ckey='MEDICAL' LIMIT 1), TRUE),
    (gen_random_uuid(), 'Contrato de Trabalho',         'CONTRATO',     'pdf',
     (SELECT id FROM t_option_entity WHERE ccode='DOC_CATEGORY' AND ckey='PROFESSIONAL' LIMIT 1), TRUE),
    (gen_random_uuid(), 'Outro Documento',              'OUTRO',        'pdf,jpg,png,doc,docx',
     (SELECT id FROM t_option_entity WHERE ccode='DOC_CATEGORY' AND ckey='OTHER' LIMIT 1), TRUE)
ON CONFLICT (codigo) DO NOTHING;
