> Updated: 2026-09-22 (terceira sessão do dia)
>
> **Lê primeiro:** «Current state» → «Next step» → «Constraints». O resto é
> referência para quando lá chegares.

## Goal

Alinhar o núcleo RH (não-sigdi) com a legislação cabo-verdiana, movimento a
movimento, com regras documentadas, testadas **e exercitadas contra a base de
dados**. O objectivo de fundo é a aplicação ser instalável em qualquer
instituição: o que a lei fixa vive em código; o que varia com a instituição ou
com o motivo vive em catálogo.

Construção **tijolo a tijolo**: um ponto por commit, com testes e documentação a
par, sem avançar enquanto o anterior não estiver verde.

## Current state

**Branch `fix-alinhamento-legislacao`**, com **21 commits locais por enviar**
(`71103f99..HEAD`, todos de 2026-09-22). O GitLab é o repo da equipa; merge para
`master` é deploy. **O push não foi feito por indicação expressa do utilizador:
não o fazer sem lhe perguntar.**

> **Regra de trabalho em vigor (2026-09-22, terceira sessão):** a implementação
> **só começa quando o utilizador autorizar**. Há um plano aprovado — ver «Next
> step» — mas nada se implementa sem o «podes avançar». A **bateria funcional só
> se corre no fim**, depois de tudo implementado; durante o caminho basta
> `mvn clean test` e a documentação a par.

- **Testes: 901, 0 falhas — mas só com a base de dados de pé.** Todos menos um
  são unitários puros; o `RecursosHumanosApplicationTests.contextLoads` carrega o
  contexto Spring completo e o Flyway liga-se ao Postgres. **Sem o contentor a
  correr dá 1 erro, e não é regressão.** Correr **sempre com `clean`** (ver Blockers).
- **Bateria funcional: 457 passos, 457 OK**, cobre **F0 a F19**. Nada commitado sem
  prova na bateria.
- **Migrações V40 a V54** aplicadas e verificadas na BD. Próxima livre: **V55**.
  **As quinze da V40 à V54 foram auditadas a 2026-09-22** contra uma base vazia e em
  repetição sobre uma cópia da base real; a V51 e a V53 falhavam na base vazia e
  foram corrigidas. Ver «Migrações» em Constraints.
- **`openapi.json`**: 231 caminhos, 250 esquemas, **0 operações não-sigdi sem
  esquema de resposta**. Regenerado a 2026-09-22; **regenerar sempre** que se
  mexa num endpoint.
- **O texto da Lei n.º 20/X/2023 está agora em `.lei20.txt`** (raiz, não
  versionado), extraído do Boletim Oficial n.º 30 de 24-03-2023 que o utilizador
  forneceu. **Usá-lo**: os três movimentos desta sessão mudaram de desenho depois
  de o ler. O `.dl3.txt` é o DL n.º 3/2010 (férias, faltas e licenças).
- **Estado da máquina quando esta sessão acabou:** contentor
  `postgres-ingt-rh` **de pé**; aplicação **parada** (foi-o para o último
  `mvn clean test` — o `clean` falha com o jar preso). A árvore de trabalho está
  limpa tirando `.claude/settings*.json` (que não são desta sessão) e os três
  `.txt` de leis, não versionados.
- **Sete jobs `@Scheduled`**: cinco do `sigdi`, mais o dos efeitos das licenças
  (`rh.licencas.efeitos.cron`, 00:15) e o do vencimento de férias
  (`rh.ferias.vencimento.cron`, 00:05). Nenhum tem lock distribuído — ver
  questão 13.
- **Nenhum handler ou controlador não-sigdi devolve `Map`.**

### O que a terceira sessão de 2026-09-22 fez (7 commits, `3bb58fb0..HEAD`)

| Commit | O que fecha |
|---|---|
| `b2777f6f` | **F17** — a bateria passa a provar as duas saídas da comissão (art. 64.º n.º 2), que era o único commit sem prova funcional |
| `8eeaed48` | O `repor_estado` apaga os tipos de ausência de teste (havia quinze acumulados no catálogo) |
| `a2689e87` | **V53 — o limite de dias tem três naturezas**: ano, ocorrência e mês (art. 15.º n.º 1) |
| `d4d7d8e9` | Handoff a par da V53 |
| `f6be1353` | **As sete modalidades de licença do art. 45.º** no seed — faltavam duas; fecha a questão 8 |
| `7fa3213a` | **Auditoria às migrações V40–V53**: o guarda do `to_regclass` tem de ser aninhado. V51 e V53 corrigidas |
| `2d86f5ac` | **V54 — a falta injustificada não conta** (art. 43.º n.º 2) **e o efeito na remuneração fica dito** (art. 16.º) |

### O que a segunda sessão de 2026-09-22 fez (5 commits, `1f2199a3..3bb58fb0`)

| Commit | O que fecha |
|---|---|
| `161b9877` | **Mudança de carreira** — `POST /funcionarios/{id}/mudanca-carreira`. Era impossível: a promoção exige a mesma carreira e a transferência a mesma categoria |
| `a65870fd` | **A acumulação não é um título de ocupar um Lugar** — sai do `TipoAfectacao`; `POST /assignments` passa a aceitar só `PRINCIPAL` |
| `9d0d7ea1` | **V52 — forma de prestação da mobilidade** (art. 134.º n.º 2): `TEMPO_INTEIRO` ou `ACUMULACAO`, no registo da mobilidade |
| `c6cf01ed` | **Consolidação da mobilidade** (art. 132.º n.º 4) — a transitória torna-se definitiva num Lugar vago do destino |
| `3bb58fb0` | **Regresso de comissão** (art. 64.º n.º 2): regressa quem tinha situação anterior; **cessa** quem não tinha |

**O utilizador forneceu o Boletim Oficial**, e isso mudou dois dos três desenhos.
Ver «O que a lei disse, e que contrariava o plano» abaixo.

### O que a primeira sessão de 2026-09-22 fez (8 commits, `71103f99..1f2199a3`)

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
   - **marcação** — **no plano aprovado, ponto 3.** Ver «Marcação de férias» abaixo:
     a lei dá o desenho todo e já não há decisões pendentes;
   - **suspensão automática das férias** por maternidade, paternidade, adopção ou
     doença (art. 8.º n.os 1 a 3) — a suspensão **manual** está feita (V51), mas
     hoje um pedido de férias e um de doença **não se falam**: ninguém suspende as
     férias sozinho quando entra uma doença que as atravessa;
   - **meios-dias** (art. 2.º n.º 6: até 5 meios-dias). O `numeroDias` é inteiro, o
     mesmo limite que já impede exprimir o art. 13.º n.º 4. **Adiado por decisão do
     utilizador**, e volta a ser possível quando a assiduidade entrar;
   - **compensação na cessação** (art. 12.º) — depende de remuneração, que não
     existe nesta aplicação;
   - **férias proporcionais depois de uma licença** (art. 47.º n.os 2 e 3): quem
     esteve de licença sem vencimento tem direito, no ano seguinte, a férias
     proporcionais ao tempo de serviço prestado. A **antiguidade** já lê o n.º 1
     desse artigo (a licença desconta), mas o vencimento da V49 só proporciona no
     **ano de ingresso** (art. 3.º) — este caso não está coberto. Fica assinalado
     para não se perder.
