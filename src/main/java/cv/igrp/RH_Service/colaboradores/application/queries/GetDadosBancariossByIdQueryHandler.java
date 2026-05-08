package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.DadosBancariosResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.DadosBancariosRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DadosBancariosId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.DadosBancariosMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetDadosBancariossByIdQueryHandler")
@RequiredArgsConstructor
public class GetDadosBancariossByIdQueryHandler
        implements QueryHandler<GetDadosBancariossByIdQuery, ResponseEntity<DadosBancariosResponseDTO>> {

    private final DadosBancariosRepository dadosBancariosRepository;
    private final DadosBancariosMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<DadosBancariosResponseDTO> handle(GetDadosBancariossByIdQuery query) {
        var d = dadosBancariosRepository.findById(DadosBancariosId.from(query.getDadosBancariosId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Dados bancários não encontrados: " + query.getDadosBancariosId()));
        return ResponseEntity.ok(mapper.toDTO(d));
    }
}
