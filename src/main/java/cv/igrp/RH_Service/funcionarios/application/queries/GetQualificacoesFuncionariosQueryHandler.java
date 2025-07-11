package cv.igrp.RH_Service.funcionarios.application.queries;

import cv.igrp.RH_Service.funcionarios.application.dto.QualificacaoResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.repository.QualificacaoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.QualificacaoMapper;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetQualificacoesFuncionariosQueryHandler implements QueryHandler<GetQualificacoesFuncionariosQuery, ResponseEntity<List<QualificacaoResponseDTO>>>{

  private static final Logger LOGGER = LoggerFactory.getLogger(GetQualificacoesFuncionariosQueryHandler.class);

  private final QualificacaoRepository qualificacaoRepository;
  private final QualificacaoMapper qualificacaoMapper;

  public GetQualificacoesFuncionariosQueryHandler(QualificacaoRepository qualificacaoRepository, QualificacaoMapper qualificacaoMapper) {

    this.qualificacaoRepository = qualificacaoRepository;
    this.qualificacaoMapper = qualificacaoMapper;
  }

   @IgrpQueryHandler
  public ResponseEntity<List<QualificacaoResponseDTO>> handle(GetQualificacoesFuncionariosQuery query) {
     var funcionarioId = ExternalID.from(query.getFuncionarioId());
     var qualificacoes = qualificacaoRepository.getAllByFuncionarioExternalId(funcionarioId);

     var responseList = qualificacoes.stream()
         .map(qualificacaoMapper::toDTO)
         .toList();

     return ResponseEntity.ok(responseList);
  }

}
