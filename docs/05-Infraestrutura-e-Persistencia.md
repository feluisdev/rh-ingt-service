# Documento de Infraestrutura, Persistência e Arquitetura Técnica — SIGDI
**Versão:** 3.0 | **Status:** Definição Arquitetural Completa | **Data:** 30/03/2026

---

## 1. Visão Geral da Arquitetura

O SIGDI adota uma arquitetura de **Microserviços Event-Driven** baseada em Spring Boot (IGRP.spring), operando sobre contentores Kubernetes na NOSi Cloud. A comunicação assíncrona entre serviços é feita via **Apache Kafka**, garantindo desacoplamento real e suportando a invalidação de cache distribuída.

### 1.1. Diagrama de Contexto (C4 — Nível 1)

**Atores:**
- Gestores e Técnicos da Administração Pública (utilizadores primários).
- Admin NOSi (operação da plataforma).
- Auditores (acesso read-only a logs e relatórios).
- Cidadãos (Portal de Transparência — Fase 3).

**Sistemas Externos:**
- **Autentika:** Provedor de Identidade (IdP) para SSO via OAuth2/OpenID Connect.
- **SIGOF (via PDEX):** Sistema de Gestão Financeira do Estado. Integração bidirecional.
- **BDRH:** Base de Dados de Recursos Humanos. Integração read-only.
- **MinIO / S3-compatible:** Armazenamento de ficheiros (evidências de check-ins, PDFs gerados).

### 1.2. Componentes (Microserviços)

| Serviço | Responsabilidade | Base de Dados |
| :--- | :--- | :--- |
| `service-strategy` | BSC, Identidade Institucional, Objetivos, Matrizes SWOT | `db_strategy` |
| `service-tactical` | Core do PAA, OKRs, Workflows de Aprovação, Change Requests | `db_tactical` |
| `service-budget-adapter` | Gateway de integração SIGOF/PDEX, Circuit Breaker, Calculadora Driver-Based | `db_budget` |
| `service-intelligence` | Motor Sage (simulação de cenários), análise de risco de OKRs | `db_intelligence` |
| `service-compliance` | Geração QUAR, Avaliação SIADAP, validação de quotas | `db_compliance` |
| `service-admin` | Multi-tenancy, gestão de utilizadores, delegações, parâmetros globais | `db_admin` |
| `service-notification` | Alertas in-app, emails transacionais, notificações de workflow | (sem estado próprio — consome Kafka) |

### 1.3. Mecanismo de Eventos (Apache Kafka)

O sistema usa Kafka como bus de eventos para comunicação assíncrona entre serviços. Os tópicos principais são:

| Tópico Kafka | Publicado por | Consumido por | Propósito |
| :--- | :--- | :--- | :--- |
| `sigdi.activity.created` | service-tactical | service-notification, service-budget | Notificações de nova atividade, reserva orçamental |
| `sigdi.activity.approved` | service-tactical | service-notification, service-budget | Trigger de sincronização orçamental |
| `sigdi.activity.state-changed` | service-tactical | service-notification | Notificações de workflow |
| `sigdi.budget.synced` | service-budget-adapter | service-tactical, service-intelligence | Atualização de dados de execução financeira |
| `sigdi.budget.alert` | service-budget-adapter | service-notification | Alertas de 70%/90%/100% de cabimento |
| `sigdi.identity.updated` | service-strategy | service-strategy (self — invalidação de cache Redis) | Invalidação do cache do mapa estratégico |
| `sigdi.driver.updated` | service-admin | service-tactical, service-notification | Notificação de driver de custo atualizado |
| `sigdi.okr.at-risk` | service-tactical | service-notification | Alerta de OKR em risco |

**Configuração Kafka:**
- Replication factor: 3 (cluster de 3 brokers).
- Retention: 7 dias para eventos operacionais, 30 dias para eventos de auditoria.
- Consumer groups: um por serviço consumidor, com dead-letter queue para mensagens com falha.

---

## 2. Decisão Arquitetural: Multi-Tenancy