2. **Antiguidade / tempo de serviço** — **FEITA (2026-09-22)**, sem migração:
   é derivada, não guardada. Ver secção própria abaixo.
   **Sobra uma coluna morta:** `affects_pay` (subtipo) continua sem consumidor —
   é remuneração, e remuneração não existe nesta aplicação.
3. **Movimentos menores** — eram sete. **Quatro feitos** a 2026-09-22 (segunda
   sessão), **três fora do âmbito por decisão do utilizador**.

   **Feitos:** **mudança de carreira** · **acumulação** (que afinal é uma forma de
   prestar a mobilidade, não um título — ver abaixo) · **consolidação da
   mobilidade** (art. 132.º n.º 4) · **regresso de comissão** (art. 64.º n.º 2).

   **Por fazer:** **permuta** (troca atómica entre dois titulares) · **estágio
   probatório** (art. 57.º do texto lido: nomeação provisória por tempo determinado
   para frequência de estágio; o n.º 2 manda que quem já está nomeado
   definitivamente noutra carreira faça o estágio **em comissão de serviço**, o que
   liga este ponto ao anterior; o n.º 3 conta o tempo, o n.º 5 devolve-o à carreira
   de origem quando corre mal) · **reintegração judicial** (art. 97.º n.º 10 al. b).

   **O utilizador decidiu (2026-09-22) que estes três ficam por implementar** e que
   se segue para outras áreas do RH — ausências, assiduidade e os subtipos de
   mobilidade/licença. **Não os reabrir** sem ele os reabrir.
4. **Percurso do colaborador** — linha temporal única (afectações, mobilidades,
   licenças, estados). Adiado até o negócio estar definido; resolveria também a
   lacuna de não haver como ler as substituições.
5. **Framework de jobs — portar do `inss_core_service`.** **Fica para o fim**, por
   decisão do utilizador (2026-09-22). Ver secção própria abaixo.

### O que a lei disse, e que contrariava o plano (2026-09-22)

O utilizador forneceu o **Boletim Oficial n.º 30 de 24-03-2023**, onde a Lei
n.º 20/X/2023 foi publicada; está extraído em **`.lei20.txt`** (raiz, não
versionado). Antes disso tentei quatro fontes públicas e **nenhuma dava o
articulado** — só a ficha do diploma. Ler o texto mudou dois dos três desenhos,
e vale a pena saber porquê antes de mexer nisto outra vez.

**A acumulação não era o que o código dizia.** O `TipoAfectacao.ACUMULACAO`
citava o art. 134.º n.º 2 al. b) para dizer «exercício cumulativo de outro
Lugar». O artigo diz outra coisa: classifica a mobilidade geral **quanto à forma
de prestação** — a tempo inteiro, ou «em regime de acumulação, quando o
funcionário passa a exercer funções noutro serviço, em acumulação com as do
serviço de origem». E como a mobilidade transitória é «sem ocupação do lugar do
quadro» (art. 135.º n.º 7), **uma mobilidade em acumulação não cria afectação
nenhuma**. O valor saiu do enum (zero linhas na BD e na auditoria) e a
acumulação passou a viver no registo da mobilidade (V52).

*Não confundir com o art. 21.º* — acumulação de funções públicas, que é outro
instituto: permissão, com incompatibilidade, manifesto interesse público e, em
regra, não remunerada (n.º 1); sendo remunerada, só nos casos taxativos do n.º 2.
**Não está implementado.**

**O regresso de comissão estava errado, e em silêncio.** O art. 64.º n.º 2 diz
que o nomeado regressa à situação anterior «quando constituída e consolidada por
tempo indeterminado, ou, **no caso contrário, cessa a relação jurídica de emprego
público**». O catálogo tinha `REGRESSA_LUGAR` sem condição: devolvia ao Lugar de
origem toda a gente, incluindo quem foi recrutado *para* a comissão e nunca teve
Lugar. Falta que só se via lendo o artigo.

**A consolidação confirmou a regra já decidida.** O art. 135.º n.º 8 define a
mobilidade definitiva como a que é feita «com ocupação do lugar do quadro», por
oposição à transitória do n.º 7. Isso sustenta no texto a decisão antiga de que
«mobilidade definitiva = transferência», e dá as condições exactas: **na mesma
função e categoria** (art. 132.º n.º 4) e para um **lugar vago** (art. 134.º n.º 1
al. a)).

**O que a lei manda e não se escreveu:** o art. 132.º n.º 4 acaba com «nos termos
regulados por **diploma de desenvolvimento**». Há, portanto, condições de
consolidação (tempo mínimo, provavelmente) que vivem noutro diploma que não
temos. **Não se inventou nenhum prazo.** Se esse diploma aparecer, é no
`AssignmentService.consolidarMobilidade` que entra.

**Aliança de datas:** a Lei n.º 20/X/2023 foi alterada pela **Lei n.º 49/X/2025**
(BO n.º 27, de 7 de Abril de 2025), que mexeu em **contratação de emergência** e
no **ciclo de gestão de pessoal** — não nos artigos da mobilidade nem da
comissão. O texto de 2023 vale para o que se fez. **Isto responde em parte à
questão aberta 10.**

### Ausências — a falta injustificada e o efeito na remuneração (V54, 2026-09-22)

**Uma frase da lei, duas coisas, e só uma delas é escolha.** Art. 43.º n.º 2: as faltas
injustificadas «não contam para efeitos de antiguidade e implicam a **opção** entre a
perda das remunerações correspondentes aos dias de ausência, ou o seu desconto nas
férias».

- **O desconto na antiguidade é imperativo** — por isso não entrou um booleano
  `counts_seniority`, que deixaria configurar o contrário da lei. Entrou um **terceiro
  valor no `regime`**: `FALTA_INJUSTIFICADA`, seguindo a secção própria do diploma. A
  instituição diz **quais** das suas linhas o são; o efeito é da lei. O
  `AntiguidadeService` ganhou a **quarta fonte**, e a questão aberta 11 fecha-se.
