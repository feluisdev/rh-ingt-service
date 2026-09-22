# RH-Service — Guia de API (v5)

> Fonte de verdade do contrato REST do núcleo RH (exclui o módulo `sigdi`).
> Documentação **v5** — supersede a `v4`. pt-PT.

## 1. Base, autenticação e cabeçalhos

| Item | Valor |
|---|---|
| **Base URL (dev)** | `http://localhost:8091` (ou a porta de `SERVICE_PORT`) |
| **Prefixo comum** | `/api/v1/rh` |
| **Swagger** | `/swagger-ui.html` (quando `ENABLE_SWAGGER=true`) |
| **Autenticação** | `development`/`staging`: **desligada**. `production`: OAuth2 Resource Server + JWT (Keycloak) — enviar `Authorization: Bearer <token>`. |
| **Cabeçalhos** | Enviar sempre `Accept: application/json`. Em `POST/PUT/PATCH`: `Content-Type: application/json`. |

> **Nota:** sem `Accept: application/json`, alguns clientes recebem XML. Enviar sempre o header.

### Contrato legível por máquina

`openapi.json` (nesta pasta) é o contrato **gerado a partir do código** pelo springdoc: caminhos, parâmetros e esquemas dos pedidos e das respostas. Foi extraído com a aplicação a correr:

```bash
curl -s -o docs/funcionarios/v5/openapi.json http://localhost:8099/v3/api-docs
```

Quem implementa um cliente deve **ler o `openapi.json` para as formas** e este guia para o resto, porque há duas coisas que o gerado ainda não diz:

1. **Os erros.** Só 6 das 328 operações declaram respostas 4xx. As regras de negócio e os **422** vivem aqui e no `regras_negocio.html`.
2. **Algumas respostas.** *(resolvido)* As 237 operações não-sigdi têm hoje esquema de resposta declarado.

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

Cobre os `POST` de criação, os `PUT` de actualização, as desactivações (`DELETE`) e as reactivações (`activate`) — 85 operações ao todo.

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
| `PATCH` | `/positions/{id}/freeze` | Congelar (estado `CONGELADO`). |
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
parentPositionId, managesUnitId, estado, legalBase,
isActive, foraDeGrelha, ocupado
```

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
| `POST` | `/assignments` | Afectar um funcionário a um Lugar. |
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
  "origem": "ADMISSAO|PROGRESSAO|PROMOCAO|MOBILIDADE|TRANSFERENCIA",
  "assignmentType": "PRINCIPAL|ACUMULACAO|SUBSTITUICAO",  // default PRINCIPAL
  "dataInicio": "YYYY-MM-DD",
  "notes": "string | null"
}
```

**Regras (422):** Lugar não disponível (CONGELADO/EXTINTO) · Lugar já **tem titular** · Lugar de carreira sem `gradeId` · Lugar fora de grelha com `gradeId` · `assignmentType` fora da lista.

**Uma cadeira, um titular.** A regra do Lugar ocupado só se aplica a `assignmentType = PRINCIPAL`. Quem entra em `SUBSTITUICAO` ou `ACUMULACAO` **não exige que o Lugar esteja vago** e não desaloja o titular — é o que autoriza a substituição do funcionário temporariamente impedido (art. 73.º al. a) a c)). Em consequência, um Lugar com substituto e **sem** titular continua a contar como **vago** em `/unidade/{id}/vagas` e na lista do picker.

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

> Não usar `POST /assignments` com `origem=PROGRESSAO`: falha sempre, porque o Lugar já tem titular — o próprio colaborador.

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

---

## 6. Dados do colaborador (sub-recursos de `/funcionarios/{funcionarioId}`)

Todos seguem o padrão CRUD + (quando aplicável) `documentos`:

| Recurso | Base |
|---|---|
| Contratos | `/funcionarios/{id}/contratos` — + `close`, `suspend`, `activate`, `documentos` (**todos `PUT`**). **`close` = cessação do vínculo** (ver nota abaixo) |
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

### 6.1 Ausências ou licença? — qual dos dois usar

Há dois recursos para uma pessoa se ausentar, e a escolha **não é de gosto**: segue a divisão do Decreto-Lei n.º 3/2010.

