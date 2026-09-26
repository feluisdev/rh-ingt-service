# RH-Service — Guia de API (v5)

> Fonte de verdade do contrato REST do núcleo RH (exclui o módulo `sigdi`).
> Documentação **v5** — supersede a `v4`. pt-PT.
> Última alteração: 2026-09-26
>
> O manual ecrã a ecrã (com os campos, a API e as regras de cada ecrã) é o `apresentacao_aplicacao.html`;
> este guia é o contrato para quem integra. Ambos são conferidos contra o código por `scripts/verificar_docs.py`.

## 1. Base, autenticação e cabeçalhos

| Item | Valor |
|---|---|
| **Base URL (dev)** | `http://localhost:8099` (a porta de `SERVICE_PORT` no `.env`) |
| **Prefixo comum** | `/api/v1/rh` |
| **Swagger** | `/swagger-ui.html` (quando `ENABLE_SWAGGER=true`) |
| **Autenticação** | `development`/`staging`: **desligada**. `production`: OAuth2 Resource Server + JWT (Keycloak) — enviar `Authorization: Bearer <token>`. |
| **Cabeçalhos** | Enviar `Accept: application/json`. Em `POST/PUT/PATCH`: `Content-Type: application/json`. **Excepção:** os três endpoints `.csv` (relação mensal, lista de antiguidade, mapa de efectivos) respondem **406** a `application/json` — pedir com `Accept: text/csv` (ou `*/*`). |

> **Nota:** sem `Accept: application/json`, alguns clientes recebem XML. Enviar sempre o header.

### Contrato legível por máquina

`openapi.json` (nesta pasta) é o contrato **gerado a partir do código** pelo springdoc: caminhos, parâmetros e esquemas dos pedidos e das respostas. Foi extraído com a aplicação a correr:

```bash
curl -s -o docs/funcionarios/v5/openapi.json http://localhost:8099/v3/api-docs
```

Quem implementa um cliente deve **ler o `openapi.json` para as formas** e este guia para o resto, porque há duas coisas que o gerado ainda não diz:

1. **Os erros.** Das 393 operações (301 do núcleo RH, contando `/documento`), só 6 declaram respostas 4xx. As regras de negócio e os **422** vivem aqui e no `regras_negocio.html`.
2. **Algumas respostas.** *(resolvido)* As 301 operações do núcleo RH têm todas esquema de resposta declarado.

> **Testar a documentação.** `PYTHONIOENCODING=utf-8 python scripts/verificar_docs.py` confere que cada caminho da API
> está neste guia (e que o guia não cita caminhos que não existem), que cada campo desenhado nos ecrãs da apresentação
> existe no DTO do `openapi.json`, as tabelas do modelo relacional, as regras BR-*, os artigos da lei e as datas.
> Tem de acabar com `FALHAS: 0`. Para confirmar que o `openapi.json` está em dia, compará-lo com o da app a correr.

Regenerar o `openapi.json` sempre que se acrescente ou mude um endpoint.

## 2. Convenções transversais

### Paginação e listagem
Endpoints de lista aceitam `pagina` (0-based) e `tamanho` (default 20) e devolvem um **wrapper**:

```json
{
  "content": [ /* … */ ],
  "totalElements": 42,
  "pageNumber": 0,
  "pageSize": 20,
  "totalPages": 3,
  "first": true,   // ver nota
  "last": false    // ver nota
}
```

> **Variações do wrapper:** a maioria dos wrappers inclui `first`/`last`; o de **funcionários** omite-os (só `pageNumber`/`pageSize`/`totalPages`); o de **Lugares** (`WrapperListaPositionDTO`) substitui-os por `dotacao`/`ocupados`/`vagas` (**`ocupados` conta titulares**); o de **auditoria** traz apenas `content`/`totalElements`.

### Resposta das operações simples

As operações que só têm a dizer **o que foi afectado e se correu bem** devolvem `SuccessResponseDTO`, e não um objecto livre:

```json
{
  "id": "uuid | null",     // o que foi criado ou alterado
  "sucesso": true,         // a operação produziu efeito
  "alertas": []            // avisos que não impedem a operação; vazio, nunca nulo
}
```

Cobre os `POST` de criação, os `PUT` de actualização, as desactivações (`DELETE`), as reactivações (`activate`) e as decisões simples (aprovar, rejeitar, validar, anular…) — 113 operações ao todo.

As operações com mais a dizer têm **DTO próprio**. Nenhuma operação devolve já um objecto livre:

| Resposta | Onde |
|---|---|
| `FuncionarioCriadoResponseDTO` | `POST /funcionarios` — traz o `numeroFuncionario` atribuído pela aplicação |
| `PedidoAusenciaCriadoResponseDTO` | `POST /funcionarios/{id}/pedidos-ausencia` — `numeroDias` contados pelo servidor e `estado` inicial |
| `LicencaEfeitoResponseDTO` | `approve` · `ativar` · `close` da licença — `afectacaoEncerradaId` e `estadoAtribuidoId`, nulos quando não houve efeito |
| `UnidadeAtualResponseDTO` | `GET .../unidade-atual` — separa o Lugar de que é titular de onde exerce funções |
| `ChefeFuncionarioResponseDTO` · `ResponsavelUnidadeResponseDTO` | `GET .../chefe` · `.../responsavel` — com `estado` PROVIDO / CHEFIA_VAGA / SEM_… |
| `VagasUnidadeResponseDTO` | `GET .../vagas` — `ocupados` conta **titulares** |
| `FileUrlDTO` | os dois `download-url` do self-service |

**`sucesso: false` não é erro.** É uma operação idempotente a dizer que não teve nada a fazer, com o motivo em `alertas`. Por exemplo, reactivar algo que já estava activo:

```json
{ "id": "uuid", "sucesso": false, "alertas": ["Dependente já está activo."] }
```

> **Breaking change:** estas operações devolviam antes `{"message": "..."}`. O campo `message` **desapareceu** — o texto de sucesso pertence ao ecrã, não à API. O que era informação a sério (o "já estava assim") passou a `sucesso: false` + `alertas`.

As operações com mais a dizer — o registo composto, os movimentos de carreira, a mudança de estado, a substituição — mantêm **DTO próprio**, e não este com campos a mais.

### Padrões de recurso
| Padrão | Descrição |
|---|---|
| `GET /{recurso}` | Lista (com filtros por query string). |
| `GET /{recurso}/{id}` | Detalhe. |
| `POST /{recurso}` | Criar (201). |
| `PUT /{recurso}/{id}` | Atualizar. |
| `DELETE /{recurso}/{id}` | Soft delete (marca `is_active=false`). |
| `PATCH .../activate` \| `PUT .../activate` | Reactivar. |
| `GET .../combobox` | Lista reduzida `{key,label}` para selects (`key` = id do registo). |
| `GET .../audit/{catalog}/{entityId}` | Histórico de revisões (Envers). |

### Códigos de estado
| Código | Significado |
|---|---|
| `200` / `201` | OK / criado. |
| `400` | Pedido inválido (ex.: catálogo de auditoria desconhecido). |
| `404` | Recurso não encontrado. |
| `422` | **Violação de regra de negócio** (Lugar já com titular, escalão obrigatório/proibido, mobilidade sem Lugar de destino, estado não permitido…). |

---

## 3. Estrutura — Mapa de Pessoal

### 3.1 Lugares (Positions) — `/api/v1/rh/estrutura/positions`
| Método | Path | Descrição |
|---|---|---|
| `GET` | `/positions?unidadeId={id}` | Lista Lugares de uma unidade + agregados de dotação. |
| `GET` | `/positions/{id}` | Detalhe de um Lugar. |
| `POST` | `/positions` | Criar Lugar. |
| `PUT` | `/positions/{id}` | Atualizar Lugar. |
| `PATCH` | `/positions/{id}/freeze` | Congelar (estado `CONGELADO`): sai da dotação. Corpo `EstadoLugarRequestDTO`; recusa um Lugar com titular. |
| `PATCH` | `/positions/{id}/unfreeze` | Descongelar: volta a `ATIVO` e à dotação. Corpo `EstadoLugarRequestDTO`. |
| `DELETE` | `/positions/{id}` | Extinguir (estado `EXTINTO`). |

**`PositionRequestDTO`**
```json
{
  "numeroLugar": "A-01",
  "jobId": "uuid",
  "unidadeOrganicaId": "uuid",
  "careerId": "uuid | null",       // null = fora de grelha
  "categoryId": "uuid | null",
  "parentPositionId": "uuid | null",
  "managesUnitId": "uuid | null",  // unidade que este Lugar dirige
  "legalBase": "string | null"
}
```

**`PositionResponseDTO`** — inclui nomes resolvidos para leitura direta:
```
id, numeroLugar, jobId, jobNome, unidadeOrganicaId, unidadeNome,
careerId, careerNome, categoryId, categoryNome,
parentPositionId, managesUnitId, estado, estadoMotivo, estadoDespacho, estadoDesde,
legalBase, isActive, foraDeGrelha, ocupado
```

**Congelar e descongelar** — `PATCH /positions/{id}/freeze` e `PATCH /positions/{id}/unfreeze`, com o mesmo corpo:

```json
PATCH /api/v1/rh/estrutura/positions/{id}/freeze
{ "motivo": "Sem dotação orçamental no OE 2027", "despachoNumero": "Desp. 12/2026" }
→ 200 { "id": "…", "sucesso": true, "alertas": [] }
```

| Caso | Resposta |
|---|---|
| sem `motivo` (os dois) | 400 |
| congelar um Lugar **com titular** — o titular sai primeiro (transferência ou cessação) | 422 |
| congelar ou descongelar um Lugar `EXTINTO` | 409 |
| congelar o já congelado · descongelar o já activo | 200 com `sucesso: false` e o motivo em `alertas` |
| Lugar inexistente | 404 |

O motivo, o despacho e a data do estado actual vêm em `estadoMotivo`, `estadoDespacho` e `estadoDesde`
(nulos nos Lugares congelados antes da V59); o histórico está na auditoria. Um Lugar que não pode voltar
**extingue-se** (`DELETE /positions/{id}`, definitivo). Regras: BR-POS-05 a BR-POS-07.

> **Breaking:** `freeze` passou a exigir corpo com `motivo` — sem ele dá 400 (ver a secção 11.39 do `breaking_change_frontend.md`).

**`WrapperListaPositionDTO`** (lista) = wrapper de paginação + `dotacao`, `ocupados`, `vagas`.

### 3.2 Cargos, Unidades, Funções
| Recurso | Base |
|---|---|
| Cargos (jobs) | `/api/v1/rh/estrutura/jobs` |
| Unidades orgânicas | `/api/v1/rh/estrutura/organizational-units` |
| Funções | `/api/v1/rh/estrutura/functions` |

Cada um: `GET` (lista), `GET/{id}`, `POST`, `PUT/{id}`, `DELETE/{id}/deactivate`, `PATCH/{id}/activate`, `GET/combobox`.

---

## 4. Colaboradores — Afectação

### Afectação (Assignments) — `/api/v1/rh/colaboradores/assignments`
| Método | Path | Descrição |
|---|---|---|
| `POST` | `/assignments` | **Colocar** num Lugar quem não tem nenhum: admissão ou reingresso. |
| `GET` | `/funcionario/{id}/unidade-atual` | Lugar/unidade corrente do funcionário. |
| `GET` | `/funcionario/{id}/chefe` | Chefe direto (via `parent_position_id`). |
| `GET` | `/unidade/{id}/responsavel` | Responsável da unidade (via `manages_unit_id`). |
| `GET` | `/unidade/{id}/vagas` | `{unidadeId, dotacao, ocupados, vagas}`. |
| `GET` | `/unidade/{id}/vagas/lista` | Lista de Lugares **vagos** (picker de admissão). |

**`AfectacaoRequestDTO`**
```json
{
  "funcionarioId": "uuid",
  "positionId": "uuid",
  "gradeId": "uuid | null",        // obrigatório em Lugar de carreira; proibido fora de grelha
  "functionId": "uuid | null",
  "origem": "omitir | ADMISSAO | REINGRESSO",   // do sistema: tem de coincidir com a calculada
  "assignmentType": "PRINCIPAL",                          // único valor aceite aqui; default PRINCIPAL
  "dataInicio": "YYYY-MM-DD",
  "notes": "string | null"
}
```

**Para que serve.** É o único caso de escrita que nenhum movimento cobre: pôr num Lugar quem **não tem
nenhum** — a pessoa criada com `POST /funcionarios` (sem o registo composto), e o **reingresso** de quem
ficou sem Lugar (disponibilidade depois de uma licença que abriu vaga). Quem já tem Lugar muda-o por um
movimento: progressão (5.4), promoção (5.5), transferência (5.6), mudança de carreira (5.9).

| Caso | Resposta |
|---|---|
| colaborador inexistente | 404 |
| inactivo · **já tem afectação principal corrente** (use o movimento) | 422 |
| em inactividade fora do quadro — a licença que lhe tirou o Lugar ainda decorre | 422 |
| sem contrato corrente `ATIVO`, ou a começar antes do contrato | 422 |
| `origem` diferente da calculada (`ADMISSAO` na primeira vez, `REINGRESSO` depois) | 422 |
| antes da admissão, ou sem ser depois do fim da última afectação | 422 |
| reingresso num Lugar de carreira de **outra categoria** (art. 122.º) | 422 |
| Lugar não disponível (CONGELADO/EXTINTO) · Lugar já **tem titular** · escalão obrigatório/proibido/de outra categoria · `assignmentType` fora da lista | 422 |

No reingresso sem `gradeId`, mantém o escalão que tinha. Quem estava em **disponibilidade** passa ao
estado de situação `ACTIVIDADE_NO_QUADRO`, com histórico; sem esse estado no catálogo, a resposta
traz um alerta. O registo composto (5.2) passa pelas mesmas regras. Regras: BR-AF-13, BR-AF-15 a BR-AF-22.

**Uma cadeira, um titular.** A regra do Lugar ocupado só se aplica a `assignmentType = PRINCIPAL`. Quem entra em `SUBSTITUICAO` **não exige que o Lugar esteja vago** e não desaloja o titular — é o que autoriza a substituição do funcionário temporariamente impedido (art. 73.º al. a) a c)). Em consequência, um Lugar com substituto e **sem** titular continua a contar como **vago** em `/unidade/{id}/vagas` e na lista do picker.

> **Este endpoint é só para a titularidade.** `assignmentType = SUBSTITUICAO` é recusado com **422**: a substituição cria-se em 5.7, que verifica se o titular pode ser substituído e liga as duas afectações. E `ACUMULACAO` **deixou de existir** como título — ver a nota abaixo.

> **A `ACUMULACAO` saiu (2026-09-22).** Fundava-se no art. 134.º n.º 2 al. b) da Lei n.º 20/X/2023, mas esse artigo trata da *forma de prestação da mobilidade* — «*em regime de acumulação, quando o funcionário passa a exercer funções noutro serviço, em acumulação com as do serviço de origem*» — e não de um título para ocupar um segundo Lugar. Como a mobilidade transitória é *sem ocupação do lugar do quadro* (art. 135.º n.º 7), uma mobilidade em acumulação **não cria afectação nenhuma**: vive no registo da mobilidade (secção 7).

---

## 5. Funcionário e registo

### 5.1 Funcionário — `/api/v1/rh/funcionarios`
| Método | Path | Descrição |
|---|---|---|
| `GET` | `/funcionarios?nome=&nif=&workerStateId=&unidadeOrganicaId=&careerId=&active=&pagina=&tamanho=` | Lista com filtros. Unidade/carreira derivam da **afectação corrente**. |
| `GET` | `/funcionarios/{id}` | Detalhe. |
| `POST` | `/funcionarios` | Criar (dados base). |
| `PUT` | `/funcionarios/{id}` | Atualizar. |
| `POST` | `/funcionarios/registar` | **Registo completo** (ver 5.2). |
| `PATCH` | `/funcionarios/{id}/worker-state` | Mudar estado do trabalhador. |
| `GET` | `/funcionarios/{id}/worker-state/historico` | Histórico de estados. |
| `POST` | `/funcionarios/{id}/progressao` | **Progressão** para o escalão seguinte (ver 5.4). |
| `POST` | `/funcionarios/{id}/promocao` | **Promoção** para a categoria seguinte (ver 5.5). |
| `POST` | `/funcionarios/{id}/transferencia` | **Transferência** para outro Lugar (ver 5.6). |
| `POST` | `/funcionarios/{id}/substituicao` | **Substituição** de um titular impedido (ver 5.7). |
| `GET` | `/funcionarios/{id}/substituicoes` | Substituições, nos dois papéis (ver 5.7). |
| `GET` | `/funcionarios/{id}/antiguidade` | **Antiguidade** a uma data (ver 5.8). |
| `POST` | `/funcionarios/{id}/mudanca-carreira` | **Mudança de carreira** (ver 5.9). |
| `GET` | `/funcionarios/{id}/details` | Detalhe agregado (inclui bloco `enquadramento` derivado do Lugar, por compat). O bloco `contrato` traz `vinculoLaboralId`, `vinculoLaboralCode` e `vinculoLaboralDesc`, **derivados** do tipo de contrato. |
| `GET` | `/funcionarios/combobox` | Combobox. |

### 5.2 Registo — `POST /api/v1/rh/funcionarios/registar`
Cria funcionário + (opcional) contrato + **afectação a um Lugar vago** numa só chamada.

```json
{
  "funcionario": { /* dados de identificação, NIF, etc. */ },
  "contrato":    { /* opcional */ },
  "afectacao": {
    "positionId": "uuid",
    "gradeId": "uuid | null",
    "functionId": "uuid | null",
    "origem": "ADMISSAO",
    "assignmentType": "PRINCIPAL",
    "dataInicio": "YYYY-MM-DD",
    "notes": "string | null"
  },
  "dadosBancarios": { /* opcional */ },
  "dossier":        [ /* opcional: lista de documentos a anexar */ ]
}
```

**Resposta:** inclui `afectacaoId` (a afectação criada). **Não** existe `enquadramentoId`.

**Com afectação, o contrato é obrigatório** (BR-AF-18): sem vínculo não há Lugar. A afectação do registo passa pelas regras
da colocação (secção 4), e a origem é sempre `ADMISSAO`.

> ⚠️ **Breaking change:** o registo já **não** aceita unidade/cargo/carreira/categoria — todos derivam do `positionId`. Ver `breaking_change_frontend.md`.

### 5.3 Mudança de estado — `PATCH /funcionarios/{id}/worker-state`
Mudar para um **estado de cessação** (catálogo: `endsEmployment = true`) termina o vínculo: cessa o contrato, **encerra a afectação corrente** (o Lugar volta a vago), muda o estado e regista o histórico. Request inalterado.

**Resposta `200`** (`EstadoColaboradorResponseDTO`, já não é um mapa)**:**
```json
{
  "funcionarioId": "uuid",
  "estadoAnteriorId": "uuid",
  "estadoNovoId": "uuid",
  "estadoNovoCode": "RETIRED",
  "dataEfectividade": "YYYY-MM-DD",
  "cessouVinculo": true,
  "contratoId": "uuid | null",
  "afectacaoEncerradaId": "uuid | null"
}
```

> **Cessação tem um caminho único.** Este endpoint e `PUT /contratos/{id}/close` fazem exactamente o mesmo, e devolvem o mesmo DTO. Que estados cessam o vínculo é configuração (`endsEmployment` no catálogo de estados), não código.

**Estados que não cessam o vínculo** produzem os efeitos da sua **situação funcional** (`situacaoFuncional` no catálogo de estados, Lei n.º 20/X/2023 art. 117.º):

| Situação | Contrato | Lugar |
|---|---|---|
| `ACTIVIDADE_NO_QUADRO` (art. 118.º) | reactiva, se suspenso | mantém |
| `ACTIVIDADE_FORA_QUADRO` (art. 119.º) | reactiva, se suspenso | mantém |
| `INACTIVIDADE_NO_QUADRO` (art. 120.º) | suspende | mantém |
| `INACTIVIDADE_FORA_QUADRO` (art. 121.º) | suspende | **encerra a afectação — abre vaga** (n.º 2) |
| `DISPONIBILIDADE` (art. 122.º) | reactiva, se suspenso | mantém |
| `APOSENTACAO` | cessa (caminho da cessação) | encerra |

Um estado **sem situação** classificada só regista histórico. Quando a situação abre vaga, a resposta traz `afectacaoEncerradaId` preenchido e `cessouVinculo: false` — o colaborador continua activo, sem Lugar.

### 5.4 Progressão — `POST /funcionarios/{id}/progressao`
Sobe o colaborador para o **escalão imediatamente superior da mesma categoria**. O Lugar e a função mantêm-se e não é precisa vaga. **O escalão é escolhido pelo sistema** (o próximo escalão activo da categoria do Lugar), não pelo cliente.