- **A opção é de cada caso**, e vive no pedido (`opcaoFaltaInjustificada` ∈
  `PERDA_REMUNERACAO` · `DESCONTO_FERIAS`). Obrigatória nos tipos injustificados (422 se
  faltar), recusada nos outros (422). Pelo self-service não existe: ninguém classifica
  uma falta sua como injustificada.
- **O efeito na remuneração é informação, não cálculo** (art. 16.º).
  `EfeitoNaRemuneracao` ∈ `SEM_PERDA` · `PERDA_PARCIAL` (n.º 2, com subsídio da
  previdência) · `PERDA_TOTAL` (n.º 4, greve) · `PERDA_VENCIMENTO_EXERCICIO` (n.º 5,
  prisão preventiva, reparável pelo n.º 6) · `DEPENDE_DA_OPCAO`. **Decisão do
  utilizador:** o RH não desconta nada, guarda a classificação para integrar com o
  sistema que processa vencimentos. Um booleano não servia — não sabe dizer «parcial».

**A migração não classifica linha nenhuma**, de propósito: estas duas colunas mandam
descontar antiguidade e mexer em salários. Tudo fica em `SEM_PERDA` e no regime que já
tinha; classifica-se pela API. Numa instalação a rodar, **nada desconta até alguém
classificar** — que é a direcção segura.

**Defeito do seed que a bateria apanhou:** a falta injustificada vinha com
`deducts_balance = true`, o que exigia um *saldo de faltas injustificadas* para se poder
registar uma — uma quota de faltar, o oposto do que a lei diz. Passou a `false`.

**Nas licenças a informação já existia** como booleano
(`t_leave_mobility_subtype.affects_pay`), que não sabe dizer «parcial». Ficam dois
contratos até alguém os convergir.

### Ausências — o limite de dias tem três naturezas (V53, 2026-09-22)

**O catálogo só sabia dizer «X dias por ano»**, e o `CreatePedidoAusenciaCommandHandler`
somava sempre o ano civil. O art. 15.º n.º 1 do DL n.º 3/2010 quase nunca fala em
anos: «até 6, **por ocasião** do casamento», «até 8, por motivo de **falecimento**
do cônjuge», «duas por **cada** prova». Escrever isso no tecto anual errava nos
**dois sentidos ao mesmo tempo** — recusava o segundo funeral do ano e deixava
passar oito dias seguidos de uma só vez.

**Três valores independentes, não um valor com uma classificação ao lado.** É a
al. q) que o obriga: «6 dias em cada ano civil **e um dia por mês**» — os dois
tectos valem juntos. `max_days_per_year` · `max_days_per_occurrence` ·
`max_days_per_month`.

**Nulo é «a lei não põe limite desta natureza»**, não zero. Greve, obrigações
legais e prisão preventiva não têm quota. Zero dá 400 e é impedido na base
(`ck_leave_type_limites_positivos`): um limite de zero dias não é um limite, é um
tipo que ninguém pode pedir, e isso diz-se desactivando a linha.

| Peça | O que faz |
|---|---|
| `colaboradores/domain/models/TipoAusencia.java` | `excedeLimitePorOcorrencia` · `excedeLimiteMensal` · `excedeLimiteAnual` — as regras, puras |
| `CreatePedidoAusenciaCommandHandler` | orquestra, pela ordem em que a recusa é mais útil |
| `PedidoAusenciaRepository.somarDiasNoMes` | a soma do mês, feita na base como a do ano |
| `db/migration/V53__leave_type_limites_por_ocorrencia_e_mes.sql` | as duas colunas + `ck_` |

**O seed traz o art. 15.º classificado** (`CASAMENTO`, `LUTO` 8 / `LUTO_OUTRO_GRAU`
3, `NASCIMENTO_FILHO`, `PROVA_EXAME`, `ASSISTENCIA_FAMILIA`,
`AUTORIZADA_DIRIGENTE`, `CONTA_FERIAS`, `GREVE`, `OBRIGACAO_LEGAL`,
`DOENCA_ATESTADO`). **A migração não corrige valores existentes** — o catálogo é da
instituição. O `repor_estado.sql` alinha a base de desenvolvimento, como já faz ao
`MOB_COMISSAO`.

### Feriados — três defeitos, e o desenho que o utilizador decidiu (2026-09-22)

**Isto ainda não está implementado.** É o ponto 1 do plano aprovado, e o que está
aqui é o diagnóstico feito e o desenho decidido — não código escrito.

**O que existe hoje.** `t_public_holiday` tem `name`, `holiday_date`,
`is_national`, `is_active` e `description`. O seed traz os **11 feriados nacionais
de Cabo Verde**. A contagem de dias úteis (`DiasUteisCalculator`) tira sábados,
domingos e os feriados que lhe derem; quem lhos dá são dois sítios, e ambos pedem
**só os nacionais** (`findAllNacionaisActivosByAno`):
`CreatePedidoAusenciaCommandHandler:51` e `SuspenderFeriasCommandHandler:110`.

**Três defeitos, por ordem de gravidade:**

1. **Os feriados são de 2026 e mais nada.** Estão no seed com data fixa. **A 1 de
   Janeiro de 2027 a aplicação deixa de conhecer feriado nenhum** e passa a contar
   dias úteis a mais em todos os pedidos, em silêncio, até alguém carregar o ano
   novo à mão. É o defeito mais grave desta área, e foi encontrado por causa de uma
   pergunta do utilizador — não estava em nenhuma lista.
2. **Os municipais nunca contam.** A coluna existe para os distinguir, mas a
   consulta pede só os nacionais. Quem atravessa um feriado municipal perde um dia
   de saldo.
3. **O `municipioCkey` está ligado à coluna errada.** `Feriado` (domínio) tem
   `municipioCkey`, e `FeriadoRepositoryImpl.toEntity` grava-o em `description` —
   e o `toDomain` lê `description` de volta para lá. Nos 11 feriados do seed, o
   «município» é, literalmente, o texto *«Feriado nacional»*. Alguém começou isto e
   ficou a meio.

**O desenho, decidido pelo utilizador e corrigido pela pergunta dele.** Eu tinha
formulado isto como «de que município se contam os feriados do colaborador», o que
está errado: **não é da pessoa, é da instituição.** O que conta é onde o **serviço**
fica, e é a instituição que cataloga o seu calendário.

- **Contar todos os feriados activos**, não só os nacionais. O calendário da
  instituição é o calendário da instituição: o RH carrega os nacionais mais o
  municipal do sítio onde está, e conta-se tudo. Isto sozinho fecha o defeito 2 sem
  nenhum campo novo e **sem saber nada sobre o colaborador**.
