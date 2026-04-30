-- Conforme Decreto-Lei 4/2024 e regime geral da função pública cabo-verdiana
INSERT INTO t_contract_type (id, code, description, is_active) VALUES
    (gen_random_uuid(), 'INDEFINIDO',         'Contrato por Tempo Indeterminado', TRUE),
    (gen_random_uuid(), 'TERMO_CERTO',        'Contrato a Termo Certo',           TRUE),
    (gen_random_uuid(), 'TERMO_INCERTO',      'Contrato a Termo Incerto',         TRUE),
    (gen_random_uuid(), 'PRESTACAO_SERVICOS', 'Prestação de Serviços',            TRUE),
    (gen_random_uuid(), 'COMISSAO_SERVICO',   'Comissão de Serviço',              TRUE)
ON CONFLICT (code) DO NOTHING;
