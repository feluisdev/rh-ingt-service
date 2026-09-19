# Bateria de testes funcionais — `testes_funcionais.ps1`

Exercita a API como um cliente faria: primeiro **navega** (lista colaboradores,
catálogos, detalhe, unidade actual), e só depois age. Cada passo tem um código
HTTP esperado, e há caminhos **positivos e negativos**.

## Como correr

```powershell
# 1. Base de dados de pé (contentor postgres-ingt-rh, porta 5436) e seed carregado:
docker cp src\main\resources\db\seed postgres-ingt-rh:/tmp/seed
docker exec -w /tmp/seed postgres-ingt-rh psql -U postgres -d recursoshumanos_db -f master_seed.sql

# 2. Aplicação a correr (perfil development, porta 8099):
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-26.0.2.10-hotspot'
mvn -B -DskipTests package
java -jar target\RH-Service-0.0.1-SNAPSHOT.jar --spring.profiles.active=development

# 3. Correr a bateria:
.\scripts\testes_funcionais.ps1
```

> O script **altera dados**: só serve para ambiente local. Para repetir do zero,
> repor o estado inicial com o SQL do fim deste ficheiro.

## Repor antes de correr

A bateria muda dados de proposito. Ha um ficheiro pronto:

```bash
docker cp scripts/repor_estado.sql postgres-ingt-rh:/tmp/repor.sql
docker exec postgres-ingt-rh sh -c "psql -U postgres -d recursoshumanos_db -q -f /tmp/repor.sql"
```

## O que cobre

| Grupo | O que prova |
|---|---|
| **F0** navegação | listas e catálogos respondem e trazem o que o front-end precisa |
| **F1** situações funcionais | os quatro estados de origem vêm classificados; situação fora da lei, cessação incoerente e `APOSENTACAO` sem cessação são recusadas |
| **F1b** movimentos | progressão sobe um escalão; promoção e transferência recusam destino inexistente; data anterior à afectação é recusada |
| **F2** estado que abre vaga | inactividade fora do quadro encerra a afectação, suspende o contrato e **não** cessa o vínculo; o regresso reactiva o contrato e não devolve o Lugar |
| **F3** licenças | o prazo do subtipo decide: 180 dias mantém o Lugar, 200 abre vaga; o regresso põe em disponibilidade; mobilidade não pode abrir vaga; `AMBOS` recusado; criar subtipo pela API funciona |
| **F4** ausências | reserva na submissão, gozo na aprovação, devolução no cancelamento, libertação na rejeição; sobreposição, falta de saldo, dupla decisão e URL de outro colaborador são recusados |
| **F5** efeitos cruzados | quem perdeu o Lugar (por estado ou por licença) não pode progredir |

## Repor o estado inicial

Corre isto **antes de cada execucao**. A bateria muda dados de proposito (abre
vagas, suspende contratos, gasta saldos), e uma segunda execucao sobre o estado
deixado pela primeira falha por motivos que nada tem a ver com o codigo.

```sql
-- 1. Afectacoes: fica so a do seed, com o escalao de partida.
--    A clausula 'not in' apanha tambem as substituicoes e o que os movimentos criaram.
delete from t_assignment where id not in (
  'e6e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ee01'::uuid,
  'e6e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ee02'::uuid,
  'e6e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ee03'::uuid);
update t_assignment set data_fim=null, is_current=true, assignment_type='PRINCIPAL', titular_assignment_id=null,
       position_id='d5e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ed01'::uuid, grade_id='81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e801'::uuid
 where funcionario_id='91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901'::uuid;
update t_assignment set data_fim=null, is_current=true, assignment_type='PRINCIPAL', titular_assignment_id=null,
       position_id='d5e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ed02'::uuid, grade_id='81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e803'::uuid
 where funcionario_id='91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e902'::uuid;
update t_assignment set data_fim=null, is_current=true, assignment_type='PRINCIPAL', titular_assignment_id=null,
       position_id='d5e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ed03'::uuid, grade_id='81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e803'::uuid
 where funcionario_id='91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e903'::uuid;

-- 2. Lugares: o LUG-0006 e congelado de proposito; os outros voltam a ATIVO.
update t_position set estado='ATIVO', is_active=true where numero_lugar <> 'LUG-0006';
update t_position set estado='CONGELADO', is_active=true where numero_lugar = 'LUG-0006';
--    A promocao sem positionId reclassifica o Lugar: repor a categoria de origem.
update t_position set category_id='71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e701'::uuid where numero_lugar in ('LUG-0001','LUG-0004');
update t_position set category_id='71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e702'::uuid where numero_lugar in ('LUG-0002','LUG-0003','LUG-0005','LUG-0006');

-- 3. Estado e contratos.
update t_funcionario set worker_state_id=(select id from t_worker_state where code='ACTIVE'), is_active=true;
update t_contrato set status='ATIVO', is_current=true, end_date=null;

-- 4. Registos que a bateria cria.
delete from t_leave_mobility; delete from t_leave_request; delete from t_leave_balance;
delete from t_historico_estado_colaborador;

-- 5. Lixo de catalogo deixado por execucoes anteriores.
delete from t_option_entity where ccode like 'TESTE%';
delete from t_worker_state where code like 'TESTE%';
delete from t_leave_mobility_subtype where code like 'TESTE%' or code like 'LIC_TST_%' or code like 'MOB_TST_%';
```

