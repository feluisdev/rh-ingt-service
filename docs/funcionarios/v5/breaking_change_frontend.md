# Breaking Changes — Frontend (v5, Position Management)

> Guia de migração do frontend para o novo modelo de **Mapa de Pessoal** (Lugares + Afectações).
> Refactor assumido como *breaking change*. pt-PT.

## TL;DR — o que muda

1. **O registo de colaborador deixa de enviar unidade/cargo/carreira/categoria.** Passa a enviar um **`positionId`** (Lugar vago) + `gradeId`. Tudo o resto deriva do Lugar.
2. **Enquadramento e Colocação desaparecem.** São substituídos por uma única **Afectação** (`assignment`).
3. **Novos ecrãs:** gestão do Mapa de Pessoal (criar/editar Lugares, chefias) e **picker de Lugar vago** na admissão.
4. **Novas queries** para chefia, responsável de unidade, unidade atual e vagas.
5. **Mobilidade** identifica o destino por `destinationUnitId` (interna) ou `entidadeDestino` (externa) — e **não toca na afectação**.

---

## 1. Registo de colaborador (BREAKING)

**Antes** — enviava-se o enquadramento (unidade + cargo + carreira + categoria) e a colocação.

**Agora** — `POST /api/v1/rh/funcionarios/registar`:
```jsonc
{
  "funcionario": { /* … */ },
  "contrato": { /* opcional */ },
  "afectacao": {
    "positionId": "uuid",     // ← Lugar vago escolhido no picker
    "gradeId": "uuid|null",   // escalão (obrigatório se o Lugar é de carreira)
    "origem": "ADMISSAO",
    "dataInicio": "YYYY-MM-DD"
  },
  "dadosBancarios": { /* opcional */ },
  "dossier": { /* opcional */ }
}
```

**Resposta** — `RegistarColaboradorResponseDTO`:
```jsonc
{
  "funcionarioId": "uuid",
  "numeroFuncionario": "…",
  "contratoId": "uuid|null",
  "afectacaoId": "uuid",        // ← antes era enquadramentoId
  "dadosBancariosId": "uuid|null",
  "documentoIds": ["uuid", …],  // do dossier
  "message": "…"
}
```
**Já não existe `enquadramentoId`.**

| Campo removido do request | Substituído por |
|---|---|
| `unidadeOrganicaId` | derivado do Lugar |
| `cargoId` / `jobId` | derivado do Lugar |
| `careerId` / `categoryId` | derivado do Lugar |
| bloco `enquadramento` | bloco `afectacao` (`positionId` + `gradeId`) |
| bloco `colocacao` | (fundido na afectação) |

**Ação FE:** no ecrã de admissão, a unidade/cargo/carreira/categoria passam a **só-leitura**, preenchidas a partir do Lugar selecionado.

---

## 2. Picker de Lugar vago (novo ecrã/componente)

Para admitir, o utilizador escolhe um **Lugar vago** da unidade:

```
GET /api/v1/rh/colaboradores/assignments/unidade/{unidadeId}/vagas/lista
→ WrapperListaPositionDTO (Lugares vagos, com nomes resolvidos)
```

Cada Lugar traz `jobNome`, `unidadeNome`, `careerNome`, `categoryNome`, `foraDeGrelha`, `ocupado`. Ao selecionar, preencher os campos derivados e (se `foraDeGrelha=false`) pedir o **escalão** (`gradeId`).

> Filtrar o picker de escalões pela categoria do Lugar (`grade.categoryId == position.categoryId`) — a API ainda não valida isto no servidor.

---

## 3. Gestão do Mapa de Pessoal (novo ecrã)

CRUD de Lugares em `/api/v1/rh/estrutura/positions`:

| Ação | Endpoint |
|---|---|
| Listar por unidade (+ vagas) | `GET /positions?unidadeId={id}` |
| Criar Lugar | `POST /positions` |
| Editar | `PUT /positions/{id}` |
| Congelar | `PATCH /positions/{id}/freeze` |
| Extinguir | `DELETE /positions/{id}` |

Ao criar, definir chefias:
- `parentPositionId` — a quem o Lugar reporta.
- `managesUnitId` — a unidade que este Lugar **dirige** (responsável).

O card de unidade pode mostrar **dotação / ocupados / vagas** (vêm no wrapper da lista).

---

## 4. Consultas de estrutura viva (novas)

| Pergunta | Endpoint |
|---|---|
| Onde está o funcionário? | `GET /assignments/funcionario/{id}/unidade-atual` |
| Quem é o chefe dele? | `GET /assignments/funcionario/{id}/chefe` |
| Quem responde pela unidade? | `GET /assignments/unidade/{id}/responsavel` |
| Quantas vagas tem a unidade? | `GET /assignments/unidade/{id}/vagas` |

---

## 5. Afectação direta (progressão, promoção, transferência)

Para movimentos fora do registo inicial: `POST /api/v1/rh/colaboradores/assignments`:
```jsonc
{
  "funcionarioId": "uuid", "positionId": "uuid",
  "gradeId": "uuid|null", "origem": "PROGRESSAO|PROMOCAO|TRANSFERENCIA",
  "dataInicio": "YYYY-MM-DD"
}
```
A afectação anterior é fechada automaticamente (histórico).

---

## 6. Mobilidade (BREAKING — o destino mudou de forma)

> **Esta secção foi reescrita.** Uma versão anterior deste documento dizia que a mobilidade
> passava a exigir `destinationPositionId` e que a aprovação criava uma afectação no Lugar de
> destino. Isso contradizia o art. 135.º n.º 7 da Lei n.º 20/X/2023 — na mobilidade transitória
> **o titular mantém o Lugar** — e foi revertido. O que vale é o que está aqui.

O destino identifica-se pela **unidade** ou pela **entidade**, e nunca por um Lugar:

```jsonc
{
  "subtipoId": "uuid",
  "destinationUnitId": "uuid | null",   // mobilidade INTERNA — outra unidade nossa
  "entidadeDestino": "string | null",   // mobilidade EXTERNA — autarquia, empresa pública…
  "dataInicio": "YYYY-MM-DD",
  "dataFim": "YYYY-MM-DD | null",
  "justification": "…"
}
```

- `PUT .../licencas-mobilidade/{id}/approve` → **não mexe na afectação**. Regista onde a pessoa exerce funções e até quando.
- `PUT .../{id}/close` → fecha o registo. **Não há regresso a restaurar**, porque nunca se saiu do Lugar.
- Aprovar MOBILIDADE **sem nenhum dos dois destinos** → `422`.
- **`destinationPositionId` é legado** e já não é usado. Um ecrã que ainda o envie não causa erro, mas não produz efeito.
- Para mudar **mesmo** de Lugar existe a **transferência** (secção 5).

---

## 7. Mudança de estado

`PATCH /api/v1/rh/funcionarios/{id}/worker-state` — request **inalterado**.
Efeito novo: `RETIRED`/`INACTIVE` **encerram a afectação corrente** → o Lugar volta a vago.

