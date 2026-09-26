package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.EstadoDiaApurado;
import cv.igrp.RH_Service.colaboradores.domain.repository.ProcessoDisciplinarRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Os dias de afastamento disciplinar — a suspensão preventiva (Estatuto Disciplinar, art. 56.º) e a pena de suspensão ou
 * de inactividade em execução (art. 16.º n.º 3) — não se apuram como faltas (BR-DIS-09).
 */
@Component
@RequiredArgsConstructor
public class DiasSuspensaoDisciplinar implements DiasEspeciaisProvider {

    private final ProcessoDisciplinarRepository repository;

    @Override
    public Map<LocalDate, EstadoDiaApurado> dias(FuncionarioId funcionarioId, LocalDate de, LocalDate ate) {
        var dias = new LinkedHashMap<LocalDate, EstadoDiaApurado>();
        for (var p : repository.findComAfastamento(funcionarioId)) {
            for (var periodo : p.periodosDeAfastamento()) {
                LocalDate inicio = periodo[0].isBefore(de) ? de : periodo[0];
                LocalDate fim = periodo[1] == null || periodo[1].isAfter(ate) ? ate : periodo[1];
                for (LocalDate d = inicio; !d.isAfter(fim); d = d.plusDays(1)) dias.put(d, EstadoDiaApurado.SUSPENSAO_DISCIPLINAR);
            }
        }
        return dias;
    }
}
