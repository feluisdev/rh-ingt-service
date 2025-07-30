package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.application.dto.TipoDocumentoResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.repository.TipoDocumentoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.TipoDocumentoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class UpdateTipoDocumentoCommandHandler implements CommandHandler<UpdateTipoDocumentoCommand, ResponseEntity<TipoDocumentoResponseDTO>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateTipoDocumentoCommandHandler.class);
  private final TipoDocumentoRepository tipoDocumentoRepository;
  private final TipoDocumentoMapper tipoDocumentoMapper;

  public UpdateTipoDocumentoCommandHandler(TipoDocumentoRepository tipoDocumentoRepository, TipoDocumentoMapper cargoMapper) {
    this.tipoDocumentoRepository = tipoDocumentoRepository;
    this.tipoDocumentoMapper = cargoMapper;
  }

  @IgrpCommandHandler
  public ResponseEntity<TipoDocumentoResponseDTO> handle(UpdateTipoDocumentoCommand command) {
    // TODO: Implement the command handling logic here
    var dto = command.getTipodocumentorequest();
    var tipoDocumentoId = ExternalID.from(command.getTipoDocumentoId());

    var tipoDocumento = tipoDocumentoRepository.getByExternalId(tipoDocumentoId).orElseThrow(

        () -> IgrpResponseStatusException.notFound("Cargo not found with ID: " + tipoDocumentoId.getStringValor())
    );

    tipoDocumento.atualizar(
        dto.getDescrcicao(),
        dto.getCodigo()
    );

    var salvo = tipoDocumentoRepository.save(tipoDocumento);

    var responseDto = tipoDocumentoMapper.toDto(salvo);

    return ResponseEntity.ok(responseDto);
  }

}
