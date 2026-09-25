# Breaking Changes — Frontend (v5, Position Management)

> Guia de migração do frontend para o novo modelo de **Mapa de Pessoal** (Lugares + Afectações).
> Refactor assumido como *breaking change*. pt-PT.
> Última alteração: 2026-09-25
>
> As secções 1 a 10 descrevem a passagem ao Mapa de Pessoal; a secção 11 junta, por ordem de data, o que mudou
> com o alinhamento à legislação (Lei n.º 20/X/2023 e DL n.º 3/2010), e a 12 é o checklist dessa parte.
> Para ver cada ecrã com os seus campos, a API e as regras: `apresentacao_aplicacao.html`.

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

> Filtrar o picker de escalões pela categoria do Lugar (`GET /categories/{categoryId}/grades`). O servidor também valida: um escalão de outra categoria dá **422** (BR-AF-06). O que a API **não** valida é a categoria pertencer à carreira ao criar o Lugar — essa cascata é do ecrã.

---

## 3. Gestão do Mapa de Pessoal (novo ecrã)

CRUD de Lugares em `/api/v1/rh/estrutura/positions`:

| Ação | Endpoint |
|---|---|
| Listar por unidade (+ vagas) | `GET /positions?unidadeId={id}` |
| Criar Lugar | `POST /positions` |
| Editar | `PUT /positions/{id}` |
| Congelar | `PATCH /positions/{id}/freeze` — corpo `{motivo, despachoNumero?}` (ver 11.39) |
| Descongelar | `PATCH /positions/{id}/unfreeze` — o mesmo corpo |
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

## 5. Movimentos (progressão, promoção, transferência, mudança de carreira, substituição)

> **Esta secção foi reescrita.** Uma versão anterior mandava fazer os movimentos por `POST /colaboradores/assignments`
> com `origem`. Deixou de ser assim: cada movimento tem endpoint próprio, com as suas regras, e a porta genérica só
> aceita a titularidade (`PRINCIPAL`) — ver 11.15.

| Movimento | Endpoint | Corpo |
|---|---|---|
| Progressão | `POST /funcionarios/{id}/progressao` | `dataEfeito` (o escalão é o seguinte, escolhido pelo sistema) |
| Promoção | `POST /funcionarios/{id}/promocao` | `categoryId`, `dataEfeito`; `positionId` e `gradeId` opcionais |
| Transferência | `POST /funcionarios/{id}/transferencia` | `positionId`, `dataEfeito`; `functionId` opcional |
| Mudança de carreira | `POST /funcionarios/{id}/mudanca-carreira` | `positionId`, `dataEfeito` (ver 11.14) |
| Substituição | `POST /funcionarios/{id}/substituicao` | `positionId`, `dataInicio` (ver 11.5) |

Todos fecham a afectação corrente na véspera da data de efeito e abrem uma nova (histórico), menos a
substituição, que acrescenta uma afectação sem fechar a do substituto. `POST /assignments` só coloca quem não tem Lugar
(ver 11.40): um movimento por essa porta dá 422.

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
Efeito novo: um estado com `endsEmployment` (no seed, `RETIRED` e `INACTIVE`) **cessa o vínculo** e encerra a afectação corrente → o Lugar volta a vago. Os outros estados produzem os efeitos da sua **situação funcional** (ver 11.1).

---

## 8. Endpoints removidos / substituídos

| Removido | Usar |
|---|---|
| `GET/POST /funcionarios/{id}/enquadramentos` | `GET .../assignments/funcionario/{id}/unidade-atual` para ler; os movimentos da secção 5 para mudar |
| `GET/POST /funcionarios/{id}/colocacoes` | o mesmo |
| auditoria catálogo `enquadramentos` | catálogo `assignments` |

O bloco `enquadramento` **ainda aparece** em `GET /funcionarios/{id}/details` (mesma forma, por compatibilidade) — mas é **derivado** do Lugar; não há CRUD de enquadramento.

---

## 9. Enumerações (para dropdowns)

| Enum | Valores |
|---|---|
| `origem` | ADMISSAO · PROGRESSAO · PROMOCAO · TRANSFERENCIA · MUDANCA_CARREIRA · SUBSTITUICAO · CONSOLIDACAO (e o legado MOBILIDADE) |
| `assignmentType` | PRINCIPAL · SUBSTITUICAO (em `POST /assignments` só PRINCIPAL) |
| `estado` (Lugar) | ATIVO · CONGELADO · EXTINTO |

**Provido/Vago** não é enum — usar o campo booleano `ocupado` das respostas de Lugar.

---

## 10. Checklist de migração FE

- [ ] Ecrã de admissão: remover inputs de unidade/cargo/carreira/categoria; adicionar **picker de Lugar vago** + escalão.
- [ ] Trocar `enquadramentoId` por `afectacaoId` na leitura da resposta de registo.
- [ ] Novo ecrã de **gestão do Mapa de Pessoal** (Lugares + chefias + vagas).
- [ ] Mobilidade: destino por **`destinationUnitId`** (interna) ou **`entidadeDestino`** (externa); `destinationPositionId` é legado e não se envia.
- [ ] Remover chamadas a `/enquadramentos` e `/colocacoes`.
- [ ] Usar as novas queries (unidade-atual, chefe, responsavel, vagas) onde antes se lia colocação/enquadramento.
- [ ] Movimentos pelos endpoints próprios (secção 5), e não por `POST /assignments`.
- [ ] Cabeçalho `Accept: application/json` em todas as chamadas.

> Contrato completo: `api_guide.md`. Conceitos e ecrãs: `apresentacao_aplicacao.html`. Tabelas: `modelo_relacional.html`.

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

### 11.8 Licença/mobilidade: a decisão e o período separam-se (BREAKING, 2026-09-22)

O `status` guardava duas coisas ao mesmo tempo: o que foi despachado e onde a licença estava no seu
período. O art. 44.º do DL n.º 3/2010 separa-as em números seguidos — o n.º 1 define a licença como
ausência prolongada (um **período**), o n.º 2 faz a concessão depender do **despacho** (um acto).
Agora o registo também as separa.

