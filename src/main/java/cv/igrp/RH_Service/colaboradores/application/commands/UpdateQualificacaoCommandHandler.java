package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.QualificacaoResponseDTO;
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
        implements CommandHandler<UpdateQualificacaoCommand, ResponseEntity<QualificacaoResponseDTO>> {

    private final QualificacaoRepository qualificacaoRepository;
    private final QualificacaoMapper mapper;

    @IgrpCommandHandler
    public ResponseEntity<QualificacaoResponseDTO> handle(UpdateQualificacaoCommand command) {
        var dto = command.getRequest();
        var id = QualificacaoId.from(command.getQualificacaoId());

        var qualificacao = qualificacaoRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Qualificação não encontrada: " + command.getQualificacaoId()));

        qualificacao.atualizar(
                dto.getLevel() != null ? dto.getLevel() : qualificacao.getLevel(),
                dto.getCourseName() != null ? dto.getCourseName() : qualificacao.getCourseName(),
                dto.getInstitution() != null ? dto.getInstitution() : qualificacao.getInstitution(),
                dto.getCountry() != null ? dto.getCountry() : qualificacao.getCountry(),
                dto.getStartDate() != null ? dto.getStartDate() : qualificacao.getStartDate(),
                dto.getEndDate() != null ? dto.getEndDate() : qualificacao.getEndDate(),
                dto.getCompleted() != null ? dto.getCompleted() : qualificacao.getCompleted()
        );

        return ResponseEntity.ok(mapper.toDTO(qualificacaoRepository.save(qualificacao)));
    }
}
