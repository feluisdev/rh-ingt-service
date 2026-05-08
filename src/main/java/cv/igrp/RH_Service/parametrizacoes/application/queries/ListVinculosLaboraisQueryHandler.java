package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaVinculoLaboralDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.filter.VinculoLaboralFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.VinculoLaboralRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.VinculoLaboralMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListVinculosLaboraisQueryHandler implements QueryHandler<ListVinculosLaboraisQuery, ResponseEntity<WrapperListaVinculoLaboralDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListVinculosLaboraisQueryHandler.class);

    private final VinculoLaboralRepository vinculoLaboralRepository;
    private final VinculoLaboralMapper vinculoLaboralMapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaVinculoLaboralDTO> handle(ListVinculosLaboraisQuery query) {
        var filter = new VinculoLaboralFilter();
        filter.setCode(query.getCode());
        filter.setIsActive(query.getIsActive());
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        var pageResult = vinculoLaboralRepository.findAll(filter);
        var content = pageResult.getData().stream().map(vinculoLaboralMapper::toDTO).toList();

        var wrapper = new WrapperListaVinculoLaboralDTO();
        wrapper.setContent(new java.util.ArrayList<>(content));
        wrapper.setTotalElements(pageResult.getTotalElements());
        wrapper.setPageNumber(pageResult.getPageNumber());
        wrapper.setPageSize(pageResult.getPageSize());
        wrapper.setTotalPages(pageResult.getTotalPages());
        wrapper.setFirst(pageResult.isFirst());
        wrapper.setLast(pageResult.isLast());

        return ResponseEntity.ok(wrapper);
    }
}