---

## 8. Endpoints removidos / substituídos

| Removido | Usar |
|---|---|
| `GET/POST /funcionarios/{id}/enquadramentos` | `POST /colaboradores/assignments` |
| `GET/POST /funcionarios/{id}/colocacoes` | `POST /colaboradores/assignments` |
| auditoria catálogo `enquadramentos` | catálogo `assignments` |

O bloco `enquadramento` **ainda aparece** em `GET /funcionarios/{id}/details` (mesma forma, por compatibilidade) — mas é **derivado** do Lugar; não há CRUD de enquadramento.

---

## 9. Enumerações (para dropdowns)

| Enum | Valores |
|---|---|
| `origem` | ADMISSAO · PROGRESSAO · PROMOCAO · MOBILIDADE · TRANSFERENCIA |
| `assignmentType` | PRINCIPAL · SUBSTITUICAO |
| `estado` (Lugar) | ATIVO · CONGELADO · EXTINTO |

**Provido/Vago** não é enum — usar o campo booleano `ocupado` das respostas de Lugar.

---

## 10. Checklist de migração FE

- [ ] Ecrã de admissão: remover inputs de unidade/cargo/carreira/categoria; adicionar **picker de Lugar vago** + escalão.
- [ ] Trocar `enquadramentoId` por `afectacaoId` na leitura da resposta de registo.
- [ ] Novo ecrã de **gestão do Mapa de Pessoal** (Lugares + chefias + vagas).
- [ ] Mobilidade: adicionar campo **`destinationPositionId`**.
- [ ] Remover chamadas a `/enquadramentos` e `/colocacoes`.
- [ ] Usar as novas queries (unidade-atual, chefe, responsavel, vagas) onde antes se lia colocação/enquadramento.
- [ ] Cabeçalho `Accept: application/json` em todas as chamadas.

> Contrato completo: `api_guide.md`. Modelo: `modelo_negocio.html`, `modelo_relacional.html`.

---

## 11. Alinhamento com a legislação (2026-09-18)

Três mudanças que se notam no front-end.

### 11.1 Estados do trabalhador ganham situação funcional

O catálogo de estados (`/catalogs/worker-states`) passa a ter **`situacaoFuncional`**, com um dos seis valores do art. 117.º da Lei n.º 20/X/2023: `ACTIVIDADE_NO_QUADRO`, `ACTIVIDADE_FORA_QUADRO`, `INACTIVIDADE_NO_QUADRO`, `INACTIVIDADE_FORA_QUADRO`, `DISPONIBILIDADE`, `APOSENTACAO`. Pode vir **nulo**, nos estados ainda por classificar.

- No ecrã de estados, é um select com estes seis valores, mais a opção vazia.
- Um estado de cessação (`endsEmployment`) só aceita `APOSENTACAO` ou nenhuma situação — caso contrário, **422**.
- `PATCH /funcionarios/{id}/worker-state` pode agora devolver `afectacaoEncerradaId` **sem** cessar o vínculo, quando a situação abre vaga. O colaborador fica activo e sem Lugar: o ecrã deve mostrá-lo assim, e não como cessado.

### 11.2 Subtipos de licença/mobilidade

- O valor **`AMBOS` de `recordType` desapareceu**: ficam `LICENCA` e `MOBILIDADE`. Se o front-end o oferecia num select, tem de o retirar.
- Três campos novos: `positionEffect` (`MANTEM`/`ABRE_VAGA`), `vacancyAfterDays` e `returnEffect` (`REGRESSA_LUGAR`/`DISPONIBILIDADE`).
- Marcar um subtipo de **mobilidade** como `ABRE_VAGA` devolve **400**.
- `approve`/`ativar` de uma licença que abre vaga devolve `afectacaoEncerradaId`; `close` pode devolver `estadoAtribuidoId`.
- A **licença parental** deixa de ser subtipo de licença: é ausência (`MATERNIDADE`, `PATERNIDADE`).

### 11.3 Saldo de ausências

- Os dias passam a ser **reservados na submissão**, e não na aprovação: criar um pedido sem saldo suficiente devolve **422** logo aí.
- `diasGozados` passa a ter valores reais — antes ficava sempre a zero.
- O `cancelar` do RH deixa de devolver **403** quando o pedido é de outro colaborador.

### 11.4 O Lugar passa a contar titulares, não ocupantes

Preparação da substituição do funcionário temporariamente impedido (art. 73.º al. a) a c)).

- `assignmentType` passa a ser **validado**: um valor fora de `PRINCIPAL`, `SUBSTITUICAO` devolve **422** (antes era gravado tal e qual). Vazio continua a valer `PRINCIPAL`. Desde 2026-09-22, em `POST /assignments` **só `PRINCIPAL` passa**, e `ACUMULACAO` deixou de existir — ver 11.15.
- A afectação com `assignmentType` diferente de `PRINCIPAL` **deixa de exigir que o Lugar esteja vago**.
- A mensagem de erro do Lugar ocupado mudou de *"já está ocupado"* para *"já tem titular"*. Quem a compare por texto tem de a actualizar.
- **Vagas e provimento passam a contar apenas titulares**: um Lugar com substituto e sem titular conta como **vago** nas contagens da unidade e na lista de Lugares.

### 11.5 Substituição de titular impedido (novo)

Endpoint novo: `POST /funcionarios/{funcionarioId}/substituicao`, onde o `{funcionarioId}` é **o substituto**.

- Novo ecrã/acção a partir do **Lugar**: "pôr alguém a substituir". Só faz sentido quando o Lugar tem titular e o titular está impedido.
- **Não há campo de data de fim** e **não há acção de terminar**: a substituição fecha-se sozinha quando o titular regressa. Um ecrã que peça data de fim está a prometer o que a API não faz.
- O `201` traz `titularId`, `titularNome` e `titularAssignmentId` — é o que o ecrã deve mostrar ("a substituir Fulano").
- **422** quando o titular não está impedido ou o estado dele não tem situação funcional; **409** quando já há substituto.
- Na ficha do colaborador, quem está a substituir pode ter **duas afectações correntes**: o seu Lugar e o que está a cobrir. Um ecrã que assuma uma só afectação corrente tem de ser revisto.

### 11.6 Respostas tipadas — o `message` desapareceu (BREAKING)

**85 operações** deixaram de devolver um objecto livre e passam a devolver `SuccessResponseDTO`:

```json
{ "id": "uuid | null", "sucesso": true, "alertas": [] }
```

São os `POST` de criação, os `PUT` de actualização, as desactivações (`DELETE`) e as reactivações (`activate`) de **todos** os catálogos, da estrutura, das carreiras e dos sub-recursos do colaborador.

