package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UploadDocumentoRequestDTO {
    private UUID documentTypeId;
    private String fileKey;
    private String originalFilename;
    private String contentType;
    private long fileSize;
    private String description;
}
