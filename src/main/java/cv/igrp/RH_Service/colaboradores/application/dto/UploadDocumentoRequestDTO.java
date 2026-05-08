package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class UploadDocumentoRequestDTO {
    private UUID documentTypeId;
    @NotBlank
    private String referenceEntity;
    private UUID referenceId;
    private String fileKey;
    private String originalFilename;
    private String contentType;
    private long fileSize;
    private String description;
}
