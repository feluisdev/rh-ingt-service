package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.SituacaoAposentacaoDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AposentacaoService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetSituacaoAposentacaoQueryHandler
        implements QueryHandler<GetSituacaoAposentacaoQuery, ResponseEntity<SituacaoAposentacaoDTO>> {

    private final AposentacaoService aposentacaoService;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpQueryHandler
    public ResponseEntity<SituacaoAposentacaoDTO> handle(GetSituacaoAposentacaoQuery query) {
        FuncionarioId f = query.getFuncionarioId() == null ? currentEmployeeResolver.resolve()
                : FuncionarioId.from(Entrada.uuid(query.getFuncionarioId(), "o colaborador"));
        return ResponseEntity.ok(AposentacaoDtos.dto(aposentacaoService.situacao(f), true));
    }
}
