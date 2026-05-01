package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoDownloadResponse {
    private String url;
    private long expiresIn;
}
