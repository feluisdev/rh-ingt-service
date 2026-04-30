package cv.igrp.RH_Service.parametrizacoes.domain.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveMobilitySubtypeFilter {
    private String code;
    private String recordType;
    private Boolean active;
    private int page = 0;
    private int size = 20;
}
