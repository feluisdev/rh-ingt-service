package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.Feriado;
import cv.igrp.RH_Service.colaboradores.domain.repository.FeriadoRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository.PublicHolidayEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository("colabsFeriadoRepositoryImpl")
@RequiredArgsConstructor
public class FeriadoRepositoryImpl implements FeriadoRepository {

    private final PublicHolidayEntityRepository entityRepository;

    @Transactional(readOnly = true)
    @Override
    public List<Feriado> findAplicaveis(LocalDate inicio, LocalDate fim, String areaCkey) {
        return entityRepository.findAplicaveisNoPeriodo(inicio, fim, areaCkey).stream()
                .map(e -> new Feriado(e.getHolidayDate(), Boolean.TRUE.equals(e.getIsRecurring())))
                .toList();
    }
}
