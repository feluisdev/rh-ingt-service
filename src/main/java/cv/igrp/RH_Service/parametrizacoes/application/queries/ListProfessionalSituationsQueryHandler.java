package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaProfessionalSituationDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.filter.ProfessionalSituationFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ProfessionalSituationRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.ProfessionalSituationMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class ListProfessionalSituationsQueryHandler implements QueryHandler<ListProfessionalSituationsQuery, ResponseEntity<WrapperListaProfessionalSituationDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListProfessionalSituationsQueryHandler.class);

    private final ProfessionalSituationRepository professionalSituationRepository;
    private final ProfessionalSituationMapper professionalSituationMapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaProfessionalSituationDTO> handle(ListProfessionalSituationsQuery query) {
        var filter = new ProfessionalSituationFilter();
        filter.setCode(query.getCode());
        filter.setIsActive(query.getIsActive());
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        var content = professionalSituationRepository.findAll(filter).stream()
            .map(professionalSituationMapper::toDTO)
            .toList();

        var wrapper = new WrapperListaProfessionalSituationDTO();
        wrapper.setContent(new ArrayList<>(content));
        wrapper.setTotalElements((long) content.size());

        return ResponseEntity.ok(wrapper);
    }
}
