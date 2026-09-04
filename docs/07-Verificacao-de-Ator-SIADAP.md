# Verificação de Ator nos Comandos SIADAP

**Versão:** 1.0 | **Status:** Em atualização (Fase 101) | **Data:** 21/08/2026

---

> **Revertido em 26/08/2026.** A decisão registada nas secções abaixo — a pertença ao CCA
> por lista de `FuncionarioId` em configuração — deixou de estar em vigor. As secções
> `## O que esta configuração decide`, `## O que acontece se ninguém a configurar` e
> `## Como configurar` descrevem um mecanismo que já não existe no código; ficam no
> documento como registo histórico, não como instrução. Ver
> `## Reversão de 2026-08-26: da lista de FuncionarioId para a permissão IGRP` para o que
> está em vigor hoje.

## O que esta configuração decide

A pertença ao Conselho Coordenador da Avaliação (CCA) identifica-se por uma lista de
`FuncionarioId` em configuração — a propriedade `sigdi.siadap.cca-employee-ids`, ligada à
variável de ambiente `SIADAP_CCA_EMPLOYEE_IDS`. É decisão do operador, tomada a
2026-08-21 e não reaberta por este documento.

Não se inventou nenhum nome de papel IAM para este efeito. O projeto já tem `ACH-A-01`
em aberto precisamente por causa de um papel IAM (`sigdi.paa.submission-period-role`)
cuja string exata nunca foi confirmada contra o realm real. Uma lista de UUIDs comparada
diretamente contra `FuncionarioId` não pertence à mesma classe de risco: não depende de
uma `authority` do Keycloak ainda por verificar, é uma comparação de identificadores
internos que a aplicação já resolve por outras vias (`CurrentEmployeeResolver`).

## O que acontece se ninguém a configurar (histórico, revertido em 26/08/2026)

> Esta secção descreve o mecanismo de lista anterior à reversão. Não reflete o
> comportamento atual — ver `## Reversão de 2026-08-26` abaixo.

Sem `SIADAP_CCA_EMPLOYEE_IDS` definida, a propriedade normaliza para uma lista vazia e
**ninguém é reconhecido como CCA** — incluindo utilizadores com o papel `RH`. As duas
ações que dependem desta verificação devolvem `403` a todos os chamadores:

- Atribuir menção de mérito (`AssignMeritRatingCommandHandler`)
- Encerrar ciclos de avaliação (`CloseEvaluationsCommandHandler`)

É um comportamento fail-closed **por decisão**, e mantém-se — o que se corrigiu nesta
fase foi o silêncio, não o fecho. O arranque da aplicação emite uma linha `WARN` no
logger `cv.igrp.RH_Service.sigdi.application.config.SiadapCcaSecurityProperties` que
nomeia a propriedade, a variável de ambiente e a consequência literal. Uma segunda linha
`WARN`, com a mesma informação, é emitida em cada chamada recusada por falta de
configuração — para que a recusa seja diagnosticável mesmo quando o log de arranque já
rolou para fora do ecrã.

## Como configurar (histórico, revertido em 26/08/2026)

> Estas instruções configuram uma propriedade que já ninguém lê. Não as sigas — ver
> `## Reversão de 2026-08-26` abaixo para como a pertença ao CCA se decide hoje.

Definir `SIADAP_CCA_EMPLOYEE_IDS` com uma lista de `FuncionarioId` (UUID) separados por
vírgula. Espaços à volta de cada UUID e vírgulas a mais são tolerados e descartados na
normalização — por exemplo, `" id1 , , id2 , "` normaliza para `{id1, id2}`.

Os `FuncionarioId` a usar são a coluna `external_id` da tabela de funcionários,
consultável através da listagem de colaboradores da aplicação.

## Reversão de 2026-08-26: da lista de FuncionarioId para a permissão IGRP

**O que se reverte.** A lista de `FuncionarioId` em `SIADAP_CCA_EMPLOYEE_IDS` deixa de
decidir quem pertence ao CCA. A decisão de 2026-08-21, registada nas secções acima, foi
tomada e vigorou; não se apaga nem se reescreve — reverte-se aqui, por acréscimo datado.
A pertença ao CCA passa a ser decidida por três permissões IGRP, declaradas em
`AppPermissions.java` e sincronizadas com a plataforma de gestão de acessos:

