# Migrar os 5 jobs do `sigdi` para o framework de jobs

> Tarefa para um agente. Última alteração: 2026-09-25. Ponto de partida: commit `5d96e070`
> (`feat(shared): framework de jobs agendados`), branch `fix-alinhamento-legislacao`.

## Objectivo

Os cinco agendadores de `sigdi/infrastructure/scheduler/` ainda são `@Scheduled` simples. Têm de passar a
implementar `ScheduledJob` e ganhar o que os jobs do RH já têm: registo de cada execução, agendamento
alterável pela API, disparo manual, detecção de execuções em falta e o lock entre as duas réplicas do k8s.
**O comportamento de negócio de cada job não muda.** É uma migração de infraestrutura.

Hoje os cinco correm em **ambas** as réplicas (`k8s/deployment.yaml`: `replicas: 2`). É esse o defeito
principal que a migração resolve.

## Ler primeiro

1. `CLAUDE.md` — secção *Scheduled Jobs*, e as convenções (pt-PT, commits `refactor(sigdi): …`).
2. `src/main/java/cv/igrp/RH_Service/shared/application/services/scheduler/` — o framework inteiro:
   `ScheduledJob` (o contrato), `JobContext`, `JobResult`, `JobParametro`, `JobRunner`.
3. Os dois exemplos já migrados, que são o molde a seguir:
   `colaboradores/infrastructure/scheduler/LicencaEfeitoJob.java` e `VencimentoFeriasJob.java`, e o teste
   `src/test/.../colaboradores/infrastructure/scheduler/VencimentoFeriasJobTest.java`.
4. `docs/funcionarios/v5/api_guide.md` §11b — o que o utilizador vê.
5. Os cinco agendadores **e o Javadoc de cada um até ao fim**. Registam decisões provadas contra a base
   real (D-17 a D-23). Não as desfazer.

## Como se migra um job (o molde)

```java
@Component
public class XScheduler implements ScheduledJob {               // sem @Scheduled
    public static final String CHAVE = "SIGDI_…";
    private final String cronPadrao;

    public XScheduler(…, @Value("${sigdi.….cron:0 30 0 * * ?}") String cronPadrao) { … }

    @Override public String getChave()       { return CHAVE; }
    @Override public String getNomeLegivel() { return "…"; }            // pt-PT; é o que o utilizador vê
    @Override public String getCronPadrao()  { return cronPadrao; }

    @Override
    public JobResult executar(JobContext ctx) {
        LocalDate hoje = ctx.dataReferencia();                           // nunca LocalDate.now(...)
        … o corpo actual, a contar …
        return JobResult.builder().processados(…).criados(…).saltados(…).falhas(…)
                .detalhes(falhas.isEmpty() ? null : Map.of("itensFalhados", falhas))
                .mensagem("…").build();
    }
}
```

- O `@Component` chega: o `SchedulerService` descobre o job por `List<ScheduledJob>`, semeia a linha em
  `t_scheduler_job` no primeiro arranque e agenda-o. Sem migração Flyway: as tabelas já existem.
- **A propriedade do cron só semeia.** Depois do primeiro arranque manda a base de dados, e muda-se com
  `PUT /api/v1/rh/schedulers/{chave}`. Os valores `0 30 0 * * ?` com `?` funcionam (o framework trata `?`
  como `*`).
- Um item que falha **não** derruba o lote. O `catch` por item fica como está, mas a falha entra também em
  `itensFalhados`. Com `falhas > 0` a execução fecha como `FALHA_PARCIAL`, e esse estado não tem retry
  automático, o que é o comportamento certo aqui.
- Deixar `getMaxTentativas()` a 0 (o valor por omissão) nos cinco. Todos são idempotentes, e o do dia
  seguinte apanha o atraso.

## Os cinco jobs

