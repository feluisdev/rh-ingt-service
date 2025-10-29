package cv.igrp.RH_Service.funcionarios.application.queries;

import cv.igrp.RH_Service.funcionarios.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.FuncionarioMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import cv.igrp.RH_Service.funcionarios.application.dto.FuncionarioDetailsDTO;

@Component
public class GetFuncionarioDetailsQueryHandler implements QueryHandler<GetFuncionarioDetailsQuery, ResponseEntity<FuncionarioDetailsDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetFuncionarioDetailsQueryHandler.class);


  private final FuncionarioRepository funcionarioRepository;
  private final FuncionarioMapper funcionarioMapper;

  public GetFuncionarioDetailsQueryHandler(FuncionarioRepository funcionarioRepository, FuncionarioMapper funcionarioMapper) {

    this.funcionarioRepository = funcionarioRepository;
    this.funcionarioMapper = funcionarioMapper;
  }

   @IgrpQueryHandler
  public ResponseEntity<FuncionarioDetailsDTO> handle(GetFuncionarioDetailsQuery query) {
     var funcionarioUuid = ExternalID.from(query.getFuncionarioId());

     var funcionario = funcionarioRepository.getByIdWithDetails(funcionarioUuid).orElseThrow(
         () -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "funcionario not found")
     );


    return ResponseEntity.ok(funcionarioMapper.toResponseDetails(funcionario));
  }

}
