> Updated: 2026-09-24 11:30 (-01:00) — sessão de 2026-09-24 (lista de antiguidade)

## Goal

Alinhar o núcleo RH (não-sigdi) com a legislação cabo-verdiana — DL n.º 3/2010 (férias, faltas,
licenças) e Lei n.º 20/X/2023 (função pública) —, ponto a ponto, com regras documentadas e testadas.
O que a lei fixa vive em código (enum); o que varia com a instituição, ou com um diploma novo, vive
em tabela editável pela API. Um ponto por commit. A aplicação vai ser apresentada ao cliente; ficam
fora o processamento salarial (outra aplicação, integração futura) e o SIGDI/avaliação de desempenho
(outro programador).

## Current state

**Branch `fix-alinhamento-legislacao`**, **43 commits locais por enviar** (`origin_git_lab`).
**Não fazer push sem o utilizador pedir** — merge para `master` no GitLab é deploy.

- **Testes: 1123, 0 falhas** — correr na **cópia isolada** (ver Blockers).
- **Bateria funcional: 724 passos**, 724 OK (2026-09-24, duas execuções seguidas com reposição
  entre elas). Blocos F20–F34: feriados (V55), contagem (V56), mapa de férias, parâmetros de férias,
  horários, registo diário, faltas por débito, pedido em horas (V58), pedido pelo próprio em `/me`,
  registo pelo próprio com validação, trabalho suplementar, relação mensal, aprovação dos pedidos de ausência, horários com data de efeito, lista de antiguidade.
- Migrações até **`V58`**. Próxima livre: **V59** (só para alterar tabelas existentes).
- **`openapi.json`**: 276 caminhos (regenerado com a app a correr).
- **Sete jobs `@Scheduled`**, sem lock distribuído (fica para o framework de jobs).
- `.claude/settings.json` e `.claude/settings.local.json` estão **versionados, com alterações locais
  que não entram em nenhum commit** (não são desta tarefa: nunca `git add -A`). Fora do git ficam os
  textos das leis na raiz — `.lei20.txt` (Lei 20/X/2023), `.dl3.txt` (DL 3/2010, numa só linha), `.estrutura.txt`.
  **Ler o articulado antes de desenhar.**

**Commits de 2026-09-23 (o dia desta tarefa), por ordem:**

| Commit | O quê |
|---|---|
| `447f5273` | **`V55`** — feriados recorrentes, com área, e todos contam |
| `dc1bbf66` | **`V56`** — `t_leave_type.contagem` ∈ DIAS_UTEIS · DIAS_SEGUIDOS (art. 76.º) |
| `6114faa8` | Mapa de férias (arts. 5.º e 6.º) — `FeriasDoAno`, `MapaFerias`, tabelas `t_ferias_*` |
| `2b9a3d00` | Parâmetros do mapa de férias em `t_parametro_ferias`, por vigência |
| `c60aaece` | `RegimeTrabalho` cita a Lei 20/X/2023 (não a lei portuguesa) |
| `3386b1fc` | Horários — catálogo, horário base, **`V57`** (horário da unidade), horário do colaborador |
| `81491475` | Registo diário — marcações, importação de relógio, consulta (art. 164.º n.º 3) |
| `d1690c2d` | Faltas por débito — apuramento mensal (art. 13.º) |
| `680d912d` | Pedido de ausência em horas e dispensa de amamentação (V58) |
| `f094668e` | Fix: pedido de ausência em `/me` segue as regras do RH |
| `14bedfb1` | Registo pelo próprio e validação da chefia directa |
| `f9760dfe` | Trabalho suplementar (art. 155.º n.º 2 a)) — autorização, horas pelas marcações |
| `01a615cf` | Relação mensal do art. 75.º (JSON e CSV); dias depois da cessação fora do vínculo |
| `f60d0b0c` (24) | Pedidos de ausência: aprovação automática dos tipos sem aprovação; decisão pela chefia directa |
| `d9703e0c` (24) | Unidade onde exerce funções pela afectação da data (feriados e horário da unidade) |
| `36959801` (24) | Horários com data de efeito (base e unidade com histórico; horário que vigorou é imutável; duplicar) |
| `296c90a8` (24) | Relatórios: lista de antiguidade anual (DL 3/2010, arts. 69.º e 70.º), JSON e CSV |

