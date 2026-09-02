> Updated: 2026-09-02 (sessão /resume #2) — **MERGE FEITO em `dev`** (--no-ff). Guard PCFR (escalão∈categoria) implementado + testado live (201/422) + mensagem de erro com nomes das categorias. Novo guia `apresentacao_aplicacao.html` (linguagem acessível, todo o âmbito não-sigdi). Docs v5 verificadas sem lacunas. **Falta só: push do `dev`** (não autorizado). Ver "Next step".
>
> Updated anterior: **Legado A+B+C CONCLUÍDO e testado (45/45)**. A: mobilidade repontada. B: CRUD standalone eliminado. C: `V31` larga 8 tabelas órfãs. Ver secções A/B/C do Inventário do LEGADO.

## Goal

Refactor dos "movimentos do colaborador": adoptar **Position Management (Mapa de Pessoal)** — criar `t_position` (Lugar) e fundir enquadramento+colocação numa `t_assignment` (Afectação). *Breaking change* assumido. Fundamentado em indústria (Oracle/Workday/SAP) + lei CV (PCFR DL 4/2024 + mapa de quadro de pessoal). **Plano aprovado pelo utilizador — implementação a decorrer.**

## Current state

- **Branch ATUAL: `dev`** (merge de `feat/position-management` feito). `dev` está À FRENTE de `origin/dev` — **push por fazer** (não autorizado). `feat/position-management` continua a existir (1 commit atrás de `dev`; os polimentos pós-merge — mensagem PCFR + guia apresentação — só estão em `dev`).
- **Plano APROVADO** (utilizador leu e deu luz verde). Fonte de verdade da execução:
  - `docs/funcionarios/v4/Plano_Implementacao_Position.md` (legível) + `.html` (visual, artifact 7cf9bc9c).
  - `docs/funcionarios/v4/ADR_Position_Management_Mapa_Pessoal.md` (ADR-002 — schema/decisões).
- **Estado do TODO — FASE 1A ✅ CONCLUÍDA:**
  - [x] 1A.1 branch criada.
  - [x] 1A.2 migração `V30__position_assignment.sql` (defensiva, idempotente — testada 2x = só skips; índices únicos parciais; `_aud`).
  - [x] 1A.3 `scripts/cleanup/clean_movimentos.sql` (idempotente).
  - [x] 1A.4 `PositionEntity` (estrutura/), `AssignmentEntity` (`@Entity(name="ColabsAssignmentEntity")`).
  - [x] 1A.5 `PositionId`/`AssignmentId`, `Position`/`Assignment` (domínio), mappers, ports (`PositionRepository`/`AssignmentRepository`), spring-data repos, adapters.
  - [x] 1A.6 build verde (JDK 23) + migração aplicada e verificada na BD dev.
  - **FASE 1B em curso:**
    - [x] 1B.1 Mapa de Pessoal CRUD (estrutura/): PositionController @ `api/v1/rh/estrutura/positions` (GET list?unidadeId, GET {id}, POST, PUT, PATCH {id}/freeze, DELETE {id}) + DTOs + Create/Update/Congelar/Extinguir + GetById/GetByUnidade. Build verde.
    - [x] 1B.2 Afectação (colaboradores/): `AssignmentService.afectar()` (valida podeSerOcupado + !isPositionOccupied + regra grelha + SCD2 fecha-antes-de-abrir) + AfectarColaboradorCommand+Handler + AssignmentController @ `api/v1/rh/colaboradores/assignments` (POST afectar; GET funcionario/{id}/unidade-atual, /chefe; GET unidade/{id}/responsavel, /vagas) + 4 query handlers. Build verde.
    - **BUG corrigido:** SCD2 dava `duplicate key ux_assignment_funcionario_current_principal` (Hibernate ordena inserts antes de updates). Fix: `AssignmentRepositoryImpl.save` usa `saveAndFlush` (fecha antigo antes de inserir novo). A re-testar.
    - [x] 1B.3 repoint `RegistarColaboradorCommandHandler`: registo usa **só `afectacao`** (→ AssignmentService). **`enquadramento` REMOVIDO** do request/response de registo; `enquadramentoId` saiu, `afectacaoId` entrou. Build verde. **A testar (requer restart da app).** Nota: `EnquadramentoController`/`Service` standalone continuam a existir (deprecados), fora do registo — limpeza separada futura.
    - [x] 1B.6 queries (unidade-atual/responsavel/chefe/vagas) — testadas.
    - [x] 1B.5 estado (`MudarEstadoColaborador`): RETIRED/INACTIVE → `AssignmentService.encerrarAfectacaoCorrente()` (fecha SCD2 a afectação corrente; removida dependência de `ColabsColocacaoEntityRepository`). Build verde. **Testado 5/5** (INACTIVE fecha afectação → unidade-atual 404 → Lugar volta a vago → novo registo 201).
    - [x] 1B.4 mobilidade (`AprovarLicencaMobilidade`): +`destinationPositionId` na licença (coluna nullable ddl-auto +`_aud`); aprovação MOBILIDADE → `AssignmentService.afectarMobilidade()` (escalão herdado da afectação corrente); removida escrita `colocacao`. **Testado 9/9** (drift-free U1→U2). `/close` (mobilidade temporária, `origin_assignment_id`) fica legado — pós-fase-1.
    - [x] 1C.1 manifests `.igrpstudio` (models/dtos/controllers Position+Assignment).
  - **Smoke/endpoint tests (1C.2/1C.3): ✅ 12/12 PASS** após o fix. Cobre: criar Lugar1/2, afectar F1->L1, ocupado=>422, unidade-atual==U1, vagas(1/1/0), MOBILIDADE F1->L2, unidade-atual==U2 **DRIFT-FREE**, vagas U1 volta(0/1), responsavel U2==F1 (via manages_unit_id), fora-grelha+escalão=>422, fora-grelha sem escalão=>201. IDs teste: JOB=1d06306e… U1(DeptoRH)=4ccdd06f… U2(DG)=d55418e7… GRADE=f241273a… CAT=b873943f… CAREER=a567155e… F1=4ed92d96… F2=80006610…
  - **Não-regressão:** app arranca com todos os módulos (sigdi incl.) sem erro; jobs/unidades/positions list 200. (Careers list foi URL errada no teste, não regressão — reconfirmar path do CareerController.)
  - App a correr em background (porta 8099) via `mvn spring-boot:run`, log `target/app3.log`.
  - Depois 1C (manifests .igrpstudio + re-teste), 2 (gate merge + relatório frontend + HTML revisão).
- **PRÉ-EXISTENTE resolvido:** app não arrancava por `criteria_superado` (varchar→numeric) em `t_key_results`/`t_strategic_indicators` (+_aud) — bug do sigdi/strategic goals, NÃO deste refactor. Colunas eram todas NULL → ALTER TYPE numeric(19,2) aplicado às 4. App agora arranca com `ddl-auto=update` normal.
- **Ficheiros 1A criados** (16): migração + script limpeza + 7 estrutura (PositionEntity/Id/Position/PositionMapper/PositionEntityRepository/PositionRepository/PositionRepositoryImpl) + 7 colaboradores (Assignment*...).
- Migração já aplicada manualmente à BD dev (via psql) — Flyway voltará a corrê-la no arranque (idempotente, sem conflito).
- BD: `docker exec postgres-ingt-rh psql -U postgres -d recursoshumanos_db`. Dados: 8 enquadramentos, 1 colocação, 20 funcionários, 10 unidades, 5 t_job, 6 career, 6 category, 4 grade. (Existe `t_cargo` 1 linha legado; entidades usam `t_job`.)

## Decisions made — do not re-litigate

- **Position Management** (não Job Management). 1 Lugar = 1 cadeira. Lugar rico (carreira+categoria). `grade` na Afectação.
- **Arranque limpo, SEM backfill** (app no início; limpam-se dados de movimentos de dev). Mas **manter script de limpeza** em `scripts/cleanup/`.
- **Tabelas antigas deprecadas, NÃO largadas** nesta fase (rede de segurança; DROP futuro).
- **Migração defensiva + idempotente** (`IF NOT EXISTS`, blocos `DO $$`); backfill NUNCA no Flyway.
- **Chefia:** `parent_position_id` (reporte) + `manages_unit_id` na posição (SAP Manages_Org_Unit). Estado provido/vago DERIVADO. `estado` só ATIVO|CONGELADO|EXTINTO.
- **Camadas:** Lugar em `estrutura/`, Afectação em `colaboradores/`.
- **NÃO tocar** `t_unidade_organica` nem `sigdi/`.
- **Automatismo do dirigente** (regresso ao lugar de origem em fim de comissão) = **pós-fase-1**. Modelo reserva espaço (`assignment_type`, `origin_assignment_id`).
- Sem SpecKit — segue-se o TODO do plano; cada passo atualiza este handoff.

## Constraints

- pt-PT. Commits conventional (`feat(estrutura):`/`feat(colaboradores):`).
- Domain Identity Pattern (`PositionId`/`AssignmentId`, nunca `ExternalID`). Referência: `sigdi/domain/admin/valueobject/InstitutionId.java`.
- Entities/controllers GENERATED BY iGRP STUDIO — lógica nos handlers; regenerar via `.igrpstudio/`.
- Convenções `colaboradores/`: `@Entity(name="Colabs...")`, prefixo `Colabs` em repos, bean names explícitos `@Repository("colabs...")`/`@Component("colabs...")`. Soft delete + Envers (`@Audited` → tabela `_aud` na migração).
- JPQL: `year(field)` (não `FUNCTION('YEAR',...)`).
- Testes: perfil `development` (sem auth), porta 8091.

## Documentação v5 (EM CURSO — 2026-09-02)

Nova pasta `docs/funcionarios/v5/` (supersede v4, que fica como arquivo). Âmbito: **tudo menos sigdi**. HTML mistura diagramas (Mermaid via CDN) + texto; pt-PT. **Sem HTML de revisão** (dispensado). Atualizar handoff a cada doc.

Entregáveis: `modelo_negocio.html` · `modelo_relacional.html` · `api_guide.md` + `.html` · `breaking_change_frontend.md` + `.html`.

Base já mapeada do código (não-sigdi): **30 entities** em 5 módulos (carreiras, colaboradores, estrutura, parametrizacoes, shared). Só `ContratoEntity` usa `@ManyToOne`/`@JoinColumn` (funcionario_id→FuncionarioEntity, contract_type_id→ContractTypeEntity); todas as outras usam colunas UUID (`*_id`) sem objeto — relações inferidas por nome. Catálogos `LeaveType`/`DocumentType` usam `category` como **String** (não FK Option, apesar do CLAUDE.md). Endpoints: todos os controllers non-sigdi já listados (bases `api/v1/rh/...`).

**Progresso:**
- [x] `modelo_negocio.html` — domínios/BC, conceitos, Position Management, PCFR, ciclo de vida, movimentos (com regresso de mobilidade), ausências, glossário. Diagramas Mermaid (flowchart/state/sequence).
- [x] `modelo_relacional.html` — convenções (UUID sem FK física exceto t_contrato; soft-delete; SCD2; Envers), vista central Lugar/Afectação/Funcionário, ER por bloco (estrutura+carreiras, colaborador, ausências, docs, catálogos), referência das 30 tabelas com FKs. Nota real: t_leave_type/t_tipo_documento usam `category` String. Doc polimórfico `t_document.reference_id`.
- [x] `api_guide.md` + `.html` — base/auth/headers, convenções (paginação `pagina`/`tamanho`, wrapper, padrões CRUD/combobox/audit, códigos 422/404/400), todos os grupos: positions, assignments, registar, funcionário+sub-recursos, licenças/mobilidade, carreiras, catálogos, /me, auditoria (catálogo `assignments`), enums, deprecados. HTML com mapa de endpoints + sequência de admissão.
- [x] `breaking_change_frontend.md` + `.html` — TL;DR, registo (antes/depois), picker de Lugar vago, gestão Mapa de Pessoal, novas queries, afectação direta, mobilidade (`destinationPositionId`), worker-state, endpoints removidos, enums, checklist FE. HTML com before/after + sequência de mobilidade.

**✅ DOCUMENTAÇÃO v5 CONCLUÍDA E VERIFICADA** (5 ficheiros em `docs/funcionarios/v5/`). v5 aponta que supersede v4 (v4 intacta, sem banner). Commit inicial `3713afc`.
- **Passagem de verificação vs código (2026-09-02):** confirmados V30 (índices `ux_assignment_position_current`/`ux_assignment_funcionario_current_principal`, estado default ATIVO), `RegistarColaboradorRequestDTO` (funcionario/contrato/afectacao/dadosBancarios/dossier), `AfectacaoRequestDTO`, `PositionResponseDTO`, catálogos de audit por módulo. **Corrigidas 4 imprecisões:** colunas audit `created_date`/`last_modified_date` (não `_at`); `dossier` é **lista**; `combobox` devolve `{key,label}` (não `{id}`); wrappers variam (funcionário sem `first/last`, position com `dotacao/ocupados/vagas`). Commit das correções a seguir.

## Fase 2 — Relatório para o frontend (PENDENTE — escrever depois)

Adiado a pedido do utilizador. Material já compilado para escrever rápido (contrato REAL, testado):

**Registo** `POST /api/v1/rh/funcionarios/registar` — body `{ funcionario, contrato?, afectacao{positionId,gradeId,functionId?,origem?,assignmentType?,dataInicio?,notes?}, dadosBancarios?, dossier? }`; resposta tem `afectacaoId` (NÃO `enquadramentoId`). Já não se envia unidade/cargo/carreira/categoria — derivam do Lugar.

**Mapa de Pessoal (Lugares)** `estrutura/positions`:
- `GET /positions?unidadeId={id}` → `WrapperListaPositionDTO{content[PositionResponseDTO],totalElements,dotacao,ocupados,vagas}`
- `GET /positions/{id}`; `POST /positions` (`PositionRequestDTO{numeroLugar,jobId,unidadeOrganicaId,careerId?,categoryId?,parentPositionId?,managesUnitId?,legalBase?}`); `PUT /positions/{id}`; `PATCH /positions/{id}/freeze`; `DELETE /positions/{id}`.
- `PositionResponseDTO`: id,numeroLugar,jobId,**jobNome**,unidadeOrganicaId,**unidadeNome**,careerId,**careerNome**,categoryId,**categoryNome**,parentPositionId,managesUnitId,estado,legalBase,isActive,foraDeGrelha,ocupado.

**Afectação** `colaboradores/assignments`:
- `POST /assignments` (`AfectacaoRequestDTO{funcionarioId,positionId,gradeId,functionId?,origem,assignmentType?,dataInicio,notes?}`)
- `GET /funcionario/{id}/unidade-atual` → {funcionarioId,**funcionarioNome**,positionId,numeroLugar,unidadeOrganicaId,**unidadeNome**,jobId,**jobNome**}
- `GET /funcionario/{id}/chefe` → {funcionarioId,funcionarioNome,chefePositionId?,chefeFuncionarioId?,**chefeNome?**,estado}
- `GET /unidade/{id}/responsavel` → {unidadeId,**unidadeNome**,positionId?,numeroLugar?,responsavelFuncionarioId?,**responsavelNome?**,estado}
- `GET /unidade/{id}/vagas` → {unidadeId,dotacao,ocupados,vagas}
- `GET /unidade/{id}/vagas/lista` → `WrapperListaPositionDTO` (Lugares VAGOS com nomes — **picker de admissão**)

**Mobilidade**: `LicencaMobilidadeRequestDTO` ganha `destinationPositionId` (obrigatório p/ MOBILIDADE ao aprovar/ativar, senão 422). `PUT .../licencas-mobilidade/{id}/approve`.
**Estado**: `PATCH .../worker-state` — RETIRED/INACTIVE encerram a afectação (Lugar volta a vago). Sem alteração de request.

**Enums**: origem=ADMISSAO|PROGRESSAO|PROMOCAO|MOBILIDADE|TRANSFERENCIA; assignmentType=PRINCIPAL|ACUMULACAO|SUBSTITUICAO; estado(Lugar)=ATIVO|CONGELADO|EXTINTO. PROVIDO/VAGO é DERIVADO (campo `ocupado`).
**Deprecados**: `/funcionarios/{id}/enquadramentos`, `/funcionarios/{id}/colocacoes` → substituídos por Afectação.
**Ecrãs FE novos**: gestão Mapa de Pessoal (criar Lugares, chefias managesUnitId/parentPositionId, ver vagas); picker de Lugar vago na admissão (unidade/cargo/carreira só-leitura, derivados).
**Notas**: enviar `Accept: application/json`. Erros 422: Lugar ocupado / não disponível / grelha (escalão obrigatório/proibido) / mobilidade sem Lugar destino.

Entregar como doc `docs/funcionarios/v4/` + (opcional) artifact HTML. Formato de revisão HTML do que foi feito também pendente.

## Inventário do LEGADO (breaking change — varrimento 2026-09-01)

**A) Consumidores ACTIVOS do modelo antigo — ✅ REPONTADOS para o novo modelo (2026-09-01):**

