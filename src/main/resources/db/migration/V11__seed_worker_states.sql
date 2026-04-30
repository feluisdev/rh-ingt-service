INSERT INTO t_worker_state (id, code, description, is_core, is_active) VALUES
    (gen_random_uuid(), 'ACTIVE',     'Activo',    TRUE,  TRUE),
    (gen_random_uuid(), 'INACTIVE',   'Inactivo',  TRUE,  TRUE),
    (gen_random_uuid(), 'SUSPENDED',  'Suspenso',  FALSE, TRUE),
    (gen_random_uuid(), 'ON_LEAVE',   'De licença',FALSE, TRUE),
    (gen_random_uuid(), 'RETIRED',    'Aposentado',FALSE, TRUE)
ON CONFLICT (code) DO NOTHING;