- **O campo `message` já não existe.** Um ecrã que mostrasse `response.message` passa a mostrar vazio. O texto de sucesso pertence ao ecrã.
- **O `id` passa a vir sempre**, também nas desactivações e reactivações, onde antes só havia texto.
- **`sucesso: false` não é erro** — é a operação a dizer que não teve nada a fazer porque o alvo já estava no estado pedido, com o motivo em `alertas`. Antes isto vinha como `{"message": "X já está activo"}` com `200`, indistinguível de uma alteração real.
- **`alertas` é sempre um array**, vazio quando não há nada a avisar. Nunca nulo.
- Os erros **não** mudaram: continuam em `400`/`404`/`409`/`422` com o corpo de problema.

### 11.7 As restantes respostas também deixaram de ser objectos livres

Não sobrou nenhuma. O que não cabia no `SuccessResponseDTO` ganhou DTO próprio:

| Endpoint | Antes | Agora | O que muda para o ecrã |
|---|---|---|---|
| `POST /funcionarios` | `{id, numeroFuncionario, message}` | `FuncionarioCriadoResponseDTO` | sai o `message` |
| `POST .../pedidos-ausencia` | `{id, numeroDias, estado, message}` | `PedidoAusenciaCriadoResponseDTO` | sai o `message` |
| `approve` · `ativar` · `close` da licença | `{message}` ou `{message, afectacaoEncerradaId}` | `LicencaEfeitoResponseDTO` | os campos de efeito **vêm sempre**, a nulo quando não houve efeito — antes só apareciam quando havia |
| `GET .../unidade-atual` | mapa livre | `UnidadeAtualResponseDTO` | os campos de mobilidade **vêm sempre**, a nulo fora de mobilidade |
| `GET .../chefe` · `.../responsavel` | mapa livre | DTO próprio | campos sempre presentes, a nulo |
| `GET .../vagas` | mapa livre | `VagasUnidadeResponseDTO` | sem alteração de nomes |
| `GET /me/**/download-url` | `{downloadUrl}` | `FileUrlDTO` | a chave passa a **`url`** |

> **Atenção à diferença subtil:** um mapa **omitia** as chaves sem valor; um DTO envia-as a `null`. Um ecrã que use "a chave existe?" para decidir tem de passar a testar o valor.

As operações que já tinham DTO próprio não mudaram: registo composto, progressão, promoção, transferência, substituição, mudança de estado.

### 11.8 Licenca/mobilidade: a decisao e o periodo separam-se (BREAKING, 2026-09-22)

O `status` guardava duas coisas ao mesmo tempo: o que foi despachado e onde a licenca estava no
seu periodo. O art. 44.o do DL n.o 3/2010 separa-as em numeros seguidos -- o n.o 1 define a
licenca como ausencia prolongada (um **periodo**), o n.o 2 faz a concessao depender do **despacho**
(um acto). Agora o registo tambem as separa.

| Campo | Eixo | Valores |
|---|---|---|
| `status` | decisao | `PENDING` / `APPROVED` / `REJECTED` / `CANCELLED` |
| `estadoPeriodo` (**novo**) | periodo, hoje | `POR_INICIAR` / `EM_CURSO` / `TERMINADA`, ou `null` se nao estiver deferida |

**`ACTIVE` e `CLOSED` deixaram de existir.** Mapeamento para quem os lia:

| Antes | Agora |
|---|---|
| `status == "ACTIVE"` para saber se esta de licenca | `estadoPeriodo == "EM_CURSO"` |
| `status == "ACTIVE"` para saber se foi deferida | `status == "APPROVED"` |
| `status == "CLOSED"` | `estadoPeriodo == "TERMINADA"` |

**O que muda nos ecras:**

- **Deferir deixou de por em vigor.** Aprovar uma licenca que comeca daqui a um mes devolve
  `afectacaoEncerradaId` a nulo e **nao** muda o estado do trabalhador. O ecra nao deve anunciar
  "vaga aberta" na aprovacao: isso acontece na data de inicio, aplicado por um job diario.
- **Um registo deferido pode nao estar a decorrer.** Uma lista que mostre "de licenca" tem de
  filtrar por `estadoPeriodo`, nao por `status`.
- **`PUT /{id}/close` passou a ser o regresso antecipado** (art. 46.o n.o 4) e devolve **409**
  se a licenca ainda nao comecou (use `cancel`) ou se ja terminou (nao ha nada a fechar --
  termina sozinha na data de fim).
- **`PUT /{id}/cancel` devolve 409** depois de a licenca comecar. Ai a saida e o regresso
  antecipado.
- **A `dataFim` depois do regresso fica na vespera** do dia em que a pessoa voltou, porque a data
  de regresso e o primeiro dia de volta ao servico.

### 11.9 Ferias: o saldo deixa de ser criado a mao (2026-09-22)

O art. 2.o n.o 4 do DL n.o 3/2010 diz que o direito a ferias vence a 1 de Janeiro. Ate agora o
saldo era escrito por alguem atraves de `POST /funcionarios/{id}/saldos-ausencia`; passou a
nascer sozinho.

**O que muda nos ecras:**

- **Nao criar saldos de ferias a mao.** O saldo existe a partir da admissao. Um ecra que peca
  "dias de direito" ao utilizador para as ferias esta a duplicar o que a lei ja fixou. O
  endpoint continua a existir para os restantes tipos de ausencia.
- **Zero dias nao e erro.** Quem e admitido em Novembro ou Dezembro tem saldo de ferias a
  **zero** ate perfazer 90 dias de servico (art. 3.o). O ecra deve mostrar zero, e nao
  "sem saldo configurado".
- **O direito cresce durante o ano de ingresso**, a cada trimestre completo. Um valor lido em
  Maio pode nao ser o mesmo em Agosto -- nao vale a pena guarda-lo em cache do lado do cliente.

**Campo novo no catalogo de tipos de ausencia** (`/catalogs/leave-types`):

| Campo | Valores | Para que serve |
|---|---|---|
| `regime` | `FERIAS` / `FALTA` | o capitulo do DL n.o 3/2010 a que a linha obedece |

- Na **criacao**, omitido vale `FALTA`.
- Na **actualizacao**, omitido **mantem** o que esta -- nao apaga a classificacao.
- Um valor fora da lista devolve **422**: os regimes sao os da lei, a instituicao mapeia mas nao
  inventa.
- Um ecra de administracao do catalogo deve expor este campo, porque e ele -- e nao o codigo --
  que determina a que linha se aplica o vencimento anual.

### 11.10 Acumulacao de ferias (2026-09-22)

Endpoint novo: **`POST /funcionarios/{id}/saldos-ausencia/{saldoId}/acumular`**, corpo
`{ "dias": N, "motivo": "..." }`. O `motivo` e **obrigatorio** -- o art. 7.o n.o 1 do
DL n.o 3/2010 so permite a acumulacao quando, por motivo de servico, as ferias nao puderam ser
gozadas nesse ano. Sem ele, **400**.

**Campos novos no `SaldoAusenciaResponseDTO`:**

| Campo | Significado |
|---|---|
| `diasAcumulados` | dias vindos do ano anterior |
| `acumulacaoMotivo` | porque e que nao puderam ser gozados |
| `diasTransportados` | dias ja cedidos ao ano seguinte |
| `diasAcumulaveis` | quanto deste ano ainda pode seguir para o seguinte |