1. ✅ `estrutura/GetOrganizationalUnitByIdQueryHandler` + `GetOrganizationalUnitsQueryHandler` — `nColaboradores` passa a contar Lugares da unidade com afectação corrente (`PositionRepository.findByUnidade` + `AssignmentRepository.isPositionOccupied`). **Testado A1** (headcount +1 após afectar). Removida dependência de `ColabsColocacaoEntityRepository`.
2. ✅ `colaboradores/GetColaboradorDetailsQueryHandler` — bloco `enquadramento` construído da afectação corrente + Lugar (mesma forma de resposta, por compat). **Testado A2** (cargoName/unitName/gradeName preenchidos p/ colaborador novo).
3. ✅ `colaboradores/GetMeProfileQueryHandler` — `/me` unit/job/career/category do Lugar, grade da afectação. Compilado (precisa auth-context p/ teste live).
4. ✅ `colaboradores/CloseContratoCommandHandler` — encerra a afectação corrente (`AssignmentService.encerrarAfectacaoCorrente`) em vez do enquadramento. Compilado.
5. ✅ `AtivarLicencaMobilidadeCommandHandler` (`/ativar`) — deixa de escrever `Colocacao(unit=null)`; usa `AssignmentService.afectarMobilidade` no mesmo caminho do `/approve`. **Testado A5** (200, sem erro de colocação). Nota: `/ativar` é reactivação — só dispara com `isActive=false`; caminho principal de mobilidade é `/approve` (9/9).
6. ✅ `EncerrarLicencaMobilidadeCommandHandler` (`/close`) e `CancelarLicencaMobilidadeCommandHandler` (`/cancel`) — **REPONTADOS (2026-09-02, Bloco A)** para `AssignmentService.regressarDeMobilidade()` (fecha a afectação de MOBILIDADE corrente + reabre o Lugar de origem via `origin_assignment_id`, se ainda vago; herda escalão/função). `afectarMobilidade` passou a registar `origin_assignment_id` (a afectação corrente antes do fecho) para permitir o regresso. Removida toda a dependência de `ColocacaoRepository`. Build verde. **TESTADO LIVE (2026-09-02): Bloco A 12/12** (afectar→approve→U2 drift-free→/close→REGRESSO a U1/L1 via origin_assignment_id→L2 volta a vago) + **regressão 13/13** (GETs jobs/unidades/functions/careers/positions/funcionarios 200; ocupado 422; fora-de-grelha 422/201; `/cancel` também regressa à origem).
   - Nota: motor de regresso é intencionalmente simples (só reabre se origem ainda vaga); mobilidade permanente (sem origin) apenas fecha a corrente.

