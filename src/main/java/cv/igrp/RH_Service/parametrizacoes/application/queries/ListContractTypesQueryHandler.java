package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaContractTypeDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.filter.ContractTypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.ContractTypeMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class ListContractTypesQueryHandler implements QueryHandler<ListContractTypesQuery, ResponseEntity<WrapperListaContractTypeDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListContractTypesQueryHandler.class);

    private final ContractTypeRepository contractTypeRepository;
    private final ContractTypeMapper contractTypeMapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaContractTypeDTO> handle(ListContractTypesQuery query) {
        var filter = new ContractTypeFilter();
        filter.setCode(query.getCode());
        filter.setIsActive(query.getIsActive());
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        var content = contractTypeRepository.findAll(filter).stream()
            .map(contractTypeMapper::toDTO)
            .toList();

        var wrapper = new WrapperListaContractTypeDTO();
        wrapper.setContent(new ArrayList<>(content));
        wrapper.setTotalElements((long) content.size());

        return ResponseEntity.ok(wrapper);
    }
}
