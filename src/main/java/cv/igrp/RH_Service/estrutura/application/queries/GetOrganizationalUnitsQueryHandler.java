package cv.igrp.RH_Service.estrutura.application.queries;

import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.estrutura.application.dto.WrapperListaOrganizationalUnitDTO;
import cv.igrp.RH_Service.estrutura.domain.filter.OrganizationalUnitFilter;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.OrganizationalUnitMapper;
import cv.igrp.RH_Service.parametrizacoes.application.port.OptionDTO;
import cv.igrp.RH_Service.parametrizacoes.application.port.OptionLookupPort;
import cv.igrp.RH_Service.parametrizacoes.domain.models.OptionCcode;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GetOrganizationalUnitsQueryHandler
        implements QueryHandler<GetOrganizationalUnitsQuery, ResponseEntity<WrapperListaOrganizationalUnitDTO>> {

    private final OrganizationalUnitRepository unitRepository;
    private final OrganizationalUnitMapper mapper;
    private final PositionRepository positionRepository;
    private final AssignmentRepository assignmentRepository;
    private final OptionLookupPort optionLookupPort;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaOrganizationalUnitDTO> handle(GetOrganizationalUnitsQuery query) {
        var filter = new OrganizationalUnitFilter();
        filter.setIsActive(query.getActive());
        filter.setParentUnitId(query.getParentUnitId() != null ? UUID.fromString(query.getParentUnitId()) : null);
        filter.setCode(query.getCode());
        filter.setNome(query.getNome());
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        var pageResult = unitRepository.findAll(filter);

        Set<String> unitTypes = pageResult.getData().stream()
                .map(u -> u.getUnitType())
                .filter(t -> t != null)
                .collect(Collectors.toSet());
        Map<String, OptionDTO> unitTypeDescs = optionLookupPort
                .findAllByCcodeAndCkeys(OptionCcode.UNIT_TYPE.getCode(), unitTypes);

        Set<UUID> parentIds = pageResult.getData().stream()
                .filter(u -> u.getParentUnitId() != null)
                .map(u -> u.getParentUnitId().getValor())
                .collect(Collectors.toSet());
        Map<UUID, String> parentNames = unitRepository.findAllByIds(parentIds).stream()
                .collect(Collectors.toMap(u -> u.getId().getValor(), u -> u.getName()));

        var content = pageResult.getData().stream().map(unit -> {
            var dto = mapper.toDTO(unit);
            long ocupados = positionRepository.findByUnidade(unit.getId().getValor()).stream()
                    .filter(p -> assignmentRepository.isPositionOccupied(p.getId().getValor()))
                    .count();
            dto.setNColaboradores(ocupados);
            if (unit.getUnitType() != null) {
                OptionDTO opt = unitTypeDescs.get(unit.getUnitType());
                if (opt != null) dto.setUnitTypeDesc(opt.cvalue());
            }
            if (unit.getParentUnitId() != null) {
                dto.setParentUnitName(parentNames.get(unit.getParentUnitId().getValor()));
            }
            return dto;
        }).toList();

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
