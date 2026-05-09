package cv.igrp.RH_Service.estrutura.domain.filter;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FunctionFilter {
    private Boolean isActive;
    private UUID jobId;
    private int page = 0;
    private int size = 20;
}
