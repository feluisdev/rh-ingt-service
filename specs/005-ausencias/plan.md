# Implementation Plan: Módulo de Ausências — BC Colaboradores

**Branch**: `005-ausencias` | **Date**: 2026-05-01 | **Spec**: [spec.md](spec.md)

## Summary

Implementar o módulo de ausências dentro do BC `colaboradores/` do sistema RH do INGT: 6 entidades (TipoAusencia, SubtipoLicencaMobilidade, Feriado, PedidoAusencia, SaldoAusencia, LicencaMobilidade), com workflow de submissão/aprovação/rejeição/cancelamento de pedidos, cálculo automático de dias úteis, controlo de saldo anual e registo de licenças/mobilidade. Segue exactamente o padrão hexagonal + CQRS do BC colaboradores/ já implementado.

## Technical Context

**Language/Version**: Java 23  
**Primary Dependencies**: Spring Boot 3.5.3, Spring Data JPA, Hibernate Envers, Flyway, Lombok  
**Storage**: PostgreSQL 17 — migração V26  
**Testing**: JUnit 5 + Mockito via `mvn test`  
**Target Platform**: Spring Boot JAR (Linux server)  
**Project Type**: web-service REST (hexagonal architecture + CQRS)  
**Performance Goals**: <200ms p95 para GET de listas; cálculo de dias úteis síncrono e imperceptível  
**Constraints**: Hexagonal obrigatório; todos os IDs UUID; CQRS; Envers em todas as entidades  
**Scale/Scope**: ~500 funcionários, ~5000 pedidos/ano

## Constitution Check

| Princípio | Estado | Notas |
|---|---|---|
| I — Arquitectura Hexagonal | ✅ PASS | Camadas domain/application/infrastructure/interfaces respeitadas |
| II — CQRS | ✅ PASS | Toda a lógica de negócio em `@IgrpCommandHandler`/`@IgrpQueryHandler` |
| III — IGRP Studio | ✅ PASS | Controllers criados manualmente seguindo o padrão `@IgrpController` do BC colaboradores/ |
| IV — Auditoria Envers | ✅ PASS | `@Audited` em todas as 6 entidades JPA |
| V — Segurança por perfil | ✅ PASS | Sem alterações à configuração de segurança |
| Java 23 | ✅ PASS | |
| Spring Boot 3.5.3 | ✅ PASS | |
| UUID como chave primária | ✅ PASS | `ExternalID` wrapper em todos os value objects |
| Artefactos em pt-PT | ✅ PASS | |

## Project Structure

### Documentation (this feature)

```text
specs/005-ausencias/
├── plan.md              ← este ficheiro
├── research.md          ← decisões técnicas
├── data-model.md        ← schema das 6 entidades
├── quickstart.md        ← cenários C1–C5
├── contracts/
│   ├── tipos-ausencia.md
│   ├── subtipos-licenca-mobilidade.md
│   ├── feriados.md
│   ├── pedidos-ausencia.md
│   ├── saldos-ausencia.md
│   └── licencas-mobilidade.md
└── tasks.md             ← gerado por /speckit-tasks
```

### Source Code

