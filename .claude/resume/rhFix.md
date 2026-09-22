> Updated: 2026-09-22

## Goal

Alinhar o núcleo RH (não-sigdi) com a legislação cabo-verdiana, movimento a
movimento, com regras documentadas, testadas **e exercitadas contra a base de
dados**. O objectivo de fundo é a aplicação ser instalável em qualquer
instituição: o que a lei fixa vive em código; o que varia com a instituição ou
com o motivo vive em catálogo.

Construção **tijolo a tijolo**: um ponto por commit, com testes e documentação a
par, sem avançar enquanto o anterior não estiver verde.

## Current state

**Branch `fix-alinhamento-legislacao`**, com **8 commits locais por enviar**
(`71103f99..HEAD`, todos de 2026-09-22 — ver a tabela abaixo). O GitLab é o repo
da equipa; merge para `master` é deploy. **O push não foi feito por indicação
expressa do utilizador: não o fazer sem lhe perguntar.**

- **Testes: 840, 0 falhas — mas só com a base de dados de pé.** 839 são unitários
  puros; o `RecursosHumanosApplicationTests.contextLoads` carrega o contexto Spring
  completo e o Flyway liga-se ao Postgres. **Sem o contentor a correr dá 1 erro, e
  não é regressão.** Correr **sempre com `clean`** (ver Blockers).
- **Bateria funcional: 305 passos, 305 OK**, cobre **F0 a F14**.
- **Migrações V40 a V51** aplicadas e verificadas na BD. Próxima livre: **V52**.
- **`openapi.json`**: 229 caminhos, 246 esquemas, **0 operações não-sigdi sem
  esquema de resposta**. Regenerado a 2026-09-22; **regenerar sempre** que se
  mexa num endpoint.
- **Sete jobs `@Scheduled`**: cinco do `sigdi`, mais o dos efeitos das licenças
  (`rh.licencas.efeitos.cron`, 00:15) e o do vencimento de férias
  (`rh.ferias.vencimento.cron`, 00:05). Nenhum tem lock distribuído — ver
  questão 13.
- **Nenhum handler ou controlador não-sigdi devolve `Map`.**

### O que a sessão de 2026-09-22 fez (7 commits, `71103f99..HEAD`)

| Commit | O que fecha |
|---|---|
| `204f29af` | **V48 — a decisão separa-se do período** na licença: `status` guarda só o despacho, o período deriva das datas, os efeitos passam a um job diário |
| `f0a135b2` | Handoff a par da V48; a questão aberta 3 fecha-se |
| `9d0e353b` | **V49 — o direito a férias vence-se sozinho** (art. 2.º n.º 4), proporcional no ano de ingresso (art. 3.º); coluna `regime` classifica o catálogo |
| `159fc1e4` | **V50 — acumulação** para o ano seguinte (art. 7.º n.º 1), acto com motivo obrigatório |
| `786e5bf9` | **V51 — suspensão de férias** pelas causas do art. 8.º; os dias voltam ao saldo |
| `4912ac75` | **Ler as substituições**, nos dois papéis — fecha a dívida D2 |
| `69a2b013` | Auditoria à documentação: F11–F13 nunca tinham entrado no README da bateria |
| `616c0f0e` | **Antiguidade** — derivada, não guardada; dá consumidor a três colunas mortas |

### O que a sessão anterior fez (19 commits, `ee61e15e..71103f99`)

| Commit | O que fecha |
|---|---|
| `d9808ec7` | Suite verde: as "4 falhas conhecidas" eram testes desalinhados do código, não bugs |
| `d0417100` | **V45 — uma cadeira, um titular**: índice do Lugar parcial em `PRINCIPAL`; enum `TipoAfectacao` |
| `57c908c3` | Docs v5 alinhados; **`openapi.json` passa a ser gerado** do código |
| `d4ded55b` | **V46 — substituição** de funcionário temporariamente impedido |
| `f47bc974` | Regras da mobilidade que descreviam código já apagado |
| `bd0a7f26` | **V47 — larga `origin_assignment_id`**, que ficou sem dono |
| `9ecfc207` · `cadfb788` · `d034c31e` | **Fim das respostas em `Map`** (85 operações + 7 DTOs próprios) |
| `82c0f998` | Bateria F6 + seed (78 → 111 passos) |
| `e8fd22f1` | Bateria F7 cessação + F8 mobilidade (→ 172) |
| `6fe7b778` | Bateria F9 promoção + F10 respostas (→ 207) |
| `ebaa4831` | Âmbito da aplicação e porque ausências ≠ assiduidade |

### O plano de alinhamento — onde estamos

**Feito:** situações funcionais art. 117.º (V42) · licenças que abrem vaga (V43)
· ciclo do saldo de ausências · contrato com estado obrigatório (V44) ·
**substituição** (V45+V46) · **respostas tipadas** · limpeza da V47 · **bateria
completa F0–F10** · **decisão separada do período na licença (V48)**.

**V48 — o que fechou (2026-09-22).** O `status` guardava ao mesmo tempo o despacho
(art. 44.º n.º 2) e o período (n.º 1). Agora guarda só a decisão — `PENDING`,
`APPROVED`, `REJECTED`, `CANCELLED` — e o período (`POR_INICIAR`, `EM_CURSO`,
`TERMINADA`) deriva das datas, exposto em `estadoPeriodo`. `ACTIVE` e `CLOSED`
desapareceram. Deferir deixou de pôr em vigor: os efeitos aplicam-se na data de
início, na própria transacção se for hoje e pelo `LicencaEfeitoScheduler`
(`rh.licencas.efeitos.cron`, 00:15) nos restantes casos, com duas marcas de
aplicação a garantir idempotência. O `close` passou a ser o regresso antecipado
do art. 46.º n.º 4 e recusa o que não começou (409, remete para o cancelamento)
ou o que já terminou; a ausência acaba na **véspera** do dia do regresso.
**Isto resolveu a questão aberta 3** (licença que acaba antes de começar): deixou
de ser possível, por construção e por restrição no esquema.

**Por fazer — quatro pontos, por ordem recomendada:**

