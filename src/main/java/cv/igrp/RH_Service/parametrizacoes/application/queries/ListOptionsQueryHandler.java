package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaOptionDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.filter.OptionFilter;
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

@Component
@RequiredArgsConstructor
public class ListOptionsQueryHandler implements QueryHandler<ListOptionsQuery, ResponseEntity<WrapperListaOptionDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListOptionsQueryHandler.class);

    private final OptionRepository optionRepository;
    private final OptionMapper optionMapper;

    @Cacheable(value = "reference-options", key = "'list_' + #query.ccode + '_' + #query.locale + '_' + #query.active + '_' + #query.ckey + '_' + #query.nome + '_' + #query.pagina + '_' + #query.tamanho")
    @IgrpQueryHandler
    public ResponseEntity<WrapperListaOptionDTO> handle(ListOptionsQuery query) {
        var filter = new OptionFilter();
        filter.setCcode(query.getCcode());
        filter.setLocale(query.getLocale());
        filter.setActive(query.getActive());
        filter.setCkey(query.getCkey());
        filter.setNome(query.getNome());
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        var pageResult = optionRepository.findAll(filter);
        var content = pageResult.getData().stream().map(optionMapper::toDTO).toList();

        var wrapper = new WrapperListaOptionDTO();
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
