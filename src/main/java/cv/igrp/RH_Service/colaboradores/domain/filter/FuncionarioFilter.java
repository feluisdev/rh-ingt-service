package cv.igrp.RH_Service.colaboradores.domain.filter;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FuncionarioFilter {
    private String nome;
    private String nif;
    private String situacaoProfissional;
    private UUID unidadeOrganicaId;
    private UUID careerId;
    private Boolean isActive = true;
    private int page = 0;
    private int size = 20;
}
