INSERT INTO t_professional_situation (id, code, description, is_active) VALUES
    (gen_random_uuid(), 'EFETIVO',       'Efectivo',           TRUE),
    (gen_random_uuid(), 'CONTRATADO',    'Contratado',         TRUE),
    (gen_random_uuid(), 'COMISSIONADO',  'Comissionado',       TRUE),
    (gen_random_uuid(), 'ESTAGIARIO',    'Estagiário',         TRUE),
    (gen_random_uuid(), 'DESTACADO',     'Destacado',          TRUE),
    (gen_random_uuid(), 'REQUISITADO',   'Requisitado',        TRUE)
ON CONFLICT (code) DO NOTHING;
