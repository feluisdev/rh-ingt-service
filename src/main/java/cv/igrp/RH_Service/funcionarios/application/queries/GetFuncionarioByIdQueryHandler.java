package cv.igrp.RH_Service.funcionarios.application.queries;

import cv.igrp.RH_Service.funcionarios.application.dto.FuncionarioResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.FuncionarioMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class GetFuncionarioByIdQueryHandler implements QueryHandler<GetFuncionarioByIdQuery, ResponseEntity<FuncionarioResponseDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetFuncionarioByIdQueryHandler.class);

  private final FuncionarioRepository funcionarioRepository;
  private final FuncionarioMapper funcionarioMapper;

  public GetFuncionarioByIdQueryHandler(FuncionarioRepository funcionarioRepository, FuncionarioMapper funcionarioMapper) {

    this.funcionarioRepository = funcionarioRepository;
    this.funcionarioMapper = funcionarioMapper;
  }

   @IgrpQueryHandler
  public ResponseEntity<FuncionarioResponseDTO> handle(GetFuncionarioByIdQuery query) {
     var funcionarioUuid = ExternalID.from(query.getFuncionarioId());

     var funcionario = funcionarioRepository.getById(funcionarioUuid).orElseThrow(
         () -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "funcionario not found")
     );
    return ResponseEntity.ok(funcionarioMapper.toResponseDTO(funcionario));
  }

}
