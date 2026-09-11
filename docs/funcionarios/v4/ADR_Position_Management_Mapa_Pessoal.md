# ADR-002 — Position Management (Mapa de Pessoal) · Desenho e Plano de Migração

| Metadado | Detalhe |
|---|---|
| **Documento** | ADR-002 — Desenho técnico + plano de migração |
| **Projeto** | SIPPROG — Sistema de Informação do Pessoal e Progressões (INGT) |
| **Âmbito** | Módulos `estrutura/` (Lugar) e `colaboradores/` (Afectação); **não** altera `t_unidade_organica` |
| **Estado** | Aprovado — âmbito e modelo decididos; aguarda arranque de implementação |
| **Decisão base** | Adoptar **Position Management** (Opção B do ADR-001), *breaking change* assumido **agora** |
| **Substitui** | Operacionaliza o ADR-001 (`ADR_Modelo_Movimentos_Enquadramento_Colocacao.md`) |
| **Data** | 2026-09-01 |

> Ancoragem dupla: boas práticas HRIS (Oracle Fusion, Workday, SAP SuccessFactors) **e** legislação cabo-verdiana (PCFR — Decreto-Lei 4/2024; **mapa de quadro de pessoal**). Os dois convergem no mesmo modelo.

---

## 1. Decisões de desenho — "bem feito", não "barato"

Cada ponto onde a indústria oferece variantes foi decidido conscientemente:

| # | Ponto de variação | Decisão | Porquê / fonte |
|---|---|---|---|
| D1 | Job Management vs **Position Management** | **Position Management** | Controlo de efectivos exige-o; a própria regra de decisão da indústria encaminha o sector público para aqui. |
| D2 | Lugar com `dotacao>1` vs **1 Lugar = 1 cadeira** | **1 Lugar = 1 cadeira** | Vaga exacta e rastreável; padrão Workday Position Management. |
| D3 | Lugar fino (só cargo) vs **Lugar rico** | **Lugar rico** (fixa carreira+categoria) | O mapa de pessoal CV define o lugar por carreira/categoria; escalão fica por-pessoa. |
| D4 | Chefia: flag booleana vs **ligação "gere unidade"** | **`manages_unit_id` na posição** (SAP `Manages_Org_Unit` / Workday manager role) | Diz *que* unidade a posição gere; permite gerir unidade externa/várias; é position-based e herdada. |
| D5 | `estado` provido/vago **guardado** vs **derivado** | **Derivado** de existir afectação corrente | Evita reintroduzir *drift* (estado vs realidade). Só estados administrativos são guardados. |
| D6 | Datar o próprio Lugar (SCD2) vs **catálogo + Envers** | **Catálogo auditado (Envers)**; alterações estruturais por extinguir+criar | O timeline de pessoas vive na Afectação; o histórico "quem dirigiu" resolve-se pela ocupação. Mais simples e sem duplo-timeline. |
| D7 | Chefia por posto: `parent_position_id` **e** `manages_unit_id` | **Ambos, papéis distintos** | `parent_position_id` = reporte estrutural (posto→posto); `manages_unit_id` = responsável de unidade. |
| D8 | Onde vive o Lugar (camada) | **`estrutura/`** (Lugar = config); **Afectação** em `colaboradores/` | Espelha a indústria: Position é estrutura organizacional; Assignment é dado do trabalhador. |

**Regra de ouro do desenho:** a unidade, o cargo e a chefia vivem **uma só vez**, no Lugar. A pessoa **herda-os** ao ser afectada. Estado de ocupação **deriva-se**, nunca se copia.

---

## 2. Modelo-alvo — schema exacto

### 2.1 `t_position` — o Lugar (novo · camada `estrutura/`)