**B) CRUD standalone DEPRECADO — ✅ ELIMINADO (2026-09-02, Bloco B):**
- **58 ficheiros apagados via `git rm`** (47 `.java` Enquadramento*/Colocacao*/`TipoAfectacao` + 11 manifests `.igrpstudio/colaboradores/`). Inclui `GetColocacoesByFuncionarioQuery(+Handler)` (não foi apanhado pelo 1.º glob `*Colocacao*` — casa `Colocacoes`; apagados à parte). Compile verde.
- **2 consumidores externos de `EnquadramentoEntity` repontados ANTES de apagar:**
  1. `FuncionarioRepositoryImpl.toSpec` — filtro de lista por `unidadeOrganicaId`/`careerId` deriva agora do Lugar via Afectação corrente (subquery aninhada `AssignmentEntity` where `isCurrent` + `positionId IN (PositionEntity where unidade/career=…)`). **Testado** (filtro U1 inclui / U2 exclui / career inclui). NB: filtro de lista aplica `isActive=true` no funcionário — F1 `4ed92d96` está inativo, usar F2 `8136de50` (ACTIVE) para testar.
  2. `AuditHistoryRepositoryImpl` — catálogo `enquadramentos`→`assignments` (mapeia `AssignmentEntity`). **Testado** (`GET .../colaboradores/audit/assignments/{assignmentId}`=200; catálogo antigo `enquadramentos`=400). Descrição Swagger no `ColaboradoresAuditHistoryController` também atualizada.