Mais os commits `docs` do handoff. **Plano geral:** 1 feriados ✔ · 2 dispensas ✔ · 3 mapa de férias ✔
· 4 assiduidade ✔ (horários → registo → faltas → horas → próprio → suplementar → relação). O que
falta está em **Plano em aberto**.

## Decisions made — do not re-litigate

**Tomadas com o utilizador em 2026-09-23:**
- **Regra de trabalho**: antes de desenhar, **lei + indústria + o que já existe** — e só depois
  propor; **tabela nova só para conceito com ciclo de vida próprio** (o utilizador recusou
  `t_dispensa_amamentacao` + `t_parametro_assiduidade`: «dispensa amamentação tem uma tabela própria??»).
- **Parâmetros do mapa de férias em tabela, não em properties**: `t_parametro_ferias`, **global**,
  com `vigente_desde` (uma linha por ano, vale até à seguinte). Sem linha, valem os da lei (`origem: LEI`).
- **`areaCkey` é provisória**: passa para uma tabela de geografia própria quando existir. Não
  investir em validá-la contra o `Option`.
- **`HIBERNATE_DDL=update` em produção** («é a primeira versão»): as tabelas sem migração nascem no
  arranque. A regra «entidade nova sem migração» mantém-se.
- **Lock dos jobs** resolve-se no framework de jobs, não antes.
- **Horários:** modalidade = nome livre (o código só conhece `controlo` ∈ FIXO · FLEXIVEL); limites
  legais não se validam (esperam o diploma de desenvolvimento, arts. 164.º n.º 4 e 165.º); o horário da
  unidade é **opcional** e herda-se da unidade-mãe; **horário base** da instituição (no máximo um,
  `PATCH /catalogs/horarios/{id}/base`, não se desactiva); **regime de prestação** PRESENCIAL ·
  TELETRABALHO · MISTO no horário do colaborador (art. 166.º); vigente = colaborador → unidade/mãe →
  base → nenhum; no flexível `duracaoDiaria` é obrigatória. **Horários sem versões** (decidido: o
  esperado recalcula-se; só o fecho mensal o congelaria).
- **Registo diário:** marcações que **nunca se apagam** (correcção = marcação nova com motivo; a
  errada anula-se com motivo e fica visível); o dia calcula-se das válidas, o que não emparelha é
  anomalia; um só modelo com `origem` (relógio repetível pela `referenciaExterna`, RH, próprio).
- **Faltas por débito:** calculam-se, não se guardam; fixo = blocos não cobertos; flexível =
  plataformas + débito da aferição sem dupla contagem; dia sem marcações = SEM_REGISTO; anomalia =
  POR_CORRIGIR; isenção fora; **art. 13.º n.º 4: o parcial soma-se no mês** e converte-se pela média do
  esperado (por confirmar com o jurídico, não bloqueia). Dias depois da cessação = FORA_DO_VINCULO.
- **Pedido em horas e amamentação:** sem tabelas novas — `horaInicio`/`horaFim` no pedido de ausência
  (V58), valem em cada dia; amamentação é o tipo `DISPENSA_AMAMENTACAO` (120 min/dia, 183 dias; Lei 20
  prevalece sobre o DL). Só tipos sem saldo, regime FALTA, sem tectos. Fim antecipado `PATCH …/terminar`.
