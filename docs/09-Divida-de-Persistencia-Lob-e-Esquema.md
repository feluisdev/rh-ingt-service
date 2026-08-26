# Dívida de Persistência — `@Lob` sobre `String` e Esquema fora do Controlo de Versões

**Versão:** 1.0 | **Status:** Inventário fechado, correção por fazer | **Data:** 24/08/2026
**Âmbito:** `src/main/java/**/entity/`, `src/main/resources/db/migration/`, `.igrpstudio/sigdi/models/`
**Base medida:** `recursoshumanos_db` em `localhost:5432`, PostgreSQL 18.4, perfil de desenvolvimento

---

## Para que serve este documento

Dois defeitos de persistência foram medidos e não corrigidos. **A não correção é decisão registada, não omissão** — cada um deles é trabalho de uma fase inteira, e a razão está escrita em números mais abaixo.

Este documento existe para que quem retomar não tenha de refazer a medição.

| | Medida | Número |
|---|---|---|
| **A** | Ocorrências de `@Lob` sobre `String` | **19**, em **11** entidades |
| **A** | Tabelas a migrar | **11** |
| **A** | Valores de negócio guardados como OID em vez de texto | **73** |
| **A** | Objetos grandes órfãos já acumulados | **93** de 166 (56 %) |
| **B** | Tabelas do esquema fora do controlo de versões | **80** de **120** (66 %) |
| **B** | Tabelas de negócio que impedem o arranque em produção | **39** |

**Nenhuma escrita foi feita na base para produzir este documento.** Só `SELECT`. A aplicação não foi arrancada — arrancá-la corre o Flyway e o `ddl-auto`, que é precisamente o objeto do inventário B.

---

## Nota de leitura: estas colunas não se leem como texto

Antes de qualquer verificação, saber isto poupa um diagnóstico errado:

```sql
select field_name from t_change_requests;
--  49721     <- é o OID de um large object, não o valor
```

```sql
select convert_from(lo_get(field_name::oid), 'UTF8') from t_change_requests;
--  budget    <- o valor
```

Quem contar linhas «vazias» sem `lo_get` conta mal.

---

# Parte A — `@Lob` sobre `String`

## O defeito

`@Lob` num campo `String` faz o Hibernate tratar o valor como CLOB. Contra PostgreSQL isso significa *large object*: o driver cria uma entrada em `pg_largeobject_metadata`, escreve o conteúdo em `pg_largeobject`, e grava o **OID** na coluna.

O `columnDefinition="TEXT"` **só afeta o DDL**. Verificado nas 19 colunas: todas com `data_type = text` em `information_schema.columns`, e todas a conter números.

Consequências, todas confirmadas nesta base:

1. **Nenhuma consulta alcança o valor.** `WHERE description LIKE '%orçamento%'` compara contra a representação decimal de um OID.
2. **Nenhum índice serve.** O `pg_trgm` ativado pela `V15` não tem texto sobre o que trabalhar nestas colunas.
3. **Nenhum relatório funciona** sem passar por `lo_get`, que nenhuma camada do projeto conhece.
4. **`DELETE` deixa o objeto órfão.** A linha desaparece, o *large object* fica. **93 dos 166 objetos desta base já são órfãos.** Cada `UPDATE` acrescenta mais um, porque escreve objeto novo e abandona o anterior. Não há `lo_unlink` em lado nenhum do projeto.

## A causa é o gerador, não descuido de quem escreveu as entidades

`.claude/skills/igrp-spring-generator/references/addModel.md`, linhas 167-170:

```
**C) Text column (`type="text"`)**
- Optionally `@ColumnDefault("'...'" )` when `defaultValue` exists
- `@Lob`
- `@Column(..., columnDefinition="TEXT")`
- Field is `String`
```

E os manifestos em `.igrpstudio/sigdi/models/` declaram `"type": "text"` nos campos correspondentes.

**As 19 ocorrências estão todas em `sigdi/infrastructure/persistence/entity/`. Zero fora de `sigdi`** — a divisão é limpa porque só esse módulo foi gerado.

A forma correta já existe no projeto, em 11 sítios de ficheiros não gerados — `SiadapInterimFeedbackEntity.java:25`, `SiadapInterimCompetencyObservationEntity.java:31`, `LicencaMobilidadeEntity.java:44`, `:56`, `:62`, `ProcessoDisciplinarEntity.java:50`, `HistoricoEstadoColaboradorEntity.java:41`, `DocumentoEntity.java:46`, `ContratoEntity.java:65`:

