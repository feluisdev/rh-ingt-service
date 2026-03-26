# Documento de Infraestrutura, Persistência e Arquitetura Técnica (Nível 5) - SIGDI

**Versão:** 2.0 (Consolidada)
**Status:** Definição Arquitetural Completa
**Data:** 15/03/2026

***

## 1. Visão Geral da Arquitetura

O SIGDI adota uma arquitetura de **Microserviços Event-Driven** baseada em Spring Boot (IGRP.spring), operando sobre contentores Kubernetes na NOSi Cloud. A arquitetura foi desenhada para garantir o desacoplamento entre o Planeamento Estratégico (longo prazo) e a Execução Financeira (curto prazo), assegurando alta disponibilidade e conformidade legal.

### 1.1. Diagrama de Contexto (C4 - Nível 1)

* **Atores:** Gestores, Técnicos, Cidadãos (Portal Transparência).

* **Sistemas Externos:**

  * **Autentika:** Provedor de Identidade (IdP).

  * **SIGOF (via PDEX):** Sistema de Gestão Financeira (Core Bancário do Estado).

  * **BDRH:** Base de Dados de Recursos Humanos.

### 1.2. Componentes (Microserviços)

1. **Service-Strategy:** Gestão do BSC, Identidade Institucional e Matrizes SWOT.
2. **Service-Tactical:** Core do PAA, Gestão de OKRs e Workflows de Aprovação.
3. **Service-Budget-Adapter:** Gateway de integração com SIGOF/PDEX, com padrão *Circuit Breaker*.
4. **Service-Intelligence:** Motor de IA (Sage) para análise preditiva e cenários "What-If".
5. **Service-Notification:** Central de alertas e e-mails transacionais.

***

## 2. Infraestrutura de Nuvem (NOSi Cloud)

### 2.1. Cluster Kubernetes (K8s)

* **Environment:** Private Cloud (OpenShift/Rancher).

* **Node Pools:**

  * **General Purpose:** Para APIs Stateless (Strategy, Tactical). Config: 4 vCPU, 16GB RAM.

  * **High Memory:** Para Service-Intelligence (Processamento de dados em memória). Config: 8 vCPU, 32GB RAM.

* **Ingress Controller:** NGINX com terminação TLS e WAF (Web Application Firewall) ativado.

### 2.2. Especificações de Hardware/Recursos (Quotas por Pod)

| Serviço                | CPU Request/Limit | RAM Request/Limit | Réplicas (Min/Max)           |
| :--------------------- | :---------------- | :---------------- | :--------------------------- |
| `Service-Strategy`     | 250m / 500m       | 512Mi / 1Gi       | 2 / 5                        |
| `Service-Tactical`     | 500m / 1000m      | 1Gi / 2Gi         | 3 / 10                       |
| `Service-Budget`       | 250m / 500m       | 512Mi / 1Gi       | 2 / 4                        |
| `Service-Intelligence` | 1000m / 2000m     | 2Gi / 4Gi         | 1 / 3                        |
| `PostgreSQL (RDS)`     | 4 vCPU            | 16GB RAM          | Cluster HA (Primary-Replica) |
| `Redis`                | 2 vCPU            | 8GB RAM           | Cluster Mode                 |

***

## 3. Persistência de Dados (Dicionário de Dados)

O sistema utiliza o padrão **Database-per-Service** para garantir isolamento.

### 3.1. Esquema: `db_strategy` (Service-Strategy)

#### Tabela: `institutional_identity`

Armazena Missão, Visão e Valores com versionamento (Snapshot).

| Campo             | Tipo         | Constraints   | Descrição                        |
| :---------------- | :----------- | :------------ | :------------------------------- |
| `id`              | UUID         | PK            | Identificador da versão.         |
| `mission`         | TEXT         | NOT NULL      | Texto da Missão.                 |
| `vision`          | TEXT         | NOT NULL      | Texto da Visão.                  |
| `values`          | JSONB        | NOT NULL      | Array de strings com valores.    |
| `is_active`       | BOOLEAN      | DEFAULT FALSE | Apenas uma versão ativa por vez. |
| `version_comment` | VARCHAR(255) | -             | Justificativa da alteração.      |

