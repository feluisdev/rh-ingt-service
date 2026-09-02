> Updated: 2026-09-02 (sessão /resume) — **Bloco A + Bloco B do legado CONCLUÍDOS e testados live (32/32)**. Ver secções A (ponto 6) e B do Inventário do LEGADO.

## Goal

Refactor dos "movimentos do colaborador": adoptar **Position Management (Mapa de Pessoal)** — criar `t_position` (Lugar) e fundir enquadramento+colocação numa `t_assignment` (Afectação). *Breaking change* assumido. Fundamentado em indústria (Oracle/Workday/SAP) + lei CV (PCFR DL 4/2024 + mapa de quadro de pessoal). **Plano aprovado pelo utilizador — implementação a decorrer.**

## Current state

- **Branch:** `feat/position-management` (criada a partir de `dev @ 9bdaac0`). TODO a executar nela.
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

**C) Tabelas/funções BD antigas (NÃO largadas nesta fase — DROP em migração futura, ADR §M6):**
- `t_employee_professional_assignments`, `t_employee_unit_assignments` (presentes na BD). **DRIFT:** `fn_apply_mobility`/`fn_validate_professional_assignment` NÃO existem na BD dev (já não estão lá).

> Ordem de limpeza: **A** ✅ (consumidores activos) → **B** ✅ (CRUD standalone eliminado) → **C** ⏳ (DROP schema, pós-merge; manter dump antes).

## Pendente (pós-fase-1, não bloqueia merge)

- **Validação PCFR — escalão ∈ categoria do Lugar (EM FALTA):** `AssignmentService.afectar()` valida apenas presença do `gradeId` (obrigatório em Lugar de carreira; proibido fora de grelha), mas **não** valida que `grade.categoryId == position.categoryId`. O ADR-002 (§2.1 constraints / §3 admissão passo 4) prevê que o escalão escolhido tem de pertencer à categoria do Lugar. Hoje a API aceitaria um escalão de outra categoria. Frontend deve, entretanto, filtrar o picker de escalões pela categoria do Lugar (`grade.categoryId == position.categoryId`). Fix futuro: em `afectar()`, carregar o grade e rejeitar 422 se a categoria não bater. (Nota: `t_grade.category_id` → `t_category.career_id`.)


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
- **NÃO commitado ainda** — 68 ficheiros no working tree (58 D + repoints + handoff). Commit quando o utilizador autorizar.

**A seguir:**
1. **Bloco C** (opcional, pós-merge): migração de DROP das tabelas antigas `t_employee_professional_assignments`/`t_employee_unit_assignments` (manter dump antes). Não bloqueia.
2. **Fase 2:** **relatório para o frontend** (contrato novo: registar com `positionId`, endpoints Position/Assignment, deprecação enquadramento/colocação) + **HTML de revisão**. Depois, com luz verde, **merge `feat/position-management → dev`**.
Cada passo → atualizar este handoff + checkboxes no plano `.md`.
