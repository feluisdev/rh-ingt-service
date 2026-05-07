package cv.igrp.RH_Service.estrutura.application.queries;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsColocacaoEntityRepository;
import cv.igrp.RH_Service.estrutura.application.dto.OrganizationalUnitResponseDTO;
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
        implements QueryHandler<GetOrganizationalUnitByIdQuery, ResponseEntity<OrganizationalUnitResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetOrganizationalUnitByIdQueryHandler.class);

    private final OrganizationalUnitRepository unitRepository;
    private final OrganizationalUnitMapper mapper;
    private final ColabsColocacaoEntityRepository colocacaoRepository;

    @IgrpQueryHandler
    public ResponseEntity<OrganizationalUnitResponseDTO> handle(GetOrganizationalUnitByIdQuery query) {
        var unit = unitRepository.findById(OrganizationalUnitId.from(query.getUnitId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Unidade orgânica não encontrada: " + query.getUnitId()));

        var dto = mapper.toDTO(unit);
        dto.setNColaboradores(colocacaoRepository.countByUnitIdAndIsCurrentTrueAndIsActiveTrue(unit.getId().getValor()));
        return ResponseEntity.ok(dto);
    }
}
