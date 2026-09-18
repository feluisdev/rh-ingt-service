> Updated: 2026-09-17 21:46

## Goal

Alinhar o núcleo RH (não-sigdi) com a legislação cabo-verdiana, movimento a
movimento, com regras documentadas e testadas. Movimentos de carreira estão
fechados; falta **situação funcional do trabalhador**, **licenças que abrem
vaga**, **substituição**, limpeza dos **subtipos de licença/mobilidade** e o
negócio das **ausências (assiduidade)**.

## Current state

Branch `dev`, 6 commits desta sessão (mais 2 de outra sessão pelo meio):

- `0b2d2fe6` progressão · `60e1c8ae` promoção + respostas tipadas ·
  `73dcb177` transferência · `86e0444e` cessação unificada ·
  `876e484b` mobilidade transitória deixa de tirar o Lugar ·
  `d0d31cd9` "onde exerce funções" + prorrogação.
- Endpoints novos: `POST /funcionarios/{id}/progressao|promocao|transferencia`,
  `PUT /licencas-mobilidade/{id}/prorrogar`.
- Serviços novos: `CessacaoService`, `MobilidadeService`, `VinculoLaboralService`
  (colaboradores/application/services).
- Migrações: **V40** `t_worker_state.ends_employment`, **V41** limites do subtipo
  (`max_duration_days`, `max_extensions`) + `t_leave_mobility.extensions_count`,
  **V42** `t_worker_state.situacao_funcional` (por aplicar à BD).
  Próxima livre: **V43**.
- **Situações funcionais (ponto 1) implementadas**, por commitar: enum
  `parametrizacoes/domain/models/SituacaoFuncional.java` com os efeitos que a lei
  fixa (abre vaga, conta antiguidade, suspende vínculo, cessa vínculo); o estado
  do catálogo só guarda **qual** a situação. O `MudarEstadoColaboradorCommandHandler`
  deixou de olhar para os códigos `SUSPENDED`/`ACTIVE`: os efeitos derivam da
  situação, e a inactividade fora do quadro encerra a afectação (art. 121.º n.º 2).
  Seed com três estados novos (`ACTIVE_OUTSIDE`, `INACTIVE_OUTSIDE`, `AVAILABLE`).
  Testes: `SituacaoFuncionalTest` (14). Regras BR-SIT-01..08 em §4.1.
- Regras documentadas: BR-PRG-01..09 (§3.1), BR-PRM-01..10 (§3.2),
  BR-TRF-01..08 (§3.3), BR-EST-04..09 (§4), BR-MOB-01..11 (§6) em
  `docs/funcionarios/v5/regras_negocio.html`; `api_guide.md` §5.3–5.6 e §7.
- **Testes:** 709; **4 falhas pré-existentes e conhecidas** (2 de `Option`,
  2 de `EnversAuditSchemaResolutionTest`). Nada mais falha.
- Sem push. Sem produção — não há dados a migrar.

## Decisions made — do not re-litigate

- **Mobilidade transitória não toca na afectação** (Lei 20/X/2023 art. 135.º
  n.º 7): o titular mantém o Lugar; encerrar é só fechar o registo.
- **Mobilidade definitiva = transferência**; interna/externa muda só o campo do
  destino (`destinationUnitId` vs `entidadeDestino`).
- **Promoção**: a modalidade infere-se do pedido (com `positionId` muda de Lugar;
  sem ele o Lugar sobe de categoria) e **não se persiste** — deduz-se do
  histórico.
- **Nada de respostas `Map`** nos endpoints novos: DTOs tipados.
- **Motivos não se validam** (`motivoCkey`, `terminationReason`): são
  parametrizados pelo utilizador no catálogo `Option`.