- **RETIDO (compat, NÃO apagar):** `EnquadramentoResponseDTO.java` (+ manifest) — ainda é o **shape de resposta** do bloco `enquadramento` em `ColaboradorDetailsResponseDTO`/`GetColaboradorDetailsQueryHandler` (repoint A2, por compat FE). Restaurado após eliminação acidental.
- **RETIDO (histórico):** `V10__enquadramento.sql` (Flyway — apagá-la parte a validação) e `docs/funcionarios/v4/ADR_Modelo_Movimentos_Enquadramento_Colocacao.md` (ADR antigo).

**C) Tabelas legadas órfãs — ✅ LARGADAS (2026-09-02, migração `V31__drop_legacy_orphan_tables.sql`):**
- Varrimento completo (69 tabelas public vs 58 `@Table` de entities). Órfãs = sem entity, sem FK viva, não criadas por Flyway (todas materializadas por `ddl-auto`). Diretiva do utilizador: "se nenhuma entity aponta, não precisamos delas".
- **Largadas (6 public + 2 `_aud`):** `t_contrato_entity`(0, FK→t_cargo), `t_cargo`(1, ex-`t_job` legado), `t_documento`(0, ex-`t_document`), `t_professional_situation`(7, catálogo dormente s/ entity), `t_employee_professional_assignments`(0)+`_aud`, `t_employee_unit_assignments`(0)+`_aud`.
- **Dump de segurança:** `scripts/cleanup/backup_legacy_before_drop_2026-09-02.sql` (data-only, 87 linhas).
- Migração `DROP TABLE IF EXISTS … CASCADE` — idempotente (aplicada 2x = ok; no-op em BD nova). **Testada:** app arranca, Flyway regista V31 (success), `ddl-auto` NÃO recria, regressão 13/13. public 69→63.
- Mantidas (não órfãs): `flyway_schema_history`, 4× `t_siadap_interim_*_aud` (Envers de entities vivas), todas as `_aud` ativas.
- **DRIFT resolvido:** `fn_apply_mobility`/`fn_validate_professional_assignment` já não existiam na BD.
- Nota: `scripts/cleanup/clean_movimentos.sql` ainda tem branches `to_regclass` p/ `t_employee_*` (agora no-op inofensivo).