```java
    @Column(name = "objectives_synthesis", columnDefinition = "TEXT")
    private String objectivesSynthesis;
```

## Inventário

`@Audited` verificado ficheiro a ficheiro, não presumido.

| # | Entidade | Linhas `@Lob` | Tabela | Colunas | `@Audited` | Cabeçalho de gerador | Manifesto declara `text` |
|---|---|---|---|---|---|---|---|
| 1 | `ChangeRequestEntity` | 38, 43, 48, 61, 65 | `t_change_requests` | `field_name`, `current_value`, `justification`, `proposed_value`, `reviewer_comment` | sim (`:18`) | sim, padrão | **sim, 5 campos** |
| 2 | `InstitutionalIdentityEntity` | 43, 49, 54 | `t_institutional_identity` | `mission`, `vision`, `values_json` | sim (`:18`) | sim, padrão | **sim, 3 campos** |
| 3 | `TacticalActivitiesEntity` | 56, 61, 74 | `t_tactical_activities` | `description_what`, `justification_why`, `methodology_how` | sim (`:24`) | removido na Fase 93 | **sim, 3 campos** |
| 4 | `KeyResultsCheckinEntity` | 43 | `t_key_result_checkins` | `comment` | sim (`:19`) | sim, padrão | **sim, 1 campo** |
| 5 | `SiadapEvaluationEntity` | 68 | `t_siadap_evaluations` | `last_negotiation_comment` | sim (`:21`) | «EXTENDED WITH CUSTOM FIELDS» | **NÃO — ausente do manifesto** |
| 6 | `SigofSyncLogEntity` | 52 | `t_sigof_sync_log` | `error_message` | sim (`:16`) | sim, padrão | **sim, 1 campo** |
| 7 | `SimulationResultsEntity` | 46 | `t_simulation_results` | `details` | sim (`:18`) | sim, padrão | **sim, 1 campo** |
| 8 | `SimulationScenariosEntity` | 37 | `t_simulation_scenarios` | `parameters` | sim (`:17`) | sim, padrão | **sim, 1 campo** |
| 9 | `StrategicGoalEntity` | 54 | `t_strategic_goals` | `description` | sim (`:18`) | sim, padrão | **sim, 1 campo** |
| 10 | `StrategicIndicatorEntity` | 37 | `t_strategic_indicators` | `evaluation_criteria` | sim (`:13`) | **nenhum cabeçalho** | **sim, 1 campo** |
| 11 | `TaticalActivityHistoryEntity` | 49 | `t_activity_approval_history` | `comment` | sim (`:17`) | sim, padrão | **sim, 1 campo** |

**19 ocorrências, 11 entidades, 11 tabelas, 19 colunas. Os 11 são `@Audited`.**

### Uma das 19 não volta pelo gerador; as outras 18 voltam

`SiadapEvaluationEntity.last_negotiation_comment` é a única coluna **ausente do manifesto**. O `.igrpstudio/sigdi/models/SiadapEvaluationEntity.json` não tem um único `"type": "text"` e não menciona o campo — foi acrescentado à mão, e a coluna nasceu da `V21__siadap_evaluation_last_negotiation_comment.sql`.

Só essa se corrige apenas no Java. **As outras 18 reaparecem na geração seguinte se o manifesto ou o template não mudarem.**

## Medição ao vivo — colunas de negócio

| Tabela | Coluna | Linhas | Não nulos | São OID vivo |
|---|---|---|---|---|
| `t_change_requests` | `field_name` | 3 | 3 | 3 |
| `t_change_requests` | `current_value` | 3 | 3 | 3 |
| `t_change_requests` | `justification` | 3 | 3 | 3 |
| `t_change_requests` | `proposed_value` | 3 | 3 | 3 |
| `t_change_requests` | `reviewer_comment` | 3 | 3 | 3 |
| `t_institutional_identity` | `mission` | 4 | 4 | 4 |
| `t_institutional_identity` | `vision` | 4 | 4 | 4 |
| `t_institutional_identity` | `values_json` | 4 | 4 | 4 |
| `t_key_result_checkins` | `comment` | 11 | 11 | 11 |
| `t_siadap_evaluations` | `last_negotiation_comment` | 11 | **0** | 0 |
| `t_sigof_sync_log` | `error_message` | **0** | 0 | 0 |
| `t_simulation_results` | `details` | 8 | 8 | 8 |
| `t_simulation_scenarios` | `parameters` | 12 | 12 | 12 |
| `t_strategic_goals` | `description` | 3 | 3 | 3 |
| `t_strategic_indicators` | `evaluation_criteria` | 1 | 1 | 1 |
| `t_tactical_activities` | `description_what` | 5 | 3 | 3 |
| `t_tactical_activities` | `justification_why` | 5 | 5 | 5 |
| `t_tactical_activities` | `methodology_how` | 5 | 3 | 3 |
| `t_activity_approval_history` | `comment` | 4 | **0** | 0 |
| | **Total** | | **73** | **73** |

