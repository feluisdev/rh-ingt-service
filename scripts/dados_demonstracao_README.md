# Dados de demonstração

Uma base com conteúdo credível para apresentar o RH-Service: 12 colaboradores fictícios em três
serviços, com chefias, um mês e meio de marcações, ausências, férias, trabalho suplementar e uma
promoção. Carrega-se **pela API**, e por isso passa pelas mesmas regras que o front. Os nomes estão
em `dados_demonstracao.json` (UTF-8); o script `dados_demonstracao.ps1` só tem ASCII.

## Como carregar

Com a app a correr (perfil `development`, porta 8099) e a partir de uma base reposta:

```bash
docker cp scripts/repor_estado.sql postgres-ingt-rh:/tmp/repor.sql
docker exec postgres-ingt-rh sh -c "psql -U postgres -d recursoshumanos_db -q -f /tmp/repor.sql"
powershell -ExecutionPolicy Bypass -File scripts/dados_demonstracao.ps1
```

No fim, o script escreve os ids das três chefias e do ministério. **Corre-se uma vez por base
reposta**: se já encontrar o NIF 500000001, pára. O `repor_estado.sql` apaga tudo o que o script
cria, incluindo os colaboradores, os Lugares `DEMO-*`, as unidades `DEMO_*` e os horários. A bateria
funcional corre depois disso sem mudanças.

As datas são relativas ao dia em que se corre: as marcações vão do início do mês anterior até
ontem, e os pedidos pendentes ficam daqui a três semanas.

## O que fica na base

| Serviço | Chefia | Equipa |
|---|---|---|
| Serviço de Recursos Humanos (`SERV_RH`, o nome corrigido) | Ana Lúcia Fortes | Carlos, Dulce, Edson (e os três colaboradores do seed) |
| Serviço Financeiro e Patrimonial (`DEMO_SFIN`) | Fátima dos Santos Gomes | Gilberto, Helena, Ivo |
| Gabinete Jurídico (`DEMO_GJUR`) | Joana Brito Évora | Leonel, Marta, Nuno |

Cada colaborador tem contrato, afectação principal e Lugar. O Lugar da chefia gere a unidade e os
Lugares da equipa apontam para ele (chefia directa). O horário base é das 08:00 às 12:30 e das 14:00
às 17:30. A Marta e o Nuno entraram este ano, e contam nas entradas dos indicadores.

As excepções são deliberadas; tudo o resto está em ordem:

| Quem | O quê | Onde se vê |
|---|---|---|
| Dulce | um atraso de 47 min no mês anterior | relação mensal: falta parcial de 0,5 |
| Edson | um dia sem marcações no mês anterior | relação mensal: um dia sem registo |
| Helena | ontem, entrada sem saída, e uma correcção pelo `/me` | caixa da Fátima (marcações por validar); relação do mês COM_PENDENCIAS |
| Gilberto | uma semana de férias aprovada no mês anterior | relação mensal: 5 dias de férias |
| Leonel | dois dias de luto, aprovados automaticamente | relação mensal: faltas justificadas |
| Carlos | trabalho suplementar autorizado, das 18:00 às 19:30 (pelas marcações) | relação mensal: 90 min; indicadores: 1,5 h |
| Dulce | seminário pedido pelo `/me` | caixa da Ana (pedidos pendentes) |
| Nuno | férias pedidas pelo `/me` fora da marcação | caixa da Joana, com `foraDaMarcacao` |
| Ivo | trabalho suplementar pedido para um sábado | caixa da Fátima (trabalho suplementar) |
| Ana, Carlos, Fátima, Joana, Ivo | férias marcadas em Agosto, por acordo | mapa de férias do ano |
| Dulce, Edson, Helena | preferência de férias para Julho do ano seguinte, pelo próprio | mapa do ano seguinte (PROPRIO) |
| Dulce | promovida a Técnico Superior no dia 1 deste mês | afectações; lista de antiguidade do ano seguinte |

## O que mostrar

Os relatórios pedem-se ao ministério (`31e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e301`), que inclui as
subunidades por omissão. O `/me` usa, em desenvolvimento, o cabeçalho `X-Employee-Id` com o id da
chefia.

- Relação mensal do mês anterior: `GET /assiduidade/relacao-mensal?unidadeId=…&mes=AAAA-MM` (e `.csv`).
- Lista de antiguidade: `GET /relatorios/lista-antiguidade?unidadeId=…` (e `.csv`).
- Mapa de efectivos: `GET /relatorios/mapa-efectivos?unidadeId=…` (e `.csv`).
- Indicadores do pessoal: `GET /relatorios/indicadores?unidadeId=…`.
- As caixas das chefias:
  - `GET /me/equipa/pedidos-ausencia-pendentes`;
  - `GET /me/equipa/marcacoes-pendentes`;
  - `GET /me/equipa/trabalho-suplementar-pendente`;
  - `GET /me/equipa/ferias/{ano}`.