> Ordem de limpeza: **A** ✅ → **B** ✅ → **C** ✅ (schema órfão largado). Legado 100% removido — código e schema.

## Pendente (pós-fase-1, não bloqueia merge)

- ~~**Validação PCFR — escalão ∈ categoria do Lugar**~~ ✅ RESOLVIDO (2026-09-02): guard em `AssignmentService.afectar()` — quando `gradeId != null`, carrega o `Grade` (via `GradeRepository`, port de `carreiras/`) e devolve 422 se `grade.getCategoryId().getValor() != position.getCategoryId()`. Cobre registo/afectação directa/mobilidade. **Testado live 2/2** (escalão da categoria → 201; de outra categoria → 422 "O escalão não pertence à categoria do Lugar"). Commit `eb00908`.


- ~~**Mobilidade temporária / `/close`**~~ ✅ RESOLVIDO (2026-09-02, Bloco A): `/close` e `/cancel` reponteados para `AssignmentService.regressarDeMobilidade()` (reabre `origin_assignment_id`). Ver secção A ponto 6.

## Blockers & risks

- Migração tem de criar tabelas `_aud` (Envers) senão Hibernate falha no arranque — verificar padrão em migração existente.
- Gate de merge: parar antes do merge; só com tudo verde + autorização.
- Contrato de API breaking (registo → `positionId`) — relatório frontend no fim.

