# Research: Documentos de Funcionários

## Decisão 1: Usar DocumentoService existente (shared/) em vez de MinioClient directo

**Decision**: Injectar `DocumentoService` (em `shared/domain/service/`) nos command handlers, em vez de usar `MinioClient` directamente.

**Rationale**: O `DocumentoService` já encapsula:
- `save(DocumentoFolder, MultipartFile)` → upload com geração de `file_key` único (`{folder.code}/{baseName}_{timestamp}.{extensao}`)
- `getPresignedLink(String fileId)` → URL pré-assinada com expiração configurável via `MINIO_PRESIGNED_URL_EXPIRATION_TIME`
- Tratamento de erros MinIO já centralizado

**Alternatives considered**:
- Injectar `MinioStorage` directamente nos handlers → viola o princípio de não expor infra ao domínio; `DocumentoService` é a abstracção correcta
- Criar nova porta de domínio `ArmazenamentoPort` → sobreengenharaia desnecessária quando o shared service já é suficiente

---

## Decisão 2: DocumentoFolder.FUNCIONARIO já existe — reutilizar

**Decision**: Usar o enum `DocumentoFolder.FUNCIONARIO` (`code="funcionario_documents"`) para organizar os ficheiros no bucket MinIO.

**Rationale**: O enum `DocumentoFolder` em `shared/application/constants/` já define o folder para documentos de funcionários. O `file_key` gerado por `DocumentoService.save()` segue o padrão `funcionario_documents/{baseName}_{timestamp}.{extensao}`. Este valor é o que se persiste na coluna `file_key` da tabela `t_document`.

**Alternatives considered**:
- Gerar o path manualmente com UUID → redundante; `DocumentoService` já o faz com garantia de unicidade

---

## Decisão 3: Aceder a DocumentType via repositório de parametrizacoes

**Decision**: Injectar `DocumentTypeRepository` (port de `parametrizacoes/domain/repository/`) nos handlers de documento para validar tipo e `allowedExtensions`.

**Rationale**: `DocumentType` pertence ao módulo `parametrizacoes` e já expõe um port `DocumentTypeRepository` com `findById(DocumentTypeId)`. Não se justifica duplicar o catálogo em `colaboradores/`.

**Alternatives considered**:
- Chamar endpoint REST interno → latência desnecessária e acoplamento fraco num monolito
- Criar espelho do catálogo em colaboradores/ → duplicação de dados e risco de dessincronização

---

## Decisão 4: Tabela própria t_document — não reutilizar t_tipo_documento

**Decision**: Criar nova tabela `t_document` em V27 para os registos de documentos. A tabela `t_tipo_documento` em `parametrizacoes` é o *catálogo de tipos*, não os documentos em si.

**Rationale**: A tabela `t_document` armazena instâncias (uploads reais) com `file_key`, `original_filename`, `content_type`, etc. O catálogo `t_tipo_documento` define as regras (extensões, categoria). São entidades com propósitos distintos.

---

## Decisão 5: Soft delete — não apagar ficheiro do MinIO

**Decision**: O `DELETE /documentos/{id}` apenas marca `is_active=false` na BD. O ficheiro permanece no MinIO.

**Rationale**: Auditoria e conformidade legal exigem rastreabilidade dos ficheiros enviados. A remoção física do MinIO é responsabilidade de um processo de ciclo de vida separado (lifecycle policy no bucket). O download de documentos inactivos retorna `404` por lógica de negócio, não por ausência física.

---

## Decisão 6: Validação de extensão no handler, não no controller

**Decision**: A validação da extensão contra `allowedExtensions` do `DocumentType` ocorre no `UploadDocumentoCommandHandler`, não no controller.

**Rationale**: Segue o princípio CQRS da constituição — lógica de negócio exclusivamente nos handlers. O controller apenas valida tamanho máximo (10 MB) como barreira primária de infra.

---

## Decisão 7: file_key armazenado como devolvido por DocumentoService

**Decision**: Persistir o `file_key` exactamente como devolvido por `DocumentoService.save()` na resposta `FileResponseDTO`. Para o download, passar esse mesmo `file_key` a `DocumentoService.getPresignedLink(fileId)`.

**Rationale**: O `DocumentoService` é a única fonte de verdade sobre a estrutura dos paths no MinIO. Replicar a lógica de geração de path seria frágil.
