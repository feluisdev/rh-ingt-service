package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.LeaveMobilitySubtypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.LeaveMobilitySubtypeRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.LeaveMobilitySubtypeMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.LeaveMobilitySubtypeId;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetLeaveMobilitySubtypeQueryHandler implements QueryHandler<GetLeaveMobilitySubtypeQuery, ResponseEntity<LeaveMobilitySubtypeResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetLeaveMobilitySubtypeQueryHandler.class);

    private final LeaveMobilitySubtypeRepository leaveMobilitySubtypeRepository;
    private final LeaveMobilitySubtypeMapper leaveMobilitySubtypeMapper;

    @IgrpQueryHandler
    public ResponseEntity<LeaveMobilitySubtypeResponseDTO> handle(GetLeaveMobilitySubtypeQuery query) {
        var id = LeaveMobilitySubtypeId.from(java.util.UUID.fromString(query.getLeaveMobilitySubtypeId()));

        var leaveMobilitySubtype = leaveMobilitySubtypeRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Subtipo de licença/mobilidade não encontrado: " + query.getLeaveMobilitySubtypeId()));

        return ResponseEntity.ok(leaveMobilitySubtypeMapper.toDTO(leaveMobilitySubtype));
    }
}
