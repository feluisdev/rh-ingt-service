> Updated: 2026-09-18 17:10

## Goal

Alinhar o núcleo RH (não-sigdi) com a legislação cabo-verdiana, movimento a
movimento, com regras documentadas, testadas e **exercitadas contra a base de
dados**. O objectivo de fundo é a aplicação ser instalável em qualquer
instituição: o que a lei fixa vive em código; o que varia com a instituição ou
com o motivo vive em catálogo.

Construção **tijolo a tijolo**: um ponto do plano por commit, com testes e
documentação a par, sem avançar enquanto o anterior não estiver verde.

## Current state

**Branch `fix-alinhamento-legislacao`**, criado em 2026-09-18. O `dev` local
coincide com `origin/dev` e `origin_git_lab/dev`; todo este trabalho segue por
este branch porque traz **breaking changes** para o front-end
(`docs/funcionarios/v5/breaking_change_frontend.md` §11). **Nada foi enviado**
para nenhum dos remotos.

Commits desta sessão (os 6 mais recentes; ao todo 20 por enviar):

| Commit | O que fecha |
|---|---|
| `0084d1f6` | Situações funcionais do art. 117.º (V42) — ponto 1 |
| `2c0d980c` | Estado do contrato passa a enum `EstadoContrato` |
| `fa89174b` | Licenças que abrem vaga + disponibilidade (V43) — pontos 2 e 3 |
| `e9be40bd` | Ciclo do saldo de ausências e quem pode cancelar — ponto 4 |
| `27b9729d` | Handoff |
| `1cfd6ecc` | Contrato sem estado (V44) + bateria de testes funcionais |

**Migrações:** V40 `ends_employment` · V41 limites do subtipo · V42
`situacao_funcional` · V43 efeito da licença no Lugar e fim do `AMBOS` · V44
`t_contrato.status` obrigatório. **Próxima livre: V45.**

**Testes unitários:** 744, com **4 falhas pré-existentes e conhecidas** (2 de
`Option`, 2 de `EnversAuditSchemaResolutionTest`). Qualquer outra falha é
regressão.

**Testes funcionais contra a BD:** `scripts/testes_funcionais.ps1` — **78 passos,
78 OK** (2026-09-18). Ver `scripts/testes_funcionais_README.md`.

**Estado do ambiente local no fim da sessão:** a aplicação ficou **a correr** na
porta 8099 e a base de dados `recursoshumanos_db` (contentor `postgres-ingt-rh`,
porta 5436) ficou com os **dados alterados pela bateria** (A sem Lugar, B em
disponibilidade, pedidos e licenças de teste). Para recomeçar limpo, correr o SQL
de reposição que está no README dos testes.

## O que foi feito, e porquê assim

### Ponto 1 — situações funcionais (V42, `0084d1f6`)

A Lei n.º 20/X/2023 (art. 117.º) fixa **seis** situações do funcionário perante o
quadro e o catálogo só tinha quatro estados sem semântica legal.

- `t_worker_state.situacao_funcional` diz **qual** a situação de cada estado. Só
  isso é configurável.
- Os **efeitos** vivem no enum `parametrizacoes/domain/models/SituacaoFuncional`:
  abre vaga (art. 121.º n.º 2), conta antiguidade (art. 120.º n.º 2), suspende o
  vínculo, cessa o vínculo (art. 93.º al. b). Não são colunas porque o legislador
  já os fixou.
- `MudarEstadoColaboradorCommandHandler` deixou de comparar códigos
  `SUSPENDED`/`ACTIVE`: tudo deriva da situação. Estado sem situação → só
  histórico, sem efeitos.
- Coerência validada em `WorkerState`: um estado de cessação só aceita
  `APOSENTACAO` ou nenhuma situação, e `APOSENTACAO` obriga a `ends_employment`.
- Seed com três estados novos: `ACTIVE_OUTSIDE`, `INACTIVE_OUTSIDE`, `AVAILABLE`.
- Regras **BR-SIT-01..08** (§4.1 do `regras_negocio.html`). Testes:
  `SituacaoFuncionalTest` (14).