#### Tabela: `strategic_goals` (BSC)

| Campo         | Tipo         | Constraints | Descrição                                      |
| :------------ | :----------- | :---------- | :--------------------------------------------- |
| `id`          | UUID         | PK          | ID do Objetivo.                                |
| `perspective` | VARCHAR(50)  | CHECK (...) | Financeira, Clientes, Processos, Aprendizagem. |
| `title`       | VARCHAR(255) | NOT NULL    | Nome do Objetivo.                              |

#### Tabela: `goal_relationships` (Links Causa-Efeito)

| Campo       | Tipo | Constraints | Descrição          |
| :---------- | :--- | :---------- | :----------------- |
| `source_id` | UUID | FK (goals)  | Objetivo "Causa".  |
| `target_id` | UUID | FK (goals)  | Objetivo "Efeito". |

***

### 3.2. Esquema: `db_tactical` (Service-Tactical)

#### Tabela: `tactical_activities` (PAA - Core)

*Ref: Backlog US003*

```sql
CREATE TABLE tactical_activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    strategic_goal_id UUID NOT NULL, -- FK lógica para db_strategy
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    budget_amount DECIMAL(19,4) NOT NULL DEFAULT 0,
    economic_classifier VARCHAR(20) NOT NULL, -- Ex: 02.02.01
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'REJECTED')),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    created_by VARCHAR(100) NOT NULL,
    deleted_at TIMESTAMP, -- Soft Delete
    CONSTRAINT chk_dates CHECK (end_date >= start_date)
);
CREATE INDEX idx_tactical_status ON tactical_activities(status);
CREATE INDEX idx_tactical_classifier ON tactical_activities(economic_classifier);
```

#### Tabela: `okrs` e `key_results`

*Ref: Backlog US004*

| Tabela        | Campo           | Tipo         | Descrição                              |
| :------------ | :-------------- | :----------- | :------------------------------------- |
| `okrs`        | `cycle`         | VARCHAR(10)  | Ex: "2026-Q1".                         |
| `key_results` | `target_value`  | DECIMAL      | Meta numérica.                         |
| `key_results` | `current_value` | DECIMAL      | Valor atual (atualizado via check-in). |
| `checkins`    | `evidence_url`  | VARCHAR(500) | Link para S3/MinIO com evidência.      |

***

### 3.3. Esquema: `db_budget` (Service-Budget-Adapter)

#### Tabela: `budget_drivers`

Parâmetros para cálculo automático de custos.
*Ref: Backlog US006*

| Campo         | Tipo        | Descrição                                    |
| :------------ | :---------- | :------------------------------------------- |
| `driver_type` | VARCHAR(50) | 'PER\_DIEM', 'FUEL', 'FLIGHT'.               |
| `params`      | JSONB       | Ex: `{"destination": "SAL", "cost": 11000}`. |
| `valid_from`  | DATE        | Início da vigência do valor.                 |

***

## 4. Estratégia de Escalabilidade e Performance

### 4.1. Caching (Redis)

Para cumprir o RNF de tempo de resposta < 2s.

* **Strategy Cache:** Cache dos Mapas Estratégicos e Identidade (TTL: 24h). Invalidação no evento `IdentityUpdated`.

* **Budget Cache:** Cache de classificadores econômicos e drivers (TTL: 1h).

* **Session Cache:** Armazenamento de sessões de usuário e tokens parciais.

### 4.2. Database Read Replicas

* Os Dashboards de BI e Relatórios (QUAR) devem conectar-se exclusivamente às **Read Replicas** do PostgreSQL para não impactar a performance transacional de escrita (CQRS Pattern).

### 4.3. Horizontal Pod Autoscaling (HPA)

* **Trigger:** CPU Usage > 70% ou Request Latency > 300ms.

* **Behavior:** Scale up rápido (stabilizationWindow: 0s), Scale down lento (300s) para evitar "flapping".

***

## 5. Contratos de Integração e APIs

### 5.1. Padrões REST

* **Versionamento:** URI Path (`/api/v1/...`).

* **Formato:** JSON (snake\_case).