```json
{
  "dataEfeito": "YYYY-MM-DD",
  "despachoNumero": "string | null",
  "observacoes": "string | null"
}
```

**Resposta `201`** (`ProgressaoResponseDTO`)**:**
```json
{
  "id": "uuid da nova afectação",
  "funcionarioId": "uuid",
  "positionId": "uuid",
  "escalaoAnteriorId": "uuid",
  "escalaoAnterior": "Escalão 1",
  "escalaoNovoId": "uuid",
  "escalaoNovo": "Escalão 2",
  "dataEfeito": "YYYY-MM-DD"
}
```

| Código | Quando |
|---|---|
| `400` | `dataEfeito` em falta. |
| `404` | Funcionário não existe. |
| `422` | Colaborador inactivo · sem contrato corrente ou com vínculo que não permite progressão · sem afectação principal corrente · `dataEfeito` não posterior ao início da afectação corrente · Lugar fora de grelha · já está no último escalão. |

No histórico, a afectação corrente fecha na véspera de `dataEfeito` e abre-se uma nova com `origem = PROGRESSAO`. Regras completas: `regras_negocio.html`, secção 3.1 (BR-PRG-01 a 09).

> Não usar `POST /assignments` para movimentos: quem já tem Lugar recebe 422 (BR-AF-16), e a `origem` só aceita `ADMISSAO` ou `REINGRESSO`.

### 5.5 Promoção — `POST /funcionarios/{id}/promocao`
Passa o colaborador à **categoria imediatamente superior da mesma carreira**. Há duas formas e **não se indica qual**: infere-se do pedido.

| Envia `positionId`? | O que acontece |
|---|---|
| **Sim** | Muda para esse Lugar, que tem de estar **vago, ATIVO e ser da categoria de destino**. O Lugar antigo fica vago. |
| **Não** | Fica no mesmo Lugar e **o Lugar sobe de categoria** (reclassificação). O Lugar mantém a nova categoria depois de a pessoa sair. |

```json
{
  "categoryId": "uuid da categoria de destino",
  "positionId": "uuid | omitir",
  "gradeId": "uuid | omitir (por omissão: 1.º escalão activo da categoria de destino)",
  "dataEfeito": "YYYY-MM-DD",
  "despachoNumero": "string | null",
  "concursoRef": "string | null",
  "observacoes": "string | null"
}
```

**Resposta `201`** (`PromocaoResponseDTO`)**:**
```json
{
  "id": "uuid da nova afectação",
  "funcionarioId": "uuid",
  "positionId": "uuid do Lugar final",
  "categoriaAnteriorId": "uuid",
  "categoriaAnterior": "Técnico",
  "categoriaNovaId": "uuid",
  "categoriaNova": "Técnico Superior",
  "escalaoId": "uuid",
  "escalao": "Escalão 1",
  "lugarReclassificado": true,
  "dataEfeito": "YYYY-MM-DD"
}
```

| Código | Quando |
|---|---|
| `400` | `categoryId` ou `dataEfeito` em falta; UUID inválido. |
| `404` | Funcionário, categoria, Lugar ou escalão não existem. |
| `422` | Colaborador inactivo · vínculo não permite · sem afectação corrente · `dataEfeito` não posterior ao início da afectação · Lugar actual fora de grelha · categoria de destino inactiva, de outra carreira ou que não é a imediatamente superior · escalão que não pertence à categoria de destino · Lugar de destino com titular, não ATIVO ou de outra categoria. |

A modalidade **não é guardada**: deduz-se do histórico comparando o Lugar da afectação anterior com o da nova. Regras completas: `regras_negocio.html`, secção 3.2 (BR-PRM-01 a 10).

### 5.6 Transferência — `POST /funcionarios/{id}/transferencia`
Mudança **definitiva** de Lugar **sem subir na grelha**: mantém carreira, categoria e **escalão** (herdado, não se envia). A unidade orgânica muda por consequência, porque deriva do Lugar.

```json
{
  "positionId": "uuid do Lugar de destino",
  "functionId": "uuid | omitir (mantém a função actual, se for compatível)",
  "dataEfeito": "YYYY-MM-DD",
  "despachoNumero": "string | null",
  "observacoes": "string | null"
}
```

**Resposta `201`** (`TransferenciaResponseDTO`)**:**
```json
{
  "id": "uuid da nova afectação",
  "funcionarioId": "uuid",
  "positionAnteriorId": "uuid",
  "numeroLugarAnterior": "L-001",
  "unidadeOrganicaAnteriorId": "uuid",
  "positionId": "uuid",
  "numeroLugar": "L-042",
  "unidadeOrganicaId": "uuid",
  "functionId": "uuid | null",
  "dataEfeito": "YYYY-MM-DD"
}
```

| Código | Quando |
|---|---|
| `400` | `positionId` ou `dataEfeito` em falta; UUID inválido. |
| `404` | Funcionário, Lugar ou função não existem. |
| `422` | Colaborador inactivo · sem afectação corrente · `dataEfeito` não posterior ao início da afectação · Lugar de destino igual ao actual, com titular ou não ATIVO · Lugar de destino de outra carreira/categoria (use a promoção) · função incompatível com o cargo do destino. |

**Sobre a função:** se não enviar `functionId`, mantém-se a função actual quando é compatível com o cargo do Lugar de destino; quando não é, devolve `422` a pedir que a indique — nunca se perde em silêncio. Regras completas: `regras_negocio.html`, secção 3.3 (BR-TRF-01 a 08).

> Uma mudança **temporária** de Lugar, com regresso, não é transferência: é **mobilidade** (secção 7).
>
> Um Lugar de destino de **outra carreira** também não é transferência: é **mudança de carreira** (5.9).

---

### 5.7 Substituição — `POST /funcionarios/{funcionarioId}/substituicao`

Põe um colaborador a substituir o **titular** de um Lugar que está temporariamente impedido: contrato a termo do art. 73.º al. a) a c), ou nomeação em substituição do art. 91.º n.º 1 al. a).

O `{funcionarioId}` do path é **quem vai substituir**, não o titular. O titular deduz-se do Lugar.

```json
POST /api/v1/rh/funcionarios/{funcionarioId}/substituicao
{
  "positionId": "uuid",            // obrigatório — o Lugar a cobrir, que tem de ter titular
  "gradeId": "uuid | null",        // obrigatório em Lugar de carreira, proibido fora de grelha
  "functionId": "uuid | null",     // tem de pertencer ao cargo do Lugar
  "dataInicio": "YYYY-MM-DD",      // obrigatório
  "despachoNumero": "string | null",
  "observacoes": "string | null"
}
```

`201` com `SubstituicaoResponseDTO`:

```json
{
  "id": "uuid",                    // a afectação de substituição criada
  "funcionarioId": "uuid",         // o substituto
  "positionId": "uuid",
  "numeroLugar": "L-01",
  "unidadeOrganicaId": "uuid",
  "gradeId": "uuid | null",
  "functionId": "uuid | null",
  "dataInicio": "YYYY-MM-DD",
  "titularId": "uuid",             // quem está a ser substituído
  "titularNome": "string",
  "titularAssignmentId": "uuid"    // a afectação do titular; é o que faz a substituição caducar
}
```

**Não se envia data de fim.** A substituição caduca quando cessa o impedimento (art. 77.º n.º 2) e **fecha-se sozinha** quando o titular regressa: ao mudar para um estado cuja situação já não permite substituição, ao encerrar-se a licença dele, ou ao cessar. Não há endpoint para a terminar à mão.

| Código | Quando |
|---|---|
| `404` | Funcionário, Lugar, escalão ou função inexistentes. |
| `409` | O titular **já está a ser substituído** — um de cada vez. |
| `422` | Substituto inactivo · o Lugar **não tem titular** (está vago: nomeie titular) · o titular **não está impedido** (`ACTIVIDADE_NO_QUADRO`, ou já sem Lugar) · o estado do titular **não tem situação funcional** atribuída · o colaborador é o próprio titular · `dataInicio` anterior à afectação do titular · as regras de Lugar da afectação normal (ocupável, escalão, função). |

> **Só se substitui quem mantém o Lugar sem o exercer.** Isso lê-se da situação funcional do estado do titular — `ACTIVIDADE_FORA_QUADRO` (art. 119.º) ou `INACTIVIDADE_NO_QUADRO` (art. 120.º) — e não de uma lista de códigos. Se a instituição não classificou o estado, a substituição é recusada em vez de se adivinhar.

> **O substituto mantém o seu próprio Lugar**, se tiver: ao contrário dos movimentos de carreira, a substituição **não encerra** a afectação principal de quem substitui.


**Ler as substituições** — `GET /funcionarios/{id}/substituicoes`

Até aqui a substituição criava-se e **nada a mostrava**: o `POST` devolvia o id, e o `GET .../unidade-atual` só responde pela afectação `PRINCIPAL`. Um ecrã de RH não conseguia dizer quem substitui quem.

| Parâmetro | Efeito |
|---|---|
| `apenasCorrentes=true` | só as que estão em vigor |
| omitido (ou `false`) | o histórico completo |

Resposta `200` (`WrapperListaSubstituicoesDTO`): `linhas` + `total`. Cada linha traz **os dois papéis na mesma consulta** — a pergunta que um ecrã faz sobre uma pessoa é «em que substituições está metida», e isso inclui os dois lados:

| Campo | Significado |
|---|---|
| `papel` | `SUBSTITUTO` (está a substituir alguém) ou `TITULAR` (está a ser substituído) |
| `contraparteId` · `contraparteNome` · `contraparteNumero` | o outro lado |
| `positionId` · `numeroLugar` · `unidadeOrganicaId` · `unidadeOrganicaNome` | o Lugar coberto |
| `dataInicio` · `dataFim` · `corrente` | o período |

**`dataFim` vem nula enquanto durar.** A substituição não tem fim combinado: caduca quando o titular regressa (art. 77.º n.º 2), e é então que a data é preenchida e `corrente` passa a falso.

Uma substituição anterior à V46 não tem ligação ao titular: aparece na mesma, sem contraparte. O registo existiu, e escondê-lo seria pior do que mostrá-lo incompleto.


### 5.8 Antiguidade — `GET /funcionarios/{id}/antiguidade`

Tempo de serviço a uma data, **descontando o que a lei manda não contar**. Parâmetro opcional `ate=YYYY-MM-DD` (por omissão, hoje) — serve para responder a «quanta antiguidade tinha à data da promoção».

**Não se guarda: deriva-se.** É recalculada a cada leitura. Um número gravado ficaria velho na primeira vez que alguém corrigisse uma data do passado.

**O que desconta, e porquê:**

| Fonte | Regra | Onde está configurado |
|---|---|---|
| Situação funcional | art. 120.º n.º 2 — a inactividade **no** quadro não conta; a inactividade **fora** do quadro suspende o vínculo, logo também não. A **disponibilidade conta** (art. 122.º n.º 1) | `t_worker_state.situacao_funcional` → enum `SituacaoFuncional` |
| Licença sem vencimento | art. 47.º n.º 1 do DL 3/2010 — «implica o desconto na antiguidade para todos os efeitos legais» | `t_leave_mobility_subtype.counts_for_seniority` |
| Vínculo laboral | há vínculos cujo tempo não conta; aplica-se ao **período do contrato**, não à pessoa | `t_vinculo_laboral.counts_seniority` |

**O que não desconta, e é deliberado:**

- **Mobilidade** — art. 137.º da Lei 20/X/2023: o tempo conta no lugar de origem. Não é configurável; mesmo que o subtipo esteja mal classificado, não desconta.
- **Estado sem situação classificada** — é configuração em falta, não um estado que não conta. Descontar por omissão tiraria tempo a quem o tem.
- **Licença por decidir, indeferida ou cancelada** — não produziu ausência nenhuma.
- **Férias não gozadas** — contam (art. 12.º n.º 3).

> **Os períodos descontados unem-se, não se somam.** Uma licença sem vencimento de longa duração chega por **dois** caminhos — pela situação funcional em que põe o funcionário e pelo subtipo da própria licença — e somá-los descontaria o dobro dos dias. Sobrepostos, fundem-se; contíguos também, porque entre eles não houve um dia de serviço.

Resposta `200` (`AntiguidadeResponseDTO`):

| Campo | Significado |
|---|---|
| `dataInicio` · `dataReferencia` | de quando a quando se contou |
| `diasTotais` | dias de calendário, extremos incluídos |
| `diasDescontados` · `diasContados` | o que saiu e o que ficou |
| `anos` · `meses` · `dias` | o mesmo, como se lê uma antiguidade |
| `periodosDescontados[]` | `inicio`, `fim`, `dias`, `motivo` — **já unidos** |

O `motivo` de um período fundido junta os dois motivos: o resultado continua a dizer *porquê* depois da fusão.

| Situação | Resposta |
|---|---|
| Colaborador inexistente | **404** |
| Sem data de admissão (não há por onde começar) | **422** |

> **Faltas injustificadas:** não contam para antiguidade (art. 43.º n.º 2). Era uma lacuna, fechada pela V54: os tipos de regime `FALTA_INJUSTIFICADA` descontam sempre, e o período aparece em `periodosDescontados` com o artigo no motivo (§6.2c, BR-ANT-06, BR-AUS-16).

---

### 5.9 Mudança de carreira — `POST /funcionarios/{id}/mudanca-carreira`

O colaborador passa a ocupar um Lugar vago de **outra carreira**. Até existir este endpoint não havia caminho: a promoção (5.5) exige a **mesma carreira** e a transferência (5.6) exige a **mesma categoria**.

**Não é uma transferência**, embora a mecânica seja a mesma. A transferência mantém a posição na grelha e muda de cadeira; aqui muda o **próprio eixo** de que a categoria e o escalão dependem. Por isso a carreira de destino **tem de ser diferente** da actual — sem essa guarda, este caminho seria uma promoção sem nenhuma das regras da promoção.

```json
{
  "positionId": "uuid do Lugar vago da carreira de destino",
  "gradeId": "uuid | omitir (entra pelo primeiro escalão activo da categoria de destino)",
  "functionId": "uuid | omitir (mantém a função actual, se for compatível)",
  "dataEfeito": "YYYY-MM-DD",
  "despachoNumero": "string | null",
  "concursoRef": "string | null",
  "observacoes": "string | null"
}
```

**Resposta `201`** (`MudancaCarreiraResponseDTO`)**:**
```json
{
  "id": "uuid da nova afectação",
  "funcionarioId": "uuid",
  "positionAnteriorId": "uuid",
  "numeroLugarAnterior": "LUG-0002",
  "carreiraAnteriorId": "uuid",
  "carreiraAnterior": "Regime Geral",
  "categoriaAnteriorId": "uuid",
  "categoriaAnterior": "Assistente Técnico",
  "positionId": "uuid",
  "numeroLugar": "LUG-0007",
  "unidadeOrganicaId": "uuid",
  "carreiraNovaId": "uuid",
  "carreiraNova": "Regime Especial",
  "categoriaNovaId": "uuid",
  "categoriaNova": "Técnico de Regime Especial",
  "escalaoId": "uuid",
  "escalao": "Escalão 1",
  "functionId": "uuid | null",
  "dataEfeito": "YYYY-MM-DD"
}
```

| Código | Quando |
|---|---|
| `400` | `positionId` ou `dataEfeito` em falta; UUID inválido. |
| `404` | Funcionário, Lugar, carreira, categoria, escalão ou função não existem. |
| `422` | Colaborador inactivo · vínculo que não permite evoluir na carreira · sem afectação corrente · `dataEfeito` não posterior ao início da afectação · **destino da mesma carreira** (use a promoção ou a transferência) · Lugar igual ao actual, com titular, não ATIVO ou fora da grelha · carreira ou categoria de destino inactivas · `gradeId` que não é da categoria de destino · função incompatível com o cargo do destino. |

**Exige Lugar vago; não reclassifica.** Ao contrário da promoção, não há a forma «o Lugar sobe»: passar um Lugar de uma carreira para outra altera o quadro de pessoal, que é decisão de organograma e não movimento de uma pessoa.

**O escalão não se herda** (ao contrário da transferência): pertence à categoria do Lugar de destino. Quem posiciona é o **acto administrativo** — o critério legal (remuneração igual ou imediatamente superior) assenta em remuneração, que esta aplicação não tem.

> **As habilitações não se verificam.** A carreira não tem campo que diga o requisito habilitacional e as qualificações não têm nível normalizado. Como o concurso na promoção, `despachoNumero` e `concursoRef` guardam-se nas notas da afectação e **não são validados**.

Regras completas: `regras_negocio.html`, secção 3.4 (BR-MCA-01 a 11).

---

## 6. Dados do colaborador (sub-recursos de `/funcionarios/{funcionarioId}`)

Todos seguem o padrão CRUD + (quando aplicável) `documentos`:

| Recurso | Base |
|---|---|
| Contratos | `/funcionarios/{id}/contratos` — + `PUT /funcionarios/{id}/contratos/{contratoId}/close`, `PUT /funcionarios/{id}/contratos/{contratoId}/suspend`, `PUT /funcionarios/{id}/contratos/{contratoId}/activate` e `documentos`. **`close` = cessação do vínculo** (ver nota em 6.5) |
| Dados bancários | `/funcionarios/{id}/dados-bancarios` |
| Dependentes | `/funcionarios/{id}/dependentes` |
| Qualificações | `/funcionarios/{id}/qualificacoes` — + `documentos` |
| Formações | `/funcionarios/{id}/formacoes` — + `documentos` |
| Documentos | `/funcionarios/{id}/documentos` — upload/list/download/delete |
| Recibos | `/funcionarios/{id}/recibos` — + `documentos` |
| Processos disciplinares | `/funcionarios/{id}/processos-disciplinares` — + `documentos` |
| Pedidos de ausência | `/funcionarios/{id}/pedidos-ausencia` — + `aprovar`/`rejeitar`/`cancelar` (**todos `PATCH`**, ver 6.1) |
| Saldos de ausência | `/funcionarios/{id}/saldos-ausencia` |
| Licenças/mobilidade | `/funcionarios/{id}/licencas-mobilidade` (ver 7) |
| Substituições | `/funcionarios/{id}/substituicoes` — leitura, nos dois papéis (ver 5.7) |
| Mapa de férias | `/funcionarios/{id}/ferias/{ano}` — + `preferencia`, `marcacao` (ver 6.6) |
| Horários | `/funcionarios/{id}/horarios` — + `vigente` (ver 6.7) |
| Marcações e assiduidade | `/funcionarios/{id}/marcacoes` — + `anular`, `validar`, `rejeitar`; `/funcionarios/{id}/assiduidade` (ver 6.8) |
| Faltas apuradas | `/funcionarios/{id}/faltas-apuradas` (ver 6.9) |
| Trabalho suplementar | `/funcionarios/{id}/trabalho-suplementar` — + `autorizar`, `recusar`, `cancelar` (ver 6.10) |


### 6.1 Ausências ou licença? — qual dos dois usar

Há dois recursos para uma pessoa se ausentar, e a escolha **não é de gosto**: segue a divisão do Decreto-Lei n.º 3/2010.

| Usar | Quando | Recurso |
|---|---|---|
| **Ausências** | Férias (cap. II) e **faltas** — ausência de um dia ou parte dele (art. 13.º) | `/funcionarios/{id}/pedidos-ausencia` + `/saldos-ausencia` |
| **Licenças** | **Ausência prolongada, mediante autorização** (art. 44.º); as sete modalidades do art. 45.º | `/funcionarios/{id}/licencas-mobilidade` com subtipo de `recordType=LICENCA` |
| **Mobilidade** | A pessoa **não se ausenta** — vai exercer funções noutro sítio (Lei 20/X/2023, art. 132.º a 135.º) | o mesmo recurso, com subtipo de `recordType=MOBILIDADE` |

O eixo é **curto contra prolongado**: a ausência conta-se em dias e desconta de um saldo anual; a licença tem início e fim, é autorizada caso a caso e **pode tirar o Lugar** ao funcionário.

> **E a assiduidade?** Existe, ao lado: horários (§6.7), marcações (§6.8), faltas por débito (§6.9), trabalho
> suplementar (§6.10) e a relação mensal (§6.11). O pedido de ausência é a parte **pedida e decidida**; a assiduidade
> é a parte **medida**. As duas encontram-se no apuramento: um pedido aprovado tira o dia (ou as horas) das faltas.
> O pedido aceita horas desde a V58 (§6.2e); os **meios-dias de férias** (art. 2.º n.º 6) ficaram adiados.

### 6.2 Saldo de ausências — quando é que os dias saem

O saldo tem três números: `diasDireito`, `diasPendentes` (reservados) e `diasGozados`. O percurso é:

| Momento | Efeito no saldo |
|---|---|
| **Submeter** o pedido | **reserva** os dias; sem saldo suficiente, **422** já aqui |
| **Aprovar** | os reservados passam a **gozados** |
| **Rejeitar**, ou cancelar enquanto `PENDENTE` | **liberta** a reserva |
| **Cancelar** depois de aprovado | **devolve** os dias gozados |

Isto mudou: a reserva era feita só na aprovação, e `diasGozados` ficava sempre a zero. Se o teu front-end mostrava os dias gozados, passa agora a ter valores reais.

### 6.2b Limites de dias: por ano, por ocorrência e por mês

O tipo de ausência trazia um só tecto, `maxDaysPerYear`, e o pedido somava sempre o **ano civil**. O art. 15.º n.º 1 do DL n.º 3/2010, porém, quase nunca fala em anos — e desde a **V53** há três campos, todos opcionais:

| Campo | O que limita | Onde a lei o diz |
|---|---|---|
| `maxDaysPerYear` | o total do **ano civil** | al. j) «até 15 por ano» · al. q) «6 dias em cada ano civil» |
| `maxDaysPerOccurrence` | os dias de **cada pedido** | al. a) casamento · al. b) e c) falecimento · al. d) e e) doença · al. f) provas · al. h) nascimento |
| `maxDaysPerMonth` | o total do **mês civil** | al. o) «um por mês por conta das férias» · al. q) «e um dia por mês» |

**Nulo quer dizer «sem limite desta natureza»** — não zero. É o caso da greve, das obrigações legais e da prisão preventiva, que não têm quota.

**O tecto por ocorrência não se acumula.** Olha só para o pedido que tem à frente: quem perde dois familiares no mesmo ano tem direito às duas ausências. Era esta a falha que a V53 fecha — com o valor escrito no tecto anual, o segundo funeral do ano era recusado *e* oito dias seguidos de uma só vez passavam.

**Podem valer ao mesmo tempo.** A al. q) tem tecto anual **e** mensal, e é por isso que são três campos independentes e não um campo com uma classificação ao lado. Um pedido pode caber no ano e não caber no mês — a recusa diz qual dos dois falhou.

Os três são recusados com **422** na criação do pedido, e a mensagem distingue-os («no máximo N dias de cada vez» · «limite mensal» · «limite anual»). No catálogo (`POST`/`PUT /catalogs/leave-types`) um valor **zero ou negativo** dá **400**: um limite de zero dias não é um limite, é um tipo que ninguém pode pedir, e isso diz-se desactivando a linha.

O seed passou a trazer as alíneas do art. 15.º já classificadas — `CASAMENTO`, `LUTO` (8 dias, cônjuge ou 1.º grau) e `LUTO_OUTRO_GRAU` (3), `NASCIMENTO_FILHO`, `PROVA_EXAME`, `ASSISTENCIA_FAMILIA`, `AUTORIZADA_DIRIGENTE`, `CONTA_FERIAS`, `GREVE`, `OBRIGACAO_LEGAL`, `DOENCA` e `DOENCA_ATESTADO`. **Numa instalação já existente o seed não os corrige** (`ON CONFLICT DO NOTHING`): a classificação das linhas antigas é da instituição, e faz-se pela API.

### 6.2c Faltas injustificadas, e o que a ausência faz à remuneração

Duas coisas que vivem na mesma frase da lei. O art. 43.º n.º 2: *«As faltas injustificadas, para além das consequências disciplinares a que possam dar lugar, **não contam para efeitos de antiguidade** e implicam a **opção** entre a perda das remunerações correspondentes aos dias de ausência, ou o seu desconto nas férias.»*

**O regime ganhou um terceiro valor.** `regime` passa a ser `FERIAS` · `FALTA` · `FALTA_INJUSTIFICADA`, seguindo as secções do próprio diploma. A instituição diz **quais** das suas linhas são injustificadas; o que daí decorre é da lei e **não se configura**:

- **descontam antiguidade**, sempre. Não há booleano para o desligar — um booleano deixaria configurar o contrário da lei. `GET /funcionarios/{id}/antiguidade` passou a contá-las, e o período aparece em `periodosDescontados` com o artigo no motivo;
- **exigem a opção** do n.º 2 no pedido.

**A opção é de cada caso, não do catálogo.** `POST /funcionarios/{id}/pedidos-ausencia` aceita `opcaoFaltaInjustificada` ∈ `PERDA_REMUNERACAO` · `DESCONTO_FERIAS`. É **obrigatória** quando o tipo é injustificado (**422** se faltar) e **recusada** quando não é (**422**): a lei só dá a opção a quem falta sem justificação. Duas faltas da mesma pessoa podem ser resolvidas de maneiras diferentes — é por isso que vive no pedido.

**Pelo self-service não se registam faltas injustificadas.** Ninguém classifica uma falta sua como injustificada, e a opção é um acto do serviço.

#### O efeito na remuneração — informação, não cálculo

**Esta aplicação não calcula remuneração e não vai passar a calcular.** O que passa a fazer é guardar a classificação do art. 16.º, para o sistema que processa vencimentos a poder ler junto com os dias:

| `efeitoRemuneracao` | O que a lei diz | Onde |
|---|---|---|
| `SEM_PERDA` | as justificadas não determinam perda de remuneração | n.º 1 |
| `PERDA_PARCIAL` | als. d), e), i), j) e t) — com direito a subsídio da previdência | n.º 2 e 3 |
| `PERDA_TOTAL` | greve: perde remuneração e **não** desconta antiguidade | n.º 4 |
| `PERDA_VENCIMENTO_EXERCICIO` | prisão preventiva; **reparável** em caso de absolvição | n.º 5 e 6 |
| `DEPENDE_DA_OPCAO` | falta injustificada: a lei não fixa, dá a opção do art. 43.º n.º 2 | — |

O n.º 3 define o valor da perda parcial como «a diferença entre a remuneração líquida a que o funcionário teria direito e o subsídio pago pela previdência social». Essa conta é de quem processa vencimentos: aqui só se diz **qual é o regime**.

Um tipo novo nasce `SEM_PERDA` — afirmar que não há perda nunca tira dinheiro a ninguém. Valor fora da lista dá **422**.

> **Nota para quem integra:** nas **licenças** esta informação já existia, mas como booleano (`t_leave_mobility_subtype.affects_pay`), que não sabe dizer «parcial». Os dois hão-de convergir; para já, ler o booleano nas licenças e o enum nas ausências.

**Numa instalação já existente nada disto vem classificado.** A migração **não** classifica linha nenhuma — estas duas colunas mandam descontar antiguidade e mexer em salários, e decidir em silêncio pela instituição seria o pior sítio para o fazer. As linhas antigas ficam em `SEM_PERDA` e no regime que já tinham, e a classificação faz-se pela API.

### 6.2d Dias úteis ou dias seguidos — art. 76.º

O DL n.º 3/2010 manda contar os fins-de-semana e feriados **intercalados** numa sucessão de faltas,
«salvo se a lei se referir expressamente a dias úteis» (art. 76.º). A regra é **dias seguidos**; dias
úteis é a excepção, e tem de estar escrita.

Cada tipo de ausência diz como se contam os seus dias — `contagem` em
`/api/v1/rh/catalogs/leave-types` e no `tipoAusencia` que vem com os pedidos e os saldos:

| `contagem` | Como conta | Quem, no seed |
|---|---|---|
| `DIAS_UTEIS` | Seg-Sex, sem os feriados do colaborador | férias, falta por conta das férias, paternidade, trabalhador-estudante (licença e pesquisas) |
| `DIAS_SEGUIDOS` | dias de calendário entre o **primeiro e o último dia útil** do período | todas as outras faltas do art. 15.º, maternidade, seminários |

Em dias seguidos, os fins-de-semana e feriados **das pontas** não contam — não estão «no decurso» da
falta:

| Pedido (Setembro de 2026) | `DIAS_UTEIS` | `DIAS_SEGUIDOS` |
|---|---|---|
| sexta 18 a segunda 21 | 2 | 4 |
| sexta 18 a domingo 20 | 1 | 1 |
| quinta 17 a quarta 23 | 5 | 7 |
| sábado 19 a domingo 20 | 422 | 422 |

O `numeroDias` do pedido e os três tectos (ano, ocorrência, mês) usam a contagem do tipo.

**Omissão:** um tipo novo sem `contagem` conta em `DIAS_UTEIS`; o `PUT` que a omita mantém a que
está. Valor fora dos dois dá **422**. Numa instalação existente, a migração deixou todas as linhas em
`DIAS_UTEIS` — como sempre se contou — e a classificação faz-se pela API.

**Dispensas em dias** (seed): `SEMINARIO` (al. w) e art. 21.º: máx. 5 dias consecutivos por
ocorrência), `TE_PESQUISA` (art. 77.º n.º 3: 6 dias úteis, por ano civil até o jurídico dizer outra
coisa) e `TE_LICENCA` (art. 77.º n.º 2: 10 dias úteis por ano civil, com desconto no vencimento). A
antecedência do art. 77.º n.º 2 e o estatuto de trabalhador-estudante **não** se validam ainda.

Regras: BR-AUS-20 a BR-AUS-23.

### 6.2e Pedido de ausência em horas — e a dispensa de amamentação

O pedido de ausência aceita, **opcionalmente**, `horaInicio` e `horaFim` (`HH:mm`). Com elas, é um
pedido em horas, e as horas valem **em cada dia do intervalo** `dataInicio`–`dataFim`. Sem elas, tudo
como sempre — dias inteiros.

```json
POST /api/v1/rh/funcionarios/{id}/pedidos-ausencia
{ "tipoAusenciaId": "…DISPENSA_AMAMENTACAO…", "dataInicio": "2027-03-01", "dataFim": "2027-08-28",
  "horaInicio": "08:00", "horaFim": "09:00", "motivo": "amamentação — período da manhã" }
→ 201 { "id": "…", "numeroDias": 0, "estado": "PENDENTE", "minutosPorDia": 60 }
```

A amamentação são duas horas por dia, em dois períodos (Lei n.º 20/X/2023, art. 172.º n.º 3): dois
pedidos de 1 hora. É o mesmo mecanismo do tratamento ambulatório, que costuma ser de um dia só.

| Tipo (seed) | Base legal | Tectos |
|---|---|---|
| `DISPENSA_AMAMENTACAO` | Lei 20, art. 172.º n.º 3 (prevalece sobre os 45 min do DL, art. 20.º) | `maxMinutosPorDia` 120; 183 dias por ocorrência (os 6 meses do art. 20.º — a confirmar) |
| `TRATAMENTO_AMBULATORIO` | DL 3/2010, art. 37.º | — |
| `CONSULTA_PRE_NATAL` | art. 15.º al. u) | — |
| `DOACAO_SANGUE` | art. 15.º al. k) | — |
| `CREDITO_SINDICAL` | art. 15.º al. r) | — |

| Caso | Resposta |
|---|---|
| só uma das horas, formato errado, início depois do fim | 422 |
| tipo que desconta saldo, de férias ou de falta injustificada, ou com tecto anual/mensal em dias | 422 |
| intervalo com mais dias do que o tecto por ocorrência | 422 |
| minutos do dia (somando os pedidos do tipo nessas datas) acima de `maxMinutosPorDia` | 422 |
| horas que se cruzam com outro pedido, ou um pedido de dias inteiros nessas datas | 409 |

A resposta do pedido traz `horaInicio`, `horaFim` e `minutosPorDia` (zero num de dias inteiros).

**Terminar antes do fim** (a amamentação que acaba mais cedo):

```json
PATCH /api/v1/rh/funcionarios/{id}/pedidos-ausencia/{pedidoId}/terminar
{ "data": "2027-06-01", "motivo": "deixou de amamentar" }
```

A decisão fica; o período acaba na véspera (`suspensoEm` = a data). Só pedidos em horas aprovados,
uma vez, com data depois do início e até ao fim. As férias continuam a suspender-se (§6.5).

**No catálogo** (`/catalogs/leave-types`), `maxMinutosPorDia` é o tecto diário dos pedidos em horas;
omisso no `PUT` mantém, `0` limpa.

**No apuramento de faltas** (§6.9), as horas de um pedido aprovado contam como cumpridas
(`minutosJustificados`).

Regras: BR-AUS-24 a BR-AUS-28, BR-FAL-07.

### 6.2f Quem aprova um pedido de ausência

- **Tipos que não requerem aprovação** (`requiresApproval: false` no catálogo — luto, casamento,
  doença, nascimento, greve…): o pedido **nasce `APROVADO`**, sem decisor, e a resposta traz
  `aprovacaoAutomatica: true`. Aprová-lo outra vez dá 409. Se a prova faltar, o RH cancela ou regista
  falta injustificada.
- **Os outros** nascem `PENDENTE` e decide, num só nível, **a chefia directa ou o RH**:

| Quem | Método | Path |
|---|---|---|
| Chefia | `GET` | `/api/v1/rh/me/equipa/pedidos-ausencia-pendentes` — a caixa da equipa directa |
| Chefia | `PATCH` | `/api/v1/rh/me/equipa/pedidos-ausencia/{id}/aprovar` `{ "motivo"? }` · `…/rejeitar` `{ "motivo" }` |
| RH | `PATCH` | `/api/v1/rh/funcionarios/{id}/pedidos-ausencia/{pedidoId}/aprovar` · `…/rejeitar` (como antes) |

A chefia directa é o titular do Lugar-pai do Lugar de quem pediu. 403 se não for; 422 nos próprios
pedidos ou ao rejeitar sem motivo (pela chefia); 409 se já não estiver `PENDENTE`. Pelo RH, o pedido
tem de ser do colaborador do caminho (404). Sem chefia definida, ou com a chefia vaga, decide o RH.

Regras: BR-AUS-29, BR-AUS-30, BR-ME-05.

### 6.3 Férias: o saldo nasce sozinho

**`POST /saldos-ausencia` deixou de ser o caminho para as férias.** O art. 2.º n.º 4 do DL n.º 3/2010 diz que «o direito a férias vence no dia 1 de Janeiro de cada ano» — e passou a ser o que acontece:

| Momento | O que acontece |
|---|---|
| **Admissão** (`POST /funcionarios`) | o saldo de férias do ano de ingresso é criado logo, com o direito proporcional — muitas vezes **zero**, o que é a resposta certa |
| **Todos os dias**, pelo job `RH_VENCIMENTO_FERIAS` (00:05; ver 11b) | o saldo do ano corrente é criado a quem não o tenha e **actualizado** a quem o direito tenha crescido |
| **1 de Janeiro** | a mesma passagem cria o saldo do ano novo, com o direito inteiro |

**Quantos dias.** Vem do `maxDaysPerYear` do tipo de ausência classificado como férias; na falta dele valem os **22 dias úteis** do art. 2.º n.º 3. A instituição pode ter outro número por diploma próprio.

**No ano de ingresso é proporcional** (art. 3.º): a partir dos **90 dias** de serviço efectivo, por cada **3 meses completos** até 31 de Dezembro. Com 22 dias anuais dá:

| Admitido em | Trimestres completos | Dias |
|---|---|---|
| Janeiro | 4 | 22 |
| Abril | 3 | 17 |
| Julho | 2 | 11 |
| Outubro | 1 | 6 |
| Novembro ou Dezembro | menos de 90 dias de serviço | **0** |

**O direito nunca encolhe abaixo do que já foi gozado ou reservado**, mesmo que o recálculo dê menos — o art. 2.º n.º 5 diz que é irrenunciável.

**Qual das linhas do catálogo são férias** lê-se do campo `regime` do tipo de ausência (`FERIAS` ou `FALTA`), e nunca do código. Ver 11.x no guia de catálogos: a instituição reclassifica pela API, e um valor fora da lista da lei dá **422**.

### 6.4 Acumulação de férias para o ano seguinte

**`POST /funcionarios/{id}/saldos-ausencia/{saldoId}/acumular`**

```json
{ "dias": 8, "motivo": "conveniência de serviço — projecto em curso" }
```

O art. 7.º n.º 1 diz que as férias «devem ser gozadas no decurso do ano civil em que se vencem, salvo se, **por motivo de serviço**, não puderem ser gozadas nesse ano, caso em que pode haver acumulação de férias para o ano seguinte».

**Não é automático, e não é o job que o faz.** É um acto do RH e o `motivo` é **obrigatório** (400 sem ele) — a lei condiciona a acumulação a haver motivo de serviço. O conteúdo não é validado: os motivos são da instituição.

Resposta `200` (`AcumulacaoFeriasResponseDTO`) mostra os dois lados, que é o que quem autoriza precisa de ver:

| Campo | Significado |
|---|---|
| `saldoDestinoId` · `anoOrigem` · `anoDestino` | de onde e para onde |
| `diasAcumulados` | dias movidos nesta operação |
| `disponivelNaOrigem` | o que resta por gozar no ano de origem |
| `disponivelNoDestino` | o que passa a haver no ano de destino |

**O que impede erros:**

| Situação | Resposta |
|---|---|
| Sem `motivo` | **400** |
| Mais dias do que os que sobram no ano | **422** |
| Ceder outra vez dias já cedidos | **422** |
| Acumular um tipo que não seja férias (`regime != FERIAS`) | **422** |
| Saldo de outro colaborador pelo URL deste | **404** |

**O saldo do ano de destino é criado se ainda não existir** — a autorização pode acontecer em Dezembro, antes de o job do vencimento passar.

**Os dias acumulados ficam à parte do direito do próprio ano.** No `SaldoAusenciaResponseDTO`:

| Campo | Significado |
|---|---|
| `diasDireito` | o que se venceu **neste** ano |
| `diasAcumulados` | o que veio do ano anterior · `acumulacaoMotivo` diz porquê |
| `diasTransportados` | o que já foi cedido ao ano seguinte |
| `diasDisponiveis` | `diasDireito + diasAcumulados − gozados − pendentes − transportados` |
| `diasAcumuláveis` | quanto deste ano ainda pode seguir para o seguinte |

**Os dias recebidos não voltam a ser acumuláveis.** O horizonte da lei é de um ano: o art. 7.º n.º 1 fala do «ano seguinte» e o art. 8.º n.º 4 manda gozar o remanescente «até ao termo do ano civil imediato». Por isso `diasAcumuláveis` conta só o que sobra do direito do próprio ano — quem gastou tudo o que se venceu neste ano e ainda tem saldo, tem-no à custa dos dias antigos, e esses ficam.

### 6.5 Suspensão de férias

**`PATCH /funcionarios/{id}/pedidos-ausencia/{pedidoId}/suspender`**

```json
{ "data": "YYYY-MM-DD", "motivo": "doença — atestado entregue no serviço" }
```

O art. 8.º manda suspender as férias por maternidade, paternidade ou adopção (n.º 1), por doença e assistência inadiável a familiares doentes (n.º 2), e por razões imperiosas de serviço mediante despacho fundamentado (n.º 5). O n.º 3 diz **a partir de quando**: «a partir da data da entrada no serviço do documento comprovativo».

Antes disto, um pedido de férias e um de doença não se falavam: quem adoecesse a meio das férias perdia-as, porque os dias tinham sido contados como gozados na aprovação.

**O último dia de férias é a véspera da data indicada** — «a partir de» inclui o próprio dia. É a mesma leitura do regresso antecipado da licença (7.0).

**O estado não muda.** O pedido continua `APROVADO`: a decisão foi tomada e não se desfaz; o que encurta é o período. No `PedidoAusenciaResponseDTO` a interrupção lê-se em `suspensoEm` e `suspensaoMotivo`.

Resposta `200` (`SuspensaoFeriasResponseDTO`):

| Campo | Significado |
|---|---|
| `suspensoEm` · `dataFim` | a data da suspensão e o novo último dia de férias |
| `diasGozados` | dias úteis efectivamente gozados até à interrupção |
| `diasRecuperados` | dias úteis que voltaram ao saldo |
| `saldoDisponivel` | o que passa a haver no saldo do ano |

**O que impede erros:**

| Situação | Resposta |
|---|---|
| Sem `motivo` | **400** |
| `data` futura | **400** |
| Pedido que não está `APROVADO` | **409** |
| Suspender no próprio dia de início (não há período gozado a interromper) | **409** |
| Férias que já terminaram, ou já suspensas | **409** |
| Tipo que não seja férias (`regime != FERIAS`) | **422** |
| Pedido de outro colaborador pelo URL deste | **404** |

**Para onde vão os dias recuperados:** voltam ao saldo do **próprio ano**. Passá-los ao ano seguinte é a acumulação (6.4) — e é isso que o art. 9.º n.º 1, remetendo para o art. 8.º n.º 4, autoriza ao mandar gozá-los «até ao termo do ano civil imediato».

> **O que ainda não existe:** a compensação na cessação (art. 12.º) e a compensação proporcional pela suspensão por razões de serviço (art. 8.º n.º 7), ambas dependentes de remuneração — e remuneração não existe nesta aplicação.

As transições do pedido são todas `PATCH`, e não `PUT` — ao contrário das do contrato e das da licença, que são `PUT`:

| Verbo | Path | Quem |
|---|---|---|
| `POST` | `/funcionarios/{id}/pedidos-ausencia` | submeter (reserva os dias) |
| `PATCH` | `/funcionarios/{id}/pedidos-ausencia/{pedidoId}/aprovar` | RH — corpo `{ "aprovadoPorId", "observacoesDecisao"? }` |
| `PATCH` | `/funcionarios/{id}/pedidos-ausencia/{pedidoId}/rejeitar` | RH — o mesmo corpo |
| `PATCH` | `/funcionarios/{id}/pedidos-ausencia/{pedidoId}/suspender` | RH — só férias (6.5) |
| `PATCH` | `/funcionarios/{id}/pedidos-ausencia/{pedidoId}/terminar` | RH — só pedidos em horas (6.2e) |
| `PATCH` | `/me/equipa/pedidos-ausencia/{id}/aprovar` · `…/rejeitar` | a chefia directa (6.2f) |
| `PATCH` | `/funcionarios/{id}/pedidos-ausencia/{pedidoId}/cancelar` | RH (qualquer colaborador) ou o próprio via self-service |

**Quem cancela:** `PATCH /funcionarios/{id}/pedidos-ausencia/{pedidoId}/cancelar` é o caminho do RH e serve para cancelar o pedido de qualquer colaborador — antes devolvia **403** a quem não fosse o próprio. O colaborador usa o self-service, que só o deixa cancelar o que é seu e enquanto estiver `PENDENTE`.

Só os tipos com `deductsBalance` mexem no saldo; para os outros, nada disto se aplica.

> **`PUT /funcionarios/{id}/contratos/{contratoId}/close` cessa o vínculo.** Cessa o contrato, encerra a afectação corrente (o Lugar fica vago), muda o estado do trabalhador para o estado de cessação por omissão e regista o histórico — os mesmos efeitos de `PATCH /funcionarios/{id}/worker-state` com um estado de cessação. Devolve `EstadoColaboradorResponseDTO`.
>
> Para **renovar ou substituir** um contrato não se usa o `close`: basta criar o contrato novo, que encerra o anterior (motivo `SUBSTITUICAO`) sem tocar na afectação nem no estado.

### 6.6 Mapa de férias — arts. 5.º e 6.º

**Marcar não é gozar.** O mapa é o plano do ano; o gozo continua a ser o pedido de férias (§6.3),
que desconta o saldo. Nada no mapa mexe em saldos.

| Método | Caminho | O quê |
|---|---|---|
| `GET` | `/api/v1/rh/funcionarios/{id}/ferias/{ano}` | preferência, marcação, alterações e direito do ano |
| `PUT` | `/api/v1/rh/funcionarios/{id}/ferias/{ano}/preferencia` | indicar a preferência (art. 5.º n.º 4) |
| `PUT` | `/api/v1/rh/funcionarios/{id}/ferias/{ano}/marcacao` | marcar, ou alterar depois de publicado (art. 5.º, art. 6.º n.º 2) |
| `GET` | `/api/v1/rh/ferias/mapa/{ano}` | o mapa: marcações e quem está sem nenhuma |
| `POST` | `/api/v1/rh/ferias/mapa/{ano}/publicar` | dar conhecimento do mapa (art. 6.º n.º 1) — **não há aprovação** |

Os dois `PUT` substituem o conjunto de períodos que havia.

**Preferência** — `{"periodos": [{"dataInicio": "2027-08-02", "dataFim": "2027-08-31"}], "observacoes": null}`.
Até 31 de Janeiro; **depois é aceite com alerta** e fica `preferenciaForaDePrazo: true`.

**Marcação** — os dias úteis contam-se no servidor, com os feriados do colaborador:

```json
{
  "origem": "FIXADA",
  "periodos": [{"dataInicio": "2027-06-01", "dataFim": "2027-06-15"},
               {"dataInicio": "2027-09-01", "dataFim": "2027-09-15"}],
  "fundamentacao": "Época alta de atendimento em Agosto",
  "motivoAlteracao": null
}
```

| Caso | Resposta |
|---|---|
| `origem` ausente ou fora de `ACORDO` · `FIXADA` | 422 |
| períodos fora do ano, sobrepostos, ou sem dias úteis | 422 |
| total acima do direito do ano | 422 |
| um período com mais dias úteis do que o direito anual (art. 5.º n.º 1) | 422 |
| interpolado sem nenhum período de 11 dias (salvo ano de ingresso, ou direito < 11) | 422 |
| `FIXADA` fora de 1 de Maio a 31 de Outubro (art. 5.º n.º 5) | 422 |
| `FIXADA` interpolada sem `fundamentacao` (art. 5.º n.º 2) | 422 |
| mapa já publicado e marcação existente, sem `motivoAlteracao` (art. 6.º n.º 2) | 422 |
| `motivoAlteracao: CONVENIENCIA_SERVICO` sem `fundamentacao` | 422 |
| total abaixo do direito | 200, com alerta |

**Publicar** devolve 201 com alertas (fora do prazo de 31 de Março; colaboradores sem marcação);
uma segunda vez é **409**.

**Parâmetros** — os prazos, a janela de fixação e o período mínimo interpolado vêm do catálogo
`/api/v1/rh/catalogs/parametros-ferias`, por vigência, com a lei por omissão (§9.2). As datas e
o mínimo que se aplicam a um ano são os da vigência desse ano.

**Não coberto:** a preferência dos cônjuges no mesmo serviço (art. 5.º n.º 6) — não há ligação
entre colaboradores. (A preferência pelo próprio e o aviso de pedido fora da marcação existem: abaixo.)

**Pelo próprio e pela chefia** (`/me`):

| Método | Path | O quê |
|---|---|---|
| `GET` | `/api/v1/rh/me/ferias/{ano}` | as minhas férias: preferência, marcação, alterações |
| `PUT` | `/api/v1/rh/me/ferias/{ano}/preferencia` | indicar a preferência (o mesmo corpo do RH); inactivo → 403 |
| `GET` | `/api/v1/rh/me/equipa/ferias/{ano}` | a chefia directa vê a preferência e a marcação da equipa |

A resposta das férias traz `preferenciaIndicadaPor` (`PROPRIO` · `RH`).

**Pedido de férias fora da marcação** (art. 6.º n.º 2): a criação do pedido (RH ou `/me`) **não
recusa**, mas a resposta traz um alerta em `alertas` — depois de o mapa ser dado a conhecer, é uma
alteração ao mapa, que se regista na marcação. Na caixa da chefia (§6.2f), `foraDaMarcacao: true`.

Regras: BR-FER-13 a BR-FER-22.

### 6.7 Horário do colaborador — assiduidade, primeiro passo

O horário de trabalho de cada colaborador, com o regime de prestação (Lei n.º 20/X/2023, arts.
164.º a 166.º). Os horários vêm do catálogo da instituição (§9.3).

| Método | Path | O quê |
|---|---|---|
| `GET` | `/api/v1/rh/funcionarios/{id}/horarios` | histórico das atribuições, da mais antiga para a mais recente |
| `POST` | `/api/v1/rh/funcionarios/{id}/horarios` | atribui a partir de uma data → 201; fecha a anterior na véspera |
| `GET` | `/api/v1/rh/funcionarios/{id}/horarios/vigente?data=2026-10-05` | o horário que vale nessa data (sem `data`: hoje) |

```json
POST /api/v1/rh/funcionarios/{id}/horarios
{ "horarioId": "…", "regimePrestacao": "TELETRABALHO", "dataInicio": "2026-10-01" }
```

`regimePrestacao` ∈ `PRESENCIAL` · `TELETRABALHO` · `MISTO` (art. 166.º); omisso = `PRESENCIAL`.

**O horário que vale numa data**, por esta ordem:

1. o **atribuído** ao colaborador para essa data (`origem: COLABORADOR`);
2. o da **unidade** onde exerce funções nessa data — na mobilidade interna, a de destino —, ou o da
   unidade-mãe mais próxima que tenha um (`UNIDADE`, regime `PRESENCIAL`);
3. o horário **base** da instituição (`BASE`, regime `PRESENCIAL`);
4. nenhum, só enquanto a instituição não tiver marcado o base (`NENHUM`, `horario` nulo).

```json
GET /api/v1/rh/funcionarios/{id}/horarios/vigente?data=2026-10-05
{
  "funcionarioId": "…", "data": "2026-10-05",
  "origem": "COLABORADOR", "regimePrestacao": "TELETRABALHO", "atribuicaoId": "…",
  "horario": { "id": "…", "nome": "Flexível", "controlo": "FLEXIVEL", "periodoAfericao": "MES",
               "duracaoDiaria": "07:00", "horasSemanais": "35:00", "blocos": [ … ], "isBase": false }
}
```

| Caso | Resposta |
|---|---|
| sem `dataInicio`, ou numa data igual ou anterior à da última atribuição | 422 |
| `horarioId` em falta, mal escrito, inexistente ou de um horário inactivo | 422 |
| `regimePrestacao` fora dos três valores | 422 |
| contrato a tempo parcial e um horário com tantas ou mais horas do que o da unidade (ou o base) | 201, com alerta |

Regras: BR-HOR-06 a BR-HOR-10.

### 6.8 Registo diário de assiduidade — marcações

O registo do art. 164.º n.º 3 da Lei n.º 20/X/2023. Guardam-se as **marcações** (entrada ou saída,
com a hora) — a prova — e o dia **calcula-se** delas: períodos, intervalos, horas e anomalias.
Uma marcação **nunca se apaga**: corrige-se com outra, e a errada anula-se com motivo.

| Método | Path | O quê |
|---|---|---|
| `POST` | `/api/v1/rh/funcionarios/{id}/marcacoes` | lançamento pelo RH → 201 (com alertas) |
| `PATCH` | `/api/v1/rh/funcionarios/{id}/marcacoes/{marcacaoId}/anular` | anula, com motivo → 200 |
| `POST` | `/api/v1/rh/assiduidade/importacao` | picagens de um relógio → 200 com relatório |
| `GET` | `/api/v1/rh/funcionarios/{id}/assiduidade?de=2026-09-01&ate=2026-09-30` | o período, por dia e por semana |

```json
POST /api/v1/rh/funcionarios/{id}/marcacoes
{ "momento": "2026-09-22T17:00", "sentido": "SAIDA", "motivo": "esqueceu-se de picar a saída" }
```

O `motivo` é **obrigatório quando o dia já tem marcações** — lançar outra é uma correcção.

```json
POST /api/v1/rh/assiduidade/importacao
{ "picagens": [
    { "numeroFuncionario": "0000002", "momento": "2026-09-22T08:02", "sentido": "ENTRADA", "referenciaExterna": "REL1-000123" }
] }
→ { "importadas": 1, "duplicadas": 0, "rejeitadas": [] }
```

A importação é **genérica**: o adaptador de cada marca de relógio converte para este formato. A
`referenciaExterna` (o id da picagem no relógio) é obrigatória e torna-a **repetível** — a mesma
referência conta como duplicada. Uma picagem má não trava as outras: vai para `rejeitadas`, com o
motivo. No máximo 5000 por lote.

```json
GET /api/v1/rh/funcionarios/{id}/assiduidade?de=2026-09-21&ate=2026-09-27
{ "dias": [ {
    "data": "2026-09-21",
    "periodos": [ { "entrada": "08:00", "saida": "12:30", "minutos": 270 }, { "entrada": "14:00", "saida": "17:30", "minutos": 210 } ],
    "intervalosMinutos": [ 90 ], "minutosTrabalhados": 480, "minutosEsperados": 480,
    "horarioNome": "Horário normal", "feriado": false, "anomalias": [],
    "marcacoes": [ { "id": "…", "momento": "2026-09-21T08:00", "sentido": "ENTRADA", "origem": "IMPORTADO", "anulada": false } ]
  } ],
  "semanas": [ { "ano": 2026, "semana": 39, "inicio": "2026-09-21", "minutosTrabalhados": 480, "minutosEsperados": 2400 } ] }
```

- `anomalias` ∈ `ENTRADA_SEM_SAIDA` · `SAIDA_SEM_ENTRADA` · `ENTRADAS_SEGUIDAS`: o dia tem de ser
  corrigido; os minutos só contam nos pares completos.
- `minutosEsperados` vem do horário vigente nesse dia (§6.7); zero em feriado ou sem horário.
- `marcacoes` traz todas, anuladas incluídas (com `motivoAnulacao`).

| Caso | Resposta |
|---|---|
| marcação no futuro, sem `momento`, `sentido` fora de ENTRADA/SAIDA | 422 |
| dia com marcações e sem `motivo` | 422 |
| anular sem motivo · anular outra vez · marcação de outra pessoa | 422 · 409 · 404 |
| consulta sem `de`/`ate`, ao contrário ou com mais de 62 dias | 422 |
| ausência aprovada, feriado ou fim-de-semana | 201, com alerta |

Regras: BR-ASS-01 a BR-ASS-07.

**Registo pelo próprio e validação da chefia** (`/me`):

| Método | Path | O quê |
|---|---|---|
| `POST` | `/api/v1/rh/me/marcacoes` `{ "sentido": "ENTRADA" }` | picagem em tempo real — hora do servidor; **só em dias de TELETRABALHO ou MISTO** (422 no presencial) |
| `POST` | `/api/v1/rh/me/marcacoes/correcoes` `{ "momento", "sentido", "motivo" }` | pedido de correcção — fica **PENDENTE** e não conta até ser validado |
| `GET` | `/api/v1/rh/me/assiduidade?de=&ate=` | a minha assiduidade (a mesma leitura do RH) |
| `GET` | `/api/v1/rh/me/equipa/marcacoes-pendentes` | a caixa da chefia: correcções por decidir da equipa directa |
| `PATCH` | `/api/v1/rh/me/equipa/marcacoes/{id}/validar` · `…/rejeitar` `{ "motivo" }` | a chefia directa decide (403 se não for; 422 nas suas) |
| `PATCH` | `/api/v1/rh/funcionarios/{id}/marcacoes/{mid}/validar` · `…/rejeitar` `{ "motivo" }` | o RH decide, sempre |

A chefia directa é o titular do Lugar-pai (`parentPositionId`) do Lugar de quem pediu. Cada marcação
traz `estado` (`VALIDA` · `PENDENTE` · `REJEITADA`) e `motivoRejeicao`; só as válidas contam. No
apuramento de faltas (§6.9), um dia com correcções pendentes é `POR_VALIDAR` (`diasPorValidar`).
Em desenvolvimento, o `/me` sabe quem é o utilizador pelo cabeçalho `X-Employee-Id`.

Regras: BR-ASS-08 a BR-ASS-11.

### 6.9 Faltas por débito — apuramento do mês

`GET /api/v1/rh/funcionarios/{id}/faltas-apuradas?mes=2026-09` — DL n.º 3/2010, art. 13.º. Cruza o
registo diário (§6.8) com o horário vigente de cada dia (§6.7) e os pedidos de ausência aprovados.
**Calcula-se a cada leitura** até ao fecho do mês: aprovar um pedido que cubra o dia tira-o daqui.

```json
{ "funcionarioId": "…", "mes": "2026-09", "isento": false,
  "dias": [
    { "data": "2026-09-07", "estado": "COM_FALTA", "motivo": "INCOMPLETO", "minutosEsperados": 480, "minutosTrabalhados": 450, "minutosEmFalta": 30 },
    { "data": "2026-09-08", "estado": "COM_FALTA", "motivo": "SEM_REGISTO", "minutosEsperados": 480, "minutosTrabalhados": 0, "minutosEmFalta": 480 },
    { "data": "2026-09-12", "estado": "FERIADO", "minutosEsperados": 0, "minutosTrabalhados": 0, "minutosEmFalta": 0 }
  ],
  "debitos": [],
  "diasSemRegisto": 1, "minutosParciais": 30, "periodoNormalMinutos": 480,
  "faltasParciais": 0.5, "totalFaltas": 1.5, "diasPorCorrigir": 0 }
```

| `estado` | O que é |
|---|---|
| `COM_FALTA` · `SEM_FALTA` | dia de trabalho apurado |
| `POR_CORRIGIR` | as marcações têm anomalias (§6.8): não se apura sem as corrigir |
| `FUTURO` | hoje ou depois — o dia não acabou |
| `FORA_DO_VINCULO` · `ISENTO` · `FERIADO` · `AUSENCIA_JUSTIFICADA` · `LICENCA` · `MOBILIDADE_EXTERNA` · `MISSAO_SERVICO` · `FORMACAO` · `SUSPENSAO_DISCIPLINAR` · `ACIDENTE_SERVICO` · `SEM_HORARIO` · `DESCANSO` | não se apura |

- `motivo`: `SEM_REGISTO` (nenhuma marcação — dia inteiro), `INCOMPLETO` (horário fixo: blocos não
  cobertos — atrasos, saídas antecipadas), `PLATAFORMA` (horário flexível: plataformas fixas não cobertas).
- `debitos`: um por período de aferição de um horário flexível — esperado − trabalhado − o que já
  contou nos dias, nunca negativo.
- **Conversão (art. 13.º n.os 3 e 4):** `diasSemRegisto` são faltas de dia inteiro; `minutosParciais`
  somam-se no mês e convertem-se pelo `periodoNormalMinutos` (a média do esperado no mês): cada período
  inteiro é uma falta, o resto até meio período é meia, acima é uma. `totalFaltas` = os dois.
- `minutosJustificados`: horas de pedidos em horas aprovados (§6.2e) fora da presença — contam como
  cumpridas; um dia sem marcações mas com horas justificadas não é `SEM_REGISTO`.
- Quem tem isenção de horário no contrato não tem débito (`isento: true`).
- `minutosSuplementares`: presença dentro de trabalho suplementar autorizado (§6.10) — fica fora de
  `minutosTrabalhados` (o tempo normal) e, no flexível, do saldo da aferição.
- `mes` fora de `yyyy-MM` → 422.

Regras: BR-FAL-01 a BR-FAL-07.

### 6.10 Trabalho suplementar (horas extras)

Lei n.º 20/X/2023, art. 155.º n.º 2 a). Autoriza-se um **intervalo de um dia**; as horas realizadas
**saem das marcações**; classifica-se pelo tipo de dia. **Sem valores** — o suplemento e o tecto de um
terço da remuneração base (n.º 7) são do processamento salarial.

| Quem | Método | Path | O quê |
|---|---|---|---|
| RH | `POST` | `/api/v1/rh/funcionarios/{id}/trabalho-suplementar` | lançar — nasce **AUTORIZADO** (também para um dia passado: `autorizacaoPosterior`) |
| RH | `GET` | `/api/v1/rh/funcionarios/{id}/trabalho-suplementar?mes=2026-09` | o mês: cada um com tipo de dia e horas realizadas; totais dos autorizados |
| RH | `PATCH` | `/api/v1/rh/funcionarios/{id}/trabalho-suplementar/{tid}/autorizar` · `/api/v1/rh/funcionarios/{id}/trabalho-suplementar/{tid}/recusar` `{ "motivo" }` · `…/cancelar` `{ "motivo" }` | decidir um PEDIDO; cancelar um pedido ou autorizado |
| Próprio | `POST` · `GET` | `/api/v1/rh/me/trabalho-suplementar` · `?mes=` | pedir (hoje ou para a frente — nasce **PEDIDO**) e ler o seu mês |
| Chefia | `POST` | `/api/v1/rh/me/equipa/trabalho-suplementar` `{ "funcionarioId", … }` | lançar para a equipa directa — nasce AUTORIZADO |
| Chefia | `GET` | `/api/v1/rh/me/equipa/trabalho-suplementar-pendente` | a caixa: pedidos por decidir da equipa directa |
| Chefia | `PATCH` | `/api/v1/rh/me/equipa/trabalho-suplementar/{id}/autorizar` · `/api/v1/rh/me/equipa/trabalho-suplementar/{id}/recusar` `{ "motivo" }` | decidir (403 se não for a chefia directa; 422 nos seus) |

```json
POST /api/v1/rh/funcionarios/{id}/trabalho-suplementar
{ "data": "2026-09-22", "horaInicio": "17:00", "horaFim": "19:00", "motivo": "fecho de contas" }

GET /api/v1/rh/funcionarios/{id}/trabalho-suplementar?mes=2026-09
{ "funcionarioId": "…", "mes": "2026-09",
  "trabalhos": [ { "id": "…", "data": "2026-09-22", "horaInicio": "17:00", "horaFim": "19:00",
                   "estado": "AUTORIZADO", "pedidoPeloProprio": false, "autorizacaoPosterior": true,
                   "tipoDia": "DIA_UTIL", "minutosAutorizados": 120, "minutosRealizados": 90, "semRegisto": false } ],
  "minutosAutorizados": 120, "minutosRealizados": 90,
  "minutosRealizadosDiaUtil": 90, "minutosRealizadosDescanso": 0, "minutosRealizadosFeriado": 0 }
```