- **Recorrência**, para fechar o defeito 1: um feriado de data fixa (1 de Janeiro,
  13 e 20 de Janeiro, 1 de Maio, 5 de Julho, 15 de Agosto, 1 de Novembro, 25 de
  Dezembro) marca-se como recorrente e vale todos os anos. Os **móveis**
  (Sexta-feira Santa, Corpus Christi) continuam a ser carregados por ano: a data
  depende da Páscoa, e qual das festas móveis é feriado é matéria de lei.
- **Área geográfica opcional**, e só para quem precisa: uma instituição com
  serviços em vários concelhos (um ministério com delegações na Praia, no Mindelo e
  no Sal) limita um feriado a uma área, e a **unidade orgânica** diz em que área
  está. **Feriado sem área vale para toda a gente**, que é o caso normal — ninguém
  é obrigado a preencher nada.
- **A área é uma entrada de catálogo**, no catálogo genérico que já existe
  (`Option`), e **não** um «concelho de Cabo Verde». Em Cabo Verde põem-se
  concelhos; noutro país põe-se o que lá houver. É o que torna isto instalável fora
  de CV, que é um objectivo do projecto.
- **Arrumar o `municipioCkey`** (defeito 3) ao mesmo tempo, senão fica a fingir.

**A direcção segura, quando faltar configuração:** conta-se só o que se sabe. Sem
área preenchida, o feriado aplica-se a todos; sem feriados carregados para um ano,
contam-se todos os dias úteis. Nunca se desconta um dia que ninguém declarou.

### Marcação de férias — a lei dá o desenho todo (art. 5.º e 6.º do DL 3/2010)

**Também ainda não está implementado** — é o ponto 3 do plano. O que mudou nesta
sessão é que **deixou de haver decisões pendentes**: eu tinha-a marcado como «por
decidir com o RH», e ao ler o articulado percebi que a lei responde.

**Não há aprovação do mapa.** O art. 6.º n.º 1 diz que «os serviços devem
**elaborar** o mapa de férias e dele **dar conhecimento** aos respectivos
funcionários». Elaborar e comunicar — não aprovar. A pergunta «quem aprova o mapa»
partia de um acto que a lei não prevê, e não se deve reintroduzir.

| Quando | Quem | O quê |
|---|---|---|
| até **31 de Janeiro** | o trabalhador | **indica a preferência** (art. 5.º n.º 4) |
| até **31 de Março** | o serviço | **elabora o mapa** e dá conhecimento (art. 6.º n.º 1) |
| depois de 31 de Março | ambos | só se altera **por acordo**, salvo conveniência de serviço fundamentada (art. 6.º n.º 2) |
| na falta de acordo | o **dirigente competente** | **fixa** entre 1 de Maio e 31 de Outubro (art. 5.º n.º 5) |

**Quem não indica preferência não tem acordo**, logo cai no n.º 5 e o dirigente
fixa-lhe as férias entre Maio e Outubro. **Não se inventa um comportamento por
omissão** — a lei já o dá.

**As outras regras do art. 5.º, que são validações:**

- n.º 1 — em gozo interpolado, **um dos períodos não pode ser inferior a 11 dias**;
  e não se pode gozar seguidamente mais dias úteis do que o direito anual (n.º 3 do
  art. 2.º, os 22);
- n.º 2 — **não pode ser imposto** o gozo interpolado, salvo conveniência de serviço
  «devidamente fundamentada». Se o serviço partir as férias de alguém contra a
  preferência dele, tem de haver fundamentação registada;
- n.º 3 — marcam-se «de acordo com os interesses das partes», assegurando o regular
  funcionamento dos serviços.

**Uma lacuna que não se deve inventar:** o n.º 6 dá preferência a **cônjuges e
unidos de facto que trabalhem no mesmo serviço** para gozarem no mesmo período.
Sabemos o estado civil de cada colaborador, mas **não há ligação entre dois
colaboradores** — não há como saber que a Maria é casada com o Francisco. Assinalar
no documento; não adivinhar a partir de apelidos nem de dependentes.

**As datas (31 de Janeiro, 31 de Março, 1 de Maio a 31 de Outubro) são valores da
lei**, e o projecto já decidiu que valores da lei que variam com o diploma vivem em
parâmetro com a lei por omissão — é o que torna isto instalável noutro país.

### Assiduidade — entra no âmbito, e a Lei n.º 20/X/2023 obriga

**Decisão do utilizador (2026-09-22): a assiduidade entra.** A questão aberta 1
fecha-se com «sim». É o ponto 4 do plano, e é um **módulo**, não um campo.

**A base legal é uma obrigação, não uma conveniência.** Art. 164.º n.º 3 da Lei
n.º 20/X/2023:

> «Os serviços públicos devem **manter um registo** que permita apurar o **número
> de horas de trabalho** prestadas pelo funcionário ou agente, **por dia e por
> semana**, com indicação da **hora de início e de termo** do trabalho, bem como
> **dos intervalos** efectuados.»

Hoje a aplicação não cumpre isto de todo.

**E a lei diz como construí-lo sem o prender a Cabo Verde:**

- **art. 164.º n.º 4** — os **limites máximos dos períodos normais de trabalho** e
  os **intervalos de descanso** vêm de **diploma de desenvolvimento**;
- **art. 165.º n.º 2** — as **modalidades de horário** de trabalho, de funcionamento
  e de atendimento vêm do mesmo diploma;
- **art. 165.º n.º 1** — distingue **período de funcionamento** (o serviço a
  trabalhar) de **período de atendimento** (aberto ao público), que pode ser igual
  ou menor;
- **art. 165.º n.º 3** — o mesmo serviço pode adoptar **uma ou várias modalidades**
  de horário ao mesmo tempo.

Ou seja: **os números não são nossos, são parâmetros.** Não temos o diploma de
desenvolvimento, e — como no tempo mínimo da consolidação e no regime da licença
extraordinária — **não se inventa**.

**O que a assiduidade destrava no DL n.º 3/2010**, e que hoje é inexprimível:

- **art. 13.º n.º 1** — falta é a ausência da totalidade **ou parte** do período
  diário de presença obrigatória;
- **art. 13.º n.º 2** — em horário flexível, o **débito de tempo** apurado no fim do
  período de aferição **é falta**;
- **art. 13.º n.º 4** — as ausências curtas **somam-se**: menos de meio período conta
  meio, mais de meio conta um período inteiro;
- **art. 20.º** — **dispensa para amamentação**: «45 minutos de dispensa em **cada
  período de trabalho**», durante os primeiros seis meses. É um direito real que
  hoje não tem como ser registado.

**Horas extras entram — com a fronteira da V54.** O **art. 150.º n.º 2 al. a)** da
Lei n.º 20/X/2023 trata o **trabalho suplementar** como **suplemento
remuneratório**, ao lado do nocturno, do trabalho em dias de descanso semanal e
complementar, em feriados e fora do local normal de trabalho. Logo: **apuramos as
horas e classificamos; quem paga é o sistema de vencimentos**, exactamente como o
`efeito_remuneracao` da V54. O DL n.º 3/2010 **não fala** em trabalho suplementar
(zero menções) — a matéria é da Lei 20/X/2023 e do diploma de desenvolvimento.