```text
src/main/java/cv/igrp/RH_Service/colaboradores/
├── application/
│   ├── commands/
│   │   ├── CreateTipoAusenciaCommand.java + Handler
│   │   ├── UpdateTipoAusenciaCommand.java + Handler
│   │   ├── AtivarTipoAusenciaCommand.java + Handler
│   │   ├── DesativarTipoAusenciaCommand.java + Handler
│   │   ├── CreateSubtipoLicencaMobilidadeCommand.java + Handler
│   │   ├── UpdateSubtipoLicencaMobilidadeCommand.java + Handler
│   │   ├── AtivarSubtipoLicencaMobilidadeCommand.java + Handler
│   │   ├── DesativarSubtipoLicencaMobilidadeCommand.java + Handler
│   │   ├── CreateFeriadoCommand.java + Handler
│   │   ├── UpdateFeriadoCommand.java + Handler
│   │   ├── AtivarFeriadoCommand.java + Handler
│   │   ├── DesativarFeriadoCommand.java + Handler
│   │   ├── CreatePedidoAusenciaCommand.java + Handler
│   │   ├── AprovarPedidoAusenciaCommand.java + Handler
│   │   ├── RejeitarPedidoAusenciaCommand.java + Handler
│   │   ├── CancelarPedidoAusenciaCommand.java + Handler
│   │   ├── CreateSaldoAusenciaCommand.java + Handler
│   │   ├── UpdateSaldoAusenciaCommand.java + Handler
│   │   ├── CreateLicencaMobilidadeCommand.java + Handler
│   │   ├── UpdateLicencaMobilidadeCommand.java + Handler
│   │   ├── AtivarLicencaMobilidadeCommand.java + Handler
│   │   └── DesativarLicencaMobilidadeCommand.java + Handler
│   ├── queries/
│   │   ├── GetTipoAusenciaByIdQuery.java + Handler
│   │   ├── GetTiposAusenciaQuery.java + Handler
│   │   ├── GetSubtipoLicencaMobilidadeByIdQuery.java + Handler
│   │   ├── GetSubtiposLicencaMobilidadeQuery.java + Handler
│   │   ├── GetFeriadoByIdQuery.java + Handler
│   │   ├── GetFeriadosQuery.java + Handler
│   │   ├── GetPedidoAusenciaByIdQuery.java + Handler
│   │   ├── GetPedidosByFuncionarioQuery.java + Handler
│   │   ├── GetSaldoAusenciaByIdQuery.java + Handler
│   │   ├── GetSaldosByFuncionarioQuery.java + Handler
│   │   ├── GetLicencaMobilidadeByIdQuery.java + Handler
│   │   └── GetLicencasByFuncionarioQuery.java + Handler
│   └── dto/
│       ├── TipoAusenciaRequest.java / Response.java / WrapperLista...
│       ├── SubtipoLicencaMobilidadeRequest.java / Response.java / WrapperLista...
│       ├── FeriadoRequest.java / Response.java / WrapperLista...
│       ├── PedidoAusenciaRequest.java / Response.java / WrapperLista...
│       ├── AprovarPedidoRequest.java / RejeitarPedidoRequest.java
│       ├── SaldoAusenciaRequest.java / Response.java / WrapperLista...
│       └── LicencaMobilidadeRequest.java / Response.java / WrapperLista...
├── domain/
│   ├── models/
│   │   ├── TipoAusencia.java
│   │   ├── SubtipoLicencaMobilidade.java
│   │   ├── Feriado.java
│   │   ├── PedidoAusencia.java         ← com métodos aprovar/rejeitar/cancelar
│   │   ├── SaldoAusencia.java
│   │   └── LicencaMobilidade.java
│   ├── repository/
│   │   ├── TipoAusenciaRepository.java
│   │   ├── SubtipoLicencaMobilidadeRepository.java
│   │   ├── FeriadoRepository.java
│   │   ├── PedidoAusenciaRepository.java
│   │   ├── SaldoAusenciaRepository.java
│   │   └── LicencaMobilidadeRepository.java
│   ├── service/
│   │   └── DiasUteisCalculator.java    ← serviço de domínio
│   ├── valueobject/
│   │   ├── TipoAusenciaId.java
│   │   ├── SubtipoLicencaMobilidadeId.java
│   │   ├── FeriadoId.java
│   │   ├── PedidoAusenciaId.java
│   │   ├── SaldoAusenciaId.java
│   │   └── LicencaMobilidadeId.java
│   └── filter/
│       ├── TipoAusenciaFilter.java
│       ├── SubtipoLicencaMobilidadeFilter.java
│       ├── FeriadoFilter.java
│       ├── PedidoAusenciaFilter.java
│       ├── SaldoAusenciaFilter.java
│       └── LicencaMobilidadeFilter.java
└── infrastructure/
    ├── mappers/
    │   ├── TipoAusenciaMapper.java
    │   ├── SubtipoLicencaMobilidadeMapper.java
    │   ├── FeriadoMapper.java
    │   ├── PedidoAusenciaMapper.java
    │   ├── SaldoAusenciaMapper.java
    │   └── LicencaMobilidadeMapper.java
    └── persistence/
        ├── entity/
        │   ├── TipoAusenciaEntity.java
        │   ├── SubtipoLicencaMobilidadeEntity.java
        │   ├── FeriadoEntity.java
        │   ├── PedidoAusenciaEntity.java
        │   ├── SaldoAusenciaEntity.java
        │   └── LicencaMobilidadeEntity.java
        ├── repository/
        │   ├── ColabsTipoAusenciaEntityRepository.java
        │   ├── ColabsSubtipoLicencaMobilidadeEntityRepository.java
        │   ├── ColabsFeriadoEntityRepository.java
        │   ├── ColabsPedidoAusenciaEntityRepository.java
        │   ├── ColabsSaldoAusenciaEntityRepository.java
        │   └── ColabsLicencaMobilidadeEntityRepository.java
        └── adapters/
            ├── TipoAusenciaRepositoryImpl.java
            ├── SubtipoLicencaMobilidadeRepositoryImpl.java
            ├── FeriadoRepositoryImpl.java
            ├── PedidoAusenciaRepositoryImpl.java
            ├── SaldoAusenciaRepositoryImpl.java
            └── LicencaMobilidadeRepositoryImpl.java

src/main/java/cv/igrp/RH_Service/colaboradores/interfaces/rest/
    ├── TipoAusenciaController.java           ← gerado por igrp-spring-generator
    ├── SubtipoLicencaMobilidadeController.java
    ├── FeriadoController.java
    ├── PedidoAusenciaController.java
    ├── SaldoAusenciaController.java
    └── LicencaMobilidadeController.java

src/main/resources/db/migration/
    └── V26__create_ausencias_tables.sql
```