- `tipoDia`: `FERIADO` (calendário do colaborador), `DESCANSO` (dia sem blocos no horário vigente) ou
  `DIA_UTIL`. Num dia útil o intervalo tem de ficar **fora dos blocos do horário** (422).
- `minutosRealizados`: presença (marcações válidas) dentro do intervalo; só nos autorizados.
  `semRegisto`: autorizado, num dia passado, sem nenhuma marcação válida.
- 422: sem motivo, `horaInicio` ≥ `horaFim` ou fora de `HH:mm`, isenção de horário no contrato, sem
  horário nesse dia, o próprio a pedir para um dia passado, recusar ou cancelar sem motivo.
  409: sobreposição com outro pedido ou autorizado; decidir o que não é PEDIDO; cancelar o que já não
  está em vigor. 403: colaborador inactivo; chefia que não é a directa.

Regras: BR-SUP-01 a BR-SUP-09, BR-ME-04.

### 6.11 Relação mensal — art. 75.º

`GET /api/v1/rh/assiduidade/relacao-mensal?mes=2026-09&unidadeId={uuid}&incluirSubunidades=true` —
DL n.º 3/2010, art. 75.º: as faltas e licenças de cada funcionário e a sua natureza, **por serviço**.
**Só leitura**, calculada a cada pedido (o fecho do mês fica para quando houver integração salarial).
`GET …/assiduidade/relacao-mensal.csv` com os mesmos parâmetros devolve o CSV (`text/csv`, anexo) — pedido com `Accept: text/csv`.

```json
{ "mes": "2026-08", "provisoria": false, "unidadeId": "…", "unidadeNome": "Ministério…", "incluirSubunidades": true,
  "unidades": [ { "unidadeId": "…", "codigo": "SERV_RH", "nome": "Serviço de Recursos Humanos",
                  "colaboradores": 1, "comPendencias": 1, "faltasPorJustificar": 18,
                  "linhas": [ { "funcionarioId": "…", "numeroFuncionario": "0000002", "nome": "Maria Santos",
                                "isento": false, "diasForaDoVinculo": 0, "diasFerias": 0,
                                "faltasJustificadas": [ { "codigo": "TRATAMENTO_AMBULATORIO", "nome": "…", "dias": 0, "minutos": 30,
                                                          "efeitoRemuneracao": "SEM_PERDA", "opcaoFaltaInjustificada": null } ],
                                "faltasInjustificadas": [],
                                "diasSemRegisto": 18, "faltasParciais": 0, "faltasPorJustificar": 18,
                                "licencas": [ { "codigo": "…", "tipoRegisto": "LICENCA", "dias": 5, "afectaRemuneracao": true, "contaAntiguidade": false } ],
                                "minutosSuplementarDiaUtil": 90, "minutosSuplementarDescanso": 0, "minutosSuplementarFeriado": 0,
                                "diasPorCorrigir": 1, "diasPorValidar": 0, "pedidosPendentes": 0, "estado": "COM_PENDENCIAS" } ] } ] }
```

- **Quem entra**: quem teve afectação principal na unidade (ou subunidades) em algum dia do mês,
  pelas datas da afectação — quem cessou a meio também, com `diasForaDoVinculo`. Uma pessoa, uma
  unidade: a da afectação que chega mais longe no mês.
- **Férias e faltas** cortadas ao mês: `dias` pela contagem do tipo; `minutos` nos pedidos em horas.
  As injustificadas trazem a `opcaoFaltaInjustificada` (art. 43.º n.º 2) quando registada.
- **Licenças e mobilidade**: dias de calendário no mês, por subtipo.
- **Faltas por justificar**: do apuramento (§6.9). **Trabalho suplementar**: minutos realizados (§6.10).
- `estado`: `COM_PENDENCIAS` com dias por corrigir/validar ou pedidos de ausência por decidir no mês.
- `provisoria: true` no mês corrente. 422: mês futuro ou mal escrito, `unidadeId` vazio ou mal
  escrito. 404: unidade que não existe.
- CSV: uma linha por colaborador, `;`, UTF-8 com BOM, decimais com vírgula.

Regras: BR-REL-01 a BR-REL-08, BR-FAL-08.

### 6.12 Lista de antiguidade — arts. 69.º e 70.º

`GET /api/v1/rh/relatorios/lista-antiguidade?ano=2026&unidadeId={uuid}&incluirSubunidades=true` —
DL n.º 3/2010, art. 69.º: a lista de cada serviço com referência a **31 de Dezembro do ano anterior**,
**por cargo** e, em cada cargo, **pela antiguidade no cargo**. `…/lista-antiguidade.csv` devolve o CSV
(para afixar e publicar). Só se gera; o ciclo de aprovação, reclamação e publicação fica para depois.

```json
{ "ano": 2026, "referencia": "2025-12-31", "unidadeId": "…", "unidadeNome": "…", "incluirSubunidades": true,
  "grupos": [ { "carreira": "Técnica", "categoria": "Técnico Principal", "foraDeGrelha": false,
                "linhas": [ { "posicao": 1, "numeroFuncionario": "0000002", "nome": "Maria Santos",
                              "unidadeCodigo": "SERV_RH", "escalao": "TP-2", "dataInicioNoCargo": "2024-03-01",
                              "diasDescontados": 0, "diasContados": 671, "anos": 1, "meses": 10, "dias": 1,
                              "dataAdmissao": "2020-01-01", "anosServico": 6, "mesesServico": 0, "diasServico": 0,
                              "observacoes": null } ] } ] }
```

- **Cargo**: a carreira e a categoria em 31 de Dezembro (pelo escalão da afectação); fora da grelha,
  o cargo (Job) do Lugar, num grupo «Fora de grelha» no fim.
- **Início no cargo**: o começo da cadeia de afectações seguidas na mesma categoria (progressões e
  transferências não cortam; uma interrupção corta).
- **Tempo contado**: dias no cargo menos os descontos da lei, em anos/meses/dias (365/30 dias);
  `observacoes` diz os períodos descontados e o motivo. O tempo de serviço total desempata.
- 422: ano cuja referência ainda não chegou, `unidadeId` mal escrito. 404: unidade que não existe.

Regras: BR-LAN-01 a BR-LAN-05.

### 6.13 Mapa de efectivos — Lei n.º 20/X/2023, art. 4.º al. aa)

`GET /api/v1/rh/relatorios/mapa-efectivos?unidadeId={uuid}&incluirSubunidades=true` — os postos de
trabalho que o serviço tem **hoje**, por unidade e por cargo; `…/mapa-efectivos.csv` em CSV.

```json
{ "data": "2026-09-24", "unidadeId": "…", "unidadeNome": "…", "incluirSubunidades": true,
  "lugares": 12, "providos": 9, "vagos": 3, "congelados": 1,
  "unidades": [ { "unidadeId": "…", "codigo": "SERV_RH", "nome": "Serviço de Recursos Humanos",
                  "lugares": 5, "providos": 4, "vagos": 1, "congelados": 0,
                  "cargos": [ { "carreira": "Técnica", "categoria": "Técnico Principal", "foraDeGrelha": false,
                                "lugares": 2, "providos": 2, "vagos": 0, "congelados": 0 } ] } ] }
```

- `lugares` = Lugares activos (a dotação); `providos` = com titular; `vagos` = activos sem titular;
  `congelados` à parte. Os extintos não entram.
- Por cargo: carreira e categoria do Lugar (fora da grelha, o cargo/Job); a categoria mais alta primeiro.
- 422: `unidadeId` mal escrito. 404: unidade que não existe.

Regras: BR-MEF-01 a BR-MEF-03.

### 6.14 Indicadores do pessoal — Lei n.º 20/X/2023, art. 38.º n.º 3

`GET /api/v1/rh/relatorios/indicadores?unidadeId={uuid}&incluirSubunidades=true&ano=2026` — o balanço
social de um serviço num ano. **Devolve números; o gráfico é do front.**

```json
{ "ano": 2026, "referencia": "2026-09-24", "unidadeId": "…", "unidadeNome": "…", "incluirSubunidades": true,
  "efectivos": 14,
  "porGenero": [ { "chave": "F", "valor": 8 }, { "chave": "M", "valor": 6 } ],
  "porEscalaoEtario": [ { "chave": "<30", "valor": 2 }, { "chave": "30-39", "valor": 5 }, … ],
  "porTipoContrato": [ … ], "porCarreira": [ … ], "porUnidade": [ … ],
  "entradas": 2, "saidas": 1,
  "diasFalta": 37, "diasUteisPotenciais": 2604, "taxaAbsentismo": 1.42,
  "minutosSuplementares": 2520, "horasSuplementares": 42.00 }
```

- Referência: hoje no ano corrente; 31 de Dezembro num ano passado. Ano futuro → 422.
- `efectivos`: afectação principal no serviço na referência; cada `por…` soma os efectivos.
- `porGenero`: «F» e «M». Há colaboradores gravados com «FEMININO»/«MASCULINO» (o seed, o enum
  `Sexo`) e com «F»/«M» (o catálogo `SEX`); contam juntos. Sem género → «Não indicado».
- `taxaAbsentismo`: dias úteis de faltas aprovadas (sem férias) ÷ dias úteis de vínculo × 100.
- `horasSuplementares`: trabalho suplementar realizado no ano (pelas marcações).

Regras: BR-IND-01 a BR-IND-05.

### 6.15 Ficheiros — `/documento`

Os ficheiros vivem no MinIO. O ecrã carrega primeiro o ficheiro e depois regista os metadados no dono
(colaborador, contrato, qualificação, formação, recibo, processo disciplinar, licença):

| Método | Path | O quê |
|---|---|---|
| `POST` | `/documento/private/{folder}` | multipart `file` numa pasta privada (`FUNCIONARIO`) → `{ "fileId", "displayName" }` |
| `POST` | `/documento/public/{path}` | multipart `file` numa pasta pública → o mesmo |
| `GET` | `/documento?fileId=` | ligação temporária assinada → `FileUrlDTO` `{ "url" }` |

Estes três estão fora do prefixo `/api/v1/rh`. Com o `fileId` (como `fileKey`), regista-se o documento no dono pelo
sub-recurso `documentos` abaixo, que valida o tipo, o tipo activo e a extensão (BR-DOC-01 a BR-DOC-03).

### Sub-recurso `documentos` (padrão)
```
POST   .../{ownerId}/documentos            # upload (multipart)
GET    .../{ownerId}/documentos            # listar
GET    .../{ownerId}/documentos/{docId}    # metadados
GET    .../{ownerId}/documentos/{docId}/download
DELETE .../{ownerId}/documentos/{docId}
```

---

## 7. Licenças e mobilidade — `/funcionarios/{id}/licencas-mobilidade`

> **A mobilidade transitória não tira o Lugar ao titular** (Lei n.º 20/X/2023, art. 135.º n.º 7). Este processo **não mexe na afectação**: regista onde a pessoa exerce funções e até quando. Para mudar mesmo de Lugar, usar a **transferência** (5.6).
>
> **A licença pode tirar** (DL n.º 3/2010) — ver 7.1. O `record_type` do subtipo é `LICENCA` ou `MOBILIDADE`; o valor `AMBOS` foi removido.

### 7.0 Dois eixos: a decisão e o período

O art. 44.º do DL n.º 3/2010 trata dois factos em números seguidos — o n.º 1 define a licença como «ausência prolongada do serviço» (um **período**) e o n.º 2 faz a concessão depender «do pedido do interessado e do **despacho** da autoridade competente» (um **acto**). Desde a **V48** o registo espelha essa separação, e a resposta traz os dois campos:

| Campo | Eixo | Valores |
|---|---|---|
| `status` | **decisão** — o que foi despachado | `PENDING` · `APPROVED` · `REJECTED` · `CANCELLED` |
| `estadoPeriodo` | **período** — onde a licença está hoje | `POR_INICIAR` · `EM_CURSO` · `TERMINADA` (nulo se não estiver deferida) |

**`ACTIVE` e `CLOSED` desapareceram.** Não eram decisões: eram o período disfarçado de decisão. Quem lia `status == "ACTIVE"` para saber se alguém está de licença passa a ler **`estadoPeriodo == "EM_CURSO"`**; quem lia `CLOSED` passa a ler `TERMINADA`.

O que isto muda no comportamento:

- **Deferir não é pôr em vigor.** Aprovar uma licença que começa daqui a um mês deixa-a `APPROVED` / `POR_INICIAR` e **não** abre vaga nem muda o estado do trabalhador. Os efeitos aplicam-se na data de início — na própria transacção se ela for hoje, por um **job diário** (`rh.licencas.efeitos.cron`, 00:15 por omissão) se for mais tarde.
- **O período acaba sozinho.** Passada a `dataFim`, a licença fica `TERMINADA` sem ninguém carregar em nada, e o mesmo job aplica o regresso e faz caducar as substituições — é o «caduca automaticamente» do art. 46.º n.º 3. Antes, uma licença esquecida ficava em vigor para sempre.
- **Não se regressa do que não começou.** O `close` de uma licença `POR_INICIAR` devolve **409** e indica o cancelamento. Antes fixava o fim em *hoje* sem olhar ao início, gravando um fim **anterior** ao início — e falseando as contagens de dias em que assentam o desconto na antiguidade e as férias proporcionais (art. 47.º n.os 1 a 3).
- **A data de regresso é o primeiro dia de volta**, logo a `dataFim` fica na **véspera**. Quem parte e regressa no mesmo dia fica com um dia — o mínimo que datas em `DATE` exprimem.

| Método | Path | Descrição |
|---|---|---|
| `POST` | `/` | Criar licença/mobilidade (nasce `PENDING`). |
| `GET` | `/` · `/{licencaId}` | Listar / detalhe. |
| `PUT` | `/{licencaId}` | Atualizar (só enquanto `PENDING`). |
| `PUT` | `/{licencaId}/approve` | **Deferir** (só a partir de `PENDING`). Valida destino e duração. **Não mexe na afectação.** Os efeitos no Lugar só se aplicam na data de início — ver 7.0. |
| `PUT` | `/{licencaId}/reject` | Rejeitar com motivo. |
| `PUT` | `/{licencaId}/close` | **Regresso antecipado** (art. 46.º n.º 4): encurta o período. Só com o registo deferido e **em curso** — ver 7.0. |
| `PUT` | `/{licencaId}/cancel` | Cancelar (`PENDING`, ou `APPROVED` que **ainda não começou**). |
| `PUT` | `/{licencaId}/prorrogar` | **Prorrogar** o período em vigor (limites do subtipo). |
| `PATCH` | `/{licencaId}/ativar` | Igual a `approve` (alias legado). |
| `DELETE` | `/{licencaId}/desativar` | Soft delete do registo. |
| `POST/GET/DELETE` | `/{licencaId}/documentos…` | Documentos anexos. |

**`LicencaMobilidadeRequestDTO`** (campos-chave):
```json
{
  "subtipoId": "uuid",                 // t_leave_mobility_subtype (record_type)
  "dataInicio": "YYYY-MM-DD",
  "dataFim": "YYYY-MM-DD | null",      // obrigatória se o subtipo tiver duração máxima
  "entidadeDestino": "string | null",  // mobilidade EXTERNA
  "destinationUnitId": "uuid | null",  // mobilidade INTERNA
  "destinationPositionId": "uuid | null", // legado, já não é usado
  "despachoNumero": "string | null",
  "justification": "string | null"
}
```

### Destino da mobilidade
| Caso | O que enviar |
|---|---|
| **Interna** — outra unidade nossa | `destinationUnitId` (tem de existir) |
| **Externa** — entidade de fora (autarquia, empresa pública, privado, organismo internacional) | `entidadeDestino` |

Sem nenhum dos dois, o `approve` devolve **422**. O `destinationPositionId` deixou de ser exigido: a mobilidade transitória não ocupa Lugar no destino.

### 7.0b Forma de prestação: a tempo inteiro ou em acumulação

O art. 134.º n.º 2 da Lei n.º 20/X/2023 classifica a mobilidade geral **quanto à forma de
prestação**:

| Valor | A lei |
|---|---|
| `TEMPO_INTEIRO` | «quando o funcionário passa a desempenhar funções noutro serviço, em regime de exclusividade» |
| `ACUMULACAO` | «quando o funcionário passa a exercer funções noutro serviço, em acumulação com as do serviço de origem» |

Envia-se em `formaPrestacao` na criação e vem no mesmo campo na leitura. **Omisso vale
`TEMPO_INTEIRO`**, porque a exclusividade é a regra (art. 20.º) — não se infere acumulação do
silêncio. Só se fixa enquanto o processo está `PENDING`: depois do despacho, passar de
exclusividade a acumulação é outro despacho (409).

> **Nem uma nem outra cria afectação.** A mobilidade transitória é *sem ocupação do lugar do
> quadro* (art. 135.º n.º 7), qualquer que seja a forma de prestação. Se procurava a acumulação
> como título de ocupar um segundo Lugar, ela **não existe** — e nunca existiu na lei; ver a nota
> em 4 e a secção 11.15 de `breaking_change_frontend.md`.

> **Não confundir com o art. 21.º** (acumulação de funções públicas), que é outro instituto:
> regime de permissão, com incompatibilidade, manifesto interesse público e, em regra, não
> remunerada; sendo remunerada, só nos casos taxativos do n.º 2. **Não está implementado.**

Pedir `ACUMULACAO` num subtipo de **licença** dá **422**: quem está de licença não exerce
funções em serviço nenhum.

---

### 7.0c Consolidar a mobilidade — `POST .../licencas-mobilidade/{licencaId}/consolidar`

Art. 132.º n.º 4: «A mobilidade definitiva ocorre nos casos de **consolidação da mobilidade
transitória, na mesma função e categoria**.» A pessoa deixa de estar em mobilidade e passa a ser
**titular de um Lugar vago do serviço de destino**.

É a única via pela qual uma mobilidade toca na afectação, e não contradiz o art. 135.º n.º 7:
esse diz que a *transitória* não ocupa Lugar; o n.º 8 define a definitiva como a que é feita
«com ocupação do lugar do quadro».

```json
{
  "positionId": "uuid do Lugar vago do serviço de destino",
  "dataEfeito": "YYYY-MM-DD",
  "despachoNumero": "string | null",
  "observacoes": "string | null"
}
```

**Resposta `201`** (`ConsolidacaoMobilidadeResponseDTO`): traz a nova afectação, a mobilidade que
se consolidou, o `mobilidadeDataFim` (a **véspera** da data de efeito) e os Lugares de partida e
de chegada.

| Código | Quando |
|---|---|
| `400` | `positionId` ou `dataEfeito` em falta; UUID inválido; data de efeito anterior ao início da mobilidade. |
| `404` | Funcionário, mobilidade ou Lugar não existem — ou a mobilidade não é desse funcionário. |
| `409` | A mobilidade não está deferida, ou **ainda não começou** (não há período transitório para consolidar). |
| `422` | Subtipo de licença (só mobilidade se consolida) · mobilidade **externa** · Lugar que não é da unidade de destino · Lugar com titular, não ATIVO ou igual ao actual · **cargo ou categoria diferentes** · sem afectação corrente · data de efeito não posterior ao início da afectação. |

**Mesma função e categoria, e o escalão mantém-se** — não há evolução na grelha numa
consolidação. Consolidar mudando de categoria ou de função exige habilitação adequada e
aprovação em **concurso comum interno** (art. 135.º n.º 6), que esta aplicação não modela.

**Só a mobilidade interna se consolida.** Numa externa o destino é outra entidade e não há
Lugar do nosso quadro onde pôr a pessoa — o que a lei prevê aí é a saída do quadro, que é uma
cessação.

> **Não se exige tempo mínimo de mobilidade.** O art. 132.º n.º 4 acaba com «nos termos regulados
> por diploma de desenvolvimento»: a condição existe, vive noutro diploma, e pôr aqui um prazo
> seria escrever no código uma regra que ninguém escreveu.

> A mobilidade fica **`APPROVED`** com o período encurtado. Não há regresso: quem consolida não
> volta ao Lugar de origem, e por isso o efeito de regresso marca-se logo como aplicado.

---

### 7.0d Regresso de comissão de serviço — regressa, ou cessa

Art. 64.º n.º 2: «Cessada a comissão de serviço, o nomeado **regressa à situação jurídico-funcional
de que era titular antes dela**, quando constituída e consolidada por tempo indeterminado, ou,
**no caso contrário, cessa a relação jurídica de emprego público**.»

Classifica-se no catálogo com `return_effect = REGRESSA_OU_CESSA`. É o **único** efeito no regresso
que pode terminar o vínculo — os outros dois (`REGRESSA_LUGAR`, `DISPONIBILIDADE`) nunca cessam nada.

