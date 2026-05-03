package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ColocacaoResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.ColocacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ColocacaoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetColocacaoAtualQueryHandler")
@RequiredArgsConstructor
public class GetColocacaoAtualQueryHandler
        implements QueryHandler<GetColocacaoAtualQuery, ResponseEntity<ColocacaoResponseDTO>> {

    private final ColocacaoRepository colocacaoRepository;
    private final ColocacaoMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<ColocacaoResponseDTO> handle(GetColocacaoAtualQuery query) {
        var colocacao = colocacaoRepository.findCurrentByFuncionarioId(FuncionarioId.from(query.getFuncionarioId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Nenhuma colocação actual encontrada para o funcionário: " + query.getFuncionarioId()));
        return ResponseEntity.ok(mapper.toDTO(colocacao));
    }
}
