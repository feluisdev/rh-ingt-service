package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.AssiduidadeResponseDTO;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/** A assiduidade do próprio: a mesma leitura do RH (§6.8), para o utilizador autenticado. */
@Component
@RequiredArgsConstructor
public class GetMinhaAssiduidadeQueryHandler implements QueryHandler<GetMinhaAssiduidadeQuery, ResponseEntity<AssiduidadeResponseDTO>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final GetAssiduidadeQueryHandler getAssiduidadeQueryHandler;

    @IgrpQueryHandler
    public ResponseEntity<AssiduidadeResponseDTO> handle(GetMinhaAssiduidadeQuery query) {
        return getAssiduidadeQueryHandler.handle(new GetAssiduidadeQuery(
                currentEmployeeResolver.resolve().getStringValor(), query.getDe(), query.getAte()));
    }
}
