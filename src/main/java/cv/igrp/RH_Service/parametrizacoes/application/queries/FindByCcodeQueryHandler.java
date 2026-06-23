package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.OptionResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Option;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.OptionRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.OptionMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FindByCcodeQueryHandler implements QueryHandler<FindByCcodeQuery, ResponseEntity<List<OptionResponseDTO>>> {

    private static final String DEFAULT_LOCALE = "pt-CV";

    private final OptionRepository optionRepository;
    private final OptionMapper optionMapper;

    @Cacheable(value = "reference-options", key = "#query.ccode + '_' + #query.locale")
    @IgrpQueryHandler
    public ResponseEntity<List<OptionResponseDTO>> handle(FindByCcodeQuery query) {
        String locale = (query.getLocale() == null || query.getLocale().isBlank())
                ? DEFAULT_LOCALE : query.getLocale();

        List<Option> options = optionRepository.findByCcodeAndLocale(query.getCcode(), locale, true);

        if (options.isEmpty() && !DEFAULT_LOCALE.equals(locale)) {
            options = optionRepository.findByCcodeAndLocale(query.getCcode(), DEFAULT_LOCALE, true);
        }

        return ResponseEntity.ok(options.stream().map(optionMapper::toDTO).toList());
    }
}
