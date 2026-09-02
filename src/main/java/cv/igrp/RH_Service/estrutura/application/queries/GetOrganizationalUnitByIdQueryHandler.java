package cv.igrp.RH_Service.estrutura.application.queries;

import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.estrutura.application.dto.OrganizationalUnitResponseDTO;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.OrganizationalUnitMapper;
import cv.igrp.RH_Service.parametrizacoes.application.port.OptionLookupPort;
import cv.igrp.RH_Service.parametrizacoes.domain.models.OptionCcode;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetOrganizationalUnitByIdQueryHandler
        implements QueryHandler<GetOrganizationalUnitByIdQuery, ResponseEntity<OrganizationalUnitResponseDTO>> {

    private final OrganizationalUnitRepository unitRepository;
    private final OrganizationalUnitMapper mapper;
    private final PositionRepository positionRepository;
    private final AssignmentRepository assignmentRepository;
    private final OptionLookupPort optionLookupPort;

    @IgrpQueryHandler
    public ResponseEntity<OrganizationalUnitResponseDTO> handle(GetOrganizationalUnitByIdQuery query) {
        var unit = unitRepository.findById(OrganizationalUnitId.from(query.getUnitId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Unidade orgânica não encontrada: " + query.getUnitId()));

        var dto = mapper.toDTO(unit);
        // nColaboradores = ocupantes correntes = Lugares da unidade com afectação corrente (novo modelo)
        long ocupados = positionRepository.findByUnidade(unit.getId().getValor()).stream()
                .filter(p -> assignmentRepository.isPositionOccupied(p.getId().getValor()))
                .count();
        dto.setNColaboradores(ocupados);

        if (unit.getUnitType() != null) {
            optionLookupPort.findByCcodeAndCkey(OptionCcode.UNIT_TYPE.getCode(), unit.getUnitType())
                    .ifPresent(opt -> dto.setUnitTypeDesc(opt.cvalue()));
        }

        if (unit.getParentUnitId() != null) {
            unitRepository.findById(unit.getParentUnitId())
                    .ifPresent(parent -> dto.setParentUnitName(parent.getName()));
        }

        return ResponseEntity.ok(dto);
    }
}