- `siadap.cca.consultarEstado` — decide a resposta de `GET siadap/me/cca-status`.
- `siadap.mencaoMerito.atribuir` — decide `POST siadap/evaluations/{id}/merit-rating`.
- `siadap.avaliacoes.fecharEmLote` — decide `POST siadap/evaluations/close`.

**O risco que a decisão de 2026-08-21 evitava volta, e é assumido por decisão.** A secção
`## O que esta configuração decide` acima justifica a lista precisamente por não depender
de uma `authority` do Keycloak por verificar, ao contrário do `ACH-A-01`. A permissão
IGRP depende dessa mesma classe de risco — chega ao chamador através do token, tal como
uma `authority`. O `AUT-05` existe precisamente para medir se essa dependência funciona:
arrancar o serviço e ler o log do `AuthorizationSyncRunner` a sincronizar as sete
permissões com a plataforma. Este plano (115-08) cobre só a parte declarativa — nomear as
propriedades que a sincronização exige, em `application.properties` e `.env.example`. A
medição fica para o plano 115-09, e o resultado esperado **neste ambiente**, sem
credenciais `IGRP_ACCESS_M2M_*`, é **limite nomeado, não satisfeito** — não se infere do
código que a sincronização funciona.

**O limite "a pertença é global" mantém-se, sem alteração.** Quem tiver
`siadap.avaliacoes.fecharEmLote` fecha as avaliações de qualquer unidade orgânica, tal
como antes um membro configurado da lista o fazia. Continua a ser o `AUT-07`, diferido
para v2 — ver `## Limite conhecido: a pertença é global` abaixo, que continua válida e não
é duplicada aqui.

**O fail-closed mantém-se.** Sem a permissão, a resposta continua `403`. Muda só onde a
mensagem é declarada: antes era lançada dentro do handler
(`IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, "...")`), agora é declarada por
ação em `@DenialMessage` no controller, ao lado do `@PreAuthorize` que a guarda.

**Onde a guarda vive agora, ponto a ponto:**

| Ação | Antes (revertido) | Agora |
|---|---|---|
| Atribuir menção de mérito | `AssignMeritRatingCommandHandler` comparava contra `SiadapCcaSecurityProperties` | `@PreAuthorize("@igrpAuthorization.checkPermission(T(Permission).SIADAP_MENCAOMERITO_ATRIBUIR)")` em `ComplianceController#assignMeritRating`, com `@DenialMessage` |
| Fechar avaliações em lote | `CloseEvaluationsCommandHandler` comparava contra `SiadapCcaSecurityProperties` | `@PreAuthorize("@igrpAuthorization.checkPermission(T(Permission).SIADAP_AVALIACOES_FECHAREMLOTE)")` em `ComplianceController#closeEvaluations`, com `@DenialMessage` |
| Consultar estado do CCA | `GetSiadapCcaStatusQueryHandler` comparava o `FuncionarioId` do chamador contra a lista | `GetSiadapCcaStatusQueryHandler` lê `IgrpAuthorizationService.checkPermission(SIADAP_CCA_CONSULTARESTADO)` diretamente, sem `@PreAuthorize` no controller — decisão registada do operador, 2026-08-27, `115-07-SUMMARY.md`; o endpoint continua a responder `200` a qualquer autenticado, nunca `403`, para preservar o contrato já consumido pelo frontend |

`SiadapCcaSecurityProperties` — a classe que lia `SIADAP_CCA_EMPLOYEE_IDS` — foi apagada
(`115-07-SUMMARY.md`). A propriedade `sigdi.siadap.cca-employee-ids` e a variável de
ambiente `SIADAP_CCA_EMPLOYEE_IDS` deixaram de existir em `application.properties` e
`.env.example` (`115-08-PLAN.md`, Task 1).

## Pré-condição declarada para a Fase 107

O critério de sucesso da Fase 107 é fechar um ciclo SIADAP completo pela interface, do
`OPEN` ao `CLOSED`. Esse percurso passa por validar quotas e por encerrar o ciclo —
as duas ações que esta configuração protege.

**Sem `SIADAP_CCA_EMPLOYEE_IDS` definida com o `FuncionarioId` de quem vai executar a
demonstração, a Fase 107 não é executável.** Está escrito aqui para ser encontrado antes
de a Fase 107 começar, não redescoberto a meio dela.

## Limite conhecido: a pertença é global

Um membro do CCA configurado pode validar quotas e encerrar ciclos de **qualquer** unidade
orgânica, não apenas da sua. Não há distinção por unidade organizacional nesta versão.

