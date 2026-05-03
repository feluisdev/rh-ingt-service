package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaLeaveMobilitySubtypeDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.filter.LeaveMobilitySubtypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.LeaveMobilitySubtypeRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.LeaveMobilitySubtypeMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListLeaveMobilitySubtypesQueryHandler implements QueryHandler<ListLeaveMobilitySubtypesQuery, ResponseEntity<WrapperListaLeaveMobilitySubtypeDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListLeaveMobilitySubtypesQueryHandler.class);

    private final LeaveMobilitySubtypeRepository leaveMobilitySubtypeRepository;
    private final LeaveMobilitySubtypeMapper leaveMobilitySubtypeMapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaLeaveMobilitySubtypeDTO> handle(ListLeaveMobilitySubtypesQuery query) {
        var filter = new LeaveMobilitySubtypeFilter();
        filter.setCode(query.getCode());
        filter.setRecordType(query.getRecordType());
        filter.setActive(query.getActive());
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        var pageResult = leaveMobilitySubtypeRepository.findAll(filter);
        var content = pageResult.getData().stream().map(leaveMobilitySubtypeMapper::toDTO).toList();

        var wrapper = new WrapperListaLeaveMobilitySubtypeDTO();
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