```
t_position
  id                    UUID  PK
  numero_lugar          VARCHAR  UNIQUE NOT NULL      -- ex.: "TS-DSI-014"
  job_id                UUID  FK→t_job      NOT NULL  -- cargo (papel genérico)
  unidade_organica_id   UUID  FK→t_unidade_organica NOT NULL
  career_id             UUID  FK→t_career            -- Lugar rico (D3)
  category_id           UUID  FK→t_category          -- Lugar rico (D3)
  parent_position_id    UUID  FK→t_position          -- reporte estrutural (D7)
  manages_unit_id       UUID  FK→t_unidade_organica  -- responsável de unidade (D4/D7), nullable
  estado                VARCHAR(20) NOT NULL          -- ATIVO | CONGELADO | EXTINTO (D5)
  legal_base            VARCHAR                       -- despacho / Boletim Oficial que aprova o lugar
  is_active             BOOLEAN NOT NULL              -- soft delete
  + AuditEntity (created_at/by, updated_at/by, Envers)
```

**Notas de schema:**
- `estado` guarda **só** o estado administrativo (`ATIVO`/`CONGELADO`/`EXTINTO`). **PROVIDO/VAGO deriva-se** (D5): `VAGO` = sem `t_assignment` corrente; `PROVIDO` = com afectação corrente.
- `career_id`/`category_id` obrigatórios para lugares de carreira; nulos para lugares fora de grelha (tarefeiros — coerente com `requiresCareerStructure`).
- **`grade_id` NÃO está no Lugar** — o escalão é a posição-na-grelha da *pessoa*, vive na Afectação.

**Constraints:**
- `UNIQUE(numero_lugar)`.
- Índice único parcial: **um só** responsável por unidade — `UNIQUE(manages_unit_id) WHERE manages_unit_id IS NOT NULL AND is_active`.
- **Um ocupante por Lugar** (D2): garantido pela regra de afectação (§3), não por FK — `UNIQUE(position_id) WHERE is_current` em `t_assignment`.
- Coerência PCFR: `category_id ∈ career_id` (validação, ver §3).

### 2.2 `t_assignment` — a Afectação (novo · camada `colaboradores/`)

Funde `t_employee_professional_assignments` (enquadramento) + `t_employee_unit_assignments` (colocação).

```
t_assignment
  id                UUID PK
  funcionario_id    UUID FK→t_funcionario  NOT NULL
  position_id       UUID FK→t_position     NOT NULL   -- a cadeira ocupada
  grade_id          UUID FK→t_grade                   -- escalão (por-pessoa); null fora de grelha
  function_id       UUID FK→t_funcao                  -- função exercida (opcional)
  assignment_type   VARCHAR(20) NOT NULL              -- PRINCIPAL | ACUMULACAO | SUBSTITUICAO
  origem            VARCHAR(20) NOT NULL              -- ADMISSAO | PROGRESSAO | PROMOCAO | MOBILIDADE | TRANSFERENCIA
  origin_assignment_id UUID FK→t_assignment           -- p/ mobilidade temporária: afectação a restaurar
  data_inicio       DATE NOT NULL
  data_fim          DATE                              -- null = corrente
  is_current        BOOLEAN NOT NULL
  is_active         BOOLEAN NOT NULL
  notes             TEXT
  + AuditEntity (Envers)
```

**O que a Afectação NÃO guarda** (herda do Lugar): `unidade_organica_id`, `cargo`, `career`, `category`, chefia. Resolvem-se via `position_id`.

**Versionamento (SCD Type 2 — inalterado face a hoje):** ao mudar, o handler fecha o registo corrente (`data_fim = nova_data − 1`, `is_current=false`) e abre novo (`is_current=true`, `data_fim=null`). Uma única linha `is_current` por funcionário (tipo `PRINCIPAL`).

### 2.3 Chefia — dois caminhos, dois significados (D7)

```
Chefe do funcionário Y  = ocupante do parent_position_id do Lugar corrente de Y
Responsável da unidade X = ocupante do Lugar onde manages_unit_id = X
```
O histórico "quem dirigiu a unidade X em <ano>" resolve-se pela **ocupação** (afectações) do Lugar gestor — não precisa de tabela datada dedicada (D6).

