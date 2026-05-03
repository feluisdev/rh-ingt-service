package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.LicencaMobilidadeResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.LicencaMobilidadeId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.LicencaMobilidadeMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetLicencaMobilidadeByIdQueryHandler")
@RequiredArgsConstructor
public class GetLicencaMobilidadeByIdQueryHandler
        implements QueryHandler<GetLicencaMobilidadeByIdQuery, ResponseEntity<LicencaMobilidadeResponseDTO>> {

    private final LicencaMobilidadeRepository licencaRepository;
    private final LicencaMobilidadeMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<LicencaMobilidadeResponseDTO> handle(GetLicencaMobilidadeByIdQuery query) {
        var licenca = licencaRepository.findById(LicencaMobilidadeId.from(query.getLicencaId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Licença/mobilidade não encontrada: " + query.getLicencaId()));
        return ResponseEntity.ok(mapper.toDTO(licenca));
    }
}
