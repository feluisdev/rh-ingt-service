package cv.igrp.RH_Service.estrutura.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class WrapperListaOrganizationalUnitDTO {
    private List<OrganizationalUnitResponseDTO> content;
    private long totalElements;
}
