# Verificação de Ator nos Comandos SIADAP

**Versão:** 1.0 | **Status:** Em atualização (Fase 101) | **Data:** 21/08/2026

---

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

## O que acontece se ninguém a configurar

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

## Como configurar

Definir `SIADAP_CCA_EMPLOYEE_IDS` com uma lista de `FuncionarioId` (UUID) separados por
vírgula. Espaços à volta de cada UUID e vírgulas a mais são tolerados e descartados na
normalização — por exemplo, `" id1 , , id2 , "` normaliza para `{id1, id2}`.

Os `FuncionarioId` a usar são a coluna `external_id` da tabela de funcionários,
consultável através da listagem de colaboradores da aplicação.

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
| `AssignMeritRatingCommandHandler` | ENFORCED | lista de CCA (`SiadapCcaSecurityProperties`) | `POST siadap/evaluations/{id}/merit-rating` |
| `CloseEvaluationsCommandHandler` | ENFORCED | lista de CCA (`SiadapCcaSecurityProperties`) | `POST siadap/evaluations/close` |
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
- O `ComplianceController` continua com **21 operações HTTP em 19 caminhos distintos —
  14 comandos e 7 consultas, nenhuma com `@PreAuthorize`**.
- A pertença ao CCA é **global**: um membro configurado valida quotas e encerra ciclos de
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
com o código: um handler `ENFORCED` tem de chamar `currentEmployeeResolver.resolve()` fora
de comentário; um handler `NOT-REQUIRED` não pode. Uma operação nova no `ComplianceController`
que despache um comando novo faz a suite falhar até que o handler correspondente declare a
sua posição — nomeando o ficheiro em falta, não apenas "algo falhou".

Formato do marcador, para quem tiver de escrever o próximo — uma linha, ASCII, imediatamente
acima da declaração da classe:
```java
// ACTOR-CHECK: ENFORCED -- evaluation.employeeId; only the avaliado may submit the self-evaluation
```

**Os dois pontos cegos do teste, ditos sem linguagem de garantia incondicional:** o teste
deriva a lista de comandos por `new XxxCommand(` no texto do controlador — um comando
construído por *builder* ou por *factory* em vez de `new` **não é visto**, e a asserção de
tamanho 14 é precisamente a rede que apanha essa primeira falha (um comando invisível à
expressão regular faz a contagem descer abaixo de 14). O segundo ponto cego: um handler
alcançável por uma via que não este controlador também não é visto. O que este teste
garante é a **completude da declaração dentro da superfície que deriva** — nunca mais do
que isso.
