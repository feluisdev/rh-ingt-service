# Documento de Visão e Business Case: SIGDI
**Versão:** 2.0 | **Status:** Aprovado para Desenvolvimento | **Data:** 30/03/2026

---

## 1. Declaração de Visão

> **Para** as Instituições da Administração Pública de Cabo Verde e seus gestores,
> **que** lutam contra a fragmentação de dados, o "gap" entre a estratégia e a execução orçamental, e a burocracia na prestação de contas,
> **o SIGDI** (Sistema Integrado de Gestão de Desempenho Institucional)
> **é uma** Plataforma de Gestão de Desempenho Empresarial (EPM) Inteligente
> **que** unifica o Planeamento Estratégico, o Plano de Atividades Anual (PAA) e a Execução Orçamental (SIGOF) numa única fonte da verdade, garantindo conformidade legal automática (SIADAP/QUAR) e agilidade na tomada de decisão.
>
> **Ao contrário** de folhas de cálculo, ferramentas genéricas de gestão (Trello, Asana) ou processos manuais,
> **o SIGDI** foi desenhado especificamente para o ecossistema regulatório e tecnológico da Administração Pública de Cabo Verde, integrando-se nativamente com o SIGOF via barramento PDEX e com o sistema de identidade Autentika.

---

## 2. Objetivos de Negócio (KPIs de Sucesso)

Para validar o sucesso do SIGDI, os seguintes indicadores serão monitorizados ao longo dos primeiros 18 meses de operação. Cada KPI tem uma baseline definida, uma meta e um método de medição.

| KPI | Baseline Estimada | Meta (18 meses) | Método de Medição |
| :--- | :--- | :--- | :--- |
| **Tempo de consolidação do QUAR** | ~40 horas/instituição/ano (manual) | ≤ 8 horas (redução de 80%) | Registo de tempo na plataforma vs. auditoria manual anterior |
| **Duração do ciclo de planeamento do PAA** | ~6 semanas (referência DGPOGs) | ≤ 4 semanas (redução de 33%) | Data de início vs. data de aprovação final no sistema |
| **Cobertura de vinculação orçamental** | ~40% das despesas vinculadas (estimativa) | 100% das despesas com vínculo a atividade PAA | Relatório automático de auditoria de vinculação |
| **Variância Orçamento Previsto vs. Realizado** | Desvio médio >20% (histórico SIGOF) | Desvio médio <10% | Comparação automática mensal via integração SIGOF |
| **Taxa de adoção ativa** | 0% (sistema novo) | ≥ 80% de utilizadores com login nos últimos 30 dias | Métricas de sessão da plataforma |

---

## 3. Âmbito e Faseamento do Produto

O SIGDI será desenvolvido e lançado em três fases para gerir o risco de adoção e permitir validação incremental com utilizadores reais.

### Fase 1 — MVP de Planeamento (Meses 1–6)
Foco exclusivo no ciclo PAA → Aprovação → Monitorização básica. Inclui:
- Elaboração de atividades 5W2H com vinculação estratégica.
- Aprovação multinível do PAA.
- Sincronização inbound com SIGOF (leitura de execução financeira).
- Dashboard de monitorização Previsto vs. Realizado.
- Geração do QUAR em PDF.

**Instituição Piloto:** [A definir pela DGPOG — critério: instituição de médio porte, ~50 funcionários, com histórico de QUAR preenchido manualmente nos últimos 2 anos.]

**Critério de sucesso da Fase 1:** Pelo menos uma instituição piloto gera o QUAR 2026 integralmente via SIGDI, com validação da DGPOG.

### Fase 2 — Planeamento Estratégico e IA (Meses 7–12)
- Mapa Estratégico BSC visual.
- Gestão de OKRs com check-ins.
- Motor de simulação de cenários "Sage" (versão determinística de priorização).
- Calculadora Driver-Based completa.
- Avaliação SIADAP.

### Fase 3 — Expansão e Inteligência Preditiva (Meses 13–24)
- Módulo de BI avançado com análise de tendências.
- Extensão a outras entidades do setor empresarial do estado.
- Motor de previsão baseado em Machine Learning (com dados históricos acumulados na Fase 1 e 2).
- Portal de Transparência (cidadãos).

---

## 4. Análise de Diferenciais

O SIGDI não compete com ferramentas de mercado genéricas — o seu diferencial é a especificidade ao ecossistema de Cabo Verde.

