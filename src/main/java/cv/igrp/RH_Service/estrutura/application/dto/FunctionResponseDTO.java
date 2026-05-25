package cv.igrp.RH_Service.estrutura.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FunctionResponseDTO {
    private String id;
    private String code;
    private String name;
    private String description;
    private String jobId;
    private String jobName;
    private String jobDescription;
    private Boolean isActive;
    private String estadoDesc;
}
