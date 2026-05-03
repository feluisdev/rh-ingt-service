package cv.igrp.RH_Service.sigdi.infrastructure.lookup;

import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.OrganizationalUnitEntity;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.repository.OrganizationalUnitEntityRepository;
import cv.igrp.RH_Service.sigdi.application.dto.OrganicaDTO;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrganicaLookupAdapter implements OrganicaLookupPort {

    private final OrganizationalUnitEntityRepository repository;

    @Override
    public Optional<OrganicaDTO> findById(UUID id) {
        return repository.findById(id).map(this::toDto);
    }

    @Override
    public Map<UUID, OrganicaDTO> findAllByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        return repository.findAllById(ids).stream()
                .collect(Collectors.toMap(OrganizationalUnitEntity::getId, this::toDto));
    }

    private OrganicaDTO toDto(OrganizationalUnitEntity entity) {
        OrganicaDTO dto = new OrganicaDTO();
        dto.setId(entity.getId().toString());
        dto.setName(entity.getName());
        dto.setAcronym(entity.getAcronym());
        return dto;
    }
}