### Pontos 2 e 3 — licenças que abrem vaga (V43, `fa89174b`)

- Três colunas no subtipo: `position_effect` (MANTEM/ABRE_VAGA),
  `vacancy_after_days`, `return_effect` (REGRESSA_LUGAR/DISPONIBILIDADE), com os
  enums `EfeitoNoLugar` e `EfeitoNoRegresso`. O prazo é dado porque a lei o faz
  variar com o motivo: cônjuge além de 1 ano (art. 56.º n.º 2), formação além de
  6 meses (art. 67.º n.º 3).
- `LicencaService`: ao entrar em vigor, a licença que abre vaga encerra a
  afectação e põe o colaborador em inactividade fora do quadro; no regresso, quem
  perdeu o Lugar fica em **disponibilidade** (art. 122.º). O estado é escolhido
  **pela situação**, via `WorkerStateRepository.findBySituacao` — não por código.
- `record_type = AMBOS` **removido**; a mobilidade fica proibida de abrir vaga
  (art. 135.º n.º 7); `LIC_PARENTAL` desactivado (é ausência, não licença).
- `LeaveMobilitySubtypeEntity` passa a mapear a coluna `name`, que a V6 declara
  `NOT NULL` — criar um subtipo pela API rebentava.
- Regras **BR-LIC-01..07** (§6.1). Testes: `LicencaAbreVagaTest` (11).

### Ponto 4 — ausências (`e9be40bd`)

- `SaldoAusencia` ganha o ciclo completo: **reservar** na submissão, **confirmar
  o gozo** na aprovação, **libertar** na rejeição ou cancelamento pendente,
  **devolver** no cancelamento depois de aprovado. Antes, `dias_gozados` ficava
  sempre a zero.
- `SaldoAusenciaService` concentra o movimento; os cinco caminhos usam-no.
- Aprovar, rejeitar, cancelar e submeter passam a `@Transactional`.
- `EstadoPedidoAusencia` — o estado do pedido passa a enum.
- Filtros e soma anual **passam para JPQL**: já não se traz o histórico do
  colaborador para filtrar em memória.
- Regras **BR-AUS-05..11**. Testes: `SaldoAusenciaTest` (10).

### Correcções encontradas ao exercitar a API (`1cfd6ecc`)

1. **Contratos sem estado nunca eram suspensos** — o seed criava `status` nulo e
   suspender/reactivar comparam o estado actual, por isso não faziam nada, em
   silêncio. **V44** preenche (corrente → `ATIVO`, resto → `CESSADO`) e torna a
   coluna obrigatória.
2. **A grelha de carreira não tinha folga** — o seed punha o colaborador no
   último escalão, e a progressão respondia sempre "já está no último escalão".
   Cada categoria passa a ter dois escalões.
3. **Cancelamento de pedido de ausência** — ao tirar o 403 que bloqueava o RH,
   ficou a faltar a verificação de que o pedido pertence ao colaborador do URL.
   Reposta como **404**, que é a semântica certa. O controlador passa sempre o
   funcionário do URL como solicitante, por isso a regra antiga dizia 403 a toda
   a gente.

## Decisions made — do not re-litigate

- **Mobilidade transitória não toca na afectação** (art. 135.º n.º 7): o titular
  mantém o Lugar; encerrar é só fechar o registo.
- **Mobilidade definitiva = transferência**; interna/externa muda só o campo do
  destino (`destinationUnitId` vs `entidadeDestino`).
- **Promoção**: a modalidade infere-se do pedido (com `positionId` muda de Lugar;
  sem ele o Lugar sobe de categoria) e **não se persiste**.
- **Nada de respostas `Map`** nos endpoints novos: DTOs tipados.
- **Motivos não se validam** (`motivoCkey`, `terminationReason`): são
  parametrizados pelo utilizador no catálogo `Option`.
