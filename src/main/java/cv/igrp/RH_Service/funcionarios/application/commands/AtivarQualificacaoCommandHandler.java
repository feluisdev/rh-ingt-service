package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.repository.QualificacaoRepository;
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
public class AtivarQualificacaoCommandHandler implements CommandHandler<AtivarQualificacaoCommand, ResponseEntity<Map<String, ?>>> {

   private static final Logger LOGGER = LoggerFactory.getLogger(AtivarQualificacaoCommandHandler.class);

  private final QualificacaoRepository qualificacaoRepository;
   public AtivarQualificacaoCommandHandler(QualificacaoRepository qualificacaoRepository) {

     this.qualificacaoRepository = qualificacaoRepository;
   }

   @IgrpCommandHandler
   public ResponseEntity<Map<String, ?>> handle(AtivarQualificacaoCommand command) {
     var funcionarioId = ExternalID.from(command.getFuncionarioId());
     var qualificacaoId = ExternalID.from(command.getQualificacaoId());

     var qualificacao = qualificacaoRepository.getById(qualificacaoId)
         .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Qualificação não encontrada: " + qualificacaoId.getStringValor()));


     // Verifica se a qualificação pertence ao funcionário correto
     if (!qualificacao.getFuncionarioId().equals(funcionarioId)) {
       throw IgrpResponseStatusException.of(HttpStatus.BAD_REQUEST, "Qualificação não pertence ao funcionário informado.");
     }

     qualificacao.ativar();
     qualificacaoRepository.save(qualificacao);

     return ResponseEntity.ok(Map.of("mensagem", "Qualificação inativada com sucesso."));
   }

}
