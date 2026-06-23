package cv.igrp.RH_Service.carreiras.domain.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CareerFilter {
    private Boolean isActive;
    private String code;
    private String nome;
    private int page = 0;
    private int size = 20;
}