### 2.4 O que sai

| Tabela / artefacto | Destino |
|---|---|
| `t_employee_professional_assignments` (enquadramento) | migra → `t_assignment`; tabela retirada após validação |
| `t_employee_unit_assignments` (colocação) | migra → `t_assignment`; tabela retirada |
| `unidade_organica_id`, `cargo_id`, `career/category` na afectação | passam a viver no Lugar |
| `fn_apply_mobility` (restaura colocação) | lógica migra para o handler de mobilidade (fecha/abre afectação) |
| `fn_validate_professional_assignment` | adaptado: valida `grade ∈ position.category` |

### 2.5 O que fica **igual** (não tocar)

`t_funcionario`, `t_contrato`, `t_unidade_organica` (**sem novas colunas** — `manages_unit_id` vive no Lugar, preservando o desacoplamento do sigdi), `t_job`, `t_career`/`t_category`/`t_grade`, catálogos `parametrizacoes/`, dependentes, dados bancários, documentos, qualificações, formação, ausências/licenças, recibos, feriados, histórico de estado. SCD Type 2 mantém-se exactamente.

---

## 3. Regras de negócio

### Admissão / registo
1. Escolhe-se um **Lugar `ATIVO` e `VAGO`** (sem afectação corrente). Rejeita se `PROVIDO`/`CONGELADO`/`EXTINTO` → HTTP 422.
2. Cria-se **1 afectação** `PRINCIPAL`, `origem=ADMISSAO`, apontando ao Lugar; `grade`/`function` por-pessoa.
3. `career`/`category`/`cargo`/`unidade` **não** são enviados — derivam do Lugar.
4. Validação PCFR: se o Lugar tem `category_id`, o `grade` escolhido tem de lhe pertencer.
5. O Lugar passa a `PROVIDO` (**derivado**, não escrito).

### Mobilidade
- Fecha a afectação corrente (SCD2) e abre nova `origem=MOBILIDADE` no **Lugar de destino**.
- **Temporária:** grava `origin_assignment_id`; ao encerrar, reabre a afectação de origem (substitui o antigo `fn_apply_mobility`).
- A unidade "muda" por consequência do novo Lugar — **sem cópia para dessincronizar** (fim do *drift*).

### Progressão / promoção (registo; motor fica p/ fase 2)
- Progressão de escalão: nova afectação `origem=PROGRESSAO`, mesmo Lugar, novo `grade`.
- Promoção de categoria/cargo: normalmente implica **novo Lugar** (nova cadeira na grelha) → nova afectação `origem=PROMOCAO`.

### Chefia
- `manages_unit_id` define o responsável da unidade; constraint garante unicidade por unidade.
- Query de resolução expõe responsável/chefe (§7). Lugar gestor `VAGO` → responsável "em acumulação" (subir na árvore).

### Vagas / mapa de pessoal
- `vagas(unidade) = nº lugares ATIVOS da unidade − nº com afectação corrente`. Derivado, sempre exacto.

---

## 4. Colocação por camada (bounded contexts)

| Conceito | BC | Justificação |
|---|---|---|
| **Lugar** (`t_position`) + Mapa de Pessoal CRUD | **`estrutura/`** | É estrutura organizacional/config, ao lado de unidades, cargos, funções. |
| **Afectação** (`t_assignment`) | **`colaboradores/`** | É dado do trabalhador (dossier), ao lado de contrato. |
| Queries de resolução (responsável/chefe/vagas) | `estrutura/` (mapa) + `colaboradores/` (por funcionário) | — |

Segue o Domain Identity Pattern: `PositionId`, `AssignmentId` (typed VOs, sem expor `ExternalID`). Convenções `colaboradores/`: `@Entity(name="Colabs...")`, prefixo `Colabs`, bean names explícitos.

---

## 5. Plano de migração de dados

