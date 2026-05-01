package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoUploadResponse {
    private String id;
    private String fileKey;
    private String originalFilename;
    private String contentType;
    private long fileSize;
    private String message;
}
