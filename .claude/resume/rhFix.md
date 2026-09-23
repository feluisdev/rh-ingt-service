> Updated: 2026-09-23 13:54 (-01:00) — sessão de 2026-09-23 (tarde)

## Goal

Alinhar o núcleo RH (não-sigdi) com a legislação cabo-verdiana — DL n.º 3/2010 (férias, faltas,
licenças) e Lei n.º 20/X/2023 (função pública) —, ponto a ponto, com regras documentadas e testadas.
O que a lei fixa vive em código (enum); o que varia com a instituição, ou com um diploma novo, vive
em tabela editável pela API. Um ponto por commit.

## Current state

**Branch `fix-alinhamento-legislacao`**, **36 commits locais por enviar** (`origin_git_lab`).
**Não fazer push sem o utilizador pedir** — merge para `master` no GitLab é deploy.

- **Testes: 1084, 0 falhas** — correr na **cópia isolada** (ver Blockers).
- **Bateria funcional: 663 passos**, 663 OK (2026-09-23, duas execuções seguidas). Blocos F20–F30
  cobrem V55, V56, mapa de férias, parâmetros de férias, horários, registo diário, faltas por débito e
  pedido em horas (V58), pedido pelo próprio em `/me`, registo pelo próprio com validação e trabalho
  suplementar.
- Migrações até **`V58`**. Próxima livre: **V59** (só para alterar tabelas existentes).
- **`openapi.json`**: 268 caminhos (regenerado com a app a correr).
- **Sete jobs `@Scheduled`**, sem lock distribuído (fica para o framework de jobs).
- Árvore limpa excepto `.claude/settings*.json` (não são desta sessão) e os textos das leis não
  versionados na raiz: `.lei20.txt` (Lei 20/X/2023), `.dl3.txt` (DL 3/2010, numa só linha),
  `.estrutura.txt`. **Ler o articulado antes de desenhar.**

**Feito hoje (6 commits):**

| Commit | O quê |
|---|---|
| `447f5273` | **`V55`** — feriados recorrentes, com área, e todos contam (`CalendarioFeriadosService`) |
| `dc1bbf66` | **`V56`** — `t_leave_type.contagem` ∈ DIAS_UTEIS · DIAS_SEGUIDOS (art. 76.º) |
| `6114faa8` | Mapa de férias — `FeriasDoAno`, `MapaFerias`, 5 endpoints, tabelas `t_ferias_*` sem migração |
| `2b9a3d00` | Parâmetros do mapa de férias saem de `application.properties` para `t_parametro_ferias`, **por vigência** (`/catalogs/parametros-ferias`) |
| `c60aaece` | `RegimeTrabalho` deixa de citar a lei portuguesa (LGTFP, 35h) e cita a Lei 20/X/2023 |
| `3386b1fc` | **Assiduidade, 1.º tijolo** — catálogo de horários, horário base, **`V57`** (horário da unidade orgânica), horário do colaborador com regime de prestação |

**Plano:** 1 feriados ✔ · 2 dispensas ✔ · 3 mapa de férias ✔ · **4 — Assiduidade**: horários ✔ →
registo diário ✔ → faltas por débito ✔ → pedido em horas + amamentação ✔ → registo pelo próprio com validação ✔ → trabalho suplementar ✔ → **relação mensal do art. 75.º, só leitura (a seguir)** ·
fecho mensal (congelar/bloquear/reabrir) **adiado até haver integração salarial** · meios-dias de férias
(em falta face à lei, DL 3/2010 art. 2.º n.º 6) · tectos em horas (só se o cliente pedir) ·
framework de jobs (por último).

## Decisions made — do not re-litigate

**Desta sessão (utilizador, 2026-09-23):**
- **Parâmetros do mapa de férias em tabela, não em properties**: mudam sem tocar em código.
  `t_parametro_ferias`, **global** (não por instituição), com `vigente_desde` (uma linha por ano,
  vale até à seguinte). Sem linha, valem os da lei (`origem: LEI`). Reverte a decisão anterior de os
  pôr em `application.properties`.
- **`areaCkey` é provisória**: passa para uma tabela de geografia própria quando existir. Não
  investir em validá-la contra o `Option`.
- **`HIBERNATE_DDL=update` em produção** («é a primeira versão») — as tabelas sem migração nascem no
  arranque. A regra «entidade nova sem migração» mantém-se. Questão 9 fechada.