- **O que é derivável não se guarda** (provido/vago, vínculo laboral, "onde
  exerce funções").
- **Substituto adiado** para depois das ausências (a lei permite, não obriga).
- **Percurso do colaborador (linha temporal única) adiado** para quando o negócio
  estiver definido.
- **O SIGDI fica de fora.** A avaliação e os créditos de desempenho (CDD) vivem no
  SIGDI; é ele que chamará o RH depois da integração. O RH **regista** a progressão
  e a promoção, não verifica elegibilidade. Confirmado pelo DL 4/2024, que remete
  os limiares para o diploma da gestão de desempenho (art. 36.º).
- **Parametrizar valores, não decisões.** Colunas de catálogo para o que a lei fixa
  e pode mudar (dias, limites, situação); nada de interruptores para decisões que a
  instituição ainda não tomou. O que é derivável da lei fica em código derivado
  (enum), não em coluna.
- **Licença parental é ausência, não licença** (art. 171.º n.º 2 vs art. 172.º):
  `MATERNIDADE` 90 dias e `PATERNIDADE` 10 dias úteis; o subtipo `LIC_PARENTAL` sai.
- **`record_type = AMBOS` sai**: licença e mobilidade têm efeitos opostos no vínculo
  e nenhum subtipo do seed o usa.

## Constraints

- **Build exige JDK 26**: `export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-26.0.2.10-hotspot"`.
- **Migrações Flyway sempre defensivas**: `to_regclass` para a tabela +
  `information_schema.columns` para a coluna. Ver V40/V41 como modelo.
- **Confirmar o número da migração livre** antes de criar (já houve colisão na V39).
- Controladores em `interfaces/rest/` são gerados pelo IGRP: lógica nos handlers e
  manifesto `.igrpstudio` actualizado a par do Java.
- JPQL usa o **nome `@Entity`**, não o da classe (ex.: `ColabsLicencaMobilidadeEntity`).
- Documentação em **pt-PT**; commits `feat|fix(<módulo>): …`.

## Blockers & risks

- ~~Texto do DL n.º 4/2024 não acessível~~ — **resolvido (2026-09-18)**. Os PDFs
  oficiais estão no site do Ministério das Finanças (mf.gov.cv/web/dnap): PCFR,
  Lei 20/X/2023 e DL 3/2010. O PCFR **não** fixa o número de créditos (art. 36.º
  remete para o diploma da gestão de desempenho, que é âmbito do SIGDI). Confirmado:
  progressão sem concurso nem tempo de serviço (art. 33.º n.º 3 e 34.º n.º 1);
  promoção com concurso interno (art. 32.º n.º 3, 34.º n.º 2); mudança de carreira
  = evolução vertical, com concurso interno e perfil (art. 35.º). O DL 3/2010
  mantém-se em vigor no que não contraria a lei nova (art. 213.º), com a maternidade
  a passar de 60 para 90 dias.
- **Por confirmar com o jurídico:** se algum diploma posterior substituiu o
  DL 3/2010 (não encontrado) e qual o diploma da mobilidade.
- **Falta decisão de negócio**: a promoção exige lugar vago na prática da
  instituição? Não bloqueia (as duas formas estão suportadas).
- `mvn compile` incremental **não apanha** assinaturas removidas do domínio — usar
  `clean` quando se muda um modelo.

## Relevant files

- `colaboradores/application/services/AssignmentService.java` — afectar,
  progredir, promover, transferir, encerrarAfectacaoCorrente.
- `colaboradores/application/services/MobilidadeService.java` — validações da
  mobilidade e `mobilidadeEmVigor` (onde exerce funções).
- `colaboradores/application/services/CessacaoService.java` — caminho único da
  cessação.
- `colaboradores/domain/models/LicencaMobilidade.java` — estados, prorrogação.
- `parametrizacoes/domain/models/{WorkerState,LeaveMobilitySubtype}.java` —
  `endsEmployment`, `maxDurationDays`, `maxExtensions`, `isMobilidade()`.
- `src/main/resources/db/migration/V40__*.sql`, `V41__*.sql` — modelo das
  migrações defensivas.
- `docs/funcionarios/v5/regras_negocio.html` — catálogo BR-*, fonte de verdade.

## Trabalho pendente (por ordem recomendada)

### 1. Situações funcionais (art. 118.º–122.º) — destrava o resto
A lei tem seis situações: actividade no quadro, actividade **fora** do quadro,
inactividade no quadro, inactividade **fora** do quadro, **disponibilidade**,
aposentação. Hoje só há 4 estados (ACTIVE/SUSPENDED/INACTIVE/RETIRED).
Regras concretas a modelar:
- inactividade **fora** do quadro **abre vaga** (art. 121.º n.º 2);
- actividade no quadro para cônjuge diplomata > 1 ano e formação no exterior
  > 6 meses **abrem vaga** (art. 118.º n.º 2);
- **disponibilidade** = aguarda vaga, com direito a contagem de tempo e abonos
  (art. 122.º); é por aqui que se regressa de licença longa;
- inactividade no quadro **não conta para antiguidade** (art. 120.º n.º 2).
Sugestão: colunas parametrizadas em `t_worker_state` (`opens_vacancy`,
`counts_seniority`, `situacao_funcional`), à imagem de `ends_employment` (V40).

### 2. Licenças que abrem vaga + disponibilidade (DL n.º 3/2010)
Hoje **nenhuma licença toca no Lugar**. A lei distingue:
- sem vencimento até 90 dias (art. 46.º) e até 3 anos (art. 48.º): **mantém** o
  lugar; pode ser preenchido por contrato a prazo que caduca no regresso;
- **longa duração** (art. 50.º–53.º): **abre vaga**, suspende o vínculo; no
  regresso, tem direito a uma vaga existente ou à primeira que ocorra;
- acompanhamento de cônjuge no estrangeiro: abre vaga **> 1 ano** (art. 56.º n.º 2);
- organismo internacional como funcionário do organismo: abre vaga (art. 62.º);
- formação: abre vaga **> 6 meses** (art. 67.º n.º 3).
Sugestão: no subtipo, `position_effect` (MANTEM/ABRE_VAGA) +
`vacancy_after_days`, e `return_effect` (REGRESSA_LUGAR/DISPONIBILIDADE).

### 3. Subtipos de licença/mobilidade — limpeza
- `record_type = AMBOS` é ambíguo; hoje `isMobilidade()` trata-o como mobilidade
  (antes era ignorado em silêncio). Decidir se se remove dos valores aceites.
- `LeaveMobilitySubtypeEntity` **não mapeia a coluna `name`**, que a V6 declara
  `NOT NULL` — qualquer INSERT tem de preencher `name` à mão (ver comentário no
  `seed_parametrizacoes.sql`). Resolver: ou mapear, ou largar o NOT NULL.
- `affects_pay` e `counts_for_seniority` existem no catálogo mas **nenhum código
  os usa**.
- Existe duplicação conceptual: `LIC_PARENTAL` (subtipo) vs `MATERNIDADE`/
  `PATERNIDADE` (tipos de ausência). Decidir onde vive a licença parental.

### 4. Ausências / assiduidade — negócio por rever
Estado actual (`PedidoAusencia`, `SaldoAusencia`, catálogo `LeaveType`), com
problemas identificados e **ainda não corrigidos**:
- **o saldo nunca passa a "gozados"**: aprovar só faz `incrementarPendentes`;
  `diasGozados` fica sempre a 0 (`SaldoAusencia`);
- **só o próprio pode cancelar** um pedido (403 para o RH) —
  `CancelarPedidoAusenciaCommandHandler`;
- aprovar/rejeitar não são `@Transactional`;
- `MATERNIDADE`/`PATERNIDADE` como tipos de ausência, sobrepostos ao subtipo de
  licença (ver ponto 3). A Lei 20/X/2023 art. 172.º fixa 90 dias (mãe) e 10 dias
  úteis (pai);
- férias: DL 3/2010 dá **22 dias úteis** (o seed já tem `max_days_per_year=22`),
  mas não há regras de marcação, acumulação nem gozo proporcional.

### 5. Substituição (depois das ausências)
Lei 20/X/2023 art. 73.º al. a)–c): contrato a termo para substituir funcionário
**temporariamente impedido** (lista aberta: doença prolongada, mobilidade,
comissão de serviço, licença sem vencimento com direito a lugar). Caduca quando
cessa a situação (art. 77.º n.º 2). Há ainda a **nomeação em substituição**
(art. 91.º n.º 1 al. a), para quem já é funcionário.
Implica: afectação `SUBSTITUICAO` ligada à afectação do titular e à ausência que
a justifica, **e tornar parcial o índice único**:
`ux_assignment_position_current` passa a exigir `assignment_type = 'PRINCIPAL'`.
Fora do âmbito: férias e faltas curtas.

