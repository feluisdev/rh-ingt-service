package cv.igrp.RH_Service.formacao.application.services;

import cv.igrp.RH_Service.colaboradores.application.services.DiasEspeciaisProvider;
import cv.igrp.RH_Service.colaboradores.domain.models.EstadoDiaApurado;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.formacao.domain.repository.FormacaoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** Os dias de uma acção de formação em que o colaborador foi admitido não se apuram como faltas (BR-FRM-11). */
@Component
@RequiredArgsConstructor
public class DiasFormacao implements DiasEspeciaisProvider {

    private final FormacaoRepositorio repository;

    @Override
    public Map<LocalDate, EstadoDiaApurado> dias(FuncionarioId funcionarioId, LocalDate de, LocalDate ate) {
        var dias = new LinkedHashMap<LocalDate, EstadoDiaApurado>();
        for (var a : repository.findComFormandoEntre(funcionarioId, de, ate)) {
            LocalDate inicio = a.getInicio().isBefore(de) ? de : a.getInicio();
            LocalDate fim = a.getFim().isAfter(ate) ? ate : a.getFim();
            for (LocalDate d = inicio; !d.isAfter(fim); d = d.plusDays(1)) dias.put(d, EstadoDiaApurado.FORMACAO);
        }
        return dias;
    }
}
