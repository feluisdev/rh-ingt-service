package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.EstadoDiaApurado;
import cv.igrp.RH_Service.colaboradores.domain.repository.MissaoServicoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** Os dias de uma missão autorizada ou realizada não se apuram como faltas (BR-MSS-06). */
@Component
@RequiredArgsConstructor
public class DiasMissaoServico implements DiasEspeciaisProvider {

    private final MissaoServicoRepository repository;

    @Override
    public Map<LocalDate, EstadoDiaApurado> dias(FuncionarioId funcionarioId, LocalDate de, LocalDate ate) {
        var dias = new LinkedHashMap<LocalDate, EstadoDiaApurado>();
        for (var m : repository.findQueContamEntre(funcionarioId, de, ate)) {
            LocalDate inicio = m.getPartida().toLocalDate().isBefore(de) ? de : m.getPartida().toLocalDate();
            LocalDate fim = m.getRegresso().toLocalDate().isAfter(ate) ? ate : m.getRegresso().toLocalDate();
            for (LocalDate d = inicio; !d.isAfter(fim); d = d.plusDays(1)) dias.put(d, EstadoDiaApurado.MISSAO_SERVICO);
        }
        return dias;
    }
}