| Campo | Eixo | Valores |
|---|---|---|
| `status` | decisão | `PENDING` / `APPROVED` / `REJECTED` / `CANCELLED` |
| `estadoPeriodo` (**novo**) | período, hoje | `POR_INICIAR` / `EM_CURSO` / `TERMINADA`, ou `null` se não estiver deferida |

**`ACTIVE` e `CLOSED` deixaram de existir.** Mapeamento para quem os lia:

| Antes | Agora |
|---|---|
| `status == "ACTIVE"` para saber se está de licença | `estadoPeriodo == "EM_CURSO"` |
| `status == "ACTIVE"` para saber se foi deferida | `status == "APPROVED"` |
| `status == "CLOSED"` | `estadoPeriodo == "TERMINADA"` |

**O que muda nos ecrãs:**

- **Deferir deixou de pôr em vigor.** Aprovar uma licença que começa daqui a um mês devolve
  `afectacaoEncerradaId` a nulo e **não** muda o estado do trabalhador. O ecrã não deve anunciar
  «vaga aberta» na aprovação: isso acontece na data de início, aplicado por um job diário.
- **Um registo deferido pode não estar a decorrer.** Uma lista que mostre «de licença» tem de
  filtrar por `estadoPeriodo`, não por `status`.
- **`PUT /{id}/close` passou a ser o regresso antecipado** (art. 46.º n.º 4) e devolve **409**
  se a licença ainda não começou (use `cancel`) ou se já terminou (não há nada a fechar —
  termina sozinha na data de fim).
- **`PUT /{id}/cancel` devolve 409** depois de a licença começar. Aí a saída é o regresso
  antecipado.
- **A `dataFim` depois do regresso fica na véspera** do dia em que a pessoa voltou, porque a data
  de regresso é o primeiro dia de volta ao serviço.

### 11.9 Férias: o saldo deixa de ser criado à mão (2026-09-22)

O art. 2.º n.º 4 do DL n.º 3/2010 diz que o direito a férias vence a 1 de Janeiro. Até agora o
saldo era escrito por alguém através de `POST /funcionarios/{id}/saldos-ausencia`; passou a
nascer sozinho.

**O que muda nos ecrãs:**

- **Não criar saldos de férias à mão.** O saldo existe a partir da admissão. Um ecrã que peça
  «dias de direito» ao utilizador para as férias está a duplicar o que a lei já fixou. O
  endpoint continua a existir para os restantes tipos de ausência.
- **Zero dias não é erro.** Quem é admitido em Novembro ou Dezembro tem saldo de férias a
  **zero** até perfazer 90 dias de serviço (art. 3.º). O ecrã deve mostrar zero, e não
  «sem saldo configurado».
- **O direito cresce durante o ano de ingresso**, a cada trimestre completo. Um valor lido em
  Maio pode não ser o mesmo em Agosto — não vale a pena guardá-lo em cache do lado do cliente.

**Campo novo no catálogo de tipos de ausência** (`/catalogs/leave-types`):

| Campo | Valores | Para que serve |
|---|---|---|
| `regime` | `FERIAS` / `FALTA` (e `FALTA_INJUSTIFICADA`, ver 11.20) | o capítulo do DL n.º 3/2010 a que a linha obedece |

- Na **criação**, omitido vale `FALTA`.
- Na **actualização**, omitido **mantém** o que está — não apaga a classificação.
- Um valor fora da lista devolve **422**: os regimes são os da lei, a instituição mapeia mas não
  inventa.
- Um ecrã de administração do catálogo deve expor este campo, porque é ele — e não o código —
  que determina a que linha se aplica o vencimento anual.

### 11.10 Acumulação de férias (2026-09-22)

Endpoint novo: **`POST /funcionarios/{id}/saldos-ausencia/{saldoId}/acumular`**, corpo
`{ "dias": N, "motivo": "..." }`. O `motivo` é **obrigatório** — o art. 7.º n.º 1 do
DL n.º 3/2010 só permite a acumulação quando, por motivo de serviço, as férias não puderam ser
gozadas nesse ano. Sem ele, **400**.

**Campos novos no `SaldoAusenciaResponseDTO`:**

| Campo | Significado |
|---|---|
| `diasAcumulados` | dias vindos do ano anterior |
| `acumulacaoMotivo` | porque é que não puderam ser gozados |
| `diasTransportados` | dias já cedidos ao ano seguinte |
| `diasAcumulaveis` | quanto deste ano ainda pode seguir para o seguinte |

**O `diasDisponiveis` mudou de fórmula:** passou a ser
`diasDireito + diasAcumulados − gozados − pendentes − transportados`. Um ecrã que somasse
`diasDireito − gozados` à mão passa a dar um número diferente do da API — use o campo.

**Um ecrã de saldo deve mostrar `diasDireito` e `diasAcumulados` separados.** São coisas
diferentes: um venceu-se este ano, o outro sobrou do anterior e tem prazo.

### 11.11 Suspensão de férias (2026-09-22)

Endpoint novo: **`PATCH /funcionarios/{id}/pedidos-ausencia/{pedidoId}/suspender`**, corpo
`{ "data": "YYYY-MM-DD", "motivo": "..." }`. O `motivo` é **obrigatório** (400 sem ele) e a
`data` não pode ser futura (400).

**Campos novos no `PedidoAusenciaResponseDTO`:** `suspensoEm` e `suspensaoMotivo`.

**O estado NÃO muda.** Umas férias interrompidas continuam `APROVADO` — a decisão foi tomada e
não se desfaz; o que encurta é a `dataFim`. Um ecrã que queira distinguir umas férias
interrompidas de umas férias que sempre tiveram aquela duração tem de olhar para `suspensoEm`,
não para o estado.

**O `numeroDias` do pedido é reescrito** para os dias efectivamente gozados, e a diferença volta
ao saldo. Um ecrã que tenha guardado o número antigo passa a divergir da API — releia o pedido.

### 11.12 Ler as substituições (2026-09-22)

Endpoint novo: **`GET /funcionarios/{id}/substituicoes`** (`?apenasCorrentes=true` para só as que
estão em vigor). Não quebra nada — preenche uma lacuna: até aqui a substituição criava-se e
nada a mostrava.

