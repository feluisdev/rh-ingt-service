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
| **F3** licenças | o prazo do subtipo decide: 180 dias mantém o Lugar, 200 abre vaga; o regresso põe em disponibilidade; mobilidade não pode abrir vaga; `AMBOS` recusado; criar subtipo pela API funciona. **Os dois eixos (V48):** deferir uma licença que só começa daqui a um mês **não** abre vaga (F3.12c) e devolve `APPROVED`/`POR_INICIAR` (F3.12e); o `close` dessa licença é recusado com 409 e o período fica intacto, sem fim anterior ao início (F3.12f–h); cancelar vale antes de começar (F3.12i) e é recusado depois (F3.15c); o regresso antecipado deixa a ausência a acabar na **véspera** (F3.18b). Datas ancoradas em `Get-Date`: com datas fixas, este bloco provava o defeito em vez da regra |
| **F4** ausências | reserva na submissão, gozo na aprovação, devolução no cancelamento, libertação na rejeição; sobreposição, falta de saldo, dupla decisão e URL de outro colaborador são recusados |
| **F5** efeitos cruzados | quem perdeu o Lugar (por estado ou por licença) não pode progredir |
| **F20** feriados (V55) | recorrentes contam em anos que o seed não traz, no período inteiro; o municipal com área só conta com a área na unidade; área desconhecida aceite, nacional com área 422, nacional repetido 409 |
| **F21** contagem (V56) | dias seguidos com o fim-de-semana intercalado, úteis só onde a lei diz; tecto de 5 seguidos; o `PUT` que omite a contagem mantém-na |
| **F22** mapa de férias | preferência, fixada fora da janela 422, marcação por acordo com alerta, mapa, publicar duas vezes 409, alterar depois de publicado só com motivo |
| **F23** parâmetros de férias | vigência nova por ano (409 repetida, 422 inválida); cada ano lê as regras do seu tempo; o mínimo interpolado novo aceita o que o da lei recusa |
| **F24** horários | NENHUM → BASE → UNIDADE (herdado da mãe) → COLABORADOR; atribuição fecha a anterior na véspera; base não se desactiva; `horarioId` da unidade mantém-se no `PUT` omisso e limpa-se em branco |
| **F25** registo diário | importação de picagens repetível (duplicadas não entram, as más vão para o relatório); dia calculado com períodos, intervalo e horas; anomalia sem saída; correcção exige motivo; anular fica visível; sábado dá alerta |
| **F26** faltas por débito | sobre a semana do F25: dia com anomalia fica por corrigir; atraso de 30 min contra o horário fixo; dia sem marcações conta inteiro (SEM_REGISTO); domingo não se apura; um pedido aprovado tira o dia do apuramento |
| **F27** pedido em horas (V58) | amamentação 1h+1h por dia durante 100 dias; a terceira passa do tecto diário; horas cruzadas 409, noutra hora cabe; tecto por ocorrência; férias em horas 422; terminar antes do fim acaba na véspera; meia hora justificada tira o atraso do apuramento |
| **F28** pedido pelo próprio (/me) | segue as regras do RH: luto de sexta a segunda conta 4 dias seguidos; seminário de 7 dias passa do tecto (422); sobreposição 409; amamentação em horas pelo próprio |
| **F37** indicadores do pessoal | indicadores do ministério com subunidades no ano corrente: referência hoje; há efectivos e as distribuições por género e unidade somam o total; escalões, carreira e contrato presentes; absentismo e horas extras calculados; entradas e saídas contadas; ano passado com referência a 31/12; ano futuro 422, unidade inexistente 404 |
| **F36** mapa de efectivos | mapa do ministério com subunidades: há Lugares e o da Maria está provido numa unidade (no fim da bateria, no DGP); totais = soma das unidades e providos + vagos = lugares; CSV; unidade mal escrita 422, inexistente 404 |
| **F35** férias: preferência pelo próprio e fora da marcação | a Maria indica a preferência pelo /me e fica PROPRIO; inactivo 403; a equipa dela vazia; pedido de férias dentro da marcação sem aviso, fora dela aceite com o aviso do art. 6.º n.º 2 (RH e /me) |
| **F34** lista de antiguidade | lista do ministério com subunidades: referência a 31/12 do ano anterior; a Maria uma vez, com início no cargo, posição e tempo contado; posições seguidas em cada cargo; CSV com cabeçalho e a linha dela; ano futuro 422, unidade mal escrita 422, inexistente 404 |
| **F33** horários com data de efeito | mudar os blocos do base (vigorou) 409, só o nome 200; duplicar dá cópia não-base editável; base com data passada 422; desactivar o base agendado 409; horário da unidade daqui a 3 dias: hoje ainda nenhum, antes vale o base, depois o da unidade; data passada 422 |
| **F32** aprovação dos pedidos de ausência | luto nasce APROVADO com aprovação automática (aprovar outra vez 409); seminário nasce PENDENTE; pela caixa da chefia: a própria 422, quem não é chefia 403 (aprovar e rejeitar); RH pelo caminho de outro colaborador 404; RH aprova, segunda decisão 409; caixa da Maria vazia |
| **F31** relação mensal | o mês do F25 pedido ao ministério com subunidades: a Maria uma vez, no SERV_RH, com os 90 min suplementares, os 30 min de tratamento ambulatório e COM_PENDENCIAS (segunda por corrigir); sem subunidades não entra; o mês corrente é provisório; CSV text/csv com cabeçalho e a linha dela; mês futuro, mal escrito, unidade vazia ou mal escrita 422; unidade inexistente 404 |
| **F30** trabalho suplementar | RH autoriza depois a terça do F25 fora do horário (90 min realizados pelas marcações, dia útil); tocar no horário 422, sem motivo 422, hora mal escrita 422, sobreposto 409, inactivo 403; o apuramento separa 90 min suplementares; a Maria pede para um sábado (para trás 422), ela própria 422, quem não é chefia 403, RH recusa sem motivo 422, autoriza, segunda vez 409, cancela |
| **F29** registo pelo próprio | picagem pelo /me: presencial 422, inactivo 403, em teletrabalho 201; correcção fica PENDENTE e não conta; quem não é chefia 403, o próprio 422; RH rejeita sem motivo 422, valida, segunda vez 409; rejeitada fica visível e não conta |