| Usar | Quando | Recurso |
|---|---|---|
| **Ausências** | Férias (cap. II) e **faltas** — ausência de um dia ou parte dele (art. 13.º) | `/funcionarios/{id}/pedidos-ausencia` + `/saldos-ausencia` |
| **Licenças** | **Ausência prolongada, mediante autorização** (art. 44.º); as sete modalidades do art. 45.º | `/funcionarios/{id}/licencas-mobilidade` com subtipo de `recordType=LICENCA` |
| **Mobilidade** | A pessoa **não se ausenta** — vai exercer funções noutro sítio (Lei 20/X/2023, art. 132.º a 135.º) | o mesmo recurso, com subtipo de `recordType=MOBILIDADE` |

O eixo é **curto contra prolongado**: a ausência conta-se em dias e desconta de um saldo anual; a licença tem início e fim, é autorizada caso a caso e **pode tirar o Lugar** ao funcionário.

> **Isto não é gestão de assiduidade.** Não há horário, registo de ponto, atrasos nem horas em débito. O pedido de ausência guarda datas e um **número inteiro de dias** — meio dia é inexprimível, e o art. 13.º n.º 4 exige meios períodos. O tipo `FALTA_INJUSTIFICADA` existe no catálogo, mas os efeitos que o art. 43.º n.º 2 lhe manda (não contar antiguidade, perda de remuneração ou desconto nas férias) **não têm campo** onde viver.

### 6.2 Saldo de ausências — quando é que os dias saem

O saldo tem três números: `diasDireito`, `diasPendentes` (reservados) e `diasGozados`. O percurso é:

| Momento | Efeito no saldo |
|---|---|
| **Submeter** o pedido | **reserva** os dias; sem saldo suficiente, **422** já aqui |
| **Aprovar** | os reservados passam a **gozados** |
| **Rejeitar**, ou cancelar enquanto `PENDENTE` | **liberta** a reserva |
| **Cancelar** depois de aprovado | **devolve** os dias gozados |

Isto mudou: a reserva era feita só na aprovação, e `diasGozados` ficava sempre a zero. Se o teu front-end mostrava os dias gozados, passa agora a ter valores reais.

### 6.3 Férias: o saldo nasce sozinho

**`POST /saldos-ausencia` deixou de ser o caminho para as férias.** O art. 2.º n.º 4 do DL n.º 3/2010 diz que «o direito a férias vence no dia 1 de Janeiro de cada ano» — e passou a ser o que acontece:

| Momento | O que acontece |
|---|---|
| **Admissão** (`POST /funcionarios`) | o saldo de férias do ano de ingresso é criado logo, com o direito proporcional — muitas vezes **zero**, o que é a resposta certa |
| **Todos os dias**, pelo job (`rh.ferias.vencimento.cron`, 00:05) | o saldo do ano corrente é criado a quem não o tenha e **actualizado** a quem o direito tenha crescido |
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
| `PATCH` | `/funcionarios/{id}/pedidos-ausencia/{pedidoId}/aprovar` | RH |
| `PATCH` | `/funcionarios/{id}/pedidos-ausencia/{pedidoId}/rejeitar` | RH |
| `PATCH` | `/funcionarios/{id}/pedidos-ausencia/{pedidoId}/cancelar` | RH (qualquer colaborador) ou o próprio via self-service |

**Quem cancela:** `PATCH /funcionarios/{id}/pedidos-ausencia/{pedidoId}/cancelar` é o caminho do RH e serve para cancelar o pedido de qualquer colaborador — antes devolvia **403** a quem não fosse o próprio. O colaborador usa o self-service, que só o deixa cancelar o que é seu e enquanto estiver `PENDENTE`.

Só os tipos com `deductsBalance` mexem no saldo; para os outros, nada disto se aplica.

> **`PUT /funcionarios/{id}/contratos/{contratoId}/close` cessa o vínculo.** Cessa o contrato, encerra a afectação corrente (o Lugar fica vago), muda o estado do trabalhador para o estado de cessação por omissão e regista o histórico — os mesmos efeitos de `PATCH /funcionarios/{id}/worker-state` com um estado de cessação. Devolve `EstadoColaboradorResponseDTO`.
>
> Para **renovar ou substituir** um contrato não se usa o `close`: basta criar o contrato novo, que encerra o anterior (motivo `SUBSTITUICAO`) sem tocar na afectação nem no estado.

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

