# Regras de Ecrã — Registo de Colaborador

| Metadado | Detalhe |
|---|---|
| **Documento** | Regras de Ecrã — Registo Colaborador |
| **Módulo** | Colaboradores |
| **Versão** | 1.0 |
| **Data** | Maio 2026 |
| **Status** | Em curso |

---

## Visão Geral

O registo de um colaborador é feito num **único ecrã dividido em tabs**. O utilizador preenche as tabs e submete tudo de uma vez para o endpoint:

```
POST /api/v1/rh/funcionarios/registar
```

O backend cria atomicamente todos os registos — se qualquer parte falhar, nada é persistido. A resposta devolve os IDs de tudo o que foi criado (`funcionarioId`, `numeroFuncionario`, `contratoId`, `enquadramentoId`, `dadosBancariosId`, `documentoIds`).

### Estrutura do payload

```json
{
  "funcionario":    { ... },   // obrigatório
  "contrato":       { ... },   // opcional
  "enquadramento":  { ... },   // opcional (ver regra de dependência abaixo)
  "dadosBancarios": { ... },   // opcional
  "dossier":        [ ... ]    // opcional — lista de documentos a anexar no acto do registo
}
```

> **Regra de dependência:** se `enquadramento` for enviado, `contrato` é **obrigatório** — caso contrário a API retorna HTTP 400.

As tabs seguem esta estrutura directamente — cada tab corresponde a uma secção do payload.

---

## Tab 1 — Dados Pessoais

Mapeia para o objecto `funcionario` do payload. É a única tab obrigatória para submeter.

### Secção: Identificação Pessoal

| Campo | Obrigatório | Regras |
|---|---|---|
| Nome Completo | Sim | Máx. 200 caracteres |
| Data de Nascimento | Sim | Não pode ser data futura |
| Género | Sim | Texto livre |
| Estado Civil | Sim | Texto livre |
| NIF | Sim | Único no sistema — erro inline HTTP 409 sem apagar formulário |
| Nacionalidade | Não | Default `CV` |

### Secção: Documento de Identificação

| Campo | Obrigatório | Regra de visibilidade |
|---|---|---|
| Tipo de Documento | Não | Sempre visível |
| Nº do Documento | Não | **Aparece** ao seleccionar Tipo; único quando preenchido |
| Data de Emissão | Não | Aparece ao seleccionar Tipo |
| Data de Validade | Não | Aparece ao seleccionar Tipo; deve ser > data de emissão |

### Secção: Admissão

| Campo | Obrigatório | Regras |
|---|---|---|
| Data de Admissão | Sim | Âncora para validações de data nas restantes tabs |

### Secção: Contactos e Morada

| Campo | Obrigatório | Regras |
|---|---|---|
| Email | Não | Único quando preenchido — erro inline HTTP 409 |
| Telefone | Não | Texto livre |
| Morada | Não | Texto livre |
| Ilha | Não | Texto livre; máx. 100 caracteres |
| Concelho | Não | Texto livre; máx. 100 caracteres |
| Localidade | Não | Texto livre; máx. 100 caracteres |

---

## Tab 2 — Contrato

Mapeia para o objecto `contrato` do payload. Tab opcional — se não preenchida, não é enviada.

| Campo | Obrigatório | Regras |
|---|---|---|
| Tipo de Contrato | Sim (se tab preenchida) | Dropdown FK → `t_contract_type`; ao seleccionar carrega os flags do tipo |
| Nº do Instrumento Contratual | Não | Único na tabela quando preenchido |
| Data de Início | Sim (se tab preenchida) | ≥ Data de Admissão |
| Data de Fim | Condicional | Ver regra abaixo |
| Base Legal / Despacho | Não | Nº de despacho ou Boletim Oficial |
| Regime de Trabalho | Não | Dropdown via `GET /reference/options?ccode=WORK_REGIME` — ver nota |
| Percentagem de Tempo | Condicional | Ver nota |
| Observações | Não | |

### Regras de visibilidade — Tipo de Contrato

Os flags são devolvidos no DTO de `t_contract_type`. O frontend usa os flags — **nunca compara o `code`**:

```
Se isRenewable = true  → Data de Fim obrigatória
Se isRenewable = false → Data de Fim oculta / desactivada
```

### Regime de Trabalho e Percentagem de Tempo

```
Se Regime de Trabalho = TEMPO_PARCIAL → Percentagem de Tempo aparece e é obrigatória
Caso contrário                        → Percentagem de Tempo oculta
```

> ⚠️ **Nota — lógica por implementar:** A condição acima usa `ckey === 'TEMPO_PARCIAL'`
> como verificação temporária no frontend. A API não expõe ainda um flag `requiresPercentage`
> no endpoint de regimes de trabalho. Esta lógica será actualizada quando esse suporte for desenvolvido.

### Aviso de limite de renovações *(não bloqueia)*

```
Se renewalCount >= contractType.maxRenewals → aviso amarelo: "Limite legal de renovações atingido"
```

---

## Tab 3 — Enquadramento

