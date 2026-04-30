package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaOptionDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.filter.OptionFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Option;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.OptionRepository;
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
public class ListOptionsQueryHandler implements QueryHandler<ListOptionsQuery, ResponseEntity<WrapperListaOptionDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListOptionsQueryHandler.class);

    private final OptionRepository optionRepository;
    private final OptionMapper optionMapper;

    @Cacheable(value = "reference-options", key = "'list_' + #query.ccode + '_' + #query.locale + '_' + #query.active + '_' + #query.pagina + '_' + #query.tamanho")
    @IgrpQueryHandler
    public ResponseEntity<WrapperListaOptionDTO> handle(ListOptionsQuery query) {
        var filter = new OptionFilter();
        filter.setCcode(query.getCcode());
        filter.setLocale(query.getLocale());
        filter.setActive(query.getActive());
        filter.setCkey(query.getCkey());
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        List<Option> options = optionRepository.findAll(filter);

        var content = options.stream().map(optionMapper::toDTO).toList();

        var wrapper = new WrapperListaOptionDTO();
        wrapper.setContent(new java.util.ArrayList<>(content));
        wrapper.setTotalElements((long) content.size());

        return ResponseEntity.ok(wrapper);
    }
}
