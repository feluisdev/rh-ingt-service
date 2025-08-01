package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.application.dto.QualificacaoResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.repository.QualificacaoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.QualificacaoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class UpdateQualificacaoCommandHandler implements CommandHandler<UpdateQualificacaoCommand, ResponseEntity<QualificacaoResponseDTO>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(UpdateQualificacaoCommandHandler.class);

  private final QualificacaoRepository qualificacaoRepository;
  private final QualificacaoMapper qualificacaoMapper;

  public UpdateQualificacaoCommandHandler(QualificacaoRepository qualificacaoRepository, QualificacaoMapper qualificacaoMapper) {

    this.qualificacaoRepository = qualificacaoRepository;
    this.qualificacaoMapper = qualificacaoMapper;
  }

   @IgrpCommandHandler
   public ResponseEntity<QualificacaoResponseDTO> handle(UpdateQualificacaoCommand command) {
     var funcionarioId = ExternalID.from(command.getFuncionarioId());
     var qualificacaoId = ExternalID.from(command.getQualificacaoId());

     var dto = command.getQualificacaorequest();

     var qualificacao = qualificacaoRepository.getByExternalId(qualificacaoId)
         .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Qualificação não encontrada: " + qualificacaoId.getStringValor()));

     if (!qualificacao.getFuncionario().getIdFuncionario().equals(funcionarioId)) {
       throw IgrpResponseStatusException.of(HttpStatus.BAD_REQUEST, "Qualificação não pertence ao funcionário informado.");
     }

     // Atualiza os campos
     qualificacao.atualizar(
         dto.getInstituicao(),
         dto.getCurso(),
         dto.getDataInicio(),
         dto.getDataConclusao(),
         dto.getNivel(),
         dto.getSituacao(),
         dto.getCargaHoraria(),
         dto.getNotaFinal()
     );

     var atualizado = qualificacaoRepository.save(qualificacao);


     var response = qualificacaoMapper.toDTO(atualizado);
     return ResponseEntity.ok(response);
   }

}
