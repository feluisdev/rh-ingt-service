package cv.igrp.RH_Service.colaboradores.domain.filter;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class EnquadramentoFilter {
    private UUID funcionarioId;
    private Boolean isCurrent;
}
