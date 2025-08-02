package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.models.Contrato;
import cv.igrp.RH_Service.funcionarios.domain.repository.*;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DocumentoMapper;
import cv.igrp.RH_Service.funcionarios.infrastructure.persistence.repository.CargoRepositoryImpl;
import cv.igrp.RH_Service.shared.application.constants.ObjetoTipo;
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

  private final TipoDocumentoRepository tipoDocumentoRepository;
  private final DocumentoMapper documentoMapper;
  public CreateContratoCommandHandler(ContratoRepository contratoRepository, FuncionarioRepository funcionarioRepository, DepartamentoRepository departamentoRepository, CargoRepository cargoRepository, TipoDocumentoRepository tipoDocumentoRepository, DocumentoMapper documentoMapper) {

    this.contratoRepository = contratoRepository;
    this.funcionarioRepository = funcionarioRepository;
    this.departamentoRepository = departamentoRepository;
    this.cargoRepository = cargoRepository;
    this.tipoDocumentoRepository = tipoDocumentoRepository;
    this.documentoMapper = documentoMapper;
  }

  @IgrpCommandHandler
  public ResponseEntity<Map<String, ?>> handle(CreateContratoCommand command) {

    var dto = command.getContratorequest();

    var idFuncionario = ExternalID.from(command.getFuncionarioId());
    var existeFuncionario = funcionarioRepository.existsById(idFuncionario);
    if(!existeFuncionario)
      throw IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Funcionario not found with id: " + idFuncionario);


    var idDepartamento = ExternalID.from(dto.getDepartamentoId());
    var existeDepartamento = departamentoRepository.existsById(idDepartamento);
    if(!existeDepartamento)
     throw IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Departament not found with id: " + idDepartamento);


    var idCargo  = ExternalID.from(dto.getCargoId());
    var existeCargo = cargoRepository.existsById(idCargo);
    if(!existeCargo)
     throw IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Cargo not found with id: " + idCargo);

    var contrato = Contrato.criar(
        dto.getTipoContrato(),
        dto.getDataInicio(),
        dto.getDataFim(),
        dto.getSalario(),
        dto.getCargaHoraria(),
        dto.getObservacoes(),
        idDepartamento,
        idFuncionario,
        idCargo
    );

    if (dto.getAnexo() != null){
      var docDto = dto.getAnexo();
      var tipoDocumento = tipoDocumentoRepository.getById(ExternalID.from(docDto.getIdTipodocumento()))
          .orElseThrow(() -> IgrpResponseStatusException.notFound("Tipo documento not found with id:: "+docDto.getIdTipodocumento()));

      var documento = documentoMapper.toDocumentoDomain(ObjetoTipo.CONTRATO, contrato.getIdContrato(), docDto, tipoDocumento);
      contrato.adicionarDocumento(documento);

    }


    var contratoSaved = contratoRepository.save(contrato);

    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("Contrato criado com sucesso!",contratoSaved.getIdContrato().getStringValor()));
  }

}
