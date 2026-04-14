# Documento de Especificação de Requisitos de Software (SRS) — SIGDI
**Projeto:** Sistema Integrado de Gestão de Desempenho Institucional
**Versão:** 2.0 | **Status:** Aprovado para Desenvolvimento | **Data:** 30/03/2026

---

## 1. Introdução e Âmbito

Este documento detalha os requisitos funcionais, não-funcionais e regras de negócio do SIGDI, servindo como guia único para as equipas de Desenvolvimento e Quality Assurance (QA). Cobre o âmbito completo das três fases do produto, com indicação clara de qual fase cada requisito pertence.

### 1.1. Convenções de Prioridade
- **Crítica (Fase 1):** Bloqueante para o MVP. Sem isto, o piloto não funciona.
- **Alta (Fase 1):** Necessária para o piloto mas não bloqueante para o primeiro sprint.
- **Alta (Fase 2):** Necessária para a expansão.
- **Média (Fase 2/3):** Melhora a proposta de valor mas não é obrigatória no piloto.

---

## 2. Requisitos Funcionais (RF)

### Módulo 1: Estratégico

| ID | Nome da Funcionalidade | Descrição | Fase | Prioridade |
| :--- | :--- | :--- | :--- | :--- |
| **RF001** | Gestão de Identidade Institucional | Registo e versionamento da Missão, Visão e Valores com Snapshots imutáveis. Histórico de versões consultável. | 2 | Baixa |
| **RF002** | Mapa Estratégico (BSC) | Criação visual de mapas estratégicos nas 4 perspetivas BSC com relações de causa-efeito. Exportação em PNG/PDF. | 2 | Alta |
| **RF003** | Gestão de OKRs | Definição de Objetivos e Resultados-Chave com check-ins periódicos, evidências e cálculo automático de progresso ponderado. | 2 | Alta |
| **RF012** | Indicadores Partilhados entre Unidades | Um KPI pode ser partilhado entre múltiplas Unidades Orgânicas, com contribuição proporcional de cada unidade para o progresso global. | 2 | Média |

### Módulo 2: Tático e Operacional

| ID | Nome da Funcionalidade | Descrição | Fase | Prioridade |
| :--- | :--- | :--- | :--- | :--- |
| **RF004** | Elaboração do PAA (5W2H) | Formulário estruturado 5W2H para criação de atividades, com vinculação obrigatória a Objetivo Estratégico ou Tático. | 1 | Crítica |
| **RF011** | Workflow de Aprovação Multinível do PAA | Submissão de atividades para aprovação com roteamento configurável (técnico → gestor → diretor). Prazos de resposta, escalada automática, notificações e registo de decisões com justificativa obrigatória. | 1 | Crítica |
| **RF013** | Gestão de Delegações Temporárias | Um aprovador pode delegar as suas funções de aprovação a outro utilizador por um período definido (ex: férias). O sistema regista a delegação e aplica-a automaticamente no workflow. | 1 | Alta |

### Módulo 3: Integração e Orçamento

| ID | Nome da Funcionalidade | Descrição | Fase | Prioridade |
| :--- | :--- | :--- | :--- | :--- |
| **RF005** | Sincronização Outbound (SIGOF) | Compilação das necessidades financeiras do PAA e exportação de payload compatível com a API do SIGOF via PDEX para a Proposta de Orçamento do Estado. | 1 | Crítica |
| **RF006** | Sincronização Inbound (SIGOF) | Consumo diário da API SIGOF para importar Cabimentos, Liquidações e Pagamentos, associados às atividades via Classificador Económico. Com tolerância a falhas (retry + Circuit Breaker). | 1 | Crítica |
| **RF007** | Monitorização Driver-Based | Cálculo automático de custos com base em drivers configuráveis. Recálculo automático ao alterar drivers. | 1 | Alta |
| **RF014** | Change Request de PAA Aprovado | Após aprovação formal do PAA, qualquer alteração a meta, prazo ou orçamento de uma atividade requer um fluxo de Pedido de Alteração com justificativa obrigatória, aprovação do gestor e registo em auditoria. | 1 | Alta |

### Módulo 4: Inteligência e Compliance

| ID | Nome da Funcionalidade | Descrição | Fase | Prioridade |
| :--- | :--- | :--- | :--- | :--- |
| **RF008** | Motor de Simulação "Sage" (Fase 2) | Simulação de cenários de corte/reafectação orçamental com algoritmo determinístico de priorização. Relatório de impacto com sugestões de ação (adiar, cancelar, reduzir escopo). | 2 | Média |
| **RF015** | Motor Preditivo "Sage" (Fase 3) | Com dados históricos de ≥ 2 ciclos orçamentais, previsão de desvio de execução baseada em modelo de ML. Pipeline de treino e atualização do modelo definido no documento de infraestrutura. | 3 | Baixa |
| **RF009** | Geração Automática do QUAR | Geração do relatório QUAR em PDF e Excel com preenchimento automático de valores realizados, desvios percentuais e semáforos de desempenho. Formatação conforme modelo oficial DGPOG. | 1 | Alta |
| **RF010** | Avaliação SIADAP | Cálculo da pontuação individual com quotas de mérito por Unidade Orgânica (incluindo arredondamento para unidades com < 4 colaboradores). Parâmetros configuráveis por ano via tabela `t_siadap_config`. | 2 | Alta |

