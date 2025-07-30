package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.repository.TipoDocumentoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.TipoDocumentoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


@Component
public class InativarTipoDocumentoCommandHandler implements CommandHandler<InativarTipoDocumentoCommand, ResponseEntity<Map<String, ?>>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(InativarTipoDocumentoCommandHandler.class);
  private final TipoDocumentoRepository tipoDocumentoRepository;
  private final TipoDocumentoMapper tipoDocumentoMapper;

  public InativarTipoDocumentoCommandHandler(TipoDocumentoRepository tipoDocumentoRepository, TipoDocumentoMapper cargoMapper) {
    this.tipoDocumentoRepository = tipoDocumentoRepository;
    this.tipoDocumentoMapper = cargoMapper;
  }


   @IgrpCommandHandler
   public ResponseEntity<Map<String, ?>> handle(InativarTipoDocumentoCommand command) {
      // TODO: Implement the command handling logic here
     var tipoDocumentoId = ExternalID.from(command.getTipoDocumentoId());

     var tipoDocumento = tipoDocumentoRepository.getByExternalId(tipoDocumentoId)
         .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Qualificação não encontrada: " + tipoDocumentoId.getStringValor()));


     tipoDocumento.inativar();
     tipoDocumentoRepository.save(tipoDocumento);

     return ResponseEntity.ok(Map.of("mensagem", "Tipo de Documento inativada com sucesso."));
   }

}
