package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.util.List;

@Data
public class WrapperListaLicencaMobilidadeDTO {
    private List<LicencaMobilidadeResponseDTO> content;
    private int totalElements;
}