> O passo mais sensível. Requer script + auditoria dos dados actuais antes de largar as tabelas antigas.

**Passo M1 — criar schema.** Migração Flyway: `t_position`, `t_assignment` (+ índices/constraints). Não larga nada ainda.

**Passo M2 — gerar Lugares a partir da ocupação actual.**
- Para cada **enquadramento corrente** (`is_current`), criar 1 Lugar (`estado=ATIVO`) com `job/unidade/career/category` do enquadramento; `numero_lugar` gerado; `estado` administrativo `ATIVO`.
- `parent_position_id` e `manages_unit_id`: **não** são inferíveis dos dados actuais (a chefia não existe hoje) → ficam nulos, preenchidos depois pelo administrador RH na construção do mapa (é aceitável: hoje também não existem).

**Passo M3 — gerar Afectações.**
- Cada enquadramento (corrente **e** histórico) → 1 `t_assignment`, mapeando `grade/function/datas/is_current`; `origem=ADMISSAO` para o primeiro, inferida (`PROGRESSAO`/`MOBILIDADE`) para os seguintes quando derivável, senão `ADMISSAO`/`TRANSFERENCIA` por defeito.
- Lugares para afectações **históricas**: reutilizar o Lugar corrente com igual `(unidade,cargo,career,category)`; se não houver, criar Lugar `estado=EXTINTO` (existiu, já não está no quadro).
- Colocações correntes que **divirjam** do enquadramento (casos de *drift* actuais) → registar em relatório de reconciliação; a unidade de verdade passa a ser a do Lugar (decisão: **a colocação corrente ganha** para a unidade, por ser a mais recente — confirmar caso a caso no relatório).

**Passo M4 — reconciliação e validação.** Relatório: nº lugares criados, afectações migradas, divergências enquadramento↔colocação encontradas, funcionários sem colocação (o bug) agora com afectação. Validar contagens.

**Passo M5 — repontar aplicação** (handlers/DTOs/manifests, §6).

**Passo M6 — retirar tabelas antigas.** Só após M4 validado e M5 em produção: migração que larga `t_employee_professional_assignments` e `t_employee_unit_assignments` (e `fn_apply_mobility`, `fn_validate_professional_assignment` antigas). Manter dump de segurança.

---

## 6. Impacto por artefacto

**Novos**
- `estrutura/`: `PositionEntity`, `Position` (domain), `PositionId`, repo/adapter/mapper, handlers CRUD (Mapa de Pessoal), controller + manifest `.igrpstudio/estrutura/`.
- `colaboradores/`: `AssignmentEntity`, `Assignment` (domain), `AssignmentId`, repo/adapter, `AssignmentMapper`, handlers, DTOs, controller + manifest.
- Migrações Flyway: `V__position_assignment.sql` (schema), `V__migrate_movimentos.sql` (dados), `V__drop_legacy_movimentos.sql`.

**Alterados**
- `RegistarColaboradorCommandHandler` — cria afectação a Lugar vago (em vez de enquadramento).
- `AprovarLicencaMobilidadeCommandHandler` — fecha/abre afectação (substitui lógica de colocação + `fn_apply_mobility`).
- `MudarEstadoColaboradorCommandHandler` — efeitos RETIRED/INACTIVE fecham a **afectação** corrente.
- `EnquadramentoController`/handlers + `ColocacaoController`/handlers → unificados em **Assignment**; endpoints antigos deprecados.
- `EnquadramentoRequestDTO` → `AssignmentRequestDTO` (`positionId` + `gradeId`, sem unidade/cargo/career/category).
- Triggers PCFR adaptados.

**Retirados** — entidades/mappers/repos de Enquadramento e Colocação; manifests `.igrpstudio/colaboradores/models/EnquadramentoEntity.json`, `ColocacaoEntity.json`.

---

## 7. Queries novas (contrato para consumidores, incl. sigdi)

