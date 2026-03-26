# Documento de UX/UI e Arquitetura de Informação - SIGDI

**Versão:** 1.0\
**Status:** Design e Prototipagem\
**Data:** 15/03/2026

***

## 1. Personas e Fluxo Crítico

### Personas

1. **Ana, a Gestora de Planeamento (Administrador/Gestor Estratégico):** Focada em visão macro, relatórios de cumprimento de metas e conformidade legal (SIADAP). Tem pouco tempo e precisa de dashboards que "falem" imediatamente onde estão os problemas.
2. **Carlos, o Técnico Administrativo (Utilizador Final/Técnico):** Responsável por inserir os dados do dia-a-dia (atividades, despesas). Teme cometer erros que bloqueiem o orçamento. Precisa de um sistema "à prova de falhas" que o guie.

### Fluxo Crítico

**"Do Planeamento à Execução"**: Criação de uma Atividade no PAA -> Vinculação Orçamental -> Aprovação -> Monitorização da Execução.

***

## 2. Mapa de Jornada do Usuário (User Journey) - Persona: Carlos (Técnico)

**Cenário:** Carlos precisa registar uma nova formação para a equipa no PAA e garantir que há verba.

| Etapa                 | Ações do Usuário                                       | Pontos de Contato                      | Emoções/Dores                                                   | Oportunidades UX                                                                                                              |
| :-------------------- | :----------------------------------------------------- | :------------------------------------- | :-------------------------------------------------------------- | :---------------------------------------------------------------------------------------------------------------------------- |
| **1. Descoberta**     | Acede ao SIGDI via Autentika para inserir a atividade. | Login SSO, Dashboard Inicial.          | *Ansiedade:* "Será que vou conseguir logar rápido? Onde clico?" | Dashboard limpo com botão de ação primária "Nova Atividade" em destaque (FAB ou Top Bar).                                     |
| **2. Definição**      | Preenche o formulário 5W2H da atividade.               | Modal de Cadastro de Atividade.        | *Dúvida:* "Qual objetivo estratégico devo selecionar?"          | **Smart Select:** O sistema sugere objetivos baseados no departamento do Carlos. Tooltips explicativos nos campos.            |
| **3. Orçamentação**   | Define os custos (Driver-Based).                       | Calculadora integrada no formulário.   | *Frustração:* "Tenho que calcular as diárias na mão?"           | **Automação:** Carlos insere "3 pessoas, 2 dias, Ilha do Sal" e o sistema preenche o valor automaticamente.                   |
| **4. Validação**      | Submete para aprovação.                                | Botão "Submeter". Feedback de sistema. | *Medo:* "E se não tiver dinheiro no SIGOF?"                     | **Pré-validação:** O sistema verifica o saldo estimado em tempo real antes do envio e mostra um "Check verde" tranquilizador. |
| **5. Acompanhamento** | Aguarda aprovação.                                     | Notificações/Lista de Tarefas.         | *Impaciência:* "O chefe já viu isso?"                           | Barra de progresso do workflow: "Em análise por Ana (Gestora)".                                                               |

***

## 3. Arquitetura de Informação (Sitemap)

A estrutura deve ser rasa (máximo 3 cliques para qualquer ação crítica).

* **Login (Autentika)**

* **Dashboard (Home)**

  * Visão Geral (Gráficos de Progresso, Pendências, Alertas Orçamentais)

  * Meus Atalhos

* **1. Estratégia (Visão Macro)**

  * 1.1. Identidade (Missão/Visão)

  * 1.2. Mapa Estratégico (BSC Visual)

  * 1.3. Matriz SWOT

* **2. Planeamento (Tático/Operacional)**

  * 2.1. Meus OKRs

  * 2.2. Gestão do PAA (Lista de Atividades 5W2H)

    * *Ação:* Nova Atividade

    * *Ação:* Editar/Detalhar

  * 2.3. Calendário de Atividades

* **3. Orçamento (Integração SIGOF)**

  * 3.1. Monitorização (Previsto vs. Realizado)

  * 3.2. Pedidos de Cabimento

* **4. Avaliação (Compliance)**

  * 4.1. Relatório QUAR (Automático)

  * 4.2. Minha Avaliação (SIADAP)