É uma consequência aceite da decisão de âmbito tomada nesta fase, não um limite
desconhecido. O ponto de partida para a evolução, caso venha a ser necessária, é alargar
a assinatura para `isCca(funcionarioId, organicUnitId)` — a interface pública de
`SiadapCcaSecurityProperties` foi desenhada para admitir essa evolução sem quebrar os
chamadores atuais.

## Os 14 comandos e a sua posição

O `ComplianceController` expõe 21 operações HTTP em 19 caminhos distintos — 14 comandos e
7 consultas, nenhuma das 21 com `@PreAuthorize`. A tabela seguinte cobre os 14 comandos;
as 7 consultas ficam fora do âmbito de `SIA-04` (ver secção seguinte).

| Handler | Disposição | Base da comparação | Endpoint |
|---|---|---|---|
| `AcceptObjectiveRevisionCommandHandler` | ENFORCED | `evaluation.employeeId` | `POST siadap/evaluations/{id}/interim-feedback/revisions/{revisionId}/accept` |
| `AcceptSiadapObjectivesCommandHandler` | ENFORCED | `evaluation.employeeId` | `POST siadap/evaluations/{id}/objectives/accept` |
| `AssignMeritRatingCommandHandler` | ENFORCED | permissão `siadap.mencaoMerito.atribuir`, guardada por `@PreAuthorize` em `ComplianceController#assignMeritRating` (revertido de lista de CCA em 2026-08-26, ver secção de reversão acima) | `POST siadap/evaluations/{id}/merit-rating` |
| `CloseEvaluationsCommandHandler` | ENFORCED | permissão `siadap.avaliacoes.fecharEmLote`, guardada por `@PreAuthorize` em `ComplianceController#closeEvaluations` (revertido de lista de CCA em 2026-08-26, ver secção de reversão acima) | `POST siadap/evaluations/close` |
| `ContractualizeObjectivesCommandHandler` | ENFORCED | `evaluation.evaluatorId` | `POST siadap/evaluations/{id}/objectives` |
| `CreateSiadapEvaluationCommandHandler` | **NOT-REQUIRED** | não há agregado prévio contra o qual comparar | `POST siadap/evaluations` |
| `EvaluateCompetenciesCommandHandler` | ENFORCED | `evaluation.evaluatorId` | `POST siadap/evaluations/{id}/competencies` |
| `FinalizeEvaluationCommandHandler` | ENFORCED | `evaluation.evaluatorId` | `POST siadap/evaluations/{id}/finalize` |
| `NegotiateObjectiveRevisionCommandHandler` | ENFORCED | `evaluation.employeeId` | `POST siadap/evaluations/{id}/interim-feedback/revisions/{revisionId}/negotiate` |
| `NegotiateSiadapObjectivesCommandHandler` | ENFORCED | `evaluation.employeeId` | `POST siadap/evaluations/{id}/objectives/negotiate` |
| `ProposeObjectiveRevisionCommandHandler` | ENFORCED | `evaluation.evaluatorId` | `POST siadap/evaluations/{id}/interim-feedback/revisions/{revisionId}/propose` |
| `RecordObjectiveAchievementCommandHandler` | ENFORCED | `evaluation.evaluatorId` | `POST siadap/evaluations/{id}/objectives/achievements` |
| `SaveSiadapInterimFeedbackCommandHandler` | ENFORCED | `evaluatorId` **ou** `employeeId` — formulário partilhado | `POST siadap/evaluations/{id}/interim-feedback` |
| `SubmitSelfEvaluationCommandHandler` | ENFORCED | `evaluation.employeeId` | `POST siadap/evaluations/{id}/self-evaluation` |

As razões acima coincidem com os marcadores `ACTOR-CHECK` escritos no próprio código de
cada handler — se um dia divergirem, é este documento que se corrige, não o código.

## O que fica coberto e o que continua descoberto depois desta fase

**Coberto:** os 14 comandos SIADAP alcançáveis pelo `ComplianceController` têm posição
declarada — 13 verificam ator, 1 declara por escrito porque não deve verificar. A
completude desta lista deixou de depender de revisão manual: `SiadapCommandActorCheckCoverageTest`
falha se algum dos 14 ficar sem marcador, ou se um marcador mentir sobre o que o código faz.

**Descoberto, e é preciso dizê-lo sem eufemismo:**

