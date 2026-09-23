> Updated: 2026-09-23 11:08 (-01:00) — fim da sessão de 2026-09-23

## Goal

Alinhar o núcleo RH (não-sigdi) com a legislação cabo-verdiana — DL n.º 3/2010 (férias, faltas,
licenças) e Lei n.º 20/X/2023 (função pública) —, ponto a ponto, com regras documentadas e testadas.
O que a lei fixa vive em código (enum); o que varia com a instituição vive em catálogo; os valores
da lei que só mudam com um diploma novo vivem em `application.properties`. Um ponto por commit.

## Current state

**Branch `fix-alinhamento-legislacao`**, **26 commits locais por enviar** (`origin_git_lab`).
**Não fazer push sem o utilizador pedir** — merge para `master` no GitLab é deploy.

- **Testes: 961, 0 falhas** — correr na **cópia isolada** (ver Blockers). Último relatório:
  `<scratchpad>/rh/target/surefire-reports`.
- **Bateria funcional: 457 passos** — é o número do README, da última execução (2026-09-22).
  **Não foi corrida nesta sessão** (regra: só no fim). V55, V56 e o mapa de férias **não têm
  blocos**, e a V56 muda números que blocos antigos verificam — ver Test plan.
- Migrações até **`V56`**. Próxima livre: **V57** (só para alterar tabelas existentes).
- **`openapi.json`**: 236 caminhos, 257 esquemas (regenerado com a app a correr).
- **Sete jobs `@Scheduled`**, sem lock distribuído (questão 13).
- Árvore limpa excepto `.claude/settings*.json` (não são desta sessão) e os textos das leis não
  versionados na raiz: `.lei20.txt` (Lei 20/X/2023), `.dl3.txt` (DL 3/2010), `.estrutura.txt`.
  **Ler o articulado antes de desenhar** — mudou o desenho três vezes nesta sessão.

**Feito nesta sessão (3 commits):**

| Commit | Ponto do plano | O quê |
|---|---|---|
| `447f5273` | 1 — Feriados | **`V55`**: `t_public_holiday.is_recurring` e `area_ckey`, `t_unidade_organica.area_ckey`. Contam **todos** os feriados activos, no **período inteiro** do pedido; recorrentes valem todos os anos desde o da data; área opcional (catálogo `AREA_GEOGRAFICA`), herdada da unidade-mãe. `CalendarioFeriadosService` |
| `dc1bbf66` | 2 — Dispensas | **`V56`**: `t_leave_type.contagem` ∈ DIAS_UTEIS · DIAS_SEGUIDOS (art. 76.º). Seed: `SEMINARIO`, `TE_PESQUISA`, `TE_LICENCA` |
| `6114faa8` | 3 — Mapa de férias | Agregados `FeriasDoAno` e `MapaFerias`; 5 endpoints; tabelas `t_ferias_*` **sem migração** (ddl-auto) |

**Plano aprovado:** 1 ✔ · 2 ✔ · 3 ✔ · **4 — Assiduidade** (por fazer) · framework de jobs (por último).

## Decisions made — do not re-litigate

**Desta sessão (utilizador, 2026-09-23):**
- **Não partir o front.** Campo novo em recurso existente: opcional, sem 422 contra catálogo, e o
  `PUT` **não apaga** o que vem omisso (nulo = manter; `""` = limpar; `false` = desmarcar).
  Guardas que só o campo novo dispara podem ficar. Por isso a `areaCkey` **não se valida** contra
  o catálogo (feriado e unidade orgânica).
- **Entidade nova não leva migração** — o `ddl-auto` cria a tabela e a `_aud`. Flyway só para
  alterar tabelas existentes. As invariantes que seriam `CHECK` ficam no domínio.
- **Calendário de feriados é da instituição, não da pessoa.** A pessoa só entra pela área da
  unidade onde **exerce funções** no início do período (mobilidade interna → destino; externa ou
  sem Lugar → só feriados sem área). Recorrente vale **desde o ano da data**, não para trás.