* **5. Configurações (Admin)**

  * 5.1. Gestão de Utilizadores

  * 5.2. Parâmetros Globais (Drivers de Custo)

***

## 4. Fluxograma de Processo (Lógica de Telas)

### Fluxo: Criação de Atividade no PAA (RF004 + RN01)

1. **Início:** Usuário clica em "Nova Atividade" no menu PAA.
2. **Tela 1: Vínculo Estratégico**

   * Sistema exibe lista de Objetivos Estratégicos disponíveis para a Unidade do usuário.

   * *Decisão:* Usuário seleciona um Objetivo?

     * **Sim:** Habilita botão "Próximo".

     * **Não:** Botão "Próximo" desabilitado. Mensagem: "Selecione um objetivo pai".
3. **Tela 2: Detalhe 5W2H**

   * Usuário preenche O Quê, Porquê, Quem, Quando, Onde.
4. **Tela 3: Orçamento (Driver-Based)**

   * Usuário seleciona tipo de despesa (ex: Deslocação).

   * Sistema exibe campos específicos (Destino, Dias).

   * Sistema calcula Total Estimado.

   * *Validação RN01:* O sistema verifica se há teto orçamental na rubrica macro.

     * **Sucesso:** Mostra "Dentro do Orçamento".

     * **Falha:** Mostra "Excede teto em X$00". Permite salvar como "Rascunho" mas bloqueia "Submeter".
5. **Fim:** Usuário clica em "Salvar/Submeter". Sistema exibe *Toast* de sucesso e redireciona para a Lista do PAA.

***

## 5. Especificação de Componentes (Design System)

Baseado em princípios de *Clean Design* e *Government Design Systems*.

* **Cores Semânticas:**

  * `Primary Blue (#0056D2)`: Ações principais, Links, Cabeçalhos ativos.

  * `Success Green (#28A745)`: Metas atingidas, Orçamento com saldo, Aprovações.

  * `Warning Yellow (#FFC107)`: Orçamento próximo do limite (90%), Prazos a vencer.

  * `Error Red (#DC3545)`: Bloqueio SIGOF, Erro de validação, Meta crítica não atingida.

* **Tipografia:**

  * Fonte: **Inter** ou **Roboto** (Alta legibilidade em telas).

  * Títulos: Bold, Hierarquia clara (H1, H2, H3).

  * Dados Tabulares: Fonte monoespaçada para valores monetários (ex: `1.250.000$00`) para facilitar comparação vertical.

* **Componentes Críticos:**

  * **Cards de KPI:** Retângulos com sombra suave, contendo: Título do KPI, Valor Atual, Sparkline (minigráfico de tendência) e Badge de Status.

  * **Tabelas de Dados (Data Grids):** Devem suportar ordenação, filtros rápidos e "Sticky Header" (cabeçalho fixo). Linhas zebradas para facilitar leitura.

  * **Modais (Dialogs):** Usados para cadastros complexos (Wizard passo-a-passo) para não perder o contexto da tela de fundo.

***

## 6. Critérios de Usabilidade (NFRs de UX)

1. **Acessibilidade (WCAG 2.1 AA):**

   * Contraste de cor mínimo de 4.5:1 para textos.

   * Navegação completa via teclado (Tab Index correto).

   * Suporte a leitores de tela (ARIA labels em ícones e botões).

2. **Feedback do Sistema:**

   * **Regra dos 2 segundos:** Qualquer ação que demore mais de 2s (ex: consulta ao SIGOF) deve exibir um *Spinner* ou *Skeleton Screen* de carregamento.

   * **Confirmação Destrutiva:** Ações de "Excluir" ou "Rejeitar" exigem confirmação explícita em modal.

3. **Prevenção de Erros:**

   * Campos monetários formatam-se automaticamente enquanto o usuário digita (máscara de input).

   * Datas inválidas (ex: Fim antes do Início) são bloqueadas no Datepicker.

4. **Curva de Aprendizado:**

   * **Onboarding:** No primeiro acesso, um "Tour Guiado" de 3 passos destaca as funções principais.

   * **Empty States:** Telas vazias (ex: PAA sem atividades) não devem mostrar apenas "Sem dados", mas sim um desenho amigável e um botão "Criar sua primeira atividade".

