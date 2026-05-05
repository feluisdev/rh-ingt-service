-- Seed do "kit Cabo Verde" para os 11 grupos de etiquetas
-- Idempotente: ON CONFLICT DO NOTHING

-- Garante UNIQUE (ccode, ckey, locale) necessário para ON CONFLICT abaixo.
-- t_option_entity pode ter sido criada pelo Hibernate sem este constraint.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'uq_option_ccode_ckey_locale'
          AND table_name = 't_option_entity'
    ) THEN
        ALTER TABLE t_option_entity
            ADD CONSTRAINT uq_option_ccode_ckey_locale UNIQUE (ccode, ckey, locale);
    END IF;
END $$;

-- t_option_entity uses created_at/updated_at (from V2 schema), not created_date.
-- The INSERTs below don't include audit columns so no ALTER needed.

-- MARITAL_STATUS (5 entradas)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active) VALUES
    (gen_random_uuid(), 'MARITAL_STATUS', 'SINGLE',    'Solteiro(a)',          'pt-CV', 1, true),
    (gen_random_uuid(), 'MARITAL_STATUS', 'MARRIED',   'Casado(a)',            'pt-CV', 2, true),
    (gen_random_uuid(), 'MARITAL_STATUS', 'DIVORCED',  'Divorciado(a)',        'pt-CV', 3, true),
    (gen_random_uuid(), 'MARITAL_STATUS', 'WIDOWED',   'Viúvo(a)',             'pt-CV', 4, true),
    (gen_random_uuid(), 'MARITAL_STATUS', 'UNION',     'União de Facto',       'pt-CV', 5, true)
ON CONFLICT (ccode, ckey, locale) DO NOTHING;

-- SEX (2 entradas)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active) VALUES
    (gen_random_uuid(), 'SEX', 'M', 'Masculino', 'pt-CV', 1, true),
    (gen_random_uuid(), 'SEX', 'F', 'Feminino',  'pt-CV', 2, true)
ON CONFLICT (ccode, ckey, locale) DO NOTHING;

-- NATIONALITY (15 entradas — principais nacionalidades em Cabo Verde)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active) VALUES
    (gen_random_uuid(), 'NATIONALITY', 'CV', 'Cabo-verdiana',    'pt-CV',  1, true),
    (gen_random_uuid(), 'NATIONALITY', 'PT', 'Portuguesa',       'pt-CV',  2, true),
    (gen_random_uuid(), 'NATIONALITY', 'SN', 'Senegalesa',       'pt-CV',  3, true),
    (gen_random_uuid(), 'NATIONALITY', 'GN', 'Guineense',        'pt-CV',  4, true),
    (gen_random_uuid(), 'NATIONALITY', 'ML', 'Maliana',          'pt-CV',  5, true),
    (gen_random_uuid(), 'NATIONALITY', 'NI', 'Nigeriana',        'pt-CV',  6, true),
    (gen_random_uuid(), 'NATIONALITY', 'GH', 'Ganesa',           'pt-CV',  7, true),
    (gen_random_uuid(), 'NATIONALITY', 'MR', 'Mauritana',        'pt-CV',  8, true),
    (gen_random_uuid(), 'NATIONALITY', 'BR', 'Brasileira',       'pt-CV',  9, true),
    (gen_random_uuid(), 'NATIONALITY', 'FR', 'Francesa',         'pt-CV', 10, true),
    (gen_random_uuid(), 'NATIONALITY', 'US', 'Americana',        'pt-CV', 11, true),
    (gen_random_uuid(), 'NATIONALITY', 'CN', 'Chinesa',          'pt-CV', 12, true),
    (gen_random_uuid(), 'NATIONALITY', 'LB', 'Libanesa',         'pt-CV', 13, true),
    (gen_random_uuid(), 'NATIONALITY', 'MZ', 'Moçambicana',      'pt-CV', 14, true),
    (gen_random_uuid(), 'NATIONALITY', 'OTHER', 'Outra',         'pt-CV', 99, true)
ON CONFLICT (ccode, ckey, locale) DO NOTHING;

-- UNIT_TYPE (4 entradas)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active) VALUES
    (gen_random_uuid(), 'UNIT_TYPE', 'MINISTRY',    'Ministério',        'pt-CV', 1, true),
    (gen_random_uuid(), 'UNIT_TYPE', 'DIRECTION',   'Direcção',          'pt-CV', 2, true),
    (gen_random_uuid(), 'UNIT_TYPE', 'DEPARTMENT',  'Departamento',      'pt-CV', 3, true),
    (gen_random_uuid(), 'UNIT_TYPE', 'SERVICE',     'Serviço',           'pt-CV', 4, true)