- **Contagem (art. 76.º):** regra é dias seguidos; úteis só onde a lei o diz (férias, al. o),
  paternidade, art. 77.º). Em dias seguidos contam os dias **entre o primeiro e o último dia útil**
  — fins-de-semana das pontas não. A migração deixou tudo `DIAS_UTEIS`; o seed classifica.
- **Art. 77.º n.º 3 (6 dias):** lido **por ano civil** no seed; por confirmar com o jurídico.
- **Datas da lei do mapa** em `rh.ferias.mapa.*` (properties + env var), não em tabela: nenhuma
  instituição as pode mudar em CV.
- **Mapa:** marcar ≠ gozar; **não há aprovação**, há publicação (segunda vez = 409); preferência
  fora de prazo **aceite com alerta**; alteração pós-publicação exige `ACORDO` ou
  `CONVENIENCIA_SERVICO` fundamentada — o gatilho é a **publicação**, não o 31 de Março.

**Anteriores (resumo; o detalhe está no git e em `regras_negocio.html`):**
- Ausências ≠ licenças (curto vs prolongado, DL 3/2010). Mobilidade não é ausência e não toca na
  afectação (art. 135.º n.º 7); definitiva = transferência.
- Uma cadeira, um titular (V45); `POST /assignments` só `PRINCIPAL`; substituição sem data de fim.
- Licença: decisão (`status`) ≠ período (derivado das datas); efeitos na data (V48).
- Férias: saldo nasce sozinho (V49); acumulação é acto com motivo (V50); suspensão devolve ao
  próprio ano (V51); meios-dias adiados.
- Antiguidade derivada, não guardada; períodos excluídos **unem-se**, não se somam.
- Falta injustificada desconta antiguidade (regime, V54); opção do art. 43.º n.º 2 no pedido;
  `efeito_remuneracao` é informação, não cálculo.
- **Fora do âmbito por decisão:** permuta, estágio probatório, reintegração judicial. Não reabrir.
- Parametrizar valores, não decisões. Motivos não se validam. Nada de filtrar em memória.
  Nada de respostas `Map`. O que é derivável não se guarda.

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
- JPQL usa o nome `@Entity` (`ColabsFuncionarioEntity`); `year()/month()/day()`; sem nomes
  totalmente qualificados no meio do código.
- `scripts/*.ps1` e `repor_estado.sql` **só ASCII**. Regenerar `openapi.json` quando um endpoint muda.
- O seed usa `ON CONFLICT DO NOTHING`: reclassificar catálogo numa base existente vai no
  `scripts/repor_estado.sql`.
- No bash deste ambiente, **heredocs com texto longo partem** («unexpected EOF») — escrever
  ficheiros com a ferramenta Write; em `docker exec … -f /tmp/x` usar `sh -c "…"` (o Git Bash
  reescreve caminhos); o Python no Windows escreve cp1252 no stdout — gravar com `encoding="utf-8"`.

## Blockers & risks

- **O servidor Java do VS Code escreve em `target/` a meio do Maven**: `application.properties`
  em falta, `NoClassDefFoundError`, «package does not exist», jar sem recursos (chegou a 242 erros
  falsos). **Correr Maven numa cópia isolada** (ver How to verify). Não é regressão.
- **A app lê o `.env` da pasta de onde arranca**: arrancar sempre a partir da raiz do projecto,
  senão `Could not resolve placeholder 'AUTH_JWT_ISSUER'`.
- Docker Desktop pode estar em baixo ao retomar: `postgres-ingt-rh` tem de estar de pé **antes**
  dos testes (o `contextLoads` corre o Flyway na base real, porta 5436).
- **Tabelas sem migração** (`t_ferias_*`, `t_public_holiday` antes da V55): com
  `HIBERNATE_DDL=validate` em produção não nascem — questão 9.