### 6. Movimentos menores que faltam
Consolidação da mobilidade (art. 132.º n.º 4) · mobilidade em acumulação
(art. 134.º n.º 2 al. b; o tipo `ACUMULACAO` existe e nunca é usado) · permuta
(troca recíproca e simultânea, tem de ser atómica) · **mudança de carreira /
evolução vertical** (art. 139.º — hoje sem caminho: a promoção exige a mesma
carreira e a transferência a mesma categoria) · regresso de comissão de serviço
(art. 64.º n.º 2) · estágio probatório → nomeação definitiva (art. 57.º, 72.º) ·
reintegração judicial (art. 97.º n.º 10 al. b).

## How to verify / resume

```bash
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-26.0.2.10-hotspot"
cd /c/Users/ivanick.santos/Nick-personal/ta-workspace/projects/Recursos_Humanos
mvn -B test            # 709 testes, 4 falhas conhecidas (Option x2, Envers x2)
git --no-pager log --oneline -8
```
Se aparecerem outras falhas, é regressão. Se o contexto Spring falhar com
"Found more than one migration with version N", é colisão de número de migração.
`mvn -B clean compile` quando se alterar assinaturas do domínio.

Arranque real (nunca foi feito nesta sessão, **nenhum endpoint novo foi chamado
contra a base de dados**): `docker-compose up` e depois
`java -jar target/RH-Service-0.0.1-SNAPSHOT.jar`, Swagger em
`http://localhost:8091/swagger-ui.html`.

