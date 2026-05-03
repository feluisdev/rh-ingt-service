package cv.igrp.RH_Service.estrutura.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditHistoryEntryDTO {
    private int revisionId;
    private String revisionDate;
    private String type;
}