- **`/me` pedido de ausência = regras do RH** (delega no handler do RH; sobreposição 409).
- **Registo pelo próprio:** picagem em `/me` **só em TELETRABALHO/MISTO** (art. 170.º); correcção
  PENDENTE validada pela **chefia directa** (titular do Lugar-pai) **ou pelo RH**; ninguém valida as
  suas; só VALIDA conta; dia com pendentes = POR_VALIDAR.
- **Sem integração com o processamento salarial por agora** → **fecho mensal adiado** (congelar,
  bloquear, reabrir só servem quando alguém recebe o número). Quando vier, perguntar à outra equipa:
  canal (API, ficheiro, Kafka), detalhe, identificador do colaborador, prazo no mês.
- **Meios-dias de férias**: direito do funcionário (DL 3/2010 art. 2.º n.º 6, até 5 por ano) — em
  falta face à lei, pode esperar. **Tectos em horas**: só se o cliente os tiver.
- **Trabalho suplementar:** tabela própria `t_trabalho_suplementar` (ciclo PEDIDO → AUTORIZADO ·
  RECUSADO → CANCELADO); o pedido de ausência foi rejeitado por ter o significado oposto. Só horas,
  sem valores. RH e chefia directa lançam (autorizado), o próprio pede; realizado **só pelas marcações**;
  autorização posterior só chefia/RH, assinalada; isentos não fazem (art. 155.º n.º 2 b), a confirmar);
  num dia útil fora dos blocos; no apuramento sai do tempo normal (`minutosSuplementares`).
- **Relação mensal:** só leitura, sem tabelas, JSON e CSV (`;`, UTF-8 com BOM, decimais com
  vírgula); **por unidade orgânica com subunidades** (unidadeId obrigatório); entram **activos e quem
  cessou no mês ou depois**, pelas **afectações que cobrem o mês**; uma pessoa, uma unidade.
- **Pedidos de ausência (2026-09-24):** os tipos com `requires_approval = false` (os direitos do
  art. 15.º: luto, casamento, doença…) **nascem APROVADOS**, sem decisor (`aprovacaoAutomatica`); se a
  prova faltar, o RH cancela ou regista injustificada. Os outros decide **a chefia directa ou o RH, num
  só nível** (caixa `/me/equipa/pedidos-ausencia…`, rejeitar pela chefia exige motivo). Corrigido: o RH
  aprovava/rejeitava pedidos de outro colaborador pelo caminho (agora 404).
- **Horários com data de efeito (2026-09-24):** o horário da unidade (`t_unidade_organica_horario`) e o
  base (`t_horario_base`) têm histórico; mudam de hoje ou de uma data futura (`horarioDesde` no PUT da
  unidade; `?desde=` no PATCH do base); data passada 422. **Um horário que já vigorou é imutável** (só o
  nome; 409) — duplica-se (`POST …/duplicar`). Sem migração: sem histórico vale a coluna/marca antiga, e
  a primeira mudança regista-a «desde sempre»; **o primeiro base da instituição vale desde sempre**. O
  «já vigorou?» vai por `HorarioUtilizacaoPort` (parametrizacoes), implementada em colaboradores,
  estrutura e no próprio base. Corrigido pelo caminho: listagem de pedidos com 500 intermitente
  (`upper(bytea)`) e `workerStateId` nulo com 500.
- **Relatórios de gestão (2026-09-24):** os três, por esta ordem, um commit cada — **lista de
  antiguidade** (feita), **mapa de efectivos**, **indicadores do pessoal** (balanço social). Só leitura,
  por unidade com subunidades, JSON e CSV, em `/relatorios/…`. Da lista, **só gerar**: o ciclo
  (aprovar, afixar, reclamações, publicar até 30/4) fica para depois. «Cargo» = carreira/categoria pelo
  escalão da afectação (fora da grelha, o Job); início no cargo = cadeia seguida na categoria;
  `QuemEstaNoServico` partilhado com a relação mensal.
