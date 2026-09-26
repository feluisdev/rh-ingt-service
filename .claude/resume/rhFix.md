> Updated: 2026-09-26 16:00 (-01:00) — frentes RH (plano_frentes_rh.md) concluídas: F0.1 a F5.1

## Goal

Alinhar o núcleo RH (não-sigdi) com a legislação cabo-verdiana — DL n.º 3/2010 (férias, faltas,
licenças) e Lei n.º 20/X/2023 (função pública) —, ponto a ponto, com regras documentadas e testadas.
O que a lei fixa vive em código (enum); o que varia com a instituição, ou com um diploma novo, vive
em tabela editável pela API. Um ponto por commit. A aplicação vai ser apresentada ao cliente; ficam
fora o processamento salarial (outra aplicação, integração futura) e o SIGDI/avaliação de desempenho
(outro programador).

## Sessão 2026-09-25/26 — as frentes RH (lei e indústria)

Plano em `.claude/resume/plano_frentes_rh.md`, **todo implementado**, um commit por frente (22 commits, `17edc0eb`..`dc234bee`,
**por enviar** — o utilizador faz o push; merge para `master` = deploy):

| Frente | Commit | O quê |
|---|---|---|
| F0.1 notificações | b33fdc2d | `Notificador` (API fluente para qualquer módulo), caixa pessoal e do RH; SMTP = TODO |
| F0.2 diário de factos | 494f585e | `DiarioFactos` + evento `FactoRegistado`; contrato `/salarial/factos` (JSON/CSV, versão 1) |
| F0.3 dias especiais | 2e838088 | porta `DiasEspeciaisProvider` para o apuramento de faltas |
| F1.1 aposentação | 2b05cd74 | limite de idade, antecipada, pré-aposentação, processo, prorrogação, avisos |
| F1.2 declarações | 1856004f | PDF (Thymeleaf + openhtmltopdf, CSS 2.1) no MinIO, numeração, verificação pública |
| F1.3 lista de antiguidade | 092db4bc | aprovar, afixar, reclamações, recurso, definitiva, publicação |
| F1.4 publicações BO | 446d1154 | actos a publicar a partir dos factos, extracto em PDF |
| F1.5 cartão profissional | 6433b81e | emissão em PDF, entrega, validade |
| F2.2 entrada ao serviço | 6c7cb654 | provimento e posse, estágio probatório com tutor, período experimental |
| F2.1 concurso | fa692575 | **módulo novo `recrutamento`** |
| F2.3 checklists | 9f97b92b | entrada e saída, modelo semeado ao arrancar, marcação automática |
| F2.4 comissão de serviço | 23202463 | sobre o registo de mobilidade `MOB_COMISSAO` (sem tabela nova) |
| F3.1 disciplinar | 65848982, 6eeffc75 | tramitação com prazos (**V61**), execução da pena, recurso; autos sugeridos, suspensão da pena, reabilitação, revisão, impedimentos |
| F3.2 formação | e630e3ae | **módulo novo `formacao`**: plano, acções, inscrições, garantia |
| F3.3 missão de serviço | 847adda2 | dias de ajudas de custo (sem valores) para o salarial |
| F3.4 exoneração | cb44da1d | pré-aviso 60 dias, condicionantes, efeitos até 90 dias |
| F3.5 acumulação | 2d300541 | casos da lei, terço da docência, autorização |
| F4.1 acidentes | d35e2a7a | qualificação, incapacidades, invalidez, seguradora |
| F4.2 medicina do trabalho | 2030c6ca | exames de aptidão (sem dados clínicos), junta médica |
| F5.1 fronteira salarial | dc234bee | `remuneracaoBase` nos factos, fecho mensal, exportação versionada |

**Divisão salarial (decidida com o utilizador):** o RH parametriza o bruto base no escalão e é a fonte dos factos; a
integração calcula remunerações, suplementos, ajudas de custo em valor, descontos e líquido. O canal da exportação
(API pronta; ficheiro/fila) combina-se com a equipa do salarial.

**Estado final:** suite **1528 testes, só as 6 falhas conhecidas do sigdi** (não corrigir); `verificar_docs` FALHAS: 0;
`openapi.json` com 471 caminhos; migrações até **V61** (próxima **V62**, só para alterar tabelas existentes).
Tabelas novas pelo ddl-auto. Manifestos `.igrpstudio` de todas as frentes (`scripts/gerar_manifestos.py`).

**Feito depois (2026-09-26):** bloqueio das férias por pena (BR-DIS-30, `7d78fb65`); doença de 30 dias seguidos sugere
a junta (BR-SST-19, job `RH_DOENCA_PROLONGADA`); manual `apresentacao_aplicacao.html` com as partes K–O (22 diapositivos
novos, 81 no total) e a tabela completa dos jobs.

**Por fazer (conscientes):** processo individual completo; envio SMTP das notificações; canal da exportação salarial
(API pronta; ficheiro/fila a combinar com a equipa do salarial); permissões por perfil (adiadas).

**Armadilhas desta sessão:** `sync.sh` corta o `mvn test` pelo `head -80` — a suite completa corre com `mvn` directo
para log; o curl de testes precisa de `Accept: application/json` (os erros vêm em problem+xml); heredocs longos no
bash partem-se — usar ficheiros; os jobs só aceitam a data de referência se a declararem (`getParametros`).

## Current state

**Branch `fix-alinhamento-legislacao`**, **48 commits locais por enviar** (`origin_git_lab`).
**Não fazer push sem o utilizador pedir** — merge para `master` no GitLab é deploy.