**Decisão:** Row-Level Security (RLS) no PostgreSQL com validação dupla na camada de aplicação.

**Justificação:** A NOSi Cloud não tem capacidade para gerir múltiplos clusters Kubernetes por instituição na fase inicial. O RLS PostgreSQL oferece isolamento com custo de operação único. A validação na camada de aplicação garante defense-in-depth.

**Implementação:**

```sql
-- Em todas as tabelas de dados (exceto tabelas de configuração global):
ALTER TABLE [tabela] ENABLE ROW LEVEL SECURITY;
ALTER TABLE [tabela] FORCE ROW LEVEL SECURITY;

CREATE POLICY institution_isolation_policy ON [tabela]
  AS PERMISSIVE
  FOR ALL
  TO application_role
  USING (institution_id = current_setting('app.current_institution_id')::uuid);
```

**Camada de Aplicação (Spring Boot — Connection Pool):**
```java
// Em cada request, após validação do JWT:
@Component
public class TenantInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, ...) {
        String institutionId = jwtService.extractInstitutionId(request);
        DataSourceUtils.getConnection(dataSource)
            .prepareStatement("SET app.current_institution_id = ?")
            .setString(1, institutionId)
            .execute();
        return true;
    }
}
```

**Testes de isolamento:** Suite de testes automatizados no CI/CD que, com tokens de instituições diferentes, tenta aceder a dados de ambas as instituições e valida que 100% dos acessos cross-tenant retornam 403 ou conjunto vazio.

---

## 3. Persistência de Dados (Dicionário Completo)

### Convenções Globais
- Todos os IDs são `UUID` gerado pelo servidor (`gen_random_uuid()`).
- Todas as tabelas de dados têm `institution_id UUID NOT NULL` com política RLS.
- Soft delete via coluna `deleted_at TIMESTAMP NULL` (registos com `deleted_at IS NOT NULL` são invisíveis por defeito nas queries).
- Auditoria via tabela `t_audit_logs` partilhada, não por triggers (ver secção 3.6).
- Timestamps em UTC sempre. Conversão para timezone do utilizador feita no frontend.

---

### 3.1. Esquema: `db_strategy`

#### `institutional_identity`
```sql
CREATE TABLE t_institutional_identity (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_id  UUID NOT NULL,
    mission         TEXT NOT NULL CHECK (length(mission) >= 20),
    vision          TEXT NOT NULL CHECK (length(vision) >= 20),
    values          JSONB NOT NULL DEFAULT '[]',
    is_active       BOOLEAN NOT NULL DEFAULT FALSE,
    version_comment VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      UUID NOT NULL,
    deleted_at      TIMESTAMPTZ
);
CREATE UNIQUE INDEX idx_identity_active ON t_institutional_identity(institution_id)
    WHERE is_active = TRUE AND deleted_at IS NULL;
```

#### `strategic_goals`
```sql
CREATE TABLE t_strategic_goals (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_id  UUID NOT NULL,
    parent_goal_id  UUID REFERENCES t_strategic_goals(id),
    perspective     VARCHAR(20) NOT NULL CHECK (perspective IN ('FINANCIAL','CUSTOMER','PROCESS','LEARNING')),
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    weight          DECIMAL(5,2) NOT NULL DEFAULT 1.0 CHECK (weight > 0 AND weight <= 10),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','CANCELLED','DELEGATED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      UUID NOT NULL,
    deleted_at      TIMESTAMPTZ
);
CREATE INDEX idx_goals_institution ON t_strategic_goals(institution_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_goals_parent ON t_strategic_goals(parent_goal_id);
```

#### `goal_relationships`
```sql
CREATE TABLE t_goal_relationships (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_id    UUID NOT NULL,
    source_id         UUID NOT NULL REFERENCES t_strategic_goals(id),
    target_id         UUID NOT NULL REFERENCES t_strategic_goals(id),
    relationship_type VARCHAR(30) NOT NULL DEFAULT 'CAUSE_EFFECT',
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by        UUID NOT NULL,
    CONSTRAINT no_self_link CHECK (source_id != target_id)
);
CREATE UNIQUE INDEX idx_goal_rel_unique ON t_goal_relationships(source_id, target_id);
```