## Relevant files

- `docs/funcionarios/v4/Plano_Implementacao_Position.md` — **plano/TODO, ler primeiro**.
- `docs/funcionarios/v4/ADR_Position_Management_Mapa_Pessoal.md` — schema/decisões (ADR-002).
- `.../estrutura/.../entity/{Job,OrganizationalUnit,Function}Entity.java` — padrão para `PositionEntity` (estrutura/).
- `.../colaboradores/.../entity/{Enquadramento,Colocacao}Entity.java` — fundem-se em `AssignmentEntity`.
- `.../colaboradores/application/commands/{RegistarColaborador,AprovarLicencaMobilidade,MudarEstadoColaborador}CommandHandler.java` — repontar (1B).
- `src/main/resources/db/migration/` — migrações Flyway (ver última versão + padrão `_aud`). `V10__enquadramento.sql` = analog.
- `sigdi/infrastructure/lookup/{Funcionario,Organica}LookupAdapter.java` — não-regressão (1C.4).

## How to verify / resume

- Ler o plano `.md`. Branch `feat/position-management`.
- **JDK 23 obrigatório** (release 23). `JAVA_HOME` do sistema aponta p/ jdk-21 (falha). Usar: `C:\Program Files\Eclipse Adoptium\jdk-23.0.2.7-hotspot`. PowerShell: `$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-23.0.2.7-hotspot"; mvn -q -DskipTests compile`.
- Run: `docker-compose up` → `java -jar target/RH-Service-0.0.1-SNAPSHOT.jar` (perfil development, 8091, sem auth).
- BD: `docker exec -i postgres-ingt-rh psql -U postgres -d recursoshumanos_db` (container `postgres-ingt-rh`, porta 5436).
- Aplicar migração à mão: `Get-Content src\main\resources\db\migration\V30__position_assignment.sql -Raw | docker exec -i postgres-ingt-rh psql -U postgres -d recursoshumanos_db`.
- Limpar movimentos: `Get-Content scripts\cleanup\clean_movimentos.sql -Raw | docker exec -i postgres-ingt-rh psql -U postgres -d recursoshumanos_db`.

