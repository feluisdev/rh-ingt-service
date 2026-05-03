package cv.igrp.RH_Service.estrutura.application.queries;

import cv.igrp.RH_Service.estrutura.application.dto.WrapperListaOrganizationalUnitDTO;
import cv.igrp.RH_Service.estrutura.domain.filter.OrganizationalUnitFilter;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.OrganizationalUnitMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetOrganizationalUnitsQueryHandler
        implements QueryHandler<GetOrganizationalUnitsQuery, ResponseEntity<WrapperListaOrganizationalUnitDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetOrganizationalUnitsQueryHandler.class);

    private final OrganizationalUnitRepository unitRepository;
    private final OrganizationalUnitMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaOrganizationalUnitDTO> handle(GetOrganizationalUnitsQuery query) {
        var filter = new OrganizationalUnitFilter();
        filter.setIsActive(query.getActive());
        filter.setParentUnitId(query.getParentUnitId() != null ? UUID.fromString(query.getParentUnitId()) : null);
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        var pageResult = unitRepository.findAll(filter);
        var content = pageResult.getData().stream().map(mapper::toDTO).toList();

        var wrapper = new WrapperListaOrganizationalUnitDTO();
        wrapper.setContent(new ArrayList<>(content));
        wrapper.setTotalElements(pageResult.getTotalElements());
        wrapper.setPageNumber(pageResult.getPageNumber());
        wrapper.setPageSize(pageResult.getPageSize());
        wrapper.setTotalPages(pageResult.getTotalPages());
        wrapper.setFirst(pageResult.isFirst());
        wrapper.setLast(pageResult.isLast());

        return ResponseEntity.ok(wrapper);
    }
}
