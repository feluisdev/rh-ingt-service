package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ColocacaoResponse;
import cv.igrp.RH_Service.colaboradores.domain.repository.ColocacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ColocacaoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ColocacaoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetColocacaoByIdQueryHandler")
@RequiredArgsConstructor
public class GetColocacaoByIdQueryHandler
        implements QueryHandler<GetColocacaoByIdQuery, ResponseEntity<ColocacaoResponse>> {

    private final ColocacaoRepository colocacaoRepository;
    private final ColocacaoMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<ColocacaoResponse> handle(GetColocacaoByIdQuery query) {
        var colocacao = colocacaoRepository.findById(ColocacaoId.from(query.getColocacaoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Colocação não encontrada: " + query.getColocacaoId()));
        return ResponseEntity.ok(mapper.toDTO(colocacao));
    }
}
