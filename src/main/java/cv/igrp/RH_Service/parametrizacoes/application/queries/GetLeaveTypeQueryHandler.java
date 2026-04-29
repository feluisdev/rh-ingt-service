package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.LeaveTypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.LeaveTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.LeaveTypeMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetLeaveTypeQueryHandler implements QueryHandler<GetLeaveTypeQuery, ResponseEntity<LeaveTypeResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetLeaveTypeQueryHandler.class);

    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveTypeMapper leaveTypeMapper;

    @IgrpQueryHandler
    public ResponseEntity<LeaveTypeResponseDTO> handle(GetLeaveTypeQuery query) {
        var id = ExternalID.from(java.util.UUID.fromString(query.getLeaveTypeId()));

        var leaveType = leaveTypeRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Tipo de licença não encontrado: " + query.getLeaveTypeId()));

        return ResponseEntity.ok(leaveTypeMapper.toDTO(leaveType));
    }
}