- **Adiados pelo utilizador (2026-09-24):** permissões por perfil e meios-dias de férias («ainda não»);
  **notificações = TODO** (esperam uma app de notificações; ficam `TODO(notificacoes)` no código).
  Nomes de endpoints e campos (inglês/português) **não se mudam agora** — só o funcionamento importa.
- **Mapa de férias — lacunas assinaladas, não adivinhadas:** cônjuges no mesmo serviço (art. 5.º
  n.º 6), preferência pelo próprio em `/me`, aviso de pedido fora da marcação.

**Anteriores (resumo; o detalhe está no git e em `regras_negocio.html`):**
- **Não partir o front.** Campo novo em recurso existente: opcional, sem 422 contra catálogo, e o
  `PUT` **não apaga** o que vem omisso (nulo = manter; `""` = limpar; `false` = desmarcar).
- **Entidade nova não leva migração** — o `ddl-auto` cria a tabela e a `_aud`. Flyway só para
  alterar tabelas existentes. As invariantes que seriam `CHECK` ficam no domínio.
- Calendário de feriados é da instituição; a pessoa entra pela área da unidade onde exerce funções.
  Contagem (art. 76.º): dias seguidos salvo onde a lei diz úteis. Mapa: marcar ≠ gozar.
- Ausências ≠ licenças; mobilidade não toca na afectação; uma cadeira, um titular; férias: saldo
  nasce sozinho, acumulação é acto com motivo; antiguidade derivada; falta injustificada desconta.
- **Fora do âmbito por decisão:** permuta, estágio probatório, reintegração judicial. Não reabrir.
- Parametrizar valores, não decisões. Motivos não se validam. Nada de filtrar em memória. Nada de
  respostas `Map`. O que é derivável não se guarda.

## Constraints

- **JDK 26**: `export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-26.0.2.10-hotspot"`.
- Docs em **pt-PT**; commits `feat|fix|docs(<âmbito>): …` com trailer `Co-Authored-By`; o handoff
  (e o `scripts/verificar_handoff.py`, quando muda) vai em `docs(handoff): …` — os commits antigos de
  handoff usavam `docs:` sem âmbito.
- O `scripts/verificar_handoff.py` também apanha o que envelhece fora do Current state (números
  repetidos, frases que anunciam o que falta fora do Next step, data mais antiga que o código, bloco seguinte da
  bateria, secção Plano em aberto). Correr antes de cada commit do handoff.
- **Pedir autorização antes de cada commit e antes de implementar cada ponto** — o utilizador
  aprova o desenho primeiro, e prefere «aos poucos».
- Migração (alterar tabela existente): defensiva, idempotente, guarda `to_regclass` **aninhado**,
  coluna sem DEFAULT → preencher → NOT NULL → DEFAULT. Provar numa base vazia (2×) e numa cópia
  (`pg_dump`) 3×, **antes** do Java.
- Controladores em `interfaces/rest/` são gerados: mudar a par o manifesto `.igrpstudio/`.
  `@ApiResponse` com `@Content` precisa de `schema` explícito.
- JPQL usa o nome `@Entity` (prefixo `Colabs` em `colaboradores`); `year()/month()/day()`.
- `scripts/*.ps1` e `repor_estado.sql` **só ASCII**. Regenerar `openapi.json` quando um endpoint muda.
- O seed usa `ON CONFLICT DO NOTHING`: numa base existente, o que muda vai no `scripts/repor_estado.sql`,
  com guarda `to_regclass` para as tabelas do ddl-auto.
- No bash deste ambiente, **heredocs com texto longo partem** («unexpected EOF») — escrever
  ficheiros com a ferramenta Write (ou scripts Python no scratchpad); em `docker exec … -f /tmp/x`
  usar `sh -c "…"`; Python no Windows: `PYTHONIOENCODING=utf-8` ou `encoding="utf-8"`.
- No PowerShell 5.1, `Invoke-WebRequest` devolve `text/csv` já como texto (não bytes).

## Blockers & risks