**O `diasDisponiveis` mudou de formula:** passou a ser
`diasDireito + diasAcumulados - gozados - pendentes - transportados`. Um ecra que somasse
`diasDireito - gozados` a mao passa a dar um numero diferente do da API -- use o campo.

**Um ecra de saldo deve mostrar `diasDireito` e `diasAcumulados` separados.** Sao coisas
diferentes: um venceu-se este ano, o outro sobrou do anterior e tem prazo.

### 11.11 Suspensao de ferias (2026-09-22)

Endpoint novo: **`PATCH /funcionarios/{id}/pedidos-ausencia/{pedidoId}/suspender`**, corpo
`{ "data": "YYYY-MM-DD", "motivo": "..." }`. O `motivo` e **obrigatorio** (400 sem ele) e a
`data` nao pode ser futura (400).

**Campos novos no `PedidoAusenciaResponseDTO`:** `suspensoEm` e `suspensaoMotivo`.

**O estado NAO muda.** Umas ferias interrompidas continuam `APROVADO` -- a decisao foi tomada e
nao se desfaz; o que encurta e a `dataFim`. Um ecra que queira distinguir umas ferias
interrompidas de umas ferias que sempre tiveram aquela duracao tem de olhar para `suspensoEm`,
nao para o estado.

**O `numeroDias` do pedido e reescrito** para os dias efectivamente gozados, e a diferenca volta
ao saldo. Um ecra que tenha guardado o numero antigo passa a divergir da API -- releia o pedido.

### 11.12 Ler as substituicoes (2026-09-22)

Endpoint novo: **`GET /funcionarios/{id}/substituicoes`** (`?apenasCorrentes=true` para so as que
estao em vigor). Nao quebra nada -- preenche uma lacuna: ate aqui a substituicao criava-se e
nada a mostrava.

Cada linha traz `papel` (`SUBSTITUTO` ou `TITULAR`), a contraparte (id, nome, numero), o Lugar
coberto (id, numero, unidade) e o periodo. **A `dataFim` vem nula enquanto durar** -- a
substituicao caduca com o regresso do titular (art. 77.o n.o 2), nao numa data combinada, por
isso um ecra nao deve pedir nem mostrar uma data de fim prevista.

Um ecra de RH que mostre um Lugar passa a poder dizer quem la esta em substituicao; um ecra de
colaborador passa a poder dizer quem o substitui enquanto esta impedido.

### 11.13 Antiguidade (2026-09-22)

Endpoint novo: **`GET /funcionarios/{id}/antiguidade`** (`?ate=YYYY-MM-DD` opcional). Nao quebra
nada -- ate aqui a antiguidade **nao se calculava em lado nenhum**.

Devolve `diasTotais`, `diasDescontados`, `diasContados`, `anos`/`meses`/`dias` e a lista
`periodosDescontados[]` com `inicio`, `fim`, `dias` e `motivo`.

**Mostre os periodos, nao so o total.** Quem discorda de uma antiguidade quer ver que periodos
foram descontados e porque -- e e isso que torna a conta defensavel a frente de um colaborador.

**Nao guarde o valor em cache.** A antiguidade e derivada e recalculada a cada leitura: muda
quando muda o estado do colaborador, quando se defere uma licenca, ou quando se corrige uma data
do passado.

### 11.14 Mudanca de carreira (2026-09-22)

Endpoint novo, **nao breaking** -- nada do que existia mudou de forma:

```
POST /api/v1/rh/funcionarios/{funcionarioId}/mudanca-carreira
```

Ate agora nao havia como mudar de carreira: a promocao exige a **mesma carreira** e a
transferencia a **mesma categoria**. Se o ecra oferecia a mudanca de carreira por
`POST /assignments` com `origem: PROMOCAO` ou `TRANSFERENCIA`, **deixe de o fazer** -- passa
pelos endpoints proprios, e estes recusam-no.

Corpo: `positionId` e `dataEfeito` obrigatorios; `gradeId`, `functionId`, `despachoNumero`,
`concursoRef` e `observacoes` opcionais. Devolve `201` com `MudancaCarreiraResponseDTO`, que
traz as **duas pontas da grelha** -- `carreiraAnterior`/`carreiraNova`,
`categoriaAnterior`/`categoriaNova` -- e o Lugar de onde veio.

**O que o ecra tem de fazer diferente dos outros movimentos:**

- **Filtrar os Lugares vagos por carreira DIFERENTE da actual.** Um destino da mesma carreira
  da 422. Nao ha a forma "o Lugar sobe" que a promocao tem: **exige-se Lugar vago**.
- **Oferecer o escalao da categoria de destino**, nao o actual. Ao contrario da transferencia,
  o escalao **nao se herda**: `gradeId` de outra categoria da 422, e sem `gradeId` entra-se
  pelo primeiro escalao activo.
- **Nao prometa validacao de habilitacoes.** Nao existe: a carreira nao tem campo que diga o
  requisito. Como o concurso na promocao, `despachoNumero` e `concursoRef` sao registo, nao
  verificacao.

### 11.15 A `ACUMULACAO` saiu, e o `POST /assignments` só aceita `PRINCIPAL` (BREAKING, 2026-09-22)

**Duas mudanças, na mesma porta.**

**1. `ACUMULACAO` deixou de existir** como valor de `assignmentType`. Fundava-se no art. 134.º
n.º 2 al. b) da Lei n.º 20/X/2023, mas esse artigo trata da *forma de prestação da mobilidade*
-- «em regime de acumulação, quando o funcionário passa a exercer funções noutro serviço, em
acumulação com as do serviço de origem» -- e não de um título para ocupar um segundo Lugar.
Como a mobilidade transitória é *sem ocupação do lugar do quadro* (art. 135.º n.º 7), uma
mobilidade em acumulação **não cria afectação nenhuma**. **Retire `ACUMULACAO` dos selects.**

**2. `POST /assignments` só aceita `PRINCIPAL`.** Enviar `SUBSTITUICAO` passa a dar **422**, a
remeter para `POST /funcionarios/{id}/substituicao`. Se algum ecra criava substituições pela
porta genérica, **mude-o**: as criadas por aí ficavam sem ligação ao titular e sem nenhuma das
regras da substituição.

Nada a migrar: confirmou-se na base que não existe nenhuma afectação com `ACUMULACAO`, nem
corrente nem em auditoria.

### 11.16 Mobilidade: forma de prestação (2026-09-22)

Campo novo, **nao breaking**: `formaPrestacao` em `POST/PUT
/funcionarios/{id}/licencas-mobilidade` e na resposta da leitura.

Valores: `TEMPO_INTEIRO` (omisso) e `ACUMULACAO`, do art. 134.o n.o 2 da Lei 20/X/2023. Quem
nao enviar nada fica em exclusividade, que e a regra do art. 20.o.