- **Lock dos jobs** (questão 13) resolve-se no framework de jobs, não antes.
- **Horários (assiduidade, 1.º tijolo):**
  - **Modalidade = nome livre**; o código só conhece `controlo` ∈ FIXO · FLEXIVEL (é o que as regras
    usam: hora marcada vs débito por período de aferição, art. 13.º n.º 2 do DL 3/2010). Enum fechado
    de modalidades seria adivinhar o diploma (art. 165.º n.º 2), que não temos.
  - **Limites legais não se validam** (duração máxima, intervalos, turnos — art. 164.º n.º 4 e 165.º
    n.º 4): esperam o diploma de desenvolvimento. Nada de inventar valores.
  - **O horário da unidade orgânica é opcional** («não ponha obrigatório»); herda-se da unidade-mãe.
  - **Horário base**: uma unidade sem horário, nem na cadeia, herda um horário base da instituição.
    No máximo um; marca-se com `PATCH /catalogs/horarios/{id}/base`; o base não se desactiva.
  - **Regime de prestação** (PRESENCIAL · TELETRABALHO · MISTO, art. 166.º) entra já, no horário do
    colaborador — o art. 170.º define falta de forma diferente conforme o regime.
  - Resolução do vigente: **colaborador → unidade/mãe → base → nenhum**.
  - No flexível há `duracaoDiaria` (obrigatória): sem ela não há débito a apurar. Não estava no
    desenho aprovado; foi apresentada ao utilizador na entrega.
- **Registo diário (assiduidade, 2.º tijolo)** — lei + prática da indústria:
  - guardam-se **marcações** (a prova: momento, ENTRADA/SAIDA, origem), que **nunca se apagam**: uma
    correcção é marcação nova com motivo; a errada anula-se com motivo e fica visível;
  - o dia **calcula-se** das válidas (pares por ordem); o que não emparelha é **anomalia** e não conta;
  - **várias formas de registar sobre um só modelo** (`origem`): relógio (importação genérica,
    repetível pela `referenciaExterna`), RH (excepções/correcções). **`/me` (o próprio, com validação
    da chefia) é o passo seguinte**;
  - correcção num dia com marcações **exige motivo**;
  - **versões dos horários: não** — o **fecho mensal** (relação do art. 75.º do DL 3/2010) guarda o
    esperado e bloqueia o mês. Ordem: registo → faltas por débito → relação mensal/fecho → trabalho
    suplementar.
- **Faltas por débito (assiduidade, 3.º tijolo)** — lei + indústria:
  - **calcula-se, não se guarda** (`GET /funcionarios/{id}/faltas-apuradas?mes=`) até ao fecho mensal;
  - fixo: blocos não cobertos (INCOMPLETO); flexível: plataformas (PLATAFORMA) + débito no fim da
    aferição sem dupla contagem; dia sem marcações conta inteiro (**SEM_REGISTO**); dia com anomalia
    **POR_CORRIGIR**; **isenção de horário fora do débito**;
  - **art. 13.º n.º 4: o parcial soma-se no mês** («é adicionada»; o mês é a unidade do art. 75.º; é
    como a indústria e o CT português art. 248.º n.º 2 fazem) e converte-se pelo período normal
    (média do esperado no mês): resto até meio período = meia falta, acima = uma. Por confirmar com o
    jurídico, mas não bloqueia;
  - **ausência parcial justifica-se com pedido de ausência em horas** (horaInicio/horaFim opcionais,
    num só dia) — é o **próximo passo**, com a amamentação (art. 172.º n.º 3 da Lei 20).
- **Pedido em horas e amamentação (assiduidade, 4.º tijolo)** — **sem tabelas novas** (o utilizador
  corrigiu a proposta de `t_dispensa_amamentacao` + `t_parametro_assiduidade`: «dispensa amamentação tem
  uma tabela própria??»). É o pedido de ausência com `horaInicio`/`horaFim` opcionais (V58), que valem em
  cada dia do intervalo; a amamentação é um tipo de catálogo (`DISPENSA_AMAMENTACAO`: 120 min/dia na nova
  coluna `max_minutos_por_dia`, 183 dias por ocorrência). Lei 20 (2h/dia) prevalece sobre o DL art. 20.º
  (45 min/período); a duração de 6 meses vem do DL — por confirmar com o jurídico. Só tipos sem saldo,
  regime FALTA e sem tectos anuais/mensais; meios-dias de férias e tectos em horas ficam para depois.
  Fim antecipado: `PATCH …/terminar` (usa `suspenso_em`). O apuramento desconta as horas justificadas.