## Test / validation plan

Por executar contra a aplicação a correr (perfil `development`, sem auth). Os
ids vêm do seed (`src/main/resources/db/seed/`).

**Já verificado (2026-09-18):**
- **V40/V41 aplicadas** e as quatro colunas existem em `recursoshumanos_db`
  (contentor `postgres-ingt-rh`, porta 5436). O passo 1 abaixo está feito.
- **A aplicação arranca** no perfil `development`, porta **8099** (não 8091),
  e `GET /v3/api-docs` responde 200. Único erro no log: a sincronização de
  permissões do IGRP (`AuthorizationSyncRunner`), por o URL do Access Management
  estar vazio no `.env` — esperado em dev.
- ⚠️ **Armadilha do empacotamento:** com o VS Code aberto, um `mvn clean package`
  pode gerar um jar **sem** os `application*.properties` nem as migrações — a app
  arranca na 8080, sem perfil, e cai no `issuer-uri`. Confirmar com
  `jar tf target/*.jar | grep application.properties`; se faltar, correr
  `mvn package` outra vez **sem** `clean`.

**Por verificar — situações funcionais (V42):**
- S1. Arrancar depois da V42: `\d t_worker_state` tem `situacao_funcional`;
  `ACTIVE`/`SUSPENDED`/`RETIRED` classificados, `INACTIVE` a NULL.