1. **Férias (DL 3/2010, cap. II)** — **o vencimento está feito (V49, 2026-09-22).**
   O saldo deixou de ser escrito à mão: nasce na admissão, vence-se a 1 de Janeiro
   (art. 2.º n.º 4) e é proporcional no ano de ingresso (art. 3.º), com o número de
   dias vindo do catálogo e a lei por recurso. Quem classifica o catálogo é a coluna
   `regime` (`FERIAS`/`FALTA`), não o código.
   **A acumulação também está feita (V50, 2026-09-22).** Os dias que, por motivo
   de serviço, não puderam ser gozados passam para o ano seguinte por
   `POST /saldos-ausencia/{saldoId}/acumular` — **acto do RH com motivo
   obrigatório**, não automatismo: a lei condiciona-a a haver motivo de serviço.
   O que é cedido sai do saldo de origem (`dias_transportados`), o que é recebido
   fica à parte do direito do próprio ano (`dias_acumulados` + motivo), e os dias
   recebidos **não voltam a ser acumuláveis** — o horizonte da lei é de um ano
   (art. 7.º n.º 1 e art. 8.º n.º 4).

   **A suspensão também está feita (V51, 2026-09-22).** Umas férias em curso
   interrompem-se por `PATCH /pedidos-ausencia/{id}/suspender` pelas causas do
   art. 8.º (parentalidade, doença, assistência a familiares, razões imperiosas
   de serviço), com motivo obrigatório. A suspensão produz efeito **a partir** da
   data indicada (n.º 3), logo o último dia de férias é a **véspera**; o pedido
   continua `APROVADO` — o que encurta é o período — e os dias recuperados voltam
   ao saldo do próprio ano, recontados em dias úteis. Passá-los ao ano seguinte
   é a acumulação da V50, que é o que o art. 9.º n.º 1 conjugado com o art. 8.º
   n.º 4 autoriza; não se inventou um segundo transporte.

   **Falta ainda:**
   - **marcação** — o mapa de férias até 31 de Março (art. 6.º), a indicação de
     preferência até 31 de Janeiro (art. 5.º n.º 4), o mínimo de 11 dias num dos
     períodos em gozo interpolado (art. 5.º n.º 1) e a fixação pelo dirigente entre
     Maio e Outubro na falta de acordo (n.º 5);
   - **suspensão das férias** por maternidade, paternidade, adopção ou doença
     (art. 8.º n.os 1 a 3) — hoje um pedido de férias e um de doença não se falam;
   - **meios-dias** (art. 2.º n.º 6: até 5 meios-dias). O `numeroDias` é inteiro, o
     mesmo limite que já impede exprimir o art. 13.º n.º 4;
   - **compensação na cessação** (art. 12.º) — depende de remuneração, que não
     existe nesta aplicação.
2. **Antiguidade / tempo de serviço** — **FEITA (2026-09-22)**, sem migração:
   é derivada, não guardada. Ver secção própria abaixo.
   **Sobra uma coluna morta:** `affects_pay` (subtipo) continua sem consumidor —
   é remuneração, e remuneração não existe nesta aplicação.
3. **Movimentos menores** — sete, sem caminho nenhum: consolidação da mobilidade
   (art. 132.º n.º 4) · acumulação (art. 134.º n.º 2 al. b) · permuta (atómica) ·
   **mudança de carreira** (art. 139.º / art. 35.º PCFR) · regresso de comissão
   (art. 64.º n.º 2) · estágio probatório (art. 57.º, 72.º) · reintegração
   judicial (art. 97.º n.º 10 al. b). **A mudança de carreira é a mais grave:**
   hoje é impossível, porque a promoção exige a mesma carreira e a transferência a
   mesma categoria.
4. **Percurso do colaborador** — linha temporal única (afectações, mobilidades,
   licenças, estados). Adiado até o negócio estar definido; resolveria também a
   lacuna de não haver como ler as substituições.
5. **Framework de jobs — portar do `inss_core_service`.** **Fica para o fim**, por
   decisão do utilizador (2026-09-22). Ver secção própria abaixo.

### Antiguidade — o que ficou feito (2026-09-22)

**Não há migração.** A antiguidade **deriva-se do percurso** e é recalculada a
cada leitura. Guardá-la obrigaria a recalcular sempre que uma data do passado
fosse corrigida, e alguém acabaria por confiar num número velho. É a mesma regra
que já vale para «provido/vago» e para o vínculo laboral.

**Endpoint:** `GET /funcionarios/{id}/antiguidade?ate=YYYY-MM-DD` (o `ate` é
opcional; por omissão, hoje). Serve para responder a «quanta antiguidade tinha à
data da promoção», que é pergunta corrente do RH.

**Duas peças:**

| Ficheiro | O que faz |
|---|---|
| `colaboradores/domain/service/CalculadoraAntiguidade.java` | a conta pura: recorta, **une** e subtrai. Sem dependências |
| `colaboradores/application/services/AntiguidadeService.java` | recolhe as três fontes da lei e chama o calculador |

**As três fontes, e as colunas que passaram a ser lidas:**

| Fonte | Regra | Coluna |
|---|---|---|
| Situação funcional | art. 120.º n.º 2 (inactividade não conta) e art. 122.º n.º 1 (disponibilidade conta) | `t_worker_state.situacao_funcional` → `SituacaoFuncional.contaAntiguidade()` |
| Licenças deferidas | art. 47.º n.º 1 do DL 3/2010 | `t_leave_mobility_subtype.counts_for_seniority` |
| Contratos | o vínculo vem do contrato, logo o desconto é do **período do contrato** | `t_vinculo_laboral.counts_seniority` |

**A decisão que faz a conta estar certa: os períodos excluídos UNEM-SE, não se
somam.** Uma licença sem vencimento de longa duração chega por dois caminhos — a
situação funcional em que põe o funcionário e o subtipo da própria licença — e
somá-los descontaria 730 dias de um ano que tem 365. Há um teste dedicado em cada
nível (`aMesmaAusenciaPorDoisCaminhosDescontaUmaVez` e
`aMesmaAusenciaPelosDoisCaminhosDescontaUmaVez`). Períodos **contíguos** também se
fundem: entre o fim de um e o início do outro não houve um dia de serviço.

**O que NÃO desconta, e é deliberado** — não mexer sem ler isto primeiro:

- **Mobilidade**: art. 137.º da Lei 20/X/2023, o tempo conta no lugar de origem.
  Não é configurável — mesmo que o subtipo esteja mal classificado, não desconta.
- **Estado sem situação classificada**: é configuração em falta, não um estado que
  não conta. Descontar por omissão tiraria tempo a quem o tem.
- **Licença por decidir, indeferida ou cancelada**: não houve ausência.
- **Férias não gozadas**: contam (art. 12.º n.º 3).
- **Greve**: perde remuneração mas não desconta antiguidade (art. 16.º n.º 4).
  Como não há tipo classificado para ela, não fazer nada é a resposta certa.

**Recorte ao serviço:** uma licença anterior à admissão só desconta a parte de
dentro; um período **em aberto** conta até à data de referência e não
indefinidamente — sem isso, uma licença sem fim descontaria tempo que ainda não
passou.

**Leitura em anos/meses/dias:** os dias descontados tiram-se do **fim** do
intervalo, como se o percurso fosse contínuo. É assim que se lê «tem 8 anos de
serviço».

**Lacuna conhecida, assinalada e não adivinhada:** o art. 43.º n.º 2 diz que as
**faltas injustificadas** não contam para antiguidade, mas `t_leave_type` não tem
coluna que o diga — só o subtipo de licença tem classificação de antiguidade.
Inferi-lo do código `FALTA_INJUSTIFICADA` seria exactamente o que este projecto
decidiu não fazer. **Para fechar:** uma migração que acrescente
`t_leave_type.counts_seniority` (ou reaproveite o `regime` da V49 com um terceiro
valor), e uma quarta fonte no `AntiguidadeService`.

**Quem passa a poder usar isto:** a progressão e a promoção verificam
elegibilidade fora do RH (DL 4/2024 art. 36.º remete para o diploma da gestão de
desempenho), mas a antiguidade é o dado que faltava para quem a quiser exigir.

### Duas dívidas pequenas, já diagnosticadas (entram no plano a 2026-09-22)

Não são pontos do alinhamento — são defeitos conhecidos, com causa localizada.
Ficam aqui para não voltarem a viver só em «Blockers» e serem esquecidos.

