# Quickstart: Módulo Colaboradores — Cenários de Teste

## Pré-condições

Os seguintes registos devem existir (módulos anteriores):
- Uma Carreira activa (ex: `career_id = <UUID>`)
- Uma Categoria activa ligada à Carreira (ex: `category_id = <UUID>`)
- Um Escalão activo ligado à Categoria (ex: `grade_id = <UUID>`)
- Um Cargo activo (ex: `cargo_id = <UUID>`)
- Uma Unidade Orgânica activa (ex: `unidade_id = <UUID>`)

---

## Cenário 1 — CRUD de Funcionário (9 passos)

**C1-S1**: `POST /api/v1/rh/funcionarios` com dados válidos → esperado **201**, body com `id` e `numeroFuncionario` (ex: F000001).

**C1-S2**: `POST /api/v1/rh/funcionarios` com o mesmo NIF → esperado **409** Conflict.

**C1-S3**: `GET /api/v1/rh/funcionarios/{id}` → esperado **200**, `isActive = true`, `numeroFuncionario = F000001`.

**C1-S4**: `GET /api/v1/rh/funcionarios?nome=<parcial>` → esperado **200**, lista filtrada.

**C1-S5**: `GET /api/v1/rh/funcionarios?situacaoProfissional=ATIVO` → esperado **200**, apenas funcionários activos.

**C1-S6**: `PUT /api/v1/rh/funcionarios/{id}` com apenas `telefone` e `morada` actualizados → esperado **200**, outros campos inalterados.

**C1-S7**: `PUT /api/v1/rh/funcionarios/{id}` com `situacaoProfissional = APOSENTADO` e `dataSaida = <data>` → esperado **200**, `isActive = false`.

**C1-S8**: `GET /api/v1/rh/funcionarios` (sem filtros) → esperado **200**, funcionário APOSENTADO **não** aparece (is_active=false filtrado por omissão).

**C1-S9**: `GET /api/v1/rh/funcionarios?active=false` → esperado **200**, funcionário APOSENTADO aparece.

---

## Cenário 2 — Enquadramento Profissional (7 passos)

**C2-S1**: `POST /api/v1/rh/enquadramentos` com funcionário, carreira, categoria, escalão, cargo, unidade e `dataInicio = 2024-01-01` → esperado **201**, `isCurrent = true`.

**C2-S2**: `GET /api/v1/rh/funcionarios/{id}/enquadramento` → esperado **200**, enquadramento com `isCurrent = true`.

**C2-S3**: `POST /api/v1/rh/enquadramentos` com o mesmo funcionário e `dataInicio = 2024-06-01` (diferente escalão) → esperado **201**; enquadramento anterior deve ter `dataFim = 2024-05-31`, `isCurrent = false`.

**C2-S4**: `GET /api/v1/rh/funcionarios/{id}/enquadramento` → esperado **200**, o novo enquadramento com `dataInicio = 2024-06-01`.

**C2-S5**: `GET /api/v1/rh/funcionarios/{id}/enquadramentos/historico` → esperado **200**, 2 registos ordenados por `dataInicio` decrescente.

**C2-S6**: `POST /api/v1/rh/enquadramentos` com `dataInicio = 2023-01-01` (anterior ao actual) → esperado **422**.

**C2-S7**: `POST /api/v1/rh/enquadramentos` com carreira inactiva → esperado **422**.

---

## Cenário 3 — Contratos (6 passos)

**C3-S1**: `POST /api/v1/rh/contratos` com `tipoContrato = EFETIVO`, `dataInicio = 2024-01-01`, `numeroContrato = CTR-001` → esperado **201**.

**C3-S2**: `GET /api/v1/rh/funcionarios/{id}/contratos` → esperado **200**, 1 contrato activo.

**C3-S3**: `POST /api/v1/rh/contratos` com mesmo funcionário (já tem contrato activo) → esperado **409**.

**C3-S4**: `DELETE /api/v1/rh/contratos/{id}` (único contrato activo) → esperado **409**.

**C3-S5**: `POST /api/v1/rh/contratos` com `numeroContrato = CTR-001` para funcionário diferente → esperado **409** (numero_contrato único globalmente).

**C3-S6**: `PUT /api/v1/rh/contratos/{id}/activate` → esperado **200** (reactivar já activo é idempotente ou retorna ok).

---

## Cenário 4 — Dependentes e Qualificações (6 passos)

**C4-S1**: `POST /api/v1/rh/dependentes` com `nome`, `parentesco = FILHO`, `dataNascimento` → esperado **201**.

**C4-S2**: `GET /api/v1/rh/funcionarios/{id}/dependentes` → esperado **200**, 1 dependente activo.

**C4-S3**: `DELETE /api/v1/rh/dependentes/{id}` → esperado **200**; dependente marcado `isActive = false`.

**C4-S4**: `GET /api/v1/rh/funcionarios/{id}/dependentes` → esperado **200**, lista vazia (inactivo não aparece).

**C4-S5**: `POST /api/v1/rh/qualificacoes` com `nivelAcademico`, `curso`, `instituicao`, `anoConclusao` → esperado **201**.

**C4-S6**: `GET /api/v1/rh/funcionarios/{id}/qualificacoes` → esperado **200**, 1 qualificação activa.

---

## Cenário 5 — Auditoria (3 passos)

**C5-S1**: `GET /api/v1/rh/colaboradores/audit/funcionarios/{id}` → esperado **200**, histórico Envers com INSERT + UPDATEs.

**C5-S2**: `GET /api/v1/rh/colaboradores/audit/enquadramentos/{id}` → esperado **200**, histórico do enquadramento.

**C5-S3**: `GET /api/v1/rh/colaboradores/audit/contratos/{id}` → esperado **200**, histórico do contrato.