Cada linha traz `papel` (`SUBSTITUTO` ou `TITULAR`), a contraparte (id, nome, número), o Lugar
coberto (id, número, unidade) e o período. **A `dataFim` vem nula enquanto durar** — a
substituição caduca com o regresso do titular (art. 77.º n.º 2), não numa data combinada, por
isso um ecrã não deve pedir nem mostrar uma data de fim prevista.

Um ecrã de RH que mostre um Lugar passa a poder dizer quem lá está em substituição; um ecrã de
colaborador passa a poder dizer quem o substitui enquanto está impedido.

### 11.13 Antiguidade (2026-09-22)

Endpoint novo: **`GET /funcionarios/{id}/antiguidade`** (`?ate=YYYY-MM-DD` opcional). Não quebra
nada — até aqui a antiguidade **não se calculava em lado nenhum**.

Devolve `diasTotais`, `diasDescontados`, `diasContados`, `anos`/`meses`/`dias` e a lista
`periodosDescontados[]` com `inicio`, `fim`, `dias` e `motivo`.

**Mostre os períodos, não só o total.** Quem discorda de uma antiguidade quer ver que períodos
foram descontados e porquê — e é isso que torna a conta defensável à frente de um colaborador.

**Não guarde o valor em cache.** A antiguidade é derivada e recalculada a cada leitura: muda
quando muda o estado do colaborador, quando se defere uma licença, ou quando se corrige uma data
do passado.

### 11.14 Mudança de carreira (2026-09-22)

Endpoint novo, **não breaking** — nada do que existia mudou de forma:

```
POST /api/v1/rh/funcionarios/{funcionarioId}/mudanca-carreira
```

Até agora não havia como mudar de carreira: a promoção exige a **mesma carreira** e a
transferência a **mesma categoria**. Se o ecrã oferecia a mudança de carreira por
`POST /assignments` com `origem: PROMOCAO` ou `TRANSFERENCIA`, **deixe de o fazer** — passa
pelos endpoints próprios, e estes recusam-no.

Corpo: `positionId` e `dataEfeito` obrigatórios; `gradeId`, `functionId`, `despachoNumero`,
`concursoRef` e `observacoes` opcionais. Devolve `201` com `MudancaCarreiraResponseDTO`, que
traz as **duas pontas da grelha** — `carreiraAnterior`/`carreiraNova`,
`categoriaAnterior`/`categoriaNova` — e o Lugar de onde veio.

**O que o ecrã tem de fazer diferente dos outros movimentos:**

- **Filtrar os Lugares vagos por carreira DIFERENTE da actual.** Um destino da mesma carreira
  dá 422. Não há a forma «o Lugar sobe» que a promoção tem: **exige-se Lugar vago**.
- **Oferecer o escalão da categoria de destino**, não o actual. Ao contrário da transferência,
  o escalão **não se herda**: `gradeId` de outra categoria dá 422, e sem `gradeId` entra-se
  pelo primeiro escalão activo.
- **Não prometa validação de habilitações.** Não existe: a carreira não tem campo que diga o
  requisito. Como o concurso na promoção, `despachoNumero` e `concursoRef` são registo, não
  verificação.

### 11.15 A `ACUMULACAO` saiu, e o `POST /assignments` só aceita `PRINCIPAL` (BREAKING, 2026-09-22)

**Duas mudanças, na mesma porta.**

**1. `ACUMULACAO` deixou de existir** como valor de `assignmentType`. Fundava-se no art. 134.º
n.º 2 al. b) da Lei n.º 20/X/2023, mas esse artigo trata da *forma de prestação da mobilidade*
— «em regime de acumulação, quando o funcionário passa a exercer funções noutro serviço, em
acumulação com as do serviço de origem» — e não de um título para ocupar um segundo Lugar.
Como a mobilidade transitória é *sem ocupação do lugar do quadro* (art. 135.º n.º 7), uma
mobilidade em acumulação **não cria afectação nenhuma**. **Retire `ACUMULACAO` dos selects.**

**2. `POST /assignments` só aceita `PRINCIPAL`.** Enviar `SUBSTITUICAO` passa a dar **422**, a
remeter para `POST /funcionarios/{id}/substituicao`. Se algum ecrã criava substituições pela
porta genérica, **mude-o**: as criadas por aí ficavam sem ligação ao titular e sem nenhuma das
regras da substituição.

Nada a migrar: confirmou-se na base que não existe nenhuma afectação com `ACUMULACAO`, nem
corrente nem em auditoria.

### 11.16 Mobilidade: forma de prestação (2026-09-22)

Campo novo, **não breaking**: `formaPrestacao` em `POST/PUT
/funcionarios/{id}/licencas-mobilidade` e na resposta da leitura.

Valores: `TEMPO_INTEIRO` (omisso) e `ACUMULACAO`, do art. 134.º n.º 2 da Lei 20/X/2023. Quem
não enviar nada fica em exclusividade, que é a regra do art. 20.º.

**É aqui que a acumulação vive agora.** Se o ecrã tinha (ou ia ter) acumulação como
`assignmentType`, é este o campo a usar — ver 11.15. Uma mobilidade em acumulação **continua
a não criar afectação nenhuma**: o Lugar de origem não muda, e o `positionId` em
`/unidade-atual` fica igual.

Só se define enquanto o processo está `PENDING`; depois do despacho dá **409**. Num subtipo de
licença dá **422**.

### 11.17 Consolidar a mobilidade (2026-09-22)

Endpoint novo, **não breaking**:

```
POST /api/v1/rh/funcionarios/{funcionarioId}/licencas-mobilidade/{licencaId}/consolidar
```

Torna definitiva uma mobilidade transitória (art. 132.º n.º 4). A pessoa deixa de estar em
mobilidade e passa a ser **titular de um Lugar vago do serviço de destino**.

**O que o ecrã tem de fazer:**

- Oferecer a acção **só em mobilidades internas já começadas** e deferidas. Externa dá 422; por
  iniciar dá 409 (para desistir dela é o cancelamento).
- **Filtrar os Lugares vagos por unidade de destino da mobilidade E pelo mesmo cargo e categoria
  do Lugar actual.** Qualquer outra coisa dá 422 — mudar de categoria ou função exigiria
  concurso comum interno (art. 135.º n.º 6).
