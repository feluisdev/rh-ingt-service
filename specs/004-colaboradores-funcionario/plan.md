# Implementation Plan: Módulo Colaboradores — Funcionário e Sub-domínios

**Branch**: `004-colaboradores-funcionario` | **Date**: 2026-04-30 | **Spec**: [spec.md](spec.md)

## Summary

Implementar o BC `colaboradores/` com 5 agregados: Funcionário (raiz), EnquadramentoProfissional, Contrato, Dependente e Qualificação. Arquitectura hexagonal idêntica aos módulos `estrutura/` e `carreiras/`. Flyway V24. Envers em todas as entidades.

## Technical Context

**Language/Version**: Java 23
**Primary Dependencies**: Spring Boot 3.5.3, Spring Data JPA, Hibernate Envers, Hibernate 6, Flyway, Lombok, Jakarta Validation
**Storage**: PostgreSQL 17 — tabelas `t_funcionario`, `employee_professional_assignments`, `t_contrato`, `t_dependente`, `t_qualificacao`, sequence `seq_numero_funcionario`
**Testing**: `mvn test` (unit); testes manuais via quickstart.md
**Target Platform**: JVM / Linux server
**Project Type**: Microserviço REST (Spring Boot)
**Performance Goals**: Listagem de até 10 000 funcionários em < 2 s com filtros combinados
**Constraints**: UUID PKs; ExternalID wrapper; sem SQL nativo excepto `nextval` para numero_funcionario e onde absolutamente necessário
**Scale/Scope**: ~10 000 funcionários; histórico de enquadramentos: ~5 por funcionário

## Constitution Check

| Princípio | Status | Notas |
|-----------|--------|-------|
| I. Arquitectura Hexagonal | ✅ PASS | domain/application/infrastructure/interfaces — idêntico a estrutura/ e carreiras/ |
| II. CQRS — handlers como fonte única | ✅ PASS | Toda lógica de negócio em CommandHandlers/QueryHandlers |
| III. IGRP Studio — controllers gerados | ✅ PASS | Controllers gerados via igrp-spring-generator; nunca editados manualmente |
| IV. Auditoria Envers obrigatória | ✅ PASS | @Audited em todas as 5 entidades |
| V. Segurança por perfil | ✅ PASS | Sem alterações à configuração de segurança |
| UUID PKs | ✅ PASS | ExternalID em todos os value objects |
| Artefactos em pt-PT | ✅ PASS | Todos os artefactos em Português Europeu |

**Sem violações detectadas. Pronto para implementação.**

## Project Structure

### Documentation (this feature)

```text
specs/004-colaboradores-funcionario/
├── plan.md              # Este ficheiro
├── research.md          # Decisões técnicas
├── data-model.md        # Modelo de dados
├── quickstart.md        # Cenários de teste manuais
├── contracts/
│   ├── funcionarios.md
│   ├── enquadramentos.md
│   ├── contratos.md
│   └── dependentes.md   # inclui qualificações
└── tasks.md             # Gerado por /speckit-tasks
```

### Source Code