---

### 3.2. Esquema: `db_tactical`

#### `tactical_activities` (PAA — Core)
```sql
CREATE TABLE t_tactical_activities (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_id        UUID NOT NULL,
    strategic_goal_id     UUID NOT NULL,  -- FK lógica: validada na camada de serviço, não FK real (cross-schema)
    organic_unit_id       UUID NOT NULL,
    title                 VARCHAR(255) NOT NULL,
    description           TEXT,
    justification_why     TEXT NOT NULL,
    responsible_who       UUID NOT NULL,  -- FK lógica para utilizador (validado via Autentika)
    location_where        VARCHAR(255),
    methodology_how       TEXT,
    start_date            DATE NOT NULL,
    end_date              DATE NOT NULL,
    budget_estimated      DECIMAL(19,4) NOT NULL DEFAULT 0,
    budget_committed      DECIMAL(19,4) NOT NULL DEFAULT 0,  -- sincronizado do SIGOF
    budget_liquidated     DECIMAL(19,4) NOT NULL DEFAULT 0,  -- sincronizado do SIGOF
    budget_paid           DECIMAL(19,4) NOT NULL DEFAULT 0,  -- sincronizado do SIGOF
    economic_classifier   VARCHAR(20) NOT NULL,
    status                VARCHAR(30) NOT NULL DEFAULT 'DRAFT'
                          CHECK (status IN ('DRAFT','PENDING_TACTICAL','PENDING_STRATEGIC','APPROVED','REJECTED','CANCELLED')),
    fiscal_year           INTEGER NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by            UUID NOT NULL,
    deleted_at            TIMESTAMPTZ,
    CONSTRAINT chk_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_budget_positive CHECK (budget_estimated >= 0)
);
CREATE INDEX idx_activities_status ON t_tactical_activities(institution_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_activities_classifier ON t_tactical_activities(institution_id, economic_classifier);
CREATE INDEX idx_activities_fiscal_year ON t_tactical_activities(institution_id, fiscal_year);
CREATE INDEX idx_activities_responsible ON t_tactical_activities(responsible_who);
```

**Nota sobre FKs cross-schema:** `strategic_goal_id` e `organic_unit_id` são FKs lógicas (não constraints de banco) para entidades em `db_strategy` e `db_admin`. A consistência é garantida:
1. Na criação: o `service-tactical` chama `service-strategy` via API para validar `strategic_goal_id` antes de persistir.
2. Na atualização de estado do objetivo: evento Kafka `sigdi.goal.deactivated` é consumido por `service-tactical`, que marca atividades vinculadas com uma flag `goal_deactivated_warning = true` e notifica o gestor.

#### `activity_approval_history`
```sql
CREATE TABLE t_activity_approval_history (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_id  UUID NOT NULL,
    activity_id     UUID NOT NULL REFERENCES t_tactical_activities(id),
    action          VARCHAR(20) NOT NULL CHECK (action IN ('SUBMITTED','APPROVED','REJECTED','ESCALATED','DELEGATED')),
    actor_id        UUID NOT NULL,
    delegated_by    UUID,  -- preenchido se ação foi em delegação
    comment         TEXT,
    from_status     VARCHAR(30) NOT NULL,
    to_status       VARCHAR(30) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_approval_activity ON t_activity_approval_history(activity_id);
```

#### `change_requests`
```sql
CREATE TABLE t_change_requests (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_id  UUID NOT NULL,
    activity_id     UUID NOT NULL REFERENCES t_tactical_activities(id),
    field_name      VARCHAR(100) NOT NULL,
    current_value   JSONB NOT NULL,
    proposed_value  JSONB NOT NULL,
    justification   TEXT NOT NULL CHECK (length(justification) >= 50),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING','APPROVED','REJECTED')),
    reviewer_id     UUID,
    reviewer_comment TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at     TIMESTAMPTZ,
    created_by      UUID NOT NULL
);
```