### Módulo 5: Administração e Configuração

| ID | Nome da Funcionalidade | Descrição | Fase | Prioridade |
| :--- | :--- | :--- | :--- | :--- |
| **RF016** | Gestão Multi-Instituição | O sistema suporta múltiplas instituições com isolamento total de dados (Row-Level Security). Admin da NOSi pode criar/desativar instituições. Admin da instituição gere apenas os seus utilizadores e parâmetros. | 1 | Crítica |
| **RF017** | Configuração de Parâmetros SIADAP | Interface para configurar por ano fiscal: quotas de mérito, pontuações por menção, regras de arredondamento para unidades pequenas. Mudança de parâmetros aplica-se apenas ao ciclo seguinte (não retroativo). | 2 | Alta |
| **RF018** | Configuração de Drivers de Custo | Interface para atualizar tabelas de diárias, combustível e outros drivers. Cada driver tem data de vigência. O sistema aplica o driver correto por data, mantendo o histórico. | 1 | Alta |

---

## 3. Requisitos Não-Funcionais (RNF)

| ID | Categoria | Requisito Técnico | Critério de Aceitação Mensurável |
| :--- | :--- | :--- | :--- |
| **RNF001** | Segurança | Autenticação exclusivamente via Autentika (SSO) com OAuth2/OpenID Connect. | Login bem-sucedido com credenciais Gov.cv. Tentativa de acesso sem token válido retorna HTTP 401 em 100% dos casos. |
| **RNF002** | Segurança | RBAC com perfis mínimos: Admin NOSi, Admin Instituição, Gestor Estratégico, Gestor Tático, Técnico, Auditor. Perfis adicionais configuráveis por instituição. | Utilizador "Técnico" não acede a menus de "Configuração Global". Teste de escalada de privilégio automatizado no pipeline CI/CD. |
| **RNF003** | Segurança | Row-Level Security (RLS) no PostgreSQL: todas as queries filtram automaticamente por `institution_id` do token JWT. | Testes de penetração cross-tenant: dados da Instituição A nunca acessíveis por token da Instituição B. |
| **RNF004** | Interoperabilidade | Arquitetura API-First. Todas as funcionalidades expostas como endpoints RESTful documentados em OpenAPI 3.0 (Swagger). | Cobertura de 100% das ações de negócio via API. Swagger UI disponível em `/api/docs`. |
| **RNF005** | Desempenho | Dashboards e Mapas Estratégicos carregam em ≤ 2 segundos no percentil 95, sob carga de 200 utilizadores simultâneos com datasets de 500 atividades e 100 objetivos. | Teste de carga K6: cenário de 200 VUs, 10 minutos de ramp-up. p95 < 2000ms. |
| **RNF006** | Desempenho | Geração de relatório QUAR (PDF) concluída em ≤ 30 segundos para instituições com até 200 colaboradores. | Teste automatizado com dataset sintético de 200 colaboradores. |
| **RNF007** | Disponibilidade | Modo de leitura offline: em caso de falha do link SIGOF/PDEX (Circuit Breaker aberto), o sistema permite navegação e consulta de dados em cache. Escrita de novas atividades permitida (sincronização assíncrona quando a ligação for restaurada). | Simulação de falha do SIGOF: utilizador consegue navegar e criar rascunhos sem mensagem de erro. |
| **RNF008** | Auditabilidade | Todas as operações de escrita (Create, Update, Delete, State Change) geram registos de Log de Auditoria imutáveis com: user_id, ip_address, timestamp, entity_type, entity_id, operation, old_value (JSON), new_value (JSON), config_version. | Logs consultáveis por perfil Auditor. Tentativa de alteração de log existente retorna erro e gera alerta. |
| **RNF009** | Conformidade | Todos os campos de data respeitam o calendário orçamental de Cabo Verde (ano económico de 1 Janeiro a 31 Dezembro). | Validação automática em todos os date pickers. |
| **RNF010** | Manutenibilidade | Cobertura mínima de testes unitários de 80% por serviço. Testes de integração de API cobrindo todos os endpoints documentados. | Pipeline CI/CD falha se cobertura < 80%. Relatório de cobertura gerado por build. |
| **RNF011** | Operação Offline | O sistema deve ser utilizável com conectividade intermitente (ilhas com link instável). Dados críticos de leitura devem estar disponíveis em cache local por até 24 horas. | Teste de utilizador com simulação de 60 minutos offline: todas as funcionalidades de leitura operacionais. |