- **O servidor Java do VS Code escreve em `target/` a meio do Maven** — correr Maven numa cópia
  isolada (ver How to verify). Não é regressão.
- **A app lê o `.env` da pasta de onde arranca**: arrancar sempre com o terminal na raiz do projecto
  (a porta real é a do `.env`, **8099**).
- Docker Desktop pode estar em baixo ao retomar: `postgres-ingt-rh` tem de estar de pé **antes**
  dos testes (o `contextLoads` corre o Flyway na base real, porta 5436).
- A bateria usa **anos futuros** nos blocos de férias e, no fim do F19, só a Maria tem Lugar; nos
  blocos de assiduidade, datas «de hoje» podem cair numa mobilidade externa de um dia da Maria (F8) —
  por isso usam hoje+N ou dias passados.
- A unidade onde a pessoa exerce funções é a da **afectação principal que cobria a data** (desde
  2026-09-24; sem nenhuma, a actual). Os feriados de um período usam a unidade do **primeiro dia**: um
  pedido que atravesse uma mudança de unidade usa a área antiga no período todo (raro).
- A `areaCkey` não validada: um ckey mal escrito faz o feriado não contar para ninguém, sem erro.
- `FeriasDoAno` e `Horario` persistem as tabelas filhas por `clear()`+reinserção (orphanRemoval).
- A validação pela **chefia directa** só está provada em testes unitários: o seed não tem Lugares
  com Lugar-pai (ver Plano em aberto, item 1).

## Relevant files

Caminhos Java relativos a `src/main/java/cv/igrp/RH_Service/`; `db/...` relativo a `src/main/resources/`.

- Assiduidade (`colaboradores/`):
  - `application/services/HorarioColaboradorService.java` — horário vigente; `UnidadeDeExercicioService.java`.
  - `domain/models/MarcacaoAssiduidade.java` · `domain/models/DiaAssiduidade.java` ·
    `application/services/AssiduidadeService.java` — registo, importação, consulta, próprio, decisão.
  - `application/services/ChefiaService.java` — chefia directa e equipa pelo Lugar-pai.
  - `domain/service/ApuramentoFaltas.java` · `application/services/ApuramentoFaltasService.java` —
    faltas por débito, conversão do art. 13.º n.º 4, `fimDoVinculo`.
  - `domain/models/TrabalhoSuplementar.java` · `application/services/TrabalhoSuplementarService.java`.
  - `application/services/RelacaoMensalService.java` ·
    `application/queries/GetRelacaoMensalCsvQueryHandler.java`.
  - Controladores: `AssiduidadeController`, `HorarioColaboradorController`,
    `TrabalhoSuplementarController`, `RelacaoMensalController`, `MeController`.
- Ausências: `application/commands/CreatePedidoAusenciaCommandHandler.java` (`criarEmHoras`) ·
  `domain/models/PedidoAusencia.java` · `domain/models/TipoAusencia.java`.
- Parametrizações: `parametrizacoes/domain/models/Horario.java` ·
  `parametrizacoes/domain/models/ParametroFerias.java`.
- Férias: `colaboradores/domain/models/FeriasDoAno.java` · `colaboradores/application/services/MapaFeriasService.java`
  · `colaboradores/application/services/CalendarioFeriadosService.java` · `colaboradores/domain/service/DiasUteisCalculator.java`.
- `db/migration/V55__feriados_recorrencia_e_area.sql` · `db/migration/V56__leave_type_contagem.sql`
  · `db/migration/V57__unidade_organica_horario.sql` · `db/migration/V58__pedido_ausencia_em_horas.sql`.
- `db/seed/seed_parametrizacoes.sql` — feriados, tipos de ausência, parâmetros de férias.
- `scripts/repor_estado.sql` · `scripts/testes_funcionais.ps1` · `scripts/testes_funcionais_README.md`
  · `scripts/verificar_handoff.py`.
