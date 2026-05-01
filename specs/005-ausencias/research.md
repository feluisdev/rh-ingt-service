# Research: Módulo de Ausências

**Feature**: 005-ausencias  
**Data**: 2026-05-01

## Decisão 1 — Cálculo de dias úteis

**Decision**: Utilitário de domínio com `java.time.LocalDate` e iteração diária.

**Rationale**: Iterar de `dataInicio` a `dataFim` (inclusive) verificando `DayOfWeek` e consultando a lista de feriados nacionais activos do ano em memória. Sem biblioteca externa — o projecto já usa Java 23 com suporte completo a `java.time`. A lista de feriados é carregada uma vez por ano e passada ao utilitário como `Set<LocalDate>`.

**Alternatives considered**: ThreeTen-Extra (`LocalDateRange`) — dependência extra desnecessária; Apache Commons Lang — legacy e desnecessário com Java 23.

**Implementation note**: Criar `DiasUteisCalculator` como serviço de domínio em `colaboradores/domain/service/`. Recebe `LocalDate inicio`, `LocalDate fim`, `Set<LocalDate> feriadosNacionais`. Lança `DomainException` se resultado for zero ou negativo.

---

## Decisão 2 — Máquina de estados para PedidoAusencia

**Decision**: Métodos de transição no agregado de domínio (`pedido.aprovar()`, `pedido.rejeitar()`, `pedido.cancelar()`).

**Rationale**: 4 estados simples, sem concorrência paralela nem timer-based transitions. Manter a lógica no agregado preserva a ubiquitous language e elimina dependências de framework na camada de domínio. Cada método valida a transição (lança `IgrpResponseStatusException` se inválida) e actualiza os campos de decisão internamente.

**Alternatives considered**: Spring State Machine — overkill para 4 estados, adiciona complexidade de configuração e viola o princípio de isolamento de domínio.

---

## Decisão 3 — Índice único parcial para Feriados

**Decision**: `CREATE UNIQUE INDEX` com cláusula `WHERE` em Flyway SQL puro.

**Rationale**: PostgreSQL suporta partial indexes nativamente. Flyway executa SQL puro sem wrapper. A constraint garante que apenas um feriado nacional activo pode existir por data, sem afectar feriados municipais ou inactivos.

```sql
CREATE UNIQUE INDEX idx_feriado_nacional_data_unique
    ON t_public_holiday (data)
    WHERE is_national = true AND is_active = true;
```

---

## Decisão 4 — Enums em JPA/PostgreSQL

**Decision**: `@Enumerated(EnumType.STRING)` com coluna `VARCHAR` (não tipo ENUM PostgreSQL nativo).

**Rationale**: Usar tipo ENUM PostgreSQL nativo (`CREATE TYPE`) em vez de VARCHAR requer DDL extra em migrations e complicações em `ALTER TYPE ADD VALUE` para novos valores. Para um projecto com Flyway e evolução frequente de catálogos, `VARCHAR` com `@Enumerated(EnumType.STRING)` é mais simples de manter. A validação de valores é feita ao nível da aplicação (enum Java). Padrão já usado nos módulos `estrutura/` e `carreiras/`.

**Alternatives considered**: Tipo ENUM PostgreSQL nativo — mais rigoroso a nível de BD mas mais rígido na evolução do schema.

---

## Decisão 5 — Localização dos catálogos TipoAusencia e SubtipoLicencaMobilidade

**Decision**: Dentro do BC `colaboradores/` (não em `shared/`), seguindo a decisão arquitectural já documentada.

**Rationale**: Estes catálogos têm flags comportamentais (`deducts_balance`, `affects_pay`, etc.) que os diferenciam dos lookups puros em `shared/Option`. A decisão arquitectural do projecto é explícita: catálogos com flags de comportamento ficam no BC que os usa.

**Endpoints**: Sob `api/v1/rh/parametrizacoes/` (consistente com os outros catálogos do módulo RH).

---

## Decisão 6 — Extensão do BC colaboradores/ vs novo BC

**Decision**: Ausências são implementadas dentro do BC `colaboradores/` existente, no mesmo pacote Java `cv.igrp.RH_Service.colaboradores`.

**Rationale**: As entidades têm FK directa para `t_funcionario`. Criar um BC separado exigiria chamadas cross-BC ou duplicação do `funcionario_id`. A arquitectura do projecto usa `colaboradores/` como BC principal para tudo relacionado com o funcionário. A migração Flyway usa o próximo número sequencial (V26).

**Alternatives considered**: BC `ausencias/` separado — aumenta complexidade de configuração Spring sem benefício claro nesta fase.
