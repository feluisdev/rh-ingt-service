> Updated: 2026-09-05 13:15

## Goal

Migrar o RH-Service de identificadores UUID soltos para objectos. A camada de
persistência já está feita (`@ManyToOne`); falta **a mesma migração no domínio**:
23 campos `UUID` cruos nos modelos de domínio passam a identidades tipadas.

## Current state

Branch `dev`, working tree limpo (só `.claude/settings*.json` modificados, são do
utilizador). Cinco commits nesta sessão:

- `ee8bc8dd` — **41 associações `@ManyToOne` em 19 entidades** (colaboradores,
  estrutura, carreiras, parametrizacoes). Domínio intacto. Helper novo:
  `shared/infrastructure/persistence/JpaReferences` (`ref(Class,UUID)` /
  `idOf(assoc, Entity::getId)`). Manifestos `.igrpstudio` sincronizados.
- `69e356fb` — árvore `specs/` **removida** (97 ficheiros); `CLAUDE.md` passa a
  apontar v5 como fonte de verdade.
- `2c2765a3` / `7b121d1d` — correcções à documentação v5.
- `2b944b38` — **4 bugs de regras de negócio** corrigidos (BR-GRD-03, BR-GRD-04,
  BR-FUN-02, BR-PH-01).

Testes: **631, 2 falhas** — ambas do `ccode` do `Option`, pré-existentes em HEAD,
não são regressão. Aplicação arranca e as 41 constraints FK existem na BD de dev.

## Decisions made — do not re-litigate

- **Sem migrations Flyway novas**: o esquema fica ao cargo do `ddl-auto=update`,
  que o utilizador vai activar em todos os ambientes no deploy.
- **`t_document.reference_id` e `t_iam_user_profile.funcionario_id` ficam UUID**:
  o primeiro é polimórfico, o segundo foi excluído pelo utilizador.
- **`specs/` não volta**: as suas listas de prioridade estavam desactualizadas e
  induziram em erro. Fonte de verdade é `docs/funcionarios/v5/`.
- **BR-PH-01 sem índice na BD**: Hibernate não declara índices parciais; a regra
  fica só com validação aplicacional, e o doc já o diz.

## Constraints

- **Build exige JDK 26**: `export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-26.0.2.10-hotspot"`.
- **Correr SEMPRE `mvn clean test`**, nunca `mvn test` sozinho. O build incremental
  deste projecto deu resultados falsos 3 vezes numa sessão — chegou a inventar 26
  falhas em testes de `sigdi` intocados. Depois de `clean`, verde.
- **Portão de módulo**: só `estrutura/infrastructure/lookup/FuncionarioLookupAdapter`
  pode importar `colaboradores` a partir de `estrutura`. Verificar com
  `grep -rln "colaboradores\." src/main/java/cv/igrp/RH_Service/estrutura/`
  — tem de devolver exactamente esse ficheiro.
- **Não editar `interfaces/rest/*` nem `*Entity.java` sem sincronizar `.igrpstudio/`**.
- Mensagens de commit **sem acentos**, conventional commits com âmbito de módulo.
- Documentação em pt-PT.

## Blockers & risks

- Nada bloqueia. A decisão do ponto 1 das *Open questions* determina o desenho.
- **`getReference` agora falha alto**: gravar uma FK inexistente lança
  `EntityNotFoundException` no flush, onde antes passava em silêncio. Seeders e
  importações têm de inserir os pais primeiro.
- `GlobalExceptionHandler` mapeia `DataIntegrityViolationException` para **400**.
  Regras protegidas por índice devolvem 409 só quando a validação aplicacional
  dispara primeiro; numa corrida até à BD o cliente recebe 400.
- Das 95 regras, **3 foram verificadas linha a linha**; as outras 92 só ao nível
  de "a classe que citam existe". Uma regra pode citar a classe certa e descrever
  mal o comportamento.

## Relevant files

- `src/main/java/cv/igrp/RH_Service/shared/infrastructure/persistence/JpaReferences.java` — o padrão a seguir para traduzir identidade↔associação.
- `colaboradores/domain/models/Documento.java:4` — precedente A: importa `DocumentTypeId` de `parametrizacoes`.
- `colaboradores/domain/models/PedidoAusencia.java:5` — precedente B: `TipoAusenciaId` local para tabela de `parametrizacoes`. **Os dois precedentes contradizem-se** — ver Open questions.
- `estrutura/domain/models/Position.java:23-28` — 6 dos 23 UUIDs a migrar.
- `colaboradores/infrastructure/mappers/AssignmentMapper.java` — mapper exemplar do padrão novo.
- `docs/funcionarios/v5/regras_negocio.html` — catálogo das 95 regras.

