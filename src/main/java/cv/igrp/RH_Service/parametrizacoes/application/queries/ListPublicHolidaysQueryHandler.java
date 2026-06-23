package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaPublicHolidayDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.filter.PublicHolidayFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.PublicHolidayRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.PublicHolidayMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ListPublicHolidaysQueryHandler implements QueryHandler<ListPublicHolidaysQuery, ResponseEntity<WrapperListaPublicHolidayDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListPublicHolidaysQueryHandler.class);

    private final PublicHolidayRepository publicHolidayRepository;
    private final PublicHolidayMapper publicHolidayMapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaPublicHolidayDTO> handle(ListPublicHolidaysQuery query) {
        var filter = new PublicHolidayFilter();
        filter.setYear(query.getYear());
        filter.setIsNational(query.getIsNational());
        filter.setIsActive(query.getIsActive());
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        if (query.getDateFrom() != null && !query.getDateFrom().isBlank()) {
            filter.setDateFrom(LocalDate.parse(query.getDateFrom()));
        }
        if (query.getDateTo() != null && !query.getDateTo().isBlank()) {
            filter.setDateTo(LocalDate.parse(query.getDateTo()));
        }
        filter.setNome(query.getNome());

        var pageResult = publicHolidayRepository.findAll(filter);
        var content = pageResult.getData().stream().map(publicHolidayMapper::toDTO).toList();

        var wrapper = new WrapperListaPublicHolidayDTO();
        wrapper.setContent(new java.util.ArrayList<>(content));
        wrapper.setTotalElements(pageResult.getTotalElements());
        wrapper.setPageNumber(pageResult.getPageNumber());
        wrapper.setPageSize(pageResult.getPageSize());
        wrapper.setTotalPages(pageResult.getTotalPages());
        wrapper.setFirst(pageResult.isFirst());
        wrapper.setLast(pageResult.isLast());

        return ResponseEntity.ok(wrapper);
    }
}