Não há endpoint novo: acontece **no fim da comissão**, seja pelo `close` (regresso antecipado) seja
pelo job diário na data de fim. Qual dos dois caminhos se segue **deriva-se do percurso**:

| Situação do colaborador | O que acontece |
|---|---|
| **Tem afectação corrente** (a comissão mantém o Lugar) | Regressa a ele. Não há estado a mudar nem nada a registar. |
| **Não tem afectação corrente** — foi recrutado *para* a comissão | A relação **cessa**, pelo `CessacaoService`, com o motivo no histórico. |

> **Não se guarda um campo a dizer qual é o caso.** É derivável do percurso, e um campo mal
> preenchido passaria a decidir uma cessação.

**Atenção ao catálogo:** até 2026-09-22 o `MOB_COMISSAO` do seed estava como `REGRESSA_LUGAR`, sem
condição — devolvia ao Lugar de origem **toda a gente**, incluindo quem nunca teve Lugar. O seed
passou também a **1095 dias e sem limite de renovações** (art. 60.º n.º 1: três anos,
sucessivamente renovável), em vez dos 365 com uma prorrogação, que é a regra da mobilidade comum.
Instalações existentes têm de reclassificar o seu próprio catálogo.

---

### 7.0e As sete modalidades de licença do art. 45.º

O art. 45.º n.º 1 do DL n.º 3/2010 fixa **sete** modalidades, e o seed traz-lhes uma linha a cada:

| Código | Modalidade | Regime | Lugar |
|---|---|---|---|
| `LIC_SEM_VENC_90` | sem vencimento **até 90 dias** (al. a) | art. 46.º–47.º | mantém-se |
| `LIC_SEM_VENCIMENTO` | sem vencimento **até 3 anos** (al. b) | art. 48.º–49.º | mantém-se |
| `LIC_LONGA_DURACAO` | sem vencimento de **longa duração** (al. c) | art. 50.º–53.º | abre vaga |
| `LIC_ACOMP_CONJUGE` | acompanhamento do cônjuge no estrangeiro (al. d) | art. 56.º | abre vaga além de 1 ano |
| `LIC_ORG_INTERNACIONAL` | organismos internacionais (al. e) | art. 62.º | abre vaga |
| `LIC_EXTRAORDINARIA` | **extraordinária** (al. f) | art. 64.º | mantém-se |
| `LIC_FORMACAO` | formação (al. g) | art. 65.º e 67.º n.º 3 | abre vaga além de 6 meses |

As duas primeiras **não são a mesma com prazo diferente**: são subsecções distintas, com
requisitos de tempo de serviço distintos — um ano para a de 90 dias (art. 46.º n.º 1), três anos
para a de três anos (art. 48.º n.º 1) —, e a de 90 dias não pode voltar a pedir-se nos **dois anos
seguintes** (art. 46.º n.º 2). Desses requisitos o catálogo só guarda o **tecto de duração**.

**Duas coisas que a aplicação ainda não verifica**, e que ficam ditas para ninguém contar com
elas:

- **O tempo de serviço exigido** por cada modalidade (um ano, três anos, dois anos para a
  formação). É derivável da antiguidade, que já existe — mas ligá-lo é um passo por fazer.
- **Quem pode pedir a extraordinária.** O art. 64.º dá-a só a quem está na situação de
  **disponibilidade**, e remete o resto do regime para «o diploma que estabelece o regime de
  mobilidade», que não temos. Por isso a linha **não traz prazo nem número de prorrogações
  inventados**, e conta para antiguidade por omissão — descontar o que não se sabe tiraria tempo
  a quem o tem.

---

### 7.1 Licenças que abrem vaga
Três campos do subtipo dizem o que a licença faz ao Lugar:

| Campo | Valores | O que faz |
|---|---|---|
| `positionEffect` | `MANTEM` · `ABRE_VAGA` | Se o Lugar fica ocupado ou vago enquanto a licença dura |
| `vacancyAfterDays` | inteiro ou nulo | Abre vaga só se a duração exceder este número de dias; nulo abre logo |
| `returnEffect` | `REGRESSA_LUGAR` · `DISPONIBILIDADE` · `REGRESSA_OU_CESSA` | O que acontece ao funcionário quando a licença termina (o terceiro é a comissão de serviço, 7.0d) |

No `approve` (ou `ativar`), se o subtipo abrir vaga para aquela duração:
- a **afectação corrente é encerrada** na data de início e o Lugar fica vago;
- o colaborador passa ao estado com situação `INACTIVIDADE_FORA_QUADRO`, se existir no catálogo;
- **o vínculo não cessa** — a resposta traz `afectacaoEncerradaId` e o colaborador continua activo.

No regresso (pelo `close` ou pelo job, quando o período acaba), se o `returnEffect` for `DISPONIBILIDADE`, o colaborador passa ao estado com essa situação (art. 122.º) e a resposta traz `estadoAtribuidoId`. A nova afectação é um acto do RH: depende de haver Lugar livre.

Um período **sem data de fim** ultrapassa qualquer prazo, logo abre vaga. A **mobilidade nunca abre vaga**: marcar um subtipo de mobilidade como `ABRE_VAGA` devolve **400**.

Valores do seed: `LIC_SEM_VENCIMENTO` mantém; `LIC_LONGA_DURACAO` e `LIC_ORG_INTERNACIONAL` abrem logo; `LIC_ACOMP_CONJUGE` além de 365 dias; `LIC_FORMACAO` além de 180.

### Duração e prorrogação
A duração máxima e o número de prorrogações são **parametrizados no subtipo** (`maxDurationDays`, `maxExtensions`). Por omissão, os subtipos de mobilidade ficam com 365 dias e 1 prorrogação (art. 132.º n.º 5). Se o subtipo tiver duração máxima, a `dataFim` é obrigatória e a duração é validada no `approve` (**422** se exceder).

**`PUT /{licencaId}/prorrogar`** — só com o registo deferido (`APPROVED`):
```json
{ "novaDataFim": "YYYY-MM-DD", "despachoNumero": "string | null", "observacoes": "string | null" }
```
Resposta `200` (`ProrrogacaoMobilidadeResponseDTO`): `id`, `dataInicio`, `dataFimAnterior`, `dataFim`, `prorrogacoes`, `maxProrrogacoes`.
Devolve **422** se exceder o número de prorrogações ou se o período acrescentado exceder o máximo do subtipo ("por igual período").

### Onde o colaborador exerce funções
Durante uma mobilidade, o **Lugar continua a ser o de origem** e passa a haver um bloco à parte a dizer onde a pessoa está:

| Endpoint | O que ganha |
|---|---|
| `GET /colaboradores/assignments/funcionario/{id}/unidade-atual` | `emMobilidade`, `mobilidadeId`, `mobilidadeInicio`, `mobilidadeFim`, `mobilidadeDestinoTipo` (INTERNO/EXTERNO), `exerceFuncoesUnidadeId`, `exerceFuncoesUnidadeNome` |
| `GET /funcionarios/{id}/details` | bloco `mobilidadeEmVigor` (id, destinoTipo, destinoUnidadeId, destinoNome, datas, despacho) |
| `GET /me` | bloco `mobilidadeEmVigor`; `currentUnit` continua a ser a unidade do Lugar |

Sem mobilidade em vigor, `exerceFuncoesUnidade*` é a unidade do próprio Lugar.

> ⚠️ **Breaking change:** `approve` e `close` já **não** mexem na afectação. Um front-end que mostrava o Lugar de destino após aprovar a mobilidade passa a mostrar o Lugar de origem, que continua a ser o do colaborador.

---

## 8. Carreiras (PCFR) — `/api/v1/rh/{careers|categories|grades}`

| Recurso | Base | Extras |
|---|---|---|
| Carreiras | `/api/v1/rh/careers` | `GET /{id}/categories`, `PUT /{id}/activate`, `combobox` |
| Categorias | `/api/v1/rh/categories` | `GET /{id}/grades`, `PUT /{id}/activate`, `combobox` |
| Escalões | `/api/v1/rh/grades` | `PUT /{id}/activate`, `combobox` |

---

## 9. Catálogos (parametrizações) — `/api/v1/rh/catalogs/*` e `/reference/options`

| Catálogo | Base |
|---|---|
| Estados do trabalhador | `/api/v1/rh/catalogs/worker-states` |
| Tipos de contrato | `/api/v1/rh/catalogs/contract-types` |
| Vínculos laborais | `/api/v1/rh/catalogs/vinculos-laborais` |
| Tipos de ausência | `/api/v1/rh/catalogs/leave-types` |
| Subtipos de mobilidade | `/api/v1/rh/catalogs/leave-mobility-subtypes` |
| Tipos de documento | `/api/v1/rh/catalogs/document-types` |
| Feriados | `/api/v1/rh/catalogs/public-holidays` |
| Parâmetros do mapa de férias (§9.2 — padrão próprio) | `/api/v1/rh/catalogs/parametros-ferias` |
| Horários (§9.3 — lista sem paginação, mais `PATCH /{id}/base`) | `/api/v1/rh/catalogs/horarios` |
| Opções genéricas | `/api/v1/rh/reference/options` |

Padrão CRUD comum: `GET`, `GET/{id}`, `POST`, `PUT/{id}`, `DELETE/{id}`, `PATCH/{id}/activate`, `GET/combobox`.

### 9.1 Feriados — o calendário da instituição (V55)

A contagem de **dias úteis** (pedidos de ausência, suspensão de férias) tira sábados, domingos e
os feriados que se aplicam ao colaborador. O calendário é **da instituição**: ela cataloga os
nacionais e os do sítio onde tem serviços, e **conta-se tudo o que estiver activo** — não só os
nacionais, como antes.

```json
POST /api/v1/rh/catalogs/public-holidays
{
  "name": "Dia da Independência",
  "holidayDate": "2026-07-05",
  "isNational": true,
  "isRecurring": true,
  "areaCkey": null,
  "description": "Feriado nacional"
}
```

| Campo | O que diz |
|---|---|
| `isRecurring` | `true`: no mesmo dia e mês **todos os anos**, a partir do ano de `holidayDate`. Para os de data fixa. Os móveis (Sexta-feira Santa, Corpus Christi) carregam-se ano a ano, com `false`. Omisso = `false`. |
| `areaCkey` | `ckey` do catálogo `AREA_GEOGRAFICA` (`/reference/options`). Nulo = vale para toda a gente, que é o caso normal. |

**Recusas:**

| Caso | Resposta |
|---|---|
| Feriado **nacional** com `areaCkey` — nacional é todo o território | 422 |
| `isRecurring` a 29 de Fevereiro | 422 |
| Já há um nacional activo nesse dia (um recorrente conta em todos os anos desde o seu) | 409 |

**O `PUT` não apaga o que não recebe:** com `isRecurring` ou `areaCkey` omissos (nulos) fica o valor
que já lá estava — um ecrã que ainda não conhece os campos não desmarca a recorrência ao corrigir
um nome. A área limpa-se enviando `""`; a recorrência desmarca-se com `false`.

**A área ainda não se valida contra o catálogo** (decisão de 2026-09-23, para não partir o
front): um `ckey` inexistente é aceite, e o feriado simplesmente não conta para ninguém.

**A área do colaborador vem da unidade orgânica.** `POST`/`PUT
/api/v1/rh/estrutura/organizational-units` aceitam `areaCkey`, sem validação e com o mesmo `PUT`
que não apaga o que não recebe; a resposta devolve-o. A área que conta é a da unidade onde o colaborador **exerce funções** no início do
período — na mobilidade interna, a de destino — ou, se ela não tiver, a da unidade-mãe mais
próxima que tenha. Mobilidade externa, sem Lugar ou sem área em lado nenhum: só contam os
feriados sem área.

**Os feriados contam-se no período inteiro** do pedido: um pedido de 28 de Dezembro a 5 de
Janeiro apanha o 1 de Janeiro do ano seguinte.

Regras: BR-PH-01 a BR-PH-07 em `regras_negocio.html`.

### 9.2 Parâmetros do mapa de férias — por vigência

As datas e o período mínimo do mapa de férias (§6.6) vêm da tabela `t_parametro_ferias`, e não
de `application.properties`: mudam-se pela API, sem tocar em código nem reiniciar. São **globais**,
porque a lei é a mesma para todas as instituições, e têm **vigência**: cada linha vale a partir de
`vigenteDesde` até à linha seguinte. Um diploma novo é uma linha nova, e os mapas dos anos
anteriores continuam a ler as regras do seu tempo.

| Método | Path | O quê |
|---|---|---|
| `GET` | `/api/v1/rh/catalogs/parametros-ferias` | todas as vigências, da mais antiga para a mais recente (lista, sem paginação) |
| `GET` | `/api/v1/rh/catalogs/parametros-ferias/vigente?ano=2027` | a que vale no ano (sem `ano`: o corrente) |
| `POST` | `/api/v1/rh/catalogs/parametros-ferias` | vigência nova → 201 com o `id` |
| `PUT` | `/api/v1/rh/catalogs/parametros-ferias/{id}` | altera uma vigência → 200 |

```json
POST /api/v1/rh/catalogs/parametros-ferias
{
  "vigenteDesde": 2028,
  "prazoPreferencia": "01-31",
  "prazoMapa": "03-31",
  "fixacaoInicio": "05-01",
  "fixacaoFim": "10-31",
  "periodoMinimoInterpolado": 11,
  "fundamento": "Diploma n.º …"
}
```

| Campo | O que diz | Lei (DL n.º 3/2010) |
|---|---|---|
| `prazoPreferencia` | até quando o trabalhador indica a preferência (art. 5.º n.º 4) | `01-31` |
| `prazoMapa` | até quando o serviço elabora e dá conhecimento do mapa (art. 6.º n.º 1) | `03-31` |
| `fixacaoInicio` · `fixacaoFim` | janela em que o dirigente fixa, sem acordo (art. 5.º n.º 5) | `05-01` · `10-31` |
| `periodoMinimoInterpolado` | em gozo interpolado, um período tem pelo menos estes dias úteis (art. 5.º n.º 1) | `11` |

A resposta traz ainda `origem`: `TABELA`, ou `LEI` quando nenhuma linha vigora no ano — então
valem os valores da lei e o `id` vem nulo. O seed carrega a linha da lei, em vigor desde 2010.

**Recusas:**

| Caso | Resposta |
|---|---|
| data que não seja `MM-dd` válido todos os anos (inclui `02-29`) | 422 |
| `prazoPreferencia` depois de `prazoMapa` | 422 |
| `fixacaoInicio` depois de `fixacaoFim` (a janela é dentro do ano civil) | 422 |
| `vigenteDesde` ausente ou sem quatro algarismos · `periodoMinimoInterpolado` < 1 | 422 |
| já há uma vigência nesse ano (no `POST`, ou no `PUT` que mude `vigenteDesde`) | 409 |

**O `PUT` não apaga o que não recebe:** campo omisso (nulo) fica como estava — muda-se um prazo
sem reenviar os outros. O `fundamento` limpa-se enviando `""`.

Regra: BR-FER-20.

### 9.3 Horários — o catálogo da instituição

Os horários de trabalho que a instituição usa. O **nome é livre**, e é nele que se diz a modalidade
(rígido, desfasado, jornada contínua…): o diploma que as define (Lei n.º 20/X/2023, art. 165.º
n.º 2) não está publicado. Os **limites legais não se validam** pela mesma razão.

| Método | Path | O quê |
|---|---|---|
| `GET` | `/api/v1/rh/catalogs/horarios?isActive=` | lista por nome (sem paginação) |
| `GET` | `/api/v1/rh/catalogs/horarios/{id}` | um horário |
| `POST` | `/api/v1/rh/catalogs/horarios` | cria → 201 |
| `PUT` | `/api/v1/rh/catalogs/horarios/{id}` | altera → 200 |
| `DELETE` | `/api/v1/rh/catalogs/horarios/{id}` | desactiva |
| `PATCH` | `/api/v1/rh/catalogs/horarios/{id}/activate` | reactiva |
| `PATCH` | `/api/v1/rh/catalogs/horarios/{id}/base` `?desde=yyyy-MM-dd` | marca como horário base a partir de hoje (ou de `desde`, futura) → 200 |
| `POST` | `/api/v1/rh/catalogs/horarios/{id}/duplicar` `?nome=` | cópia editável (não é base) → 201 |

```json
POST /api/v1/rh/catalogs/horarios
{
  "nome": "Horário normal",
  "controlo": "FIXO",
  "blocos": [
    { "diaSemana": 1, "inicio": "08:00", "fim": "12:30" },
    { "diaSemana": 1, "inicio": "14:00", "fim": "17:30" }
  ]
}
```

| Campo | O que diz |
|---|---|
| `controlo` | `FIXO`: o período normal são os blocos. `FLEXIVEL`: cumpre-se `duracaoDiaria`, e a falta é o débito no fim de cada `periodoAfericao` (DL n.º 3/2010, art. 13.º n.º 2) |
| `blocos[].diaSemana` | 1 = segunda … 7 = domingo |
| `blocos[].inicio` · `fim` | `HH:mm`; o intervalo de descanso é o espaço entre dois blocos |
| `blocos[].obrigatorio` | no flexível, marca as plataformas fixas; omisso = `true`. No fixo é sempre `true` |
| `periodoAfericao` · `duracaoDiaria` | só no flexível, e aí obrigatórios (`SEMANA`·`MES`; `HH:mm`) |
| `horasSemanais` (resposta) | calculado; não se envia |
| `isBase` (resposta) | o horário da instituição, para quem não tem horário na pessoa nem na unidade |

**Recusas:** bloco com início depois do fim ou a passar a meia-noite, blocos sobrepostos no mesmo
dia, nenhum bloco, sem nome → 422; flexível sem período ou sem duração, duração que não cabe nos
blocos de um dia, plataformas fixas que passam da duração → 422; período ou duração num fixo → 422;
marcar um inactivo como base, ou com `desde` passado → 422; desactivar o base de hoje ou o agendado → 409.

**Datas de efeito.** Um horário que **já vigorou** num dia passado (de um colaborador, de uma unidade
ou como base) não muda de blocos, controlo, aferição ou duração → 409; só o nome. Duplica-se e
atribui-se o novo a partir de uma data. O base e o horário da unidade mudam de hoje (ou de uma data
futura); os dias passados ficam com o que vigorava. O `isBase` da resposta é o de hoje; o primeiro base
da instituição vale desde sempre.

**O `PUT` não apaga o que não recebe.** O período e a duração limpam-se em branco; passar a `FIXO`
sem os enviar limpa-os. Os blocos, quando vêm, substituem os anteriores por inteiro. Desactivar não
retira o horário a quem já o tem.

**O horário da unidade orgânica** (V57): `POST`/`PUT /api/v1/rh/estrutura/organizational-units`
aceitam `horarioId`, opcional. Nulo quer dizer «o da unidade-mãe»; uma unidade sem horário nem na
cadeia segue o horário base. Só se valida quando vem — um horário que exista e esteja activo, senão
422 —, o `PUT` que o omita mantém-no, e `""` limpa-o. **Tem data de efeito:** vale a partir de hoje,
ou de `horarioDesde` (campo novo, opcional, `yyyy-MM-dd`, hoje ou futura; passada → 422), e os dias
passados ficam com o que vigorava. A resposta devolve o horário **de hoje** (uma mudança agendada só
aparece na data).

Regras: BR-HOR-01 a BR-HOR-07, BR-HOR-11 a BR-HOR-14.

---

## 10. Self-service — `/api/v1/rh/me`

O trabalhador autenticado acede aos seus próprios dados (perfil derivado do Lugar corrente).

| Método | Path | Descrição |
|---|---|---|
| `GET` | `/me/profile` | Perfil (unidade/cargo/carreira do Lugar; escalão da afectação; mobilidade em vigor). |
| `GET` · `POST` | `/me/leave-requests` | Pedidos de ausência próprios (`?status=&leaveTypeId=&year=`); submeter com as regras do RH. |
| `PUT` | `/me/leave-requests/{id}/cancel` | Cancelar o seu, só enquanto `PENDENTE`. |
| `GET` | `/me/leave-balances` | Saldos. |
| `GET` · `POST` | `/me/leaves-mobilities` · `GET /me/leaves-mobilities/{id}` | Licenças e mobilidades próprias; pedir só nos subtipos `canSelfSubmit`. |
| `GET` | `/me/ferias/{ano}` · `PUT /me/ferias/{ano}/preferencia` | As minhas férias e a preferência (§6.6). |
| `GET` | `/me/assiduidade?de=&ate=` | A minha assiduidade (§6.8). |
| `POST` | `/me/marcacoes` · `/me/marcacoes/correcoes` | Picar (só teletrabalho/misto) e pedir correcção (§6.8). |
| `GET` · `POST` | `/me/trabalho-suplementar` | O meu trabalho suplementar do mês e pedir (§6.10). |
| `GET` | `/me/payroll-slips` · `/me/payroll-slips/{id}/download` | Recibos. |
| `GET` | `/me/documents` · `/me/documents/{id}/download` | Documentos. |

