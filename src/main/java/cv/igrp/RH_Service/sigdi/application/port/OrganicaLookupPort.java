package cv.igrp.RH_Service.sigdi.application.port;

import cv.igrp.RH_Service.sigdi.application.dto.OrganicaDTO;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface OrganicaLookupPort {

    Optional<OrganicaDTO> findById(UUID id);

    Map<UUID, OrganicaDTO> findAllByIds(Collection<UUID> ids);

    Optional<UUID> findResponsibleEmployeeId(UUID unitId);
}