**D1. Feriados municipais ignorados na contagem de dias úteis.**
`CreatePedidoAusenciaCommandHandler:50` chama `findAllNacionaisActivosByAno`, e o
`DiasUteisCalculator` desconta só os **nacionais**. Um pedido que atravesse um
feriado municipal conta **um dia a mais**, apesar de `t_public_holiday.is_national`
existir precisamente para os distinguir.

*Porque ainda não está feito:* corrigir a chamada é trivial; a pergunta é **de
que município** se contam os feriados. A informação não está na afectação nem no
funcionário — é preciso decidir onde vive (unidade orgânica? colaborador?) antes
de escrever código. **Decide o RH** (questão aberta 4).

*Porque importa agora:* a contagem de dias úteis é a base das férias. Convém
fechar isto **durante** o ponto 1, não depois — senão as férias nascem com a
mesma contagem errada.

**D2. ~~Não há como ler as substituições~~ — FEITA (2026-09-22).**
`GET /funcionarios/{id}/substituicoes` devolve os dois papéis na mesma consulta
(`SUBSTITUTO` e `TITULAR`), com a contraparte, o Lugar e o período, e
`apenasCorrentes` separa o que está em vigor do histórico. Escolheu-se o endpoint
próprio em vez de esperar pelo percurso do colaborador — não o impede, e a
lacuna estava a custar caro: o fecho automático da substituição provava-se por
via indirecta na bateria. Agora prova-se directamente (F6.30b e F6.30d).

O texto abaixo fica como registo do que era o problema.

**D2 (histórico). Não havia como ler as substituições.**
`POST /funcionarios/{id}/substituicao` devolve o id e mais nada o lista depois.
`GET .../unidade-atual` só devolve a `PRINCIPAL`. Um ecrã de RH **não consegue
mostrar quem substitui quem**, e na bateria o fecho automático teve de ser provado
por via indirecta (F6.30-33): põe-se o titular impedido outra vez e tenta-se nova
substituição — se a anterior não tivesse fechado daria 409, e dá 201.

*Decisão que falta:* endpoint próprio (`GET /funcionarios/{id}/substituicoes` e/ou
`GET /positions/{id}/substituicoes`) ou esperar pelo **percurso do colaborador**
(ponto 4), que resolveria os dois problemas de uma vez. **Decide o RH/produto**
(questão aberta 2).

*Nota:* é barato e independente do resto — um handler de consulta sobre
`findSubstituicoesCorrentes`, que já existe no repositório. Se o percurso do
colaborador ficar para longe, não vale a pena esperar por ele.

### Framework de jobs — a portar do `inss_core_service` (por último)

O RH tem hoje seis `@Scheduled` crus (cinco no `sigdi`, mais o das licenças que
entra agora). Cru quer dizer: hora no código, ninguém sabe se correu, nada se
repete sem SQL à mão, e uma execução que **não aconteceu** é invisível.

Está resolvido noutro projecto da equipa e deve ser trazido em vez de reinventado:

- **Repositório:** `C:\Users\ivanick.santos\Nosi-work\projects_nosi_workspace\projects\inss_core_service`
- **Branch:** `dev-pre-release` (é onde o repositório já está; não há diferença
  para o HEAD nos ficheiros do scheduler)
- **Documentação:** `docs/schedulers/README.md` (16 secções), `scheduler.html`
  (diagramas), `FRONTEND.md` (contrato para o front-end)
- **Código:** `src/main/java/gw/inss/core/shared/application/services/scheduler/`
  — `ScheduledJob` (interface de 4 métodos), `JobContext`, `JobResult`,
  `JobRunner`, `SchedulerService`, `ExecucaoRegistoService`, `SchedulerSweeper`,
  `EstadoExecucao`, `TipoDisparo`, `JobParametro`
- **Entidades:** `shared/infrastructure/persistence/entity/scheduler/` —
  `SchedulerJobEntity`, `SchedulerExecucaoEntity`
- **Migrações lá:** V40 (execuções), V47 (jobs), V48 (parâmetros). **Atenção:** os
  números colidem com os nossos; renumerar ao trazer.
- **REST:** `shared/interfaces/rest/SchedulerController.java` (`api/v1/schedulers`)
- **Pools:** `shared/config/SchedulingConfig.java` — `taskScheduler` (triggers)
  separado do `jobExecutor` (trabalho). É essa separação que torna o timeout
  possível.

**O que se ganha** (por implementar uma interface de quatro métodos): cron
configurável em BD sem recompilar · histórico de execuções com duração,
contadores e stacktrace · disparo manual com formulário gerado a partir de
`getParametros()` · re-execução de uma corrida antiga com os parâmetros originais
· detecção de execuções que não aconteceram (estado `OMITIDA`, linha sintética
criada pelo sweeper) · guarda de concorrência · timeout · retry com backoff
5→15→45 min.

**As três ideias que valem a pena mesmo que se porte só uma parte:**

1. **O período vem de `agendadoPara`, nunca de `now()`.** Repetir em Setembro a
   execução de Agosto tem de processar Agosto. É a mesma armadilha que já nos
   mordeu na bateria ("datas ancoradas em `Get-Date`").
2. **Gravar os parâmetros na abertura do registo, não no fim.** Uma execução que
   rebenta com excepção fica sem se saber a que período dizia respeito — e é
   essa a linha que alguém vai querer repetir.
3. **Abrir e fechar o registo em `REQUIRES_NEW`, num bean separado.** Se o job
   rebentou *porque* a BD caiu, gravar o desfecho não pode ir de boleia na
   transacção moribunda.

**Retry assimétrico**, que é o que o torna correcto: `FALHA` e `TIMEOUT` repetem;
`FALHA_PARCIAL` **não** (voltaria a falhar exactamente nos mesmos itens — é
trabalho humano); `OMITIDA` também não (dispararia rajadas).

**Limitação que vem com ele, e que é a mesma que já temos:** não há lock
distribuído. Com réplicas, o job corre em todas. Lá a nota diz que o passo
seguinte seria ShedLock dentro do `JobRunner`. Cá, as marcas de aplicação
(`efeito_entrada_aplicado_em`, `efeito_regresso_aplicado_em`, V48) limitam o
estrago mas não substituem um lock. Entra no mesmo saco de produção que o
`HIBERNATE_DDL`.

**Ordem:** depois das férias, da antiguidade e dos movimentos menores. O job das
licenças nasce agora no molde cru do `sigdi` e é o primeiro candidato a migrar
quando o framework entrar — a lógica de negócio não muda, só passa de
`@Scheduled` para `executar(JobContext)`.

### Âmbito — o que a aplicação faz sobre o funcionário

**17 sub-recursos** sob `/funcionarios/{id}`:

| Área | Acções |
|---|---|
| **Movimentos** | afectação · progressão · promoção · transferência · substituição · mudança de estado · cessação |
| **Ausências** | pedidos (criar, aprovar, rejeitar, cancelar) e saldos |
| **Licenças/mobilidade** | criar → approve/reject → prorrogar → close/cancel |
| **Dossier** | contratos · qualificações · formações · dependentes · dados bancários · documentos · recibos · processos disciplinares |
| **Self-service** | `/me`: perfil, pedidos, saldos, mobilidades, recibos, documentos |
| **Auditoria** | histórico por entidade (Envers) |

