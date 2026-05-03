package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.DocumentTypeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class DocumentType {

    private DocumentTypeId id;
    private String codigo;
    private String descricao;
    private String allowedExtensions;
    private UUID categoryOptionId;
    private boolean active;

    private DocumentType() {}

    private DocumentType(DocumentTypeId id, String codigo, String descricao,
                         String allowedExtensions, UUID categoryOptionId, boolean active) {
        this.id = id;
        this.codigo = codigo;
        this.descricao = descricao;
        this.allowedExtensions = allowedExtensions;
        this.categoryOptionId = categoryOptionId;
        this.active = active;
    }

    public static DocumentType criar(String codigo, String descricao,
                                     String allowedExtensions, UUID categoryOptionId) {
        Objects.requireNonNull(codigo, "codigo não pode ser nulo");
        return new DocumentType(DocumentTypeId.gerarNovo(), codigo, descricao,
                allowedExtensions, categoryOptionId, true);
    }

    public static DocumentType reconstruir(DocumentTypeId id, String codigo, String descricao,
                                           String allowedExtensions, UUID categoryOptionId, boolean active) {
        return new DocumentType(id, codigo, descricao, allowedExtensions, categoryOptionId, active);
    }

    public void atualizar(String descricao, String allowedExtensions, UUID categoryOptionId) {
        this.descricao = descricao;
        this.allowedExtensions = allowedExtensions;
        this.categoryOptionId = categoryOptionId;
    }

    public void desativar() {
        if (!this.active) throw IgrpResponseStatusException.conflict("Tipo de documento já está inactivo.");
        this.active = false;
    }

    public void reativar() {
        if (this.active) throw IgrpResponseStatusException.conflict("Tipo de documento já está activo.");
        this.active = true;
    }
}