- **`/me` pedido de ausência = regras do RH** (fix, 2026-09-23): o handler do self-service delega no
  `CreatePedidoAusenciaCommandHandler` (antes contava dias de calendário, sem contagem/feriados/tectos).
  Sobreposição passou de 400 para 409. Ganhou `startTime`/`endTime` opcionais.
- **Registo pelo próprio (feito):** picagem em tempo real em
  `/me` **só em dias de TELETRABALHO/MISTO** (art. 170.º: presencial = presença no local, que a web
  não prova; teletrabalho = disponibilidade); pedido de correcção PENDENTE validado pela **chefia
  directa** (Lugar-pai) **ou pelo RH** (sempre; e quando a chefia está vaga); ninguém valida as suas;
  `estado` na marcação (VALIDA/PENDENTE/REJEITADA), só VALIDA conta; dia com pendentes = POR_VALIDAR
  no apuramento.
- **Sem integração com o processamento salarial por agora** (utilizador, 2026-09-23). Por isso o
  **fecho mensal completo fica adiado** (só serve quando alguém recebe o número); faz-se a **relação
  mensal como leitura**. Quando a integração vier, perguntar à outra equipa: como recebe (API, ficheiro,
  Kafka), que detalhe, identificador do colaborador, prazo no mês.
- **Meios-dias de férias**: direito do funcionário (DL 3/2010 art. 2.º n.º 6, até 5 por ano) — em falta
  face à lei, não «a pedido do cliente»; pode esperar. **Tectos em horas**: só se o cliente os tiver.
- **Trabalho suplementar (feito)** — Lei 20 art. 155.º n.º 2 a); só horas, sem valores. **Tabela
  própria** `t_trabalho_suplementar` (ddl-auto): é uma autorização com ciclo de vida
  (PEDIDO → AUTORIZADO · RECUSADO → CANCELADO); o pedido de ausência foi rejeitado por ter o significado
  oposto (estragaria apuramento, saldos, sobreposições). Nenhum catálogo novo: `TipoDiaSuplementar`
  (DIA_UTIL · DESCANSO · FERIADO) é enum, calculado. Decisões do utilizador: **RH e chefia directa
  lançam (autorizado), o próprio pede** (PEDIDO); **realizado só pelas marcações** dentro do intervalo
  (nada declarado); **autorização posterior** só pela chefia/RH, assinalada. Isentos de horário não fazem
  (art. 155.º n.º 2 b) — leitura a confirmar); num dia útil o intervalo fica fora dos blocos; no
  apuramento, a presença dentro do suplementar autorizado sai do tempo normal (`minutosSuplementares`),
  para não contar duas vezes no saldo do flexível. Fora: tecto de 1/3 (dinheiro), nocturno, compensação
  em descanso — diploma de desenvolvimento / jurídico.
- **Regra de trabalho** (utilizador): antes de desenhar, **lei + indústria + o que já existe** — e só
  depois propor; tabela nova só para conceito com ciclo de vida próprio.
- **Mapa de férias — fica por fazer** (lacunas assinaladas, não adivinhadas): preferência dos
  cônjuges no mesmo serviço (art. 5.º n.º 6, não há ligação entre colaboradores); preferência
  indicada pelo próprio em `/me`; aviso quando um pedido de férias não coincide com a marcação.

**Anteriores (resumo; o detalhe está no git e em `regras_negocio.html`):**
- **Não partir o front.** Campo novo em recurso existente: opcional, sem 422 contra catálogo, e o
  `PUT` **não apaga** o que vem omisso (nulo = manter; `""` = limpar; `false` = desmarcar). Guardas
  que só o campo novo dispara podem ficar (por isso o `horarioId` da unidade valida-se quando vem).
- **Entidade nova não leva migração** — o `ddl-auto` cria a tabela e a `_aud`. Flyway só para
  alterar tabelas existentes. As invariantes que seriam `CHECK` ficam no domínio.
