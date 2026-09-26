package cv.igrp.RH_Service.formacao.application.services;

import cv.igrp.RH_Service.colaboradores.application.services.GarantiasDeFormacao;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.formacao.domain.repository.FormacaoRepositorio;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/** As permanências em dívida pelas formações custeadas (art. 95.º b)), para a exoneração voluntária. */
@Component
@RequiredArgsConstructor
public class GarantiasDeFormacaoAdapter implements GarantiasDeFormacao {

    private final FormacaoRepositorio repository;

    @Override
    public List<String> emCurso(FuncionarioId funcionarioId, LocalDate em) {
        return repository.findComGarantiaEm(funcionarioId, em).stream()
                .map(a -> a.inscricaoDe(funcionarioId).map(i -> i.getGarantiaAte()).filter(Objects::nonNull)
                        .filter(ate -> !ate.isBefore(em))
                        .map(ate -> "Prazo de garantia da formação «" + a.getTema() + "» até " + Datas.pt(ate) + " (art. 95.º b)).")
                        .orElse(null))
                .filter(Objects::nonNull).toList();
    }
}
