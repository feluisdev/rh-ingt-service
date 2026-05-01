package cv.igrp.RH_Service.colaboradores.domain.filter;

import lombok.Data;

import java.util.UUID;

@Data
public class LicencaMobilidadeFilter {
    private UUID funcionarioId;
    private Boolean active;
    private UUID subtipoId;
}