**E aqui que a acumulacao vive agora.** Se o ecra tinha (ou ia ter) acumulacao como
`assignmentType`, e este o campo a usar -- ver 11.15. Uma mobilidade em acumulacao **continua
a nao criar afectacao nenhuma**: o Lugar de origem nao muda, e o `positionId` em
`/unidade-atual` fica igual.

So se define enquanto o processo esta `PENDING`; depois do despacho da **409**. Num subtipo de
licenca da **422**.

### 11.17 Consolidar a mobilidade (2026-09-22)

Endpoint novo, **nao breaking**:

```
POST /api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade/{licencaId}/consolidar
```

Torna definitiva uma mobilidade transitoria (art. 132.o n.o 4). A pessoa deixa de estar em
mobilidade e passa a ser **titular de um Lugar vago do servico de destino**.

**O que o ecra tem de fazer:**

- Oferecer a accao **so em mobilidades internas ja comecadas** e deferidas. Externa da 422; por
  iniciar da 409 (para desistir dela e o cancelamento).
- **Filtrar os Lugares vagos por unidade de destino da mobilidade E pelo mesmo cargo e categoria
  do Lugar actual.** Qualquer outra coisa da 422 -- mudar de categoria ou funcao exigiria
  concurso comum interno (art. 135.o n.o 6).
- Nao oferecer escolha de escalao: mantem-se, porque a categoria e a mesma.
- Depois da consolidacao, `/unidade-atual` passa a dar o **Lugar novo** e `emMobilidade` fica
  `false`. A mobilidade fica `APPROVED` com `dataFim` na **vespera** da data de efeito.

### 11.18 Regresso de comissao de servico pode cessar o vinculo (BREAKING de comportamento, 2026-09-22)

Nao ha endpoint novo nem campo novo. O que muda e **o que acontece no fim de uma comissao de
servico**.

Art. 64.o n.o 2: cessada a comissao, o nomeado regressa a situacao de que era titular antes dela
"quando constituida e consolidada por tempo indeterminado, ou, **no caso contrario, cessa a
relacao juridica de emprego publico**".

Ate agora o catalogo tinha a comissao como `REGRESSA_LUGAR` sem condicao: devolvia ao Lugar de
origem **toda a gente**, incluindo quem foi recrutado PARA a comissao e nunca teve Lugar. Passa a
haver um terceiro valor de `return_effect`, `REGRESSA_OU_CESSA`, e com ele:

- quem **tem** afectacao corrente regressa a ela, como antes;
- quem **nao tem** ve a relacao de emprego publico **cessar** -- contrato encerrado, estado de
  cessacao, registo no historico.

**O que o ecra tem de fazer:** ao fechar uma comissao (`close`) ou ao mostrar o resultado do job
diario, **contar com que o colaborador possa vir cessado**. Nao assuma que depois de uma
mobilidade a pessoa continua activa. O `estadoAtribuidoId` da resposta traz o estado de cessacao
quando foi esse o caso.

Nos selects de `returnEffect` (configuracao de subtipos) acrescente `REGRESSA_OU_CESSA`.

**Catalogo:** o `MOB_COMISSAO` do seed passou a `REGRESSA_OU_CESSA`, com 1095 dias e sem limite de
renovacoes (art. 60.o n.o 1: tres anos, sucessivamente renovavel), em vez de 365 com uma
prorrogacao. Instalacoes existentes tem de reclassificar o seu proprio catalogo -- nao ha migracao
que o faca por elas, porque o catalogo e da instituicao.

### Checklist

### 11.19 Tipos de ausencia: tres tectos de dias, e nao um (2026-09-22)

O tipo de ausencia tinha um so campo de limite, `maxDaysPerYear`, e o pedido somava sempre o **ano
civil**. Passa a ter **tres**, todos opcionais:

| Campo | O que limita |
|---|---|
| `maxDaysPerYear` | o total do ano civil |
| `maxDaysPerOccurrence` | os dias de **cada pedido** |
| `maxDaysPerMonth` | o total do mes civil |

Nao ha campo removido nem renomeado: quem so conhecer o `maxDaysPerYear` continua a funcionar.

**Porque foi preciso.** O art. 15.o n.o 1 do DL n.o 3/2010 quase nunca fala em anos -- "ate 6, POR
OCASIAO do casamento", "ate 8, por motivo de FALECIMENTO do conjuge", "duas por CADA prova". Com
esses numeros escritos no tecto anual, o segundo funeral do mesmo ano era recusado e, ao mesmo
tempo, oito dias seguidos de uma so vez passavam. A al. q) mostra porque sao tres valores
independentes: "6 dias em cada ano civil **e um dia por mes**" -- os dois tectos valem juntos.

**Nulo quer dizer sem limite desta natureza**, nao zero. E o caso da greve e das obrigacoes legais.

**O que o ecra tem de fazer:**

- mostrar os tres campos na configuracao do tipo de ausencia, e deixar claro que vazio e "sem
  limite" -- um `0` passa a dar **400**, porque um limite de zero dias diz-se desactivando a linha;
