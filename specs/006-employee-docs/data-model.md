# Data Model: Documentos de Funcionários

## Entidade Principal: Documento

### Tabela: `t_document`

| Coluna | Tipo | Nulo | Padrão | Descrição |
|--------|------|------|--------|-----------|
| `id` | UUID | NÃO | — | Chave primária |
| `reference_entity` | VARCHAR(50) | NÃO | — | Tipo da entidade proprietária (ex: `FUNCIONARIO`) |
| `reference_id` | UUID | NÃO | — | ID da entidade proprietária (FK lógica, sem FK física) |
| `document_type_id` | UUID | NÃO | — | FK → `t_tipo_documento.id` |
| `file_key` | VARCHAR(500) | NÃO | — | Chave/path do ficheiro no MinIO (ex: `funcionario_documents/bi_1746123456.pdf`) |
| `original_filename` | VARCHAR(255) | NÃO | — | Nome original do ficheiro enviado |
| `content_type` | VARCHAR(100) | NÃO | — | MIME type (ex: `application/pdf`, `image/jpeg`) |
| `file_size` | BIGINT | NÃO | — | Tamanho em bytes |
| `description` | TEXT | SIM | NULL | Observações opcionais sobre o documento |
| `is_active` | BOOLEAN | NÃO | TRUE | Soft delete |
| `created_date` | TIMESTAMP | NÃO | — | Auditoria Envers |
| `created_by` | VARCHAR(255) | NÃO | — | Auditoria Envers |
| `last_modified_date` | TIMESTAMP | SIM | — | Auditoria Envers |
| `last_modified_by` | VARCHAR(255) | SIM | — | Auditoria Envers |

### Índices

```sql
CREATE INDEX idx_document_reference ON t_document (reference_entity, reference_id, is_active);
CREATE INDEX idx_document_type ON t_document (document_type_id);
```

### Restrições

- Sem FK física para `reference_id` — o modelo é polimórfico; a consistência é garantida pela lógica da aplicação
- FK física para `document_type_id` → `t_tipo_documento(id)`

---

## Domínio: Documento (modelo de domínio Java)

```text
Documento
├── DocumentoId id                   (value object — wraps ExternalID/UUID)
├── String referenceEntity           ("FUNCIONARIO" nesta iteração)
├── FuncionarioId referenceId        (typed ID do funcionário proprietário)
├── DocumentTypeId documentTypeId    (typed ID do tipo de documento — de parametrizacoes/)
├── String fileKey                   (chave no MinIO, devolvida por DocumentoService)
├── String originalFilename
├── String contentType
├── long fileSize
├── String description               (nullable)
└── Boolean isActive
```

**Métodos de domínio**:
- `criar(...)` — factory com `isActive=true`
- `reconstituir(...)` — factory para reconstrução a partir da BD
- `desativar()` — sets `isActive=false`

---

## Dependências de Catálogo (somente leitura)

| Catálogo | Módulo | Port | Campo relevante |
|----------|--------|------|-----------------|
| `DocumentType` | `parametrizacoes` | `DocumentTypeRepository` | `allowedExtensions` (ex: `"pdf,jpg,png"`) |
| `Funcionario` | `colaboradores` | `FuncionarioRepository` | Verificação de existência |

---

## Value Object: DocumentoId

Segue o padrão de todos os value objects do BC colaboradores/:

```java
public final class DocumentoId {
    private final ExternalID valor;
    // gerarNovo(), from(UUID), from(String), getValor(), getStringValor()
    // equals + hashCode
}
```

---

## Referência: DocumentoFolder (enum existente no shared/)

O enum `DocumentoFolder.FUNCIONARIO` (código: `"funcionario_documents"`) é passado ao `DocumentoService.save()` para determinar o subdirectório no MinIO. O `file_key` resultante segue o padrão:

```
funcionario_documents/{originalName}_{timestamp}.{extensao}
```

Este valor é persistido integralmente na coluna `file_key` e usado posteriormente em `DocumentoService.getPresignedLink(fileKey)`.
