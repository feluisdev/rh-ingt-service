package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class WrapperListaFuncionarioDTO {
    private List<FuncionarioResponseDTO> content;
    private long totalElements;
    private int pageNumber;
    private int pageSize;
    private int totalPages;
}