### 4.1. Integração Nativa com o Ecossistema do Estado (SIGOF/PDEX)
A sincronização bidirecional com o SIGOF permite que o planeamento físico (atividades PAA) controle e monitorize a execução financeira (cabimentos, liquidações, pagamentos) em tempo quase real. Esta integração é implementada através do barramento PDEX gerido pela NOSi, com um contrato de schema formal acordado antes do início do desenvolvimento.

### 4.2. Conformidade Legal "By Design" (SIADAP/QUAR)
O sistema foi desenhado com as legislações SIADAP de 2024 e as portarias de 2026 incorporadas nas regras de negócio — incluindo as quotas de mérito, regras de arredondamento para unidades pequenas e os formulários oficiais do QUAR. A conformidade não é uma funcionalidade adicional, é uma restrição de design.

### 4.3. Motor de Análise "Sage"
Na Fase 2, o Sage opera como motor de simulação determinística: dado um cenário de corte orçamental, aplica regras de priorização (peso estratégico, obrigatoriedade legal, nível de execução) para identificar atividades a adiar, reduzir ou cancelar — sem IA generativa, com total auditabilidade. Na Fase 3, com dados históricos suficientes (mínimo de 2 ciclos orçamentais), evolui para modelos preditivos.

### 4.4. Orçamentação Driver-Based
Os custos são calculados a partir de parâmetros oficiais atualizáveis (tabelas de diárias por destino/nível, tabelas de combustível, tabelas salariais do BDRH), garantindo que os valores financeiros reflitam a realidade operacional e não estimativas arbitrárias.

---

## 5. Análise SWOT (Atualizada)

| **Forças** | **Fraquezas** |
| :--- | :--- |
| Integração profunda com SIGOF, Autentika e BDRH. | Dependência da estabilidade e disponibilidade das APIs de sistemas legados (SIGOF/PDEX). |
| Alinhamento total com a legislação vigente (SIADAP, PAA, QUAR). | Necessidade de mudança cultural profunda nas instituições. |
| Arquitetura moderna (Microsserviços, API-First, Event-Driven). | Ausência de dados históricos estruturados para alimentar a IA na Fase 3. |
| Foco em UX para utilizadores não-técnicos da administração pública. | Complexidade de parametrização inicial para instituições com baixa maturidade de gestão. |
| Desenvolvido sobre o framework IGRP — ecossistema familiar à NOSi. | Equipa de suporte e manutenção ainda a definir após o go-live. |
| **Oportunidades** | **Ameaças** |
| Agenda de Transformação Digital do Governo de Cabo Verde. | Resistência à mudança por parte de funcionários habituados a processos manuais e Excel. |
| Demanda crescente por transparência e accountability na gestão pública. | Instabilidade na infraestrutura de conectividade em algumas ilhas (operação offline obrigatória). |
| Possibilidade de expansão para empresas públicas e municípios. | Mudanças legislativas (portarias SIADAP) que exijam refatoração de regras de negócio. |
| Posicionamento como referência regional para países lusófonos da CEDEAO. | Atrasos na formalização do contrato de integração com a NOSi/SIGOF. |

---

## 6. Riscos Estratégicos e Plano de Mitigação

### Risco 1 — Adoção (Resistência Cultural)
- **Probabilidade:** Alta. **Impacto:** Alto.
- **Descrição:** O sistema expõe ineficiências, cria responsabilização explícita e exige disciplina de registo que não existia antes.
- **Mitigação:**
  - Plano de Gestão de Mudança com Champion por instituição (utilizador interno que lidera a adoção).
  - Formação focada em benefícios pessoais (menos burocracia no fim do ano, geração automática do QUAR).
  - Lançamento faseado: a instituição piloto gera entusiasmo antes da expansão.
  - KPI de adoção monitorizado mensalmente com alerta ao sponsor executivo se < 60%.

### Risco 2 — Integração SIGOF (Dependência Tecnológica)
- **Probabilidade:** Média. **Impacto:** Crítico.
- **Descrição:** Se o schema do SIGOF mudar sem aviso prévio, ou se o PDEX tiver downtime, o módulo orçamental para completamente.
- **Mitigação:**
  - Contrato formal de schema API assinado com a NOSi antes do Sprint 1 do Service-Budget-Adapter.
  - Arquitetura de Circuit Breaker com cache local (máximo 24h de dados em leitura offline).
  - SLA formal com a NOSi: disponibilidade mínima de 99% para o endpoint PDEX, com janela de notificação de mudanças de schema de 30 dias.
  - Testes de contrato automatizados (Consumer-Driven Contract Testing com Pact) executados em cada deploy.

