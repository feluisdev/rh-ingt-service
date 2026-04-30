package cv.igrp.RH_Service.estrutura.application.queries;

import cv.igrp.RH_Service.estrutura.application.dto.OrganizationalUnitResponse;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.OrganizationalUnitMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetOrganizationalUnitByIdQueryHandler
        implements QueryHandler<GetOrganizationalUnitByIdQuery, ResponseEntity<OrganizationalUnitResponse>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetOrganizationalUnitByIdQueryHandler.class);

    private final OrganizationalUnitRepository unitRepository;
    private final OrganizationalUnitMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<OrganizationalUnitResponse> handle(GetOrganizationalUnitByIdQuery query) {
        var unit = unitRepository.findById(OrganizationalUnitId.from(query.getUnitId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Unidade orgânica não encontrada: " + query.getUnitId()));

        return ResponseEntity.ok(mapper.toDTO(unit));
    }
}
