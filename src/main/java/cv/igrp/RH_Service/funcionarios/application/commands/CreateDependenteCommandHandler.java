package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.models.Dependente;
import cv.igrp.RH_Service.funcionarios.domain.repository.DependenteRepository;
import cv.igrp.RH_Service.funcionarios.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DependenteMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.funcionarios.application.dto.DependenteResponseDTO;

@Component
public class CreateDependenteCommandHandler implements CommandHandler<CreateDependenteCommand, ResponseEntity<DependenteResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateDependenteCommandHandler.class);

  private final DependenteRepository dependenteRepository;
  private final FuncionarioRepository funcionarioRepository;
  private final DependenteMapper dependenteMapper;

  public CreateDependenteCommandHandler(DependenteRepository dependenteRepository, FuncionarioRepository funcionarioRepository, DependenteMapper dependenteMapper) {

    this.dependenteRepository = dependenteRepository;
    this.funcionarioRepository = funcionarioRepository;
    this.dependenteMapper = dependenteMapper;
  }

  @IgrpCommandHandler
  public ResponseEntity<DependenteResponseDTO> handle(CreateDependenteCommand command) {
    var dto = command.getDependenterequest();
    var idFuncionario = ExternalID.from(command.getFuncionarioId());

    var funcionario = funcionarioRepository.getByExternalId(idFuncionario)
        .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Funcionario not found with id: " + idFuncionario));

    var dependente = Dependente.criar(
        dto.getNome(),
        dto.getDataNascimento(),
        dto.getParentesco(),
        dto.getCpf(),
        funcionario
    );

    var dependenteSalvo = dependenteRepository.save(dependente);

    var responseDTO = dependenteMapper.toResponseDTO(dependenteSalvo);

    return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
  }

}
