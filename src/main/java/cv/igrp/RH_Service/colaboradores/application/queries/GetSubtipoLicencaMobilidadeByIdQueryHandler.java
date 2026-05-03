package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.SubtipoLicencaMobilidadeResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.SubtipoLicencaMobilidadeMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetSubtipoLicencaMobilidadeByIdQueryHandler")
@RequiredArgsConstructor
public class GetSubtipoLicencaMobilidadeByIdQueryHandler
        implements QueryHandler<GetSubtipoLicencaMobilidadeByIdQuery, ResponseEntity<SubtipoLicencaMobilidadeResponseDTO>> {

    private final SubtipoLicencaMobilidadeRepository subtipoRepository;
    private final SubtipoLicencaMobilidadeMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<SubtipoLicencaMobilidadeResponseDTO> handle(GetSubtipoLicencaMobilidadeByIdQuery query) {
        var subtipo = subtipoRepository.findById(SubtipoLicencaMobilidadeId.from(query.getId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Subtipo não encontrado: " + query.getId()));
        return ResponseEntity.ok(mapper.toDTO(subtipo));
    }
}