- `docs/funcionarios/v5/regras_negocio.html` (BR-HOR, BR-ASS, BR-FAL, BR-AUS-24..28, BR-SUP, BR-REL, BR-ME)
  · `docs/funcionarios/v5/api_guide.md` (§6.7 a §6.11, §9.1 a §9.3)
  · `docs/funcionarios/v5/breaking_change_frontend.md` (§11.21 a §11.32 + checklist)
  · `docs/funcionarios/v5/modelo_relacional.html` · `docs/funcionarios/v5/openapi.json`.

## How to verify / resume

Com o terminal na raiz do projecto (o `tar` copia a pasta actual). `S` é a pasta temporária da
sessão (o scratchpad que o sistema indica). Os números esperados são os do **Current state**.

```bash
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-26.0.2.10-hotspot"
git status -sb && git log --oneline -4          # comparar com o Current state
docker start postgres-ingt-rh                  # antes dos testes

# Suite na copia isolada (o VS Code nao lhe toca):
S="<scratchpad da sessao>"
rm -rf "$S/rh" && mkdir -p "$S/rh" && tar --exclude=./target --exclude=./.git -cf - . | tar -xf - -C "$S/rh"
(cd "$S/rh" && mvn -B clean test)               # Tests run = o do Current state, 0 falhas

# O handoff diz a verdade? (le os relatorios da copia)
PYTHONIOENCODING=utf-8 python scripts/verificar_handoff.py --relatorios "$S/rh/target/surefire-reports"   # FALHAS: 0
```

App e bateria (porta 8099; o jar da cópia, arrancado a partir da raiz):

```bash
(cd "$S/rh" && mvn -B -q package -DskipTests)
jar tf "$S/rh/target/RH-Service-0.0.1-SNAPSHOT.jar" | grep classes/application.properties   # tem de aparecer
java -jar "$S/rh/target/RH-Service-0.0.1-SNAPSHOT.jar" --spring.profiles.active=development > "$S/app.log" 2>&1 &
until grep -q "Started .* in" "$S/app.log"; do sleep 2; done   # esperar o arranque (senao o curl grava lixo)
curl -s -o docs/funcionarios/v5/openapi.json http://localhost:8099/v3/api-docs   # se um endpoint mudou
# repor -> correr -> repor -> correr; as duas execucoes verdes:
docker cp scripts/repor_estado.sql postgres-ingt-rh:/tmp/repor.sql
docker exec postgres-ingt-rh sh -c "psql -U postgres -d recursoshumanos_db -q -f /tmp/repor.sql"
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/testes_funcionais.ps1
# parar: Stop-Process sobre o java.exe com 'RH-Service' na linha de comando
```

Seed (ids completos no padrão `xxxxxxxx-e1e1-e1e1-e1e1-e1e1e1e1eNNN`, ver os ficheiros do seed):
colaboradores 0000001 Francisco `91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901`, 0000002 Maria `…e902`,
0000003 Joana `…e903`; unidades MIN_FIN `31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e301` → DGP `…e302` →
SERV_RH `…e303` (a da Maria); tipos LUTO `e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e5`, SEMINARIO `…e1f2`,
TE_PESQUISA `…e1f3`.

## Test / validation plan

Para cada ponto novo: testes unitários (domínio e serviço), prova com a app, um bloco novo na
bateria (o próximo é o **F35**) com positivos e negativos, e a bateria inteira duas vezes com
reposição entre elas. Os blocos e as armadilhas pagas estão no `scripts/testes_funcionais_README.md`.

## Open questions

- RH: limite de substituições por pessoa. Cliente: promoção exige Lugar vago?
- Jurídico:
  - período de soma do art. 13.º n.º 4 (decidido: mês — confirmar);
  - diploma de desenvolvimento da Lei 20/X/2023: horários, limites, turnos (os horários ficaram sem
    limites por isso) e, no trabalho suplementar, limites em horas, horário nocturno e compensação em
    descanso (art. 165.º n.º 4);
  - isenção de horário exclui o trabalho suplementar (art. 155.º n.º 2 b))?
  - duração da amamentação (6 meses vem do DL; a Lei 20 fixa só as 2h/dia);
  - diploma da mobilidade; período dos 6 dias do art. 77.º n.º 3.