- Não oferecer escolha de escalão: mantém-se, porque a categoria é a mesma.
- Depois da consolidação, `/unidade-atual` passa a dar o **Lugar novo** e `emMobilidade` fica
  `false`. A mobilidade fica `APPROVED` com `dataFim` na **véspera** da data de efeito.

### 11.18 Regresso de comissão de serviço pode cessar o vínculo (BREAKING de comportamento, 2026-09-22)

Não há endpoint novo nem campo novo. O que muda é **o que acontece no fim de uma comissão de
serviço**.

Art. 64.º n.º 2: cessada a comissão, o nomeado regressa à situação de que era titular antes dela
«quando constituída e consolidada por tempo indeterminado, ou, **no caso contrário, cessa a
relação jurídica de emprego público**».

Até agora o catálogo tinha a comissão como `REGRESSA_LUGAR` sem condição: devolvia ao Lugar de
origem **toda a gente**, incluindo quem foi recrutado PARA a comissão e nunca teve Lugar. Passa a
haver um terceiro valor de `return_effect`, `REGRESSA_OU_CESSA`, e com ele:

- quem **tem** afectação corrente regressa a ela, como antes;
- quem **não tem** vê a relação de emprego público **cessar** — contrato encerrado, estado de
  cessação, registo no histórico.

**O que o ecrã tem de fazer:** ao fechar uma comissão (`close`) ou ao mostrar o resultado do job
diário, **contar com que o colaborador possa vir cessado**. Não assuma que depois de uma
mobilidade a pessoa continua activa. O `estadoAtribuidoId` da resposta traz o estado de cessação
quando foi esse o caso.

Nos selects de `returnEffect` (configuração de subtipos) acrescente `REGRESSA_OU_CESSA`.

**Catálogo:** o `MOB_COMISSAO` do seed passou a `REGRESSA_OU_CESSA`, com 1095 dias e sem limite de
renovações (art. 60.º n.º 1: três anos, sucessivamente renovável), em vez de 365 com uma
prorrogação. Instalações existentes têm de reclassificar o seu próprio catálogo — não há migração
que o faça por elas, porque o catálogo é da instituição.

### 11.19 Tipos de ausência: três tectos de dias, e não um (2026-09-22)

O tipo de ausência tinha um só campo de limite, `maxDaysPerYear`, e o pedido somava sempre o **ano
civil**. Passa a ter **três**, todos opcionais:

| Campo | O que limita |
|---|---|
| `maxDaysPerYear` | o total do ano civil |
| `maxDaysPerOccurrence` | os dias de **cada pedido** |
| `maxDaysPerMonth` | o total do mês civil |

Não há campo removido nem renomeado: quem só conhecer o `maxDaysPerYear` continua a funcionar.

**Porque foi preciso.** O art. 15.º n.º 1 do DL n.º 3/2010 quase nunca fala em anos — «até 6, POR
OCASIÃO do casamento», «até 8, por motivo de FALECIMENTO do cônjuge», «duas por CADA prova». Com
esses números escritos no tecto anual, o segundo funeral do mesmo ano era recusado e, ao mesmo
tempo, oito dias seguidos de uma só vez passavam. A al. q) mostra porque são três valores
independentes: «6 dias em cada ano civil **e um dia por mês**» — os dois tectos valem juntos.

**Nulo quer dizer sem limite desta natureza**, não zero. É o caso da greve e das obrigações legais.

**O que o ecrã tem de fazer:**

- mostrar os três campos na configuração do tipo de ausência, e deixar claro que vazio é «sem
  limite» — um `0` passa a dar **400**, porque um limite de zero dias diz-se desactivando a linha;
- tratar o **422** da criação do pedido a ler a mensagem: há agora três recusas diferentes («no
  máximo N dias de cada vez», «limite mensal», «limite anual»), e a que interessa mostrar é a que
  veio;
- contar com mais linhas no catálogo: o seed traz agora as alíneas do art. 15.º
  (`CASAMENTO`, `LUTO`, `LUTO_OUTRO_GRAU`, `NASCIMENTO_FILHO`, `PROVA_EXAME`,
  `ASSISTENCIA_FAMILIA`, `AUTORIZADA_DIRIGENTE`, `CONTA_FERIAS`, `GREVE`, `OBRIGACAO_LEGAL`,
  `DOENCA_ATESTADO`). Um select de tipos com altura fixa para meia dúzia de linhas fica curto.

**Catálogo:** como no `MOB_COMISSAO`, o seed **não corrige** linhas já existentes
(`ON CONFLICT DO NOTHING`). Numa instalação a rodar, o `LUTO` continua a dizer 5 dias por ano até
alguém o reclassificar pela API — e, a partir da V53, é pela API que se faz, sem tocar em código.

### 11.20 Faltas injustificadas e efeito na remuneração (2026-09-22)

Duas coisas que vivem na mesma frase do art. 43.º n.º 2 do DL n.º 3/2010.

**1. `regime` passa a ter três valores**, e não dois: `FERIAS` · `FALTA` · `FALTA_INJUSTIFICADA`.
Um select que só conheça os dois primeiros deixa de mostrar linhas válidas.

Classificar um tipo como injustificado tem consequências que o ecrã deve deixar claras a quem
classifica: **as faltas desse tipo passam a descontar antiguidade**, sempre, e isso não se
desliga. É deliberado — a lei não dá a opção.

**2. `POST /funcionarios/{id}/pedidos-ausencia` ganha `opcaoFaltaInjustificada`** —
`PERDA_REMUNERACAO` ou `DESCONTO_FERIAS`:

- **obrigatória** quando o tipo escolhido é injustificado: sem ela dá **422**;
- **recusada** quando não é: com ela dá **422**.

O formulário tem de reagir ao tipo escolhido, mostrando o campo só quando o tipo tem
`regime = FALTA_INJUSTIFICADA`. A opção vem de volta no `GET` dos pedidos, no mesmo campo.

Pelo **self-service** este campo não existe: um pedido de um tipo injustificado submetido por aí
é recusado, porque ninguém classifica uma falta sua como injustificada.

**3. O tipo de ausência ganha `efeitoRemuneracao`** — `SEM_PERDA` · `PERDA_PARCIAL` ·
`PERDA_TOTAL` · `PERDA_VENCIMENTO_EXERCICIO` · `DEPENDE_DA_OPCAO` (art. 16.º).

