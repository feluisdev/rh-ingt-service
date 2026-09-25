package cv.igrp.RH_Service.colaboradores.application.services;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Em que mês de processamento entra um facto com uma data de efeito (BR-FAC-03): o mês da data de
 * efeito. Enquanto não houver fecho mensal (F5.1), nenhum mês está fechado — é sempre esse.
 */
@Service
public class CompetenciaSalarial {

    public YearMonth competencia(LocalDate dataEfeito) {
        return YearMonth.from(dataEfeito);
    }
}