- **Testes: 1135, 0 falhas** — correr na **cópia isolada** (ver Blockers).
- **Bateria funcional: 751 passos**, 751 OK (2026-09-24, duas execuções seguidas com reposição
  entre elas). Blocos F20–F37: feriados (V55), contagem (V56), mapa de férias, parâmetros de férias,
  horários, registo diário, faltas por débito, pedido em horas (V58), pedido pelo próprio em `/me`,
  registo pelo próprio com validação, trabalho suplementar, relação mensal, aprovação dos pedidos de ausência, horários com data de efeito, lista de antiguidade, preferência de férias pelo próprio e aviso fora da marcação, mapa de efectivos, indicadores do pessoal.
- **Dados de demonstração:** `scripts/dados_demonstracao.ps1` (guia em `scripts/dados_demonstracao_README.md`)
  carrega pela API, numa base reposta, 12 colaboradores fictícios em três serviços com chefias,
  marcações, ausências, férias, trabalho suplementar e uma promoção; o `repor_estado.sql` apaga-os.
- Migrações até **`V58`**. Próxima livre: **V59** (só para alterar tabelas existentes).
- **`openapi.json`**: 282 caminhos (regenerado com a app a correr).
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
| `128c816a` (24) | Mapa de férias: preferência pelo próprio (/me) e aviso de pedido fora da marcação |
| `3b66ca19` (24) | Relatórios: mapa de efectivos (Lei 20, art. 4.º al. aa)), por serviço, JSON e CSV |
| `c3d1a689` (24) | Relatórios: indicadores do pessoal (Lei 20, art. 38.º n.º 3), JSON |
| `ed873aee` (24) | Fix: os indicadores juntam «F»/«FEMININO» e «M»/«MASCULINO» |
| (este) (24) | Dados de demonstração (`scripts/dados_demonstracao.ps1`, pela API) |

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
  antiguidade**, **mapa de efectivos**, **indicadores do pessoal** (balanço social) — os três feitos. Só leitura,
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
  · `scripts/verificar_handoff.py` · `scripts/dados_demonstracao.ps1` · `scripts/dados_demonstracao.json`
  · `scripts/dados_demonstracao_README.md`.
- Relatórios (`colaboradores/`): `application/services/QuemEstaNoServico.java` · `ListaAntiguidadeService.java`
  · `MapaEfectivosService.java` · `IndicadoresPessoalService.java` · `interfaces/rest/RelatoriosController.java`.
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
bateria (o próximo é o **F38**) com positivos e negativos, e a bateria inteira duas vezes com
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

**Plano de fecho para a apresentação — concluído em 2026-09-24** (aprovado pelo utilizador em 2026-09-24: «vamos pôr tudo isso
nesse plano e fechar; você vai fazer tudo de uma vez» — um commit por ponto, sem push):

1. ✔ **Mapa de férias — preferência pelo próprio** (art. 5.º n.º 4): `PUT /me/ferias/{ano}/preferencia`,
   `GET /me/ferias/{ano}`, `GET /me/equipa/ferias/{ano}` (a chefia directa vê a equipa); fica quem
   indicou (PROPRIO/RH). **Aviso de pedido de férias fora da marcação** (art. 6.º n.º 2): alerta na
   criação (RH e /me), sem bloquear; `foraDaMarcacao` na caixa da chefia.
2. ✔ **Mapa de efectivos** (Lei 20, art. 4.º al. aa) e arts. 38.º–41.º): por unidade e cargo, os Lugares
   (activos, providos, vagos, congelados) e os efectivos; JSON e CSV em `/relatorios/`.
3. ✔ **Indicadores do pessoal** (Lei 20, art. 38.º n.º 3 — balanço social): efectivos por vínculo,
   género, escalão etário, carreira e unidade; entradas e saídas no ano; absentismo e horas extras.
   JSON (o front desenha).
4. ✔ **Dados de demonstração credíveis**: 12–15 colaboradores em 2–3 unidades com chefias (Lugar-pai),
   um mês de marcações com faltas e atrasos, férias, pedidos aprovados e pendentes, horas extras, uma
   promoção; o nome do SERV_RH corrigido na base (`ServiÃ§o`).

**Depois do fecho (fica em aberto, consciente):**
- Decisões do utilizador: **push** e deploy para um ambiente de demonstração (merge para `master` =
  deploy); perguntas da secção Open questions.
- Adiados pelo utilizador: **permissões**, **meios-dias de férias**, **notificações** (TODO),
  **erros em XML** (configuração global pronta), **formação e disciplinar** como processos, **cônjuges**
  no mapa de férias (art. 5.º n.º 6), o **ciclo** da lista de antiguidade (aprovar/afixar/reclamar/
  publicar), **tectos em horas**, **fecho mensal** (integração salarial), **`areaCkey`** (geografia),
  renomear endpoints/campos, **framework de jobs com lock distribuído**.
- Arrumação: a secção «Como correr» do `testes_funcionais_README.md` manda correr na árvore de
  trabalho e repor «com o SQL do fim deste ficheiro» (desactualizado), e o cabeçalho do
  `repor_estado.sql` diz que é gerado do README (já não é); o `CLAUDE.md` diz 8091 no Swagger e no
  `.env` (é 8099); o `breaking_change_frontend.md` tem um `### Checklist` vazio entre a §11.18 e a
  §11.19, os itens colados ao fim da §11.21, e as §11.21 em diante em ordem decrescente.

## Next step

O plano de fecho está feito. **Esperar pelo utilizador**: decide o push e o deploy para demonstração
(merge para `master` = deploy) e qual dos adiados (secção Plano em aberto) entra a seguir. Nada
se implementa sem essa escolha. Antes de desenhar o que vier: lei, indústria e **o que já
existe**; aprovação do desenho antes de implementar; um commit por ponto.
