package cv.igrp.RH_Service.parametrizacoes.domain.filter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptionFilter {

    private String ccode;
    private String locale;
    private Boolean active;
    private String ckey;
    private String nome;
    private int page = 0;
    private int size = 20;
}