**Cem por cento dos valores não nulos são OIDs.** Nenhuma coluna escapou. Duas estão por povoar e uma tabela está vazia — nessas o defeito existe no código mas ainda não tem dados a resgatar.

```
pg_largeobject_metadata          166 objetos
  referenciados                   73
  órfãos                          93   (56 %)
conteúdo                        8877 bytes
```

## As sombras Envers **já guardam texto** — são 11 tabelas a migrar, não 22

Assumia-se que cada tabela `@Audited` duplicaria o trabalho. **A medição desmente-o.**

```
audit_schema.t_change_requests_aud
 rev  | field_name | current_value | reviewer_comment
 1202 | budget     | 40000.00      |
 1203 | budget     | 40000.00      | aprovado no codigo antigo
 1253 | budget     | 40000.00      | aprovado no codigo desta fase
```

Verificado nas 16 colunas `_aud` com dados: **zero valores apontam para um objeto grande vivo**. Apenas 4 valores em todo o conjunto são sequer numéricos, e são montantes (`45000`), não OIDs.

| Sombra | Não nulos | São OID vivo |
|---|---|---|
| `t_change_requests_aud` (5 colunas) | 27 | 0 |
| `t_institutional_identity_aud` (3 colunas) | 30 | 0 |
| `t_key_result_checkins_aud.comment` | 56 | 0 |
| `t_simulation_results_aud.details` | 8 | 0 |
| `t_simulation_scenarios_aud.parameters` | 12 | 0 |
| `t_strategic_goals_aud.description` | 6 | 0 |
| `t_strategic_indicators_aud.evaluation_criteria` | 3 | 0 |
| `t_tactical_activities_aud` (3 colunas) | 38 | 0 |
| `t_siadap_evaluations_aud`, `t_sigof_sync_log_aud`, `t_activity_approval_history_aud` | 0 | 0 |

**Duas consequências:**

1. **A migração cobre 11 tabelas, não 22.**
2. **A sombra é fonte de recuperação.** Se um `lo_get` falhar por objeto perdido, o texto da última revisão está em `audit_schema.<tabela>_aud`.

**Aviso:** mediu-se o facto, não o mecanismo. Não se provou *porquê* o Envers escapa ao tratamento CLOB. Quem migrar deve reconfirmar contra a sua própria base antes de contar com esta propriedade.

---

# Parte B — Esquema fora do controlo de versões

## Método, para ser refeito

1. Nomes de tabela declarados pelas entidades:
   `grep -rhno '@Table(name *= *"[^"]*"' src/main --include=*.java` → **58 ficheiros, 58 nomes distintos**.
   Confirmou-se a ausência de `@JoinTable`, `@CollectionTable`, `@SecondaryTable` e `@ElementCollection` — **zero ocorrências**, logo não há tabelas implícitas.
2. Alvos de `CREATE TABLE` nas migrações **rastreadas pelo git** (`git ls-files`, não a listagem do diretório), **removendo comentários primeiro** com `sed 's/--.*//'`.
3. Diferença de conjuntos.
4. **Inspecionar cada candidato individualmente.** Contagem crua de `grep` é ponto de partida, não resultado.
5. Sombras Envers registadas **à parte**, nunca somadas.

Cruzamento com o esquema real:

```
tabelas de negócio na base (public, sem flyway_schema_history nem _aud) ... 58
tabelas declaradas por @Table ......................................... 58
na base sem entidade / entidade sem tabela ............................ 0 / 0
```

**Correspondência exata. As entidades descrevem o esquema; as migrações não.**

## Falsos positivos, filtrados e nomeados

Dos 39 candidatos, **apenas 4 são sequer mencionados** nas migrações. Os quatro foram lidos:

| Candidato | Onde | O que a menção é | Veredicto |
|---|---|---|---|
| `t_evaluation_objectives` | `V22:2` | comentário | **verdadeiro positivo** |
| `t_institutional_identity` | `V25:20` | comentário: «to avoid table-creation-ordering dependencies» | **verdadeiro positivo** |
| `t_tactical_activities` | `V30:4`, `:24` | comentário + `UPDATE`; o cabeçalho admite que a coluna «já existe (é mapeada por `TacticalActivitiesEntity`)» | **verdadeiro positivo** |
| `t_unidade_organica` | `V7:10-11`, `V13:12-20` | dois `ALTER TABLE ... DROP COLUMN` | **verdadeiro positivo** |

**Zero falsos positivos.** Nenhuma migração cria nenhuma delas. O caso de `t_unidade_organica` é o mais claro: há uma migração *com o nome da tabela no ficheiro* que só lhe apaga uma coluna, presumindo que já existe.

## Categoria A — tabelas de negócio sem migração: **39 de 58**

**Têm** `CREATE TABLE` (19): `t_bsc_perspective_config`, `t_contract_type`, `t_contrato`, `t_dependente`, `t_employee_professional_assignments`, `t_funcionario`, `t_goal_relationships`, `t_leave_mobility_subtype`, `t_leave_type`, `t_paa_submission_period`, `t_qualificacao`, `t_siadap_evaluations`, `t_siadap_interim_actions`, `t_siadap_interim_competency_observations`, `t_siadap_interim_feedback`, `t_siadap_interim_objective_revisions`, `t_strategic_goals`, `t_tipo_documento`, `t_worker_state`.

**Não têm** (39) — existem só porque o Hibernate as criou:

| | | | |
|---|---|---|---|
| `t_activity_approval_history` | `t_budget_drivers` | `t_career` | `t_category` |
| **`t_change_requests`** | `t_dados_bancarios` | `t_disciplinary_process` | `t_document` |
| `t_employee_unit_assignments` | `t_evaluation_competencies` | `t_evaluation_objectives` | `t_financial_execution_mirror` |
| `t_funcao` | `t_grade` | `t_historico_estado_colaborador` | `t_iam_user_profile` |
| `t_institutional_identity` | `t_institutions` | `t_job` | `t_key_result_checkins` |
| `t_key_results` | `t_leave_balance` | `t_leave_mobility` | `t_leave_request` |
| `t_notification_logs` | `t_okrs` | `t_option_entity` | `t_payroll_slip` |
| `t_public_holiday` | `t_siadap_config` | `t_sigof_sync_log` | `t_simulation_results` |
| `t_simulation_scenarios` | `t_strategic_indicators` | `t_tactical_activities` | `t_training` |
| `t_unidade_organica` | `t_user_delegations` | `t_vinculo_laboral` | |

Evidência para `t_change_requests`, o caso que abriu o inventário:

```bash
$ grep -rl "t_change_requests" src/main/resources/db/migration/ | wc -l
0
```

```properties
# application-development.properties:38
spring.jpa.hibernate.ddl-auto=update
```

**Nota que liga as duas partes deste documento: das 11 tabelas da Parte A, apenas 2 têm `CREATE TABLE` no repositório** (`t_strategic_goals` e `t_siadap_evaluations`). Uma migração de dados para o `@Lob` assentaria em nove tabelas que o repositório não descreve.

## Categoria B — auditoria: **41 tabelas**, natureza diferente

Separadas de propósito. Somá-las às de negócio daria um número maior e menos verdadeiro: uma `_aud` em falta significa que o Envers a criará, não que falta desenho.

```
tabelas em audit_schema ................................. 57
  revinfo ............................................... 1    sem migração (159 revisões)
  sombras _aud .......................................... 56
    criadas por migração com prefixo audit_schema. ...... 16
    criadas pelo Envers, sem migração ................... 40
```

A `V1__audit_schema.sql` cria o esquema e nada mais:

```sql
CREATE SCHEMA IF NOT EXISTS audit_schema;
```

### Defeito colateral: quatro tabelas `_aud` mortas em `public`

`V18` e `V22` criam quatro tabelas de auditoria **sem o prefixo `audit_schema.`**, ao contrário das outras dezasseis:

```sql
-- V18__siadap_interim_feedback.sql:18-19
-- Audit table (Envers)
CREATE TABLE IF NOT EXISTS t_siadap_interim_feedback_aud (
```

O comentário diz Envers; a instrução escreve em `public`. Resultado: existem nos dois esquemas, e o Envers usa a de `audit_schema`.