A aplicação **não calcula remuneração**: é informação para o sistema que a processa. Para o
front-end, é mais um campo de classificação no ecrã de tipos de ausência, que nasce `SEM_PERDA`
e recusa valores fora da lista com 422.

**Catálogo:** como no `MOB_COMISSAO` e nos limites da V53, a migração **não classifica** as linhas
existentes — estas duas colunas mandam descontar antiguidade e mexer em salários. Tudo fica em
`SEM_PERDA` e no regime que já tinha, e a classificação faz-se pela API. Numa instalação a rodar,
**nenhuma falta desconta antiguidade até alguém classificar o tipo**, que é a direcção segura.

**Nota:** nas licenças esta informação já existia como booleano
(`t_leave_mobility_subtype.affects_pay`), que não sabe dizer «parcial». Por agora são dois
contratos diferentes: booleano nas licenças, enum nas ausências.

### 11.21 Feriados: recorrentes, com área, e todos contam (2026-09-23)

**Nada deixa de funcionar**: são campos novos e opcionais. O que muda é o **número de dias** que um
pedido de ausência desconta.

**1. `public-holidays` ganha `isRecurring` e `areaCkey`**, no pedido e na resposta.

- `isRecurring: true` — o feriado vale todos os anos no mesmo dia e mês. Os de data fixa do seed
  vêm marcados; os móveis (Sexta-feira Santa, Corpus Christi) continuam a ser um por ano.
- `areaCkey` — ckey do catálogo `AREA_GEOGRAFICA` (`/reference/options?ccode=AREA_GEOGRAFICA`).
  Vazio = vale para todos. **422** num feriado nacional com área e num 29 de Fevereiro
  recorrente. A área **não** se valida ainda contra o catálogo.
- O `PUT` **não apaga** o que não recebe: o ecrã actual, que não envia os dois campos, continua a
  funcionar sem desmarcar nada. Para limpar a área envia-se `""`; para desmarcar, `false`.

**2. `organizational-units` ganha `areaCkey`**, no pedido e na resposta, com o mesmo `PUT` que não
apaga. Vazio herda a da unidade-mãe.

**3. Os dias descontados podem mudar.** Passam a contar **todos** os feriados activos (antes só os
nacionais) e os do período inteiro (antes só os do ano de início). Um pedido que atravesse um
feriado municipal ou o Ano Novo desconta menos um dia do que descontava. O `numeroDias` da
resposta já reflecte isto — não há nada a recalcular no ecrã.

### 11.22 Tipos de ausência: dias úteis ou seguidos, e três dispensas novas (2026-09-23)

**Nada deixa de funcionar**: `contagem` é um campo novo e opcional em `leave-types` (pedido e
resposta) e no `tipoAusencia` dos pedidos e saldos. Valores: `DIAS_UTEIS` · `DIAS_SEGUIDOS`. Omisso
na criação vale `DIAS_UTEIS`; omisso no `PUT` mantém o que está. Valor fora dos dois dá **422**.

**O que muda são os números.** Num tipo em `DIAS_SEGUIDOS`, o `numeroDias` do pedido passa a contar
os fins-de-semana e feriados **intercalados** (art. 76.º do DL n.º 3/2010): um luto de sexta a
segunda passa de 2 dias para 4. Os das pontas não contam. Numa instalação existente nada muda até
alguém classificar o tipo — a migração deixou tudo em `DIAS_UTEIS`.

**Três tipos novos no seed:** `SEMINARIO` (máx. 5 dias seguidos por pedido), `TE_PESQUISA` (6 dias
úteis por ano) e `TE_LICENCA` (10 dias úteis por ano, com desconto no vencimento). Todos pedem
aprovação.

### 11.23 Mapa de férias (2026-09-23)

**Nada deixa de funcionar**: são cinco endpoints novos, e o pedido de férias continua igual.

- `GET /funcionarios/{id}/ferias/{ano}` (+ `PUT …/preferencia`, `PUT …/marcacao`) e
  `GET /ferias/mapa/{ano}`, `POST /ferias/mapa/{ano}/publicar`. Ver `api_guide.md` §6.6.
- A **preferência** fora de prazo é aceite: mostrar o alerta, não tratar como erro.
- A **marcação** tem `origem` obrigatória (`ACORDO` / `FIXADA`); `FIXADA` só entre Maio e Outubro,
  e interpolada precisa de `fundamentacao`.
- Depois de **publicado**, alterar pede `motivoAlteracao` (`ACORDO` / `CONVENIENCIA_SERVICO`) e, na
  conveniência, `fundamentacao`. O ecrã só deve mostrar estes campos quando `mapaPublicadoEm` vem
  preenchido.
- Publicar duas vezes dá **409**.

### 11.24 Parâmetros do mapa de férias num catálogo (2026-09-23)

**Nada deixa de funcionar**: os endpoints do mapa respondem igual. Os prazos e a janela de fixação
deixaram de vir de `application.properties` e passam a vir de um catálogo por vigência.

- `GET/POST /catalogs/parametros-ferias`, `PUT /catalogs/parametros-ferias/{id}` e
  `GET /catalogs/parametros-ferias/vigente?ano=`. Ver `api_guide.md` §9.2.
- Datas em texto `MM-dd`. O `PUT` não apaga o que vem omisso.
- A resposta traz `origem` (`TABELA` / `LEI`): com `LEI` não há linha para editar (`id` nulo) —
  o ecrã oferece criar uma vigência.

### 11.25 Horários: catálogo, unidade orgânica e colaborador (2026-09-23)

**Nada deixa de funcionar**: são endpoints novos e um campo opcional novo na unidade orgânica.

- Catálogo `/catalogs/horarios` (lista sem paginação, `GET/{id}`, `POST`, `PUT`, `DELETE`,
  `PATCH /{id}/activate`, `PATCH /{id}/base`). Ver `api_guide.md` §9.3.
- Unidade orgânica: `horarioId` opcional no `POST`/`PUT` e na resposta. O `PUT` que não o envia
  **mantém-no**; `""` limpa. Só dá 422 quando vem preenchido com um horário inexistente ou inactivo.
