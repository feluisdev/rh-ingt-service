package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class WrapperListaTipoAusenciaDTO {
    private List<TipoAusenciaResponse> content;
    private long totalElements;
}