**Dispensa — metade já está feita, metade depende disto.** «Dispensa» na lei é duas
coisas:

| Em **dias** — já são ausências, é catálogo | Onde |
|---|---|
| maternidade, paternidade, adopção | art. 17.º, 18.º, 19.º |
| seminários, estudos e pesquisas — **máx. 5 dias consecutivos** | art. 21.º n.º 2 |
| trabalhador-estudante — **6 dias úteis** para pesquisas, sem perda de vencimento nem antiguidade | art. 77.º n.º 3 |

O tecto de 5 dias do art. 21.º é **por ocorrência**, e desde a V53 há onde o
guardar. Isto é o ponto 2 do plano, e é barato.

| Em **minutos** — só com assiduidade | Onde |
|---|---|
| amamentação: 45 minutos por cada período de trabalho, 6 meses | art. 20.º |

**E o buraco estrutural que a assiduidade resolve:** hoje **toda** a linha de
`t_leave_request` é um *pedido* com estado. Uma falta que ninguém pediu — que é o
caso normal de uma falta injustificada: a pessoa não apareceu — só se regista
criando um pedido em nome dela. Funciona, mas é um contorno, e é a fronteira real
entre «ausências» e «assiduidade».

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

**D1. Feriados — DECIDIDA a 2026-09-22, é o ponto 1 do plano aprovado.**
Deixou de ser dívida sem dono: o desenho está fechado e a decisão é do utilizador
— **o calendário é da instituição, e ela cataloga-o**. Ver «Feriados — três
defeitos, e o desenho que o utilizador decidiu» acima, que substitui o que aqui
estava. Em resumo: contam-se **todos** os feriados activos e não só os nacionais;
os de data fixa passam a **recorrentes** (senão a aplicação fica sem feriados a
partir de 2027); a **área geográfica** é opcional e vive na unidade orgânica, para
instituições com serviços em vários concelhos.

*Porque importa:* a contagem de dias úteis é a base das férias, que já estão
construídas por cima dela.

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
> **Atenção:** todos os caminhos desta secção são do **`inss_core_service`**, não
> deste repositório. Nenhum deles existe aqui.

- **Documentação (lá):** `docs/schedulers/README.md` (16 secções), `scheduler.html`
  (diagramas), `FRONTEND.md` (contrato para o front-end)
- **Código:** `src/main/java/gw/inss/core/shared/application/services/scheduler/`
  — `ScheduledJob` (interface de 4 métodos), `JobContext`, `JobResult`,
  `JobRunner`, `SchedulerService`, `ExecucaoRegistoService`, `SchedulerSweeper`,
  `EstadoExecucao`, `TipoDisparo`, `JobParametro`
- **Entidades:** `shared/infrastructure/persistence/entity/scheduler/` —
  `SchedulerJobEntity`, `SchedulerExecucaoEntity`
- **Migrações lá:** V40 (execuções), V47 (jobs), V48 (parâmetros). **Atenção:** os
  números colidem com os nossos; renumerar ao trazer.
- **REST (lá):** `shared/interfaces/rest/SchedulerController.java` (`api/v1/schedulers`)
- **Pools (lá):** `shared/config/SchedulingConfig.java` — `taskScheduler` (triggers)
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

**Sobre os movimentos de 2026-09-22 (segunda sessão)**

- **A mudança de carreira não é uma transferência.** A transferência mantém a
  posição na grelha e muda de cadeira; a mudança de carreira muda o **próprio
  eixo** de que a categoria e o escalão dependem, o que a aproxima de um novo
  provimento. A guarda que a define: **a carreira de destino tem de ser
  diferente** — sem ela, seria uma promoção sem nenhuma das regras da promoção.
- **Exige Lugar vago e não reclassifica.** Passar um Lugar de uma carreira para
  outra altera o quadro de pessoal, que é decisão de organograma e não movimento
  de uma pessoa. (A promoção pode reclassificar porque aí o Lugar sobe um degrau
  dentro da mesma carreira.)
- **Quem posiciona na nova carreira é o acto administrativo.** O critério legal
  — remuneração igual ou imediatamente superior — é aritmética sobre remuneração,
  que esta aplicação não tem. Entrar sempre pela base seria **contrário** ao
  espírito da regra, que existe para proteger quem muda com anos de serviço.
  Quando houver remuneração, é no `mudarCarreira` que a regra entra.
- **As habilitações não se verificam** em nenhum destes movimentos: a carreira não
  tem campo que diga o requisito e as qualificações não têm nível normalizado.
  Como o concurso na promoção, registam-se despacho e referência sem validação.
- **`POST /assignments` é só da titularidade.** A substituição tem endpoint próprio
  e por ali dá 422. Era um buraco aberto pela V45: ao tornar o índice do Lugar
  parcial em `PRINCIPAL`, deixou de haver nada a impedir que a porta genérica
  criasse afectações a outro título sem validação nenhuma.
- **A acumulação é forma de prestação da mobilidade, não título** (art. 134.º
  n.º 2 al. b)). `TEMPO_INTEIRO` por omissão, porque a exclusividade é a regra
  (art. 20.º). Só se fixa enquanto o processo está por decidir; depois do despacho
  é outro despacho. Numa licença dá 422 — quem está de licença não exerce funções
  em serviço nenhum.
- **A consolidação é a única via pela qual uma mobilidade toca na afectação**, e
  não contradiz o art. 135.º n.º 7: esse fala da transitória; o n.º 8 define a
  definitiva como a que ocupa lugar do quadro. **Na mesma função e categoria**, e
  o escalão mantém-se — não há evolução na grelha numa consolidação. **Só a
  interna**: numa externa não há Lugar nosso onde pôr a pessoa.
- **Não se inventou tempo mínimo de mobilidade** para consolidar — a condição está
  em diploma de desenvolvimento que não temos.
- **O regresso de comissão bifurca-se, e o caminho deriva-se do percurso.** Quem
  tem afectação corrente regressa a ela (a comissão mantém o Lugar); quem não tem
  foi recrutado *para* a comissão e **a relação cessa**, automaticamente, porque a
  lei não dá escolha. **Não se guarda um campo** a dizer qual é o caso: um campo
  mal preenchido passaria a decidir uma cessação.
- **`REGRESSA_OU_CESSA` é o único efeito no regresso que termina um vínculo.**

## Constraints