### O que não existe de todo — âmbito por decidir, não dívida

| Área | Nota |
|---|---|
| **Assiduidade efectiva** | ponto, horário, atrasos, horas em débito, trabalho suplementar. Ver secção própria em Blockers |
| **Antiguidade** | ponto 2 acima |
| **Remuneração** | há recibos como *documento*, não há cálculo. O DL 25/2025 (Tabela Única) não está ligado ao escalão |
| **Efeito disciplinar no vínculo** | há o registo do processo; a pena de inactividade (art. 121.º) não tem caminho |
| **Aposentação** | existe como estado final, não como processo |
| **Concursos e recrutamento** | a promoção por concurso interno (art. 32.º n.º 3) regista-se sem o concurso existir |
| **Avaliação de desempenho** | é o SIGDI — fora do âmbito por decisão |

## Decisions made — do not re-litigate

- **Ausências e licenças são dois sítios porque a lei os separa.** O DL 3/2010
  trata férias (cap. II), faltas (cap. III) e licenças (cap. IV) em capítulos
  distintos, e o art. 44.º define licença como «ausência **prolongada**, mediante
  autorização». O eixo é **curto contra prolongado**. Documentado em
  `modelo_negocio.html` §8.1 e `api_guide.md` §6.1.
- **A mobilidade não é uma ausência**: vem da Lei 20/X/2023 e a pessoa trabalha,
  noutro sítio. Partilha a tabela das licenças por conveniência; o `record_type`
  separa-as. **Não toca na afectação** (art. 135.º n.º 7).
- **Mobilidade definitiva = transferência.**
- **Promoção**: a modalidade infere-se do pedido (com `positionId` muda de Lugar;
  sem ele o Lugar sobe de categoria) e **não se persiste**.
- **Uma cadeira, um titular**: índice único do Lugar parcial em `PRINCIPAL` (V45).
  Substituição e acumulação **não disputam** a titularidade.
- **Provido/vago deriva do titular**, não de "afectação corrente".
- **Substituição**: quem pode ser substituído lê-se da **situação funcional**
  (`ACTIVIDADE_FORA_QUADRO` ou `INACTIVIDADE_NO_QUADRO`), nunca de códigos. Não
  tem data de fim: caduca quando o titular regressa (art. 77.º n.º 2). Não encerra
  a afectação de quem substitui (art. 91.º n.º 1 al. a)).
- **Férias e faltas curtas ficam fora da substituição.** Quem está de férias
  continua em `ACTIVIDADE_NO_QUADRO` (art. 118.º). Se a instituição quiser cobrir
  férias longas, cria um estado classificado como `INACTIVIDADE_NO_QUADRO` — **sem
  mexer em código**.
- **`INACTIVE` tem `ends_employment=true` e situação nula, de propósito**: modela
  as cessações que não são aposentação (exoneração, caducidade, mútuo acordo). A
  lei não o conta entre as seis situações do art. 117.º porque quem cessa deixa de
  ter situação perante o quadro. **Não é buraco do seed.** O nome é que engana:
  `SUSPENDED` e `INACTIVE_OUTSIDE` são inactividade e **não** cessam nada.
- **Nada de respostas `Map`**: `SuccessResponseDTO` (id, sucesso, alertas) nas
  operações simples; DTO próprio quando há mais a dizer. O `message` desapareceu.
- **Motivos não se validam** (`motivoCkey`, `terminationReason`, `ccode` do
  `Option`): são parametrizados pela instituição.