- S2. `POST /worker-states` com `situacaoFuncional` inválido → **422**.
- S3. `endsEmployment=true` com situação que não é `APOSENTACAO` → **422**.
- S4. `PATCH /funcionarios/{id}/worker-state` para estado
  `INACTIVIDADE_FORA_QUADRO` → **200**, contrato `SUSPENSO`, afectação encerrada
  (Lugar vago), colaborador **continua activo**, `afectacaoEncerradaId` preenchido.
- S5. Voltar a `ACTIVIDADE_NO_QUADRO` → contrato reactivado; **não** recupera o
  Lugar sozinho (precisa de nova afectação).
- S6. `ACTIVIDADE_FORA_QUADRO` → contrato e afectação intactos.
- S7. Estado sem situação → só histórico.
- S8. Novos estados do seed (`ACTIVE_OUTSIDE`, `INACTIVE_OUTSIDE`, `AVAILABLE`)
  existem após o seed; em bases já semeadas têm de ser criados pela API.

**Por verificar — o resto (nenhum endpoint novo foi ainda chamado contra a BD):**

1. **V40/V41 aplicam-se** — feito (ver acima).
2. **Progressão** — `POST /api/v1/rh/funcionarios/{id}/progressao`
   `{"dataEfeito":"2026-10-01"}` num colaborador com afectação num Lugar de
   carreira → **201** com `escalaoAnterior`/`escalaoNovo`; confirmar em
   `t_assignment` que há 2 linhas para o mesmo `position_id`, a antiga com
   `data_fim = 2026-09-30` e `is_current=false`.
3. **Promoção nas duas formas** — com `positionId` de Lugar vago da categoria
   seguinte → **201** `lugarReclassificado=false`; sem `positionId` → **201**
   `lugarReclassificado=true` e `t_position.category_id` do Lugar actualizado.
4. **Transferência** — destino da mesma categoria → **201**; destino de outra
   categoria → **422**; sem `functionId` e com função incompatível → **422**.
5. **Cessação pelos dois caminhos** — (a) `PATCH /funcionarios/{id}/worker-state`
   para `RETIRED`; (b) noutro colaborador, `PUT /funcionarios/{id}/contratos/{cid}/close`.
   Em **ambos**: `t_funcionario.is_active=false`, contrato `CESSADO`, afectação
   fechada, **uma linha nova em `t_historico_estado_colaborador`**. Era aqui que
   o segundo caminho falhava antes.
6. **Mobilidade transitória** — criar + `approve` com `destinationUnitId` →
   **200**; confirmar que `t_assignment` **não mudou** (mesma linha corrente);
   `GET /colaboradores/assignments/funcionario/{id}/unidade-atual` mostra
   `emMobilidade=true` e `exerceFuncoesUnidadeNome` = unidade de destino;
   `GET /funcionarios/{id}/details` traz `mobilidadeEmVigor`.
7. **Mobilidade externa** — `approve` só com `entidadeDestino` → **200**.
   Sem destino nenhum → **422**.
8. **Duração** — `dataFim` a mais de 365 dias do início num subtipo de
   mobilidade → **422** no `approve`.
9. **Prorrogação** — `PUT /licencas-mobilidade/{id}/prorrogar` com `novaDataFim`
   → **200** `prorrogacoes=1`; repetir → **422** (limite 1).
10. **Encerrar** → **200**, e o colaborador continua no mesmo Lugar (`unidade-atual`
    volta a `emMobilidade=false`, sem perda de afectação).

## Open questions

- A promoção, na prática da instituição, exige lugar vago da categoria superior?
  (decide: RH/jurídico)
- Licença parental: tipo de ausência ou subtipo de licença? (ponto 3)
- `record_type = AMBOS` mantém-se ou desaparece?
- Confirmar com o jurídico se o DL n.º 3/2010 continua a ser o diploma aplicável
  às licenças e qual o diploma da mobilidade.

## Next step

Modelar as **situações funcionais** (ponto 1): alinhar `t_worker_state` com as
seis situações da lei e acrescentar, parametrizadas, `opens_vacancy` e
`counts_seniority` — migração **V42**, defensiva, no molde da V40.