- **Bateria desactualizada**: o `repor_estado.sql` passa 15 tipos de ausência a `DIAS_SEGUIDOS`;
  blocos que afirmem `numeroDias` de faltas que atravessem fim-de-semana podem falhar **sem bug**.
- A `areaCkey` não validada: um ckey mal escrito faz o feriado não contar para ninguém, sem erro.
- `FeriasDoAno` persiste períodos por `clear()`+reinserção (orphanRemoval); as alterações só
  crescem — o adaptador acrescenta as que o domínio tem a mais, por contagem.

## Relevant files

- `colaboradores/application/services/CalendarioFeriadosService.java` — feriados de um colaborador
  num período (área, herança da unidade-mãe, mobilidade).
- `colaboradores/domain/models/Feriado.java` — `ocorrenciasEntre` (recorrência).
- `colaboradores/domain/service/DiasUteisCalculator.java` — os dois modos de contagem.
- `colaboradores/application/commands/CreatePedidoAusenciaCommandHandler.java:52` — feriados do
  período e contagem do tipo; os três tectos a seguir.
- `parametrizacoes/domain/models/ContagemDias.java` · `parametrizacoes/domain/models/PublicHoliday.java`.
- `colaboradores/domain/models/FeriasDoAno.java:125` — `marcar`; `:161` — regras do art. 5.º.
- `colaboradores/application/services/MapaFeriasService.java` — direito do ano, dias úteis,
  publicação. `colaboradores/config/FeriasParametros.java` — `rh.ferias.mapa.*`.
- `colaboradores/interfaces/rest/MapaFeriasController.java` — 5 endpoints.
- `db/migration/V55__feriados_recorrencia_e_area.sql` · `db/migration/V56__leave_type_contagem.sql`.
- `db/seed/seed_parametrizacoes.sql` — feriados (§7) e tipos de ausência classificados (§5).
- `scripts/repor_estado.sql` · `scripts/testes_funcionais.ps1` · `scripts/testes_funcionais_README.md`
  · `scripts/verificar_handoff.py`.
- `docs/funcionarios/v5/regras_negocio.html` (BR-PH-01..07, BR-AUS-20..23, BR-FER-13..20) ·
  `docs/funcionarios/v5/api_guide.md` (§6.2d, §6.6, §9.1) ·
  `docs/funcionarios/v5/breaking_change_frontend.md` (§11.21–11.23 + checklist) ·
  `docs/funcionarios/v5/modelo_relacional.html` · `docs/funcionarios/v5/openapi.json`.

## How to verify / resume

