package cv.igrp.RH_Service.sigdi.infrastructure.lookup;

import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.sigdi.application.dto.OrganicaDTO;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrganicaLookupAdapter implements OrganicaLookupPort {

    private final OrganizationalUnitRepository repository;

    @Override
    public Optional<OrganicaDTO> findById(UUID id) {
        return repository.findById(OrganizationalUnitId.from(id)).map(this::toDto);
    }

    @Override
    public Map<UUID, OrganicaDTO> findAllByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        return repository.findAllByIds(ids).stream()
                .collect(Collectors.toMap(
                        u -> u.getId().getValor(),
                        this::toDto));
    }

    @Override
    public Optional<UUID> findResponsibleEmployeeId(UUID unitId) {
        return repository.findById(OrganizationalUnitId.from(unitId))
                .map(OrganizationalUnit::getResponsibleEmployeeId)
                .flatMap(Optional::ofNullable);
    }

    @Override
    public List<OrganicaDTO> findAllActiveUnits() {
        return repository.findAllActive().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private OrganicaDTO toDto(OrganizationalUnit unit) {
        OrganicaDTO dto = new OrganicaDTO();
        dto.setId(unit.getId().getValor().toString());
        dto.setName(unit.getName());
        dto.setAcronym(unit.getAcronym());
        dto.setResponsibleEmployeeId(unit.getResponsibleEmployeeId() != null
                ? unit.getResponsibleEmployeeId().toString()
                : null);
        return dto;
    }
}
