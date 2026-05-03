package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.ContractTypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.ContractTypeMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ContractTypeId;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetContractTypeQueryHandler implements QueryHandler<GetContractTypeQuery, ResponseEntity<ContractTypeResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetContractTypeQueryHandler.class);

    private final ContractTypeRepository contractTypeRepository;
    private final ContractTypeMapper contractTypeMapper;

    @IgrpQueryHandler
    public ResponseEntity<ContractTypeResponseDTO> handle(GetContractTypeQuery query) {
        var id = ContractTypeId.from(UUID.fromString(query.getContractTypeId()));

        return contractTypeRepository.findById(id)
            .map(contractTypeMapper::toDTO)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Não encontrado: " + query.getContractTypeId()));
    }
}