- Colaborador: `GET/POST /funcionarios/{id}/horarios` e `GET /funcionarios/{id}/horarios/vigente?data=`.
  Ver `api_guide.md` §6.7. A resposta do vigente diz a `origem` (`COLABORADOR`, `UNIDADE`, `BASE`,
  `NENHUM`): o ecrã deve mostrar de onde vem o horário.
- Horas em `HH:mm`; dias da semana de 1 (segunda) a 7 (domingo).
- A atribuição pode devolver 201 **com alerta** (tempo parcial com horário de horas completas):
  mostrar, não tratar como erro.

### 11.26 Registo diário de assiduidade (2026-09-23)

**Nada deixa de funcionar**: são endpoints novos.

- `POST /funcionarios/{id}/marcacoes`, `PATCH /funcionarios/{id}/marcacoes/{mid}/anular`,
  `POST /assiduidade/importacao` e `GET /funcionarios/{id}/assiduidade?de=&ate=`. Ver
  `api_guide.md` §6.8.
- Uma marcação **não se edita nem se apaga**: o ecrã oferece «corrigir» (nova marcação, com motivo
  se o dia já tiver marcações) e «anular» (com motivo). As anuladas continuam a vir, marcadas.
- Mostrar as `anomalias` do dia: são os dias a corrigir.
- O lançamento pode devolver 201 **com alerta** (ausência aprovada, feriado, fim-de-semana).

### 11.27 Faltas por débito (2026-09-23)

**Nada deixa de funcionar**: é um endpoint novo, só de leitura.

- `GET /funcionarios/{id}/faltas-apuradas?mes=yyyy-MM`. Ver `api_guide.md` §6.9.
- É um **cálculo**, não um registo: muda quando se corrige uma marcação ou se aprova um pedido. O
  ecrã deve dizer que o mês ainda não está fechado.
- Mostrar os dias `POR_CORRIGIR` e os `SEM_REGISTO` à parte: são o que o RH trata antes do fecho.
- `faltasParciais` e `totalFaltas` são decimais em meios (0.5, 1, 1.5…).

### 11.28 Pedido de ausência em horas e dispensa de amamentação (2026-09-23)

**Nada deixa de funcionar**: os campos são novos e opcionais.

- Pedido de ausência: `horaInicio`/`horaFim` opcionais (`HH:mm`). Sem eles, dias inteiros como
  sempre. Com eles, as horas valem em cada dia do intervalo; a resposta traz `minutosPorDia` e
  `numeroDias` = 0. Ver `api_guide.md` §6.2e.
- Mostrar os campos de hora só para tipos que os admitem (sem saldo, regime FALTA, sem tectos
  anuais/mensais) — ex.: `DISPENSA_AMAMENTACAO`, `TRATAMENTO_AMBULATORIO`.
- `PATCH /funcionarios/{id}/pedidos-ausencia/{pid}/terminar` `{data, motivo}`: fim antecipado de um
  pedido em horas aprovado.
- Catálogo de tipos de ausência: `maxMinutosPorDia` opcional (omisso mantém, 0 limpa).
- Faltas apuradas: cada dia traz `minutosJustificados`.

### 11.29 Pedido de ausência pelo próprio com as regras do RH (2026-09-23)

`POST /me/leave-requests` passa a contar e validar como o pedido lançado pelo RH:

- `numeroDias` pode mudar para o mesmo período: conta pela linha do catálogo (dias úteis ou
  seguidos) e tira os feriados. Antes contava dias de calendário.
- Os tectos aplicam-se: um pedido acima do tecto passa a dar **422**.
- Sobreposição com outro pedido passa de **400** para **409** (como no RH).
- Novos campos opcionais `startTime`/`endTime` (`HH:mm`) para pedidos em horas.

### 11.30 Registo pelo próprio e validação da chefia (2026-09-23)

**Nada deixa de funcionar**: endpoints novos e campos novos nas respostas.

- `/me`: `POST /me/marcacoes` (picagem em tempo real, só em teletrabalho/misto — 422 no presencial),
  `POST /me/marcacoes/correcoes` (fica PENDENTE), `GET /me/assiduidade`,
  `GET /me/equipa/marcacoes-pendentes`, `PATCH /me/equipa/marcacoes/{id}/validar|rejeitar`.
- RH: `PATCH /funcionarios/{id}/marcacoes/{mid}/validar|rejeitar`.
- Cada marcação traz `estado` (VALIDA/PENDENTE/REJEITADA) e `motivoRejeicao`; mostrar as pendentes à
  parte. Faltas apuradas: estado de dia `POR_VALIDAR` e `diasPorValidar`.
- O botão de picar só aparece a quem está em teletrabalho/misto nesse dia (ler o horário vigente).

### 11.31 Trabalho suplementar (horas extras) (2026-09-23)

**Nada deixa de funcionar**: endpoints novos e um campo novo numa resposta.

- RH: `POST|GET /funcionarios/{id}/trabalho-suplementar` (`?mes=yyyy-MM`),
  `PATCH /funcionarios/{id}/trabalho-suplementar/{tid}/autorizar|recusar|cancelar`.
- `/me`: `POST|GET /me/trabalho-suplementar`, `POST /me/equipa/trabalho-suplementar`,
  `GET /me/equipa/trabalho-suplementar-pendente`, `PATCH /me/equipa/trabalho-suplementar/{id}/autorizar|recusar`.
- Horas em `HH:mm`. Mostrar `tipoDia` (DIA_UTIL/DESCANSO/FERIADO), autorizado contra realizado, e
  assinalar `autorizacaoPosterior` e `semRegisto`.
- Faltas apuradas: cada dia traz `minutosSuplementares` (fora de `minutosTrabalhados`).
- Sem valores em dinheiro: só horas.

### 11.32 Relação mensal do art. 75.º (2026-09-23)

**Nada deixa de funcionar**: dois endpoints novos, só leitura.

- `GET /assiduidade/relacao-mensal?mes=yyyy-MM&unidadeId=...&incluirSubunidades=true` (JSON) e
  `GET /assiduidade/relacao-mensal.csv` (mesmos parâmetros; descarregar como ficheiro).
- Ecrã por unidade: uma linha por colaborador, com o `estado` (COMPLETA/COM_PENDENCIAS) em destaque e
  a indicação `provisoria` no mês corrente.
