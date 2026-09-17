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

> **Variações do wrapper:** a maioria dos wrappers inclui `first`/`last`; o de **funcionários** omite-os (só `pageNumber`/`pageSize`/`totalPages`); o de **Lugares** (`WrapperListaPositionDTO`) substitui-os por `dotacao`/`ocupados`/`vagas`; o de **auditoria** traz apenas `content`/`totalElements`.

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
| `422` | **Violação de regra de negócio** (Lugar ocupado, escalão obrigatório/proibido, mobilidade sem Lugar de destino, estado não permitido…). |

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

**Regras (422):** Lugar não disponível (CONGELADO/EXTINTO) · Lugar já ocupado · Lugar de carreira sem `gradeId` · Lugar fora de grelha com `gradeId`.

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
Mudar para `RETIRED`/`INACTIVE` **encerra a afectação corrente** (o Lugar volta a vago). Request inalterado.

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

> Não usar `POST /assignments` com `origem=PROGRESSAO`: falha sempre, porque o Lugar já está ocupado pelo próprio colaborador.

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
| `422` | Colaborador inactivo · vínculo não permite · sem afectação corrente · `dataEfeito` não posterior ao início da afectação · Lugar actual fora de grelha · categoria de destino inactiva, de outra carreira ou que não é a imediatamente superior · escalão que não pertence à categoria de destino · Lugar de destino ocupado, não ATIVO ou de outra categoria. |

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
| `422` | Colaborador inactivo · sem afectação corrente · `dataEfeito` não posterior ao início da afectação · Lugar de destino igual ao actual, ocupado ou não ATIVO · Lugar de destino de outra carreira/categoria (use a promoção) · função incompatível com o cargo do destino. |

**Sobre a função:** se não enviar `functionId`, mantém-se a função actual quando é compatível com o cargo do Lugar de destino; quando não é, devolve `422` a pedir que a indique — nunca se perde em silêncio. Regras completas: `regras_negocio.html`, secção 3.3 (BR-TRF-01 a 08).

> Uma mudança **temporária** de Lugar, com regresso, não é transferência: é **mobilidade** (secção 7).

---

## 6. Dados do colaborador (sub-recursos de `/funcionarios/{funcionarioId}`)

Todos seguem o padrão CRUD + (quando aplicável) `documentos`:

| Recurso | Base |
|---|---|
| Contratos | `/funcionarios/{id}/contratos` — + `close`, `suspend`, `activate`, `documentos` |
| Dados bancários | `/funcionarios/{id}/dados-bancarios` |
| Dependentes | `/funcionarios/{id}/dependentes` |
| Qualificações | `/funcionarios/{id}/qualificacoes` — + `documentos` |
| Formações | `/funcionarios/{id}/formacoes` — + `documentos` |
| Documentos | `/funcionarios/{id}/documentos` — upload/list/download/delete |
| Recibos | `/funcionarios/{id}/recibos` — + `documentos` |
| Processos disciplinares | `/funcionarios/{id}/processos-disciplinares` — + `documentos` |
| Pedidos de ausência | `/funcionarios/{id}/pedidos-ausencia` — + `aprovar`/`rejeitar`/`cancelar` |
| Saldos de ausência | `/funcionarios/{id}/saldos-ausencia` |
| Licenças/mobilidade | `/funcionarios/{id}/licencas-mobilidade` (ver 7) |

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

| Método | Path | Descrição |
|---|---|---|
| `POST` | `/` | Criar licença/mobilidade. |
| `GET` | `/` · `/{licencaId}` | Listar / detalhe. |
| `PUT` | `/{licencaId}` | Atualizar. |
| `PUT` | `/{licencaId}/approve` | Aprovar. MOBILIDADE → cria afectação no Lugar de destino. |
| `PUT` | `/{licencaId}/reject` | Rejeitar. |
| `PUT` | `/{licencaId}/close` | Encerrar. MOBILIDADE temporária → **regressa ao Lugar de origem**. |
| `PUT` | `/{licencaId}/cancel` | Cancelar (reverte mobilidade ativa). |
| `PATCH` | `/{licencaId}/ativar` · `DELETE /{licencaId}/desativar` | Aliases legados de approve/cancel. |
| `POST/GET/DELETE` | `/{licencaId}/documentos…` | Documentos anexos. |

**`LicencaMobilidadeRequestDTO`** (campos-chave):
```json
{
  "subtipoId": "uuid",                 // t_leave_mobility_subtype (record_type)
  "dataInicio": "YYYY-MM-DD",
  "dataFim": "YYYY-MM-DD | null",
  "entidadeDestino": "string",
  "destinationUnitId": "uuid | null",
  "destinationPositionId": "uuid | null", // OBRIGATÓRIO p/ MOBILIDADE (senão 422 no approve)
  "despachoNumero": "string | null",
  "justification": "string | null"
}
```

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

| Enum | Valores |
|---|---|
| `origem` (afectação) | `ADMISSAO`, `PROGRESSAO`, `PROMOCAO`, `MOBILIDADE`, `TRANSFERENCIA` |
| `assignmentType` | `PRINCIPAL`, `ACUMULACAO`, `SUBSTITUICAO` |
| `estado` (Lugar) | `ATIVO`, `CONGELADO`, `EXTINTO` (provido/vago é **derivado**) |
| `record_type` (subtipo mobilidade) | `LICENCA`, `MOBILIDADE`, `AMBOS` |

---

## 13. Endpoints deprecados / removidos

| Antigo | Substituto |
|---|---|
| `POST/GET /funcionarios/{id}/enquadramentos` | Afectação (`/colaboradores/assignments`) |
| `POST/GET /funcionarios/{id}/colocacoes` | Afectação (`/colaboradores/assignments`) |
| Registo com `unidade/cargo/carreira/categoria` | Registo com `positionId` (deriva do Lugar) |
| catálogo audit `enquadramentos` | catálogo audit `assignments` |

Ver `breaking_change_frontend.md` para o guia de migração do frontend.
