> Updated: 2026-09-19 14:10

## Goal

Alinhar o núcleo RH (não-sigdi) com a legislação cabo-verdiana, movimento a
movimento, com regras documentadas, testadas **e exercitadas contra a base de
dados**. O objectivo de fundo é a aplicação ser instalável em qualquer
instituição: o que a lei fixa vive em código; o que varia com a instituição ou
com o motivo vive em catálogo.

Construção **tijolo a tijolo**: um ponto por commit, com testes e documentação a
par, sem avançar enquanto o anterior não estiver verde.

## Current state

**Branch `fix-alinhamento-legislacao`**, 34 commits por enviar (`origin/dev` e
`origin_git_lab/dev`). **Nada foi enviado para nenhum remoto.**

- **Testes unitários: 763, 0 falhas.** Correr sempre com `clean` (ver Blockers).
- **Bateria funcional: 207 passos, 207 OK** (2026-09-19), contra a BD local. **Cobre F0 a F10 — o plano de validação está completo.**
- **Migrações V40 a V47 aplicadas e verificadas na BD.** Próxima livre: **V48**.
- **`openapi.json` regenerado**: 225 caminhos, 238 esquemas, **0 operações
  não-sigdi sem esquema de resposta**.

Commits desta sessão (mais recente primeiro):

| Commit | O que fecha |
|---|---|
| `df71e027` · `d8b33e24` · `9a632376` · `7b46b044` | Este handoff, em quatro passos |
| `6fe7b778` | **F9 promoção + F10 contrato das respostas** (172 → 207 passos) |
| `e8fd22f1` | **F7 cessação + F8 mobilidade** (135 → 172 passos) |
| `82c0f998` | F6 da bateria + seed (78 → 111 passos) |
| `c93274fe` | Plano do que falta antes da bateria |
| `d034c31e` | Últimos resíduos de `Map` + docs v5 desalinhados |
| `cadfb788` | Fim das respostas em `Map` — nenhuma sobra |
| `9ecfc207` | `SuccessResponseDTO` em 85 operações |
| `bd0a7f26` | V47 larga `origin_assignment_id` |
| `f47bc974` | Regras da mobilidade que descreviam código apagado |
| `d4ded55b` | **Substituição** (V46) |
| `57c908c3` | Docs v5 alinhados + `openapi.json` gerado |
| `d0417100` | **Uma cadeira, um titular** (V45) + enum `TipoAfectacao` |
| `d9808ec7` | Suite verde (as 4 falhas "conhecidas" eram testes desalinhados) |

### O plano de alinhamento, ponto a ponto

**Feito:** situações funcionais art. 117.º (V42) · licenças que abrem vaga (V43)
· ciclo do saldo de ausências · contrato com estado obrigatório (V44) ·
**substituição** (V45+V46) · **respostas tipadas** · limpeza do
`origin_assignment_id` (V47).

**Por fazer — quatro pontos**, detalhados em "Test / validation plan" e abaixo:

1. **Férias (DL 3/2010)** — o saldo funciona; falta marcação, acumulação e gozo
   proporcional ao tempo de serviço.
2. **Dívida do catálogo** — `affects_pay`, `counts_for_seniority` (subtipo) e
   `counts_seniority` (vínculo) existem e **ninguém os lê**;
   `SituacaoFuncional.contaAntiguidade()` também não tem consumidor porque **não
   há cálculo de antiguidade em lado nenhum**. É aí que os três se ligam.
3. **Movimentos menores** — consolidação da mobilidade (art. 132.º n.º 4) ·
   acumulação (art. 134.º n.º 2 al. b; `ACUMULACAO` existe no enum e **nunca é
   usado**) · permuta (atómica) · **mudança de carreira** (art. 139.º / art. 35.º
   PCFR — hoje **sem caminho nenhum**: a promoção exige a mesma carreira e a
   transferência a mesma categoria) · regresso de comissão (art. 64.º n.º 2) ·
   estágio probatório (art. 57.º, 72.º) · reintegração judicial (art. 97.º n.º 10
   al. b).
4. **Percurso do colaborador** — linha temporal única. Adiado até o negócio estar
   definido.

## Decisions made — do not re-litigate

- **Mobilidade transitória não toca na afectação** (art. 135.º n.º 7): o titular
  mantém o Lugar; encerrar é fechar o registo. O destino é `destinationUnitId`
  (interna) ou `entidadeDestino` (externa). `destinationPositionId` é **legado**.
