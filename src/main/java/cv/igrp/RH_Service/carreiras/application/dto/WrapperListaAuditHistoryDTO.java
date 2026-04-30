package cv.igrp.RH_Service.carreiras.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class WrapperListaAuditHistoryDTO {
    private List<AuditHistoryEntryDTO> content;
    private long totalElements;
}
