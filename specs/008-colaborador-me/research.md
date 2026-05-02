# Research: Meu Perfil — Área Reservada do Colaborador

**Feature**: 008-colaborador-me | **Date**: 2026-05-02

---

## D1 — IAM User Profile Sync Filter

**Decision**: Portar `IAMUserProfileSyncFilter` do projecto `igrp_platform_process_manager_studio` para `shared/infrastructure/security/` com adição da FK `funcionario_id→t_funcionario`.

**Rationale**: Padrão validado em produção no mesmo stack (Spring Boot 3.5, Keycloak, JWT). Claims sincronizados: `sub`, `preferred_username`, `email`, `given_name`, `family_name`. O filter corre em cada request autenticado e é idempotente (cria na primeira vez, actualiza só se claims mudaram).

**Alternatives considered**:
- Adicionar `keycloak_sub` directamente em `t_funcionario` — rejeitado: viola SRP, polui o aggregate root do funcionário com detalhes de infra IAM.
- Usar `employee_external_mapping` do Bloco 9 — rejeitado: reservado para integração SAD; semântica diferente.

---

## D2 — CurrentEmployeeResolver (Port & Adapter)

**Decision**: Interface `CurrentEmployeeResolver` em `shared/domain/service/` (domain port); implementação `CurrentEmployeeResolverImpl` em `shared/infrastructure/security/` (adapter).

**Rationale**: A interface no domínio permite que handlers dependam de uma abstracção, não de detalhes de JWT ou Spring Security. Injectável e mockável em testes unitários. Segue rigorosamente o padrão Port & Adapter da constituição.

**Assinatura**:
```java
// shared/domain/service/CurrentEmployeeResolver.java
public interface CurrentEmployeeResolver {
    FuncionarioId resolve();  // throws IgrpResponseStatusException 404
}
```

**Alternatives considered**:
- Injectar `SecurityContextHelper` directamente nos handlers — rejeitado: acopla handlers a detalhes de infraestrutura de segurança; viola hexagonal.
- Método estático — rejeitado: não testável, não injectável.

---

## D3 — Ligação Keycloak Sub → FuncionarioId

**Decision**: Campo `funcionario_id UUID FK→t_funcionario` em `t_iam_user_profile`, resolvido no primeiro login por email.

**Rationale**: O email é o atributo de identidade mais estável partilhado entre o Keycloak e o sistema RH. Não requer configuração manual pelo administrador. A resolução é feita uma única vez (no primeiro login) e guardada para todas as requests subsequentes.

**Lógica de resolução no IAMUserProfileSyncFilter**:
```
jwt.email → SELECT id FROM t_funcionario WHERE email = jwt.email AND is_active = true LIMIT 1
         → t_iam_user_profile.funcionario_id = resultado (null se não encontrado)
```

**Edge cases**:
- Email não existe em `t_funcionario` → `funcionario_id = null` → `resolve()` lança 404.
- Funcionário inactivado depois do primeiro login → `resolve()` devolve o ID mas o handler verifica `is_active` (403).

---

## D4 — ApplicationAuditorAware Actualizado

**Decision**: Actualizar `ApplicationAuditorAware` para usar `sub` do JWT como auditor, com fallback para `authentication.getName()` e depois `"system"`.

**Rationale**: `sub` (UUID Keycloak) é o identificador estável e único por utilizador. Alinha com o projecto de referência e garante rastreabilidade de auditoria mesmo se o username mudar.

**Precedência**:
1. `jwt.getClaimAsString("sub")` — se JwtAuthenticationToken
2. `authentication.getName()` — qualquer outro tipo de Authentication
3. `"system"` — fallback para tarefas de background / dev sem autenticação

---

## D5 — Dev Mode sem JWT

**Decision**: Em `development`/`staging`, `CurrentEmployeeResolverImpl` lê o header HTTP `X-Employee-Id` (UUID do funcionário) directamente.

**Rationale**: Em dev, o Spring Security está desactivado — não há JWT. O header `X-Employee-Id` segue o mesmo padrão do `X-Institution-Id` já no `SecurityContextHelper`, mantendo consistência na abordagem de debug.

**Comportamento**:
```
[dev] X-Employee-Id: <uuid> presente → FuncionarioId.from(uuid)
[dev] X-Employee-Id ausente          → primeiro funcionário activo da BD (fallback de conveniência)
[prod] JWT presente                  → sub → t_iam_user_profile → funcionario_id
[prod] JWT ausente                   → 401 (Spring Security rejeita antes do handler)
```

---

## D6 — Padrão SelfService Commands

**Decision**: Comandos com prefixo `SelfService*` que recebem `FuncionarioId` já resolvido. Não reutilizam handlers HR existentes.

**Rationale**: Separação limpa de responsabilidades. O handler HR recebe o `funcionarioId` de path/body (fornecido pelo operador RH); o handler self-service recebe o `FuncionarioId` do `CurrentEmployeeResolver` (resolvido automaticamente do contexto de segurança). A lógica de domínio é partilhada via repositórios e modelos — não via reutilização de handlers.

**Exemplo**:
```
HR:            CreatePedidoAusenciaCommand(funcionarioId: String, request: ...)
Self-service:  SelfServiceCriarPedidoAusenciaCommand(funcionarioId: FuncionarioId, request: ...)
                  → reutiliza PedidoAusenciaRepository, regras de negócio de domínio
                  → não chama CreatePedidoAusenciaCommandHandler
```