- Calendário de feriados é da instituição; a pessoa só entra pela área da unidade onde exerce
  funções. Contagem (art. 76.º): dias seguidos salvo onde a lei diz úteis. Art. 77.º n.º 3 por ano
  civil (por confirmar). Mapa: marcar ≠ gozar; publicação, não aprovação.
- Ausências ≠ licenças; mobilidade não toca na afectação; uma cadeira, um titular; licença: decisão ≠
  período; férias: saldo nasce sozinho, acumulação é acto com motivo, suspensão devolve ao próprio
  ano; antiguidade derivada; falta injustificada desconta antiguidade.
- **Fora do âmbito por decisão:** permuta, estágio probatório, reintegração judicial. Não reabrir.
- Parametrizar valores, não decisões. Motivos não se validam. Nada de filtrar em memória. Nada de
  respostas `Map`. O que é derivável não se guarda.

## Constraints

- **JDK 26**: `export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-26.0.2.10-hotspot"`.
- Docs em **pt-PT**; commits `feat|fix|docs(<módulo>): …` com trailer `Co-Authored-By`.
- **Pedir autorização antes de cada commit e antes de implementar cada ponto** — o utilizador
  aprova o desenho primeiro, e prefere «aos poucos».
- Migração (alterar tabela existente): defensiva, idempotente, guarda `to_regclass` **aninhado**,
  coluna sem DEFAULT → preencher → NOT NULL → DEFAULT. Provar numa base vazia (2×) e numa cópia
  (`pg_dump`) 3×, **antes** do Java.
- Controladores em `interfaces/rest/` são gerados: mudar a par o manifesto `.igrpstudio/`.
  `@ApiResponse` com `@Content` precisa de `schema` explícito.
- JPQL usa o nome `@Entity` (`ColabsFuncionarioEntity`); `year()/month()/day()`; entidades de
  `colaboradores` levam o prefixo `Colabs` no nome.
- `scripts/*.ps1` e `repor_estado.sql` **só ASCII**. Regenerar `openapi.json` quando um endpoint muda.
- O seed usa `ON CONFLICT DO NOTHING`: numa base existente, o que muda vai no `scripts/repor_estado.sql`,
  com guarda `to_regclass` para as tabelas do ddl-auto.
- No bash deste ambiente, **heredocs com texto longo partem** («unexpected EOF») — escrever
  ficheiros com a ferramenta Write; em `docker exec … -f /tmp/x` usar `sh -c "…"`; o Python no
  Windows escreve cp1252 no stdout — `PYTHONIOENCODING=utf-8` ou gravar com `encoding="utf-8"`.

## Blockers & risks

- **O servidor Java do VS Code escreve em `target/` a meio do Maven** — correr Maven numa cópia
  isolada (ver How to verify). Não é regressão.
- **A app lê o `.env` da pasta de onde arranca**: arrancar sempre a partir da raiz do projecto.
- Docker Desktop pode estar em baixo ao retomar: `postgres-ingt-rh` tem de estar de pé **antes**
  dos testes (o `contextLoads` corre o Flyway na base real, porta 5436).
- A bateria corre com **anos futuros** nos blocos F20–F24 e só com a Maria (única com Lugar no fim do
  F19). Hoje a Maria fica numa mobilidade externa de um dia (F8): datas de «hoje» dão horário BASE.
- **Horários sem versões**: alterar os blocos muda o horário também para trás. O registo diário e as
  faltas vão precisar do horário de cada dia — decidir antes de os desenhar.
- A unidade onde a pessoa exerce funções lê-se pelo Lugar **corrente**, mesmo para datas passadas
  (feriados e horários).
- A `areaCkey` não validada: um ckey mal escrito faz o feriado não contar para ninguém, sem erro.
- `FeriasDoAno` e `Horario` persistem as tabelas filhas por `clear()`+reinserção (orphanRemoval).

## Relevant files

- `colaboradores/application/commands/CreatePedidoAusenciaCommandHandler.java` — `criarEmHoras` (V58).
  `colaboradores/domain/models/PedidoAusencia.java` — `definirHoras`, `terminar`, `ultimoDiaEmVigor`.
  `colaboradores/domain/models/TipoAusencia.java` — `motivoParaRecusarHoras`.
