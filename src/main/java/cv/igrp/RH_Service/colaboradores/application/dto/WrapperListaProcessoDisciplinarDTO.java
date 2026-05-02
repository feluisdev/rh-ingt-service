package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.util.List;

@Data
public class WrapperListaProcessoDisciplinarDTO {
    private List<ProcessoDisciplinarDTO> content;
    private int totalElements;
}
