# Janela de Autoavaliação SIADAP — Prazo do RH e Abertura Automática

**Versão:** 1.0 | **Status:** Ativo (Fase 102) | **Data:** 21/08/2026

---

## Como o RH define a janela

A janela de autoavaliação é um prazo de submissão como qualquer outro do ciclo PAA/SIADAP,
definido em `/admin/paa-periods`. O RH cria um período com:

- Finalidade: **Autoavaliação SIADAP** (`SIADAP_SELF_EVAL`, o sexto valor da sequência anual).
- Nível: **Individual**.
- Ano e datas de início e fim.

É a mesma lista onde o RH já define os outros cinco prazos do ciclo — objetivos
estratégicos PAA/BSC, atividades PAA, objetivos individuais SIADAP, avaliação intercalar
e avaliação final. Não há um ecrã separado para este prazo nem um comportamento
diferente do já conhecido.

## Quando o agendador corre

O `SelfEvaluationOpeningScheduler` corre diariamente à 1:00 por omissão. O valor é
alterável sem tocar em código através da propriedade
`sigdi.siadap.self-evaluation-opening.cron` (variável de ambiente
`SIGDI_SIADAP_SELF_EVALUATION_OPENING_CRON`).

Em cada execução, o agendador procura todas as avaliações que estão em **Em Curso** e,
para cada uma, pergunta se o prazo de autoavaliação individual do ano dessa avaliação
está ativo agora. Se estiver, a avaliação transita para **Autoavaliação**. Se não
estiver, a avaliação fica exatamente onde estava — nada é escrito, nada é assinalado
como pendente.

## Os dois caminhos de fecho produzem o mesmo estado

Este é o núcleo do critério 5 desta fase, e é onde a decisão de desenho evita um defeito
concreto: o RH pode fechar um prazo manualmente a qualquer momento, escrevendo
`status = CLOSED` e nada mais — sem tocar em datas, sem tocar em nenhuma avaliação. Ou o
prazo pode simplesmente deixar a data de fim passar, sem qualquer ação do RH.

Estes dois caminhos são, para o agendador, **indistinguíveis**. Ambos tornam falso o
mesmo predicado, `isActiveToday()` — o que combina "o período está aberto" com "hoje
está dentro do intervalo". O agendador nunca pergunta pela causa; pergunta apenas pelo
resultado desse predicado através da mesma consulta de sempre. Fecho manual e
**expiração natural** chegam ao agendador pela mesma porta e produzem o mesmo sinal.

Consequência prática, dita sem rodeios: **depois de qualquer um dos dois — fecho manual
ou expiração natural — o agendador deixa de abrir avaliações desse ano, e as que ficaram
em Em Curso ficam onde estão.** Não há uma segunda via, nem prevista nem acidental, por
onde a abertura continue a acontecer depois de o prazo deixar de estar ativo.

## O dia em que o agendador não corre

Se a execução diária falhar ou for saltada por qualquer razão de infraestrutura, nada se
perde **enquanto a janela continuar aberta**: a consulta que o agendador faz é sobre o
estado atual do prazo e da avaliação, não sobre uma diferença desde a última execução.
A execução seguinte processa exatamente as mesmas avaliações que teria processado no dia
que faltou.

Se, entretanto, a janela fechar — por fecho manual ou por a data ter passado — antes de o
agendador voltar a correr, essas avaliações **não** são abertas. Esta é uma consequência
declarada, não um defeito escondido: quem precisar de corrigir a situação abre um novo
prazo para esse ano. O que fazer às avaliações que ficaram sem autoavaliação por este
motivo é âmbito da Fase 103, não deste documento.

## Não há caminho de volta

A abertura da fase de autoavaliação não é reversível pela aplicação. O método privado
`changePhase` só tem um chamador em toda a base de código: `openSelfEvaluationPhase()`.
Não existe nenhum caminho de domínio — nenhum comando, nenhum endpoint — que faça uma
avaliação voltar de **Autoavaliação** para **Em Curso**.

Uma avaliação aberta por engano — por exemplo, um prazo criado com o ano errado — só
volta atrás por intervenção direta na base de dados. Nomear isto aqui é o que evita que
seja descoberto em pânico, a meio de um incidente, por quem não sabia que a transição
não tinha volta.

## Fuso horário

A decisão de "a janela está aberta hoje" é tomada no fuso horário de Cabo Verde. O
agendador não faz nenhuma comparação de datas por si próprio — delega inteiramente na
consulta de períodos, que lê a data de hoje através de `AppTimeZone.CABO_VERDE`.

Vale a pena nomear o que **não** se copiou ao construir este agendador: o
`TacitAcceptanceScheduler`, que serve de precedente para a forma do job, lê a data de
hoje sem qualquer fuso explícito (`TacitAcceptanceScheduler.java:45`), o que a faz
depender do fuso horário do contentor onde a aplicação corre. É um defeito conhecido do
agendador do PAA e continua por corrigir — não foi copiado para este agendador novo, e
não deveria ser copiado se algum dia este ficheiro servir de modelo para outro.

## O que este documento não cobre

Não há verificação de papel a proteger a criação de prazos de submissão. Qualquer
utilizador que alcance o endpoint de criação de períodos pode criar um prazo de
autoavaliação e, com isso, provocar a transição em massa de avaliações — sem que exista
`@PreAuthorize` nenhum a impedi-lo. Isto está registado como `T-010`, fora de âmbito por
decisão do operador, e não é um problema que este documento resolve.

A abertura da fase de autoavaliação torna alcançável um endpoint de escrita
(`POST .../self-evaluation`) que, até esta fase, ninguém conseguia alcançar por a fase
nunca abrir. A única barreira que protege esse endpoint é a verificação de ator em
handler — comparação direta com `evaluation.employeeId`, entregue pela Fase 101 — e não
uma camada de RBAC, que o backend não tem.