ON CONFLICT (ccode, ckey, locale) DO NOTHING;

-- DOC_CATEGORY (5 entradas)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active) VALUES
    (gen_random_uuid(), 'DOC_CATEGORY', 'IDENTIFICATION', 'Identificação',      'pt-CV', 1, true),
    (gen_random_uuid(), 'DOC_CATEGORY', 'ACADEMIC',       'Académico',          'pt-CV', 2, true),
    (gen_random_uuid(), 'DOC_CATEGORY', 'PROFESSIONAL',   'Profissional',       'pt-CV', 3, true),
    (gen_random_uuid(), 'DOC_CATEGORY', 'MEDICAL',        'Médico',             'pt-CV', 4, true),
    (gen_random_uuid(), 'DOC_CATEGORY', 'OTHER',          'Outro',              'pt-CV', 5, true)
ON CONFLICT (ccode, ckey, locale) DO NOTHING;

-- LEAVE_CATEGORY (4 entradas)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active) VALUES
    (gen_random_uuid(), 'LEAVE_CATEGORY', 'ANNUAL',      'Férias Anuais',      'pt-CV', 1, true),
    (gen_random_uuid(), 'LEAVE_CATEGORY', 'MEDICAL',     'Baixa Médica',       'pt-CV', 2, true),
    (gen_random_uuid(), 'LEAVE_CATEGORY', 'MATERNITY',   'Licença Maternidade','pt-CV', 3, true),
    (gen_random_uuid(), 'LEAVE_CATEGORY', 'OTHER',       'Outra',              'pt-CV', 4, true)
ON CONFLICT (ccode, ckey, locale) DO NOTHING;

-- QUALIFICATION_LEVEL (5 entradas)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active) VALUES
    (gen_random_uuid(), 'QUALIFICATION_LEVEL', 'PRIMARY',    'Ensino Básico',     'pt-CV', 1, true),
    (gen_random_uuid(), 'QUALIFICATION_LEVEL', 'SECONDARY',  'Ensino Secundário', 'pt-CV', 2, true),
    (gen_random_uuid(), 'QUALIFICATION_LEVEL', 'BACHELORS',  'Bacharelato',       'pt-CV', 3, true),
    (gen_random_uuid(), 'QUALIFICATION_LEVEL', 'MASTERS',    'Mestrado',          'pt-CV', 4, true),
    (gen_random_uuid(), 'QUALIFICATION_LEVEL', 'DOCTORATE',  'Doutoramento',      'pt-CV', 5, true)
ON CONFLICT (ccode, ckey, locale) DO NOTHING;

-- RELATIONSHIP_TYPE (5 entradas)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active) VALUES
    (gen_random_uuid(), 'RELATIONSHIP_TYPE', 'SPOUSE',   'Cônjuge',    'pt-CV', 1, true),
    (gen_random_uuid(), 'RELATIONSHIP_TYPE', 'CHILD',    'Filho(a)',   'pt-CV', 2, true),
    (gen_random_uuid(), 'RELATIONSHIP_TYPE', 'PARENT',   'Progenitor', 'pt-CV', 3, true),
    (gen_random_uuid(), 'RELATIONSHIP_TYPE', 'SIBLING',  'Irmão/Irmã', 'pt-CV', 4, true),
    (gen_random_uuid(), 'RELATIONSHIP_TYPE', 'OTHER',    'Outro',      'pt-CV', 5, true)
ON CONFLICT (ccode, ckey, locale) DO NOTHING;

-- ISLAND (10 entradas — ilhas de Cabo Verde)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active) VALUES
    (gen_random_uuid(), 'ISLAND', 'SAL',       'Sal',             'pt-CV',  1, true),
    (gen_random_uuid(), 'ISLAND', 'BOA_VISTA', 'Boa Vista',       'pt-CV',  2, true),
    (gen_random_uuid(), 'ISLAND', 'SAO_TIAGO', 'Santiago',        'pt-CV',  3, true),
    (gen_random_uuid(), 'ISLAND', 'SAO_VICENTE','São Vicente',     'pt-CV',  4, true),
    (gen_random_uuid(), 'ISLAND', 'SANTO_ANTAO','Santo Antão',     'pt-CV',  5, true),
    (gen_random_uuid(), 'ISLAND', 'FOGO',      'Fogo',            'pt-CV',  6, true),
    (gen_random_uuid(), 'ISLAND', 'BRAVA',     'Brava',           'pt-CV',  7, true),
    (gen_random_uuid(), 'ISLAND', 'SAO_NICOLAU','São Nicolau',    'pt-CV',  8, true),
    (gen_random_uuid(), 'ISLAND', 'MAIO',      'Maio',            'pt-CV',  9, true),
    (gen_random_uuid(), 'ISLAND', 'SAO_LUIS',  'São Luís (Brava)','pt-CV', 10, true)