### F6 - substituicao de titular impedido (2026-09-19)

Prova o que os testes unitarios nao alcancam, porque depende de um indice
parcial na base: **duas afectacoes correntes no mesmo Lugar**, a do titular
(`PRINCIPAL`) e a de quem o substitui (`SUBSTITUICAO`).

- so se substitui quem esta impedido -- titular em actividade da 422
- Lugar vago da 422 (nomeia-se titular, nao substituto), a si proprio da 422
- quem substitui **mantem o seu proprio Lugar** (art. 91.o n.o 1 al. a))
- o Lugar do titular **continua provido**: nao aparece na lista de vagas
- segundo substituto da 409
- o **regresso do titular fecha a substituicao sozinho** (art. 77.o n.o 2),
  provado sem endpoint de leitura: uma segunda substituicao passa a ser aceite,
  quando antes dava 409

### F7 - cessacao pelos dois caminhos (2026-09-19)

Prova que o estado de cessacao e o `close` do contrato sao **equivalentes**:
ambos cessam o contrato, encerram a afectacao e mudam o estado. Cobre tambem o
efeito na substituicao: cessar o titular liberta o Lugar, e o Lugar passa a
aparecer nas vagas.

O `close` repetido **nao e erro**: e idempotente, e o 2.o nao sobrepoe a data nem
o motivo do 1.o -- e isso que o bloco verifica.

### F8 - mobilidade transitoria ponta a ponta (2026-09-19)

O bloco existe para provar o art. 135.o n.o 7: **a mobilidade nao mexe na
afectacao**. Interna e externa, prorrogacao dentro e fora do limite do subtipo,
encerramento, e os negativos (sem destino, subtipo de mobilidade a abrir vaga).

**As datas ancoram-se no dia corrente.** "Em vigor" quer dizer que a licenca
cobre HOJE: uma mobilidade marcada para o ano que vem existe mas nao poe ninguem
em mobilidade, e o ecra mostra a pessoa no seu Lugar. Foi assim que os primeiros
passos falharam -- expectativa errada do teste, nao do codigo.

Confirmado na base no fim: a colaboradora manteve **uma so** afectacao PRINCIPAL
corrente, com duas mobilidades a passar por ela sem criarem nem fecharem
afectacao nenhuma.

## Resultado da última execução

**172 passos, 172 OK** (2026-09-19), contra a base local com a V47 aplicada.

Encontrou dois problemas reais, já corrigidos:

1. **Contratos sem estado** — o seed criava contratos com `status` nulo, e por isso
   nunca eram suspensos nem reactivados: as duas operações comparam o estado actual
   e não faziam nada, em silêncio. Corrigido pela **V44** (preenche e torna
   obrigatório) e pelo seed.
2. **Grelha sem folga** — o seed punha o colaborador logo no último escalão da
   categoria, o que tornava a progressão impossível de exercitar. O seed passa a ter
   dois escalões por categoria.

Notas do que o script escreve, e que o PowerShell 5.1 obriga: o ficheiro é **só
ASCII**, porque um acento lido como ANSI parte o script a meio.