- **Mobilidade definitiva = transferência.**
- **Promoção**: a modalidade infere-se do pedido (com `positionId` muda de Lugar;
  sem ele o Lugar sobe de categoria) e **não se persiste**.
- **Uma cadeira, um titular**: o índice único do Lugar é parcial em `PRINCIPAL`
  (V45). Substituição e acumulação **não disputam** a titularidade.
- **Provido/vago deriva do titular**, não de "afectação corrente".
- **Substituição**: quem pode ser substituído lê-se da **situação funcional**
  (`ACTIVIDADE_FORA_QUADRO` ou `INACTIVIDADE_NO_QUADRO`), nunca de códigos. Não
  tem data de fim: caduca quando o titular regressa (art. 77.º n.º 2). Não
  encerra a afectação de quem substitui (art. 91.º n.º 1 al. a)).
- **Fora do âmbito da substituição: férias e faltas curtas.** Quem está de férias
  continua em `ACTIVIDADE_NO_QUADRO` (art. 118.º) e aprovar uma ausência não toca
  no estado do trabalhador. Se a instituição quiser cobrir férias longas, cria um
  estado classificado como `INACTIVIDADE_NO_QUADRO` — **sem mexer em código**.
- **`INACTIVE` tem `ends_employment=true` e situação nula, de propósito**: modela
  as cessações que não são aposentação (exoneração, caducidade, mútuo acordo). A
  lei não o conta entre as seis situações do art. 117.º porque quem cessa deixa de
  ter situação perante o quadro. **Não é um buraco do seed.** O nome é que engana:
  `SUSPENDED` e `INACTIVE_OUTSIDE` são inactividade e **não** cessam nada.
- **Nada de respostas `Map`**: `SuccessResponseDTO` (id, sucesso, alertas) nas
  operações simples; DTO próprio quando há mais a dizer. O campo `message`
  desapareceu de 85 operações.
- **Motivos não se validam** (`motivoCkey`, `terminationReason`, `ccode` do
  `Option`): são parametrizados pela instituição.