- `db/migration/V58__pedido_ausencia_em_horas.sql`.
- `colaboradores/application/services/ChefiaService.java` — chefia directa e equipa pelo Lugar-pai.
  `AssiduidadeService.picarPeloProprio` · `pedirCorrecao` · `pendentesDaEquipa` · `decidir`.
- `colaboradores/domain/service/ApuramentoFaltas.java` — tempo em falta por dia, débito da aferição,
  conversão do art. 13.º n.º 4. `colaboradores/application/services/ApuramentoFaltasService.java` —
  classifica os dias (feriado, pedido aprovado, licença, mobilidade externa, isenção...).
- `colaboradores/application/services/AssiduidadeService.java` — lançar (correcção com motivo),
  anular, importar (repetível), consultar (dia e semana, esperado do horário vigente).
- `colaboradores/domain/models/DiaAssiduidade.java` — emparelhar marcações, intervalos, anomalias.
- `colaboradores/domain/models/MarcacaoAssiduidade.java` — registar, anular (nunca apagar).
- `colaboradores/application/services/HorarioColaboradorService.java` — atribuir, histórico,
  horário vigente (colaborador → unidade → base → nenhum), alerta do tempo parcial.
- `colaboradores/application/services/UnidadeDeExercicioService.java` — unidade onde exerce funções
  e herança pela unidade-mãe; partilhado pelos feriados e pelos horários.
- `colaboradores/domain/models/HorarioColaborador.java` · `colaboradores/domain/models/RegimePrestacao.java`.
- `parametrizacoes/domain/models/Horario.java` — invariantes, horas calculadas, horário base.
- `parametrizacoes/infrastructure/mappers/HorarioMapper.java` — `HH:mm`, dia 1..7, enums.
- `estrutura/application/commands/HorarioDaUnidade.java` — validação do `horarioId` da unidade.
- `parametrizacoes/domain/models/ParametroFerias.java` · `parametrizacoes/application/services/ParametrosFeriasService.java`.
- `colaboradores/application/services/CalendarioFeriadosService.java` · `colaboradores/domain/service/DiasUteisCalculator.java`.
- `colaboradores/domain/models/FeriasDoAno.java` · `colaboradores/application/services/MapaFeriasService.java`.
- `db/migration/V55__feriados_recorrencia_e_area.sql` · `db/migration/V56__leave_type_contagem.sql`
  · `db/migration/V57__unidade_organica_horario.sql`.
- `db/seed/seed_parametrizacoes.sql` — feriados (§7), tipos de ausência (§5), parâmetros de férias (§9).
- `scripts/repor_estado.sql` · `scripts/testes_funcionais.ps1` · `scripts/testes_funcionais_README.md`
  · `scripts/verificar_handoff.py`.
- `docs/funcionarios/v5/regras_negocio.html` (BR-PH, BR-AUS-20..23, BR-FER-13..20, **BR-HOR-01..10**)
  · `docs/funcionarios/v5/api_guide.md` (§6.6, **§6.7**, §9.1, **§9.2**, **§9.3**)
  · `docs/funcionarios/v5/breaking_change_frontend.md` (§11.21–**11.25** + checklist)
  · `docs/funcionarios/v5/modelo_relacional.html` · `docs/funcionarios/v5/openapi.json`.

## How to verify / resume

```bash
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-26.0.2.10-hotspot"
cd /c/Users/ivanick.santos/Nick-personal/ta-workspace/projects/Recursos_Humanos
git status -sb && git log --oneline -4          # ahead 29 (+ o commit deste handoff)
docker start postgres-ingt-rh                  # antes dos testes

# Suite na copia isolada (o VS Code nao lhe toca):
S=<scratchpad>                                  # pasta temporaria da sessao
rm -rf "$S/rh" && mkdir -p "$S/rh" && tar --exclude=./target --exclude=./.git -cf - . | tar -xf - -C "$S/rh"
(cd "$S/rh" && mvn -B clean test)               # esperado: Tests run: 1001, Failures: 0, Errors: 0

# O handoff diz a verdade?
python scripts/verificar_handoff.py --relatorios "$S/rh/target/surefire-reports"   # esperado: FALHAS: 0
```

App (para regenerar o contrato ou provar endpoints) — **a partir da raiz**, porta 8099:

