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
| `assignmentType` | PRINCIPAL · ACUMULACAO · SUBSTITUICAO |
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

- `assignmentType` passa a ser **validado**: um valor fora de `PRINCIPAL`, `SUBSTITUICAO`, `ACUMULACAO` devolve **422** (antes era gravado tal e qual). Vazio continua a valer `PRINCIPAL`.
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

### Checklist

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
