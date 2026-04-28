<!--
  Sync Impact Report
  Versão: 0.0.0 → 1.0.0
  - Constituição inicial do projeto RH-Service (criada a partir do template)
  - Princípios derivados do CLAUDE.md e arquitetura documentada do projeto
  - Adicionados: Princípios I–V, Restrições Técnicas, Convenções de Desenvolvimento, Governação
  - Removidos: placeholders de exemplo sem substituição
  - Templates verificados:
      ✅ .specify/templates/plan-template.md — secção "Constitution Check" genérica, compatível
      ✅ .specify/templates/spec-template.md — sem referências a princípios específicos, compatível
      ✅ .specify/templates/tasks-template.md — estrutura por user story, compatível
  - Deferred:
      TODO(RATIFICATION_DATE): data de ratificação original desconhecida; marcar quando acordada com a equipa
-->

# RH-Service Constitution

## Princípios Fundamentais

### I. Arquitetura Hexagonal (NÃO NEGOCIÁVEL)

Todo o código de domínio DEVE ser isolado de detalhes de infraestrutura. A estrutura de cada módulo DEVE seguir estritamente as seguintes camadas:

- `interfaces/rest/` — controllers gerados automaticamente pelo IGRP Studio (nunca editar)
- `application/` — handlers de comandos/queries e DTOs
- `domain/` — modelos de domínio puros e lógica de negócio (sem dependências de infraestrutura)
- `infrastructure/` — repositórios JPA e adaptadores externos

**Proibido**: lógica de negócio em controllers; acesso direto à base de dados a partir da camada de domínio; dependências circulares entre camadas; importar classes de `infrastructure` a partir de `domain`.

### II. CQRS — Handlers como Única Fonte de Lógica de Negócio

A lógica de negócio DEVE residir exclusivamente nos handlers anotados com `@IgrpCommandHandler`. Controllers anotados com `@IgrpController` NUNCA devem conter lógica de negócio. Qualquer operação de escrita DEVE ser modelada como um Command; qualquer operação de leitura DEVE ser modelada como uma Query. Esta separação é obrigatória e não admite exceções.

### III. IGRP Studio — Controllers São Gerados, Nunca Editados

Controllers em `interfaces/rest/` são gerados automaticamente a partir de manifests em `.igrpstudio/<módulo>/` e estão marcados com `/* THIS FILE WAS GENERATED AUTOMATICALLY */`. Regenerar um manifest sobrescreve o controller — qualquer edição manual será perdida. Para adicionar novos endpoints ou módulos, DEVE usar-se o skill `igrp-spring-generator` e atualizar os manifests correspondentes.

### IV. Auditoria Obrigatória por Hibernate Envers

Todas as entidades persistidas DEVEM ser auditadas via Hibernate Envers. Os campos de rastreio (`createdAt`, `updatedAt`, utilizador atuante) DEVEM ser preenchidos automaticamente através de `ApplicationAuditorAware`, que lê o contexto de segurança via `SecurityContextHelper`. Nenhuma entidade de domínio escrita pode ficar isenta de auditoria.

### V. Segurança Estrita por Perfil de Execução

O comportamento de segurança DEVE variar exclusivamente com base no perfil ativo (`SERVICE_PROFILE`):

| Perfil | Comportamento |
|---|---|
| `development` / `staging` | Segurança totalmente desativada, CORS permissivo |
| `production` | OAuth2 Resource Server com validação JWT via Keycloak obrigatória |

NUNCA assumir que a ausência de segurança em `development` representa o comportamento de produção. Fluxos de autenticação DEVEM ser testados apenas contra um ambiente com o perfil `production` ativo.

## Restrições Técnicas

As seguintes restrições são não negociáveis e DEVEM ser respeitadas em todas as features:

- **Linguagem**: Java 23 — sem downgrade
- **Framework**: Spring Boot 3.5.3 + Spring Cloud 2025.0.0
- **Base de Dados**: PostgreSQL 17 via JPA/Hibernate — único ORM permitido; sem SQL nativo exceto onde Hibernate seja insuficiente e devidamente justificado
- **Chave Primária**: Todas as entidades DEVEM usar `ExternalID` (wrapper UUID) como identificador primário — sem chaves inteiras auto-incrementadas
- **Armazenamento de Ficheiros**: MinIO (S3-compatible) — sem alternativa para armazenamento de ficheiros
- **Autenticação**: Keycloak com OAuth2/JWT — proibido implementar autenticação própria
- **Idioma dos Artefactos**: Todos os artefactos SpecKit (specs, planos, tasks, checklists) DEVEM ser escritos em Português Europeu (pt-PT)

## Convenções de Desenvolvimento

- **Commits**: Conventional Commits com scope por módulo
  ```
  feat(funcionarios): ...
  fix(sigdi): ...
  refactor(security): ...
  docs(sigdi): ...
  ```
- **Novos endpoints/módulos**: Sempre via skill `igrp-spring-generator` com atualização dos manifests em `.igrpstudio/`
- **Testes**: `mvn test` para execução completa; `mvn test -Dtest=NomeDaClasse` para classe individual
- **Configuração**: Variáveis de ambiente definidas em `.env` para desenvolvimento local; NUNCA hardcoded no código fonte
- **Swagger**: Disponível em `http://localhost:8091/swagger-ui.html` quando `ENABLE_SWAGGER=true`

## Governação

Esta constituição é o documento de referência para todas as decisões de arquitetura e desenvolvimento do RH-Service. Qualquer desvio aos princípios DEVE ser explicitamente justificado, documentado e aprovado antes de ser implementado.

**Procedimento de Emenda**:
1. Propor alteração com justificação técnica clara
2. Documentar o impacto nos templates SpecKit e artefactos existentes
3. Incrementar a versão segundo semver:
   - MAJOR: remoção ou redefinição incompatível de princípio
   - MINOR: novo princípio ou secção adicionada
   - PATCH: clarificação, reformulação ou correção tipográfica
4. Atualizar `LAST_AMENDED_DATE` para a data de aprovação

**Revisão de Conformidade**: A conformidade com esta constituição DEVE ser verificada na secção "Constitution Check" de cada `plan.md` gerado por `/speckit-plan`, e em cada revisão de Pull Request.

**Versão**: 1.0.0 | **Ratificada**: TODO(RATIFICATION_DATE): data desconhecida — confirmar com a equipa | **Última Alteração**: 2026-04-27
