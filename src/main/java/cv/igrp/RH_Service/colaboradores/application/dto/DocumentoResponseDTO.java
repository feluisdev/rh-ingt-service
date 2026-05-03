package cv.igrp.RH_Service.colaboradores.application.dto;

import cv.igrp.RH_Service.parametrizacoes.application.dto.DocumentTypeResponseDTO;
import lombok.Data;

@Data
public class DocumentoResponseDTO {
    private String id;
    private String funcionarioId;
    private String documentTypeId;
    private DocumentTypeResponseDTO documentType;
    private String originalFilename;
    private String contentType;
    private long fileSize;
    private String description;
    private Boolean isActive;
}
