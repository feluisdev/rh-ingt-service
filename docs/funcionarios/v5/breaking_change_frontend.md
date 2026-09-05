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

> Contrato completo: `api_guide.md` / `api_guide.html`. Modelo: `modelo_negocio.html`, `modelo_relacional.html`.