- **O que é derivável não se guarda** (provido/vago, vínculo laboral, "onde
  exerce funções", efeitos da situação funcional).
- **O SIGDI fica de fora.** A avaliação e os créditos de desempenho (CDD) vivem
  no SIGDI, que chamará o RH depois da integração. O RH **regista** a progressão
  e a promoção, não verifica elegibilidade. Confirmado: o DL 4/2024 remete os
  limiares para o diploma da gestão de desempenho (art. 36.º).
- **Parametrizar valores, não decisões.** Colunas para o que a lei fixa e pode
  variar (dias, limites, situação); nada de interruptores para decisões que a
  instituição ainda não tomou.
- **Valores fechados viram enum, não constantes soltas**: já feito para
  `EstadoContrato`, `EstadoPedidoAusencia`, `SituacaoFuncional`, `TipoRegisto`,
  `EfeitoNoLugar`, `EfeitoNoRegresso`. **Falta**: tipo de afectação
  (`PRINCIPAL`/`SUBSTITUICAO`/`ACUMULACAO`), origem da afectação, estado do Lugar
  (`Position.ATIVO`) e estados de `LicencaMobilidade`
  (`PENDING`/`ACTIVE`/`CLOSED`…).
- **Licença parental é ausência** (art. 171.º n.º 2 vs art. 172.º): `MATERNIDADE`
  90 dias, `PATERNIDADE` 10 dias úteis.
- **Nada de filtrar em memória**: filtros e somas vão para a consulta.
- **Substituto** e **percurso do colaborador** adiados (ver pendentes).

## Constraints

- **Build exige JDK 26**: `export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-26.0.2.10-hotspot"`.
- **Migrações Flyway sempre defensivas**: `to_regclass` para a tabela +
  `information_schema.columns` para a coluna. V40 a V44 são o modelo.
- **Confirmar o número livre** antes de criar a migração (já houve colisão na V39).
- Controladores em `interfaces/rest/` são gerados pelo IGRP: lógica nos handlers,
  e **manifesto `.igrpstudio` actualizado a par do Java** (entity + DTOs).
- JPQL usa o **nome `@Entity`**, não o da classe (ex.:
  `ColabsPedidoAusenciaEntity`, `ColabsLicencaMobilidadeEntity`). Uma `@Query`
  errada passa nos testes unitários e só rebenta no arranque real.
- Para extrair o ano em JPQL: `year(campo)`, nunca `FUNCTION('YEAR', campo)`.
- Documentação em **pt-PT**; commits `feat|fix|refactor(<módulo>): …`.
- **Scripts PowerShell só com ASCII**: o PS 5.1 lê o ficheiro como ANSI e um
  acento parte o script a meio, de forma difícil de diagnosticar.
- **Nomes totalmente qualificados no meio do código não passam**: usar imports.

## Armadilhas do ambiente (custaram tempo nesta sessão)

1. **Jar sem recursos.** Com o VS Code aberto, um `mvn clean package` pode gerar
   um jar **sem** os `application*.properties` nem as migrações: a app arranca na
   8080, sem perfil, e cai no `issuer-uri`. Confirmar sempre:
   `jar tf target/RH-Service-0.0.1-SNAPSHOT.jar | Select-String 'classes/application.properties'`.
   Se faltar, correr `mvn package` outra vez **sem** `clean`.
2. **Jar preso.** Se a app estiver a correr, o `repackage` falha com *"Unable to
   rename ... to ...jar.original"*. Fechar o processo primeiro:
   `Get-CimInstance Win32_Process -Filter "Name='java.exe'" | Where-Object { $_.CommandLine -match 'RH-Service' }`.
3. **Arranque lento**: 3 a 5 minutos até ao *Started*. Não é bloqueio.
3b. **O compilador incremental mente.** Depois de mexer em muitos controladores,
   um `mvn test` sem `clean` falha com `NoClassDefFoundError: CareerRequestDTO`
   (ou outra classe qualquer) embora o import esteja lá e a classe exista em
   `target/classes`. Também diz *"Nothing to compile"* quando há muito que
   compilar, escondendo erros reais. Ao mexer em assinaturas ou em lotes de
   ficheiros, usar sempre **`mvn -B clean test`**.
4. **Porta 8099**, não 8091 (o `.env` local manda).
5. **Erro esperado no log**: `AuthorizationSyncRunner` falha porque o URL do
   Access Management está vazio no `.env`. Não impede o arranque.

## Relevant files

**Domínio e regras**
- `parametrizacoes/domain/models/SituacaoFuncional.java` — as seis situações e os
  seus efeitos.
- `parametrizacoes/domain/models/{EfeitoNoLugar,EfeitoNoRegresso,TipoRegisto}.java`
- `parametrizacoes/domain/models/{WorkerState,LeaveMobilitySubtype}.java`
- `colaboradores/domain/models/{EstadoContrato,EstadoPedidoAusencia}.java`
- `colaboradores/domain/models/{Contrato,PedidoAusencia,SaldoAusencia,LicencaMobilidade,SubtipoLicencaMobilidade}.java`

**Serviços de aplicação**
- `AssignmentService` — afectar, progredir, promover, transferir,
  `encerrarAfectacaoCorrente`.
- `LicencaService` — efeitos da licença no Lugar e no regresso (novo).
- `SaldoAusenciaService` — ciclo do saldo (novo).
- `MobilidadeService` — validações da mobilidade, `mobilidadeEmVigor`,
  `subtipoSeExistir`.
- `CessacaoService` — caminho único da cessação.

**Migrações e dados**
- `db/migration/V40..V44` — modelo das migrações defensivas.
- `db/seed/seed_parametrizacoes.sql`, `seed_carreiras.sql`,
  `seed_colaboradores.sql`.

**Documentação (pt-PT, fonte de verdade)**
- `docs/funcionarios/v5/regras_negocio.html` — catálogo BR-*.
- `docs/funcionarios/v5/modelo_relacional.html` — inclui §7.1 "onde vive cada
  decisão".
- `docs/funcionarios/v5/api_guide.md` — §5.3 efeitos do estado, §6.1 saldo,
  §7.1 licenças que abrem vaga.
- `docs/funcionarios/v5/breaking_change_frontend.md` — §11 para o front-end.
- `scripts/testes_funcionais.ps1` + `README` — bateria contra a BD.

## Trabalho pendente (por ordem recomendada)

### ~~1. Substituição~~ — FEITO (2026-09-19, `d4ded55b`)

V45 (índice do Lugar parcial em `PRINCIPAL`) + V46 (`titular_assignment_id`) +
`SubstituicaoService` + `POST /funcionarios/{id}/substituicao`. Caduca sozinha
quando o titular regressa. Enum `TipoAfectacao` feito. **Não exercitado contra
a BD** — ver secção "Antes da bateria".

### ~~2. Respostas tipadas~~ — FEITO (2026-09-19, `9ecfc207`, `cadfb788`)

`SuccessResponseDTO` em `shared` (id, sucesso, alertas) para 85 operações, mais
sete DTOs próprios para as respostas ricas. **Nenhum handler e nenhum controlador
não-sigdi devolve `Map`**, e as 237 operações do contrato têm esquema declarado.
`origin_assignment_id` largado na V47.

## Antes da bateria — o que falta (plano de 2026-09-19)

A bateria actual (78 passos) **não ficou partida** pela conversão dos `Map`: lê
`id`, `estado`, `numeroDias`, `afectacaoEncerradaId` e `estadoAtribuidoId`, e
todos sobrevivem nos DTOs novos. O que falta é outra coisa.

### A. Bloqueadores — sem isto a bateria não prova nada de novo

**A1. A base está gasta.** 2 colaboradores, 3 afectações, **nenhuma corrente**:
a execução anterior deixou o A sem Lugar e o B em disponibilidade. O SQL de
reposição do `testes_funcionais_README.md` §"Repor o estado inicial" resolve
isso, mas **não limpa o que esta sessão criou**: as etiquetas `ccode='TESTE_MAP'`
e o estado `TESTE_DISP`. Juntar ao SQL.

**A2. O seed não chega para o que falta exercitar.** Há **2 Lugares**, e ambos
são precisos como ocupados. Não dá para:
- promoção **com `positionId`** — precisa de um Lugar vago da categoria de cima;
- transferência com destino real;
- substituição — precisa de titular impedido **e** de um terceiro colaborador
  para substituir.

Mínimo: **5 a 6 Lugares** (dois ocupados, um vago da categoria superior, um vago
da mesma categoria, um fora de grelha) e **um terceiro colaborador**.

**A3. Decidir o `INACTIVE`.** É o único estado do catálogo **sem situação
funcional**, por isso não produz efeito nenhum. Ou é intencional (o caso de
demonstração do "estado sem situação") ou é um buraco do seed. Se for para
classificar, é `INACTIVIDADE_NO_QUADRO`.

### B. Alargar a bateria

| Bloco | O que prova | Passos (estimativa) |
|---|---|---|
| **F6 substituição** | entra sem desalojar o titular · titular não impedido → 422 · Lugar vago → 422 · estado sem situação → 422 · a si próprio → 422 · segundo substituto → 409 · o regresso do titular **fecha sozinho** a substituição | ~10 |
| **F7 cessação** | os dois caminhos (estado `RETIRED` e `close` do contrato) dão o mesmo resultado · encerram também a substituição | ~6 |
| **F8 mobilidade** | ponta a ponta: aprovar com destino interno e externo · **não** mexe na afectação · prorrogar · encerrar | ~8 |
| **F9 promoção** | as duas formas, com Lugar vago real: com `positionId` muda de Lugar; sem ele o Lugar sobe de categoria | ~6 |
| **F10 contrato das respostas** | `sucesso`/`alertas` · 2.ª desactivação idempotente · `assignmentType` inválido → 422 · `downloadUrl` passou a `url` | ~6 |

Total estimado: **78 → ~114 passos**.

### C. Decisões que não são minhas

1. **Limite de substituições por pessoa.** Hoje não há: a mesma pessoa pode
   cobrir três Lugares ao mesmo tempo. Validar com o RH.
2. **A promoção exige lugar vago da categoria superior?** As duas formas estão
   suportadas; a prática da instituição decide qual se usa.
3. **`INACTIVE`** — ver A3.

### 3. Férias (DL n.º 3/2010)
O seed já dá 22 dias úteis, e o ciclo do saldo está fechado. Falta o negócio:
marcação, acumulação e gozo proporcional ao tempo de serviço.

### 4. Dívida conhecida do catálogo
- `affects_pay` e `counts_for_seniority` (subtipo) e `counts_seniority`
  (vínculo laboral) existem e **nenhum código os usa**.
- `SituacaoFuncional.contaAntiguidade()` também ainda não tem consumidor: **não
  há cálculo de antiguidade em lado nenhum**. Quando houver, é aí que estas três
  coisas se ligam.

### 5. Movimentos menores que faltam
Consolidação da mobilidade (art. 132.º n.º 4) · mobilidade em acumulação
(art. 134.º n.º 2 al. b; o tipo `ACUMULACAO` existe e nunca é usado) · permuta
(troca recíproca e simultânea, tem de ser atómica) · **mudança de carreira /
evolução vertical** (art. 139.º e art. 35.º do PCFR — hoje sem caminho: a
promoção exige a mesma carreira e a transferência a mesma categoria) · regresso
de comissão de serviço (art. 64.º n.º 2) · estágio probatório → nomeação
definitiva (art. 57.º, 72.º) · reintegração judicial (art. 97.º n.º 10 al. b).

### 6. Percurso do colaborador
Endpoint que junte numa só linha temporal as afectações, as mobilidades, as
licenças e as mudanças de estado. Adiado até o negócio estar definido.

## Legislação — o que já está confirmado

Os PDFs oficiais estão no site do Ministério das Finanças (`mf.gov.cv/web/dnap`)
e foram lidos nesta sessão:

- **PCFR (DL n.º 4/2024)**: progressão sem concurso nem tempo de serviço
  (art. 33.º n.º 3, 34.º n.º 1); promoção com concurso interno (art. 32.º n.º 3,
  34.º n.º 2); mudança de carreira = evolução vertical (art. 35.º). **Não fixa o
  número de créditos** — remete para o diploma da gestão de desempenho (art. 36.º),
  que é âmbito do SIGDI.
- **DL n.º 3/2010** mantém-se em vigor no que não contraria a lei nova
  (art. 213.º da Lei 20/X/2023), com a maternidade a passar de 60 para 90 dias.
- **DL n.º 25/2025** é só a Tabela Única de Remuneração.

**Por confirmar com o jurídico:** se algum diploma posterior substituiu o
DL n.º 3/2010 (não encontrado) e qual o diploma da mobilidade.

**Decisão de negócio pendente (RH):** a promoção, na prática da instituição,
exige lugar vago da categoria superior? Não bloqueia — as duas formas estão
suportadas.

## How to verify / resume

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-26.0.2.10-hotspot'
cd C:\Users\ivanick.santos\Nick-personal\ta-workspace\projects\Recursos_Humanos

git switch fix-alinhamento-legislacao
git log --oneline -6

mvn -B test        # 744 testes, 4 falhas conhecidas (Option x2, Envers x2)
```

Arranque real e bateria funcional:

```powershell
mvn -B -DskipTests package
# confirmar que o jar tem recursos (ver armadilha 1)
java -jar target\RH-Service-0.0.1-SNAPSHOT.jar --spring.profiles.active=development
# esperar pelo "Started RecursosHumanosApplication" (3-5 min)

# repor os dados de teste: SQL no scripts/testes_funcionais_README.md
.\scripts\testes_funcionais.ps1     # esperado: 78/78
```

Se o contexto Spring falhar com *"Found more than one migration with version N"*,
é colisão de número de migração. `mvn -B clean compile` quando se alterar
assinaturas do domínio.

## Verificado contra a base de dados (2026-09-18)

A bateria de 78 passos cobre, com caminho positivo **e** negativo:

- **F0 navegação** — listas, detalhe, unidade actual e os três catálogos.
- **F1 situações funcionais** — os quatro estados de origem vêm classificados;
  situação fora da lei, cessação incoerente e `APOSENTACAO` sem cessação → 422.
- **F1b movimentos** — progressão sobe um escalão (201); destino inexistente na
  promoção e na transferência → 404; data anterior à afectação → 422.
- **F2 estado que abre vaga** — encerra a afectação, suspende o contrato, **não**
  cessa o vínculo; o regresso reactiva o contrato e **não** devolve o Lugar;
  repetir o mesmo estado → 409; estado inexistente → 404.
- **F3 licenças** — 180 dias mantém o Lugar, 200 abre vaga; o regresso põe em
  disponibilidade; mobilidade a abrir vaga → 400; `AMBOS` → 422; criar subtipo
  pela API → 201; aprovar duas vezes → 409.
- **F4 ausências** — reserva, gozo, devolução e libertação, com o saldo lido a
  cada passo; sobreposição → 409; sem saldo → 422; dupla decisão → 409; URL de
  outro colaborador → 404; filtro por estado feito na BD.
- **F5 efeitos cruzados** — quem perdeu o Lugar (por estado ou por licença) não
  pode progredir → 422. É a prova de que a vaga abriu mesmo.

**Ainda não exercitado contra a BD:** cessação pelos dois caminhos (estado
`RETIRED` e `close` do contrato), mobilidade transitória de ponta a ponta
(aprovar com destino interno e externo, duração, prorrogação, encerrar) e
promoção nas duas formas com um Lugar vago real. O seed só tem dois Lugares,
ambos ocupados — para a promoção com `positionId` é preciso criar um Lugar vago
primeiro.

## Next step

**A1 + A2: preparar os dados, e só depois alargar a bateria.** Sem um terceiro
colaborador e sem Lugares vagos, os blocos F6 a F9 não têm como correr.

Ordem: seed (A1, A2, A3) -> bateria F6 a F10 -> corrigir o que ela apanhar ->
depois as funcionalidades que faltam (ferias, divida do catalogo, movimentos
menores, percurso).

Antes de mexer, `mvn -B clean test` (com `clean`, ver armadilha 3b): esperado
**763 testes, 0 falhas**.
