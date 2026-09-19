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
2. **Algumas respostas.** Restam 19 operações sem esquema: os `combobox` (devolvem `ComboboxItemDTO[]`, mas a anotação apaga-o) e cinco consultas de afectação que ainda devolvem `Map` e esperam DTO próprio.

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

### 6.1 Saldo de ausências — quando é que os dias saem

O saldo tem três números: `diasDireito`, `diasPendentes` (reservados) e `diasGozados`. O percurso é:

| Momento | Efeito no saldo |
|---|---|
| **Submeter** o pedido | **reserva** os dias; sem saldo suficiente, **422** já aqui |
| **Aprovar** | os reservados passam a **gozados** |
| **Rejeitar**, ou cancelar enquanto `PENDENTE` | **liberta** a reserva |
| **Cancelar** depois de aprovado | **devolve** os dias gozados |

Isto mudou: a reserva era feita só na aprovação, e `diasGozados` ficava sempre a zero. Se o teu front-end mostrava os dias gozados, passa agora a ter valores reais.

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

| Método | Path | Descrição |
|---|---|---|
| `POST` | `/` | Criar licença/mobilidade (nasce `PENDING`). |
| `GET` | `/` · `/{licencaId}` | Listar / detalhe. |
| `PUT` | `/{licencaId}` | Atualizar (só enquanto `PENDING`). |
| `PUT` | `/{licencaId}/approve` | Aprovar (só a partir de `PENDING`). Valida destino e duração. **Não mexe na afectação.** |
| `PUT` | `/{licencaId}/reject` | Rejeitar com motivo. |
| `PUT` | `/{licencaId}/close` | Encerrar (fim do período ou regresso antecipado). Só a partir de `ACTIVE`. |
| `PUT` | `/{licencaId}/cancel` | Cancelar (`PENDING` ou `ACTIVE`). |
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

No `close`, se o `returnEffect` for `DISPONIBILIDADE`, o colaborador passa ao estado com essa situação (art. 122.º) e a resposta traz `estadoAtribuidoId`. A nova afectação é um acto do RH: depende de haver Lugar livre.

Um período **sem data de fim** ultrapassa qualquer prazo, logo abre vaga. A **mobilidade nunca abre vaga**: marcar um subtipo de mobilidade como `ABRE_VAGA` devolve **400**.

Valores do seed: `LIC_SEM_VENCIMENTO` mantém; `LIC_LONGA_DURACAO` e `LIC_ORG_INTERNACIONAL` abrem logo; `LIC_ACOMP_CONJUGE` além de 365 dias; `LIC_FORMACAO` além de 180.

### Duração e prorrogação
A duração máxima e o número de prorrogações são **parametrizados no subtipo** (`maxDurationDays`, `maxExtensions`). Por omissão, os subtipos de mobilidade ficam com 365 dias e 1 prorrogação (art. 132.º n.º 5). Se o subtipo tiver duração máxima, a `dataFim` é obrigatória e a duração é validada no `approve` (**422** se exceder).

**`PUT /{licencaId}/prorrogar`** — só com o registo `ACTIVE`:
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
