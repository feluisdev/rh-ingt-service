package cv.igrp.RH_Service.parametrizacoes.domain.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveTypeFilter {
    private String code;
    private Boolean active;
    private int page = 0;
    private int size = 20;
}
