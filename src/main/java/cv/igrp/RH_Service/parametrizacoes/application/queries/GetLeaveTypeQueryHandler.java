package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.LeaveTypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.application.port.OptionLookupPort;
import cv.igrp.RH_Service.parametrizacoes.domain.models.OptionCcode;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.LeaveTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.LeaveTypeId;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.LeaveTypeMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetLeaveTypeQueryHandler implements QueryHandler<GetLeaveTypeQuery, ResponseEntity<LeaveTypeResponseDTO>> {

    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveTypeMapper leaveTypeMapper;
    private final OptionLookupPort optionLookupPort;

    @IgrpQueryHandler
    public ResponseEntity<LeaveTypeResponseDTO> handle(GetLeaveTypeQuery query) {
        var id = LeaveTypeId.from(java.util.UUID.fromString(query.getLeaveTypeId()));

        var leaveType = leaveTypeRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Tipo de licença não encontrado: " + query.getLeaveTypeId()));

        var dto = leaveTypeMapper.toDTO(leaveType);
        if (leaveType.getCategory() != null) {
            optionLookupPort.findByCcodeAndCkey(OptionCcode.LEAVE_CATEGORY.getCode(), leaveType.getCategory())
                    .ifPresent(opt -> dto.setCategoryDesc(opt.cvalue()));
        }
        return ResponseEntity.ok(dto);
    }
}