| Tabela | Linhas em `public` | Linhas em `audit_schema` |
|---|---|---|
| `t_siadap_interim_feedback_aud` | **0** | 14 |
| `t_siadap_interim_competency_observations_aud` | **0** | 131 |
| `t_siadap_interim_objective_revisions_aud` | **0** | 26 |
| `t_siadap_interim_actions_aud` | **0** | 0 |

As quatro cópias em `public` estão vazias e nada as lê.

## Total

```
tabelas no esquema ............................ 120
  public ......................................  63   (58 negócio + flyway_schema_history + 4 _aud mortas)
  audit_schema ................................  57   (56 sombras + revinfo)

sob controlo de versões .......................  40
FORA do controlo de versões ...................  80   (66 %)
  negócio ..................................... 39
  sombras _aud ................................ 40
  revinfo .....................................  1
```

## A consequência: produção não arranca

```properties
# application-development.properties:38   →  spring.jpa.hibernate.ddl-auto=update
# application-production.properties:21    →  spring.jpa.hibernate.ddl-auto=validate
```

Em desenvolvimento o Hibernate corre antes do Flyway e cria o que falta — é por isso que o defeito é invisível a quem só trabalha localmente. Em produção `validate` **não cria nada**: compara e recusa arrancar se divergir.

Numa base fresca com o perfil `production`, o Flyway aplicaria as 28 migrações do repositório, criaria 19 tabelas de negócio e 16 sombras, e o Hibernate falharia a validação **nas 39 tabelas de negócio listadas na Categoria A**. As 40 sombras não contam para esta falha, porque o Envers as cria.

O mesmo vale para `staging`, que também usa `validate` e onde, além disso, `spring.flyway.enabled=false` (`application-staging.properties:21`).

O mecanismo já estava descrito em [`06-Flyway-Analise-e-Guia-Operacional.md`](06-Flyway-Analise-e-Guia-Operacional.md), de 2026-05-04, com a tabela «BD fresca suportada?», que responde sim em `development` e não em `staging` e `production`. **O que faltava era o número.** Atenção: esse documento cobre «V1.0 a V34», uma série apagada três dias depois de ele ser escrito — está desatualizado no âmbito, embora certo no diagnóstico.

## Origem: 36 migrações foram apagadas

```
03867b3  2026-05-07  feluisdev
chore(migrations): remove deprecated Flyway migrations V0.9 to V20
```

Esse commit apagou **36 ficheiros** de `src/main/resources/db/migration/`, entre eles `V0.9__Create_Sigdi_Base_Tables.sql` (que criava `t_institutions`, `t_institutional_identity`, `t_strategic_goals`, `t_tactical_activities`, `t_key_results`), `V20__create_estrutura_tables.sql`, `V23__create_carreiras_tables.sql`, `V24__create_colaboradores_tables.sql`, `V26__create_ausencias_tables.sql`, `V27__create_document_table.sql`, `V28__create_employee_unit_assignments.sql`, `V29__create_iam_user_profile.sql`, `V30__create_training.sql`, `V31__create_disciplinary_process.sql`, `V32__create_payroll_slip.sql`, `V2__create_option_entity.sql` e `V17__create_public_holidays.sql`.

**Praticamente todas as 39 tabelas da Categoria A tiveram, em tempos, um `CREATE TABLE` no repositório.** A numeração foi reiniciada — a série atual `V1..V31` reutiliza números da série apagada — e a nova série nunca reescreveu as criações.

O `flyway_schema_history` desta base confirma-o: 29 entradas, todas da série **nova**, a começar em `2026-07-14 21:00:50`. A série antiga nunca foi aplicada aqui. Esta base é o cenário limpo — o Flyway criou 19 tabelas, o Hibernate criou 39.

## A `V24`: aplicada na base, ausente do repositório

O mesmo defeito visto do outro lado.

```
installed_rank | version | description             | checksum  | installed_on        | success
      22       |   24    | update kpi criteria fix | 611372856 | 2026-07-14 21:04:40 | t
```

`V24__update_kpi_criteria_fix.sql` está **aplicada com sucesso** e **nunca esteve no histórico do git** nesta série — o repositório salta de `V23` para `V25`. O ficheiro existe na árvore de trabalho **por rastrear**, e a decisão sobre ele é do operador.

O que faz: apaga oito colunas `criteria_*` de `t_key_results` e recria seis como `NUMERIC`, mais os `ALTER` equivalentes em `audit_schema.t_key_results_aud`. **Não cria tabela nenhuma**, pelo que não altera as contagens acima — mas altera as **colunas**. Qualquer ambiente que corra o Flyway a partir do repositório fica com `t_key_results` na forma anterior, e a `V25` em diante aplica-se sobre um esquema diferente daquele contra o qual foi testada.