- **Build exige JDK 26**: `export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-26.0.2.10-hotspot"`.
- **Migrações Flyway sempre defensivas E idempotentes**, sem excepção (regra reafirmada
  pelo utilizador a 2026-09-22). `to_regclass` + `information_schema.columns`, e o
  mesmo cuidado com as colunas de `audit_schema.*_aud`. **Confirmar o número livre**
  antes de criar.
  - **O guarda tem de ser ANINHADO.** O PL/pgSQL prepara a instrução inteira quando a
    executa, por isso `IF to_regclass('t_x') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM
    t_x ...)` rebenta com *relation does not exist* numa base onde a tabela falta — o
    curto-circuito lógico não salva. A V49 tem a forma certa; a V51 e a V53 tinham a
    forma plana e foram corrigidas.
  - **Verificar, não acreditar.** Duas provas: correr o ficheiro contra uma base
    **vazia**, e corrê-lo **duas vezes** sobre uma cópia da base real (`pg_dump` para
    uma base nova), comparando depois as contagens das tabelas que ele actualiza.
  - **Editar uma migração já aplicada parte o checksum.** Apagar a linha de
    `flyway_schema_history` e arrancar uma vez com `--spring.flyway.out-of-order=true`.
    **Atenção:** reaplicar com um jar antigo grava o checksum do ficheiro antigo — usar
    fontes actualizadas (`mvn test` serve).
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
- **Não afirmar `emMobilidade -eq $false` depois de um regresso.** Esse campo responde
  por **qualquer** mobilidade em vigor, e o F8 deixa uma externa que abriu e fechou no
  mesmo dia — fica com um dia, que cobre hoje. Provar o **próprio registo**
  (`status` + `estadoPeriodo` + `dataFim`). Custou uma execução no F17.
- **O seed não corrige o que já existe**: `seed_parametrizacoes` usa
  `ON CONFLICT (code) DO NOTHING`. Uma linha de catálogo reclassificada num commit
  **não chega a uma base já criada** — foi por isso que o `MOB_COMISSAO` continuava
  `REGRESSA_LUGAR`. A reclassificação vive agora no `repor_estado.sql`.
- **Não escolher do catálogo «o primeiro da lista»**: a ordem não é garantida. O
  `$subMob` do F8 passou a exigir `returnEffect = REGRESSA_LUGAR`; se lhe calhasse a
  comissão, o F8.21 deixava de poder falhar (ela não tem limite de prorrogações).

## Blockers & risks

- **O compilador incremental do Maven mente.** Depois de mexer em lotes de
  ficheiros, `mvn test` sem `clean` diz *"Nothing to compile"* (escondendo erros)
  ou rebenta com `NoClassDefFoundError` numa classe que existe e está importada.
  **Usar sempre `mvn -B clean test`.** Custou tempo duas vezes.
- **Jar preso**: com a app a correr, `clean`/`repackage` falha. Fechar primeiro.
- **Não editar `src/` com um build a correr**, nem correr dois Maven ao mesmo tempo
  sobre o mesmo `target/`. O Maven apanha o ficheiro a meio e o resultado parece uma
  regressão de mais de cem testes que não existe; a colisão de dois Maven produz um
  jar sem recursos e erros de ficheiros em falta. O IDE do utilizador tem Maven
  próprio.
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

### Ausências não é assiduidade — e o modelo ainda não a consegue exprimir

> **Actualização de 2026-09-22: a assiduidade ENTRA no âmbito** (decisão do
> utilizador) e é o **ponto 4 do plano aprovado**. O que está nesta secção deixou
> de ser argumento para não a fazer e passou a ser a **lista do que ela tem de
> resolver**. A base legal — art. 164.º n.º 3 da Lei n.º 20/X/2023, que **obriga**
> ao registo de horas — está na secção «Assiduidade» acima.

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

### ~~Bug: feriados municipais ignorados~~ — DECIDIDO, por implementar (ponto 1)

Continua por corrigir no código, mas já **não está à espera de ninguém**: o desenho
foi decidido a 2026-09-22 e são **três** defeitos, não um — o pior é que os
feriados carregados acabam em **2026**. Ver «Feriados — três defeitos, e o desenho
que o utilizador decidiu» acima.

## Relevant files

- `colaboradores/application/services/SubstituicaoService.java` — regras da
  substituição; `encerrarPorRegressoDoTitular` tem duas assinaturas.
- `colaboradores/application/services/AssignmentService.java:106`
  `validarLugarParaAfectacao` — validações de Lugar partilhadas entre afectar e
  substituir. **Não** inclui a regra do titular único, que depende do título.
- `parametrizacoes/domain/models/SituacaoFuncional.java` — as seis situações e
  `permiteSubstituicao()`.
- `colaboradores/domain/models/TipoAfectacao.java` — PRINCIPAL/SUBSTITUICAO
  (a `ACUMULACAO` saiu na segunda sessão).
- `shared/application/dto/SuccessResponseDTO.java` — `de()`, `semEfeito()`.
- **Feriados — onde mexer no ponto 1 do plano:**
  - `colaboradores/domain/service/DiasUteisCalculator.java` — Seg-Sex menos os
    feriados que lhe derem. O calculador está certo; o problema é **quem lhe dá a
    lista**.
  - `colaboradores/domain/repository/FeriadoRepository.java` —
    `findAllNacionaisActivosByAno`, que é o filtro a mais.
  - Os dois consumidores: `CreatePedidoAusenciaCommandHandler:51` e
    `SuspenderFeriasCommandHandler:110`.
  - `colaboradores/infrastructure/persistence/adapters/FeriadoRepositoryImpl.java` —
    é aqui que o `municipioCkey` é gravado na coluna `description`.
  - `colaboradores/domain/models/Feriado.java` — tem `municipioCkey`; a entidade
    (`parametrizacoes/infrastructure/persistence/entity/PublicHolidayEntity.java`) não tem coluna para ele.
  - `db/seed/seed_parametrizacoes.sql` secção 7 — os 11 nacionais, **só de 2026**.
- Migrações em `db/migration/`: **V45**, **V46**, **V47**, **`V48`** (licença: decisão vs. período),
  **`V49`** (`t_leave_type.regime`), **`V50`** (acumulação de férias),
  **`V51`** (suspensão de férias), **`V52`** (forma de prestação da mobilidade),
  **`V53`** (limites por ocorrência e por mês), **`V54`** (falta injustificada +
  efeito na remuneração). A antiguidade **não tem migração**: é derivada.
  **Próxima livre: V55.**
