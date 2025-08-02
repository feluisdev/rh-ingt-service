package cv.igrp.RH_Service.funcionarios.application.queries;

import cv.igrp.RH_Service.funcionarios.application.dto.QualificacaoResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.repository.QualificacaoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.QualificacaoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class GetQualificacaoQueryHandler implements QueryHandler<GetQualificacaoQuery, ResponseEntity<QualificacaoResponseDTO>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetQualificacaoQueryHandler.class);


  private final QualificacaoRepository qualificacaoRepository;
  private final QualificacaoMapper qualificacaoMapper;

  public GetQualificacaoQueryHandler(QualificacaoRepository qualificacaoRepository, QualificacaoMapper qualificacaoMapper) {

    this.qualificacaoRepository = qualificacaoRepository;
    this.qualificacaoMapper = qualificacaoMapper;

  }

   @IgrpQueryHandler
  public ResponseEntity<QualificacaoResponseDTO> handle(GetQualificacaoQuery query) {
     var qualificacaoId = ExternalID.from(query.getQualificacaoId());

     var qualificacao = qualificacaoRepository.getById(qualificacaoId)
         .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND, "Qualificação não encontrada: " + qualificacaoId.getStringValor()));

     var responseDTO = qualificacaoMapper.toDTO(qualificacao);
     return ResponseEntity.ok(responseDTO);
  }

}