**Lacunas relacionadas:** a série atual não tem `V16` nem `V17` — esses números pertenceram à série apagada. Com `spring.flyway.out-of-order=true` em desenvolvimento (`:31`), os saltos passam despercebidos.

---

# O que uma correção exigiria

## Parte A — `@Lob`

**O `@Lob` tem de sair *e* o manifesto tem de mudar.** Corrigir só o Java deixa a porta encostada: `.igrpstudio/sigdi/models/*.json` declara `"type": "text"`, o gerador aplica `addModel.md:167-170`, e a geração seguinte repõe `@Lob` em silêncio. Os dados convertidos voltariam a ser escritos como OID no primeiro `UPDATE`, sem erro e sem aviso.

Trocar `"type": "text"` por `"type": "string"` **não serve em geral** — o gerador emite `VARCHAR` com `length` para `string`, e há campos aqui (`values_json`, `parameters`, `details`) que excedem 255 caracteres. **A correção certa é no template:** fazer `type: "text"` emitir apenas `@Column(columnDefinition="TEXT")` sem `@Lob`, que é a forma já usada, e certa, nos 11 sítios não gerados. Resolve os 18 campos de uma vez e impede a reincidência.

Por cada uma das 11 tabelas e 19 colunas, uma migração teria de:

1. **Ler o texto** por `lo_get`, e **não** por leitura byte-a-byte de `pg_largeobject` — é tabela interna e a paginação em `pageno` é fácil de errar:

   ```sql
   -- forma da leitura; NÃO é a migração
   select id, convert_from(lo_get(field_name::oid), 'UTF8')
   from t_change_requests where field_name ~ '^[0-9]+$';
   ```

2. **Guardar os OIDs antes de sobrescrever.** Depois de a coluna passar a texto, o OID perde-se e o objeto fica inalcançável para `lo_unlink`.

3. **Escrever o texto de volta** na mesma coluna `text` — já tem o tipo certo, não é preciso `ALTER TABLE`.

4. **Não repetir sobre as `_aud`.** Já têm texto. **Verificar antes** com o mesmo `SELECT` de contagem usado aqui, e só agir se a verificação contrariar esta medição.

5. **`lo_unlink`** dos 73 convertidos e dos 93 órfãos, **depois** de a conversão estar confirmada, nunca antes.

6. **Guardas de idempotência.** O filtro `~ '^[0-9]+$'` é a guarda natural: uma segunda passagem não encontra OIDs e não faz nada. Segue o padrão de `V19`/`V20`, que verificam o estado antes de agir em vez de presumir base fresca.

7. **Ordem entre código e dados.** Remover o `@Lob` antes de converter faz o Hibernate devolver `"49721"` à aplicação, e o número aparece no ecrã. Converter antes de remover faz o Hibernate reescrever o texto como OID novo no `UPDATE` seguinte. **As duas metades têm de entrar juntas**, o que exige janela de paragem.

## Parte B — esquema

Reescrever `CREATE TABLE` para as 39 tabelas da Categoria A, alinhado com as entidades atuais e não com as migrações apagadas — o esquema evoluiu por `ddl-auto` desde 2026-05-07 e as versões antigas já não descrevem a forma corrente. Cada uma com `CREATE TABLE IF NOT EXISTS` para não partir bases existentes.

**Só depois disso faz sentido migrar o `@Lob`**, porque nove das onze tabelas dessa migração estão nesta lista.

---

## Limites deste inventário

- Mede **uma** base: `recursoshumanos_db` em `localhost`, perfil de desenvolvimento. **Staging e produção não foram tocados**, e o volume lá determina se a conversão do `@Lob` é uma transação ou uma operação por lotes.
- Não prova o mecanismo pelo qual as sombras `_aud` escapam ao tratamento CLOB — mede o facto.
- Não avalia se as 39 tabelas da Categoria A têm hoje o mesmo esquema que teriam se as migrações apagadas ainda existissem. Comparou-se a **existência** da tabela, não a sua forma.
- Não determina se algum dos 93 órfãos contém dados sem cópia noutro sítio.

---

**Nada foi corrigido por este documento.** Nenhuma migração foi escrita, nenhuma entidade alterada, nenhuma escrita feita na base. A decisão de adiar é do operador, tomada a 2026-08-24, e os números acima são a razão dela.