- **Férias (DL 3/2010, cap. II):**
  - `colaboradores/application/services/FeriasService.java` — vencimento
    (art. 2.º), proporcionalidade do ano de ingresso (art. 3.º) e **acumulação**
    (art. 7.º n.º 1).
  - `colaboradores/infrastructure/scheduler/VencimentoFeriasScheduler.java` —
    diário, `rh.ferias.vencimento.cron`, 00:05.
  - `colaboradores/application/commands/SuspenderFeriasCommandHandler.java` —
    suspensão (art. 8.º).
  - `parametrizacoes/domain/models/RegimeAusencia.java` — `FERIAS` / `FALTA` /
    `FALTA_INJUSTIFICADA`; é por aqui, e **nunca pelo código**, que se sabe quais
    linhas do catálogo são férias e quais não contam para antiguidade.
- **Licença/mobilidade (V48):**
  - `colaboradores/domain/models/EstadoPeriodoLicenca.java` — o eixo temporal.
  - `colaboradores/application/services/LicencaEfeitoService.java` — efeitos na
    data devida, idempotentes.
  - `colaboradores/infrastructure/scheduler/LicencaEfeitoScheduler.java` —
    `rh.licencas.efeitos.cron`, 00:15.
- **Antiguidade:**
  - `colaboradores/domain/service/CalculadoraAntiguidade.java` — a conta pura
    (recorta, **une**, subtrai). É aqui que vive a regra de não somar períodos.
  - `colaboradores/application/services/AntiguidadeService.java` — as **quatro**
    fontes (situação funcional · subtipo de licença · vínculo do contrato · faltas
    injustificadas, esta desde a V54).
- **Movimentos de 2026-09-22 (segunda sessão):**
  - `colaboradores/application/services/AssignmentService.java` — ganhou
    `mudarCarreira` e `consolidarMobilidade`; o `afectar` ganhou a guarda que só
    deixa passar `PRINCIPAL`.
  - `colaboradores/domain/models/FormaPrestacaoMobilidade.java` — art. 134.º n.º 2.
  - `colaboradores/domain/models/LicencaMobilidade.java` — `consolidar` e
    `definirFormaPrestacao`.
  - `colaboradores/application/commands/ConsolidarMobilidadeCommandHandler.java`
  - `colaboradores/application/commands/MudarCarreiraColaboradorCommandHandler.java`
  - `colaboradores/application/services/LicencaService.java` —
    `aplicarRegressoDeComissao` (art. 64.º n.º 2).
  - `parametrizacoes/domain/models/EfeitoNoRegresso.java` — `REGRESSA_OU_CESSA`.
  - `db/migration/V52__mobilidade_forma_prestacao.sql`
- **Limites de dias e regime da falta (terceira sessão):**
  - `colaboradores/domain/models/TipoAusencia.java` — `excedeLimitePorOcorrencia`,
    `excedeLimiteMensal`, `excedeLimiteAnual`, `isFaltaInjustificada`,
    `contaParaAntiguidade`. As regras vivem aqui; o handler orquestra.
  - `colaboradores/application/commands/CreatePedidoAusenciaCommandHandler.java` —
    os três tectos e a opção do art. 43.º n.º 2.
  - `parametrizacoes/domain/models/EfeitoNaRemuneracao.java` — art. 16.º; é
    **informação**, não cálculo.
  - `colaboradores/domain/models/OpcaoFaltaInjustificada.java` — art. 43.º n.º 2.
- `colaboradores/application/queries/ListarSubstituicoesQueryHandler.java` — a
  leitura das substituições, nos dois papéis.
- `db/seed/seed_carreiras.sql` — `ordem_progressao` (1=ASS_TEC, 2=TEC_SUP); **sem
  ela a promoção recusa sempre**.
- `db/seed/seed_colaboradores.sql` — 3 colaboradores, 6 Lugares.
- `scripts/testes_funcionais.ps1` — bateria completa (F0 a F19, 457 passos).
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
mvn -B clean test                  # esperado: 901 testes, 0 falhas (COM clean)
                                   # sem a BD: 1 erro em contextLoads, nao e regressao