### 7.1 Licenças que abrem vaga
Três campos do subtipo dizem o que a licença faz ao Lugar:

| Campo | Valores | O que faz |
|---|---|---|
| `positionEffect` | `MANTEM` · `ABRE_VAGA` | Se o Lugar fica ocupado ou vago enquanto a licença dura |
| `vacancyAfterDays` | inteiro ou nulo | Abre vaga só se a duração exceder este número de dias; nulo abre logo |
| `returnEffect` | `REGRESSA_LUGAR` · `DISPONIBILIDADE` | O que acontece ao funcionário quando a licença termina |

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
| Opções genéricas | `/api/v1/rh/reference/options` |

Padrão CRUD comum: `GET`, `GET/{id}`, `POST`, `PUT/{id}`, `DELETE/{id}`, `PATCH/{id}/activate`, `GET/combobox`.

---

## 10. Self-service — `/api/v1/rh/me`

O trabalhador autenticado acede aos seus próprios dados (perfil derivado do Lugar corrente).

| Método | Path | Descrição |
|---|---|---|
| `GET` | `/me/profile` | Perfil (unidade/cargo/carreira do Lugar; escalão da afectação). |
| `GET` | `/me/leave-requests` · `POST /me/leave-requests` · `PUT /me/leave-requests/{id}/cancel` | Pedidos de ausência próprios. |
| `GET` | `/me/leave-balances` | Saldos. |
| `GET/POST` | `/me/leaves-mobilities` · `GET /me/leaves-mobilities/{id}` | Mobilidades próprias. |
| `GET` | `/me/payroll-slips` · `/me/payroll-slips/{id}/download` | Recibos. |
| `GET` | `/me/documents` · `/me/documents/{id}/download` | Documentos. |

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

## 12. Enumerações

Os que estão marcados **validado** são enums fechados no domínio: um valor fora da lista devolve **422**, em vez de ser gravado tal e qual.

| Enum | Valores | |
|---|---|---|
| `origem` (afectação) | `ADMISSAO`, `PROGRESSAO`, `PROMOCAO`, `MOBILIDADE`, `TRANSFERENCIA`, `SUBSTITUICAO` | |
| `assignmentType` | `PRINCIPAL`, `SUBSTITUICAO`, `ACUMULACAO` — omisso vale `PRINCIPAL`. A `SUBSTITUICAO` cria-se pelo endpoint de substituição (5.7), não por `POST /assignments`. | **validado** |
| `estado` (Lugar) | `ATIVO`, `CONGELADO`, `EXTINTO` (provido/vago é **derivado do titular**) | |
| `recordType` (subtipo licença/mobilidade) | `LICENCA`, `MOBILIDADE` — o valor **`AMBOS` foi removido na V43** | **validado** |
| `situacaoFuncional` (estado do trabalhador) | `ACTIVIDADE_NO_QUADRO`, `ACTIVIDADE_FORA_QUADRO`, `INACTIVIDADE_NO_QUADRO`, `INACTIVIDADE_FORA_QUADRO`, `DISPONIBILIDADE`, `APOSENTACAO` — pode ser nulo | **validado** |
| `positionEffect` (subtipo) | `MANTEM`, `ABRE_VAGA` | **validado** |
| `returnEffect` (subtipo) | `REGRESSA_LUGAR`, `DISPONIBILIDADE` | **validado** |
| `status` (contrato) | `ATIVO`, `SUSPENSO`, `CESSADO` — obrigatório desde a V44 | **validado** |
| `estado` (pedido de ausência) | `PENDENTE`, `APROVADO`, `REJEITADO`, `CANCELADO` | **validado** |

---

## 13. Endpoints deprecados / removidos

| Antigo | Substituto |
|---|---|
| `POST/GET /funcionarios/{id}/enquadramentos` | Afectação (`/colaboradores/assignments`) |
| `POST/GET /funcionarios/{id}/colocacoes` | Afectação (`/colaboradores/assignments`) |
| Registo com `unidade/cargo/carreira/categoria` | Registo com `positionId` (deriva do Lugar) |
| catálogo audit `enquadramentos` | catálogo audit `assignments` |

Ver `breaking_change_frontend.md` para o guia de migração do frontend.
