package cv.igrp.RH_Service.estrutura.domain.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobFilter {
    private Boolean isActive;
    private int page = 0;
    private int size = 20;
}
