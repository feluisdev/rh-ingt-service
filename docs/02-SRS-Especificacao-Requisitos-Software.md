# Documento de Especificação de Requisitos de Software (SRS) - SIGDI
**Projeto:** Sistema Integrado de Gestão de Desempenho Institucional  
**Versão:** 1.0  
**Status:** Aprovado para Desenvolvimento  
**Data:** 15/03/2026

---

## 1. Introdução
Este documento detalha os requisitos funcionais, não-funcionais e regras de negócio do **SIGDI**, servindo como guia único para as equipas de Desenvolvimento e Quality Assurance (QA). O sistema visa unificar o planeamento estratégico e a execução orçamental da Administração Pública.

---

## 2. Requisitos Funcionais (RF)

| ID | Nome da Funcionalidade | Descrição do Comportamento Esperado | Prioridade |
| :--- | :--- | :--- | :--- |
| **RF001** | **Gestão de Identidade Institucional** | O sistema deve permitir o registo e versionamento da Missão, Visão e Valores da instituição. Deve bloquear a edição de versões históricas já aprovadas (Snapshots). | Baixa |
| **RF002** | **Mapa Estratégico (BSC)** | O sistema deve permitir a criação visual de mapas estratégicos, categorizando objetivos nas 4 perspetivas do BSC (Financeira, Clientes, Processos, Aprendizagem) e estabelecendo relações de causa-efeito (links) entre eles. | Alta |
| **RF003** | **Gestão de OKRs** | O sistema deve permitir a definição de Objetivos (Qualitativos) e Resultados-Chave (Quantitativos). Deve suportar *check-ins* periódicos com evidências (upload de arquivos ou links). | Alta |
| **RF004** | **Elaboração do PAA (5W2H)** | O sistema deve disponibilizar um formulário estruturado no modelo **5W2H** para criação de atividades. Cada atividade deve, obrigatoriamente, estar vinculada a um Objetivo Estratégico ou Tático. | Crítica |
| **RF005** | **Sincronização Outbound (SIGOF)** | O sistema deve compilar as necessidades financeiras do PAA e exportar um arquivo/payload compatível com a API do **SIGOF** via barramento **PDEX** para a Proposta de Orçamento do Estado. | Crítica |
| **RF006** | **Sincronização Inbound (SIGOF)** | O sistema deve consumir diariamente a API do SIGOF para importar dados de **Cabimentos**, **Liquidações** e **Pagamentos**, associando-os às atividades do SIGDI através de chaves de correlação (ex: Classificador Económico). | Crítica |
| **RF007** | **Monitorização Driver-Based** | O sistema deve calcular custos automaticamente com base em drivers configuráveis (ex: Valor Diária x Dias x Pessoas). Alterações nos drivers devem recalcular o orçamento planeado de todas as atividades não fechadas. | Média |
| **RF008** | **Motor de IA "Sage" (What-If)** | O sistema deve permitir a simulação de cenários (ex: "Corte linear de 10%"). A IA deve identificar quais atividades se tornam inviáveis e sugerir ajustes no PAA. | Média |
| **RF009** | **Geração Automática do QUAR** | O sistema deve gerar o relatório **QUAR** em formato PDF/Excel, preenchendo automaticamente as colunas de "Realizado" e calculando os desvios percentuais baseados nos inputs do RF003 e RF006. | Alta |
| **RF010** | **Avaliação SIADAP** | O sistema deve calcular a pontuação individual dos colaboradores baseando-se no cumprimento dos OKRs e nas competências comportamentais, aplicando as quotas de diferenciação de mérito. | Alta |

---

## 3. Requisitos Não-Funcionais (RNF)

| ID | Categoria | Requisito Técnico | Critério de Aceitação |
| :--- | :--- | :--- | :--- |
| **RNF001** | **Segurança** | Autenticação deve ser realizada exclusivamente via **Autentika (Single Sign-On)** utilizando protocolo OAuth2/OpenID Connect. | Login bem-sucedido com credenciais Gov.cv. |
| **RNF002** | **Segurança** | Controle de Acesso Baseado em Função (**RBAC**). Perfis mínimos: Admin, Gestor Estratégico, Gestor Tático, Técnico, Auditor. | Usuário "Técnico" não pode ver menus de "Configuração Global". |
| **RNF003** | **Interoperabilidade** | Arquitetura **API-First**. Todas as funcionalidades devem expor endpoints RESTful documentados em **OpenAPI (Swagger)**. | Cobertura de 100% das ações de negócio via API. |
| **RNF004** | **Desempenho** | O tempo de carregamento de Dashboards e Mapas Estratégicos não deve exceder **2 segundos** (95º percentil) sob carga normal. | Teste de carga com JMeter/K6. |
| **RNF005** | **Disponibilidade** | O sistema deve operar em modo "Offline-First" ou possuir *caching* agressivo para consulta de dados em caso de falha no link com o SIGOF (Circuit Breaker). | Sistema permite navegação (leitura) sem conexão ao PDEX. |
| **RNF006** | **Auditabilidade** | Todas as operações de escrita (Create, Update, Delete) devem gerar registos de **Log de Auditoria** (Quem, Quando, Onde, O Que, Valor Antigo, Valor Novo). | Logs imutáveis e consultáveis por perfil Auditor. |

---

## 4. Regras de Negócio (RN)

*   **RN01 - Princípio da Vinculação Orçamental:** Nenhuma dotação orçamental pode ser criada no SIGDI sem estar associada a uma Atividade do PAA, e nenhuma Atividade pode existir sem vínculo a um Objetivo Estratégico.
*   **RN02 - Hierarquia de Agregação:** O progresso (%) de um Objetivo Pai é a média ponderada do progresso dos seus Objetivos Filhos (Key Results ou Atividades).
*   **RN03 - Bloqueio por Falta de Cabimento:** Se o valor "Liquidado" no SIGOF atingir 90% do valor "Cabimentado", o sistema deve emitir alertas visuais. Se atingir 100%, deve bloquear novos pedidos de despesa associados àquela atividade (Soft Lock).
*   **RN04 - Regras SIADAP 2026:**
    *   A menção "Bom" corresponde a 1,5 pontos.
    *   Apenas 25% dos colaboradores (ou conforme portaria anual) podem receber menção "Excelente". O sistema deve validar esta quota por Unidade Orgânica.
*   **RN05 - Imutabilidade Pós-Aprovação:** Após a aprovação formal do PAA pelo Conselho Diretivo, qualquer alteração de meta ou orçamento requer um fluxo de "Pedido de Alteração" (Change Request) com justificativa obrigatória.

---

## 5. Matriz de Rastreabilidade (Esboço)

Esta matriz conecta o que será desenvolvido (RF) com o valor de negócio esperado (KPIs do Business Case).

| Requisito Funcional (RF) | Objetivo de Negócio (KPI) Impactado | Justificativa |
| :--- | :--- | :--- |
| **RF004 (PAA 5W2H)** | **Alinhamento Orçamental (100%)** | Garante que toda ação operacional tenha "pai" estratégico. |
| **RF005 / RF006 (Integração SIGOF)** | **Assertividade Financeira** | Elimina a discrepância entre o planeado físico e o executado financeiro. |
| **RF009 (Autom. QUAR)** | **Eficiência Operacional (Redução 80%)** | Automatiza a tarefa mais manual e demorada da gestão pública. |
| **RF008 (IA What-If)** | **Agilidade no Planeamento** | Reduz o tempo de re-planejamento de semanas para minutos. |
| **RF002 (Mapa BSC)** | **Alinhamento Estratégico** | Visualização clara da estratégia para todos os níveis hierárquicos. |
