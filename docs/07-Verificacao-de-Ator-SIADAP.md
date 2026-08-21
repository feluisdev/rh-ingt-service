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
