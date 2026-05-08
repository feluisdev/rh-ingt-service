package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.VinculoLaboralResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.VinculoLaboralRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.VinculoLaboralMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.VinculoLaboralId;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetVinculoLaboralQueryHandler implements QueryHandler<GetVinculoLaboralQuery, ResponseEntity<VinculoLaboralResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetVinculoLaboralQueryHandler.class);

    private final VinculoLaboralRepository vinculoLaboralRepository;
    private final VinculoLaboralMapper vinculoLaboralMapper;

    @IgrpQueryHandler
    public ResponseEntity<VinculoLaboralResponseDTO> handle(GetVinculoLaboralQuery query) {
        var id = VinculoLaboralId.from(UUID.fromString(query.getVinculoLaboralId()));

        return vinculoLaboralRepository.findById(id)
            .map(vinculoLaboralMapper::toDTO)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Não encontrado: " + query.getVinculoLaboralId()));
    }
}
