package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoId;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.DocumentTypeId;
import lombok.Getter;

import java.util.UUID;

@Getter
public class Documento {

    private DocumentoId id;
    private String referenceEntity;
    private UUID referenceId;
    private DocumentTypeId documentTypeId;
    private String fileKey;
    private String originalFilename;
    private String contentType;
    private long fileSize;
    private String description;
    private Boolean isActive;

    private Documento() {}

    public static Documento criar(String referenceEntity, UUID referenceId,
                                   DocumentTypeId documentTypeId,
                                   String fileKey, String originalFilename,
                                   String contentType, long fileSize, String description) {
        Documento d = new Documento();
        d.id = DocumentoId.gerarNovo();
        d.referenceEntity = referenceEntity;
        d.referenceId = referenceId;
        d.documentTypeId = documentTypeId;
        d.fileKey = fileKey;
        d.originalFilename = originalFilename;
        d.contentType = contentType;
        d.fileSize = fileSize;
        d.description = description;
        d.isActive = true;
        return d;
    }

    public static Documento reconstituir(DocumentoId id, String referenceEntity,
                                          UUID referenceId, DocumentTypeId documentTypeId,
                                          String fileKey, String originalFilename,
                                          String contentType, long fileSize, String description,
                                          Boolean isActive) {
        Documento d = new Documento();
        d.id = id;
        d.referenceEntity = referenceEntity;
        d.referenceId = referenceId;
        d.documentTypeId = documentTypeId;
        d.fileKey = fileKey;
        d.originalFilename = originalFilename;
        d.contentType = contentType;
        d.fileSize = fileSize;
        d.description = description;
        d.isActive = isActive;
        return d;
    }

    public void desativar() { this.isActive = false; }
}
