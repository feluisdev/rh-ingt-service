package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.OptionResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.service.ReferenceLookupService;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.OptionMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FindByCcodeQueryHandler implements QueryHandler<FindByCcodeQuery, ResponseEntity<List<OptionResponseDTO>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FindByCcodeQueryHandler.class);

    private final ReferenceLookupService referenceLookupService;
    private final OptionMapper optionMapper;

    @Cacheable(value = "reference-options", key = "#query.ccode + '_' + #query.locale")
    @IgrpQueryHandler
    public ResponseEntity<List<OptionResponseDTO>> handle(FindByCcodeQuery query) {
        var options = referenceLookupService.findByCcode(query.getCcode(), query.getLocale());
        var dtos = options.stream().map(optionMapper::toDTO).toList();
        return ResponseEntity.ok(dtos);
    }
}
