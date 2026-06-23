package cv.igrp.RH_Service.parametrizacoes.infrastructure.lookup;

import cv.igrp.RH_Service.parametrizacoes.application.port.OptionDTO;
import cv.igrp.RH_Service.parametrizacoes.application.port.OptionLookupPort;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Option;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.OptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OptionLookupAdapter implements OptionLookupPort {

    private final OptionRepository optionRepository;

    @Override
    public Optional<OptionDTO> findByCcodeAndCkey(String ccode, String ckey) {
        List<Option> results = optionRepository.findByCcodeAndCkey(ccode, ckey, true);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        if (results.size() > 1) {
            log.warn("Encontrados {} registos activos para ccode='{}' ckey='{}'; a retornar o primeiro.",
                    results.size(), ccode, ckey);
        }
        return Optional.of(toDto(results.get(0)));
    }

    @Override
    public Map<String, OptionDTO> findAllByCcodeAndCkeys(String ccode, Collection<String> ckeys) {
        if (ckeys == null || ckeys.isEmpty()) return Map.of();
        return optionRepository.findAllByCcodeAndCkeyIn(ccode, ckeys, true)
                .stream()
                .collect(Collectors.toMap(Option::getCkey, this::toDto));
    }

    private OptionDTO toDto(Option option) {
        return new OptionDTO(option.getCcode(), option.getCkey(), option.getCvalue(), option.getLocale());
    }
}
