# Plano de Implementação — Position Management (Fase 1)

| | |
|---|---|
| **Base** | ADR-002 (`ADR_Position_Management_Mapa_Pessoal.md`) |
| **Branch** | `feat/position-management` (a partir de `dev @ 9bdaac0`) |
| **Progressão** | Fase 2 — **fora** deste plano |
| **Versão HTML (visual)** | `Plano_Implementacao_Position.html` · Artifact: https://claude.ai/code/artifact/7cf9bc9c-d069-473b-8c03-dd36beb1d388 |

> Documento para ler **antes** de qualquer código. Fluxo completo: schema → limpeza → lógica → testes → gate de merge.

---

## 1. Decisões de arranque (fixadas — não reabrir)

- **Modelo:** Position Management "bem feito". 1 Lugar = 1 cadeira · Lugar rico (carreira+categoria) · escalão na afectação · `manages_unit_id` para chefia de unidade.
- **Arranque limpo, sem backfill.** App no início → limpamos os dados de movimentos de dev. Sem reconstrução de Lugares a partir de dados antigos.
- **Tabelas antigas deprecadas, não largadas.** `t_employee_professional_assignments` e `t_employee_unit_assignments` ficam como rede de segurança. DROP só em fase futura.
- **Migração defensiva + idempotente.** Nada criado sem verificar existência; corre N vezes sem partir. Backfill **nunca** no Flyway.
- **Script de limpeza** em `scripts/cleanup/` (SQL idempotente, fora do Flyway).
- **Isolamento:** branch própria; **não** toca `t_unidade_organica` nem `sigdi/`. Merge para `dev` só com tudo verde **e** autorização explícita.
- **Cada passo atualiza o handoff** (`.claude/resume/rhFix.md`) para `/resume` continuar sem contexto.

---

## 2. Fluxo em quatro fases

```
FASE 1A ─────────► FASE 1B ─────────► FASE 1C ─────────► FASE 2 (gate)
Schema + limpeza   Lógica + endpoints  Regenerar + testar  Merge → dev
+ domínio          Mapa/Afectação      manifests, smoke,   (relatório FE +
                   registo/mobilidade  endpoint, regressão  luz verde)
```

Sequencial. Cada fase termina com o handoff atualizado. A Fase 2 (merge) só arranca com autorização.

---

## 3. Schema — migração defensiva e idempotente

Migração Flyway **só de estrutura** (nunca dados). Guardas `IF NOT EXISTS`; FKs/índices dentro de blocos `DO $$` que verificam `pg_constraint`/`pg_class` antes de criar. Correr duas vezes não parte.

### `t_position` — o Lugar
```sql
CREATE TABLE IF NOT EXISTS t_position (
  id                   UUID PRIMARY KEY,
  numero_lugar         VARCHAR(60)  NOT NULL,
  job_id               UUID NOT NULL,
  unidade_organica_id  UUID NOT NULL,
  career_id            UUID,               -- null = fora de grelha (tarefeiro)
  category_id          UUID,               -- null = fora de grelha
  parent_position_id   UUID,               -- reporte estrutural (chefia)
  manages_unit_id      UUID,               -- unidade que este Lugar dirige
  estado               VARCHAR(20) NOT NULL DEFAULT 'ATIVO', -- ATIVO|CONGELADO|EXTINTO
  legal_base           VARCHAR(255),
  is_active            BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMP, created_by VARCHAR(255),
  updated_at TIMESTAMP, updated_by VARCHAR(255)
);
-- FKs + índices em DO $$ ... IF NOT EXISTS (idempotente)
-- UNIQUE(numero_lugar); UNIQUE(manages_unit_id) WHERE is_active
-- t_position_aud (Envers) criada na mesma migração
```

### `t_assignment` — a Afectação (funde enquadramento + colocação)
```sql
CREATE TABLE IF NOT EXISTS t_assignment (
  id                   UUID PRIMARY KEY,
  funcionario_id       UUID NOT NULL,
  position_id          UUID NOT NULL,      -- a cadeira ocupada
  grade_id             UUID,               -- escalão (por-pessoa); null fora de grelha
  function_id          UUID,
  assignment_type      VARCHAR(20) NOT NULL DEFAULT 'PRINCIPAL', -- PRINCIPAL|ACUMULACAO|SUBSTITUICAO
  origem               VARCHAR(20) NOT NULL, -- ADMISSAO|PROGRESSAO|PROMOCAO|MOBILIDADE|TRANSFERENCIA
  origin_assignment_id UUID,               -- mobilidade temporária: afectação a restaurar
  data_inicio          DATE NOT NULL,
  data_fim             DATE,               -- null = corrente
  is_current           BOOLEAN NOT NULL DEFAULT true,
  is_active            BOOLEAN NOT NULL DEFAULT true,
  notes                TEXT,
  created_at TIMESTAMP, created_by VARCHAR(255),
  updated_at TIMESTAMP, updated_by VARCHAR(255)
);
-- UNIQUE(position_id) WHERE is_current  (um ocupante por cadeira)
-- t_assignment_aud (Envers) criada na mesma migração
```