| Query | Resolução |
|---|---|
| `GET responsavel-unidade/{unidadeId}` | ocupante da afectação corrente do Lugar com `manages_unit_id=unidadeId` |
| `GET chefe-funcionario/{funcionarioId}` | ocupante do `parent_position_id` do Lugar corrente do funcionário |
| `GET unidade-atual/{funcionarioId}` | `afectação corrente → position → unidade_organica_id` (contrato que o SIADAP consome; substitui `colocação → unit_id`) |
| `GET vagas/{unidadeId}` | lugares ATIVOS − ocupados |
| `GET mapa-pessoal/{unidadeId}` | lista de Lugares (provido/vago/congelado) da unidade |

---

## 8. Não-regressão do `sigdi/`

O sigdi está desacoplado (ver ADR-001 §verificação): só depende de `FuncionarioRepository` (`t_funcionario`) e `OrganizationalUnitRepository` (`t_unidade_organica`) — **ambos intactos**. Guarda cópias próprias (`employee_id`, `organic_unit_id`, `evaluator_id`).

Ações no plano:
- **Verificação de compilação** dos lookups `FuncionarioLookupAdapter` / `OrganicaLookupAdapter` após o refactor.
- Passar a alimentar `evaluator_id` a partir de `GET responsavel-unidade` / `chefe-funcionario` (hoje sem fonte fiável) — melhoria, opcional na fase 1.
- Confirmar `GET unidade-atual` a devolver o mesmo UUID de unidade que a colocação devolvia.

---

## 9. Faseamento

- **Fase 1a** — Schema + `PositionEntity`/`AssignmentEntity` + Mapa de Pessoal CRUD (Lugares, chefias) + migração de dados (M1–M4).
- **Fase 1b** — Repontar registo + Assignment CRUD + mobilidade (M5). Queries §7.
- **Fase 1c** — Retirar tabelas antigas (M6), regenerar manifests/controllers, não-regressão sigdi.
- **Fase 2** (fora deste ADR) — Motor de progressão (elegibilidade por tempo-no-escalão, alertas); reanima `Category.ordem_progressao`, `Grade`, `eligible_for_progression`.

---

## 10. Riscos

- **Migração de dados** (M2–M4) é o ponto crítico — reconstruir Lugares/histórico e reconciliar *drift* actual. Mitigar: relatório de reconciliação + dump + validação de contagens antes de M6.
- **Chefias por preencher** após migração (não existem hoje) — administrador RH constrói o mapa; não bloqueia a fase 1a.
- **Controllers gerados** (`.igrpstudio/`): regenerar, não editar à mão; manter lógica nos handlers.
- **Contrato de API breaking** nos endpoints de registo/enquadramento/colocação — coordenar com frontend (picker de Lugar + ecrãs de Mapa de Pessoal).

---

## 11. Fora de âmbito

Motor de progressão (fase 2). Camada "linha do quadro" agregada (números legais de dotação por unidade/carreira separados dos Lugares individuais) — só se necessário mais tarde; hoje a dotação deriva-se da contagem de Lugares.

---

## 12. Fontes

- Oracle Fusion HCM — Jobs and Positions; Assignments.
- Workday — Position Management; Supervisory Organizations; manager como **role atribuído a posição** ([role-based assignments](https://www.workday.upenn.edu/home/about/policies/role-based-security-role-assignments), [managers](https://erp.umd.edu/training-resources/workday-learning-hubs/managers-workday)).
- SAP SuccessFactors — Position Management; **`Manages_Org_Unit`** / Head of Unit ([blog](https://community.sap.com/t5/human-capital-management-blog-posts-by-members/how-manages-org-unit-play-a-major-role-in-data-replication-and-workflow/ba-p/14449855)); Manager Flag na posição.
- Kimball — Slowly Changing Dimensions Type 2.
- Cabo Verde — **PCFR, Decreto-Lei nº 4/2024** (B.O. nº 8, I Série, 24-01-2024); **mapa de quadro de pessoal**; Tabela Única de Remuneração (2025).
