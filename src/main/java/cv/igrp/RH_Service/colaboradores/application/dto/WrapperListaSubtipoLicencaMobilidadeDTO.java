package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class WrapperListaSubtipoLicencaMobilidadeDTO {
    private List<SubtipoLicencaMobilidadeResponseDTO> content;
    private long totalElements;
}