### Framework de jobs (por último)

Portar do `inss_core_service` (branch `dev-pre-release`, em
`C:\Users\ivanick.santos\Nosi-work\projects_nosi_workspace\projects\inss_core_service`): docs em
`docs/schedulers/README.md` desse repositório; código em `shared/application/services/scheduler`
(ScheduledJob, JobRunner, SchedulerService, SchedulerSweeper). Renumerar as migrações dele.
Ideias-chave: período vem de `agendadoPara`, parâmetros gravados na abertura, registo em
`REQUIRES_NEW`, retry só em FALHA/TIMEOUT. **Inclui o lock distribuído** dos sete jobs.

## Plano em aberto

Aprovado pelo utilizador em 2026-09-24, por esta ordem:

1. **Relatórios de gestão** — lista de antiguidade ✔; a seguir o **mapa de efectivos** (Lei 20, art. 4.º
   al. aa) e arts. 38.º–41.º: funções e postos de trabalho que o serviço tem, face ao quadro), depois os
   **indicadores do pessoal** (art. 38.º n.º 3). Desenhar cada um antes de implementar.

Depois, ou a decidir:
2. **Formação e disciplinar** como processos com fluxo (hoje são registos; a pena é texto livre) —
   perguntar ao utilizador o que quer (ficou sem resposta).
3. **Erros em XML**: com `jackson-dataformat-xml` no classpath, um `ProblemDetail` sai em
   `application/problem+xml` a quem não pede nada, pede `*/*` ou é um navegador (provado com a app);
   os sucessos saem em JSON. Solução pronta: uma configuração global que põe o conversor XML no fim.
   O utilizador disse «depois vemos» (não remover a dependência; não pôr `produces` em 49 controladores).
4. **Dados de demonstração credíveis** — marcações para os três colaboradores, **hierarquia de
   chefias** (Lugares com Lugar-pai: é o que falta para mostrar a caixa da chefia), e o nome da unidade
   SERV_RH duplamente codificado na **base** (`ServiÃ§o`; o ficheiro do seed está certo).
5. **Mapa de férias** — cônjuges (art. 5.º n.º 6), preferência pelo próprio no `/me`, aviso de pedido
   fora da marcação.
6. **Framework de jobs com lock distribuído** (por último; ver secção própria).
7. Adiados/só a pedido: **permissões**, **meios-dias de férias**, **notificações** (TODO), **tectos em
   horas**, **fecho mensal** (integração salarial), **`areaCkey`** (tabela de geografia), renomear
   endpoints/campos.
8. Arrumação: a secção «Como correr» do `testes_funcionais_README.md` manda correr na árvore de
    trabalho e repor «com o SQL do fim deste ficheiro» (desactualizado), e o cabeçalho do
    `repor_estado.sql` diz que é gerado do README (já não é); o `CLAUDE.md` diz 8091 no Swagger e no
    `.env` (é 8099); o `breaking_change_frontend.md` tem um `### Checklist` vazio entre a §11.18 e a
    §11.19, os itens colados ao fim da §11.21, e as §11.21–11.33 em ordem decrescente.
9. Decisões do utilizador: **push** (merge para `master` = deploy); perguntas da secção Open questions.

## Next step

Seguir o **Plano em aberto**: item 1, o **mapa de efectivos** — apresentar o desenho (lei, indústria,
o que já existe: Lugares, provimento, vagas) e pedir aprovação antes de implementar. Antes de desenhar: lei, indústria
e **o que já existe**; aprovação do desenho antes de implementar; um commit por ponto.
