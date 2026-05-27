package cv.igrp.RH_Service.parametrizacoes.domain.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractTypeFilter {
    private String code;
    private Boolean isActive;
    private java.util.UUID vinculoLaboralId;
    private int page = 0;
    private int size = 20;
}
