package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.models.Contrato;
import cv.igrp.RH_Service.funcionarios.domain.repository.CargoRepository;
import cv.igrp.RH_Service.funcionarios.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.funcionarios.domain.repository.DepartamentoRepository;
import cv.igrp.RH_Service.funcionarios.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.persistence.repository.CargoRepositoryImpl;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


@Component

public class CreateContratoCommandHandler implements CommandHandler<CreateContratoCommand,ResponseEntity<Map<String, ?>>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateContratoCommandHandler.class);

  private final ContratoRepository contratoRepository;
  private final FuncionarioRepository funcionarioRepository;
  private final DepartamentoRepository departamentoRepository;
  private final CargoRepository cargoRepository;
  public CreateContratoCommandHandler(ContratoRepository contratoRepository, FuncionarioRepository funcionarioRepository, DepartamentoRepository departamentoRepository, CargoRepository cargoRepository) {

    this.contratoRepository = contratoRepository;
    this.funcionarioRepository = funcionarioRepository;
    this.departamentoRepository = departamentoRepository;
    this.cargoRepository = cargoRepository;
  }

  @IgrpCommandHandler
  public ResponseEntity<Map<String, ?>> handle(CreateContratoCommand command) {
    // TODO: Implement the command handling logic here
    var dto = command.getContratorequest();
    var idFuncionario = ExternalID.from(command.getFuncionarioId());

    var funcionario = funcionarioRepository.getByExternalId(idFuncionario)
        .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Funcionario not found with id: " + idFuncionario));


    var idDepartamento = ExternalID.from(dto.getDepartamentoId());
    var departamento = departamentoRepository.getByExternalId(idDepartamento)
        .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Departament not found with id: " + idDepartamento));


    var idCargo  = ExternalID.from(dto.getCargoId());

    var cargo = cargoRepository.getByExternalId(idCargo)
        .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Cargo not found with id: " + idCargo));

    var contrato = Contrato.criar(
        dto.getTipoContrato(),
        dto.getDataInicio(),
        dto.getSalario(),
        dto.getCargaHoraria(),
        dto.getObservacoes(),
        departamento,
        funcionario,
        cargo
    );


    var contratoSaved = contratoRepository.save(contrato);

    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("Contrato criado com sucesso!",contratoSaved.getExternalId()));
  }

}