- tratar o **422** da criacao do pedido a ler a mensagem: ha agora tres recusas diferentes ("no
  maximo N dias de cada vez", "limite mensal", "limite anual"), e a que interessa mostrar e a que
  veio;
- contar com mais linhas no catalogo: o seed traz agora as alineas do art. 15.o
  (`CASAMENTO`, `LUTO`, `LUTO_OUTRO_GRAU`, `NASCIMENTO_FILHO`, `PROVA_EXAME`,
  `ASSISTENCIA_FAMILIA`, `AUTORIZADA_DIRIGENTE`, `CONTA_FERIAS`, `GREVE`, `OBRIGACAO_LEGAL`,
  `DOENCA_ATESTADO`). Um select de tipos com altura fixa para meia duzia de linhas fica curto.

**Catalogo:** como no MOB_COMISSAO, o seed **nao corrige** linhas ja existentes
(`ON CONFLICT DO NOTHING`). Numa instalacao a rodar, o `LUTO` continua a dizer 5 dias por ano ate
alguem o reclassificar pela API -- e, a partir da V53, e pela API que se faz, sem tocar em codigo.

### 11.20 Faltas injustificadas e efeito na remuneracao (2026-09-22)

Duas coisas que vivem na mesma frase do art. 43.o n.o 2 do DL n.o 3/2010.

**1. `regime` passa a ter tres valores**, e nao dois: `FERIAS` · `FALTA` · `FALTA_INJUSTIFICADA`.
Um select que so conheca os dois primeiros deixa de mostrar linhas validas.

Classificar um tipo como injustificado tem consequencias que o ecra deve deixar claras a quem
classifica: **as faltas desse tipo passam a descontar antiguidade**, sempre, e isso nao se
desliga. E deliberado -- a lei nao da a opcao.

**2. `POST /funcionarios/{id}/pedidos-ausencia` ganha `opcaoFaltaInjustificada`** --
`PERDA_REMUNERACAO` ou `DESCONTO_FERIAS`:

- **obrigatoria** quando o tipo escolhido e injustificado: sem ela da **422**;
- **recusada** quando nao e: com ela da **422**.

O formulario tem de reagir ao tipo escolhido, mostrando o campo so quando o tipo tem
`regime = FALTA_INJUSTIFICADA`. A opcao vem de volta no `GET` dos pedidos, no mesmo campo.

Pelo **self-service** este campo nao existe: um pedido de um tipo injustificado submetido por ai
e recusado, porque ninguem classifica uma falta sua como injustificada.

**3. O tipo de ausencia ganha `efeitoRemuneracao`** -- `SEM_PERDA` · `PERDA_PARCIAL` ·
`PERDA_TOTAL` · `PERDA_VENCIMENTO_EXERCICIO` · `DEPENDE_DA_OPCAO` (art. 16.o).

A aplicacao **nao calcula remuneracao**: e informacao para o sistema que a processa. Para o
front-end, e mais um campo de classificacao no ecra de tipos de ausencia, que nasce `SEM_PERDA`
e recusa valores fora da lista com 422.

**Catalogo:** como no MOB_COMISSAO e nos limites da V53, a migracao **nao classifica** as linhas
existentes -- estas duas colunas mandam descontar antiguidade e mexer em salarios. Tudo fica em
`SEM_PERDA` e no regime que ja tinha, e a classificacao faz-se pela API. Numa instalacao a rodar,
**nenhuma falta desconta antiguidade ate alguem classificar o tipo**, que e a direccao segura.

**Nota:** nas licencas esta informacao ja existia como booleano
(`t_leave_mobility_subtype.affects_pay`), que nao sabe dizer "parcial". Por agora sao dois
contratos diferentes: booleano nas licencas, enum nas ausencias.

### 11.34 Horarios com data de efeito (2026-09-24)

- **Muda um comportamento:** editar os blocos/controlo/afericao/duracao de um horario que ja vigorou da
  **409** (so o nome muda). Oferecer "Duplicar" (`POST /catalogs/horarios/{id}/duplicar`, opcional
  `?nome=`) e atribuir o novo a partir de uma data.
- `PATCH /catalogs/horarios/{id}/base` aceita `?desde=yyyy-MM-dd` (hoje ou futura). `isBase` e o de hoje.
- Unidade organica: campo novo opcional `horarioDesde` no `PUT` (hoje ou futura). A resposta traz o
  `horarioId` de hoje; uma mudanca agendada so aparece na data.
- Datas passadas: 422 (nao se reescreve o apuramento de dias ja passados).

### 11.33 Pedidos de ausencia: aprovacao automatica e decisao da chefia (2026-09-24)

- **Muda um comportamento:** os tipos com `requiresApproval: false` (luto, casamento, doenca,
  nascimento, greve...) passam a nascer `APROVADO` (antes `PENDENTE`). Um ecra que mostrava estes
  pedidos na lista "por aprovar" deixa de os ver la; aprova-los da 409.
- Campo novo na resposta do pedido: `aprovacaoAutomatica` (boolean).
- Caixa da chefia: `GET /me/equipa/pedidos-ausencia-pendentes`,
  `PATCH /me/equipa/pedidos-ausencia/{id}/aprovar` (corpo opcional `{ "motivo" }`) e `.../rejeitar`
  (`{ "motivo" }` obrigatorio).
- Caminho do RH igual; um pedido de outro colaborador no caminho passa a dar 404.

### 11.32 Relacao mensal do art. 75.o (2026-09-23)

**Nada deixa de funcionar**: dois endpoints novos, so leitura.

- `GET /assiduidade/relacao-mensal?mes=yyyy-MM&unidadeId=...&incluirSubunidades=true` (JSON) e
  `GET /assiduidade/relacao-mensal.csv` (mesmos parametros; descarregar como ficheiro).
- Ecra por unidade: uma linha por colaborador, com o `estado` (COMPLETA/COM_PENDENCIAS) em destaque e
  a indicacao `provisoria` no mes corrente.
- Faltas apuradas: os dias depois do fim do vinculo passam a `FORA_DO_VINCULO` (antes COM_FALTA/SEM_REGISTO).

### 11.31 Trabalho suplementar (horas extras) (2026-09-23)

**Nada deixa de funcionar**: endpoints novos e um campo novo numa resposta.

- RH: `POST|GET /funcionarios/{id}/trabalho-suplementar` (`?mes=yyyy-MM`),
  `PATCH /funcionarios/{id}/trabalho-suplementar/{tid}/autorizar|recusar|cancelar`.
- `/me`: `POST|GET /me/trabalho-suplementar`, `POST /me/equipa/trabalho-suplementar`,
  `GET /me/equipa/trabalho-suplementar-pendente`, `PATCH /me/equipa/trabalho-suplementar/{id}/autorizar|recusar`.
- Horas em `HH:mm`. Mostrar `tipoDia` (DIA_UTIL/DESCANSO/FERIADO), autorizado vs realizado, e assinalar
  `autorizacaoPosterior` e `semRegisto`.
- Faltas apuradas: cada dia traz `minutosSuplementares` (fora de `minutosTrabalhados`).
- Sem valores em dinheiro: so horas.

### 11.30 Registo pelo proprio e validacao da chefia (2026-09-23)

**Nada deixa de funcionar**: endpoints novos e campos novos nas respostas.

- `/me`: `POST /me/marcacoes` (picagem em tempo real, so em teletrabalho/misto -- 422 no presencial),
  `POST /me/marcacoes/correcoes` (fica PENDENTE), `GET /me/assiduidade`,
  `GET /me/equipa/marcacoes-pendentes`, `PATCH /me/equipa/marcacoes/{id}/validar|rejeitar`.
- RH: `PATCH /funcionarios/{id}/marcacoes/{mid}/validar|rejeitar`.
- Cada marcacao traz `estado` (VALIDA/PENDENTE/REJEITADA) e `motivoRejeicao`; mostrar as pendentes a
  parte. Faltas apuradas: estado de dia `POR_VALIDAR` e `diasPorValidar`.
- O botao de picar so aparece a quem esta em teletrabalho/misto nesse dia (ler o horario vigente).

### 11.29 Pedido de ausencia pelo proprio com as regras do RH (2026-09-23)

`POST /me/leave-requests` passa a contar e validar como o pedido lancado pelo RH:

- `numeroDias` pode mudar para o mesmo periodo: conta pela linha do catalogo (dias uteis ou
  seguidos) e tira os feriados. Antes contava dias de calendario.
- Os tectos aplicam-se: um pedido acima do tecto passa a dar **422**.
- Sobreposicao com outro pedido passa de **400** para **409** (como no RH).
- Novos campos opcionais `startTime`/`endTime` (`HH:mm`) para pedidos em horas.

### 11.28 Pedido de ausencia em horas e dispensa de amamentacao (2026-09-23)

**Nada deixa de funcionar**: os campos sao novos e opcionais.

- Pedido de ausencia: `horaInicio`/`horaFim` opcionais (`HH:mm`). Sem eles, dias inteiros como
  sempre. Com eles, as horas valem em cada dia do intervalo; a resposta traz `minutosPorDia` e
  `numeroDias` = 0. Ver `api_guide.md` §6.2e.
- Mostrar os campos de hora so para tipos que os admitem (sem saldo, regime FALTA, sem tectos
  anuais/mensais) -- ex.: `DISPENSA_AMAMENTACAO`, `TRATAMENTO_AMBULATORIO`.
- `PATCH /funcionarios/{id}/pedidos-ausencia/{pid}/terminar` `{data, motivo}`: fim antecipado de um
  pedido em horas aprovado.
- Catalogo de tipos de ausencia: `maxMinutosPorDia` opcional (omisso mantem, 0 limpa).
- Faltas apuradas: cada dia traz `minutosJustificados`.

### 11.27 Faltas por debito (2026-09-23)

**Nada deixa de funcionar**: e um endpoint novo, so de leitura.

- `GET /funcionarios/{id}/faltas-apuradas?mes=yyyy-MM`. Ver `api_guide.md` §6.9.
- E um **calculo**, nao um registo: muda quando se corrige uma marcacao ou se aprova um pedido. O
  ecra deve dizer que o mes ainda nao esta fechado.
- Mostrar os dias `POR_CORRIGIR` e os `SEM_REGISTO` a parte: sao o que o RH trata antes do fecho.
- `faltasParciais` e `totalFaltas` sao decimais em meios (0.5, 1, 1.5...).

### 11.26 Registo diario de assiduidade (2026-09-23)

**Nada deixa de funcionar**: sao endpoints novos.

- `POST /funcionarios/{id}/marcacoes`, `PATCH /funcionarios/{id}/marcacoes/{mid}/anular`,
  `POST /assiduidade/importacao` e `GET /funcionarios/{id}/assiduidade?de=&ate=`. Ver
  `api_guide.md` §6.8.
- Uma marcacao **nao se edita nem se apaga**: o ecra oferece "corrigir" (nova marcacao, com motivo
  se o dia ja tiver marcacoes) e "anular" (com motivo). As anuladas continuam a vir, marcadas.
- Mostrar as `anomalias` do dia: sao os dias a corrigir.
- O lancamento pode devolver 201 **com alerta** (ausencia aprovada, feriado, fim-de-semana).

### 11.25 Horarios: catalogo, unidade organica e colaborador (2026-09-23)

**Nada deixa de funcionar**: sao endpoints novos e um campo opcional novo na unidade organica.

- Catalogo `/catalogs/horarios` (lista sem paginacao, `GET/{id}`, `POST`, `PUT`, `DELETE`,
  `PATCH /{id}/activate`, `PATCH /{id}/base`). Ver `api_guide.md` §9.3.
- Unidade organica: `horarioId` opcional no `POST`/`PUT` e na resposta. O `PUT` que nao o envia
  **mantem-no**; `""` limpa. So da 422 quando vem preenchido com um horario inexistente ou inactivo.
- Colaborador: `GET/POST /funcionarios/{id}/horarios` e `GET /funcionarios/{id}/horarios/vigente?data=`.
  Ver `api_guide.md` §6.7. A resposta do vigente diz a `origem` (`COLABORADOR`, `UNIDADE`, `BASE`,
  `NENHUM`): o ecra deve mostrar de onde vem o horario.
- Horas em `HH:mm`; dias da semana de 1 (segunda) a 7 (domingo).
- A atribuicao pode devolver 201 **com alerta** (tempo parcial com horario de horas completas):
  mostrar, nao tratar como erro.

### 11.24 Parametros do mapa de ferias num catalogo (2026-09-23)

**Nada deixa de funcionar**: os endpoints do mapa respondem igual. Os prazos e a janela de fixacao
deixaram de vir de `application.properties` e passam a vir de um catalogo por vigencia.

- `GET/POST /catalogs/parametros-ferias`, `PUT /catalogs/parametros-ferias/{id}` e
  `GET /catalogs/parametros-ferias/vigente?ano=`. Ver `api_guide.md` §9.2.
- Datas em texto `MM-dd`. O `PUT` nao apaga o que vem omisso.
- A resposta traz `origem` (`TABELA` / `LEI`): com `LEI` nao ha linha para editar (`id` nulo) --
  o ecra oferece criar uma vigencia.

### 11.23 Mapa de ferias (2026-09-23)

**Nada deixa de funcionar**: sao cinco endpoints novos, e o pedido de ferias continua igual.

- `GET/PUT /funcionarios/{id}/ferias/{ano}` (+ `/preferencia`, `/marcacao`) e
  `GET /ferias/mapa/{ano}`, `POST /ferias/mapa/{ano}/publicar`. Ver `api_guide.md` §6.6.
- A **preferencia** fora de prazo e aceite: mostrar o alerta, nao tratar como erro.
- A **marcacao** tem `origem` obrigatoria (`ACORDO` / `FIXADA`); `FIXADA` so entre Maio e Outubro,
  e interpolada precisa de `fundamentacao`.
- Depois de **publicado**, alterar pede `motivoAlteracao` (`ACORDO` / `CONVENIENCIA_SERVICO`) e, na
  conveniencia, `fundamentacao`. O ecra so deve mostrar estes campos quando `mapaPublicadoEm` vem
  preenchido.
- Publicar duas vezes da **409**.

### 11.22 Tipos de ausencia: dias uteis ou seguidos, e tres dispensas novas (2026-09-23)

**Nada deixa de funcionar**: `contagem` e um campo novo e opcional em `leave-types` (pedido e
resposta) e no `tipoAusencia` dos pedidos e saldos. Valores: `DIAS_UTEIS` · `DIAS_SEGUIDOS`. Omisso
na criacao vale `DIAS_UTEIS`; omisso no `PUT` mantem o que esta. Valor fora dos dois da **422**.

**O que muda sao os numeros.** Num tipo em `DIAS_SEGUIDOS`, o `numeroDias` do pedido passa a contar
os fins-de-semana e feriados **intercalados** (art. 76.o do DL n.o 3/2010): um luto de sexta a
segunda passa de 2 dias para 4. Os das pontas nao contam. Numa instalacao existente nada muda ate
alguem classificar o tipo -- a migracao deixou tudo em `DIAS_UTEIS`.

**Tres tipos novos no seed:** `SEMINARIO` (max. 5 dias seguidos por pedido), `TE_PESQUISA` (6 dias
uteis por ano) e `TE_LICENCA` (10 dias uteis por ano, com desconto no vencimento). Todos pedem
aprovacao.

### 11.21 Feriados: recorrentes, com area, e todos contam (2026-09-23)

**Nada deixa de funcionar**: sao campos novos e opcionais. O que muda e o **numero de dias** que um
pedido de ausencia desconta.

**1. `public-holidays` ganha `isRecurring` e `areaCkey`**, no pedido e na resposta.

- `isRecurring: true` -- o feriado vale todos os anos no mesmo dia e mes. Os de data fixa do seed
  vem marcados; os moveis (Sexta-feira Santa, Corpus Christi) continuam a ser um por ano.
- `areaCkey` -- ckey do catalogo `AREA_GEOGRAFICA` (`/reference/options?ccode=AREA_GEOGRAFICA`).
  Vazio = vale para todos. **422** num feriado nacional com area e num 29 de Fevereiro
  recorrente. A area **nao** se valida ainda contra o catalogo.
- O `PUT` **nao apaga** o que nao recebe: o ecra actual, que nao envia os dois campos, continua a
  funcionar sem desmarcar nada. Para limpar a area envia-se `""`; para desmarcar, `false`.

**2. `organizational-units` ganha `areaCkey`**, no pedido e na resposta, com o mesmo `PUT` que nao
apaga. Vazio herda a da unidade-mae.

**3. Os dias descontados podem mudar.** Passam a contar **todos** os feriados activos (antes so os
nacionais) e os do periodo inteiro (antes so os do ano de inicio). Um pedido que atravesse um
feriado municipal ou o Ano Novo desconta menos um dia do que descontava. O `numeroDias` da
resposta ja reflecte isto -- nao ha nada a recalcular no ecra.

- [ ] Select de `situacaoFuncional` no catálogo de estados.
- [ ] Tratar `afectacaoEncerradaId` com `cessouVinculo: false` (activo, sem Lugar).
- [ ] Retirar `AMBOS` dos selects de `recordType`.
- [ ] Campos de efeito no Lugar no ecrã de subtipos.
- [ ] Mostrar `diasGozados` e tratar o **422** na criação do pedido de ausência.
- [ ] Tratar o **422** de `assignmentType` inválido e rever comparações pela mensagem "já está ocupado".
- [ ] Acção de substituição a partir do Lugar, sem data de fim nem botão de terminar.
- [ ] Ficha do colaborador a aguentar duas afectações correntes (a sua e a que substitui).
- [ ] Substituir a leitura de `response.message` por texto do próprio ecrã.
- [ ] Tratar `sucesso: false` como "nada a fazer" e mostrar os `alertas`.
- [ ] `downloadUrl` passa a `url` nos dois endpoints de download do self-service.
- [ ] Deixar de testar "a chave existe" — os DTOs enviam os campos a `null`.
- [ ] Mudança de carreira pelo endpoint próprio, com os Lugares vagos filtrados por **outra** carreira.
- [ ] Escalão escolhido na **categoria de destino** — na mudança de carreira não se herda.
- [ ] Retirar `ACUMULACAO` dos selects de `assignmentType`.
- [ ] Deixar de criar substituições por `POST /assignments` — passa a dar 422.
- [ ] Campo `formaPrestacao` no formulário de mobilidade (omisso = `TEMPO_INTEIRO`).
- [ ] Acção de consolidar na mobilidade interna, com os Lugares filtrados por unidade de destino, cargo e categoria.
- [ ] Contar com que o fim de uma **comissão de serviço** possa deixar o colaborador **cessado**.
- [ ] `REGRESSA_OU_CESSA` no select de `returnEffect` da configuração de subtipos.
- [ ] Três campos de limite no ecrã de tipos de ausência; vazio é "sem limite" e `0` dá **400**.
- [ ] Mostrar a mensagem do **422** do pedido: há agora recusa por ocorrência, por mês e por ano.
- [ ] Select de tipos de ausência preparado para o catálogo completo do art. 15.º.
- [ ] Terceiro valor `FALTA_INJUSTIFICADA` no select de `regime`, com aviso de que desconta antiguidade.
- [ ] Campo `opcaoFaltaInjustificada` no pedido, visível **só** para tipos injustificados (422 nos dois sentidos).
- [ ] Campo `efeitoRemuneracao` no ecrã de tipos de ausência (nasce `SEM_PERDA`).
- [ ] Contar com que, numa instalação existente, nada disto venha classificado.
- [ ] Caixa `isRecurring` e select de `areaCkey` (`AREA_GEOGRAFICA`) no ecrã de feriados (`""` limpa a área no `PUT`).
- [ ] Select de `areaCkey` no ecrã de unidades orgânicas (vazio herda a da unidade-mãe).
- [ ] Contar com que o `numeroDias` de um pedido desconte feriados municipais e os do ano seguinte.
- [ ] Select de `contagem` (`DIAS_UTEIS` / `DIAS_SEGUIDOS`) no ecrã de tipos de ausência; vazio mantém.
- [ ] Explicar no formulário do pedido que, em tipos `DIAS_SEGUIDOS`, os fins-de-semana intercalados contam.
- [ ] Contar com três tipos novos no select de ausências: `SEMINARIO`, `TE_PESQUISA`, `TE_LICENCA`.
- [ ] Ecrã de preferência de férias por colaborador e ano (vários períodos; alerta de fora de prazo).
- [ ] Ecrã do mapa: marcações, lista de quem está sem marcação, e o botão de dar conhecimento (409 na segunda vez).
- [ ] Marcação com `origem`; `fundamentacao` quando `FIXADA` interpolada; `motivoAlteracao` só com o mapa publicado.
- [ ] Ecrã de parâmetros do mapa de férias: lista de vigências, criar uma nova (ano + datas `MM-dd` + mínimo interpolado) e editar.
- [ ] Ecrã do catálogo de horários: blocos por dia, fixo ou flexível (período de aferição + duração diária + plataformas), e o botão de marcar o horário base.
- [ ] Select opcional de horário no formulário da unidade orgânica (vazio = segue a unidade-mãe).
- [ ] Separador de horário no colaborador: histórico, atribuir a partir de uma data (horário + regime de prestação) e o horário vigente com a origem.
- [ ] Ecrã de assiduidade do colaborador: semana/mês com períodos, horas trabalhadas vs esperadas, dias com anomalia destacados, e as acções corrigir e anular (com motivo).
- [ ] Pedido de ausência: horas de início e fim (só para tipos que as admitem), e a acção terminar para pedidos em horas aprovados.
- [ ] Horários: botão «Duplicar» e aviso de 409 ao editar um horário que já vigorou; data de efeito (opcional) ao mudar o base e o horário da unidade.
- [ ] Pedidos de ausência: caixa da chefia (aprovar/rejeitar com motivo); mostrar «aprovado automaticamente» nos tipos que não requerem aprovação.
- [ ] Relação mensal: ecrã por unidade (mês, com subunidades), pendências em destaque, botão de descarregar o CSV.
- [ ] Trabalho suplementar: separador no colaborador (mês, autorizado vs realizado por tipo de dia), pedido pelo próprio no `/me`, e a caixa de pendentes da chefia (autorizar/recusar).
- [ ] `/me`: botão de picar (só em teletrabalho/misto), pedido de correcção com motivo, e a caixa de pendentes da equipa para a chefia (validar/rejeitar).
- [ ] Separador de faltas apuradas do mês: dias com falta e motivo, débitos da aferição, total em dias e meios-dias, e atalho para justificar (pedido de ausência).