| Classe | Chave proposta | Cron (propriedade · omissão) | Data | Cuidados |
|---|---|---|---|---|
| `PaaSubmissionPeriodExpiryScheduler` | `SIGDI_PAA_FECHO_PERIODOS` | `sigdi.paa.submission-period-expiry.cron` · `0 30 0 * * ?` | `ctx.dataReferencia()` em vez de `LocalDate.now(CABO_VERDE)`; declarar `JobParametro.dataReferencia()` | **Sem `@Transactional`** (ver abaixo). Manter `SystemAuditor.runAs(AUDITOR_NAME, …)`. Contadores: `closed`→criados, `failed`→falhas, `skipped`→saltados |
| `PaaSubmissionPeriodOpeningScheduler` | `SIGDI_PAA_GERACAO_FORMULARIOS` | `sigdi.paa.form-generation.cron` · `0 45 0 * * ?` | idem | **Sem `@Transactional`** (D-20). Manter `runAs(AUDITOR_NAME)`, os interruptores `enabled`/`dry-run` e `blocksRegeneration` (switch sem `default` — não mexer). Com `enabled=false`, devolver `JobResult` com a mensagem de «desligado», não `null`. `getTimeout()` → 60 min (é criação em massa) |
| `TacitAcceptanceScheduler` | `SIGDI_PAA_ACEITACAO_TACITA` | não tem; cron fixo `0 0 1 * * ?` → criar `sigdi.paa.tacit-acceptance.cron` com esse valor por omissão | idem | Tem `@Transactional` → **manter**, agora em `executar`. Não tem paginação nem contagem: acrescentar contadores |
| `SelfEvaluationOpeningScheduler` | `SIGDI_SIADAP_ABERTURA_AUTOAVALIACAO` | `sigdi.siadap.self-evaluation-opening.cron` · `0 0 1 * * ?` | **não** declarar parâmetro de data: a data é decidida dentro de `SelfEvaluationWindowPolicy` (hoje, CABO_VERDE) | Tem `@Transactional` → manter em `executar` |
| `SelfEvaluationTacitAcceptanceScheduler` | `SIGDI_SIADAP_ACEITACAO_TACITA_AUTOAVALIACAO` | `sigdi.siadap.self-evaluation-tacit-acceptance.cron` · `0 0 1 * * ?` | idem, sem parâmetro | Tem `@Transactional` → manter. `Boolean.TRUE.equals(windowOpen)` fica como está |

**Nomes das classes:** manter os actuais (`…Scheduler`). São citados em `docs/08-Janela-de-Autoavaliacao-SIADAP.md`,
nos testes e nos Javadocs dos vizinhos. Renomear traz churn sem ganho.

## Armadilhas — ler antes de escrever código

1. **`@Transactional` e o autor da auditoria.** Nos dois jobs PAA, a ausência de `@Transactional` é
   deliberada e está provada contra a base (Javadoc, D-20). O `AuditingEntityListener` só resolve o autor
   no `flush`; com uma transacção à volta de tudo, o `flush` acontece depois de o `runAs` terminar e o autor
   gravado seria o errado. Não pôr `@Transactional` em `executar` nesses dois. Nos outros três,
   `@Transactional` passa do método `@Scheduled` para `executar`. Funciona porque o `JobRunner` chama o job
   através do proxy do Spring (é o bean injectado em `List<ScheduledJob>`).
2. **Dois âmbitos de auditor, aninhados.** O framework já corre `executar` dentro de
   `SystemAuditor.runAs("scheduler:<chave>")`. Os jobs PAA abrem lá dentro o seu `runAs(AUDITOR_NAME)`. O
   `SystemAuditor` repõe o valor anterior, por isso o aninhamento é seguro, e o `AUDITOR_NAME` do job
   **ganha**, que é o que os testes verificam. Manter os `AUDITOR_NAME`.
3. **Os testes lêem `@Scheduled` por reflexão.** `…ExpirySchedulerTest:201`, `…OpeningSchedulerTest:299`,
   `SelfEvaluationOpeningSchedulerTest:262` e `SelfEvaluationTacitAcceptanceSchedulerTest:258` fazem
   `method.getAnnotation(Scheduled.class).cron()`. Deixam de valer: reescrevê-los para verificar que
   `getCronPadrao()` devolve o valor injectado, e que o construtor tem
   `@Value("${sigdi.….cron:<omissão>}")` (por reflexão no parâmetro, se quiser manter a garantia de que a
   propriedade continua sobreponível). As chamadas `scheduler.closeExpiredPeriods()` e afins passam a
   `scheduler.executar(JobContext.para(hoje))`, ou `JobContext.vazio()` nos SIADAP.
4. **Ordem 00:30 → 00:45 → 01:00.** Hoje os cinco correm em série, num único thread (o scheduler por
   omissão do Spring). No framework correm num pool de 4, por isso o que ordena é só a hora. Os de 00:30 e
   00:45 ficam separados pelo relógio, como está documentado nos Javadocs. **Os três de 01:00 passam a
   poder correr em paralelo.** Os dois SIADAP são mutuamente exclusivos por ano (Javadoc do
   `SelfEvaluationTacitAcceptanceScheduler`), e o `TacitAcceptanceScheduler` mexe noutro agregado
   (actividades PAA). Não mudar as horas por omissão, mas registar esta consequência no Javadoc de cada um.
   Se algum teste ou raciocínio mostrar que o paralelo é um problema, **parar e perguntar ao utilizador**:
   escalonar para 01:00/01:05/01:10 é uma decisão de negócio.
