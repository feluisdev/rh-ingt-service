# Documento de Visão e Business Case: SIGDI
**Versão:** 1.0 | **Status:** Estudo de Produto

## 1. Declaração de Visão
> **Para** as Instituições da Administração Pública e seus gestores,
> **que** lutam contra a fragmentação de dados, o "gap" entre a estratégia e a execução orçamental, e a burocracia na prestação de contas,
> **o** **SIGDI** (Sistema Integrado de Gestão de Desempenho Institucional)
> **é uma** Plataforma de Gestão de Desempenho Empresarial (EPM) Inteligente
> **que** unifica o Planeamento Estratégico, o Plano de Atividades Anual (PAA) e a Execução Orçamental (SIGOF) numa "única fonte da verdade", garantindo conformidade legal automática (SIADAP/QUAR) e agilidade na tomada de decisão.

---

## 2. Objetivos de Negócio (KPIs)
Para validar o sucesso do SIGDI, perseguiremos métricas que comprovam eficiência operacional e alinhamento estratégico:

*   **Eficiência Operacional:** Reduzir em **80%** o tempo gasto na consolidação de relatórios de prestação de contas e no preenchimento manual do QUAR.
*   **Agilidade no Planeamento:** Reduzir em **30%** a duração dos ciclos de planeamento orçamental e revisão do PAA.
*   **Alinhamento Orçamental:** Garantir que **100%** das despesas executadas no SIGOF tenham um vínculo explícito com uma atividade ou objetivo estratégico no SIGDI.
*   **Assertividade Financeira:** Reduzir a variância entre o Orçamento Previsto vs. Realizado através de alertas preventivos de dotação.

---

## 3. Análise de Diferenciais (O que nos torna únicos?)
O SIGDI não é apenas mais um software de gestão de tarefas; ele é o "sistema nervoso" da instituição.

1.  **Integração Nativa com o Ecossistema do Estado (SIGOF/PDEX):**
    *   Ao contrário de ferramentas de mercado genéricas (Trello, Asana), o SIGDI "fala a língua" da Administração Pública de Cabo Verde. A sincronização bidirecional com o SIGOF permite que o planeamento físico (atividades) controle a execução financeira (cabimentos), algo inexistente nas soluções atuais.

2.  **Conformidade Legal "By Design" (SIADAP/QUAR):**
    *   O sistema já nasce preparado para as legislações de 2024/2026. A geração automática do Quadro de Avaliação e Responsabilização (QUAR) e o suporte às regras de pontuação do SIADAP eliminam o retrabalho burocrático no final do ano.

3.  **Inteligência Artificial (Sage IA):**
    *   Incorporação de IA para análise preditiva ("What-If"), permitindo simular cenários de cortes orçamentais em segundos e sugerir planos de correção (FCA) antes que os problemas se materializem.

4.  **Planeamento "Driver-Based":**
    *   Orçamentação baseada em direcionadores reais (ex: custo de diárias, tabelas salariais BDRH), garantindo que os números financeiros reflitam a realidade física da operação.

---

## 4. Análise SWOT

| **Forças (Strengths)** | **Fraquezas (Weaknesses)** |
| :--- | :--- |
| - Integração profunda com SIGOF e Autentika (SSO).<br>- Alinhamento total com a legislação (SIADAP, PAA, QUAR).<br>- Arquitetura moderna (Microsserviços, API-First).<br>- Foco na experiência do utilizador (UX) para reduzir a curva de aprendizagem. | - Dependência da estabilidade das APIs de sistemas legados (SIGOF/PDEX).<br>- Necessidade de mudança cultural profunda nas instituições (sair do papel/Excel).<br>- Complexidade inicial de parametrização para instituições com baixa maturidade de gestão. |
| **Oportunidades (Opportunities)** | **Ameaças (Threats)** |
| - Agenda de Transformação Digital do Governo e modernização administrativa.<br>- Demanda crescente por transparência e *accountability*.<br>- Possibilidade de expansão para outras entidades do setor público e empresarial do estado.<br>- Evolução da IA para automação de tarefas repetitivas. | - Resistência à mudança por parte de funcionários habituados a processos manuais.<br>- Instabilidade na infraestrutura de conectividade em algumas ilhas/setores.<br>- Mudanças legislativas abruptas que exijam refatoração das regras de negócio. |

---

## 5. Riscos Estratégicos
Fatores críticos que podem impedir o sucesso e respectivas mitigações:

1.  **Risco de Adoção (Resistência Cultural):**
    *   *Impacto:* Alto. O sistema expõe ineficiências e exige disciplina.
    *   *Mitigação:* Plano de Gestão de Mudança robusto, com formação focada em benefícios pessoais (menos burocracia) e gamificação do uso.

2.  **Risco de Integração (Dependência Tecnológica):**
    *   *Impacto:* Crítico. Se o SIGOF mudar ou falhar, o módulo orçamental para.
    *   *Mitigação:* Arquitetura de "Circuit Breaker" e *cache* local para permitir operação offline/assíncrona; contratos de nível de serviço (SLA) claros com a NOSi/DGT.

3.  **Risco de Qualidade de Dados (Garbage In, Garbage Out):**
    *   *Impacto:* Médio. Dados históricos ruins podem enviesar a IA.
    *   *Mitigação:* Etapa inicial de saneamento de dados e validações rígidas na entrada de novas informações (travas de sistema).
