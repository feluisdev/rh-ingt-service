package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.FuncionarioResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.FuncionarioMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetFuncionarioByIdQueryHandler")
@RequiredArgsConstructor
public class GetFuncionarioByIdQueryHandler
        implements QueryHandler<GetFuncionarioByIdQuery, ResponseEntity<FuncionarioResponseDTO>> {

    private final FuncionarioRepository funcionarioRepository;
    private final FuncionarioMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<FuncionarioResponseDTO> handle(GetFuncionarioByIdQuery query) {
        var funcionario = funcionarioRepository.findById(FuncionarioId.from(query.getFuncionarioId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Funcionário não encontrado: " + query.getFuncionarioId()));
        return ResponseEntity.ok(mapper.toDTO(funcionario));
    }
}