```

**Confirmar que este documento não mente:**

```bash
python scripts/verificar_handoff.py     # esperado: FALHAS: 0
```

Lê as afirmações **deste** ficheiro e compara-as com o repositório: cada caminho
citado existe, cada `ficheiro.java:linha` cabe no ficheiro, e os números (testes,
passos da bateria, commits por enviar, próxima migração livre, caminhos e esquemas
do `openapi.json`, jobs `@Scheduled`) batem com a realidade. **Não tem números
escritos à mão** — lê-os daqui —, por isso continua a servir depois de alguém
actualizar o handoff. Correr **depois** de `mvn clean test`, senão a verificação
dos testes é saltada (e ele di-lo).

> **Se o Maven se queixar de "error while writing ...class" ou de um pacote que
> claramente existe:** e o servidor Java do VS Code a compilar para o mesmo
> `target/`. Nao e regressao — repetir o comando resolve. Se insistir, fechar o
> VS Code.

Arranque real e bateria — **a bateria só no fim**, por indicação do utilizador
(2026-09-22): durante a implementação basta `mvn clean test` e a documentação a par.
Estes passos servem para quando chegar a altura, ou para verificar um estado herdado:

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
# esperado: PASSOS: 457   OK: 457   FALHAS: 0

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

**A bateria cobre F0 a F19 — 457 passos, todos OK.** F11 (vencimento de férias),
F12 (acumulação), F13 (suspensão) e F14 (antiguidade) entraram na primeira sessão
de 2026-09-22, e o F6 ganhou a leitura das substituições; F15 (mudança de
carreira) e F16 (consolidação da mobilidade) entraram na segunda; **F17 (regresso
de comissão)** na terceira. Não há nada commitado sem prova na bateria.

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

**Quatro das treze fecharam-se a 2026-09-22 (terceira sessão)**, e três delas não
eram perguntas para o RH — eram perguntas para a lei, que já as respondia. Fica o
registo, porque a tentação de as reabrir é real.

1. ~~**Assiduidade entra no âmbito?**~~ — **RESOLVIDA: entra** (decisão do
   utilizador, 2026-09-22). É um módulo, e a Lei n.º 20/X/2023 art. 164.º n.º 3
   **obriga** ao registo. Ver «Assiduidade» acima. Horas extras entram; dispensas em
   dias são catálogo, dispensas em minutos dependem do horário.
2. ~~**Ler as substituições**~~ — **RESOLVIDA (2026-09-22).**
   `GET /funcionarios/{id}/substituicoes`, nos dois papéis.
3. ~~**Licença que acaba antes de começar**~~ — **RESOLVIDA na V48.** Separou-se a
   decisão do período e o caso deixou de ter caminho.
4. ~~**Feriados municipais — de que município?**~~ — **RESOLVIDA, e a pergunta
   estava mal feita** (2026-09-22). Não é da pessoa: **é da instituição**, e é ela
   que cataloga o seu calendário. Ver «Feriados» acima para o desenho e para os
   **três defeitos** encontrados — incluindo o que ninguém tinha visto: os feriados
   acabam em 2026.
5. **Limite de substituições por pessoa** — hoje não há nenhum. **Validar com o RH.**
6. **A promoção exige lugar vago da categoria superior?** As duas formas estão
   suportadas; a prática da instituição decide. **Cliente.**
7. **`ACUMULACAO` como título de afectação** — **fechada na prática** a 2026-09-22
   (segunda sessão): saiu do `TipoAfectacao` e `POST /assignments` só aceita
   `PRINCIPAL`. O que sobra é o **art. 21.º da Lei 20/X/2023** — acumulação de
   *funções públicas*, outro instituto: permissão, com incompatibilidade, manifesto
   interesse público e, em regra, não remunerada. **Não está implementado**, e não
   está no plano.
8. ~~**Modalidades de licença do art. 45.º em falta no seed**~~ — **RESOLVIDA
   (`f6be1353`).** As sete estão no catálogo.
9. **Produção**: `ddl-auto` vem de `${HIBERNATE_DDL:update}` e os
   `application-<perfil>.properties` estão vazios. Sem `HIBERNATE_DDL` definida, o
   Hibernate altera o esquema por baixo do Flyway. **Adiado — ainda não há
   produção** (confirmado pelo utilizador).
10. **Jurídico**: confirmar se algum diploma substituiu o DL n.º 3/2010, e obter
    **o diploma de desenvolvimento** da Lei n.º 20/X/2023 — que fixa os limites dos
    períodos normais de trabalho, os intervalos e as modalidades de horário
    (art. 164.º n.º 4 e art. 165.º n.º 2). **É pré-requisito para os valores por
    omissão da assiduidade**; sem ele, parametriza-se sem omissões inventadas.
    Falta também o diploma da mobilidade, que o art. 132.º n.º 4 invoca para as
    condições da consolidação e o art. 64.º deste DL para a licença extraordinária.
11. ~~**Faltas injustificadas e antiguidade**~~ — **RESOLVIDA na V54.** Não era
    pergunta para o RH: o art. 43.º n.º 2 é imperativo. O que faltava era dizer
    **quais** das linhas do catálogo o são — terceiro valor do `regime`.
12. ~~**Marcação de férias — quem aprova o mapa?**~~ — **RESOLVIDA, e a pergunta
    estava mal feita** (2026-09-22). **Não há aprovação**: o art. 6.º n.º 1 manda
    elaborar e dar conhecimento. Quem não indica preferência cai no art. 5.º n.º 5 e
    o dirigente fixa entre Maio e Outubro. Ver «Marcação de férias» acima.
13. **Lock distribuído para os jobs** — sete `@Scheduled` e nenhum tem lock. Com
    réplicas, correm em todas. As marcas de idempotência limitam o estrago mas não
    substituem um lock. Mesmo saco de produção que o `HIBERNATE_DDL` (questão 9), e
    é o que o framework de jobs do `inss_core_service` resolveria.

## Next step

### Antes de tocar em código

```bash
docker start postgres-ingt-rh          # a BD tem de estar de pe ANTES dos testes
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-26.0.2.10-hotspot"
mvn -B clean test                      # esperado: 901 testes, 0 falhas
```

Isto confirma que se parte de verde. **A bateria funcional não se corre agora** —
ver a regra abaixo.

### As três regras de trabalho em vigor (2026-09-22)

1. **A implementação só começa quando o utilizador autorizar.** Há plano aprovado;
   não há autorização automática. Não começar a escrever código por iniciativa
   própria.
2. **A bateria só se corre no fim**, depois de tudo implementado. Pelo caminho:
   `mvn clean test` e **documentação a par**, que é como o utilizador a quer.
3. **Toda a migração é defensiva e idempotente**, verificada nas duas provas antes
   de se escrever Java. Ver «Constraints».

### O plano aprovado, por ordem

O utilizador aprovou esta ordem a 2026-09-22, depois de eu a ter fundamentado na
lei. **Cada ponto tem a sua secção própria acima, com o articulado.**

| # | O quê | Porquê agora | Tamanho |
|---|---|---|---|
| 1 | **Feriados** — contar todos os activos, recorrência, área geográfica opcional, arrumar o `municipioCkey` | É o único que está a custar dinheiro em silêncio: um dia a mais no saldo, e **zero feriados a partir de 2027** | pequeno |
| 2 | **Dispensas em dias** — art. 21.º (máx. 5 consecutivos) e art. 77.º n.º 3 (6 dias úteis) | Só catálogo; completa o que a V53 abriu | pequeno |
| 3 | **Mapa de férias** — art. 5.º e 6.º | Já não tem decisões pendentes: a lei dá o desenho todo | médio |
| 4 | **Assiduidade** — o módulo | Obrigação legal (art. 164.º n.º 3) que hoje não se cumpre | grande, por tijolos |

**A assiduidade, por tijolos**, na ordem em que cada um destrava o seguinte:
modalidades de horário e horário do colaborador → registo diário (entrada, saída,
intervalos) → apuramento de horas por dia e por semana → faltas por débito de tempo
(art. 13.º n.º 2) e ausências curtas que se somam (n.º 4) → trabalho suplementar
(registar e classificar, **não pagar**) → dispensas em minutos (art. 20.º).

### O que NÃO se reabre

- **Permuta, estágio probatório e reintegração judicial** — fora do âmbito por
  decisão do utilizador. Não são dívida esquecida.
- **Meios-dias** — adiados por decisão do utilizador («por agora não teremos pedido
  de meio dia»). Voltam a estar em cima da mesa quando a assiduidade entrar, porque
  é a mesma mudança de contrato (`numero_dias` é `INTEGER`).
- **Quem aprova o mapa de férias** — não há aprovação, há elaboração e conhecimento.
- **De que município são os feriados de uma pessoa** — não é da pessoa, é da
  instituição.
- **Se as faltas injustificadas descontam antiguidade** — descontam, por imperativo
  legal.
- **Framework de jobs do `inss_core_service`** — fica para o fim, por decisão do
  utilizador. Ver secção própria.

### Ainda à espera de decisão

| Assunto | Quem decide | Onde |
|---|---|---|
| Limite de substituições por pessoa | RH | questão 5 |
| A promoção exige lugar vago da categoria superior? | cliente | questão 6 |
| `HIBERNATE_DDL` e lock dos jobs, antes de haver produção | utilizador | questões 9 e 13 |
| Diploma de desenvolvimento da Lei 20/X/2023 (horários) e diploma da mobilidade | jurídico | questão 10 |

### Push

**21 commits locais por enviar** (`git push origin_git_lab fix-alinhamento-legislacao`).
O utilizador pediu para não o fazer; **não enviar sem lhe perguntar.**
