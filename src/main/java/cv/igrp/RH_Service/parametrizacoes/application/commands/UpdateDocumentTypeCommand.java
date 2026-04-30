package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.DocumentTypeRequestDTO;
import cv.igrp.framework.core.domain.Command;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDocumentTypeCommand implements Command {

    private DocumentTypeRequestDTO documentTypeRequest;
    private String documentTypeId;
}