#### `okrs` e `key_results`
```sql
CREATE TABLE t_okrs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_id  UUID NOT NULL,
    strategic_goal_id UUID NOT NULL,  -- FK lógica
    title           VARCHAR(255) NOT NULL,
    cycle           VARCHAR(10) NOT NULL,  -- Ex: "2026-Q1", "2026-H1", "2026"
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      UUID NOT NULL,
    deleted_at      TIMESTAMPTZ
);

CREATE TABLE t_key_results (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_id  UUID NOT NULL,
    okr_id          UUID NOT NULL REFERENCES t_okrs(id),
    title           VARCHAR(255) NOT NULL,
    target_value    DECIMAL(19,4) NOT NULL,
    current_value   DECIMAL(19,4) NOT NULL DEFAULT 0,
    unit            VARCHAR(50),  -- Ex: "pessoas", "documentos", "%"
    weight          DECIMAL(5,2) NOT NULL DEFAULT 1.0,
    deleted_at      TIMESTAMPTZ
);

CREATE TABLE t_checkins (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_id  UUID NOT NULL,
    key_result_id   UUID NOT NULL REFERENCES t_key_results(id),
    value_added     DECIMAL(19,4) NOT NULL,
    comment         TEXT NOT NULL,
    evidence_url    VARCHAR(500),
    checkin_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    created_by      UUID NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

---

### 3.3. Esquema: `db_budget`

#### `budget_drivers`
```sql
CREATE TABLE t_budget_drivers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_type     VARCHAR(50) NOT NULL,  -- 'PER_DIEM', 'FUEL', 'FLIGHT'
    params          JSONB NOT NULL,  -- Ex: {"destination": "SAL", "level": "SENIOR", "cost": 11000}
    valid_from      DATE NOT NULL,
    currency        VARCHAR(3) NOT NULL DEFAULT 'CVE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      UUID NOT NULL
);
CREATE INDEX idx_drivers_type_date ON t_budget_drivers(driver_type, valid_from DESC);
```

#### `sigof_sync_log`
```sql
CREATE TABLE t_sigof_sync_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_id  UUID NOT NULL,
    sync_started_at TIMESTAMPTZ NOT NULL,
    sync_ended_at   TIMESTAMPTZ,
    status          VARCHAR(20) NOT NULL CHECK (status IN ('RUNNING','SUCCESS','PARTIAL','FAILED')),
    records_updated INTEGER,
    error_message   TEXT,
    fiscal_year     INTEGER NOT NULL
);
```

---

### 3.4. Esquema: `db_compliance`

#### `siadap_config`
```sql
CREATE TABLE t_siadap_config (
    id                              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fiscal_year                     INTEGER NOT NULL UNIQUE,
    good_score                      DECIMAL(4,2) NOT NULL DEFAULT 1.5,
    excellent_score                 DECIMAL(4,2) NOT NULL DEFAULT 3.0,
    excellent_quota                 DECIMAL(4,3) NOT NULL DEFAULT 0.25,
    min_collaborators_for_quota     INTEGER NOT NULL DEFAULT 4,
    results_weight                  DECIMAL(4,3) NOT NULL DEFAULT 0.60,
    competencies_weight             DECIMAL(4,3) NOT NULL DEFAULT 0.40,
    created_at                      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by                      UUID NOT NULL,
    CONSTRAINT chk_weights CHECK (results_weight + competencies_weight = 1.0)
);
```

---

### 3.5. Esquema: `db_admin`

#### `institutions`
```sql
CREATE TABLE t_institutions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(20) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    type            VARCHAR(50) NOT NULL,  -- 'MINISTRY', 'DIRECTORATE', 'AUTONOMOUS'
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deactivated_at  TIMESTAMPTZ
);
```

#### `user_delegations`
```sql
CREATE TABLE t_user_delegations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_id  UUID NOT NULL,
    delegator_id    UUID NOT NULL,
    delegate_id     UUID NOT NULL,
    scope           VARCHAR(30) NOT NULL DEFAULT 'ALL_APPROVALS',
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_delegation_dates CHECK (end_date >= start_date),
    CONSTRAINT no_self_delegation CHECK (delegator_id != delegate_id)
);
CREATE INDEX idx_delegations_active ON t_user_delegations(delegate_id, institution_id)
    WHERE is_active = TRUE AND end_date >= CURRENT_DATE;