```text
src/main/java/cv/igrp/RH_Service/colaboradores/
├── application/
│   ├── commands/
│   │   ├── CreateFuncionarioCommand + Handler
│   │   ├── UpdateFuncionarioCommand + Handler
│   │   ├── CreateEnquadramentoCommand + Handler
│   │   ├── CreateContratoCommand + Handler
│   │   ├── UpdateContratoCommand + Handler
│   │   ├── DesativarContratoCommand + Handler
│   │   ├── AtivarContratoCommand + Handler
│   │   ├── CreateDependenteCommand + Handler
│   │   ├── UpdateDependenteCommand + Handler
│   │   ├── DesativarDependenteCommand + Handler
│   │   ├── AtivarDependenteCommand + Handler
│   │   ├── CreateQualificacaoCommand + Handler
│   │   ├── UpdateQualificacaoCommand + Handler
│   │   ├── DesativarQualificacaoCommand + Handler
│   │   └── AtivarQualificacaoCommand + Handler
│   ├── queries/
│   │   ├── GetFuncionariosQuery + Handler
│   │   ├── GetFuncionarioByIdQuery + Handler
│   │   ├── GetEnquadramentoAtualQuery + Handler
│   │   ├── GetEnquadramentosHistoricoQuery + Handler
│   │   ├── GetEnquadramentoByIdQuery + Handler
│   │   ├── GetContratosByFuncionarioQuery + Handler
│   │   ├── GetContratoByIdQuery + Handler
│   │   ├── GetDependentesByFuncionarioQuery + Handler
│   │   ├── GetQualificacoesByFuncionarioQuery + Handler
│   │   └── GetColaboradoresAuditHistoryQuery + Handler
│   └── dto/
│       ├── FuncionarioRequest / FuncionarioResponse / WrapperListaFuncionarioDTO
│       ├── EnquadramentoRequest / EnquadramentoResponse / WrapperListaEnquadramentoDTO
│       ├── ContratoRequest / ContratoResponse / WrapperListaContratoDTO
│       ├── DependenteRequest / DependenteResponse / WrapperListaDependenteDTO
│       ├── QualificacaoRequest / QualificacaoResponse / WrapperListaQualificacaoDTO
│       └── AuditHistoryEntryDTO / WrapperListaAuditHistoryDTO
├── domain/
│   ├── models/
│   │   ├── Funcionario.java
│   │   ├── EnquadramentoProfissional.java
│   │   ├── Contrato.java
│   │   ├── Dependente.java
│   │   └── Qualificacao.java
│   ├── filter/
│   │   ├── FuncionarioFilter.java
│   │   ├── EnquadramentoFilter.java
│   │   ├── ContratoFilter.java
│   │   └── DependenteFilter.java / QualificacaoFilter.java
│   ├── repository/
│   │   ├── FuncionarioRepository.java
│   │   ├── EnquadramentoRepository.java
│   │   ├── ContratoRepository.java
│   │   ├── DependenteRepository.java
│   │   └── QualificacaoRepository.java
│   └── valueobject/
│       ├── FuncionarioId.java
│       ├── EnquadramentoId.java
│       ├── ContratoId.java
│       ├── DependenteId.java
│       └── QualificacaoId.java
├── infrastructure/
│   ├── mappers/
│   │   ├── FuncionarioMapper.java
│   │   ├── EnquadramentoMapper.java
│   │   ├── ContratoMapper.java
│   │   ├── DependenteMapper.java
│   │   └── QualificacaoMapper.java
│   └── persistence/
│       ├── adapters/
│       │   ├── FuncionarioRepositoryImpl.java
│       │   ├── EnquadramentoRepositoryImpl.java
│       │   ├── ContratoRepositoryImpl.java
│       │   ├── DependenteRepositoryImpl.java
│       │   └── QualificacaoRepositoryImpl.java
│       ├── entity/
│       │   ├── FuncionarioEntity.java
│       │   ├── EnquadramentoEntity.java
│       │   ├── ContratoEntity.java
│       │   ├── DependenteEntity.java
│       │   └── QualificacaoEntity.java
│       └── repository/
│           ├── FuncionarioEntityRepository.java
│           ├── EnquadramentoEntityRepository.java
│           ├── ContratoEntityRepository.java
│           ├── DependenteEntityRepository.java
│           └── QualificacaoEntityRepository.java
└── interfaces/
    └── rest/
        ├── FuncionarioController.java          (gerado por igrp-spring-generator)
        ├── EnquadramentoController.java        (gerado)
        ├── ContratoController.java             (gerado)
        ├── DependenteController.java           (gerado)
        ├── QualificacaoController.java         (gerado)
        └── ColaboradoresAuditHistoryController.java (gerado)

src/main/resources/db/migration/
└── V24__create_colaboradores_tables.sql
```

## Key Implementation Notes

### numero_funcionario (geração atómica)

```java
// No CreateFuncionarioCommandHandler:
long seq = jdbcTemplate.queryForObject("SELECT nextval('seq_numero_funcionario')", Long.class);
String numeroFuncionario = String.format("F%06d", seq);
```

### is_active derivado no domínio

```java
// No Funcionario.java:
public void atualizar(..., String situacaoProfissional, ...) {
    this.situacaoProfissional = situacaoProfissional;
    this.isActive = "ATIVO".equals(situacaoProfissional);
    // ...
}
```

### Encerramento atómico do enquadramento anterior

```java
// No CreateEnquadramentoCommandHandler (numa transacção):
// 1. Buscar enquadramento actual (isCurrent = true)
// 2. Se existe: atualizar dataFim = dataInicio - 1 dia, isCurrent = false → save
// 3. Criar novo enquadramento com isCurrent = true → save
```

### Guard: cargo e unidade orgânica independentes

O `t_cargo` é catálogo global sem FK para `t_unidade_organica`. Validação: apenas verificar se cada entidade existe e está activa individualmente. Sem validação cruzada cargo/unidade (FR-009b removido — não aplicável no schema actual).

### Auditoria — endpoint de historial

Catálogos suportados: `funcionarios` → `FuncionarioEntity`, `enquadramentos` → `EnquadramentoEntity`, `contratos` → `ContratoEntity`, `dependentes` → `DependenteEntity`, `qualificacoes` → `QualificacaoEntity`. Padrão idêntico ao `GetCarreirasAuditHistoryQueryHandler`.

## Complexity Tracking

Sem violações à constituição. Sem complexidade adicional justificada.