ON CONFLICT (ccode, ckey, locale) DO NOTHING;

-- CONCELHO (22 entradas — municípios de Cabo Verde)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active) VALUES
    (gen_random_uuid(), 'CONCELHO', 'PRAIA',           'Praia',              'pt-CV',  1, true),
    (gen_random_uuid(), 'CONCELHO', 'SAO_DOMINGOS',    'São Domingos',       'pt-CV',  2, true),
    (gen_random_uuid(), 'CONCELHO', 'SAO_MIGUEL',      'São Miguel',         'pt-CV',  3, true),
    (gen_random_uuid(), 'CONCELHO', 'TARRAFAL_STGO',   'Tarrafal (Santiago)','pt-CV',  4, true),
    (gen_random_uuid(), 'CONCELHO', 'SANTA_CATARINA',  'Santa Catarina',     'pt-CV',  5, true),
    (gen_random_uuid(), 'CONCELHO', 'SANTA_CRUZ',      'Santa Cruz',         'pt-CV',  6, true),
    (gen_random_uuid(), 'CONCELHO', 'SAO_LOURENCO',    'São Lourenço dos Órgãos', 'pt-CV', 7, true),
    (gen_random_uuid(), 'CONCELHO', 'RIBEIRA_GRANDE_SA','Ribeira Grande de Santiago', 'pt-CV', 8, true),
    (gen_random_uuid(), 'CONCELHO', 'MINDELO',         'São Vicente (Mindelo)', 'pt-CV', 9, true),
    (gen_random_uuid(), 'CONCELHO', 'PORTO_NOVO',      'Porto Novo',         'pt-CV', 10, true),
    (gen_random_uuid(), 'CONCELHO', 'PAUL',            'Paul',               'pt-CV', 11, true),
    (gen_random_uuid(), 'CONCELHO', 'RIBEIRA_GRANDE_SA2','Ribeira Grande (Santo Antão)', 'pt-CV', 12, true),
    (gen_random_uuid(), 'CONCELHO', 'SANTA_CATARINA_FOG','Santa Catarina do Fogo', 'pt-CV', 13, true),
    (gen_random_uuid(), 'CONCELHO', 'SAO_FILIPE',      'São Filipe',         'pt-CV', 14, true),
    (gen_random_uuid(), 'CONCELHO', 'MOSTEIROS',       'Mosteiros',          'pt-CV', 15, true),
    (gen_random_uuid(), 'CONCELHO', 'BRAVA',           'Brava',              'pt-CV', 16, true),
    (gen_random_uuid(), 'CONCELHO', 'SAL',             'Sal',                'pt-CV', 17, true),
    (gen_random_uuid(), 'CONCELHO', 'BOA_VISTA',       'Boa Vista',          'pt-CV', 18, true),
    (gen_random_uuid(), 'CONCELHO', 'MAIO',            'Maio',               'pt-CV', 19, true),
    (gen_random_uuid(), 'CONCELHO', 'SAO_NICOLAU_N',   'São Nicolau (Norte)','pt-CV', 20, true),
    (gen_random_uuid(), 'CONCELHO', 'TARRAFAL_SN',     'Tarrafal (São Nicolau)', 'pt-CV', 21, true),
    (gen_random_uuid(), 'CONCELHO', 'RIBEIRA_BRAVA',   'Ribeira Brava',      'pt-CV', 22, true)
ON CONFLICT (ccode, ckey, locale) DO NOTHING;

-- TRAINING_TYPE (4 entradas)
INSERT INTO t_option_entity (id, ccode, ckey, cvalue, locale, sort_order, active) VALUES
    (gen_random_uuid(), 'TRAINING_TYPE', 'INTERNAL',   'Formação Interna',   'pt-CV', 1, true),
    (gen_random_uuid(), 'TRAINING_TYPE', 'EXTERNAL',   'Formação Externa',   'pt-CV', 2, true),
    (gen_random_uuid(), 'TRAINING_TYPE', 'ONLINE',     'Formação Online',    'pt-CV', 3, true),
    (gen_random_uuid(), 'TRAINING_TYPE', 'SCHOLARSHIP', 'Bolsa de Estudo',   'pt-CV', 4, true)
ON CONFLICT (ccode, ckey, locale) DO NOTHING;
