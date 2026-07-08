package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.PaaSubmissionPeriodResponseDTO;
import cv.igrp.RH_Service.sigdi.application.dto.WrapperListPaaSubmissionPeriodDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class GetAllSubmissionPeriodsQueryHandler implements QueryHandler<GetAllSubmissionPeriodsQuery, ResponseEntity<WrapperListPaaSubmissionPeriodDTO>> {

    private final PaaSubmissionPeriodRepository repository;

    public GetAllSubmissionPeriodsQueryHandler(PaaSubmissionPeriodRepository repository) {
        this.repository = repository;
    }

    @IgrpQueryHandler
    @Override
    public ResponseEntity<WrapperListPaaSubmissionPeriodDTO> handle(GetAllSubmissionPeriodsQuery query) {
        int pageNumber = parsePageNumber(query.getPageNumber());
        int pageSize = parsePageSize(query.getPageSize());

        List<PaaSubmissionPeriod> data;
        long total;
        if (query.getPurpose() != null && !query.getPurpose().isBlank()) {
            Purpose purpose = Purpose.fromCodeOrThrow(query.getPurpose());
            data = repository.findAllByPurpose(pageNumber, pageSize, purpose);
            total = repository.countAllByPurpose(purpose);
        } else {
            data = repository.findAll(pageNumber, pageSize);
            total = repository.countAll();
        }
        int totalPages = (int) Math.ceil((double) total / pageSize);

        List<PaaSubmissionPeriodResponseDTO> list = data.stream().map(p -> {
            PaaSubmissionPeriodResponseDTO dto = new PaaSubmissionPeriodResponseDTO();
            dto.setId(p.getId());
            dto.setType(p.getType().getCode());
            dto.setTypeDesc(p.getType().getDescription());
            dto.setStartDate(p.getStartDate());
            dto.setEndDate(p.getEndDate());
            dto.setStatus(p.getStatus());
            dto.setStatusDesc("OPEN".equals(p.getStatus()) ? "Aberto" : "Fechado");
            dto.setYear(p.getYear());
            dto.setPurpose(p.getPurpose().getCode());
            dto.setPurposeDesc(p.getPurpose().getDescription());

            long days = ChronoUnit.DAYS.between(LocalDate.now(), p.getEndDate());
            dto.setDaysRemaining(Math.max(0, days));
            return dto;
        }).toList();

        WrapperListPaaSubmissionPeriodDTO response = new WrapperListPaaSubmissionPeriodDTO();
        response.setPageNumber(pageNumber);
        response.setPageSize(pageSize);
        response.setTotalElements(total);
        response.setTotalPages(totalPages);
        response.setFirst(pageNumber == 0);
        response.setLast(pageNumber >= totalPages - 1);
        response.setData(list);

        return ResponseEntity.ok(response);
    }

    private int parsePageNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 0) throw new NumberFormatException("pageNumber must be >= 0");
            return parsed;
        } catch (Exception e) {
            throw IgrpResponseStatusException.badRequest("pageNumber inválido");
        }
    }

    private int parsePageSize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 20;
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) throw new NumberFormatException("pageSize must be > 0");
            return parsed;
        } catch (Exception e) {
            throw IgrpResponseStatusException.badRequest("pageSize inválido");
        }
    }
}
