package cv.igrp.RH_Service.sigdi.application.port;

import cv.igrp.RH_Service.sigdi.application.dto.FuncionarioDTO;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface FuncionarioLookupPort {

    Optional<FuncionarioDTO> findById(UUID id);

    Map<UUID, FuncionarioDTO> findAllByIds(Collection<UUID> ids);
}