> **Envers:** as entidades são `@Audited` → a migração cria também `t_position_aud` e `t_assignment_aud`. Está na checklist (senão o Hibernate falha no arranque).

---

## 4. Script de limpeza — `scripts/cleanup/`

Fora do Flyway. Idempotente e defensivo: só toca no que existe; seguro a correr N vezes. Por defeito limpa **só** os dados de movimentos — não mexe em funcionários nem contratos.

```sql
-- scripts/cleanup/clean_movimentos.sql
DO $$
BEGIN
  IF to_regclass('public.t_assignment') IS NOT NULL THEN
    TRUNCATE TABLE t_assignment RESTART IDENTITY CASCADE;
  END IF;
  IF to_regclass('public.t_position') IS NOT NULL THEN
    TRUNCATE TABLE t_position RESTART IDENTITY CASCADE;
  END IF;
  IF to_regclass('public.t_employee_professional_assignments') IS NOT NULL THEN
    TRUNCATE TABLE t_employee_professional_assignments CASCADE;
  END IF;
  IF to_regclass('public.t_employee_unit_assignments') IS NOT NULL THEN
    TRUNCATE TABLE t_employee_unit_assignments CASCADE;
  END IF;
  RAISE NOTICE 'Movimentos limpos — pronto para arranque Position Management.';
END $$;
```

Correr:
```bash
docker exec -i postgres-ingt-rh psql -U postgres -d recursoshumanos_db < scripts/cleanup/clean_movimentos.sql
```

> Reset mais fundo (incl. funcionários/contratos para demo do zero) = segundo script `clean_colaboradores.sql` no mesmo padrão. Por agora só o de movimentos.

---

## 5. TODO — passo a passo (tracker: `TODO_Fase1_Position.md`)

### Fase 1A — Schema + limpeza + domínio ✅ CONCLUÍDA
- [x] **1A.1** Criar branch `feat/position-management` a partir de `dev`.
- [x] **1A.2** Migração Flyway defensiva `V30__position_assignment.sql`: `t_position` + `t_assignment` + índices únicos parciais + tabelas `_aud`. Testada idempotente (2ª corrida = só skips).
- [x] **1A.3** Script `scripts/cleanup/clean_movimentos.sql` (idempotente, defensivo).
- [x] **1A.4** Entidades JPA: `PositionEntity` (estrutura), `AssignmentEntity` (`@Entity(name="ColabsAssignmentEntity")`).
- [x] **1A.5** Domínio + VOs `PositionId`/`AssignmentId` + mappers + ports + spring-data repos + adapters.
- [x] **1A.6** Build verde (JDK 23) + migração aplica e verifica na BD (colunas/índices/`_aud` OK).

### Fase 1B — Lógica + endpoints
- [x] **1B.1** Mapa de Pessoal CRUD (criar/editar/congelar/extinguir; chefia via `managesUnitId`). PositionController + DTOs + commands/queries + handlers. Build verde.
- [x] **1B.2** Afectação: `AssignmentService.afectar()` (SCD2 fecha-antes-de-abrir; valida ocupável/ocupado/grelha) + AfectarColaboradorCommand+Handler + AssignmentController. **Testado 12/12.** Fix `saveAndFlush` para ordenação Hibernate.
- [x] **1B.3** Repoint `RegistarColaboradorCommandHandler` — bloco `afectacao` (positionId) via AssignmentService; `enquadramento` marcado `@Deprecated`. Aditivo, build verde. **A testar (requer restart).**
- [x] **1B.4** Repontar mobilidade (`AprovarLicencaMobilidade…`): adicionado `destinationPositionId` à licença (coluna nullable via ddl-auto, +`_aud`); aprovação de MOBILIDADE chama `AssignmentService.afectarMobilidade()` (fecha afectação corrente, abre no Lugar de destino, `origem=MOBILIDADE`, escalão herdado). Removida escrita na `colocacao` deprecada. **Testado 9/9** (drift-free U1→U2, A vago/B ocupado, sem Lugar destino => 422). Nota: `/close` (mobilidade temporária, restaura via `origin_assignment_id`) fica no caminho legado — pós-fase-1.
- [x] **1B.5** Repontar `MudarEstadoColaborador…` (RETIRED/INACTIVE fecham a afectação corrente via `AssignmentService.encerrarAfectacaoCorrente`; Lugar volta a vago). **Testado 5/5.**
- [x] **1B.6** Queries: responsável-unidade, chefe-funcionário, unidade-atual, vagas. Testadas.

