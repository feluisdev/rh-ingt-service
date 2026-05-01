# Research: Colocações de Funcionários

**Feature**: 007-colocacoes-funcionario  
**Date**: 2026-05-01

---

## Decisão 1: Ponto de integração com LicencaMobilidade

**Decision**: Estender `AtivarLicencaMobilidadeCommandHandler` para injectar `ColocacaoRepository` e criar uma `Colocacao` do tipo `MOBILIDADE` quando a licença é activada.

**Rationale**: O handler já existente `AtivarLicencaMobilidadeCommandHandler` é o único ponto onde o estado da licença muda para activo. Adicionar a criação da colocação neste handler é a abordagem menos intrusiva e mantém a atomicidade da operação.

**Alternatives considered**:
- Evento de domínio: mais elegante mas introduz infraestrutura adicional (event bus) não presente no projecto.
- Handler separado `AprovarLicencaComColocacaoCommandHandler`: duplicaria a lógica de activação.

---

## Decisão 2: Validação de unidade orgânica e cargo (cross-BC)

**Decision**: Para validar a existência de `UnidadeOrganica` e `Cargo`, usar Spring Data JPA repositories das respectivas entidades (`OrganizationalUnitEntity`, `CargoEntity`) directamente no `RegistarColocacaoCommandHandler`, sem criar domain ports completos para esses BCs.

**Rationale**: Os módulos `estrutura` e `shared` não expõem domain repository ports completos ainda. Criar ports completos nesta feature estaria fora de âmbito. A injecção directa de JPA repositories para validação simples de existência é pragmática e alinhada com o padrão já usado noutras cross-BC queries no projecto.

**Alternatives considered**:
- Domain ports completos: mais correcto architecturalmente mas duplicaria esforço sem valor imediato.
- Sem validação: viola FR-005 e FR-006.

---

## Decisão 3: `unit_id` opcional na colocação de mobilidade

**Decision**: O campo `unit_id` (unidade) é `nullable` na tabela. Para colocações do tipo `MOBILIDADE`, pode ser `null` se `entidadeDestino` da `LicencaMobilidade` for apenas um nome textual sem UUID correspondente.

**Rationale**: O modelo `LicencaMobilidade` tem `entidadeDestino` como `String` (nome textual), não um UUID de `t_unidade_organica`. Forçar um UUID quebraria aprovações onde a mobilidade é para entidade externa. A especificação (US5-AC2) permite colocação sem unidade em mobilidade.

**Alternatives considered**:
- Forçar UUID: quebraria aprovações para entidades externas.
- Ignorar mobilidade: viola FR-014.

---

## Decisão 4: Gestão de `is_current` — atomicidade

**Decision**: O método `ColocacaoRepository.fecharColocacaoAtual(FuncionarioId)` actualiza via JPQL `UPDATE ... SET is_current=false, end_date=:hoje WHERE funcionario_id=:id AND is_current=true`, executado antes de criar a nova colocação, dentro da mesma transacção.

**Rationale**: Garante que nunca existem duas colocações `is_current=true` para o mesmo funcionário, mesmo em cenários de concorrência (Postgres lock row-level durante UPDATE). É simples, sem triggers, e alinhado com o padrão do campo `is_current` já usado em `employee_professional_assignments`.

**Alternatives considered**:
- Trigger PostgreSQL: mais robusto mas adiciona lógica fora do Java, difícil de testar e auditar.
- Lógica em Java via findAll: race condition em concorrência.

---

## Decisão 5: Número da migração Flyway

**Decision**: `V28__create_employee_unit_assignments.sql`

**Rationale**: A última migração existente é V27 (t_document). Sequência natural é V28.

---

## Decisão 6: Nome do `@Entity` JPA

**Decision**: `@Entity(name = "ColabsColocacaoEntity")` com `@Table(name = "employee_unit_assignments")`.

**Rationale**: Seguindo o padrão obrigatório do projecto (`@Entity(name="Colabs...")`) para evitar colisões com o Hibernate (documentado na implementação de Documentos). O nome da tabela segue o modelo relacional v4.0.
