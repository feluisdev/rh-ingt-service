package cv.igrp.RH_Service.sigdi.application.port;

import cv.igrp.RH_Service.sigdi.application.dto.FuncionarioDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface FuncionarioLookupPort {

    Optional<FuncionarioDTO> findById(UUID id);

    Map<UUID, FuncionarioDTO> findAllByIds(Collection<UUID> ids);

    Optional<UUID> findCurrentOrganizationalUnitId(UUID employeeId);

    /**
     * Identificadores dos funcionários cujo enquadramento na unidade orgânica
     * {@code unitId} cobriu o ano {@code year}, sem duplicados e por ordem de primeira
     * ocorrência.
     */
    List<UUID> findEmployeeIdsAssignedToUnitInYear(UUID unitId, int year);
}
