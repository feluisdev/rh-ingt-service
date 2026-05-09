package cv.igrp.RH_Service.estrutura.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FunctionRequestDTO {
    private String code;
    private String name;
    private String description;
    private String jobId;
}
