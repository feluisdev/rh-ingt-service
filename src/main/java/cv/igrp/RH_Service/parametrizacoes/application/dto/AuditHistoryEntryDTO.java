package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class AuditHistoryEntryDTO {
    private Integer revisionNumber;
    private String revisionDate;
    private String modificationType;
}
