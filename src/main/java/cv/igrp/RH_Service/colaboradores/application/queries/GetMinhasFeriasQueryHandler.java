package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.FeriasAnoResponseDTO;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetMinhasFeriasQueryHandler implements QueryHandler<GetMinhasFeriasQuery, ResponseEntity<FeriasAnoResponseDTO>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final GetFeriasDoAnoQueryHandler feriasDoAno;

    @IgrpQueryHandler
    public ResponseEntity<FeriasAnoResponseDTO> handle(GetMinhasFeriasQuery query) {
        return feriasDoAno.handle(new GetFeriasDoAnoQuery(currentEmployeeResolver.resolve().getStringValor(), query.getAno()));
    }
}