## Repor o estado inicial

Corre isto **antes de cada execucao**. A bateria muda dados de proposito (abre
vagas, suspende contratos, gasta saldos), e uma segunda execucao sobre o estado
deixado pela primeira falha por motivos que nada tem a ver com o codigo.

```sql
-- 0. Colaboradores que a bateria cria (F11 admite gente a cada execucao, para provar o
--    vencimento de ferias). Nunca eram apagados: ao fim de algumas corridas havia dezenas, e
--    como o F0.1 le a PRIMEIRA PAGINA de /funcionarios, o trio do seed saia dela -- a bateria
--    falhava do F0 em diante por dados acumulados, nao por codigo. Apaga-se antes de tudo o
--    resto, pela ordem das chaves estrangeiras.
DO $$
DECLARE extras uuid[];
BEGIN
  SELECT coalesce(array_agg(id), '{}') INTO extras FROM t_funcionario
   WHERE numero_funcionario NOT IN ('0000001','0000002','0000003');
  IF array_length(extras, 1) IS NULL THEN RETURN; END IF;

  UPDATE t_unidade_organica SET responsible_employee_id = NULL
   WHERE responsible_employee_id = ANY(extras);

  DELETE FROM t_leave_request  WHERE funcionario_id = ANY(extras);
  DELETE FROM t_leave_balance  WHERE funcionario_id = ANY(extras);
  DELETE FROM t_leave_mobility WHERE funcionario_id = ANY(extras);
  DELETE FROM t_assignment     WHERE funcionario_id = ANY(extras);
  DELETE FROM t_contrato       WHERE funcionario_id = ANY(extras);
  DELETE FROM t_dados_bancarios WHERE funcionario_id = ANY(extras);
  DELETE FROM t_dependente     WHERE funcionario_id = ANY(extras);
  DELETE FROM t_disciplinary_process WHERE funcionario_id = ANY(extras);
  DELETE FROM t_historico_estado_colaborador WHERE funcionario_id = ANY(extras);
  DELETE FROM t_payroll_slip   WHERE funcionario_id = ANY(extras);
  DELETE FROM t_qualificacao   WHERE funcionario_id = ANY(extras);
  DELETE FROM t_training       WHERE funcionario_id = ANY(extras);
  DELETE FROM t_funcionario    WHERE id = ANY(extras);
END $$;

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
--    O LUG-0007 e da outra carreira (Regime Especial): a mudanca de carreira
--    ocupa-o, e sem repor carreira E categoria a execucao seguinte nao tem destino.
update t_position set career_id='61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e602'::uuid,
       category_id='71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e703'::uuid where numero_lugar = 'LUG-0007';
--    O LUG-0008 vive noutra unidade (DGP) e e o destino da consolidacao da mobilidade.
update t_position set unidade_organica_id='31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e302'::uuid,
       career_id='61e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e601'::uuid,
       category_id='71e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e702'::uuid where numero_lugar = 'LUG-0008';

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
- o **regresso do titular fecha a substituicao sozinho** (art. 77.o n.o 2)
- **as substituicoes leem-se** (`GET /funcionarios/{id}/substituicoes`): os dois
  papeis na mesma consulta -- `SUBSTITUTO` visto de quem substitui (F6.29c) e
  `TITULAR` visto de quem e substituido (F6.29g) --, com a contraparte e o Lugar
- enquanto dura, **sem data de fim**: caduca com o regresso, nao numa data
  combinada (F6.29e2)
- o fecho automatico **deixou de se provar por via indirecta**: o F6.30b confirma
  que ja nao ha nenhuma em vigor e o F6.30d que o historico a guarda, encerrada e
  com data de fim. A prova antiga (uma segunda substituicao passa a ser aceite,
  quando antes dava 409) fica como confirmacao

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

### F9 - promocao nas duas formas (2026-09-19)

A aplicacao deduz a modalidade do pedido e nao a persiste. O bloco prova as
duas: com `positionId` a pessoa muda para o Lugar vago da categoria de cima
(`lugarReclassificado=false`, e o Lugar que deixou fica vago); sem `positionId`
e o **proprio Lugar** que sobe de categoria (`lugarReclassificado=true`, a pessoa
fica onde esta e o `GET /estrutura/positions/{id}` mostra a categoria nova).

Negativos: mesma categoria, Lugar de outra categoria, data anterior a afectacao,
categoria inexistente.

Depende de `ordem_progressao` estar definida nas categorias -- sem ela a
promocao recusa sempre. Esta no seed (ASS_TEC=1, TEC_SUP=2).

### F10 - contrato das respostas (2026-09-19)

Le as respostas como um cliente as le, e nao so o codigo HTTP: `{id, sucesso,
alertas}` nas operacoes simples, `alertas` vazio e nunca nulo, **`message` ja nao
existe**, o `assignmentType` fora da lista da 422, e os erros continuam com o
corpo de problema (`title`) e **sem** `sucesso`.

**A bateria passou a enviar `Accept: application/json`.** Sem esse cabecalho o
servidor negoceia e devolve os **erros em XML** (ProblemDetail) enquanto os
sucessos vem em JSON -- dois formatos na mesma API. Foi o F10.12 que o expos: o
`ConvertFrom-Json` falhava no corpo do 404. O `api_guide` ja avisava; agora a
bateria comporta-se como um cliente correcto.

### F11 - vencimento de ferias (2026-09-22)

Prova que o saldo de ferias **nasce sozinho** (DL 3/2010, art. 2.o n.o 4) e que e
**proporcional no ano de ingresso** (art. 3.o). Cria colaboradores pela API com
datas de admissao diferentes e le o saldo que aparece sem ninguem o criar:

| Admitido em | Dias | Porque |
|---|---|---|
| 1 de Janeiro | 22 | ano inteiro, valor do catalogo |
| 1 de Julho | 11 | dois trimestres completos |
| 1 de Dezembro | 0 | menos de 90 dias de servico efectivo |

Cobre tambem a classificacao do catalogo: o `regime` (`FERIAS`/`FALTA`) e que diz
quais linhas sao ferias -- **nunca o codigo** --, um tipo novo nasce `FALTA` (a
omissao segura), reclassifica-se pela API, e um valor fora da lista da lei da 422.

### F12 - acumulacao de ferias (2026-09-22)

Art. 7.o n.o 1: os dias que, **por motivo de servico**, nao puderam ser gozados
passam para o ano seguinte. Nao e automatico -- e um acto com motivo obrigatorio.

- sem motivo da 400; mais dias do que sobram da 422
- a origem perde os dias cedidos e o destino recebe-os **a parte** do direito do
  proprio ano (`diasAcumulados` + motivo)
- ceder outra vez os mesmos dias da 422
- os dias **recebidos nao voltam a ser acumulaveis**: o horizonte da lei e de um
  ano (art. 7.o n.o 1 e art. 8.o n.o 4), nao uma corrente
- o saldo do ano de destino **nasce da propria acumulacao**, porque a autorizacao
  pode acontecer em Dezembro, antes de o job do vencimento passar
- uma falta nao se acumula (422); o saldo de outro colaborador pelo URL deste, 404

### F13 - suspensao de ferias (2026-09-22)

Art. 8.o: as ferias suspendem-se por parentalidade, doenca, assistencia a
familiares ou razoes imperiosas de servico. Ate aqui um pedido de ferias e um de
doenca **nao se falavam**: quem adoecesse a meio perdia-as.

- o **ultimo dia de ferias e a vespera** da data indicada -- o n.o 3 diz «a partir
  da data da entrada no servico do documento comprovativo»
- o pedido continua **`APROVADO`**: a decisao nao se desfaz, o que encurta e o
  periodo (a mesma separacao da V48)
- os dias **voltam mesmo ao saldo**, e prova-se com a diferenca: 14 disponiveis
  antes, 4 gozados ate a interrupcao, 10 depois
- sem motivo da 400; data futura da 400; suspender duas vezes da 409; o pedido de
  outro colaborador pelo URL deste da 404

### F14 - antiguidade (2026-09-22)

Prova que o tempo de servico se calcula, e que as colunas que ninguem lia passaram a ser lidas.
Usa o colaborador admitido a 1 de Janeiro (F11.4), de percurso conhecido:

- conta **desde a admissao**, e sem nada a descontar o contado e o total
- `?ate=` responde a "quanta antiguidade tinha a data X" -- Janeiro da 31 dias, com os dois
  extremos incluidos
- pos-se o colaborador em **inactividade no quadro** durante 30 dias e traz-se de volta: o
  desconto e de exactamente 30 dias (art. 120.o n.o 2), o contado baixa na mesma medida, e o
  resultado **diz porque descontou** e em que periodo
- a **disponibilidade nao desconta** (art. 122.o n.o 1): o total descontado nao mexe
- colaborador inexistente da 404

### F15 - mudanca de carreira (2026-09-22)

Prova o movimento que ate aqui nao tinha caminho nenhum: a promocao exige a **mesma carreira**
e a transferencia a **mesma categoria**, logo mudar de carreira era impossivel.

Usa o colaborador B, que no fim do F9 esta num Lugar do Regime Geral, e o **LUG-0007** do seed,
vago no Regime Especial -- o unico destino de carreira diferente que existe.

- **a carreira tem de mudar**: um destino da mesma carreira da 422, e o erro remete para a
  promocao ou para a transferencia. E a guarda que impede este caminho de ser uma promocao
  sem as regras da promocao
- destino igual ao Lugar actual, data anterior a afectacao corrente e Lugar inexistente sao
  recusados como nos outros movimentos
- **o escalao nao se herda**: o escalao do F9, que e da carreira de origem, da 422; sem
  `gradeId` entra-se pelo primeiro escalao activo da categoria de destino
- a resposta traz as **duas pontas da grelha** (carreira e categoria, antes e depois) e de que
  Lugar veio
- depois do movimento, o Lugar que deixou fica **vago** e o de destino **deixa de o estar**

O seed ganhou o que faltava para isto ser exercitavel: a carreira REG_ESP existia mas sem
categoria, sem escaloes e sem Lugar -- ou seja, nao era utilizavel. Passou a ter a categoria
TEC_ESP, dois escaloes e o LUG-0007.

### F16 - consolidacao da mobilidade (2026-09-22)

Prova o art. 132.o n.o 4: a mobilidade definitiva ocorre por **consolidacao da transitoria, na
mesma funcao e categoria**. E a unica via pela qual uma mobilidade toca na afectacao -- e nao
contradiz o art. 135.o n.o 7, que diz que a TRANSITORIA nao ocupa Lugar: o n.o 8 define a
definitiva como a que e feita "com ocupacao do lugar do quadro".

O bloco **navega para encontrar o cenario**, em vez de o escrever a mao: junta as vagas de todas
as unidades e procura um PAR de Lugares vagos do mesmo cargo e da mesma categoria em unidades
diferentes. Se o seed mudar de numeracao, continua a servir.

- Lugar de destino **inexistente** da 404; sem `positionId` da 400
- Lugar que **nao e da unidade de destino** da 422: consolida-se onde se esteve em mobilidade
- mobilidade de **outro colaborador** pelo URL deste da 404
- consolidada, a pessoa e **titular do Lugar de destino**, mudou de unidade organica, o Lugar
  que deixou ficou **vago** e o de destino **deixou de o estar**
- o **despacho nao se desfaz**: fica `APPROVED`, com o periodo `TERMINADA` e o ultimo dia em
  mobilidade na **vespera** da data de efeito
- **consolidar duas vezes** da 409

Nao se afirma que `emMobilidade` passa a falso: esse campo responde por QUALQUER mobilidade em
vigor, e os blocos anteriores deixam uma que cobre hoje -- comecou e foi encerrada no mesmo dia,
ficando com um dia, que e o comportamento deliberado da BR-MOB-14.

O seed ganhou o **LUG-0008**, vago noutra unidade (DGP) com o mesmo cargo e categoria do
LUG-0002. Sem ele nao ha destino possivel: todos os outros Lugares vivem no SERV_RH.

### F17 - regresso de comissao de servico (2026-09-22)

Prova o art. 64.o n.o 2: «Cessada a comissao de servico, o nomeado regressa a situacao
juridico-funcional de que era titular antes dela, quando constituida e consolidada por tempo
indeterminado, ou, **no caso contrario, cessa a relacao juridica de emprego publico**.»

Sao **duas saidas**, e o caminho **deriva-se do percurso**, nao de um campo que alguem preencha:
a comissao mantem o Lugar, logo quem tinha situacao anterior continua titular dele e regressa;
quem foi recrutado *para* a comissao nunca teve situacao para onde voltar.

Era uma falta **silenciosa**: o catalogo classificava a comissao como `REGRESSA_LUGAR` sem
condicao, e o regresso devolvia ao Lugar de origem toda a gente. Nada falhava.

- o subtipo vem do catalogo classificado `REGRESSA_OU_CESSA`, mantem o Lugar e tem a duracao da
  comissao -- **1095 dias sem limite de prorrogacoes** (art. 60.o n.o 1), e nao o ano com uma
  prorrogacao da mobilidade comum (art. 132.o n.o 5)
- **quem tinha Lugar regressa**: o `close` nao atribui estado nenhum, e a pessoa continua
  titular do mesmo Lugar
- o registo fica `APPROVED` com o periodo `TERMINADA`, e o ultimo dia em comissao e a **vespera**
  do regresso
- **quem nao tinha Lugar cessa**: admite-se alguem sem afectacao nenhuma (`unidade-atual` da
  404), nomeia-se em comissao e, ao cessa-la, o vinculo termina no estado de cessacao
- a cessacao **fica no historico** com o motivo do subtipo e o artigo por extenso, em vez de ser
  um desaparecimento silencioso
- **a bifurcacao e do catalogo, nao de "nao ter Lugar"**: repete-se o mesmo cenario com um
  subtipo `REGRESSA_LUGAR` e nao cessa nada nem escreve historico. Sem este passo o bloco
  provaria outra regra -- que quem nao tem Lugar cessa --, que nao e a da lei
- **cessar a comissao duas vezes** da 409

Como no F16, nao se afirma que `emMobilidade` passa a falso: esse campo responde por QUALQUER
mobilidade em vigor, e o F8 deixa uma externa que comecou e foi encerrada no mesmo dia -- fica
com um dia, que cobre hoje (BR-MOB-14). Prova-se o **proprio registo**. Custou uma execucao.

O `repor_estado.sql` ganhou a **reclassificacao do MOB_COMISSAO**: o seed usa
`ON CONFLICT (code) DO NOTHING`, portanto numa base ja criada a linha antiga fica como estava, e
o bloco correria contra o catalogo velho sem provar nada.

E o F8 passou a escolher a mobilidade **comum** de forma explicita (`REGRESSA_LUGAR`) em vez da
primeira da lista: a ordem do catalogo nao e garantida e, se lhe calhasse a comissao, o F8.21
(prorrogar alem do maximo) deixava de poder falhar -- ela nao tem limite de prorrogacoes.

### F18 - limites de dias por natureza (2026-09-22)

Prova o art. 15.o n.o 1 do DL n.o 3/2010 e a **V53**: o tecto de dias nao e um numero, sao tres,
e cada um conta uma coisa diferente. O catalogo so sabia dizer "X dias por ano", e o pedido
somava sempre o ano civil -- mas o artigo quase nunca fala em anos.

- o catalogo devolve os tres tectos, e o seed traz as alineas ja classificadas: a prova de exame
  sao **2 por cada prova** (al. f), a assistencia a familiar **15 por ano** (al. j), a falta
  autorizada pelo dirigente **6 por ano E um por mes** (al. q), o luto **8 por falecimento** do
  conjuge ou 1.o grau (al. b) -- e nao 5 por ano, como dizia
- uma semana inteira para uma prova de dois dias da **422**
- **tres provas em tres semanas passam todas**, e no fim do ano ja foram mais dias do que o tecto
  de cada uma: e o ponto do bloco -- um tecto **por ocorrencia nao se acumula**. Antes da V53 o
  segundo pedido morria, e era assim que o segundo funeral do ano era recusado
- o tecto **anual**, esse, soma: duas semanas de assistencia passam, as duas seguintes dao 422
- o tecto **mensal** conta o mes civil: uma falta autorizada passa, a segunda no mesmo mes da
  422, e a do mes seguinte passa outra vez -- o tecto anual de 6 ainda tinha folga
- a instituicao parametriza os tres pela API; um tecto de **zero** da 400, porque um limite de
  zero dias nao e um limite: e um tipo que ninguem pode pedir, e isso diz-se desactivando a linha

**Todas as datas do bloco sao disjuntas, e nao por acaso:** a sobreposicao e verificada por
funcionario e nao por tipo, logo dois pedidos que se cruzem dao 409 antes de chegarem ao limite --
e o bloco estaria a provar outra coisa. As semanas ancoram-se na proxima segunda-feira a contar
de `Get-Date`, e as contagens sao lidas da resposta em vez de assumidas, para um feriado no meio
nao transformar uma regra certa numa falha.

O `repor_estado.sql` ganhou a correccao do `LUTO` e da `DOENCA`, pela mesma razao do
`MOB_COMISSAO`: o seed usa `ON CONFLICT (code) DO NOTHING` e nao toca em linhas que ja existam.

### F19 - falta injustificada e efeito na remuneracao (2026-09-22)

Prova o art. 43.o n.o 2 e o art. 16.o do DL n.o 3/2010, e sobretudo o que os separa: **so uma das
duas coisas e escolha**. O desconto na antiguidade e imperativo; a opcao entre perder a
remuneracao e descontar nas ferias e de cada caso.

- o catalogo traz o terceiro regime e a classificacao do art. 16.o: a greve perde remuneracao
  mas continua `FALTA` (n.o 4 -- nao desconta antiguidade), a doenca perde **parcialmente**
  (n.o 2), e a injustificada fica em `DEPENDE_DA_OPCAO`, porque ai a lei nao fixa, da uma opcao
- registar uma falta injustificada **sem dizer o que se faz aos dias** da 422; com um valor fora
  da lista, 422; e a mesma opcao **num tipo que nao e injustificado** tambem da 422
- duas faltas, duas opcoes diferentes -- cada pedido guarda a **sua**, que e o ponto de a opcao
  viver no pedido e nao no catalogo
- **a antiguidade desconta-as**, e prova-se pela DIFERENCA: mede-se antes, registam-se as duas
  faltas, e descontam exactamente dois dias. Cancelar uma devolve um -- um pedido sem efeito nao
  produziu ausencia nenhuma
- a instituicao classifica pela API (regime e efeito), e valores fora das listas da lei dao 422

**Duas armadilhas que este bloco pagou:**

1. **As faltas tem de ser no PASSADO.** Uma falta marcada para daqui a dois meses nao desconta
   antiguidade nenhuma -- o calculador recorta os periodos ao tempo ja servido, e bem. A primeira
   versao do bloco punha-as no futuro, como o resto do F18, e media zero.
2. **Mede-se a diferenca, nao o total.** O B chega aqui com dias ja descontados de blocos
   anteriores (esteve em inactividade fora do quadro). Um `-ge 2` passava sem o bloco provar
   nada; o que prova e `depois - antes = 2`.

**E um defeito do seed que so apareceu aqui:** a falta injustificada vinha com
`deducts_balance = true`, o que exigia um *saldo de faltas injustificadas* para se poder registar
uma -- isto e, uma quota de faltar, o oposto do que a lei diz. Passou a `false`: o que ela pode
descontar sao **ferias**, e isso e a opcao do art. 43.o n.o 2, guardada no pedido.

O F11.3 foi actualizado: os regimes passaram a ser tres.

### F20 a F24 - calendario, contagem, mapa de ferias, parametros e horarios (2026-09-23)

Os cinco blocos usam **anos futuros** (ano corrente + 2, + 3 e + 4), longe dos pedidos dos blocos
anteriores, que andam nos proximos meses: assim nao colidem com nada, e os feriados recorrentes do
seed (desde 2026) tem de valer num ano que o seed nao traz. Tudo com a **Maria** (0000002): no fim
do F19 e a unica com Lugar (o Francisco perdeu-o, a Joana foi cessada).

- **F20** cria um tipo de teste sem saldo nem tectos, em dias uteis (`CNT_TST_<hora>`), para medir
  so o calendario. O numero esperado de 24/12 a 04/01 calcula-se no proprio script (dias uteis
  menos Natal e Ano Novo). A area vai para a unidade onde a Maria exerce e sai no fim do bloco.
- **F21** usa os tipos do seed ja classificados (LUTO e SEMINARIO seguidos, TE_PESQUISA uteis).
- **F22** usa tres semanas de Setembro (sem feriados no seed): 15 dias uteis, abaixo do direito,
  e por isso 200 com alerta.
- **F23** cria uma vigencia no ano + 4 com minimo interpolado 10: a marcacao 10+6 passa nesse ano e
  e recusada no anterior, que le a linha da lei (11).
- **F24** e o cenario da prova manual dos horarios. O horario vai para a **mae** da unidade da
  Maria, para provar a heranca.

**Duas armadilhas que estes blocos pagaram:**

1. **A funcao `Linhas` perdia as listas simples.** Com uma resposta que e um array (sem pagina),
   `$d.content` nao e `$null` no PowerShell: enumera os membros e devolve um nulo por elemento.
   A lista vinha com o tamanho certo e os elementos vazios. Nenhum bloco anterior lia uma lista
   simples; o F24 (historico de horarios) foi o primeiro. Corrigido no `Linhas`.
2. **Hoje, a Maria esta numa mobilidade externa de um dia** (criada pelo F8). Sem unidade onde
   exerca, o horario que vale e o base -- e bem. A heranca da unidade prova-se cinco dias a frente.

A V56 passou 15 tipos de ausencia a dias seguidos, mas **nenhum bloco antigo partiu**: os que
verificam `numeroDias` usam periodos de segunda a sexta.

### F25 - registo diario de assiduidade (2026-09-23)

Seis semanas atras, longe das mobilidades e dos pedidos dos blocos anteriores; o horario esperado e
o base que o F24 deixou (TST Atendimento, 8h). Importa um lote de picagens da Maria pelo numero de
funcionario (5 boas, uma de numero desconhecido, uma sem referencia), repete o lote (5 duplicadas,
nada entra), le a semana, corrige a terca (sem motivo 422, com motivo 201), anula a saida das 12:30
(o dia passa a ter ENTRADAS_SEGUIDAS e 210 minutos, e a anulada continua visivel).

**Armadilha de ambiente, nao de codigo:** numa das corridas o F21.1 deu 500 porque o pool de
ligacoes da app tinha ligacoes a base ja fechadas (SQLState 08003, `This connection has been
closed`). O pool recuperou sozinho e a corrida seguinte passou toda. Se acontecer, repor e correr
outra vez; se se repetir, reiniciar a app.

### F26 - faltas por debito (2026-09-23)

Le o apuramento do mes da semana do F25 e verifica os dias que o F25 deixou: a segunda por corrigir,
a terca com 30 minutos de atraso (INCOMPLETO), a quarta sem marcacoes (SEM_REGISTO, 480 min). Depois
justifica a quarta com um pedido de ausencia aprovado (pelo Francisco) e a quarta sai do apuramento.

**Armadilha paga:** o sabado dessa semana e 15 de Agosto -- feriado recorrente do seed --, e o dia
vinha FERIADO, com razao. O passo verifica o domingo e aceita DESCANSO ou FERIADO.

### F27 - pedido em horas e dispensa de amamentacao (2026-09-23)

A amamentacao sao dois pedidos de 1 hora (08:00-09:00 e 15:00-16:00) durante 100 dias, daqui a
200 dias (longe de tudo). Um terceiro de 30 min passa das 2 horas por dia; um tratamento ambulatorio
das 08:30 as 09:30 nesses dias cruza-se (409), das 10:00 as 11:00 cabe. Depois de aprovar, termina-se
a amamentacao da manha ao 10.o dia (acaba na vespera, continua APROVADO). Por fim, meia hora de
tratamento ambulatorio aprovada na terca do F25 cobre o atraso, e a terca passa a SEM_FALTA com 30 min
justificados. O `repor_estado.sql` traz os cinco tipos novos (o seed nao os mete numa base ja criada).

### F28 - pedido de ausencia pelo proprio (/me) com as regras do RH (2026-09-23)

O `Chamar` ganhou o parametro opcional `-Como <funcionarioId>`, que envia o cabecalho `X-Employee-Id`
(em desenvolvimento e assim que o `/me` sabe quem e o utilizador). A Maria pede pelo `/me` e o pedido
conta como o do RH. Armadilha paga: a sobreposicao testada num sabado dava 422 («nao contem dias
uteis», e bem) antes de chegar a sobreposicao; passou para a segunda-feira.

### F29 - registo pelo proprio e validacao (2026-09-23)

A picagem em tempo real prova-se com um admitido do F11 (activo) a quem se atribui teletrabalho a partir
de hoje; o Francisco, inactivo no fim do F19, prova o 403. A Maria pede correcoes de ontem; o RH valida
uma e rejeita outra. A validacao pela chefia directa fica nos testes unitarios: o seed nao tem Lugares
com Lugar-pai, e no fim da bateria so a Maria tem Lugar.

**Armadilha paga:** o `repor_estado.sql` apaga os admitidos do F11 no ponto 0, antes de tudo -- e o
F29 deixou-lhes marcacoes e atribuicoes de horario. O DELETE falhou pelas chaves estrangeiras, o script
parou, e a segunda corrida caiu em 223 passos por estado nao reposto. O ponto 0 passou a apagar primeiro
as marcacoes, as atribuicoes e as ferias desses colaboradores.

### F30 - trabalho suplementar (2026-09-23)

A terca do F25 foi das 08:00 as 17:00 contra o base 07:30-15:30: o RH autoriza depois (caso urgente)
das 15:30 as 17:30, e o realizado sai das marcacoes -- 90 min. No apuramento dessa terca, os 90 min
passam a `minutosSuplementares` e saem do tempo normal. A Maria pede pelo `/me` para um sabado daqui a
uma semana (qualquer hora serve num dia de descanso); o RH autoriza e depois cancela. A decisao pela
chefia directa fica nos testes unitarios, pela mesma razao do F29. O `repor_estado.sql` apaga o
trabalho suplementar (ponto 0 para os admitidos do F11; ponto 9 para todos).

### F31 - relacao mensal (2026-09-23)

Le o mes da semana do F25 ao nivel do ministerio (MIN_FIN), com as subunidades: a Maria esta no
SERV_RH, dois niveis abaixo. **Armadilha paga:** no PowerShell 5.1 o `Invoke-WebRequest` entrega um
`text/csv` ja como texto, e o `GetString` sobre ele falhava em silencio (dentro do `try`); o passo aceita
agora texto ou bytes.

### F32 - aprovacao automatica e decisao da chefia (2026-09-24)

Os tipos sem aprovacao nascem aprovados: o passo condicional do F26 («aprovar, se ainda nao estiver
aprovado») deixou de correr, porque o tipo de teste desse pedido nao requer aprovacao -- por isso a
bateria sem o F32 passou de 681 para 680 passos, sem nenhuma falha. A decisao pela chefia directa fica
nos testes unitarios, pela mesma razao do F29.

### F33 - horarios com data de efeito (2026-09-24)

Os horarios passaram a ter data de efeito, e a bateria cria-os hoje mas apura semanas passadas: o F24
agenda agora o segundo base para daqui a 60 dias (o primeiro base vale desde sempre), e o F25 a F31
apuram contra o base normal (08:00-12:30 e 14:00-17:30). Por isso o F27 justifica a meia hora em falta
no fim do dia (17:00-17:30), e o F30 lanca as marcacoes das 18:00 as 19:30 e autoriza o suplementar das
18:00 as 20:00. **Armadilha paga:** a listagem dos pedidos de ausencia dava 500 intermitente
(`upper(bytea)`: o estado nulo ia para o PostgreSQL sem tipo), conforme a ordem das primeiras chamadas
depois do arranque -- corrigido com `CAST(:estado AS string)`. O F16.11 e condicional (so corre se
houver um Lugar vago noutra unidade): 714 ou 715 passos conforme o estado dos Lugares.

### F34 - lista de antiguidade (2026-09-24)

Le a lista do ano corrente (referencia 31 de Dezembro do ano anterior) ao nivel do ministerio. A Maria
aparece com o inicio no cargo de 2015-06-01 (o seed) e 10 anos e 7 meses contados.

## Resultado da última execução

**751 passos, 751 OK** (2026-09-24), duas execucoes seguidas com o `repor_estado.sql` entre elas
(F37 incluido). Armadilha paga: os admitidos do F11 nao tem Lugar em nenhum servico, logo nao
entram nas entradas do ano do ministerio -- o F37 so verifica que entradas e saidas vem contadas.

**742 passos, 742 OK** (2026-09-24), duas execucoes seguidas com o `repor_estado.sql` entre elas
(F36 incluido). Armadilha paga: no fim da bateria o Lugar principal da Maria e do DGP (os blocos de
mobilidade e promocao mudam-na), e nao do SERV_RH -- o F36 nao presume a unidade.

**736 passos, 736 OK** (2026-09-24), duas execucoes seguidas com o `repor_estado.sql` entre elas
(F35 incluido).

**724 passos, 724 OK** (2026-09-24), duas execucoes seguidas com o `repor_estado.sql` entre elas
(F34 incluido).

**715 passos, 715 OK** (2026-09-24), duas execucoes seguidas com o `repor_estado.sql` entre elas
(a primeira com 714: o F16.11 condicional nao correu; 0 falhas nas duas).

**695 passos, 695 OK** (2026-09-24), duas execucoes seguidas com o `repor_estado.sql` entre elas
(F32 incluido).

**681 passos, 681 OK** (2026-09-23), duas execucoes seguidas com o `repor_estado.sql` entre elas
(F31 incluido).

**663 passos, 663 OK** (2026-09-23), duas execucoes seguidas com o `repor_estado.sql` entre elas
(F30 incluido).

**636 passos, 636 OK** (2026-09-23), duas execucoes seguidas com o `repor_estado.sql` entre elas.

**614 passos, 614 OK** (2026-09-23), duas execucoes seguidas com o `repor_estado.sql` entre elas.

**606 passos, 606 OK** (2026-09-23), duas execucoes seguidas com o `repor_estado.sql` entre elas
(F27 incluido; V58 aplicada).

**587 passos, 587 OK** (2026-09-23), duas execucoes seguidas com o `repor_estado.sql` entre elas
(F26 incluido).

**575 passos, 575 OK** (2026-09-23), duas execucoes seguidas com o `repor_estado.sql` entre elas,
contra a base local com a V57 aplicada (F25 incluido).

**553 passos, 553 OK** (2026-09-23), duas execucoes seguidas com o `repor_estado.sql` entre elas,
contra a base local com a V57 aplicada. A anterior (antes dos blocos novos) deu 458 de 458: um passo
a mais do que os 457 de 2026-09-22 que este README dizia.

**457 passos, 457 OK** (2026-09-22), contra a base local com a V54 aplicada.

O F15 (mudanca de carreira), o F16 (consolidacao da mobilidade), o F17 (regresso de comissao), o
F18 (limites de dias por natureza) e o F19 (falta injustificada e efeito na remuneracao) entraram
nesta data, mais os passos da forma de prestacao no F8, os da porta generica no F10 e as sete
modalidades do art. 45.o no F3.
Nenhum tem migracao: a `origem` da afectacao e `VARCHAR(20)` sem restricao, e o `return_effect`
tambem nao tem restricao na base.

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