---

## 4. Regras de Negócio (RN)

### 4.1. Regras de Planeamento e Vinculação

**RN01 — Princípio da Vinculação Orçamental:**
Nenhuma dotação orçamental pode ser criada no SIGDI sem associação a uma Atividade do PAA, e nenhuma Atividade pode existir sem vínculo a um Objetivo Estratégico ou Tático. O sistema bloqueia tecnicamente qualquer tentativa de violar esta hierarquia.

**RN02 — Hierarquia de Progresso:**
O progresso (%) de um Objetivo Pai é a média ponderada do progresso dos seus Objetivos Filhos, com pesos configuráveis. Por defeito, todos os filhos têm peso 1.0. A fórmula é:
```
Progresso_Pai = SUM(Progresso_Filho_i × Peso_Filho_i) / SUM(Peso_Filho_i)
```
Objetivos com `status = CANCELLED` ou `status = DELEGATED` são excluídos do cálculo.

**RN05 — Imutabilidade Pós-Aprovação:**
Após aprovação formal do PAA pelo Conselho Diretivo (estado `APPROVED`), qualquer alteração a meta, orçamento, prazo ou responsável de uma atividade requer um Change Request (RF014) com justificativa obrigatória, aprovação do Gestor responsável e registo completo em auditoria. O sistema não permite edição direta de campos vinculados em atividades `APPROVED`.

**RN06 — Validação de Período de Atividade:**
A data de início de uma atividade deve ser ≥ à data de início do ciclo PAA vigente. A data de fim deve ser ≤ à data de fim do ciclo PAA vigente. Atividades plurianuais requerem um campo de justificação adicional e aprovação de nível superior.

**RN07 — Indicadores Partilhados:**
Quando um KPI é partilhado entre múltiplas Unidades Orgânicas (RF012), cada unidade define a sua meta parcial. O progresso global é calculado como a soma dos valores realizados de todas as unidades dividida pela soma das metas parciais. A gestão do KPI partilhado é responsabilidade de uma Unidade Coordenadora designada.

### 4.2. Regras de Orçamento e Execução

**RN03 — Alertas e Bloqueios de Cabimento:**
- Se o valor "Liquidado" no SIGOF atingir **70%** do valor "Cabimentado": alerta visual amarelo no dashboard da atividade e notificação ao responsável.
- Se o valor "Liquidado" atingir **90%**: alerta vermelho + notificação ao Gestor Tático.
- Se o valor "Liquidado" atingir **100%**: bloqueio de novos pedidos de despesa (Soft Lock) + notificação ao Gestor Estratégico. O Soft Lock pode ser ultrapassado com autorização explícita do Gestor Estratégico, com justificativa obrigatória registada em auditoria.

**RN08 — Vigência dos Drivers de Custo:**
O sistema aplica o driver com a `valid_from` mais recente que seja ≤ à data de criação da atividade. Drivers não têm `valid_until` explícito — são substituídos pelo driver seguinte. Atividades já criadas não têm o seu orçamento recalculado automaticamente quando um driver muda; o gestor recebe uma notificação de "Driver atualizado — reveja o orçamento estimado".

### 4.3. Regras SIADAP

**RN04 — Regras SIADAP (Parametrizáveis por Ano):**
As seguintes regras são lidas da tabela `t_siadap_config` para o `fiscal_year` em processamento e não podem ser alteradas retroativamente:

| Parâmetro | Valor Padrão (Portaria 2026) | Configurável? |
| :--- | :--- | :--- |
| Pontuação "Bom" | 1.5 pontos | Sim |
| Pontuação "Excelente" | 3.0 pontos | Sim |
| Quota máxima "Excelente" por Unidade Orgânica | 25% | Sim |
| Quota máxima "Bom" por Unidade Orgânica | 35% (quando aplicável) | Sim |
| Mínimo de colaboradores para aplicar quota de "Excelente" | 4 | Sim |
| Regra de arredondamento (unidades < 4 colaboradores) | 1 colaborador pode receber "Excelente" mesmo que represente > 25% | Sim |

**RN09 — Cálculo de Pontuação SIADAP:**
A pontuação individual é calculada como:
```
Pontuação = (% cumprimento OKRs × peso_resultados) + (avaliação_competências × peso_competências)
```
Os pesos `peso_resultados` e `peso_competências` são parametrizáveis (padrão: 60% / 40%). O sistema valida as quotas de mérito antes de fechar a avaliação, impedindo que a Unidade Orgânica exceda os limites definidos em `siadap_config`.

### 4.4. Regras de Workflow de Aprovação

