# Project Context

## What This Is
Este projeto, `sipprog-rh-backend`, é o backend do sistema de Recursos Humanos e Planeamento (SIGDI) desenvolvido em Java/Spring Boot com base na Framework IGRP. O foco principal atual é garantir a integração completa e correta com o frontend, estabilizar/refatorar o código existente e desenvolver novas funcionalidades, dando absoluta prioridade aos módulos Tático e Estratégico do SIGDI.

## Core Value
Garantir que o frontend consiga consumir as APIs sem falhas (ajustando contratos, auth e endpoints), melhorar a estabilidade e performance do sistema através de testes rigorosos com dados reais, e entregar as funcionalidades de gestão tática e estratégica necessárias.

## Requirements

### Validated

- ✓ Mapeamento base e geração de controladores, DTOs e entidades a partir dos manifestos `.igrpstudio` para os módulos `funcionarios` e `sigdi`.
- ✓ Infraestrutura base configurada (Spring Boot 3.5.x, PostgreSQL, Spring Security OAuth2, Eureka).

### Active

- [ ] Ajustar DTOs de Request e Response para alinhamento total com os contratos esperados pelo Frontend, priorizando os endpoints Táticos e Estratégicos.
- [ ] Completar endpoints em falta no `TaticalController` e `StrategyController` para garantir fluxos completos de gestão.
- [ ] Validar e corrigir fluxos de autenticação, permissões e CORS, assegurando que a comunicação com o Frontend é segura e sem bloqueios.
- [ ] Refatorar código existente para aplicar as convenções do projeto (sem dados mockados, remoção de código comentado, tratamento de erros claro e documentação focada no "porquê").
- [ ] Aumentar a cobertura de testes lógicos no módulo SIGDI utilizando a base de dados real (apagando testes exploratórios).
- [ ] Otimizar queries e performance nas listagens e pesquisas pesadas dos módulos Tático e Estratégico.

### Out of Scope

- [Novas features do módulo Funcionários] — O foco atual (prioridade) está no módulo SIGDI (Estratégico e Tático).
- [Testes com dados mockados] — Regra explícita do utilizador proíbe a utilização de dados mockados/falsos ("REAL DATA ONLY").

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Foco nos Módulos Tático/Estratégico | Foram indicados como prioridades críticas de negócio. | — Pending |
| Refatorização sem perda de sincronismo | Garantir que o código se mantém alinhado com `.igrpstudio` para evitar sobrescritas durante a geração de código. | — Pending |
| Testes apenas com dados reais | Diretriz clara do utilizador para testar a lógica de negócio na base de dados diretamente. | — Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-04-25 after initialization*