## How to verify / resume

```bash
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-26.0.2.10-hotspot"
cd c:/Users/ivanick.santos/Nick-personal/ta-workspace/projects/Recursos_Humanos

mvn clean test          # esperado: 631 testes, 2 falhas (OptionTest,
                        # CreateOptionCommandHandlerTest) — pre-existentes

# os 23 UUIDs por migrar:
grep -rn "private UUID " src/main/java/cv/igrp/RH_Service/{colaboradores,estrutura,carreiras,parametrizacoes}/domain/models/*.java
```

Arrancar (Postgres `postgres-ingt-rh` já de pé no 5436; MinIO **não** é preciso):

```bash
mvn -DskipTests spring-boot:run     # porto 8099, perfil development
curl "http://localhost:8099/api/v1/rh/careers?page=0&size=2"   # espera 200
```

Erros normais no arranque, **ignorar**: Keycloak `qa-ingt.gov.cv` inacessível e
`[Permission Sync] URI with undefined scheme`. Ambos ambientais.

Confirmar que as 41 FKs existem na BD:

```bash
docker exec postgres-ingt-rh psql -U postgres -d recursoshumanos_db -t -c \
 "SELECT count(*) FROM information_schema.table_constraints
  WHERE constraint_type='FOREIGN KEY' AND table_schema='public';"   # 51 (41 nossas + sigdi)
```

## Test / validation plan

Para cada correcção de `2b944b38`, o que confirmar contra a app a correr (nenhuma
tem teste automatizado — **escrever um seria bom primeiro passo**):

1. **BR-GRD-03** — criar carreira→categoria→escalão, afectar um funcionário a um
   Lugar com esse escalão, e tentar `DELETE`/desactivar o escalão.
   Esperado: **409** com "está referenciado em enquadramentos profissionais activos".
   Antes devolvia 200 e desactivava. Evidência: corpo da resposta + `is_active` na BD.
2. **BR-GRD-04** — desactivar duas vezes a mesma carreira.
   Esperado: **409** na segunda (antes era 500). Repetir para categoria e escalão.
3. **BR-FUN-02** — afectar com um `functionId` cujo `job_id` difere do `job_id` do
   Lugar. Esperado: **422** "A função ... não pertence ao cargo indicado".
   Com função genérica (`job_id` nulo) tem de continuar a passar.
4. **BR-PH-01** — criar feriado nacional numa data, desactivá-lo, criar outro na
   mesma data. Esperado: **201** (antes 409 indevido). Com o primeiro activo,
   esperado 409.

## Open questions

1. **Referências entre contextos (13 dos 23 campos)** — que tipo usa o domínio?
   O código tem dois precedentes opostos (ver *Relevant files*). Saídas:
   **(a)** importar o tipo do contexto dono — simples, mas acopla domínios e
   obriga a reescrever o portão `estrutura→colaboradores`, porque
   `OrganizationalUnit.responsibleEmployeeId` teria de importar `FuncionarioId`;
   **(b)** tipo local em cada contexto — respeita fronteiras, duplica tipos;
   **(c)** tipar só dentro do contexto e deixar UUID a atravessar — ~10 campos.
   **Decide o utilizador.**
2. **Execução da migração** — caso exemplar primeiro para aprovar o padrão, um
   commit por contexto, ou tudo de uma vez. **Decide o utilizador.**
3. **BR-CON-05** — o código aceita `percentagemTempo = 0` fora do `TEMPO_PARCIAL`
   e persiste-o; o documento diz que é proibido. O `!= 0` em
   `colaboradores/application/services/ContratoService.java:52-56` parece
   deliberado. Apertar rejeita payloads que hoje funcionam — alteração visível
   para o frontend. **Parqueada pelo utilizador.**
4. **BR-OPT-01** — documento diz unicidade `(ccode, ckey)`; código usa
   `(ccode, ckey, locale)`, sem constraint de BD a arbitrar. Se `locale` faz parte
   da chave, corrige-se o documento; se não, corrige-se o código.
   **Parqueada pelo utilizador.**
5. **Dívida anterior, não tocada** — validação de `ccode` do `Option` comentada
   (mantém os 2 testes vermelhos); `V24__update_kpi_criteria_fix.sql` altera
   `t_key_results`, tabela que nenhuma migration cria.
6. **Integração SAD** — único bloco funcional por implementar. A spec foi removida
   com as `specs/`; parte-se do capítulo 6 da spec técnica e do v5.

## Next step

Perguntar ao utilizador a decisão 1 (tipo nas referências entre contextos) e a 2
(forma de execução). Só depois começar a migração dos 23 campos.
