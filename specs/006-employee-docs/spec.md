# Feature Specification: Documentos de Funcionários

**Feature Branch**: `006-employee-docs`
**Created**: 2026-05-01
**Status**: Draft

## User Scenarios & Testing

### User Story 1 — Upload de Documento (Priority: P1)

Um gestor de RH associa um documento oficial (BI, NIF, Diploma, Contrato assinado, etc.) ao dossier digital de um funcionário. O sistema valida o tipo de ficheiro, armazena-o em segurança e regista os metadados para consulta futura.

**Why this priority**: Sem upload, o módulo não tem valor; é a operação fundacional de que todas as outras dependem.

**Independent Test**: Pode ser validado completamente fazendo upload de um PDF para um funcionário existente e verificando que os metadados ficam acessíveis via GET.

**Acceptance Scenarios**:

1. **Given** um funcionário activo e um tipo de documento activo (ex: "Bilhete de Identidade" com extensões permitidas `pdf,jpg,png`), **When** o gestor faz upload de um ficheiro PDF com `documentTypeId` válido, **Then** o sistema retorna `201 Created` com o `id` do documento e os metadados registados.
2. **Given** o mesmo contexto, **When** o gestor tenta fazer upload de um ficheiro `.docx` não permitido pelo tipo de documento, **Then** o sistema retorna `400 Bad Request` com mensagem indicando as extensões aceites.
3. **Given** um `funcionarioId` inexistente, **When** se tenta fazer upload, **Then** o sistema retorna `404 Not Found`.
4. **Given** um `documentTypeId` inactivo ou inexistente, **When** se tenta fazer upload, **Then** o sistema retorna `400 Bad Request`.
5. **Given** um ficheiro com mais de 10 MB, **When** se tenta fazer upload, **Then** o sistema rejeita com `400 Bad Request` antes de aceder ao armazenamento.

---

### User Story 2 — Listagem e Consulta de Metadados (Priority: P2)

Um gestor de RH ou um auditor visualiza a lista de documentos associados a um funcionário, podendo filtrar por tipo de documento ou estado activo. Ao seleccionar um documento, acede aos seus metadados (nome original, tipo, tamanho, data de upload).

**Why this priority**: Consulta e auditoria são o segundo caso de uso mais frequente; sem listagem o upload perde utilidade imediata.

**Independent Test**: Pode ser validado independentemente do download — listar documentos de um funcionário com e sem filtros e verificar os campos de resposta.

**Acceptance Scenarios**:

1. **Given** um funcionário com dois documentos (um BI e um Diploma), **When** se pede a listagem sem filtros, **Then** o sistema retorna ambos os documentos com `totalElements=2`.
2. **Given** o mesmo funcionário, **When** se filtra por `documentTypeId` do BI, **Then** o sistema retorna apenas o documento BI.
3. **Given** um funcionário sem documentos, **When** se pede a listagem, **Then** o sistema retorna `200 OK` com `content=[]` e `totalElements=0`.
4. **Given** um `documentoId` válido, **When** se pede os metadados individuais, **Then** o sistema retorna `originalFilename`, `contentType`, `fileSize`, `description` e `isActive`.

---

### User Story 3 — Download via URL Pré-assinada (Priority: P3)

Um gestor de RH precisa de aceder ao conteúdo de um documento armazenado. O sistema gera uma ligação temporária e segura que permite fazer o download directamente do armazenamento, sem expor credenciais internas.

**Why this priority**: Depende de US1 (upload) e US2 (consulta); o download é necessário para utilização prática mas não bloqueia auditoria de metadados.

**Independent Test**: Pode ser validado separadamente — pedir download de um documento existente e verificar que a resposta contém uma URL temporária válida com tempo de expiração.

**Acceptance Scenarios**:

1. **Given** um documento activo e armazenado, **When** se pede o download, **Then** o sistema retorna `200 OK` com `{ "url": "...", "expiresIn": 3600 }` — a URL deve ser acessível directamente para obter o ficheiro.
2. **Given** um `documentoId` inexistente, **When** se pede o download, **Then** o sistema retorna `404 Not Found`.
3. **Given** um documento marcado como inactivo (soft delete), **When** se pede o download, **Then** o sistema retorna `404 Not Found`.

---

### User Story 4 — Remoção de Documento (Priority: P4)

Um gestor de RH remove logicamente um documento do dossier de um funcionário (ex: documento errado, substituído por versão corrigida). O ficheiro permanece no armazenamento mas deixa de aparecer nas listagens activas.

**Why this priority**: Operação de manutenção; não bloqueia os casos de uso principais.

**Independent Test**: Pode ser validado fazendo soft delete de um documento e confirmando que já não aparece na listagem com `active=true` e que `GET /{id}` e `GET /{id}/download` retornam `404` — apenas documentos activos são acessíveis via API.

**Acceptance Scenarios**:

1. **Given** um documento activo, **When** o gestor faz DELETE, **Then** o sistema retorna `200 OK` e o documento deixa de aparecer na listagem com filtro `active=true`.
2. **Given** um documento já removido (inactivo), **When** se tenta remover novamente, **Then** o sistema retorna `200 OK` (idempotente — operação já foi aplicada).
3. **Given** um `documentoId` inexistente, **When** se tenta remover, **Then** o sistema retorna `404 Not Found`.

