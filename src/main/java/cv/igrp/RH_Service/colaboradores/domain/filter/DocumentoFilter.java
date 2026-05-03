package cv.igrp.RH_Service.colaboradores.domain.filter;

import lombok.Data;

import java.util.UUID;

@Data
public class DocumentoFilter {
    private UUID referenceId;
    private UUID documentTypeId;
    private Boolean active;
}
