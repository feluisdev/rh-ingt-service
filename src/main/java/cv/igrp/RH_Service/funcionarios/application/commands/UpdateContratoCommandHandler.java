package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.repository.*;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.ContratoMapper;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DependenteMapper;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DocumentoMapper;
import cv.igrp.RH_Service.shared.application.constants.ObjetoTipo;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cv.igrp.RH_Service.funcionarios.application.dto.ContratoResponseDTO;

@Component
public class UpdateContratoCommandHandler implements CommandHandler<UpdateContratoCommand, ResponseEntity<ContratoResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(UpdateContratoCommandHandler.class);
  private final ContratoRepository contratoRepository;
  private final FuncionarioRepository funcionarioRepository;
  private final DepartamentoRepository departamentoRepository;
  private final CargoRepository cargoRepository;

  private final ContratoMapper contratoMapper;
  private final TipoDocumentoRepository tipoDocumentoRepository;
  private final DocumentoMapper documentoMapper;
  public UpdateContratoCommandHandler(ContratoRepository contratoRepository, FuncionarioRepository funcionarioRepository, DepartamentoRepository departamentoRepository, CargoRepository cargoRepository, ContratoMapper contratoMapper, TipoDocumentoRepository tipoDocumentoRepository, DocumentoMapper documentoMapper) {

     this.contratoRepository = contratoRepository;
     this.funcionarioRepository = funcionarioRepository;
     this.departamentoRepository = departamentoRepository;
     this.cargoRepository = cargoRepository;
    this.contratoMapper = contratoMapper;
    this.tipoDocumentoRepository = tipoDocumentoRepository;
    this.documentoMapper = documentoMapper;
  }

   @IgrpCommandHandler
   public ResponseEntity<ContratoResponseDTO> handle(UpdateContratoCommand command) {

     var idContrato = ExternalID.from(command.getContratoId());
     var contrato = contratoRepository.getById(idContrato)
         .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Contrato not found with id: " + idContrato));

     var dto = command.getContratorequest();


     var idDepartamento = ExternalID.from(dto.getDepartamentoId());
     /*var existeDepartamento = departamentoRepository.existsById(idDepartamento);
     if(!existeDepartamento)
       throw IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Departament not found with id: " + idDepartamento);*/


     var idCargo  = ExternalID.from(dto.getCargoId());
    /* var existeCargo = cargoRepository.existsById(idCargo);
     if(!existeCargo)
       throw IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Cargo not found with id: " + idCargo);*/

     var departamento = departamentoRepository.getById(idDepartamento).orElseThrow(
         () -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Departament not found ")
     );

     var cargo = cargoRepository.getById(idCargo).orElseThrow(
         () -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "cargo not found ")
     );


     contrato.atualizar(
         dto.getTipoContrato(),
         dto.getSalario(),
         dto.getCargaHoraria(),
         dto.getObservacoes(),
         departamento,
         cargo,
          dto.getDataInicio(),
          dto.getDataFim()
     );

     if (dto.getAnexo() != null){
       System.out.println("handler:: "+dto.getAnexo());
       var docDto = dto.getAnexo();
       var tipoDocumento = tipoDocumentoRepository.getById(ExternalID.from(docDto.getIdTipodocumento()))
           .orElseThrow(() -> IgrpResponseStatusException.notFound("Tipo documento not found with id:: "+docDto.getIdTipodocumento()));

       var documento = documentoMapper.toDocumentoDomain(ObjetoTipo.CONTRATO, contrato.getIdContrato(), docDto, tipoDocumento);
       contrato.adicionarDocumento(documento);

     }

     contratoRepository.save(contrato);

     var responseDTO = contratoMapper.toDTO(contrato);

     return ResponseEntity.ok(responseDTO);
   }

}
