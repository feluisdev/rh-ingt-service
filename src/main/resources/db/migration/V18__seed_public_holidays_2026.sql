ALTER TABLE t_public_holiday
    ALTER COLUMN created_date SET DEFAULT NOW(),
    ALTER COLUMN created_by SET DEFAULT 'seed';

INSERT INTO t_public_holiday (id, name, holiday_date, is_national, description, is_active)
VALUES
    (gen_random_uuid(), 'Ano Novo', '2026-01-01', TRUE, 'Celebração do início do novo ano civil', TRUE),
    (gen_random_uuid(), 'Dia dos Heróis Nacionais', '2026-01-20', TRUE, 'Aniversário da morte de Amílcar Cabral (1973)', TRUE),
    (gen_random_uuid(), 'Dia da Democracia e da Liberdade', '2026-01-13', TRUE, 'Comemoração da abertura política multipartidária (1991)', TRUE),
    (gen_random_uuid(), 'Dia Internacional da Mulher', '2026-03-08', TRUE, 'Dia Internacional dos Direitos da Mulher', TRUE),
    (gen_random_uuid(), 'Dia do Trabalhador', '2026-05-01', TRUE, 'Dia Internacional do Trabalho', TRUE),
    (gen_random_uuid(), 'Dia da Criança', '2026-06-01', TRUE, 'Dia Internacional da Criança', TRUE),
    (gen_random_uuid(), 'Dia da Independência Nacional', '2026-07-05', TRUE, 'Proclamação da Independência de Cabo Verde (1975)', TRUE),
    (gen_random_uuid(), 'Assunção de Nossa Senhora', '2026-08-15', TRUE, 'Dia de Nossa Senhora da Graça', TRUE),
    (gen_random_uuid(), 'Dia de Todos os Santos', '2026-11-01', TRUE, 'Solenidade de Todos os Santos', TRUE),
    (gen_random_uuid(), 'Imaculada Conceição', '2026-12-08', TRUE, 'Solenidade da Imaculada Conceição de Nossa Senhora', TRUE),
    (gen_random_uuid(), 'Dia de Natal', '2026-12-25', TRUE, 'Celebração do Nascimento de Jesus Cristo', TRUE)
ON CONFLICT DO NOTHING;