- Faltas apuradas: os dias depois do fim do vínculo passam a `FORA_DO_VINCULO` (antes COM_FALTA/SEM_REGISTO).

### 11.33 Pedidos de ausência: aprovação automática e decisão da chefia (2026-09-24)

- **Muda um comportamento:** os tipos com `requiresApproval: false` (luto, casamento, doença,
  nascimento, greve…) passam a nascer `APROVADO` (antes `PENDENTE`). Um ecrã que mostrava estes
  pedidos na lista «por aprovar» deixa de os ver lá; aprová-los dá 409.
- Campo novo na resposta do pedido: `aprovacaoAutomatica` (boolean).
- Caixa da chefia: `GET /me/equipa/pedidos-ausencia-pendentes`,
  `PATCH /me/equipa/pedidos-ausencia/{id}/aprovar` (corpo opcional `{ "motivo" }`) e `.../rejeitar`
  (`{ "motivo" }` obrigatório).
- Caminho do RH igual; um pedido de outro colaborador no caminho passa a dar 404.

### 11.34 Horários com data de efeito (2026-09-24)

- **Muda um comportamento:** editar os blocos/controlo/aferição/duração de um horário que já vigorou dá
  **409** (só o nome muda). Oferecer «Duplicar» (`POST /catalogs/horarios/{id}/duplicar`, opcional
  `?nome=`) e atribuir o novo a partir de uma data.
- `PATCH /catalogs/horarios/{id}/base` aceita `?desde=yyyy-MM-dd` (hoje ou futura). `isBase` é o de hoje.
- Unidade orgânica: campo novo opcional `horarioDesde` no `PUT` (hoje ou futura). A resposta traz o
  `horarioId` de hoje; uma mudança agendada só aparece na data.
- Datas passadas: 422 (não se reescreve o apuramento de dias já passados).

### 11.35 Lista de antiguidade (2026-09-24)

**Nada deixa de funcionar**: dois endpoints novos, só leitura.

- `GET /relatorios/lista-antiguidade?ano=&unidadeId=&incluirSubunidades=true` (JSON) e `.csv`.
- Ecrã por cargo (carreira/categoria), com a posição, a data de início no cargo, os dias descontados e o
  tempo contado (anos/meses/dias); botão de descarregar o CSV.

### 11.36 Mapa de férias: preferência pelo próprio e pedido fora da marcação (2026-09-24)

**Nada deixa de funcionar**: endpoints novos e campos novos.

- `/me`: `GET /me/ferias/{ano}`, `PUT /me/ferias/{ano}/preferencia`, `GET /me/equipa/ferias/{ano}`.
- Férias do ano: campo `preferenciaIndicadaPor` (PROPRIO/RH).
- Criação de pedido de ausência: resposta com `alertas` (ex.: férias fora da marcação do mapa) —
  mostrar ao utilizador. No `/me` os alertas vêm no `SuccessResponseDTO`.
- Caixa da chefia: `foraDaMarcacao` em cada pedido pendente.

### 11.37 Mapa de efectivos (2026-09-24)

**Nada deixa de funcionar**: dois endpoints novos, só leitura.

- `GET /relatorios/mapa-efectivos?unidadeId=&incluirSubunidades=true` (JSON) e `.csv`.
- Ecrã por unidade e cargo: lugares, providos, vagos, congelados, com totais.

### 11.38 Indicadores do pessoal (2026-09-24)

**Nada deixa de funcionar**: um endpoint novo, só leitura.

- `GET /relatorios/indicadores?unidadeId=&incluirSubunidades=true&ano=` — números para gráficos:
  efectivos por género, escalão etário, contrato, carreira e unidade (listas `{chave, valor}`),
  entradas, saídas, taxa de absentismo e horas extras. O gráfico é do front.

### 11.39 Congelar exige motivo, e passa a poder descongelar-se (BREAKING, 2026-09-24)

- **Muda um comportamento:** `PATCH /positions/{id}/freeze` passa a exigir corpo `{ "motivo": "…", "despachoNumero": "…" }`
  (despacho opcional). **Sem corpo ou sem motivo dá 400** — o ecrã actual, que não manda corpo, tem de pedir o motivo.
- **Não se congela um Lugar com titular:** 422. O ecrã não deve oferecer «Congelar» num Lugar provido (`ocupado: true`).
- **Novo:** `PATCH /positions/{id}/unfreeze`, com o mesmo corpo — o Lugar volta a `ATIVO` e à dotação. Oferecer
  «Descongelar» só nos Lugares `CONGELADO`.
- Congelar o já congelado, ou descongelar o já activo: 200 com `sucesso: false` (nada a fazer). Extinto: 409 nos dois.
- `PositionResponseDTO` ganha `estadoMotivo`, `estadoDespacho`, `estadoDesde` — mostrar no Lugar congelado porquê e desde quando.
- Um Lugar que não pode voltar extingue-se (`DELETE /positions/{id}`); o congelamento é sempre reversível.

### 11.40 `POST /assignments` passa a ser só a colocação (BREAKING, 2026-09-24)

- **Quem já tem Lugar recebe 422** — os movimentos fazem-se pelos endpoints próprios (secção 5). A porta genérica
  deixava mover pessoas sem nenhuma das regras dos movimentos.
- **A `origem` é do sistema:** `ADMISSAO` na primeira vez, `REINGRESSO` para quem já teve Lugar. Omitir, ou enviar a
  calculada; outro valor dá 422. Valor novo `REINGRESSO` nas listas de origem.
- **Exige contrato corrente `ATIVO`** (e não começa antes dele) — também no registo composto: afectação sem contrato dá 422.
- Recusa quem está em inactividade fora do quadro (licença em curso) e, no reingresso, um Lugar de outra categoria (art. 122.º).
  Sem `gradeId` no reingresso mantém-se o escalão que tinha; quem estava em disponibilidade volta ao estado de actividade.
- **Ecrã:** «Colocar num Lugar» só para colaboradores sem Lugar (`unidade-atual` sem `positionId`); no reingresso, filtrar os
  Lugares vagos pela categoria que a pessoa tinha.

---

### 11.41 Notificações na aplicação (2026-09-25)

