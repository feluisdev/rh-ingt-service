INSERT INTO t_leave_mobility_subtype (id, code, description, record_type, affects_pay, counts_for_seniority, can_self_submit, is_active) VALUES
    (gen_random_uuid(), 'LIC_SEM_VENC',      'Licença sem Vencimento',     'LICENCA',    FALSE, TRUE,  FALSE, TRUE),
    (gen_random_uuid(), 'LIC_COM_VENC',      'Licença com Vencimento',     'LICENCA',    TRUE,  TRUE,  FALSE, TRUE),
    (gen_random_uuid(), 'MOBILIDADE_INT',    'Mobilidade Interna',         'MOBILIDADE', TRUE,  TRUE,  FALSE, TRUE),
    (gen_random_uuid(), 'MOBILIDADE_EXT',    'Mobilidade Externa',         'MOBILIDADE', TRUE,  TRUE,  FALSE, TRUE),
    (gen_random_uuid(), 'REQUISICAO',        'Requisição',                 'MOBILIDADE', TRUE,  TRUE,  FALSE, TRUE),
    (gen_random_uuid(), 'DESTACAMENTO',      'Destacamento',               'AMBOS',      TRUE,  TRUE,  FALSE, TRUE)
ON CONFLICT (code) DO NOTHING;
