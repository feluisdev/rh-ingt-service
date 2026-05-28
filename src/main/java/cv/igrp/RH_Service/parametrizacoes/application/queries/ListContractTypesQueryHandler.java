package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaContractTypeDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.filter.ContractTypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.VinculoLaboralRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.VinculoLaboralId;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.ContractTypeMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ListContractTypesQueryHandler implements QueryHandler<ListContractTypesQuery, ResponseEntity<WrapperListaContractTypeDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListContractTypesQueryHandler.class);

    private final ContractTypeRepository contractTypeRepository;
    private final ContractTypeMapper contractTypeMapper;
    private final VinculoLaboralRepository vinculoLaboralRepository;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaContractTypeDTO> handle(ListContractTypesQuery query) {
        var filter = new ContractTypeFilter();
        filter.setCode(query.getCode());
        filter.setIsActive(query.getIsActive());
        filter.setNome(query.getNome());
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        var pageResult = contractTypeRepository.findAll(filter);

        Set<UUID> vinculoIds = pageResult.getData().stream()
                .map(ct -> ct.getVinculoLaboralId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<UUID, String> vinculoNames = vinculoIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> vinculoLaboralRepository.findById(VinculoLaboralId.from(id))
                                .map(v -> v.getDescription()).orElse(null),
                        (a, b) -> a));

        var content = pageResult.getData().stream().map(ct -> {
            var dto = contractTypeMapper.toDTO(ct);
            if (ct.getVinculoLaboralId() != null)
                dto.setVinculoLaboralName(vinculoNames.get(ct.getVinculoLaboralId()));
            return dto;
        }).toList();

        var wrapper = new WrapperListaContractTypeDTO();
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
