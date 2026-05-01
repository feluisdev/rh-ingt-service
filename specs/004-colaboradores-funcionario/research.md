# Research: Módulo Colaboradores — Funcionário e Sub-domínios

## Decisão 1: Geração de `numero_funcionario`

**Decision**: Sequência PostgreSQL dedicada (`seq_numero_funcionario`) + formatação no handler.
**Rationale**: Uma sequência de BD garante atomicidade e unicidade sem bloqueios de tabela. O handler lê `nextval('seq_numero_funcionario')` via `JdbcTemplate` e formata como `F%06d`. Sem colisão em concorrência.
**Alternatives considered**: UUID formatado (não sequencial, difícil de ler); MAX(id)+1 (race condition); gerado em memória com AtomicLong (não sobrevive a reinícios).

## Decisão 2: `is_active` derivado de `situacao_profissional`

**Decision**: Campo `is_active` na entidade JPA é actualizado automaticamente pelo domain model sempre que `situacao_profissional` muda. Regra: `is_active = (situacao_profissional == 'ATIVO')`.
**Rationale**: Mantém consistência sem campo independente; o domínio é a única fonte de verdade; a filtragem por `is_active` na query JPA é simples e indexável.
**Alternatives considered**: Campo independente (propenso a inconsistência); calcular no momento da query (não indexável).

## Decisão 3: Cargo ↔ Unidade Orgânica — sem validação cruzada

**Decision**: A validação cruzada cargo/unidade (Q3 da clarificação) **não é aplicável**. `t_cargo` é um catálogo global sem `unidade_organica_id`. O enquadramento regista cargo e unidade de forma independente.
**Rationale**: Inspecção do schema existente (`V20__create_estrutura_tables.sql` e `JobEntity.java`) confirma que `t_cargo` não tem FK para `t_unidade_organica`. O spec Q3 foi baseado numa assunção incorrecta sobre o modelo de dados existente.
**Action**: FR-009b (validação cruzada cargo/unidade) será removido do plano de implementação. Cargo e unidade orgânica são validados individualmente (activos e existentes), mas não cruzados entre si.

## Decisão 4: Encerramento automático do enquadramento anterior

**Decision**: Operação atómica dentro do `CreateEnquadramentoCommandHandler` numa única transacção: UPDATE enquadramento anterior (`data_fim`, `is_current = false`) + INSERT novo enquadramento (`is_current = true`).
**Rationale**: Garante consistência sem janelas de inconsistência; Spring `@Transactional` assegura rollback em caso de falha.
**Alternatives considered**: Evento de domínio (overengineering para este caso); trigger de BD (esconde lógica fora do domain).

## Decisão 5: Contrato — rejeição ao criar segundo activo

**Decision**: `CreateContratoCommandHandler` verifica se existe contrato activo antes de persistir; se sim, retorna 409 Conflict.
**Rationale**: O gestor deve encerrar explicitamente o contrato anterior, garantindo intenção e rastreabilidade. Sem auto-encerramento implícito (diferente do enquadramento).
**Alternatives considered**: Auto-encerrar o anterior (perde clareza sobre o encerramento; contratos têm semântica legal distinta dos enquadramentos).

## Decisão 6: Flyway — versão V24

**Decision**: Próxima migração é `V24__create_colaboradores_tables.sql`.
**Rationale**: Última migração existente é V23 (carreiras). A migração cria: `seq_numero_funcionario`, `t_funcionario`, `employee_professional_assignments`, `t_contrato`, `t_dependente`, `t_qualificacao`.

## Decisão 7: Auditoria da tabela `employee_professional_assignments`

**Decision**: `@Audited` via Envers; a tabela de revisões (`employee_professional_assignments_aud`) é criada automaticamente.
**Rationale**: Constituição exige auditoria em todas as entidades. O histórico de enquadramentos já serve como histórico operacional, mas o Envers acrescenta o who/when das alterações.

## Decisão 8: Endpoint de auditoria

**Decision**: `GET /api/v1/rh/colaboradores/audit/{catalog}/{entityId}` — mesmo padrão dos módulos estrutura/ e carreiras/.
**Rationale**: Consistência com módulos anteriores; catálogos: `funcionarios`, `enquadramentos`, `contratos`, `dependentes`, `qualificacoes`.