**RN10 — Workflow de Aprovação:**
O workflow de aprovação do PAA segue o seguinte fluxo padrão (configurável por instituição):
1. **Técnico** cria atividade → estado `DRAFT`.
2. **Técnico** submete → estado `PENDING_TACTICAL`.
3. **Gestor Tático** aprova → estado `PENDING_STRATEGIC`.
4. **Gestor Estratégico** aprova → estado `APPROVED`.
5. Em qualquer etapa, rejeição devolve ao estado anterior com comentário obrigatório.

**RN11 — Escalada Automática:**
Se um aprovador não responder dentro do prazo configurado (padrão: 3 dias úteis), o sistema envia uma notificação de lembrete. Se não houver resposta em mais 2 dias úteis, escalada automática para o nível superior com notificação ao responsável original.

**RN12 — Delegações Temporárias:**
Uma delegação define: delegante, delegado, data início, data fim, e âmbito (todas as aprovações, ou apenas um tipo específico). Durante o período de delegação, o delegado tem as mesmas capacidades de aprovação que o delegante, para o âmbito definido. O sistema regista ambos (delegante e delegado) no log de auditoria de cada decisão tomada em delegação.

---

## 5. Requisitos de Integração (RI)

### RI01 — Contrato SIGOF/PDEX (Obrigatório antes do Sprint 1 do Service-Budget-Adapter)
A integração com o SIGOF é sujeita a um contrato formal de schema acordado com a NOSi, documentado em OpenAPI, e testado com Consumer-Driven Contract Testing (Pact). O schema contratado inclui:

**Inbound (SIGOF → SIGDI) — Payload de Execução Financeira:**
```json
{
  "institution_code": "string",
  "fiscal_year": "integer",
  "economic_classifier": "string (format: XX.XX.XX)",
  "organic_unit_code": "string",
  "budget_allocated": "decimal(19,4)",
  "committed": "decimal(19,4)",
  "liquidated": "decimal(19,4)",
  "paid": "decimal(19,4)",
  "reference_date": "date (ISO 8601)"
}
```

**Outbound (SIGDI → SIGOF) — Proposta Orçamental:**
```json
{
  "institution_code": "string",
  "fiscal_year": "integer",
  "activities": [
    {
      "activity_id": "uuid",
      "title": "string",
      "economic_classifier": "string",
      "organic_unit_code": "string",
      "estimated_amount": "decimal(19,4)",
      "justification": "string"
    }
  ]
}
```

**Nota:** Os campos exatos devem ser validados contra o schema real do SIGOF antes do início do desenvolvimento. O payload acima é um contrato proposto sujeito a negociação com a NOSi.

### RI02 — Autentika (SSO)
- Protocolo: OAuth2 Authorization Code Flow + PKCE.
- Claims obrigatórias no JWT: `sub` (user_id), `institution_id`, `roles` (array), `name`, `email`.
- O SIGDI não armazena passwords. A gestão de utilizadores (criar, desativar) é feita no Autentika pelo Admin da Instituição.

### RI03 — BDRH (Consulta de Dados de RH)
- Integração read-only para consulta de estrutura orgânica, lista de colaboradores e dados salariais agregados (para drivers de custo).
- Frequência: sincronização diária às 02:00 AM.
- Dados sensíveis (salários individuais) nunca são armazenados no SIGDI — usados apenas para cálculo de custo em memória.

---

## 6. Matriz de Rastreabilidade Completa

| Requisito (RF) | Regra de Negócio | US Backlog | KPI Impactado | Fase |
| :--- | :--- | :--- | :--- | :--- |
| RF004 (PAA 5W2H) | RN01, RN06 | US003 | Alinhamento Orçamental 100% | 1 |
| RF011 (Workflow Aprovação) | RN10, RN11, RN12 | US009 (novo) | Agilidade no Planeamento | 1 |
| RF005 / RF006 (SIGOF) | RN03, RN08 | US005 | Assertividade Financeira | 1 |
| RF009 (QUAR) | RN04, RN09 | US008 | Eficiência Operacional 80% | 1 |
| RF014 (Change Request) | RN05 | US010 (novo) | Auditabilidade | 1 |
| RF016 (Multi-Instituição) | — | US011 (novo) | Segurança / Escalabilidade | 1 |
| RF002 (Mapa BSC) | RN02 | US002 | Alinhamento Estratégico | 2 |
| RF003 (OKRs) | RN02, RN07 | US004 | Alinhamento Estratégico | 2 |
| RF010 (SIADAP) | RN04, RN09 | US008 | Eficiência Operacional | 2 |
| RF008 (IA Sage) | — | US007 | Agilidade no Planeamento | 2 |
| RF012 (KPIs Partilhados) | RN07 | US012 (novo) | Alinhamento Estratégico | 2 |