## Open questions

- Nenhuma a bloquear. Docs/ADRs/HTMLs ainda untracked — commit não autorizado (fazer no fim, ou quando o utilizador pedir).

## Test / validation plan (para a próxima sessão)

**IMPORTANTE:** a app em background (porta 8099, log `target/app3.log`) corre classes ANTIGAS. Para testar o repoint do registo (1B.3), **reiniciar a app**:
```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-23.0.2.7-hotspot"
Get-CimInstance Win32_Process -Filter "Name='java.exe'" | ? { $_.CommandLine -match "RH-Service|spring-boot|RecursosHumanos" } | % { Stop-Process -Id $_.ProcessId -Force }
Set-Location "C:\Users\ivanick.santos\Nick-personal\ta-workspace\projects\Recursos_Humanos"
mvn -q -DskipTests compile
Start-Process mvn -ArgumentList "-DskipTests","spring-boot:run" -RedirectStandardOutput "target\app4.log" -RedirectStandardError "target\app4.err.log" -WindowStyle Hidden
# aguardar "Tomcat started on port 8099" em target\app4.log
```

**Teste 1 — endpoints do modelo Position (script completo já validado 12/12):** ver o bloco PowerShell no histórico desta sessão (limpa movimentos → cria 2 Lugares → afecta → 422 ocupado → unidade-atual → vagas → mobilidade drift-free → responsável → fora-de-grelha). IDs teste na secção Current state.

**Teste 2 — registo com afectação (NOVO, 1B.3, precisa restart):** POST `http://localhost:8099/api/v1/rh/colaboradores/registar` (confirmar path exacto no `FuncionarioController`/`RegistarColaboradorController`) com body incluindo bloco `afectacao:{positionId, gradeId, origem:"ADMISSAO"}` (sem `enquadramento`). Esperado: 201 com `afectacaoId` preenchido; NÃO deve existir `enquadramentoId` na resposta. Criar primeiro um Lugar vago e usar um funcionário/NIF novo.

