package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.FechoMensalDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.FechoMensal;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/** Do fecho mensal para o DTO, e o mês vindo de fora. */
public final class FechoMensalDtos {

    private FechoMensalDtos() {}

    public static FechoMensalDTO dto(FechoMensal f, int linhas, List<String> alertas) {
        return new FechoMensalDTO(f.getMes().toString(), f.getEstado().name(), f.getFechadoEm(), f.getFechos(), f.getMotivoReabertura(),
                f.getReabertoEm(), f.getTotalFactos(), linhas, new ArrayList<>(alertas != null ? alertas : List.of()));
    }

    public static YearMonth mes(String valor) {
        return GetFactosSalariaisQueryHandler.mes(valor);
    }
}