**A caixa da chefia directa** (titular do Lugar-pai), em `/me/equipa`:

| Método | Path | Descrição |
|---|---|---|
| `GET` | `/me/equipa/pedidos-ausencia-pendentes` | Pedidos por decidir (§6.2f). |
| `PATCH` | `/me/equipa/pedidos-ausencia/{id}/aprovar` · `…/rejeitar` | Decidir. |
| `GET` | `/me/equipa/marcacoes-pendentes` | Correcções por validar (§6.8). |
| `PATCH` | `/me/equipa/marcacoes/{id}/validar` · `…/rejeitar` | Decidir. |
| `GET` | `/me/equipa/trabalho-suplementar-pendente` | Trabalho suplementar por autorizar (§6.10). |
| `POST` | `/me/equipa/trabalho-suplementar` | Lançar para alguém da equipa. |
| `PATCH` | `/me/equipa/trabalho-suplementar/{id}/autorizar` · `…/recusar` | Decidir. |
| `GET` | `/me/equipa/ferias/{ano}` | Férias da equipa (§6.6). |

Quem é «eu»: em produção, o utilizador do JWT ligado ao funcionário pelo perfil IAM; em desenvolvimento, o cabeçalho
`X-Employee-Id`. Um colaborador inactivo recebe **403** (BR-ME-02).

**`POST /me/leave-requests` segue as regras do pedido do RH** (§6.2 a §6.2e): conta os dias pela
linha do catálogo (dias úteis ou seguidos) com os feriados do período, aplica os tectos, recusa
sobreposição com **409** (antes 400) e aceita, opcionalmente, `startTime`/`endTime` (`HH:mm`) para um
pedido em horas — a amamentação, por exemplo. Antes contava dias de calendário. Regra: BR-ME-03.

---

## 11. Auditoria — `/.../audit/{catalog}/{entityId}`

Cada módulo expõe um controller de auditoria (Envers):

| Módulo | Base | Catálogos aceites |
|---|---|---|
| Colaboradores | `/api/v1/rh/colaboradores/audit` | `funcionarios`, **`assignments`**, `contratos`, `dependentes`, `qualificacoes` |
| Estrutura | `/api/v1/rh/estrutura/audit` | `organizational-units`, `jobs`, `functions` |
| Carreiras | `/api/v1/rh/carreiras/audit` | `careers`, `categories`, `grades` |
| Catálogos | `/api/v1/rh/catalogs/audit` | `reference-options`, `worker-states`, `vinculos-laborais`, `contract-types`, `document-types`, `leave-types`, `leave-mobility-subtypes`, `public-holidays` |

> Nota: o catálogo antigo `enquadramentos` foi substituído por **`assignments`** (devolve `400` se usado).

---

## 11b. Tarefas agendadas (jobs) — `/api/v1/rh/schedulers`

Os trabalhos que correm sozinhos — hoje o **vencimento do direito a férias** (6.3) e os **efeitos das licenças e mobilidades** (7.0) — passaram a ter memória: cada execução fica registada (quando correu, quanto tempo levou, o que fez, porque falhou), o agendamento muda-se sem reiniciar a aplicação, e qualquer um pode ser disparado à mão. É o mesmo desenho do `inss_core_service`.

| Chave | Tarefa | Por omissão | Parâmetros do disparo manual |
|---|---|---|---|
| `RH_VENCIMENTO_FERIAS` | Vencimento do direito a férias | todos os dias às 00:05 · 3 tentativas automáticas | `ano` — se vazio, o ano do agendamento |
| `RH_EFEITOS_LICENCAS` | Efeitos das licenças e mobilidades | todos os dias às 00:15 | `data` (`aaaa-MM-dd` ou `dd/MM/aaaa`) — se vazio, o dia do agendamento |

| Método | Caminho | O que faz |
|---|---|---|
| `GET` | `/api/v1/rh/schedulers` | lista a configuração de todos (`nome`, `frequencia`, paginação) |
| `GET` | `/api/v1/rh/schedulers/{chave}` | configuração de um: frequência, hora, `activo`, `ultimaExecucao`, `proximaExecucao` e os `parametros` que aceita — a interface gera o formulário a partir desta lista |
| `PUT` | `/api/v1/rh/schedulers/{chave}` | muda o agendamento: `frequencia` (`DIARIO`, `SEMANAL`, `QUINZENAL`, `MENSAL`, `TRIMESTRAL`, `SEMESTRAL`, `ANUAL`) + os campos que ela pede (`hora`, `minuto`, `diaDaSemana`, `diaDoMes` 1–28, `mes`) e `timezone` opcional |
| `PATCH` | `/api/v1/rh/schedulers/{chave}/activo` | `{ "activo": false }` suspende sem perder a configuração |
| `POST` | `/api/v1/rh/schedulers/{chave}/executar` | dispara já, em segundo plano → **202** com o `id` da execução. Corpo opcional: `{ "parametros": { "data": "2026-09-20" } }` |
| `POST` | `/api/v1/rh/schedulers/execucoes/{id}/reexecutar` | repete uma execução **com o mesmo instante e os mesmos parâmetros** → 202 |
| `GET` | `/api/v1/rh/schedulers/{chave}/execucoes` | histórico, mais recentes primeiro (`estado`, `disparo`, `referencia`, paginação) |
| `GET` | `/api/v1/rh/schedulers/execucoes/{id}` | detalhe de uma execução, com os itens que falharam em `detalhes.itensFalhados` |

**Estados de uma execução.** `A_CORRER` · `SUCESSO` · `FALHA_PARCIAL` (terminou, mas houve itens com erro — os que falharam vêm nos `detalhes`) · `FALHA` (rebentou) · `TIMEOUT` (excedeu o tempo limite, 30 min por omissão) · `OMITIDA` (devia ter corrido e não correu — a aplicação estava parada à hora prevista; é uma linha criada pelo sistema, para que a falta se veja na mesma lista). Só `FALHA` e `TIMEOUT` têm repetição automática (5, 15 e 45 min depois, até ao número de tentativas do job); `FALHA_PARCIAL` não — os mesmos itens voltariam a falhar, e corrige-se o dado e repete-se à mão.

**Executar ou repetir?** O `executar` trata **hoje** (ou o que se indicar nos parâmetros). O `reexecutar` trata o que a execução original tratava: repetir a 2 de Janeiro a execução de 31 de Dezembro que falhou vence as férias do ano que acabou, não as do novo. Serve também uma `OMITIDA`.

**Várias réplicas.** O serviço corre com duas; o cron dispara em ambas, mas cada disparo **corre uma só vez** — o registo da execução bloqueia a linha do job antes de verificar. Uma alteração de agendamento feita numa réplica chega às outras em até 5 minutos.

**Erros.** `400` parâmetro desconhecido, em falta ou inválido (antes de a execução abrir) ou agendamento fora do intervalo · `404` tarefa ou execução inexistente · `409` a tarefa já está a correr.

> As variáveis `RH_FERIAS_VENCIMENTO_CRON` e `RH_LICENCAS_EFEITOS_CRON` passaram a valer **só no primeiro arranque**, para semear o agendamento. Depois disso manda o que está na base (`t_scheduler_job`) e muda-se pelo `PUT`. Os cinco jobs do `sigdi` continuam como estavam (`@Scheduled`, propriedades `sigdi.*`).

---

## 12. Enumerações

Os que estão marcados **validado** são enums fechados no domínio: um valor fora da lista devolve **422**, em vez de ser gravado tal e qual.

| Enum | Valores | |
|---|---|---|
| `origem` (afectação) | `ADMISSAO`, `REINGRESSO`, `PROGRESSAO`, `PROMOCAO`, `TRANSFERENCIA`, `MUDANCA_CARREIRA`, `SUBSTITUICAO`, `CONSOLIDACAO` (e o legado `MOBILIDADE`) — posta pelo sistema | |
| `assignmentType` | `PRINCIPAL`, `SUBSTITUICAO` — omisso vale `PRINCIPAL`. Em `POST /assignments` **só `PRINCIPAL` passa**: a `SUBSTITUICAO` cria-se pelo endpoint próprio (5.7) e dá 422 aqui. `ACUMULACAO` deixou de existir (art. 134.º n.º 2 al. b) é forma de prestação da mobilidade, não título). | **validado** |
| `estado` (Lugar) | `ATIVO`, `CONGELADO`, `EXTINTO` (provido/vago é **derivado do titular**) | |
| `recordType` (subtipo licença/mobilidade) | `LICENCA`, `MOBILIDADE` — o valor **`AMBOS` foi removido na V43** | **validado** |
| `situacaoFuncional` (estado do trabalhador) | `ACTIVIDADE_NO_QUADRO`, `ACTIVIDADE_FORA_QUADRO`, `INACTIVIDADE_NO_QUADRO`, `INACTIVIDADE_FORA_QUADRO`, `DISPONIBILIDADE`, `APOSENTACAO` — pode ser nulo | **validado** |
| `positionEffect` (subtipo) | `MANTEM`, `ABRE_VAGA` | **validado** |
| `returnEffect` (subtipo) | `REGRESSA_LUGAR`, `DISPONIBILIDADE`, `REGRESSA_OU_CESSA` | **validado** |
| `status` (contrato) | `ATIVO`, `SUSPENSO`, `CESSADO` — obrigatório desde a V44 | **validado** |
| `estado` (pedido de ausência) | `PENDENTE`, `APROVADO`, `REJEITADO`, `CANCELADO` | **validado** |
| `status` · `estadoPeriodo` (licença/mobilidade) | `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED` · `POR_INICIAR`, `EM_CURSO`, `TERMINADA` | |
| `formaPrestacao` (mobilidade) | `TEMPO_INTEIRO`, `ACUMULACAO` | **validado** |
| `regime` (tipo de ausência) | `FERIAS`, `FALTA`, `FALTA_INJUSTIFICADA` | **validado** |
| `contagem` (tipo de ausência) | `DIAS_UTEIS`, `DIAS_SEGUIDOS` | **validado** |
| `efeitoRemuneracao` (tipo de ausência) | `SEM_PERDA`, `PERDA_PARCIAL`, `PERDA_TOTAL`, `PERDA_VENCIMENTO_EXERCICIO`, `DEPENDE_DA_OPCAO` | **validado** |
| `opcaoFaltaInjustificada` (pedido) | `PERDA_REMUNERACAO`, `DESCONTO_FERIAS` | **validado** |
| `regimeTrabalho` (contrato) | `TEMPO_COMPLETO`, `TEMPO_PARCIAL`, `ISENCAO_HORARIO`, `DEDICACAO_EXCLUSIVA` | **validado** |
| `origem` · `motivoAlteracao` (mapa de férias) | `ACORDO`, `FIXADA` · `ACORDO`, `CONVENIENCIA_SERVICO` | **validado** |
| `preferenciaIndicadaPor` (férias) | `PROPRIO`, `RH` | |
| `controlo` · `periodoAfericao` (horário) | `FIXO`, `FLEXIVEL` · `SEMANA`, `MES` | **validado** |
| `regimePrestacao` (horário do colaborador) | `PRESENCIAL`, `TELETRABALHO`, `MISTO` | **validado** |
| `origem` (horário vigente) | `COLABORADOR`, `UNIDADE`, `BASE`, `NENHUM` | |
| `sentido` · `origem` · `estado` (marcação) | `ENTRADA`, `SAIDA` · `MANUAL`, `IMPORTADO`, `PROPRIO` · `VALIDA`, `PENDENTE`, `REJEITADA` | **validado** |
| `anomalias` (dia) | `ENTRADA_SEM_SAIDA`, `SAIDA_SEM_ENTRADA`, `ENTRADAS_SEGUIDAS` | |
| `estado` · `motivo` (dia apurado) | `COM_FALTA`, `SEM_FALTA`, `POR_CORRIGIR`, `POR_VALIDAR`, `FUTURO`, `FORA_DO_VINCULO`, `ISENTO`, `FERIADO`, `AUSENCIA_JUSTIFICADA`, `LICENCA`, `MOBILIDADE_EXTERNA`, `MISSAO_SERVICO`, `FORMACAO`, `SUSPENSAO_DISCIPLINAR`, `ACIDENTE_SERVICO`, `SEM_HORARIO`, `DESCANSO` · `SEM_REGISTO`, `INCOMPLETO`, `PLATAFORMA` | |
| `estado` · `tipoDia` (trabalho suplementar) | `PEDIDO`, `AUTORIZADO`, `RECUSADO`, `CANCELADO` · `DIA_UTIL`, `DESCANSO`, `FERIADO` | |
| `estado` (linha da relação mensal) | `COMPLETA`, `COM_PENDENCIAS` | |
| `estado` (chefe / responsável) | `PROVIDO`, `CHEFIA_VAGA`, `SEM_CHEFIA_DEFINIDA` | |

---

## 13. Endpoints deprecados / removidos

| Antigo | Substituto |
|---|---|
| `POST/GET /funcionarios/{id}/enquadramentos` | Afectação (`/colaboradores/assignments`) |
| `POST/GET /funcionarios/{id}/colocacoes` | Afectação (`/colaboradores/assignments`) |
| Registo com `unidade/cargo/carreira/categoria` | Registo com `positionId` (deriva do Lugar) |
| catálogo audit `enquadramentos` | catálogo audit `assignments` |

Ver `breaking_change_frontend.md` para o guia de migração do frontend.

<!-- secao:notificacoes -->
## 14. Notificações — `/api/v1/rh/me/notificacoes` e `/api/v1/rh/notificacoes`

Avisos na aplicação, escritos pelo próprio sistema quando acontece algo que alguém tem de saber (BR-NOT-01..06).
O front **só lê e marca lidas**; não há POST — quem cria é o código. O envio por correio electrónico ainda não existe
(fica numa fila, *TODO(smtp)*).

| Método | Caminho | O quê |
|---|---|---|
| GET | `/api/v1/rh/me/notificacoes?naoLidas=&page=&size=` | As minhas, das mais recentes para as mais antigas (`WrapperListaNotificacoesDTO`, com `naoLidas`) |
| GET | `/api/v1/rh/me/notificacoes/contagem` | Quantas estão por ler (o número no sino) |
| PATCH | `/api/v1/rh/me/notificacoes/{notificacaoId}/lida` | Marcar lida uma minha (404 se for de outra pessoa) |
| PATCH | `/api/v1/rh/me/notificacoes/lidas` | Marcar lidas todas as minhas; devolve quantas mudaram |
| GET | `/api/v1/rh/notificacoes?perfil=RH&naoLidas=&page=&size=` | A caixa partilhada do RH |
| PATCH | `/api/v1/rh/notificacoes/{notificacaoId}/lida?perfil=RH` | Marcar lida uma da caixa do RH (fica lida para todo o RH) |

`NotificacaoDTO`: `id`, `tipo` (ex.: `PEDIDO_AUSENCIA_PENDENTE`, `PEDIDO_AUSENCIA_DECIDIDO` — para o ícone), `titulo`, `texto`,
`recursoTipo` + `recursoId` (ex.: `PEDIDO_AUSENCIA` e o id do pedido — para abrir o ecrã certo), `perfil` (nulo nas pessoais),
`criadaEm`, `lidaEm`, `lida`.

**Quem recebe o quê (hoje):** pedido de ausência por decidir → a chefia directa (sem chefia, a caixa do RH); pedido decidido →
quem pediu. Cada frente nova acrescenta os seus avisos (ver as secções seguintes).

**Para programadores:** notificar é uma linha, de qualquer módulo —
`notificador.para(funcionarioId).tipo(TipoNotificacao.X).titulo("…").texto("…").recurso("TIPO", id).enviar()`;
`notificador.paraRh()…`; `notificador.para(chefiaService.chefeDirecto(id))…` (sem chefia não faz nada). Um tipo novo
acrescenta-se ao enum `TipoNotificacao`.
<!-- /secao:notificacoes -->

<!-- secao:factos-salariais -->
## 15. Fronteira salarial — `/api/v1/rh/salarial`

O processamento de remunerações e descontos é de outra aplicação. Este serviço entrega-lhe **os factos do RH** (BR-FAC-01..05),
sem valores. Contrato de leitura **versão 1** (`versao` na resposta).

| Método | Caminho | O quê |
|---|---|---|
| GET | `/api/v1/rh/salarial/factos?mes=yyyy-MM` | Os factos que entram nesse mês de processamento, pela ordem em que foram registados (`FactosSalariaisDTO`) |
| GET | `/api/v1/rh/salarial/factos.csv?mes=yyyy-MM` | O mesmo em CSV (`;`, UTF-8 com BOM; `dados` como `chave=valor, …`) |
| GET | `/api/v1/rh/salarial/funcionarios/{funcionarioId}/factos` | Os factos de um colaborador, do mais recente para o mais antigo |

Cada `FactoRhDTO`: `tipo` (ADMISSAO, REINGRESSO, PROGRESSAO, PROMOCAO, TRANSFERENCIA, MUDANCA_CARREIRA, CONSOLIDACAO_MOBILIDADE,
MUDANCA_SITUACAO, CESSACAO, …), `dataEfeito`, `mesCompetencia`, `ajusteDeMesAnterior` (a data é de um mês já fechado),
`numeroFuncionario`, `nif`, `nome`, `descricao` (legível), `referenciaTipo` + `referenciaId` (o acto: `AFECTACAO`, `WORKER_STATE`, …)
e `dados` (ex.: `lugarId`, `escalaoId`, `funcaoId`, `origem`; `estado`, `situacaoFuncional`, `motivo`).

O diário **só cresce**: uma correcção aparece como um facto novo. O fecho do mês e a exportação acompanham-no (secção de fecho mensal).
<!-- /secao:factos-salariais -->

<!-- secao:aposentacao -->
## 16. Aposentação e limite de idade — `/api/v1/rh/funcionarios/{id}/aposentacao`

Lei n.º 20/X/2023, arts. 48.º e 173.º–179.º (BR-APO-01..12). O vínculo cessa aos **65 anos** (até aos **70** com prorrogação
autorizada); a antecipada a pedido exige **34 anos de serviço**; a pré-aposentação, **58 anos e 30 de serviço**.

