> Updated: 2026-09-01 (sessão /resume) — 1B.5 concluído; Testes 2/3 + 1B.5 PASS.

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

## Pendente (pós-fase-1, não bloqueia merge)

- **Mobilidade temporária / `/close`**: o endpoint `PUT .../licencas-mobilidade/{id}/close` ("mobilidades restauram colocação anterior") continua no caminho **legado `colocacao`** (`EncerrarLicencaMobilidade…`/`ColocacaoRepository`). No novo modelo isto deve reabrir a afectação de origem via `origin_assignment_id` (o campo já existe em `t_assignment`). O ADR-002 (D6 / §3 Mobilidade / §9) marca o automatismo de regresso ao lugar de origem como **pós-fase-1**. Repointar quando se fizer o motor de regresso: `AssignmentService` fecha a afectação de MOBILIDADE e reabre a `origin_assignment_id`.

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

**CONCLUÍDO nesta sessão:** app reiniciada na porta 8099 com classes novas (JDK 23); **1B.5** implementado + testado; **Teste 2** (registo com afectacao) 6/6; **Teste 3** (não-regressão) 6/6; path careers confirmado = `api/v1/rh/careers`. Nota de tooling: em PowerShell 5.1 usar `Invoke-WebRequest -UseBasicParsing` + header `Accept: application/json` (senão devolve XML / NullReferenceException). Scripts de teste no scratchpad da sessão.

1. **1B.4** (opcional/transicional) repoint `AprovarLicencaMobilidadeCommandHandler` para usar `AssignmentService.afectar(origem=MOBILIDADE)` — o novo modelo já cobre mobilidade; handler antigo ainda escreve colocação deprecada.
2. **Fase 2:** escrever **relatório para o frontend** (contrato novo: registar com `positionId`, endpoints Position/Assignment, deprecação enquadramento/colocação) + **HTML de revisão** do que foi feito (o utilizador quer ler no fim). Depois, com luz verde, **merge `feat/position-management → dev`**.
Cada passo → atualizar este handoff + checkboxes no plano `.md`.
