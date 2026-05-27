package cv.igrp.RH_Service.parametrizacoes.domain.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkerStateFilter {
    private String code;
    private String nome;
    private Boolean isActive;
    private int page = 0;
    private int size = 20;
}