* **Erros:** [RFC 7807](https://tools.ietf.org/html/rfc7807) Problem Details.

### 5.2. Especificação de Endpoint Crítico (Ex: Simulação IA)

*Ref: Backlog US007*

**POST** `/api/v1/intelligence/scenarios`

* **Request:**

  ```json
  {
    "type": "BUDGET_CUT",
    "percentage": 10.0,
    "scope": "GLOBAL" // ou DEPARTMENT_ID
  }
  ```

* **Response:**

  ```json
  {
    "scenarioId": "scn-123",
    "impactedActivities": ["act-001", "act-099"],
    "suggestedActions": [
      {"activityId": "act-001", "action": "CANCEL", "reason": "Low Priority"},
      {"activityId": "act-099", "action": "REDUCE_SCOPE", "reason": "Can save 20%"}
    ]
  }
  ```

***

## 6. Estratégia de Deploy e CI/CD (Pipeline IGRP)

### 6.1. Pipeline Stages

1. **Commit:** Desenvolvedor faz push no GitLab.
2. **Build & Test:** Maven Build + JUnit Tests (>80% coverage).
3. **Security Scan (SAST):** SonarQube verifica vulnerabilidades (OWASP Top 10).
4. **Containerize:** Build Docker Image -> Push to Harbor Registry.
5. **Deploy Staging:** Helm Upgrade no namespace `sigdi-staging`.
6. **Integration Test:** Testes de API (Newman) contra Staging.
7. **Approval:** Gate manual para Tech Lead.
8. **Deploy Prod:** Blue/Green deployment no namespace `sigdi-prod`.

### 6.2. Rollback Strategy

* Monitoramento automático de Error Rate pós-deploy.

* Se Error Rate > 1% nos primeiros 5 min, o tráfego é revertido automaticamente para a versão "Blue" (Anterior).

***

## 7. Monitoramento e Observabilidade

### 7.1. Stack de Ferramentas

* **Coleta de Métricas:** Prometheus (Scraping a cada 15s).

* **Visualização:** Grafana (Dashboards por Microserviço).

* **Logs:** ELK Stack (Elasticsearch, Logstash, Kibana). Logs estruturados em JSON.

* **Tracing:** Jaeger (Rastreabilidade de requisições distribuídas).

### 7.2. Alertas Críticos (PagerDuty)

1. **SIGOF Down:** Se `Service-Budget-Adapter` registrar > 5 falhas consecutivas de conexão (Circuit Breaker Aberto).
2. **Budget Exceeded:** Se uma atividade for aprovada sem cabimento (Inconsistência de dados crítica).
3. **High Latency:** Se p95 latency > 2s por mais de 5 minutos.

***

## 8. Segurança e Autenticação

### 8.1. Identity Management

* **Provedor:** Autentika (Gov.cv).

* **Protocolo:** OAuth2 / OpenID Connect.

* **Tokens:** JWT assinado com RS256. Claims incluem `roles` (ex: `strategy_manager`, `tactical_user`).

### 8.2. Segurança de Aplicação

* **Rate Limiting:** 100 req/min por IP (configurado no Ingress/API Gateway).

* **SQL Injection:** Uso obrigatório de JPA/Hibernate Criteria ou Parameterized Queries.

* **XSS/CSRF:** Proteções nativas do React/Next.js e Spring Security habilitadas.

### 8.3. Auditoria Financeira

* Todas as operações em `tactical_activities` e `budget_drivers` geram logs imutáveis na tabela `audit_logs` contendo:

  * `user_id`, `ip_address`, `timestamp`, `old_value`, `new_value`, `operation_type`.

***

## 9. Plano de Disaster Recovery (DR)

### 9.1. Backup Policy

* **Database:** Snapshots diários completos (02:00 AM) + WAL Archiving contínuo (RPO = 15 min).

* **Retention:** 30 dias on-site, 5 anos off-site (Cold Storage para fins legais).

### 9.2. Procedimento de Restauro

1. Notificar Stakeholders.
2. Provisionar nova infraestrutura via Terraform (se necessário).
3. Restaurar último Snapshot saudável.
4. Reaplicar WAL logs até o ponto da falha.
5. Validar integridade dos dados.
6. Apontar DNS para novo ambiente.
7. **RTO Estimado:** 4 horas.

