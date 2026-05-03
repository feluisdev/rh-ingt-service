package cv.igrp.RH_Service.sigdi.infrastructure.lookup;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsFuncionarioEntityRepository;
import cv.igrp.RH_Service.sigdi.application.dto.FuncionarioDTO;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FuncionarioLookupAdapter implements FuncionarioLookupPort {

    private final ColabsFuncionarioEntityRepository repository;

    @Override
    public Optional<FuncionarioDTO> findById(UUID id) {
        return repository.findById(id).map(this::toDto);
    }

    @Override
    public Map<UUID, FuncionarioDTO> findAllByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        return repository.findAllById(ids).stream()
                .collect(Collectors.toMap(FuncionarioEntity::getId, this::toDto));
    }

    private FuncionarioDTO toDto(FuncionarioEntity entity) {
        FuncionarioDTO dto = new FuncionarioDTO();
        dto.setId(entity.getId().toString());
        dto.setNomeCompleto(entity.getNomeCompleto());
        return dto;
    }
}