Mapeia para o objecto `enquadramento` do payload. Tab opcional — se não preenchida, não é enviada.

| Campo | Obrigatório | Regras |
|---|---|---|
| Carreira | Condicional | Ver regra abaixo |
| Categoria | Condicional | Filtrado pela Carreira seleccionada |
| Escalão | Condicional | Filtrado pela Categoria seleccionada |
| Cargo | Sim (se tab preenchida) | Sempre obrigatório |
| Função | Não | Filtrado pelo Cargo seleccionado |
| Unidade Orgânica | Sim (se tab preenchida) | |
| Data de Início | Sim (se tab preenchida) | ≥ Data de Admissão |

### Regras de cascata

```
Carreira seleccionada  → limpar e recarregar Categoria
Categoria seleccionada → limpar e recarregar Escalão
Cargo seleccionado     → limpar e recarregar Função (apenas funções do cargo)
```

### Obrigatoriedade de Carreira *(data-driven via flag do contrato)*

```
Se contractType.requiresCareerStructure = true  → Carreira, Categoria, Escalão obrigatórios
Se contractType.requiresCareerStructure = false → Carreira, Categoria, Escalão opcionais
```

> Este flag só está disponível se a Tab 2 tiver sido preenchida. Se o contrato não for preenchido neste registo, os campos de carreira ficam opcionais.

### Validação cruzada com o contrato *(se Tab 2 preenchida)*

```
Data de Início do enquadramento ≥ startDate do contrato
Data de Início do enquadramento ≤ endDate do contrato (se existir)
```

---

## Tab 4 — Dados Bancários

Mapeia para o objecto `dadosBancarios` do payload. Tab opcional — se não preenchida, não é enviada.

| Campo | Obrigatório | Regras |
|---|---|---|
| Banco | Não | Dropdown via `GET /reference/options?ccode=BANCO` |
| Nº de Conta | Não | |
| IBAN | Não | Validação de formato (25 caracteres para CV) |
| Nº Segurança Social (INPS) | Não | |

---

## Tab 5 — Dossier de Documentos *(opcional)*

Mapeia para o array `dossier` do payload. Permite fazer upload de um ou mais documentos **no acto do registo**. Tab opcional — se não preenchida, não é enviada.

Cada documento no array requer os seguintes campos:

| Campo | Obrigatório | Regras |
|---|---|---|
| Tipo de Documento (`documentTypeId`) | Sim | FK → `t_document_type`; valida extensões permitidas |
| Chave do ficheiro (`fileKey`) | Sim | Chave MinIO do ficheiro previamente carregado |
| Nome original (`originalFilename`) | Sim | Nome original do ficheiro |
| Tipo de conteúdo (`contentType`) | Sim | MIME type (ex: `application/pdf`) |
| Tamanho do ficheiro (`fileSize`) | Sim | Tamanho em bytes |
| Descrição (`description`) | Não | Anotação livre |

> O frontend deve fazer o upload para o MinIO primeiro (obtendo a `fileKey`) e só então incluir o documento neste array.

---

## Após o Registo — Dossier Complementar

Os sub-recursos abaixo **não fazem parte deste ecrã de registo inicial**. São preenchidos em ecrãs próprios após o registo, usando o `funcionarioId` devolvido na resposta.

> Excepção: documentos podem ser carregados directamente no registo via Tab 5 (ver acima).

| Sub-recurso | Endpoint |
|---|---|
| Dependentes | `POST /funcionarios/{id}/dependentes` |
| Habilitações | `POST /funcionarios/{id}/qualificacoes` |
| Formações | `POST /funcionarios/{id}/formacoes` |
| Processos Disciplinares | `POST /funcionarios/{id}/processos-disciplinares` |
| Documentos | `POST /funcionarios/{id}/documentos` |

---

## Regras Transversais

| Regra | Detalhe |
|---|---|
| **Endpoint único** | Toda a submissão vai para `POST /api/v1/rh/funcionarios/registar` — uma única chamada |
| **Tabs opcionais** | Se uma tab não for preenchida, o objecto correspondente não é enviado no payload |
| **Dependência enquadramento** | `enquadramento` só pode ser enviado se `contrato` também for enviado — erro HTTP 400 caso contrário |
| **Datas âncora** | `dataAdmissao` da Tab 1 é o limite inferior de todos os campos de data das restantes tabs |
| **Lookups de Option** | Todos os dropdowns de catálogo usam `GET /reference/options?ccode={X}` |
| **Flags data-driven** | Regras de visibilidade do contrato usam os flags do DTO (`isRenewable`, `requiresCareerStructure`) — nunca comparar `code` |
| **Erros de unicidade** | NIF, email, nº documento — erro inline sem apagar formulário (HTTP 409) |
| **Dossier no registo** | Documentos podem ser carregados na Tab 5 durante o registo; também podem ser adicionados posteriormente via `POST /funcionarios/{id}/documentos` |
| **Resposta** | A resposta inclui `numeroFuncionario` (gerado automaticamente, formato `F000001`) e `documentoIds` (lista de IDs dos documentos criados) |