**Teste 3 — não-regressão:** GET jobs/unidades/positions (200); reconfirmar path do CareerController (o teste anterior usou URL errada).

**Limpeza entre corridas:** `Get-Content scripts\cleanup\clean_movimentos.sql -Raw | docker exec -i postgres-ingt-rh psql -U postgres -d recursoshumanos_db`.

## Next step

**CONCLUÍDO nesta sessão (2026-09-02):**
- **Bloco A** (consumidores activos do legado): `/close`+`/cancel` de mobilidade reponteados para `AssignmentService.regressarDeMobilidade()` (regresso ao Lugar de origem via `origin_assignment_id`; `afectarMobilidade` grava origem). **Live 12/12 + regressão 13/13.**
- **Bloco B** (CRUD standalone deprecado): 58 ficheiros eliminados; 2 consumidores externos de `EnquadramentoEntity` repontados (filtro de funcionários por unidade/carreira; catálogo audit `enquadramentos`→`assignments`); `EnquadramentoResponseDTO` retido por compat. **Live 7/7.** Compile verde.
- **Total 32/32 no build final.** App a correr na porta 8099 (log `target/appB2.log`). BD limpa.
- Nota tooling: PowerShell 5.1 → `Invoke-WebRequest -UseBasicParsing` + `Accept: application/json`. Scripts de teste no scratchpad (`test_blocoA.ps1`, `test_blocoB_filter.ps1`, `test_regressao.ps1`).
- **COMMITADO** (branch `feat/position-management`): `8306216` refactor(colaboradores): regresso de mobilidade via afectacao (Bloco A) + `ea3b3d0` refactor(colaboradores): eliminar CRUD standalone enquadramento/colocacao (Bloco B). `settings.json`/`data/` deixados fora. Ainda por fazer push (não autorizado).

- **Bloco C CONCLUÍDO** (2026-09-02): `V31__drop_legacy_orphan_tables.sql` larga 8 tabelas órfãs (6 public + 2 `_aud`); dump em `scripts/cleanup/`. Testado (app arranca, Flyway V31 success, sem recriação, regressão 13/13). **Por commitar** (V31 + dump + handoff).

**CONCLUÍDO na sessão /resume #2 (2026-09-02):**
- **Guard PCFR** (escalão∈categoria) em `AssignmentService.afectar()`: injeta `GradeRepository`+`CategoryRepository` (ports de `carreiras/`); 422 se `grade.categoryId != position.categoryId`. Cobre registo/afectação/mobilidade. **Testado live 2/2** (201 / 422). Commits `eb00908` (guard) + `48201c0` (mensagem com nomes das categorias).
- **Merge `feat/position-management → dev`** feito (`--no-ff`, 147 ficheiros). `settings*`/`data/` fora.
- **Relatório FE** consolidado: `docs/funcionarios/v5/breaking_change_frontend.md`+`.html` é o relatório oficial; completado shape da resposta de registo (commit `32de329`).
- **Novo guia `docs/funcionarios/v5/apresentacao_aplicacao.html`** (commit `08691a1`): apresentação em linguagem acessível para colegas/stakeholders, cobre TODO o âmbito não-sigdi (config apoio → conceitos → Mapa Pessoal → PCFR → ciclo de vida → movimentos → ausências → self-service → histórico/auditoria). 6 diagramas Mermaid.
- **Verificação de lacunas nas docs v5:** cruzadas com os 33 controllers não-sigdi — `modelo_negocio`/`modelo_relacional`/`api_guide` cobrem tudo; `breaking_change` é parcial por design. Sem lacunas.

**A seguir:**
1. **Push do `dev`** para o remoto — PENDENTE (não autorizado). Remotos: `origin` e `origin_git_lab` (ambos têm `dev`). Perguntar qual/quais.
2. **HTML de revisão do refactor** (documento-resumo do trabalho feito) — ainda pendente, opcional.
Cada passo → atualizar este handoff + checkboxes no plano `.md`.