**Nada deixa de funcionar**: endpoints novos, só leitura e marcar lida (guia §14).

- Sino: `GET /me/notificacoes/contagem` → `naoLidas`; lista `GET /me/notificacoes?naoLidas=true`; abrir uma →
  `PATCH /me/notificacoes/{id}/lida`; "marcar todas" → `PATCH /me/notificacoes/lidas`.
- Caixa do RH: `GET /notificacoes?perfil=RH` e `PATCH /notificacoes/{id}/lida?perfil=RH`.
- Cada notificação traz `recursoTipo` + `recursoId` para abrir o ecrã certo (hoje: `PEDIDO_AUSENCIA`).
- Os pedidos de ausência passam a avisar: por decidir → a chefia directa (sem chefia, a caixa do RH); decidido → quem pediu.

### 11.42 Fronteira salarial — diário de factos (2026-09-25)

**Nada deixa de funcionar**: endpoints novos, só leitura (guia §15).

- `GET /salarial/factos?mes=yyyy-MM` (e `.csv`) — ecrã de consulta do RH e contrato de leitura com o salarial (`versao: 1`).
- `GET /salarial/funcionarios/{id}/factos` — separador «Factos para o salarial» na ficha do colaborador.
- Os movimentos, a colocação, a mudança de estado e a cessação passam a registar o facto; as respostas não mudam.

### 11.43 Aposentação e limite de idade (2026-09-25)

**Nada deixa de funcionar**: endpoints novos (guia §16).

- Ficha do colaborador: separador «Aposentação» com `GET /funcionarios/{id}/aposentacao` (datas, condições, processos, prorrogações)
  e as acções do processo (`deferir`, `indeferir`, `desligar`, `concluir`, `cancelar`) e da prorrogação (`autorizar`, `indeferir`).
- Relatórios: «Aposentações previstas» com `GET /relatorios/aposentacao?unidadeId=&ate=` (e `.csv`).
- `/me`: «A minha aposentação» (`GET /me/aposentacao`) e o pedido da antecipada ou da pré-aposentação (`POST /me/aposentacao/processos`).
- Notificações novas: `LIMITE_IDADE_PROXIMO` e `PROCESSO_APOSENTACAO` (recurso `PROCESSO_APOSENTACAO`, `PRORROGACAO_PERMANENCIA`, `FUNCIONARIO`).

### 11.44 Declarações e documentos emitidos (2026-09-25)

**Nada deixa de funcionar**: endpoints novos (guia §17).

- Ficha do colaborador: «Declarações» (`POST/GET /funcionarios/{id}/declaracoes`, emitir e recusar) e «Documentos emitidos»
  (`GET /funcionarios/{id}/documentos-emitidos`, descarregar por `GET /documentos-emitidos/{id}/link`, anular).
- Caixa do RH: `GET /declaracoes/por-emitir`.
- `/me`: pedir (`POST /me/declaracoes`), ver os pedidos e descarregar os documentos (`/me/documentos-emitidos/{id}/link`).
- Página pública de verificação: `GET /verificacao/documentos/{codigo}` (sem login).
- Upload de ficheiros: `POST /documento/private` (sem pasta) passa a aceitar-se e grava em `outros`; uma pasta desconhecida também cai em
  `outros` (antes dava 400).

### 11.45 Lista de antiguidade oficial — o ciclo (2026-09-25)

**Nada deixa de funcionar**: endpoints novos (guia §18); a lista gerada (`/relatorios/lista-antiguidade`) não muda.

- Ecrã «Listas de antiguidade»: aprovar a partir da gerada, afixar, recalcular, tornar definitiva, publicar, anular.
- Reclamações por lista: registar, decidir, recurso e decisão do recurso; mostrar os `alertas` das respostas.
- `/me`: «A minha antiguidade» (`GET /me/listas-antiguidade`) e reclamar (`POST /me/listas-antiguidade/{id}/reclamacoes`).
- Notificações novas: `LISTA_ANTIGUIDADE_AFIXADA`, `RECLAMACAO_ANTIGUIDADE` (recurso `LISTA_ANTIGUIDADE`).

## 12. Checklist do alinhamento com a legislação (secção 11)

Por ordem das secções. Cada item remete para o ecrã correspondente em `apresentacao_aplicacao.html`.

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
- [ ] Indicadores: painel do serviço (gráficos por género, idade, contrato, carreira, unidade; absentismo; horas extras; entradas e saídas).
- [ ] Mapa de efectivos: ecrã por unidade e cargo (lugares, providos, vagos, congelados) e o CSV.
- [ ] Férias no `/me`: indicar a preferência até 31 de Janeiro, ver a marcação; a chefia vê a da equipa. Mostrar o aviso de pedido fora da marcação.
- [ ] Lista de antiguidade: ecrã por serviço e ano, agrupado por cargo, e o CSV para afixar.
- [ ] Horários: botão «Duplicar» e aviso de 409 ao editar um horário que já vigorou; data de efeito (opcional) ao mudar o base e o horário da unidade.
- [ ] Pedidos de ausência: caixa da chefia (aprovar/rejeitar com motivo); mostrar «aprovado automaticamente» nos tipos que não requerem aprovação.
- [ ] Relação mensal: ecrã por unidade (mês, com subunidades), pendências em destaque, botão de descarregar o CSV.
- [ ] Trabalho suplementar: separador no colaborador (mês, autorizado vs realizado por tipo de dia), pedido pelo próprio no `/me`, e a caixa de pendentes da chefia (autorizar/recusar).
- [ ] `/me`: botão de picar (só em teletrabalho/misto), pedido de correcção com motivo, e a caixa de pendentes da equipa para a chefia (validar/rejeitar).
- [ ] Separador de faltas apuradas do mês: dias com falta e motivo, débitos da aferição, total em dias e meios-dias, e atalho para justificar (pedido de ausência).
- [ ] Congelar: pedir o motivo (e o despacho) e esconder a acção nos Lugares com titular; «Descongelar» nos congelados, com motivo; mostrar `estadoMotivo`/`estadoDesde`.
- [ ] «Colocar num Lugar» só para quem não tem Lugar; sem campo de origem (é do sistema); no reingresso, Lugares vagos da categoria anterior; contrato antes da colocação.
