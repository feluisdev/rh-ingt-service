# Breaking Changes — Frontend (v5, Position Management)

> Guia de migração do frontend para o novo modelo de **Mapa de Pessoal** (Lugares + Afectações).
> Refactor assumido como *breaking change*. pt-PT.

## TL;DR — o que muda

1. **O registo de colaborador deixa de enviar unidade/cargo/carreira/categoria.** Passa a enviar um **`positionId`** (Lugar vago) + `gradeId`. Tudo o resto deriva do Lugar.
2. **Enquadramento e Colocação desaparecem.** São substituídos por uma única **Afectação** (`assignment`).
3. **Novos ecrãs:** gestão do Mapa de Pessoal (criar/editar Lugares, chefias) e **picker de Lugar vago** na admissão.
4. **Novas queries** para chefia, responsável de unidade, unidade atual e vagas.
5. **Mobilidade** passa a exigir `destinationPositionId`.

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

## 6. Mobilidade (BREAKING — campo novo)

`LicencaMobilidadeRequestDTO` ganha **`destinationPositionId`** (o Lugar de destino):

```jsonc
{
  "subtipoId": "uuid",
  "destinationUnitId": "uuid",
  "destinationPositionId": "uuid",  // ← OBRIGATÓRIO para MOBILIDADE
  "dataInicio": "YYYY-MM-DD",
  "justification": "…"
}
```

- `PUT .../licencas-mobilidade/{id}/approve` → cria a afectação no Lugar de destino.
- `PUT .../{id}/close` (mobilidade temporária) → **regressa ao Lugar de origem**.
- Aprovar/ativar MOBILIDADE **sem** `destinationPositionId` → `422`.

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

As operações com mais a dizer mantêm o DTO próprio que já tinham: registo composto, progressão, promoção, transferência, substituição, mudança de estado.

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