### Fase 1C — Regenerar + testar
- [x] **1C.1** Manifests `.igrpstudio`: models (PositionEntity, AssignmentEntity), DTOs (PositionRequest/Response/WrapperListaPosition, AfectacaoRequest), controllers (Position, Assignment).
- [x] **1C.2** Smoke tests: app arranca (porta 8099), migração V30 aplicada, endpoints respondem.
- [x] **1C.3** Testes de endpoint fluxo novo: **12/12 PASS** (criar Lugar → afectar → ocupado 422 → mobilidade drift-free → vagas → responsável → fora-de-grelha).
- [x] **1C.4** Não-regressão: app arranca com todos os módulos (sigdi incl.); GET jobs/unidades/functions/positions(?unidadeId)/careers/funcionarios → 200 (6/6). Path careers confirmado: `api/v1/rh/careers` (módulo `carreiras`). Registo-com-afectacao testado após restart (Teste 2, 6/6).
- [ ] **1C.5** Relatório de testes consolidado.

### Fase 2 — Gate
- [ ] **2.1** Documento-relatório para o frontend (contrato de API novo).
- [ ] **2.2** Com tudo verde + autorização: merge `feat/position-management → dev`.

---

## 6. Testes — fumo, endpoint e não-regressão

Perfil `development` (segurança OFF, **sem auth**), porta 8091, BD `localhost:5436`. Testes por `curl`/HTTP.

### Funcionalidade nova (tem de passar)
| Caso | Acção | Esperado |
|---|---|---|
| Criar Lugar | POST mapa-pessoal | 201; Lugar `ATIVO`, vago |
| Registar colaborador | POST registo com `positionId` vago | 201; afectação criada; Lugar provido |
| Lugar ocupado | registar 2.ª pessoa no mesmo Lugar | 422 (uma cadeira, um ocupante) |
| Mobilidade | aprovar mobilidade p/ Lugar de outra unidade | afectação antiga fechada, nova corrente; unidade muda sem drift |
| Vagas | GET vagas/{unidade} | dotação − ocupados, exacto |
| Responsável / chefe | GET responsavel-unidade, chefe-funcionario | ocupante correcto (ou "vago") |
| Fora de grelha | registar tarefeiro (Lugar sem carreira) | 201; sem escalão; sem validação PCFR |

### Não-regressão (não pode partir)
- Funcionário: criar/editar/listar/estado — inalterado.
- Contrato, dependentes, dados bancários, documentos — inalterado.
- Ausências / licenças / saldos — inalterado.
- `sigdi/`: compila; `FuncionarioLookupAdapter` e `OrganicaLookupAdapter` devolvem o mesmo; `GET unidade-atual` = mesmo UUID que a colocação devolvia.
- Flyway: histórico consistente; migração aplica-se limpa em BD nova.

---

## 7. Gate de merge

Merge para `dev` **só quando**: build verde · migração aplica limpa em BD nova · todos os testes de funcionalidade nova passam · zero regressões (incl. sigdi compila e responde) · relatório de testes verde · relatório de frontend entregue · **e autorização explícita**.

As tabelas antigas **não** são largadas neste merge — ficam deprecadas. O DROP é fase posterior.

---

## 8. Relatório para o frontend (entregável final)

Escrito **depois** de endpoints implementados e testados (reflecte o contrato real):
- **Registo:** deixa de enviar unidade/cargo/carreira/categoria → envia `positionId` + `gradeId`.
- **Ecrãs novos:** gestão do Mapa de Pessoal (criar Lugares, marcar chefias, ver vagas).
- **Picker de Lugar vago** na admissão (unidade/cargo/carreira só-leitura, derivados).
- **Endpoints deprecados** (enquadramento/colocação) → Afectação; tabela mapeamento antigo→novo.
- **Queries novas:** responsável-unidade, chefe-funcionário, vagas, mapa-pessoal.
- Contratos request/response com exemplos JSON.

---

## 9. Casos cobertos pelo Position (referência)

| Caso | Cobertura |
|---|---|
| Carreira PCFR | Lugar rico + afectação com escalão |
| Fora de grelha (tarefeiro) | Lugar sem carreira; afectação sem escalão |
| Dirigente (comissão) | Lugar de chefia + `manages_unit_id`; duas cadeiras via `assignment_type` |
| Progressão de escalão | nova afectação, mesmo Lugar, `origem=PROGRESSAO` |
| Promoção | novo Lugar, `origem=PROMOCAO` |
| Mobilidade permanente/temporária | nova afectação; temporária via `origin_assignment_id` |
| Acumulação / substituição | `assignment_type=ACUMULACAO`/`SUBSTITUICAO` |
| Vaga / congelado / extinto | derivado / `estado` |
| Responsável de unidade / chefia | `manages_unit_id` / `parent_position_id` |
| Cessação / reforma | fecha afectação; Lugar volta a vago |
| Dotação / quadro de pessoal | contar Lugares por unidade/carreira |