```

---

### 3.6. Tabela de Auditoria (Partilhada — Schema `db_audit`)

```sql
CREATE TABLE t_audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_id  UUID NOT NULL,
    user_id         UUID NOT NULL,
    ip_address      INET,
    timestamp       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    service         VARCHAR(50) NOT NULL,
    entity_type     VARCHAR(100) NOT NULL,
    entity_id       UUID NOT NULL,
    operation       VARCHAR(20) NOT NULL CHECK (operation IN ('CREATE','UPDATE','DELETE','STATE_CHANGE','LOGIN','EXPORT')),
    old_value       JSONB,
    new_value       JSONB,
    change_request_id UUID,  -- preenchido para operações via CR
    config_version  VARCHAR(50),  -- versão do siadap_config usada (para avaliações)
    metadata        JSONB  -- campos adicionais contextuais
) PARTITION BY RANGE (timestamp);

-- Partição por mês (criada automaticamente via job mensal)
CREATE TABLE t_audit_logs_2026_01 PARTITION OF t_audit_logs
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');

-- Índices por partição (herdados)
CREATE INDEX idx_audit_institution ON t_audit_logs(institution_id, timestamp DESC);
CREATE INDEX idx_audit_entity ON t_audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_user ON t_audit_logs(user_id, timestamp DESC);
```

**Imutabilidade:** A tabela `t_audit_logs` tem `GRANT SELECT, INSERT ON t_audit_logs TO application_role`. `UPDATE` e `DELETE` não são concedidos a nenhum role de aplicação. A role de administração de base de dados (NOSi) pode apenas consultar.

---

## 4. Infraestrutura de Nuvem (NOSi Cloud)

### 4.1. Cluster Kubernetes (K8s)

- **Ambiente:** Private Cloud (OpenShift/Rancher).
- **Namespaces:** `sigdi-dev`, `sigdi-staging`, `sigdi-prod`.
- **Ingress Controller:** NGINX com terminação TLS (Let's Encrypt) e WAF (ModSecurity OWASP ruleset).
- **Service Mesh:** Envoy sidecar para observabilidade de tráfego inter-serviço (Istio ou Linkerd — a decidir com NOSi).

### 4.2. Recursos por Pod

| Serviço | CPU Request/Limit | RAM Request/Limit | Réplicas Min/Max | HPA Trigger |
| :--- | :--- | :--- | :--- | :--- |
| `service-strategy` | 250m / 500m | 512Mi / 1Gi | 2 / 5 | CPU > 70% |
| `service-tactical` | 500m / 1000m | 1Gi / 2Gi | 3 / 10 | CPU > 70% |
| `service-budget-adapter` | 250m / 500m | 512Mi / 1Gi | 2 / 4 | CPU > 70% |
| `service-intelligence` | 1000m / 2000m | 2Gi / 4Gi | 1 / 3 | CPU > 60% |
| `service-compliance` | 500m / 1000m | 1Gi / 2Gi | 2 / 4 | CPU > 70% |
| `service-admin` | 250m / 500m | 512Mi / 1Gi | 2 / 3 | CPU > 70% |
| `service-notification` | 250m / 500m | 512Mi / 1Gi | 2 / 4 | Queue lag > 1000 |
| `PostgreSQL (HA)` | 4 vCPU | 16GB RAM | Primary + 2 Replicas | — |
| `Redis Cluster` | 2 vCPU | 8GB RAM | 3 primários + 3 réplicas | — |
| `Kafka Cluster` | 2 vCPU / broker | 8GB RAM / broker | 3 brokers | — |

---

## 5. Estratégia de Caching (Redis)

O Redis é usado para três propósitos distintos, com TTLs e estratégias de invalidação diferentes:

| Cache | Chave Redis | TTL | Invalidação |
| :--- | :--- | :--- | :--- |
| Mapa Estratégico | `strategy:map:{institutionId}` | 24h | Evento Kafka `sigdi.identity.updated` ou `sigdi.goal.updated` |
| Classificadores Económicos | `budget:classifiers:{institutionId}` | 1h | Manual (admin) ou atualização de driver |
| Sessão de Utilizador | `session:{userId}` | 8h (janela de trabalho) | Logout, desativação de utilizador |
| Resultados QUAR (cache de geração) | `compliance:quar:{institutionId}:{year}` | 30min | Novo check-in ou fechamento de avaliação |
| Saldo Orçamental por Rubrica | `budget:balance:{institutionId}:{classifier}:{year}` | 5min | Evento `sigdi.budget.synced` |

**Nota:** Dashboards de BI conectam-se exclusivamente às Read Replicas do PostgreSQL (padrão CQRS), nunca ao primário. O Redis não substitui o banco — serve apenas dados quentes de alta leitura.

---

## 6. Contratos de Integração

### 6.1. Padrões REST
- Versionamento: URI Path (`/api/v1/...`).
- Formato: JSON com `snake_case`.
- Envelope padrão (JSend):
```json
{ "status": "success", "data": { ... } }
{ "status": "error", "message": "...", "code": "BUSINESS_CODE" }
```
- Erros de validação: RFC 7807 Problem Details.
- Paginação: `{ "content": [...], "page": 0, "size": 20, "totalElements": 150, "totalPages": 8 }`.

### 6.2. Consumer-Driven Contract Testing (Pact)
Cada serviço consumidor define o seu contrato Pact. O CI/CD valida que o produtor cumpre todos os contratos antes de qualquer deploy em staging ou produção. Arquivos Pact versionados no Pact Broker.

---

## 7. Pipeline CI/CD

### 7.1. Etapas

1. **Commit** → Developer faz push no GitLab (branch feature ou main).
2. **Build & Test** → Maven build + JUnit (cobertura > 80%, falha se abaixo).
3. **Contract Tests** → Pact verifica contratos SIGOF e entre microserviços.
4. **Security Scan** → SonarQube (OWASP Top 10 + quality gate).
5. **Multi-tenancy Tests** → Suite automatizada de isolamento cross-tenant.
6. **Containerize** → Docker build + push para Harbor Registry.
7. **Deploy Staging** → Helm upgrade no namespace `sigdi-staging`.
8. **Integration Tests** → Newman (Postman) contra Staging.
9. **Approval Gate** → Tech Lead revê e aprova manualmente.
10. **Deploy Prod** → Blue/Green deployment no namespace `sigdi-prod`.
11. **Smoke Test** → Suite mínima de testes de fumo contra Prod.
12. **Rollback Auto** → Se error rate > 1% nos primeiros 5 minutos, tráfego revertido para Blue.

---

## 8. Monitoramento e Observabilidade

### 8.1. Stack
- **Métricas:** Prometheus (scraping a cada 15s) + Grafana (dashboards por microserviço).
- **Logs:** ELK Stack (Elasticsearch + Logstash + Kibana). Logs estruturados em JSON com campos: `timestamp`, `level`, `service`, `traceId`, `spanId`, `institutionId` (para correlação), `userId`, `message`.
- **Tracing Distribuído:** Jaeger (rastreabilidade end-to-end de requests entre microserviços).
- **Alertas:** PagerDuty com escalada para on-call NOSi.

### 8.2. Alertas Críticos

| Alerta | Condição | Ação |
| :--- | :--- | :--- |
| SIGOF Indisponível | Circuit Breaker aberto por > 10min | PagerDuty P1 → NOSi + Admin SIGDI |
| Orçamento Excedido sem Bloqueio | Atividade APPROVED com liquidated > committed | PagerDuty P1 → Inconsistência de dados |
| Latência Alta | p95 > 2s por > 5min em qualquer serviço | PagerDuty P2 → equipa de dev |
| Falha de Isolamento Cross-Tenant | Qualquer log de acesso cross-tenant | PagerDuty P1 → Security incident |
| Job de Sincronização Falhado | sigof_sync_log com status FAILED por 2 ciclos consecutivos | PagerDuty P2 → Admin |
| Espaço em Disco | > 85% em qualquer volume PostgreSQL | PagerDuty P2 → NOSi infra |

---

## 9. Segurança

### 9.1. Identity e Autorização
- **Autenticação:** Autentika SSO (OAuth2 PKCE). Tokens JWT RS256. Expiração: 1h (access token), 8h (refresh token).
- **Claims JWT obrigatórias:** `sub` (userId), `institution_id`, `roles` (array), `name`, `email`.
- **RBAC:** Definido por `roles` no JWT. Validado em cada endpoint via anotação Spring Security `@PreAuthorize`.
- **Rate Limiting:** 100 req/min por IP (Ingress NGINX). 500 req/min por `userId` (API Gateway).

### 9.2. Segurança de Aplicação
- SQL Injection: JPA Criteria API ou Parameterized Queries obrigatórios. Queries nativas proibidas sem revisão de segurança.
- XSS: React/Next.js com escape automático. `dangerouslySetInnerHTML` proibido.
- CSRF: Spring Security com tokens CSRF em todas as mutations. SameSite=Strict nos cookies de sessão.
- Ficheiros: uploads validados por tipo MIME (não apenas extensão). Tamanho máximo: 25MB. Scan antivírus via ClamAV antes de mover para MinIO.

### 9.3. Auditoria Financeira
Todas as operações em `tactical_activities` e `budget_drivers` geram logs imutáveis na tabela `t_audit_logs` (ver secção 3.6). A aplicação não pode ler logs da sua própria instituição via API de negócio — logs são acessíveis apenas via interface de Auditoria, por perfil `AUDITOR`.

---

## 10. Plano de Disaster Recovery

### 10.1. Backup
- **PostgreSQL:** Snapshots diários às 02:00 AM + WAL Archiving contínuo (RPO = 15min).
- **MinIO (ficheiros):** Replicação síncrona para segundo datacenter NOSi.
- **Retenção:** 30 dias on-site, 5 anos off-site em Cold Storage (obrigação legal).

### 10.2. Procedimento de Restauro
1. Notificar stakeholders (Sponsor + NOSi + Admin SIGDI).
2. Provisionar infraestrutura via Terraform (se necessário).
3. Restaurar último snapshot saudável do PostgreSQL.
4. Reaplicar WAL logs até o ponto da falha.
5. Restaurar ficheiros MinIO do backup do segundo datacenter.
6. Validar integridade dos dados (checksum das tabelas críticas).
7. Executar suite de smoke tests automatizados.
8. Apontar DNS para novo ambiente.
9. **RTO estimado:** 4 horas.

---

## 11. Decisões Arquiteturais em Aberto (Action Items pré-Sprint 1)

| Decisão | Opções | Responsável | Prazo |
| :--- | :--- | :--- | :--- |
| Confirmar schema SIGOF com NOSi | Contrato API formal | Tech Lead + NOSi | Antes do Sprint 1 do service-budget-adapter |
| Service Mesh: Istio vs Linkerd | Depende de suporte NOSi Cloud | Arquiteto + NOSi Infra | Sprint 0 |
| Kafka: NOSi managed vs self-hosted | Custo vs controlo | Tech Lead + NOSi | Sprint 0 |
| Dados de treino Sage Fase 3 | SIGOF histórico (2+ anos)? BDRH? | PO + NOSi | Fase 2 planning |
| Domínio e certificado TLS | sigdi.gov.cv ou subdomínio NOSi | PO + NOSi | Sprint 0 |
