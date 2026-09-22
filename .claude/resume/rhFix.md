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

**Branch `fix-alinhamento-legislacao`**. O commit que a versão anterior deste
documento dava como pendente **já foi enviado**; há **1 commit local por enviar**
a 2026-09-22 (`204f29af`, a V48). O GitLab é o repo da equipa; merge para
`master` é deploy.

- **Testes: 784, 0 falhas — mas só com a base de dados de pé.** 783 são unitários
  puros; o `RecursosHumanosApplicationTests.contextLoads` carrega o contexto Spring
  completo e o Flyway liga-se ao Postgres. **Sem o contentor a correr dá 1 erro, e
  não é regressão.** Correr **sempre com `clean`** (ver Blockers).
- **Bateria funcional: 221 passos, 221 OK**, cobre **F0 a F10**.
- **Migrações V40 a V48** aplicadas e verificadas na BD. Próxima livre: **V49**.
- **`openapi.json`**: 225 caminhos, 238 esquemas, **0 operações não-sigdi sem
  esquema de resposta**.
- **Nenhum handler ou controlador não-sigdi devolve `Map`.**

### O que esta sessão fez (19 commits, `ee61e15e..HEAD`)

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

1. **Férias (DL 3/2010, cap. II)** — o ciclo do saldo funciona e o F4 cobre-o,
   mas **o saldo é criado à mão** via `POST /saldos-ausencia`. Falta o negócio:
   marcação, acumulação entre anos, e gozo **proporcional ao tempo de serviço** no
   ano de admissão. Quem instale isto hoje pensa que tem gestão de férias e tem
   metade.
2. **Antiguidade / tempo de serviço** — **não se calcula em lado nenhum**. É a
   raiz de três colunas mortas: `affects_pay` e `counts_for_seniority` (subtipo) e
   `counts_seniority` (vínculo laboral) existem e **ninguém as lê**;
   `SituacaoFuncional.contaAntiguidade()` também não tem consumidor. Quem
   parametrizar estas colunas hoje fica convencido de que fez alguma coisa.
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

### Lacuna: não há como ler as substituições

`POST /funcionarios/{id}/substituicao` devolve o id, mas **nada o lista depois**.
`GET .../unidade-atual` só devolve a `PRINCIPAL`. Um ecrã de RH **não consegue
mostrar** quem substitui quem. Na bateria, o fecho automático teve de ser provado
**indirectamente** (F6.30-33): põe-se o titular impedido outra vez e tenta-se nova
substituição — se a anterior não tivesse fechado daria `409`, e dá `201`.

### Achado: licença que acaba antes de começar

`LicencaMobilidade.encerrar()` fixa a data de fim em **hoje** sem verificar se hoje
é anterior ao **início**. Visto na BD: `LIC_FORMACAO, início 2026-10-01, fim
2026-09-19`. Não bloqueia nada, mas **falseia contagens de dias** — e a contagem de
dias é a base das férias e da antiguidade.

### Bug: feriados municipais ignorados

`CreatePedidoAusenciaCommandHandler:50` usa `findAllNacionaisActivosByAno`. Um
pedido que atravesse um feriado municipal conta um dia a mais, embora
`t_public_holiday.is_national` exista para os distinguir.

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
- `db/migration/V45`, `V46`, `V47`.
- `db/seed/seed_carreiras.sql` — `ordem_progressao` (1=ASS_TEC, 2=TEC_SUP); **sem
  ela a promoção recusa sempre**.
- `db/seed/seed_colaboradores.sql` — 3 colaboradores, 6 Lugares.
- `scripts/testes_funcionais.ps1` — bateria completa (F0 a F10, 207 passos).
- `scripts/repor_estado.sql` — **correr antes de cada execução**.
- `scripts/testes_funcionais_README.md` — o que cada bloco prova.
- `docs/funcionarios/v5/openapi.json` — contrato gerado; **fonte para as formas**.
- `docs/funcionarios/v5/api_guide.md` — §5.7 substituição, §6.1 ausências vs
  licença, §12 enums, §2 respostas.
- `docs/funcionarios/v5/modelo_negocio.html` — §8.1 porquê dois sítios, §8.2
  porque não é assiduidade.
- `docs/funcionarios/v5/regras_negocio.html` — BR-*, incluindo BR-SUB-01..09.
- `docs/funcionarios/v5/breaking_change_frontend.md` — §11.4 a §11.7.

## How to verify / resume

```bash
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-26.0.2.10-hotspot"
cd /c/Users/ivanick.santos/Nick-personal/ta-workspace/projects/Recursos_Humanos
git switch fix-alinhamento-legislacao

# A BD tem de estar de pe ANTES dos testes: o contextLoads liga-se-lhe.
docker start postgres-ingt-rh      # se falhar, o Docker Desktop esta em baixo
mvn -B clean test                  # esperado: 763 testes, 0 falhas (COM clean)
                                   # sem a BD: 1 erro em contextLoads, nao e regressao
```

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
# esperado: PASSOS: 207   OK: 207   FALHAS: 0

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

**A bateria cobre F0 a F10 — 207 passos, todos OK.** Não há blocos por escrever.

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
2. **Ler as substituições** — não há endpoint. Endpoint próprio ou dentro do
   percurso do colaborador? **Decide o RH/produto.**
3. ~~**Licença que acaba antes de começar**~~ — **RESOLVIDA na V48 (2026-09-22).**
   Não se recusou com 422 nem se colou a data: separou-se a decisão do período, e
   o caso deixou de ter caminho. Encerrar o que não começou é 409 e remete para o
   cancelamento (art. 44.º n.º 1: não houve ausência); a restrição
   `ck_leave_mobility_periodo` impede-o também no esquema.
4. **Feriados municipais** — corrigir é pequeno; a pergunta é se a contagem deve
   usar o município do colaborador, e essa informação não está na afectação.
   **Decide o RH.**
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

## Next step

**Enviar o commit da V48** (`git push origin_git_lab fix-alinhamento-legislacao`)
e depois atacar o **ponto 1 do plano: férias**.

A questão que bloqueava as férias — a licença que acabava antes de começar — está
resolvida: as contagens de dias já assentam em períodos válidos.

O caminho das férias:
1. O saldo tem de **nascer sozinho** — hoje é criado à mão. Regra do DL 3/2010
   cap. II, com **proporcionalidade no ano de admissão** (art. 3.º: a partir dos
   90 dias de serviço efectivo, 6 ou 5 dias úteis por cada 3 meses completos).
2. Acumulação entre anos.
3. Marcação (o pedido já existe; falta o que distingue férias de uma falta comum).

Ao chegar à antiguidade, lembrar que o art. 47.º n.º 1 manda descontar os dias de
licença sem vencimento, e os n.os 2 e 3 fazem as férias do ano seguinte
proporcionais ao tempo de serviço — é para isso que a V48 preparou o terreno.

Antes de começar: `docker start postgres-ingt-rh`, `mvn -B clean test` (784, 0
falhas), repor a BD e correr a bateria (221/221) para confirmar que se parte de
verde.

**Cuidado que custou tempo nesta sessão:** não editar `src/` com um build a
correr. O Maven apanha o ficheiro a meio e o resultado parece uma regressão de
mais de cem testes que não existe. Também não correr dois Maven ao mesmo tempo
sobre o mesmo `target/` — o IDE do utilizador tem um Maven próprio, e a colisão
produz um jar sem recursos e erros de ficheiros em falta.
