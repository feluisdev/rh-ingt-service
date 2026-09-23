package cv.igrp.RH_Service.parametrizacoes.application.services;

import cv.igrp.RH_Service.parametrizacoes.domain.models.ParametroFerias;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ParametroFeriasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Os parâmetros do mapa de férias que valem num ano: a linha de {@code t_parametro_ferias} em
 * vigor, ou os valores da lei se não houver nenhuma (BR-FER-20).
 */
@Service
@RequiredArgsConstructor
public class ParametrosFeriasService {

    private final ParametroFeriasRepository parametroFeriasRepository;

    public ParametroFerias vigenteEm(int ano) {
        return parametroFeriasRepository.findVigenteEm(ano).orElseGet(ParametroFerias::daLei);
    }
}
