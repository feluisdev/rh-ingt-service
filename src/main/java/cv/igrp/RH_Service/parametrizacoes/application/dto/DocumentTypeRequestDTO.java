package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class DocumentTypeRequestDTO {

    @NotBlank
    private String codigo;

    private String descricao;

    private String allowedExtensions;

    private UUID categoryOptionId;
}
