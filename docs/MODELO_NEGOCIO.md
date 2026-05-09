# Modelo de Negócio — RH-Service (SIPPROG)

> Versão 1.0 · Maio 2026  
> Sistema de Informação do Pessoal e Progressões — INGT, Cabo Verde

Este documento explica o modelo de negócio do módulo de Recursos Humanos de A a Z: o que existe, porquê existe, como se relaciona e que regras governa.

---

## Índice

1. [Visão Geral](#1-visão-geral)
2. [A Grande Separação de Responsabilidades](#2-a-grande-separação-de-responsabilidades)
3. [Camada 0 — Lookups Genéricos (OptionEntity)](#3-camada-0--lookups-genéricos-optionentity)
4. [Camada 1 — Parametrizações com Comportamento](#4-camada-1--parametrizações-com-comportamento)
5. [Camada 2 — Estrutura Organizacional](#5-camada-2--estrutura-organizacional)
6. [Camada 3 — Carreiras e Progressão (PCFR)](#6-camada-3--carreiras-e-progressão-pcfr)
7. [Camada 4 — O Funcionário e o seu Dossier](#7-camada-4--o-funcionário-e-o-seu-dossier)
8. [Os Três Históricos Independentes](#8-os-três-históricos-independentes)
9. [Dossier: Formação e Vida Académica](#9-dossier-formação-e-vida-académica)
10. [Documentos — Sistema Polimórfico](#10-documentos--sistema-polimórfico)
11. [Ausências, Licenças e Mobilidades](#11-ausências-licenças-e-mobilidades)
12. [Recibos de Vencimento](#12-recibos-de-vencimento)
13. [Sistema de Feriados](#13-sistema-de-feriados)
14. [Fluxo Completo de Dependências](#14-fluxo-completo-de-dependências)
15. [Regras de Negócio Críticas](#15-regras-de-negócio-críticas)
16. [Resumo das 26 Tabelas](#16-resumo-das-26-tabelas)

---

## 1. Visão Geral

O RH-Service gere o **dossier completo do funcionário público** da administração de Cabo Verde. Cobre o ciclo de vida desde a admissão até à cessação: identificação pessoal, contratos, enquadramento na grelha de carreiras (PCFR), colocação em unidades orgânicas, documentos, ausências, licenças, mobilidades e recibos de vencimento.

O modelo organiza-se em **9 blocos funcionais** e **26 tabelas**, seguindo três princípios fundamentais:

- **Separação de históricos**: contrato, enquadramento de carreira e colocação têm tabelas próprias e ciclos de vida independentes.
- **Dois tipos de catálogo**: lookups simples (sem lógica) ficam num único repositório genérico (`t_option_entity`); catálogos com comportamento têm tabela própria.
- **Soft delete universal**: nada é apagado fisicamente — tudo tem `is_active = false`.

---

## 2. A Grande Separação de Responsabilidades

O princípio mais importante do modelo: **onde fica cada informação e porquê**.

```
t_funcionario          → quem é (dados pessoais, estado actual)
t_contrato             → qual é o seu contrato (historial completo)
t_employee_professional_assignments → onde está na grelha de carreiras (historial completo)
t_employee_unit_assignments         → em que unidade está (historial completo)
t_document             → os seus ficheiros (polimórfico)
t_option_entity        → lookups simples sem lógica (sexo, estado civil, ilha...)
tabelas dedicadas      → catálogos com flags que alteram o comportamento do sistema
```

**Regra de ouro:** o `t_funcionario` não sabe em que unidade está nem qual é o seu escalão — isso vive nos históricos. O `t_funcionario` apenas sabe **quem é** e em que **estado laboral** se encontra.

---

## 3. Camada 0 — Lookups Genéricos (OptionEntity)

### O que é

A `t_option_entity` é uma tabela chave-valor genérica para todos os **lookups que são apenas etiquetas**, sem qualquer lógica de negócio associada.

```
ccode  → nome do grupo  (ex: 'MARITAL_STATUS')
ckey   → chave única    (ex: 'SOLTEIRO')
cvalue → label          (ex: 'Solteiro(a)')
locale → idioma         (ex: 'pt-CV')
```

### Grupos configurados

| `ccode` | Utilização |
|---|---|
| `MARITAL_STATUS` | Estado civil do funcionário (`t_funcionario.estado_civil`) |
| `SEX` | Género do funcionário (`t_funcionario.genero`) |
| `NATIONALITY` | Nacionalidade do funcionário e país de instituição em qualificações |
| `ISLAND` | Ilha da morada do funcionário |
| `CONCELHO` | Concelho da morada do funcionário |
| `RELATIONSHIP_TYPE` | Parentesco dos dependentes (`t_dependente.relationship_type`) |
| `QUALIFICATION_LEVEL` | Nível académico das habilitações (`t_qualificacao.level`) |
| `TRAINING_TYPE` | Tipo de formação profissional (`t_training.training_type`) |
| `DOC_CATEGORY` | Categoria de documento do dossier (`t_tipo_documento.category`) |
| `LEAVE_CATEGORY` | Categoria de tipo de ausência (`t_leave_type.category`) |
| `UNIT_TYPE` | Tipo de unidade orgânica (string livre em implementação actual) |
| `CAREER_REGIME` | Regime da carreira PCFR (string livre em implementação actual) |
| `BANCO` | Banco para dados bancários (`t_dados_bancarios.banco`) |
| `WORK_REGIME` | Regime de trabalho do contrato (`t_contrato.regime_trabalho`) |

### Como os valores são guardados

Os campos que referenciam `t_option_entity` **não guardam o UUID** — guardam directamente o `ckey` como texto (`VARCHAR`). Por exemplo: `genero = 'M'`, `estado_civil = 'CASADO'`, `level = 'LICENCIATURA'`. A validação de que o `ckey` existe no `ccode` correcto é feita pela camada aplicacional.

### Regra de uso

> Se o catálogo tem campos booleanos, limites numéricos ou relacionamentos que alteram o comportamento do sistema → **tabela dedicada**. Se é apenas uma etiqueta configurável → **`t_option_entity`**.

---

## 4. Camada 1 — Parametrizações com Comportamento

São catálogos que precisam de tabela própria porque têm **campos que alteram a lógica do sistema**.

### 4.1 Estados do Trabalhador (`t_worker_state`)

Define o estado laboral do funcionário: `ACTIVE`, `INACTIVE`, `SUSPENDED`, etc.

Campo crítico: **`is_core = true`** — impede a desactivação de estados núcleo (`ACTIVE` e `INACTIVE`). Se `is_core = true`, o `DELETE` devolve HTTP 409.

O `worker_state_id` é o único FK de catálogo guardado directamente no `t_funcionario`. Todos os outros catálogos ficam nos históricos ou nos sub-recursos.

Impacto: funcionários `INACTIVE` não podem aceder à área reservada `/me`.

### 4.2 Vínculos Laborais (`t_vinculo_laboral`)

Classifica a natureza do vínculo: `EFETIVO`, `CONTRATADO`, `COMISSIONADO`, `ESTAGIARIO`.

Campos críticos:
- **`counts_seniority`** — se `false`, o tempo com este vínculo não conta para antiguidade (regras PCFR).
- **`eligible_for_progression`** — se `false`, o funcionário não pode progredir na carreira.

**Não vive no `t_funcionario`**: o vínculo é uma propriedade do **tipo de contrato**, não do funcionário. Ao criar um tipo de contrato, o administrador RH associa-lhe um vínculo laboral. Quando o funcionário tem um contrato activo, o seu vínculo é inferido através do tipo de contrato.

### 4.3 Tipos de Contrato (`t_contract_type`)

Cada tipo representa uma figura legal distinta (Decreto-Lei 4/2024): `NOMEACAO_DEFINITIVA`, `CTFP_TERMO_CERTO`, `CTFP_TERMO_INCERTO`, `COMISSAO_SERVICO`, etc.

Campos críticos:
- **`vinculo_laboral_id`** — liga o tipo de contrato ao vínculo laboral que implica.
- **`is_renewable`** — `CTFP a termo certo` é renovável; `Nomeação Definitiva` não é.
- **`max_renewals`** — nº máximo de renovações permitidas por lei (controla alertas do sistema).
- **`max_duration_months`** — duração máxima legal em meses.

### 4.4 Tipos de Documento (`t_tipo_documento`)

Define que tipos de ficheiro o sistema aceita: `CNI`, `PASSAPORTE`, `CONTRATO`, `CERTIDAO`, etc.

Campo crítico: **`allowed_extensions`** (ex: `"pdf,jpg,png"`) — a API rejeita uploads com extensões não autorizadas.

Nota: este catálogo usa nomenclatura portuguesa nos campos (`codigo`, `descricao`) ao contrário dos outros.

### 4.5 Tipos de Ausência (`t_leave_type`)

Define os tipos de licença de curta duração: `FERIAS`, `DOENCA`, `MATERNIDADE`, `LUTO`, etc.

Campos críticos que alteram completamente o fluxo:
- **`deducts_balance = true`** → o sistema valida o saldo disponível antes de aprovar; se insuficiente, o pedido é rejeitado automaticamente.
- **`requires_approval = true`** → o pedido fica em `PENDING` até a chefia aprovar. Se `false`, vai directamente para `APPROVED`.
- **`max_days_per_year`** → limite legal anual; `null` = sem limite.

### 4.6 Subtipos de Licença e Mobilidade (`t_leave_mobility_subtype`)

Distingue licenças de longa duração de mobilidades:
- **Licenças**: sem vencimento, parental, formação.
- **Mobilidades**: comissão de serviço, requisição, destacamento.

O campo **`record_type`** (`LICENCA`, `MOBILIDADE`, `AMBOS`) determina onde o registo aparece na interface.

Campos com impacto sistémico:
- **`affects_pay`** → se `true`, o processamento salarial é afectado.
- **`counts_for_seniority`** → se `false`, o período não conta para antiguidade.
- **`can_self_submit`** → se `true`, o próprio colaborador pode submeter pela área reservada `/me`.

---

## 5. Camada 2 — Estrutura Organizacional

Define a hierarquia da organização e os cargos disponíveis.

### 5.1 Unidades Orgânicas (`t_unidade_organica`)

Estrutura hierárquica auto-referencial:

```
Direcção (parent_unit_id = null)
  └── Departamento (parent_unit_id = uuid da Direcção)
        └── Divisão
              └── Secção
```

O campo `type` identifica o nível (ex: `DIRECAO`, `DEPARTAMENTO`). Um `DELETE` é bloqueado se existirem sub-unidades activas ou funcionários colocados.

### 5.2 Cargos (`t_job`)

Designação oficial do cargo que o funcionário ocupa: `DIRETOR_SERVICOS`, `TECNICO_SUPERIOR`, `COORDENADOR`, etc. Referenciado pelos enquadramentos profissionais e colocações.

### 5.3 Funções (`t_funcao`)

Função efectivamente exercida dentro do cargo. Um `TECNICO_SUPERIOR` (cargo) pode exercer a função de `COORDENADOR_PROJETO` ou `ANALISTA_SISTEMAS`. Referenciada pelos enquadramentos.

Cada função tem um campo `job_id UUID FK→t_job` (nullable) que a liga ao cargo ao qual pertence:

- **`job_id` preenchido** — função específica de um cargo. Ao criar um enquadramento com esse cargo, só estas funções são válidas.
- **`job_id = null`** — função genérica, compatível com qualquer cargo.

A validação é feita na camada de domínio (`OrgFunction.validarCompatibilidadeComCargo()`): se `job_id != null` e não coincide com o `cargo_id` do enquadramento, o sistema rejeita com HTTP 422.

O endpoint `GET api/v1/rh/estrutura/functions?jobId={cargoId}` permite ao frontend filtrar as funções disponíveis ao seleccionar um cargo.

---

## 6. Camada 3 — Carreiras e Progressão (PCFR)

Modela a grelha salarial do PCFR (Plano de Carreiras, Funções e Remunerações, Decreto-Lei 4/2024). A hierarquia é rígida e obrigatória:

```
Carreira  →  Categoria  →  Escalão
```

### 6.1 Carreiras (`t_career`)

Agrupamento de nível mais alto: `TECNICO_SUPERIOR_I`, `ASSISTENTE_TECNICO`, `DIRIGENTE`, etc. Cada carreira tem um regime (ex: `GERAL`, `ESPECIAL`).

### 6.2 Categorias (`t_category`)

Nível dentro da carreira. O par `(career_id, code)` é único. O campo `ordem_progressao` define a sequência de progressão — um funcionário avança do escalão mais baixo da categoria actual para o mais alto, depois sobe de categoria.

Exemplo para a carreira `TECNICO_SUPERIOR_I`:
- Categoria `TSA` (Técnico Superior Assistente) — `ordem_progressao = 1`
- Categoria `TSP` (Técnico Superior Principal) — `ordem_progressao = 2`
- Categoria `TSE` (Técnico Superior Especialista) — `ordem_progressao = 3`

### 6.3 Escalões (`t_grade`)

Nível dentro da categoria. O par `(category_id, grade_number)` é único. Cada escalão tem:
- **`salary_index`** — índice da grelha PCFR
- **`salary_base`** — salário base em CVE correspondente ao índice

### Validação da hierarquia

Um trigger de base de dados (`fn_validate_professional_assignment`) garante que:
- A `category_id` indicada pertence à `career_id` indicada.
- O `grade_id` indicado pertence à `category_id` indicada.

Se a hierarquia for incoerente, a operação falha com HTTP 422.

---

## 7. Camada 4 — O Funcionário e o seu Dossier

### 7.1 O Funcionário (`t_funcionario`)

O aggregate root central. Contém apenas **dados pessoais e estado actual**:

| Campo | Detalhe |
|---|---|
| `numero_funcionario` | Gerado automaticamente por sequência (`F000001`); imutável. |
| `nif` | Único e imutável após criação. |
| `worker_state_id` | Estado laboral actual (FK→`t_worker_state`). Único FK de catálogo directo. |
| `document_type_id` | Tipo do documento de identificação (BI, Passaporte, etc.). |
| `numero_documento` | Único quando preenchido. |
| `genero`, `estado_civil`, `nacionalidade` | Valores `ckey` validados contra `t_option_entity`. |
| `morada`, `ilha`, `concelho`, `localidade` | Morada do funcionário. |
| `data_admissao` | Data de entrada na função pública. |

**O que NÃO está no funcionário:**
- Qual é o seu contrato → está em `t_contrato`
- Em que escalão/categoria está → está em `t_employee_professional_assignments`
- Em que unidade está → está em `t_employee_unit_assignments`
- Qual é o seu vínculo laboral → inferido do tipo de contrato activo

### 7.2 Dependentes (`t_dependente`)

Cônjuge, filhos e outros dependentes para efeitos de INPS e subsídios familiares. O campo `relationship_type` usa valores do grupo `RELATIONSHIP_TYPE`.

### 7.3 Dados Bancários (`t_dados_bancarios`)

Dados para processamento de vencimento e declarações INPS:
- `banco` (ckey do grupo `BANCO`)
- `numero_conta`, `iban`
- `numero_seguranca_social` (Nº INPS)

Um funcionário pode ter vários registos (conta principal + poupança). O soft delete (`is_active`) determina quais são considerados no pagamento.

---

## 8. Os Três Históricos Independentes

Este é o coração do modelo. O dossier profissional do funcionário tem **três dimensões de historial completamente independentes**, cada uma com o seu próprio ciclo de vida:

```
Funcionário
    │
    ├── Historial de Contratos          (t_contrato)
    │     ↳ muda quando: renovação, mudança de tipo de contrato
    │
    ├── Historial de Enquadramentos     (t_employee_professional_assignments)
    │     ↳ muda quando: promoção de escalão, mudança de categoria, mudança de cargo
    │
    └── Historial de Colocações         (t_employee_unit_assignments)
          ↳ muda quando: mobilidade para outra unidade, destacamento
```

**São independentes porque mudam por razões completamente diferentes.** Uma promoção de escalão não altera o contrato. Uma renovação de contrato não altera o escalão. Uma mobilidade para outra unidade não altera nem o contrato nem o escalão.

### Padrão `is_current`

Em cada historial, apenas **um registo activo por funcionário** de cada vez. Quando um novo registo é criado:
1. O registo anterior recebe `is_current = false` e `end_date = novo_start_date − 1 dia`.
2. O novo registo fica com `is_current = true` e `end_date = null`.

Esta lógica é executada pelo **handler da aplicação** (não por trigger) para contratos e enquadramentos.

### 8.1 Contratos (`t_contrato`)

Regista cada instrumento contratual do funcionário.

| Campo | Significado |
|---|---|
| `contract_type_id` | FK para o tipo de contrato (que por sua vez liga ao vínculo laboral). |
| `contract_number` | Nº do instrumento contratual (nullable — Nomeação Definitiva usa só o despacho). |
| `regime_trabalho` | `TEMPO_COMPLETO`, `TEMPO_PARCIAL`, `ISENCAO_HORARIO`, `DEDICACAO_EXCLUSIVA`. |
| `percentagem_tempo` | Obrigatório apenas se `regime_trabalho = TEMPO_PARCIAL` (ex: `50.00`). |
| `renewal_count` | Nº de renovações consecutivas do mesmo tipo — o sistema alerta ao atingir `max_renewals`. |
| `status` | `ATIVO` → `SUSPENSO` (durante licença sem vencimento) → `CESSADO`. |
| `termination_reason` | Motivo de cessação (base LGTFP); determina direitos do funcionário. |
| `legal_base` | Nº do despacho / Boletim Oficial. |

**Fluxo típico de um funcionário:**
1. Admitido com `CTFP_TERMO_CERTO` (2 anos) → `renewal_count = 0`
2. Renovação do CTFP → `renewal_count = 1`; contrato anterior fica `CESSADO`
3. Nomeação definitiva → novo contrato `NOMEACAO_DEFINITIVA`; CTFP fica `CESSADO`

### 8.2 Enquadramento Profissional (`t_employee_professional_assignments`)

Regista o posicionamento do funcionário na grelha PCFR.

| Campo | Significado |
|---|---|
| `career_id` | A carreira em que está enquadrado. |
| `category_id` | A categoria dentro da carreira (validado por trigger). |
| `grade_id` | O escalão dentro da categoria (validado por trigger). |
| `cargo_id` | O cargo que ocupa (FK→`t_job`). |
| `function_id` | A função que exerce (FK→`t_funcao`). |

**Fluxo típico:**
1. Admitido na carreira `TECNICO_SUPERIOR_I`, categoria `TSA`, escalão 1.
2. Após 2 anos, progressão para escalão 2 → novo enquadramento; anterior fecha.
3. Após promoção, passa a categoria `TSP`, escalão 1 → novo enquadramento.

### 8.3 Colocações (`t_employee_unit_assignments`)

Regista em que unidade orgânica o funcionário está colocado.

| Campo | Significado |
|---|---|
| `unit_id` | A unidade orgânica de colocação. |
| `job_id` | O cargo na unidade destino (opcional). |
| `assignment_type` | Tipo de afectação: `PRIMARIA`, `SECUNDARIA`, `TEMPORARIA`. |
| `is_current` | `true` = colocação activa principal. |

---

## 9. Dossier: Formação e Vida Académica

### 9.1 Qualificações (`t_qualificacao`)

Habilitações literárias do funcionário: `BASICO`, `SECUNDARIO`, `LICENCIATURA`, `MESTRADO`, `DOUTORAMENTO`. Permite registar formações em curso (`completed = false`, `end_date = null`).

### 9.2 Formações Profissionais (`t_training`)

Acções de formação frequentadas: cursos, seminários, congressos, e-learning. O campo `duration_hours` regista o número de horas.

### 9.3 Processos Disciplinares (`t_disciplinary_process`)

Registo de processos disciplinares com a pena aplicada, datas de vigência e referência ao Boletim Oficial. Acesso restrito a `ROLE_HR_ADMIN`.

---

## 10. Documentos — Sistema Polimórfico

A tabela `t_document` é **genérica e polimórfica**: associa ficheiros a qualquer entidade do sistema através de dois campos:

```
reference_entity  → nome da tabela  (ex: 't_leave_request', 't_training')
reference_id      → UUID do registo
```

Isto evita criar tabelas de documentos separadas para cada entidade. Um único documento pode ser:
- O justificativo de uma ausência (`reference_entity = 't_leave_request'`)
- O certificado de uma formação (`reference_entity = 't_training'`)
- O processo disciplinar digitalizado (`reference_entity = 't_disciplinary_process'`)
- O diploma académico (`reference_entity = 't_qualificacao'`)

O ficheiro físico é armazenado no **MinIO** (S3-compatible). A tabela guarda apenas a `file_key` (caminho no bucket). O download gera uma URL pré-assinada — o ficheiro não passa pelo servidor.

O tipo de documento (`t_tipo_documento`) define as extensões aceites no upload (`allowed_extensions = 'pdf,jpg,png'`).

---

## 11. Ausências, Licenças e Mobilidades

### 11.1 Saldos de Ausência (`t_leave_balance`)

Um registo por funcionário/tipo de ausência/ano. O sistema cria automaticamente os saldos anuais para os tipos que deduzem saldo (`deducts_balance = true`). O constraint `UQ(employee_id, leave_type_id, year)` impede duplicados.

### 11.2 Pedidos de Ausência (`t_leave_request`)

Ausências de curta duração (férias, doença, luto, maternidade, etc.).

**Ciclo de vida:** `PENDING → APPROVED | REJECTED → CANCELLED`

**Regras automáticas ao submeter:**
1. `end_date ≥ start_date`.
2. `working_days` calculado automaticamente, excluindo fins-de-semana e `t_public_holiday`.
3. Se `deducts_balance = true` → valida `working_days ≤ saldo_disponível`.
4. Se `requires_approval = false` → vai directamente para `APPROVED`.
5. Rejeita sobreposições para o mesmo funcionário (excepto pedidos `CANCELLED`/`REJECTED`).

O campo `approver_id` regista a chefia que aprovou/rejeitou.

### 11.3 Licenças e Mobilidades (`t_leave_mobility`)

Ausências de longa duração e movimentações entre serviços.

**Ciclo de vida:** `PENDING → ACTIVE | CANCELLED → CLOSED`

**Diferença fundamental para os pedidos de ausência:**
- As licenças/mobilidades são geridas pelo `t_leave_mobility_subtype`, não pelo `t_leave_type`.
- Uma mobilidade ao ser aprovada executa `fn_apply_mobility`: encerra a colocação orgânica actual do funcionário e cria uma nova na unidade de destino (`destination_unit_id`).
- Ao encerrar (`CLOSED`) uma mobilidade temporária, o sistema restaura a colocação orgânica anterior.

---

## 12. Recibos de Vencimento

A tabela `t_payroll_slip` é um **repositório de PDFs** gerados pelo sistema salarial externo. O RH-Service não processa vencimentos — apenas armazena e disponibiliza os recibos.

O constraint `UQ(employee_id, period_year, period_month)` garante um único recibo por funcionário por mês. O PDF é armazenado no MinIO e referenciado por `document_id`.

---

## 13. Sistema de Feriados

A tabela `t_public_holiday` serve exclusivamente para o **cálculo de dias úteis** nos pedidos de ausência. Dois tipos:
- **Nacionais** (`is_national = true`) — aplicam-se a todos os funcionários.
- **Municipais** (`is_national = false`) — aplicam-se por concelho da unidade orgânica do funcionário.

Seed pré-configurado com os 11 feriados nacionais de Cabo Verde para 2026.

---

## 14. Fluxo Completo de Dependências

Para criar um funcionário completo, a ordem obrigatória é:

```
PASSO 1 — Lookups base (t_option_entity)
  ↓ Sexo, estado civil, nacionalidade, ilha, concelho, banco...
  ↓ (populados automaticamente por seed Flyway)

PASSO 2 — Parametrizações com comportamento
  ↓ t_worker_state    → estados laborais (ACTIVE, INACTIVE, SUSPENDED)
  ↓ t_vinculo_laboral → vínculos (EFETIVO, CONTRATADO, COMISSIONADO)
  ↓ t_contract_type   → tipos de contrato (referencia o vínculo laboral)
  ↓ t_tipo_documento  → tipos de ficheiro aceites
  ↓ t_leave_type      → tipos de ausência (férias, doença...)
  ↓ t_leave_mobility_subtype → tipos de licença/mobilidade
  ↓ t_public_holiday  → feriados nacionais e municipais

PASSO 3 — Estrutura Organizacional
  ↓ t_unidade_organica → hierarquia de direcções/departamentos
  ↓ t_job              → cargos disponíveis
  ↓ t_funcao           → funções disponíveis

PASSO 4 — Carreiras e Progressão
  ↓ t_career    → carreiras PCFR
  ↓ t_category  → categorias (dentro da carreira)
  ↓ t_grade     → escalões (dentro da categoria, com índice e salário)

PASSO 5 — Funcionário
  ↓ t_funcionario → dados pessoais + worker_state_id

PASSO 6 — Dados do Dossier
  ↓ t_dados_bancarios → dados bancários e INPS
  ↓ t_contrato        → contrato actual (referencia contract_type)
  ↓ t_employee_professional_assignments → enquadramento (carreira+categoria+escalão+cargo)
  ↓ t_employee_unit_assignments         → colocação (unidade orgânica)
  ↓ t_dependente                        → dependentes familiares

PASSO 7 — Formação e Vida Académica
  ↓ t_qualificacao          → habilitações literárias
  ↓ t_training              → formações profissionais
  ↓ t_disciplinary_process  → processos disciplinares (se aplicável)

PASSO 8 — Documentos (associados a qualquer entidade acima)
  ↓ t_document → ficheiros digitais (polimórfico)

PASSO 9 — Operação corrente
  ↓ t_leave_request    → pedidos de ausência
  ↓ t_leave_balance    → saldos de ausência (por tipo/ano)
  ↓ t_leave_mobility   → licenças e mobilidades
  ↓ t_payroll_slip     → recibos de vencimento
```

---

## 15. Regras de Negócio Críticas

### Unicidade e imutabilidade

| Tabela | Campo | Regra |
|---|---|---|
| `t_funcionario` | `nif` | Único e imutável após criação |
| `t_funcionario` | `numero_funcionario` | Gerado automaticamente, imutável |
| `t_funcionario` | `numero_documento` | Único quando preenchido |
| `t_funcionario` | `email` | Único quando preenchido |
| `t_category` | `(career_id, code)` | Código único dentro da carreira |
| `t_grade` | `(category_id, grade_number)` | Escalão único dentro da categoria |
| `t_leave_balance` | `(employee_id, leave_type_id, year)` | Um saldo por funcionário/tipo/ano |
| `t_payroll_slip` | `(employee_id, period_year, period_month)` | Um recibo por funcionário por mês |
| `t_public_holiday` | `holiday_date` (nacional) | Um feriado nacional activo por data |

### Triggers e validações automáticas

| Trigger | Tabela | O que faz |
|---|---|---|
| `fn_validate_professional_assignment` | `t_employee_professional_assignments` | Garante que `category` pertence à `career` e `grade` pertence à `category` |
| `OrgFunction.validarCompatibilidadeComCargo()` | Domínio (aplicação) | Ao criar enquadramento: se `functionId` presente e `funcao.job_id != null`, valida `funcao.job_id == cargo_id` — rejeita com 422 se incompatível |
| `fn_check_leave_balance` | `t_leave_request` | Se `deducts_balance = true`, valida saldo antes de aceitar o pedido |
| `fn_check_leave_overlap` | `t_leave_request` | Rejeita pedidos sobrepostos para o mesmo funcionário |
| `fn_apply_mobility` | `t_leave_mobility` | Ao aprovar mobilidade: encerra colocação actual e cria nova no destino |
| `fn_audit_generic` | Todas | Regista todas as alterações em `change_history` com dados antes/depois |
| `fn_set_updated_at` | Todas | Actualiza automaticamente `updated_at` em cada UPDATE |

### Soft delete e bloqueios

| Operação | Bloqueada quando |
|---|---|
| `DELETE t_worker_state` | `is_core = true` ou referenciado por funcionários activos |
| `DELETE t_contract_type` | Referenciado por contratos activos |
| `DELETE t_unidade_organica` | Tem sub-unidades activas ou funcionários colocados |
| `DELETE t_grade` | Referenciado por enquadramentos activos |
| `DELETE t_funcionario` | Tem pedidos de ausência em `PENDING` |

### Regras do `is_current`

Apenas **um registo activo** por funcionário em cada historial:

- **Contratos**: ao criar novo contrato, o handler encerra o anterior (`status = CESSADO`, `is_current = false`, `end_date = start_date_novo − 1 dia`).
- **Enquadramentos**: idem — ao criar novo, o anterior fecha com `end_date = start_date_novo − 1 dia`.
- **Colocações**: usa `end_date IS NULL` para identificar a colocação actual; `is_current = true` marca a principal.

---

## 16. Resumo das 26 Tabelas

| # | Tabela | Bloco | Descrição |
|---|---|---|---|
| 1 | `t_option_entity` | 0 | Lookups genéricos (chave-valor) |
| 2 | `t_worker_state` | 1 | Estados laborais com flag `is_core` |
| 3 | `t_vinculo_laboral` | 1 | Vínculos laborais (PCFR) |
| 4 | `t_contract_type` | 1 | Tipos de contrato com regras de renovação |
| 5 | `t_tipo_documento` | 1 | Tipos de documento com extensões aceites |
| 6 | `t_leave_type` | 1 | Tipos de ausência com `deducts_balance` e `requires_approval` |
| 7 | `t_leave_mobility_subtype` | 1 | Subtipos de licença/mobilidade com `affects_pay` e `counts_for_seniority` |
| 8 | `t_unidade_organica` | 2 | Hierarquia organizacional auto-referencial |
| 9 | `t_job` | 2 | Cargos |
| 10 | `t_funcao` | 2 | Funções |
| 11 | `t_career` | 3 | Carreiras PCFR |
| 12 | `t_category` | 3 | Categorias (dentro da carreira) |
| 13 | `t_grade` | 3 | Escalões com índice e salário base |
| 14 | `t_funcionario` | 4 | Dados pessoais do funcionário (aggregate root) |
| 15 | `t_dependente` | 4 | Dependentes familiares |
| 16 | `t_dados_bancarios` | 4 | Dados bancários e INPS |
| 17 | `t_contrato` | 5 | Historial de contratos |
| 18 | `t_employee_professional_assignments` | 5 | Historial de enquadramentos (carreira+categoria+escalão) |
| 19 | `t_employee_unit_assignments` | 5 | Historial de colocações orgânicas |
| 20 | `t_qualificacao` | 6 | Habilitações literárias |
| 21 | `t_training` | 6 | Formações profissionais |
| 22 | `t_disciplinary_process` | 6 | Processos disciplinares |
| 23 | `t_document` | 7 | Documentos/ficheiros (polimórfico) |
| 24 | `t_leave_balance` | 8 | Saldos de ausência por tipo e ano |
| 25 | `t_leave_request` | 8 | Pedidos de ausência (curta duração) |
| 26 | `t_leave_mobility` | 8 | Licenças e mobilidades (longa duração) |
| 27 | `t_payroll_slip` | 8 | Recibos de vencimento |
| 28 | `t_public_holiday` | 9 | Feriados (cálculo de dias úteis) |

---

*Documento gerado em Maio de 2026 — RH-Service v4.5 — SIPPROG/INGT*