---

### Edge Cases

- O que acontece se o armazenamento de ficheiros estiver indisponível durante o upload? O sistema deve retornar `503 Service Unavailable` sem registar metadados parciais na BD.
- O que acontece se dois uploads simultâneos gerarem o mesmo `file_key`? O sistema gera chaves com UUID — colisão é astronomicamente improvável, mas o `file_key` deve ser único por design.
- O que acontece se o ficheiro estiver corrompido (tamanho declarado ≠ tamanho real)? O sistema confia no tamanho reportado pelo multipart e regista o que recebeu.
- Um funcionário pode ter múltiplos documentos do mesmo tipo? Sim — ex: dois contratos em vigor em períodos diferentes. Não há restrição de unicidade por tipo.

## Requirements

### Functional Requirements

- **FR-001**: O sistema DEVE permitir associar um ficheiro a um funcionário existente, registando os metadados do documento (tipo, nome original, MIME type, tamanho, descrição opcional).
- **FR-002**: O sistema DEVE validar que o tipo de documento está activo antes de aceitar o upload.
- **FR-003**: O sistema DEVE validar que a extensão do ficheiro é permitida pelo tipo de documento antes de o armazenar.
- **FR-004**: O sistema DEVE rejeitar ficheiros com tamanho superior a 10 MB.
- **FR-005**: O sistema DEVE armazenar o ficheiro num sistema de armazenamento externo e registar apenas a chave de localização na base de dados.
- **FR-006**: O sistema DEVE gerar uma chave de armazenamento única por documento. O padrão efectivo é determinado pelo serviço de armazenamento interno: `funcionario_documents/{nomeOriginal}_{timestamp}.{extensao}`, garantindo unicidade por geração de timestamp.
- **FR-007**: O sistema DEVE permitir listar os documentos de um funcionário, com filtros opcionais por tipo de documento e estado activo/inactivo.
- **FR-008**: O sistema DEVE permitir consultar os metadados de um documento individual.
- **FR-009**: O sistema DEVE gerar um endereço temporário de acesso ao ficheiro com tempo de expiração configurável (padrão: 3600 segundos), sem expor credenciais de armazenamento.
- **FR-010**: O sistema DEVE permitir desactivar logicamente um documento sem apagar o ficheiro do armazenamento.
- **FR-011**: O sistema DEVE registar auditoria de todas as alterações aos documentos (quem criou, quando, últimas modificações).
- **FR-012**: O sistema DEVE retornar `404` ao tentar aceder, fazer download ou remover um documento que não pertença ao funcionário indicado no URL ou que não exista.

### Key Entities

- **Documento**: Registo de um ficheiro associado a uma entidade do sistema. Atributos principais: identificador único, tipo de documento, referência à entidade proprietária, chave de localização no armazenamento, nome original, MIME type, tamanho, descrição, estado activo.
- **TipoDocumento**: Catálogo existente (parametrizacoes/) que define as extensões permitidas por categoria de documento (ex: BI aceita `pdf,jpg,png`).
- **Funcionario**: Entidade proprietária principal dos documentos nesta fase (referência lógica).

## Success Criteria

### Measurable Outcomes

- **SC-001**: Um gestor consegue fazer upload de um documento e aceder aos seus metadados em menos de 5 segundos em condições normais de rede.
- **SC-002**: A listagem de documentos de um funcionário com até 50 registos é apresentada em menos de 1 segundo.
- **SC-003**: 100% das tentativas de upload com extensão não permitida são rejeitadas antes de qualquer escrita no armazenamento.
- **SC-004**: 100% das tentativas de upload com ficheiros superiores a 10 MB são rejeitadas na recepção do pedido, sem consumir armazenamento.
- **SC-005**: O endereço temporário de download gerado é acessível durante o período de expiração configurado e expira correctamente após esse período.
- **SC-006**: Após soft delete, o documento não aparece em nenhuma listagem com filtro `active=true`, mas os metadados persistem e são auditáveis.

## Assumptions

- O sistema de armazenamento de ficheiros (MinIO/S3-compatible) está operacional e acessível a partir do serviço RH.
- O catálogo de tipos de documento (`TipoDocumento`) já existe com o campo `allowed_extensions` populado — os tipos disponíveis são geridos pelo módulo de parametrizações já implementado.
- A autenticação e autorização de acesso aos endpoints é gerida pela camada de segurança existente; esta spec não define controlo de acesso por papel.
- O modelo polimórfico suporta outras entidades proprietárias no futuro (ex: `PEDIDO_AUSENCIA`), mas esta implementação foca-se exclusivamente em `FUNCIONARIO`.
- Não há limite ao número de documentos por funcionário nem por tipo de documento.
- A configuração do tempo de expiração das URLs pré-assinadas é feita via variável de ambiente; o valor padrão é 3600 segundos (1 hora).
- O soft delete não remove o ficheiro do armazenamento externo — gestão de ciclo de vida do armazenamento é responsabilidade de um processo externo.
- Ficheiros com o mesmo nome podem existir em paralelo para o mesmo funcionário — a unicidade é garantida pela chave UUID gerada internamente.
