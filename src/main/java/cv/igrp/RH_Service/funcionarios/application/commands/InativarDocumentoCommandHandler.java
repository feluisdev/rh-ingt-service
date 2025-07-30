package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.models.Documento;
import cv.igrp.RH_Service.funcionarios.domain.repository.DocumentoRepository;
import cv.igrp.RH_Service.funcionarios.domain.repository.TipoDocumentoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DocumentoMapper;
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
public class InativarDocumentoCommandHandler implements CommandHandler<InativarDocumentoCommand, ResponseEntity<Map<String, ?>>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(InativarDocumentoCommandHandler.class);
  private final DocumentoRepository documentoRepository;
  private final DocumentoMapper tipoDocumentoMapper;
   public InativarDocumentoCommandHandler(DocumentoRepository documentoRepository, DocumentoMapper tipoDocumentoMapper) {
     this.documentoRepository = documentoRepository;
     this.tipoDocumentoMapper = tipoDocumentoMapper;
   }

   @IgrpCommandHandler
   public ResponseEntity<Map<String, ?>> handle(InativarDocumentoCommand command) {
      // TODO: Implement the command handling logic here
     var tipoDocumentoId = ExternalID.from(command.getDocumentoId());

     Documento tipoDocumento = documentoRepository.getByExternalId(tipoDocumentoId)
         .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Documento não encontrada: " + tipoDocumentoId.getStringValor()));


     tipoDocumento.desativar();
     documentoRepository.save(tipoDocumento);

     return ResponseEntity.ok(Map.of("mensagem", "Documento inativada com sucesso."));
   }

}
