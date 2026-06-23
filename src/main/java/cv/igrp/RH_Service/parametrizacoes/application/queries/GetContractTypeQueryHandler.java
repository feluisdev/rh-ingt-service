package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.ContractTypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.VinculoLaboralRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ContractTypeId;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.VinculoLaboralId;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.ContractTypeMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetContractTypeQueryHandler implements QueryHandler<GetContractTypeQuery, ResponseEntity<ContractTypeResponseDTO>> {

    private final ContractTypeRepository contractTypeRepository;
    private final ContractTypeMapper contractTypeMapper;
    private final VinculoLaboralRepository vinculoLaboralRepository;

    @IgrpQueryHandler
    public ResponseEntity<ContractTypeResponseDTO> handle(GetContractTypeQuery query) {
        var id = ContractTypeId.from(UUID.fromString(query.getContractTypeId()));
        var ct = contractTypeRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Não encontrado: " + query.getContractTypeId()));
        var dto = contractTypeMapper.toDTO(ct);
        if (ct.getVinculoLaboralId() != null)
            vinculoLaboralRepository.findById(VinculoLaboralId.from(ct.getVinculoLaboralId()))
                    .ifPresent(v -> dto.setVinculoLaboralName(v.getDescription()));
        return ResponseEntity.ok(dto);
    }
}
