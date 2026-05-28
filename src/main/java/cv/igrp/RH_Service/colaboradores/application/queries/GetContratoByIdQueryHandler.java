package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ContratoResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ContratoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ContratoMapper;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ContractTypeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetContratoByIdQueryHandler")
@RequiredArgsConstructor
public class GetContratoByIdQueryHandler
        implements QueryHandler<GetContratoByIdQuery, ResponseEntity<ContratoResponseDTO>> {

    private final ContratoRepository contratoRepository;
    private final ContratoMapper mapper;
    private final ContractTypeRepository contractTypeRepository;

    @IgrpQueryHandler
    public ResponseEntity<ContratoResponseDTO> handle(GetContratoByIdQuery query) {
        var c = contratoRepository.findById(ContratoId.from(query.getContratoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Contrato não encontrado: " + query.getContratoId()));
        var dto = mapper.toDTO(c);
        if (c.getContractTypeId() != null)
            contractTypeRepository.findById(ContractTypeId.from(c.getContractTypeId()))
                    .ifPresent(ct -> dto.setContractTypeName(ct.getDescription()));
        return ResponseEntity.ok(dto);
    }
}