- **O que é derivável não se guarda** (provido/vago, vínculo laboral, "onde
  exerce funções", efeitos da situação funcional).
- **Parametrizar valores, não decisões.** Colunas para o que a lei fixa e varia
  (dias, limites, situação); nada de interruptores para decisões não tomadas.
- **Valores fechados viram enum**: `EstadoContrato`, `EstadoPedidoAusencia`,
  `SituacaoFuncional`, `TipoRegisto`, `EfeitoNoLugar`, `EfeitoNoRegresso`,
  `TipoAfectacao`. **Falta**: origem da afectação, estado do Lugar
  (`Position.ATIVO`), estados de `LicencaMobilidade`.
- **O SIGDI fica de fora.** O RH regista a progressão e a promoção; não verifica
  elegibilidade. O DL 4/2024 art. 36.º remete os limiares para o diploma da gestão
  de desempenho.
- **Licença parental é ausência** (art. 171.º n.º 2 vs 172.º): `MATERNIDADE` 90
  dias, `PATERNIDADE` 10 dias úteis.
- **Nada de filtrar em memória**: filtros e somas vão para a consulta.

## Constraints

- **Build exige JDK 26**: `export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-26.0.2.10-hotspot"`.
- **Migrações Flyway sempre defensivas**: `to_regclass` para a tabela +
  `information_schema.columns` para a coluna. V40 a V47 são o modelo.
- **Confirmar o número livre** antes de criar a migração.
- Controladores em `interfaces/rest/` são gerados pelo IGRP: lógica nos handlers,
  e **manifesto `.igrpstudio` actualizado a par do Java** (acção + DTOs + entity).
- **Um `@ApiResponse` com `@Content` sem `schema` apaga o tipo inferido** — o
  contrato sai com a resposta vazia mesmo com o Java a devolver um DTO. Ao
  acrescentar endpoints, declarar
  `schema = @Schema(implementation = XptoDTO.class)` (ou `array = @ArraySchema(...)`).
- **Nomes totalmente qualificados no meio do código não passam**: usar imports.
- JPQL usa o **nome `@Entity`**, não o da classe (`ColabsAssignmentEntity`,
  `ColabsPedidoAusenciaEntity`). Uma `@Query` errada passa nos testes unitários e
  só rebenta no arranque.
- Para o ano em JPQL: `year(campo)`, nunca `FUNCTION('YEAR', campo)`.
- **`scripts/*.ps1` só com ASCII**: o PS 5.1 lê como ANSI e um acento parte o
  ficheiro a meio.
- Documentação em **pt-PT**; commits `feat|fix|refactor|test|docs(<módulo>): …`.
- **Regenerar `openapi.json`** sempre que se acrescente ou mude um endpoint.

### Escrever blocos da bateria — lições já pagas

- **Navegar antes de agir.** Cada bloco obtém os ids da própria API (unidades,
  vagas, categorias, escalões, catálogos). Nenhum id é escrito à mão.
- **Papéis pelo `numeroFuncionario`, nunca pela posição na lista.** A ordem de
  `/funcionarios` muda com os dados: numa execução o "A" era o Francisco, na
  seguinte era a Joana, e os passos seguintes liam a pessoa errada **sem falhar**.
  Há um helper `PorNumero` no topo do script.
- **Datas ancoradas em `Get-Date`.** "Em vigor" (mobilidade, licença) quer dizer
  que o registo **cobre hoje** — `mobilidadeEmVigor` usa `LocalDate.now()`. Datas
  fixas em anos futuros fazem o teste falhar sem haver bug.
- **`.Count` precisa de `@()` a envolver o pipeline todo** no PS 5.1:
  `@(@(Linhas $r) | Where-Object {...}).Count`. Sem isso, um único resultado não
  conta como 1.
- **O `close` do contrato é idempotente** (200, não 409) e não sobrepõe a data
  nem o motivo do primeiro. O mesmo vale para desactivações repetidas de
  catálogo, que devolvem 409 — confirmar caso a caso em vez de assumir.
- **Enviar sempre `Accept: application/json`.** Sem o cabeçalho o servidor
  negoceia e devolve os **erros em XML** (ProblemDetail) enquanto os sucessos vêm
  em JSON — dois formatos na mesma API. A função `Chamar` já o envia.
- **Repor a BD antes de cada execução** (`scripts/repor_estado.sql`).

## Blockers & risks

- **O compilador incremental do Maven mente.** Depois de mexer em lotes de
  ficheiros, `mvn test` sem `clean` diz *"Nothing to compile"* (escondendo erros)
  ou rebenta com `NoClassDefFoundError: CareerRequestDTO` numa classe que existe,
  está compilada e está importada. **Usar sempre `mvn -B clean test`.** Custou
  tempo duas vezes.
- **Jar preso**: se a app estiver a correr, `clean`/`repackage` falha. Fechar
  primeiro (ver How to verify).
- **Jar sem recursos**: um `package` sem `clean` pode gerar um jar incompleto.
  Confirmar sempre `jar tf … | grep classes/application.properties`.
- **Testes unitários não vêem erros de contrato.** Mockam o `commandBus`, por isso
  um handler e um controlador com tipos diferentes passam nos testes e rebentam em
  runtime — aconteceu (`ClassCastException` no `ReferenceOptionsController`). **Só
  a bateria os apanha.**
- **A bateria deixa a BD alterada.** Repor antes de cada execução.
- **Risco por verificar**: cessação, mobilidade ponta a ponta e promoção nas duas
  formas **nunca correram contra a BD** — são o F7, F8 e F9.

### Achado por decidir: licença que acaba antes de começar

`LicencaMobilidade.encerrar()` põe a data de fim em **hoje** quando a licença
ainda não terminou, sem verificar se hoje é anterior ao **início**. Quem aprova
uma licença futura e a encerra logo fica com um período incoerente — visto na BD:
`LIC_FORMACAO, inicio 2026-10-01, fim 2026-09-19`. Não bloqueia nada hoje, mas
falseia qualquer contagem de dias. **Decidir**: recusar o encerramento antes do
início, ou fixar `dataFim = max(hoje, dataInicio)`.

### Lacuna conhecida: não há como ler as substituições

`POST /funcionarios/{id}/substituicao` devolve o id, mas **nada o lista depois**.
`GET .../unidade-atual` só devolve a afectação `PRINCIPAL`; não há endpoint que
diga "este Lugar tem substituto" nem "esta pessoa está a substituir alguém".

Consequências:
- um ecrã de RH **não consegue mostrar** quem substitui quem;
- na bateria, o fecho automático da substituição teve de ser provado
  **indirectamente** (F6.30-33): põe-se o titular impedido outra vez e tenta-se
  nova substituição — se a anterior não tivesse fechado daria `409`, e dá `201`.

Candidato a endpoint próprio (`GET /colaboradores/assignments/funcionario/{id}`
ou `.../position/{id}/ocupantes`) ou a entrar no ponto 4 (percurso do
colaborador). **Decisão por tomar.**

## Relevant files

- `colaboradores/application/services/SubstituicaoService.java` — regras da
  substituição; `encerrarPorRegressoDoTitular` tem duas assinaturas (por
  `AssignmentId` e por `FuncionarioId`).
- `colaboradores/application/services/AssignmentService.java:106`
  `validarLugarParaAfectacao` — validações de Lugar partilhadas entre afectar e
  substituir. **Não** inclui a regra do titular único, que depende do título.
- `parametrizacoes/domain/models/SituacaoFuncional.java` — as seis situações e
  `permiteSubstituicao()`.
- `colaboradores/domain/models/TipoAfectacao.java` — PRINCIPAL/SUBSTITUICAO/ACUMULACAO.
- `shared/application/dto/SuccessResponseDTO.java` — `de()`, `semEfeito()`.
- `db/migration/V45`, `V46`, `V47` — índice parcial, ligação ao titular, limpeza.
- `db/seed/seed_carreiras.sql` — `ordem_progressao` (1=ASS_TEC, 2=TEC_SUP).
- `db/seed/seed_colaboradores.sql` — 3 colaboradores, 6 Lugares.
- `scripts/testes_funcionais.ps1` — bateria completa (F0 a F10, 207 passos).
- `scripts/repor_estado.sql` — reposição, **correr antes de cada execução**.
- `scripts/testes_funcionais_README.md` — o que cada bloco cobre.
- `docs/funcionarios/v5/openapi.json` — contrato gerado; **fonte para as formas**.
- `docs/funcionarios/v5/api_guide.md` — §5.7 substituição, §12 enums, §2 respostas.
- `docs/funcionarios/v5/regras_negocio.html` — BR-*, incluindo BR-SUB-01..09.
- `docs/funcionarios/v5/breaking_change_frontend.md` — §11.4 titular, §11.6 e
  §11.7 respostas tipadas.

## How to verify / resume

```bash
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-26.0.2.10-hotspot"
cd /c/Users/ivanick.santos/Nick-personal/ta-workspace/projects/Recursos_Humanos
git switch fix-alinhamento-legislacao

mvn -B clean test          # esperado: 763 testes, 0 falhas  (COM clean)
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
# esperado: PASSOS: 111   OK: 111   FALHAS: 0

# 4. regenerar o contrato depois de mexer em endpoints
curl -s -o docs/funcionarios/v5/openapi.json http://localhost:8099/v3/api-docs
```

Ambiente: BD no contentor **`postgres-ingt-rh`**, porta **5436**, base
`recursoshumanos_db`, user `postgres`. App na porta **8099** (não 8091 — o `.env`
local manda). Erro esperado e inofensivo no arranque:
`AuthorizationSyncRunner` falha porque o URL do Access Management está vazio.

Se o seed for recarregado de raiz:

```bash
docker exec postgres-ingt-rh sh -c "mkdir -p /tmp/seed"
docker cp src/main/resources/db/seed/. postgres-ingt-rh:/tmp/seed/
docker exec postgres-ingt-rh sh -c "cd /tmp/seed && psql -U postgres -d recursoshumanos_db -q -f master_seed.sql"
```

Estado esperado depois de repor: Francisco Bastos em LUG-0001 (TEC_SUP, esc. 1),
Maria Santos em LUG-0002 (ASS_TEC, esc. 1), Joana Tavares em LUG-0003 (ASS_TEC,
esc. 1). Vagos: **LUG-0004** (TEC_SUP, para a promoção com `positionId`),
**LUG-0005** (ASS_TEC, para a transferência), **LUG-0006** (CONGELADO, para o 422).

## Test / validation plan

**A bateria cobre F0 a F10 — 207 passos, todos OK.** Não há blocos por escrever.

**O que cada bloco prova está em `scripts/testes_funcionais_README.md`**, e é lá
que deve ser actualizado. Não se repete aqui de propósito: duas cópias da mesma
coisa divergem sempre — foi o que aconteceu com os `.html` gémeos dos documentos
v5, que andaram meses a contradizer o código.

### Como correr

Ver "How to verify / resume" acima. Em resumo: app a correr na 8099, **repor a BD
com `scripts/repor_estado.sql`**, e `powershell -File scripts/testes_funcionais.ps1`.
Esperado: `PASSOS: 207   OK: 207   FALHAS: 0`.

### Se acrescentares um bloco novo

- Entra **antes** do `=========== RESUMO ===========`.
- Segue as "lições já pagas" em Constraints (navegar antes de agir, papéis pelo
  `numeroFuncionario`, datas em `Get-Date`, `@()` no `.Count`, só ASCII).
- **Os blocos são encadeados**: cada um herda o estado que o anterior deixou. No
  fim do F10 a base fica assim (confirmado a 2026-09-19):

  | Colaborador | Estado | Lugar |
  |---|---|---|
  | 0000001 Francisco Bastos | `INACTIVE` (cessado no F7) | sem Lugar |
  | 0000002 Maria Santos | `ACTIVE` | um Lugar de TEC_SUP |
  | 0000003 Joana Tavares | `RETIRED` (cessada no F7) | sem Lugar |

  Um bloco novo que precise de alguém activo **com** Lugar tem de usar a Maria, ou
  reafectar alguém primeiro — é o que o F6.9, o F8.6 e o F9.6 fazem.
- **O F9 reclassifica um Lugar** (a promoção sem `positionId` faz o Lugar subir de
  categoria). O `repor_estado.sql` repõe as categorias dos seis Lugares; se
  acrescentares Lugares ao seed, acrescenta-os lá também.

## Open questions

1. **Ler as substituições** — não há endpoint (ver Blockers). Endpoint próprio ou
   dentro do percurso do colaborador? **Decide o RH/produto.**
2. **Limite de substituições por pessoa** — hoje não há nenhum: a mesma pessoa
   pode cobrir vários Lugares. **Validar com o RH.**
3. **A promoção exige lugar vago da categoria superior?** As duas formas estão
   suportadas; a prática da instituição decide. **Cliente.**
4. **`ACUMULACAO`** — está no enum e não tem endpoint, serviço nem regra. Desde a
   V45 o índice já não a bloqueia, por isso `POST /assignments` com
   `assignmentType=ACUMULACAO` cria uma segunda afectação **sem validação
   nenhuma**. Decidir: fechar com 422 até haver regras, implementar como
   modalidade de mobilidade (art. 134.º n.º 2 al. b), ou como título próprio.
5. **Licença que acaba antes de começar** — `LicencaMobilidade.encerrar()` fixa a
   data de fim em **hoje** sem verificar se hoje é anterior ao **início**. Quem
   aprova uma licença futura e a encerra logo fica com um período incoerente.
   Visto na BD a 2026-09-19: `LIC_FORMACAO, início 2026-10-01, fim 2026-09-19`.
   Não bloqueia nada, mas **falseia qualquer contagem de dias** — e a contagem de
   dias é a base das férias (ponto 1) e da antiguidade (ponto 2), por isso convém
   decidir **antes** desses. Opções: (a) recusar o encerramento antes do início
   com 422; (b) `dataFim = max(hoje, dataInicio)`; (c) permitir e tratar o
   período como nulo nas contagens. **Decide o RH/produto.**
6. **Produção**: `ddl-auto` vem de `${HIBERNATE_DDL:update}` e os
   `application-<perfil>.properties` estão vazios. Sem `HIBERNATE_DDL` definida, o
   Hibernate altera o esquema por baixo do Flyway. **Adiado — ainda não há
   produção** (confirmado pelo utilizador).
7. **Jurídico**: confirmar se algum diploma substituiu o DL 3/2010 e qual é o
   diploma da mobilidade.

## Next step

A bateria está completa e verde. O próximo passo é **funcionalidade**, e a ordem
depende da decisão 5 (licença que acaba antes de começar), porque falseia
contagens de dias:

1. **Decidir a questão 5** — é barata de resolver e desbloqueia as duas seguintes.
2. **Férias (DL 3/2010)** — marcação, acumulação e gozo proporcional. O saldo já
   funciona e o F4 cobre-o.
3. **Dívida do catálogo** — as três colunas que ninguém lê, quando houver cálculo
   de antiguidade.

Antes de mexer: `mvn -B clean test` (763, 0 falhas), repor a BD e correr a
bateria (207/207) para confirmar que se parte de verde.