5. **Timeout não mata JDBC.** Ao exceder o tempo limite, o registo fecha como `TIMEOUT` e a guarda de
   concorrência liberta-se, mas o thread pode continuar até terminar. Por isso o Opening leva 60 min, e
   não os 30 por omissão.
6. **`dataReferencia()` e re-execução.** Repetir uma `OMITIDA` de dias atrás faz os jobs PAA correrem com
   essa data. Com as consultas actuais (`findOpenExpired`, `findOpenActiveOn`, `…EndDateBefore`) isso só
   trata menos, nunca mais, por isso é seguro. Confirmar esta leitura em cada consulta antes de declarar o
   parâmetro.
7. **Mensagens.** `getNomeLegivel()` e `mensagem` são lidos pelo utilizador final: pt-PT simples, sem
   enums nem ids (os ids ficam em `itensFalhados`, que é detalhe técnico). Os logs existentes, em inglês,
   podem ficar como estão.

## Documentação a actualizar

- `src/main/resources/application.properties` (bloco «Agendadores SIGDI/PAA») e `.env.example`: dizer
  que as variáveis `*_CRON` só semeiam o primeiro arranque. Acrescentar `sigdi.paa.tacit-acceptance.cron`.
- `docs/funcionarios/v5/api_guide.md` §11b: acrescentar as cinco chaves à tabela e **retirar** a frase
  «Os cinco jobs do `sigdi` continuam como estavam». Actualizar `Última alteração`.
- `CLAUDE.md`, secção *Scheduled Jobs*: retirar «The five `sigdi` jobs are still plain `@Scheduled`».
- `docs/08-Janela-de-Autoavaliacao-SIADAP.md`: as linhas 23–25 e 85–86 descrevem o cron/`@Scheduled` —
  alinhar.
- Os Javadocs dos cinco que falam em «sem lock distribuído», «corre em todas as instâncias» ou na ordem
  por thread único.

## Verificação (tudo tem de passar)

1. **JDK 26** e cópia isolada, porque o VS Code estraga o `target/`. Ver a memória
   `feedback_implementation_patterns`:
   ```bash
   S=<scratchpad>/rh; rm -rf "$S"; mkdir -p "$S"
   tar --exclude=./target --exclude=./.git --exclude=./prototipo_aistudio --exclude=./data -cf - . | tar -xf - -C "$S"
   export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-26.0.2.10-hotspot"
   cd "$S" && mvn -B -o test
   ```
   Referência: **1219 testes, 0 falhas** no commit `5d96e070`. O número sobe com os testes reescritos, e as
   falhas têm de continuar em 0.
2. Arrancar a aplicação numa porta à parte (`SERVICE_PORT=8199 SERVICE_PROFILE=development`, com o `.env`
   carregado) e confirmar:
   - no log, `SchedulerService: 7 job(s) agendados (de 7)`, e nenhum `@Scheduled` do sigdi a disparar;
   - `GET /api/v1/rh/schedulers` → 7 itens, os cinco novos com a descrição e a hora certas
     (`Diário às 00:30`, …) e o fuso `Atlantic/Cape_Verde`;
   - um disparo manual inofensivo, e depois `GET …/execucoes/{id}` → `SUCESSO` com os contadores. Usar o
     `SIGDI_PAA_GERACAO_FORMULARIOS` só com `PAA_FORM_GENERATION_DRY_RUN=true`, porque cria agregados na
     base de dev.
3. `PYTHONIOENCODING=utf-8 python scripts/verificar_docs.py` → `FALHAS: 0`. Não há endpoints novos, por
   isso o `openapi.json` não muda. Se mudar, regenerar com `curl -s -o docs/funcionarios/v5/openapi.json
   http://localhost:8199/v3/api-docs`.
4. `grep -rn "@Scheduled" src/main/java` → só o `SchedulerSweeper` (a manutenção do próprio framework).

## Fecho

- Um commit: `refactor(sigdi): agendadores passam ao framework de jobs`, com a linha
  `Co-Authored-By` do costume. **Não fazer push**: o push é do utilizador (a conta local não tem escrita
  no GitLab), e o merge para `master` é um deploy.
- Não mexer em `.claude/settings*.json` nem em `prototipo_aistudio/` (ignorado).
- Reportar ao utilizador o número de testes, o resultado do `verificar_docs` e qualquer ponto da
  armadilha 4 que tenha ficado em aberto.
