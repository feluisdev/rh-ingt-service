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

```sql
delete from t_assignment where id not in ('e6e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ee01'::uuid,'e6e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1ee02'::uuid);
update t_assignment set data_fim=null, is_current=true, grade_id='81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e801'::uuid where funcionario_id='91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e901'::uuid;
update t_assignment set data_fim=null, is_current=true, grade_id='81e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e803'::uuid where funcionario_id='91e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e902'::uuid;
update t_funcionario set worker_state_id=(select id from t_worker_state where code='ACTIVE'), is_active=true;
update t_contrato set status='ATIVO', is_current=true, end_date=null;
delete from t_leave_mobility; delete from t_leave_request; delete from t_leave_balance; delete from t_historico_estado_colaborador;
```

## Resultado da última execução

**78 passos, 78 OK** (2026-09-18), contra a base de dados local com a V44 aplicada.

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