- O backend continua **sem camada RBAC** (`T-010`, fora de âmbito por decisão do operador).
- O `ComplianceController` tem **21 operações HTTP em 19 caminhos distintos — 14 comandos
  e 7 consultas**. Isto já não é "nenhuma com `@PreAuthorize`", como este documento dizia
  antes da Fase 115: três comandos passaram a ter `@PreAuthorize` (`assignMeritRating`,
  `closeEvaluations`, `openSelfEvaluationPhase`, ver a secção de reversão acima). As
  restantes **18** — 11 comandos que continuam a verificar o ator dentro do handler e as
  7 consultas — continuam sem `@PreAuthorize`. A consulta `ccaStatus` é o único ponto que
  lê uma permissão sem `@PreAuthorize` no controller, por decisão registada
  (`115-07-SUMMARY.md`).
- A pertença ao CCA é **global**: quem tiver `siadap.avaliacoes.fecharEmLote` (antes da
  reversão de 2026-08-26, quem estivesse na lista) valida quotas e encerra ciclos de
  **qualquer** unidade orgânica, incluindo aquelas a que não pertence.
- As **7 consultas** do mesmo controlador estão fora do âmbito de `SIA-04`, que fala em
  comandos — quem conhecer um `evaluationId` continua a poder **ler** a avaliação alheia.

Nenhuma destas quatro lacunas é regressão desta fase: três são herdadas do estado anterior
do código-base, e a quarta (pertença global do CCA) é consequência aceite da decisão de
âmbito já registada na secção anterior.

## Como isto se mantém verdadeiro

`SiadapCommandActorCheckCoverageTest` (JUnit 5 puro, sem Spring, sem base de dados) deriva
o conjunto de comandos do texto de `ComplianceController.java` — cada `new XxxCommand(`
encontrado — e verifica, para cada um, que o ficheiro `XxxCommandHandler.java` correspondente
existe e declara exatamente um marcador `ACTOR-CHECK`. Cruza ainda a disposição declarada
com o código, mas — **desde a Fase 115 (2026-08-26, plano 115-08)** — de duas formas
legítimas, não uma só:

- Um handler `ENFORCED` cuja razão **não** nomear um método do `ComplianceController` tem de
  chamar `currentEmployeeResolver.resolve()` fora de comentário. É a forma original, e
  continua a valer para os 11 comandos que se verificam a si próprios contra o agregado.
- Um handler `ENFORCED` cuja razão nomear um método do `ComplianceController` como guardado
  por `@PreAuthorize` (formato `"... guarded by @PreAuthorize on ComplianceController#metodo"`)
  tem essa afirmação verificada contra o ficheiro do controlador — o método tem mesmo de ter
  `@PreAuthorize(...checkPermission...)`, não basta o comentário dizê-lo. É a forma que
  `AssignMeritRatingCommandHandler`, `CloseEvaluationsCommandHandler` e
  `OpenSelfEvaluationPhaseCommandHandler` passaram a usar quando a verificação subiu para o
  controller (ver a secção de reversão acima, para os dois primeiros).

Um handler `ENFORCED` sem nenhuma das duas formas continua a falhar a suite — reconhecer a
segunda forma não é o mesmo que deixar de medir. Um handler `NOT-REQUIRED` continua a não
poder chamar `currentEmployeeResolver.resolve()`. Uma operação nova no `ComplianceController`
que despache um comando novo faz a suite falhar até que o handler correspondente declare a
sua posição — nomeando o ficheiro em falta, não apenas "algo falhou".

Formato do marcador, para quem tiver de escrever o próximo — uma linha, ASCII, imediatamente
acima da declaração da classe:
```java
// ACTOR-CHECK: ENFORCED -- evaluation.employeeId; only the avaliado may submit the self-evaluation
```
Ou, para a forma guardada no controller:
```java
// ACTOR-CHECK: ENFORCED -- siadap.mencaoMerito.atribuir permission, guarded by @PreAuthorize on ComplianceController#assignMeritRating
```

**Os dois pontos cegos do teste, ditos sem linguagem de garantia incondicional:** o teste
deriva a lista de comandos por `new XxxCommand(` no texto do controlador — um comando
construído por *builder* ou por *factory* em vez de `new` **não é visto**, e a asserção de
tamanho 15 (14 até à Fase 111, que acrescentou um comando) é precisamente a rede que apanha
essa primeira falha (um comando invisível à expressão regular faz a contagem descer abaixo
de 15). O segundo ponto cego: um handler alcançável por uma via que não este controlador
também não é visto. O que este teste garante é a **completude da declaração dentro da
superfície que deriva** — nunca mais do que isso.
