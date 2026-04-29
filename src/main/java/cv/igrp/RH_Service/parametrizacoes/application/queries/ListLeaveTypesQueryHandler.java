package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaLeaveTypeDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.filter.LeaveTypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.LeaveType;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.LeaveTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.LeaveTypeMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListLeaveTypesQueryHandler implements QueryHandler<ListLeaveTypesQuery, ResponseEntity<WrapperListaLeaveTypeDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListLeaveTypesQueryHandler.class);

    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveTypeMapper leaveTypeMapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaLeaveTypeDTO> handle(ListLeaveTypesQuery query) {
        var filter = new LeaveTypeFilter();
        filter.setCode(query.getCode());
        filter.setActive(query.getActive());
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        List<LeaveType> leaveTypes = leaveTypeRepository.findAll(filter);

        var content = leaveTypes.stream().map(leaveTypeMapper::toDTO).toList();

        var wrapper = new WrapperListaLeaveTypeDTO();
        wrapper.setContent(new java.util.ArrayList<>(content));
        wrapper.setTotalElements((long) content.size());

        return ResponseEntity.ok(wrapper);
    }
}