| Método | Caminho | O quê |
|---|---|---|
| GET | `/api/v1/rh/funcionarios/{funcionarioId}/aposentacao` | Situação: `faz65`, `faz70`, `prorrogadoAte`, `limiteEfectivo`, tempo de serviço, `completa34Anos`, `preAposentacaoPossivel`, processos e prorrogações |
| POST | `/api/v1/rh/funcionarios/{funcionarioId}/aposentacao/processos` | Abrir um processo (`modalidade`, `dataPrevista`, `fundamentacao`, `acordoFuncionario`) |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/aposentacao/processos/{processoId}/deferir` | `despachoNumero`, `despachoData`, `dataPrevista` |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/aposentacao/processos/{processoId}/indeferir` | `motivo` |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/aposentacao/processos/{processoId}/desligar` | `data`; na pré-aposentação `percentagemPrestacao` (70–80); `workerStateId` opcional |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/aposentacao/processos/{processoId}/concluir` | `data` da aposentação; `workerStateId` opcional (por omissão, o estado de aposentação do catálogo) |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/aposentacao/processos/{processoId}/cancelar` | `motivo` (só antes da desligação) |
| POST | `/api/v1/rh/funcionarios/{funcionarioId}/aposentacao/prorrogacoes` | `manifestacaoVontade`, `propostaFundamentada`, `validaAte` |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/aposentacao/prorrogacoes/{prorrogacaoId}/autorizar` | `despachoNumero`, `despachoData` |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/aposentacao/prorrogacoes/{prorrogacaoId}/indeferir` | `motivo` |
| GET | `/api/v1/rh/relatorios/aposentacao?unidadeId=&incluirSubunidades=&ate=` | Quem atinge o limite, os 34 anos ou a pré-aposentação até `ate` (por omissão, 12 meses) (e `.csv`) |
| GET | `/api/v1/rh/me/aposentacao` | A minha situação |
| POST | `/api/v1/rh/me/aposentacao/processos` | Pedir a antecipada ou a pré-aposentação (o próprio) |

**Ciclo:** `PEDIDO` → `DEFERIDO` (despacho) → `DESLIGADO` (inactividade no quadro aguardando aposentação; na pré-aposentação, o início
dela) → `CONCLUIDO` (a cessação: contrato cessado, afectação encerrada, estado de aposentação, facto `APOSENTACAO`). `INDEFERIDO` e
`CANCELADO` terminam-no. Na **antecipada** o Lugar deixado fica **extinto** (art. 178.º). Erros: 422 (condição por cumprir — a mensagem
diz a data em que se cumpre; despacho ou motivo em falta; prestação fora de 70–80%), 409 (processo em curso; passo fora de ordem).

**Avisos:** o job `RH_ALERTA_APOSENTACAO` avisa o RH 180, 90 e 30 dias antes do limite e no dia; o colaborador, aos 180 e no dia.
<!-- /secao:aposentacao -->

<!-- secao:declaracoes -->
## 17. Declarações e documentos emitidos — `/api/v1/rh/funcionarios/{id}/declaracoes`

BR-DEC-01..08. O PDF é gerado no acto de emitir e fica no **MinIO**; descarrega-se por link assinado (`FileUrlDTO.url`, temporário).
Modelos **de teste** até a instituição aprovar os oficiais.

| Método | Caminho | O quê |
|---|---|---|
| POST | `/api/v1/rh/funcionarios/{funcionarioId}/declaracoes` | O RH emite logo (`tipo`, `finalidade`) → `PedidoDeclaracaoDTO` com o `documento` |
| GET | `/api/v1/rh/funcionarios/{funcionarioId}/declaracoes` | Os pedidos do colaborador |
| GET | `/api/v1/rh/declaracoes/por-emitir` | A caixa do RH: pedidos do próprio por emitir |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/declaracoes/{pedidoId}/emitir` | Emitir um pedido |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/declaracoes/{pedidoId}/recusar` | Recusar (`motivo`) |
| GET | `/api/v1/rh/funcionarios/{funcionarioId}/documentos-emitidos` | Os documentos emitidos para o colaborador |
| GET | `/api/v1/rh/documentos-emitidos/{documentoId}/link` | Link assinado para o PDF |
| PATCH | `/api/v1/rh/documentos-emitidos/{documentoId}/anular` | Anular (`motivo`) |
| GET | `/api/v1/rh/verificacao/documentos/{codigo}` | **Pública** (sem autenticação): `valido`, `anulado`, `tipo`, `numero`, `emitidoEm`, `titular` |
| POST | `/api/v1/rh/me/declaracoes` | O próprio pede (`tipo`, `finalidade`) |
| GET | `/api/v1/rh/me/declaracoes` | Os meus pedidos |
| GET | `/api/v1/rh/me/documentos-emitidos` | Os meus documentos |
| GET | `/api/v1/rh/me/documentos-emitidos/{documentoId}/link` | Link para um documento meu (404 se não for meu) |

**Tipos:** `VINCULO`, `TEMPO_SERVICO`, `ANTIGUIDADE_CATEGORIA`, `SITUACAO_FUNCIONAL`. **Numeração** `DEC-2026-000001` (sem repetidos
nem saltos por erro). **Código de verificação** impresso no rodapé do PDF, com o endereço da verificação.

**Pasta por omissão nos ficheiros (`/documento/private/{folder}`):** sem pasta, ou com uma pasta que não existe, o ficheiro vai para
`outros` (`DocumentoFolder.OUTROS`); as declarações vão para `documentos_emitidos`.
<!-- /secao:declaracoes -->

<!-- secao:lista-antiguidade-ciclo -->
## 18. Lista de antiguidade oficial — `/api/v1/rh/listas-antiguidade`

DL n.º 3/2010, arts. 71.º–74.º (BR-LAN-06..15). A lista **gerada** continua em `GET /relatorios/lista-antiguidade` (§6.12); a **oficial**
congela-a e segue o ciclo: `APROVADA` → `AFIXADA` → `DEFINITIVA` → `PUBLICADA` (ou `ANULADA`).

| Método | Caminho | O quê |
|---|---|---|
| POST | `/api/v1/rh/listas-antiguidade` | Aprovar e congelar (`ano`, `unidadeId`, `incluirSubunidades`, `aprovadaPor`, `dataAprovacao`) |
| GET | `/api/v1/rh/listas-antiguidade?ano=&unidadeId=` | As listas (cabeçalho) |
| GET | `/api/v1/rh/listas-antiguidade/{listaId}` | Uma lista com as linhas e as reclamações |
| PATCH | `/api/v1/rh/listas-antiguidade/{listaId}/afixar` | `data`, `local` — abre o prazo e avisa cada pessoa da lista |
| PATCH | `/api/v1/rh/listas-antiguidade/{listaId}/recalcular` | Voltar a gerar as linhas (antes de definitiva) |
| PATCH | `/api/v1/rh/listas-antiguidade/{listaId}/definitiva` | Depois do prazo e sem reclamações por decidir |
| PATCH | `/api/v1/rh/listas-antiguidade/{listaId}/publicar` | `serie`, `numero`, `data` do Boletim Oficial (aviso se depois de 30/4) |
| PATCH | `/api/v1/rh/listas-antiguidade/{listaId}/anular` | `motivo` |
| POST | `/api/v1/rh/listas-antiguidade/{listaId}/reclamacoes` | O RH regista (`funcionarioId`, `fundamento`, `texto`, `noEstrangeiro`) |
| PATCH | `/api/v1/rh/listas-antiguidade/{listaId}/reclamacoes/{reclamacaoId}/decidir` | `deferida`, `decisao` |
| PATCH | `/api/v1/rh/listas-antiguidade/{listaId}/reclamacoes/{reclamacaoId}/recurso` | `texto` (20 dias; 60 no estrangeiro) |
| PATCH | `/api/v1/rh/listas-antiguidade/{listaId}/reclamacoes/{reclamacaoId}/recurso/decidir` | `provido`, `decisao` |
| GET | `/api/v1/rh/me/listas-antiguidade` | As listas onde apareço (só a minha linha e as minhas reclamações) |
| POST | `/api/v1/rh/me/listas-antiguidade/{listaId}/reclamacoes` | Reclamar (o próprio) |

**Prazos:** reclamação 30 dias depois da afixação (`fimPrazoReclamacao`), 60 no estrangeiro (`fimPrazoReclamacaoEstrangeiro`); decisão
notificada em 30 dias (aviso se passar); recurso 20 dias depois da decisão. **Fundamentos:** `OMISSAO` (o único de quem não consta),
`GRADUACAO`, `SITUACAO`, `CONTAGEM`. As respostas das decisões e da publicação trazem `alertas` quando um prazo da lei foi ultrapassado.
<!-- /secao:lista-antiguidade-ciclo -->

<!-- secao:publicacoes -->
## 19. Publicações no Boletim Oficial — `/api/v1/rh/publicacoes`

Lei n.º 20/X/2023, arts. 89.º–90.º (BR-PUB-01..06). A caixa do RH com os actos a publicar: os previstos na lei **nascem sozinhos**
dos actos do RH (admissão, promoção, mudança de carreira, transferência, consolidação, comissão, cessação); os outros criam-se à mão.

| Método | Caminho | O quê |
|---|---|---|
| GET | `/api/v1/rh/publicacoes?estado=A_PUBLICAR` | Os actos (por estado; por omissão, todos) |
| POST | `/api/v1/rh/publicacoes` | Criar à mão (`tipoActo`, `meio`, `funcionarioId`, `sumario`, `dataActo`) |
| POST | `/api/v1/rh/publicacoes/{publicacaoId}/extracto` | Gerar o extracto em PDF (fica em `extractoId`; descarrega-se por `/documentos-emitidos/{id}/link`) |
| PATCH | `/api/v1/rh/publicacoes/{publicacaoId}/publicada` | Registar a publicação (`serie`, `numero`, `dataPublicacao`) |
| PATCH | `/api/v1/rh/publicacoes/{publicacaoId}/cancelar` | Cancelar (`motivo`) |
| GET | `/api/v1/rh/funcionarios/{funcionarioId}/publicacoes` | Os actos de um colaborador |
<!-- /secao:publicacoes -->

<!-- secao:cartao-profissional -->
## 20. Cartão de identificação profissional — `/api/v1/rh/funcionarios/{id}/cartao-profissional`

Lei n.º 20/X/2023, art. 25.º (BR-CID-01..06). PDF em formato de cartão (modelo de teste) no MinIO; descarrega-se pelo `documentoId`
em `/documentos-emitidos/{id}/link`.

| Método | Caminho | O quê |
|---|---|---|
| POST | `/api/v1/rh/funcionarios/{funcionarioId}/cartao-profissional` | Emitir (substitui e anula o que estiver em uso) |
| GET | `/api/v1/rh/funcionarios/{funcionarioId}/cartao-profissional` | Os cartões, cada um com `valido` e `motivoInvalidade` |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/cartao-profissional/{cartaoId}/entregar` | `data` da entrega (recepção atestada) |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/cartao-profissional/{cartaoId}/devolver` | `data` da devolução |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/cartao-profissional/{cartaoId}/anular` | `motivo` |
| GET | `/api/v1/rh/me/cartao-profissional` | Os meus cartões |
<!-- /secao:cartao-profissional -->

<!-- secao:entrada-servico -->
## 21. Entrada ao serviço — `/api/v1/rh/funcionarios/{id}/provimentos`

Lei n.º 20/X/2023, arts. 52.º–81.º (BR-PRV-01..14). Depois do registo, do contrato e da colocação, o RH regista o **provimento**:
a forma de vínculo, o despacho e a **posse**. A nomeação provisória e o contrato de estágio abrem o **estágio probatório** (1 ano,
tutor); os contratos a termo, o **período experimental** (60 ou 30 dias).

| Método | Caminho | O quê |
|---|---|---|
| POST | `/api/v1/rh/funcionarios/{funcionarioId}/provimentos` | `modalidade`, `despachoNumero`, `despachoData`, `dataPosse`, `concursoRef`, `vemDeOutraCarreira`, `tutorId`, `mesesPrevistos` |
| GET | `/api/v1/rh/funcionarios/{funcionarioId}/provimentos` | Os provimentos e os períodos de prova (`EntradaServicoDTO`) |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/periodos-prova/{periodoId}/concluir` | Decidir no fim (`avaliacao`, `fundamentacao`, `data`; `workerStateId` se sem sucesso) |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/periodos-prova/{periodoId}/cessar` | Cessação antecipada fundamentada |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/periodos-prova/{periodoId}/denunciar` | Denúncia pelo agente (período experimental) |
| GET | `/api/v1/rh/me/tutorias` | Os estágios de que sou tutor |
| PATCH | `/api/v1/rh/me/tutorias/{periodoId}/relatorio` | O tutor remete o relatório (`avaliacao`, `fundamentacao`, `data`) |

**Com sucesso:** nasce o provimento seguinte (nomeação definitiva / contrato por tempo indeterminado) e o facto `PROVIMENTO` (daí a
publicação). **Sem sucesso:** a cessação do vínculo na data do fim (exoneração obrigatória; cessação do contrato), salvo quem vem de
outra carreira, que regressa. As respostas trazem `alertas` com o que o RH ainda tem de fazer.
<!-- /secao:entrada-servico -->

<!-- secao:concurso -->
## 22. Concursos — `/api/v1/rh/concursos`

Lei n.º 20/X/2023, arts. 123.º–129.º (BR-CNC-01..20). O RH cria o concurso em rascunho, define os Lugares, os métodos, o júri e o
prazo, e abre-o. As candidaturas são registadas pelo RH. Depois: apreciar (admitir ou propor a exclusão, com audiência), lançar as
notas, publicar a lista provisória, homologar e prover pela ordem da lista.

| Método | Caminho | O quê |
|---|---|---|
| POST | `/api/v1/rh/concursos` | Criar em rascunho (`referencia`, `finalidade`, `tipo`, `modalidade`, `vinculo`, `categoriaId`, `lugares`, `metodos`, `juri`, `candidaturasDe`, `candidaturasAte`, `quotaDeficiencia`…) |
| PUT | `/api/v1/rh/concursos/{concursoId}` | Alterar (só em rascunho; listas omitidas ficam como estão) |
| GET | `/api/v1/rh/concursos` | Lista (`estado` opcional) |
| GET | `/api/v1/rh/concursos/{concursoId}` | O concurso com as candidaturas |
| PATCH | `/api/v1/rh/concursos/{concursoId}/abrir` | Abrir (publicar o aviso) |
| PATCH | `/api/v1/rh/concursos/{concursoId}/encerrar` | Encerrar as candidaturas (depois do prazo) |
| PATCH | `/api/v1/rh/concursos/{concursoId}/avaliar` | Passar à avaliação |
| PATCH | `/api/v1/rh/concursos/{concursoId}/lista-provisoria` | Classificar e publicar a lista provisória |
| PATCH | `/api/v1/rh/concursos/{concursoId}/homologar` | Homologar (`despacho`, `data`) |
| PATCH | `/api/v1/rh/concursos/{concursoId}/concluir` | Concluir |
| PATCH | `/api/v1/rh/concursos/{concursoId}/anular` | Anular (`motivo`) |
| POST | `/api/v1/rh/concursos/{concursoId}/candidaturas` | Registar uma candidatura (`nome`, `documento`… ou `funcionarioId`; `deficiencia`, `vinculadoAdministracao`) |
| PATCH | `/api/v1/rh/concursos/{concursoId}/candidaturas/{candidaturaId}/admitir` | Admitir |
| PATCH | `/api/v1/rh/concursos/{concursoId}/candidaturas/{candidaturaId}/excluir` | Propor a exclusão (`motivo`; abre a audiência) |
| PATCH | `/api/v1/rh/concursos/{concursoId}/candidaturas/{candidaturaId}/audiencia` | Decidir a audiência (`excluir`, `resposta`) |
| PATCH | `/api/v1/rh/concursos/{concursoId}/candidaturas/{candidaturaId}/nota` | Lançar uma nota (`metodo`, `nota`) |
| PATCH | `/api/v1/rh/concursos/{concursoId}/candidaturas/{candidaturaId}/prover` | Prover num Lugar (`lugarId`) |
| PATCH | `/api/v1/rh/concursos/{concursoId}/candidaturas/{candidaturaId}/desistir` | Registar a desistência |
| GET | `/api/v1/rh/me/candidaturas` | As minhas candidaturas |

Valores: `finalidade` ∈ INGRESSO · ACESSO; `tipo` ∈ COMUM · ESPECIAL; `modalidade` ∈ EXTERNO · INTERNO · INTERNO_RESTRITO;
`metodo` ∈ TRIAGEM_CURRICULAR · PROVA_CONHECIMENTOS · AVALIACAO_COMPETENCIAS · ENTREVISTA · CURSO_FORMACAO · PROVAS_FISICAS;
papel no júri ∈ PRESIDENTE · VOGAL · SUPLENTE. O provido entra ao serviço pelo registo, contrato, colocação e provimento (§21),
com `concursoRef` = a referência do concurso.
<!-- /secao:concurso -->

<!-- secao:checklists -->
## 23. Checklists de entrada e de saída — `/api/v1/rh/checklists`

BR-CHK-01..12. A admissão (ou o reingresso) abre a checklist de **entrada**; a cessação, a de **saída** — com os itens do
modelo, cada um com a área responsável e o prazo. O RH, a informática, o património e a chefia vão marcando; o próprio marca os
seus. O provimento e o cartão profissional marcam-se sozinhos.

| Método | Caminho | O quê |
|---|---|---|
| GET | `/api/v1/rh/checklists/modelo` | O modelo (`tipo` opcional) |
| POST | `/api/v1/rh/checklists/modelo` | Acrescentar um item (`tipo`, `codigo`, `descricao`, `responsavel`, `obrigatorio`, `prazoDias`, `ordem`) |
| PUT | `/api/v1/rh/checklists/modelo/{itemId}` | Alterar ou desactivar (`activo`) |
| GET | `/api/v1/rh/checklists` | Lista de trabalho: abertas com pendentes (`tipo`, `responsavel`, `atrasadas`) |
| GET | `/api/v1/rh/funcionarios/{funcionarioId}/checklists` | As do colaborador |
| POST | `/api/v1/rh/funcionarios/{funcionarioId}/checklists` | Abrir à mão (`tipo`, `dataReferencia`) |
| POST | `/api/v1/rh/funcionarios/{funcionarioId}/checklists/{checklistId}/itens` | Acrescentar um item (`descricao`, `responsavel`, `obrigatorio`, `prazo`) |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/checklists/{checklistId}/itens/{itemId}` | Marcar (`estado` ∈ FEITO · NAO_APLICAVEL · PENDENTE, `observacao`, `data`) |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/checklists/{checklistId}/cancelar` | Cancelar (`motivo`) |
| GET | `/api/v1/rh/me/checklists` | As minhas e as da minha equipa com itens da chefia |
| PATCH | `/api/v1/rh/me/checklists/{checklistId}/itens/{itemId}` | Marcar um item meu ou da chefia (403 aos outros) |

Cada checklist traz `pendentes`, `atrasados` e os itens com `atrasado` e `automatico`. Para outro serviço cumprir um item:
`ChecklistService.cumprir(funcionarioId, tipo, codigo, observacao, data)`.
<!-- /secao:checklists -->

<!-- secao:comissao-servico -->
## 24. Comissão de serviço — `/api/v1/rh/comissoes-servico`

Lei n.º 20/X/2023, arts. 59.º, 60.º e 64.º (BR-CMS-01..08). A comissão regista-se como licença/mobilidade do subtipo de comissão
(`POST .../licencas-mobilidade`, depois `approve`); estas rotas tratam do que lhe é próprio.

| Método | Caminho | O quê |
|---|---|---|
| GET | `/api/v1/rh/comissoes-servico` | Em curso, pelo fim (`terminaAte` opcional), com `diasParaTermo` |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/comissoes-servico/{licencaId}/renovar` | Mais 3 anos (`despacho`) |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/comissoes-servico/{licencaId}/cessar` | `iniciativa` ∈ ENTIDADE · NOMEADO · PENA_DISCIPLINAR, `dataAviso`, `dataEfeito`, `motivo` |

Com a entidade ou o nomeado, a data de efeito é no mínimo o aviso + 60 dias (por omissão, esse dia); com a pena disciplinar, sem
aviso. O regresso (ou a cessação da relação, art. 64.º n.º 2) aplica-se no dia de efeito.
<!-- /secao:comissao-servico -->

<!-- secao:processo-disciplinar -->
## 25. Processo disciplinar — tramitação — `/api/v1/rh/funcionarios/{id}/processos-disciplinares`

Estatuto Disciplinar (BR-DIS-03..24). O registo antigo (`POST`/`PUT` do processo) continua como estava. A tramitação começa com
a **participação** e segue por actos, cada um com a sua `data` (por omissão, hoje); a resposta traz a fase, os actos, os
**prazos** que correm e os **alertas**.

| Método | Caminho | O quê |
|---|---|---|
| GET | `/api/v1/rh/processos-disciplinares` | Os processos em curso, com prazos e alertas |
| POST | `/api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/participacoes` | Participação (`especie`, `dataInfraccao`, `factos`, `penaPrevista`, `numero`) |
| GET | `/api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/tramitacao` | O processo com a tramitação |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/instaurar` | `despacho`, `entidade`, `instrutorId`/`instrutorNome` |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/instrutor` | Nomear ou substituir o instrutor |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/iniciar-instrucao` | Início da instrução |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/prorrogar-instrucao` | `dias` (até 30; 15 por omissão) |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/suspensao-preventiva` | `data`, `dias`, `perdaVencimento` |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/levantar-suspensao` | Levantar a suspensão preventiva |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/acusar` | `pena` (aplicável), `texto` |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/notificar-acusacao` | `dias` (prazo de defesa), `complexo` |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/defesa` | `texto` |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/relatorio` | `pena` proposta (vazia = arquivar), `duracao`, `texto` |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/decidir` | `pena` (vazia = arquivar), `duracao`, `entidade`, `texto` (fundamentação) |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/notificar-decisao` | Notificação (efeitos no dia seguinte) |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/recurso` | Recurso hierárquico (15 dias) |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/decidir-recurso` | `resultado` ∈ MANTIDA · DIMINUIDA · ANULADA, `pena`, `duracao` |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/arquivar` | `motivo` |

| GET | `/api/v1/rh/processos-disciplinares/autos-sugeridos` | Autos por falta de assiduidade / abandono de lugar a levantar (BR-DIS-25) |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/reabilitar` | `despacho` (5 anos depois; BO) |
| PATCH | `/api/v1/rh/funcionarios/{funcionarioId}/processos-disciplinares/{processoId}/rever` | `resultado` ∈ REVOGADA · ALTERADA, `pena`, `duracao`, `despacho` |

Na decisão, `suspensaoAnos` (1 a 3) suspende a multa ou a suspensão (art. 34.º); a resposta traz `penaSuspensaAte`. Uma nova
punição durante a suspensão fá-la caducar e a pena executa-se. A pena impede o concurso, a promoção e a nova comissão (BR-DIS-29).

`pena` ∈ CENSURA_ESCRITA · MULTA (dias) · SUSPENSAO (dias) · INACTIVIDADE (meses) · APOSENTACAO_COMPULSIVA · DEMISSAO ·
CESSACAO_COMISSAO. A execução (facto, cessação do vínculo, publicação, cessação da comissão) é automática no dia devido.
<!-- /secao:processo-disciplinar -->