- **O que é derivável não se guarda** (provido/vago, vínculo laboral, "onde exerce
  funções", efeitos da situação funcional).
- **Parametrizar valores, não decisões.**
- **Valores fechados viram enum**: `EstadoContrato`, `EstadoPedidoAusencia`,
  `SituacaoFuncional`, `TipoRegisto`, `EfeitoNoLugar`, `EfeitoNoRegresso`,
  `TipoAfectacao`. **Falta**: origem da afectação, estado do Lugar, estados de
  `LicencaMobilidade`.
- **O SIGDI fica de fora.** O RH regista a progressão e a promoção; não verifica
  elegibilidade (DL 4/2024 art. 36.º remete para o diploma da gestão de desempenho).
- **Licença parental é ausência** (art. 171.º n.º 2 vs 172.º): `MATERNIDADE` 90
  dias, `PATERNIDADE` 10 dias úteis.
- **Nada de filtrar em memória.**

### Decisões tomadas a 2026-09-22 — também não se voltam a discutir

**Sobre a licença (V48)**

- **A decisão e o período são dois eixos.** O art. 44.º trata-os em números
  seguidos: o n.º 2 é o despacho (um acto), o n.º 1 é a ausência (um período).
  O `status` guarda só a decisão — `PENDING`, `APPROVED`, `REJECTED`,
  `CANCELLED`; o período deriva das datas. **`ACTIVE` e `CLOSED` não voltam.**
- **Deferir não é pôr em vigor.** Os efeitos no Lugar pertencem ao período:
  aplicam-se na data de início, na própria transacção se for hoje, pelo job se
  for mais tarde.
- **«A partir de» inclui o próprio dia.** Vale no regresso antecipado (art. 46.º
  n.º 4) e na suspensão de férias (art. 8.º n.º 3): o último dia de ausência é a
  **véspera**. Quem parte e regressa no mesmo dia fica com um dia.
- **Os efeitos marcam-se, para o job poder repetir.** `efeito_entrada_aplicado_em`
  e `efeito_regresso_aplicado_em`: a pergunta é sobre o estado actual, não sobre
  um intervalo desde a última execução. Um dia falhado não perde nada.

**Sobre as férias (V49, V50, V51)**

- **O saldo de férias nasce sozinho** (art. 2.º n.º 4). Não se cria à mão: nasce
  na admissão e é mantido por um job diário. `POST /saldos-ausencia` fica para os
  outros tipos.
- **Os dias vêm do catálogo, com a lei por recurso.** `max_days_per_year` do tipo
  classificado como férias; na falta dele, os 22 do art. 2.º n.º 3. A instituição
  pode ter outro número por diploma próprio — não pode decidir que as férias não
  se vencem.
- **Os «6 ou 5 dias» do art. 3.º não se escrevem no código.** São 22 repartidos
  por quatro trimestres (5,5 de cada vez); arredondando o acumulado saem 6, 11,
  17 e 22. Continua certo com um direito anual diferente de 22.
- **Qual linha do catálogo são férias lê-se do `regime`**, nunca do código. O
  catálogo é da instituição; o regime é da lei (`FERIAS`/`FALTA`). Mesmo padrão da
  situação funcional e do efeito da licença no Lugar.
- **A acumulação não é automática.** O art. 7.º n.º 1 só a permite «por motivo de
  serviço»: é um acto do RH com **motivo obrigatório**, não um job a arrastar
  dias. O conteúdo do motivo não se valida.
- **Os dias acumulados não se voltam a acumular.** O horizonte da lei é de um ano
  (art. 7.º n.º 1 e art. 8.º n.º 4). O gozo imputa-se primeiro ao direito do
  próprio ano.
- **A suspensão não inventou transporte novo.** Os dias recuperados voltam ao
  saldo do próprio ano; passá-los ao seguinte é a acumulação — que é o que o
  art. 9.º n.º 1, remetendo para o art. 8.º n.º 4, autoriza.
- **Meios-dias ficam de fora por decisão do utilizador** («por agora não teremos
  pedido de meio dia»). Não é assiduidade — é uma mudança de contrato.

**Sobre a antiguidade**

- **Não se guarda: deriva-se.** Recalculada a cada leitura, como provido/vago e
  como o vínculo laboral.
- **Os períodos excluídos unem-se, não se somam.** A mesma ausência chega pela
  situação funcional e pelo subtipo da licença; somá-las descontaria o dobro.
- **Não se desconta o que a lei manda contar**, nem se adivinha configuração em
  falta: mobilidade (art. 137.º), estado sem situação classificada, licença por
  decidir, férias não gozadas (art. 12.º n.º 3) e greve (art. 16.º n.º 4).

**Sobre as substituições**

- **Lêem-se nos dois papéis na mesma consulta.** A pergunta de um ecrã é «em que
  substituições está esta pessoa metida», e isso inclui os dois lados.
- **Endpoint próprio, sem esperar pelo percurso do colaborador.** Não o impede.

## Constraints

- **Build exige JDK 26**: `export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-26.0.2.10-hotspot"`.
- **Migrações Flyway sempre defensivas**: `to_regclass` + `information_schema.columns`.
  V40 a V47 são o modelo. **Confirmar o número livre** antes de criar.
- Controladores em `interfaces/rest/` são gerados pelo IGRP: lógica nos handlers,
  e **manifesto `.igrpstudio` actualizado a par do Java** (acção + DTOs + entity).
- **Um `@ApiResponse` com `@Content` sem `schema` apaga o tipo inferido** — o
  contrato sai vazio mesmo com o Java a devolver DTO. Declarar
  `schema = @Schema(implementation = XptoDTO.class)` ou `array = @ArraySchema(...)`.
- **Nomes totalmente qualificados no meio do código não passam**: usar imports.
- JPQL usa o **nome `@Entity`** (`ColabsAssignmentEntity`), não o da classe. Uma
  `@Query` errada passa nos testes unitários e só rebenta no arranque.
- Ano em JPQL: `year(campo)`, nunca `FUNCTION('YEAR', campo)`.
- **`scripts/*.ps1` só com ASCII**: o PS 5.1 lê como ANSI e um acento parte o ficheiro.
- Documentação em **pt-PT**; commits `feat|fix|refactor|test|docs(<módulo>): …`.
- **Regenerar `openapi.json`** sempre que se mude um endpoint.

### Escrever blocos da bateria — lições já pagas

- **Navegar antes de agir.** Cada bloco obtém os ids da própria API. Nenhum id à mão.
- **Papéis pelo `numeroFuncionario`, nunca pela posição na lista.** A ordem de
  `/funcionarios` muda com os dados: numa execução o "A" era o Francisco, na
  seguinte era a Joana, e os passos liam a pessoa errada **sem falhar**. Há um
  helper `PorNumero` no topo do script.
- **Datas ancoradas em `Get-Date`.** "Em vigor" quer dizer que o registo **cobre
  hoje** (`mobilidadeEmVigor` usa `LocalDate.now()`). Datas fixas em anos futuros
  fazem o teste falhar sem haver bug.
- **`.Count` precisa de `@()` a envolver o pipeline todo** no PS 5.1.
- **Enviar sempre `Accept: application/json`.** Sem o cabeçalho o servidor
  negoceia e devolve os **erros em XML** enquanto os sucessos vêm em JSON.
- **O `close` do contrato é idempotente** (200, não 409) e não sobrepõe o primeiro.
- **Repor a BD antes de cada execução** (`scripts/repor_estado.sql`).

## Blockers & risks

- **O compilador incremental do Maven mente.** Depois de mexer em lotes de
  ficheiros, `mvn test` sem `clean` diz *"Nothing to compile"* (escondendo erros)
  ou rebenta com `NoClassDefFoundError` numa classe que existe e está importada.
  **Usar sempre `mvn -B clean test`.** Custou tempo duas vezes.
- **Jar preso**: com a app a correr, `clean`/`repackage` falha. Fechar primeiro.
- **Jar sem recursos**: `package` sem `clean` pode gerar um jar incompleto.
  Confirmar `jar tf … | grep classes/application.properties`.
- **Testes unitários não vêem erros de contrato.** Mockam o `commandBus`: um
  handler e um controlador com tipos diferentes passam nos testes e rebentam em
  runtime — aconteceu (`ClassCastException` no `ReferenceOptionsController`). **Só
  a bateria os apanha.**
- **A bateria deixa a BD alterada.** Repor antes de cada execução.
- **O servidor Java do VS Code compila para o mesmo `target/`.** A extensão
  `redhat.java` recompila a cada ficheiro guardado e escreve em `target/classes`,
  colidindo com o Maven da consola: dá *"error while writing ...class"* ou
  *"package ... does not exist"* em pacotes que existem. **Não é regressão.**
  Repetir o comando resolve; se insistir, fechar o VS Code ou suspender a
  extensão. Custou duas corridas nesta sessão, e o sintoma engana.
- **Um script de edição que não verifica a âncora dá confirmação falsa.** Os
  blocos F11–F13 do README da bateria **nunca entraram** porque apontavam para uma
  tabela que acaba no F5 — do F6 em diante o documento usa secções `###`. O script
  gravou o ficheiro à mesma e imprimiu "ok". **Verificar sempre se a marca existe
  antes de substituir, e confirmar no ficheiro depois.**
- **Editar uma migração já aplicada parte o arranque.** O `contextLoads` dos
  testes corre o Flyway, por isso **correr os testes aplica as migrações**. Mudar
  o ficheiro depois disso dá *checksum mismatch*. Em desenvolvimento: desfazer à
  mão os efeitos e apagar a linha de `flyway_schema_history` para ser reaplicada.
- **`ADD COLUMN ... DEFAULT` preenche já as linhas existentes.** Uma migração que
  acrescente a coluna com omissão e só depois classifique `WHERE ... IS NULL`
  nunca classifica nada — e **não falha**, nem nos testes nem no arranque. A ordem
  certa é: coluna sem omissão → classificar → `NOT NULL` → `SET DEFAULT`. Custou
  uma classificação inteira errada na V49 (7 linhas em `FALTA`, férias incluídas),
  só visível ao consultar a base.
- **Docker pode não estar a correr** ao retomar — foi o caso a 2026-09-21, com o
  Docker Desktop em baixo. `postgres-ingt-rh` tem de estar de pé **antes dos
  testes**, não só antes da bateria: o `contextLoads` falha sem ele com
  `Connection to localhost:5436 refused`. Sintoma enganador — parece regressão e
  é ambiente.

### Ausências não é assiduidade — e o modelo não a consegue exprimir

Verificado contra o **DL n.º 3/2010** (texto obtido em `mf.gov.cv`) e contra o
esquema. **Não é questão de nomes**: há coisas que nenhuma configuração do
catálogo consegue representar.

A definição legal de falta (**art. 13.º**) assenta no **período diário de presença
obrigatória** — um horário, que não temos:
- **n.º 1** — falta é a ausência da totalidade *ou parte* do período diário;
- **n.º 4** — as ausências curtas **somam-se**: menos de meio período conta meio,
  mais de meio conta um período inteiro;
- **n.º 2** — em horário flexível, o **débito de tempo** é falta.

O nosso `t_leave_request` tem `data_inicio`/`data_fim` (**DATE**) e `numero_dias`
(**int**): **meio dia é inexprimível**, quanto mais horas. E toda a linha é um
*pedido* com estado — não há como registar uma falta que ninguém pediu.

Efeitos que a lei manda e o catálogo não tem campo para exprimir:
- **art. 16.º n.º 2** — certas faltas implicam **perda parcial** da remuneração.
  `t_leave_type` **não tem `affects_pay`** (esse campo existe só no subtipo).
- **art. 43.º n.º 2** — as injustificadas **não contam para antiguidade** e
  implicam perda de remuneração **ou desconto nas férias**. O `deducts_balance` só
  desconta no saldo do próprio tipo.
- **art. 16.º n.º 4** — a greve perde remuneração mas **não** desconta antiguidade.

**Configura-se o catálogo de motivos, não o regime.**

### ~~Lacuna: não há como ler as substituições~~ — RESOLVIDA (2026-09-22)

`GET /funcionarios/{id}/substituicoes` devolve-as nos **dois papéis** — `SUBSTITUTO`
e `TITULAR` —, com a contraparte, o Lugar e o período; `apenasCorrentes` separa o
que está em vigor do histórico. Enquanto durar, a `dataFim` vem **nula**: caduca
com o regresso do titular (art. 77.º n.º 2), não numa data combinada.

Decidiu-se o endpoint próprio em vez de esperar pelo percurso do colaborador —
não o impede, e a lacuna custava caro: o fecho automático da substituição
provava-se **por via indirecta**. Agora prova-se directamente (F6.30b: já não há
nenhuma em vigor; F6.30d: o histórico guarda-a, encerrada e com data de fim), e a
prova antiga fica como confirmação.

Uma substituição anterior à V46 não tem ligação ao titular: aparece na mesma, sem
contraparte. O registo existiu, e escondê-lo seria pior do que mostrá-lo
incompleto.

### ~~Achado: licença que acaba antes de começar~~ — RESOLVIDO na V48 (2026-09-22)

Era o `encerrar()` a fixar o fim em hoje sem olhar ao início. Não se corrigiu com
uma guarda: **separou-se a decisão do período**, e o caso deixou de ter caminho.
Encerrar o que não começou é 409 e remete para o cancelamento (art. 44.º n.º 1:
não houve ausência); `ck_leave_mobility_periodo` impede-o também no esquema; e a
linha estragada na BD foi normalizada pela própria migração.

### Bug: feriados municipais ignorados — POR FAZER, decisão adiada pelo utilizador

`CreatePedidoAusenciaCommandHandler:50` usa `findAllNacionaisActivosByAno`, e o
`DiasUteisCalculator` desconta só os **nacionais**. Um pedido que atravesse um
feriado municipal conta um dia a mais, embora `t_public_holiday.is_national`
exista precisamente para os distinguir.

**Estado (2026-09-22):** o utilizador disse «decido depois». Corrigir a chamada é
trivial; o que falta decidir é **de que município** se contam, e essa informação
não existe na afectação nem no funcionário. Escolher por ele seria inventar um
modelo de dados.

**Importa agora** porque a contagem de dias úteis é a base das férias — e as
férias já estão construídas por cima dela.

## Relevant files

- `colaboradores/application/services/SubstituicaoService.java` — regras da
  substituição; `encerrarPorRegressoDoTitular` tem duas assinaturas.
- `colaboradores/application/services/AssignmentService.java:106`
  `validarLugarParaAfectacao` — validações de Lugar partilhadas entre afectar e
  substituir. **Não** inclui a regra do titular único, que depende do título.
- `parametrizacoes/domain/models/SituacaoFuncional.java` — as seis situações e
  `permiteSubstituicao()`.
- `colaboradores/domain/models/TipoAfectacao.java` — PRINCIPAL/SUBSTITUICAO/ACUMULACAO.
- `shared/application/dto/SuccessResponseDTO.java` — `de()`, `semEfeito()`.
- `colaboradores/domain/service/DiasUteisCalculator.java` — Seg-Sex menos feriados
  **nacionais**; é aqui que o bug dos municipais vive.
- `db/migration/V45`, `V46`, `V47`, **`V48`** (licença: decisão vs. período),
  **`V49`** (`t_leave_type.regime`), **`V50`** (acumulação de férias),
  **`V51`** (suspensão de férias). A antiguidade **não tem migração**: é derivada.
- **Férias (DL 3/2010, cap. II):**
  - `colaboradores/application/services/FeriasService.java` — vencimento
    (art. 2.º), proporcionalidade do ano de ingresso (art. 3.º) e **acumulação**
    (art. 7.º n.º 1).
  - `colaboradores/infrastructure/scheduler/VencimentoFeriasScheduler.java` —
    diário, `rh.ferias.vencimento.cron`, 00:05.
  - `colaboradores/application/commands/SuspenderFeriasCommandHandler.java` —
    suspensão (art. 8.º).
  - `parametrizacoes/domain/models/RegimeAusencia.java` — `FERIAS`/`FALTA`; é por
    aqui, e **nunca pelo código**, que se sabe quais linhas do catálogo são férias.
- **Licença/mobilidade (V48):**
  - `colaboradores/domain/models/EstadoPeriodoLicenca.java` — o eixo temporal.
  - `colaboradores/application/services/LicencaEfeitoService.java` — efeitos na
    data devida, idempotentes.
  - `colaboradores/infrastructure/scheduler/LicencaEfeitoScheduler.java` —
    `rh.licencas.efeitos.cron`, 00:15.
- **Antiguidade:**
  - `colaboradores/domain/service/CalculadoraAntiguidade.java` — a conta pura
    (recorta, **une**, subtrai). É aqui que vive a regra de não somar períodos.
  - `colaboradores/application/services/AntiguidadeService.java` — as três fontes.
- `colaboradores/application/queries/ListarSubstituicoesQueryHandler.java` — a
  leitura das substituições, nos dois papéis.
- `db/seed/seed_carreiras.sql` — `ordem_progressao` (1=ASS_TEC, 2=TEC_SUP); **sem
  ela a promoção recusa sempre**.
- `db/seed/seed_colaboradores.sql` — 3 colaboradores, 6 Lugares.
- `scripts/testes_funcionais.ps1` — bateria completa (F0 a F14, 305 passos).
- `scripts/repor_estado.sql` — **correr antes de cada execução**.
- `scripts/testes_funcionais_README.md` — o que cada bloco prova.
- `docs/funcionarios/v5/openapi.json` — contrato gerado; **fonte para as formas**.
- `docs/funcionarios/v5/api_guide.md` — §5.7 substituição (criar **e ler**),
  **§5.8 antiguidade**, §6.1 ausências vs licença, **§6.3 vencimento de férias**,
  **§6.4 acumulação**, **§6.5 suspensão**, **§7.0 os dois eixos da licença**,
  §12 enums, §2 respostas.
- `docs/funcionarios/v5/modelo_negocio.html` — §8.1 porquê dois sítios, §8.2
  porque não é assiduidade.
- `docs/funcionarios/v5/regras_negocio.html` — BR-*, incluindo BR-SUB-01..**10**,
  **BR-FER-01..12** (férias) e **BR-ANT-01..06** (antiguidade).
- `docs/funcionarios/v5/breaking_change_frontend.md` — §11.4 a **§11.13**.

## How to verify / resume

```bash
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-26.0.2.10-hotspot"
cd /c/Users/ivanick.santos/Nick-personal/ta-workspace/projects/Recursos_Humanos
git switch fix-alinhamento-legislacao

# A BD tem de estar de pe ANTES dos testes: o contextLoads liga-se-lhe.
docker start postgres-ingt-rh      # se falhar, o Docker Desktop esta em baixo
mvn -B clean test                  # esperado: 840 testes, 0 falhas (COM clean)
                                   # sem a BD: 1 erro em contextLoads, nao e regressao
```

> **Se o Maven se queixar de "error while writing ...class" ou de um pacote que
> claramente existe:** e o servidor Java do VS Code a compilar para o mesmo
> `target/`. Nao e regressao — repetir o comando resolve. Se insistir, fechar o
> VS Code.

Arranque real e bateria:

```bash
# 1. fechar a app se estiver a correr (senao o clean falha)
powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"Name='java.exe'\" | Where-Object { \$_.CommandLine -match 'RH-Service' } | ForEach-Object { Stop-Process -Id \$_.ProcessId -Force }"

mvn -B clean package -DskipTests
jar tf target/RH-Service-0.0.1-SNAPSHOT.jar | grep classes/application.properties   # tem de aparecer

java -jar target/RH-Service-0.0.1-SNAPSHOT.jar --spring.profiles.active=development
# esperar pelo "Started RecursosHumanosApplication" (30 a 50 s)

# 2. repor a BD ANTES de correr
docker cp scripts/repor_estado.sql postgres-ingt-rh:/tmp/repor.sql
docker exec postgres-ingt-rh sh -c "psql -U postgres -d recursoshumanos_db -q -f /tmp/repor.sql"

# 3. bateria
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/testes_funcionais.ps1
# esperado: PASSOS: 305   OK: 305   FALHAS: 0

# 4. regenerar o contrato depois de mexer em endpoints
curl -s -o docs/funcionarios/v5/openapi.json http://localhost:8099/v3/api-docs
```

Ambiente: BD no contentor **`postgres-ingt-rh`**, porta **5436**, base
`recursoshumanos_db`, user `postgres`. App na porta **8099** (não 8091 — o `.env`
local manda). Erro esperado e inofensivo no arranque: `AuthorizationSyncRunner`
falha porque o URL do Access Management está vazio.

Recarregar o seed de raiz:

```bash
docker exec postgres-ingt-rh sh -c "mkdir -p /tmp/seed"
docker cp src/main/resources/db/seed/. postgres-ingt-rh:/tmp/seed/
docker exec postgres-ingt-rh sh -c "cd /tmp/seed && psql -U postgres -d recursoshumanos_db -q -f master_seed.sql"
```

Estado esperado depois de repor: Francisco Bastos em LUG-0001 (TEC_SUP, esc. 1),
Maria Santos em LUG-0002 (ASS_TEC, esc. 1), Joana Tavares em LUG-0003 (ASS_TEC,
esc. 1). Vagos: **LUG-0004** (TEC_SUP, promoção com `positionId`), **LUG-0005**
(ASS_TEC, transferência), **LUG-0006** (CONGELADO, para o 422).

## Test / validation plan

**A bateria cobre F0 a F14 — 305 passos, todos OK.** F11 (vencimento de férias),
F12 (acumulação) e F13 (suspensão) entraram a 2026-09-22, e o F6 ganhou a leitura
das substituições.

**O que cada bloco prova está em `scripts/testes_funcionais_README.md`**, e é lá
que se actualiza. Não se repete aqui de propósito: duas cópias divergem sempre —
foi o que aconteceu com os `.html` gémeos dos documentos v5, que andaram meses a
contradizer o código.

### Se acrescentares um bloco novo

- Entra **antes** do `=========== RESUMO ===========`.
- Segue as "lições já pagas" em Constraints.
- **Os blocos são encadeados**: cada um herda o estado que o anterior deixou. No
  fim do F10 a base fica assim:

  | Colaborador | Estado | Lugar |
  |---|---|---|
  | 0000001 Francisco Bastos | `INACTIVE` (cessado no F7) | sem Lugar |
  | 0000002 Maria Santos | `ACTIVE` | um Lugar de TEC_SUP |
  | 0000003 Joana Tavares | `RETIRED` (cessada no F7) | sem Lugar |

  Um bloco que precise de alguém activo **com** Lugar usa a Maria, ou reafecta
  primeiro — é o que o F6.9, o F8.6 e o F9.6 fazem.
- **O F9 reclassifica um Lugar.** O `repor_estado.sql` repõe as categorias dos
  seis; se acrescentares Lugares ao seed, acrescenta-os lá também.

## Open questions

1. **Assiduidade entra no âmbito?** Se entrar, é um **módulo** (horário + registo
   diário + saldo de horas), não um campo no catálogo. Pré-requisito: a
   antiguidade. Se não entrar, dizê-lo nos documentos para ninguém instalar isto a
   pensar que tem controlo de assiduidade. **Decide o cliente.**
2. ~~**Ler as substituições**~~ — **RESOLVIDA (2026-09-22).** Fez-se o endpoint
   próprio, `GET /funcionarios/{id}/substituicoes`. Não impede o percurso do
   colaborador mais tarde; quando existir, agrega isto com mobilidades, licenças e
   estados.
3. ~~**Licença que acaba antes de começar**~~ — **RESOLVIDA na V48 (2026-09-22).**
   Não se recusou com 422 nem se colou a data: separou-se a decisão do período, e
   o caso deixou de ter caminho. Encerrar o que não começou é 409 e remete para o
   cancelamento (art. 44.º n.º 1: não houve ausência); a restrição
   `ck_leave_mobility_periodo` impede-o também no esquema.
4. **Feriados municipais** — corrigir é pequeno; a pergunta é se a contagem deve
   usar o município do colaborador, e essa informação não está na afectação.
   **ADIADA pelo utilizador a 2026-09-22 («decido depois»).** Continua a valer o
   aviso: a contagem de dias úteis é a base das férias, que já estão construídas.
5. **Limite de substituições por pessoa** — hoje não há nenhum. **Validar com o RH.**
6. **A promoção exige lugar vago da categoria superior?** As duas formas estão
   suportadas; a prática da instituição decide. **Cliente.**
7. **`ACUMULACAO`** — está no enum e não tem endpoint, serviço nem regra. Desde a
   V45 o índice já não a bloqueia, por isso `POST /assignments` com
   `assignmentType=ACUMULACAO` cria uma segunda afectação **sem validação
   nenhuma**. Decidir: fechar com 422 até haver regras, implementar como
   modalidade de mobilidade (art. 134.º n.º 2 al. b), ou como título próprio.
8. **Faltam duas modalidades de licença do art. 45.º** no seed: sem vencimento
   **até 90 dias** e **extraordinária**. O catálogo é da instituição, logo
   acrescentam-se sem código — mas convém virem no seed.
9. **Produção**: `ddl-auto` vem de `${HIBERNATE_DDL:update}` e os
   `application-<perfil>.properties` estão vazios. Sem `HIBERNATE_DDL` definida, o
   Hibernate altera o esquema por baixo do Flyway. **Adiado — ainda não há
   produção** (confirmado pelo utilizador).
10. **Jurídico**: confirmar se algum diploma substituiu o DL 3/2010 e qual é o
    diploma da mobilidade.
11. **Faltas injustificadas e antiguidade** — o art. 43.º n.º 2 diz que não
    contam, mas `t_leave_type` não tem coluna que diga quais o são. **Não se
    adivinhou** a partir do código `FALTA_INJUSTIFICADA`. Para fechar: acrescentar
    `counts_seniority` a `t_leave_type` (ou um terceiro valor ao `regime` da V49) e
    uma quarta fonte ao `AntiguidadeService`. **Decide o RH** se as quer descontar.
12. **Marcação de férias** (art. 5.º e 6.º) — mapa até 31 de Março, preferência até
    31 de Janeiro, mínimo de 11 dias num dos períodos, fixação pelo dirigente entre
    Maio e Outubro na falta de acordo. **Por decidir com o RH/produto: quem aprova
    o mapa, e o que acontece a quem não indica preferência.** É a única parte do
    cap. II que falta além dos meios-dias.
13. **Lock distribuído para os jobs** — há agora **sete** `@Scheduled` (cinco do
    `sigdi`, mais o dos efeitos das licenças e o do vencimento de férias) e nenhum
    tem lock. Com réplicas, correm em todas. As marcas de idempotência limitam o
    estrago mas não substituem um lock. Entra no mesmo saco de produção que o
    `HIBERNATE_DDL` (questão 9), e é o que o framework de jobs do
    `inss_core_service` resolveria.

## Next step

**Push por fazer** — há **8 commits locais** por enviar
(`git push origin_git_lab fix-alinhamento-legislacao`). O utilizador pediu para
não o fazer ainda; **não enviar sem lhe perguntar**.

### O que pode avançar já, sem esperar por ninguém

Por ordem do plano, e ambos independentes:

1. **Movimentos menores** (ponto 3) — sete, sem caminho nenhum. A **mudança de
   carreira** é a mais grave: hoje é *impossível*, porque a promoção exige a mesma
   carreira e a transferência exige a mesma categoria. Art. 139.º e art. 35.º do
   PCFR.
2. **Percurso do colaborador** (ponto 4) — linha temporal única. Já não precisa de
   resolver a leitura das substituições (feita), mas continua a valer como
   agregador de afectações, mobilidades, licenças e estados.

### O que está à espera de decisão (não avançar sem)

| Assunto | Quem decide | Onde está |
|---|---|---|
| Marcação de férias — quem aprova o mapa; quem não indica preferência | RH/produto | questão 12 |
| Feriados municipais — de que município | RH | questão 4, adiada pelo utilizador |
| Faltas injustificadas descontam antiguidade? | RH | questão 11 |
| Meios-dias | **já decidido: não, por agora** | abaixo |
| Assiduidade entra no âmbito? | cliente | questão 1 |

### Do capítulo II das férias, o que falta

- **Marcação** (art. 5.º e 6.º) — mapa de férias até 31 de Março, indicação de
  preferência até 31 de Janeiro, mínimo de 11 dias num dos períodos em gozo
  interpolado, fixação pelo dirigente entre Maio e Outubro na falta de acordo.
  **Por decidir com o RH/produto:** quem aprova o mapa, e o que acontece a quem
  não indica preferência.

- **Marcação** (art. 5.º e 6.º) — mapa de férias até 31 de Março, indicação de
  preferência até 31 de Janeiro, mínimo de 11 dias num dos períodos em gozo
  interpolado, fixação pelo dirigente entre Maio e Outubro na falta de acordo.
  **Por decidir com o RH/produto:** quem aprova o mapa, e o que acontece a quem
  não indica preferência.
- **Meios-dias** (art. 2.º n.º 6, até 5) — **ADIADO por decisão do utilizador
  (2026-09-22): «por agora não teremos pedido de meio dia».** Fica na lista do que
  está por fazer, não em discussão. O que o trava é ser uma mudança de contrato:
  `t_leave_request.numero_dias` e as quatro contagens de `t_leave_balance` são
  `INTEGER`, e passá-las a decimal obriga a rever qualquer ecrã que some ou
  formate dias. Enquanto não se fizer, **um pedido de meio dia é inexprimível** —
  arredondaria para zero (dia de graça) ou para um (desconto a dobrar).
  Não confundir com o art. 13.º n.º 4 (meios períodos nas faltas), que depende do
  horário e é mesmo assiduidade.

Foi por serem decisões de processo, e não de código, que se fez primeiro a
acumulação, a suspensão e a antiguidade.

A questão que bloqueava as férias — a licença que acabava antes de começar — está
resolvida: as contagens de dias já assentam em períodos válidos.

O caminho das férias:
1. O saldo tem de **nascer sozinho** — hoje é criado à mão. Regra do DL 3/2010
   cap. II, com **proporcionalidade no ano de admissão** (art. 3.º: a partir dos
   90 dias de serviço efectivo, 6 ou 5 dias úteis por cada 3 meses completos).
2. Acumulação entre anos.
3. Marcação (o pedido já existe; falta o que distingue férias de uma falta comum).

Antes de começar: `docker start postgres-ingt-rh`, `mvn -B clean test` (**840**, 0
falhas), repor a BD e correr a bateria (**305/305**) para confirmar que se parte de
verde.

> A antiguidade já está feita e **já lê** o art. 47.º n.º 1 (a licença sem
> vencimento desconta). O que os n.os 2 e 3 desse artigo mandam — férias do ano
> seguinte proporcionais ao tempo de serviço, depois de uma licença — **ainda não
> está**: é um caso particular do vencimento que a V49 não cobre, porque ela
> proporciona apenas no **ano de ingresso** (art. 3.º). Fica assinalado aqui para
> não se perder.

**Migração já aplicada não se edita.** O `contextLoads` dos testes corre o Flyway,
por isso **correr os testes aplica as migrações**. Editar o ficheiro depois disso
faz o arranque falhar com *checksum mismatch*. Em desenvolvimento resolve-se
desfazendo à mão os efeitos da migração e apagando a linha respectiva de
`flyway_schema_history`, para ser reaplicada limpa.

**`ADD COLUMN ... DEFAULT` preenche já as linhas existentes.** Uma migração que
acrescente a coluna com omissão e só depois classifique `WHERE ... IS NULL` nunca
classifica nada — e não falha, nem nos testes nem no arranque. A ordem certa é:
coluna sem omissão → classificar → `NOT NULL` → `SET DEFAULT`. Custou uma
classificação inteira errada (7 linhas em `FALTA`, férias incluídas), só visível
ao consultar a base.

**Cuidado que custou tempo nesta sessão:** não editar `src/` com um build a
correr. O Maven apanha o ficheiro a meio e o resultado parece uma regressão de
mais de cem testes que não existe. Também não correr dois Maven ao mesmo tempo
sobre o mesmo `target/` — o IDE do utilizador tem um Maven próprio, e a colisão
produz um jar sem recursos e erros de ficheiros em falta.
