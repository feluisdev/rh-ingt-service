package cv.igrp.RH_Service.parametrizacoes.domain.service;

import cv.igrp.RH_Service.parametrizacoes.domain.models.Option;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.OptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReferenceLookupService {

    private static final String DEFAULT_LOCALE = "pt-CV";

    private final OptionRepository optionRepository;

    public List<Option> findByCcode(String ccode, String requestedLocale) {
        String locale = (requestedLocale == null || requestedLocale.isBlank())
                ? DEFAULT_LOCALE : requestedLocale;

        List<Option> results = optionRepository.findByCcodeAndLocale(ccode, locale, true);

        if (results.isEmpty() && !DEFAULT_LOCALE.equals(locale)) {
            results = optionRepository.findByCcodeAndLocale(ccode, DEFAULT_LOCALE, true);
        }

        return results;
    }
}