## Notas de Implementação

### DiasUteisCalculator
Serviço de domínio puro (sem dependências Spring). Recebe `LocalDate inicio`, `LocalDate fim`, `Set<LocalDate> feriadosNacionais`. Itera dia a dia, exclui `DayOfWeek.SATURDAY`, `DayOfWeek.SUNDAY` e datas no set de feriados. Lança `IgrpResponseStatusException.unprocessableEntity()` se `inicio > fim` ou resultado = 0.

### PedidoAusencia — Máquina de estados
Métodos de domínio: `aprovar(FuncionarioId aprovadoPor, LocalDate dataDecisao, String obs)`, `rejeitar(...)`, `cancelar()`. Cada método verifica o estado actual e lança `IgrpResponseStatusException.conflict()` se a transição for inválida.

### Bean name conflicts (prevenção)
Todas as interfaces Spring Data JPA devem usar o prefixo `Colabs` (ex: `ColabsTipoAusenciaEntityRepository`) para evitar conflitos com repositórios legados — padrão já estabelecido no BC colaboradores/.

### Entity name conflicts (prevenção)
Todas as entidades JPA devem usar `@Entity(name = "ColabsXEntity")` — padrão já estabelecido.

### Migração V26
```sql
-- Tabelas: t_leave_type, t_leave_mobility_subtype, t_public_holiday,
--          t_leave_request, t_leave_balance, t_leave_mobility
-- Índice único parcial para feriados nacionais
-- Seed: 11 feriados nacionais CV 2026
```

### Feriados seed — anos futuros
A seed é apenas para 2026. Para anos seguintes, o gestor de RH usa o endpoint `POST /feriados` para criar os registos. Não existe mecanismo automático de replicação nesta fase.
