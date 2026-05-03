package cv.igrp.RH_Service.colaboradores.domain.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeriadoFilter {
    private Integer ano;
    private Boolean isNational;
    private Boolean active;
}
