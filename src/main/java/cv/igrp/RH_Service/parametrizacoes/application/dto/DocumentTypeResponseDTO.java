package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class DocumentTypeResponseDTO {

    private String id;

    private String codigo;

    private String descricao;

    private String allowedExtensions;

    private String category;
    private String categoryDesc;

    private Boolean isActive;
    private String estadoDesc;
}
