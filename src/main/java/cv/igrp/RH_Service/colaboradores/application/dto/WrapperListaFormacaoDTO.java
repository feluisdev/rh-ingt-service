package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.util.List;

@Data
public class WrapperListaFormacaoDTO {
    private List<FormacaoDTO> content;
    private int totalElements;
}
