package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.util.List;

@Data
public class WrapperListaDocumentoDTO {
    private List<DocumentoResponseDTO> content;
    private int totalElements;
}
