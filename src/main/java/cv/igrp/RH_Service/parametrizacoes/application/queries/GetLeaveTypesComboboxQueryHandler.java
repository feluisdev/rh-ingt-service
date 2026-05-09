package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.LeaveTypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.LeaveTypeRepository;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetLeaveTypesComboboxQueryHandler
        implements QueryHandler<GetLeaveTypesComboboxQuery, ResponseEntity<List<ComboboxItemDTO>>> {

    private final LeaveTypeRepository leaveTypeRepository;

    @IgrpQueryHandler
    public ResponseEntity<List<ComboboxItemDTO>> handle(GetLeaveTypesComboboxQuery query) {
        var filter = new LeaveTypeFilter();
        filter.setActive(true);
        filter.setPage(0);
        filter.setSize(500);
        var items = leaveTypeRepository.findAll(filter).getData().stream()
                .map(lt -> new ComboboxItemDTO(lt.getId().getStringValor(), lt.getDescription()))
                .toList();
        return ResponseEntity.ok(items);
    }
}
