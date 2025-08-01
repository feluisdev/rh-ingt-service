package cv.igrp.RH_Service.funcionarios.application.commands;

import cv.igrp.RH_Service.funcionarios.domain.models.Qualificacao;
import cv.igrp.RH_Service.funcionarios.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.funcionarios.domain.repository.QualificacaoRepository;
import cv.igrp.RH_Service.funcionarios.domain.repository.TipoDocumentoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DocumentoMapper;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.QualificacaoMapper;
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

import java.util.Map;

@Component
public class CreateQualificacaoCommandHandler implements CommandHandler<CreateQualificacaoCommand, ResponseEntity<Map<String, ?>>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateQualificacaoCommandHandler.class);

  private final QualificacaoRepository qualificacaoRepository;
  private final QualificacaoMapper qualificacaoMapper;
  private final FuncionarioRepository funcionarioRepository;
  private final TipoDocumentoRepository tipoDocumentoRepository;
  private final DocumentoMapper documentoMapper;


  public CreateQualificacaoCommandHandler(QualificacaoRepository qualificacaoRepository, QualificacaoMapper qualificacaoMapper, FuncionarioRepository funcionarioRepository, TipoDocumentoRepository tipoDocumentoRepository, DocumentoMapper documentoMapper) {

    this.qualificacaoRepository = qualificacaoRepository;
    this.qualificacaoMapper = qualificacaoMapper;
    this.funcionarioRepository = funcionarioRepository;
    this.tipoDocumentoRepository = tipoDocumentoRepository;
    this.documentoMapper = documentoMapper;
  }

  @IgrpCommandHandler
  public ResponseEntity<Map<String, ?>> handle(CreateQualificacaoCommand command) {
    var funcionarioId = ExternalID.from(command.getFuncionarioId());

    var funcionario = funcionarioRepository.getById(funcionarioId)
        .orElseThrow(() -> IgrpResponseStatusException.notFound("Funcionário não encontrado: " + funcionarioId));

    var dto = command.getQualificacaorequest();

    var qualificacao = Qualificacao.criar(
        dto.getInstituicao(),
        dto.getCurso(),
        dto.getDataInicio(),
        dto.getDataConclusao(),
        dto.getNivel(),
        dto.getSituacao(),
        dto.getCargaHoraria(),
        dto.getNotaFinal(),
        funcionario
    );

    if (dto.getAnexo() != null){
      var docDto = dto.getAnexo();
      var tipoDocumento = tipoDocumentoRepository.getById(ExternalID.from(docDto.getIdTipodocumento()))
          .orElseThrow(() -> IgrpResponseStatusException.notFound("Tipo documento not found with id:: "+docDto.getIdTipodocumento()));

      var documento = documentoMapper.toDocumentoDomain(ObjetoTipo.QUALIFICACAO, qualificacao.getIdQualificacao(), docDto, tipoDocumento);
      qualificacao.adicionarDocumento(documento);

    }

    var saved = qualificacaoRepository.save(qualificacao);

    Map<String, Object> response = Map.of("qualificacaoId", saved.getIdQualificacao().getStringValor());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);

  }

}
