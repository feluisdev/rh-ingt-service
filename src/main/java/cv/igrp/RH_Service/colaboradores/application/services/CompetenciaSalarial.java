package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.repository.FechoMensalRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Em que mês de processamento entra um facto com uma data de efeito (BR-FAC-03, BR-FEC-03): o mês da data de efeito — ou,
 * se esse mês já foi fechado, o primeiro mês aberto a seguir (o ajuste entra no mês seguinte, como fazem os sistemas de
 * processamento; o que o salarial já recebeu não se reescreve).
 */
@Service
public class CompetenciaSalarial {

    private final FechoMensalRepository fechos;

    public CompetenciaSalarial(FechoMensalRepository fechos) {
        this.fechos = fechos;
    }

    public YearMonth competencia(LocalDate dataEfeito) {
        YearMonth mes = YearMonth.from(dataEfeito);
        if (fechos == null) return mes;
        var fechados = fechos.mesesFechadosDesde(mes);
        while (fechados.contains(mes)) mes = mes.plusMonths(1);
        return mes;
    }
}
