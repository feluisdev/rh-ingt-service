package cv.igrp.RH_Service.estrutura.application.port;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface FuncionarioLookupPort {

    Optional<FuncionarioLookupDTO> findById(UUID id);

    Map<UUID, FuncionarioLookupDTO> findAllByIds(Collection<UUID> ids);
}