```bash
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-26.0.2.10-hotspot"
cd /c/Users/ivanick.santos/Nick-personal/ta-workspace/projects/Recursos_Humanos
git status -sb && git log --oneline -4          # commit do handoff no topo, 6114faa8 logo abaixo; ahead 26
docker start postgres-ingt-rh                  # antes dos testes

# Suite na copia isolada (o VS Code nao lhe toca):
S=<scratchpad>                                  # pasta temporaria da sessao
rm -rf "$S/rh" && mkdir -p "$S/rh" && tar --exclude=./target --exclude=./.git -cf - . | tar -xf - -C "$S/rh"
(cd "$S/rh" && mvn -B clean test)               # esperado: Tests run: 961, Failures: 0, Errors: 0

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
0000003 Joana (`91e1…e903`). Tipos: LUTO `e1e1…e1e5`, SEMINARIO `…e1f2`, TE_PESQUISA `…e1f3`.

## Test / validation plan

A **bateria só corre no fim** (regra do utilizador), e tem de ganhar blocos para o que esta sessão
fez. Provas manuais já feitas contra a app (2026-09-23), a transformar em blocos:

| Bloco | Setup → acção | Esperado |
|---|---|---|
| Feriados | Maria, `POST /funcionarios/{id}/pedidos-ausencia` tipo sem saldo, 2026-12-24 a 2027-01-04 | `numeroDias` 6 (Natal + 1/1/2027 recorrente) |
| Feriados | criar `AREA_GEOGRAFICA` TST_PRAIA; feriado municipal 2027-02-10 com área; pedido 8–12/2 | 5; depois de `PUT` da unidade `31e1…e303` com `areaCkey` → 4 |
| Feriados | feriado com área inexistente / nacional com área / nacional duplicado | 201 (sem validação) / 422 / 409 |
| Contagem | LUTO 2026-10-02 (sex) a 10-05 (seg) | 4 |
| Contagem | SEMINARIO qui 10-08 a qua 10-14 · seg 10-19 a sex 10-23 | 422 (7 > 5) · 5 |
| Contagem | TE_PESQUISA 10-30 a 11-02 | 2 |
| Contagem | `PUT leave-types/{id}` sem `contagem` · com `CORRIDOS` | mantém · 422 |
| Mapa | `PUT /funcionarios/{Maria}/ferias/2027/preferencia` Agosto | 200, sem alertas |
| Mapa | `PUT …/marcacao` ACORDO 2027-08-02..31 | 200; `GET` dá 22 dias úteis, direito 22 |
| Mapa | Francisco FIXADA em Dezembro | 422 (janela Maio–Outubro) |
| Mapa | `GET /ferias/mapa/2027` | 1 linha (0000002), 2 em `semMarcacao` |
| Mapa | `POST /ferias/mapa/2027/publicar` duas vezes | 201 com alerta · 409 |
| Mapa | alterar Maria sem motivo · com `motivoAlteracao: ACORDO` | 422 · 200 e `alteracoes[0]` com antes/depois |

**Rever antes** os blocos existentes que verificam `numeroDias` de LUTO/DOENCA/CASAMENTO/etc.:
com a V56 passam a dias seguidos. Documentar no `scripts/testes_funcionais_README.md`.

## Open questions

- Limite de substituições por pessoa — RH. Promoção exige Lugar vago? — cliente.
- `HIBERNATE_DDL` em produção e lock dos jobs (9, 13) — utilizador; agora pesa mais, com tabelas
  sem migração.
- Jurídico: diploma de desenvolvimento da Lei 20/X/2023 (horários — **pré-requisito da
  assiduidade**), diploma da mobilidade, e o período dos 6 dias do art. 77.º n.º 3.
- Mapa: preferência pelo próprio em `/me`? avisar pedido de férias fora da marcação? cônjuges
  (art. 5.º n.º 6) — sem ligação entre colaboradores. Validar `areaCkey` quando o ecrã existir?

### Framework de jobs (por último)

Portar do `inss_core_service` (branch `dev-pre-release`, em
`C:\Users\ivanick.santos\Nosi-work\projects_nosi_workspace\projects\inss_core_service`): docs em
`docs/schedulers/README.md` desse repositório; código em `shared/application/services/scheduler`
(ScheduledJob, JobRunner, SchedulerService, SchedulerSweeper). Renumerar as migrações dele.
Ideias-chave: período vem de `agendadoPara`, parâmetros gravados na abertura, registo em
`REQUIRES_NEW`, retry só em FALHA/TIMEOUT.

### Assiduidade (ponto 4)

Por tijolos: modalidades de horário e horário do colaborador → registo diário (entrada, saída,
intervalos; art. 164.º n.º 3 da Lei 20/X/2023 obriga) → horas por dia/semana → faltas por débito
(art. 13.º n.º 2 DL 3/2010) e ausências curtas que somam (n.º 4) → trabalho suplementar (registar
e classificar, **não pagar**) → dispensa de amamentação em minutos (art. 20.º). Os limites vêm de
diploma de desenvolvimento que não temos: **parametrizar sem inventar omissões**. Meios-dias
voltam à mesa aqui.

## Next step

Apresentar ao utilizador o **desenho do primeiro tijolo da assiduidade** (modalidades de horário e horário
do colaborador) lendo antes os arts. 164.º–165.º em `.lei20.txt` — **sem implementar** até ele
aprovar.
