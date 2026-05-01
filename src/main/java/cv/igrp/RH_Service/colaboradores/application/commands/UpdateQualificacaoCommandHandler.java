package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.QualificacaoResponse;
import cv.igrp.RH_Service.colaboradores.domain.repository.QualificacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.QualificacaoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.QualificacaoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsUpdateQualificacaoCommandHandler")
@RequiredArgsConstructor
public class UpdateQualificacaoCommandHandler
        implements CommandHandler<UpdateQualificacaoCommand, ResponseEntity<QualificacaoResponse>> {

    private final QualificacaoRepository qualificacaoRepository;
    private final QualificacaoMapper mapper;

    @IgrpCommandHandler
    public ResponseEntity<QualificacaoResponse> handle(UpdateQualificacaoCommand command) {
        var dto = command.getRequest();
        var id = QualificacaoId.from(command.getQualificacaoId());

        var qualificacao = qualificacaoRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Qualificação não encontrada: " + command.getQualificacaoId()));

        qualificacao.atualizar(
                dto.getNivelAcademico() != null ? dto.getNivelAcademico() : qualificacao.getNivelAcademico(),
                dto.getCurso() != null ? dto.getCurso() : qualificacao.getCurso(),
                dto.getInstituicao() != null ? dto.getInstituicao() : qualificacao.getInstituicao(),
                dto.getAnoConclusao() != null ? dto.getAnoConclusao() : qualificacao.getAnoConclusao(),
                dto.getPais() != null ? dto.getPais() : qualificacao.getPais()
        );

        return ResponseEntity.ok(mapper.toDTO(qualificacaoRepository.save(qualificacao)));
    }
}