```bash
(cd "$S/rh" && mvn -B -q package -DskipTests)
jar tf "$S/rh/target/RH-Service-0.0.1-SNAPSHOT.jar" | grep classes/application.properties   # tem de aparecer
java -jar "$S/rh/target/RH-Service-0.0.1-SNAPSHOT.jar" --spring.profiles.active=development &
curl -s -o docs/funcionarios/v5/openapi.json http://localhost:8099/v3/api-docs
# parar: powershell ... Stop-Process sobre o java.exe com 'RH-Service' na linha de comando
docker cp scripts/repor_estado.sql postgres-ingt-rh:/tmp/repor.sql
docker exec postgres-ingt-rh sh -c "psql -U postgres -d recursoshumanos_db -q -f /tmp/repor.sql"
```

Seed de colaboradores: 0000001 Francisco (`91e1…e901`), 0000002 Maria (`91e1…e902`),
0000003 Joana (`91e1…e903`). Unidade da Maria `31e1…e303` (SERV_RH), mãe `31e1…e302`.
Tipos: LUTO `e1e1…e1e5`, SEMINARIO `…e1f2`, TE_PESQUISA `…e1f3`.

## Test / validation plan

Bateria: **553 passos, 553 OK** (repor → correr → repor → correr). Blocos novos F20 (feriados), F21
(contagem), F22 (mapa de férias), F23 (parâmetros de férias), F24 (horários) — ver
`scripts/testes_funcionais_README.md`. A função `Linhas` do script passou a devolver listas simples
tal como vêm (antes perdia os elementos). Para cada tijolo novo da assiduidade: testes unitários,
prova manual com a app, e um bloco F25+ na bateria.

## Open questions

- Limite de substituições por pessoa — RH. Promoção exige Lugar vago? — cliente.
- Jurídico: **período de soma do art. 13.º n.º 4** (decidido: mês); diploma de desenvolvimento da Lei 20/X/2023 (**horários, limites, turnos** — os
  horários ficaram sem limites por isso), diploma da mobilidade, e o período dos 6 dias do art. 77.º
  n.º 3.
- **Horários com versões?** — decidir antes do registo diário (ver Blockers).
- Jurídico, trabalho suplementar: isenção de horário exclui mesmo (art. 155.º n.º 2 b))? limites em
  horas por dia/ano, horário nocturno e compensação em descanso (diploma de desenvolvimento, art. 165.º
  n.º 4).

### Framework de jobs (por último)

Portar do `inss_core_service` (branch `dev-pre-release`, em
`C:\Users\ivanick.santos\Nosi-work\projects_nosi_workspace\projects\inss_core_service`): docs em
`docs/schedulers/README.md` desse repositório; código em `shared/application/services/scheduler`
(ScheduledJob, JobRunner, SchedulerService, SchedulerSweeper). Renumerar as migrações dele.
Ideias-chave: período vem de `agendadoPara`, parâmetros gravados na abertura, registo em
`REQUIRES_NEW`, retry só em FALHA/TIMEOUT. **Inclui o lock distribuído** (questão 13).

### Assiduidade (ponto 4)

Por tijolos: horários ✔ → **registo diário** (entrada, saída, intervalos; art. 164.º n.º 3 da Lei
20/X/2023 obriga) → horas por dia/semana → faltas por débito (art. 13.º n.º 2 DL 3/2010) e ausências
curtas que somam (n.º 4: < meio período = meio; > meio = um período) → trabalho suplementar
(registar e classificar, **não pagar**) → dispensa de amamentação (art. 172.º n.º 3 da Lei 20: duas
horas por dia, em dois períodos). Os limites vêm de diploma que não temos: **parametrizar sem
inventar omissões**. Meios-dias voltam à mesa aqui.

## Next step

Apresentar o **desenho da relação mensal do art. 75.º** (DL 3/2010), **só leitura, sem congelar nem
bloquear** (o fecho fica para quando houver integração salarial): por mês, por colaborador e agrupada
por unidade — faltas justificadas por tipo, licenças por subtipo, faltas por justificar (dias e
meios-dias, do apuramento), dias fora do vínculo, mobilidade e, agora, horas de trabalho suplementar
por tipo de dia. Reutilizar `ApuramentoFaltasService`, pedidos de ausência, licenças e
`TrabalhoSuplementarService`; sem tabelas novas. Antes: lei, indústria e **o que já existe**. **Sem
implementar** até o utilizador aprovar.
