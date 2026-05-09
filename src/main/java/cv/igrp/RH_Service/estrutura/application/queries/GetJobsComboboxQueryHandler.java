package cv.igrp.RH_Service.estrutura.application.queries;

import cv.igrp.RH_Service.estrutura.domain.filter.JobFilter;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetJobsComboboxQueryHandler
        implements QueryHandler<GetJobsComboboxQuery, ResponseEntity<List<ComboboxItemDTO>>> {

    private final JobRepository jobRepository;

    @IgrpQueryHandler
    public ResponseEntity<List<ComboboxItemDTO>> handle(GetJobsComboboxQuery query) {
        var filter = new JobFilter();
        filter.setIsActive(true);
        filter.setPage(0);
        filter.setSize(500);
        var items = jobRepository.findAll(filter).getData().stream()
                .map(j -> new ComboboxItemDTO(j.getId().getStringValor(), j.getName()))
                .toList();
        return ResponseEntity.ok(items);
    }
}
