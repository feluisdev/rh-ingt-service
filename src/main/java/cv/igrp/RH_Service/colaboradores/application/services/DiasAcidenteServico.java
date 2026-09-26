package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.EstadoDiaApurado;
import cv.igrp.RH_Service.colaboradores.domain.repository.AcidenteServicoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Os dias de incapacidade temporária por acidente em serviço ou doença profissional qualificados são faltas justificadas sem
 * perda de direitos (Lei n.º 20/X/2023, art. 188.º; BR-SST-05): não se apuram como faltas.
 */
@Component
@RequiredArgsConstructor
public class DiasAcidenteServico implements DiasEspeciaisProvider {

    private final AcidenteServicoRepository repository;

    @Override
    public Map<LocalDate, EstadoDiaApurado> dias(FuncionarioId funcionarioId, LocalDate de, LocalDate ate) {
        var dias = new LinkedHashMap<LocalDate, EstadoDiaApurado>();
        for (var a : repository.findByFuncionario(funcionarioId)) {
            if (!a.contaComoAcidente()) continue;
            for (var i : a.getIncapacidades()) {
                LocalDate inicio = i.inicio().isBefore(de) ? de : i.inicio();
                LocalDate fim = i.fim() == null || i.fim().isAfter(ate) ? ate : i.fim();
                for (LocalDate d = inicio; !d.isAfter(fim); d = d.plusDays(1)) dias.put(d, EstadoDiaApurado.ACIDENTE_SERVICO);
            }
        }
        return dias;
    }
}