### Risco 3 — Qualidade de Dados (Garbage In, Garbage Out)
- **Probabilidade:** Alta. **Impacto:** Médio.
- **Descrição:** As instituições não têm histórico de dados estruturados. O primeiro ciclo PAA pode ter dados inconsistentes que enviésam relatórios e análises.
- **Mitigação:**
  - Etapa de onboarding com validação assistida: o sistema guia o gestor na criação dos primeiros objetivos e atividades com templates pré-definidos por tipo de instituição.
  - Validações rígidas de entrada (travas de sistema) que impedem submissão de dados incompletos.
  - Relatório de completude de dados disponível para o Admin da instituição durante o período de configuração inicial.

### Risco 4 — Multi-tenancy e Isolamento de Dados
- **Probabilidade:** Média. **Impacto:** Crítico.
- **Descrição:** Com múltiplas instituições na mesma plataforma, um bug de autorização pode expor dados de uma instituição a outra — incidente de segurança grave no contexto governamental.
- **Mitigação:**
  - Decisão arquitetural tomada antes do Sprint 1: adoção de Row-Level Security (RLS) no PostgreSQL com `institution_id` em todas as tabelas de dados, complementado por validação na camada de aplicação (defense in depth).
  - Testes de penetração com foco em escalada de privilégios entre instituições, antes de cada go-live.
  - Ver detalhe técnico no documento 05 — Infraestrutura e Persistência.

### Risco 5 — Mudanças Legislativas
- **Probabilidade:** Baixa. **Impacto:** Alto.
- **Descrição:** Portarias anuais do SIADAP podem alterar quotas, ponderações ou formulários, exigindo refatoração urgente de regras de negócio.
- **Mitigação:**
  - Regras SIADAP parametrizáveis (tabela `t_siadap_config` com vigência por ano) — não hard-coded.
  - Processo de atualização de parâmetros com zero downtime (atualização via API de configuração pelo Admin).
  - Rastreabilidade: cada cálculo de pontuação regista o `config_version` utilizado, para auditoria futura.

---

## 7. Plano de Rollout Institucional

### 7.1. Critérios de Seleção da Instituição Piloto
A instituição piloto deve cumprir os seguintes critérios:
- Entre 30 e 100 funcionários (escala gerível para o piloto).
- QUAR dos últimos 2 anos preenchido manualmente (baseline para comparação).
- Sponsor executivo comprometido (Diretor Nacional ou equivalente).
- Equipa técnica local com capacidade de receber formação (mínimo 1 ponto focal).
- Conetividade estável à internet (para a Fase 1).

### 7.2. Timeline de Rollout
| Fase | Período | Instituições | Objetivo |
| :--- | :--- | :--- | :--- |
| Piloto Controlado | Mês 4–6 | 1 instituição | Validar fluxo completo PAA → QUAR |
| Expansão Limitada | Mês 7–9 | 3–5 instituições | Validar multi-tenancy e carga |
| Rollout Nacional Fase 1 | Mês 10–12 | Todos os ministérios | Cobertura do ciclo orçamental 2027 |
| Rollout Fase 2 | Mês 13–18 | Todos + empresas públicas | BSC, OKRs, IA |

### 7.3. Plano de Formação
- **Nível 1 — Administradores de Sistema** (1 por instituição): 16 horas (configuração de utilizadores, parâmetros, relatórios).
- **Nível 2 — Gestores Estratégicos/Táticos** (Anas): 8 horas (BSC, OKRs, aprovação de PAA, leitura de dashboards).
- **Nível 3 — Técnicos Administrativos** (Carlos): 4 horas (criação de atividades, check-ins, orçamentação).
- Materiais: vídeos de 5 minutos por funcionalidade crítica + manual PDF + helpdesk por email.

---

## 8. Estimativa de ROI

### 8.1. Custos Evitados (Benefícios Quantificáveis)
| Benefício | Cálculo | Valor Anual Estimado |
| :--- | :--- | :--- |
| Redução de horas em consolidação QUAR | 40h → 8h × 20 instituições × custo hora (2.500 CVE) | ~1.600.000 CVE |
| Redução de retrabalho por desvio orçamental | 20% variância → 10% × impacto financeiro médio | A quantificar com DGPOG |
| Eliminação de ferramentas de terceiros (Excel, Google Sheets, etc.) | Licenças + horas de manutenção de ficheiros | A quantificar por instituição |

### 8.2. Nota Metodológica
Os valores acima são estimativas iniciais a validar com a DGPOG durante a fase de onboarding do piloto. O ROI completo deve ser calculado após o primeiro ciclo operacional completo (12 meses), incluindo benefícios intangíveis como melhoria na qualidade de decisão e redução de erros de conformidade legal.
